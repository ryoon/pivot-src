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
package pivot.wtk.media.drawing;

import java.awt.Paint;

/**
 * Shape listener interface.
 *
 * @author gbrown
 */
public interface ShapeListener {
    /**
     * Shape listener adapter.
     *
     * @author gbrown
     */
    public static class Adapter implements ShapeListener {
        public void originChanged(Shape shape, int previousX, int previousY) {
        }

        public void boundsChanged(Shape shape, int previousX, int previousY,
            int previousWidth, int previousHeight) {
        }

        public void strokeChanged(Shape shape, Paint previousStroke) {
        }

        public void strokeThicknessChanged(Shape shape, int previousStrokeThickness) {
        }

        public void fillChanged(Shape shape, Paint previousFill) {
        }

        public void regionInvalidated(Shape shape, int x, int y, int width, int height) {
        }
    }

    /**
     * Called when a shape's origin has changed.
     *
     * @param shape
     * @param previousX
     * @param previousY
     */
    public void originChanged(Shape shape, int previousX, int previousY);

    /**
     * Called when a shape's bounds have changed.
     *
     * @param shape
     * @param previousWidth
     * @param previousHeight
     */
    public void boundsChanged(Shape shape, int previousX, int previousY,
        int previousWidth, int previousHeight);

    /**
     * Called when a shape's stroke has changed.
     *
     * @param shape
     * @param previousStroke
     */
    public void strokeChanged(Shape shape, Paint previousStroke);

    /**
     * Called when a shape's stroke thickness has changed.
     *
     * @param shape
     * @param previousStrokeThickness
     */
    public void strokeThicknessChanged(Shape shape, int previousStrokeThickness);

    /**
     * Called when a shape's fill has changed.
     *
     * @param shape
     * @param previousFill
     */
    public void fillChanged(Shape shape, Paint previousFill);

    /**
     * Called when a region within a shape needs to be repainted.
     *
     * @param shape
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void regionInvalidated(Shape shape, int x, int y, int width, int height);
}
