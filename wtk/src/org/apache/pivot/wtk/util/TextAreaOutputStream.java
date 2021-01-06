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
package org.apache.pivot.wtk.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.TextArea;

/**
 * Creates an {@link OutputStream} that outputs to a {@link TextArea}
 * (in the EDT thread, using callbacks) for display.
 * <p> Can be used with the {@link org.apache.pivot.util.Console} class for output (using the
 * {@link #toPrintStream} method).
 */
public final class TextAreaOutputStream extends OutputStream {
    /** The TextArea we are going to stream to. */
    private TextArea textArea;

    /** Default line buffer size (can be overridden through a constructor). */
    private static final int DEFAULT_BUFFER_SIZE = 256;

    /** Buffer size to use for incoming lines of text. */
    private int lineBufferSize;

    /** The charset to use for converting incoming bytes to characters. */
    private Charset incomingCharset;

    /** The buffered line for this stream. */
    private ByteArrayOutputStream lineBuffer;

    /**
     * Simple constructor given the {@link TextArea} to stream to.
     *
     * @param textAreaToUse The TextArea to use for output.
     */
    public TextAreaOutputStream(final TextArea textAreaToUse) {
        this(textAreaToUse, null, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructor given the {@link TextArea} to stream to, and the
     * non-default line buffer size to use.
     *
     * @param textAreaToUse The TextArea to use for output.
     * @param lineBufferSizeToUse The non-default size for the input line buffer.
     */
    public TextAreaOutputStream(final TextArea textAreaToUse, final int lineBufferSizeToUse) {
        this(textAreaToUse, null, lineBufferSizeToUse);
    }

    /**
     * Constructor given the {@link TextArea} to stream to, and the charset to use
     * for decoding the incoming bytes into characters.
     *
     * @param textAreaToUse The TextArea to use for output.
     * @param charsetToUse The charset used to convert incoming bytes to characters
     * (can be {@code null} to use the platform standard charset).
     */
    public TextAreaOutputStream(final TextArea textAreaToUse, final Charset charsetToUse) {
        this(textAreaToUse, charsetToUse, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructor given the {@link TextArea} to stream to, the charset to use
     * for decoding the incoming bytes into characters, and the line buffer size to use.
     *
     * @param textAreaToUse The TextArea to use for output.
     * @param charsetToUse The charset used to convert incoming bytes to characters
     * (can be {@code null} to use the platform standard charset).
     * @param lineBufferSizeToUse The size for the input line buffer.
     */
    public TextAreaOutputStream(final TextArea textAreaToUse, final Charset charsetToUse,
        final int lineBufferSizeToUse) {
        this.textArea        = textAreaToUse;
        this.incomingCharset = (charsetToUse == null) ? Charset.defaultCharset() : charsetToUse;
        this.lineBufferSize  = lineBufferSizeToUse;
        this.lineBuffer      = new ByteArrayOutputStream(lineBufferSize);
    }

    /**
     * @throws IOException if this stream is already closed.
     */
    private void checkIfOpen() throws IOException {
        if (textArea == null || lineBuffer == null) {
            throw new IOException("TextAreaOutputStream is closed.");
        }
    }

    /**
     * Flush the (byte) line buffer if there is anything cached.
     * @param addNewLine Add a newline ('\n') character after any buffered text.
     */
    private void flushLineBuffer(final boolean addNewLine) {
	final String text;

        if (lineBuffer.size() > 0) {
            byte[] bytes = lineBuffer.toByteArray();
            text = new String(bytes, incomingCharset);
            lineBuffer.reset();
        } else {
            text = "";
        }

        // Do the actual text manipulation (including scrolling) on the event thread
        if (!text.isEmpty() || addNewLine) {
            ApplicationContext.queueCallback(() -> {
                int length    = textArea.getCharacterCount();
                int newLength = length;

                if (!text.isEmpty()) {
                    textArea.insertText(text, length);
                    newLength += text.length();
                }

                if (addNewLine) {
                    textArea.insertText("\n", newLength++);
                }

                Bounds lastCharBounds = textArea.getCharacterBounds(newLength);
                textArea.scrollAreaToVisible(lastCharBounds);
            });
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        this.textArea        = null;
        this.incomingCharset = null;
        this.lineBuffer      = null;
    }

    @Override
    public void flush() throws IOException {
        checkIfOpen();
        flushLineBuffer(false);
    }

    @Override
    public void write(final int b) throws IOException {
        if (b == '\n') {
            flushLineBuffer(true);
        } else if (b != '\r') {
            lineBuffer.write(b);
        }
    }

    /**
     * @return A new {@link PrintStream} using this object as the basis (and the
     * same charset specified by one of the constructors).
     */
    public PrintStream toPrintStream() {
        try {
            return new PrintStream(this, false, incomingCharset.name());
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Impossible unsupported encoding error!", uee);
        }
    }

}
