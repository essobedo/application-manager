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
import com.gitlab.essobedo.appma.exception.ApplicationException;
import com.gitlab.essobedo.appma.spi.VersionManager;
import com.gitlab.essobedo.appma.task.Task;
import java.io.File;
import java.io.OutputStream;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public abstract class AbstractBar2VersionManager implements VersionManager<Bar2> {
    @Override
    public Task<String> check(final Bar2 application) throws ApplicationException {
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
    public Task<Void> store(final Bar2 application, final OutputStream target) throws ApplicationException {

        return new Task<Void>("store") {
            @Override
            public boolean cancelable() {
                return true;
            }

            @Override
            public Void execute() throws ApplicationException {
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
            public Configuration execute() {
                return null;
            }
        };
    }
}
