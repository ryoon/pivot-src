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

import java.awt.Cursor;
import static java.awt.dnd.DnDConstants.ACTION_COPY;
import static java.awt.dnd.DnDConstants.ACTION_LINK;
import static java.awt.dnd.DnDConstants.ACTION_MOVE;
import static java.awt.dnd.DragSource.DefaultCopyDrop;
import static java.awt.dnd.DragSource.DefaultLinkDrop;
import static java.awt.dnd.DragSource.DefaultMoveDrop;
import java.awt.event.InputEvent;


/**
 * Enumeration defining supported drop actions.
 */
public enum DropAction {
    /**
     * The "copy" action.
     */
    COPY(ACTION_COPY, DefaultCopyDrop),
    /**
     * The "move" action.
     */
    MOVE(ACTION_MOVE, DefaultMoveDrop),
    /**
     * The "link" action.
     */
    LINK(ACTION_LINK, DefaultLinkDrop);

    /**
     * The corresponding native constant for the action.
     */
    private final int nativeValue;

    /**
     * The corresponding native cursor for this action.
     */
    private final Cursor nativeCursor;

    /**
     * Construct given the native constant value.
     * @param dndValue The native action constant.
     * @param cursorValue The native cursor for this action.
     */
    DropAction(final int dndValue, final Cursor cursorValue) {
        nativeValue = dndValue;
        nativeCursor = cursorValue;
    }

    /**
     * @return The bitmask for this value (<code>2 ** ordinal</code>).
     */
    public int getMask() {
        return 1 << ordinal();
    }

    /**
     * @return The native drop action for this action.
     */
    public int getNativeDropAction() {
        return nativeValue;
    }

    /**
     * @return The native cursor for this action.
     */
    public Cursor getNativeCursor() {
        return nativeCursor;
    }

    /**
     * Given a bitmask of the possible drop actions (from a source),
     * decide if this action (represented by its mask) is selected
     * in the given bitmask.
     * @param dropActions The bitmask of supported drop actions.
     * @return Whether or not this action is in the bitmask.
     */
    public boolean isSelected(final int dropActions) {
        return ((dropActions & getMask()) > 0);
    }

    /**
     * Get a bitmask of native source actions from a bitmask of
     * our values.
     * @param supported The supported actions bitmask.
     * @return The native action bitmask.
     */
    public static int getSourceActions(final int supported) {
        int awtSourceActions = 0;

        for (DropAction action : values()) {
            if (action.isSelected(supported)) {
                awtSourceActions |= action.nativeValue;
            }
        }

        return awtSourceActions;
    }

    /**
     * Convert a native drop action to one of these values.
     * @param nativeDropAction The native value.
     * @return The corresponding one of our values.
     */
    public static DropAction getDropAction(final int nativeDropAction) {
        DropAction dropAction = null;

        for (DropAction action : values()) {
            if (nativeDropAction == action.nativeValue) {
                dropAction = action;
                break;
            }
        }

        return dropAction;
    }

    /**
     * Get the bitmask of the supported drop actions from the given
     * native drop actions bitmask.
     * @param sourceActions The native supported actions.
     * @return Translated bitmask of our supported actions.
     */
    public static int getSupportedDropActions(final int sourceActions) {
        int dropActions = 0;

        for (DropAction action : values()) {
            if ((sourceActions & action.nativeValue) > 0) {
                dropActions |= action.getMask();
            }
        }

        return dropActions;
    }

    /**
     * Using the same logic as {@link Keyboard#getDropAction}, determinen the appropriate
     * action given the current keyboard modifiers (platform-dependent).
     * @param event The current event with all the current state.
     * @return The appropriate one of our values for the event.
     */
    public static DropAction getDropAction(final InputEvent event) {
        DropAction dropAction = null;

        if (Platform.isOSX()) {
            if (event.isAltDown() && event.isMetaDown()) {
                dropAction = LINK;
            } else if (event.isAltDown()) {
                dropAction = COPY;
            } else {
                dropAction = MOVE;
            }
        } else if (Platform.isWindows()) {
            if (event.isControlDown() && event.isShiftDown()) {
                dropAction = LINK;
            } else if (event.isControlDown()) {
                dropAction = COPY;
            } else {
                dropAction = MOVE;
            }
        } else {
            // Note: different desktop managers *may* have different conventions
            if (event.isControlDown() && event.isShiftDown()) {
                dropAction = LINK;
            } else if (event.isControlDown()) {
                dropAction = COPY;
            } else {
                dropAction = MOVE;
            }
        }

        return dropAction;
    }

}
