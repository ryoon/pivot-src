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
package org.apache.pivot.serialization;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;


/**
 * This is a {@link Reader} that can be instantiated inline with any other
 * {@code Reader} to provide macro capabilities.
 * <p> We recognize <code>#define <i>NAME value</i></code> as definitions
 * as well as <code>#undef <i>NAME</i></code> to remove a previous definition.
 * <p> The macro name must correspond to the Unicode naming conventions (see
 * {@link Character#isUnicodeIdentifierStart} and {@link Character#isUnicodeIdentifierPart}).
 * <p> Macro substitutions are recognized as <code>${<i>NAME</i>}</code> anywhere
 * in the underlying stream. Nested macros are supported, and are expanded at the
 * point of definition, if defined, or at the point of expansion if defined later.
 */
public final class MacroReader extends Reader {
    /** The input reader we are mapping. */
    private Reader in;

    /** The map of our defined variables and their values. */
    private Map<String, String> variableMap = new HashMap<>();

    /** The lookahead queue, set either by one-character lookahead (such as
     * while recognizing "$NAME") or from macro expansion.
     */
    private Queue<Integer> lookaheadQueue = new ArrayDeque<>();

    /** The previous character read. */
    private int lastCh = -1;

    /**
     * Construct a new macro reader on top of the given reader.
     * @param reader The input reader we are going to wrap.
     */
    public MacroReader(final Reader reader) {
        this.in = reader;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    /**
     * Add the given character to the lookahead queue for reprocessing.
     * @param ch The character to queue.
     */
    private void queue(final int ch) {
        if (ch != -1) {
            lookaheadQueue.add(ch);
        }
    }

    /**
     * Add all the characters of the given string to the lookahead queue
     * for reprocessing.
     * @param str The string to be queued up again.
     */
    private void queue(final String str) {
        for (int i = 0; i < str.length(); i++) {
            lookaheadQueue.add((int) str.charAt(i));
        }
    }

    /**
     * Parse out the next word in the stream (according to Unicode
     * Identifier semantics) as the macro name, skipping leading whitespace.
     * @return The next parsed "word" string.
     * @throws IOException for errors reading the stream.
     */
    private String getNextWord() throws IOException {
        StringBuilder buf = new StringBuilder();
        int ch;
        do {
            ch = getNextChar(true);
        } while (ch != -1 && Character.isWhitespace(ch));
        if (ch != -1) {
            buf.append((char) ch);
            while ((ch = getNextChar(true)) != -1
                   && ((buf.length() == 0 && Character.isUnicodeIdentifierStart(ch))
                    || (buf.length() > 0 && Character.isUnicodeIdentifierPart(ch)))) {
                buf.append((char) ch);
            }
            // Re-queue the character that terminated the word
            queue(ch);
        }
        return buf.toString();
    }

    private void skipToEol() throws IOException {
        int ch;
        do {
            ch = getNextChar(true);
        } while (ch != -1 && ch != '\n');
    }

    /**
     * Get the next character in the input stream, either from the
     * {@link #lookaheadQueue} if anything is queued, or by reading
     * from the underlying {@link Reader}.
     * <p> This is the heart of the processing that handles both
     * macro definition and expansion.
     * @param   handleMacros   set to {@code false} only when
     *                         invoking this method recursively
     *                         to ignore unknown macro commands
     *                         or undefined macros
     * @return  The next character in the stream.
     * @throws IOException if there are errors reading the stream.
     */
    private int getNextChar(final boolean handleMacros) throws IOException {
        int ret = -1;
        if (!lookaheadQueue.isEmpty()) {
            ret = lookaheadQueue.poll().intValue();
        } else {
            ret = in.read();
        }
        // Check for macro define or undefine (starting with "#"
        // at the beginning of a line) (unless we're recursing to
        // skip an unknown declaration keyword).
        if (ret == '#' && lastCh == '\n' && handleMacros) {
            String keyword = getNextWord();
            if (keyword.equalsIgnoreCase("undef")) {
                String name = getNextWord();
                skipToEol();
                variableMap.remove(name);
                return getNextChar(true);
            } else if (!keyword.equalsIgnoreCase("define")) {
                // Basically ignore any commands we don't understand
                // by simply queueing the text back to be read again
                // but with the flag set to ignore this command (so
                // we don't get into infinite recursion!)
                queue(ret);
                queue(keyword);
                queue(' ');
                return getNextChar(false);
            }
            // Define a macro
            String name = getNextWord();
            StringBuilder buf = new StringBuilder();
            int ch;
            do {
                ch = getNextChar(true);
            } while (ch != -1 && Character.isWhitespace(ch) && ch != '\\' && ch != '\n');
            queue(ch);
            do {
                while ((ch = getNextChar(true)) != -1 && ch != '\\' && ch != '\n') {
                    buf.append((char) ch);
                }
                // Check for line continuation character
                if (ch == '\\') {
                    int next = getNextChar(true);
                    if (next == '\n') {
                        buf.append((char) next);
                    } else {
                        buf.append((char) ch);
                        buf.append((char) next);
                    }
                }
            } while (ch != -1 && ch != '\n');
            variableMap.put(name, buf.toString());
            return getNextChar(true);
        } else if (ret == '$' && handleMacros) {
            // Check for macro expansion
            // Note: this allows for nested expansion
            int next = getNextChar(true);
            if (next == '{') {
                // Beginning of macro expansion
                StringBuilder buf = new StringBuilder();
                int ch;
                while ((ch = getNextChar(true)) != -1 && ch != '}') {
                    buf.append((char) ch);
                }
                String expansion = variableMap.get(buf.toString());
                if (expansion == null) {
                    queue(ret);
                    queue(next);
                    queue(buf.toString());
                    queue(ch);
                    ret = getNextChar(false);
                } else {
                    queue(expansion);
                    ret = getNextChar(true);
                }
            } else {
                queue(next);
            }
        }
        return (lastCh = ret);
    }

    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        int read = -1;
        for (int i = 0; i < len; i++) {
            int ch = getNextChar(true);
            if (ch == -1) {
                break;
            }
            read = i;
            cbuf[off + i] = (char) ch;
        }
        return (read == -1) ? read : read + 1;
    }

}
