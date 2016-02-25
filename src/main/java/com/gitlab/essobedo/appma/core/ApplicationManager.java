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

import com.gitlab.essobedo.appma.exception.ApplicationException;
import com.gitlab.essobedo.appma.task.Task;
import javafx.stage.Stage;

/**
 * The facade allowing the managed applications to interact with the application manager.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public interface ApplicationManager {

    /**
     * Gives the current {@link Stage}.
     * @return The current {@link Stage} in case of a Java FX application, {@code null} otherwise.
     */
    Stage getStage();

    /**
     * Gives the task allowing to check if there is a new version of the application.
     * @return The task allowing to check for a new version.
     * @throws ApplicationException in case the task could not be created.
     */
    Task<String> checkForUpdate() throws ApplicationException;

    /**
     * Triggers an upgrade of the application. The upgrade will be down asynchronously by the
     * application manager.
     * @return {@code true} if the upgrade could be triggered, {@code false} otherwise.
     */
    boolean upgrade();

    /**
     * Callback allowing to notify the application manager that an application exit has been
     * requested.
     */
    void onExit();
}
