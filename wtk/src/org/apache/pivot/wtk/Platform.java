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

import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import org.apache.pivot.wtk.Keyboard.Modifier;

/**
 * Provides platform-specific information.
 */
public final class Platform {
    private static FontRenderContext fontRenderContext;

    private static final String OS_NAME;

    private static final boolean OS_IS_WINDOWS;
    private static final boolean OS_IS_OSX;
    private static final boolean OS_IS_LINUX;

    private static final int DEFAULT_MULTI_CLICK_INTERVAL = 400;
    private static final int DEFAULT_CURSOR_BLINK_RATE = 600;

    private static final Modifier COMMAND_MODIFIER;
    private static final Modifier WORD_NAVIGATION_MODIFIER;
    private static final String KEYSTROKE_MODIFIER_SEPARATOR;

    static {
        OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

        OS_IS_WINDOWS = OS_NAME.startsWith("windows");
        OS_IS_OSX     = OS_NAME.startsWith("mac os x");
        OS_IS_LINUX   = OS_NAME.startsWith("linux");

        if (OS_IS_OSX) {
            COMMAND_MODIFIER = Modifier.META;
            WORD_NAVIGATION_MODIFIER = Modifier.ALT;
            KEYSTROKE_MODIFIER_SEPARATOR = "";
        } else if (OS_IS_WINDOWS) {
            COMMAND_MODIFIER = Modifier.CTRL;
            WORD_NAVIGATION_MODIFIER = Modifier.CTRL;
            KEYSTROKE_MODIFIER_SEPARATOR = "+";
        } else {
            COMMAND_MODIFIER = Modifier.CTRL;
            WORD_NAVIGATION_MODIFIER = Modifier.CTRL;
            KEYSTROKE_MODIFIER_SEPARATOR = "-";
        }

        // Initialize the font render context
        initializeFontRenderContext();

        // Listen for changes to the font desktop hints property
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.addPropertyChangeListener("awt.font.desktophints", new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent event) {
                initializeFontRenderContext();
                ApplicationContext.invalidateDisplays();
            }
        });
    }

    /** Private constructor because this is a utility class. */
    private Platform() {
    }

    /**
     * @return true if this is a Windows platform we're running on.
     */
    public static boolean isWindows() {
        return OS_IS_WINDOWS;
    }

    /**
     * @return true if this is a Mac OS X platform we're running on.
     */
    public static boolean isOSX() {
        return OS_IS_OSX;
    }

    /**
     * @return true if this is a Linux platform we're running on.
     */
    public static boolean isLinux() {
        return OS_IS_LINUX;
    }

    /**
     * @return The platform's font rendering context.
     */
    public static FontRenderContext getFontRenderContext() {
        return fontRenderContext;
    }

    /**
     * Do the one-time initialization of the font rendering context from the hints.
     */
    private static void initializeFontRenderContext() {
        Object aaHint = null;
        Object fmHint = null;

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        java.util.Map<?, ?> fontDesktopHints =
            (java.util.Map<?, ?>) toolkit.getDesktopProperty("awt.font.desktophints");
        if (fontDesktopHints != null) {
            aaHint = fontDesktopHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
            fmHint = fontDesktopHints.get(RenderingHints.KEY_FRACTIONALMETRICS);
        }

        if (aaHint == null) {
            aaHint = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
        }

        if (fmHint == null) {
            fmHint = RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT;
        }

        fontRenderContext = new FontRenderContext(null, aaHint, fmHint);
    }

    /**
     * @return The system multi-click interval.
     */
    public static int getMultiClickInterval() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Integer multiClickInterval = (Integer) toolkit.getDesktopProperty("awt.multiClickInterval");

        if (multiClickInterval == null) {
            multiClickInterval = Integer.valueOf(DEFAULT_MULTI_CLICK_INTERVAL);
        }

        return multiClickInterval.intValue();
    }

    /**
     * @return The system cursor blink rate.
     */
    public static int getCursorBlinkRate() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Integer cursorBlinkRate = (Integer) toolkit.getDesktopProperty("awt.cursorBlinkRate");

        if (cursorBlinkRate == null) {
            cursorBlinkRate = Integer.valueOf(DEFAULT_CURSOR_BLINK_RATE);
        }

        return cursorBlinkRate.intValue();
    }

    /**
     * @return The system drag threshold.
     */
    public static int getDragThreshold() {
        return java.awt.dnd.DragSource.getDragThreshold();
    }

    /**
     * @return The system command modifier key.
     */
    public static Modifier getCommandModifier() {
        return COMMAND_MODIFIER;
    }

    /**
     * @return The word navigation modifier key.
     */
    public static Modifier getWordNavigationModifier() {
        return WORD_NAVIGATION_MODIFIER;
    }

    /**
     * @return The keystroke modifier separator text.
     */
    public static String getKeyStrokeModifierSeparator() {
        return KEYSTROKE_MODIFIER_SEPARATOR;
    }
}
