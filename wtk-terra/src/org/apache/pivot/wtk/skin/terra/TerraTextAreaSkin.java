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
package org.apache.pivot.wtk.skin.terra;

import org.apache.pivot.wtk.skin.TextAreaSkin;

/**
 * Terra TextArea skin. Deals with colors that depend on
 * the current theme.
 */
public class TerraTextAreaSkin extends TextAreaSkin {
    /**
     * Specific constructor with nothing to do.
     * <p> Default colors, etc. set by call to {@link TerraTheme#setDefaultStyles}
     * from {@link TextAreaSkin#install}.
     */
    public TerraTextAreaSkin() {
    }

    /**
     * Set the foreground color to the given theme color.
     *
     * @param color The theme color index for the foreground color.
     */
    public final void setColor(final int color) {
        setColor(getColor(color));
    }

    /**
     * Set the inactive color to the given theme color.
     *
     * @param inactiveColor The theme color index for the inactive color.
     */
    public final void setInactiveColor(final int inactiveColor) {
        setInactiveColor(getColor(inactiveColor));
    }

    /**
     * Set the background color to the given theme color.
     *
     * @param backgroundColor The theme color index for the background color.
     */
    public final void setBackgroundColor(final int backgroundColor) {
        setBackgroundColor(getColor(backgroundColor));
    }

    /**
     * Set the selection color to the given theme color.
     *
     * @param selectionColor The theme color index for the selection color.
     */
    public final void setSelectionColor(final int selectionColor) {
        setSelectionColor(getColor(selectionColor));
    }

    /**
     * Set the selection background color to the given theme color.
     *
     * @param selectionBackgroundColor The theme color index for the selection background color.
     */
    public final void setSelectionBackgroundColor(final int selectionBackgroundColor) {
        setSelectionBackgroundColor(getColor(selectionBackgroundColor));
    }

    /**
     * Set the inactive selection color to the given theme color.
     *
     * @param inactiveSelectionColor The theme color index for the inactive selection color.
     */
    public final void setInactiveSelectionColor(final int inactiveSelectionColor) {
        setInactiveSelectionColor(getColor(inactiveSelectionColor));
    }

    /**
     * Set the inactive selection background color to the given theme color.
     *
     * @param inactiveSelectionBackgroundColor The theme color index for the inactive
     * selection background color.
     */
    public final void setInactiveSelectionBackgroundColor(final int inactiveSelectionBackgroundColor) {
        setInactiveSelectionBackgroundColor(getColor(inactiveSelectionBackgroundColor));
    }
}
