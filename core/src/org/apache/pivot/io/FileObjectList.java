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
package org.apache.pivot.io;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.vfs2.FileObject;
import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.collections.adapter.ListAdapter;

/**
 * Collection representing a list of files. Each entry in the list is unique;
 * i.e. a single file can't be added to the list more than once.
 */
public class FileObjectList extends ListAdapter<FileObject> {
    private static final long serialVersionUID = 1327073299912864048L;

    private static class FilePathComparator implements Comparator<FileObject>, Serializable {
        private static final long serialVersionUID = -4470959470309721902L;

        @Override
        public int compare(FileObject file1, FileObject file2) {
            String path1 = file1.getName().getPath();
            String path2 = file2.getName().getPath();

            return path1.compareTo(path2);
        }
    }

    private static final FilePathComparator FILE_PATH_COMPARATOR = new FilePathComparator();

    public FileObjectList() {
        this(new java.util.ArrayList<FileObject>());
    }

    public FileObjectList(java.util.List<FileObject> files) {
        super(files);

        super.setComparator(FILE_PATH_COMPARATOR);
    }

    @Override
    public int add(FileObject file) {
        int index = indexOf(file);

        if (index == -1) {
            index = super.add(file);
        }

        return index;
    }

    @Override
    @UnsupportedOperation
    public void insert(FileObject file, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    @UnsupportedOperation
    public FileObject update(int index, FileObject file) {
        throw new UnsupportedOperationException();
    }

    @Override
    @UnsupportedOperation
    public void setComparator(Comparator<FileObject> comparator) {
        throw new UnsupportedOperationException();
    }
}
