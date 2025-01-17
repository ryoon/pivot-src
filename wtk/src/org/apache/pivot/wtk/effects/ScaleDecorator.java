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
package org.apache.pivot.wtk.effects;

import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.VerticalAlignment;

/**
 * Decorator that scales the painting of a component along the X and/or Y axes.
 * <p> Generally speaking, decorators don't force a repaint of the component(s)
 * they are attached to when their parameters are changed. So, if this decorator
 * is changed after being applied to a particular component (e.g., to do a
 * dynamic resize) then either the component.repaint() method must be called or
 * the decorator should be removed and added again to force a repaint with the
 * new scale.
 */
public class ScaleDecorator implements Decorator {
    private float scaleX;
    private float scaleY;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
    private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;

    /**
     * Creates a new {@code ScaleDecorator} with the default <code>scaleX</code>
     * <code>scaleY</code> values of <code>1.0f</code>.
     */
    public ScaleDecorator() {
        this(1f, 1f);
    }

    /**
     * Creates a new {@code ScaleDecorator} with a "square" scaling of the given
     * value in both directions.
     *
     * @param scale The scale to use for both X and Y directions.
     */
    public ScaleDecorator(float scale) {
        this(scale, scale);
    }

    /**
     * Creates a new {@code ScaleDecorator} with the specified <code>scaleX</code>
     * and <code>scaleY</code> values.
     *
     * @param scaleX The amount to scale the component's x-axis
     * @param scaleY The amount to scale the component's y-axis
     */
    public ScaleDecorator(float scaleX, float scaleY) {
        setScale(scaleX, scaleY);
    }

    /**
     * Gets the amount by which drawing operations will be scaled along the
     * x-axis.
     *
     * @return The amount to scale the component's x-axis
     */
    public float getScaleX() {
        return scaleX;
    }

    /**
     * Sets the amount by which drawing operations will be scaled along the
     * x-axis.
     *
     * @param scaleX The amount to scale the component's x-axis
     */
    public void setScaleX(float scaleX) {
        setScale(scaleX, scaleY);
    }

    /**
     * Sets the amount by which drawing operations will be scaled along the
     * x-axis.
     *
     * @param scaleX The amount to scale the component's x-axis
     */
    public void setScaleX(Number scaleX) {
        Utils.checkNull(scaleX, "scaleX");

        setScaleX(scaleX.floatValue());
    }

    /**
     * Gets the amount by which drawing operations will be scaled along the
     * y-axis.
     *
     * @return The amount to scale the component's y-axis
     */
    public float getScaleY() {
        return scaleY;
    }

    /**
     * Sets the amount by which drawing operations will be scaled along the
     * y-axis.
     *
     * @param scaleY The amount to scale the component's y-axis
     */
    public void setScaleY(float scaleY) {
        setScale(scaleX, scaleY);
    }

    /**
     * Sets the amount by which drawing operations will be scaled along the
     * y-axis.
     *
     * @param scaleY The amount to scale the component's y-axis
     */
    public void setScaleY(Number scaleY) {
        Utils.checkNull(scaleY, "scaleY");

        setScaleY(scaleY.floatValue());
    }

    /**
     * Sets the amount by which drawing operations will be scaled along the x
     * and y axes.
     *
     * @param scaleX The amount to scale the component's x-axis.
     * @param scaleY The amount to scale the component's y-axis.
     */
    public void setScale(float scaleX, float scaleY) {
        Utils.checkNonNegative(scaleX, "scaleX");
        Utils.checkNonNegative(scaleY, "scaleY");

        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    /**
     * Sets the amount by which drawing operations will be scaled along both the
     * x and y axes.
     *
     * @param scale The amount to scale the component's x and y axes.
     */
    public void setScale(float scale) {
        setScaleX(scale);
        setScaleY(scale);
    }

    /**
     * Gets the horizontal alignment of the decorator. A left alignment will
     * paint the component's left edge at the component's x-coordinate. A right
     * alignment will paint the component's right edge along the right side of
     * the component's bounding box. A center or justified alignment will paint
     * the scaled component centered with respect to the component's bounding
     * box.
     *
     * @return The horizontal alignment
     */
    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Sets the horizontal alignment of the decorator. A left alignment will
     * paint the component's left edge at the component's x-coordinate. A right
     * alignment will paint the component's right edge along the right side of
     * the component's bounding box. A center or justified alignment will paint
     * the scaled component centered with respect to the component's bounding
     * box.
     *
     * @param horizontalAlignment The horizontal alignment
     */
    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        Utils.checkNull(horizontalAlignment, "horizontalAlignment");

        this.horizontalAlignment = horizontalAlignment;
    }

    /**
     * Gets the vertical alignment of the decorator. A top alignment will paint
     * the component's top edge at the component's y-coordinate. A bottom
     * alignment will paint the component's bottom edge along the bottom side of
     * the component's bounding box. A center or justified alignment will paint
     * the scaled component centered with respect to the component's bounding
     * box.
     *
     * @return The vertical alignment
     */
    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Sets the vertical alignment of the decorator. A top alignment will paint
     * the component's top edge at the component's y-coordinate. A bottom
     * alignment will paint the component's bottom edge along the bottom side of
     * the component's bounding box. A center or justified alignment will paint
     * the scaled component centered with respect to the component's bounding
     * box.
     *
     * @param verticalAlignment The vertical alignment
     */
    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        Utils.checkNull(verticalAlignment, "verticalAlignment");

        this.verticalAlignment = verticalAlignment;
    }

    /**
     * Gets the x translation that will be applied with respect to the specified
     * component, given this decorator's <code>scaleX</code> and
     * <code>horizontalAlignment</code> properties.
     *
     * @param component The component being decorated
     * @return The amount to translate x-coordinate actions when decorating this
     * component
     */
    private int getTranslatedX(Component component) {
        int width = component.getWidth();
        int translatedWidth = (int) Math.ceil(width * scaleX);

        int tx;

        switch (horizontalAlignment) {
            case LEFT:
                tx = 0;
                break;
            case RIGHT:
                tx = width - translatedWidth;
                break;
            default:
                tx = (width - translatedWidth) / 2;
                break;
        }

        return tx;
    }

    /**
     * Gets the y translation that will be applied with respect to the specified
     * component, given this decorator's <code>scaleY</code> and
     * <code>verticalAlignment</code> properties.
     *
     * @param component The component being decorated
     * @return The amount to translate y-coordinate actions when decorating this
     * component
     */
    private int getTranslatedY(Component component) {
        int height = component.getHeight();
        int translatedHeight = (int) Math.ceil(height * scaleY);

        int ty;

        switch (verticalAlignment) {
            case TOP:
                ty = 0;
                break;
            case BOTTOM:
                ty = height - translatedHeight;
                break;
            default:
                ty = (height - translatedHeight) / 2;
                break;
        }

        return ty;
    }

    @Override
    public Graphics2D prepare(Component component, Graphics2D graphics) {
        GraphicsUtilities.setAntialiasingOn(graphics);

        FontRenderContext fontRenderContext = GraphicsUtilities.prepareForText(graphics);

        int tx = getTranslatedX(component);
        int ty = getTranslatedY(component);

        if (tx != 0 || ty != 0) {
            graphics.translate(tx, ty);
        }

        // TODO revisit. This is a workaround for Sun bug #6513150, which
        // fails if we apply a zero scale on the graphics (should be legal).
        graphics.scale(Math.max(scaleX, Float.MIN_NORMAL), Math.max(scaleY, Float.MIN_NORMAL));

        return graphics;
    }

    @Override
    public void update() {
        // No-op
    }

    public void repaint(final Component component, int x, int y, int width, int height) {
        Container parent = component.getParent();

        if (parent != null) {
            int tx = getTranslatedX(component);
            int ty = getTranslatedY(component);

            int xUpdated = (int) ((x * scaleX) + component.getX() + tx);
            int yUpdated = (int) ((y * scaleY) + component.getY() + ty);
            int widthUpdated = (int) Math.ceil(width * scaleX);
            int heightUpdated = (int) Math.ceil(height * scaleY);

            parent.repaint(xUpdated, yUpdated, widthUpdated, heightUpdated);
        }
    }

    @Override
    public Bounds getBounds(Component component) {
        int width = (int) Math.ceil(component.getWidth() * scaleX);
        int height = (int) Math.ceil(component.getHeight() * scaleY);

        int tx = getTranslatedX(component);
        int ty = getTranslatedY(component);

        return new Bounds(tx, ty, width, height);
    }

    @Override
    public AffineTransform getTransform(Component component) {
        return AffineTransform.getScaleInstance(scaleX, scaleY);
    }
}
