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
package org.apache.pivot.charts;

import org.apache.pivot.wtk.Skin;

/**
 * Provides a mapping from a concrete component class to a skin class.
 */
public interface Provider {
    /**
     * Find the skin class associated with the given chart component.
     * @param componentClass Class of the chart component whose skin we need.
     * @return The skin class associated with the chart component.
     */
    Class<? extends Skin> getSkinClass(Class<? extends ChartView> componentClass);
}
