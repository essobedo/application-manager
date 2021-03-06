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

import com.github.essobedo.appma.core.Configuration;
import com.github.essobedo.appma.exception.ApplicationException;
import com.github.essobedo.appma.spi.VersionManager;
import com.github.essobedo.appma.task.Task;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class FooFXVersionManager implements VersionManager<FooFX> {
    @Override
    public Task<String> check(final FooFX application) throws ApplicationException {
        return new Task<String>("Check") {
            @Override
            public boolean cancelable() {
                return true;
            }

            @Override
            public String execute() throws ApplicationException {
                return "2.0";
            }
        };
    }
    @Override
    public Task<Void> store(final FooFX application, final OutputStream target) throws ApplicationException {
        return new Task<Void>("store") {
            @Override
            public boolean cancelable() {
                return true;
            }

            @Override
            public Void execute() throws ApplicationException {
                byte[] bytesIn = new byte[1024];
                try (final BufferedOutputStream bos = new BufferedOutputStream(target);
                     final InputStream inputStream = new FileInputStream(
                         new File(new File(FooFXVersionManager.class.getResource("/").toURI()),
                             System.getProperty("test.folder") + "/upgrade.zip"))) {
                    int read;
                    while ((read = inputStream.read(bytesIn)) != -1) {
                        bos.write(bytesIn, 0, read);
                    }
                } catch (Exception e) {
                    throw new ApplicationException("Could not store the patch", e);
                }
                return null;
            }
        };
    }
    @Override
    public Task<Configuration> upgrade(final File upgradeRoot, final File appRoot, final String oldVersion) throws ApplicationException {
        return new Task<Configuration>("Upgrade") {
            @Override
            public boolean cancelable() {
                return false;
            }

            @Override
            public Configuration execute() throws ApplicationException {
                try {
                    Files.copy(Paths.get(upgradeRoot.getAbsolutePath(), "com/github/essobedo/foo/FooFX.class"),
                        Paths.get(appRoot.getAbsolutePath(), "com/github/essobedo/foo/FooFX.class"), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new ApplicationException("Could not apply patch", e);
                }
                return null;
            }
        };
    }
}
