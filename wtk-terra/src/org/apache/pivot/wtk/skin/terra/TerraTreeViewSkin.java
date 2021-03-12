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
package org.apache.pivot.wtk.skin.terra;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.GeneralPath;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.util.ClassUtils;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Utils;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.Keyboard.Modifier;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeView.SelectMode;
import org.apache.pivot.wtk.TreeViewBranchListener;
import org.apache.pivot.wtk.TreeViewListener;
import org.apache.pivot.wtk.TreeViewNodeListener;
import org.apache.pivot.wtk.TreeViewNodeStateListener;
import org.apache.pivot.wtk.TreeViewSelectionListener;
import org.apache.pivot.wtk.skin.ComponentSkin;

/**
 * Tree view skin.
 */
public class TerraTreeViewSkin extends ComponentSkin implements TreeView.Skin, TreeViewListener,
    TreeViewBranchListener, TreeViewNodeListener, TreeViewNodeStateListener,
    TreeViewSelectionListener {

    /**
     * Node info visitor interface.
     */
    protected interface NodeInfoVisitor {
        /**
         * Visits the specified node info.
         *
         * @param nodeInfo The object to visit
         */
        void visit(NodeInfo nodeInfo);
    }

    /**
     * Iterates through the visible nodes. For callers who wish to know the path
     * of each visible node, using this iterator will be much more efficient
     * than manually iterating over the visible nodes and calling
     * {@code getPath()} on each node.
     */
    protected final class VisibleNodeIterator implements Iterator<NodeInfo> {
        private int index;
        private int end;

        private Path path = null;
        private NodeInfo previous = null;

        public VisibleNodeIterator() {
            this(0, visibleNodes.getLength() - 1);
        }

        /**
         * Creates a new visible node iterator that will iterate over a portion
         * of the visible nodes list (useful during painting).
         *
         * @param startIndex The start index, inclusive
         * @param endIndex The end index, inclusive
         */
        public VisibleNodeIterator(final int startIndex, final int endIndex) {
            if (startIndex < 0 || endIndex >= visibleNodes.getLength()) {
                throw new IndexOutOfBoundsException();
            }

            this.index = startIndex;
            this.end = endIndex;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            return (index <= end);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NodeInfo next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            NodeInfo next = visibleNodes.get(index++);

            if (path == null) {
                // First iteration
                path = next.getPath();
            } else if (next.parent == previous) {
                // Child of previous visible node
                path.add(0);
            } else {
                int n = path.getLength();
                while (next.parent != previous.parent) {
                    path.remove(--n, 1);
                    previous = previous.parent;
                }

                int tail = path.get(n - 1);
                path.update(n - 1, tail + 1);
            }

            previous = next;

            return next;
        }

        /**
         * This operation is not supported by this iterator.
         *
         * @throws UnsupportedOperationException always since this is unsupported.
         */
        @Override
        @UnsupportedOperation
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Gets the index of the node last returned by a call to {@link #next()}
         * , as seen in the current visible nodes list. Note that as branches
         * are expanded and collapsed, the row index of any given node in the
         * tree will change.
         *
         * @return The row index of the current node, or <code>-1</code> if
         * {@code next()} has not yet been called.
         */
        public int getRowIndex() {
            return (path == null ? -1 : index - 1);
        }

        /**
         * Gets the path of the node last returned by a call to {@link #next()}.
         *
         * @return The path to the node, or {@code null} if {@code next()} has
         * not yet been called.
         */
        public Path getPath() {
            return path;
        }
    }

    /**
     * An internal data structure that keeps track of skin-related metadata for
     * a tree node. The justification for the existence of this class lies in
     * the {@code visibleNodes} data structure, which is a flat list of nodes
     * that are visible at any given time. In this context, visible means that
     * their parent hierarchy is expanded, <b>not</b> that they are being
     * painted. This list, combined with {@code getNodeHeight()}, enables us to
     * quickly determine which nodes to paint given a graphics clip rect. It
     * also enables us to quickly traverse the tree view when handling key
     * events. <p> NOTE: some of this data is managed by {@code TreeView} and
     * cached here to provide further optimizations during painting and user
     * input.
     */
    private static class NodeInfo {
        // Core metadata
        final TreeView treeView;
        final BranchInfo parent;
        final Object data;
        final int depth;

        // Cached fields. Note that this is maintained as a bitmask in favor of
        // separate properties because it allows us to easily clear any cached
        // field for all nodes in one common method. See #clearField(byte)
        byte fields = 0;

        public static final byte HIGHLIGHTED_MASK = 1 << 0;
        public static final byte SELECTED_MASK = 1 << 1;
        public static final byte DISABLED_MASK = 1 << 2;
        public static final byte CHECKMARK_DISABLED_MASK = 1 << 3;
        public static final byte CHECK_STATE_CHECKED_MASK = 1 << 4;
        public static final byte CHECK_STATE_MIXED_MASK = 1 << 5;

        public static final byte CHECK_STATE_MASK = CHECK_STATE_CHECKED_MASK
            | CHECK_STATE_MIXED_MASK;

        public NodeInfo(final TreeView treeViewValue, final BranchInfo parentValue,
            final Object dataValue) {
            this.treeView = treeViewValue;
            this.parent = parentValue;
            this.data = dataValue;

            depth = (parent == null) ? 0 : parent.depth + 1;

            // Newly created nodes are guaranteed to not be selected or checked,
            // but they may be disabled or have their checkmarks disabled, so
            // we set those flags appropriately here.

            @SuppressWarnings("unchecked")
            Filter<Object> disabledNodeFilter = (Filter<Object>) treeView.getDisabledNodeFilter();
            if (disabledNodeFilter != null) {
                setDisabled(disabledNodeFilter.include(data));
            }

            @SuppressWarnings("unchecked")
            Filter<Object> disabledCheckmarkFilter = (Filter<Object>) treeView.getDisabledCheckmarkFilter();
            if (disabledCheckmarkFilter != null) {
                setCheckmarkDisabled(disabledCheckmarkFilter.include(data));
            }
        }

        @SuppressWarnings("unchecked")
        public static NodeInfo newInstance(final TreeView treeView, final BranchInfo parent, final Object data) {
            NodeInfo nodeInfo = null;

            if (data instanceof List<?>) {
                nodeInfo = new BranchInfo(treeView, parent, (List<Object>) data);
            } else {
                nodeInfo = new NodeInfo(treeView, parent, data);
            }

            return nodeInfo;
        }

        @SuppressWarnings("unchecked")
        public Path getPath() {
            Path path = Path.forDepth(depth);

            NodeInfo nodeInfo = this;

            while (nodeInfo.parent != null) {
                List<Object> parentData = (List<Object>) nodeInfo.parent.data;
                int index = parentData.indexOf(nodeInfo.data);
                path.insert(index, 0);

                nodeInfo = nodeInfo.parent;
            }

            return path;
        }

        public boolean isHighlighted() {
            return ((fields & HIGHLIGHTED_MASK) != 0);
        }

        public void setHighlighted(final boolean highlighted) {
            if (highlighted) {
                fields |= HIGHLIGHTED_MASK;
            } else {
                fields &= ~HIGHLIGHTED_MASK;
            }
        }

        public boolean isSelected() {
            return ((fields & SELECTED_MASK) != 0);
        }

        public void setSelected(final boolean selected) {
            if (selected) {
                fields |= SELECTED_MASK;
            } else {
                fields &= ~SELECTED_MASK;
            }
        }

        public boolean isDisabled() {
            return ((fields & DISABLED_MASK) != 0);
        }

        public void setDisabled(final boolean disabled) {
            if (disabled) {
                fields |= DISABLED_MASK;
            } else {
                fields &= ~DISABLED_MASK;
            }
        }

        public boolean isCheckmarkDisabled() {
            return ((fields & CHECKMARK_DISABLED_MASK) != 0);
        }

        public void setCheckmarkDisabled(final boolean checkmarkDisabled) {
            if (checkmarkDisabled) {
                fields |= CHECKMARK_DISABLED_MASK;
            } else {
                fields &= ~CHECKMARK_DISABLED_MASK;
            }
        }

        public TreeView.NodeCheckState getCheckState() {
            TreeView.NodeCheckState checkState;

            switch (fields & CHECK_STATE_MASK) {
                case CHECK_STATE_CHECKED_MASK:
                    checkState = TreeView.NodeCheckState.CHECKED;
                    break;
                case CHECK_STATE_MIXED_MASK:
                    checkState = TreeView.NodeCheckState.MIXED;
                    break;
                default:
                    checkState = TreeView.NodeCheckState.UNCHECKED;
                    break;
            }

            return checkState;
        }

        public boolean isChecked() {
            return ((fields & CHECK_STATE_CHECKED_MASK) != 0);
        }

        public void setCheckState(final TreeView.NodeCheckState checkState) {
            fields &= ~CHECK_STATE_MASK;

            switch (checkState) {
                case CHECKED:
                    fields |= CHECK_STATE_CHECKED_MASK;
                    break;
                case MIXED:
                    fields |= CHECK_STATE_MIXED_MASK;
                    break;
                case UNCHECKED:
                    break;
                default:
                    break;
            }
        }

        public void clearField(final byte mask) {
            fields &= ~mask;
        }

        @Override
        public String toString() {
            return ClassUtils.simpleToString(this);
        }
    }

    /**
     * An internal data structure that keeps track of skin-related metadata for
     * a tree branch.
     */
    private static final class BranchInfo extends NodeInfo {
        // Core skin metadata
        private List<NodeInfo> children = null;

        public static final byte EXPANDED_MASK = 1 << 6;

        public BranchInfo(final TreeView treeView, final BranchInfo parent, final List<Object> data) {
            super(treeView, parent, data);
        }

        /**
         * Loads this branch info's children. The children list is initialized
         * to {@code null} and loaded lazily to allow the skin to only create
         * {@code NodeInfo} objects for the nodes that it actually needs in
         * order to paint. Thus, it is the responsibility of the skin to check
         * if {@code children} is null and call {@code loadChildren()} if
         * necessary.
         */
        @SuppressWarnings("unchecked")
        public void loadChildren() {
            if (children == null || children.isEmpty()) {
                List<Object> dataLocal = (List<Object>) this.data;
                int count = dataLocal.getLength();

                children = new ArrayList<>(count);

                for (int i = 0; i < count; i++) {
                    Object nodeData = dataLocal.get(i);
                    NodeInfo childNodeInfo = NodeInfo.newInstance(treeView, this, nodeData);
                    children.add(childNodeInfo);
                }
            }
        }

        public boolean isExpanded() {
            return ((fields & EXPANDED_MASK) != 0);
        }

        public void setExpanded(final boolean expanded) {
            if (expanded) {
                fields |= EXPANDED_MASK;
            } else {
                fields &= ~EXPANDED_MASK;
            }
        }
    }

    private BranchInfo rootBranchInfo = null;
    private List<NodeInfo> visibleNodes = new ArrayList<>();

    private NodeInfo highlightedNode = null;
    private Path selectPath = null;

    // Styles
    private Font font;
    private Color color;
    private Color disabledColor;
    private Color backgroundColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;
    private Color highlightColor;
    private Color highlightBackgroundColor;
    private int spacing;
    private int indent;
    private boolean showHighlight;
    private boolean showBranchControls;
    private boolean showEmptyBranchControls;
    private Color branchControlColor;
    private Color branchControlSelectionColor;
    private Color branchControlInactiveSelectionColor;
    private Color gridColor;
    private boolean showGridLines;

    private boolean validateSelection = false;

    private static final int BRANCH_CONTROL_IMAGE_WIDTH = 8;
    private static final int BRANCH_CONTROL_IMAGE_HEIGHT = 8;
    private static final int VERTICAL_SPACING = 1;

    private static final Checkbox CHECKBOX = new Checkbox();
    private static final int CHECKBOX_VERTICAL_PADDING = 2;

    static {
        CHECKBOX.setSize(CHECKBOX.getPreferredSize());
        CHECKBOX.setTriState(true);
    }

    public TerraTreeViewSkin() {
    }

    /** @return the {@code TreeView} we are attached to. */
    public TreeView getTreeView() {
        return (TreeView) getComponent();
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        font = getThemeFont();

        setDefaultStyles();

        TreeView treeView = (TreeView) component;
        treeView.getTreeViewListeners().add(this);
        treeView.getTreeViewBranchListeners().add(this);
        treeView.getTreeViewNodeListeners().add(this);
        treeView.getTreeViewNodeStateListeners().add(this);
        treeView.getTreeViewSelectionListeners().add(this);

        treeDataChanged(treeView, null);
    }

    @Override
    public int getPreferredWidth(final int height) {
        TreeView treeView = getTreeView();
        TreeView.NodeRenderer nodeRenderer = treeView.getNodeRenderer();

        int preferredWidth = 0;

        VisibleNodeIterator visibleNodeIterator = new VisibleNodeIterator();
        while (visibleNodeIterator.hasNext()) {
            NodeInfo nodeInfo = visibleNodeIterator.next();

            int nodeWidth = (nodeInfo.depth - 1) * (indent + spacing);

            nodeRenderer.render(nodeInfo.data, visibleNodeIterator.getPath(),
                visibleNodeIterator.getRowIndex(), treeView, false, false,
                TreeView.NodeCheckState.UNCHECKED, false, false);
            nodeWidth += nodeRenderer.getPreferredWidth(-1);

            preferredWidth = Math.max(preferredWidth, nodeWidth);
        }

        if (showBranchControls) {
            preferredWidth += indent + spacing;
        }

        if (treeView.getCheckmarksEnabled()) {
            preferredWidth += Math.max(CHECKBOX.getWidth(), indent) + spacing;
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(final int width) {
        int nodeHeight = getNodeHeight();
        int visibleNodeCount = visibleNodes.getLength();

        int preferredHeight = nodeHeight * visibleNodeCount;

        if (visibleNodeCount > 1) {
            preferredHeight += VERTICAL_SPACING * (visibleNodeCount - 1);
        }

        return preferredHeight;
    }

    @Override
    public int getBaseline(final int width, final int height) {
        int baseline = -1;

        if (visibleNodes.getLength() > 0) {
            TreeView treeView = getTreeView();
            TreeView.NodeRenderer nodeRenderer = treeView.getNodeRenderer();

            NodeInfo nodeInfo = visibleNodes.get(0);

            int nodeWidth = width - (nodeInfo.depth - 1) * (indent + spacing);
            int nodeHeight = getNodeHeight();

            boolean expanded = false;
            boolean selected = nodeInfo.isSelected();
            boolean highlighted = nodeInfo.isHighlighted();
            boolean disabled = nodeInfo.isDisabled();

            if (showBranchControls) {
                if (nodeInfo instanceof BranchInfo) {
                    BranchInfo branchInfo = (BranchInfo) nodeInfo;
                    expanded = branchInfo.isExpanded();
                }

                nodeWidth -= (indent + spacing);
            }

            TreeView.NodeCheckState checkState = TreeView.NodeCheckState.UNCHECKED;
            if (treeView.getCheckmarksEnabled()) {
                checkState = nodeInfo.getCheckState();
                nodeWidth -= (Math.max(indent, CHECKBOX.getWidth()) + spacing);
            }

            nodeRenderer.render(nodeInfo.data, nodeInfo.getPath(), 0, treeView, expanded, selected,
                checkState, highlighted, disabled);
            baseline = nodeRenderer.getBaseline(nodeWidth, nodeHeight);
        }

        return baseline;
    }

    @Override
    public void layout() {
        if (validateSelection) {
            // Ensure that the selection is visible
            scrollSelectionToVisible();
        }

        validateSelection = false;
    }

    @Override
    public void paint(final Graphics2D graphics) {
        TreeView treeView = getTreeView();
        TreeView.NodeRenderer nodeRenderer = treeView.getNodeRenderer();

        int width = getWidth();
        int height = getHeight();

        int nodeHeight = getNodeHeight();

        // Paint the background
        if (backgroundColor != null) {
            graphics.setPaint(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        // nodeStart and nodeEnd are both inclusive
        int nodeStart = 0;
        int nodeEnd = visibleNodes.getLength() - 1;

        // Ensure that we only paint items that are visible
        Rectangle clipBounds = graphics.getClipBounds();
        if (clipBounds != null) {
            nodeStart = Math.max(nodeStart,
                (int) (clipBounds.y / (double) (nodeHeight + VERTICAL_SPACING)));
            nodeEnd = Math.min(
                nodeEnd,
                (int) ((clipBounds.y + clipBounds.height) / (double) (nodeHeight + VERTICAL_SPACING)));
        }

        int nodeY = nodeStart * (nodeHeight + VERTICAL_SPACING);

        VisibleNodeIterator visibleNodeIterator = new VisibleNodeIterator(nodeStart, nodeEnd);
        while (visibleNodeIterator.hasNext()) {
            NodeInfo nodeInfo = visibleNodeIterator.next();

            boolean expanded = false;
            boolean highlighted = nodeInfo.isHighlighted();
            boolean selected = nodeInfo.isSelected();
            boolean disabled = nodeInfo.isDisabled();

            int nodeX = (nodeInfo.depth - 1) * (indent + spacing);

            if (treeView.isEnabled()) {
                if (selected) {
                    // Paint the selection state
                    Color selectionBackgroundColorLocal = treeView.isFocused() ? this.selectionBackgroundColor
                        : inactiveSelectionBackgroundColor;
                    graphics.setPaint(selectionBackgroundColorLocal);
                    graphics.fillRect(0, nodeY, width, nodeHeight);
                } else if (highlighted && !disabled) {
                    // Paint the highlight state
                    graphics.setPaint(highlightBackgroundColor);
                    graphics.fillRect(0, nodeY, width, nodeHeight);
                }
            }

            // Paint the expand/collapse control
            if (showBranchControls) {
                if (nodeInfo instanceof BranchInfo) {
                    BranchInfo branchInfo = (BranchInfo) nodeInfo;

                    boolean showBranchControl = true;
                    if (!showEmptyBranchControls) {
                        branchInfo.loadChildren();
                        showBranchControl = !(branchInfo.children == null || branchInfo.children.isEmpty());
                    }

                    if (showBranchControl) {
                        expanded = branchInfo.isExpanded();

                        Color branchControlColorLocal;

                        if (selected) {
                            if (treeView.isFocused()) {
                                branchControlColorLocal = branchControlSelectionColor;
                            } else {
                                branchControlColorLocal = branchControlInactiveSelectionColor;
                            }
                        } else {
                            branchControlColorLocal = this.branchControlColor;
                        }

                        GeneralPath shape = new GeneralPath();

                        int imageX = nodeX + (indent - BRANCH_CONTROL_IMAGE_WIDTH) / 2;
                        int imageY = nodeY + (nodeHeight - BRANCH_CONTROL_IMAGE_HEIGHT) / 2;

                        if (expanded) {
                            shape.moveTo(imageX, imageY + 1);
                            shape.lineTo(imageX + 8, imageY + 1);
                            shape.lineTo(imageX + 4, imageY + 7);
                        } else {
                            shape.moveTo(imageX + 1, imageY);
                            shape.lineTo(imageX + 7, imageY + 4);
                            shape.lineTo(imageX + 1, imageY + 8);
                        }

                        shape.closePath();

                        Graphics2D branchControlGraphics = (Graphics2D) graphics.create();
                        GraphicsUtilities.setAntialiasingOn(branchControlGraphics);
                        if (!treeView.isEnabled() || disabled) {
                            branchControlGraphics.setComposite(AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER, 0.5f));
                        }
                        branchControlGraphics.setPaint(branchControlColorLocal);
                        branchControlGraphics.fill(shape);
                        branchControlGraphics.dispose();
                    }
                }

                nodeX += indent + spacing;
            }

            // Paint the checkbox
            TreeView.NodeCheckState checkState = TreeView.NodeCheckState.UNCHECKED;
            if (treeView.getCheckmarksEnabled()) {
                checkState = nodeInfo.getCheckState();

                int checkboxWidth = CHECKBOX.getWidth();
                int checkboxHeight = CHECKBOX.getHeight();

                int checkboxX = Math.max(indent - checkboxWidth, 0) / 2;
                int checkboxY = (nodeHeight - checkboxHeight) / 2;
                Graphics2D checkboxGraphics = (Graphics2D) graphics.create(nodeX + checkboxX, nodeY
                    + checkboxY, checkboxWidth, checkboxHeight);

                Button.State state;
                switch (checkState) {
                    case CHECKED:
                        state = Button.State.SELECTED;
                        break;
                    case MIXED:
                        state = Button.State.MIXED;
                        break;
                    default:
                        state = Button.State.UNSELECTED;
                        break;
                }

                CHECKBOX.setState(state);
                CHECKBOX.setEnabled(treeView.isEnabled() && !disabled
                    && !nodeInfo.isCheckmarkDisabled());
                CHECKBOX.paint(checkboxGraphics);
                checkboxGraphics.dispose();

                nodeX += Math.max(indent, checkboxWidth) + spacing;
            }

            int nodeWidth = Math.max(width - nodeX, 0);

            // Paint the node data
            Graphics2D rendererGraphics = (Graphics2D) graphics.create(nodeX, nodeY, nodeWidth,
                nodeHeight);
            nodeRenderer.render(nodeInfo.data, visibleNodeIterator.getPath(),
                visibleNodeIterator.getRowIndex(), treeView, expanded, selected, checkState,
                highlighted, disabled);
            nodeRenderer.setSize(nodeWidth, nodeHeight);
            nodeRenderer.paint(rendererGraphics);
            rendererGraphics.dispose();

            // Paint the grid line
            if (showGridLines) {
                graphics.setPaint(gridColor);

                GraphicsUtilities.drawLine(graphics, 0, nodeY + nodeHeight, width,
                    Orientation.HORIZONTAL);
            }

            nodeY += nodeHeight + VERTICAL_SPACING;
        }
    }

    public Font getFont() {
        return font;
    }

    public void setFont(final Font newFont) {
        Utils.checkNull(newFont, "font");

        this.font = newFont;
        invalidateComponent();
    }

    public final void setFont(final String fontString) {
        setFont(decodeFont(fontString));
    }

    public final void setFont(final Dictionary<String, ?> fontDictionary) {
        setFont(Theme.deriveFont(fontDictionary));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(final Color colorValue) {
        Utils.checkNull(colorValue, "color");

        this.color = colorValue;
        repaintComponent();
    }

    public void setColor(final String colorString) {
        setColor(GraphicsUtilities.decodeColor(colorString, "color"));
    }

    public final void setColor(final int colorIndex) {
        setColor(getColor(colorIndex));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(final Color disabledColorValue) {
        Utils.checkNull(disabledColorValue, "disabledColor");

        this.disabledColor = disabledColorValue;
        repaintComponent();
    }

    public void setDisabledColor(final String disabledColorString) {
        setDisabledColor(GraphicsUtilities.decodeColor(disabledColorString, "disabledColor"));
    }

    public final void setDisabledColor(final int disabledColorIndex) {
        setDisabledColor(getColor(disabledColorIndex));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(final Color backgroundColorValue) {
        // We allow a null background color here
        this.backgroundColor = backgroundColorValue;
        repaintComponent();
    }

    public void setBackgroundColor(final String backgroundColorString) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColorString, "backgroundColor"));
    }

    public final void setBackgroundColor(final int backgroundColorIndex) {
        setBackgroundColor(getColor(backgroundColorIndex));
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(final Color selectionColorValue) {
        Utils.checkNull(selectionColorValue, "selectionColor");

        this.selectionColor = selectionColorValue;
        repaintComponent();
    }

    public void setSelectionColor(final String selectionColorString) {
        setSelectionColor(GraphicsUtilities.decodeColor(selectionColorString, "selectionColor"));
    }

    public final void setSelectionColor(final int selectionColorIndex) {
        setSelectionColor(getColor(selectionColorIndex));
    }

    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public void setSelectionBackgroundColor(final Color selectionBackgroundColorValue) {
        Utils.checkNull(selectionBackgroundColorValue, "selectionBackgroundColor");

        this.selectionBackgroundColor = selectionBackgroundColorValue;
        repaintComponent();
    }

    public void setSelectionBackgroundColor(final String selectionBackgroundColorString) {
        setSelectionBackgroundColor(
            GraphicsUtilities.decodeColor(selectionBackgroundColorString, "selectionBackgroundColor"));
    }

    public final void setSelectionBackgroundColor(final int selectionBackgroundColorIndex) {
        setSelectionBackgroundColor(getColor(selectionBackgroundColorIndex));
    }

    public Color getInactiveSelectionColor() {
        return inactiveSelectionColor;
    }

    public void setInactiveSelectionColor(final Color inactiveSelectionColorValue) {
        Utils.checkNull(inactiveSelectionColorValue, "inactiveSelectionColor");

        this.inactiveSelectionColor = inactiveSelectionColorValue;
        repaintComponent();
    }

    public void setInactiveSelectionColor(final String inactiveSelectionColorString) {
        setInactiveSelectionColor(
            GraphicsUtilities.decodeColor(inactiveSelectionColorString, "inactiveSelectionColor"));
    }

    public final void setInactiveSelectionColor(final int inactiveSelectionColorIndex) {
        setInactiveSelectionColor(getColor(inactiveSelectionColorIndex));
    }

    public Color getInactiveSelectionBackgroundColor() {
        return inactiveSelectionBackgroundColor;
    }

    public void setInactiveSelectionBackgroundColor(final Color inactiveSelectionBackgroundColorValue) {
        Utils.checkNull(inactiveSelectionBackgroundColorValue, "inactiveSelectionBackgroundColor");

        this.inactiveSelectionBackgroundColor = inactiveSelectionBackgroundColorValue;
        repaintComponent();
    }

    public void setInactiveSelectionBackgroundColor(final String inactiveSelectionBackgroundString) {
        setInactiveSelectionBackgroundColor(
            GraphicsUtilities.decodeColor(inactiveSelectionBackgroundString, "inactiveSelectionBackgroundColor"));
    }

    public final void setInactiveSelectionBackgroundColor(final int inactiveSelectionBackgroundIndex) {
        setInactiveSelectionBackgroundColor(getColor(inactiveSelectionBackgroundIndex));
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(final Color highlightColorValue) {
        Utils.checkNull(highlightColorValue, "highlightColor");

        this.highlightColor = highlightColorValue;
        repaintComponent();
    }

    public void setHighlightColor(final String highlightColorString) {
        setHighlightColor(GraphicsUtilities.decodeColor(highlightColorString, "highlightColor"));
    }

    public final void setHighlightColor(final int highlightColorIndex) {
        setHighlightColor(getColor(highlightColorIndex));
    }

    public Color getHighlightBackgroundColor() {
        return highlightBackgroundColor;
    }

    public void setHighlightBackgroundColor(final Color highlightBackgroundColorValue) {
        Utils.checkNull(highlightBackgroundColorValue, "highlightBackgroundColor");

        this.highlightBackgroundColor = highlightBackgroundColorValue;
        repaintComponent();
    }

    public void setHighlightBackgroundColor(final String highlightBackgroundColorString) {
        setHighlightBackgroundColor(
            GraphicsUtilities.decodeColor(highlightBackgroundColorString, "highlightBackgroundColor"));
    }

    public final void setHighlightBackgroundColor(final int highlightBackgroundColorIndex) {
        setHighlightBackgroundColor(getColor(highlightBackgroundColorIndex));
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(final int spacingValue) {
        Utils.checkNonNegative(spacingValue, "spacing");

        this.spacing = spacingValue;
        invalidateComponent();
    }

    public void setSpacing(final Number spacingValue) {
        Utils.checkNull(spacingValue, "spacing");

        setSpacing(spacingValue.intValue());
    }

    public int getIndent() {
        return indent;
    }

    public void setIndent(final int indentValue) {
        Utils.checkNonNegative(indentValue, "indent");

        this.indent = indentValue;
        invalidateComponent();
    }

    public void setIndent(final Number indentValue) {
        Utils.checkNull(indentValue, "indent");

        setIndent(indentValue.intValue());
    }

    public boolean getShowHighlight() {
        return showHighlight;
    }

    public void setShowHighlight(final boolean showHighlightValue) {
        this.showHighlight = showHighlightValue;
        repaintComponent();
    }

    public boolean getShowBranchControls() {
        return showBranchControls;
    }

    public void setShowBranchControls(final boolean showBranchControlsValue) {
        this.showBranchControls = showBranchControlsValue;
        invalidateComponent();
    }

    public boolean getShowEmptyBranchControls() {
        return showEmptyBranchControls;
    }

    public void setShowEmptyBranchControls(final boolean showEmptyBranchControlsValue) {
        this.showEmptyBranchControls = showEmptyBranchControlsValue;
        repaintComponent();
    }

    public Color getBranchControlColor() {
        return branchControlColor;
    }

    public void setBranchControlColor(final Color branchControlColorValue) {
        Utils.checkNull(branchControlColorValue, "branchControlColor");

        this.branchControlColor = branchControlColorValue;
        repaintComponent();
    }

    public void setBranchControlColor(final String branchControlColorString) {
        setBranchControlColor(GraphicsUtilities.decodeColor(branchControlColorString, "branchControlColor"));
    }

    public final void setBranchControlColor(final int branchControlColorIndex) {
        setBranchControlColor(getColor(branchControlColorIndex));
    }

    public Color getBranchControlSelectionColor() {
        return branchControlSelectionColor;
    }

    public void setBranchControlSelectionColor(final Color branchControlSelectionColorValue) {
        Utils.checkNull(branchControlSelectionColorValue, "branchControlSelectionColor");

        this.branchControlSelectionColor = branchControlSelectionColorValue;
        repaintComponent();
    }

    public void setBranchControlSelectionColor(final String branchControlSelectionColorString) {
        setBranchControlSelectionColor(
            GraphicsUtilities.decodeColor(branchControlSelectionColorString, "branchControlSelectionColor"));
    }

    public final void setBranchControlSelectionColor(final int branchControlSelectionColorIndex) {
        setBranchControlSelectionColor(getColor(branchControlSelectionColorIndex));
    }

    public Color getBranchControlInactiveSelectionColor() {
        return branchControlInactiveSelectionColor;
    }

    public void setBranchControlInactiveSelectionColor(final Color branchControlInactiveSelectionColorValue) {
        Utils.checkNull(branchControlInactiveSelectionColorValue, "branchControlInactiveSelectionColor");

        this.branchControlInactiveSelectionColor = branchControlInactiveSelectionColorValue;
        repaintComponent();
    }

    public void setBranchControlInactiveSelectionColor(final String branchControlInactiveSelectionString) {
        setBranchControlInactiveSelectionColor(
            GraphicsUtilities.decodeColor(branchControlInactiveSelectionString, "branchControlInactiveSelectionColor"));
    }

    public final void setBranchControlInactiveSelectionColor(final int branchControlInactiveSelectionIndex) {
        setBranchControlInactiveSelectionColor(getColor(branchControlInactiveSelectionIndex));
    }

    public Color getGridColor() {
        return gridColor;
    }

    public void setGridColor(final Color gridColorValue) {
        Utils.checkNull(gridColorValue, "gridColor");

        this.gridColor = gridColorValue;
        repaintComponent();
    }

    public void setGridColor(final String gridColorString) {
        setGridColor(GraphicsUtilities.decodeColor(gridColorString, "gridColor"));
    }

    public final void setGridColor(final int gridColorIndex) {
        setGridColor(getColor(gridColorIndex));
    }

    public boolean getShowGridLines() {
        return showGridLines;
    }

    public void setShowGridLines(final boolean showGridLinesValue) {
        this.showGridLines = showGridLinesValue;
        repaintComponent();
    }

    /**
     * @return The fixed node height of this skin.
     */
    protected int getNodeHeight() {
        TreeView treeView = getTreeView();
        TreeView.NodeRenderer nodeRenderer = treeView.getNodeRenderer();
        nodeRenderer.render(null, null, -1, treeView, false, false,
            TreeView.NodeCheckState.UNCHECKED, false, false);

        int nodeHeight = nodeRenderer.getPreferredHeight(-1);
        if (treeView.getCheckmarksEnabled()) {
            nodeHeight = Math.max(CHECKBOX.getHeight() + (2 * CHECKBOX_VERTICAL_PADDING),
                nodeHeight);
        }

        return nodeHeight;
    }

    /**
     * @return The metadata associated with the node found at the specified
     * y-coordinate, or {@code null} if there is no node at that location.
     * @param y The current Y location.
     */
    protected final NodeInfo getNodeInfoAt(final int y) {
        NodeInfo nodeInfo = null;

        int nodeHeight = getNodeHeight();
        int index = y / (nodeHeight + VERTICAL_SPACING);

        if (index >= 0 && index < visibleNodes.getLength()) {
            nodeInfo = visibleNodes.get(index);
        }

        return nodeInfo;
    }

    /**
     * @return The metadata associated with the node at the specified path. The
     * path must be valid. The empty path is supported and represents the root
     * node info.
     * @param path The path to query.
     */
    protected final NodeInfo getNodeInfoAt(final Path path) {
        assert (path != null) : "Path is null";

        NodeInfo result = null;
        int n = path.getLength();

        if (n == 0) {
            result = rootBranchInfo;
        } else {
            BranchInfo branchInfo = rootBranchInfo;

            for (int i = 0; i < n - 1; i++) {
                branchInfo.loadChildren();
                NodeInfo nodeInfo = branchInfo.children.get(path.get(i));

                assert (nodeInfo instanceof BranchInfo) : "Invalid path";

                branchInfo = (BranchInfo) nodeInfo;
            }

            branchInfo.loadChildren();
            result = branchInfo.children.get(path.get(n - 1));
        }

        return result;
    }

    /**
     * @return The metadata for the branch node at the specified path.
     * @param path The path to query.
     * @see #getNodeInfoAt(Path)
     */
    protected final BranchInfo getBranchInfoAt(final Path path) {
        return (BranchInfo) getNodeInfoAt(path);
    }

    /**
     * @return The bounding box defined by the specified node, or <tt>null</tt> if
     * the node is not currently visible.
     * @param nodeInfo The node information to search for.
     */
    protected final Bounds getNodeBounds(final NodeInfo nodeInfo) {
        Bounds bounds = null;

        int index = visibleNodes.indexOf(nodeInfo);

        if (index >= 0) {
            int nodeHeight = getNodeHeight();
            int nodeY = index * (nodeHeight + VERTICAL_SPACING);

            bounds = new Bounds(0, nodeY, getWidth(), nodeHeight);
        }

        return bounds;
    }

    /**
     * Accepts the specified visitor on all node info objects that exist in this
     * skin's node info hierarchy.
     *
     * @param visitor The callback to execute on each node info object
     */
    protected final void accept(final NodeInfoVisitor visitor) {
        Sequence<NodeInfo> nodes = new ArrayList<>();
        nodes.add(rootBranchInfo);

        while (nodes.getLength() > 0) {
            NodeInfo nodeInfo = nodes.get(0);
            nodes.remove(0, 1);

            visitor.visit(nodeInfo);

            if (nodeInfo instanceof BranchInfo) {
                BranchInfo branchInfo = (BranchInfo) nodeInfo;

                if (branchInfo.children != null) {
                    for (int i = 0, n = branchInfo.children.getLength(); i < n; i++) {
                        nodes.insert(branchInfo.children.get(i), i);
                    }
                }
            }
        }
    }

    /**
     * Adds all children of the specified branch to the visible node list. Any
     * children nodes that are expanded [branches] will also have their children
     * made visible, and so on. Invalidates the component only if necessary.
     * @param parentBranchInfo The parent branch to traverse.
     */
    private void addVisibleNodes(final BranchInfo parentBranchInfo) {
        int insertIndex = -1;

        if (parentBranchInfo == rootBranchInfo) {
            // Bootstrap case since the root branch is implicitly expanded
            insertIndex = 0;
        } else {
            int branchIndex = visibleNodes.indexOf(parentBranchInfo);
            if (branchIndex >= 0) {
                insertIndex = branchIndex + 1;
            }
        }

        if (insertIndex >= 0) {
            Sequence<NodeInfo> nodes = new ArrayList<>();

            // The parent branch's children are the baseline nodes to make visible
            parentBranchInfo.loadChildren();
            for (int i = 0, n = parentBranchInfo.children.getLength(); i < n; i++) {
                nodes.add(parentBranchInfo.children.get(i));
            }

            while (nodes.getLength() > 0) {
                NodeInfo nodeInfo = nodes.get(0);
                nodes.remove(0, 1);
                visibleNodes.insert(nodeInfo, insertIndex++);

                // If we encounter an expanded branch, we add that branch's
                // children to our list of nodes that are to become visible
                if (nodeInfo instanceof BranchInfo) {
                    BranchInfo branchInfo = (BranchInfo) nodeInfo;
                    if (branchInfo.isExpanded()) {
                        branchInfo.loadChildren();
                        for (int i = 0, n = branchInfo.children.getLength(); i < n; i++) {
                            nodes.insert(branchInfo.children.get(i), i);
                        }
                    }
                }
            }

            invalidateComponent();
        }
    }

    /**
     * Adds the specified child of the specified branch to the visible node
     * list. It is assumed that the child in question is not an expanded branch.
     * Invalidates the component only if necessary.
     *
     * @param parentBranchInfo The branch info of the parent node.
     * @param index The index of the child within its parent.
     */
    private void addVisibleNode(final BranchInfo parentBranchInfo, final int index) {
        parentBranchInfo.loadChildren();

        assert (index >= 0) : "Index is too small";
        assert (index < parentBranchInfo.children.getLength()) : "Index is too large";

        int branchIndex = visibleNodes.indexOf(parentBranchInfo);

        if (parentBranchInfo == rootBranchInfo
            || (branchIndex >= 0 && parentBranchInfo.isExpanded())) {

            NodeInfo nodeInfo = parentBranchInfo.children.get(index);
            int insertIndex = branchIndex + index + 1;

            if (index > 0) {
                // Siblings of the node that lie before it may be expanded
                // branches, thus adding their own children to the
                // visible nodes list and pushing down our insert index
                NodeInfo youngerSibling = parentBranchInfo.children.get(index - 1);

                // Try to insert after our younger sibling
                insertIndex = visibleNodes.indexOf(youngerSibling) + 1;

                // Continue looking as long as the node at our insert index
                // has a greater depth than we do, which means that it's a
                // descendant of our younger sibling
                for (int n = visibleNodes.getLength(), nodeDepth = youngerSibling.depth; insertIndex < n
                    && visibleNodes.get(insertIndex).depth > nodeDepth; insertIndex++) {
                    continue;
                }
            }

            visibleNodes.insert(nodeInfo, insertIndex);

            invalidateComponent();
        }
    }

    /**
     * Removes the specified children of the specified branch from the visible
     * node list if necessary. If they are not already in the visible node list,
     * nothing happens. Invalidates the component only if necessary.
     *
     * @param parentBranchInfo The branch info of the parent node.
     * @param index The index of the first child node to remove from the visible
     * nodes sequence.
     * @param count The number of child nodes to remove, or <code>-1</code> to remove
     * all child nodes from the visible nodes sequence.
     */
    private void removeVisibleNodes(final BranchInfo parentBranchInfo, final int index, final int count) {
        parentBranchInfo.loadChildren();
        int childrenLength = parentBranchInfo.children.getLength();

        int countUpdated = count;

        if (countUpdated == -1) {
            assert (index == 0) : "Non-zero index with 'remove all' count";
            countUpdated = childrenLength;
        }

        // If the index is greater-equal the child length, then there could
        // not possibly be any visible nodes, so just quit
        if (index >= childrenLength) {
            return;
        }
        assert (index + countUpdated <= childrenLength) : "Value too big";
        if (countUpdated > 0) {
            NodeInfo first = parentBranchInfo.children.get(index);
            NodeInfo last = parentBranchInfo.children.get(index + countUpdated - 1);

            int rangeStart = visibleNodes.indexOf(first);

            if (rangeStart >= 0) {
                int rangeEnd = visibleNodes.indexOf(last) + 1;

                assert (rangeEnd > rangeStart) : "Invalid visible node structure";

                // Continue looking as long as the node at our endpoint has a
                // greater depth than the last child node, which means that
                // it's a descendant of the last child node
                for (int n = visibleNodes.getLength(), nodeDepth = last.depth; rangeEnd < n
                    && visibleNodes.get(rangeEnd).depth > nodeDepth; rangeEnd++) {
                    continue;
                }

                visibleNodes.remove(rangeStart, rangeEnd - rangeStart);

                invalidateComponent();
            }
        }
    }

    /**
     * Repaints the region occupied by the specified node.
     *
     * @param nodeInfo The node to search for.
     */
    protected void repaintNode(final NodeInfo nodeInfo) {
        Bounds bounds = getNodeBounds(nodeInfo);
        if (bounds != null) {
            repaintComponent(bounds);
        }
    }

    /**
     * Clears the highlighted node if one exists.
     */
    protected void clearHighlightedNode() {
        if (highlightedNode != null) {
            highlightedNode.setHighlighted(false);
            repaintNode(highlightedNode);

            highlightedNode = null;
        }
    }

    /**
     * Clears our {@code NodeInfo} hierarchy of the specified cached field.
     *
     * @param mask The bitmask specifying which field to clear.
     */
    private void clearFields(final byte mask) {
        accept(nodeInfo -> nodeInfo.clearField(mask));
    }

    /**
     * Scrolls the last visible (expanded) selected node into viewport
     * visibility. If no such node exists, nothing happens. <p> This should only
     * be called when the tree view is valid.
     */
    private void scrollSelectionToVisible() {
        TreeView treeView = getTreeView();

        Sequence<Path> selectedPaths = treeView.getSelectedPaths();
        int n = selectedPaths.getLength();

        if (n > 0) {
            Bounds nodeBounds = null;

            for (int i = n - 1; i >= 0 && nodeBounds == null; i--) {
                NodeInfo nodeInfo = getNodeInfoAt(selectedPaths.get(i));
                nodeBounds = getNodeBounds(nodeInfo);
            }

            if (nodeBounds != null) {
                Bounds visibleSelectionBounds = treeView.getVisibleArea(nodeBounds);
                if (visibleSelectionBounds != null
                    && visibleSelectionBounds.height < nodeBounds.height) {
                    treeView.scrollAreaToVisible(nodeBounds);
                }
            }
        }
    }

    @Override
    public boolean mouseMove(final Component component, final int x, final int y) {
        boolean consumed = super.mouseMove(component, x, y);

        TreeView treeView = getTreeView();

        if (showHighlight && treeView.getSelectMode() != SelectMode.NONE) {
            NodeInfo previousHighlightedNode = highlightedNode;
            highlightedNode = getNodeInfoAt(y);

            if (highlightedNode != previousHighlightedNode) {
                if (previousHighlightedNode != null) {
                    previousHighlightedNode.setHighlighted(false);
                    repaintNode(previousHighlightedNode);
                }

                if (highlightedNode != null) {
                    highlightedNode.setHighlighted(true);
                    repaintNode(highlightedNode);
                }
            }
        }

        return consumed;
    }

    @Override
    public void mouseOut(final Component component) {
        super.mouseOut(component);

        clearHighlightedNode();
        selectPath = null;
    }

    @Override
    public boolean mouseDown(final Component component, final Mouse.Button button,
        final int x, final int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        if (!consumed) {
            TreeView treeView = getTreeView();
            NodeInfo nodeInfo = getNodeInfoAt(y);

            if (nodeInfo != null && !nodeInfo.isDisabled()) {
                int nodeHeight = getNodeHeight();
                int baseNodeX = (nodeInfo.depth - 1) * (indent + spacing);

                int nodeX = baseNodeX + (showBranchControls ? indent + spacing : 0);
                int nodeY = (y / (nodeHeight + VERTICAL_SPACING)) * (nodeHeight + VERTICAL_SPACING);

                int checkboxWidth = CHECKBOX.getWidth();
                int checkboxHeight = CHECKBOX.getHeight();

                int checkboxX = Math.max(indent - checkboxWidth, 0) / 2;
                int checkboxY = (nodeHeight - checkboxHeight) / 2;

                // Only proceed if the user DIDN'T click on a checkbox
                if (!treeView.getCheckmarksEnabled() || nodeInfo.isCheckmarkDisabled()
                    || x < nodeX + checkboxX || x >= nodeX + checkboxX + checkboxWidth
                    || y < nodeY + checkboxY || y >= nodeY + checkboxY + checkboxHeight) {
                    Path path = nodeInfo.getPath();

                    // See if the user clicked on an expand/collapse control of
                    // a branch. If so, expand/collapse the branch
                    if (showBranchControls && nodeInfo instanceof BranchInfo && x >= baseNodeX
                        && x < baseNodeX + indent) {
                        BranchInfo branchInfo = (BranchInfo) nodeInfo;
                        treeView.setBranchExpanded(path, !branchInfo.isExpanded());
                        consumed = true;
                    }

                    // If we haven't consumed the event, then proceed to manage
                    // the selection state of the node
                    if (!consumed) {
                        SelectMode selectMode = treeView.getSelectMode();

                        if (button == Mouse.Button.LEFT) {
                            Modifier commandModifier = Platform.getCommandModifier();

                            if (Keyboard.isPressed(commandModifier)
                                && selectMode == SelectMode.MULTI) {
                                // Toggle the item's selection state
                                if (nodeInfo.isSelected()) {
                                    treeView.removeSelectedPath(path);
                                } else {
                                    treeView.addSelectedPath(path);
                                }
                            } else if (Keyboard.isPressed(commandModifier)
                                && selectMode == SelectMode.SINGLE) {
                                // Toggle the item's selection state
                                if (nodeInfo.isSelected()) {
                                    treeView.clearSelection();
                                } else {
                                    treeView.setSelectedPath(path);
                                }
                            } else {
                                if (selectMode != SelectMode.NONE) {
                                    if (nodeInfo.isSelected()) {
                                        selectPath = path;
                                    } else {
                                        treeView.setSelectedPath(path);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            treeView.requestFocus();
        }

        return consumed;
    }

    @Override
    public boolean mouseUp(final Component component, final Mouse.Button button,
        final int x, final int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        TreeView treeView = getTreeView();
        if (selectPath != null
            && !treeView.getFirstSelectedPath().equals(treeView.getLastSelectedPath())) {
            treeView.setSelectedPath(selectPath);
            selectPath = null;
        }

        return consumed;
    }

    @Override
    public boolean mouseClick(final Component component, final Mouse.Button button,
        final int x, final int y, final int count) {
        boolean consumed = super.mouseClick(component, button, x, y, count);

        if (!consumed) {
            TreeView treeView = getTreeView();
            NodeInfo nodeInfo = getNodeInfoAt(y);

            if (nodeInfo != null && !nodeInfo.isDisabled()) {
                int nodeHeight = getNodeHeight();
                int baseNodeX = (nodeInfo.depth - 1) * (indent + spacing);

                int nodeX = baseNodeX + (showBranchControls ? indent + spacing : 0);
                int nodeY = (y / (nodeHeight + VERTICAL_SPACING)) * (nodeHeight + VERTICAL_SPACING);

                int checkboxWidth = CHECKBOX.getWidth();
                int checkboxHeight = CHECKBOX.getHeight();

                int checkboxX = Math.max(indent - checkboxWidth, 0) / 2;
                int checkboxY = (nodeHeight - checkboxHeight) / 2;

                if (treeView.getCheckmarksEnabled() && !nodeInfo.isCheckmarkDisabled()
                    && x >= nodeX + checkboxX && x < nodeX + checkboxX + checkboxWidth
                    && y >= nodeY + checkboxY && y < nodeY + checkboxY + checkboxHeight) {
                    Path path = nodeInfo.getPath();
                    treeView.setNodeChecked(path, !nodeInfo.isChecked());
                } else {
                    if (selectPath != null && count == 1 && button == Mouse.Button.LEFT) {
                        TreeView.NodeEditor nodeEditor = treeView.getNodeEditor();
                        if (nodeEditor != null) {
                            if (nodeEditor.isEditing()) {
                                nodeEditor.endEdit(true);
                            }
                            nodeEditor.beginEdit(treeView, selectPath);
                        }
                    }
                    selectPath = null;
                }
            }
        }

        return consumed;
    }

    @Override
    public boolean mouseWheel(final Component component, final Mouse.ScrollType scrollType,
        final int scrollAmount, final int wheelRotation, final int x, final int y) {
        if (highlightedNode != null) {
            Bounds nodeBounds = getNodeBounds(highlightedNode);

            highlightedNode.setHighlighted(false);
            highlightedNode = null;

            if (nodeBounds != null) {
                repaintComponent(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height, true);
            }
        }

        return super.mouseWheel(component, scrollType, scrollAmount, wheelRotation, x, y);
    }

    /**
     * Keyboard handling (arrow keys with modifiers).
     * <ul>
     * <li>{@link KeyCode#UP UP} Selects the previous enabled node when select mode
     * is not {@link SelectMode#NONE}</li>
     * <li>{@link KeyCode#DOWN DOWN} Selects the next enabled node when select mode
     * is not {@link SelectMode#NONE}</li>
     * <li>{@link Modifier#SHIFT SHIFT} + {@link KeyCode#UP UP} Increases the
     * selection size by including the previous enabled node when select mode is
     * {@link SelectMode#MULTI}</li>
     * <li>{@link Modifier#SHIFT SHIFT} + {@link KeyCode#DOWN DOWN} Increases the
     * selection size by including the next enabled node when select mode is
     * {@link SelectMode#MULTI}</li>
     * </ul>
     */
    @Override
    public boolean keyPressed(final Component component, final int keyCode, final KeyLocation keyLocation) {
        boolean consumed = false;

        TreeView treeView = getTreeView();
        SelectMode selectMode = treeView.getSelectMode();

        switch (keyCode) {
            case KeyCode.UP:
                if (selectMode != SelectMode.NONE) {
                    Path firstSelectedPath = treeView.getFirstSelectedPath();

                    int index;
                    if (firstSelectedPath != null) {
                        NodeInfo previousSelectedNode = getNodeInfoAt(firstSelectedPath);
                        index = visibleNodes.indexOf(previousSelectedNode);
                    } else {
                        // Select the last visible node
                        index = visibleNodes.getLength();
                    }

                    NodeInfo newSelectedNode = null;
                    do {
                        newSelectedNode = (--index >= 0) ? visibleNodes.get(index) : null;
                    } while (newSelectedNode != null && newSelectedNode.isDisabled());

                    if (newSelectedNode != null) {
                        if (Keyboard.isPressed(Modifier.SHIFT)
                            && treeView.getSelectMode() == SelectMode.MULTI) {
                            treeView.addSelectedPath(newSelectedNode.getPath());
                        } else {
                            treeView.setSelectedPath(newSelectedNode.getPath());
                        }
                        treeView.scrollAreaToVisible(getNodeBounds(newSelectedNode));
                    }
                    consumed = true;
                }
                break;

            case KeyCode.DOWN:
                if (selectMode != SelectMode.NONE) {
                    Path lastSelectedPath = treeView.getLastSelectedPath();

                    int index;
                    if (lastSelectedPath != null) {
                        NodeInfo previousSelectedNode = getNodeInfoAt(lastSelectedPath);
                        index = visibleNodes.indexOf(previousSelectedNode);
                    } else {
                        // Select the first visible node
                        index = -1;
                    }

                    NodeInfo newSelectedNode = null;
                    int n = visibleNodes.getLength();
                    do {
                        newSelectedNode = (++index <= n - 1) ? visibleNodes.get(index) : null;
                    } while (newSelectedNode != null && newSelectedNode.isDisabled());

                    if (newSelectedNode != null) {
                        if (Keyboard.isPressed(Modifier.SHIFT)
                            && treeView.getSelectMode() == SelectMode.MULTI) {
                            treeView.addSelectedPath(newSelectedNode.getPath());
                        } else {
                            treeView.setSelectedPath(newSelectedNode.getPath());
                        }
                        treeView.scrollAreaToVisible(getNodeBounds(newSelectedNode));
                    }
                    consumed = true;
                }
                break;

            case KeyCode.LEFT:
                if (showBranchControls) {
                    Sequence<Path> paths = treeView.getSelectedPaths();

                    if (paths != null && paths.getLength() > 0) {
                        Path path = paths.get(paths.getLength() - 1);
                        NodeInfo nodeInfo = getNodeInfoAt(path);
                        if (nodeInfo instanceof BranchInfo) {
                            BranchInfo branchInfo = (BranchInfo) nodeInfo;
                            if (branchInfo.isExpanded()) {
                                treeView.collapseBranch(branchInfo.getPath());
                            }
                        }
                        consumed = true;
                    }
                }
                break;

            case KeyCode.RIGHT:
                if (showBranchControls) {
                    Sequence<Path> paths = treeView.getSelectedPaths();

                    if (paths != null && paths.getLength() > 0) {
                        Path path = paths.get(paths.getLength() - 1);
                        NodeInfo nodeInfo = getNodeInfoAt(path);
                        if (nodeInfo instanceof BranchInfo) {
                            BranchInfo branchInfo = (BranchInfo) nodeInfo;
                            if (!branchInfo.isExpanded()) {
                                treeView.expandBranch(branchInfo.getPath());
                            }
                        }
                        consumed = true;
                    }
                }
                break;

            default:
                consumed = super.keyPressed(component, keyCode, keyLocation);
                break;
        }

        if (consumed) {
            clearHighlightedNode();
        }

        return consumed;
    }

    /**
     * {@link KeyCode#SPACE SPACE} toggles check mark selection when select mode
     * is {@link SelectMode#SINGLE}.
     */
    @Override
    public boolean keyReleased(final Component component, final int keyCode, final KeyLocation keyLocation) {
        boolean consumed = false;

        TreeView treeView = getTreeView();

        if (keyCode == KeyCode.SPACE) {
            if (treeView.getCheckmarksEnabled()
                && treeView.getSelectMode() == SelectMode.SINGLE) {
                Path selectedPath = treeView.getSelectedPath();

                if (selectedPath != null) {
                    NodeInfo nodeInfo = getNodeInfoAt(selectedPath);

                    if (!nodeInfo.isCheckmarkDisabled()) {
                        treeView.setNodeChecked(selectedPath, !treeView.isNodeChecked(selectedPath));
                    }
                }
            }
        } else {
            consumed = super.keyReleased(component, keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public boolean isFocusable() {
        TreeView treeView = getTreeView();
        return (treeView.getSelectMode() != SelectMode.NONE);
    }

    @Override
    public boolean isOpaque() {
        return (backgroundColor != null && backgroundColor.getTransparency() == Transparency.OPAQUE);
    }

    // ComponentStateListener methods

    @Override
    public void enabledChanged(final Component component) {
        super.enabledChanged(component);
        repaintComponent();
    }

    @Override
    public void focusedChanged(final Component component, final Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);
        repaintComponent();
    }

    // TreeView.Skin methods

    @Override
    public Path getNodeAt(final int y) {
        Path path = null;

        NodeInfo nodeInfo = getNodeInfoAt(y);

        if (nodeInfo != null) {
            path = nodeInfo.getPath();
        }

        return path;
    }

    @Override
    public Bounds getNodeBounds(final Path path) {
        Bounds nodeBounds = null;

        NodeInfo nodeInfo = getNodeInfoAt(path);

        if (nodeInfo != null) {
            nodeBounds = getNodeBounds(nodeInfo);
        }

        return nodeBounds;
    }

    @Override
    public int getNodeIndent(final int depth) {
        TreeView treeView = getTreeView();

        int nodeIndent = (depth - 1) * (indent + spacing);

        if (showBranchControls) {
            nodeIndent += indent + spacing;
        }

        if (treeView.getCheckmarksEnabled()) {
            nodeIndent += Math.max(CHECKBOX.getWidth(), indent) + spacing;
        }

        return nodeIndent;
    }

    @Override
    public int getRowIndex(final Path path) {
        int rowIndex = -1;

        NodeInfo nodeInfo = getNodeInfoAt(path);

        if (nodeInfo != null) {
            rowIndex = visibleNodes.indexOf(nodeInfo);
        }

        return rowIndex;
    }

    // TreeViewListener methods

    @Override
    public void treeDataChanged(final TreeView treeView, final List<?> previousTreeData) {
        @SuppressWarnings("unchecked")
        List<Object> treeData = (List<Object>) treeView.getTreeData();

        visibleNodes.clear();

        if (treeData == null) {
            rootBranchInfo = null;
        } else {
            rootBranchInfo = new BranchInfo(treeView, null, treeData);
            addVisibleNodes(rootBranchInfo);
        }

        invalidateComponent();
    }

    @Override
    public void nodeRendererChanged(final TreeView treeView,
        final TreeView.NodeRenderer previousNodeRenderer) {
        invalidateComponent();
    }

    @Override
    public void selectModeChanged(final TreeView treeView, final SelectMode previousSelectMode) {
        // The selection has implicitly been cleared
        clearFields(NodeInfo.SELECTED_MASK);
        repaintComponent();
    }

    @Override
    public void checkmarksEnabledChanged(final TreeView treeView) {
        // The check state of all nodes has implicitly been cleared
        clearFields(NodeInfo.CHECK_STATE_MASK);
        invalidateComponent();
    }

    @Override
    public void showMixedCheckmarkStateChanged(final TreeView treeView) {
        if (treeView.getCheckmarksEnabled()) {
            // The check state of all *branch* nodes may have changed, so we
            // need to update the cached check state of all BranchNode
            // instances in our hierarchy
            Sequence<NodeInfo> nodes = new ArrayList<>();
            nodes.add(rootBranchInfo);

            while (nodes.getLength() > 0) {
                NodeInfo nodeInfo = nodes.get(0);
                nodes.remove(0, 1);

                // Only branch nodes can be affected by this event
                if (nodeInfo instanceof BranchInfo) {
                    BranchInfo branchInfo = (BranchInfo) nodeInfo;

                    // Update the cached entry for this branch
                    Path path = branchInfo.getPath();
                    branchInfo.setCheckState(treeView.getNodeCheckState(path));

                    // Add the branch's children to the queue
                    if (branchInfo.children != null) {
                        for (int i = 0, n = branchInfo.children.getLength(); i < n; i++) {
                            nodes.insert(branchInfo.children.get(i), i);
                        }
                    }
                }
            }

            repaintComponent();
        }
    }

    @Override
    public void disabledNodeFilterChanged(final TreeView treeView, final Filter<?> previousDisabledNodeFilter) {
        @SuppressWarnings("unchecked")
        final Filter<Object> disabledNodeFilter = (Filter<Object>) treeView.getDisabledNodeFilter();

        accept(nodeInfo -> {
            if (nodeInfo != rootBranchInfo) {
                nodeInfo.setDisabled(disabledNodeFilter != null
                    && disabledNodeFilter.include(nodeInfo.data));
                }
            }
        );

        repaintComponent();
    }

    @Override
    public void disabledCheckmarkFilterChanged(final TreeView treeView,
        final Filter<?> previousDisabledCheckmarkFilter) {
        @SuppressWarnings("unchecked")
        final Filter<Object> disabledCheckmarkFilter = (Filter<Object>) treeView.getDisabledCheckmarkFilter();

        accept(nodeInfo -> {
            if (nodeInfo != rootBranchInfo) {
                nodeInfo.setCheckmarkDisabled(disabledCheckmarkFilter != null
                    && disabledCheckmarkFilter.include(nodeInfo.data));
                }
            }
        );

        repaintComponent();
    }

    // TreeViewBranchListener methods

    @Override
    public void branchExpanded(final TreeView treeView, final Path path) {
        BranchInfo branchInfo = getBranchInfoAt(path);

        branchInfo.setExpanded(true);
        addVisibleNodes(branchInfo);

        repaintNode(branchInfo);
    }

    @Override
    public void branchCollapsed(final TreeView treeView, final Path path) {
        BranchInfo branchInfo = getBranchInfoAt(path);

        branchInfo.setExpanded(false);
        removeVisibleNodes(branchInfo, 0, -1);

        repaintNode(branchInfo);
    }

    @Override
    public Vote previewBranchExpandedChange(final TreeView treeView, final Path path) {
        // We currently have no reason to refuse to open / close the branch
        // although other listeners might have a reason
        return Vote.APPROVE;
    }

    @Override
    public void branchExpandedChangeVetoed(final TreeView treeView, final Path path, final Vote reason) {
        // Nothing really to do -- our visual state doesn't change until/unless the
        // expand/collapse really happens
    }

    // TreeViewNodeListener methods

    @Override
    @SuppressWarnings("unchecked")
    public void nodeInserted(final TreeView treeView, final Path path, final int index) {
        BranchInfo branchInfo = getBranchInfoAt(path);
        List<Object> branchData = (List<Object>) branchInfo.data;

        // Update our internal branch info
        if (branchInfo.children != null) {
            NodeInfo nodeInfo = NodeInfo.newInstance(treeView, branchInfo, branchData.get(index));
            branchInfo.children.insert(nodeInfo, index);
        }

        // Add the node to the visible nodes list
        addVisibleNode(branchInfo, index);

        // If the empty branch controls are not shown, then this event might
        // need a repaint of the parent
        if (!showEmptyBranchControls) {
            repaintNode(branchInfo);
        }
    }

    @Override
    public void nodesRemoved(final TreeView treeView, final Path path, final int index, final int count) {
        BranchInfo branchInfo = getBranchInfoAt(path);

        // Remove the nodes from the visible nodes list
        removeVisibleNodes(branchInfo, index, count);

        // Update our internal branch info
        if (branchInfo.children != null) {
            // Problem: if "loadChildren" was called on this branch the first time
            // by "removeVisibleNodes" above, then the "children" will actually be
            // correct here (i.e., already removed), so this is unnecessary.
            int len = branchInfo.children.getLength();
            if (index < len && index + count <= len) {
                branchInfo.children.remove(index, count);
            }
        }

        // If the empty branch controls are not shown, then this event might
        // need a repaint of the parent
        if (!showEmptyBranchControls) {
            repaintNode(branchInfo);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void nodeUpdated(final TreeView treeView, final Path path, final int index) {
        BranchInfo branchInfo = getBranchInfoAt(path);
        List<Object> branchData = (List<Object>) branchInfo.data;

        branchInfo.loadChildren();
        NodeInfo nodeInfo = branchInfo.children.get(index);

        Object previousNodeData = nodeInfo.data;
        Object nodeData = branchData.get(index);

        if (previousNodeData != nodeData) {
            // Remove the old node from the visible nodes list
            removeVisibleNodes(branchInfo, index, 1);

            // Update our internal branch info
            nodeInfo = NodeInfo.newInstance(treeView, branchInfo, nodeData);
            branchInfo.children.update(index, nodeInfo);

            // Add the new node to the visible nodes list
            addVisibleNode(branchInfo, index);
        } else {
            // This update might affect the node's disabled state
            Filter<Object> disabledNodeFilter = (Filter<Object>) treeView.getDisabledNodeFilter();
            nodeInfo.setDisabled(disabledNodeFilter != null && disabledNodeFilter.include(nodeData));

            if (visibleNodes.indexOf(nodeInfo) >= 0) {
                // The updated node data might affect our preferred width
                invalidateComponent();
            }
        }
    }

    @Override
    public void nodesCleared(final TreeView treeView, final Path path) {
        BranchInfo branchInfo = getBranchInfoAt(path);

        // Remove the node from the visible nodes list
        removeVisibleNodes(branchInfo, 0, -1);

        // Update our internal branch info
        if (branchInfo.children != null) {
            branchInfo.children.clear();
        }
    }

    @Override
    public void nodesSorted(final TreeView treeView, final Path path) {
        BranchInfo branchInfo = getBranchInfoAt(path);

        // Remove the child nodes from the visible nodes list
        removeVisibleNodes(branchInfo, 0, -1);

        // Re-load the branch's children to get the correct sort order
        branchInfo.children = null;
        branchInfo.loadChildren();

        // Add the child nodes back to the visible nodes list
        addVisibleNodes(branchInfo);
    }

    // TreeViewNodeStateListener methods

    @Override
    public void nodeCheckStateChanged(final TreeView treeView, final Path path,
        final TreeView.NodeCheckState previousCheckState) {
        NodeInfo nodeInfo = getNodeInfoAt(path);

        nodeInfo.setCheckState(treeView.getNodeCheckState(path));

        repaintNode(nodeInfo);
    }

    // TreeViewSelectionListener methods

    @Override
    public void selectedPathAdded(final TreeView treeView, final Path path) {
        // Update the node info
        NodeInfo nodeInfo = getNodeInfoAt(path);
        nodeInfo.setSelected(true);

        if (treeView.isValid()) {
            Bounds nodeBounds = getNodeBounds(nodeInfo);

            if (nodeBounds != null) {
                // Ensure that the selection is visible
                Bounds visibleSelectionBounds = treeView.getVisibleArea(nodeBounds);
                if (visibleSelectionBounds.height < nodeBounds.height) {
                    treeView.scrollAreaToVisible(nodeBounds);
                }
            }
        } else {
            validateSelection = true;
        }

        repaintNode(nodeInfo);
    }

    @Override
    public void selectedPathRemoved(final TreeView treeView, final Path path) {
        NodeInfo nodeInfo = getNodeInfoAt(path);
        nodeInfo.setSelected(false);
        repaintNode(nodeInfo);
    }

    @Override
    public void selectedPathsChanged(final TreeView treeView, final Sequence<Path> previousSelectedPaths) {
        if (previousSelectedPaths != null && previousSelectedPaths != treeView.getSelectedPaths()) {
            // Ensure that the selection is visible
            if (treeView.isValid()) {
                scrollSelectionToVisible();
            } else {
                validateSelection = true;
            }

            // Un-select the previous selected paths
            for (int i = 0, n = previousSelectedPaths.getLength(); i < n; i++) {
                NodeInfo previousSelectedNode = getNodeInfoAt(previousSelectedPaths.get(i));
                previousSelectedNode.setSelected(false);
                repaintNode(previousSelectedNode);
            }

            Sequence<Path> selectedPaths = treeView.getSelectedPaths();

            // Select the current selected paths
            for (int i = 0, n = selectedPaths.getLength(); i < n; i++) {
                NodeInfo selectedNode = getNodeInfoAt(selectedPaths.get(i));
                selectedNode.setSelected(true);
                repaintNode(selectedNode);
            }
        }
    }

}
