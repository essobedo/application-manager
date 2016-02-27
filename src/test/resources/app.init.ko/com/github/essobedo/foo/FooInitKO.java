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

import com.github.essobedo.appma.core.ApplicationManager;
import com.github.essobedo.appma.exception.ApplicationException;
import com.github.essobedo.appma.spi.Manageable;
import javafx.scene.Scene;
import javafx.scene.image.Image;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class FooInitKO implements Manageable {

    @Override
    public String name() {
        return "FooInitKO";
    }

    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public String title() {
        return "FooInitKO";
    }

    @Override
    public Image icon() {
        throw new UnsupportedOperationException("#icon()");
    }

    @Override
    public boolean accept(final String[] arguments) {
        return arguments != null && arguments.length == 1;
    }

    @Override
    public boolean isJavaFX() {
        return false;
    }

    @Override
    public Scene init(final ApplicationManager manager, final String[] arguments) throws ApplicationException {
        throw new ApplicationException("Could not init FooInitKO");
    }

    @Override
    public void destroy() throws ApplicationException {
    }
}
