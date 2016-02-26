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

import com.gitlab.essobedo.appma.core.ApplicationManager;
import com.gitlab.essobedo.appma.exception.ApplicationException;
import com.gitlab.essobedo.appma.spi.Manageable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class FooFX implements Manageable {
    
    @Override
    public String name() {
        return "foo FX";
    }
    @Override
    public String version() {
        return "1.0";
    }
    @Override
    public String title() {
        return "title FooFX";
    }
    @Override
    public Image icon() {
        return null;
    }
    @Override
    public boolean accept(final String... arguments) {
        return true;
    }
    @Override
    public boolean isJavaFX() {
        return true;
    }
    @Override
    public Scene init(final ApplicationManager manager, final String... arguments) throws ApplicationException {
        final VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        final Label label = new Label("foo");
        label.setId("label");
        vBox.getChildren().addAll(label);
        return new Scene(vBox, 100.0d, 50.0d);
    }
    @Override
    public void destroy() throws ApplicationException {
    }
}
