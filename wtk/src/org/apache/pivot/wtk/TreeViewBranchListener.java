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

import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.util.Vote;

/**
 * Tree view branch listener interface.
 */
public interface TreeViewBranchListener {
    /**
     * Tree view branch listener adapter.
     */
    public static class Adapter implements TreeViewBranchListener {
        @Override
        public void branchExpanded(TreeView treeView, Path path) {
            // empty block
        }

        @Override
        public void branchCollapsed(TreeView treeView, Path path) {
            // empty block
        }

        @Override
        public Vote previewBranchExpandedChange(TreeView treeView, Path path) {
            return Vote.APPROVE;
        }

        @Override
        public void branchExpandedChangeVetoed(TreeView treeView, Path path, Vote reason) {
            // empty block
        }
    }

    /**
     * Called when a tree node is expanded. This event can be used to perform
     * lazy loading of tree node data.
     *
     * @param treeView The source of the event.
     * @param path The path of the node that was shown.
     */
    public void branchExpanded(TreeView treeView, Path path);

    /**
     * Called when a tree node is collapsed.
     *
     * @param treeView The source of the event.
     * @param path The path of the node that was collapsed.
     */
    public void branchCollapsed(TreeView treeView, Path path);

    /**
     * Called before a tree node is expanded or collapsed to allow the application
     * or the skin to refuse the operation.
     *
     * @param treeView The source of the event.
     * @param path The path of the node about to be collapsed or expanded.
     */
    public Vote previewBranchExpandedChange(TreeView treeView, Path path);

    /**
     * Called when the {@link #previewBranchExpandedChange previewBranchExpandedChange()}
     * tally produces anything but a {@link Vote#APPROVE} result (to say NOT to expand or
     * collapse the branch).  The application or skin can reverse any GUI changes that may
     * have happened in the preview method.
     *
     * @param treeView The source of the event.
     * @param path The path of the node whose state will remain the same.
     * @param reason The tallied vote result that caused the veto.
     */
    public void branchExpandedChangeVetoed(TreeView treeView, Path path, Vote reason);

}
