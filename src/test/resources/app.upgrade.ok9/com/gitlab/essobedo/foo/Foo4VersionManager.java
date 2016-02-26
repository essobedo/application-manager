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
package com.gitlab.essobedo.foo;

import com.gitlab.essobedo.appma.core.Configuration;
import com.gitlab.essobedo.appma.core.io.Folder;
import com.gitlab.essobedo.appma.exception.ApplicationException;
import com.gitlab.essobedo.appma.spi.VersionManager;
import com.gitlab.essobedo.appma.task.Task;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class Foo4VersionManager implements VersionManager<Foo4> {
    @Override
    public Task<String> check(final Foo4 application) throws ApplicationException {
        return new Task<String>("Check") {
            @Override
            public boolean cancelable() {
                return true;
            }

            @Override
            public String execute() throws ApplicationException {
                try {
                    if (new File(new File(Foo4VersionManager.class.getResource("/").toURI()),
                        System.getProperty("test.folder") + "/upgrade.zip").exists()) {
                        return "2.0";
                    }
                    return null;
                } catch (URISyntaxException e) {
                    throw new ApplicationException("Could not check for update", e);
                }
            }
        };
    }

    @Override
    public Task<Void> store(final Foo4 application, final OutputStream target) throws ApplicationException {

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
                         new File(new File(Foo4VersionManager.class.getResource("/").toURI()),
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
    public Task<Configuration> upgrade(final File upgradeRoot, final File appRoot, final String oldVersion)
        throws ApplicationException {
        return new Task<Configuration>("Upgrade") {
            @Override
            public boolean cancelable() {
                return false;
            }

            @Override
            public Configuration execute() throws ApplicationException {
                try {
                    File[] files = appRoot.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        File file = files[i];
                        if ("upgrade.zip".equals(file.getName())) {
                            continue;
                        } else if (file.isFile()) {
                            Files.delete(Paths.get(file.getAbsolutePath()));
                        } else {
                            Folder folder = new Folder(file);
                            folder.delete();
                        }
                    }
                    files = upgradeRoot.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        File file = files[i];
                        if (file.isFile()) {
                            Files.copy(Paths.get(file.getAbsolutePath()),
                                Paths.get(appRoot.getAbsolutePath(), file.getName()));
                        } else {
                            Folder folder = new Folder(file);
                            folder.copy(new File(appRoot, file.getName()));
                        }
                    }
                } catch (IOException e) {
                    throw new ApplicationException("Could not apply patch", e);
                }
                return null;
            }
        };
    }
}

