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
package pivot.tutorials;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.collections.Map;
import pivot.serialization.JSONSerializer;
import pivot.util.CalendarDate;
import pivot.wtk.Action;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Border;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.ComponentStateListener;
import pivot.wtk.DragSource;
import pivot.wtk.DropAction;
import pivot.wtk.DropTarget;
import pivot.wtk.ImageView;
import pivot.wtk.ListView;
import pivot.wtk.LocalManifest;
import pivot.wtk.Manifest;
import pivot.wtk.Menu;
import pivot.wtk.MenuPopup;
import pivot.wtk.MessageType;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.Prompt;
import pivot.wtk.PushButton;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Slider;
import pivot.wtk.SliderValueListener;
import pivot.wtk.Spinner;
import pivot.wtk.TableView;
import pivot.wtk.TableViewHeader;
import pivot.wtk.TextArea;
import pivot.wtk.Visual;
import pivot.wtk.Window;
import pivot.wtk.content.CalendarDateSpinnerData;
import pivot.wtk.content.NumericSpinnerData;
import pivot.wtk.content.TableRow;
import pivot.wtk.content.TableViewHeaderData;
import pivot.wtk.effects.ReflectionDecorator;
import pivot.wtk.effects.WatermarkDecorator;
import pivot.wtk.media.Image;
import pivot.wtk.text.Document;
import pivot.wtk.text.PlainTextSerializer;
import pivot.wtkx.WTKXSerializer;

public class Demo implements Application {
    private MenuPopup menuPopup = null;
    private ImageView menuImageView = null;

    private Slider redSlider = null;
    private Slider greenSlider = null;
    private Slider blueSlider = null;
    private Border colorBorder = null;

    private TableView sortableTableView = null;
    private TableView customTableView = null;
    private TableViewHeader sortableTableViewHeader = null;

    private PushButton alertButton = null;
    private PushButton promptButton = null;

    private Window window = null;

    public void startup(final Display display, Dictionary<String, String> properties) throws Exception {
        // pivot.wtk.Theme.setTheme(new pivot.wtk.skin.terra.TerraTheme("test"));

        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        Component content = (Component)wtkxSerializer.readObject("pivot/tutorials/demo.wtkx");

        // Text
        PlainTextSerializer plainTextSerializer = new PlainTextSerializer("UTF-8");
        InputStream inputStream = getClass().getResourceAsStream("text_area.txt");

        Document document = null;
        try {
            document = plainTextSerializer.readObject(inputStream);
        } catch(Exception exception) {
            System.out.println(exception);
        }

        TextArea textArea = (TextArea)wtkxSerializer.getObjectByName("text.textArea");
        textArea.setDocument(document);

        final WatermarkDecorator watermarkDecorator = new WatermarkDecorator("Preview");
        watermarkDecorator.setOpacity(0.1f);
        watermarkDecorator.setFont(watermarkDecorator.getFont().deriveFont(Font.BOLD, 24));

        textArea.getDecorators().add(watermarkDecorator);

        textArea.getComponentStateListeners().add(new ComponentStateListener() {
            public void enabledChanged(Component component) {
                // No-op
            }

            public void focusedChanged(Component component, boolean temporary) {
                component.getDecorators().remove(watermarkDecorator);
                component.getComponentStateListeners().remove(this);
            }
        });

        new Action("selectImageAction") {
            public String getDescription() {
                return "Select Image Action";
            }

            public void perform() {
                Button.Group imageMenuGroup = Button.getGroup("imageMenuGroup");
                Button selectedItem = imageMenuGroup.getSelection();

                String imageName = (String)selectedItem.getUserData();

                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                URL imageURL = classLoader.getResource(imageName);

                // If the image has not been added to the resource cache yet,
                // add it
                Image image = (Image)ApplicationContext.getResourceCache().get(imageURL);

                if (image == null) {
                    image = Image.load(imageURL);
                    ApplicationContext.getResourceCache().put(imageURL, image);
                }

                // Update the image
                menuImageView.setImage(image);
            }
        };

        ListView iconListView = (ListView)wtkxSerializer.getObjectByName("lists.iconListView");
        iconListView.setItemDisabled(3, true);
        iconListView.setItemDisabled(4, true);

        ListView checkedListView = (ListView)wtkxSerializer.getObjectByName("lists.checkedListView");
        checkedListView.setItemChecked(0, true);
        checkedListView.setItemChecked(2, true);
        checkedListView.setItemChecked(3, true);

        menuImageView = (ImageView)wtkxSerializer.getObjectByName("menus.imageView");
        menuImageView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener() {
            public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
                if (button == Mouse.Button.RIGHT) {
                    menuPopup.open(display, component.mapPointToAncestor(display, x, y));
                }

                return false;
            }

            public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
                return false;
            }

            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                return false;
            }
        });

        // Spinners
        Spinner numericSpinner = (Spinner)wtkxSerializer.getObjectByName("spinners.numericSpinner");
        initializeNumericSpinner(numericSpinner);

        Spinner dateSpinner = (Spinner)wtkxSerializer.getObjectByName("spinners.dateSpinner");
        initializeDateSpinner(dateSpinner);

        // Sliders
        SliderValueListener sliderValueListener = new SliderValueListener() {
            public void valueChanged(Slider slider, int previousValue) {
                Color color = new Color(redSlider.getValue(), greenSlider.getValue(),
                    blueSlider.getValue());
                colorBorder.getStyles().put("backgroundColor", color);
            }
        };

        redSlider = (Slider)wtkxSerializer.getObjectByName("spinners.redSlider");
        redSlider.getSliderValueListeners().add(sliderValueListener);

        greenSlider = (Slider)wtkxSerializer.getObjectByName("spinners.greenSlider");
        greenSlider.getSliderValueListeners().add(sliderValueListener);

        blueSlider = (Slider)wtkxSerializer.getObjectByName("spinners.blueSlider");
        blueSlider.getSliderValueListeners().add(sliderValueListener);

        Color color = new Color(redSlider.getValue(), greenSlider.getValue(),
            blueSlider.getValue());
        colorBorder = (Border)wtkxSerializer.getObjectByName("spinners.colorBorder");
        colorBorder.getStyles().put("backgroundColor", color);

        sortableTableView = (TableView)wtkxSerializer.getObjectByName("tables.sortableTableView");
        sortableTableViewHeader = (TableViewHeader)wtkxSerializer.getObjectByName("tables.sortableTableViewHeader");
        customTableView = (TableView)wtkxSerializer.getObjectByName("tables.customTableView");
        initializeTableViews();

        DragSource imageDragSource = new DragSource() {
            private Image image = null;
            private Point offset = null;
            private LocalManifest content = null;

            public boolean beginDrag(Component component, int x, int y) {
                ImageView imageView = (ImageView)component;
                image = imageView.getImage();

                if (image != null) {
                    imageView.setImage((Image)null);
                    content = new LocalManifest();
                    content.putImage(image);
                    offset = new Point(x - (imageView.getWidth() - image.getWidth()) / 2,
                        y - (imageView.getHeight() - image.getHeight()) / 2);
                }

                return (image != null);
            }

            public void endDrag(Component component, DropAction dropAction) {
                if (dropAction == null) {
                    ImageView imageView = (ImageView)component;
                    imageView.setImage(image);
                }

                image = null;
                offset = null;
                content = null;
            }

            public boolean isNative() {
                return false;
            }

            public LocalManifest getContent() {
                return content;
            }

            public Visual getRepresentation() {
                return image;
            }

            public Point getOffset() {
                return offset;
            }

            public int getSupportedDropActions() {
                return DropAction.MOVE.getMask();
            }
        };

        DropTarget imageDropTarget = new DropTarget() {
            public DropAction dragEnter(Component component, Manifest dragContent,
                int supportedDropActions, DropAction userDropAction) {
                DropAction dropAction = null;

                ImageView imageView = (ImageView)component;
                if (imageView.getImage() == null
                    && dragContent.containsImage()
                    && DropAction.MOVE.isSelected(supportedDropActions)) {
                    dropAction = DropAction.MOVE;
                    component.getStyles().put("backgroundColor", "#f0e68c");
                }

                return dropAction;
            }

            public void dragExit(Component component) {
                component.getStyles().put("backgroundColor", null);
            }

            public DropAction dragMove(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsImage() ? DropAction.MOVE : null);
            }

            public DropAction userDropActionChange(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsImage() ? DropAction.MOVE : null);
            }

            public DropAction drop(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                DropAction dropAction = null;

                if (dragContent.containsImage()) {
                    ImageView imageView = (ImageView)component;
                    try {
                        imageView.setImage(dragContent.getImage());
                        dropAction = DropAction.MOVE;
                    } catch(IOException exception) {
                        System.err.println(exception);
                    }
                }

                dragExit(component);

                return dropAction;
            }
        };

        ImageView imageView1 = (ImageView)wtkxSerializer.getObjectByName("dragdrop.imageView1");
        imageView1.setDragSource(imageDragSource);
        imageView1.setDropTarget(imageDropTarget);

        ImageView imageView2 = (ImageView)wtkxSerializer.getObjectByName("dragdrop.imageView2");
        imageView2.setDragSource(imageDragSource);
        imageView2.setDropTarget(imageDropTarget);

        ImageView imageView3 = (ImageView)wtkxSerializer.getObjectByName("dragdrop.imageView3");
        imageView3.setDragSource(imageDragSource);
        imageView3.setDropTarget(imageDropTarget);

        alertButton = (PushButton)wtkxSerializer.getObjectByName("alerts.alertButton");
        promptButton = (PushButton)wtkxSerializer.getObjectByName("alerts.promptButton");
        initializeAlertButtons();

        menuPopup = new MenuPopup((Menu)wtkxSerializer.readObject("pivot/tutorials/menu_popup.wtkx"));

        window = new Window();
        window.setTitle("Pivot Demo");
        window.setMaximized(true);
        window.setContent(content);

        window.open(display);
    }

    @SuppressWarnings("unchecked")
    private void initializeTableViews() {
        // Set table header data
        TableView.ColumnSequence columns = sortableTableView.getColumns();
        columns.get(0).setHeaderData(new TableViewHeaderData("#"));
        columns.get(1).setHeaderData(new TableViewHeaderData("A"));
        columns.get(2).setHeaderData(new TableViewHeaderData("B"));
        columns.get(3).setHeaderData(new TableViewHeaderData("C"));
        columns.get(4).setHeaderData(new TableViewHeaderData("D"));

        // Populate table
        ArrayList<Object> tableData = new ArrayList<Object>();

        for (int i = 0; i < 10000; i++) {
            TableRow tableRow = new TableRow();

            tableRow.put("i", i);
            tableRow.put("a", (int)Math.round(Math.random() * 10));
            tableRow.put("b", (int)Math.round(Math.random() * 100));
            tableRow.put("c", (int)Math.round(Math.random() * 1000));
            tableRow.put("d", (int)Math.round(Math.random() * 10000));

            tableData.add(tableRow);
        }

        sortableTableView.setTableData(tableData);

        // Install header press listener
        sortableTableViewHeader.getTableViewHeaderPressListeners().add(new TableView.SortHandler());

        customTableView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener() {
            public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
               return false;
            }

            public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
               return false;
            }

            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
               if (button == Mouse.Button.LEFT) {
                   List<CustomTableRow> customTableData =
                       (List<CustomTableRow>)customTableView.getTableData();

                  int columnIndex = customTableView.getColumnAt(x);
                  if (columnIndex == 0) {
                     int rowIndex = customTableView.getRowAt(y);
                     CustomTableRow row = customTableData.get(rowIndex);

                     row.setA(!row.getA());
                     customTableData.update(rowIndex, row);
                  }
               }

               return false;
            }
        });
    }

    private void initializeNumericSpinner(Spinner numericSpinner) {
        NumericSpinnerData numericSpinnerData = new NumericSpinnerData(0, 256, 4);
        numericSpinner.setSpinnerData(numericSpinnerData);
        numericSpinner.setSelectedIndex(0);
    }

    private void initializeDateSpinner(Spinner dateSpinner) {
        CalendarDate lowerBound = new CalendarDate(2008, 0, 0);
        CalendarDate upperBound = new CalendarDate(2019, 11, 30);
        CalendarDateSpinnerData spinnerData = new CalendarDateSpinnerData(lowerBound, upperBound);

        CalendarDate today = new CalendarDate();
        dateSpinner.setSpinnerData(spinnerData);
        dateSpinner.setSelectedItem(today);
    }

    private void initializeAlertButtons() {
        alertButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Button.Group messageTypeGroup = Button.getGroup("messageType");
                Button selection = messageTypeGroup.getSelection();

                Map<String, ?> userData = JSONSerializer.parseMap((String)selection.getUserData());
                String messageType = (String)userData.get("type");

                if (messageType.equals("custom")) {
                    ArrayList<String> options = new ArrayList<String>();
                    options.add("OK");
                    options.add("Cancel");

                    Component body = null;
                    WTKXSerializer wtkxSerializer = new WTKXSerializer();
                    try {
                        body = (Component)wtkxSerializer.readObject("pivot/tutorials/alert.wtkx");
                    } catch(Exception exception) {
                        System.out.println(exception);
                    }

                    Alert alert = new Alert(MessageType.QUESTION, "Please select your favorite icon:",
                        options, body);
                    alert.setTitle("Select Icon");
                    alert.setSelectedOption(0);
                    alert.getDecorators().update(0, new ReflectionDecorator());

                    alert.open(window);
                } else {
                    String message = (String)userData.get("message");
                    Alert.alert(MessageType.decode(messageType), message, window);
                }
            }
        });

        promptButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Button.Group messageTypeGroup = Button.getGroup("messageType");
                Button selection = messageTypeGroup.getSelection();

                Map<String, ?> userData = JSONSerializer.parseMap((String)selection.getUserData());
                String messageType = (String)userData.get("type");

                if (messageType.equals("custom")) {
                    ArrayList<String> options = new ArrayList<String>();
                    options.add("OK");
                    options.add("Cancel");

                    Component body = null;
                    WTKXSerializer wtkxSerializer = new WTKXSerializer();
                    try {
                        body = (Component)wtkxSerializer.readObject("pivot/tutorials/alert.wtkx");
                    } catch(Exception exception) {
                        System.out.println(exception);
                    }

                    Prompt prompt = new Prompt(MessageType.QUESTION, "Please select your favorite icon:",
                        options, body);
                    prompt.setTitle("Select Icon");
                    prompt.setSelectedOption(0);
                    prompt.getDecorators().update(0, new ReflectionDecorator());

                    prompt.open(window);
                } else {
                    String message = (String)userData.get("message");
                    Prompt.prompt(MessageType.decode(messageType), message, window);
                }
            }
        });
    }

    public boolean shutdown(boolean optional) throws Exception {
        window.close();
        return true;
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }
}
