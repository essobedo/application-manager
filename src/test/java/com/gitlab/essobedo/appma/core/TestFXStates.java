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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class TestFXStates extends ApplicationTest {
    private final String folderName = "app.upgrade.fx.ok3";

    private DefaultApplicationManager manager;

    public void stop() {
        try {
            manager.destroy();
        } catch (ApplicationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testStates() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            manager.asyncInitNShow(null, latch::countDown).get();
            fail("An ApplicationException is expected");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof ApplicationException);
        }
        latch.await();
    }

    @Override
    public void start(final Stage stage) throws Exception {
        this.manager = new DefaultApplicationManager(
            TestDefaultApplicationManager.getRootFolder(folderName));
        manager.create();
        Scene scene = new Scene(new VBox(), 200.0d, 150.0d);
        stage.setScene(scene);
        stage.show();
        manager.init();
    }
}
