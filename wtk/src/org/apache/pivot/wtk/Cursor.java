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

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration defining the supported mouse cursor types, and
 * providing a mapping to the underlying {@link java.awt.Cursor} values.
 */
public enum Cursor {
    DEFAULT(java.awt.Cursor.DEFAULT_CURSOR),
    HAND(java.awt.Cursor.HAND_CURSOR),
    TEXT(java.awt.Cursor.TEXT_CURSOR),
    WAIT(java.awt.Cursor.WAIT_CURSOR),
    CROSSHAIR(java.awt.Cursor.CROSSHAIR_CURSOR),
    MOVE(java.awt.Cursor.MOVE_CURSOR),
    RESIZE_NORTH(java.awt.Cursor.N_RESIZE_CURSOR),
    RESIZE_SOUTH(java.awt.Cursor.S_RESIZE_CURSOR),
    RESIZE_EAST(java.awt.Cursor.E_RESIZE_CURSOR),
    RESIZE_WEST(java.awt.Cursor.W_RESIZE_CURSOR),
    RESIZE_NORTH_EAST(java.awt.Cursor.NE_RESIZE_CURSOR),
    RESIZE_NORTH_WEST(java.awt.Cursor.NW_RESIZE_CURSOR),
    RESIZE_SOUTH_EAST(java.awt.Cursor.SE_RESIZE_CURSOR),
    RESIZE_SOUTH_WEST(java.awt.Cursor.SW_RESIZE_CURSOR);

    private int cursorID;

    /** Facilitate lookup of one of our values given the AWT equivalent. */
    private static class Lookup {
        /** Correspondence from the AWT cursor ID to our own. */
        private static Map<Integer, Cursor> map = new HashMap<>();
    }

    Cursor(final int id) {
        this.cursorID = id;
        Lookup.map.put(id, this);
    }

    /**
     * @return A {@link java.awt.Cursor} object corresponding to this {@code Cursor}.
     */
    public java.awt.Cursor getAWTCursor() {
        return new java.awt.Cursor(cursorID);
    }

    /**
     * @return One of our {@code Cursor} values given the {@link java.awt.Cursor} ID value.
     * @param cursorID One of the {@link java.awt.Cursor} ID values to lookup.
     */
    public static Cursor getCursor(final int cursorID) {
        Cursor cursor = Lookup.map.get(cursorID);
        if (cursor != null) {
            return cursor;
        }
        throw new IllegalArgumentException("Unknown mouse cursor type " + cursorID);

    }
}
