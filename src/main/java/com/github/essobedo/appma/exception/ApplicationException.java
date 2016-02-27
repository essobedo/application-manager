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
package com.github.essobedo.appma.exception;

/**
 * The main exception of the application manager.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class ApplicationException extends Exception {

    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = -7491091906224057082L;

    /**
     * Constructs an {@code ApplicationException} with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method)
     */
    public ApplicationException(final String message) {
        super(message);
    }

    /**
     * Constructs an {@code ApplicationException} with the specified detail message
     * and cause.
     *
     * <p> Note that the detail message associated with {@code cause} is
     * <i>not</i> automatically incorporated into this exception's detail
     * message.
     *
     * @param message The detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method)
     * @param cause   The cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A null value is permitted,
     *                and indicates that the cause is nonexistent or unknown.)
     */
    public ApplicationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
