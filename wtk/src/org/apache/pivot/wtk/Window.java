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

import java.net.URL;
import java.util.Iterator;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImageUtils;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.media.Image;

/**
 * Top-level container representing the entry point into a user interface.
 * Windows are direct descendants of the display.
 */
@DefaultProperty("content")
public class Window extends Container {
    /**
     * Window skin interface.
     */
    public interface Skin extends org.apache.pivot.wtk.Skin {
        public Bounds getClientArea();
    }

    /**
     * Class representing a mapping from keystrokes to actions.
     */
    public static class ActionMapping {
        private Window window = null;

        private Keyboard.KeyStroke keyStroke = null;
        private Action action = null;

        public ActionMapping() {
        }

        public ActionMapping(Keyboard.KeyStroke keyStroke, Action action) {
            setKeyStroke(keyStroke);
            setAction(action);
        }

        public ActionMapping(Keyboard.KeyStroke keyStroke, String actionID) {
            setKeyStroke(keyStroke);
            setAction(actionID);
        }

        public Window getWindow() {
            return window;
        }

        public Keyboard.KeyStroke getKeyStroke() {
            return keyStroke;
        }

        public void setKeyStroke(Keyboard.KeyStroke keyStroke) {
            Keyboard.KeyStroke previousKeyStroke = this.keyStroke;

            if (keyStroke != previousKeyStroke) {
                if (window != null) {
                    Utils.checkNull(keyStroke, "keyStroke");

                    if (window.actionMap.containsKey(keyStroke)) {
                        throw new IllegalArgumentException("A mapping for " + keyStroke
                            + " already exists.");
                    }

                    if (previousKeyStroke != null) {
                        window.actionMap.remove(previousKeyStroke);
                    }

                    window.actionMap.put(keyStroke, action);

                    window.windowActionMappingListeners.keyStrokeChanged(this, previousKeyStroke);
                }

                this.keyStroke = keyStroke;
            }
        }

        public void setKeyStroke(String keyStroke) {
            Utils.checkNull(keyStroke, "keyStroke");

            setKeyStroke(Keyboard.KeyStroke.decode(keyStroke));
        }

        public Action getAction() {
            return action;
        }

        public void setAction(Action action) {
            Action previousAction = this.action;

            if (action != previousAction) {
                if (window != null) {
                    Utils.checkNull(action, "action");

                    window.actionMap.put(keyStroke, action);

                    window.windowActionMappingListeners.actionChanged(this, previousAction);
                }

                this.action = action;
            }
        }

        public void setAction(String actionID) {
            Utils.checkNull(actionID, "actionID");

            Action actionValue = Action.getNamedActions().get(actionID);
            if (actionValue == null) {
                throw new IllegalArgumentException("An action with ID " + actionID
                    + " does not exist.");
            }

            setAction(actionValue);
        }
    }

    public class ActionMappingSequence implements Sequence<ActionMapping> {
        @Override
        public int add(ActionMapping actionMapping) {
            if (actionMapping.window != null) {
                throw new IllegalArgumentException("Action mapping already has a window.");
            }

            Utils.checkNull(actionMapping.keyStroke, "Keystroke");
            Utils.checkNull(actionMapping.action, "Action");

            if (actionMap.containsKey(actionMapping.keyStroke)) {
                throw new IllegalArgumentException("A mapping for " + actionMapping.keyStroke
                    + " already exists.");
            }

            actionMapping.window = Window.this;

            int index = actionMappings.add(actionMapping);
            actionMap.put(actionMapping.keyStroke, actionMapping.action);

            windowActionMappingListeners.actionMappingAdded(Window.this);

            return index;
        }

        @Override
        @UnsupportedOperation
        public void insert(ActionMapping actionMapping, int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        @UnsupportedOperation
        public ActionMapping update(int index, ActionMapping actionMapping) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(ActionMapping actionMapping) {
            int index = indexOf(actionMapping);

            if (index >= 0) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<ActionMapping> remove(int index, int count) {
            Sequence<ActionMapping> removed = actionMappings.remove(index, count);

            for (int i = 0, n = removed.getLength(); i < n; i++) {
                ActionMapping actionMapping = removed.get(i);

                actionMapping.window = null;

                actionMap.remove(actionMapping.keyStroke);
            }

            windowActionMappingListeners.actionMappingsRemoved(Window.this, index, removed);

            return removed;
        }

        @Override
        public ActionMapping get(int index) {
            return actionMappings.get(index);
        }

        @Override
        public int indexOf(ActionMapping actionMapping) {
            return actionMappings.indexOf(actionMapping);
        }

        @Override
        public int getLength() {
            return actionMappings.getLength();
        }
    }

    public class IconImageSequence implements Sequence<Image>, Iterable<Image> {
        @Override
        public int add(Image image) {
            int index = iconImageList.add(image);

            windowListeners.iconAdded(Window.this, image);

            return index;
        }

        @Override
        public void insert(Image image, int index) {
            iconImageList.insert(image, index);

            windowListeners.iconInserted(Window.this, image, index);
        }

        @Override
        @UnsupportedOperation
        public Image update(int index, Image image) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(Image image) {
            int index = indexOf(image);

            if (index >= 0) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Image> remove(int index, int count) {
            Sequence<Image> removed = iconImageList.remove(index, count);
            windowListeners.iconsRemoved(Window.this, index, removed);
            return removed;
        }

        @Override
        public Image get(int index) {
            return iconImageList.get(index);
        }

        @Override
        public int indexOf(Image image) {
            return iconImageList.indexOf(image);
        }

        @Override
        public int getLength() {
            return iconImageList.getLength();
        }

        @Override
        public Iterator<Image> iterator() {
            return new ImmutableIterator<>(iconImageList.iterator());
        }
    }

    private Window owner = null;
    private ArrayList<Window> ownedWindows = new ArrayList<>();

    private ArrayList<ActionMapping> actionMappings = new ArrayList<>();
    private ActionMappingSequence actionMappingSequence = new ActionMappingSequence();

    private HashMap<Keyboard.KeyStroke, Action> actionMap = new HashMap<>();

    private String title = null;
    private ArrayList<Image> iconImageList = new ArrayList<>();
    private IconImageSequence iconImageSequence = new IconImageSequence();
    private Component content = null;
    private Component focusDescendant = null;

    private boolean opening = false;
    private boolean closing = false;

    private Point restoreLocation = null;

    private WindowListener.Listeners windowListeners = new WindowListener.Listeners();
    private WindowStateListener.Listeners windowStateListeners = new WindowStateListener.Listeners();
    private WindowActionMappingListener.Listeners windowActionMappingListeners =
        new WindowActionMappingListener.Listeners();

    private static WindowClassListener.Listeners windowClassListeners = new WindowClassListener.Listeners();

    private static Window activeWindow = null;

    public Window() {
        this(null);
    }

    public Window(Component content) {
        setContent(content);
        installSkin(Window.class);
    }

    @Override
    protected void setParent(Container parent) {
        if (parent != null && (!(parent instanceof Display))) {
            throw new IllegalArgumentException("Window parent must be null or display, cannot be "
                + parent);
        }

        if (parent == null && isActive()) {
            clearActive();
        }

        super.setParent(parent);
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);
            if (component == content) {
                throw new UnsupportedOperationException("Window content cannot be removed.");
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible && owner != null && !owner.isVisible()) {
            throw new IllegalStateException("Owner is not visible.");
        }

        super.setVisible(visible);

        if (visible && isActive()) {
            clearActive();
        }

        for (Window ownedWindow : ownedWindows) {
            ownedWindow.setVisible(visible);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (!enabled && isActive()) {
            clearActive();
        }
    }

    public Window getOwner() {
        return owner;
    }

    public Window getRootOwner() {
        return (owner == null) ? this : owner.getRootOwner();
    }

    public Window getOwnedWindow(int index) {
        return ownedWindows.get(index);
    }

    public int getOwnedWindowCount() {
        return ownedWindows.getLength();
    }

    /**
     * Tests whether this window is an owning ancestor of a given window. A
     * window is not considered an owner of itself.
     *
     * @param window The window which could be an owned descendant of this window.
     * @return {@code true} if this window is an owning ancestor of the given
     * window; {@code false} otherwise.
     */
    public boolean isOwner(Window window) {
        Utils.checkNull(window, "window");

        Window ownerValue = window.getOwner();

        while (ownerValue != null && ownerValue != this) {
            ownerValue = ownerValue.getOwner();
        }

        return (ownerValue == this);
    }

    /**
     * Returns this window's open state.
     *
     * @return {@code true} if the window is open; {@code false} otherwise.
     */
    public boolean isOpen() {
        return (getParent() != null);
    }

    /**
     * Returns this window's opening state.
     *
     * @return {@code true} if the window is opening; {@code false}
     * otherwise.
     */
    public boolean isOpening() {
        return opening;
    }

    /**
     * Opens the window.
     *
     * @param display The display on which to open this window.
     */
    public final void open(Display display) {
        open(display, null);
    }

    /**
     * Opens the window.
     *
     * @param ownerArgument The window's owner. The window is opened on the
     * owner's display.
     * @throws IllegalArgumentException if the owner argument is {@code null}.
     */
    public final void open(Window ownerArgument) {
        Utils.checkNull(ownerArgument, "owner");

        open(ownerArgument.getDisplay(), ownerArgument);
    }

    /**
     * Opens the window. <p> Note that this method is not a synchronous call, it
     * schedules an event to open the window.
     *
     * @param display The display on which the window will be opened.
     * @param ownerArgument The window's owner, or {@code null} if the window
     * has no owner.
     */
    public void open(Display display, Window ownerArgument) {
        Utils.checkNull(display, "display");

        if (ownerArgument != null) {
            if (!ownerArgument.isOpen()) {
                throw new IllegalArgumentException("owner is not open.");
            }

            if (isOwner(ownerArgument)) {
                throw new IllegalArgumentException("owner is an owned descendant of this window.");
            }
        }

        if (isOpen()) {
            if (getDisplay() != display) {
                throw new IllegalStateException("Window is already open on a different display.");
            }

            if (this.owner != ownerArgument) {
                throw new IllegalStateException("Window is already open with a different owner.");
            }
        }

        if (!isOpen()) {
            opening = true;
            Vote vote = windowStateListeners.previewWindowOpen(this);

            if (vote == Vote.APPROVE) {
                // Set the owner and add to the owner's owned window list
                this.owner = ownerArgument;

                if (ownerArgument != null) {
                    ownerArgument.ownedWindows.add(this);
                }

                // Add the window to the display
                display.add(this);

                // Notify listeners
                opening = false;
                windowStateListeners.windowOpened(this);

                moveToFront();
            } else {
                if (vote == Vote.DENY) {
                    opening = false;
                }

                windowStateListeners.windowOpenVetoed(this, vote);
            }

        }
    }

    /**
     * Returns this window's closed state.
     *
     * @return {@code true} if the window is closed; {@code false} otherwise.
     */
    public boolean isClosed() {
        return !isOpen();
    }

    /**
     * Returns this window's closing state.
     *
     * @return {@code true} if the window is closing; {@code false}
     * otherwise.
     */
    public boolean isClosing() {
        return closing;
    }

    /**
     * Closes the window and all of its owned windows. If any owned window fails
     * to close, this window will also fail to close.
     */
    public void close() {
        if (!isClosed()) {
            closing = true;

            // Close all owned windows (create a copy of the owned window
            // list so owned windows can remove themselves from the list
            // without interrupting the iteration)
            boolean cancel = false;
            for (Window ownedWindow : new ArrayList<>(this.ownedWindows)) {
                ownedWindow.close();
                cancel |= !(ownedWindow.isClosing() || ownedWindow.isClosed());
            }

            // Close this window only if all owned windows are closing or closed
            // (we allow the owner to close even if an owned window is only
            // reports
            // that it is closing, under the assumption that it will ultimately
            // close - not doing so would prevent close transitions from running
            // in parallel, forcing them to run in series)
            if (cancel) {
                closing = false;
            } else {
                Vote vote = windowStateListeners.previewWindowClose(this);

                if (vote == Vote.APPROVE) {
                    // Remove the window from the display
                    Display display = getDisplay();
                    display.remove(this);

                    // Clear the owner and remove from the owner's owned window list
                    Window ownerValue = this.owner;
                    this.owner = null;

                    if (ownerValue != null) {
                        ownerValue.ownedWindows.remove(this);
                    }

                    // Notify listeners
                    closing = false;

                    windowStateListeners.windowClosed(this, display, ownerValue);
                } else {
                    if (vote == Vote.DENY) {
                        closing = false;
                    }

                    windowStateListeners.windowCloseVetoed(this, vote);
                }
            }
        }
    }

    /**
     * Returns the window's title.
     *
     * @return The pane's title, or {@code null} if no title is set.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the window's title.
     *
     * @param title The new title, or {@code null} for no title.
     */
    public void setTitle(String title) {
        String previousTitle = this.title;

        if (previousTitle != title) {
            this.title = title;
            windowListeners.titleChanged(this, previousTitle);
        }
    }

    /**
     * @return The icons for this window.
     */
    public IconImageSequence getIcons() {
        return iconImageSequence;
    }

    /**
     * Sets the window's icon by URL. <p> If the icon already exists in the
     * application context resource cache, the cached value will be used.
     * Otherwise, the icon will be loaded synchronously and added to the cache.
     *
     * @param iconURL The location of the icon to set.
     */
    public void setIcon(URL iconURL) {
        Utils.checkNull(iconURL, "iconURL");

        Image icon = Image.loadFromCache(iconURL);

        getIcons().remove(0, getIcons().getLength());
        getIcons().add(icon);
    }

    /**
     * Sets the window's icon by {@linkplain ClassLoader#getResource(String)
     * resource name}.
     *
     * @param iconName The resource name of the icon to set.
     * @see #setIcon(URL)
     * @see ImageUtils#findByName(String,String)
     */
    public void setIcon(String iconName) {
        setIcon(ImageUtils.findByName(iconName, "icon"));
    }

    public Component getContent() {
        return content;
    }

    public void setContent(Component content) {
        Component previousContent = this.content;

        if (content != previousContent) {
            this.content = null;

            // Remove any previous content component
            if (previousContent != null) {
                remove(previousContent);
            }

            // Add the component
            if (content != null) {
                insert(content, 0);
            }

            this.content = content;

            windowListeners.contentChanged(this, previousContent);
        }
    }

    /**
     * @return The bounds of the window's client area.
     */
    public Bounds getClientArea() {
        Window.Skin windowSkin = (Window.Skin) getSkin();
        return windowSkin.getClientArea();
    }

    /**
     * Returns the window's active state.
     *
     * @return {@code true} if the window is active; {@code false}; otherwise.
     */
    public boolean isActive() {
        return (activeWindow == this);
    }

    /**
     * Requests that this window become the active window.
     *
     * @return {@code true} if the window became active; {@code false}
     * otherwise.
     */
    public boolean requestActive() {
        if (isOpen() && isVisible() && isEnabled()) {
            setActiveWindow(this);
        }

        return isActive();
    }

    /**
     * Called to notify a window that its active state has changed.
     *
     * @param active        The new value of the "active" state for this window.
     * @param obverseWindow The "other" window (that is now in the obverse state).
     */
    protected void setActive(boolean active, Window obverseWindow) {
        windowListeners.activeChanged(this, obverseWindow);
    }

    /**
     * Returns the currently active window.
     *
     * @return The window that is currently active, or {@code null} if no
     * window is active.
     */
    public static Window getActiveWindow() {
        return activeWindow;
    }

    /**
     * Sets the active window. The window must be activatable, open, and
     * enabled. If the window is not currently visible, it will be made visible.
     *
     * @param activeWindow The window to activate, or {@code null} to clear the
     * active window.
     */
    private static void setActiveWindow(Window activeWindow) {
        Window previousActiveWindow = Window.activeWindow;

        if (previousActiveWindow != activeWindow) {
            // Set the active window
            Window.activeWindow = activeWindow;

            // Notify the windows of the state change
            if (previousActiveWindow != null) {
                previousActiveWindow.setActive(false, activeWindow);
            }

            // Activate the window
            if (activeWindow != null) {
                activeWindow.setActive(true, previousActiveWindow);
            }

            windowClassListeners.activeWindowChanged(previousActiveWindow);
        }
    }

    /**
     * Clears the active window.
     */
    public static void clearActive() {
        if (activeWindow != null) {
            setActiveWindow(activeWindow.owner);
        }
    }

    /**
     * @return The window descendant to which focus will be restored when this
     * window is moved to the front.
     */
    public Component getFocusDescendant() {
        return focusDescendant;
    }

    /**
     * Clears the window descendant to which focus will be restored when this
     * window is moved to the front, meaning that when this window is moved to
     * front, focus will not be restored to the window.
     */
    public void clearFocusDescendant() {
        focusDescendant = null;
    }

    @Override
    protected void descendantGainedFocus(Component descendant, Component previousFocusedComponent) {
        this.focusDescendant = descendant;

        super.descendantGainedFocus(descendant, previousFocusedComponent);
    }

    @Override
    protected void descendantRemoved(Component descendant) {
        super.descendantRemoved(descendant);

        if (descendant == focusDescendant) {
            focusDescendant = null;
        }
    }

    /**
     * @return The action mappings for this window.
     */
    public ActionMappingSequence getActionMappings() {
        return actionMappingSequence;
    }

    /**
     * Determines if this is the top-most window.
     *
     * @return {@code true} if this window is at the top of the Z-order of its display.
     */
    public boolean isTopMost() {
        Display display = getDisplay();
        return display.get(display.getLength() - 1) == this;
    }

    /**
     * Determines if this is the bottom-most window.
     *
     * @return {@code true} if this window is at the bottom of the Z-order of its display.
     */
    public boolean isBottomMost() {
        Display display = getDisplay();
        return display.get(0) == this;
    }

    /**
     * Moves the window to the top of the window stack. All owned windows are
     * subsequently moved to the front, ensuring that this window's owned
     * windows remain on top of it. If the window does not have any owned
     * windows, focus is restored to it.
     */
    public void moveToFront() {
        if (!isOpen()) {
            throw new IllegalStateException("Window is not open.");
        }

        // If this window is not currently top-most, move it to the top
        Display display = getDisplay();
        int top = display.getLength() - 1;

        int i = display.indexOf(this);
        if (i < top) {
            display.move(i, top);
        }

        int ownedWindowCount = ownedWindows.getLength();

        if (ownedWindowCount == 0) {
            // Restore focus
            if (isShowing() && isEnabled() && focusDescendant != null) {
                focusDescendant.requestFocus();
            }
        } else {
            // Move all open owned windows to the front of this window,
            // preserving the current z-order
            ArrayList<Integer> ownedWindowIndexes = new ArrayList<>(ownedWindowCount);

            for (Window ownedWindow : ownedWindows) {
                if (ownedWindow.isOpen()) {
                    ownedWindowIndexes.add(Integer.valueOf(display.indexOf(ownedWindow)));
                }
            }

            ArrayList.sort(ownedWindowIndexes);

            ArrayList<Window> sortedOwnedWindows = new ArrayList<>(ownedWindows.getLength());
            for (Integer index : ownedWindowIndexes) {
                sortedOwnedWindows.add((Window) display.get(index.intValue()));
            }

            for (Window ownedWindow : sortedOwnedWindows) {
                ownedWindow.moveToFront();
            }
        }
    }

    /**
     * Moves the window to the bottom of the window stack. If the window is
     * active, the active window will be cleared. If the window is the focus
     * host, the focus will be cleared.
     */
    public void moveToBack() {
        if (!isOpen()) {
            throw new IllegalStateException("Window is not open.");
        }

        if (isActive()) {
            clearActive();
        }

        if (containsFocus()) {
            clearFocus();
        }

        // Ensure that the window and all of its owning ancestors are moved
        // to the back
        Display display = getDisplay();

        int i = display.indexOf(this);
        if (i > 0) {
            display.move(i, 0);
        }

        if (owner != null) {
            owner.moveToBack();
        }
    }

    public boolean isMaximized() {
        return (restoreLocation != null);
    }

    public void setMaximized(boolean maximized) {
        if (maximized != isMaximized()) {
            if (maximized) {
                restoreLocation = getLocation();
                setLocation(0, 0);
            } else {
                setLocation(restoreLocation.x, restoreLocation.y);
                restoreLocation = null;
            }

            invalidate();

            windowListeners.maximizedChanged(this);
        }
    }

    public void align(Bounds bounds, HorizontalAlignment horizontalAlignment,
        VerticalAlignment verticalAlignment) {
        align(bounds, horizontalAlignment, 0, verticalAlignment, 0);
    }

    public void align(Bounds bounds, HorizontalAlignment horizontalAlignment, int horizontalOffset,
        VerticalAlignment verticalAlignment, int verticalOffset) {

        int x = 0;
        int y = 0;

        Dimensions size = getSize();

        switch (horizontalAlignment) {
            case LEFT:
                x = bounds.x - size.width;
                break;
            case RIGHT:
                x = bounds.x + bounds.width - size.width;
                break;
            case CENTER:
                x = bounds.x + (int) Math.round((double) (bounds.width - size.width) / 2);
                break;
            default:
                throw new IllegalArgumentException("Unsupported horizontal alignment.");
        }

        x += horizontalOffset;

        switch (verticalAlignment) {
            case TOP:
                y = bounds.y - size.height;
                break;
            case BOTTOM:
                y = bounds.y + bounds.height;
                break;
            case CENTER:
                y = bounds.y + (int) Math.round((double) (bounds.height - size.height) / 2);
                break;
            default:
                throw new IllegalArgumentException("Unsupported vertical alignment.");
        }

        y += verticalOffset;

        setLocation(x, y);
    }

    @Override
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        /*
         * Use keyPressed rather than keyReleased else this sequence: Press
         * Ctrl, Press C, Release Ctrl, Release C will not trigger the Ctrl-C
         * action.
         */
        boolean consumed = super.keyPressed(keyCode, keyLocation);

        // Perform any action defined for this keystroke
        // in the active window's action dictionary
        Keyboard.KeyStroke keyStroke = new Keyboard.KeyStroke(keyCode, Keyboard.getModifiers());

        Action action = actionMap.get(keyStroke);
        if (action != null && action.isEnabled()) {
            action.perform(this);
        }

        return consumed;
    }

    public ListenerList<WindowListener> getWindowListeners() {
        return windowListeners;
    }

    public ListenerList<WindowStateListener> getWindowStateListeners() {
        return windowStateListeners;
    }

    public ListenerList<WindowActionMappingListener> getWindowActionMappingListeners() {
        return windowActionMappingListeners;
    }

    public static ListenerList<WindowClassListener> getWindowClassListeners() {
        return windowClassListeners;
    }

}
