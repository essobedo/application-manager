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
package com.gitlab.essobedo.appma.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class allowing to execute all the tasks of the application manager asynchronously.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
class AsyncTaskExecutor {

    /**
     * The executor used to launch the tasks asynchronously.
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Executes a specified task asynchronously.
     * @param runnable the task to execute asynchronously.
     */
    public void execute(final Runnable runnable) {
        executor.execute(runnable);
    }

    /**
     * Stops the executor.
     */
    public void stop() {
        executor.shutdown();
    }
}
