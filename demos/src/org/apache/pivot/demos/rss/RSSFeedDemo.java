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
package org.apache.pivot.demos.rss;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.pivot.collections.Map;
import org.apache.pivot.io.IOTask;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BrowserApplicationContext;
import org.apache.pivot.wtk.CardPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FlowPane;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * RSS feed demo application.
 *
 * @author gbrown
 */
public class RSSFeedDemo implements Application {
    // Loads the feed in the background so the UI doesn't block
    private class LoadFeedTask extends IOTask<NodeList> {
        public NodeList execute() throws TaskExecutionException {
            NodeList itemNodeList = null;

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder;
            try {
                documentBuilder = documentBuilderFactory.newDocumentBuilder();
            } catch(ParserConfigurationException exception) {
                throw new TaskExecutionException(exception);
            }

            Document document;
            try {
                document = documentBuilder.parse(FEED_URI);
            } catch(IOException exception) {
                throw new TaskExecutionException(exception);
            } catch(SAXException exception) {
                throw new TaskExecutionException(exception);
            }

            try {
                itemNodeList = (NodeList)xpath.evaluate("/rss/channel/item",
                    document, XPathConstants.NODESET);
            } catch(XPathExpressionException exception) {
                // No-op
            }

            return itemNodeList;
        }
    }

    // Prepares an RSS feed news item for presentation in a list view
    private class RSSItemRenderer extends FlowPane implements ListView.ItemRenderer {
        private Label titleLabel = new Label();
        private Label categoriesHeadingLabel = new Label("subject:");
        private Label categoriesLabel = new Label();
        private Label submitterHeadingLabel = new Label("submitter:");
        private Label submitterLabel = new Label();

        public RSSItemRenderer() {
            super(Orientation.VERTICAL);

            getStyles().put("padding", new Insets(2, 2, 8, 2));

            add(titleLabel);

            FlowPane categoriesFlowPane = new FlowPane();
            add(categoriesFlowPane);

            categoriesFlowPane.add(categoriesHeadingLabel);
            categoriesFlowPane.add(categoriesLabel);

            FlowPane submitterFlowPane = new FlowPane();
            add(submitterFlowPane);

            submitterFlowPane.add(submitterHeadingLabel);
            submitterFlowPane.add(submitterLabel);
        }

        @Override
        public void setSize(int width, int height) {
            super.setSize(width, height);

            // Since this component doesn't have a parent, it won't be validated
            // via layout; ensure that it is valid here
            validate();
        }

        public void render(Object item, ListView listView, boolean selected,
            boolean checked, boolean highlighted, boolean disabled) {
            if (item != null) {
                Element itemElement = (Element)item;

                try {
                    String title = (String)xpath.evaluate("title", itemElement, XPathConstants.STRING);
                    titleLabel.setText(title);

                    String categories = "";
                    NodeList categoryNodeList = (NodeList)xpath.evaluate("category", itemElement,
                        XPathConstants.NODESET);
                    for (int j = 0; j < categoryNodeList.getLength(); j++) {
                        Element categoryElement = (Element)categoryNodeList.item(j);
                        String category = categoryElement.getTextContent();
                        if (j > 0) {
                            categories += ", ";
                        }

                        categories += category;
                    }

                    categoriesLabel.setText(categories);

                    String submitter = (String)xpath.evaluate("dz:submitter/dz:username", itemElement,
                        XPathConstants.STRING);
                    submitterLabel.setText(submitter);
                } catch(XPathExpressionException exception) {
                    System.err.println(exception);
                }
            }

            Font font = (Font)listView.getStyles().get("font");
            Font largeFont = font.deriveFont(Font.BOLD, 14);
            titleLabel.getStyles().put("font", largeFont);
            categoriesLabel.getStyles().put("font", font);
            submitterLabel.getStyles().put("font", font);

            Color color;
            if (listView.isEnabled() && !disabled) {
                if (selected) {
                    if (listView.isFocused()) {
                        color = (Color)listView.getStyles().get("selectionColor");
                    } else {
                        color = (Color)listView.getStyles().get("inactiveSelectionColor");
                    }
                } else {
                    color = (Color)listView.getStyles().get("color");
                }
            } else {
                color = (Color)listView.getStyles().get("disabledColor");
            }

            titleLabel.getStyles().put("color", color);
            categoriesHeadingLabel.getStyles().put("color", color);
            categoriesLabel.getStyles().put("color", color);
            submitterHeadingLabel.getStyles().put("color", color);
            submitterLabel.getStyles().put("color", color);
        }
    }

    // Handles double-clicks on the list view
    private class FeedViewMouseButtonHandler extends ComponentMouseButtonListener.Adapter {
        private int index = -1;

        @Override
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            if (count == 1) {
                index = feedListView.getItemAt(y);
            } else if (count == 2
                && feedListView.getItemAt(y) == index) {
                Element itemElement = (Element)feedListView.getListData().get(index);

                try {
                    String link = (String)xpath.evaluate("link", itemElement, XPathConstants.STRING);
                    BrowserApplicationContext.open(new URL(link));
                } catch(XPathExpressionException exception) {
                    System.err.print(exception);
                } catch(MalformedURLException exception) {
                    System.err.print(exception);
                }
            }

            return false;
        }
    };

    private XPath xpath;

    private Window window = null;

    @WTKX private ListView feedListView;
    @WTKX private CardPane cardPane;
    @WTKX private Label statusLabel;

    public static final String FEED_URI = "http://feeds.dzone.com/javalobby/frontpage?format=xml";

    public RSSFeedDemo() {
        // Create an XPath instance
        xpath = XPathFactory.newInstance().newXPath();

        // Set the namespace resolver
        xpath.setNamespaceContext(new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                String namespaceURI;
                if (prefix.equals("dz")) {
                    namespaceURI = "http://www.developerzone.com/modules/dz/1.0";
                } else {
                    namespaceURI = XMLConstants.NULL_NS_URI;
                }

                return namespaceURI;
            }

            public String getPrefix(String uri) {
                throw new UnsupportedOperationException();
            }

            public Iterator<String> getPrefixes(String uri) {
                throw new UnsupportedOperationException();
            }
        });
    }

    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "rss_feed_demo.wtkx");
        wtkxSerializer.bind(this, RSSFeedDemo.class);

        feedListView.setItemRenderer(new RSSItemRenderer());
        feedListView.getComponentMouseButtonListeners().add(new FeedViewMouseButtonHandler());

        LoadFeedTask loadFeedTask = new LoadFeedTask();
        loadFeedTask.execute(new TaskListener<NodeList>() {
            public void taskExecuted(Task<NodeList> task) {
                feedListView.setListData(new NodeListAdapter(task.getResult()));
                cardPane.setSelectedIndex(1);
            }

            public void executeFailed(Task<NodeList> task) {
                statusLabel.setText(task.getFault().toString());
            }
        });

        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }

        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(RSSFeedDemo.class, args);
    }
}
