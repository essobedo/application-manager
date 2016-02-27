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
package com.github.essobedo.appma.task;

import com.github.essobedo.appma.exception.ApplicationException;
import com.github.essobedo.appma.exception.TaskInterruptedException;
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
 * The root class of the {@code TaskObserver} in case of a Java FX application.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 * @param <T> The return type of the task.
 */
@SuppressWarnings("PMD.AbstractNaming")
public abstract class TaskProgressFX<T> extends TaskProgress {

    /**
     * The progress of the task.
     */
    private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", -1);
    /**
     * The status of the task.
     */
    private final StringProperty message = new SimpleStringProperty(this, "message", "");
    /**
     * Indicates whether the task is over.
     */
    private final BooleanProperty over = new SimpleBooleanProperty(this, "over", false);

    /**
     * Constructs a {@code TaskProgressFX} with the specified task.
     * @param task the task to observe.
     */
    public TaskProgressFX(final Task<T> task) {
        super(task);
    }

    /**
     * Gives the current progress of the task.
     * @return the current progress of the task.
     */
    public final double getProgress() {
        return progress.get();
    }
    /**
     * Gives the current progress of the task.
     * @return the current progress of the task.
     */
    public final ReadOnlyDoubleProperty progressProperty() {
        return progress;
    }
    /**
     * Gives the current status of the task.
     * @return the current status of the task.
     */
    public final String getMessage() {
        return message.get();
    }
    /**
     * Gives the current status of the task.
     * @return the current status of the task.
     */
    public final ReadOnlyStringProperty messageProperty() {
        return message;
    }
    /**
     * Indicates whether the task is over.
     * @return {@code true} if the task is over, {@code false} otherwise.
     */
    public final boolean isOver() {
        return over.get();
    }
    /**
     * Indicates whether the task is over.
     * @return {@code true} if the task is over, {@code false} otherwise.
     */
    public final ReadOnlyBooleanProperty overProperty() {
        return over;
    }
    /**
     * Cancels the underlying task if possible.
     */
    public final void cancelTask() {
        getTask().cancel();
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
        Platform.runLater(() -> this.message.setValue(message));
    }

    /**
     * Executes the underlying task.
     * @return the result of the task.
     * @throws ApplicationException if an error occurs while executing the task.
     * @throws TaskInterruptedException if the task has been interrupted.
     */
    public final T execute() throws ApplicationException, TaskInterruptedException {
        try {
            return (T) getTask().execute();
        } finally {
            Platform.runLater(() -> this.over.setValue(true));
        }
    }
}
