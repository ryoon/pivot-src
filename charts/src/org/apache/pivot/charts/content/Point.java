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
package org.apache.pivot.charts.content;

/**
 * Represents value data for X/Y chart views.
 */
public class Point {
    /**
     * The X-position data.
     */
    private float x = 0;
    /**
     * The Y-position data.
     */
    private float y = 0;

    /**
     * @return The X-position data.
     */
    public float getX() {
        return x;
    }

    /**
     * Set the X-position data.
     * @param xValue The X-position value.
     */
    public void setX(final float xValue) {
        x = xValue;
    }

    /**
     * @return The Y-position data.
     */
    public float getY() {
        return y;
    }

    /**
     * Set the Y-position data.
     * @param yValue The Y-position value.
     */
    public void setY(final float yValue) {
        y = yValue;
    }
}
