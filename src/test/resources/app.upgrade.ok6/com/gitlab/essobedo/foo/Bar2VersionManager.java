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
public class Bar2VersionManager implements VersionManager<Bar2> {
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

