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
package org.apache.pivot.demos.tables;

import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class FixedColumnTable implements Application {
    private Window window = null;
    private TableView primaryTableView = null;
    private TableViewHeader primaryTableViewHeader = null;
    private TableView fixedTableView = null;
    private TableViewHeader fixedTableViewHeader = null;

    private boolean synchronizingSelection = false;

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "fixed_column_table.wtkx");
        primaryTableView = (TableView)wtkxSerializer.get("primaryTableView");
        primaryTableViewHeader = (TableViewHeader)wtkxSerializer.get("primaryTableViewHeader");
        fixedTableView = (TableView)wtkxSerializer.get("fixedTableView");
        fixedTableViewHeader = (TableViewHeader)wtkxSerializer.get("fixedTableViewHeader");

        // Keep selection state in sync
        primaryTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener() {
            @Override
            public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
                if (!synchronizingSelection) {
                    synchronizingSelection = true;
                    fixedTableView.addSelectedRange(rangeStart, rangeEnd);
                    synchronizingSelection = false;
                }
            }

            @Override
            public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
                if (!synchronizingSelection) {
                    synchronizingSelection = true;
                    fixedTableView.removeSelectedRange(rangeStart, rangeEnd);
                    synchronizingSelection = false;
                }
            }

            @Override
            public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
                if (!synchronizingSelection) {
                    synchronizingSelection = true;
                    fixedTableView.setSelectedRanges(tableView.getSelectedRanges());
                    synchronizingSelection = false;
                }
            }
        });

        fixedTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener() {
            @Override
            public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
                if (!synchronizingSelection) {
                    synchronizingSelection = true;
                    primaryTableView.addSelectedRange(rangeStart, rangeEnd);
                    synchronizingSelection = false;
                }
            }

            @Override
            public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
                if (!synchronizingSelection) {
                    synchronizingSelection = true;
                    primaryTableView.removeSelectedRange(rangeStart, rangeEnd);
                    synchronizingSelection = false;
                }
            }

            @Override
            public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
                if (!synchronizingSelection) {
                    synchronizingSelection = true;
                    primaryTableView.setSelectedRanges(tableView.getSelectedRanges());
                    synchronizingSelection = false;
                }
            }
        });

        // Keep header state in sync
        primaryTableViewHeader.getTableViewHeaderPressListeners().add(new TableView.SortHandler() {
            @Override
            public void headerPressed(TableViewHeader tableViewHeader, int index) {
                super.headerPressed(tableViewHeader, index);

                TableView.ColumnSequence columns = fixedTableView.getColumns();
                for (int i = 0, n = columns.getLength(); i < n; i++) {
                    TableView.Column column = columns.get(i);
                    column.setSortDirection((SortDirection)null);
                }
            }
        });

        fixedTableViewHeader.getTableViewHeaderPressListeners().add(new TableView.SortHandler() {
            @Override
            public void headerPressed(TableViewHeader tableViewHeader, int index) {
                super.headerPressed(tableViewHeader, index);

                TableView.ColumnSequence columns = primaryTableView.getColumns();
                for (int i = 0, n = columns.getLength(); i < n; i++) {
                    TableView.Column column = columns.get(i);
                    column.setSortDirection((SortDirection)null);
                }
            }
        });

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(FixedColumnTable.class, args);
    }
}
