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

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.PrintGraphics;
import java.awt.RenderingHints;
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
import java.awt.event.InputMethodEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.im.InputMethodRequests;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.awt.print.PrinterGraphics;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ExceptionUtils;
import org.apache.pivot.util.Version;
import org.apache.pivot.wtk.Component.DecoratorSequence;
import org.apache.pivot.wtk.effects.Decorator;
import org.apache.pivot.wtk.effects.ShadeDecorator;

/**
 * Base class for application contexts.
 */
public abstract class ApplicationContext implements Application.UncaughtExceptionHandler {
    /**
     * Native display host. This is the Pivot interface with AWT.
     */
    public static class DisplayHost extends java.awt.Component {
        private static final long serialVersionUID = -815713849595314026L;

        private transient Display display = new Display(this);
        private AWTEvent currentAWTEvent = null;

        private Component focusedComponent = null;

        private Point dragLocation = null;
        private Component dragDescendant = null;
        private transient Manifest dragManifest = null;
        private DropAction userDropAction = null;
        private Component dropDescendant = null;

        private MenuPopup menuPopup = null;

        private double scale = 1.0;

        private boolean debugPaint = false;

        private boolean bufferedImagePaintEnabled = true;
        private BufferedImage bufferedImage = null;
        private GraphicsConfiguration bufferedImageGC = null;

        private boolean volatileImagePaintEnabled = true;
        private VolatileImage volatileImage = null;
        private GraphicsConfiguration volatileImageGC = null;

        private Random random = null;

        private transient DropTargetListener dropTargetListener = new DropTargetListener() {
            @Override
            public void dragEnter(final DropTargetDragEvent event) {
                if (dragDescendant != null) {
                    throw new IllegalStateException("Local drag already in progress.");
                }

                java.awt.Point location = event.getLocation();
                dragLocation = new Point(location.x, location.y);

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

                display.validate();
            }

            @Override
            public void dragExit(final DropTargetEvent event) {
                // Clear drag location and state
                dragLocation = null;
                dragManifest = null;

                // Clear drop state
                userDropAction = null;

                if (dropDescendant != null) {
                    DropTarget dropTarget = dropDescendant.getDropTarget();
                    dropTarget.dragExit(dropDescendant);
                }

                dropDescendant = null;

                display.validate();
            }

            @Override
            public void dragOver(final DropTargetDragEvent event) {
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
                        if (dropLocation == null) {
                            dropLocation = display.getMouseLocation();
                        }
                        if (dropLocation != null) {
                            dropAction = dropTarget.dragMove(dropDescendant, dragManifest,
                                getSupportedDropActions(event.getSourceActions()),
                                dropLocation.x, dropLocation.y, userDropAction);
                        }
                    }
                } else {
                    if (previousDropDescendant != null) {
                        DropTarget previousDropTarget = previousDropDescendant.getDropTarget();
                        previousDropTarget.dragExit(previousDropDescendant);
                    }

                    if (dropDescendant != null) {
                        DropTarget dropTarget = dropDescendant.getDropTarget();
                        dropAction = dropTarget.dragEnter(dropDescendant, dragManifest,
                            getSupportedDropActions(event.getSourceActions()), userDropAction);
                    }
                }

                // Update cursor
                setCursor(getDropCursor(dropAction));

                if (dropAction == null) {
                    event.rejectDrag();
                } else {
                    event.acceptDrag(getNativeDropAction(dropAction));
                }

                display.validate();
            }

            @Override
            public void dropActionChanged(final DropTargetDragEvent event) {
                userDropAction = getDropAction(event.getDropAction());

                DropAction dropAction = null;

                if (dropDescendant != null) {
                    java.awt.Point location = event.getLocation();
                    Point dropLocation = dropDescendant.mapPointFromAncestor(display, location.x,
                        location.y);
                    if (dropLocation == null) {
                        dropLocation = display.getMouseLocation();
                    }

                    if (dropLocation != null) {
                        DropTarget dropTarget = dropDescendant.getDropTarget();
                        dropAction = dropTarget.userDropActionChange(dropDescendant, dragManifest,
                            getSupportedDropActions(event.getSourceActions()),
                            dropLocation.x, dropLocation.y, userDropAction);
                    }
                }

                if (dropAction == null) {
                    event.rejectDrag();
                } else {
                    event.acceptDrag(getNativeDropAction(dropAction));
                }

                display.validate();
            }

            @Override
            public void drop(final DropTargetDropEvent event) {
                java.awt.Point location = event.getLocation();
                dropDescendant = getDropDescendant(location.x, location.y);

                DropAction dropAction = null;

                if (dropDescendant != null) {
                    Point dropLocation = dropDescendant.mapPointFromAncestor(display, location.x,
                        location.y);
                    if (dropLocation == null) {
                        dropLocation = display.getMouseLocation();
                    }

                    DropTarget dropTarget = dropDescendant.getDropTarget();

                    // Simulate a user drop action change to get the current
                    // drop action
                    int supportedDropActions = getSupportedDropActions(event.getSourceActions());

                    if (dropLocation != null) {
                        dropAction = dropTarget.userDropActionChange(dropDescendant, dragManifest,
                            supportedDropActions, dropLocation.x, dropLocation.y, userDropAction);

                        if (dropAction != null) {
                            // Perform the drop
                            event.acceptDrop(getNativeDropAction(dropAction));
                            dropTarget.drop(dropDescendant, dragManifest, supportedDropActions,
                                dropLocation.x, dropLocation.y, userDropAction);
                        }
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
                dragLocation = null;

                // Clear drop state
                dropDescendant = null;

                display.validate();
            }
        };

        private transient TextInputMethodListener textInputMethodListener = new ComponentTextInputMethodListener();

        @Override
        public InputMethodRequests getInputMethodRequests() {
            return textInputMethodListener;
        }

        public DisplayHost() {
            enableEvents(
                  AWTEvent.COMPONENT_EVENT_MASK
                | AWTEvent.FOCUS_EVENT_MASK
                | AWTEvent.MOUSE_EVENT_MASK
                | AWTEvent.MOUSE_MOTION_EVENT_MASK
                | AWTEvent.MOUSE_WHEEL_EVENT_MASK
                | AWTEvent.KEY_EVENT_MASK);
            enableInputMethods(true);
            addInputMethodListener(textInputMethodListener);

            try {
                System.setProperty("sun.awt.noerasebackground", "true");
                System.setProperty("sun.awt.erasebackgroundonresize", "false");

                if (Boolean.getBoolean("org.apache.pivot.wtk.disablevolatilebuffer")) {
                    volatileImagePaintEnabled = false;
                }

                debugPaint = Boolean.getBoolean("org.apache.pivot.wtk.debugpaint");
                if (debugPaint) {
                    random = new Random();
                }

                boolean debugFocus = Boolean.getBoolean("org.apache.pivot.wtk.debugfocus");

                if (debugFocus) {
                    final Decorator focusDecorator = new ShadeDecorator(0.2f, Color.RED);

                    ComponentClassListener focusChangeListener = new ComponentClassListener() {
                        @Override
                        public void focusedComponentChanged(final Component previousFocusedComponent) {
                            if (previousFocusedComponent != null
                                && previousFocusedComponent.getDecorators().indexOf(focusDecorator) > -1) {
                                previousFocusedComponent.getDecorators().remove(focusDecorator);
                            }

                            Component currentFocusedComponent = Component.getFocusedComponent();
                            if (currentFocusedComponent != null
                                && currentFocusedComponent.getDecorators().indexOf(focusDecorator) == -1) {
                                currentFocusedComponent.getDecorators().add(focusDecorator);
                            }

                            System.out.println("focusedComponentChanged():\n  from = "
                                + previousFocusedComponent + "\n  to = " + currentFocusedComponent);
                        }
                    };

                    Component.getComponentClassListeners().add(focusChangeListener);
                }
            } catch (SecurityException ex) {
                // No-op
            }

            // Add native drop support
            @SuppressWarnings("unused")
            java.awt.dnd.DropTarget dropTarget = new java.awt.dnd.DropTarget(this, dropTargetListener);

            setFocusTraversalKeysEnabled(false);
        }

        public Display getDisplay() {
            return display;
        }

        public AWTEvent getCurrentAWTEvent() {
            return currentAWTEvent;
        }

        /**
         * @return The current scale (or zoom) factor for the entire application's
         * display.
         * @see #setScale
         */
        public double getScale() {
            return scale;
        }

        private int scaleValue(final int value) {
            return Math.max((int) Math.ceil((double) value / scale), 0);
        }

        /**
         * Use this method to scale up or down (that is zoom in or out)
         * the entire application's display.
         * <p> For the main application window, use this (and related) methods to scale the display,
         * but for any contained windows a {@link org.apache.pivot.wtk.effects.ScaleDecorator} must
         * be used instead.
         *
         * @param newScale The new scale (zoom) factor for the entire display.
         * @see #scaleUp
         * @see #scaleDown
         * @see #getScale
         */
        public void setScale(final double newScale) {
            if (newScale != scale) {
                scale = newScale;
                display.setSize(scaleValue(getWidth()), scaleValue(getHeight()));
                display.repaint();
            }
        }

        /**
         * Use this method to zoom in to the application's main window
         * (that is, make all the text and components look visually bigger).
         * <p> The scale is increased in discrete steps for each call to
         * this method: 1, 1.25, 1.5, 2.0, then whole integer values
         * up to a maximum of 12.
         * @see #setScale
         * @see #scaleDown
         * @see #getScale
         */
        public void scaleUp() {
            double newScale;

            if (scale < 1) {
                newScale = 1;
            } else if (scale < 1.25) {
                newScale = 1.25;
            } else if (scale < 1.5) {
                newScale = 1.5;
            } else if (scale < 2) {
                newScale = 2;
            } else {
                newScale = Math.min(Math.floor(scale) + 1, 12);
            }

            setScale(newScale);
        }

        /**
         * Use this method to zoom out of the application's main window
         * (that is, to make all the text and components visually smaller).
         * <p> The scale is decreased in discrete steps for each call to
         * this method:  next whole integer down for values above 2.0, then
         * 2.0, 1.5, 1.25, then finally 1.
         * @see #setScale
         * @see #scaleUp
         * @see #getScale
         */
        public void scaleDown() {
            double newScale;

            if (scale <= 1.25) {
                newScale = 1;
            } else if (scale <= 1.5) {
                newScale = 1.25;
            } else if (scale <= 2) {
                newScale = 1.5;
            } else {
                newScale = Math.ceil(scale) - 1;
            }

            setScale(newScale);
        }

        /**
         * Under some conditions, e.g. running under Linux in an applet,
         * volatile buffering can reduce performance.
         * @param enabled Whether or not to use volatile image painting.
         */
        public void setVolatileImagePaintEnabled(final boolean enabled) {
            volatileImagePaintEnabled = enabled;
            if (enabled) {
                bufferedImage = null;
                bufferedImageGC = null;
            } else {
                volatileImage = null;
                volatileImageGC = null;
            }
        }

        public void setBufferedImagePaintEnabled(final boolean enabled) {
            bufferedImagePaintEnabled = enabled;
            if (!enabled) {
                bufferedImage = null;
                bufferedImageGC = null;
            }
        }

        @Override
        public void repaint(final int x, final int y, final int width, final int height) {
            int xValue = x;
            int yValue = y;
            int widthValue = width;
            int heightValue = height;

            // Ensure that the repaint call is properly bounded (some
            // implementations of AWT do not properly clip the repaint call
            // when x or y is negative: the negative value is converted to 0,
            // but the width/height is not adjusted)
            if (xValue < 0) {
                widthValue = Math.max(widthValue + xValue, 0);
                xValue = 0;
            }

            if (yValue < 0) {
                heightValue = Math.max(heightValue + yValue, 0);
                yValue = 0;
            }

            if (widthValue > 0 && heightValue > 0) {
                if (scale == 1) {
                    super.repaint(xValue, yValue, widthValue, heightValue);
                } else {
                    super.repaint((int) Math.floor(xValue * scale),
                        (int) Math.floor(yValue * scale),
                        (int) Math.ceil(widthValue * scale) + 1,
                        (int) Math.ceil(heightValue * scale) + 1);
                }
            }
        }

        @Override
        public void paint(final Graphics graphics) {
            // Intersect the clip region with the bounds of this component
            // (for some reason, AWT does not do this automatically)
            graphics.clipRect(0, 0, getWidth(), getHeight());

            if (graphics instanceof PrintGraphics || graphics instanceof PrinterGraphics) {
                print(graphics);
                return;
            }

            java.awt.Rectangle clipBounds = graphics.getClipBounds();
            if (clipBounds != null && !clipBounds.isEmpty()) {
                try {
                    boolean bPaintSuccess = false;
                    if (volatileImagePaintEnabled) {
                        bPaintSuccess = paintVolatileBuffered((Graphics2D) graphics);
                    }
                    if (!bPaintSuccess && bufferedImagePaintEnabled) {
                        bPaintSuccess = paintBuffered((Graphics2D) graphics);
                    }
                    if (!bPaintSuccess) {
                        paintDisplay((Graphics2D) graphics);
                    }

                    if (debugPaint) {
                        graphics.setColor(new java.awt.Color(random.nextInt(256),
                            random.nextInt(256), random.nextInt(256), 75));
                        graphics.fillRect(0, 0, getWidth(), getHeight());
                    }
                } catch (RuntimeException exception) {
                    System.err.println("Exception thrown during paint(): " + exception);
                    throw exception;
                }
            }
        }

        @Override
        public void update(final Graphics graphics) {
            paint(graphics);
        }

        @Override
        public void print(final Graphics graphics) {
            // TODO: verify if/how we have to re-scale output in this case ...

            // Intersect the clip region with the bounds of this component
            // (for some reason, AWT does not do this automatically)
            graphics.clipRect(0, 0, getWidth(), getHeight());

            java.awt.Rectangle clipBounds = graphics.getClipBounds();
            if (clipBounds != null && !clipBounds.isEmpty()) {
                try {
                    // When printing, there is no point in using offscreen buffers.
                    paintDisplay((Graphics2D) graphics);
                } catch (RuntimeException exception) {
                    System.err.println("Exception thrown during print(): " + exception);
                    throw exception;
                }
            }
        }

        /**
         * Attempts to paint the display using an offscreen buffer.
         *
         * @param graphics The source graphics context.
         * @return {@code true} if the display was painted using the offscreen
         * buffer; {@code false}, otherwise.
         */
        private boolean paintBuffered(final Graphics2D graphics) {
            boolean painted = false;

            // Paint the display into an offscreen buffer
            GraphicsConfiguration gc = graphics.getDeviceConfiguration();
            java.awt.Rectangle clipBounds = graphics.getClipBounds();

            if (bufferedImage == null || bufferedImageGC != gc
                || bufferedImage.getWidth() < clipBounds.width
                || bufferedImage.getHeight() < clipBounds.height) {

                bufferedImage = gc.createCompatibleImage(clipBounds.width, clipBounds.height,
                    Transparency.OPAQUE);
                bufferedImageGC = gc;
            }

            if (bufferedImage != null) {
                Graphics2D bufferedImageGraphics = (Graphics2D) bufferedImage.getGraphics();
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
         * @param graphics The source graphics context.
         * @return {@code true} if the display was painted using the offscreen
         * buffer; {@code false}, otherwise.
         */
        private boolean paintVolatileBuffered(final Graphics2D graphics) {
            boolean painted = false;

            // Paint the display into a volatile offscreen buffer
            GraphicsConfiguration gc = graphics.getDeviceConfiguration();
            java.awt.Rectangle gcBounds = gc.getBounds();
            if (volatileImage == null || volatileImageGC != gc) {
                if (volatileImage != null) {
                    volatileImage.flush();
                }
                volatileImage = gc.createCompatibleVolatileImage(gcBounds.width, gcBounds.height,
                    Transparency.OPAQUE);
                // we need to create a new volatile if the GC changes
                volatileImageGC = gc;
            }

            // If we have a valid volatile image, attempt to paint the
            // display to it
            int valid = volatileImage.validate(gc);

            if (valid == java.awt.image.VolatileImage.IMAGE_OK
             || valid == java.awt.image.VolatileImage.IMAGE_RESTORED) {
                java.awt.Rectangle clipBounds = graphics.getClipBounds();
                Graphics2D volatileImageGraphics = volatileImage.createGraphics();
                volatileImageGraphics.setClip(clipBounds.x, clipBounds.y, clipBounds.width,
                    clipBounds.height);

                try {
                    paintDisplay(volatileImageGraphics);
                    // this drawImage method doesn't use width and height
                    int x2 = clipBounds.x + clipBounds.width;
                    int y2 = clipBounds.y + clipBounds.height;
                    graphics.drawImage(volatileImage, clipBounds.x, clipBounds.y, x2, y2,
                        clipBounds.x, clipBounds.y, x2, y2, this);
                } finally {
                    volatileImageGraphics.dispose();
                }

                painted = !volatileImage.contentsLost();
            } else {
                volatileImage.flush();
                volatileImage = null;
            }

            return painted;
        }

        /**
         * Paints the display including any decorators.
         *
         * @param graphics The graphics to paint into.
         */
        private void paintDisplay(final Graphics2D graphics) {
            if (scale != 1) {
                graphics.scale(scale, scale);
            }

            Graphics2D decoratedGraphics = graphics;

            DecoratorSequence decorators = display.getDecorators();
            int n = decorators.getLength();
            for (int i = n - 1; i >= 0; i--) {
                Decorator decorator = decorators.get(i);
                decoratedGraphics = decorator.prepare(display, decoratedGraphics);
            }

            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
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

        private Component getDropDescendant(final int x, final int y) {
            Component descendant = display.getDescendantAt(x, y);

            while (descendant != null && descendant.getDropTarget() == null) {
                descendant = descendant.getParent();
            }

            if (descendant != null && descendant.isBlocked()) {
                descendant = null;
            }

            return descendant;
        }

        private void startNativeDrag(final DragSource dragSource,
            final Component dragDescendantArgument, final MouseEvent mouseEvent) {
            java.awt.dnd.DragSource awtDragSource = java.awt.dnd.DragSource.getDefaultDragSource();

            final int supportedDropActions = dragSource.getSupportedDropActions();

            DragGestureRecognizer dragGestureRecognizer = new DragGestureRecognizer(
                java.awt.dnd.DragSource.getDefaultDragSource(), DisplayHost.this) {
                private static final long serialVersionUID = -3204487375572082596L;

                {
                    appendEvent(mouseEvent);
                }

                @Override
                public synchronized int getSourceActions() {
                    return DropAction.getSourceActions(supportedDropActions);
                }

                @Override
                protected void registerListeners() {
                    // No-op
                }

                @Override
                protected void unregisterListeners() {
                    // No-op
                }
            };

            java.util.List<InputEvent> inputEvents = new java.util.ArrayList<>();
            inputEvents.add(mouseEvent);

            // TODO If current user drop action is supported by drag source, use it
            // as initial action - otherwise, select MOVE, COPY, LINK in that order
            java.awt.Point location = new java.awt.Point(mouseEvent.getX(), mouseEvent.getY());
            DragGestureEvent trigger = new DragGestureEvent(dragGestureRecognizer,
                DnDConstants.ACTION_MOVE, location, inputEvents);

            LocalManifest dragContent = dragSource.getContent();
            LocalManifestAdapter localManifestAdapter = new LocalManifestAdapter(dragContent);

            awtDragSource.startDrag(trigger, java.awt.Cursor.getDefaultCursor(), null, null,
                localManifestAdapter, new DragSourceListener() {
                    @Override
                    public void dragEnter(final DragSourceDragEvent event) {
                        DragSourceContext context = event.getDragSourceContext();
                        context.setCursor(getDropCursor(getDropAction(event.getDropAction())));
                    }

                    @Override
                    public void dragExit(final DragSourceEvent event) {
                        DragSourceContext context = event.getDragSourceContext();
                        context.setCursor(java.awt.Cursor.getDefaultCursor());
                    }

                    @Override
                    public void dragOver(final DragSourceDragEvent event) {
                        DragSourceContext context = event.getDragSourceContext();
                        context.setCursor(getDropCursor(getDropAction(event.getDropAction())));
                    }

                    @Override
                    public void dropActionChanged(final DragSourceDragEvent event) {
                        DragSourceContext context = event.getDragSourceContext();
                        context.setCursor(getDropCursor(getDropAction(event.getDropAction())));
                    }

                    @Override
                    public void dragDropEnd(final DragSourceDropEvent event) {
                        DragSourceContext context = event.getDragSourceContext();
                        context.setCursor(java.awt.Cursor.getDefaultCursor());
                        dragSource.endDrag(dragDescendantArgument,
                            getDropAction(event.getDropAction()));
                    }
                });
        }

        @Override
        protected void processEvent(final AWTEvent event) {
            currentAWTEvent = event;
            try {
                super.processEvent(event);
            } finally {
                currentAWTEvent = null;
            }

            display.validate();
        }

        @Override
        protected void processComponentEvent(final ComponentEvent event) {
            super.processComponentEvent(event);

            switch (event.getID()) {
                case ComponentEvent.COMPONENT_RESIZED:
                    if (scale == 1) {
                        display.setSize(Math.max(getWidth(), 0), Math.max(getHeight(), 0));
                    } else {
                        display.setSize(Math.max((int) Math.ceil(getWidth() / scale), 0),
                            Math.max((int) Math.ceil(getHeight() / scale), 0));
                    }
                    break;

                default:
                    break;
            }
        }

        @Override
        protected void processFocusEvent(final FocusEvent event) {
            super.processFocusEvent(event);

            switch (event.getID()) {
                case FocusEvent.FOCUS_GAINED:
                    if (focusedComponent != null && focusedComponent.isShowing()
                        && !focusedComponent.isBlocked()) {
                        focusedComponent.requestFocus();
                    }
                    break;

                case FocusEvent.FOCUS_LOST:
                    focusedComponent = Component.getFocusedComponent();
                    Component.clearFocus();
                    break;

                default:
                    break;
            }
        }

        @Override
        protected void processInputMethodEvent(final InputMethodEvent event) {
            super.processInputMethodEvent(event);
        }

        @Override
        protected void processMouseEvent(final MouseEvent event) {
            super.processMouseEvent(event);

            int x = (int) Math.round(event.getX() / scale);
            int y = (int) Math.round(event.getY() / scale);

            // Set the mouse button state
            int mouseButtons = Mouse.Button.getButtons(event.getModifiersEx());
            Mouse.setButtons(mouseButtons);

            // Get the button associated with this event
            Mouse.Button button = Mouse.Button.getButton(event.getButton());

            // Process the event
            int eventID = event.getID();
            if (eventID == MouseEvent.MOUSE_ENTERED || eventID == MouseEvent.MOUSE_EXITED) {
                try {
                    switch (eventID) {
                        case MouseEvent.MOUSE_ENTERED:
                            display.mouseOver();
                            break;

                        case MouseEvent.MOUSE_EXITED:
                            display.mouseOut();
                            break;

                        default:
                            break;
                    }
                } catch (Throwable exception) {
                    handleUncaughtException(exception);
                }
            } else {
                // Determine the mouse owner
                Component mouseOwner;
                Component mouseCapturer = Mouse.getCapturer();
                if (mouseCapturer == null) {
                    mouseOwner = display;
                } else {
                    mouseOwner = mouseCapturer;
                    Point location = mouseOwner.mapPointFromAncestor(display, x, y);
                    if (location == null) {
                        location = display.getMouseLocation();
                    }
                    if (location != null) {
                        x = location.x;
                        y = location.y;
                    }
                }

                // Delegate the event to the owner
                try {
                    boolean consumed;

                    switch (eventID) {
                        case MouseEvent.MOUSE_PRESSED:
                            requestFocus();
                            requestFocusInWindow();

                            consumed = mouseOwner.mouseDown(button, x, y);

                            if (button == Mouse.Button.LEFT) {
                                dragLocation = new Point(x, y);
                            } else if (menuPopup == null && button == Mouse.Button.RIGHT && !consumed) {
                                // Instantiate a context menu
                                Menu menu = new Menu();

                                // Allow menu handlers to configure the menu
                                Component component = mouseOwner;
                                int componentX = x;
                                int componentY = y;

                                do {
                                    MenuHandler menuHandler = component.getMenuHandler();
                                    if (menuHandler != null) {
                                        if (menuHandler.configureContextMenu(component, menu,
                                            componentX, componentY)) {
                                            // Stop propagation
                                            break;
                                        }
                                    }

                                    if (component instanceof Container) {
                                        Container container = (Container) component;
                                        component = container.getComponentAt(componentX, componentY);

                                        if (component != null) {
                                            componentX -= component.getX();
                                            componentY -= component.getY();
                                        }
                                    } else {
                                        component = null;
                                    }
                                } while (component != null && component.isEnabled());

                                // Show the context menu if it contains any sections
                                if (menu.getSections().getLength() > 0) {
                                    menuPopup = new MenuPopup(menu);

                                    menuPopup.getWindowStateListeners().add(
                                        new WindowStateListener() {
                                            @Override
                                            public void windowClosed(final Window window,
                                                final Display displayArgument, final Window owner) {
                                                menuPopup.getMenu().getSections().clear();
                                                menuPopup = null;
                                                window.getWindowStateListeners().remove(this);
                                            }
                                        });

                                    Window window = null;
                                    if (mouseOwner == display) {
                                        window = (Window) display.getComponentAt(x, y);
                                    } else {
                                        window = mouseOwner.getWindow();
                                    }

                                    Display windowDisplay = window.getDisplay();
                                    Point location = mouseOwner.mapPointToAncestor(windowDisplay, x, y);
                                    menuPopup.open(window, location);
                                }
                            }

                            if (consumed) {
                                event.consume();
                            }

                            break;

                        case MouseEvent.MOUSE_RELEASED:
                            if (dragDescendant == null) {
                                consumed = mouseOwner.mouseUp(button, x, y);

                                if (consumed) {
                                    event.consume();
                                }
                            } else {
                                DragSource dragSource = dragDescendant.getDragSource();

                                repaintDragRepresentation();

                                if (dropDescendant == null) {
                                    dragSource.endDrag(dragDescendant, null);
                                } else {
                                    DropTarget dropTarget = dropDescendant.getDropTarget();
                                    DropAction dropAction = dropTarget.drop(dropDescendant,
                                        dragManifest, dragSource.getSupportedDropActions(), x, y,
                                        getUserDropAction(event));
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

                        default:
                            break;
                    }
                } catch (Throwable exception) {
                    handleUncaughtException(exception);
                }
            }
        }

        @Override
        protected void processMouseMotionEvent(final MouseEvent event) {
            super.processMouseMotionEvent(event);

            int x = (int) Math.round(event.getX() / scale);
            int y = (int) Math.round(event.getY() / scale);

            // Process the event
            try {
                switch (event.getID()) {
                    case MouseEvent.MOUSE_MOVED:
                    case MouseEvent.MOUSE_DRAGGED:
                        if (dragDescendant == null) {
                            // A drag is not active
                            Component mouseCapturer = Mouse.getCapturer();

                            if (mouseCapturer == null) {
                                // The mouse is not captured, so propagate the
                                // event to the display
                                if (!display.isMouseOver()) {
                                    display.mouseOver();
                                }

                                display.mouseMove(x, y);

                                int dragThreshold = Platform.getDragThreshold();

                                if (dragLocation != null
                                    && (Math.abs(x - dragLocation.x) > dragThreshold
                                    ||  Math.abs(y - dragLocation.y) > dragThreshold)) {
                                    // The user has dragged the mouse past the drag threshold; try
                                    // to find a drag source
                                    dragDescendant = display.getDescendantAt(dragLocation.x,
                                        dragLocation.y);

                                    while (dragDescendant != null
                                        && dragDescendant.getDragSource() == null) {
                                        dragDescendant = dragDescendant.getParent();
                                    }

                                    if (dragDescendant == null || dragDescendant.isBlocked()) {
                                        // There was nothing to drag, so clear the drag location
                                        dragDescendant = null;
                                        dragLocation = null;
                                    } else {
                                        DragSource dragSource = dragDescendant.getDragSource();
                                        dragLocation = dragDescendant.mapPointFromAncestor(display, x, y);
                                        if (dragLocation == null) {
                                            dragLocation = display.getMouseLocation();
                                        }

                                        if (dragLocation != null) {
                                            if (dragSource.beginDrag(dragDescendant,
                                                dragLocation.x, dragLocation.y)) {
                                                // A drag has started
                                                if (dragSource.isNative()) {
                                                    startNativeDrag(dragSource, dragDescendant, event);

                                                    // Clear the drag state since it is not used for
                                                    // native drags
                                                    dragDescendant = null;
                                                    dragLocation = null;
                                                } else {
                                                    if (dragSource.getRepresentation() != null
                                                        && dragSource.getOffset() == null) {
                                                        throw new IllegalStateException(
                                                            "Drag offset is required when a "
                                                                + " representation is specified.");
                                                    }

                                                    if (display.isMouseOver()) {
                                                        display.mouseOut();
                                                    }

                                                    // Get the drag content
                                                    dragManifest = dragSource.getContent();

                                                    // Get the initial user drop action
                                                    userDropAction = getUserDropAction(event);

                                                    // Repaint the drag visual
                                                    dragLocation = new Point(x, y);
                                                    repaintDragRepresentation();
                                                }
                                            } else {
                                                // Clear the drag state
                                                dragDescendant = null;
                                                dragLocation = null;
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Delegate the event to the capturer
                                Point location = mouseCapturer.mapPointFromAncestor(display, x, y);
                                if (location == null) {
                                    location = display.getMouseLocation();
                                }

                                if (location != null) {
                                    boolean consumed = mouseCapturer.mouseMove(location.x, location.y);

                                    if (consumed) {
                                        event.consume();
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

                                        Point dropLocation = dropDescendant.mapPointFromAncestor(
                                            display, x, y);
                                        if (dropLocation == null) {
                                            dropLocation = display.getMouseLocation();
                                        }
                                        if (dropLocation != null) {
                                            dropAction = dropTarget.dragMove(dropDescendant, dragManifest,
                                                dragSource.getSupportedDropActions(),
                                                dropLocation.x, dropLocation.y, userDropAction);
                                        }
                                    }
                                } else {
                                    if (previousDropDescendant != null) {
                                        DropTarget previousDropTarget = previousDropDescendant.getDropTarget();
                                        previousDropTarget.dragExit(previousDropDescendant);
                                    }

                                    if (dropDescendant != null) {
                                        DropTarget dropTarget = dropDescendant.getDropTarget();
                                        dropAction = dropTarget.dragEnter(dropDescendant,
                                            dragManifest, dragSource.getSupportedDropActions(),
                                            userDropAction);
                                    }
                                }

                                // Update cursor
                                setCursor(getDropCursor(dropAction));

                                // Repaint the drag visual
                                repaintDragRepresentation();

                                dragLocation = new Point(x, y);
                                repaintDragRepresentation();
                            }
                        }

                        break;

                    default:
                        break;
                }
            } catch (Throwable exception) {
                handleUncaughtException(exception);
            }
        }

        @Override
        protected void processMouseWheelEvent(final MouseWheelEvent event) {
            super.processMouseWheelEvent(event);

            // Get the event coordinates
            int x = (int) Math.round(event.getX() / scale);
            int y = (int) Math.round(event.getY() / scale);

            // Get the scroll type
            Mouse.ScrollType scrollType = null;
            switch (event.getScrollType()) {
                case MouseWheelEvent.WHEEL_BLOCK_SCROLL:
                    scrollType = Mouse.ScrollType.BLOCK;
                    break;

                case MouseWheelEvent.WHEEL_UNIT_SCROLL:
                    scrollType = Mouse.ScrollType.UNIT;
                    break;

                default:
                    break;
            }

            // Process the event
            try {
                switch (event.getID()) {
                    case MouseEvent.MOUSE_WHEEL:
                        if (Keyboard.isPressed(Keyboard.Modifier.CTRL)
                            && Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                            // Mouse wheel scaling
                            if (event.getWheelRotation() < 0) {
                                scaleUp();
                            } else {
                                scaleDown();
                            }
                        } else if (dragDescendant == null) {
                            // Determine the mouse owner
                            Component mouseOwner;
                            Component mouseCapturer = Mouse.getCapturer();
                            if (mouseCapturer == null) {
                                mouseOwner = display;
                            } else {
                                mouseOwner = mouseCapturer;
                                Point location = mouseOwner.mapPointFromAncestor(display, x, y);
                                if (location == null) {
                                    location = display.getMouseLocation();
                                }
                                if (location != null) {
                                    x = location.x;
                                    y = location.y;
                                }
                            }

                            // Delegate the event to the owner
                            boolean consumed = mouseOwner.mouseWheel(scrollType,
                                event.getScrollAmount(), event.getWheelRotation(), x, y);

                            if (consumed) {
                                event.consume();
                            }
                        }
                        break;

                    default:
                        break;
                }
            } catch (Throwable exception) {
                handleUncaughtException(exception);
            }
        }

        @Override
        protected void processKeyEvent(final KeyEvent event) {
            super.processKeyEvent(event);

            int modifiersEx = event.getModifiersEx();
            int awtKeyLocation = event.getKeyLocation();

            // Set the keyboard modifier state
            int keyboardModifiers = Keyboard.Modifier.getModifiers(modifiersEx, awtKeyLocation);
            Keyboard.setModifiers(keyboardModifiers);

            // Get the key location
            Keyboard.KeyLocation keyLocation = Keyboard.KeyLocation.fromAWTLocation(awtKeyLocation);

            if (dragDescendant == null) {
                // Process the event
                Component currentFocusedComponent = Component.getFocusedComponent();

                boolean consumed = false;
                int keyCode;

                switch (event.getID()) {
                    case KeyEvent.KEY_PRESSED:
                        keyCode = event.getKeyCode();

                        if (Keyboard.isPressed(Keyboard.Modifier.CTRL)
                            && Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                            if (keyCode == Keyboard.KeyCode.PLUS
                                || keyCode == Keyboard.KeyCode.EQUALS
                                || keyCode == Keyboard.KeyCode.ADD) {
                                scaleUp();
                            } else if (keyCode == Keyboard.KeyCode.MINUS
                                || keyCode == Keyboard.KeyCode.SUBTRACT) {
                                scaleDown();
                            }
                        }

                        try {
                            if (currentFocusedComponent == null) {
                                for (Application application : applications) {
                                    if (application instanceof Application.UnprocessedKeyHandler) {
                                        Application.UnprocessedKeyHandler unprocessedKeyHandler =
                                                (Application.UnprocessedKeyHandler) application;
                                        unprocessedKeyHandler.keyPressed(keyCode, keyLocation);
                                    }
                                }
                            } else {
                                if (!currentFocusedComponent.isBlocked()) {
                                    consumed = currentFocusedComponent.keyPressed(keyCode,
                                        keyLocation);
                                }
                            }
                        } catch (Throwable exception) {
                            handleUncaughtException(exception);
                        }

                        if (consumed) {
                            event.consume();
                        }
                        break;

                    case KeyEvent.KEY_RELEASED:
                        keyCode = event.getKeyCode();

                        try {
                            if (currentFocusedComponent == null) {
                                for (Application application : applications) {
                                    if (application instanceof Application.UnprocessedKeyHandler) {
                                        Application.UnprocessedKeyHandler unprocessedKeyHandler =
                                                (Application.UnprocessedKeyHandler) application;
                                        unprocessedKeyHandler.keyReleased(keyCode, keyLocation);
                                    }
                                }
                            } else {
                                if (!currentFocusedComponent.isBlocked()) {
                                    consumed = currentFocusedComponent.keyReleased(keyCode,
                                        keyLocation);
                                }
                            }
                        } catch (Throwable exception) {
                            handleUncaughtException(exception);
                        }

                        if (consumed) {
                            event.consume();
                        }
                        break;

                    case KeyEvent.KEY_TYPED:
                        char keyChar = event.getKeyChar();

                        try {
                            if (currentFocusedComponent == null) {
                                for (Application application : applications) {
                                    if (application instanceof Application.UnprocessedKeyHandler) {
                                        Application.UnprocessedKeyHandler unprocessedKeyHandler =
                                            (Application.UnprocessedKeyHandler) application;
                                        unprocessedKeyHandler.keyTyped(keyChar);
                                    }
                                }
                            } else {
                                if (!currentFocusedComponent.isBlocked()) {
                                    consumed = currentFocusedComponent.keyTyped(keyChar);
                                }
                            }
                        } catch (Throwable exception) {
                            handleUncaughtException(exception);
                        }

                        if (consumed) {
                            event.consume();
                        }
                        break;

                    default:
                        break;
                }
            } else {
                DragSource dragSource = dragDescendant.getDragSource();

                // If the user drop action changed, notify the drop descendant
                if (dropDescendant != null) {
                    DropAction previousUserDropAction = userDropAction;
                    userDropAction = getUserDropAction(event);

                    if (previousUserDropAction != userDropAction) {
                        DropTarget dropTarget = dropDescendant.getDropTarget();

                        Point dropLocation = dragLocation;
                        if (dropLocation != null) {
                            dropLocation = dropDescendant.mapPointFromAncestor(display,
                                dropLocation.x, dropLocation.y);
                        }
                        if (dropLocation == null) {
                            dropLocation = display.getMouseLocation();
                        }
                        if (dropLocation != null) {
                            dropTarget.userDropActionChange(dropDescendant, dragManifest,
                                dragSource.getSupportedDropActions(),
                                dropLocation.x, dropLocation.y, userDropAction);
                        }
                    }
                }
            }
        }
    }

    /**
     * Resource cache dictionary implementation.
     * <p> Note that this implementation does not have a way to limit the number of elements
     * it contains, so the cache continues to grow. To keep it small you have to
     * manually remove old elements from it when they are no longer necessary.
     */
    public static final class ResourceCacheDictionary implements Dictionary<URL, Object>,
        Iterable<URL> {
        private ResourceCacheDictionary() {
        }

        @Override
        public synchronized Object get(final URL key) {
            try {
                return resourceCache.get(key.toURI());
            } catch (URISyntaxException exception) {
                throw new RuntimeException(exception);
            }
        }

        @Override
        public synchronized Object put(final URL key, final Object value) {
            try {
                return resourceCache.put(key.toURI(), value);
            } catch (URISyntaxException exception) {
                throw new RuntimeException(exception);
            }
        }

        @Override
        public synchronized Object remove(final URL key) {
            try {
                return resourceCache.remove(key.toURI());
            } catch (URISyntaxException exception) {
                throw new RuntimeException(exception);
            }
        }

        @Override
        public synchronized boolean containsKey(final URL key) {
            try {
                return resourceCache.containsKey(key.toURI());
            } catch (URISyntaxException exception) {
                throw new RuntimeException(exception);
            }
        }

        @Override
        public Iterator<URL> iterator() {
            return new Iterator<URL>() {
                private Iterator<URI> iterator = resourceCache.iterator();

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public URL next() {
                    try {
                        return iterator.next().toURL();
                    } catch (MalformedURLException exception) {
                        throw new RuntimeException(exception);
                    }
                }

                @Override
                @UnsupportedOperation
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public synchronized int getCount() {
            return resourceCache.getCount();
        }
    }

    /**
     * Class representing a scheduled callback.
     */
    public static final class ScheduledCallback extends TimerTask {
        private Runnable runnable;
        private QueuedCallback queuedCallback = null;

        private ScheduledCallback(final Runnable callback) {
            runnable = callback;
        }

        @Override
        public void run() {
            if (queuedCallback != null) {
                queuedCallback.cancel();
            }

            queuedCallback = queueCallback(runnable);
        }

        @Override
        public boolean cancel() {
            if (queuedCallback != null) {
                queuedCallback.cancel();
            }

            return super.cancel();
        }
    }

    /**
     * Class representing a queued callback.
     */
    public static final class QueuedCallback implements Runnable {
        private Runnable runnable;
        private volatile boolean executed = false;
        private volatile boolean cancelled = false;

        private QueuedCallback(final Runnable callback) {
            runnable = callback;
        }

        @Override
        public void run() {
            if (!cancelled) {
                try {
                    runnable.run();
                } catch (Throwable exception) {
                    handleUncaughtException(exception);
                }

                for (Display display : displays) {
                    display.validate();
                }

                executed = true;
            }
        }

        public boolean cancel() {
            cancelled = true;
            return (!executed);
        }
    }


    protected static URL origin = null;
    protected static ArrayList<Display> displays = new ArrayList<>();
    protected static ArrayList<Application> applications = new ArrayList<>();

    private static Timer timer = null;

    private static HashMap<URI, Object> resourceCache = new HashMap<>();
    private static ResourceCacheDictionary resourceCacheDictionary = new ResourceCacheDictionary();

    private static final Package CURRENT_PACKAGE;
    private static final Version JVM_VERSION;
    private static final Version JAVA_VERSION;
    private static final Version PIVOT_VERSION;

    static {
        CURRENT_PACKAGE = ApplicationContext.class.getPackage();
        JVM_VERSION = Version.safelyDecode(System.getProperty("java.vm.version"));
        JAVA_VERSION = Version.safelyDecode(System.getProperty("java.runtime.version"));
        PIVOT_VERSION = Version.safelyDecode(CURRENT_PACKAGE.getImplementationVersion());
    }

    /**
     * Returns this application's origin (the URL of it's originating server).
     *
     * @return The application's origin, or {@code null} if the origin cannot
     * be determined.
     */
    public static URL getOrigin() {
        return origin;
    }

    /**
     * @return The dictionary of cached resources.
     */
    public static ResourceCacheDictionary getResourceCache() {
        return resourceCacheDictionary;
    }

    /**
     * Adds the styles from a named stylesheet to the named or typed style
     * collections.
     * <p> Does not allow macros (standard behavior) which can also be 25x
     * faster than allowing macros.
     *
     * @param resourceName The resource name of the stylesheet to apply.
     */
    public static void applyStylesheet(final String resourceName) {
        applyStylesheet(resourceName, false);
    }

    /**
     * Adds the styles from a named stylesheet to the named or typed style
     * collections.
     *
     * @param resourceName The resource name of the stylesheet to apply.
     * @param allowMacros Whether or not there will be macros in the stylesheet.
     */
    @SuppressWarnings("unchecked")
    public static void applyStylesheet(final String resourceName, final boolean allowMacros) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        URL stylesheetLocation = classLoader.getResource(resourceName.substring(1));
        if (stylesheetLocation == null) {
            throw new RuntimeException("Unable to locate style sheet resource \"" + resourceName + "\".");
        }

        try (InputStream inputStream = stylesheetLocation.openStream()) {
            JSONSerializer serializer = new JSONSerializer();
            serializer.setAllowMacros(allowMacros);
            Map<String, ?> stylesheet = (Map<String, ?>) serializer.readObject(inputStream);

            for (String name : stylesheet) {
                Map<String, ?> styles = (Map<String, ?>) stylesheet.get(name);

                int i = name.lastIndexOf('.') + 1;
                if (Character.isUpperCase(name.charAt(i))) {
                    // Assume the current package if none specified
                    if (!name.contains(".")) {
                        name = CURRENT_PACKAGE.getName() + "." + name;
                    }

                    Class<?> type = null;
                    try {
                        type = Class.forName(name);
                    } catch (ClassNotFoundException exception) {
                        // No-op
                    }

                    if (type != null && Component.class.isAssignableFrom(type)) {
                        Component.getTypedStyles().put((Class<? extends Component>) type, styles);
                    }
                } else {
                    Component.getNamedStyles().put(name, styles);
                }
            }
        } catch (IOException | SerializationException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Returns the current JVM version, parsed from the "java.vm.version" system
     * property.
     *
     * @return The current JVM version, or an "empty" version if it can't be
     * determined (that is, "0.0.0_00").
     */
    public static Version getJVMVersion() {
        return JVM_VERSION;
    }

    /**
     * Returns the current Java Runtime version, parsed from the "java.runtime.version"
     * system property.
     *
     * @return The current Java version, or an "empty" version if it can't be
     * determined (that is, "0.0.0_00").
     */
    public static Version getJavaVersion() {
        return JAVA_VERSION;
    }

    /**
     * Returns the current Pivot version.
     *
     * @return The current Pivot version (determined at build time), or
     * an "empty" version if it can't be determined (that is, "0.0.0_00").
     */
    public static Version getPivotVersion() {
        return PIVOT_VERSION;
    }

    /**
     * Helper method to schedule a task for one-time or recurring execution.
     * The task will be executed on the UI thread.
     *
     * @param callback The task to execute.
     * @param delay The length of time to wait before executing the task (in milliseconds).
     * @param period The interval at which the task will be repeated (also in milliseconds)
     * (0 = non-recurring).
     * @return The callback object.
     */
    private static ScheduledCallback timerSchedule(final Runnable callback, final long delay,
        final long period) {
        ScheduledCallback scheduledCallback = new ScheduledCallback(callback);

        // TODO This is a workaround for a potential OS X bug; revisit
        try {
            try {
                if (period == 0L) {
                    timer.schedule(scheduledCallback, delay);
                } else {
                    timer.schedule(scheduledCallback, delay, period);
                }
            } catch (IllegalStateException exception) {
                createTimer();
                if (period == 0L) {
                    timer.schedule(scheduledCallback, delay);
                } else {
                    timer.schedule(scheduledCallback, delay, period);
                }
            }
        } catch (Throwable throwable) {
            System.err.println("Unable to schedule callback: " + throwable);
        }

        return scheduledCallback;
    }

    /**
     * Schedules a task for one-time execution. The task will be executed on the UI thread.
     *
     * @param callback The task to execute.
     * @param delay The length of time to wait before executing the task (in milliseconds).
     * @return The callback object.
     */
    public static ScheduledCallback scheduleCallback(final Runnable callback, final long delay) {
        return timerSchedule(callback, delay, 0L);
    }

    /**
     * Schedules a task for repeated execution. The task will be executed on the
     * UI thread and will begin executing immediately.
     *
     * @param callback The task to execute.
     * @param period The interval at which the task will be repeated (in milliseconds).
     * @return The callback object.
     */
    public static ScheduledCallback scheduleRecurringCallback(final Runnable callback, final long period) {
        return timerSchedule(callback, 0L, period);
    }

    /**
     * Schedules a task for repeated execution. The task will be executed on the UI thread.
     *
     * @param callback The task to execute.
     * @param delay The length of time to wait before the first execution of the
     * task (milliseconds) (can be 0).
     * @param period The interval at which the task will be repeated (also in milliseconds).
     * @return The callback object.
     */
    public static ScheduledCallback scheduleRecurringCallback(final Runnable callback, final long delay,
        final long period) {
        return timerSchedule(callback, delay, period);
    }

    /**
     * Runs a task and then schedules it for repeated execution.
     * The task will be executed on the UI thread and will begin executing immediately.
     *
     * @param callback The task to execute.
     * @param period The interval at which the task will be repeated (in milliseconds).
     * @return The callback object.
     */
    public static ScheduledCallback runAndScheduleRecurringCallback(final Runnable callback, final long period) {
        return runAndScheduleRecurringCallback(callback, 0, period);
    }

    /**
     * Runs a task once and then schedules it for repeated execution. The task will be executed on the
     * UI thread.  This is a common pattern for caret blink, scrolling, etc. to have an immediate
     * effect, with recurring execution after that.
     *
     * @param callback The task to execute.
     * @param delay The length of time to wait before the next execution of the task (milliseconds).
     * @param period The interval at which the task will be repeated (also in milliseconds).
     * @return The callback object.
     */
    public static ScheduledCallback runAndScheduleRecurringCallback(final Runnable callback, final long delay,
        final long period) {

        ScheduledCallback scheduledCallback = timerSchedule(callback, delay, period);

        // Before returning, run the task once to start things off
        callback.run();

        return scheduledCallback;
    }

    /**
     * Queues a task to execute after all pending events have been processed and
     * returns without waiting for the task to complete.
     *
     * @param callback The task to execute.
     * @return The callback object (used to manipulate or wait for the task).
     */
    public static QueuedCallback queueCallback(final Runnable callback) {
        return queueCallback(callback, false);
    }

    /**
     * Queues a task to execute after all pending events have been processed and
     * optionally waits for the task to complete.
     *
     * @param callback The task to execute.
     * @param wait If {@code true}, does not return until the task has
     * executed. Otherwise, returns immediately.
     * @return The callback object (used to manipulate or wait for the task).
     */
    public static QueuedCallback queueCallback(final Runnable callback, final boolean wait) {
        QueuedCallback queuedCallback = new QueuedCallback(callback);

        // TODO This is a workaround for a potential OS X bug; revisit
        try {
            if (wait) {
                try {
                    java.awt.EventQueue.invokeAndWait(queuedCallback);
                } catch (InvocationTargetException exception) {
                    throw new RuntimeException(exception.getCause());
                } catch (InterruptedException exception) {
                    throw new RuntimeException(exception);
                }
            } else {
                java.awt.EventQueue.invokeLater(queuedCallback);
            }
        } catch (Throwable throwable) {
            System.err.println("Unable to queue callback: " + throwable);
        }

        return queuedCallback;
    }

    protected static void createTimer() {
        timer = new Timer();
    }

    protected static void destroyTimer() {
        timer.cancel();
        timer = null;
    }

    public static List<Display> getDisplays() {
        return displays;
    }

    protected static void invalidateDisplays() {
        for (Display display : displays) {
            display.invalidate();
        }
    }

    private static DropAction getUserDropAction(final InputEvent event) {
        return DropAction.getDropAction(event);
    }

    private static DropAction getDropAction(final int nativeDropAction) {
        return DropAction.getDropAction(nativeDropAction);
    }

    private static int getSupportedDropActions(final int sourceActions) {
        return DropAction.getSupportedDropActions(sourceActions);
    }

    private static int getNativeDropAction(final DropAction dropAction) {
        return (dropAction == null ? 0 : dropAction.getNativeDropAction());
    }

    private static java.awt.Cursor getDropCursor(final DropAction dropAction) {
        return (dropAction == null ? java.awt.Cursor.getDefaultCursor() : dropAction.getNativeCursor());
    }

    public static void defaultUncaughtExceptionHandler(final Thread thread, final Throwable exception) {
        exception.printStackTrace();

        Display display = (displays.getLength() > 0) ? displays.get(0) : null;
        if (display == null) {
            return;
        }

        String message = String.format("%1$s on Thread %2$s",
                exception.getClass().getSimpleName(), thread.getName());

        TextArea body = null;
        String bodyText = ExceptionUtils.toString(exception);
        if (bodyText != null && bodyText.length() > 0) {
            body = new TextArea();
            body.setPreferredWidth(400);
            body.putStyle(Style.wrapText, true);
            body.setText(bodyText);
            body.setEditable(false);
        }

        Alert alert = new Alert(MessageType.ERROR, message, null, body, false);
        alert.open(display);
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable exception) {
        handleUncaughtException(thread, exception);
    }

    public static void handleUncaughtException(final Throwable exception) {
        handleUncaughtException(Thread.currentThread(), exception);
    }

    public static void handleUncaughtException(final Thread thread, final Throwable exception) {
        boolean handled = false;

        for (Application application : applications) {
            if (application instanceof Application.UncaughtExceptionHandler) {
                Application.UncaughtExceptionHandler uncaughtExceptionHandler =
                        (Application.UncaughtExceptionHandler) application;
                uncaughtExceptionHandler.uncaughtException(thread, exception);
                handled = true;
            }
        }

        if (!handled) {
            defaultUncaughtExceptionHandler(thread, exception);
        }
    }

}
