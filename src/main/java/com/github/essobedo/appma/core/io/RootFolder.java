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
package com.github.essobedo.appma.core.io;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Class allowing to get the root folder that contains a given class.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class RootFolder {

    /**
     * The class for which we want to know its parent folder.
     */
    private final Class<?> clazz;

    /**
     * Constructs a {@code RootFolder} with the specified class.
     * @param clazz the class for which we want to know its parent folder.
     */
    public RootFolder(final Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * Gives the folder that contains the class. In case the class is located in a folder
     * it will give the folder from which the packages start. In case the class is located
     * in a jar file it will give the folder that contains the jar file.
     * @return the {@code File} corresponding to the folder that contains the class.
     */
    public File getLocation() {
        final String name = clazz.getName();
        final String resource = String.format("/%s.class", name.replace('.', '/'));
        final URL url = clazz.getResource(resource);
        if (url == null) {
            throw new IllegalStateException(String.format("Cannot locate the class '%s'", name));
        }
        try {
            if ("file".equals(url.getProtocol())) {
                final String urlStr = url.toString();
                final String root = urlStr.substring(0, urlStr.length() - resource.length() + 1);
                return new File(new URI(root));
            } else if ("jar".equals(url.getProtocol())) {
                String urlStr = url.getPath();
                int index = urlStr.lastIndexOf('!');
                urlStr = urlStr.substring(0, index);
                index = urlStr.lastIndexOf('/');
                final String root = urlStr.substring(0, index + 1);
                return new File(new URI(root));
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException(String.format("Could not manage the path '%s'", url), e);
        }
        throw new IllegalStateException(String.format("Cannot manage the protocol '%s'", url.getProtocol()));
    }
}
