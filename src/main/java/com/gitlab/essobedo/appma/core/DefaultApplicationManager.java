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
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
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
     * The arguments that have
     */
    private final String[] arguments;

    /**
     * The root directory of the application
     */
    private final File root;

    /**
     * The file in which the compressed content of the patch should be stored in case of an upgrade
     */
    private final File patchTargetFile;

    /**
     * The folder in which the uncompressed content of the patch should be stored in case of an upgrade
     */
    private final File patchContentTargetFolder;

    /**
     * The configuration file of the launcher
     */
    private Configuration configuration;

    /**
     * The current application
     */
    private Manageable application;

    /**
     * The current state of the application
     */
    private final AtomicReference<ApplicationState> state = new AtomicReference<>(ApplicationState.DESTROYED);

    /**
     * The executor used to execute all the asynchronous tasks
     */
    private final AsyncTaskExecutor executor = new AsyncTaskExecutor();

    /**
     * The current stage
     */
    private Stage stage;

    public DefaultApplicationManager(final File root, final String[] arguments) throws ApplicationException {
        this(root, arguments, null, null);
    }

    DefaultApplicationManager(final File root, final String[] arguments,
        final File patchTargetFile, final File patchContentTargetFolder) throws ApplicationException {
        this.root = root;
        this.arguments = arguments;
        this.patchTargetFile = patchTargetFile;
        this.patchContentTargetFolder = patchContentTargetFolder;
        loadConfiguration();
    }

    private void loadConfiguration() throws ApplicationException {
        final ConfigurationFactory factory = new ConfigurationFactory(root);
        setConfiguration(factory.create());
    }

    private void reload(final Configuration configuration) throws ApplicationException {
        if (configuration == null) {
            loadConfiguration();
        } else {
            final String configurationName = ConfigurationFactory.getConfigurationName();
            final File configFile = new File(root, configurationName);
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

    private void setConfiguration(final Configuration configuration) {
        synchronized(this) {
            this.configuration = configuration;
        }
    }

    AsyncTaskExecutor getExecutor() {
        return executor;
    }

    Configuration getConfiguration() {
        synchronized(this) {
            return configuration;
        }
    }

    void setStage(final Stage stage) {
        this.stage = stage;
    }

    Manageable getApplication() {
        synchronized(this) {
            return application;
        }
    }

    @Override
    public Stage getStage() {
        return stage;
    }

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

    private ClassLoader getClassLoader(final Configuration configuration) throws ApplicationException {
        final URL[] urls = configuration.getClasspathAsUrls();
        return new URLClassLoader(urls, getClass().getClassLoader());
    }

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
    public Task<String> checkForUpdate() {
        final Manageable application = getApplication();
        if (application == null) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, "Could not check for update as there is no application running");
            }
            return null;
        }
        final ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(application.getClass().getClassLoader());
            final VersionManager versionManager = getVersionManager(application.getClass().getName(),
                application.getClass().getClassLoader());
            if (versionManager == null) {
                if (LOG.isLoggable(Level.SEVERE)) {
                    LOG.log(Level.SEVERE, "No version manager could be found");
                }
                return null;
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
        } finally {
            Thread.currentThread().setContextClassLoader(contextCL);
        }
        return null;
    }

    @Override
    public boolean upgrade() {
        if (state.get() != ApplicationState.INITIALIZED) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, String.format(
                    "Could not upgrade the application as the state is illegal: %s", state.get()));
            }
            return false;
        }
        final Runnable runnable = () -> {
            try {
                doUpgrade();
            } catch (ApplicationException e) {
                if (LOG.isLoggable(Level.SEVERE)) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                }
                exit();
            }
        };
        executor.execute(runnable);
        return true;
    }

    void doUpgrade() throws ApplicationException {
        if (state.get() != ApplicationState.INITIALIZED) {
            throw new ApplicationException(String.format(
                "Could not upgrade the application as the state is illegal: %s", state.get()));
        }
        Manageable application = getApplication();
        if (application == null) {
            throw new ApplicationException(String.format(
                "Could not upgrade the application as the state is illegal: %s", state.get()));
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
        application = null;
        if (!state.compareAndSet(ApplicationState.DESTROYED, ApplicationState.UPGRADING)) {
            throw new ApplicationException(String.format(
                "Could not upgrade the application as the state is illegal: %s", state.get()));
        }
        if (patchFolder == null || !applyPatch(className, patchFolder, oldVersion)) {
            return;
        }
        if (!state.compareAndSet(ApplicationState.UPGRADING, ApplicationState.DESTROYED)) {
            throw new ApplicationException(String.format(
                "Could not upgrade the application as the state is illegal: %s", state.get()));
        }
        create();
        if (getStage() != null && getApplication().icon() != null) {
            Platform.runLater(() -> getStage().getIcons().add(getApplication().icon()));
        }
        initNShow();
    }

    void initNShow() throws ApplicationException {
        final Scene scene = init();
        if (getStage() != null) {
            Platform.runLater(() -> showApplication(scene));
        }
    }

    private void showApplication(final Scene scene) {
        final Stage primaryStage = getStage();
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        final Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
        primaryStage.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);
    }

    private boolean applyPatch(final String className, final File patchFolder,
                               final String oldVersion) throws ApplicationException {
        final ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        try {
            final ConfigurationFactory factory = new ConfigurationFactory(patchFolder);
            final Configuration config = factory.create();
            final ClassLoader cl = getClassLoader(config);
            Thread.currentThread().setContextClassLoader(cl);
            final VersionManager versionManager = getVersionManager(className, cl);
            if (versionManager == null) {
                throw new ApplicationException("No version manager could be found");
            } else if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, "Applying the patch");
            }
            final Configuration configuration = executeTask(
                ((VersionManager<?>)versionManager).upgrade(patchFolder, root, oldVersion));
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

    private File getPatchContent(final Manageable application, final VersionManager versionManager) throws ApplicationException {
        File destFolder;
        final ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        File file2Delete = null;
        try {
            Thread.currentThread().setContextClassLoader(application.getClass().getClassLoader());
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, String.format("Getting the new version of the application '%s' version '%s'",
                    application.name(),
                    application.version()));
            }
            final File zipFile = patchTargetFile == null ?
                File.createTempFile("upgrade", application.name()) : patchTargetFile;
            file2Delete = zipFile;
            try (OutputStream out = new FileOutputStream(zipFile)) {
                executeTask(versionManager.store(application, out));
            }
            destFolder = patchContentTargetFolder == null ?
                new File(Files.createTempDirectory("upgrade").toString()) : patchContentTargetFolder;
            final Task<Void> unzip = new UnzipTask(zipFile, destFolder);
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, "Unzipping the patch");
            }
            executeTask(unzip);
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

    private <T> T executeTask(final Task<T> task) throws ApplicationException, TaskInterruptedException {
        if (getStage() == null) {
            new LogProgress(task);
        } else {
            final StatusBar bar = new StatusBar(task);
            final Scene scene = new Scene(bar, 300.0d, 150.0d);

            Platform.runLater(() -> {
                showStatusWindow(scene);
            });
        }
        return task.execute();
    }

    private void showStatusWindow(final Scene scene) {
        final Stage primaryStage = getStage();
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> event.consume());
        primaryStage.setScene(scene);
        final Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
        primaryStage.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);
    }

    private VersionManager<?> getVersionManager(final String className, final ClassLoader classLoader)
        throws ApplicationException {
        final ServiceLoader<VersionManager> loader = ServiceLoader.load(VersionManager.class, classLoader);
        final Iterator<VersionManager> iterator = loader.iterator();
        while (iterator.hasNext()) {
            final VersionManager versionManager = iterator.next();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, String.format("The version manager '%s' has ben found",
                    versionManager.getClass().getName()));
                LOG.log(Level.FINE, String.format("The version manager '%s' has '%s' generic interfaces",
                    versionManager.getClass().getName(), versionManager.getClass().getGenericInterfaces().length));
                LOG.log(Level.FINE, String.format("The version manager '%s' has '%s' as generic super class",
                    versionManager.getClass().getName(), versionManager.getClass().getGenericSuperclass()));
            }
            final Type[] types = versionManager.getClass().getGenericInterfaces().length == 0 ?
                (versionManager.getClass().getGenericSuperclass() == null ? new Type[]{} :
                new Type[]{versionManager.getClass().getGenericSuperclass()}) :
                versionManager.getClass().getGenericInterfaces();
            if (types.length != 1) {
                continue;
            }
            if (!(types[0] instanceof ParameterizedType)) {
                return versionManager;
            }
            final ParameterizedType type = (ParameterizedType)types[0];
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, String.format("The version manager '%s' has '%s' type arguments",
                    versionManager.getClass().getName(), type.getActualTypeArguments().length));
            }
            if (type.getActualTypeArguments().length != 1 ) {
                continue;
            }
            final Class<?> typeClass = (Class<?>)type.getActualTypeArguments()[0];
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, String.format("The version manager '%s' is for the type '%s'",
                    versionManager.getClass().getName(), typeClass));
            }

            try {
                if (!typeClass.isAssignableFrom(Class.forName(className, false, classLoader))) {
                    continue;
                }
            } catch (ClassNotFoundException e) {
                throw new ApplicationException(String.format("Could not find the class '%s'", className), e);
            }

            return versionManager;
        }
        return null;
    }

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
