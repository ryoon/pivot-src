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

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * A column or row heading component that display a line number or
 * character column ruler suitable for use with scrolling text controls
 * ({@link TextArea} or {@link TextPane}).
 */
public class NumberRuler extends Component {
    /** Maximum allowed number of digits to display (arbitrary). */
    private static final int MAX_TEXT_SIZE = 20;

    /** Default number of digits to display for numbers in vertical rulers. */
    private static final int DEFAULT_TEXT_SIZE = 5;

    /** Current orientation for one of these (defaults to vertical, since most commonly used for line numbering). */
    private Orientation orientation = Orientation.VERTICAL;

    /**
     * The expected number of digits to allow for in vertical rulers.
     * <p> Since we don't know apriori how many rows/lines will be
     * shown in a vertical ruler, we rely on the user/caller to tell
     * us how much space to allow for the numbers.
     * <p> Note: there is probably a better way to figure this out,
     * but it would require, in general, expensive re-layout as
     * more and more rows are added, and then what to do if rows
     * get taken away?
     */
    private int textSize = DEFAULT_TEXT_SIZE;

    /**
     * The listeners for changes here.
     */
    private NumberRulerListener.Listeners rulerListeners = new NumberRulerListener.Listeners();

    /**
     * Default constructor - instantiate our skin.
     */
    public NumberRuler() {
        installSkin(NumberRuler.class);
    }

    /**
     * @return The current orientation of this ruler.
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Set the ruler orientation.
     *
     * @param newOrientation The new orientation of this ruler: vertical for a line number ruler,
     * or horizontal for a character number ruler.
     */
    public void setOrientation(final Orientation newOrientation) {
        Utils.checkNull(newOrientation, "orientation");

        if (newOrientation != orientation) {
            orientation = newOrientation;
            rulerListeners.orientationChanged(this);
        }
    }

    /**
     * @return The number of digits of space to allow for the
     * numbers in a vertical ruler.
     */
    public int getTextSize() {
        return textSize;
    }

    /**
     * Set the number of digits of space to allow for the numbers.
     *
     * @param size The (integer) number of digits to allow in vertical
     * ruler numbers. The default of {@link #DEFAULT_TEXT_SIZE} allows
     * for 99,999 maximum rows.
     * @throws IllegalArgumentException if the value is negative,
     * or exceeds {@link #MAX_TEXT_SIZE}.
     */
    public void setTextSize(final String size) {
        Utils.checkNullOrEmpty(size, "size");

        setTextSize(Integer.parseInt(size));
    }

    /**
     * Set the number of digits of space to allow for the numbers.
     *
     * @param size The (integer) number of digits to allow in vertical
     * ruler numbers. The default of {@link #DEFAULT_TEXT_SIZE} allows
     * for 99,999 maximum rows.
     * @throws IllegalArgumentException if the value is negative,
     * or exceeds {@link #MAX_TEXT_SIZE}.
     */
    public void setTextSize(final Number size) {
        Utils.checkNullOrEmpty(size, "size");

        setTextSize(size.intValue());
    }

    /**
     * Set the number of digits of space to allow for the numbers.
     *
     * @param size The number of digits to allow in vertical ruler numbers.
     * The default of {@link #DEFAULT_TEXT_SIZE} allows
     * for 99,999 maximum rows.
     * @throws IllegalArgumentException if the value is negative,
     * or exceeds {@link #MAX_TEXT_SIZE}.
     */
    public void setTextSize(final int size) {
        if (size <= 0 || size > MAX_TEXT_SIZE) {
            throw new IllegalArgumentException(
                "Text size must be positive and less or equal to " + MAX_TEXT_SIZE + ".");
        }

        if (size != textSize) {
            int previousSize = textSize;
            textSize = size;
            rulerListeners.textSizeChanged(this, previousSize);
        }
    }

    /**
     * @return The current list of listeners for changes in this component.
     */
    public ListenerList<NumberRulerListener> getRulerListeners() {
        return rulerListeners;
    }

}
