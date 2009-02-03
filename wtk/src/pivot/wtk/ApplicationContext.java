/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.util.ImmutableIterator;
import pivot.wtk.Component.DecoratorSequence;
import pivot.wtk.Manifest;
import pivot.wtk.RemoteManifest;
import pivot.wtk.effects.Decorator;

/**
 * Base class for application contexts.
 * <p>
 * TODO Fire events when entries are added to/removed from the cache?
 * <p>
 * TODO Provide a means of mapping common "actions" to keystrokes (e.g. "copy"
 * to Control-C or Command-C)
 *
 * @author gbrown
 */
public abstract class ApplicationContext {
    /**
     * Resource cache dictionary implementation.
     *
     * @author gbrown
     */
    public static final class ResourceCacheDictionary
        implements Dictionary<URL, Object>, Iterable<URL> {
        public Object get(URL key) {
            return resourceCache.get(key);
        }

        public Object put(URL key, Object value) {
            return resourceCache.put(key, value);
        }

        public Object remove(URL key) {
            return resourceCache.remove(key);
        }

        public boolean containsKey(URL key) {
            return resourceCache.containsKey(key);
        }

        public boolean isEmpty() {
            return resourceCache.isEmpty();
        }

        public Iterator<URL> iterator() {
            return new ImmutableIterator<URL>(resourceCache.iterator());
        }
    }

    /**
     * Native AWT display host.
     *
     * @author gbrown
     */
    public final class DisplayHost extends java.awt.Container {
        public static final long serialVersionUID = 0;

        private Point mouseLocation = null;

        private Component focusedComponent = null;

        private Point dragLocation = null;
        private Component dragDescendant = null;

        private Manifest dragManifest = null;

        private DropAction userDropAction = null;
        private Component dropDescendant = null;

        private DropTargetListener dropTargetListener = new DropTargetListener() {
            public void dragEnter(DropTargetDragEvent event) {
                if (dragDescendant != null) {
                    throw new IllegalStateException("Local drag already in progress.");
                }

                // Set display host
                Mouse.setDisplayHost(DisplayHost.this);

                java.awt.Point location = event.getLocation();
                mouseLocation = new Point(location.x, location.y);

                // Initialize drag state
                dragManifest = new RemoteManifest(event.getTransferable());

                // Initialize drop state
                userDropAction = getDropAction(event.getDropAction());

                // Notify drop target
                dropDescendant = getDropDescendant(location.x, location.y);

                DropAction dropAction = null;

                if (dropDescendant != null) {
                    DropTarget dropTarget = dropDescendant.getDropTarget();
                    dropAction = dropTarget.dragEnter(dropDescendant, dragManifest,
                        getSupportedDropActions(event.getSourceActions()), userDropAction);
                }

                if (dropAction == null) {
                    event.rejectDrag();
                } else {
                    event.acceptDrag(getNativeDropAction(dropAction));
                }
            }

            public void dragExit(DropTargetEvent event) {
                // Clear display host
                Mouse.setDisplayHost(null);
                mouseLocation = null;

                // Clear drag state
                dragManifest = null;

                // Clear drop state
                userDropAction = null;

                if (dropDescendant != null) {
                    DropTarget dropTarget = dropDescendant.getDropTarget();
                    dropTarget.dragExit(dropDescendant);
                }

                dropDescendant = null;
            }

            public void dragOver(DropTargetDragEvent event) {
                java.awt.Point location = event.getLocation();

                // Get the previous and current drop descendant and call
                // move or exit/enter as appropriate
                Component previousDropDescendant = dropDescendant;
                dropDescendant = getDropDescendant(location.x, location.y);

                DropAction dropAction = null;

                if (previousDropDescendant == dropDescendant) {
                    if (dropDescendant != null) {
                        DropTarget dropTarget = dropDescendant.getDropTarget();

                        Point dropLocation = dropDescendant.mapPointFromAncestor(display,
                            location.x, location.y);
                        dropAction = dropTarget.dragMove(dropDescendant, dragManifest,
                            getSupportedDropActions(event.getSourceActions()),
                            dropLocation.x, dropLocation.y, userDropAction);
                    }
                } else {
                    if (previousDropDescendant != null) {
                        DropTarget previousDropTarget = previousDropDescendant.getDropTarget();
                        previousDropTarget.dragExit(previousDropDescendant);
                    }

                    if (dropDescendant != null) {
                        DropTarget dropTarget = dropDescendant.getDropTarget();
                        dropAction = dropTarget.dragEnter(dropDescendant, dragManifest,
                            getSupportedDropActions(event.getSourceActions()),
                            userDropAction);
                    }
                }

                // Update cursor
                setCursor(getDropCursor(dropAction));

                if (dropAction == null) {
                    event.rejectDrag();
                } else {
                    event.acceptDrag(getNativeDropAction(dropAction));
                }
            }

            public void dropActionChanged(DropTargetDragEvent event) {
                userDropAction = getDropAction(event.getDropAction());

                DropAction dropAction = null;

                if (dropDescendant != null) {
                    java.awt.Point location = event.getLocation();
                    Point dropLocation = dropDescendant.mapPointFromAncestor(display,
                        location.x, location.y);

                    DropTarget dropTarget = dropDescendant.getDropTarget();
                    dropAction = dropTarget.userDropActionChange(dropDescendant, dragManifest,
                        getSupportedDropActions(event.getSourceActions()), dropLocation.x, dropLocation.y,
                        userDropAction);
                }

                if (dropAction == null) {
                    event.rejectDrag();
                } else {
                    event.acceptDrag(getNativeDropAction(dropAction));
                }
            }

            public void drop(DropTargetDropEvent event) {
                java.awt.Point location = event.getLocation();
                dropDescendant = getDropDescendant(location.x, location.y);

                DropAction dropAction = null;

                if (dropDescendant != null) {
                    Point dropLocation = dropDescendant.mapPointFromAncestor(display,
                        location.x, location.y);
                    DropTarget dropTarget = dropDescendant.getDropTarget();

                    // Simulate a user drop action change to get the current drop action
                    int supportedDropActions = getSupportedDropActions(event.getSourceActions());

                    dropAction = dropTarget.userDropActionChange(dropDescendant, dragManifest,
                        supportedDropActions, dropLocation.x, dropLocation.y, userDropAction);

                    if (dropAction != null) {
                        // Perform the drop
                        event.acceptDrop(getNativeDropAction(dropAction));
                        dropTarget.drop(dropDescendant, dragManifest,
                            supportedDropActions, dropLocation.x, dropLocation.y, userDropAction);
                    }
                }

                if (dropAction == null) {
                    event.rejectDrop();
                }

                event.dropComplete(true);

                // Restore the cursor to the default
                setCursor(java.awt.Cursor.getDefaultCursor());

                // Clear drag state
                dragManifest = null;

                // Clear drop state
                dropDescendant = null;
            }
        };

        protected DisplayHost() {
            enableEvents(AWTEvent.COMPONENT_EVENT_MASK
                | AWTEvent.FOCUS_EVENT_MASK
                | AWTEvent.MOUSE_EVENT_MASK
                | AWTEvent.MOUSE_MOTION_EVENT_MASK
                | AWTEvent.MOUSE_WHEEL_EVENT_MASK
                | AWTEvent.KEY_EVENT_MASK);

            try {
                System.setProperty("sun.awt.noerasebackground", "true");
                System.setProperty("sun.awt.erasebackgroundonresize", "true");
            } catch (SecurityException exception) {
            }

            // Add debug event queue
            EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
            eventQueue.push(new EventQueue() {
                public void postEvent(AWTEvent event) {
                    // TODO System.out.println(event);
                    super.postEvent(event);
                }
                public void dispatchEvent(AWTEvent event) {
                    // TODO System.out.println(event);
                    super.dispatchEvent(event);
                }
            });

            // Add native drop support
            new java.awt.dnd.DropTarget(this, dropTargetListener);

            setFocusTraversalKeysEnabled(false);
        }

        public Point getMouseLocation() {
            return mouseLocation;
        }

        @Override
        public void repaint(int x, int y, int width, int height) {
            // Ensure that the repaint call is properly bounded (some
            // implementations of AWT do not properly clip the repaint call
            // when x or y is negative: the negative value is converted to 0,
            // but the width/height is not adjusted)
            if (x < 0) {
                width = Math.max(width + x, 0);
                x = 0;
            }

            if (y < 0) {
                height = Math.max(height + y, 0);
                y = 0;
            }

            if (width > 0
                && height > 0) {
                super.repaint(x, y, width, height);
            }
        }

        @Override
        public void paint(Graphics graphics) {
            // Intersect the clip region with the bounds of this component
            // (for some reason, AWT does not do this automatically)
            ((Graphics2D)graphics).clip(new java.awt.Rectangle(0, 0, getWidth(), getHeight()));

            java.awt.Rectangle clipBounds = graphics.getClipBounds();
            if (clipBounds != null
                && !clipBounds.isEmpty()) {
                try {
                    if (!paintVolatileBuffered((Graphics2D)graphics)) {
                        if (!paintBuffered((Graphics2D)graphics)) {
                            paintDisplay((Graphics2D)graphics);
                        }
                    }
                } catch (RuntimeException exception) {
                    System.out.println("Exception thrown during paint(): " + exception);
                    throw exception;
                }
            }
        }

        /**
         * Attempts to paint the display using an offscreen buffer.
         *
         * @param graphics
         * The source graphics context.
         *
         * @return
         * <tt>true</tt> if the display was painted using the offscreen
         * buffer; <tt>false</tt>, otherwise.
         */
        private boolean paintBuffered(Graphics2D graphics) {
            boolean painted = false;

            // Paint the display into an offscreen buffer
            GraphicsConfiguration gc = graphics.getDeviceConfiguration();
            java.awt.Rectangle clipBounds = graphics.getClipBounds();
            java.awt.image.BufferedImage bufferedImage =
                gc.createCompatibleImage(clipBounds.width, clipBounds.height,
                    Transparency.OPAQUE);

            if (bufferedImage != null) {
                Graphics2D bufferedImageGraphics = (Graphics2D)bufferedImage.getGraphics();
                bufferedImageGraphics.setClip(0, 0, clipBounds.width, clipBounds.height);
                bufferedImageGraphics.translate(-clipBounds.x, -clipBounds.y);

                try {
                    paintDisplay(bufferedImageGraphics);
                    graphics.drawImage(bufferedImage, clipBounds.x, clipBounds.y, this);
                } finally {
                    bufferedImageGraphics.dispose();
                }

                painted = true;
            }

            return painted;
        }

        /**
         * Attempts to paint the display using a volatile offscreen buffer.
         *
         * @param graphics
         * The source graphics context.
         *
         * @return
         * <tt>true</tt> if the display was painted using the offscreen
         * buffer; <tt>false</tt>, otherwise.
         */
        private boolean paintVolatileBuffered(Graphics2D graphics) {
            boolean painted = false;

            // Paint the display into a volatile offscreen buffer
            GraphicsConfiguration gc = graphics.getDeviceConfiguration();
            java.awt.Rectangle clipBounds = graphics.getClipBounds();
            java.awt.image.VolatileImage volatileImage =
                gc.createCompatibleVolatileImage(clipBounds.width, clipBounds.height,
                    Transparency.OPAQUE);

            // If we have a valid volatile image, attempt to paint the
            // display to it
            if (volatileImage != null) {
                int valid = volatileImage.validate(getGraphicsConfiguration());

                if (valid == java.awt.image.VolatileImage.IMAGE_OK
                    || valid == java.awt.image.VolatileImage.IMAGE_RESTORED) {
                    Graphics2D volatileImageGraphics = (Graphics2D)volatileImage.getGraphics();
                    volatileImageGraphics.setClip(0, 0, clipBounds.width, clipBounds.height);
                    volatileImageGraphics.translate(-clipBounds.x, -clipBounds.y);

                    try {
                        paintDisplay(volatileImageGraphics);
                        graphics.drawImage(volatileImage, clipBounds.x, clipBounds.y, this);
                    } finally {
                        volatileImageGraphics.dispose();
                    }

                    painted = !volatileImage.contentsLost();
                }
            }

            return painted;
        }

        /**
         * Paints the display including any decorators.
         *
         * @param graphics
         */
        private void paintDisplay(Graphics2D graphics) {
            Graphics2D decoratedGraphics = graphics;

            DecoratorSequence decorators = display.getDecorators();
            int n = decorators.getLength();
            for (int i = n - 1; i >= 0; i--) {
                Decorator decorator = decorators.get(i);
                decoratedGraphics = decorator.prepare(display, decoratedGraphics);
            }

            display.paint(graphics);

            for (int i = 0; i < n; i++) {
                Decorator decorator = decorators.get(i);
                decorator.update();
            }

            // Paint the drag visual
            if (dragDescendant != null) {
                DragSource dragSource = dragDescendant.getDragSource();
                Visual dragRepresentation = dragSource.getRepresentation();

                if (dragRepresentation != null) {
                    Point dragOffset = dragSource.getOffset();
                    int tx = dragLocation.x - dragOffset.x;
                    int ty = dragLocation.y - dragOffset.y;

                    graphics.translate(tx, ty);
                    dragRepresentation.paint(graphics);
                }
            }
        }

        private void repaintDragRepresentation() {
            DragSource dragSource = dragDescendant.getDragSource();
            Visual dragRepresentation = dragSource.getRepresentation();

            if (dragRepresentation != null) {
                Point dragOffset = dragSource.getOffset();

                repaint(dragLocation.x - dragOffset.x, dragLocation.y - dragOffset.y,
                    dragRepresentation.getWidth(), dragRepresentation.getHeight());
            }
        }

        private Component getDropDescendant(int x, int y) {
            Component dropDescendant = display.getDescendantAt(x, y);

            while (dropDescendant != null
                && dropDescendant.getDropTarget() == null) {
                dropDescendant = dropDescendant.getParent();
            }

            if (dropDescendant != null
                && dropDescendant.isBlocked()) {
                dropDescendant = null;
            }

            return dropDescendant;
        }

        private void startNativeDrag(final DragSource dragSource, final MouseEvent mouseEvent) {
            java.awt.dnd.DragSource awtDragSource = java.awt.dnd.DragSource.getDefaultDragSource();

            final int supportedDropActions = dragSource.getSupportedDropActions();

            DragGestureRecognizer dragGestureRecognizer =
                new DragGestureRecognizer(java.awt.dnd.DragSource.getDefaultDragSource(), displayHost) {
                private static final long serialVersionUID = 0;

                {   appendEvent(mouseEvent);
                }

                public int getSourceActions() {
                    int awtSourceActions = 0;

                    if (DropAction.COPY.isSelected(supportedDropActions)) {
                        awtSourceActions |= DnDConstants.ACTION_COPY;
                    }

                    if (DropAction.MOVE.isSelected(supportedDropActions)) {
                        awtSourceActions |= DnDConstants.ACTION_MOVE;
                    }

                    if (DropAction.LINK.isSelected(supportedDropActions)) {
                        awtSourceActions |= DnDConstants.ACTION_LINK;
                    }

                    return awtSourceActions;
                }

                protected void registerListeners() {
                    // No-op
                }

                protected void unregisterListeners() {
                    // No-op
                }
            };

            java.util.List<InputEvent> inputEvents = new java.util.ArrayList<InputEvent>();
            inputEvents.add(mouseEvent);

            // TODO If current user drop action is supported by drag source, use it
            // as initial action - otherwise, select MOVE, COPY, LINK in that order
            java.awt.Point location = new java.awt.Point(mouseEvent.getX(), mouseEvent.getY());
            DragGestureEvent trigger = new DragGestureEvent(dragGestureRecognizer,
                DnDConstants.ACTION_MOVE, location, inputEvents);

            LocalManifest dragContent = dragSource.getContent();
            LocalManifestAdapter localManifestAdapter = new LocalManifestAdapter(dragContent);

            awtDragSource.startDrag(trigger, java.awt.Cursor.getDefaultCursor(),
                null, null, localManifestAdapter, new DragSourceListener() {
                public void dragEnter(DragSourceDragEvent event) {
                    DragSourceContext context = event.getDragSourceContext();
                    context.setCursor(getDropCursor(getDropAction(event.getDropAction())));
                }

                public void dragExit(DragSourceEvent event) {
                    DragSourceContext context = event.getDragSourceContext();
                    context.setCursor(java.awt.Cursor.getDefaultCursor());
                }

                public void dragOver(DragSourceDragEvent event) {
                    DragSourceContext context = event.getDragSourceContext();
                    context.setCursor(getDropCursor(getDropAction(event.getDropAction())));
                }

                public void dropActionChanged(DragSourceDragEvent event) {
                    DragSourceContext context = event.getDragSourceContext();
                    context.setCursor(getDropCursor(getDropAction(event.getDropAction())));
                }

                public void dragDropEnd(DragSourceDropEvent event) {
                    DragSourceContext context = event.getDragSourceContext();
                    context.setCursor(java.awt.Cursor.getDefaultCursor());
                }
            });
        }

        @Override
        protected void processComponentEvent(ComponentEvent event) {
            super.processComponentEvent(event);

            switch (event.getID()) {
                case ComponentEvent.COMPONENT_RESIZED: {
                    display.setSize(getWidth(), getHeight());
                    break;
                }

                case ComponentEvent.COMPONENT_MOVED: {
                    // No-op
                    break;
                }

                case ComponentEvent.COMPONENT_SHOWN: {
                    // No-op
                    break;
                }

                case ComponentEvent.COMPONENT_HIDDEN: {
                    // No-op
                    break;
                }
            }
        }

        @Override
        protected void processFocusEvent(FocusEvent event) {
            super.processFocusEvent(event);

            switch(event.getID()) {
                case FocusEvent.FOCUS_GAINED: {
                    if (focusedComponent != null
                        && focusedComponent.isShowing()
                        && !focusedComponent.isBlocked()) {
                        focusedComponent.requestFocus(true);
                    }

                    break;
                }

                case FocusEvent.FOCUS_LOST: {
                    focusedComponent = Component.getFocusedComponent();
                    Component.clearFocus(true);

                    break;
                }
            }
        }

        @Override
        protected void processMouseEvent(MouseEvent event) {
            super.processMouseEvent(event);

            int x = event.getX();
            int y = event.getY();

            // Set the mouse button state
            int mouseButtons = 0x00;

            int modifiersEx = event.getModifiersEx();
            if ((modifiersEx & MouseEvent.BUTTON1_DOWN_MASK) > 0) {
                mouseButtons |= Mouse.Button.LEFT.getMask();
            }

            if ((modifiersEx & MouseEvent.BUTTON2_DOWN_MASK) > 0) {
                mouseButtons |= Mouse.Button.MIDDLE.getMask();
            }

            if ((modifiersEx & MouseEvent.BUTTON3_DOWN_MASK) > 0) {
                mouseButtons |= Mouse.Button.RIGHT.getMask();
            }

            Mouse.setButtons(mouseButtons);

            // Get the button associated with this event
            Mouse.Button button = null;
            switch (event.getButton()) {
                case MouseEvent.BUTTON1: {
                    button = Mouse.Button.LEFT;
                    break;
                }

                case MouseEvent.BUTTON2: {
                    button = Mouse.Button.MIDDLE;
                    break;
                }

                case MouseEvent.BUTTON3: {
                    button = Mouse.Button.RIGHT;
                    break;
                }
            }

            // Process the event
            switch (event.getID()) {
                case MouseEvent.MOUSE_PRESSED: {
                    requestFocus();
                    dragLocation = new Point(x, y);
                    display.mouseDown(button, x, y);
                    break;
                }

                case MouseEvent.MOUSE_RELEASED: {
                    if (dragDescendant == null) {
                        display.mouseUp(button, x, y);
                    } else {
                        DragSource dragSource = dragDescendant.getDragSource();

                        repaintDragRepresentation();

                        if (dropDescendant == null) {
                            dragSource.endDrag(dragDescendant, null);
                        } else {
                            DropTarget dropTarget = dropDescendant.getDropTarget();
                            DropAction dropAction = dropTarget.drop(dropDescendant, dragManifest,
                                dragSource.getSupportedDropActions(), x, y, getUserDropAction(event));
                            dragSource.endDrag(dragDescendant, dropAction);
                        }

                        setCursor(java.awt.Cursor.getDefaultCursor());

                        // Clear the drag state
                        dragDescendant = null;
                        dragManifest = null;

                        // Clear the drop state
                        userDropAction = null;
                        dropDescendant = null;
                    }

                    // Clear the drag location
                    dragLocation = null;

                    break;
                }

                case MouseEvent.MOUSE_ENTERED: {
                    Mouse.setDisplayHost(this);
                    mouseLocation = new Point(x, y);
                    display.mouseOver();
                    break;
                }

                case MouseEvent.MOUSE_EXITED: {
                    display.mouseOut();
                    mouseLocation = null;
                    Mouse.setDisplayHost(null);
                    break;
                }
            }
        }

        @Override
        protected void processMouseMotionEvent(MouseEvent event) {
            super.processMouseMotionEvent(event);

            int x = event.getX();
            int y = event.getY();

            // Set the mouse location
            if (mouseLocation == null) {
                mouseLocation = new Point();
            }

            mouseLocation.x = x;
            mouseLocation.y = y;

            // Process the event
            switch (event.getID()) {
                case MouseEvent.MOUSE_MOVED:
                case MouseEvent.MOUSE_DRAGGED: {
                    if (dragDescendant == null) {
                        // A drag is not active, so propagate the event to the display
                        display.mouseMove(x, y);

                        int dragThreshold = Platform.getDragThreshold();

                        if (dragLocation != null
                            && (Math.abs(x - dragLocation.x) > dragThreshold
                                || Math.abs(y - dragLocation.y) > dragThreshold)) {
                            // The user has dragged the mouse past the drag threshold; try
                            // to find a drag source
                            dragDescendant = display.getDescendantAt(dragLocation.x,
                                dragLocation.y);

                            while (dragDescendant != null
                                && dragDescendant.getDragSource() == null) {
                                dragDescendant = dragDescendant.getParent();
                            }

                            if (dragDescendant == null
                                || dragDescendant.isBlocked()) {
                                // There was nothing to drag, so clear the drag location
                                dragDescendant = null;
                                dragLocation = null;
                            } else {
                                DragSource dragSource = dragDescendant.getDragSource();
                                dragLocation = dragDescendant.mapPointFromAncestor(display, x, y);

                                if (dragSource.beginDrag(dragDescendant, dragLocation.x, dragLocation.y)) {
                                    // A drag has started
                                    if (dragSource.isNative()) {
                                        // Clear the drag state since it is not used for
                                        // native drags
                                        dragDescendant = null;
                                        dragLocation = null;

                                        startNativeDrag(dragSource, event);
                                    } else {
                                        if (dragSource.getRepresentation() != null
                                            && dragSource.getOffset() == null) {
                                            throw new IllegalStateException("Drag offset is required when a "
                                                + " respresentation is specified.");
                                        }

                                        if (display.isMouseOver()) {
                                            display.mouseOut();
                                        }

                                        // Get the drag content
                                        dragManifest = dragSource.getContent();

                                        // Get the initial user drop action
                                        userDropAction = getUserDropAction(event);

                                        // Repaint the drag visual
                                        dragLocation.x = x;
                                        dragLocation.y = y;

                                        repaintDragRepresentation();
                                    }
                                } else {
                                    // Clear the drag state
                                    dragDescendant = null;
                                    dragLocation = null;
                                }
                            }
                        }
                    } else {
                        if (dragLocation != null) {
                            DragSource dragSource = dragDescendant.getDragSource();

                            // Get the previous and current drop descendant and call
                            // move or exit/enter as appropriate
                            Component previousDropDescendant = dropDescendant;
                            dropDescendant = getDropDescendant(x, y);

                            DropAction dropAction = null;

                            if (previousDropDescendant == dropDescendant) {
                                if (dropDescendant != null) {
                                    DropTarget dropTarget = dropDescendant.getDropTarget();

                                    Point dropLocation = dropDescendant.mapPointFromAncestor(display, x, y);
                                    dropAction = dropTarget.dragMove(dropDescendant, dragManifest,
                                        dragSource.getSupportedDropActions(),
                                        dropLocation.x, dropLocation.y, userDropAction);
                                }
                            } else {
                                if (previousDropDescendant != null) {
                                    DropTarget previousDropTarget = previousDropDescendant.getDropTarget();
                                    previousDropTarget.dragExit(previousDropDescendant);
                                }

                                if (dropDescendant != null) {
                                    DropTarget dropTarget = dropDescendant.getDropTarget();
                                    dropAction = dropTarget.dragEnter(dropDescendant, dragManifest,
                                        dragSource.getSupportedDropActions(), userDropAction);
                                }
                            }

                            // Update cursor
                            setCursor(getDropCursor(dropAction));

                            // Repaint the drag visual
                            repaintDragRepresentation();

                            dragLocation.x = x;
                            dragLocation.y = y;

                            repaintDragRepresentation();
                        }
                    }

                    break;
                }
            }
        }

        @Override
        protected void processMouseWheelEvent(MouseWheelEvent event) {
            super.processMouseWheelEvent(event);

            // Get the event coordinates
            int x = event.getX();
            int y = event.getY();

            // Get the scroll type
            Mouse.ScrollType scrollType = null;
            switch (event.getScrollType()) {
                case MouseWheelEvent.WHEEL_BLOCK_SCROLL: {
                    scrollType = Mouse.ScrollType.BLOCK;
                    break;
                }

                case MouseWheelEvent.WHEEL_UNIT_SCROLL: {
                    scrollType = Mouse.ScrollType.UNIT;
                    break;
                }
            }

            // Process the event
            switch (event.getID()) {
                case MouseEvent.MOUSE_WHEEL: {
                    if (dragDescendant == null) {
                        display.mouseWheel(scrollType, event.getScrollAmount(),
                            event.getWheelRotation(), x, y);
                    }
                    break;
                }
            }
        }

        @Override
        protected void processKeyEvent(KeyEvent event) {
            super.processKeyEvent(event);

            // Set the keyboard modifier state
            int keyboardModifiers = 0;

            int modifiersEx = event.getModifiersEx();
            if ((modifiersEx & KeyEvent.SHIFT_DOWN_MASK) > 0) {
                keyboardModifiers |= Keyboard.Modifier.SHIFT.getMask();
            }

            if ((modifiersEx & KeyEvent.CTRL_DOWN_MASK) > 0) {
                keyboardModifiers |= Keyboard.Modifier.CTRL.getMask();
            }

            if ((modifiersEx & KeyEvent.ALT_DOWN_MASK) > 0) {
                keyboardModifiers |= Keyboard.Modifier.ALT.getMask();
            }

            if ((modifiersEx & KeyEvent.META_DOWN_MASK) > 0) {
                keyboardModifiers |= Keyboard.Modifier.META.getMask();
            }

            Keyboard.setModifiers(keyboardModifiers);

            // Get the key location
            Keyboard.KeyLocation keyLocation = null;
            switch (event.getKeyLocation()) {
                case KeyEvent.KEY_LOCATION_STANDARD: {
                    keyLocation = Keyboard.KeyLocation.STANDARD;
                    break;
                }

                case KeyEvent.KEY_LOCATION_LEFT: {
                    keyLocation = Keyboard.KeyLocation.LEFT;
                    break;
                }

                case KeyEvent.KEY_LOCATION_RIGHT: {
                    keyLocation = Keyboard.KeyLocation.RIGHT;
                    break;
                }

                case KeyEvent.KEY_LOCATION_NUMPAD: {
                    keyLocation = Keyboard.KeyLocation.KEYPAD;
                    break;
                }
            }

            if (dragDescendant == null) {
                // Process the event
                Component focusedComponent = Component.getFocusedComponent();

                switch (event.getID()) {
                    case KeyEvent.KEY_PRESSED: {
                        int keyCode = event.getKeyCode();
                        boolean consumed = false;

                        if (focusedComponent != null) {
                            consumed = focusedComponent.keyPressed(keyCode, keyLocation);
                        }

                        if (consumed) {
                            event.consume();
                        }

                        break;
                    }

                    case KeyEvent.KEY_RELEASED: {
                        int keyCode = event.getKeyCode();
                        boolean consumed = false;

                        if (focusedComponent != null) {
                            consumed = focusedComponent.keyReleased(keyCode, keyLocation);
                        }

                        if (consumed) {
                            event.consume();
                        }

                        break;
                    }

                    case KeyEvent.KEY_TYPED: {
                        boolean consumed = false;

                        if (focusedComponent != null) {
                            char keyChar = event.getKeyChar();
                            consumed = focusedComponent.keyTyped(keyChar);
                        }

                        if (consumed) {
                            event.consume();
                        }

                        break;
                    }
                }
            } else {
                DragSource dragSource = dragDescendant.getDragSource();

                // If the user drop action changed, notify the drop descendant
                if (dropDescendant != null) {
                    DropAction previousUserDropAction = userDropAction;
                    userDropAction = getUserDropAction(event);

                    if (previousUserDropAction != userDropAction) {
                        DropTarget dropTarget = dropDescendant.getDropTarget();

                        Point dropLocation = dropDescendant.mapPointFromAncestor(display,
                            mouseLocation.x, mouseLocation.y);
                        dropTarget.userDropActionChange(dropDescendant, dragManifest,
                            dragSource.getSupportedDropActions(),
                            dropLocation.x, dropLocation.y, userDropAction);
                    }
                }
            }
        }
    }

    private static class IntervalTask extends TimerTask {
        private Runnable runnable = null;

        public IntervalTask(Runnable runnable) {
            this.runnable = runnable;
        }

        public void run() {
            queueCallback(runnable);
        }
    }

    private static class TimeoutTask extends TimerTask {
        private Runnable runnable = null;
        int timeoutID = -1;

        public TimeoutTask(Runnable runnable, int timeoutID) {
            this.runnable = runnable;
            this.timeoutID = timeoutID;
        }

        public void run() {
            queueCallback(runnable, true);
            clearTimeout(timeoutID);
        }
    }

    private DisplayHost displayHost;
    private Display display;

    protected static URL origin = null;

    private static HashMap<URL, Object> resourceCache = new HashMap<URL, Object>();
    private static ResourceCacheDictionary resourceCacheDictionary = new ResourceCacheDictionary();

    private static Timer timer = new Timer(true);
    private static HashMap<Integer, TimerTask> timerTaskMap = new HashMap<Integer, TimerTask>();
    private static int nextTimerTaskID = 0;

    private static final String DEFAULT_THEME_CLASS_NAME = "pivot.wtk.skin.terra.TerraTheme";

    protected ApplicationContext() {
        // Create the display host and display
        displayHost = new DisplayHost();
        display = new Display(displayHost);

        try {
            // Load and instantiate the default theme, if possible
            Class<?> themeClass = Class.forName(DEFAULT_THEME_CLASS_NAME);
            Theme.setTheme((Theme)themeClass.newInstance());
        } catch (Exception exception) {
            // No-op; assume that a custom theme will be installed later
            // by the caller
            System.out.println("Warning: Unable to load default theme.");
        }
    }

    protected DisplayHost getDisplayHost() {
        return displayHost;
    }

    protected Display getDisplay() {
        return display;
    }

    /**
     * Returns this application's origin (the URL of it's originating server).
     */
    public static URL getOrigin() {
        return origin;
    }

    /**
     * Resource properties accessor.
     */
    public static ResourceCacheDictionary getResourceCache() {
        return resourceCacheDictionary;
    }

    /**
     * Opens the resource at the given location.
     *
     * @param location
     */
    public static void open(URL location) {
        // TODO Remove dynamic invocation when Java 6 is supported on the Mac

        try {
            Class<?> desktopClass = Class.forName("java.awt.Desktop");
            Method getDesktopMethod = desktopClass.getMethod("getDesktop", new Class<?>[] {});
            Method browseMethod = desktopClass.getMethod("browse", new Class[] {URI.class});
            Object desktop = getDesktopMethod.invoke(null, (Object[]) null);
            browseMethod.invoke(desktop, location.toURI());
        } catch (Exception exception) {
            System.out.println("Unable to open URL in default browser.");
        }
    }

    /**
     * Issues a system alert sound.
     */
    public static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Schedules a task for repeated execution. The task will be executed on the
     * UI thread.
     *
     * @param runnable
     * The task to execute.
     *
     * @param period
     * The interval at which the task should be executed.
     *
     * @return An integer ID that can be used to cancel execution of the task.
     */
    public static int setInterval(Runnable runnable, long period) {
        int intervalID = nextTimerTaskID++;

        IntervalTask intervalTask = new IntervalTask(runnable);
        timerTaskMap.put(intervalID, intervalTask);

        try {
            timer.schedule(intervalTask, 0, period);
        } catch (IllegalStateException exception) {
            System.out.println(exception.getMessage());

            // TODO This is a workaround for an apparent bug in the Mac OSX
            // Java Plugin, which appears to prematurely kill the timer thread.
            // Remove this when the issue is fixed.
            timer = new Timer(true);
            timerTaskMap.clear();
            timerTaskMap.put(intervalID, intervalTask);
            timer.schedule(intervalTask, 0, period);
        }

        return intervalID;
    }

    /**
     * Cancels execution of a scheduled task.
     *
     * @param intervalID
     * The ID of the task to cancel.
     */
    public static void clearInterval(int intervalID) {
        clearTimerTask(intervalID);
    }

    /**
     * Schedules a task for execution after an elapsed time.
     *
     * @param runnable
     * The task to execute.
     *
     * @param timeout
     * The time after which the task should begin executing.
     */
    public static int setTimeout(Runnable runnable, long timeout) {
        int timeoutID = nextTimerTaskID++;

        TimeoutTask timeoutTask = new TimeoutTask(runnable, timeoutID);
        timerTaskMap.put(timeoutID, timeoutTask);

        try {
            timer.schedule(timeoutTask, timeout);
        } catch (IllegalStateException exception) {
            System.out.println(exception.getMessage());

            // TODO This is a workaround for an apparent bug in the Mac OSX
            // Java Plugin, which appears to prematurely kill the timer thread.
            // Remove this when the issue is fixed.
            timer = new Timer(true);
            timerTaskMap.clear();
            timerTaskMap.put(timeoutID, timeoutTask);
            timer.schedule(timeoutTask, timeout);
        }

        return timeoutID;
    }

    /**
     * Cancels execution of a scheduled task.
     *
     * @param timeoutID
     * The ID of the task to cancel.
     */
    public static void clearTimeout(int timeoutID) {
        clearTimerTask(timeoutID);
    }

    /**
     * Cancels execution of a timer task.
     *
     * @param timerTaskID
     */
    private static void clearTimerTask(int timerTaskID) {
        TimerTask timerTask = timerTaskMap.remove(timerTaskID);
        if (timerTask != null) {
            timerTask.cancel();
        }
    }

    /**
     * Queues a task to execute after all pending events have been processed and
     * returns without waiting for the task to complete.
     *
     * @param runnable
     * The runnable to execute.
     */
    public static void queueCallback(Runnable runnable) {
        queueCallback(runnable, false);
    }

    /**
     * Queues a task to execute after all pending events have been processed and
     * optionally waits for the task to complete.
     *
     * @param runnable
     * The runnable to execute.
     *
     * @param wait
     * If <tt>true</tt>, does not return until the runnable has executed.
     * Otherwise, returns immediately.
     */
    public static void queueCallback(Runnable runnable, boolean wait) {
        if (wait) {
            try {
                java.awt.EventQueue.invokeAndWait(runnable);
            } catch (InvocationTargetException exception) {
            } catch (InterruptedException exception) {
            }
        } else {
            java.awt.EventQueue.invokeLater(runnable);
        }
    }

    private static DropAction getUserDropAction(InputEvent event) {
        DropAction userDropAction;

        if ((event.isControlDown() && event.isShiftDown())
            || (event.isAltDown() && event.isMetaDown())) {
            userDropAction = DropAction.LINK;
        } else if (event.isControlDown()
            || (event.isAltDown())) {
            userDropAction = DropAction.COPY;
        } else if (event.isShiftDown()){
            userDropAction = DropAction.MOVE;
        } else {
            userDropAction = null;
        }

        return userDropAction;
    }

    private static DropAction getDropAction(int nativeDropAction) {
        DropAction dropAction = null;

        switch (nativeDropAction) {
            case DnDConstants.ACTION_COPY: {
                dropAction = DropAction.COPY;
                break;
            }

            case DnDConstants.ACTION_MOVE: {
                dropAction = DropAction.MOVE;
                break;
            }

            case DnDConstants.ACTION_LINK: {
                dropAction = DropAction.LINK;
                break;
            }
        }

        return dropAction;
    }

    private static int getSupportedDropActions(int sourceActions) {
        int dropActions = 0;

        if ((sourceActions & DnDConstants.ACTION_COPY) > 0) {
            dropActions |= DropAction.COPY.getMask();
        }

        if ((sourceActions & DnDConstants.ACTION_MOVE) > 0) {
            dropActions |= DropAction.MOVE.getMask();
        }

        if ((sourceActions & DnDConstants.ACTION_LINK) > 0) {
            dropActions |= DropAction.LINK.getMask();
        }

        return dropActions;
    }

    private static int getNativeDropAction(DropAction dropAction) {
        int nativeDropAction = 0;

        if (dropAction != null) {
            switch(dropAction) {
                case COPY: {
                    nativeDropAction = DnDConstants.ACTION_COPY;
                    break;
                }

                case MOVE: {
                    nativeDropAction = DnDConstants.ACTION_MOVE;
                    break;
                }

                case LINK: {
                    nativeDropAction = DnDConstants.ACTION_LINK;
                    break;
                }
            }
        }

        return nativeDropAction;
    }

    private static java.awt.Cursor getDropCursor(DropAction dropAction) {
        // Update the drop cursor
        java.awt.Cursor cursor = java.awt.Cursor.getDefaultCursor();

        if (dropAction != null) {
            // Show the cursor for the drop action returned by the
            // drop target
            switch (dropAction) {
                case COPY: {
                    cursor = java.awt.dnd.DragSource.DefaultCopyDrop;
                    break;
                }

                case MOVE: {
                    cursor = java.awt.dnd.DragSource.DefaultMoveDrop;
                    break;
                }

                case LINK: {
                    cursor = java.awt.dnd.DragSource.DefaultLinkDrop;
                    break;
                }
            }
        }

        return cursor;
    }
}
