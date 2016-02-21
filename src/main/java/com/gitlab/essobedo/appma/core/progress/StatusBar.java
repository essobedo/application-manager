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
package com.gitlab.essobedo.appma.core.progress;

import com.gitlab.essobedo.appma.task.Task;
import com.gitlab.essobedo.appma.task.TaskProgress;
import com.gitlab.essobedo.appma.i18n.Localization;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class StatusBar extends VBox {

    private final ProgressBar bar;
    private final Label label;
    private final Button button;
    private final Progress progress;

    public StatusBar(final Task<?> task) {
        super(10);
        setAlignment(Pos.CENTER);
        this.bar = new ProgressBar();
        bar.setMinWidth(250);
        this.label = new Label(task.getName());
        this.button = new Button(Localization.getMessage("cancel"));
        button.setOnAction(event -> task.cancel());
        button.setDisable(!task.cancelable());
        getChildren().addAll(label, bar, button);
        this.progress = new Progress(task);
    }

    private class Progress extends TaskProgress {

        private Progress(final Task<?> task) {
            super(task);
        }

        @Override
        public void updateProgress(final int done, final int max) {
            if (max >= done && max > 0) {
                StatusBar.this.bar.setProgress((double) (done / max));
            }
        }

        @Override
        public void updateMessage(final String message) {
            StatusBar.this.label.setText(message);
        }

        @Override
        public void cancel() {
            StatusBar.this.button.setDisable(true);
        }
    }
}
