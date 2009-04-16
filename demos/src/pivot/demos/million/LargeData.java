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
package pivot.demos.million;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.serialization.CSVSerializer;
import pivot.serialization.SerializationException;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Display;
import pivot.wtk.Label;
import pivot.wtk.ListButton;
import pivot.wtk.PushButton;
import pivot.wtk.TableView;
import pivot.wtk.TableViewHeader;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class LargeData implements Application {
    private class LoadDataCallback implements Runnable {
        private class AddRowsCallback implements Runnable {
            private ArrayList<Object> page;

            public AddRowsCallback(ArrayList<Object> page) {
                this.page = page;
            }

            @SuppressWarnings("unchecked")
            public void run() {
                List<Object> tableData = (List<Object>)tableView.getTableData();
                for (Object item : page) {
                    tableData.add(item);
                }
            }
        }

        private URL fileURL;

        public LoadDataCallback(URL fileURL) {
            this.fileURL = fileURL;
        }

        public void run() {
            Exception fault = null;

            long t0 = System.currentTimeMillis();

            int i = 0;
            try {
                InputStream inputStream = null;

                try {
                    inputStream = fileURL.openStream();

                    CSVSerializer csvSerializer = new CSVSerializer("ISO-8859-1");
                    csvSerializer.getKeys().add("c0");
                    csvSerializer.getKeys().add("c1");
                    csvSerializer.getKeys().add("c2");
                    csvSerializer.getKeys().add("c3");

                    CSVSerializer.StreamIterator streamIterator = csvSerializer.getStreamIterator(inputStream);

                    ArrayList<Object> page = new ArrayList<Object>(PAGE_SIZE);
                    while (streamIterator.hasNext()
                        && !abort) {
                        Object item = streamIterator.next();
                        if (item != null) {
                            page.add(item);
                        }
                        i++;

                        if (!streamIterator.hasNext()
                            || page.getLength() == PAGE_SIZE) {
                            ApplicationContext.queueCallback(new AddRowsCallback(page));
                            page = new ArrayList<Object>(PAGE_SIZE);
                        }
                    }
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } catch(IOException exception) {
                fault = exception;
            } catch(SerializationException exception) {
                fault = exception;
            }

            long t1 = System.currentTimeMillis();

            final String status;
            if (abort) {
                status = "Aborted";
            } else if (fault != null) {
                status = fault.toString();
            } else {
                status = "Read " + i + " rows in " + (t1 - t0) + "ms";
            }

            ApplicationContext.queueCallback(new Runnable() {
                public void run() {
                    statusLabel.setText(status);
                    loadDataButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                }
            });
        }
    }

    private String basePath = null;

	private Window window = null;

    private ListButton fileListButton = null;
    private PushButton loadDataButton = null;
    private PushButton cancelButton = null;
    private Label statusLabel = null;
    private TableView tableView = null;
    private TableViewHeader tableViewHeader = null;

    private CSVSerializer csvSerializer;

    private volatile boolean abort = false;

    private static final String BASE_PATH_KEY = "basePath";
    private static final int PAGE_SIZE = 100;

    public LargeData() {
    	csvSerializer = new CSVSerializer("ISO-8859-1");
    	csvSerializer.getKeys().add("c0");
    	csvSerializer.getKeys().add("c1");
    	csvSerializer.getKeys().add("c2");
    	csvSerializer.getKeys().add("c3");
    }

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        basePath = properties.get(BASE_PATH_KEY);
        if (basePath == null) {
            throw new IllegalArgumentException("basePath is required.");
        }

        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(getClass().getResource("large_data.wtkx"));

        fileListButton = (ListButton)wtkxSerializer.getObjectByName("fileListButton");

        loadDataButton = (PushButton)wtkxSerializer.getObjectByName("loadDataButton");
        loadDataButton.getButtonPressListeners().add(new ButtonPressListener() {
        	public void buttonPressed(Button button) {
        	    loadDataButton.setEnabled(false);
        		cancelButton.setEnabled(true);

        		loadData();
        	}
        });

        cancelButton = (PushButton)wtkxSerializer.getObjectByName("cancelButton");
        cancelButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                abort = true;

                loadDataButton.setEnabled(true);
                cancelButton.setEnabled(false);
            }
        });

        statusLabel = (Label)wtkxSerializer.getObjectByName("statusLabel");

        tableView = (TableView)wtkxSerializer.getObjectByName("tableView");

        tableViewHeader = (TableViewHeader)wtkxSerializer.getObjectByName("tableViewHeader");
        tableViewHeader.getTableViewHeaderPressListeners().add(new TableView.SortHandler() {
        	@Override
        	public void headerPressed(TableViewHeader tableViewHeader, int index) {
        		long startTime = System.currentTimeMillis();
        		super.headerPressed(tableViewHeader, index);
        		long endTime = System.currentTimeMillis();

        		statusLabel.setText("Data sorted in " + (endTime - startTime) + " ms.");
        	}
        });

        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    private void loadData() {
        abort = false;
        tableView.getTableData().clear();

    	String fileName = (String)fileListButton.getSelectedItem();

    	URL origin = ApplicationContext.getOrigin();

    	URL fileURL = null;
    	try {
    	    fileURL = new URL(origin, basePath + "/" + fileName);
    	} catch(MalformedURLException exception) {
    	    System.err.println(exception.getMessage());
    	}

    	if (fileURL != null) {
    	    statusLabel.setText("Loading " + fileURL);

    	    LoadDataCallback callback = new LoadDataCallback(fileURL);
    	    Thread thread = new Thread(callback);
    	    thread.setDaemon(true);
    	    thread.setPriority(Thread.MIN_PRIORITY);
    	    thread.start();
    	}
    }
}
