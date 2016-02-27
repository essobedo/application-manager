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
import java.util.Observable;

/**
 * The root class of all the tasks managed by the application manager.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 * @param <T> The return type of the task.
 */
@SuppressWarnings("PMD.AbstractNaming")
public abstract class Task<T> extends Observable {

    /**
     * The maximum work to be done.
     */
    private int max;
    /**
     * The work already done.
     */
    private int done;
    /**
     * The status of the task.
     */
    private String message;
    /**
     * Indicates whether the task has been canceled or not.
     */
    private boolean canceled;
    /**
     * The name of the task.
     */
    private final String name;

    /**
     * Constructs a {@code Task} with the specified name.
     * @param name the name of the task.
     */
    protected Task(final String name) {
        this.name = name;
    }

    /**
     * Updates the current progress of the task.
     * @param done the work already done.
     * @param max the maximum work to be done.
     */
    protected final void updateProgress(final int done, final int max) {
        synchronized (this) {
            this.done = done;
            this.max = max;
            this.setChanged();
            this.notifyObservers(Task.Event.PROGRESS);
        }
    }

    /**
     * Updates the status of the task.
     * @param message the status of the task.
     */
    protected final void updateMessage(final String message) {
        synchronized (this) {
            this.message = message;
            this.setChanged();
            this.notifyObservers(Task.Event.MESSAGE);
        }
    }

    /**
     * Cancels the task if possible.
     */
    public final void cancel() {
        if (cancelable()) {
            synchronized (this) {
                this.canceled = true;
                this.setChanged();
                this.notifyObservers(Task.Event.CANCEL);
            }
        }
    }

    /**
     * Gives the current status of the task.
     * @return the current status of the task.
     */
    public final String getMessage() {
        synchronized (this) {
            return this.message;
        }
    }

    /**
     * Gives the work already done.
     * @return the work already done.
     */
    public final int getWorkDone() {
        synchronized (this) {
            return this.done;
        }
    }

    /**
     * Gives the maximum work to be done.
     * @return the maximum work to be done.
     */
    public final int getMax() {
        synchronized (this) {
            return this.max;
        }
    }

    /**
     * Gives the name of the task.
     * @return the name of the task.
     */
    public final String getName() {
        synchronized (this) {
            return this.name;
        }
    }

    /**
     * Indicates whether the task has been canceled.
     * @return {@code true} if the task has been canceled, {@code false} otherwise.
     */
    protected final boolean isCanceled() {
        synchronized (this) {
            return canceled;
        }
    }

    /**
     * Indicates whether the task can be canceled.
     * @return {@code true} if the task can be canceled, {@code false} otherwise.
     */
    public abstract boolean cancelable();

    /**
     * Executes the task.
     * @return the result of the task.
     * @throws ApplicationException if an error occurs while executing the task.
     * @throws TaskInterruptedException if the task has been interrupted.
     */
    public abstract T execute() throws ApplicationException, TaskInterruptedException;

    /**
     * The possible events for a task.
     */
    public enum Event {
        /**
         * The progress of the task has been updated.
         */
        PROGRESS,
        /**
         * The status of the task has been updated.
         */
        MESSAGE,
        /**
         * The task has been canceled.
         */
        CANCEL
    }
}
