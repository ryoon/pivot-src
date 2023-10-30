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
package org.apache.pivot.wtk.text;

import org.apache.pivot.util.Utils;

/**
 * Abstract base class for list elements. <p> TODO Add indent and item spacing
 * properties.
 */
public abstract class List extends Block {
    /**
     * Element representing a list item.
     */
    public static class Item extends Element {
        /**
         * Default constructor.
         */
        public Item() {
            super();
        }

        /**
         * Copy constructor with option to copy the children also.
         *
         * @param item      Item to copy.
         * @param recursive Whether to copy the children as well.
         */
        public Item(final Item item, final boolean recursive) {
            super(item, recursive);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void insert(final Node node, final int index) {
            Utils.notInstanceOf("Child node", node, Block.class);

            super.insert(node, index);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Item duplicate(final boolean recursive) {
            return new Item(this, recursive);
        }
    }

    /**
     * Default constructor.
     */
    public List() {
        super();
    }

    /**
     * Copy constructor with option to copy the children.
     *
     * @param list      List element to copy.
     * @param recursive Option to copy the children as well.
     */
    public List(final List list, final boolean recursive) {
        super(list, recursive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert(final Node node, final int index) {
        Utils.notInstanceOf("Child node", node, Item.class);

        super.insert(node, index);
    }
}
