/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.wtk;

import java.io.File;
import java.io.IOException;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.immutable.ImmutableList;
import org.apache.pivot.io.FileList;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Component representing a file browser.
 */
public class FileBrowser extends Container {
    /**
     * File browser skin interface.
     */
    public interface Skin extends org.apache.pivot.wtk.Skin {
        /**
         * Get the file selection at the given X/Y coordinates.
         * @param x The mouse X-position.
         * @param y The mouse Y-position.
         * @return The file displayed at the given coordinates.
         */
        File getFileAt(int x, int y);
    }

    /**
     * Value of the {@code "user.home"} system property (default root directory).
     */
    private static final String USER_HOME = System.getProperty("user.home");

    /**
     * The current root directory (usually set by constructor).
     */
    private File rootDirectory;
    /**
     * Current list of selected files (one for single select, can be many for multi-select).
     */
    private FileList selectedFiles = new FileList();
    /**
     * Whether multiple selection is enabled.
     */
    private boolean multiSelect = false;
    /**
     * Filter for files to disable (make non-selectable).
     */
    private Filter<File> disabledFileFilter = null;

    /**
     * List of listeners for events on this file browser.
     */
    private FileBrowserListener.Listeners fileBrowserListeners = new FileBrowserListener.Listeners();

    /**
     * Creates a new FileBrowser with the root directory set to the
     * <code>"user.home"</code> value.
     * <p> Note that this version sets the mode to "open".
     */
    public FileBrowser() {
        this(USER_HOME);
    }

    /**
     * Creates a new FileBrowser <p> Note that this version of the constructor
     * must be used when a custom root directory has to be set.
     *
     * @param rootDirectoryName The root directory full name.
     */
    public FileBrowser(final String rootDirectoryName) {
        Utils.checkNull(rootDirectoryName, "rootDirectoryName");
        internalSetRootDirectory(new File(rootDirectoryName), false);

        installSkin(FileBrowser.class);
    }

    /**
     * Creates a new FileBrowser <p> Note that this version of the constructor
     * must be used when a custom root directory has to be set.
     *
     * @param initialRootDirectory The initial root directory.
     */
    public FileBrowser(final File initialRootDirectory) {
        internalSetRootDirectory(initialRootDirectory, false);

        installSkin(FileBrowser.class);
    }

    /**
     * @return The current root directory.
     */
    public File getRootDirectory() {
        return rootDirectory;
    }

    /**
     * Sets the root directory. Clears any existing file selection.
     *
     * @param rootDir The new root directory to browse in.
     * @throws IllegalArgumentException if the argument is {@code null}
     * or is not a directory.
     */
    public void setRootDirectory(final File rootDir) {
        internalSetRootDirectory(rootDir, true);
    }

    /**
     * Set the root directory to the canonical path of the given directory, and optionally
     * notify the listeners of the change. If the listeners are notified, the existing
     * file selection is cleared.
     *
     * @param rootDir The new root directory value to set.
     * @param invokeListeners Whether or not to invoke the {@link #fileBrowserListeners}.
     * @throws IllegalArgumentException if the directory is {@code null} or is not a directory.
     */
    private void internalSetRootDirectory(final File rootDir, final boolean invokeListeners) {
        Utils.checkNull(rootDir, "rootDirectory");

        File newRootDirectory = rootDir;
        try {
            newRootDirectory = newRootDirectory.getCanonicalFile();
        } catch (IOException ioe) {
            // leave newRootDirectory as-is
        }

        if (!newRootDirectory.isDirectory()) {
            throw new IllegalArgumentException(newRootDirectory.getPath() + " is not a directory.");
        }

        if (newRootDirectory.exists()) {
            if (invokeListeners) {
                File previousRootDirectory = rootDirectory;

                if (!newRootDirectory.equals(previousRootDirectory)) {
                    rootDirectory = newRootDirectory;
                    selectedFiles.clear();
                    fileBrowserListeners.rootDirectoryChanged(this, previousRootDirectory);
                }
            } else {
                rootDirectory = newRootDirectory;
            }
        } else {
            setRootDirectory(newRootDirectory.getParentFile());
        }
    }

    /**
     * Adds a file to the file selection.
     *
     * @param file The new file to add to the selection.
     * @return {@code true} if the file was added; {@code false} if it was
     * already selected.
     * @throws IllegalArgumentException if the file argument is {@code null}
     * or if the file is not in the current root directory.
     */
    public boolean addSelectedFile(final File file) {
        Utils.checkNull(file, "file");

        File selectedFile = file;
        if (selectedFile.isAbsolute()) {
            try {
                selectedFile = selectedFile.getCanonicalFile();
            } catch (IOException ioe) {
                // leave the file as-is
            }
            if (!selectedFile.getParentFile().equals(rootDirectory)) {
                throw new IllegalArgumentException(selectedFile.getPath() + " is not a child of the root directory.");
            }
        } else {
            selectedFile = new File(rootDirectory, selectedFile.getPath());
        }

        int index = selectedFiles.add(selectedFile);
        if (index != -1) {
            fileBrowserListeners.selectedFileAdded(this, selectedFile);
        }

        return (index != -1);
    }

    /**
     * Removes a file from the file selection.
     *
     * @param file The previously selected file to be removed from the selection.
     * @return {@code true} if the file was removed; {@code false} if it was
     * not already selected.
     * @throws IllegalArgumentException if the file argument is {@code null}.
     */
    public boolean removeSelectedFile(final File file) {
        Utils.checkNull(file, "file");

        int index = selectedFiles.remove(file);
        if (index != -1) {
            fileBrowserListeners.selectedFileRemoved(this, file);
        }

        return (index != -1);
    }

    /**
     * When in single-select mode, returns the currently selected file.
     *
     * @return The currently selected file.
     */
    public File getSelectedFile() {
        if (multiSelect) {
            throw new IllegalStateException("File browser is not in single-select mode.");
        }

        return (selectedFiles.getLength() == 0) ? null : selectedFiles.get(0);
    }

    /**
     * Sets the selection to a single file.
     *
     * @param file The only file to select, or {@code null} to select nothing.
     */
    public void setSelectedFile(final File file) {
        if (file == null) {
            clearSelection();
        } else {
            if (file.isAbsolute()) {
                setRootDirectory(file.getParentFile());
            }

            setSelectedFiles(new ArrayList<>(file));
        }
    }

    /**
     * Returns the currently selected files.
     *
     * @return An immutable list containing the currently selected files. Note
     * that the returned list is a wrapper around the actual selection, not a
     * copy. Any changes made to the selection state will be reflected in the
     * list, but events will not be fired.
     */
    public ImmutableList<File> getSelectedFiles() {
        return new ImmutableList<>(selectedFiles);
    }

    /**
     * Sets the selected files.
     *
     * @param files The files to select.
     * @return The files that were selected, with duplicates eliminated.
     * @throws IllegalArgumentException if the selected files sequence is {@code null}
     * or if the sequence is longer than one file and multi-select is not enabled, or
     * if any entry is the sequence is {@code null} or whose parent is not the
     * current root directory.
     */
    public Sequence<File> setSelectedFiles(final Sequence<File> files) {
        Utils.checkNull(files, "selectedFiles");

        if (!multiSelect && files.getLength() > 1) {
            throw new IllegalArgumentException("Multi-select is not enabled.");
        }

        // Update the selection
        Sequence<File> previousSelectedFiles = getSelectedFiles();

        FileList fileList = new FileList();
        for (int i = 0, n = files.getLength(); i < n; i++) {
            File file = files.get(i);

            Utils.checkNull(file, "file");

            if (!file.isAbsolute()) {
                file = new File(rootDirectory, file.getPath());
            }

            File canonicalFile = file;
            try {
                canonicalFile = canonicalFile.getCanonicalFile();
            } catch (IOException ioe) {
                // Just leave canonicalFile as-is
            }

            if (!canonicalFile.getParentFile().equals(rootDirectory)) {
                throw new IllegalArgumentException(file.getPath() + " is not a child of the root directory.");
            }

            fileList.add(file);
        }

        selectedFiles = fileList;

        // Notify listeners
        fileBrowserListeners.selectedFilesChanged(this, previousSelectedFiles);

        return getSelectedFiles();
    }

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        setSelectedFiles(new ArrayList<File>());
    }

    /**
     * @return Whether or not the given file is selected.
     *
     * @param file The file to test.
     */
    public boolean isFileSelected(final File file) {
        return (selectedFiles.indexOf(file) != -1);
    }

    /**
     * @return The file browser's multi-select state.
     */
    public boolean isMultiSelect() {
        return multiSelect;
    }

    /**
     * Sets the file browser's multi-select state.
     *
     * @param selectState {@code true} if multi-select is enabled;
     * {@code false}, otherwise.
     */
    public void setMultiSelect(final boolean selectState) {
        if (multiSelect != selectState) {
            // Clear any existing selection
            selectedFiles.clear();

            multiSelect = selectState;

            fileBrowserListeners.multiSelectChanged(this);
        }
    }

    /**
     * Returns the current file filter.
     *
     * @return The current file filter, or {@code null} if no filter is set.
     */
    public Filter<File> getDisabledFileFilter() {
        return disabledFileFilter;
    }

    /**
     * Sets the file filter.
     *
     * @param disabledFilter The file filter to use, or {@code null} for no
     * filter.
     */
    public void setDisabledFileFilter(final Filter<File> disabledFilter) {
        Filter<File> previousDisabledFileFilter = disabledFileFilter;

        if (previousDisabledFileFilter != disabledFilter) {
            disabledFileFilter = disabledFilter;
            fileBrowserListeners.disabledFileFilterChanged(this, previousDisabledFileFilter);
        }
    }

    /**
     * Call the skin and return the file at the given position.
     *
     * @param x The mouse X-position.
     * @param y The mouse Y-position.
     * @return The file displayed at those coordinates.
     */
    public File getFileAt(final int x, final int y) {
        FileBrowser.Skin fileBrowserSkin = (FileBrowser.Skin) getSkin();
        return fileBrowserSkin.getFileAt(x, y);
    }

    /**
     * @return The file browser listeners.
     */
    public ListenerList<FileBrowserListener> getFileBrowserListeners() {
        return fileBrowserListeners;
    }
}
