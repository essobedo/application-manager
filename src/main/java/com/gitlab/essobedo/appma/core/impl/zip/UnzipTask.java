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

import com.gitlab.essobedo.appma.task.Task;
import com.gitlab.essobedo.appma.exception.ApplicationException;
import com.gitlab.essobedo.appma.i18n.Localization;
import java.io.File;
import java.io.IOException;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class UnzipTask extends Task<Void> {

    private final File zipFile;
    private final File destFolder;

    public UnzipTask(final File zipFile, final File destFolder) {
        super(Localization.getMessage("unzip.patch"));
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
            final ZipFile file = new ZipFile(zipFile);
            file.unzip(destFolder);
        } catch (IOException e) {
            throw new ApplicationException("Could not unzip the patch", e);
        }
        return null;
    }
}
