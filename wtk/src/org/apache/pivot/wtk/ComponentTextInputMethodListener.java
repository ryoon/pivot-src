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

import java.awt.Rectangle;
import java.awt.event.InputMethodEvent;
import java.awt.font.TextHitInfo;
import java.text.AttributedCharacterIterator;

/**
 * The wrapper implementation of the {@link TextInputMethodListener} interface,
 * which defers to the listener (if any) on the currently focused component.
 */
public class ComponentTextInputMethodListener implements TextInputMethodListener {
    /**
     * @return The text input method listener (if any) of the currently focused component.
     */
    private TextInputMethodListener getCurrentListener() {
        Component currentFocus = Component.getFocusedComponent();
        if (currentFocus != null) {
            return currentFocus.getTextInputMethodListener();
        }
        return null;
    }

    @Override
    public AttributedCharacterIterator cancelLatestCommittedText(
            final AttributedCharacterIterator.Attribute[] attributes) {
        TextInputMethodListener listener = getCurrentListener();
        if (listener != null) {
            return listener.cancelLatestCommittedText(attributes);
        }
        return null;
    }

    @Override
    public AttributedCharacterIterator getCommittedText(final int beginIndex, final int endIndex,
            final AttributedCharacterIterator.Attribute[] attributes) {
        TextInputMethodListener listener = getCurrentListener();
        if (listener != null) {
            return listener.getCommittedText(beginIndex, endIndex, attributes);
        }
        return null;
    }

    @Override
    public int getCommittedTextLength() {
        TextInputMethodListener listener = getCurrentListener();
        if (listener != null) {
            return listener.getCommittedTextLength();
        }
        return 0;
    }

    @Override
    public int getInsertPositionOffset() {
        TextInputMethodListener listener = getCurrentListener();
        if (listener != null) {
            return listener.getInsertPositionOffset();
        }
        return 0;
    }

    @Override
    public TextHitInfo getLocationOffset(final int x, final int y) {
        TextInputMethodListener listener = getCurrentListener();
        if (listener != null) {
            return listener.getLocationOffset(x, y);
        }
        return null;
    }

    @Override
    public AttributedCharacterIterator getSelectedText(
            final AttributedCharacterIterator.Attribute[] attributes) {
        TextInputMethodListener listener = getCurrentListener();
        if (listener != null) {
            return listener.getSelectedText(attributes);
        }
        return null;
    }

    @Override
    public Rectangle getTextLocation(final TextHitInfo offset) {
        TextInputMethodListener listener = getCurrentListener();
        if (listener != null) {
            return listener.getTextLocation(offset);
        }
        return new Rectangle();
    }

    @Override
    public void inputMethodTextChanged(final InputMethodEvent event) {
        TextInputMethodListener listener = getCurrentListener();
        if (listener != null) {
            listener.inputMethodTextChanged(event);
        }
    }

    @Override
    public void caretPositionChanged(final InputMethodEvent event) {
        TextInputMethodListener listener = getCurrentListener();
        if (listener != null) {
            listener.caretPositionChanged(event);
        }
    }

}

