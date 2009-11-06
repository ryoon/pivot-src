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
package org.apache.pivot.wtk.skin;

import java.awt.Graphics2D;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FlowPane;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;

/**
 * Flow pane skin.
 */
public class FlowPaneSkin extends ContainerSkin {
    private HorizontalAlignment alignment = HorizontalAlignment.LEFT;
    private Insets padding = Insets.NONE;
    private int horizontalSpacing = 2;
    private int verticalSpacing = 2;
    private boolean alignToBaseline = false;

    @Override
    public int getPreferredWidth(int height) {
        FlowPane flowPane = (FlowPane)getComponent();

        int preferredWidth = 0;

        // Preferred width is the sum of the preferred widths of all components
        // (height constraint is ignored)
        int j = 0;
        for (int i = 0, n = flowPane.getLength(); i < n; i++) {
            Component component = flowPane.get(i);

            if (component.isVisible()) {
                preferredWidth += component.getPreferredWidth();
                j++;
            }
        }

        // Include spacing
        if (j > 1) {
            preferredWidth += horizontalSpacing * (j - 1);
        }

        // Include left and right padding values
        preferredWidth += padding.left + padding.right;

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        FlowPane flowPane = (FlowPane)getComponent();

        int preferredHeight = 0;

        if (width == -1) {
            // Preferred height is the maximum preferred height of all components
            for (int i = 0, n = flowPane.getLength(); i < n; i++) {
                Component component = flowPane.get(i);

                if (component.isVisible()) {
                    preferredHeight = Math.max(preferredHeight,
                        component.getPreferredHeight());
                }
            }
        } else {
            int contentWidth = Math.max(width - (padding.left + padding.right), 0);

            // Break the components into multiple rows
            int rowCount = 0;

            int rowWidth = 0;
            int rowHeight = 0;
            for (int i = 0, n = flowPane.getLength(); i < n; i++) {
                Component component = flowPane.get(i);

                if (component.isVisible()) {
                    Dimensions componentSize = component.getPreferredSize();

                    if (rowWidth + componentSize.width > contentWidth
                        && rowWidth > 0) {
                        // The component is too big to fit in the remaining space,
                        // and it is not the only component in this row; wrap
                        preferredHeight += rowHeight;

                        rowCount++;
                        rowWidth = 0;
                        rowHeight = 0;
                    }

                    rowWidth += componentSize.width + horizontalSpacing;
                    rowHeight = Math.max(rowHeight, componentSize.height);
                }
            }

            // Add the last row
            if (rowHeight > 0) {
                preferredHeight += rowHeight;
                rowCount++;
            }

            // Include spacing
            if (rowCount > 0) {
                preferredHeight += verticalSpacing * (rowCount - 1);
            }
        }

        // Include top and bottom padding values
        preferredHeight += padding.top + padding.bottom;

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        FlowPane flowPane = (FlowPane)getComponent();

        int preferredWidth = 0;
        int preferredHeight = 0;

        int j = 0;
        for (int i = 0, n = flowPane.getLength(); i < n; i++) {
            Component component = flowPane.get(i);

            if (component.isVisible()) {
                Dimensions componentSize = component.getPreferredSize();
                preferredWidth += componentSize.width;
                preferredHeight = Math.max(preferredHeight, componentSize.height);
                j++;
            }
        }

        // Include horizontal spacing
        if (j > 1) {
            preferredWidth += horizontalSpacing * (j - 1);
        }

        // Include padding
        preferredWidth += padding.left + padding.right;
        preferredHeight += padding.top + padding.bottom;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width) {
        FlowPane flowPane = (FlowPane)getComponent();

        int baseline = -1;

        int contentWidth = Math.max(width - (padding.left + padding.right), 0);

        // Break the components into multiple rows, and calculate the baseline of the
        // first row
        int rowWidth = 0;
        for (int i = 0, n = flowPane.getLength(); i < n; i++) {
            Component component = flowPane.get(i);

            if (component.isVisible()) {
                Dimensions componentSize = component.getPreferredSize();

                if (rowWidth + componentSize.width > contentWidth && rowWidth > 0) {
                    // The component is too big to fit in the remaining space,
                    // and it is not the only component in this row; wrap
                    break;
                }

                baseline = Math.max(baseline, component.getBaseline(componentSize.width));
                rowWidth += componentSize.width + horizontalSpacing;
            }
        }

        // Include top padding value
        if (baseline != -1) {
            baseline += padding.top;
        }

        return baseline;
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);
        if (debugBaseline) {
            drawBaselineDebug(graphics);
        }
    }

    @Override
    public void layout() {
        FlowPane flowPane = (FlowPane)getComponent();
        int width = getWidth();
        int contentWidth = Math.max(width - (padding.left + padding.right), 0);

        // Break the components into multiple rows
        ArrayList<ArrayList<Component>> rows = new ArrayList<ArrayList<Component>>();

        ArrayList<Component> row = new ArrayList<Component>();
        int rowWidth = 0;

        for (int i = 0, n = flowPane.getLength(); i < n; i++) {
            Component component = flowPane.get(i);

            if (component.isVisible()) {
                Dimensions componentSize = component.getPreferredSize();
                component.setSize(componentSize);

                if (rowWidth + componentSize.width > contentWidth
                    && rowWidth > 0) {
                    // The component is too big to fit in the remaining space,
                    // and it is not the only component in this row
                    rows.add(row);
                    row = new ArrayList<Component>();
                    rowWidth = 0;
                }

                // Add the component to the row
                row.add(component);
                rowWidth += componentSize.width + horizontalSpacing;
            }
        }

        // Add the last row
        if (row.getLength() > 0) {
            rows.add(row);
        }

        // Lay out the rows
        int rowY = padding.top;

        for (int i = 0, n = rows.getLength(); i < n; i++) {
            row = rows.get(i);

            // Determine the row dimensions
            rowWidth = 0;
            int rowHeight = 0;
            int baseline = -1;
            for (Component component : row) {
                rowWidth += component.getWidth();
                rowHeight = Math.max(rowHeight, component.getHeight());
                baseline = Math.max(baseline, component.getBaseline(component.getWidth()));
            }

            rowWidth += horizontalSpacing * (row.getLength() - 1);

            int x = 0;
            switch (alignment) {
                case LEFT: {
                    x = padding.left;
                    break;
                }
                case CENTER: {
                    x = (width - rowWidth) / 2;
                    break;
                }
                case RIGHT: {
                    x = width - rowWidth - padding.right;
                    break;
                }
            }

            for (Component component : row) {
                int y;
                if (alignToBaseline && baseline != -1) {
                    // Align to baseline
                    y = baseline - component.getBaseline(component.getWidth());
                } else {
                    // Align to bottom
                    y = rowHeight - component.getHeight();
                }

                component.setLocation(x, y + rowY);
                x += (component.getWidth() + horizontalSpacing);
            }

            rowY += (rowHeight + verticalSpacing);
        }
    }

    public HorizontalAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(HorizontalAlignment alignment) {
        if (alignment == null) {
            throw new IllegalArgumentException("alignment is null.");
        }

        this.alignment = alignment;
        invalidateComponent();
    }

    public final void setAlignment(String alignment) {
        if (alignment == null) {
            throw new IllegalArgumentException("alignment is null.");
        }

        setAlignment(HorizontalAlignment.valueOf(alignment.toUpperCase()));
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    public final void setPadding(String padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(Insets.decode(padding));
    }

    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    public void setHorizontalSpacing(int horizontalSpacing) {
        if (horizontalSpacing < 0) {
            throw new IllegalArgumentException("horizontalSpacing is negative.");
        }
        this.horizontalSpacing = horizontalSpacing;
        invalidateComponent();
    }

    public final void setHorizontalSpacing(Number horizontalSpacing) {
        if (horizontalSpacing == null) {
            throw new IllegalArgumentException("horizontalSpacing is null.");
        }

        setHorizontalSpacing(horizontalSpacing.intValue());
    }

    public int getVerticalSpacing() {
        return verticalSpacing;
    }

    public void setVerticalSpacing(int verticalSpacing) {
        if (verticalSpacing < 0) {
            throw new IllegalArgumentException("verticalSpacing is negative.");
        }
        this.verticalSpacing = verticalSpacing;
        invalidateComponent();
    }

    public final void setVerticalSpacing(Number verticalSpacing) {
        if (verticalSpacing == null) {
            throw new IllegalArgumentException("verticalSpacing is null.");
        }

        setVerticalSpacing(verticalSpacing.intValue());
    }

    public boolean getAlignToBaseline() {
        return alignToBaseline;
    }

    public void setAlignToBaseline(boolean alignToBaseline) {
        this.alignToBaseline = alignToBaseline;
        invalidateComponent();
    }
}
