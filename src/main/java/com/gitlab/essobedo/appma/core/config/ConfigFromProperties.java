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
package com.gitlab.essobedo.appma.core.config;

import com.gitlab.essobedo.appma.core.Configuration;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Class allowing to create a {@link Configuration} by extracting the information from
 * the content of a {@link Properties}.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@SuppressWarnings("PMD.UseUtilityClass")
public class ConfigFromProperties extends Configuration {

    /**
     * The name of the parameter in the properties file from which we
     * extract the classpath.
     */
    private static final String CLASSPATH = "classpath";

    /**
     * Constructs an {@code ConfigFromProperties} using specified parent folder and
     * properties. It will extract the value of the property {@code classpath}, split the
     * content using {@code ';'} as separator and use each entry as a relative path from the
     * parent folder to build the full path.
     * @param parentFolder the parent folder from which all the relative paths must start.
     * @param properties the properties containing the key {@code classpath} for which we want
     * the value
     */
    public ConfigFromProperties(final File parentFolder, final Properties properties) {
        super(ConfigFromProperties.asFiles(parentFolder, properties.getProperty(ConfigFromProperties.CLASSPATH)));
    }

    /**
     * Splits the content of {@code classpath} using {@code ';'} as separator then use
     * each entry as a relative path from the parent folder to build the corresponding
     * {@code File}.
     * @param parentFolder the parent folder from which all the relative paths must start.
     * @param classpath the classpath to process.
     * @return a list of {@code File} corresponding to all the entries that
     * could be found in the content of {@code classpath}. If the content of {@code classpath}
     * is {@code null} or empty, an empty list will be returned.
     */
    private static List<File> asFiles(final File parentFolder, final String classpath) {
        if (classpath == null || classpath.isEmpty()) {
            return Collections.emptyList();
        }
        final String[] paths = classpath.split(";");
        final List<File> files = new ArrayList<>(paths.length);
        for (final String path : paths) {
            files.add(new File(parentFolder, path));
        }
        return files;
    }

    /**
     * Saves the content of the specified {@code Configuration} into the specified
     * target {@code File}.
     *
     * @param config the configuration to save into the disc.
     * @param target the target file in which the content of the {@code Configuration}
     * will be stored.
     * @throws IOException if the configuration could not be stored
     */
    public static void store(final Configuration config, final File target) throws IOException {
        if (config.getClasspath().isEmpty()) {
            return;
        }
        final StringBuilder result = new StringBuilder(512);
        final Path parentFolder = Paths.get(target.getParentFile().getAbsolutePath());
        boolean first = true;
        for (final File file : config.getClasspath()) {
            final Path classpathEntry = Paths.get(file.getAbsolutePath());
            if (first) {
                first = false;
            } else {
                result.append(';');
            }
            result.append(parentFolder.relativize(classpathEntry));
        }
        final Properties properties = new Properties();
        properties.put(ConfigFromProperties.CLASSPATH, result.toString());
        try (final OutputStream outputStream = new FileOutputStream(target)) {
            properties.store(outputStream, null);
        }
    }
}
