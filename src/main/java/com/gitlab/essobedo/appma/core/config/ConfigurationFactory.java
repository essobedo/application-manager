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
import com.gitlab.essobedo.appma.exception.ApplicationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory class allowing to create a {@code Configuration} object.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class ConfigurationFactory {

    /**
     * The name of the system parameter allowing to specify the name of the
     * configuration file.
     */
    private static final String PARAM_CONFIG = "essobedo.appma.core.config";

    /**
     * The name of the default configuration file.
     */
    private static final String DEFAULT_CONFIG = "appma.properties";

    /**
     * The logger of the class.
     */
    private static final Logger LOG = Logger.getLogger(ConfigurationFactory.class.getName());

    /**
     * The parent folder from which all the relative paths must start.
     */
    private final File parentFolder;

    /**
     * Constructs a {@code ConfigurationFactory} using the specified parent folder.
     * @param parentFolder The folder from which all the relative paths must start.
     */
    public ConfigurationFactory(final File parentFolder) {
        this.parentFolder = parentFolder;
    }

    /**
     * Creates a {@code Configuration} corresponding to the current context. If
     * a configuration file whose name is {@link #getConfigurationName()} is available
     * directly under the parent folder, it will use this file to create the {@code Configuration}.
     * Otherwise it will check if there is jar files directly under the parent folder if so
     * it will build the {@code Configuration} based on the list of jar files that could be found
     * otherwise it will use the parent folder to create the {@code Configuration}.
     * @return The {@code Configuration} that matches the best with the current context.
     * @throws ApplicationException If an error occurred while creating the {@code Configuration}
     */
    public Configuration create() throws ApplicationException {
        final String configuration = ConfigurationFactory.getConfigurationName();
        final Configuration config;
        final File configFile = new File(parentFolder, configuration);
        if (configFile.exists()) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, String.format("The configuration could be found at '%s'",
                    configFile.getAbsolutePath()));
            }
            final Properties properties = new Properties();
            try (final InputStream input = new FileInputStream(configFile)) {
                properties.load(input);
            } catch (IOException e) {
                throw new ApplicationException(String.format("Could not load the configuration from '%s'",
                    configFile.getAbsolutePath()), e);
            }
            config = new ConfigFromProperties(parentFolder, properties);
            if (config.getClasspath().isEmpty()) {
                throw new ApplicationException(String.format("No classpath defined in '%s'",
                    configFile.getAbsolutePath()));
            }
        } else {
            final File folder = parentFolder;
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, String.format("No configuration could be found using the directory '%s'",
                    folder.getAbsolutePath()));
            }
            final File[] files = folder.listFiles((File dir, String name) -> name.endsWith(".jar"));
            if (files != null && files.length > 0) {
                config = new Configuration(files);
            } else {
                config = new Configuration(Collections.singletonList(folder));
            }
        }
        return config;
    }

    /**
     * Guves the name of the default configuration file.
     * @return the name of the default configuration file.
     */
    public static String getConfigurationName() {
        return System.getProperty(ConfigurationFactory.PARAM_CONFIG,
            ConfigurationFactory.DEFAULT_CONFIG);
    }
}
