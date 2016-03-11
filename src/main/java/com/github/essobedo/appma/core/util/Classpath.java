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
package com.github.essobedo.appma.core.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Class allowing to release the jar files defined in the {@code JarFileFactory} which is specific to the Oracle's
 * JDK.
 *
 * <p>This is a hack needed to properly release jar files from which we get resources thanks to
 * {@link ClassLoader#getResource(String)}, {@link ClassLoader#getResourceAsStream(String)} or
 * {@link ClassLoader#getResources(String)}. Indeed by default, the {@link JarFile} instances are automatically stored
 * into the cache of {@code JarFileFactory} in case we call directly or indirectly one of the previous methods and
 * those instances are not released even if we call {@link java.net.URLClassLoader#close()}, so the purpose of this
 * hack is to remove them from the cache to fully release them.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class Classpath {

    /**
     * The logger of the class.
     */
    private static final Logger LOG = Logger.getLogger(Classpath.class.getName());

    /**
     * Instance of {@code JarFileFactory} that contains the {@code JarFile} instances into its cache to release.
     */
    private static final Object JAR_FILE_FACTORY = Classpath.getJarFileFactory();

    /**
     * Method allowing to get the instance of {@link JarFile} corresponding to a given {@link URL} that could
     * be found into the cache.
     */
    private static final Method GET = Classpath.getMethodGetByURL();

    /**
     * Method allowing to close a given instance of {@link JarFile} and remove it from the cache to make it available
     * to the GC.
     */
    private static final Method CLOSE = Classpath.getMethodCloseJarFile();

    /**
     * The urls of the resources corresponding to the classpath.
     */
    private final URL[] urls;

    /**
     * Constructs a {@code Classpath} with the specified urls.
     * @param urls the urls of the resources corresponding to the classpath.
     */
    public Classpath(final URL... urls) {
        this.urls = urls.clone();
    }

    /**
     * Releases all the urls to make it available for the GC.
     */
    public void release() {
        for (final URL url : urls) {
            if (url.getPath().endsWith("jar") || url.getPath().endsWith("zip")) {
                try {
                    CLOSE.invoke(JAR_FILE_FACTORY, GET.invoke(JAR_FILE_FACTORY, url));
                } catch (InvocationTargetException | IllegalAccessException e) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.log(
                            Level.WARNING,
                            String.format(
                                "Could not close the jar file '%s' used by the classloader",
                                url
                            ),
                            e
                        );
                    }
                }
            }
        }
    }

    /**
     * Gives the {@link Method} allowing to release a given {@link JarFile} instance.
     * @return The {@link Method} allowing to release a given {@link JarFile} instance.
     */
    private static Method getMethodCloseJarFile() {
        if (JAR_FILE_FACTORY != null) {
            try {
                final Method method = JAR_FILE_FACTORY.getClass().getMethod("close", JarFile.class);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                if (LOG.isLoggable(Level.SEVERE)) {
                    LOG.log(Level.SEVERE, "Could not find the method close of the class JarFileFactory", e);
                }
            }
        }
        return null;
    }

    /**
     * Gives the {@link Method} allowing to access to {@link JarFile} instances of the cache.
     * @return The {@link Method} allowing to access to {@link JarFile} instances of the cache.
     */
    private static Method getMethodGetByURL() {
        if (JAR_FILE_FACTORY != null) {
            try {
                final Method method = JAR_FILE_FACTORY.getClass().getMethod("get", URL.class);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                if (LOG.isLoggable(Level.SEVERE)) {
                    LOG.log(Level.SEVERE, "Could not find the method get of the class JarFileFactory", e);
                }
            }
        }
        return null;
    }

    /**
     * Gives the instance of the singleton {@code JarFileFactory}.
     * @return The instance of the singleton {@code JarFileFactory}.
     */
    private static Object getJarFileFactory() {
        try {
            final Method getInstance = Class.forName(
                "sun.net.www.protocol.jar.JarFileFactory",
                true,
                Classpath.class.getClassLoader()
            ).getMethod("getInstance");
            getInstance.setAccessible(true);
            return getInstance.invoke(null);
        } catch (NoSuchMethodException e) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, "Could not find the method getInstance of the class JarFileFactory", e);
            }
        } catch (ClassNotFoundException e) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Could not find the class JarFileFactory", e);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, "Could not invoke the method getInstance of the class JarFileFactory", e);
            }
        }
        return null;
    }

}
