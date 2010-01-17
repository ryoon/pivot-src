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

/**
 * Label listener interface.
 */
public interface LabelListener {
    /**
     * Label listener adapter.
     */
    public static class Adapter implements LabelListener {
        public void textChanged(Label label, String previousText) {
        }

        public void textKeyChanged(Label label, String previousTextKey) {
        }
    }

    /**
     * Called when a label's text has changed.
     *
     * @param label
     * @param previousText
     */
    public void textChanged(Label label, String previousText);

    /**
     * Called when a label's text key has changed.
     *
     * @param label
     * @param previousTextKey
     */
    public void textKeyChanged(Label label, String previousTextKey);
}
