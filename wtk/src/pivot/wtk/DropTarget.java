/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import pivot.wtk.data.Manifest;

/**
 * Interface representing a drop target.
 *
 * @author gbrown
 */
public interface DropTarget {
    /**
     * Called when the mouse first enters a drop target during a drag
     * operation.
     *
     * @param component
     * @param dragContent
     * @param supportedDropActions
     * @param userDropAction
     *
     * @return
     * The drop action that would result if the user dropped the item at this
     * location, or <tt>null</tt> if the target cannot accept the drop.
     */
    public DropAction dragEnter(Component component, Manifest dragContent,
        int supportedDropActions, DropAction userDropAction);

    /**
     * Called when the mouse leaves a drop target during a drag operation.
     *
     * @param component
     */
    public void dragExit(Component component);

    /**
     * Called when the mouse is moved while positioned over a drop target
     * during a drag operation.
     *
     * @param component
     * @param dragContent
     * @param supportedDropActions
     * @param x
     * @param y
     * @param userDropAction
     *
     * @return
     * The drop action that would result if the user dropped the item at this
     * location, or <tt>null</tt> if the target cannot accept the drop.
     */
    public DropAction dragMove(Component component, Manifest dragContent,
        int supportedDropActions, int x, int y, DropAction userDropAction);

    /**
     * Called when the user drop action changes while the mouse is positioned
     * over a drop target during a drag operation.
     *
     * @param component
     * @param dragContent
     * @param supportedDropActions
     * @param x
     * @param y
     * @param userDropAction
     *
     * @return
     * The drop action that would result if the user dropped the item at this
     * location, or <tt>null</tt> if the target cannot accept the drop.
     */
    public DropAction userDropActionChange(Component component, Manifest dragContent,
        int supportedDropActions, int x, int y, DropAction userDropAction);

    /**
     * Called to drop the drag content.
     *
     * @param component
     * @param dragContent
     * @param supportedDropActions
     * @param x
     * @param y
     * @param userDropAction
     *
     * @return
     * The drop action used to perform the drop, or <tt>null</tt> if the target
     * rejected the drop.
     */
    public DropAction drop(Component component, Manifest dragContent,
        int supportedDropActions, int x, int y, DropAction userDropAction);
}
