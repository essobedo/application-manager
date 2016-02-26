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
public class BarVersionManager implements VersionManager<Bar> {
    @Override
    public Task<String> check(final Bar application) throws ApplicationException {
        throw new UnsupportedOperationException("#check()");
    }

    @Override
    public Task<Void> store(final Bar application, final OutputStream target) throws ApplicationException {
        throw new UnsupportedOperationException("#store()");
    }

    @Override
    public Task<Configuration> upgrade(final File upgradeRoot, final File appRoot, final String oldVersion)
        throws ApplicationException {
        throw new UnsupportedOperationException("#upgrade()");
    }
}
