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
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.text.Paragraph;

class TextPaneSkinParagraphView extends TextPaneSkinBlockView {

    private static final int PARAGRAPH_TERMINATOR_WIDTH = 4;

    private static class Row {
        public int x = 0;
        public int y = 0;
        public int width = 0;
        public int height = 0;
        public ArrayList<RowSegment> rowSegments = new ArrayList<>();
    }

    private static class RowSegment {
        public TextPaneSkinNodeView nodeView;
        public int offset;

        RowSegment(final TextPaneSkinNodeView nodeViewValue, final int offsetValue) {
            this.nodeView = nodeViewValue;
            this.offset = offsetValue;
        }
    }

    private ArrayList<Row> rows = null;
    private Bounds terminatorBounds = Bounds.EMPTY;

    TextPaneSkinParagraphView(final TextPaneSkin textPaneSkin, final Paragraph paragraph) {
        super(textPaneSkin, paragraph);
    }

    @Override
    public void invalidateUpTree() {
        super.invalidateUpTree();
        terminatorBounds = null;
    }

    @Override
    protected void childLayout(final int breakWidth) {
        // Break the views into multiple rows

        Paragraph paragraph = (Paragraph) getNode();
        rows = new ArrayList<>();
        int offset = 0;

        ParagraphChildLayouter layouter = new ParagraphChildLayouter();
        Row row = new Row();
        for (TextPaneSkinNodeView nodeView : this) {
            nodeView.layout(Math.max(breakWidth - (row.width + PARAGRAPH_TERMINATOR_WIDTH), 0));

            int nodeViewWidth = nodeView.getWidth();

            if (row.width + nodeViewWidth > breakWidth && row.width > 0) {
                // The view is too big to fit in the remaining space,
                // and it is not the only view in this row
                rows.add(row);
                layouter.endRow(row);
                row = new Row();
                row.width = 0;
            }

            // Add the view to the row
            RowSegment segment = new RowSegment(nodeView, offset);
            row.rowSegments.add(segment);
            layouter.startSegment(segment);
            offset += nodeView.getCharacterCount();
            row.width += nodeViewWidth;

            // If the view was split into multiple views, add them to their own rows
            nodeView = getNext(nodeView);
            while (nodeView != null) {
                rows.add(row);
                layouter.endRow(row);
                row = new Row();

                nodeView.layout(breakWidth);

                segment = new RowSegment(nodeView, offset);
                row.rowSegments.add(segment);
                layouter.startSegment(segment);
                offset += nodeView.getCharacterCount();
                row.width = nodeView.getWidth();

                nodeView = getNext(nodeView);
            }
        }

        // Add the last row
        if (row.rowSegments.getLength() > 0) {
            rows.add(row);
            layouter.endRow(row);
        }

        // calculate paragraph width and adjust for alignment
        layouter.end(paragraph, rows);

        // Recalculate terminator bounds
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        LineMetrics lm = getTextPaneSkin().getFont().getLineMetrics("", 0, 0, fontRenderContext);
        int terminatorHeight = (int) Math.ceil(lm.getHeight());

        int terminatorY;
        if (getCharacterCount() == 1) {
            // The terminator is the only character in this paragraph
            terminatorY = 0;
        } else {
            terminatorY = layouter.rowY - terminatorHeight;
        }

        terminatorBounds = new Bounds(layouter.x, terminatorY, PARAGRAPH_TERMINATOR_WIDTH, terminatorHeight);

        // Ensure that the paragraph is visible even when empty
        layouter.paragraphWidth += terminatorBounds.width;
        int height = Math.max(layouter.rowY, terminatorBounds.height);

        setSize(layouter.paragraphWidth, height);
    }

    @Override
    public Dimensions getPreferredSize(final int breakWidth) {
        // Break the views into multiple rows

        Paragraph paragraph = (Paragraph) getNode();
        ArrayList<Row> rowsLocal = new ArrayList<>();
        int offset = 0;

        ParagraphChildLayouter layouter = new ParagraphChildLayouter();
        Row row = new Row();
        for (TextPaneSkinNodeView nodeView : this) {
            nodeView.layout(Math.max(breakWidth - (row.width + PARAGRAPH_TERMINATOR_WIDTH), 0));

            int nodeViewWidth = nodeView.getWidth();

            if (row.width + nodeViewWidth > breakWidth && row.width > 0) {
                // The view is too big to fit in the remaining space,
                // and it is not the only view in this row
                rowsLocal.add(row);
                layouter.endRow(row);
                row = new Row();
                row.width = 0;
            }

            // Add the view to the row
            RowSegment segment = new RowSegment(nodeView, offset);
            row.rowSegments.add(segment);
            layouter.startSegment(segment);
            offset += nodeView.getCharacterCount();
            row.width += nodeViewWidth;

            // If the view was split into multiple views, add them to their own rows
            nodeView = getNext(nodeView);
            while (nodeView != null) {
                rowsLocal.add(row);
                layouter.endRow(row);
                row = new Row();

                nodeView.layout(breakWidth);

                segment = new RowSegment(nodeView, offset);
                row.rowSegments.add(segment);
                layouter.startSegment(segment);
                offset += nodeView.getCharacterCount();
                row.width = nodeView.getWidth();

                nodeView = getNext(nodeView);
            }
        }

        // Add the last row
        if (row.rowSegments.getLength() > 0) {
            rowsLocal.add(row);
            layouter.endRow(row);
        }

        // calculate paragraph width and adjust for alignment
        layouter.end(paragraph, rowsLocal);

        // Recalculate terminator bounds
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        LineMetrics lm = getTextPaneSkin().getFont().getLineMetrics("", 0, 0, fontRenderContext);
        int terminatorHeight = (int) Math.ceil(lm.getHeight());

        int terminatorY;
        if (getCharacterCount() == 1) {
            // The terminator is the only character in this paragraph
            terminatorY = 0;
        } else {
            terminatorY = layouter.rowY - terminatorHeight;
        }

        Bounds terminatorBoundsLocal = new Bounds(layouter.x, terminatorY,
            PARAGRAPH_TERMINATOR_WIDTH, terminatorHeight);

        // Ensure that the paragraph is visible even when empty
        layouter.paragraphWidth += terminatorBoundsLocal.width;
        int height = Math.max(layouter.rowY, terminatorBoundsLocal.height);

        return new Dimensions(layouter.paragraphWidth, height);
    }

    private static TextPaneSkinNodeView getNext(final TextPaneSkinNodeView child) {
        // Using instanceof checks because there is no nice place in the hierarchy
        // to put an abstract method
        if (child instanceof TextPaneSkinSpanView) {
            return ((TextPaneSkinSpanView) child).getNext();
        } else if (child instanceof TextPaneSkinTextNodeView) {
            return ((TextPaneSkinTextNodeView) child).getNext();
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * The layout process and the line breaking process are interleaved, because
     * tab stops mean that we need to calculate X and width for each segment
     * before moving on to the next segment.
     */
    private static class ParagraphChildLayouter {
        // Add the row views to this view, lay out, and calculate height
        public int x = 0;
        public int paragraphWidth = 0;
        public int rowY = 0;

        public void startSegment(final RowSegment segment) {
            segment.nodeView.setLocation(x, segment.nodeView.getY());
            x += segment.nodeView.getWidth();
        }

        public void endRow(final Row row) {
            row.y = rowY;

            // Determine the row height
            for (RowSegment segment : row.rowSegments) {
                row.height = Math.max(row.height, segment.nodeView.getHeight());
            }

            // Determine row-segment Y values
            int rowBaseline = -1;
            for (RowSegment segment : row.rowSegments) {
                rowBaseline = Math.max(rowBaseline, segment.nodeView.getBaseline());
            }
            for (RowSegment segment : row.rowSegments) {
                int nodeViewBaseline = segment.nodeView.getBaseline();
                int y;
                if (rowBaseline == -1 || nodeViewBaseline == -1) {
                    // Align to bottom
                    y = row.height - segment.nodeView.getHeight();
                } else {
                    // Align to baseline
                    y = rowBaseline - nodeViewBaseline;
                }
                segment.nodeView.setLocation(segment.nodeView.getX(), y + rowY);
            }

            rowY += row.height;
        }

        public void end(final Paragraph paragraph, final ArrayList<Row> rows) {
            // calculate paragraph width
            for (Row row : rows) {
                paragraphWidth = Math.max(paragraphWidth, row.width);
            }

            // adjust for horizontal alignment
            for (Row row : rows) {
                switch (paragraph.getHorizontalAlignment()) {
                    case LEFT:
                        x = 0;
                        break;
                    case CENTER:
                        x = (paragraphWidth - row.width) / 2;
                        break;
                    case RIGHT:
                        x = paragraphWidth - row.width;
                        break;
                    default:
                        throw new IllegalStateException();
                }
                for (RowSegment segment : row.rowSegments) {
                    segment.nodeView.setLocation(x, segment.nodeView.getY());
                    x += segment.nodeView.getWidth();
                }
            }
        }
    }

    @Override
    protected void setSkinLocation(final int skinX, final int skinY) {
        super.setSkinLocation(skinX, skinY);
        for (int i = 0, n = rows.getLength(); i < n; i++) {
            Row row = rows.get(i);
            for (RowSegment segment : row.rowSegments) {
                segment.nodeView.setSkinLocation(skinX + segment.nodeView.getX(),
                    skinY + segment.nodeView.getY());
            }
        }
    }

    @Override
    public void paint(final Graphics2D graphics) {
        // The default paint() method paints the document children, but because of row-splitting,
        // the children we want to paint are not the same.

        // Determine the paint bounds
        Bounds paintBounds = new Bounds(getSize());
        Rectangle clipBounds = graphics.getClipBounds();
        if (clipBounds != null) {
            paintBounds = paintBounds.intersect(clipBounds);
        }

        for (int i = 0, n = rows.getLength(); i < n; i++) {
            Row row = rows.get(i);
            for (RowSegment segment : row.rowSegments) {
                paintChild(graphics, paintBounds, segment.nodeView);
            }
        }
    }

    @Override
    public int getInsertionPoint(final int x, final int y) {
        int offset = -1;

        int n = rows.getLength();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                Row row = rows.get(i);

                if (y >= row.y && y < row.y + row.height) {
                    if (x < row.x) {
                        RowSegment firstNodeSegment = row.rowSegments.get(0);
                        offset = firstNodeSegment.offset;
                    } else if (x > row.x + row.width - 1) {
                        RowSegment lastNodeSegment = row.rowSegments.get(row.rowSegments.getLength() - 1);
                        offset = lastNodeSegment.offset
                            + lastNodeSegment.nodeView.getCharacterCount();

                        if (offset < getCharacterCount() - 1) {
                            offset--;
                        }
                    } else {
                        for (RowSegment segment : row.rowSegments) {
                            Bounds nodeViewBounds = segment.nodeView.getBounds();

                            if (nodeViewBounds.contains(x, y)) {
                                offset = segment.nodeView.getInsertionPoint(
                                    x - segment.nodeView.getX(), y - segment.nodeView.getY())
                                    + segment.offset;
                                break;
                            }
                        }
                    }
                }

                if (offset != -1) {
                    break;
                }
            }
        } else {
            offset = getCharacterCount() - 1;
        }

        return offset;
    }

    @Override
    public int getNextInsertionPoint(final int x, final int from, final TextPane.ScrollDirection direction) {
        int offset = -1;

        int n = rows.getLength();
        if (n == 0 && from == -1) {
            // There are no rows; select the terminator character
            offset = 0;
        } else {
            int i;
            if (from == -1) {
                i = (direction == TextPane.ScrollDirection.DOWN) ? -1 : rows.getLength();
            } else {
                // Find the row that contains offset
                if (from == getCharacterCount() - 1) {
                    i = rows.getLength() - 1;
                } else {
                    i = 0;
                    while (i < n) {
                        Row row = rows.get(i);
                        RowSegment firstNodeSegment = row.rowSegments.get(0);
                        RowSegment lastNodeSegment = row.rowSegments.get(row.rowSegments.getLength() - 1);
                        if (from >= firstNodeSegment.offset
                            && from < lastNodeSegment.offset
                                + lastNodeSegment.nodeView.getCharacterCount()) {
                            break;
                        }

                        i++;
                    }
                }
            }

            // Move to the next or previous row
            if (direction == TextPane.ScrollDirection.DOWN) {
                i++;
            } else {
                i--;
            }

            if (i >= 0 && i < n) {
                // Find the node view that contains x and get the insertion point from it
                Row row = rows.get(i);

                for (RowSegment segment : row.rowSegments) {
                    Bounds bounds = segment.nodeView.getBounds();
                    if (bounds.containsX(x)) {
                        offset = segment.nodeView.getNextInsertionPoint(
                            x - segment.nodeView.getX(), -1, direction) + segment.offset;
                        break;
                    }
                }

                if (offset == -1) {
                    // No node view contained the x position; move to the end of the row
                    RowSegment lastNodeSegment = row.rowSegments.get(row.rowSegments.getLength() - 1);
                    offset = lastNodeSegment.offset + lastNodeSegment.nodeView.getCharacterCount();

                    if (offset < getCharacterCount() - 1) {
                        offset--;
                    }
                }
            }
        }

        return offset;
    }

    @Override
    public int getRowAt(final int offset) {
        int rowIndex = -1;

        if (offset == getCharacterCount() - 1) {
            rowIndex = (rows.getLength() == 0) ? 0 : rows.getLength() - 1;
        } else {
            for (int i = 0, n = rows.getLength(); i < n; i++) {
                Row row = rows.get(i);
                RowSegment firstNodeSegment = row.rowSegments.get(0);
                RowSegment lastNodeSegment = row.rowSegments.get(row.rowSegments.getLength() - 1);

                if (offset >= firstNodeSegment.offset
                    && offset < lastNodeSegment.offset
                        + lastNodeSegment.nodeView.getCharacterCount()) {
                    rowIndex = i;
                    break;
                }
            }
        }

        return rowIndex;
    }

    @Override
    public int getRowCount() {
        return Math.max(rows.getLength(), 1);
    }

    @Override
    public Bounds getCharacterBounds(final int offset) {
        Bounds characterBounds = null;
        // This is really ugly, but *sometimes* during caret calculations we don't
        // want the terminator size here because it includes the composed text width.
        TextPaneSkin textPaneSkin = getTextPaneSkin();
        if (offset >= getCharacterCount() - 1 && (!textPaneSkin.getDoingCaretCalculations() || rows.getLength() == 0)) {
            characterBounds = terminatorBounds;
        } else {
            if (rows != null) {
                for (int i = 0, n = rows.getLength(); i < n; i++) {
                    Row row = rows.get(i);
                    for (RowSegment segment : row.rowSegments) {
                        int nodeViewOffset = segment.offset;
                        int characterCount = segment.nodeView.getCharacterCount();

                        if (offset >= nodeViewOffset && offset < nodeViewOffset + characterCount) {
                            characterBounds = segment.nodeView.getCharacterBounds(offset - nodeViewOffset);

                            if (characterBounds != null) {
                                characterBounds = characterBounds.translate(segment.nodeView.getLocation());
                            }

                            break;
                        }
                    }
                }
            }

            if (characterBounds != null) {
                characterBounds = characterBounds.intersect(getSize());
            }
        }

        return characterBounds;
    }

}
