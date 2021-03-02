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

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Iterator;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.LinkedStack;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSON;
import org.apache.pivot.text.AttributedStringCharacterIterator;
import org.apache.pivot.text.CharSpan;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * A component that allows a user to enter multiple lines of unformatted text.
 */
public class TextArea extends Component {
    /**
     * Class representing a paragraph of text.
     */
    public static final class Paragraph {
        private StringBuilder characters = new StringBuilder(INITIAL_PARAGRAPH_CAPACITY);
        private TextArea textArea = null;
        private int offset = -1;

        private ParagraphListener.Listeners paragraphListeners = new ParagraphListener.Listeners();

        /**
         * @return The characters in this paragraph.
         */
        public CharSequence getCharacters() {
            return characters;
        }

        /**
         * @return The {@code TextArea} this paragraph belongs to.
         */
        public TextArea getTextArea() {
            return textArea;
        }

        /**
         * Append the given character to this paragraph.
         *
         * @param character The character to append.
         */
        public void append(final char character) {
            if (textArea != null) {
                throw new IllegalStateException();
            }

            characters.append(character);
        }

        /**
         * Clear all the characters in this paragraph.
         */
        public void clear() {
            if (textArea != null) {
                throw new IllegalStateException();
            }

            characters.delete(0, characters.length());
        }

        /**
         * Insert the characters at the given index into this paragraph.
         *
         * @param text  The sequence of characters to insert.
         * @param index The index into this paragraph where to insert the text.
         */
        public void insertText(final CharSequence text, final int index) {
            Utils.checkNull(text, "Text to insert");

            indexBoundsCheck("index", index, 0, characters.length());

            int count = text.length();

            if (textArea != null) {
                Utils.checkTextMaximumLength(textArea.characterCount, count, textArea.maximumLength);
            }

            characters.insert(index, text);

            if (textArea != null) {
                // Update offsets and character count
                textArea.updateParagraphOffsets(textArea.paragraphs.indexOf(this) + 1, count);
                textArea.characterCount += count;

                // Update selection state
                int previousSelectionStart = textArea.selectionStart;
                int previousSelectionLength = textArea.selectionLength;
                textArea.selectionStart = offset + index + count;
                textArea.selectionLength = 0;

                // Fire change events
                paragraphListeners.textInserted(this, index, count);
                textArea.textAreaContentListeners.textChanged(textArea);

                if (textArea.selectionStart != previousSelectionStart
                 || textArea.selectionLength != previousSelectionLength) {
                    textArea.textAreaSelectionListeners.selectionChanged(textArea,
                        textArea.selectionStart, textArea.selectionLength);
                }
            }
        }

        /**
         * Remove all the text in this paragraph starting at the index.
         *
         * @param index The starting index in the paragraph where to remove the text.
         */
        public void removeText(final int index) {
            removeText(index, characters.length() - index);
        }

        /**
         * Remove some of the text in this paragraph.
         *
         * @param index Index in the paragraph where to start to remove the text.
         * @param count Number of characters to remove starting at the index.
         */
        public void removeText(final int index, final int count) {
            Utils.checkIndexBounds(index, count, 0, characters.length());

            characters.delete(index, index + count);

            if (textArea != null) {
                // Update offsets and character count
                textArea.updateParagraphOffsets(textArea.paragraphs.indexOf(this) + 1, -count);
                textArea.characterCount -= count;

                // Update selection state
                int previousSelectionStart = textArea.selectionStart;
                int previousSelectionLength = textArea.selectionLength;
                textArea.selectionStart = offset + index;
                textArea.selectionLength = 0;

                // Fire change events
                paragraphListeners.textRemoved(this, index, count);
                textArea.textAreaContentListeners.textChanged(textArea);

                if (textArea.selectionStart != previousSelectionStart
                 || textArea.selectionLength != previousSelectionLength) {
                    textArea.textAreaSelectionListeners.selectionChanged(textArea,
                        textArea.selectionStart, textArea.selectionLength);
                }
            }
        }

        /**
         * @return The relative offset of the text in this paragraph from the
         * beginning of the entire text.
         */
        public int getOffset() {
            return offset;
        }

        /**
         * @return The listeners on this paragraph.
         */
        public ListenerList<ParagraphListener> getParagraphListeners() {
            return paragraphListeners;
        }
    }

    /**
     * Paragraph listener interface.
     */
    public interface ParagraphListener {
        /**
         * Paragraph listener interface adapter.
         */
        class Adapter implements ParagraphListener {
            @Override
            public void textInserted(final Paragraph paragraph, final int index, final int count) {
                // empty block
            }

            @Override
            public void textRemoved(final Paragraph paragraph, final int index, final int count) {
                // empty block
            }
        }

        /**
         * Paragraph listeners.
         */
        class Listeners extends ListenerList<ParagraphListener>
            implements ParagraphListener {
            @Override
            public void textInserted(final Paragraph paragraph, final int index, final int count) {
                forEach(listener -> listener.textInserted(paragraph, index, count));
            }

            @Override
            public void textRemoved(final Paragraph paragraph, final int index, final int count) {
                forEach(listener -> listener.textRemoved(paragraph, index, count));
            }
        }

        /**
         * Called when text has been inserted into a paragraph.
         *
         * @param paragraph The source of the event.
         * @param index The index at which the text was inserted.
         * @param count The number of characters that were inserted.
         */
        void textInserted(Paragraph paragraph, int index, int count);

        /**
         * Called when characters have been removed from a paragraph.
         *
         * @param paragraph The source of the event.
         * @param index The index from which the text was removed.
         * @param count The number of characters that were removed.
         */
        void textRemoved(Paragraph paragraph, int index, int count);
    }

    /**
     * Enum representing a scroll direction.
     */
    public enum ScrollDirection {
        UP,
        DOWN
    }

    /**
     * Text area skin interface. Text area skins are required to implement this.
     */
    public interface Skin {
        /**
         * @return The insertion point for a given location.
         *
         * @param x The X-location to check (likely from the mouse location).
         * @param y The Y-location to check.
         */
        int getInsertionPoint(int x, int y);

        /**
         * @return The next insertion point given an x coordinate and a
         * character index.
         *
         * @param x         The current X-location.
         * @param from      The current character index to move from.
         * @param direction The direction we want to move.
         */
        int getNextInsertionPoint(int x, int from, ScrollDirection direction);

        /**
         * @return The row index of the character at a given index.
         *
         * @param index The character index to check.
         */
        int getRowAt(int index);

        /**
         * @return The index of the first character in the row containing a
         * given character index.
         *
         * @param index The character index to check.
         */
        int getRowOffset(int index);

        /**
         * @return The number of characters in the row containing a given
         * character index.
         *
         * @param index The character index to check.
         */
        int getRowLength(int index);

        /**
         * @return The total number of rows in the text area.
         */
        int getRowCount();

        /**
         * @return The bounds of the character at a given index.
         *
         * @param index The index of the character to check.
         */
        Bounds getCharacterBounds(int index);

        /**
         * @return The current setting of the "tabWidth" style (so "setText"
         * uses the same value as Ctrl-Tab from user).
         */
        int getTabWidth();
    }

    /**
     * Translates between text and context data during data binding.
     */
    public interface TextBindMapping {
        /**
         * Converts a value from the bind context to a text representation
         * during a {@link Component#load(Object)} operation.
         *
         * @param value The value from the bind context to convert to text.
         * @return The string representation of the value to display.
         */
        String toString(Object value);

        /**
         * Converts a text string to a value to be stored in the bind context
         * during a {@link Component#store(Object)} operation.
         *
         * @param text The current text from the control to convert to an object
         * suitable for storage in the bind context.
         * @return The text converted to an object suitable for the bind context.
         */
        Object valueOf(String text);
    }

    /**
     * Text area paragraph sequence.
     */
    public final class ParagraphSequence implements Sequence<Paragraph>, Iterable<Paragraph> {
        @Override
        public int add(final Paragraph paragraph) {
            int index = getLength();
            insert(paragraph, index);

            return index;
        }

        @Override
        public void insert(final Paragraph paragraph, final int index) {
            Utils.checkNull(paragraph, "Paragraph");

            if (paragraph.textArea != null) {
                throw new IllegalArgumentException(
                    "Paragraph is already in use by another text area.");
            }

            // Determine insertion count, including terminator character
            int characterCountLocal = paragraph.characters.length();

            if (getLength() > 0) {
                characterCountLocal++;
            }

            Utils.checkTextMaximumLength(TextArea.this.characterCount, characterCountLocal,
                    TextArea.this.maximumLength);

            // Set the paragraph offset
            if (index == paragraphs.getLength()) {
                paragraph.offset = TextArea.this.characterCount;

                // Include terminator character
                if (index > 0) {
                    paragraph.offset++;
                }
            } else {
                paragraph.offset = paragraphs.get(index).offset;
            }

            // Insert the paragraph
            paragraphs.insert(paragraph, index);
            paragraph.textArea = TextArea.this;

            // Update offsets and character count
            updateParagraphOffsets(index + 1, characterCountLocal);
            TextArea.this.characterCount += characterCountLocal;

            // Update selection state
            int previousSelectionStart = selectionStart;
            int previousSelectionLength = selectionLength;
            selectionStart = paragraph.offset + paragraph.characters.length();
            selectionLength = 0;

            // Fire change events
            textAreaContentListeners.paragraphInserted(TextArea.this, index);
            textAreaContentListeners.textChanged(TextArea.this);

            if (selectionStart != previousSelectionStart
                || selectionLength != previousSelectionLength) {
                textAreaSelectionListeners.selectionChanged(TextArea.this, selectionStart,
                    selectionLength);
            }
        }

        @Override
        @UnsupportedOperation
        public Paragraph update(final int index, final Paragraph paragraph) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(final Paragraph paragraph) {
            int index = indexOf(paragraph);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Paragraph> remove(final int index, final int count) {
            Sequence<Paragraph> removed = paragraphs.remove(index, count);

            if (count > 0) {
                int characterCountLocal = 0;
                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    Paragraph paragraph = removed.get(i);
                    paragraph.textArea = null;
                    paragraph.offset = -1;
                    characterCountLocal += paragraph.characters.length() + 1;
                }

                // Don't include the implicit final terminator in the character count
                if (getLength() == 0) {
                    characterCountLocal--;
                }

                // Update offsets
                updateParagraphOffsets(index, -characterCountLocal);
                TextArea.this.characterCount -= characterCountLocal;

                // Update selection state
                int previousSelectionStart = selectionStart;
                int previousSelectionLength = selectionLength;
                selectionStart = (index == paragraphs.getLength()) ? TextArea.this.characterCount
                    : paragraphs.get(index).offset;
                selectionLength = 0;

                // Fire change events
                textAreaContentListeners.paragraphsRemoved(TextArea.this, index, removed);
                textAreaContentListeners.textChanged(TextArea.this);

                if (selectionStart != previousSelectionStart
                    || selectionLength != previousSelectionLength) {
                    textAreaSelectionListeners.selectionChanged(TextArea.this, selectionStart,
                        selectionLength);
                }
            }

            return removed;
        }

        @Override
        public Paragraph get(final int index) {
            return paragraphs.get(index);
        }

        @Override
        public int indexOf(final Paragraph paragraph) {
            return paragraphs.indexOf(paragraph);
        }

        @Override
        public int getLength() {
            return paragraphs.getLength();
        }

        @Override
        public Iterator<Paragraph> iterator() {
            return new ImmutableIterator<>(paragraphs.iterator());
        }
    }

    /**
     * Interface for undo history.
     */
    private interface Edit {
        void undo();
    }

    /**
     * Undo event for an insert.
     */
    private class InsertTextEdit implements Edit {
        private final int index;
        private final int count;

        InsertTextEdit(final CharSequence insertText, final int insertIndex) {
            index = insertIndex;
            count = insertText.length();
        }

        @Override
        public void undo() {
            removeText(index, count, false);
        }
    }

    /**
     * Undo event for a remove.
     */
    private class RemoveTextEdit implements Edit {
        private final int index;
        private final CharSequence text;

        RemoveTextEdit(final int removeIndex, final int count) {
            index = removeIndex;
            text = getText(index, index + count);
        }

        @Override
        public void undo() {
            insertText(text, index, false);
        }
    }

    private ArrayList<Paragraph> paragraphs = new ArrayList<>();
    private ParagraphSequence paragraphSequence = new ParagraphSequence();
    private AttributedStringCharacterIterator composedText = null;

    private int characterCount = 0;

    private int selectionStart = 0;
    private int selectionLength = 0;

    private boolean expandTabs = false;

    /** Default maximum length (~1MB) or 2^20 - 1. */
    private static final int DEFAULT_MAX_LENGTH = 1048575;

    private int maximumLength = DEFAULT_MAX_LENGTH;
    private boolean editable = true;

    private String textKey = null;
    private BindType textBindType = BindType.BOTH;
    private TextBindMapping textBindMapping = null;

    private LinkedStack<Edit> editHistory = new LinkedStack<>(MAXIMUM_EDIT_HISTORY_LENGTH);

    private TextAreaListener.Listeners textAreaListeners = new TextAreaListener.Listeners();
    private TextAreaContentListener.Listeners textAreaContentListeners = new TextAreaContentListener.Listeners();
    private TextAreaSelectionListener.Listeners textAreaSelectionListeners = new TextAreaSelectionListener.Listeners();
    private TextAreaBindingListener.Listeners textAreaBindingListeners = new TextAreaBindingListener.Listeners();

    private static final int INITIAL_PARAGRAPH_CAPACITY = 256;
    private static final int MAXIMUM_EDIT_HISTORY_LENGTH = 30;

    public TextArea() {
        installSkin(TextArea.class);
        try {
            setText(new StringReader(""));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    protected void setSkin(final org.apache.pivot.wtk.Skin skin) {
        checkSkin(skin, TextArea.Skin.class);

        super.setSkin(skin);
    }

    private TextArea.Skin getTextAreaSkin() {
        return (TextArea.Skin) getSkin();
    }

    /**
     * Returns the text content of the text area.
     *
     * @return A string containing a copy of the text area's text content.
     */
    public String getText() {
        return getText(0, characterCount);
    }

    /**
     * Return the current text that is in process of being composed
     * using the Input Method Editor.  This is temporary text that
     * must be displayed, scrolled, etc. but is not a permanent
     * part of what would be returned from {@link #getText} for instance.
     *
     * @return The current composed text or {@code null} if we're not
     * using an IME or we're in English input mode, or user just
     * committed or deleted the composed text.
     */
    public AttributedStringCharacterIterator getComposedText() {
        return composedText;
    }

    /**
     * Called from the Input Method Editor callbacks to set the current
     * composed text (that is, the text currently being composed into something
     * meaningful).
     *
     * @param currentComposedText The current composed text (which can be {@code null}
     * for many different reasons).
     */
    public void setComposedText(final AttributedStringCharacterIterator currentComposedText) {
        this.composedText = currentComposedText;
    }

    /**
     * Returns a portion of the text content of the text area.
     *
     * @param beginIndex The beginning location (inclusive) to obtain text from.
     * @param endIndex   The ending location (exclusive) of the text segment to fetch.
     * @return A string containing a copy of the text area's text content.
     */
    public String getText(final int beginIndex, final int endIndex) {
        return getCharacters(beginIndex, endIndex).toString();
    }

    /**
     * @return A character sequence representing the text input's content.
     */
    public CharSequence getCharacters() {
        return getCharacters(0, characterCount);
    }

    /**
     * @return A (sub) character sequence representing the contents between
     * the given indices.
     * @param start The start of the sequence (inclusive).
     * @param end The end of the sequence (exclusive).
     */
    public CharSequence getCharacters(final int start, final int end) {
        Utils.checkTwoIndexBounds(start, end, 0, characterCount);

        int count = end - start;
        if (count == 0) {
            return "";
        }

        StringBuilder textBuilder = new StringBuilder(count);

        // Get paragraph and character offset at beginIndex
        int paragraphIndex = getParagraphAt(start);
        Paragraph paragraph = paragraphs.get(paragraphIndex);

        int characterOffset = start - paragraph.offset;

        // Read characters until end is reached, appending to text builder
        // and moving to next paragraph as needed
        for (int i = 0; i < count; i++) {
            if (characterOffset == paragraph.characters.length() && i < characterCount) {
                textBuilder.append('\n');
                paragraph = paragraphs.get(++paragraphIndex);
                characterOffset = 0;
            } else {
                textBuilder.append(paragraph.characters.charAt(characterOffset++));
            }
        }

        return textBuilder;
    }

    /**
     * Sets the text content of the text area.
     *
     * @param text The new text for the control (cannot be {@code null}).
     */
    public void setText(final String text) {
        Utils.checkNull(text, "Text");
        Utils.checkTextMaximumLength(-1, text.length(), maximumLength);

        try {
            if (!text.equals(this.getText())) {
                setText(new StringReader(text));
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setText(final URL textURL) throws IOException {
        Utils.checkNull(textURL, "URL for text");

        try (InputStream inputStream = textURL.openStream()) {
            setText(new InputStreamReader(inputStream));
        }
    }

    public void setText(final File f) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(f.toPath())) {
            setText(reader);
        }
    }

    public void setText(final File f, final Charset cs) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(f.toPath(), cs)) {
            setText(reader);
        }
    }

    public void setText(final Reader textReader) throws IOException {
        Utils.checkNull(textReader, "Text reader");

        // Construct the paragraph list
        ArrayList<Paragraph> paragraphsLocal = new ArrayList<>();
        int characterCountLocal = 0;

        Paragraph paragraph = new Paragraph();
        int tabPosition = 0;
        int tabWidth = getTextAreaSkin().getTabWidth();

        int c = textReader.read();
        while (c != -1) {
            Utils.checkTextMaximumLength(characterCount, ++characterCountLocal, maximumLength);

            if (c == '\n') {
                paragraphsLocal.add(paragraph);
                paragraph = new Paragraph();
                tabPosition = 0;
            } else if (c == '\t' && expandTabs) {
                int spaces = tabWidth - (tabPosition % tabWidth);
                for (int i = 0; i < spaces; i++) {
                    Utils.checkTextMaximumLength(characterCount, ++characterCountLocal, maximumLength);
                    paragraph.append(' ');
                }
                tabPosition += spaces;
            } else {
                paragraph.append((char) c);
                tabPosition++;
            }

            c = textReader.read();
        }

        paragraphsLocal.add(paragraph);

        // Clear the edit history
        editHistory.clear();

        // Update content
        paragraphSequence.remove(0, paragraphSequence.getLength());

        for (int i = 0, n = paragraphsLocal.getLength(); i < n; i++) {
            paragraphSequence.add(paragraphsLocal.get(i));
        }
    }

    public void insertText(final CharSequence text, final int index) {
        insertText(text, index, true);
    }

    private void insertText(final CharSequence text, final int index, final boolean addToEditHistory) {
        Utils.checkNull(text, "Text to insert");
        Utils.checkTextMaximumLength(characterCount, text.length(), maximumLength);

        indexBoundsCheck("index", index, 0, characterCount);

        if (text.length() > 0) {
            // Insert the text
            int paragraphIndex = getParagraphAt(index);
            Paragraph paragraph = paragraphs.get(paragraphIndex);

            int characterOffset = index - paragraph.offset;
            int tabPosition = characterOffset;
            int tabWidth = getTextAreaSkin().getTabWidth();

            StringBuilder textBuilder = new StringBuilder();

            for (int i = 0, n = text.length(); i < n; i++) {
                char c = text.charAt(i);

                if (c == '\n') {
                    // Split paragraph at current offset
                    int count = paragraph.characters.length();

                    CharSequence trailingCharacters = paragraph.characters.subSequence(
                        characterOffset, count);
                    paragraph.removeText(characterOffset, count - characterOffset);
                    paragraph.insertText(textBuilder, characterOffset);

                    paragraph = new Paragraph();
                    paragraph.insertText(trailingCharacters, 0);
                    paragraphSequence.insert(paragraph, ++paragraphIndex);
                    characterOffset = 0;
                    tabPosition = 0;

                    textBuilder = new StringBuilder();
                } else if (c == '\t' && expandTabs) {
                    int spaces = tabWidth - (tabPosition % tabWidth);
                    for (int j = 0; j < spaces; j++) {
                        textBuilder.append(' ');
                    }
                    tabPosition += spaces;
                } else {
                    // Append character
                    textBuilder.append(c);
                    tabPosition++;
                }
            }

            paragraph.insertText(textBuilder, characterOffset);

            // Add an insert history item
            if (addToEditHistory) {
                editHistory.push(new InsertTextEdit(text, index));
            }
        }
    }

    public void removeText(final CharSpan charSelection) {
        removeText(charSelection.start, charSelection.length, true);
    }

    public void removeText(final int index, final int count) {
        removeText(index, count, true);
    }

    private void removeText(final int index, final int count, final boolean addToEditHistory) {
        if (index < 0 || index + count > characterCount) {
            throw new IndexOutOfBoundsException();
        }

        if (count > 0) {
            // Add a remove history item
            if (addToEditHistory) {
                editHistory.push(new RemoveTextEdit(index, count));
            }

            // Identify the leading and trailing paragraph indexes
            int endParagraphIndex = getParagraphAt(index + count);
            Paragraph endParagraph = paragraphs.get(endParagraphIndex);

            int beginParagraphIndex = endParagraphIndex;
            Paragraph beginParagraph = endParagraph;

            while (beginParagraph.offset > index) {
                beginParagraph = paragraphs.get(--beginParagraphIndex);
            }

            if (beginParagraphIndex == endParagraphIndex) {
                // The removal affects only a single paragraph
                beginParagraph.removeText(index - beginParagraph.offset, count);
            } else {
                // The removal spans paragraphs; remove any intervening paragraphs and
                // merge the leading and trailing segments
                String leadingText = beginParagraph.characters.substring(0, index
                    - beginParagraph.offset);
                endParagraph.removeText(0, (index + count) - endParagraph.offset);
                paragraphSequence.remove(beginParagraphIndex, endParagraphIndex
                    - beginParagraphIndex);
                endParagraph.insertText(leadingText, 0);
            }
        }
    }

    private void updateParagraphOffsets(final int from, final int count) {
        if (count != 0) {
            for (int i = from, n = paragraphs.getLength(); i < n; i++) {
                Paragraph paragraph = paragraphs.get(i);
                paragraph.offset += count;
            }
        }
    }

    /**
     * @return The text area's paragraph sequence.
     */
    public ParagraphSequence getParagraphs() {
        return paragraphSequence;
    }

    /**
     * @return The index of the paragraph containing a given character index.
     *
     * @param index The character index to check ({@code [0..characterCount]}).
     */
    public int getParagraphAt(final int index) {
        indexBoundsCheck("index", index, 0, characterCount);

        int paragraphIndex = paragraphs.getLength() - 1;
        Paragraph paragraph = paragraphs.get(paragraphIndex);

        while (paragraph.offset > index) {
            paragraph = paragraphs.get(--paragraphIndex);
        }

        return paragraphIndex;
    }

    /**
     * @return The character at a given index.
     *
     * @param index The index of the character to fetch ({@code [0..characterCount-1]}).
     */
    public char getCharacterAt(final int index) {
        indexBoundsCheck("index", index, 0, characterCount - 1);

        int paragraphIndex = getParagraphAt(index);
        Paragraph paragraph = paragraphs.get(paragraphIndex);

        int characterOffset = index - paragraph.offset;

        return (characterOffset == paragraph.characters.length()) ? '\n'
            : paragraph.characters.charAt(characterOffset);
    }

    /**
     * @return The number of characters in the text area, including line break
     * characters.
     */
    public int getCharacterCount() {
        return characterCount;
    }

    /**
     * Places any selected text on the clipboard and deletes it from the text
     * input.
     */
    public void cut() {
        copy();
        removeText(selectionStart, selectionLength);
    }

    /**
     * Places any selected text on the clipboard.
     */
    public void copy() {
        // Copy selection to clipboard
        String selectedText = getSelectedText();

        if (selectedText.length() > 0) {
            LocalManifest clipboardContent = new LocalManifest();
            clipboardContent.putText(selectedText);
            Clipboard.setContent(clipboardContent);
        }
    }

    /**
     * Inserts text from the clipboard into the text input.
     */
    public void paste() {
        Manifest clipboardContent = Clipboard.getContent();

        if (clipboardContent != null && clipboardContent.containsText()) {
            // Paste the string representation of the content
            String text = null;
            try {
                text = clipboardContent.getText();
            } catch (IOException exception) {
                // No-op
            }

            if (text != null) {
                if ((characterCount + text.length()) > maximumLength) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    removeText(selectionStart, selectionLength);
                    insertText(text, selectionStart);
                }
            }
        }
    }

    public void undo() {
        if (editHistory.getDepth() > 0) {
            Edit edit = editHistory.pop();
            edit.undo();
        }
    }

    /**
     * @return The starting index of the selection.
     */
    public int getSelectionStart() {
        return selectionStart;
    }

    /**
     * @return The length of the selection; may be <code>0</code>.
     */
    public int getSelectionLength() {
        return selectionLength;
    }

    /**
     * Returns a span representing the current selection.
     *
     * @return A span containing the current selection. Both start and end
     * points are inclusive. Returns {@code null} if the selection is empty.
     */
    public Span getSelection() {
        return (selectionLength == 0) ? null : new Span(selectionStart, selectionStart
            + selectionLength - 1);
    }

    /**
     * Returns a character span (start, length) representing the current selection.
     *
     * @return A char span with the start and length values.
     */
    public CharSpan getCharSelection() {
        return new CharSpan(selectionStart, selectionLength);
    }

    /**
     * Sets the selection. The sum of the selection start and length must be
     * less than the length of the text area's content.
     *
     * @param newStart The starting index of the selection.
     * @param newLength The length of the selection.
     */
    public void setSelection(final int newStart, final int newLength) {
        int start = newStart;
        int length = newLength;

        Utils.checkNonNegative(length, "selectionLength");

        int composedTextLength = composedText != null ? (composedText.getEndIndex() - composedText.getBeginIndex()) : 0;
        Utils.checkIndexBounds(start, length, 0, characterCount + composedTextLength);

        int previousSelectionStart = selectionStart;
        int previousSelectionLength = selectionLength;

        if (previousSelectionStart != start || previousSelectionLength != length) {
            selectionStart = start;
            selectionLength = length;

            textAreaSelectionListeners.selectionChanged(this, previousSelectionStart,
                previousSelectionLength);
        }
    }

    /**
     * Sets the selection.
     *
     * @param selection The new span of text to select.
     * @see #setSelection(int, int)
     */
    public final void setSelection(final Span selection) {
        Utils.checkNull(selection, "selection span");

        setSelection(selection.normalStart(), (int) selection.getLength());
    }

    /**
     * Sets the selection.
     *
     * @param selection The character span (start and length) for the selection.
     * @see #setSelection(int, int)
     * @throws IllegalArgumentException if the character span is {@code null}.
     */
    public final void setSelection(final CharSpan selection) {
        Utils.checkNull(selection, "selection");

        setSelection(selection.start, selection.length);
    }

    /**
     * Selects all text.
     */
    public void selectAll() {
        setSelection(0, characterCount);
    }

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        setSelection(0, 0);
    }

    /**
     * Returns the selected text.
     *
     * @return A string containing a copy of the selected text.
     */
    public String getSelectedText() {
        return getText(selectionStart, selectionStart + selectionLength);
    }

    /**
     * @return The maximum length of the text area's text content.
     */
    public int getMaximumLength() {
        return maximumLength;
    }

    /**
     * Sets the maximum length of the text area's text content.
     *
     * @param newMaximum The maximum length of the text area's text content.
     */
    public void setMaximumLength(final int newMaximum) {
        Utils.checkNonNegative(newMaximum, "maximumLength");

        int previousMaximumLength = maximumLength;

        if (previousMaximumLength != newMaximum) {
            maximumLength = newMaximum;

            // Truncate the text, if necessary
            if (characterCount > maximumLength) {
                removeText(maximumLength, characterCount - maximumLength);
            }

            // Fire change events
            textAreaListeners.maximumLengthChanged(this, previousMaximumLength);
        }
    }

    /**
     * @return The text area's editable flag.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets the text area's editable flag.
     *
     * @param editableValue Whether or not the text should now be editable.
     */
    public void setEditable(final boolean editableValue) {
        if (editable != editableValue) {
            if (!editableValue) {
                if (isFocused()) {
                    clearFocus();
                }
            }

            editable = editableValue;

            textAreaListeners.editableChanged(this);
        }
    }

    /**
     * Returns the text area's text key.
     *
     * @return The text key, or {@code null} if no text key is set.
     */
    public String getTextKey() {
        return textKey;
    }

    /**
     * Sets the text area's text key.
     *
     * @param newTextKey The text key, or {@code null} to clear the binding.
     */
    public void setTextKey(final String newTextKey) {
        String previousTextKey = textKey;

        if (previousTextKey != newTextKey) {
            textKey = newTextKey;
            textAreaBindingListeners.textKeyChanged(this, previousTextKey);
        }
    }

    public BindType getTextBindType() {
        return textBindType;
    }

    public void setTextBindType(final BindType newTextBindType) {
        Utils.checkNull(newTextBindType, "textBindType");

        BindType previousTextBindType = textBindType;

        if (previousTextBindType != newTextBindType) {
            textBindType = newTextBindType;
            textAreaBindingListeners.textBindTypeChanged(this, previousTextBindType);
        }
    }

    public TextBindMapping getTextBindMapping() {
        return textBindMapping;
    }

    public void setTextBindMapping(final TextBindMapping newTextBindMapping) {
        TextBindMapping previousTextBindMapping = textBindMapping;

        if (previousTextBindMapping != newTextBindMapping) {
            textBindMapping = newTextBindMapping;
            textAreaBindingListeners.textBindMappingChanged(this, previousTextBindMapping);
        }
    }

    @Override
    public void load(final Object context) {
        if (textKey != null && JSON.containsKey(context, textKey) && textBindType != BindType.STORE) {
            Object value = JSON.get(context, textKey);

            if (textBindMapping == null) {
                value = (value == null) ? "" : value.toString();
            } else {
                value = textBindMapping.toString(value);
            }

            setText((String) value);
        }
    }

    @Override
    public void store(final Object context) {
        if (textKey != null && textBindType != BindType.LOAD) {
            String text = getText();
            JSON.put(context, textKey,
                (textBindMapping == null) ? text : textBindMapping.valueOf(text));
        }
    }

    @Override
    public void clear() {
        if (textKey != null) {
            setText("");
        }
    }

    public boolean getExpandTabs() {
        return expandTabs;
    }

    /**
     * Sets whether tab characters (<code>\t</code>) are expanded to an
     * appropriate number of spaces during {@link #setText} and
     * {@link #insertText} operations.
     *
     * @param expandValue <code>true</code> to replace tab characters with space
     * characters (depending on the setting of the
     * {@link TextArea.Skin#getTabWidth} value) or <code>false</code> to leave
     * tabs alone. Note: this only affects tabs encountered during program
     * operations; tabs entered via the keyboard by the user are always
     * expanded, regardless of this setting.
     */
    public void setExpandTabs(final boolean expandValue) {
        expandTabs = expandValue;
    }

    public int getInsertionPoint(final int x, final int y) {
        return getTextAreaSkin().getInsertionPoint(x, y);
    }

    public int getNextInsertionPoint(final int x, final int from, final ScrollDirection direction) {
        return getTextAreaSkin().getNextInsertionPoint(x, from, direction);
    }

    public int getRowAt(final int index) {
        return getTextAreaSkin().getRowAt(index);
    }

    public int getRowOffset(final int index) {
        return getTextAreaSkin().getRowOffset(index);
    }

    public int getRowLength(final int index) {
        return getTextAreaSkin().getRowLength(index);
    }

    public CharSequence getRowCharacters(final int index) {
        TextArea.Skin textAreaSkin = getTextAreaSkin();
        int offset = textAreaSkin.getRowOffset(index);
        int length = textAreaSkin.getRowLength(index);
        return getCharacters(offset, offset + length);
    }

    public int getRowCount() {
        return getTextAreaSkin().getRowCount();
    }

    public Bounds getCharacterBounds(final int index) {
        // We need to validate in case we get called from user-code after a user-code
        // initiated modification, but before another layout has run.
        validate();
        return getTextAreaSkin().getCharacterBounds(index);
    }

    public ListenerList<TextAreaListener> getTextAreaListeners() {
        return textAreaListeners;
    }

    public ListenerList<TextAreaContentListener> getTextAreaContentListeners() {
        return textAreaContentListeners;
    }

    public ListenerList<TextAreaSelectionListener> getTextAreaSelectionListeners() {
        return textAreaSelectionListeners;
    }

    public ListenerList<TextAreaBindingListener> getTextAreaBindingListeners() {
        return textAreaBindingListeners;
    }
}
