/*
 * Copyright (C) 2016 essobedo.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.gitlab.essobedo.appma.core;

import com.gitlab.essobedo.appma.core.config.ConfigFromProperties;
import com.gitlab.essobedo.appma.core.config.ConfigurationFactory;
import com.gitlab.essobedo.appma.core.io.Folder;
import com.gitlab.essobedo.appma.core.progress.LogProgress;
import com.gitlab.essobedo.appma.core.progress.StatusBar;
import com.gitlab.essobedo.appma.core.zip.UnzipTask;
import com.gitlab.essobedo.appma.exception.ApplicationException;
import com.gitlab.essobedo.appma.exception.TaskInterruptedException;
import com.gitlab.essobedo.appma.spi.Manageable;
import com.gitlab.essobedo.appma.spi.VersionManager;
import com.gitlab.essobedo.appma.task.Task;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * The default implementation of {@link ApplicationManager}.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
class DefaultApplicationManager implements ApplicationManager {

    /**
     * The logger of the class.
     */
    private static final Logger LOG = Logger.getLogger(DefaultApplicationManager.class.getName());
    /**
     * The format of the error message to use in case of an illegal state while upgrading.
     */
    private static final String COULD_NOT_UPGRADE_ILLEGAL_STATE =
        "Could not upgrade the application as the state is illegal: %s";

    /**
     * The arguments to pass to the application on initialization.
     */
    private final String[] arguments;

    /**
     * The root directory of the application.
     */
    private final File root;

    /**
     * The file in which the compressed content of the patch should be stored in case of an upgrade.
     */
    private final File patchTargetFile;

    /**
     * The folder in which the uncompressed content of the patch should be stored in case of an upgrade.
     */
    private final File patchContentTargetFolder;

    /**
     * The configuration of the application manager.
     */
    private Configuration configuration;

    /**
     * The current application.
     */
    private Manageable application;

    /**
     * The current state of the application.
     */
    private final AtomicReference<ApplicationState> state = new AtomicReference<>(ApplicationState.DESTROYED);

    /**
     * The executor used to execute all the asynchronous tasks.
     */
    private final AsyncTaskExecutor executor = new AsyncTaskExecutor();

    /**
     * The current stage.
     */
    private Stage stage;

    /**
     * Constructs a {@code DefaultApplicationManager} with the specified root folder and arguments.
     * @param root the root folder of the application.
     * @param arguments the arguments to pass to the application on initialization.
     * @throws ApplicationException The configuration could not be loaded.
     */
    DefaultApplicationManager(final File root, final String... arguments) throws ApplicationException {
        this(root, null, null, arguments);
    }

    /**
     * Constructs a {@code DefaultApplicationManager} with the specified root folder, patch file,
     * patch content folder and arguments.
     *
     * <p><i>This constructor is for testing purpose only</i>
     * @param root the root folder of the application.
     * @param patchTargetFile The patch file to use in case of an upgrade.
     * @param patchContentTargetFolder the folder to use to store the content of the patch in case
     * of an upgrade.
     * @param arguments the arguments to pass to the application on initialization.
     * @throws ApplicationException The configuration could not be loaded.
     */
    DefaultApplicationManager(final File root, final File patchTargetFile,
        final File patchContentTargetFolder, final String... arguments) throws ApplicationException {
        this.root = root;
        this.arguments = arguments;
        this.patchTargetFile = patchTargetFile;
        this.patchContentTargetFolder = patchContentTargetFolder;
        loadConfiguration();
    }

    /**
     * Loads the configuration from the root directory.
     * @throws ApplicationException If the configuration could not be loaded.
     */
    private void loadConfiguration() throws ApplicationException {
        final ConfigurationFactory factory = new ConfigurationFactory(root);
        setConfiguration(factory.create());
    }

    /**
     * Persists if needed the provided configuration and reloads the configuration from the
     * root directory.
     * @param configuration The configuration to store if needed.
     * @throws ApplicationException If the configuration could not be re-loaded.
     */
    private void reload(final Configuration configuration) throws ApplicationException {
        final String configurationName = ConfigurationFactory.getConfigurationName();
        final File configFile = new File(root, configurationName);
        if (configuration == null) {
            if (configFile.exists() && !configFile.delete() && LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, String.format("The file '%s' could not be deleted",
                    configFile.getAbsolutePath()));
            }
            loadConfiguration();
        } else {
            try {
                ConfigFromProperties.store(configuration, configFile);
                setConfiguration(configuration);
            } catch (IOException e) {
                if (LOG.isLoggable(Level.SEVERE)) {
                    LOG.log(Level.SEVERE, "The configuration could not be stored", e);
                }
                loadConfiguration();
            }
        }
    }

    /**
     * Sets the new configuration.
     * @param configuration The new configuration.
     */
    private void setConfiguration(final Configuration configuration) {
        synchronized (this) {
            this.configuration = configuration;
        }
    }

    /**
     * Gives the configuration of the application manager.
     * @return the configuration of the application manager.
     */
    private Configuration getConfiguration() {
        synchronized (this) {
            return configuration;
        }
    }

    /**
     * Gives the current application.
     * @return the current application.
     */
    Manageable getApplication() {
        synchronized (this) {
            return application;
        }
    }

    @Override
    public Stage getStage() {
        synchronized (this) {
            return stage;
        }
    }

    /**
     * Creates the application.
     * @return The application that has been created by the application manager.
     * @throws ApplicationException if the application could not be created.
     */
    protected Manageable create() throws ApplicationException {
        if (!state.compareAndSet(ApplicationState.DESTROYED, ApplicationState.CREATING)) {
            throw new ApplicationException(String.format(
                "Could not create the application as the state is illegal: %s", state.get()));
        }

        final ClassLoader classLoader = getClassLoader(getConfiguration());
        final ServiceLoader<Manageable> loader = ServiceLoader.load(Manageable.class, classLoader);
        final Iterator<Manageable> iterator = loader.iterator();
        Manageable application = null;
        while (iterator.hasNext()) {
            final Manageable app = iterator.next();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, String.format("The application '%s' version '%s' has ben found", app.name(),
                    app.version()));
            }
            if (app.accept(arguments)) {
                application = app;
                break;
            } else if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, String.format(
                    "The application '%s' version '%s' is not compatible with the arguments '%s'",
                    app.name(), app.version(), Arrays.toString(arguments)));
            }
        }
        if (application == null) {
            throw new ApplicationException("Could not find any compliant application");
        }
        synchronized (this) {
            this.application = application;
        }
        state.set(ApplicationState.CREATED);
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, String.format("The application '%s' version '%s' has ben found", application.name(),
                application.version()));
        }
        return application;
    }

    /**
     * Initializes the application.
     * @return The {@link Scene} of the initialized application in case of a Java FX application
     * or {@code null} otherwise.
     * @throws ApplicationException if the application could not be created.
     */
    protected Scene init() throws ApplicationException {
        if (!state.compareAndSet(ApplicationState.CREATED, ApplicationState.INITIALIZING)) {
            throw new ApplicationException(String.format(
                "Could not init the application as the state is illegal: %s", state.get()));
        }

        final Manageable application = getApplication();
        final ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        final Scene scene;
        try {
            Thread.currentThread().setContextClassLoader(application.getClass().getClassLoader());
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, String.format("Init the application '%s' version '%s'", application.name(),
                    application.version()));
            }
            scene = application.init(this, arguments);
            state.set(ApplicationState.INITIALIZED);

        } catch (ApplicationException e) {
            state.set(ApplicationState.UNKNOWN);
            throw e;
        } catch (RuntimeException e) {
            state.set(ApplicationState.UNKNOWN);
            throw new ApplicationException("Could not init the application", e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextCL);
        }
        return scene;
    }

    /**
     * Creates the {@link ClassLoader} corresponding to the specified {@link Configuration}.
     * @param configuration The configuration to use to create the {@link ClassLoader}.
     * @return The {@link ClassLoader} corresponding to the specified {@link Configuration}.
     * @throws ApplicationException if the {@link ClassLoader} could not be created.
     */
    private ClassLoader getClassLoader(final Configuration configuration) throws ApplicationException {
        final URL[] urls = configuration.getClasspathAsUrls();
        return new URLClassLoader(urls, getClass().getClassLoader());
    }

    /**
     * Destroys the application.
     * @throws ApplicationException if the application could not be created.
     */
    protected void destroy() throws ApplicationException {
        if (!state.compareAndSet(ApplicationState.INITIALIZED, ApplicationState.DESTROYING)) {
            throw new ApplicationException(String.format(
                "Could not destroy the application as the state is illegal: %s", state.get()));
        }
        final Manageable application = getApplication();
        final ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(application.getClass().getClassLoader());
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, String.format("Destroy the application '%s' version '%s'", application.name(),
                    application.version()));
            }
            if (getStage() != null && application.icon() != null) {
                Platform.runLater(() -> getStage().getIcons().removeAll(application.icon()));
            }
            application.destroy();
            synchronized (this) {
                this.application = null;
            }
            state.set(ApplicationState.DESTROYED);

        } catch (ApplicationException e) {
            state.set(ApplicationState.UNKNOWN);
            throw e;
        } catch (RuntimeException e) {
            state.set(ApplicationState.UNKNOWN);
            throw new ApplicationException("Could not destroy the application", e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextCL);
        }
    }

    @Override
    public Task<String> checkForUpdate() throws ApplicationException {
        final Manageable application = getApplication();
        if (application == null) {
            throw new ApplicationException("Could not check for update as there is no application running");
        }
        final ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(application.getClass().getClassLoader());
            final VersionManager versionManager = getVersionManager(application.getClass().getName(),
                application.getClass().getClassLoader());
            if (versionManager == null) {
                throw new ApplicationException("No version manager could be found");
            }
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, String.format("Checking for update for the application '%s' version '%s'",
                    application.name(),
                    application.version()));
            }
            return versionManager.check(application);
        } catch (ApplicationException e) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, "Could not check for update", e);
            }
            throw e;
        } finally {
            Thread.currentThread().setContextClassLoader(contextCL);
        }
    }

    @Override
    public Future<Void> upgrade() {
        final Callable<Void> task = () -> {
            try {
                doUpgrade();
            } catch (ApplicationException e) {
                if (LOG.isLoggable(Level.SEVERE)) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                }
                exit();
                throw e;
            }
            return null;
        };
        final FutureTask<Void> future = new FutureTask<>(task);
        executor.execute(future);
        return future;
    }

    /**
     * Upgrades the application.
     * @throws ApplicationException if the application could not be upgraded.
     */
    void doUpgrade() throws ApplicationException {
        if (state.get() != ApplicationState.INITIALIZED) {
            throw new ApplicationException(String.format(COULD_NOT_UPGRADE_ILLEGAL_STATE, state.get()));
        }
        final Manageable application = getApplication();
        if (application == null) {
            throw new ApplicationException(String.format(COULD_NOT_UPGRADE_ILLEGAL_STATE, state.get()));
        }
        final String className = application.getClass().getName();
        final VersionManager versionManager = getVersionManager(className,
                                                                application.getClass().getClassLoader());
        if (versionManager == null) {
            throw new ApplicationException("No version manager could be found");
        }
        final File patchFolder = getPatchContent(application, versionManager);
        final String oldVersion = application.version();
        destroy();
        applyNShow(className, patchFolder, oldVersion);
    }

    /**
     * Applies the patch and launches the upgraded application.
     * @param className the name of the class of the application to upgrade.
     * @param patchFolder the folder that contains the content of the patch.
     * @param oldVersion the previous version of the application.
     * @throws ApplicationException In case an error occurs.
     */
    private void applyNShow(final String className, final File patchFolder, final String oldVersion)
                            throws ApplicationException {
        if (!state.compareAndSet(ApplicationState.DESTROYED, ApplicationState.UPGRADING)) {
            throw new ApplicationException(String.format(COULD_NOT_UPGRADE_ILLEGAL_STATE, state.get()));
        }
        if (patchFolder == null || !applyPatch(className, patchFolder, oldVersion)) {
            return;
        }
        if (!state.compareAndSet(ApplicationState.UPGRADING, ApplicationState.DESTROYED)) {
            throw new ApplicationException(String.format(COULD_NOT_UPGRADE_ILLEGAL_STATE, state.get()));
        }
        create();
        if (getStage() != null && getApplication().icon() != null) {
            Platform.runLater(() -> getStage().getIcons().add(getApplication().icon()));
        }
        initNShow();
    }

    /**
     * Triggers an initialization of the application. It will be done asynchronously.
     * @param stage the stage to use to initialize the application.
     * @param callbackOnError callback to use in case of an error.
     * @return The {@link Future} object allowing to be notified once the task is over.
     */
    Future<Void> asyncInitNShow(final Stage stage, final Runnable callbackOnError) {
        final Callable<Void> task = () -> {
            try {
                synchronized (this) {
                    this.stage = stage;
                }
                initNShow();
            } catch (ApplicationException e) {
                if (callbackOnError != null) {
                    Platform.runLater(callbackOnError::run);
                }
                if (LOG.isLoggable(Level.SEVERE)) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                }
                throw e;
            }
            return null;
        };
        final FutureTask<Void> future = new FutureTask<>(task);
        executor.execute(future);
        return future;
    }

    /**
     * Initializes and shows the application.
     * @throws ApplicationException in case the application could not be initialized.
     */
    private void initNShow() throws ApplicationException {
        final Scene scene = init();
        if (getStage() != null) {
            Platform.runLater(() -> showApplication(scene));
        }
    }

    /**
     * Shows the application in the middle of the screen.
     * @param scene the scene to display in the middle of the screen.
     */
    private void showApplication(final Scene scene) {
        final Stage primaryStage = getStage();
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        final Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
        primaryStage.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);
    }

    /**
     * Applies the patch.
     * @param className The name of the application to upgrade.
     * @param patchFolder the folder containing the content of the patch.
     * @param oldVersion the previous version of the application.
     * @return {@code true} if the patch could be applied, {@code false} otherwise.
     * @throws ApplicationException in case the patch could not be applied.
     */
    private boolean applyPatch(final String className, final File patchFolder,
                               final String oldVersion) throws ApplicationException {
        final ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        try {
            final ConfigurationFactory factory = new ConfigurationFactory(patchFolder);
            final Configuration config = factory.create();
            final ClassLoader classLoader = getClassLoader(config);
            Thread.currentThread().setContextClassLoader(classLoader);
            final VersionManager versionManager = getVersionManager(className, classLoader);
            if (versionManager == null) {
                throw new ApplicationException("No version manager could be found");
            }
            final Configuration configuration = executeTask("Applying the patch",
                ((VersionManager<?>) versionManager).upgrade(patchFolder, root, oldVersion));
            reload(configuration);
        } catch (TaskInterruptedException e) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "The task has been interrupted", e);
            }
            exit();
            return false;
        } catch (ApplicationException e) {
            state.set(ApplicationState.UNKNOWN);
            throw e;
        } catch (RuntimeException e) {
            state.set(ApplicationState.UNKNOWN);
            throw new ApplicationException("Could not upgrade the application", e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextCL);
            final Folder folder = new Folder(patchFolder);
            folder.delete();
        }
        return true;
    }

    /**
     * Gets the content of the patch and stores it into a folder.
     * @param application the application for which we want to get the patch.
     * @param versionManager the version manager to use to get the content of the patch.
     * @return a {@code File} corresponding to the folder that contains the content of the patch.
     * @throws ApplicationException if the content of the patch could not be retrieved.
     */
    private File getPatchContent(final Manageable application, final VersionManager versionManager)
                                 throws ApplicationException {
        File destFolder;
        final ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        File file2Delete = null;
        try {
            Thread.currentThread().setContextClassLoader(application.getClass().getClassLoader());
            final File zipFile = getPatchTargetFile();
            file2Delete = zipFile;
            try (OutputStream out = new FileOutputStream(zipFile)) {
                executeTask(String.format("Getting the new version of the application '%s'",
                    application.name()), versionManager.store(application, out));
            }
            destFolder = getPatchContentTargetFolder();
            final Task<Void> unzip = new UnzipTask(zipFile, destFolder);
            executeTask("Unzipping the patch", unzip);
        } catch (TaskInterruptedException e) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "The task has been interrupted", e);
            }
            exit();
            destFolder = null;
        } catch (ApplicationException e) {
            state.set(ApplicationState.UNKNOWN);
            throw e;
        } catch (RuntimeException | IOException e) {
            state.set(ApplicationState.UNKNOWN);
            throw new ApplicationException("Could not upgrade the application", e);
        } finally {
            if (file2Delete != null && !file2Delete.delete() && LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, String.format("The file '%s' could not be deleted",
                    file2Delete.getAbsolutePath()));
            }
            Thread.currentThread().setContextClassLoader(contextCL);
        }
        return destFolder;
    }

    /**
     * Gives the folder that will contain the content of the patch.
     * @return the folder that will contain the content of the patch.
     * @throws IOException in case the temporary directory could not be created.
     */
    private File getPatchContentTargetFolder() throws IOException {
        if (patchContentTargetFolder == null) {
            return new File(Files.createTempDirectory("upgrade").toString());
        } else {
            return patchContentTargetFolder;
        }
    }

    /**
     * Gives the file that will contain the patch.
     * @return the file that will contain the patch.
     * @throws IOException in case the temporary file could not be created.
     */
    private File getPatchTargetFile() throws IOException {
        if (patchTargetFile == null) {
            return File.createTempFile("upgrade", "tmp");
        } else {
            return patchTargetFile;
        }
    }

    /**
     * Executes the specified task and use {@link LogProgress} or {@link StatusBar} to
     * provide information about how the task is progressing. If the application is
     * a Java FX application, it will use the {@link StatusBar} otherwise it will use the
     * {@link LogProgress}.
     * @param messageInfo the info message to log before executing the task.
     * @param task the task to execute.
     * @param <T> the return type of the task to execute.
     * @return The result of the task
     * @throws ApplicationException if the task fails.
     * @throws TaskInterruptedException if the task has been interrupted.
     */
    private <T> T executeTask(final String messageInfo, final Task<T> task)
                                throws ApplicationException, TaskInterruptedException {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, messageInfo);
        }
        if (getStage() == null) {
            new LogProgress(task);
        } else {
            final StatusBar bar = new StatusBar(task);
            final Scene scene = new Scene(bar, 300.0d, 150.0d);

            Platform.runLater(() ->  showStatusWindow(scene));
        }
        return task.execute();
    }

    /**
     * Show the window providing the status of a task in the middle of the screen.
     * @param scene the window to display in the middle of the screen.
     */
    private void showStatusWindow(final Scene scene) {
        final Stage primaryStage = getStage();
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(Event::consume);
        primaryStage.setScene(scene);
        final Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
        primaryStage.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);
    }

    /**
     * Gives the version manager that could be found using the given class loader and that matches
     * with the specified full qualified name of the application.
     * @param className the full qualified name of the application for which we look for a version manager.
     * @param classLoader the classloader to use to find the version manager.
     * @return the version manager that matches with the specified criteria, {@code null} if none could
     * be found.
     * @throws ApplicationException if an error occurs while looking for a version manager.
     */
    private VersionManager<?> getVersionManager(final String className, final ClassLoader classLoader)
        throws ApplicationException {
        final ServiceLoader<VersionManager> loader = ServiceLoader.load(VersionManager.class, classLoader);
        for (final VersionManager versionManager : loader) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, String.format("The version manager '%s' has ben found",
                    versionManager.getClass().getName()));
                LOG.log(Level.FINE, String.format("The version manager '%s' has '%s' generic interfaces",
                    versionManager.getClass().getName(), versionManager.getClass().getGenericInterfaces().length));
                LOG.log(Level.FINE, String.format("The version manager '%s' has '%s' as generic super class",
                    versionManager.getClass().getName(), versionManager.getClass().getGenericSuperclass()));
            }
            if (accept(className, classLoader, versionManager)) {
                return versionManager;
            }
        }
        return null;
    }

    /**
     * Indicates whether the given {@link VersionManager} matches with the specified criteria.
     * @param className the full qualified name of the application for which we want a version manager.
     * @param classLoader the classloader to use to check the version manager.
     * @param versionManager the version manager to check.
     * @return {@code true} if the version manager matches, {@code false} otherwise.
     * @throws ApplicationException if the version manager could not be checked.
     */
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private boolean accept(final String className, final ClassLoader classLoader,
                           final VersionManager versionManager) throws ApplicationException {

        final Type[] types = getTypes(versionManager);
        if (types.length == 1) {
            final ParameterizedType type;
            if (types[0] instanceof ParameterizedType) {
                type = (ParameterizedType) types[0];
            } else {
                return true;
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, String.format("The version manager '%s' has '%s' type arguments",
                    versionManager.getClass().getName(), type.getActualTypeArguments().length));
            }
            if (type.getActualTypeArguments().length == 1) {
                final Class<?> typeClass = (Class<?>) type.getActualTypeArguments()[0];
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, String.format("The version manager '%s' is for the type '%s'",
                        versionManager.getClass().getName(), typeClass));
                }

                try {
                    return typeClass.isAssignableFrom(Class.forName(className, false, classLoader));
                } catch (ClassNotFoundException e) {
                    throw new ApplicationException(String.format("Could not find the class '%s'", className), e);
                }
            }
        }
        return false;
    }

    /**
     * Gives the generic types of the specified version manager.
     * @param versionManager the version manager for which we want the generic types.
     * @return the generic types of the provided version manager.
     */
    private Type[] getTypes(final VersionManager versionManager) {
        final Class<?> versionManagerClass = versionManager.getClass();
        if (versionManagerClass.getGenericInterfaces().length == 0) {
            if (versionManagerClass.getGenericSuperclass() == null) {
                return new Type[]{};
            } else {
                return new Type[]{versionManagerClass.getGenericSuperclass()};
            }
        } else {
            return versionManagerClass.getGenericInterfaces();
        }
    }

    /**
     * Exit the whole application properly.
     */
    private void exit() {
        executor.stop();
        if (getStage() != null) {
            Platform.runLater(() -> {
                getStage().close();
                Platform.exit();
            });
        }
    }

    @Override
    public void onExit() {
        try {
            destroy();
        } catch (ApplicationException e) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "Could not destroy the application on exit.", e);
            }
        }
        exit();
    }
}
