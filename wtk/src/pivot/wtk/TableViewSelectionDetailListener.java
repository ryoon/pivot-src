/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import pivot.collections.Sequence;

/**
 * <p>Table view selection detail listener interface.</p>
 *
 * @author gbrown
 */
public interface TableViewSelectionDetailListener {
    /**
     * Called when a range has been added to a table view's selection.
     *
     * @param tableView
     * @param rangeStart
     * @param rangeEnd
     */
    public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd);

    /**
     * Called when a range has been removed from a table view's selection.
     *
     * @param tableView
     * @param rangeStart
     * @param rangeEnd
     */
    public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd);

    /**
     * Called when a table view's selection state has been reset.
     *
     * @param tableView
     * @param previousSelectedRanges
     */
    public void selectionReset(TableView tableView, Sequence<Span> previousSelectedRanges);
}
