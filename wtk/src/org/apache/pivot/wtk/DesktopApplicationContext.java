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
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.immutable.ImmutableMap;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.media.Picture;

/**
 * Application context used to execute applications in a native frame
 * window.
 */
public final class DesktopApplicationContext extends ApplicationContext {
    /**
     * Display listener interface.
     */
    public interface DisplayListener {
        /**
         * Called when the host window for secondary display has been opened.
         *
         * @param display
         */
        public void hostWindowOpened(Display display);

        /**
         * Called when the host window for secondary display has been closed.
         *
         * @param display
         */
        public void hostWindowClosed(Display display);
    }

    // Custom display host that sets the title of the host frame to match the
    // title of the root Pivot owner window
    private static class DesktopDisplayHost extends DisplayHost {
        private static final long serialVersionUID = 0;

        private Window rootOwner = null;
        private Runnable updateHostWindowTitleBarCallback = null;

        private WindowListener rootOwnerListener = new WindowListener.Adapter() {
            @Override
            public void titleChanged(Window window, String previousTitle) {
                updateFrameTitleBar();
            }

            @Override
            public void iconChanged(Window window, Image previousIcon) {
                updateFrameTitleBar();
            }
        };

        public DesktopDisplayHost() {
            Display display = getDisplay();
            display.getContainerListeners().add(new ContainerListener.Adapter() {
                @Override
                public void componentInserted(Container container, int index) {
                    if (index == container.getLength() - 1) {
                        updateFrameTitleBar();
                    }
                }

                @Override
                public void componentsRemoved(Container container, int index, Sequence<Component> removed) {
                    if (index == container.getLength()) {
                        updateFrameTitleBar();
                    }
                }

                @Override
                public void componentMoved(Container container, int from, int to) {
                    int n = container.getLength();

                    if (from == n - 1
                        || to == n - 1) {
                        updateFrameTitleBar();
                    }
                }
            });
        }

        private void updateFrameTitleBar() {
            Display display = getDisplay();
            int n = display.getLength();

            Window rootOwner;
            if (n == 0) {
                rootOwner = null;
            } else {
                Window topWindow = (Window)display.get(display.getLength() - 1);
                rootOwner = topWindow.getRootOwner();
            }

            Window previousRootOwner = this.rootOwner;
            if (rootOwner != previousRootOwner) {
                if (previousRootOwner != null) {
                    previousRootOwner.getWindowListeners().remove(rootOwnerListener);
                }

                if (rootOwner != null) {
                    rootOwner.getWindowListeners().add(rootOwnerListener);
                }

                this.rootOwner = rootOwner;
            }

            if (updateHostWindowTitleBarCallback == null) {
                updateHostWindowTitleBarCallback = new Runnable() {
                    @Override
                    public void run() {
                        java.awt.Window hostWindow = getDisplay().getHostWindow();

                        if (DesktopDisplayHost.this.rootOwner == null) {
                            ((TitledWindow)hostWindow).setTitle(DEFAULT_HOST_WINDOW_TITLE);
                            hostWindow.setIconImage(null);
                        } else {
                            ((TitledWindow)hostWindow).setTitle(DesktopDisplayHost.this.rootOwner.getTitle());

                            Image icon = DesktopDisplayHost.this.rootOwner.getIcon();
                            if (icon instanceof Picture) {
                                Picture rootPicture = (Picture)icon;
                                hostWindow.setIconImage(rootPicture.getBufferedImage());
                            }
                        }

                        updateHostWindowTitleBarCallback = null;
                    }
                };

                queueCallback(updateHostWindowTitleBarCallback);
            }
        }
    }

    // The AWT Window class does not define a title property; this interface allows
    // the HostFrame and HostDialog titles to be handled polymorphicaly
    private interface TitledWindow {
        public String getTitle();
        public void setTitle(String title);
    }

    // Native host frame
    private static class HostFrame extends java.awt.Frame implements TitledWindow {
        private static final long serialVersionUID = 5340356674429280196L;

        public HostFrame() {
            enableEvents(AWTEvent.WINDOW_EVENT_MASK
                | AWTEvent.WINDOW_STATE_EVENT_MASK);

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
        public String getTitle() {
            return super.getTitle();
        }

        @Override
        public void setTitle(String title) {
            super.setTitle(title);
        }

        @Override
        public void processWindowEvent(WindowEvent event) {
            super.processWindowEvent(event);

            if (this == windowedHostFrame) {
                switch(event.getID()) {
                    case WindowEvent.WINDOW_OPENED: {
                        // Load the application
                        try {
                            Class<?> applicationClass = Class.forName(applicationClassName);
                            application = (Application)applicationClass.newInstance();
                        } catch(Exception exception) {
                            Alert.alert(MessageType.ERROR, exception.getMessage(),
                                primaryDisplayHost.getDisplay());
                            exception.printStackTrace();
                        }

                        // Set focus to the display host
                        primaryDisplayHost.requestFocus();

                        // Start the application
                        if (application != null) {
                            try {
                                application.startup(primaryDisplayHost.getDisplay(),
                                    new ImmutableMap<String, String>(properties));
                            } catch(Exception exception) {
                                displayException(exception);
                            }

                            // Add the application to the application list
                            applications.add(application);

                            // Hook into OS X application menu
                            String osName = System.getProperty("os.name");
                            if (osName.toLowerCase().startsWith("mac os x")) {
                                try {
                                    // Get the EAWT classes and methods
                                    Class<?> eawtApplicationClass = Class.forName("com.apple.eawt.Application");
                                    Class<?> eawtApplicationListenerClass = Class.forName("com.apple.eawt.ApplicationListener");
                                    Class<?> eawtApplicationEventClass = Class.forName("com.apple.eawt.ApplicationEvent");

                                    Method setEnabledAboutMenuMethod = eawtApplicationClass.getMethod("setEnabledAboutMenu",
                                        new Class<?>[] {Boolean.TYPE});

                                    Method addApplicationListenerMethod = eawtApplicationClass.getMethod("addApplicationListener",
                                        new Class<?>[] {eawtApplicationListenerClass});

                                    final Method setHandledMethod = eawtApplicationEventClass.getMethod("setHandled",
                                        new Class<?>[] {Boolean.TYPE});

                                    // Create the proxy handler
                                    InvocationHandler handler = new InvocationHandler() {
                                    @Override
                                        public Object invoke(Object proxy, Method method, Object[] args)
                                            throws Throwable {
                                            boolean handled = true;

                                            String methodName = method.getName();
                                            if (methodName.equals("handleAbout"))  {
                                                Application.AboutHandler aboutHandler = (Application.AboutHandler)application;
                                                aboutHandler.aboutRequested();
                                            } else if (methodName.equals("handleQuit")) {
                                                handled = !exit();
                                            }

                                            // Invoke setHandled()
                                            setHandledMethod.invoke(args[0], new Object[] {handled});

                                            return null;
                                        }
                                    };

                                    Object eawtApplication = eawtApplicationClass.newInstance();

                                    setEnabledAboutMenuMethod.invoke(eawtApplication,
                                        application instanceof Application.AboutHandler);

                                    Object eawtApplicationListener =
                                        Proxy.newProxyInstance(DesktopApplicationContext.class.getClassLoader(),
                                            new Class<?>[]{eawtApplicationListenerClass}, handler);

                                    // Invoke the addApplicationListener() method with the proxy listener
                                    addApplicationListenerMethod.invoke(eawtApplication, new Object[] {eawtApplicationListener});
                                } catch (Throwable throwable) {
                                    System.err.println("Unable to attach EAWT hooks: " + throwable);
                                }
                            }
                        }

                        break;
                    }

                    case WindowEvent.WINDOW_CLOSING: {
                        exit();
                        break;
                    }

                    case WindowEvent.WINDOW_CLOSED: {
                        System.exit(0);
                        break;
                    }
                }
            }
        }

        @Override
        protected void processWindowStateEvent(WindowEvent event) {
            super.processWindowStateEvent(event);

            if (this == windowedHostFrame) {
                switch(event.getID()) {
                    case WindowEvent.WINDOW_ICONIFIED: {
                        try {
                            application.suspend();
                        } catch(Exception exception) {
                            displayException(exception);
                        }

                        break;
                    }

                    case WindowEvent.WINDOW_DEICONIFIED: {
                        try {
                            application.resume();
                        } catch(Exception exception) {
                            displayException(exception);
                        }

                        break;
                    }
                }
            }
        }
    }

    // Native host dialog for secondary displays
    private static class HostDialog extends java.awt.Dialog implements TitledWindow {
        private static final long serialVersionUID = 5340356674429280196L;

        private DisplayHost displayHost = new DesktopDisplayHost();

        private DisplayListener displayCloseListener;

        public HostDialog(java.awt.Window owner, boolean modal, DisplayListener displayCloseListener) {
            super(owner, modal
                ? java.awt.Dialog.ModalityType.APPLICATION_MODAL : java.awt.Dialog.ModalityType.MODELESS);

            this.displayCloseListener = displayCloseListener;

            enableEvents(AWTEvent.WINDOW_EVENT_MASK);

            // Disable focus traversal keys
            setFocusTraversalKeysEnabled(false);

            // Clear the background
            setBackground(null);

            // Add the display host
            add(displayHost);
        }

        @Override
        public void update(Graphics graphics) {
            paint(graphics);
        }

        public Display getDisplay() {
            return displayHost.getDisplay();
        }

        @Override
        public String getTitle() {
            return super.getTitle();
        }

        @Override
        public void setTitle(String title) {
            super.setTitle(title);
        }

        @Override
        public void processWindowEvent(WindowEvent event) {
            super.processWindowEvent(event);

            switch(event.getID()) {
                case WindowEvent.WINDOW_OPENED: {
                    Display display = displayHost.getDisplay();
                    displays.add(display);

                    if (displayCloseListener != null) {
                        displayCloseListener.hostWindowOpened(display);
                    }

                    displayHost.requestFocus();

                    break;
                }

                case WindowEvent.WINDOW_CLOSING: {
                    dispose();
                    break;
                }

                case WindowEvent.WINDOW_CLOSED: {
                    Display display = displayHost.getDisplay();
                    displays.remove(display);

                    if (displayCloseListener != null) {
                        displayCloseListener.hostWindowClosed(display);
                    }

                    break;
                }
            }
        }
    }

    private static String applicationClassName = null;
    private static HashMap<String, String> properties = null;

    private static Application application = null;

    private static DisplayHost primaryDisplayHost = null;
    private static HostFrame windowedHostFrame = null;
    private static HostFrame fullScreenHostFrame = null;

    public static final String DEFAULT_HOST_WINDOW_TITLE = "Apache Pivot";

    public static final String X_ARGUMENT = "x";
    public static final String Y_ARGUMENT = "y";
    public static final String WIDTH_ARGUMENT = "width";
    public static final String HEIGHT_ARGUMENT = "height";
    public static final String CENTER_ARGUMENT = "center";
    public static final String RESIZABLE_ARGUMENT = "resizable";
    public static final String MAXIMIZED_ARGUMENT = "maximized";
    public static final String UNDECORATED_ARGUMENT = "undecorated";
    public static final String FULL_SCREEN_ARGUMENT = "fullScreen";
    public static final String STYLESHEET_ARGUMENT = "stylesheet";

    public static boolean isActive() {
        return (application != null);
    }

    /**
     * Terminates the application context.
     */
    public static boolean exit() {
        boolean cancelShutdown = false;

        if (application != null) {
            try {
                cancelShutdown = application.shutdown(true);
            } catch(Exception exception) {
                displayException(exception);
            }

            if (!cancelShutdown) {
                // Remove the application from the application list
                applications.remove(application);
            }
        }

        if (!cancelShutdown) {
            try {
                Preferences preferences = Preferences.userNodeForPackage(DesktopApplicationContext.class);
                preferences = preferences.node(applicationClassName);

                boolean maximized = (windowedHostFrame.getExtendedState()
                    & java.awt.Frame.MAXIMIZED_BOTH) == java.awt.Frame.MAXIMIZED_BOTH;
                if (!maximized) {
                    preferences.putInt(X_ARGUMENT, windowedHostFrame.getX());
                    preferences.putInt(Y_ARGUMENT, windowedHostFrame.getY());
                    preferences.putInt(WIDTH_ARGUMENT, windowedHostFrame.getWidth());
                    preferences.putInt(HEIGHT_ARGUMENT, windowedHostFrame.getHeight());
                }

                preferences.putBoolean(MAXIMIZED_ARGUMENT, maximized);

                preferences.flush();
            } catch (SecurityException exception) {
                // No-op
            } catch (BackingStoreException exception) {
                // No-op
            }

            windowedHostFrame.dispose();
            fullScreenHostFrame.dispose();
        }

        return cancelShutdown;
    }

    /**
     * Primary aplication entry point.
     *
     * @param args
     */
    public static void main(String[] args) {
        // Get the application class name
        if (args.length == 0) {
            System.err.println("Application class name is required.");
            return;
        }

        applicationClassName = args[0];

        // Get the startup properties
        properties = new HashMap<String, String>();

        int x = 0;
        int y = 0;
        int width = 800;
        int height = 600;
        boolean center = false;
        boolean resizable = true;
        boolean maximized = false;
        boolean undecorated = false;
        boolean fullScreen = false;
        String stylesheet = null;

        try {
            Preferences preferences = Preferences.userNodeForPackage(DesktopApplicationContext.class);
            preferences = preferences.node(applicationClassName);

            x = preferences.getInt(X_ARGUMENT, x);
            y = preferences.getInt(Y_ARGUMENT, y);
            width = preferences.getInt(WIDTH_ARGUMENT, width);
            height = preferences.getInt(HEIGHT_ARGUMENT, height);
            maximized = preferences.getBoolean(MAXIMIZED_ARGUMENT, maximized);

            // Update positioning if window is offscreen
            GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            if (screenDevices.length == 1) {
                if (x < 0) {
                    x = 0;
                }

                if (y < 0) {
                    y = 0;
                }
            }
        } catch (SecurityException exception) {
            System.err.println("Unable to retrieve startup preferences: " + exception);
        }

        final String STARTUP_PROPERTY_WARNING = "\"%s\" is not a valid startup property (expected"
            + " format is \"--name=value\").";

        for (int i = 1, n = args.length; i < n; i++) {
            String arg = args[i];

            if (arg.startsWith("--")) {
                arg = arg.substring(2);
                String[] property = arg.split("=");

                if (property.length == 2) {
                    String key = property[0];
                    String value = property[1];

                    try {
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
                        } else if (key.equals(MAXIMIZED_ARGUMENT)) {
                            maximized = Boolean.parseBoolean(value);
                        } else if (key.equals(UNDECORATED_ARGUMENT)) {
                            undecorated = Boolean.parseBoolean(value);
                        } else if (key.equals(FULL_SCREEN_ARGUMENT)) {
                            fullScreen = Boolean.parseBoolean(value);
                        } else if (key.equals(STYLESHEET_ARGUMENT)) {
                            stylesheet = value;
                        } else {
                            properties.put(key, value);
                        }
                    } catch (NumberFormatException exception) {
                        System.err.println("\"" + value + "\" is not a valid value for startup"
                            + " property \"" + key + "\".");
                    }
                } else {
                    System.err.println(String.format(STARTUP_PROPERTY_WARNING, arg));
                }
            } else {
                System.err.println(String.format(STARTUP_PROPERTY_WARNING, arg));
            }
        }

        // Set the origin
        try {
            // Load the JNLP classes dynamically because they are only available
            // when run via javaws
            Class<?> serviceManagerClass = Class.forName("javax.jnlp.ServiceManager");
            Method lookupMethod = serviceManagerClass.getMethod("lookup", String.class);
            Object basicService = lookupMethod.invoke(null, "javax.jnlp.BasicService");

            Class<?> basicServiceClass = Class.forName("javax.jnlp.BasicService");
            Method getCodeBaseMethod = basicServiceClass.getMethod("getCodeBase");
            URL codeBase = (URL)getCodeBaseMethod.invoke(basicService);

            if (codeBase != null) {
                origin = new URL(codeBase.getProtocol(), codeBase.getHost(), codeBase.getPort(), "");
            }
        } catch (Exception exception) {
            // No-op
        }

        if (origin == null) {
            // Could not obtain origin from JNLP; use user's home directory
            File userHome = new File(System.getProperty("user.home"));

            try {
                origin = userHome.toURI().toURL();
            } catch(MalformedURLException exception) {
                // No-op
            }
        }

        // Start the timer
        createTimer();

        // Apply stylesheet
        if (stylesheet != null) {
            applyStylesheet(stylesheet.substring(1));
        }

        // Create the display host
        primaryDisplayHost = new DesktopDisplayHost();
        displays.add(primaryDisplayHost.getDisplay());

        // Create the windowed host frame
        windowedHostFrame = new HostFrame();
        windowedHostFrame.add(primaryDisplayHost);
        windowedHostFrame.setUndecorated(undecorated);

        windowedHostFrame.setTitle(DEFAULT_HOST_WINDOW_TITLE);
        windowedHostFrame.setSize(width, height);
        windowedHostFrame.setResizable(resizable);

        if (center) {
            java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            windowedHostFrame.setLocation((screenSize.width - width) / 2,
                (screenSize.height - height) / 2);
        } else {
            windowedHostFrame.setLocation(x, y);
        }

        if (maximized) {
            windowedHostFrame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        }

        // Add a key listener to the display host to toggle between full-screen
        // and windowed mode
        primaryDisplayHost.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_F
                    && (keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0
                    && (keyEvent.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0) {
                    setFullScreen(!isFullScreen());
                }
            }
        });

        // Create the full-screen host frame
        fullScreenHostFrame = new HostFrame();
        fullScreenHostFrame.setUndecorated(true);

        // Open the windowed host
        windowedHostFrame.setVisible(true);

        // Go to full-screen mode, if requested
        setFullScreen(fullScreen);
    }

    private static void displayException(Exception exception) {
        exception.printStackTrace();

        String message = exception.getClass().getName();

        Label body = null;
        String bodyText = exception.getMessage();
        if (bodyText != null
            && bodyText.length() > 0) {
            body = new Label(bodyText);
            body.getStyles().put("wrapText", true);
        }

        Alert.alert(MessageType.ERROR, message, body, primaryDisplayHost.getDisplay());
    }

    /**
     * Returns the full-screen mode flag.
     */
    public static boolean isFullScreen() {
        return (!windowedHostFrame.isVisible());
    }

    /**
     * Sets the full-screen mode flag.
     *
     * @param fullScreen
     */
    public static void setFullScreen(boolean fullScreen) {
        if (fullScreen != isFullScreen()) {
            GraphicsDevice graphicsDevice =
                windowedHostFrame.getGraphicsConfiguration().getDevice();

            if (fullScreen) {
                // Go to full screen mode
                windowedHostFrame.remove(primaryDisplayHost);
                windowedHostFrame.setVisible(false);

                fullScreenHostFrame.add(primaryDisplayHost);
                fullScreenHostFrame.setTitle(windowedHostFrame.getTitle());
                fullScreenHostFrame.setVisible(true);

                graphicsDevice.setFullScreenWindow(fullScreenHostFrame);
            } else {
                // Go to windowed mode
                try {
                    graphicsDevice.setFullScreenWindow(null);
                } catch (Exception exception) {
                    // TODO remove this catch. On Win32 platforms, the
                    // preceding call can throw.
                }

                fullScreenHostFrame.remove(primaryDisplayHost);
                fullScreenHostFrame.setVisible(false);

                windowedHostFrame.add(primaryDisplayHost);
                windowedHostFrame.setTitle(fullScreenHostFrame.getTitle());
                windowedHostFrame.setVisible(true);
            }

            primaryDisplayHost.requestFocus();
        }
    }

    /**
     * Sizes the window's native host frame to match its preferred size.
     *
     * @param window
     */
    public static void sizeHostToFit(Window window) {
        if (window == null) {
            throw new IllegalArgumentException();
        }

        if (isFullScreen()) {
            throw new IllegalStateException();
        }

        Dimensions size = window.getPreferredSize();
        java.awt.Window hostWindow = window.getDisplay().getHostWindow();
        java.awt.Insets frameInsets = hostWindow.getInsets();
        hostWindow.setSize(size.width + (frameInsets.left + frameInsets.right),
            size.height + (frameInsets.top + frameInsets.bottom));
    }

    /**
     * Creates a new secondary display.
     *
     * @param width
     * @param height
     * @param x
     * @param y
     * @param modal
     * @param owner
     */
    public static Display createDisplay(int width, int height, int x, int y, boolean modal,
        boolean resizable, boolean undecorated, java.awt.Window owner,
        DisplayListener displayCloseListener) {
        if (owner == null) {
            throw new IllegalArgumentException();
        }

        if (isFullScreen()) {
            throw new IllegalStateException();
        }

        final HostDialog hostDialog = new HostDialog(owner, modal, displayCloseListener);
        hostDialog.setLocation(x, y);
        hostDialog.setSize(width, height);
        hostDialog.setResizable(resizable);
        hostDialog.setUndecorated(undecorated);

        // Open the window in a callback; otherwise, if it is modal, it will block the
        // calling thread
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                hostDialog.setVisible(true);
            }
        });

        return hostDialog.getDisplay();
    }

    /**
     * Utility method to make it easier to define <tt>main()</tt> entry-points
     * into applications. For example:
     *
     * <code>
     * public class MyApp implements Application {
     *   public static void main(String[] args) throws Exception {
     *     DesktopApplicationContext.main(MyApp.class, args);
     *   }
     * }
     * </code>
     *
     * @param applicationClass
     * @param applicationArgs
     */
    public static final void main(Class<? extends Application> applicationClass,
        String[] applicationArgs) {
        String[] args = new String[applicationArgs.length + 1];
        System.arraycopy(applicationArgs, 0, args, 1, applicationArgs.length);
        args[0] = applicationClass.getName();
        main(args);
    }
}