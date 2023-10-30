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
package org.apache.pivot.wtk.text;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Element representing a bulleted list.
 */
public class BulletedList extends List {
    /**
     * List bullet styles.
     */
    public enum Style {
        /** Unicode character 0x2022 aka "BULLET". */
        CIRCLE,
        /** Unicode character 0x25e6 aka "WHITE BULLET". */
        CIRCLE_OUTLINE
    }

    private Style style = Style.CIRCLE;

    private BulletedListListener.Listeners bulletedListListeners = new BulletedListListener.Listeners();

    public BulletedList() {
        super();
    }

    public BulletedList(final BulletedList bulletedList, final boolean recursive) {
        super(bulletedList, recursive);
        this.style = bulletedList.style;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(final Style newStyle) {
        Utils.checkNull(newStyle, "style");

        Style previousStyle = this.style;
        if (previousStyle != newStyle) {
            this.style = newStyle;
            bulletedListListeners.styleChanged(this, previousStyle);
        }
    }

    @Override
    public BulletedList duplicate(final boolean recursive) {
        return new BulletedList(this, recursive);
    }

    public ListenerList<BulletedListListener> getBulletedListListeners() {
        return bulletedListListeners;
    }
}
