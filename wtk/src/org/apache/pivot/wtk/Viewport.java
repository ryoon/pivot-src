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

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * Abstract base class for viewport components. Viewports provide a windowed
 * view on a component (called the "view") that is too large to fit within a
 * given area. They are generally scrollable. <p> Even though this class is a
 * {@link Container}, no components should be added to it via the {@link #add
 * add()} method. The component that gets the windowed (or scrollable) view
 * should be added via the {@link #setView setView()} method (or the "view"
 * property).
 */
@DefaultProperty("view")
public abstract class Viewport extends Container {
    /**
     * Viewport skin interface. Viewport skins must implement this.
     */
    public interface Skin {
        /**
         * @return The bounds of the Viewport within the container, for example, in
         * ScrollPaneSkin, this excludes the scrollbars.
         */
        Bounds getViewportBounds();
    }

    /** Current top scroll offset. */
    private int scrollTop = 0;
    /** Current left scroll offset. */
    private int scrollLeft = 0;
    /** The current component (typically a {@link Container}) that we are viewing. */
    private Component view;

    /** Whether we are consuming or propagating repaints. */
    private boolean consumeRepaint = false;
    /** Whether repainting is optimized, or whether a full repaint is required. */
    private boolean repaintAllViewport = false;

    /** Current set of listeners for events. */
    private ViewportListener.Listeners viewportListeners = new ViewportListener.Listeners();

    @Override
    protected void setSkin(final org.apache.pivot.wtk.Skin skin) {
        checkSkin(skin, Viewport.Skin.class);

        super.setSkin(skin);
    }

    /**
     * @return The current top scroll offset.
     */
    public int getScrollTop() {
        return scrollTop;
    }

    /**
     * Set the new top scroll offset.
     *
     * @param newScrollTop The new top offset.
     */
    public void setScrollTop(final int newScrollTop) {
        int previousScrollTop = scrollTop;

        if (newScrollTop != previousScrollTop) {
            scrollTop = newScrollTop;
            viewportListeners.scrollTopChanged(this, previousScrollTop);
        }
    }

    /**
     * @return The current left scroll offset.
     */
    public int getScrollLeft() {
        return scrollLeft;
    }

    /**
     * Set the new left scroll offset.
     *
     * @param newScrollLeft The new left offset.
     */
    public void setScrollLeft(final int newScrollLeft) {
        int previousScrollLeft = scrollLeft;

        if (newScrollLeft != previousScrollLeft) {
            scrollLeft = newScrollLeft;
            viewportListeners.scrollLeftChanged(this, previousScrollLeft);
        }
    }

    /**
     * @return The (single) component (typically a {@link Container}) that we
     * are providing a windowed (or scrollable) view of.
     */
    public Component getView() {
        return view;
    }

    /**
     * Set the single component (typically a {@link Container}) that we will
     * provide a windowed (or scrollable) view of.
     *
     * @param newView The new component (container) we are viewing.
     */
    public void setView(final Component newView) {
        Component previousView = view;

        if (newView != previousView) {
            // Remove any previous view component
            view = null;

            if (previousView != null) {
                remove(previousView);
            }

            // Set the new view component
            if (newView != null) {
                insert(newView, 0);
            }

            view = newView;

            viewportListeners.viewChanged(this, previousView);
        }
    }

    /**
     * Returns the {@code consumeRepaint} flag, which controls whether the
     * viewport will propagate repaints to its parent or consume them. This flag
     * enables skins to optimize viewport scrolling by blitting the display to
     * reduce the required repaint area.
     *
     * @return {@code true} if this viewport will consume repaints that bubble
     * up through it; {@code false} if it will propagate them up like normal.
     */
    public boolean isConsumeRepaint() {
        return consumeRepaint;
    }

    /**
     * Sets the {@code consumeRepaint} flag, which controls whether the
     * viewport will propagate repaints to its parent or consume them. This flag
     * enables skins to optimize viewport scrolling by blitting the display to
     * reduce the required repaint area.
     *
     * @param newConsumeRepaint {@code true} to consume repaints that bubble up
     * through this viewport; {@code false} to propagate them up like normal.
     */
    public void setConsumeRepaint(final boolean newConsumeRepaint) {
        consumeRepaint = newConsumeRepaint;
    }

    /**
     * @return The bounds of the Viewport within the container, for example, in
     * ScrollPaneSkin, this excludes the scrollbars.
     */
    public Bounds getViewportBounds() {
        Viewport.Skin viewportSkin = (Viewport.Skin) getSkin();
        return viewportSkin.getViewportBounds();
    }

    @Override
    public void repaint(final int x, final int y, final int width, final int height, final boolean immediate) {
        if (!consumeRepaint) {
            super.repaint(x, y, width, height, immediate);
        }
    }

    /**
     * This method should not be called to remove child components from the
     * Viewport because the viewable child(ren) are set by the {@link #setView}
     * method instead. Any attempt to remove the "view" component with this
     * method will result in an exception.
     */
    @Override
    public Sequence<Component> remove(final int index, final int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);
            if (component == view) {
                throw new UnsupportedOperationException();
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    /**
     * @return The current list of listeners for events on this viewport.
     */
    public ListenerList<ViewportListener> getViewportListeners() {
        return viewportListeners;
    }

    /**
     * Tell if the viewport painting mode is optimized (repaint only needed
     * area, default), or repaint all. <p> This is implemented as a workaround
     * for various painting issues on some platforms. So, if you experience
     * problems with the scrolled-in area not being painted properly by default,
     * consider setting this property {@code true} using the
     * {@link #setRepaintAllViewport setRepaintAllViewport} method.
     *
     * @return {@code false} if optimized, otherwise {@code true} (repaint
     * entire viewport)
     */
    public boolean isRepaintAllViewport() {
        return repaintAllViewport;
    }

    /**
     * Set the viewport painting mode. <p> This is implemented as a workaround
     * for various painting issues on some platforms. So, if you experience
     * problems with the scrolled-in area not being painted properly by default,
     * consider setting this property {@code true} (default is {@code false}).
     *
     * @param newRepaintAllViewport {@code false} means optimized (repaint only
     * needed area, default), while {@code true} means repaint all
     */
    public void setRepaintAllViewport(final boolean newRepaintAllViewport) {
        repaintAllViewport = newRepaintAllViewport;
    }

}
