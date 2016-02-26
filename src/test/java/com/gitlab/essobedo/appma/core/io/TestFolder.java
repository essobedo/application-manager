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
package com.gitlab.essobedo.appma.core.io;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class TestFolder {

    @Test
    public void testActions() throws Exception {
        Path tempDir = Files.createTempDirectory("TestFolder");
        Folder folder = new Folder(new File(TestFolder.class.getResource("/folder").toURI()));
        File target = new File(tempDir.toString());
        folder.copy(target);
        assertEquals(2, target.list().length);
        assertTrue(new File(target, "subfolder1").exists());
        assertTrue(new File(target, "subfolder1").isDirectory());
        assertTrue(new File(target, "subfolder1/foo.properties").exists());
        assertTrue(new File(target, "subfolder1/foo.properties").isFile());
        assertTrue(new File(target, "test.properties").exists());
        assertTrue(new File(target, "test.properties").isFile());

        assertTrue(target.exists());
        folder = new Folder(target);
        folder.delete();
        assertFalse(target.exists());
    }

    @Test
    public void testActionsFailure() throws Exception {
        Path tempDir = Files.createTempDirectory("TestFolder");
        File dir = new File(tempDir.toString());
        Folder folder = new Folder(dir);
        assertTrue(dir.delete());
        assertFalse(dir.exists());
        folder.delete();
        folder.copy(new File("foo"));
    }
}
