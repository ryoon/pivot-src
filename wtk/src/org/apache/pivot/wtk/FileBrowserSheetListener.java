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

/**
 * File browser sheet listener interface.
 *
 * @author gbrown
 */
public interface FileBrowserSheetListener {
    /**
     * Called when a file browser sheet's file name input label has changed.
     *
     * @param fileBrowserSheet
     * @param previousFileNameInputLabel
     */
    public void fileNameInputLabelChanged(FileBrowserSheet fileBrowserSheet, String previousFileNameInputLabel);

    /**
     * Called when a file browser sheet's show file name input flag has changed.
     *
     * @param fileBrowserSheet
     */
    public void showFileNameInputChanged(FileBrowserSheet fileBrowserSheet);

    /**
     * Called when a file browser sheet's selected file has changed.
     *
     * @param fileBrowserSheet
     * @param previousSelectedFile
     */
    public void selectedFileChanged(FileBrowserSheet fileBrowserSheet, File previousSelectedFile);
}
