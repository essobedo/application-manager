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
package com.gitlab.essobedo.appma.spi;

import com.gitlab.essobedo.appma.core.Configuration;
import com.gitlab.essobedo.appma.task.Task;
import com.gitlab.essobedo.appma.exception.ApplicationException;
import java.io.File;
import java.io.OutputStream;

/**
 * The class allowing to check for new version of a given application and allowing to upgrade
 * the application from a version to another.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 * @param <T> the type of the application that can manage the version manager.
 */
public interface VersionManager<T extends Manageable> {

    /**
     * Gives the task allowing to check for a new version of the specified application.
     * @param application the application for which we want to check for an update.
     * @return the task allowing to check for a new version of the specified application.
     * @throws ApplicationException if the task could not be created.
     */
    Task<String> check(T application) throws ApplicationException;

    /**
     * Gives the task allowing to store the content of the patch.
     * @param application the application for which we want the patch.
     * @param target the stream in which the content of the patch should be written.
     * @return the task allowing to store the content of the patch.
     * @throws ApplicationException if the task could not be created.
     */
    Task<Void> store(T application, OutputStream target) throws ApplicationException;

    /**
     * Gives the task allowing to upgrade the application.
     * @param upgradeRoot the root folder that contains the content of the patch.
     * @param appRoot the root folder of the application.
     * @param oldVersion the previous version of the application.
     * @return the task allowing to upgrade the application.
     * @throws ApplicationException if the task could not be created.
     */
    Task<Configuration> upgrade(File upgradeRoot, File appRoot, String oldVersion) throws ApplicationException;
}
