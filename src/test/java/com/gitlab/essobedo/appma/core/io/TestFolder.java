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
}
