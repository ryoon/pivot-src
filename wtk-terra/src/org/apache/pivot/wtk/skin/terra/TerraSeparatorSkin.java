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

import org.apache.pivot.wtk.skin.SeparatorSkin;

/**
 * Terra separator skin.
 */
public class TerraSeparatorSkin extends SeparatorSkin {
    /**
     * Specific constructor with nothing to do.
     * <p> Default colors, etc. set by call to {@link TerraTheme#setDefaultStyles}
     * from {@link SeparatorSkin#install}.
     */
    public TerraSeparatorSkin() {
    }

    /**
     * Set the foreground color to the theme index color.
     *
     * @param color The theme color index for the new foreground color.
     */
    public void setColor(final int color) {
        setColor(getColor(color));
    }

    /**
     * Set the heading color to the theme index color.
     *
     * @param headingColor The theme color index for the new heading color.
     */
    public void setHeadingColor(final int headingColor) {
        setHeadingColor(getColor(headingColor));
    }
}
