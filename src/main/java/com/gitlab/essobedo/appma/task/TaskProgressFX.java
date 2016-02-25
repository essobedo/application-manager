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
package com.gitlab.essobedo.appma.task;

import com.gitlab.essobedo.appma.exception.ApplicationException;
import com.gitlab.essobedo.appma.exception.TaskInterruptedException;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@SuppressWarnings("PMD.AbstractNaming")
public abstract class TaskProgressFX<T> extends TaskProgress {

    private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", -1);
    private final StringProperty message = new SimpleStringProperty(this, "message", "");
    private final BooleanProperty over = new SimpleBooleanProperty(this, "over", false);

    public TaskProgressFX(final Task<T> task) {
        super(task);
    }

    public final double getProgress() { return progress.get(); }
    public final ReadOnlyDoubleProperty progressProperty() { return progress; }

    public final String getMessage() { return message.get(); }
    public final ReadOnlyStringProperty messageProperty() { return message; }

    public final boolean isOver() { return over.get(); }
    public final ReadOnlyBooleanProperty overProperty() { return over; }

    public final void cancelTask() {
        task.cancel();
    }

    @Override
    public final void updateProgress(final int done, final int max) {
        Platform.runLater(() -> {
            if (done <= max && max > 0) {
                this.progress.set((double) (done / max));
            } else {
                this.progress.set(-1.0d);
            }
        });
    }

    @Override
    public final void updateMessage(final String message) {
        Platform.runLater(() -> {
            this.message.setValue(message);
        });
    }

    public final T execute() throws ApplicationException, TaskInterruptedException {
        try {
            return (T) task.execute();
        } finally {
            Platform.runLater(() -> {
                this.over.setValue(true);
            });
        }
    }
}
