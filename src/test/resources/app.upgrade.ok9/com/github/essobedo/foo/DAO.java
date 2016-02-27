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
package com.github.essobedo.foo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class DAO {
    private static final Properties PROPERTIES;
    static {
        PROPERTIES = new Properties();
        try (InputStream inputStream = DAO.class.getResourceAsStream("/test.properties")) {
            PROPERTIES.load(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }
}
