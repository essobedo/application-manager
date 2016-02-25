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
package com.gitlab.essobedo.appma.core.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class allowing to manipulate a folder and its content.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class Folder {

    /**
     * The logger of the class.
     */
    private static final Logger LOG = Logger.getLogger(Folder.class.getName());

    /**
     * The folder to manipulate.
     */
    private final File folderAsFile;

    /**
     * Constructs a {@code Folder} using the specified {@code File} as corresponding
     * folder.
     * @param folder the folder to manipulate.
     */
    public Folder(final File folder) {
        this.folderAsFile = folder;
    }

    /**
     * Deletes the folder and its content.
     */
    public void delete() {
        try {
            final Path directory = Paths.get(folderAsFile.getAbsolutePath());
            Files.walkFileTree(directory, new DeleteFileVisitor());
        } catch (IOException e) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, String.format("Could not delete the content of the folder '%s'",
                    folderAsFile.getAbsolutePath()), e);
            }
        }
    }

    /**
     * Copies the folder and its content to the specified location.
     * @param destination the folder in which the content of the folder will be copied
     */
    public void copy(final File destination) {
        try {
            final Path directory = Paths.get(folderAsFile.getAbsolutePath());
            final Path target = Paths.get(destination.getAbsolutePath());
            Files.walkFileTree(directory, new CopyFileVisitor(directory, target));
        } catch (IOException e) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, String.format("Could not copy the content of the folder '%s'",
                    folderAsFile.getAbsolutePath()), e);
            }
        }
    }

    /**
     * Class allowing to delete the content of the folder.
     */
    private static class DeleteFileVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Class allowing to copy the content of the folder.
     */
    private static class CopyFileVisitor extends SimpleFileVisitor<Path> {
        /**
         * The source directory.
         */
        private final Path directory;
        /**
         * The target directory.
         */
        private final Path target;

        /**
         * Constructs a {@code CopyFileVisitor} using the specified source and target directories.
         * @param directory the source directory.
         * @param target the target directory.
         */
        CopyFileVisitor(final Path directory, final Path target) {
            this.directory = directory;
            this.target = target;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.copy(file, target.resolve(directory.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
            throws IOException {
            Files.copy(dir, target.resolve(directory.relativize(dir)), StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }
    }
}
