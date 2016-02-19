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

import com.gitlab.essobedo.appma.core.io.RootFolder;
import com.gitlab.essobedo.appma.exception.ApplicationException;
import com.gitlab.essobedo.appma.spi.Manageable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class TestDefaultApplicationManager {

    private File temp;
    private File patchTargetFile;

    @After
    public void destroy() {
        if (temp != null) {
            temp.delete();
        }
        if (patchTargetFile != null && patchTargetFile.exists()) {
            patchTargetFile.delete();
        }
    }

    private File getRootFolder(String folderName) {
        RootFolder folder = new RootFolder(TestDefaultApplicationManager.class);
        return new File(folder.getLocation(), folderName);
    }

    @Test
    public void testAppUndefined() throws Exception {
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.ko"), null);
        try {
            manager.create();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
    }

    @Test
    public void testAppUndefinedJar() throws Exception {
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.jar.ko"), null);
        try {
            manager.create();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
    }

    @Test
    public void testAppDefinedNotCompatible() throws Exception {
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.ko2"), null);
        try {
            manager.create();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
    }

    @Test
    public void testAppDefinedCompatibleJar() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.jar.ok"),
            new String[]{temp.getAbsolutePath()});
        Manageable application = manager.create();
        assertEquals("foo", application.name());
        assertEquals("1.0", application.version());
        assertEquals("foo", application.title());
        assertFalse(application.isJavaFX());
        assertNull(manager.init());
        manager.destroy();
        Properties properties = load(temp);
        assertEquals("true", properties.getProperty("init"));
        assertEquals("true", properties.getProperty("destroy"));
        assertEquals(2, properties.size());
    }

    @Test
    public void testAppDefinedCompatible() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.ok"),
            new String[]{temp.getAbsolutePath()});
        Manageable application = manager.create();
        assertEquals("foo", application.name());
        assertEquals("1.0", application.version());
        assertEquals("foo", application.title());
        assertFalse(application.isJavaFX());
        assertNull(manager.init());
        manager.destroy();
        Properties properties = load(temp);
        assertEquals("true", properties.getProperty("init"));
        assertEquals("true", properties.getProperty("destroy"));
        assertEquals(2, properties.size());
    }

    @Test
    public void testAppFailingInitJar() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.jar.init.ko"),
            new String[]{temp.getAbsolutePath()});
        Manageable application = manager.create();
        assertEquals("FooInitKO", application.name());
        assertEquals("1.0", application.version());
        assertEquals("FooInitKO", application.title());
        assertFalse(application.isJavaFX());
        try {
            manager.init();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
    }

    @Test
    public void testAppFailingInit() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.init.ko"),
            new String[]{temp.getAbsolutePath()});
        Manageable application = manager.create();
        assertEquals("FooInitKO", application.name());
        assertEquals("1.0", application.version());
        assertEquals("FooInitKO", application.title());
        assertFalse(application.isJavaFX());
        try {
            manager.init();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
    }

    @Test
    public void testAppFailingInit2Jar() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.jar.init.ko"),
            new String[]{temp.getAbsolutePath(), "foo"});
        Manageable application = manager.create();
        assertEquals("FooInitKO2", application.name());
        assertEquals("2.0", application.version());
        assertEquals("FooInitKO2", application.title());
        assertFalse(application.isJavaFX());
        try {
            manager.init();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
    }

    @Test
    public void testAppFailingInit2() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.init.ko"),
            new String[]{temp.getAbsolutePath(), "foo"});
        Manageable application = manager.create();
        assertEquals("FooInitKO2", application.name());
        assertEquals("2.0", application.version());
        assertEquals("FooInitKO2", application.title());
        assertFalse(application.isJavaFX());
        try {
            manager.init();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
    }

    @Test
    public void testAppFailingDestroyJar() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.jar.destroy.ko"),
            new String[]{temp.getAbsolutePath()});
        Manageable application = manager.create();
        assertEquals("FooDestroyKO", application.name());
        assertEquals("1.0", application.version());
        assertEquals("FooDestroyKO", application.title());
        assertFalse(application.isJavaFX());
        assertNull(manager.init());
        try {
            manager.destroy();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
        Properties properties = load(temp);
        assertEquals("true", properties.getProperty("init"));
        assertEquals(1, properties.size());
    }

    @Test
    public void testAppFailingDestroy() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.destroy.ko"),
            new String[]{temp.getAbsolutePath()});
        Manageable application = manager.create();
        assertEquals("FooDestroyKO", application.name());
        assertEquals("1.0", application.version());
        assertEquals("FooDestroyKO", application.title());
        assertFalse(application.isJavaFX());
        assertNull(manager.init());
        try {
            manager.destroy();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
        Properties properties = load(temp);
        assertEquals("true", properties.getProperty("init"));
        assertEquals(1, properties.size());
    }

    @Test
    public void testAppFailingDestroy2Jar() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.jar.destroy.ko"),
            new String[]{temp.getAbsolutePath(), "foo"});
        Manageable application = manager.create();
        assertEquals("FooDestroyKO2", application.name());
        assertEquals("2.0", application.version());
        assertEquals("FooDestroyKO2", application.title());
        assertFalse(application.isJavaFX());
        assertNull(manager.init());
        try {
            manager.destroy();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
        Properties properties = load(temp);
        assertEquals("true", properties.getProperty("init"));
        assertEquals(1, properties.size());
    }

    @Test
    public void testAppFailingDestroy2() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.destroy.ko"),
            new String[]{temp.getAbsolutePath(), "foo"});
        Manageable application = manager.create();
        assertEquals("FooDestroyKO2", application.name());
        assertEquals("2.0", application.version());
        assertEquals("FooDestroyKO2", application.title());
        assertFalse(application.isJavaFX());
        assertNull(manager.init());
        try {
            manager.destroy();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
        Properties properties = load(temp);
        assertEquals("true", properties.getProperty("init"));
        assertEquals(1, properties.size());
    }

    @Test
    public void testAppDep1() throws Exception {
        testApp("app.dep.ok1");
    }

    @Test
    public void testAppDep2() throws Exception {
        testApp("app.dep.ok2");
    }

    @Test
    public void testAppDep3() throws Exception {
        try {
            System.setProperty("essobedo.appma.core.config", "foo.properties");
            testApp("app.dep.ok3");
        } finally {
            System.clearProperty("essobedo.appma.core.config");
        }
    }

    @Test
    public void testAppDep4() throws Exception {
        testApp("app.dep.ok4");
    }

    @Test
    public void testAppDep5() throws Exception {
        testApp("app.dep.ok5");
    }

    @Test
    public void testAppDep6() throws Exception {
        testApp("app.dep.ok6");
    }

    @Test
    public void testAppDepInvalid1() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.dep.ko1"),
            new String[]{temp.getAbsolutePath()});
        Manageable application = manager.create();
        assertEquals("FooDep", application.name());
        assertEquals("1.0", application.version());
        assertEquals("FooDep", application.title());
        assertFalse(application.isJavaFX());
        try {
            manager.init();
            fail("An NoClassDefFoundError is expected");
        } catch (NoClassDefFoundError e) {
            // expected
        }
    }

    @Test
    public void testAppDepInvalid2() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        try {
            new DefaultApplicationManager(getRootFolder("app.dep.ko2"),
                new String[]{temp.getAbsolutePath()});
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
    }

    @Test
    public void testAppDepInvalid3() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.dep.ko3"),
            new String[]{temp.getAbsolutePath()});
        try {
            manager.create();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
    }

    @Test
    public void testAppDepInvalid4() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder("app.dep.ko4"),
            new String[]{temp.getAbsolutePath()});
        Manageable application = manager.create();
        assertEquals("FooDep", application.name());
        assertEquals("1.0", application.version());
        assertEquals("FooDep", application.title());
        assertFalse(application.isJavaFX());
        try {
            manager.init();
            fail("An NoClassDefFoundError is expected");
        } catch (NoClassDefFoundError e) {
            // expected
        }
    }

    @Test
    public void testAppUpgrade() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        this.patchTargetFile = File.createTempFile("TestDefaultApplicationManager", "tmp");
        File patchContentTargetFolder = new File(patchTargetFile.getParentFile(), "patchContentTargetFolder");
        String folderName = "app.upgrade.ok";
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder(folderName),
            new String[]{temp.getAbsolutePath()}, patchTargetFile, patchContentTargetFolder);
        Manageable application = manager.create();
        assertEquals("FooDep", application.name());
        assertEquals("1.0", application.version());
        assertEquals("FooDep", application.title());
        assertFalse(application.isJavaFX());
        assertNull(manager.init());
        Properties properties = load(temp);
        assertEquals("true", properties.getProperty("init"));
        assertEquals(1, properties.size());
        application = null;
        ApplicationManager applicationManager = manager;
        try {
            System.setProperty("test.folder", folderName);
            assertEquals("2.0", applicationManager.checkForUpdate().execute());
            manager.doUpgrade();
        } finally {
            System.clearProperty("test.folder");
        }
        application = manager.getApplication();
        assertNotNull(application);
        assertEquals("FooDep2", application.name());
        assertEquals("2.0", application.version());
        assertEquals("FooDepTitle2", application.title());
        assertFalse(application.isJavaFX());
        try {
            manager.create();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
        try {
            manager.init();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
        applicationManager.onExit();
        properties = load(temp);
        assertEquals("2", properties.getProperty("init"));
        assertEquals("2", properties.getProperty("destroy"));
        assertEquals(2, properties.size());
        assertFalse(patchTargetFile.exists());
        assertFalse(patchContentTargetFolder.exists());
    }

    @Test
    public void testAppUpgrade2() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        this.patchTargetFile = File.createTempFile("TestDefaultApplicationManager", "tmp");
        File patchContentTargetFolder = new File(patchTargetFile.getParentFile(), "patchContentTargetFolder");
        String folderName = "app.upgrade.ok2";
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder(folderName),
            new String[]{temp.getAbsolutePath()}, patchTargetFile, patchContentTargetFolder);
        Manageable application = manager.create();
        assertEquals("FooDep", application.name());
        assertEquals("1.0", application.version());
        assertEquals("FooDep", application.title());
        assertFalse(application.isJavaFX());
        assertNull(manager.init());
        Properties properties = load(temp);
        assertEquals("true", properties.getProperty("init"));
        assertEquals(1, properties.size());
        application = null;
        ApplicationManager applicationManager = manager;
        try {
            System.setProperty("test.folder", folderName);
            assertEquals("2.0", applicationManager.checkForUpdate().execute());
            manager.upgrade();
            while (manager.getApplication() == null || manager.getApplication().version().equals("1.0")){
                Thread.sleep(100);
            }
        } finally {
            System.clearProperty("test.folder");
        }
        application = manager.getApplication();
        assertNotNull(application);
        assertEquals("FooDep2", application.name());
        assertEquals("2.0", application.version());
        assertEquals("FooDepTitle2", application.title());
        assertFalse(application.isJavaFX());
        try {
            manager.create();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
        try {
            manager.init();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        }
        applicationManager.onExit();
        properties = load(temp);
        assertEquals("2", properties.getProperty("init"));
        assertEquals("2", properties.getProperty("destroy"));
        assertEquals(2, properties.size());
        assertFalse(patchTargetFile.exists());
        assertFalse(patchContentTargetFolder.exists());
    }

    @Test
    public void testAppUpgrade3() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        String folderName = "app.upgrade.ok3";
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder(folderName),
            new String[]{temp.getAbsolutePath()});
        Manageable application = manager.create();
        assertEquals("foo", application.name());
        assertEquals("1.0", application.version());
        assertEquals("foo", application.title());
        assertFalse(application.isJavaFX());
        assertNull(manager.init());
        Properties properties = load(temp);
        assertEquals("true", properties.getProperty("init"));
        assertEquals(1, properties.size());
        application = null;
        ApplicationManager applicationManager = manager;
        assertEquals("2.0", applicationManager.checkForUpdate().execute());
        applicationManager.onExit();
        properties = load(temp);
        assertEquals("true", properties.getProperty("init"));
        assertEquals("true", properties.getProperty("destroy"));
        assertEquals(2, properties.size());
    }

    @Test
    public void testNoVersionManager() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        this.patchTargetFile = File.createTempFile("TestDefaultApplicationManager", "tmp");
        File patchContentTargetFolder = new File(patchTargetFile.getParentFile(), "patchContentTargetFolder");
        String folderName = "app.upgrade.ko";
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder(folderName),
            new String[]{temp.getAbsolutePath()}, patchTargetFile, patchContentTargetFolder);
        Manageable application = manager.create();
        assertEquals("foo", application.name());
        assertEquals("1.0", application.version());
        assertEquals("foo", application.title());
        assertFalse(application.isJavaFX());
        assertNull(manager.init());
        Properties properties = load(temp);
        assertEquals("true", properties.getProperty("init"));
        assertEquals(1, properties.size());
        application = null;
        ApplicationManager applicationManager = manager;
        try {
            System.setProperty("test.folder", folderName);
            assertNull(applicationManager.checkForUpdate());
        } finally {
            System.clearProperty("test.folder");
        }
        try {
            System.setProperty("test.folder", folderName);
            manager.doUpgrade();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        } finally {
            System.clearProperty("test.folder");
        }
    }

    @Test
    public void testNoVersionManager2() throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        this.patchTargetFile = File.createTempFile("TestDefaultApplicationManager", "tmp");
        File patchContentTargetFolder = new File(patchTargetFile.getParentFile(), "patchContentTargetFolder");
        String folderName = "app.upgrade.ko2";
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder(folderName),
            new String[]{temp.getAbsolutePath()}, patchTargetFile, patchContentTargetFolder);
        Manageable application = manager.create();
        assertEquals("foo", application.name());
        assertEquals("1.0", application.version());
        assertEquals("foo", application.title());
        assertFalse(application.isJavaFX());
        assertNull(manager.init());
        Properties properties = load(temp);
        assertEquals("true", properties.getProperty("init"));
        assertEquals(1, properties.size());
        application = null;
        ApplicationManager applicationManager = manager;
        try {
            System.setProperty("test.folder", folderName);
            assertNull(applicationManager.checkForUpdate());
        } finally {
            System.clearProperty("test.folder");
        }
        try {
            System.setProperty("test.folder", folderName);
            manager.doUpgrade();
            fail("An ApplicationException is expected");
        } catch (ApplicationException e) {
            // expected
        } finally {
            System.clearProperty("test.folder");
        }
    }

    private void testApp(String folderName) throws Exception {
        this.temp = File.createTempFile("TestDefaultApplicationManager", "tmp");
        DefaultApplicationManager manager = new DefaultApplicationManager(getRootFolder(folderName),
            new String[]{temp.getAbsolutePath()});
        Manageable application = manager.create();
        assertEquals("FooDep", application.name());
        assertEquals("1.0", application.version());
        assertEquals("FooDep", application.title());
        assertFalse(application.isJavaFX());
        assertNull(manager.init());
        manager.destroy();
        Properties properties = load(temp);
        assertEquals("true", properties.getProperty("init"));
        assertEquals("true", properties.getProperty("destroy"));
        assertEquals(2, properties.size());
    }

    private static Properties load(File file) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(file)) {
            properties.load(input);
        }
        return properties;
    }
}
