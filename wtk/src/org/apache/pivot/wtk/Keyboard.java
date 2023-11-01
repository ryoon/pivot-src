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

import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.InputEvent.META_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.KeyEvent.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pivot.collections.EnumSet;
import org.apache.pivot.collections.Set;
import org.apache.pivot.util.Utils;

/**
 * Class representing the system keyboard.
 */
public final class Keyboard {
    /**
     * Private constructor for utility class.
     */
    private Keyboard() {
    }

    /**
     * Enumeration representing keyboard modifiers.
     */
    public enum Modifier {
        SHIFT(SHIFT_DOWN_MASK),
        CTRL(CTRL_DOWN_MASK),
        ALT(ALT_DOWN_MASK),
        META(META_DOWN_MASK);

        /**
         * The AWT modifier value.
         */
        private final int awtModifier;

        /**
         * The standard modifier text appropriate for the platform.
         */
        private final String keySymbol;

        /**
         * Define the enum value, along with the AWT modifier value.
         *
         * @param modifier The AWT modifier for this enum.
         */
        Modifier(final int modifier) {
            awtModifier = modifier;
            keySymbol = getModifiersExText(modifier);
        }

        /**
         * @return The one-bit mask for this modifier, which is
         * {@code 2**ordinal}.
         */
        public int getMask() {
            return 1 << ordinal();
        }

        /**
         * Determine the complete mask for all the given modifiers.
         * @param modifiers The set of modifiers to test.
         * @return The complete mask corresponding to the set.
         */
        public static int getMask(final Set<Modifier> modifiers) {
           int mask = 0;
           for (Modifier mod : modifiers) {
               mask |= mod.getMask();
           }
           return mask;
        }

        /**
         * Determine the complete mask for all the given modifiers.
         * @param modifiers The list of modifiers to test.
         * @return The complete mask corresponding to the list.
         */
        public static int getMask(final Modifier... modifiers) {
           int mask = 0;
           for (Modifier mod : modifiers) {
               mask |= mod.getMask();
           }
           return mask;
        }

        /**
         * Determine the complete set of AWT modifiers for a set of mask values.
         *
         * @param mask The complete mask set of our modifiers.
         * @return     The complete mask for AWT use.
         */
        public static int getAWTMask(final int mask) {
            int awtMask = 0;
            for (Modifier m : values()) {
                if ((mask & m.getMask()) > 0) {
                    awtMask |= m.awtModifier;
                }
            }
            return awtMask;
        }

        /**
         * Determine the complete set of our modifier masks that are represented by the
         * given AWT modifiers set.
         *
         * @param awtModifiers The set of modifiers returned by {@link KeyEvent#getModifiersEx}.
         * @param keyLocation  Where the key event originated (used to modify/ignore some modifiers).
         * @return             The complete mask of the our modifiers represented by the input.
         */
        public static int getModifiers(final int awtModifiers, final int keyLocation) {
            int modifiers = 0;

            for (Modifier m : values()) {
                if (m == CTRL) {
                    // Ignore CTRL when Alt-Graphics is pressed
                    if ((awtModifiers & m.awtModifier) > 0
                     && ((awtModifiers & ALT.awtModifier) == 0
                      || keyLocation == KEY_LOCATION_RIGHT)) {
                         modifiers |= m.getMask();
                     }
                 } else {
                     if ((awtModifiers & m.awtModifier) > 0) {
                         modifiers |= m.getMask();
                     }
                 }
            }

            return modifiers;
        }

        /**
         * The set of all possible keyboard modifiers (for use with {@link #isPressed},
         * or {@link Modifier#getMask(Set)}, {@link #areAllPressed(Set)}, or
         * {@link #areAnyPressed(Set)}).
         */
        public static final Set<Modifier> ALL_MODIFIERS =
            EnumSet.of(Modifier.SHIFT, Modifier.CTRL, Modifier.ALT, Modifier.META);

        /**
         * Convert the input string into one of our enum values.
         *
         * @param input Should be one of the enum constants, but we will also
         *              recognize the constant regardless of case, and also
         *              allow the Unicode character equivalent to be valid.
         * @return The enum value corresponding to the input.
         * @throws IllegalArgumentException if the input cannot be recognized
         * @throws NullPointerException if the input value is {@code null}
         */
         public static Modifier decode(final String input) {
             if (input == null) {
                 throw new NullPointerException("Null input to Modifier.decode");
             }

             for (Modifier m : values()) {
                 if (m.toString().equalsIgnoreCase(input)) {
                     return m;
                 } else if (m.keySymbol.equals(input)) {
                     return m;
                 }
             }

             throw new IllegalArgumentException("Illegal input to Modifier.decode: '" + input + "'");
        }
    }

    /**
     * A tuple class to return in one swoop the current set of modifiers being pressed.
     */
    public static class Modifiers {
        /** Is the platform-specified {@code Cmd} key pressed? */
        public boolean cmdPressed;
        /** Is the platform-specific word navigation key pressed? */
        public boolean wordNavPressed;
        /** Is the {@link Modifier#SHIFT} key pressed? */
        public boolean shiftPressed;
        /** Is the {@link Modifier#CTRL} key pressed? */
        public boolean ctrlPressed;
        /** Is the {@link Modifier#ALT} key pressed? */
        public boolean altPressed;
        /** Is the {@link Modifier#META} key prssed? */
        public boolean metaPressed;
    }

    /**
     * Enumeration representing key locations.
     */
    public enum KeyLocation {
        /** The "standard" location; in the regular key location. */
        STANDARD(KEY_LOCATION_STANDARD),
        /** On the left side of the keyboard. */
        LEFT(KEY_LOCATION_LEFT),
        /** On the right side of the keyboard. */
        RIGHT(KEY_LOCATION_RIGHT),
        /** On the numeric keypad. */
        KEYPAD(KEY_LOCATION_NUMPAD);

        /**
         * The native key location we are mapping.
         */
        private final int nativeLocation;

        /**
         * Construct given the native key location we represent.
         * @param keyLocation The native location value.
         */
        KeyLocation(final int keyLocation) {
            nativeLocation = keyLocation;
        }

        /**
         * Translate the given native AWT key location constant into one of our values.
         * @param awtKeyLocation The native location.
         * @return The corresponding one of our values.
         */
        public static KeyLocation fromAWTLocation(final int awtKeyLocation) {
            KeyLocation keyLocation = null;

            for (KeyLocation loc : values()) {
                if (awtKeyLocation == loc.nativeLocation) {
                    keyLocation = loc;
                    break;
                }
            }

            return keyLocation;
        }
    }

    /**
     * Represents a keystroke, a combination of a keycode and modifier flags.
     */
    public static final class KeyStroke {
        private int keyCode = KeyCode.UNDEFINED;
        private int keyModifiers = 0x00;

        public static final String COMMAND_ABBREVIATION = "CMD";

        /**
         * Pattern to recognize modifiers and key values. Note: this supports the "current" Unicode symbols used
         * on OSX to display keystrokes (and what is returned by {@link java.awt.event.InputEvent#getModifiersExText})
         * but could, potentially, be subject to change.
         * <p> Supported patterns include: <code>F12</code> (key by itself), <code>Cmd+A</code> ("Cmd" modifier,
         * which is platform-specific, using "+" separator), <code>Ctrl-Shift-Alt-Left</code> (multiple modifiers,
         * using "-" separator), <code>&#x2303;&#x21E7;Right</code> (OSX-style modifiers without separator),
         * <code>&#x2325;-&#x21E7;-F4</code> (OSX-style with separators), or <code>ALT+ctrl+R</code> (upper- or
         * lower-case modifiers).
         * @see KeyStroke#decode
         */
        private static final Pattern KEYSTROKE_PATTERN =
                Pattern.compile("([\u21E7\u2303\u2325\u2318]+|(([a-zA-Z]+|[\u21E7\u2303\u2325\u2318])[\\-\\+])+)?(.+)");

        /**
         * Construct from a key code and the bit mask of the desired modifiers.
         * @param code The key code desired.
         * @param modifiers The bit mask of modifiers to use.
         * @see Keyboard.KeyCode
         * @see Keyboard.Modifier
         */
        public KeyStroke(final int code, final int modifiers) {
            keyCode = code;
            keyModifiers = modifiers;
        }

        /**
         * @return The key code associated with this keystroke.
         */
        public int getKeyCode() {
            return keyCode;
        }

        /**
         * @return The bit mask of modifiers associated with this keystroke.
         */
        public int getModifiers() {
            return keyModifiers;
        }

        @Override
        public boolean equals(final Object object) {
            boolean equals = false;

            if (object instanceof KeyStroke) {
                KeyStroke keyStroke = (KeyStroke) object;
                equals = (this.keyCode == keyStroke.keyCode
                       && this.keyModifiers == keyStroke.keyModifiers);
            }

            return equals;
        }

        @Override
        public int hashCode() {
            // NOTE Key codes are currently defined as 16-bit values, so
            // shifting by 4 bits to append the modifiers should be OK.
            // However, if Sun changes the key code values in the future,
            // this may no longer be safe.
            int hashCode = keyCode << 4 | keyModifiers;
            return hashCode;
        }

        @Override
        public String toString() {
            int awtModifiers = Modifier.getAWTMask(keyModifiers);

            if (awtModifiers != 0x00) {
                String sep = Platform.getKeyStrokeModifierSeparator();
                return getModifiersExText(awtModifiers).replace("+", sep)
                    + sep + getKeyText(keyCode);
            }

            return getKeyText(keyCode);
        }

        /**
         * Decode a keystroke value from its string representation.
         *
         * @param value Input value, such as {@code "Cmd-F1"},
         *              {@code "Ctrl+Shift+Left"}, or even <code>"&#x2303;&#x2318;F1"</code>.
         * @return      The corresponding {@code KeyStroke} value.
         * @throws IllegalArgumentException if the input string cannot be
         *         decoded.
         */
        public static KeyStroke decode(final String value) {
            Utils.checkNull(value, "value");

            int keyCode = KeyCode.UNDEFINED;
            int keyModifiers = 0x00;
            String sep = Platform.getKeyStrokeModifierSeparator();
            boolean emptySep = sep.equals("");

            Matcher m = KEYSTROKE_PATTERN.matcher(value);
            if (m.matches()) {
                String modifiers = m.group(1);
                if (modifiers != null) {
                    String[] keys;
                    if (modifiers.indexOf('-') >= 0 || modifiers.indexOf('+') >= 0) {
                        keys = modifiers.split("[\\-\\+]");
                    } else {
                        keys  = modifiers.split("");
                    }
                    for (int i = 0, n = keys.length; i < n; i++) {
                        String modifierAbbreviation = keys[i].toUpperCase(Locale.ENGLISH);

                        Modifier modifier;
                        if (modifierAbbreviation.equals(COMMAND_ABBREVIATION)) {
                            modifier = Platform.getCommandModifier();
                        } else {
                            modifier = Modifier.decode(modifierAbbreviation);
                        }

                        keyModifiers |= modifier.getMask();
                    }
                }

                // The final part is the KeyCode itself
                String code = m.group(4);
                try {
                    Field keyCodeField = KeyCode.class.getField(code.toUpperCase(Locale.ENGLISH));
                    keyCode = ((Integer) keyCodeField.get(null)).intValue();
                } catch (Exception exception) {
                    throw new IllegalArgumentException(exception);
                }
            } else {
                throw new IllegalArgumentException("KeyStroke cannot be decoded from '" + value + "'");
            }

            return new KeyStroke(keyCode, keyModifiers);
        }
    }

    /**
     * Contains a set of key code constants that are common to all locales.
     */
    public static final class KeyCode {
        public static final int A = VK_A;
        public static final int B = VK_B;
        public static final int C = VK_C;
        public static final int D = VK_D;
        public static final int E = VK_E;
        public static final int F = VK_F;
        public static final int G = VK_G;
        public static final int H = VK_H;
        public static final int I = VK_I;
        public static final int J = VK_J;
        public static final int K = VK_K;
        public static final int L = VK_L;
        public static final int M = VK_M;
        public static final int N = VK_N;
        public static final int O = VK_O;
        public static final int P = VK_P;
        public static final int Q = VK_Q;
        public static final int R = VK_R;
        public static final int S = VK_S;
        public static final int T = VK_T;
        public static final int U = VK_U;
        public static final int V = VK_V;
        public static final int W = VK_W;
        public static final int X = VK_X;
        public static final int Y = VK_Y;
        public static final int Z = VK_Z;

        public static final int N0 = VK_0;
        public static final int N1 = VK_1;
        public static final int N2 = VK_2;
        public static final int N3 = VK_3;
        public static final int N4 = VK_4;
        public static final int N5 = VK_5;
        public static final int N6 = VK_6;
        public static final int N7 = VK_7;
        public static final int N8 = VK_8;
        public static final int N9 = VK_9;

        public static final int PERIOD = VK_PERIOD;

        public static final int TAB = VK_TAB;
        public static final int SPACE = VK_SPACE;
        public static final int ENTER = VK_ENTER;
        public static final int ESCAPE = VK_ESCAPE;
        public static final int BACKSPACE = VK_BACK_SPACE;
        public static final int DELETE = VK_DELETE;
        public static final int INSERT = VK_INSERT;

        public static final int UP = VK_UP;
        public static final int DOWN = VK_DOWN;
        public static final int LEFT = VK_LEFT;
        public static final int RIGHT = VK_RIGHT;

        public static final int PAGE_UP = VK_PAGE_UP;
        public static final int PAGE_DOWN = VK_PAGE_DOWN;

        public static final int HOME = VK_HOME;
        public static final int END = VK_END;

        public static final int KEYPAD_0 = VK_NUMPAD0;
        public static final int KEYPAD_1 = VK_NUMPAD1;
        public static final int KEYPAD_2 = VK_NUMPAD2;
        public static final int KEYPAD_3 = VK_NUMPAD3;
        public static final int KEYPAD_4 = VK_NUMPAD4;
        public static final int KEYPAD_5 = VK_NUMPAD5;
        public static final int KEYPAD_6 = VK_NUMPAD6;
        public static final int KEYPAD_7 = VK_NUMPAD7;
        public static final int KEYPAD_8 = VK_NUMPAD8;
        public static final int KEYPAD_9 = VK_NUMPAD9;
        public static final int KEYPAD_UP = VK_KP_UP;
        public static final int KEYPAD_DOWN = VK_KP_DOWN;
        public static final int KEYPAD_LEFT = VK_KP_LEFT;
        public static final int KEYPAD_RIGHT = VK_KP_RIGHT;

        public static final int PLUS = VK_PLUS;
        public static final int MINUS = VK_MINUS;
        public static final int EQUALS = VK_EQUALS;

        public static final int ADD = VK_ADD;
        public static final int SUBTRACT = VK_SUBTRACT;
        public static final int MULTIPLY = VK_MULTIPLY;
        public static final int DIVIDE = VK_DIVIDE;

        public static final int SLASH = VK_SLASH;
        public static final int ASTERISK = VK_ASTERISK;

        public static final int F1 = VK_F1;
        public static final int F2 = VK_F2;
        public static final int F3 = VK_F3;
        public static final int F4 = VK_F4;
        public static final int F5 = VK_F5;
        public static final int F6 = VK_F6;
        public static final int F7 = VK_F7;
        public static final int F8 = VK_F8;
        public static final int F9 = VK_F9;
        public static final int F10 = VK_F10;
        public static final int F11 = VK_F11;
        public static final int F12 = VK_F12;

        public static final int UNDEFINED = VK_UNDEFINED;
    }

    /**
     * The current set of pressed modifier keys.
     */
    private static int currentModifiers = 0;

    /**
     * @return A bitfield representing the keyboard modifiers that are currently
     * pressed.
     */
    public static int getModifiers() {
        return currentModifiers;
    }

    /**
     * Set the current set of pressed keyboard modifiers.
     *
     * @param modifiers The complete bit mask of current modifiers.
     */
    protected static void setModifiers(final int modifiers) {
        currentModifiers = modifiers;
    }

    /**
     * Tests the pressed state of a modifier.
     *
     * <p> Note: this method tests whether or not one modifier is pressed.
     * It does not, however, test if this is the ONLY modifier pressed. Use
     * one of the {@code arePressed(...)} methods for that purpose. To test
     * efficiently for whether more than one modifier is pressed, use one
     * of the {@code areAnyPressed(...)} or {@code areAllPressed(...)} methods.
     * And finally, to succinctly test for the "Cmd" key (which is
     * platform-dependent) being pressed, use {@link isCmdPressed}.
     *
     * @param modifier The modifier to test.
     * @return {@code true} if the modifier is pressed; {@code false},
     * otherwise.
     */
    public static boolean isPressed(final Modifier modifier) {
        return (currentModifiers & modifier.getMask()) > 0;
    }

    /**
     * Test to see if and only if the given set of modifier(s) are pressed.
     *
     * @param modifierSet The set of modifiers to test.
     * @return {@code true} if only those modifiers (and no others)
     * are pressed.
     */
    public static boolean arePressed(final Set<Modifier> modifierSet) {
       return currentModifiers == Modifier.getMask(modifierSet);
    }

    /**
     * Test to see if and only if the given modifier(s) are pressed.
     *
     * @param modifiers The modifiers to test.
     * @return {@code true} if only those modifiers (and no others)
     * are pressed.
     */
    public static boolean arePressed(final Modifier... modifiers) {
       return currentModifiers == Modifier.getMask(modifiers);
    }

    /**
     * Are any of the given set of {@link Modifier}s pressed?
     *
     * @param modifierSet The set of modifiers to test.
     * @return {@code true} if any of them are pressed, {@code false}
     * if none are pressed.
     */
    public static boolean areAnyPressed(final Set<Modifier> modifierSet) {
        return (currentModifiers & Modifier.getMask(modifierSet)) > 0;
    }

    /**
     * Are any of the given list of {@link Modifier}s pressed?
     *
     * @param modifiers The modifiers to test.
     * @return {@code true} if any of them are pressed, {@code false}
     * if none are pressed.
     */
    public static boolean areAnyPressed(final Modifier... modifiers) {
        return (currentModifiers & Modifier.getMask(modifiers)) > 0;
    }

    /**
     * Are all of the given set of {@link Modifier}s pressed?
     * <p> This is typically used to test two modifiers (like CTRL and SHIFT).
     *
     * @param modifierSet The set of modifiers to test.
     * @return {@code true} if all of the modifiers are pressed, {@code false}
     * if only some or none are pressed.
     */
    public static boolean areAllPressed(final Set<Modifier> modifierSet) {
        int mask = Modifier.getMask(modifierSet);
        return (currentModifiers & mask) == mask;
    }

    /**
     * Are all of the given list of {@link Modifier}s pressed?
     * <p> This is typically used to test two modifiers (like CTRL and SHIFT).
     *
     * @param modifiers The modifiers to test.
     * @return {@code true} if all of the modifiers are pressed, {@code false}
     * if only some or none are pressed.
     */
    public static boolean areAllPressed(final Modifier... modifiers) {
        int mask = Modifier.getMask(modifiers);
        return (currentModifiers & mask) == mask;
    }

    /**
     * Shortcut method to test if the {@link Platform#getCommandModifier} is pressed.
     *
     * @return The result of {@code isPressed(Platform.getCommandModifier())}.
     */
    public static boolean isCmdPressed() {
        return isPressed(Platform.getCommandModifier());
    }

    /**
     * Shortcut method to test if the {@link Platform#getWordNavigationModifier} is pressed.
     *
     * @return The result of {@code isPressed(Platform.getWordNavigationModifier())}.
     */
    public static boolean isWordNavPressed() {
        return isPressed(Platform.getWordNavigationModifier());
    }

    /**
     * Return a standardized set of flags to say which modifiers are currently pressed.
     * <p> This is for convenience in keypress handlers that typically need to know all
     * these states, and have to deal with the platform differences as well.  So,
     * consolidate that logic here for "one-stop shopping".
     *
     * @return The platform-specific set of flags as to which modifiers are currently
     *         pressed by the user.
     */
    public static Modifiers pressed() {
        Modifiers mods = new Modifiers();

        mods.cmdPressed = isCmdPressed();
        mods.wordNavPressed = isWordNavPressed();

        mods.shiftPressed = isPressed(Modifier.SHIFT);
        mods.ctrlPressed = isPressed(Modifier.CTRL);
        mods.altPressed = isPressed(Modifier.ALT);
        mods.metaPressed = isPressed(Modifier.META);

        return mods;
    }

    /**
     * Returns the current drop action.
     *
     * @return The drop action corresponding to the currently pressed modifier keys.
     * @see DropAction#getDropAction
     */
    public static DropAction getDropAction() {
        DropAction dropAction = null;

        if (Platform.isOSX()) {
            if (arePressed(Modifier.ALT, Modifier.META)) {
                dropAction = DropAction.LINK;
            } else if (arePressed(Modifier.ALT)) {
                dropAction = DropAction.COPY;
            } else {
                dropAction = DropAction.MOVE;
            }
        } else if (Platform.isWindows()) {
            if (arePressed(Modifier.CTRL, Modifier.SHIFT)) {
                dropAction = DropAction.LINK;
            } else if (arePressed(Modifier.CTRL)) {
                dropAction = DropAction.COPY;
            } else {
                dropAction = DropAction.MOVE;
            }
        } else {
            // Note: different desktop managers *may* have different conventions
            if (arePressed(Modifier.CTRL, Modifier.SHIFT)) {
                dropAction = DropAction.LINK;
            } else if (arePressed(Modifier.CTRL)) {
                dropAction = DropAction.COPY;
            } else {
                dropAction = DropAction.MOVE;
            }
        }

        return dropAction;
    }
}
