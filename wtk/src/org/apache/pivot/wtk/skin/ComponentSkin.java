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
package org.apache.pivot.wtk.skin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

import org.apache.pivot.collections.EnumSet;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentListener;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.ComponentMouseListener;
import org.apache.pivot.wtk.ComponentMouseWheelListener;
import org.apache.pivot.wtk.ComponentStateListener;
import org.apache.pivot.wtk.ComponentTooltipListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.CSSColor;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.DragSource;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.FontUtilities;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.Keyboard.Modifier;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Skin;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TextInputMethodListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Tooltip;
import org.apache.pivot.wtk.util.ColorUtilities;

/**
 * Abstract base class for component skins.
 */
public abstract class ComponentSkin implements Skin, ComponentListener, ComponentStateListener,
    ComponentMouseListener, ComponentMouseButtonListener, ComponentMouseWheelListener,
    ComponentKeyListener, ComponentTooltipListener {

    /** The component to which this skin is attached. */
    private Component installedComponent = null;

    /** This component's current full width (usually calculated during layout). */
    private int width = 0;
    /** This component's current full height (usually calculated during layout). */
    private int height = 0;

    /** The allowance in the X-direction for tooltip text before going off the left edge. */
    private static final int TOOLTIP_X_ALLOWANCE = 16;

    @Override
    public final int getWidth() {
        return width;
    }

    @Override
    public final int getHeight() {
        return height;
    }

    @Override
    public final Dimensions getSize() {
        return new Dimensions(width, height);
    }

    /**
     * Set the final size of the component after layout has finished.
     * <p> All subclasses must call this superclass method in order to
     * set the {@link #width} and {@link #height} values, but may need
     * to do additional calculations before doing so.
     *
     * @param newWidth  The new (final) width of the component after layout.
     * @param newHeight The new (final) height of the component after layout.
     */
    @Override
    public void setSize(final int newWidth, final int newHeight) {
        this.width = newWidth;
        this.height = newHeight;
    }

    /**
     * @return The preferred size (width and height) of this component.
     * <p> Depending on the component this can be a static value or derived
     * (as for a container) from its subcomponents, etc.
     * <p> The default implementation simply calls {@link #getPreferredWidth}
     * and {@link #getPreferredHeight}.
     */
    @Override
    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    @Override
    public final int getBaseline() {
        return getBaseline(width, height);
    }

    /**
     * Should be implemented in every subclass.
     * <p> The default implementation here simply returns -1 (no baseline).
     */
    @Override
    public int getBaseline(final int trialWidth, final int trialHeight) {
        return -1;
    }

    /**
     * Must be implemented in every subclass in order to do component-specific
     * operations at instantiation time, but every subclass must call this
     * superclass method to setup the necessary listeners, etc.
     */
    @Override
    public void install(final Component component) {
        assert (this.installedComponent == null)
            : "This " + getClass().getSimpleName() + " is already installed on a component.";

        component.getComponentListeners().add(this);
        component.getComponentStateListeners().add(this);
        component.getComponentMouseListeners().add(this);
        component.getComponentMouseButtonListeners().add(this);
        component.getComponentMouseWheelListeners().add(this);
        component.getComponentKeyListeners().add(this);
        component.getComponentTooltipListeners().add(this);

        this.installedComponent = component;
    }

    /**
     * @return The installed component for this skin instance, set by {@link #install}
     * (which therefore must be called by every subclass).
     */
    @Override
    public final Component getComponent() {
        return installedComponent;
    }

    /**
     * By default, skins are focusable.
     */
    @Override
    public boolean isFocusable() {
        return true;
    }

    /**
     * By default, skins are assumed to be opaque.
     */
    @Override
    public boolean isOpaque() {
        return true;
    }

    // Component events
    @Override
    public void parentChanged(final Component component, final Container previousParent) {
        // No-op
    }

    @Override
    public void sizeChanged(final Component component, final int previousWidth, final int previousHeight) {
        // No-op
    }

    @Override
    public void preferredSizeChanged(final Component component, final int previousPreferredWidth,
        final int previousPreferredHeight) {
        // No-op
    }

    @Override
    public void widthLimitsChanged(final Component component, final int previousMinimumWidth,
        final int previousMaximumWidth) {
        // No-op
    }

    @Override
    public void heightLimitsChanged(final Component component, final int previousMinimumHeight,
        final int previousMaximumHeight) {
        // No-op
    }

    @Override
    public void locationChanged(final Component component, final int previousX, final int previousY) {
        // No-op
    }

    @Override
    public void visibleChanged(final Component component) {
        // No-op
    }

    @Override
    public void cursorChanged(final Component component, final Cursor previousCursor) {
        // No-op
    }

    @Override
    public void tooltipTextChanged(final Component component, final String previousTooltipText) {
        // No-op
    }

    @Override
    public void tooltipDelayChanged(final Component component, final int previousTooltipDelay) {
        // No-op
    }

    @Override
    public void dragSourceChanged(final Component component, final DragSource previousDragSource) {
        // No-op
    }

    @Override
    public void dropTargetChanged(final Component component, final DropTarget previousDropTarget) {
        // No-op
    }

    @Override
    public void menuHandlerChanged(final Component component, final MenuHandler previousMenuHandler) {
        // No-op
    }

    @Override
    public void nameChanged(final Component component, final String previousName) {
        // No-op
    }

    // Component state events
    @Override
    public void enabledChanged(final Component component) {
        // No-op
    }

    @Override
    public void focusedChanged(final Component component, final Component obverseComponent) {
        // No-op
    }

    // Component mouse events
    @Override
    public boolean mouseMove(final Component component, final int x, final int y) {
        return false;
    }

    @Override
    public void mouseOver(final Component component) {
        // No-op
    }

    @Override
    public void mouseOut(final Component component) {
        // No-op
    }

    // Component mouse button events
    @Override
    public boolean mouseDown(final Component component, final Mouse.Button button, final int x, final int y) {
        return false;
    }

    @Override
    public boolean mouseUp(final Component component, final Mouse.Button button, final int x, final int y) {
        return false;
    }

    @Override
    public boolean mouseClick(final Component component, final Mouse.Button button, final int x, final int y,
        final int count) {
        return false;
    }

    // Component mouse wheel events
    @Override
    public boolean mouseWheel(final Component component, final Mouse.ScrollType scrollType,
        final int scrollAmount, final int wheelRotation, final int x, final int y) {
        return false;
    }

    // Component key events
    @Override
    public boolean keyTyped(final Component component, final char character) {
        return false;
    }

    /**
     * Keyboard handling (Tab key or Shift Tab).
     * <ul>
     * <li>{@link KeyCode#TAB TAB} Transfers focus forwards</li>
     * <li>{@link KeyCode#TAB TAB} + {@link Modifier#SHIFT SHIFT} Transfers focus backwards</li>
     * </ul>
     */
    @Override
    public boolean keyPressed(final Component component, final int keyCode,
        final KeyLocation keyLocation) {
        boolean consumed = false;

        EnumSet<Modifier> otherModifiers = EnumSet.noneOf(Modifier.class);
        otherModifiers.addAll(Modifier.ALL_MODIFIERS);
        otherModifiers.remove(Modifier.SHIFT);

        if (keyCode == KeyCode.TAB
         && !Keyboard.areAnyPressed(otherModifiers)
         &&  getComponent().isFocused()) {
            FocusTraversalDirection direction = Keyboard.isPressed(Modifier.SHIFT)
                ? FocusTraversalDirection.BACKWARD
                : FocusTraversalDirection.FORWARD;

            // Transfer focus to the next component
            Component focusedComponent = component.transferFocus(direction);

            // Ensure that the focused component is visible
            if (component != focusedComponent && focusedComponent != null) {
                focusedComponent.scrollAreaToVisible(0, 0, focusedComponent.getWidth(),
                    focusedComponent.getHeight());
            }

            consumed = true;
        }

        return consumed;
    }

    @Override
    public boolean keyReleased(final Component component, final int keyCode,
        final KeyLocation keyLocation) {
        return false;
    }

    @Override
    public void tooltipTriggered(final Component component, final int x, final int y) {
        String tooltipText = component.getTooltipText();

        if (tooltipText != null) {
            Label tooltipLabel = new Label(tooltipText);
            boolean tooltipWrapText = component.getTooltipWrapText();
            tooltipLabel.putStyle(Style.wrapText, tooltipWrapText);
            Tooltip tooltip = new Tooltip(tooltipLabel);

            Display display = component.getDisplay();
            Point location = component.mapPointToAncestor(display, x, y);

            // Ensure that the tooltip stays on screen
            int tooltipX = location.x + TOOLTIP_X_ALLOWANCE;
            int tooltipY = location.y;

            int tooltipWidth = tooltip.getPreferredWidth();
            int tooltipHeight = tooltip.getPreferredHeight();
            if (tooltipX + tooltipWidth > display.getWidth()) {
                // Try to just fit it inside the display if there would be room to shift it
                // above the cursor, otherwise move it to the left of the cursor.
                if (tooltipY > tooltipHeight) {
                    tooltipX = display.getWidth() - tooltipWidth;
                } else {
                    tooltipX = location.x - tooltipWidth - TOOLTIP_X_ALLOWANCE;
                }
                if (tooltipX < 0) {
                    tooltipX = 0;
                }
                // Adjust the y location if the tip ends up being behind the mouse cursor
                // because of these x adjustments.
                if (tooltipX < location.x && tooltipX + tooltipWidth > location.x) {
                    tooltipY -= tooltipHeight;
                    if (tooltipY < 0) {
                        tooltipY = 0;
                    }
                }
            }
            if (tooltipY + tooltipHeight > display.getHeight()) {
                tooltipY -= tooltipHeight;
            }

            tooltip.setLocation(tooltipX, tooltipY);
            tooltip.open(component.getWindow());
        }
    }

    // Utility methods
    /**
     * Mark the component's entire size as invalid, to be repainted when
     * the event queue is empty.
     */
    protected void invalidateComponent() {
        if (installedComponent != null) {
            installedComponent.invalidate();
            installedComponent.repaint();
        }
    }

    /**
     * Repaint the entire component when the event queue is empty.
     */
    protected void repaintComponent() {
        repaintComponent(false);
    }

    /**
     * Repaint the entire component with the option to do so immediately
     * (vs. when the event queue is empty).
     * @param immediate {@code true} to repaint the entire component now.
     */
    protected void repaintComponent(final boolean immediate) {
        if (installedComponent != null) {
            installedComponent.repaint(immediate);
        }
    }

    /**
     * Repaint the given area of the component when the event queue is empty.
     * @param area The bounding box of the area to be repainted.
     */
    protected void repaintComponent(final Bounds area) {
        assert (area != null) : "area is null.";

        if (installedComponent != null) {
            installedComponent.repaint(area.x, area.y, area.width, area.height);
        }
    }

    /**
     * Repaint the area of the component specified by the given location and size
     * when the event queue is empty.
     * @param x The starting X-position to paint.
     * @param y The starting Y-position to paint.
     * @param areaWidth The width of the area to repaint.
     * @param areaHeight The height of the area to repaint.
     */
    protected void repaintComponent(final int x, final int y, final int areaWidth, final int areaHeight) {
        if (installedComponent != null) {
            installedComponent.repaint(x, y, areaWidth, areaHeight);
        }
    }

    /**
     * Repaint the area of the component specified by the given location and size
     * with the option to do so immediately or when the event queue is empty.
     * @param x The starting X-position to paint.
     * @param y The starting Y-position to paint.
     * @param areaWidth The width of the area to repaint.
     * @param areaHeight The height of the area to repaint.
     * @param immediate {@code true} to repaint the given area now.
     */
    protected void repaintComponent(final int x, final int y, final int areaWidth, final int areaHeight,
        final boolean immediate) {
        if (installedComponent != null) {
            installedComponent.repaint(x, y, areaWidth, areaHeight, immediate);
        }
    }

    /**
     * Interpret a string as a font specification.
     *
     * @param value Either a JSON dictionary {@link Theme#deriveFont describing
     * a font relative to the current theme}, or one of the
     * {@link Font#decode(String) standard Java font specifications}, with the
     * additional capability of supplying a list of font names (comma-separated)
     * (similar to CSS) if desired.
     * @return The font corresponding to the specification.
     * @throws IllegalArgumentException if the given string is {@code null}
     * or empty or the font specification cannot be decoded.
     * @see FontUtilities#decodeFont(String)
     */
    public static final Font decodeFont(final String value) {
        return FontUtilities.decodeFont(value);
    }

    /**
     * Convert any object we support into its corresponding font.
     * <p> Uses {@link FontUtilities#decodeFont} or {@link Theme#deriveFont}
     * to do the work.
     *
     * @param fontValue The object to be converted to a font.
     * @return The converted font.
     * @throws IllegalArgumentException if the value is {@code null} or
     * cannot be converted.
     * @see FontUtilities#fromObject
     */
    public Font fontFromObject(final Object fontValue) {
        return FontUtilities.fromObject(fontValue);
    }

    /**
     * Returns the current Theme.
     *
     * @return The currently loaded theme.
     */
    public final Theme currentTheme() {
        return Theme.getTheme();
    }

    /**
     * Returns whether the current Theme is dark.
     *
     * Usually this means that (if true) any
     * color will be transformed in the opposite way.
     *
     * @return {@code true} if it is dark, {@code false} otherwise (default)
     */
    public final boolean themeIsDark() {
        return currentTheme().isThemeDark();
    }

    /**
     * Returns whether the current Theme is flat.
     *
     * Note that flat themes usually have no bevel, gradients, shadow effects,
     * and in some cases even no borders.
     *
     * @return {@code true} if it is flat, {@code false} otherwise (default)
     */
    public final boolean themeIsFlat() {
        return currentTheme().isThemeFlat();
    }

    /**
     * Returns whether the current Theme has transitions enabled.
     *
     * @return {@code true} if transitions are enabled
     * (default), {@code false} otherwise
     */
    public final boolean themeHasTransitionEnabled() {
        return currentTheme().isTransitionEnabled();
    }

    /**
     * Returns whether the current Theme has thick focus rectangles.
     *
     * @return {@code true} if thick focus rectangles are drawn (new default),
     * or {@code false} otherwise (default for previous versions).
     */
    public final boolean themeHasThickFocusRectangle() {
        return currentTheme().isThickFocusRectangle();
    }

    /**
     * Returns the Theme default background color.
     *
     * @return {@link Color#WHITE} if the theme is not dark
     * (default), or {@link Color#BLACK}.
     */
    public final Color defaultBackgroundColor() {
        return currentTheme().getDefaultBackgroundColor();
    }

    /**
     * Returns the Theme default foreground color.
     *
     * @return {@link Color#BLACK} if the theme is not dark
     * (default), or {@link Color#WHITE}.
     */
    public final Color defaultForegroundColor() {
        return currentTheme().getDefaultForegroundColor();
    }

    /**
     * Returns a dashed line stroke for drawing focus rectangles, based on
     * the current theme setting for "thick" ones.
     *
     * @return A line stroke object with the correct thickness and dash pattern.
     */
    public final BasicStroke getFocusRectangleStroke() {
        return GraphicsUtilities.getFocusStroke(themeHasThickFocusRectangle());
    }

    /**
     * Returns the current theme color indicated by the index value.
     *
     * @param index Index into the theme's color palette.
     * @return The current theme color value.
     */
    public final Color getColor(final int index) {
        return currentTheme().getColor(index);
    }

    /**
     * Interpret an object as a color value.
     *
     * @param colorValue One of a {@link String} (interpreted by {@link GraphicsUtilities#decodeColor(String,String)}),
     * a straight {@link Color}, one of our {@link CSSColor} values, or an integer index into the theme's
     * color palette.
     * @return The real {@link Color} value.
     * @throws IllegalArgumentException if the {@code colorValue} is {@code null} or of a type we don't recognize.
     * @see ColorUtilities#fromObject
     */
    public final Color colorFromObject(final Object colorValue) {
        return ColorUtilities.fromObject(colorValue, null, false);
    }

    /**
     * Interpret an object as a color value.
     *
     * @param colorValue One of a {@link String} (interpreted by {@link GraphicsUtilities#decodeColor(String,String)}),
     * a straight {@link Color}, one of our {@link CSSColor} values, or an integer index into the theme's
     * color palette.
     * @param allowNull Whether or not to allow a null color.
     * @return The real {@link Color} value.
     * @throws IllegalArgumentException if the {@code colorValue} is {@code null} (unless {@code allowNull}
     * is {@code true}), or of a type we don't recognize.
     * @see ColorUtilities#fromObject
     */
    public final Color colorFromObject(final Object colorValue, final boolean allowNull) {
        return ColorUtilities.fromObject(colorValue, null, allowNull);
    }

    /**
     * Interpret an object as a color value.
     *
     * @param colorValue One of a {@link String} (interpreted by {@link GraphicsUtilities#decodeColor(String,String)}),
     * a straight {@link Color}, one of our {@link CSSColor} values, or an integer index into the theme's
     * color palette.
     * @param description An optional description for the call to {@link Utils#checkNull} in case of a null input value.
     * @return The real {@link Color} value.
     * @throws IllegalArgumentException if the {@code colorValue} is {@code null}, or of a type we don't recognize.
     * @see ColorUtilities#fromObject
     */
    public final Color colorFromObject(final Object colorValue, final String description) {
        return ColorUtilities.fromObject(colorValue, description, false);
    }

    /**
     * Interpret an object as a color value.
     *
     * @param colorValue One of a {@link String} (interpreted by {@link GraphicsUtilities#decodeColor(String,String)}),
     * a straight {@link Color}, one of our {@link CSSColor} values, or an integer index into the theme's
     * color palette.
     * @param description An optional description for the call to {@link Utils#checkNull} in case of a null input value.
     * @param allowNull Whether or not to allow a null color.
     * @return The real {@link Color} value.
     * @throws IllegalArgumentException if the {@code colorValue} is {@code null} (unless {@code allowNull}
     * is {@code true}), or of a type we don't recognize.
     * @see ColorUtilities#fromObject
     */
    public final Color colorFromObject(final Object colorValue, final String description, final boolean allowNull) {
        return ColorUtilities.fromObject(colorValue, description, allowNull);
    }

    /**
     * Returns the current font setting for the theme.
     *
     * @return The default font for the theme.
     */
    public final Font getThemeFont() {
        return currentTheme().getFont();
    }

    /**
     * Sets the default styles for this skin by calling
     * {@link Theme#setDefaultStyles} with the current skin object.
     */
    public final void setDefaultStyles() {
        currentTheme().setDefaultStyles(this);
    }

    /**
     * Returns the input method listener for this component.
     * <p> Should be overridden by any component's skin that wants
     * to handle Input Method events (such as {@code TextInput}).
     *
     * @return The input method listener (if any) for this
     * component.
     */
    public TextInputMethodListener getTextInputMethodListener() {
        return null;
    }

}
