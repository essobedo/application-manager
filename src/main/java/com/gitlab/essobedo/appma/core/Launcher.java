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
import com.gitlab.essobedo.appma.exception.ApplicationException;
import com.gitlab.essobedo.appma.i18n.Localization;
import com.gitlab.essobedo.appma.spi.Manageable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
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
     * configuration of the logger
     */
    private static final String PARAM_LOGGER = "essobedo.logger.config";

    /**
     * The current application manager
     */
    private static DefaultApplicationManager APPLICATION_MANAGER;

    public static void main(final String[] args) throws IOException {
        final File logs = new File("logs");
        if (logs.exists() || logs.mkdir()) {
            final LogManager manager = LogManager.getLogManager();
            final String loggerPath = System.getProperty(Launcher.PARAM_LOGGER);
            try (final InputStream input = loggerPath == null ?
                Launcher.class.getResourceAsStream("/conf/logging.properties") :
                new FileInputStream(loggerPath)) {
                manager.readConfiguration(input);
            }
        }
        try {
            final RootFolder root = new RootFolder(Launcher.class);
            final DefaultApplicationManager applicationManager = new DefaultApplicationManager(
                root.getLocation(), args);
            final Manageable application = applicationManager.create();
            if (application.isJavaFX()) {
                APPLICATION_MANAGER = applicationManager;
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
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        final ProgressBar bar = new ProgressBar();
        bar.setMinWidth(250);
        final Label label = new Label(Localization.getMessage("status.loading"));
        final Button button = new Button(Localization.getMessage("exit"));
        button.setOnAction(event -> primaryStage.close());
        button.setDisable(true);
        vBox.getChildren().addAll(label, bar, button);
        primaryStage.setScene(new Scene(vBox, 300.0d, 150.0d));
        final Manageable application = Launcher.APPLICATION_MANAGER.getApplication();
        if (application.title() == null) {
            primaryStage.setTitle(Localization.getMessage("title.window"));
        } else {
            primaryStage.setTitle(application.title());
        }
        if (application.icon() != null) {
            primaryStage.getIcons().add(application.icon());
        }
        primaryStage.show();

        final Runnable runnable = () -> {
            try {
                final Scene scene = Launcher.APPLICATION_MANAGER.init();
                Launcher.APPLICATION_MANAGER.setStage(primaryStage);
                Platform.runLater(() -> {
                    primaryStage.setScene(scene);
                    final Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
                    primaryStage.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
                    primaryStage.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);
                });
            } catch (ApplicationException e) {
                label.setText(Localization.getMessage("status.error"));
                button.setDisable(false);
                if (LOG.isLoggable(Level.SEVERE)) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        };
        Launcher.APPLICATION_MANAGER.getExecutor().execute(runnable);
    }
}
