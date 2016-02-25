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
import java.util.Observable;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@SuppressWarnings("PMD.AbstractNaming")
public abstract class Task<T> extends Observable {

    private int max;
    private int done;
    private String message;
    private boolean canceled;

    private final String name;

    protected Task(final String name) {
        this.name = name;
    }

    protected void updateProgress(final int done, final int max) {
        synchronized (this) {
            this.done = done;
            this.max = max;
            this.setChanged();
            this.notifyObservers(Task.Event.PROGRESS);
        }
    }

    protected void updateMessage(final String message) {
        synchronized (this) {
            this.message = message;
            this.setChanged();
            this.notifyObservers(Task.Event.MESSAGE);
        }
    }

    public void cancel() {
        synchronized (this) {
            this.canceled = true;
            this.setChanged();
            this.notifyObservers(Task.Event.CANCEL);
        }
    }

    public String getMessage() {
        synchronized (this) {
            return this.message;
        }
    }

    public int getWorkDone() {
        synchronized (this) {
            return this.done;
        }
    }

    public int getMax() {
        synchronized (this) {
            return this.max;
        }
    }

    public String getName() {
        synchronized (this) {
            return this.name;
        }
    }

    protected boolean isCanceled() {
        synchronized (this) {
            return canceled;
        }
    }

    public abstract boolean cancelable();

    public abstract T execute() throws ApplicationException, TaskInterruptedException;

    public enum Event {
        PROGRESS, MESSAGE, CANCEL
    }
}
