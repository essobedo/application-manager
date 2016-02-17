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
package com.gitlab.essobedo.appma.core.impl.zip;

import com.gitlab.essobedo.appma.core.impl.DefaultApplicationManager;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class ZipFile {

    private final File file;

    public ZipFile(final File file) {
        this.file = file;
    }


    /**
     * Extracts the content of the zip file to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param destDir
     * @throws IOException
     */
    public void unzip(final File destDir) throws IOException {
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        try (final ZipInputStream zipIn = new ZipInputStream(new FileInputStream(this.file))) {
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                final File file = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    // if the entry is a directory, make the directory
                    file.mkdir();
                } else {
                    // if the entry is a file, extracts it
                    ZipFile.extractFile(zipIn, file);
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param file
     * @throws IOException
     */
    private static void extractFile(final ZipInputStream zipIn, final File file) throws IOException {
        byte[] bytesIn = new byte[1024];
        try (final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}
