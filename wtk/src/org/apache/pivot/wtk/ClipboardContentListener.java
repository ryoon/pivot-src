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

/**
 * Clipboard content listener interface.
 */
public interface ClipboardContentListener {
    /**
     * Called when the content of the clipboard has been changed.
     *
     * @param previousContent What used to be on the clipboard before
     * the content changed. Note that this is a {@link LocalManifest}
     * because the only time this listener is registered is via
     * the {@link Clipboard#setContent(LocalManifest, ClipboardContentListener)}
     * method and so the previous content will always be "local" (that is,
     * generated/set by a Pivot application).
     * <p> The current content can be accessed using {@link Clipboard#getContent}.
     */
    void contentChanged(LocalManifest previousContent);
}
