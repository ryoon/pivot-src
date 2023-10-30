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
 * Element representing a numbered list.
 */
public class NumberedList extends List {
    /**
     * List numbering styles.
     */
    public enum Style {
        /** Decimal numbers, such as <code>1</code>, <code>2</code>, or <code>3</code>. */
        DECIMAL,
        /** Lower case alphabetic, such as <code>a</code>, <code>b</code>, or <code>c</code>. */
        LOWER_ALPHA,
        /** Upper case alphabetic, such as <code>A</code>, <code>B</code>, or <code>C</code>. */
        UPPER_ALPHA,
        /** Lower case Roman numerals, such as <code>i</code>, <code>ii</code>, <code>iii</code>. */
        LOWER_ROMAN,
        /** Upper case Roman numerals, such as <code>I</code>, <code>II</code>, <code>III</code>. */
        UPPER_ROMAN
    }

    /**
     * The numbering style for this list.
     */
    private Style style = Style.DECIMAL;

    /**
     * The list of listeners of this list (mostly the skin classes).
     */
    private NumberedListListener.Listeners numberedListListeners = new NumberedListListener.Listeners();

    /**
     * Default constructor using the default numbering style.
     */
    public NumberedList() {
        super();
    }

    /**
     * "Copy" constructor using the style of the given list, and whether the copy is recursive
     * (also copying all child elements).
     *
     * @param numberedList Element to copy from.
     * @param recursive    Whether to copy all children as well.
     */
    public NumberedList(final NumberedList numberedList, final boolean recursive) {
        super(numberedList, recursive);
        this.style = numberedList.style;
    }

    /**
     * Access the number style of this list.
     *
     * @return The list's number style.
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Set the new numbering style for this list.
     *
     * @param newStyle The updated numbering style for this list.
     */
    public void setStyle(final Style newStyle) {
        Utils.checkNull(newStyle, "style");

        Style previousStyle = style;
        if (previousStyle != newStyle) {
            style = newStyle;
            numberedListListeners.styleChanged(this, previousStyle);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NumberedList duplicate(final boolean recursive) {
        return new NumberedList(this, recursive);
    }

    /**
     * Access the list of listeners for changes to this element.
     *
     * @return The list of listeners.
     */
    public ListenerList<NumberedListListener> getNumberedListListeners() {
        return numberedListListeners;
    }
}
