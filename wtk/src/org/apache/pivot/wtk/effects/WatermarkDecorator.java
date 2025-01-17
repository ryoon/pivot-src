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

import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.net.URL;

import org.apache.pivot.util.ImageUtils;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.FontUtilities;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.media.Image;

/**
 * Decorator that paints a watermark effect over a component.
 */
public class WatermarkDecorator implements Decorator {
    private float opacity = 0.1f;
    private double theta = Math.PI / 4;

    private BoxPane boxPane = new BoxPane(Orientation.HORIZONTAL);
    private ImageView imageView = new ImageView();
    private Label label = new Label();

    private Component component = null;
    private Graphics2D graphics = null;

    /**
     * Creates a new {@code WatermarkDecorator} with no text or image.
     */
    public WatermarkDecorator() {
        this(null, null);
    }

    /**
     * Creates a new {@code WatermarkDecorator} with the specified string as
     * its text and no image.
     *
     * @param text The text to paint over the decorated component
     */
    public WatermarkDecorator(String text) {
        this(text, null);
    }

    /**
     * Creates a new {@code WatermarkDecorator} with no text and the specified
     * image.
     *
     * @param image The image to paint over the decorated component
     */
    public WatermarkDecorator(Image image) {
        this(null, image);
    }

    /**
     * Creates a new {@code WatermarkDecorator} with the specified text and
     * image.
     *
     * @param text The text to paint over the decorated component
     * @param image The image to paint over the decorated component
     */
    public WatermarkDecorator(String text, Image image) {
        boxPane.add(imageView);
        boxPane.add(label);

        boxPane.putStyle(Style.verticalAlignment, VerticalAlignment.CENTER);
        imageView.putStyle(Style.opacity, opacity);

        Font font = label.getStyleFont(Style.font);
        label.putStyle(Style.font, font.deriveFont(Font.BOLD, 60));

        label.setText(text != null ? text : "");
        imageView.setImage(image);

        validate();
    }

    /**
     * Gets the text that will be painted over this decorator's component.
     *
     * @return This decorator's text
     */
    public String getText() {
        return label.getText();
    }

    /**
     * Sets the text that will be painted over this decorator's component.
     *
     * @param text This decorator's text
     */
    public void setText(String text) {
        label.setText(text != null ? text : "");
        validate();
    }

    /**
     * Gets the font that will be used when painting this decorator's text.
     *
     * @return This decorator's font
     */
    public Font getFont() {
        return label.getStyleFont(Style.font);
    }

    /**
     * Sets the font that will be used when painting this decorator's text.
     *
     * @param font This decorator's font
     */
    public void setFont(Font font) {
        Utils.checkNull(font, "font");

        label.putStyle(Style.font, font);
        validate();
    }

    /**
     * Sets the font that will be used when painting this decorator's text.
     *
     * @param font This decorator's font
     */
    public final void setFont(String font) {
        setFont(FontUtilities.decodeFont(font));
    }

    /**
     * Gets the image that will be painted over this decorator's component.
     *
     * @return This decorator's image
     */
    public Image getImage() {
        return imageView.getImage();
    }

    /**
     * Sets the image that will be painted over this decorator's component.
     *
     * @param image This decorator's image
     */
    public void setImage(Image image) {
        imageView.setImage(image);
        validate();
    }

    /**
     * Sets the image that will be painted over this decorator's component by
     * URL. <p> If the icon already exists in the application context resource
     * cache, the cached value will be used. Otherwise, the icon will be loaded
     * synchronously and added to the cache.
     *
     * @param imageURL The location of the image to set.
     */
    public void setImage(URL imageURL) {
        setImage(Image.loadFromCache(imageURL));
    }

    /**
     * Sets the image that will be painted over this decorator's component.
     *
     * @param imageName The resource name of the image to set.
     * @see #setImage(URL)
     * @see ImageUtils#findByName(String,String)
     */
    public void setImage(String imageName) {
        setImage(ImageUtils.findByName(imageName, "image"));
    }

    /**
     * Gets the opacity of the watermark.
     *
     * @return This decorator's opacity
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Sets the opacity of the watermark.
     *
     * @param opacity This decorator's opacity
     */
    public void setOpacity(float opacity) {
        this.opacity = opacity;
        imageView.putStyle(Style.opacity, opacity);
    }

    /**
     * Gets the angle at the watermark will be painted, in radians.
     *
     * @return This decorator's watermark angle
     */
    public double getTheta() {
        return theta;
    }

    /**
     * Sets the angle at the watermark will be painted, in radians. This value
     * must lie between <code>0</code> and <code>PI / 2</code> (inclusive).
     *
     * @param theta This decorator's watermark angle
     */
    public void setTheta(double theta) {
        if (theta < 0 || theta > Math.PI / 2) {
            throw new IllegalArgumentException("Theta must be between 0 and PI / 2.");
        }

        this.theta = theta;
    }

    /**
     * Sets this decorator's box pane to its preferred size and validates it.
     */
    private void validate() {
        boxPane.setSize(boxPane.getPreferredSize());
        boxPane.validate();
    }

    @Override
    public Graphics2D prepare(Component componentArgument, Graphics2D graphicsArgument) {
        this.component = componentArgument;
        this.graphics = graphicsArgument;

        return graphicsArgument;
    }

    @Override
    public void update() {
        int width = component.getWidth();
        int height = component.getHeight();

        double sinTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);

        Graphics2D watermarkGraphics = (Graphics2D) graphics.create();
        watermarkGraphics.clipRect(0, 0, component.getWidth(), component.getHeight());
        watermarkGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        watermarkGraphics.rotate(theta);

        // Calculate the separation in between each repetition of the watermark
        int dX = (int) (1.5 * boxPane.getWidth());
        int dY = 2 * boxPane.getHeight();

        // Prepare the origin of our graphics context
        int x = 0;
        int y = (int) (-width * sinTheta);
        watermarkGraphics.translate(x, y);

        for (int yStop = (int) (height * cosTheta), p = 0; y < yStop; y += dY, p = 1 - p) {
            for (int xStop = (int) (height * sinTheta + width * cosTheta); x < xStop; x += dX) {
                boxPane.paint(watermarkGraphics);
                watermarkGraphics.translate(dX, 0);
            }

            // Move X origin back to its starting position & Y origin down
            watermarkGraphics.translate(-x, dY);
            x = 0;

            // Shift the x back and forth to add randomness feel to pattern
            watermarkGraphics.translate((int) ((0.5f - p) * boxPane.getWidth()), 0);
        }

        watermarkGraphics.dispose();

        component = null;
        graphics = null;
    }

}
