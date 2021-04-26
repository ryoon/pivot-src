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
 * An immutable class representing the location of an object.
 * <p> This class is immutable (unlike a {@link java.awt.Point}), so that
 * the {@link #translate} method returns a new object, rather than
 * modifying the original (for instance).
 */
public final class Point implements Serializable {
    private static final long serialVersionUID = 5193175754909343769L;

    /**
     * The (integer) X-value of this point, representing a distance from the
     * left of the parent object.
     */
    public final int x;
    /**
     * The (integer) Y-value of this point, representing a distance down
     * from the top of the parent object.
     */
    public final int y;

    /**
     * Map key used to access the X-value.
     */
    public static final String X_KEY = "x";
    /**
     * Map key used to access the Y-value.
     */
    public static final String Y_KEY = "y";

    /**
     * Construct a point given the X/Y coordinates.
     *
     * @param xValue The X-position for the point.
     * @param yValue The Y-position for the point.
     */
    public Point(final int xValue, final int yValue) {
        x = xValue;
        y = yValue;
    }

    /**
     * A "copy" constructor to duplicate a point value.
     *
     * @param point The other point to copy.
     */
    public Point(final Point point) {
        Utils.checkNull(point, "point");

        x = point.x;
        y = point.y;
    }

    /**
     * Construct a point from a dictionary containing the X- and Y-position
     * values as entries.
     *
     * @param point The source dictionary containing the values.
     * @throws IllegalArgumentException if the input is {@code null}.
     * @see #X_KEY
     * @see #Y_KEY
     */
    public Point(final Dictionary<String, ?> point) {
        Utils.checkNull(point, "point");

        this.x = point.getInt(X_KEY);
        this.y = point.getInt(Y_KEY);
    }

    /**
     * Construct a point from a sequence of two number values for the
     * X- and Y-positions respectively.
     *
     * @param point The source sequence containing the values (values must be
     *              {@link Number}s).
     * @throws IllegalArgumentException if the input is {@code null}.
     */
    public Point(final Sequence<?> point) {
        Utils.checkNull(point, "point");

        x = ((Number) point.get(0)).intValue();
        y = ((Number) point.get(1)).intValue();
    }

    /**
     * Return a new {@code Point} object which represents
     * this point moved to a new location, {@code dx} and
     * {@code dy} away from the original.
     *
     * @param dx The distance to move in the horizontal
     * direction (positive or negative).
     * @param dy The distance to move in the vertical
     * direction (positive moves downward on the screen,
     * and negative to move upward).
     * @return A new object represented the translated location.
     */
    public Point translate(final int dx, final int dy) {
        return new Point(x + dx, y + dy);
    }

    @Override
    public boolean equals(final Object object) {
        boolean equals = false;

        if (object instanceof Point) {
            Point point = (Point) object;
            equals = (x == point.x && y == point.y);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + x + "," + y + "]";
    }

    /**
     * Decode a string value (which could be a JSON-formatted string (map or list))
     * that contains the two values for a new point.
     * <p> The format of a JSON map would be:
     * <pre>{ "x":nnn, "y":nnn }</pre>
     * <p> The format for a JSON list would be:
     * <pre>[ x, y ]</pre>
     * <p> Or the string can simply be two numbers:
     * <pre>x [,;] y</pre>
     *
     * @param value The string to be interpreted (must not be {@code null}).
     * @return The new Point object if the string can be decoded successfully.
     * @throws IllegalArgumentException if the input is {@code null} or if the
     * value could not be successfully decoded as a JSON map or list, or simply
     * two values.
     * @see #Point(Dictionary)
     * @see #Point(Sequence)
     * @see #Point(int, int)
     */
    public static Point decode(final String value) {
        Utils.checkNull(value);

        Point point;
        if (value.startsWith("{")) {
            try {
                point = new Point(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else if (value.startsWith("[")) {
            try {
                point = new Point(JSONSerializer.parseList(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            String[] parts = value.split("\\s*[,;]\\s*");
            if (parts.length == 2) {
                try {
                    point = new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(ex);
                }
            } else {
                throw new IllegalArgumentException("Invalid format for Point: '" + value + "'");
            }
        }

        return point;
    }

}
