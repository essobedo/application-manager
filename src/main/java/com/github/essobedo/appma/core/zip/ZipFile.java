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
package com.github.essobedo.appma.core.zip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The class allowing to unzip a given zip file.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class ZipFile {

    /**
     * The zip file to unzip.
     */
    private final File file;

    /**
     * Constructs a {@code ZipFile} with the specified file.
     * @param file the zip file to unzip.
     */
    public ZipFile(final File file) {
        this.file = file;
    }

    /**
     * Extracts the content of the zip file into the specified folder. The folder will be
     * created automatically if it does not exist.
     * @param destDir the destination folder.
     * @throws IOException In case the file could not be unzipped.
     */
    public void unzip(final File destDir) throws IOException {
        if (!destDir.exists() && !destDir.mkdir()) {
            throw new IOException(String.format("Could not create the destination directory '%s'",
                destDir.getAbsolutePath()));
        }
        try (final ZipInputStream zipIn = new ZipInputStream(new FileInputStream(this.file))) {
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                final File file = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    // if the entry is a directory, make the directory
                    if (!file.mkdir()) {
                        throw new IOException(String.format("Could not create the sub-directory '%s'",
                            file.getAbsolutePath()));
                    }
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
     * Extracts a zip entry (file entry).
     * @param zipIn the zip entry to extract.
     * @param file the destination file
     * @throws IOException In case the zip entry could not be extracted.
     */
    private static void extractFile(final ZipInputStream zipIn, final File file) throws IOException {
        final byte[] bytesIn = new byte[1024];
        try (final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}
