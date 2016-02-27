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
package com.github.essobedo.appma.core;

import com.github.essobedo.appma.core.DefaultApplicationManager;
import com.github.essobedo.appma.exception.ApplicationException;
import com.github.essobedo.appma.spi.Manageable;
import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class TestAppFXUpgrade extends ApplicationTest implements ChangeListener<Scene> {
    private CountDownLatch latch = new CountDownLatch(1);
    private final String folderName = "app.upgrade.fx.ok";
    private File temp;
    private File patchTargetFile;
    private File patchContentTargetFolder;
    private DefaultApplicationManager manager;

    public void stop() {
        if (temp != null) {
            temp.delete();
        }
        if (patchTargetFile != null && patchTargetFile.exists()) {
            patchTargetFile.delete();
        }
        if (patchContentTargetFolder != null && patchContentTargetFolder.exists()) {
            patchContentTargetFolder.delete();
        }
        manager.getStage().sceneProperty().removeListener(this);
        try {
            manager.destroy();
        } catch (ApplicationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testAppFXUpgrade() throws Exception {
        latch.await();
        latch = new CountDownLatch(1);
        Manageable application = manager.getApplication();
        assertEquals("foo FX", application.name());
        assertEquals("1.0", application.version());
        assertEquals("title FooFX", application.title());
        assertTrue(application.isJavaFX());
        verifyThat("#label", hasText("foo"));
        ApplicationManager applicationManager = manager;
        try {
            System.setProperty("test.folder", folderName);
            assertEquals("2.0", applicationManager.checkForUpdate().execute());
            manager.doUpgrade();
        } finally {
            System.clearProperty("test.folder");
        }
        assertFalse(patchTargetFile.exists());
        assertFalse(patchContentTargetFolder.exists());
        latch.await();
    }

    @Test
    public void testAppFXUpgraded() throws Exception {
        latch.await();
        Manageable application = manager.getApplication();
        assertNotNull(application);
        assertEquals("foo FX", application.name());
        assertEquals("2.0", application.version());
        assertEquals("title FooFX", application.title());
        assertTrue(application.isJavaFX());
        verifyThat("#label", hasText("bar"));
    }

    @Override
    public void start(final Stage stage) throws Exception {
        this.temp = File.createTempFile("TestAppFXUpgrade", "tmp");
        this.patchTargetFile = File.createTempFile("TestAppFXUpgrade", "tmp");
        this.patchContentTargetFolder = new File(Files.createTempDirectory("patchContentTargetFolder").toString());
        this.manager = new DefaultApplicationManager(
            TestDefaultApplicationManager.getRootFolder(folderName),
            patchTargetFile, patchContentTargetFolder, temp.getAbsolutePath());
        manager.create();
        Scene scene = new Scene(new VBox(), 200.0d, 150.0d);
        stage.setScene(scene);
        stage.show();
        stage.sceneProperty().addListener(this);
        manager.asyncInitNShow(stage, null);
    }

    @Override
    public void changed(final ObservableValue<? extends Scene> observable, final Scene oldValue, final Scene newValue) {
        latch.countDown();
    }
}
