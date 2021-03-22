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

import java.io.Serializable;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Utils;

/**
 * Class representing the corner radii of a rectangular object.
 */
public final class CornerRadii implements Serializable {
    private static final long serialVersionUID = -433469769555042467L;

    /**
     * Radius of the top left corner.
     */
    public final int topLeft;
    /**
     * Radius of the top right corner.
     */
    public final int topRight;
    /**
     * Radius of the bottom left corner.
     */
    public final int bottomLeft;
    /**
     * Radius of the bottom right corner.
     */
    public final int bottomRight;

    /** Dictionary key for the top left value. */
    public static final String TOP_LEFT_KEY = "topLeft";
    /** Dictionary key for the top right value. */
    public static final String TOP_RIGHT_KEY = "topRight";
    /** Dictionary key for the bottom left value. */
    public static final String BOTTOM_LEFT_KEY = "bottomLeft";
    /** Dictionary key for the bottom right value. */
    public static final String BOTTOM_RIGHT_KEY = "bottomRight";

    /**
     * Corner radii whose top, left, bottom, and right values are all zero.
     */
    public static final CornerRadii NONE = new CornerRadii(0);

    /**
     * Construct with a single value for all four corners.
     *
     * @param radius The single value for all corners.
     */
    public CornerRadii(final int radius) {
        this(radius, radius, radius, radius);
    }

    /**
     * Construct with a single value for all corners.
     *
     * @param radius The (integer) value for all corners.
     */
    public CornerRadii(final Number radius) {
        Utils.checkNull(radius, "radius");
        int radii = radius.intValue();
        Utils.checkNonNegative(radii, "radii");

        topLeft = radii;
        topRight = radii;
        bottomLeft = radii;
        bottomRight = radii;
    }

    /**
     * Check for valid values for all corners.
     *
     * @param radii The complete object to check all its values.
     * @throws IllegalArgumentException if any of the values are negative.
     */
    private void check(final CornerRadii radii) {
        check(radii.topLeft, radii.topRight, radii.bottomLeft, radii.bottomRight);
    }

    /**
     * Check the individual corner radius values.
     *
     * @param tL The top left value.
     * @param tR The top right value.
     * @param bL The bottom left value.
     * @param bR The bottom right value.
     * @throws IllegalArgumentException if any of the values are negative.
     */
    private void check(final int tL, final int tR, final int bL, final int bR) {
        Utils.checkNonNegative(tL, "topLeft");
        Utils.checkNonNegative(tR, "topRight");
        Utils.checkNonNegative(bL, "bottomLeft");
        Utils.checkNonNegative(bR, "bottomRight");
    }

    /**
     * "Copy" constructor from another corner radii object.
     *
     * @param cornerRadii The other object to copy from.
     * @throws IllegalArgumentException if the object is {@code null}.
     */
    public CornerRadii(final CornerRadii cornerRadii) {
        Utils.checkNull(cornerRadii, "cornerRadii");

        check(cornerRadii);

        topLeft = cornerRadii.topLeft;
        topRight = cornerRadii.topRight;
        bottomLeft = cornerRadii.bottomLeft;
        bottomRight = cornerRadii.bottomRight;
    }

    /**
     * Construct given the individual corner radius values.
     *
     * @param topLeftValue The new top left radius value.
     * @param topRightValue The new top right radius value.
     * @param bottomLeftValue The new bottom left radius.
     * @param bottomRightValue The bottom right radius value.
     * @throws IllegalArgumentException if any of the values are negative.
     */
    public CornerRadii(final int topLeftValue, final int topRightValue,
            final int bottomLeftValue, final int bottomRightValue) {
        check(topLeftValue, topRightValue, bottomLeftValue, bottomRightValue);

        topLeft = topLeftValue;
        topRight = topRightValue;
        bottomLeft = bottomLeftValue;
        bottomRight = bottomRightValue;
    }

    /**
     * Construct a {@link CornerRadii} object from a dictionary specifying
     * values for each of the four corners.
     *
     * @param cornerRadii A dictionary with keys {@value #TOP_LEFT_KEY},
     * {@value #TOP_RIGHT_KEY}, {@value #BOTTOM_LEFT_KEY},
     * {@value #BOTTOM_RIGHT_KEY}, all with numeric values. Omitted values are
     * treated as zero.
     */
    public CornerRadii(final Dictionary<String, ?> cornerRadii) {
        Utils.checkNull(cornerRadii, "cornerRadii");

        topLeft = cornerRadii.getInt(TOP_LEFT_KEY, 0);
        topRight = cornerRadii.getInt(TOP_RIGHT_KEY, 0);
        bottomLeft = cornerRadii.getInt(BOTTOM_LEFT_KEY, 0);
        bottomRight = cornerRadii.getInt(BOTTOM_RIGHT_KEY, 0);

        check(this);
    }

    /**
     * Construct from a sequence of four numeric values.
     *
     * @param cornerRadii Sequence of values in the order of:
     * top left, top right, bottom left, bottom right.
     * @throws IllegalArgumentException if the input is {@code null} or if any
     * of the values are negative.
     */
    public CornerRadii(final Sequence<?> cornerRadii) {
        Utils.checkNull(cornerRadii, "cornerRadii");

        topLeft = ((Number) cornerRadii.get(0)).intValue();
        topRight = ((Number) cornerRadii.get(1)).intValue();
        bottomLeft = ((Number) cornerRadii.get(2)).intValue();
        bottomRight = ((Number) cornerRadii.get(3)).intValue();

        check(this);
    }

    @Override
    public boolean equals(final Object object) {
        boolean equals = false;

        if (object instanceof CornerRadii) {
            CornerRadii cornerRadii = (CornerRadii) object;
            equals = (topLeft == cornerRadii.topLeft && topRight == cornerRadii.topRight
                && bottomLeft == cornerRadii.bottomLeft && bottomRight == cornerRadii.bottomRight);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + topLeft;
        result = prime * result + topRight;
        result = prime * result + bottomLeft;
        result = prime * result + bottomRight;
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + topLeft + "," + topRight + "; "
            + bottomLeft + "," + bottomRight + "]";
    }

    /**
     * Convert any of our supported object sources into a {@code CornerRadii} object.
     *
     * @param source Any supported object ({@code Integer}, {@code Number}, {@code String}, etc.).
     * @return       The constructed {@code CornerRadii} object.
     * @throws       IllegalArgumentException if the source if {@code null} or we can't
     *               figure out how to convert.
     */
    public static CornerRadii fromObject(final Object source) {
        Utils.checkNull(source, "cornerRadii");

        if (source instanceof CornerRadii) {
            return (CornerRadii) source;
        } else if (source instanceof String) {
            return decode((String) source);
        } else if (source instanceof Integer) {
            return new CornerRadii((Integer) source);
        } else if (source instanceof Number) {
            return new CornerRadii((Number) source);
        } else if (source instanceof Dictionary) {
            @SuppressWarnings("unchecked")
            Dictionary<String, ?> dictionary = (Dictionary<String, ?>) source;
            return new CornerRadii(dictionary);
        } else if (source instanceof Sequence) {
            return new CornerRadii((Sequence<?>) source);
        } else {
            throw new IllegalArgumentException("Unable to convert "
                + source.getClass().getSimpleName() + " to CornerRadii!");
        }
    }

    /**
     * Convert a string into corner radii.
     * <p> If the string value is a JSON map, then parse the map
     * and construct using the {@link #CornerRadii(Dictionary)} method.
     * <p> If the string value is a JSON list, then parse the list
     * and construct using the first four values as top left, top right,
     * bottom left, and bottom right respectively, using the
     * {@link #CornerRadii(int, int, int, int)} constructor.
     * <p> A form of 4 integers values separate by commas or semicolons
     * is also accepted, as in "n, n; n, n", where the values are in the
     * same order as the JSON list form.
     * <p> Otherwise the string should be a single integer value
     * that will be used to construct the radii using the {@link #CornerRadii(int)}
     * constructor.
     *
     * @param value The string value to decode into new corner radii.
     * @return The decoded corner radii.
     * @throws IllegalArgumentException if the value is {@code null} or
     * if the string starts with <code>"{"</code> but it cannot be parsed as
     * a JSON map, or if it starts with <code>"["</code> but cannot be parsed
     * as a JSON list.
     */
    public static CornerRadii decode(final String value) {
        Utils.checkNullOrEmpty(value, "value");

        CornerRadii cornerRadii;
        if (value.startsWith("{")) {
            try {
                cornerRadii = new CornerRadii(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else if (value.startsWith("[")) {
            try {
                cornerRadii = new CornerRadii(JSONSerializer.parseList(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            String[] parts = value.split("\\s*[,;]\\s*");
            if (parts.length == 4) {
                try {
                    cornerRadii = new CornerRadii(
                        Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(ex);
                }
            } else if (parts.length == 1) {
                cornerRadii = new CornerRadii(Integer.parseInt(value));
            } else {
                throw new IllegalArgumentException("Bad format for corner radii value: " + value);
            }
        }

        return cornerRadii;
    }

}
