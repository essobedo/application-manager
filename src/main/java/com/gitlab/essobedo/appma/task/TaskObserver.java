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

import java.util.Observable;
import java.util.Observer;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public interface TaskObserver extends Observer {

    default void update(final Observable observable, final Object object) {
        if (!(observable instanceof Task)) {
            throw new IllegalArgumentException("A task was expected");
        } else if (!(object instanceof Task.Event)) {
            throw new IllegalArgumentException("A task event was expected");
        }
        final Task<?> task = (Task<?>)observable;
        final Task.Event event = (Task.Event)object;
        switch (event) {
            case PROGRESS:
                updateProgress(task.getWorkDone(), task.getMax());
                break;
            case MESSAGE:
                updateMessage(task.getMessage());
                break;
            case CANCEL:
                if (task.cancelable()) {
                    cancel();
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown event");
        }
    }

    void updateProgress(int done, int max);

    void updateMessage(String message);

    void cancel();
}
