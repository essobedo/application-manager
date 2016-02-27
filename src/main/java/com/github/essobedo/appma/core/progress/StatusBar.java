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
package com.github.essobedo.appma.core.progress;

import com.github.essobedo.appma.i18n.Localization;
import com.github.essobedo.appma.task.Task;
import com.github.essobedo.appma.task.TaskProgressFX;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/**
 * Class allowing to show any progress of a given task thanks to a progress bar and a label to display
 * the status. It is used by default for all Java FX tasks.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class StatusBar extends VBox {

    /**
     * The cancel button.
     */
    private final Button button;

    /**
     * Constructs a {@code StatusBar} with the specified task.
     * @param task the task for which we want to show the progress.
     */
    public StatusBar(final Task<?> task) {
        super(10);
        this.setAlignment(Pos.CENTER);
        final ProgressBar bar = new ProgressBar();
        bar.setMinWidth(250);
        final Label label = new Label(task.getName());
        this.button = new Button(Localization.getMessage("cancel"));
        button.setOnAction(event -> task.cancel());
        button.setDisable(!task.cancelable());
        this.getChildren().addAll(label, bar, button);
        final Progress progress = new Progress(task);
        label.textProperty().bind(progress.messageProperty());
        bar.progressProperty().bind(progress.progressProperty());
        progress.overProperty().addListener((observable) -> {
            bar.progressProperty().unbind();
            bar.setProgress(1d);
        });
    }

    /**
     * Class allowing to disable the cancel button in case the task has already been canceled.
     */
    private class Progress extends TaskProgressFX {

        /**
         * Constructs a {@code Progress} with the specified task.
         * @param task the task for which we want to show the progress.
         */
        Progress(final Task<?> task) {
            super(task);
        }

        @Override
        public void cancel() {
            StatusBar.this.button.setDisable(true);
        }
    }
}
