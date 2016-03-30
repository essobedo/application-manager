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
package com.github.essobedo.appma.i18n;

import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class that manages the internationalization of the message to show to the end-users.
 *
 * @author <a href="mailto:nicolas.filotto@gmail.com">Nicolas Filotto</a>
 * @version $Id$
 */
public final class Localization {
    /**
     * The logger of the class.
     */
    private static final Logger LOG = Logger.getLogger(Localization.class.getName());

    /**
     * The {@code ResourceBundle} containing all the messages of the application.
     */
    private final ResourceBundle resourceBundle;

    /**
     * Default constructor.
     * @param baseName the base name of the resource bundle
     */
    Localization(final String baseName) {
        this(baseName, Localization.class.getClassLoader());
    }

    /**
     * Default constructor.
     * @param baseName the base name of the resource bundle
     * @param loader the class loader from which to load the resource bundle
     */
    public Localization(final String baseName, final ClassLoader loader) {
        try {
            resourceBundle = ResourceBundle.getBundle(baseName, Locale.getDefault(), loader);
        } catch (RuntimeException e) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, "Could not access to the resource bundle", e);
            }
            throw e;
        }
    }

    /**
     * Gives the messages corresponding to the specified key using the given parameters.
     *
     * @param key The key of the message to retrieve.
     * @param params The parameters to use to construct the message.
     * @return The message internationalized.
     */
    public String getLocalizedMessage(final String key, final Object... params) {
        try {
            final String message = resourceBundle.getString(key);
            return String.format(message, params);
        } catch (MissingResourceException | IllegalFormatException e) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, "Could not find the message corresponding to the key " + key);
            }
        }
        return key;
    }

    /**
     * Gives the messages corresponding to the specified key using the given parameters.
     *
     * @param key The key of the message to retrieve.
     * @param params The parameters to use to construct the message.
     * @return The message internationalized.
     */
    public static String getMessage(final String key, final Object... params) {
        return LocalizationHolder.INSTANCE.getLocalizedMessage(key, params);
    }

    /**
     * Class allowing to manage the lazy loading of the singleton.
     */
    private static class LocalizationHolder {
        /**
         * A singleton containing all the messages of the application.
         */
        private static final Localization INSTANCE = new Localization("appma.i18n.messages");
    }
}
