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

import java.util.Observable;
import java.util.Observer;

/**
 * Defines the {@link Observer} of a {@link Task}.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public interface TaskObserver extends Observer {

    @Override
    default void update(final Observable observable, final Object object) {
        final Task<?> task;
        if (observable instanceof Task) {
            task = (Task<?>) observable;
        } else {
            throw new IllegalArgumentException("A task was expected");
        }
        final Task.Event event;
        if (object instanceof Task.Event) {
            event = (Task.Event) object;
        } else {
            throw new IllegalArgumentException("A task event was expected");
        }
        switch (event) {
            case PROGRESS:
                updateProgress(task.getWorkDone(), task.getMax());
                break;
            case MESSAGE:
                updateMessage(task.getMessage());
                break;
            case CANCEL:
                cancel();
                break;
            default:
                throw new IllegalArgumentException("Unknown event");
        }
    }

    /**
     * Notifies that the current progress of the task has changed.
     * @param done the work already done.
     * @param max the maximum work to be done.
     */
    void updateProgress(int done, int max);

    /**
     * Notifies that the status of the task has changed.
     * @param message the status of the task.
     */
    void updateMessage(String message);

    /**
     * Notifies that the task has been canceled.
     */
    void cancel();
}
