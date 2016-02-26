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

import com.gitlab.essobedo.appma.core.io.RootFolder;
import com.gitlab.essobedo.appma.i18n.Localization;
import com.gitlab.essobedo.appma.spi.Manageable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The main entry point to the application manager.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class Launcher extends Application {

    /**
     * The logger of the class.
     */
    private static final Logger LOG = Logger.getLogger(Launcher.class.getName());

    /**
     * The name of the system parameter allowing to externalize the
     * configuration of the logger.
     */
    private static final String PARAM_LOGGER = "essobedo.logger.config";

    /**
     * The current application manager.
     */
    private static DefaultApplicationManager applicationManager;

    /**
     * The main method.
     * @param args the arguments.
     * @throws IOException in case the logger could not be set up.
     */
    public static void main(final String... args) throws IOException {
        setUpLogger();

        DefaultApplicationManager applicationManager = null;
        Manageable application = null;
        try {
            final RootFolder root = new RootFolder(Launcher.class);
            applicationManager = new DefaultApplicationManager(root.getLocation(), args);
            application = applicationManager.create();
            if (application.isJavaFX()) {
                Launcher.applicationManager = applicationManager;
                launch(args);
            } else {
                applicationManager.init();
            }
        } catch (Exception e) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
            System.exit(1);
        }
        if (!application.isJavaFX()) {
            try {
                applicationManager.destroy();
            } catch (Exception e) {
                if (LOG.isLoggable(Level.SEVERE)) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            System.exit(0);
        }
    }

    /**
     * Sets up the logger. It will first check if the system property {@code essobedo.logger.config}
     * has been set, if so it will use it as configuration of the logger otherwise it will get the
     * file {@code /conf/logging.properties} from the classloader.
     * @throws IOException if the logger could not be set up.
     */
    private static void setUpLogger() throws IOException {
        final File logs = new File("logs");
        if (logs.exists() || logs.mkdir()) {
            final LogManager manager = LogManager.getLogManager();
            final String loggerPath = System.getProperty(Launcher.PARAM_LOGGER);
            try (final InputStream input = loggerPath == null
                ? Launcher.class.getResourceAsStream("/conf/logging.properties")
                : new FileInputStream(loggerPath)) {
                manager.readConfiguration(input);
            }
        }
    }

    @Override
    public void start(final Stage primaryStage) {
        start(Launcher.applicationManager, primaryStage);
    }

    /**
     * Starts the launcher using the specified application manager.
     * @param applicationManager the application manager to use.
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set. The primary stage will be embedded in
     * the browser if the application was launched as an applet.
     * Applications may create other stages, if needed, but they will not be
     * primary stages and will not be embedded in the browser.
     * @return The {@link Future} object allowing to be notified once the application has
     * been initialized and shown.
     */
    static Future<Void> start(final DefaultApplicationManager applicationManager, final Stage primaryStage) {
        final VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        final ProgressBar bar = new ProgressBar();
        bar.setMinWidth(250);
        final Label label = new Label(Localization.getMessage("status.loading"));
        vBox.getChildren().addAll(label, bar);
        primaryStage.setScene(new Scene(vBox, 300.0d, 150.0d));
        final Manageable application = applicationManager.getApplication();
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(Event::consume);
        if (application.title() == null) {
            primaryStage.setTitle(Localization.getMessage("title.window"));
        } else {
            primaryStage.setTitle(application.title());
        }
        if (application.icon() != null) {
            primaryStage.getIcons().add(application.icon());
        }
        primaryStage.show();

        return applicationManager.asyncInitNShow(primaryStage,
            () -> label.setText(Localization.getMessage("status.error")));
    }
}
