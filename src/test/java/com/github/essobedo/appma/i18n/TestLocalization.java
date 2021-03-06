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

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class TestLocalization {

    @Test
    public void testConstructor() {
        try {
            new Localization("foo");
            fail("A RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }
        new Localization("i18n.foo");
    }

    @Test
    public void testLocalizedMessage() {
        Localization localization = new Localization("i18n.foo");
        assertEquals("Hello", localization.getLocalizedMessage("key1"));
        assertEquals("Hello World!", localization.getLocalizedMessage("key2", "World"));
        assertEquals("key3", localization.getLocalizedMessage("key3", "World"));
        assertEquals("key4", localization.getLocalizedMessage("key4"));
    }

    @Test
    public void testContainsLocalizedMessage() {
        Localization localization = new Localization("i18n.foo");
        assertTrue(localization.containsLocalizedMessage("key1"));
        assertTrue(localization.containsLocalizedMessage("key2"));
        assertTrue(localization.containsLocalizedMessage("key3"));
        assertFalse(localization.containsLocalizedMessage("key4"));
    }

    @Test
    public void testContainsMessage() {
        assertTrue(Localization.containsMessage("cancel"));
        assertFalse(Localization.containsMessage("foo"));
    }
}
