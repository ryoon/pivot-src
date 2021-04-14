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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.Locale;

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
        SHIFT(InputEvent.SHIFT_DOWN_MASK),
        CTRL(InputEvent.CTRL_DOWN_MASK),
        ALT(InputEvent.ALT_DOWN_MASK),
        META(InputEvent.META_DOWN_MASK);

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
        private Modifier(final int modifier) {
            awtModifier = modifier;
            keySymbol = InputEvent.getModifiersExText(modifier);
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
         public static final Modifier decode(final String input) {
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
     * Enumeration representing key locations.
     */
    public enum KeyLocation {
        STANDARD, LEFT, RIGHT, KEYPAD
    }

    /**
     * Represents a keystroke, a combination of a keycode and modifier flags.
     */
    public static final class KeyStroke {
        private int keyCode = KeyCode.UNDEFINED;
        private int keyModifiers = 0x00;

        public static final String COMMAND_ABBREVIATION = "CMD";

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
            // However, i:w f Sun changes the key code values in the future,
            // this may no longer be safe.
            int hashCode = keyCode << 4 | keyModifiers;
            return hashCode;
        }

        @Override
        public String toString() {
            int awtModifiers = Modifier.getAWTMask(keyModifiers);

            if (awtModifiers != 0x00) {
                String sep = Platform.getKeyStrokeModifierSeparator();
                return InputEvent.getModifiersExText(awtModifiers).replace("+", sep)
                    + sep + KeyEvent.getKeyText(keyCode);
            }

            return KeyEvent.getKeyText(keyCode);
        }

        /**
         * Decode a keystroke value from its string representation.
         *
         * @param value Input value, such as {@code "Cmd-F1"},
         *              {@code "Ctrl-Shift-Left"}, or even <code>"&#x2303;&#x2318;F1"</code>.
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

            String[] keys = value.split(sep);
            for (int i = 0, n = keys.length; i < n; i++) {
                if ((emptySep && keys[i].charAt(0) < 0x1000) || (i < n - 1)) {
                    // The first n-1 parts are Modifier values
                    String modifierAbbreviation = keys[i].toUpperCase(Locale.ENGLISH);

                    Modifier modifier;
                    if (modifierAbbreviation.equals(COMMAND_ABBREVIATION)) {
                        modifier = Platform.getCommandModifier();
                    } else {
                        modifier = Modifier.decode(modifierAbbreviation);
                    }

                    keyModifiers |= modifier.getMask();
                } else {
                    // The final part is the KeyCode itself
                    String code = emptySep ? value.substring(i) : keys[i];
                    try {
                        Field keyCodeField = KeyCode.class.getField(code.toUpperCase(Locale.ENGLISH));
                        keyCode = ((Integer) keyCodeField.get(null)).intValue();
                    } catch (Exception exception) {
                        throw new IllegalArgumentException(exception);
                    }
                }
            }

            return new KeyStroke(keyCode, keyModifiers);
        }
    }

    /**
     * Contains a set of key code constants that are common to all locales.
     */
    public static final class KeyCode {
        public static final int A = KeyEvent.VK_A;
        public static final int B = KeyEvent.VK_B;
        public static final int C = KeyEvent.VK_C;
        public static final int D = KeyEvent.VK_D;
        public static final int E = KeyEvent.VK_E;
        public static final int F = KeyEvent.VK_F;
        public static final int G = KeyEvent.VK_G;
        public static final int H = KeyEvent.VK_H;
        public static final int I = KeyEvent.VK_I;
        public static final int J = KeyEvent.VK_J;
        public static final int K = KeyEvent.VK_K;
        public static final int L = KeyEvent.VK_L;
        public static final int M = KeyEvent.VK_M;
        public static final int N = KeyEvent.VK_N;
        public static final int O = KeyEvent.VK_O;
        public static final int P = KeyEvent.VK_P;
        public static final int Q = KeyEvent.VK_Q;
        public static final int R = KeyEvent.VK_R;
        public static final int S = KeyEvent.VK_S;
        public static final int T = KeyEvent.VK_T;
        public static final int U = KeyEvent.VK_U;
        public static final int V = KeyEvent.VK_V;
        public static final int W = KeyEvent.VK_W;
        public static final int X = KeyEvent.VK_X;
        public static final int Y = KeyEvent.VK_Y;
        public static final int Z = KeyEvent.VK_Z;

        public static final int N0 = KeyEvent.VK_0;
        public static final int N1 = KeyEvent.VK_1;
        public static final int N2 = KeyEvent.VK_2;
        public static final int N3 = KeyEvent.VK_3;
        public static final int N4 = KeyEvent.VK_4;
        public static final int N5 = KeyEvent.VK_5;
        public static final int N6 = KeyEvent.VK_6;
        public static final int N7 = KeyEvent.VK_7;
        public static final int N8 = KeyEvent.VK_8;
        public static final int N9 = KeyEvent.VK_9;

        public static final int PERIOD = KeyEvent.VK_PERIOD;

        public static final int TAB = KeyEvent.VK_TAB;
        public static final int SPACE = KeyEvent.VK_SPACE;
        public static final int ENTER = KeyEvent.VK_ENTER;
        public static final int ESCAPE = KeyEvent.VK_ESCAPE;
        public static final int BACKSPACE = KeyEvent.VK_BACK_SPACE;
        public static final int DELETE = KeyEvent.VK_DELETE;
        public static final int INSERT = KeyEvent.VK_INSERT;

        public static final int UP = KeyEvent.VK_UP;
        public static final int DOWN = KeyEvent.VK_DOWN;
        public static final int LEFT = KeyEvent.VK_LEFT;
        public static final int RIGHT = KeyEvent.VK_RIGHT;

        public static final int PAGE_UP = KeyEvent.VK_PAGE_UP;
        public static final int PAGE_DOWN = KeyEvent.VK_PAGE_DOWN;

        public static final int HOME = KeyEvent.VK_HOME;
        public static final int END = KeyEvent.VK_END;

        public static final int KEYPAD_0 = KeyEvent.VK_NUMPAD0;
        public static final int KEYPAD_1 = KeyEvent.VK_NUMPAD1;
        public static final int KEYPAD_2 = KeyEvent.VK_NUMPAD2;
        public static final int KEYPAD_3 = KeyEvent.VK_NUMPAD3;
        public static final int KEYPAD_4 = KeyEvent.VK_NUMPAD4;
        public static final int KEYPAD_5 = KeyEvent.VK_NUMPAD5;
        public static final int KEYPAD_6 = KeyEvent.VK_NUMPAD6;
        public static final int KEYPAD_7 = KeyEvent.VK_NUMPAD7;
        public static final int KEYPAD_8 = KeyEvent.VK_NUMPAD8;
        public static final int KEYPAD_9 = KeyEvent.VK_NUMPAD9;
        public static final int KEYPAD_UP = KeyEvent.VK_KP_UP;
        public static final int KEYPAD_DOWN = KeyEvent.VK_KP_DOWN;
        public static final int KEYPAD_LEFT = KeyEvent.VK_KP_LEFT;
        public static final int KEYPAD_RIGHT = KeyEvent.VK_KP_RIGHT;

        public static final int PLUS = KeyEvent.VK_PLUS;
        public static final int MINUS = KeyEvent.VK_MINUS;
        public static final int EQUALS = KeyEvent.VK_EQUALS;

        public static final int ADD = KeyEvent.VK_ADD;
        public static final int SUBTRACT = KeyEvent.VK_SUBTRACT;
        public static final int MULTIPLY = KeyEvent.VK_MULTIPLY;
        public static final int DIVIDE = KeyEvent.VK_DIVIDE;

        public static final int SLASH = KeyEvent.VK_SLASH;
        public static final int ASTERISK = KeyEvent.VK_ASTERISK;

        public static final int F1 = KeyEvent.VK_F1;
        public static final int F2 = KeyEvent.VK_F2;
        public static final int F3 = KeyEvent.VK_F3;
        public static final int F4 = KeyEvent.VK_F4;
        public static final int F5 = KeyEvent.VK_F5;
        public static final int F6 = KeyEvent.VK_F6;
        public static final int F7 = KeyEvent.VK_F7;
        public static final int F8 = KeyEvent.VK_F8;
        public static final int F9 = KeyEvent.VK_F9;
        public static final int F10 = KeyEvent.VK_F10;
        public static final int F11 = KeyEvent.VK_F11;
        public static final int F12 = KeyEvent.VK_F12;

        public static final int UNDEFINED = KeyEvent.VK_UNDEFINED;
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
     * Returns the current drop action.
     *
     * @return The drop action corresponding to the currently pressed modifier keys.
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
            // TODO: is this correct for Linux / Unix / ???
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
