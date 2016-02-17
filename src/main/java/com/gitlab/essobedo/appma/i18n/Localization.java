/*
 * Copyright (C) 2015 essobedo.
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
package com.gitlab.essobedo.appma.i18n;

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
    private static final ResourceBundle RESOURCE_BUNDLE;

    static {
        try {
            RESOURCE_BUNDLE = ResourceBundle.getBundle("appma.i18n.messages");
        } catch (RuntimeException e) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, "Could not access to the resource bundle", e);
            }
            throw e;
        }
    }

    /**
     * Default constructor.
     */
    private Localization() {
    }

    /**
     * Gives the messages corresponding to the specified key using the given parameters.
     *
     * @param key The key of the message to retrieve.
     * @param params The parameters to use to construct the message.
     * @return The message internationalized.
     */
    public static String getMessage(final String key, final Object... params) {
        try {
            final String message = RESOURCE_BUNDLE.getString(key);
            return String.format(message, params);
        } catch (MissingResourceException e) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, "Could not find the message corresponding to the key " + key);
            }
        }
        return key;
    }
}
