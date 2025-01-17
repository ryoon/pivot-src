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
 * Enumeration representing a focus traversal direction.
 */
public enum FocusTraversalDirection {
    /** Traverse the focusable elements in a forward direction, that is,
     * left-to-right and top-to-bottom. */
    FORWARD,
    /** Traverse the focusable elements in a backward direction, that is,
      * bottom-to-top, right-to-left. */
    BACKWARD
}
