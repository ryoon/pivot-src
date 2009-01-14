/*
 * Copyright (c) 2009 VMware, Inc.
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
package pivot.wtk.skin;

import java.awt.Graphics2D;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.wtk.Bounds;
import pivot.wtk.Component;
import pivot.wtk.ConstrainedVisual;
import pivot.wtk.Dimensions;
import pivot.wtk.TextArea;
import pivot.wtk.TextAreaListener;
import pivot.wtk.text.Document;
import pivot.wtk.text.Element;
import pivot.wtk.text.ElementListener;
import pivot.wtk.text.Node;
import pivot.wtk.text.NodeListener;
import pivot.wtk.text.Paragraph;
import pivot.wtk.text.TextNode;
import pivot.wtk.text.TextNodeListener;

/**
 * Text area skin.
 *
 * @author gbrown
 */
public class TextAreaSkin extends ContainerSkin
    implements TextAreaListener {
    /**
     * Abstract base class for node views.
     *
     * @author gbrown
     */
    public abstract class NodeView implements ConstrainedVisual, NodeListener {
        private ElementView parent = null;
        private Node node = null;

        private int x = 0;
        private int y = 0;

        private int width = 0;
        private int height = 0;

        public NodeView(ElementView parent, Node node) {
            this.parent = parent;

            node.getNodeListeners().add(this);
            this.node = node;
        }

        public ElementView getParent() {
            return parent;
        }

        public Node getNode() {
            return node;
        }

        public void dispose() {
            node.getNodeListeners().remove(this);
            node = null;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public void setSize(int width, int height) {
            assert(width >= 0);
            assert(height >= 0);

            this.width = width;
            this.height = height;
        }

        public Bounds getBounds() {
            return new Bounds(x, y, width, height);
        }

        public boolean isValid() {
            return true;
        }

        public void invalidate() {
            if (parent != null) {
                parent.invalidate();
            }
        }

        public void validate() {
            if (width > 0
                && height > 0) {
                layout();
            }
        }

        public void layout() {
            // No-op
        }

        public abstract NodeView breakAt(int x);

        public void parentChanged(Node node, Element previousParent) {
            // No-op
        }

        public void offsetChanged(Node node, int previousOffset) {
            // No-op
        }
    }

    /**
     * Abstract base class for element views.
     *
     * @author gbrown
     */
    public abstract class ElementView extends NodeView
        implements ElementListener {
        protected ArrayList<NodeView> nodeViews = new ArrayList<NodeView>();
        private boolean valid = true;

        public ElementView(ElementView parent, Element element) {
            super(parent, element);

            element.getElementListeners().add(this);

            // Create child node views
            for (Node node : element) {
                nodeViews.add(createNodeView(node));
            }
        }

        @Override
        public void dispose() {
            // Dispose child node views
            for (NodeView nodeView : nodeViews) {
                nodeView.dispose();
            }

            Element element = (Element)getNode();
            element.getElementListeners().remove(this);

            super.dispose();
        }

        public NodeView get(int index) {
            return nodeViews.get(index);
        }

        public int getLength() {
            return nodeViews.getLength();
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public void invalidate() {
            if (valid) {
                valid = false;
                super.invalidate();
            }
        }

        @Override
        public void validate() {
            if (!valid) {
                try {
                    super.validate();

                    for (int i = 0, n = nodeViews.getLength(); i < n; i++) {
                        NodeView nodeView = nodeViews.get(i);
                        nodeView.validate();
                    }
                } finally {
                    valid = true;
                }
            }
        }

        public void paint(Graphics2D graphics) {
            java.awt.Rectangle clipBounds = graphics.getClipBounds();
            Bounds paintBounds = (clipBounds == null) ?
                new Bounds(0, 0, getWidth(), getHeight()) : new Bounds(clipBounds);

            for (NodeView nodeView : nodeViews) {
                Bounds nodeViewBounds = nodeView.getBounds();

                // Only paint node views that intersect the current clip rectangle
                if (nodeViewBounds.intersects(paintBounds)) {
                    // Create a copy of the current graphics context and
                    // translate to the node view's coordinate system
                    Graphics2D nodeViewGraphics = (Graphics2D)graphics.create();
                    nodeViewGraphics.translate(nodeViewBounds.x, nodeViewBounds.y);
                    nodeViewGraphics.clipRect(0, 0, nodeViewBounds.width, nodeViewBounds.height);

                    // Paint the node view
                    nodeView.paint(nodeViewGraphics);

                    // Dispose of the node views's graphics
                    nodeViewGraphics.dispose();
                }
            }
        }

        public NodeView getNodeViewAt(int x, int y) {
            NodeView nodeView = null;

            int i = nodeViews.getLength() - 1;
            while (i >= 0) {
                nodeView = nodeViews.get(i);
                Bounds bounds = nodeView.getBounds();
                if (bounds.contains(x, y)) {
                    break;
                }

                i--;
            }

            if (i < 0) {
                nodeView = null;
            }

            return nodeView;
        }

        public void nodeInserted(Element element, int index) {
            Node node = element.get(index);
            nodeViews.insert(createNodeView(node), index);
        }

        public void nodesRemoved(Element element, int index, Sequence<Node> nodes) {
            Sequence<NodeView> removed = nodeViews.remove(index, nodes.getLength());
            for (int i = 0, n = removed.getLength(); i < n; i++) {
                NodeView nodeView = removed.get(i);
                nodeView.dispose();
            }
        }

        private NodeView createNodeView(Node node) {
            NodeView nodeView = null;

            if (node instanceof Document) {
                nodeView = new DocumentView(this, (Document)node);
            } else if (node instanceof Paragraph) {
                nodeView = new ParagraphView(this, (Paragraph)node);
            } else if (node instanceof TextNode) {
                nodeView = new TextNodeView(this, (TextNode)node);
            } else {
                throw new IllegalArgumentException("Unsupported node type: "
                    + node.getClass().getName());
            }

            return nodeView;
        }
    }

    /**
     * Document view.
     *
     * @author gbrown
     */
    public class DocumentView extends ElementView {
        public DocumentView(ElementView parent, Document document) {
            super(parent, document);
        }

        public int getPreferredWidth(int height) {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getPreferredHeight(int width) {
            // TODO Auto-generated method stub
            return 0;
        }

        public Dimensions getPreferredSize() {
            // TODO Auto-generated method stub
            return null;
        }

        public void layout() {
            // TODO
        }

        @Override
        public void invalidate() {
            super.invalidate();

            TextAreaSkin.this.invalidateComponent();
        }

        @Override
        public NodeView breakAt(int x) {
            return this;
        }
    }

    public class ParagraphView extends ElementView {
        public ParagraphView(ElementView parent, Paragraph paragraph) {
            super(parent, paragraph);
        }

        @Override
        public void dispose() {
            // TODO

            super.dispose();
        }

        public int getPreferredWidth(int height) {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getPreferredHeight(int width) {
            // TODO Auto-generated method stub
            return 0;
        }

        public Dimensions getPreferredSize() {
            // TODO Auto-generated method stub
            return null;
        }

        public void layout() {
            // TODO
        }

        @Override
        public NodeView breakAt(int x) {
            return this;
        }
    }

    /**
     * Text node view.
     *
     * @author gbrown
     */
    public class TextNodeView extends NodeView implements TextNodeListener {
        public TextNodeView(ElementView parent, TextNode textNode) {
            super(parent, textNode);
        }

        public int getPreferredHeight(int width) {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getPreferredWidth(int height) {
            // TODO Auto-generated method stub
            return 0;
        }

        public Dimensions getPreferredSize() {
            // TODO Auto-generated method stub
            return null;
        }

        public void paint(Graphics2D graphics) {
            // TODO Auto-generated method stub

        }

        public NodeView breakAt(int x) {
            // TODO
            return null;
        }

        public void charactersInserted(TextNode textNode, int index, int count) {
            // TODO Auto-generated method stub

        }

        public void charactersRemoved(TextNode textNode, int index,
            String characters) {
            // TODO Auto-generated method stub
        }
    }

    private DocumentView documentView = null;

    public void install(Component component) {
        super.install(component);

        TextArea textArea = (TextArea)component;
        Document text = textArea.getText();

        if (text != null) {
            documentView = new DocumentView(null, text);
        }

        textArea.getTextAreaListeners().add(this);
    }

    public void uninstall() {
        if (documentView != null) {
            documentView.dispose();
        }

        documentView = null;

        TextArea textArea = (TextArea)getComponent();
        textArea.getTextAreaListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        return documentView.getPreferredWidth(height);
    }

    public int getPreferredHeight(int width) {
        return documentView.getPreferredHeight(width);
    }

    public Dimensions getPreferredSize() {
        return documentView.getPreferredSize();
    }

    public void layout() {
        documentView.validate();
    }

    public void paint(Graphics2D graphics) {
        documentView.paint(graphics);
    }

    @Override
    public boolean isFocusable() {
        // TODO Update Container#requestFocus() to only transfer focus to
        // first subcomponent if the container itself is not focusable;
        // alternatively, we could define an inner component that wraps the
        // document view and set focus to that.

        // Note that this is only an issue when the text area is enabled
        // (i.e. editable).


        return true;
    }

    public void textChanged(TextArea textArea, Document previousText) {
        if (documentView != null) {
            documentView.dispose();
        }

        Document text = textArea.getText();
        if (text != null) {
            documentView = new DocumentView(null, text);
        }
    }
}
