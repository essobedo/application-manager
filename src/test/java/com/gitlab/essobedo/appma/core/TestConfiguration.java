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
package com.gitlab.essobedo.appma.core;

import com.gitlab.essobedo.appma.exception.ApplicationException;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.fail;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class TestConfiguration {

    private File temp;

    @After
    public void destroy() {
        if (temp != null) {
            temp.delete();
        }
    }

    @Test
    public void testClasspathAsUrlsFailure() throws IOException {
        Configuration configuration = new Configuration(new File("foo"));
        try {
            configuration.getClasspathAsUrls();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
        this.temp = File.createTempFile("TestConfiguration", "tmp");
        configuration = new Configuration(temp);
        try {
            configuration.getClasspathAsUrls();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
    }
}
