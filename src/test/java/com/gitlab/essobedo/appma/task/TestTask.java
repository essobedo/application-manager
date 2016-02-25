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
package com.gitlab.essobedo.appma.task;

import com.gitlab.essobedo.appma.exception.TaskInterruptedException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class TestTask {

    @Test
    public void testName() {
        Task<?> task = new Task<Void>("foo") {
            @Override
            public boolean cancelable() {
                throw new UnsupportedOperationException("#cancelable()");
            }

            @Override
            public Void execute()  {
                throw new UnsupportedOperationException("#execute()");
            }
        };
        assertEquals("foo", task.getName());
    }

    @Test
    public void updateProgress() throws Exception {
        Task<?> task = new Task<Object>("foo") {
            @Override
            public boolean cancelable() {
                throw new UnsupportedOperationException("#cancelable()");
            }

            @Override
            public Void execute() {
                updateProgress(2, 4);
                return null;

            }
        };
        AtomicInteger called = new AtomicInteger();
        AtomicInteger done = new AtomicInteger();
        AtomicInteger max = new AtomicInteger();
        task.addObserver((o, arg) -> {
            called.incrementAndGet();
            done.set(((Task<?>)o).getWorkDone());
            max.set(((Task<?>)o).getMax());
        });
        assertEquals(0, task.getWorkDone());
        assertEquals(0, task.getMax());
        assertEquals(0, called.get());
        assertEquals(task.getWorkDone(), done.get());
        assertEquals(task.getMax(), max.get());
        task.updateProgress(1, 3);
        assertEquals(1, task.getWorkDone());
        assertEquals(3, task.getMax());
        assertEquals(1, called.get());
        assertEquals(task.getWorkDone(), done.get());
        assertEquals(task.getMax(), max.get());
        task.execute();
        assertEquals(2, task.getWorkDone());
        assertEquals(4, task.getMax());
        assertEquals(2, called.get());
        assertEquals(task.getWorkDone(), done.get());
        assertEquals(task.getMax(), max.get());
    }

    @Test
    public void updateMessage() throws Exception {
        Task<?> task = new Task<Object>("foo") {
            @Override
            public boolean cancelable() {
                throw new UnsupportedOperationException("#cancelable()");
            }

            @Override
            public Void execute() {
                updateMessage("execute in");
                return null;

            }
        };
        AtomicInteger called = new AtomicInteger();
        AtomicReference<String> message = new AtomicReference<>();
        task.addObserver((o, arg) -> {
            called.incrementAndGet();
            message.set(((Task<?>)o).getMessage());
        });
        assertNull(task.getMessage());
        assertEquals(0, called.get());
        assertNull(message.get());
        task.updateMessage("execute out");
        assertEquals("execute out", task.getMessage());
        assertEquals(1, called.get());
        assertEquals(task.getMessage(), message.get());
        task.execute();
        assertEquals("execute in", task.getMessage());
        assertEquals(2, called.get());
        assertEquals(task.getMessage(), message.get());
    }

    @Test
    public void cancel() throws Exception {
        AtomicBoolean cancelable = new AtomicBoolean();
        Task<?> task = new Task<Object>("foo") {
            @Override
            public boolean cancelable() {
                return cancelable.get();
            }

            @Override
            public Void execute() throws TaskInterruptedException {
                if (isCanceled()) {
                    throw new TaskInterruptedException();
                }
                return null;

            }
        };
        AtomicInteger called = new AtomicInteger();
        AtomicBoolean canceled = new AtomicBoolean();
        task.addObserver((o, arg) -> {
            called.incrementAndGet();
            canceled.set(((Task<?>)o).isCanceled());
        });
        assertFalse(task.cancelable());
        task.cancel();
        assertFalse(task.isCanceled());
        assertEquals(0, called.get());
        assertEquals(task.isCanceled(), canceled.get());
        cancelable.set(true);
        assertTrue(task.cancelable());
        assertFalse(task.isCanceled());
        assertEquals(0, called.get());
        assertEquals(task.isCanceled(), canceled.get());
        task.execute();
        assertFalse(task.isCanceled());
        assertEquals(0, called.get());
        assertEquals(task.isCanceled(), canceled.get());
        task.cancel();
        assertTrue(task.isCanceled());
        assertEquals(1, called.get());
        assertEquals(task.isCanceled(), canceled.get());
        try {
            task.execute();
            fail("A TaskInterruptedException was expected");
        } catch (TaskInterruptedException e) {
            // expected
        }
    }
}
