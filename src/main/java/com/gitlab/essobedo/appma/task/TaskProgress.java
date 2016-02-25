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

/**
 * The root class of the {@code TaskObserver}.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@SuppressWarnings("PMD.AbstractNaming")
public abstract class TaskProgress implements TaskObserver {

    /**
     * The task to observe.
     */
    private final Task<?> task;

    /**
     * Constructs a {@code TaskProgress} with the specified task.
     * @param task the task to observe.
     */
    protected TaskProgress(final Task<?> task) {
        this.task = task;
        task.addObserver(this);
    }

    /**
     * Gives the task to observe.
     * @return the task to observe.
     */
    protected Task<?> getTask() {
        return task;
    }
}
