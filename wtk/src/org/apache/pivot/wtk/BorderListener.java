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

import org.apache.pivot.util.ListenerList;

/**
 * Border listener interface.
 */
public interface BorderListener {
    /**
     * Border listeners.
     */
    class Listeners extends ListenerList<BorderListener> implements BorderListener {
        @Override
        public void titleChanged(final Border border, final String previousTitle) {
            forEach(listener -> listener.titleChanged(border, previousTitle));
        }

        @Override
        public void contentChanged(final Border border, final Component previousContent) {
            forEach(listener -> listener.contentChanged(border, previousContent));
        }
    }

    /**
     * Border listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    class Adapter implements BorderListener {
        @Override
        public void titleChanged(final Border border, final String previousTitle) {
            // empty block
        }

        @Override
        public void contentChanged(final Border border, final Component previousContent) {
            // empty block
        }
    }

    /**
     * Called when a border's title has changed.
     *
     * @param border        The border component that has changed.
     * @param previousTitle The previous title for the border.
     */
    default void titleChanged(Border border, String previousTitle) {
    }

    /**
     * Called when a border's content component has changed.
     *
     * @param border          The border that has changed.
     * @param previousContent The previous content of the border.
     */
    default void contentChanged(Border border, Component previousContent) {
    }
}
