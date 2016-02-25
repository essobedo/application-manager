/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Nicolas Filotto
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.gitlab.essobedo.appma.core.io;

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
