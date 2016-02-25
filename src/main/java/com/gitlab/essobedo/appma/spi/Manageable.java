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

import com.gitlab.essobedo.appma.core.ApplicationManager;
import com.gitlab.essobedo.appma.exception.ApplicationException;
import javafx.scene.Scene;
import javafx.scene.image.Image;

/**
 * Defines an application that can be managed by the {@link ApplicationManager}.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public interface Manageable {

    /**
     * Gives the name of the application.
     * @return the name of the application
     */
    String name();

    /**
     * Gives the identifier of the version of the application.
     * @return the identifier of the version of the application.
     */
    String version();

    /**
     * Gives the title of the window to use when launching the application. This is only
     * needed in case the application is a Java FX application.
     * @return the title of the window to use when launching the application.
     */
    String title();

    /**
     * Gives the icon to use when launching the application. This is only
     * needed in case the application is a Java FX application.
     * @return the icon to use when launching the application.
     */
    Image icon();

    /**
     * Indicates whether the application can handle the specified arguments.
     * @param arguments the arguments to check.
     * @return {@code true} if the application can handle the specified arguments, {@code false}
     * otherwise.
     */
    boolean accept(String... arguments);

    /**
     * Indicates whether the application is a Java FX application.
     * @return {@code true} if the  application is a Java FX application, {@code false}
     * otherwise.
     */
    boolean isJavaFX();

    /**
     * Initializes the application.
     * @param manager the application manager allowing to interact with it.
     * @param arguments the arguments to use to initialize the application.
     * @return a {@link Scene} corresponding to the initialized application if it is a Java FX
     * application or {@code null} otherwise.
     * @throws ApplicationException if the application could not be initialized.
     */
    Scene init(ApplicationManager manager, String... arguments) throws ApplicationException;

    /**
     * Destroys the application.
     * @throws ApplicationException if the application could not be destroyed.
     */
    void destroy() throws ApplicationException;
}
