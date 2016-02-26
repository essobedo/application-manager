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
package com.gitlab.essobedo.appma.core.progress;

import com.gitlab.essobedo.appma.exception.ApplicationException;
import com.gitlab.essobedo.appma.exception.TaskInterruptedException;
import com.gitlab.essobedo.appma.task.Task;
import org.junit.Test;

/**
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class TestLogProgress {

    @Test
    public void testAll() throws TaskInterruptedException, ApplicationException {
        Task<Void> task = new Task<Void>("foo") {
            @Override
            public boolean cancelable() {
                return true;
            }
            @Override
            public Void execute() throws ApplicationException, TaskInterruptedException {
                updateMessage("start");
                for (int i = 0; i < 10; i++) {
                    updateProgress(i + 1, 10);
                }
                updateMessage("end");
                return null;
            }
        };
        new LogProgress(task);
        task.execute();
        task.cancel();
    }
}
