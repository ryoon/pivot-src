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
package org.apache.pivot.charts.content;

import org.apache.pivot.collections.ArrayList;

/**
 * Represents series data for value chart views.
 *
 * <p> Essentially just a list, but with a name that can be displayed
 * in the chart.
 *
 * @param <T> The type of value contained in this data.
 */
public class ValueSeries<T> extends ArrayList<T> {
    private static final long serialVersionUID = 301207354854079022L;

    private String name = null;

    public ValueSeries() {
        this(null);
    }

    public ValueSeries(final String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
