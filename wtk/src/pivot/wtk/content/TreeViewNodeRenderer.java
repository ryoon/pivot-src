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
package pivot.wtk.content;

import java.awt.Color;
import java.awt.Font;

import pivot.wtk.Component;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.TreeView;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.media.Image;

/**
 * Default tree node renderer.
 *
 * @author gbrown
 */
public class TreeViewNodeRenderer extends FlowPane implements TreeView.NodeRenderer {
    protected ImageView imageView = new ImageView();
    protected Label label = new Label();

    public static final int DEFAULT_ICON_WIDTH = 16;
    public static final int DEFAULT_ICON_HEIGHT = 16;
    public static boolean DEFAULT_SHOW_ICON = true;

    public TreeViewNodeRenderer() {
        super();

        getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        add(imageView);
        add(label);

        imageView.setPreferredSize(DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT);
        imageView.setDisplayable(DEFAULT_SHOW_ICON);

        setPreferredHeight(DEFAULT_ICON_HEIGHT);
    }

    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @SuppressWarnings("unchecked")
    public void render(Object node, TreeView treeView, boolean expanded,
        boolean selected, boolean highlighted, boolean disabled) {
        Image icon = null;
        String text = null;

        if (node instanceof TreeNode) {
            TreeNode treeNode = (TreeNode)node;

            if (expanded
                && treeNode instanceof TreeBranch) {
                TreeBranch treeBranch = (TreeBranch)treeNode;
                icon = treeBranch.getExpandedIcon();

                if (icon == null) {
                    icon = treeBranch.getIcon();
                }
            } else {
                icon = treeNode.getIcon();
            }

            text = treeNode.getText();
        } else if (node instanceof Image) {
            icon = (Image)node;
        } else {
            if (node != null) {
                text = node.toString();
            }
        }

        // Update the image view
        imageView.setImage(icon);
        imageView.getStyles().put("opacity",
            (treeView.isEnabled() && !disabled) ? 1.0f : 0.5f);

        // Show/hide the label
        if (text == null) {
            label.setDisplayable(false);
        } else {
            label.setDisplayable(true);
            label.setText(text);

            // Update the label styles
            Component.StyleDictionary labelStyles = label.getStyles();

            Object labelFont = treeView.getStyles().get("font");
            if (labelFont instanceof Font) {
                labelStyles.put("font", labelFont);
            }

            Object color = null;
            if (treeView.isEnabled() && !disabled) {
                if (selected) {
                    if (treeView.isFocused()) {
                        color = treeView.getStyles().get("selectionColor");
                    } else {
                        color = treeView.getStyles().get("inactiveSelectionColor");
                    }
                } else {
                    color = treeView.getStyles().get("color");
                }
            } else {
                color = treeView.getStyles().get("disabledColor");
            }

            if (color instanceof Color) {
                labelStyles.put("color", color);
            }
        }
    }

    public int getIconWidth() {
        return imageView.getPreferredWidth(-1);
    }

    public void setIconWidth(int iconWidth) {
        if (iconWidth == -1) {
            throw new IllegalArgumentException();
        }

        imageView.setPreferredWidth(iconWidth);
    }

    public int getIconHeight() {
        return imageView.getPreferredHeight(-1);
    }

    public void setIconHeight(int iconHeight) {
        if (iconHeight == -1) {
            throw new IllegalArgumentException();
        }

        imageView.setPreferredHeight(iconHeight);
    }

    public boolean getShowIcon() {
        return imageView.isDisplayable();
    }

    public void setShowIcon(boolean showIcon) {
        imageView.setDisplayable(showIcon);
    }
}
