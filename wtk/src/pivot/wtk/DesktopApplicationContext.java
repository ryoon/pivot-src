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
import java.awt.Graphics;
import java.awt.event.WindowEvent;

import pivot.collections.HashMap;
import pivot.collections.immutable.ImmutableMap;
import pivot.wtk.media.Image;
import pivot.wtk.media.Picture;

/**
 * Application context used to execute applications in a native frame
 * window.
 *
 * @author gbrown
 */
@SuppressWarnings("deprecation")
public final class DesktopApplicationContext extends ApplicationContext {
    private static class HostFrame extends java.awt.Frame {
        private static final long serialVersionUID = 0;

        private HostFrame() {
            enableEvents(AWTEvent.WINDOW_EVENT_MASK
                | AWTEvent.WINDOW_STATE_EVENT_MASK);

            // Add the display host
            add(applicationContext.getDisplayHost());

            // Disable focus traversal keys
            setFocusTraversalKeysEnabled(false);

            // Clear the background
            setBackground(null);
        }

        @Override
        public void update(Graphics graphics) {
            paint(graphics);
        }

        @Override
        public void processWindowEvent(WindowEvent event) {
            super.processWindowEvent(event);

            switch(event.getID()) {
                case WindowEvent.WINDOW_OPENED: {
                    applicationContext.getDisplayHost().requestFocus();

                    createTimer();

                    try {
                        application.startup(applicationContext.getDisplay(),
                            new ImmutableMap<String, String>(properties));
                    } catch(Exception exception) {
                        exception.printStackTrace();
                        String message = exception.getMessage();
                        if (message == null) {
                            message = exception.getClass().getName();
                        }

                        Alert.alert(MessageType.ERROR, message, applicationContext.getDisplay());
                    }

                    break;
                }

                case WindowEvent.WINDOW_CLOSING: {
                    boolean shutdown = true;

                    try {
                        shutdown = application.shutdown(true);
                    } catch(Exception exception) {
                        exception.printStackTrace();
                        Alert.alert(MessageType.ERROR, exception.getMessage(),
                            applicationContext.getDisplay());
                    }

                    if (shutdown) {
                        destroyTimer();

                        java.awt.Window window = event.getWindow();
                        window.setVisible(false);
                        window.dispose();
                    }

                    break;
                }

                case WindowEvent.WINDOW_CLOSED: {
                    exit();
                    break;
                }
            }
        }

        @Override
        protected void processWindowStateEvent(WindowEvent event) {
            super.processWindowStateEvent(event);

            switch(event.getID()) {
                case WindowEvent.WINDOW_ICONIFIED: {
                    try {
                        application.suspend();
                    } catch(Exception exception) {
                        exception.printStackTrace();
                        Alert.alert(MessageType.ERROR, exception.getMessage(),
                            applicationContext.getDisplay());
                    }

                    break;
                }

                case WindowEvent.WINDOW_DEICONIFIED: {
                    try {
                        application.resume();
                    } catch(Exception exception) {
                        exception.printStackTrace();
                        Alert.alert(MessageType.ERROR, exception.getMessage(),
                            applicationContext.getDisplay());
                    }

                    break;
                }
            }
        }
    }

    private static DesktopApplicationContext applicationContext = null;
    private static HashMap<String, String> properties = null;
    private static Application application = null;

    private static int x = 0;
    private static int y = 0;
    private static int width = 800;
    private static int height = 600;
    private static boolean center = false;
    private static boolean resizable = true;

    private static final String DEFAULT_HOST_FRAME_TITLE = "Pivot"; // TODO i18n

    private static final String X_ARGUMENT = "x";
    private static final String Y_ARGUMENT = "y";
    private static final String WIDTH_ARGUMENT = "width";
    private static final String HEIGHT_ARGUMENT = "height";
    private static final String CENTER_ARGUMENT = "center";
    private static final String RESIZABLE_ARGUMENT = "resizable";

    /**
     * Terminates the application context.
     */
    public static void exit() {
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        // Get the application class name and startup properties
        String applicationClassName = null;
        properties = new HashMap<String, String>();

        for (int i = 0, n = args.length; i < n; i++) {
            String arg = args[i];

            if (i == 0) {
                applicationClassName = arg;
            } else {
                String[] property = arg.split(":");

                if (property.length == 2) {
                    String key = property[0];
                    String value = property[1];

                    if (key.equals(X_ARGUMENT)) {
                        x = Integer.parseInt(value);
                    } else if (key.equals(Y_ARGUMENT)) {
                        y = Integer.parseInt(value);
                    } else if (key.equals(WIDTH_ARGUMENT)) {
                        width = Integer.parseInt(value);
                    } else if (key.equals(HEIGHT_ARGUMENT)) {
                        height = Integer.parseInt(value);
                    } else if (key.equals(CENTER_ARGUMENT)) {
                        center = Boolean.parseBoolean(value);
                    } else if (key.equals(RESIZABLE_ARGUMENT)) {
                        resizable = Boolean.parseBoolean(value);
                    } else {
                        properties.put(key, value);
                    }
                } else {
                    System.err.println(arg + " is not a valid startup property.");
                }
            }
        }

        // Create the application context
        applicationContext = new DesktopApplicationContext();

        // Load the application
        if (applicationClassName == null) {
            System.err.println("Application class name is required.");
        } else {
            Class<?> applicationClass = Class.forName(applicationClassName);
            application = (Application)applicationClass.newInstance();
        }

        // Create the host frame
        final HostFrame hostFrame = new HostFrame();
        hostFrame.setTitle(DEFAULT_HOST_FRAME_TITLE);

        hostFrame.setSize(width, height);
        hostFrame.setResizable(resizable);

        if (center) {
            java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            hostFrame.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
        } else {
            hostFrame.setLocation(x, y);
        }

        // Open the window and focus the display host
        hostFrame.setVisible(true);

        Window.getWindowClassListeners().add(new WindowClassListener() {
            public void activeWindowChanged(Window previousActiveWindow) {
                ApplicationContext.queueCallback(new Runnable() {
                    public void run() {
                        Window activeWindow = Window.getActiveWindow();

                        if (activeWindow == null) {
                            hostFrame.setTitle(DEFAULT_HOST_FRAME_TITLE);
                        } else {
                            Window rootOwner = activeWindow.getRootOwner();
                            hostFrame.setTitle(rootOwner.getTitle());

                            Image rootIcon = rootOwner.getIcon();
                            if (rootIcon instanceof Picture) {
                                Picture rootPicture = (Picture)rootIcon;
                                hostFrame.setIconImage(rootPicture.getBufferedImage());
                            }
                        }
                    }
                });
            }
        });
    }
}
