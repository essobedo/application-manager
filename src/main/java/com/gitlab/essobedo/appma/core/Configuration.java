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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The class defining the configuration of the application manager.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class Configuration {

    /**
     * The list of {@code File} representing the classpath to be loaded by the application manager.
     */
    private final List<File> classpath;

    /**
     * Constructs a {@code Configuration} with the specified {@code classpath}.
     * @param classpath the classpath to be loaded by the application manager.
     */
    public Configuration(final File... classpath) {
        this(Arrays.asList(classpath));
    }

    /**
     * Constructs a {@code Configuration} with the specified {@code classpath}.
     * @param classpath the classpath to be loaded by the application manager.
     */
    public Configuration(final List<File> classpath) {
        this.classpath = Collections.unmodifiableList(classpath);
    }

    /**
     * Gives the classpath to be loaded by the application manager.
     * @return The list of {@code File} representing the classpath to be loaded by
     * the application manager.
     */
    public List<File> getClasspath() {
        return this.classpath;
    }

    /**
     * Gives the classpath to be loaded by the application manager as {@link URL}.
     * @return An array of {@link URL} representing the classpath to be loaded by
     * the application manager.
     * @throws ApplicationException in case a file of the classpath could not be found.
     */
    public URL[] getClasspathAsUrls() throws ApplicationException {
        final URL[] urls = new URL[classpath.size()];
        for (int i = 0; i < urls.length; i++) {
            final File file = classpath.get(i);
            if (!file.exists()) {
                throw new ApplicationException(String.format("The file '%s' doesn't exist", file.getAbsolutePath()));
            }
            final URL url;
            try {
                url = file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new ApplicationException(String.format("Could not create a valid URL for the file '%s'",
                    file.getAbsolutePath()), e);
            }
            try (final InputStream input = url.openStream()) {
                // Ensure that url is accessible
                if (input.read() == -1) {
                    throw new ApplicationException(String.format("The file '%s' is empty",
                        file.getAbsolutePath()));
                }
            } catch (IOException e) {
                throw new ApplicationException(String.format("Could not access to '%s'", file.getAbsolutePath()), e);
            }
            urls[i] = url;
        }
        return urls;
    }
}
