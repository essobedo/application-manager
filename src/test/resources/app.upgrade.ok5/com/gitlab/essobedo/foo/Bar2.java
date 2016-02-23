/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Nicolas Filotto
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.gitlab.essobedo.foo;

import com.gitlab.essobedo.appma.core.ApplicationManager;
import com.gitlab.essobedo.appma.exception.ApplicationException;
import com.gitlab.essobedo.appma.spi.Manageable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import javafx.scene.Scene;
import javafx.scene.image.Image;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class Bar2 implements Manageable {

    private File file;
    private final Properties properties = new Properties();

    @Override
    public String name() {
        return "foo";
    }

    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public String title() {
        return "foo";
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
        properties.put("init", "true");
        this.file = new File(arguments[0]);
        try (final OutputStream out = new FileOutputStream(file)) {
            properties.store(out, null);
        } catch (IOException e) {
            throw new ApplicationException("Could store the file", e);
        }
        return null;
    }

    @Override
    public void destroy() throws ApplicationException {
        properties.put("destroy", "true");
        try (final OutputStream out = new FileOutputStream(file)) {
            properties.store(out, null);
        } catch (IOException e) {
            throw new ApplicationException("Could store the file", e);
        }
    }
}
