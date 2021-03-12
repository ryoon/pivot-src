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

import org.apache.pivot.wtk.skin.TextPaneSkin;

/**
 * Terra Text Pane skin. This part of the skin deals with color
 * selection that is specific to the theme.
 */
public class TerraTextPaneSkin extends TextPaneSkin {
    /**
     * Specific constructor with nothing to do.
     * <p> Default colors, etc. set by call to {@link TerraTheme#setDefaultStyles}
     * from {@link TextPaneSkin#install}.
     */
    public TerraTextPaneSkin() {
    }

    /**
     * Set the foreground color to the given theme index color.
     *
     * @param color The theme index for the new foreground color.
     */
    public final void setColor(final int color) {
        setColor(getColor(color));
    }

    /**
     * Set the inactive color to the given theme index color.
     *
     * @param inactiveColor The theme index for the new inactive color.
     */
    public final void setInactiveColor(final int inactiveColor) {
        setInactiveColor(getColor(inactiveColor));
    }

    /**
     * Set the selection color to the given theme index color.
     *
     * @param selectionColor The theme index for the new selection color.
     */
    public final void setSelectionColor(final int selectionColor) {
        setSelectionColor(getColor(selectionColor));
    }

    /**
     * Set the selection background color to the given theme index color.
     *
     * @param selectionBackgroundColor The theme index for the new selection background color.
     */
    public final void setSelectionBackgroundColor(final int selectionBackgroundColor) {
        setSelectionBackgroundColor(getColor(selectionBackgroundColor));
    }

    /**
     * Set the inactive selection color to the given theme index color.
     *
     * @param inactiveSelectionColor The theme index for the new inactive selection color.
     */
    public final void setInactiveSelectionColor(final int inactiveSelectionColor) {
        setInactiveSelectionColor(getColor(inactiveSelectionColor));
    }

    /**
     * Set the inactive selection background color to the given theme index color.
     *
     * @param inactiveSelectionBackgroundColor The theme index for the new inactive selection
     * background color.
     */
    public final void setInactiveSelectionBackgroundColor(final int inactiveSelectionBackgroundColor) {
        setInactiveSelectionBackgroundColor(getColor(inactiveSelectionBackgroundColor));
    }
}
