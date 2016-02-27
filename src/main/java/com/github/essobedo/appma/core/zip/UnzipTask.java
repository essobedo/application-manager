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

import com.github.essobedo.appma.exception.ApplicationException;
import com.github.essobedo.appma.task.Task;
import com.github.essobedo.appma.i18n.Localization;
import java.io.File;
import java.io.IOException;

/**
 * {@link Task} allowing to unzip of zip file into a given folder.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class UnzipTask extends Task<Void> {

    /**
     * The zip file to unzip.
     */
    private final File zipFile;
    /**
     * The destination folder.
     */
    private final File destFolder;

    /**
     * Constructs a {@code UnzipTask} with the specified zip file and destination folder.
     * @param zipFile the zip file to unzip.
     * @param destFolder the destination folder.
     */
    public UnzipTask(final File zipFile, final File destFolder) {
        super(Localization.getMessage("patch.unzip"));
        this.zipFile = zipFile;
        this.destFolder = destFolder;
    }

    @Override
    public boolean cancelable() {
        return true;
    }

    @Override
    public Void execute() throws ApplicationException {
        try {
            updateMessage(Localization.getMessage("patch.unzipping"));
            final ZipFile file = new ZipFile(zipFile);
            file.unzip(destFolder);
        } catch (IOException e) {
            throw new ApplicationException("Could not unzip the patch", e);
        }
        return null;
    }
}
