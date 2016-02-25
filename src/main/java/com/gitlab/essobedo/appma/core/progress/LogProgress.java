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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class allowing to log any progress of a given task. It is used by default for all
 * non Java FX tasks.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class LogProgress extends TaskProgress {

    /**
     * The logger of the class.
     */
    private static final Logger LOG = Logger.getLogger(LogProgress.class.getName());

    /**
     * Constructs a {@code LogProgress} with the specified task.
     * @param task the task for which we want to log the progress.
     */
    public LogProgress(final Task<?> task) {
        super(task);
    }

    @Override
    public void updateProgress(final int done, final int max) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, String.format("Task '%s': %d out of %d has been done",
                getTask().getName(), done, max));
        }
    }

    @Override
    public void updateMessage(final String message) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, String.format("Task '%s': %s", getTask().getName(), message));
        }
    }

    @Override
    public void cancel() {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, String.format("The task '%s' has been canceled", getTask().getName()));
        }
    }
}
