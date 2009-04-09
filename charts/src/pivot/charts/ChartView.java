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
package pivot.charts;

import java.util.Comparator;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;
import pivot.util.Service;
import pivot.wtk.Component;

/**
 * Abstract base class for chart views.
 *
 * @author gbrown
 */
public abstract class ChartView extends Component {
    /**
     * Represents a chart category.
     */
    public static class Category {
        private ChartView chartView = null;

        private String key;
        private String label;

        public Category() {
            this(null, null);
        }

        public Category(String key) {
            this(key, key);
        }

        public Category(String key, String label) {
            this.key = key;
            this.label = label;
        }

        public ChartView getChartView() {
            return chartView;
        }

        private void setChartView(ChartView chartView) {
            this.chartView = chartView;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key)  {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            String previousKey = this.key;

            if (previousKey != key) {
                this.key = key;

                if (chartView != null) {
                    chartView.chartViewCategoryListeners.categoryKeyChanged(chartView,
                        chartView.categories.indexOf(this), previousKey);
                }
            }
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            if (label == null) {
                throw new IllegalArgumentException("label is null.");
            }

            String previousLabel = this.label;

            if (previousLabel != label) {
                this.label = label;

                if (chartView != null) {
                    chartView.chartViewCategoryListeners.categoryLabelChanged(chartView,
                        chartView.categories.indexOf(this), previousLabel);
                }
            }
        }
    }

    /**
     * Represents an element of a chart, such as a bar or a pie wedge.
     */
    public static class Element {
        private int seriesIndex;
        private int elementIndex;

        public Element(int seriesIndex, int elementIndex) {
            this.seriesIndex = seriesIndex;
            this.elementIndex = elementIndex;
        }

        /**
         * Returns the element's series index.
         *
         * @return
         * The element's series index.
         */
        public int getSeriesIndex() {
            return seriesIndex;
        }

        /**
         * Returns the element's index within its series. For a category series,
         * the element index represents the index of the category in the
         * category sequence. Otherwise, it represents the index of the item
         * within the series.
         *
         * @return
         * The element index.
         */
        public int getElementIndex() {
            return elementIndex;
        }

        public String toString() {
            String string = getClass().getName()
                + seriesIndex + ", " + elementIndex;
            return string;
        }
    }

    /**
     * Chart view skin interface.
     */
    public interface Skin {
        public Element getElementAt(int x, int y);
    }

    /**
     * Internal class for managing the chart's category list.
     */
    public final class CategorySequence implements Sequence<Category> {
        public int add(Category category) {
            int i = getLength();
            insert(category, i);

            return i;
        }

        public void insert(Category category, int index) {
            if (category == null) {
                throw new IllegalArgumentException("category is null.");
            }

            if (category.getChartView() != null) {
                throw new IllegalArgumentException("category is already in use by another chart view.");
            }

            categories.insert(category, index);
            category.setChartView(ChartView.this);

            chartViewCategoryListeners.categoryInserted(ChartView.this, index);
        }

        public Category update(int index, Category category) {
            throw new UnsupportedOperationException();
        }

        public int remove(Category category) {
            int index = indexOf(category);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        public Sequence<Category> remove(int index, int count) {
            Sequence<Category> removed = categories.remove(index, count);

            if (count > 0) {
                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    removed.get(i).setChartView(null);
                }

                chartViewCategoryListeners.categoriesRemoved(ChartView.this, index, removed);
            }

            return removed;
        }

        public Category get(int index) {
            return categories.get(index);
        }

        public int indexOf(Category category) {
            return categories.indexOf(category);
        }

        public int getLength() {
            return categories.getLength();
        }
    }

    /**
     * List event handler.
     */
    private class ListHandler implements ListListener<Object> {
        public void itemInserted(List<Object> list, int index) {
            // Notify listeners that items were inserted
            chartViewSeriesListeners.seriesInserted(ChartView.this, index);
        }

        public void itemsRemoved(List<Object> list, int index, Sequence<Object> items) {
            if (items == null) {
                // All items were removed; clear the selection and notify
                // listeners
                chartViewSeriesListeners.seriesRemoved(ChartView.this, index, -1);
            } else {
                // Notify listeners that items were removed
                int count = items.getLength();
                chartViewSeriesListeners.seriesRemoved(ChartView.this, index, count);
            }
        }

        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            chartViewSeriesListeners.seriesUpdated(ChartView.this, index);
        }

        public void comparatorChanged(List<Object> list,
            Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                chartViewSeriesListeners.seriesSorted(ChartView.this);
            }
        }
    }

    /**
     * Chart view listener list.
     */
    private class ChartViewListenerList extends ListenerList<ChartViewListener>
        implements ChartViewListener {
        public void chartDataChanged(ChartView chartView, List<?> previousChartData) {
            for (ChartViewListener listener : this) {
                listener.chartDataChanged(chartView, previousChartData);
            }
        }

        public void seriesNameKeyChanged(ChartView chartView, String previousSeriesNameKey) {
            for (ChartViewListener listener : this) {
                listener.seriesNameKeyChanged(chartView, previousSeriesNameKey);
            }
        }

        public void titleChanged(ChartView chartView, String previousTitle) {
            for (ChartViewListener listener : this) {
                listener.titleChanged(chartView, previousTitle);
            }
        }

        public void horizontalAxisLabelChanged(ChartView chartView, String previousXAxisLabel) {
            for (ChartViewListener listener : this) {
                listener.horizontalAxisLabelChanged(chartView, previousXAxisLabel);
            }
        }

        public void verticalAxisLabelChanged(ChartView chartView, String previousYAxisLabel) {
            for (ChartViewListener listener : this) {
                listener.verticalAxisLabelChanged(chartView, previousYAxisLabel);
            }
        }

        public void showLegendChanged(ChartView chartView) {
            for (ChartViewListener listener : this) {
                listener.showLegendChanged(chartView);
            }
        }
    }

    /**
     * Chart view category listener list.
     */
    private class ChartViewCategoryListenerList extends ListenerList<ChartViewCategoryListener>
        implements ChartViewCategoryListener {
        public void categoryInserted(ChartView chartView, int index) {
            for (ChartViewCategoryListener listener : this) {
                listener.categoryInserted(chartView, index);
            }
        }

        public void categoriesRemoved(ChartView chartView, int index, Sequence<ChartView.Category> categories) {
            for (ChartViewCategoryListener listener : this) {
                listener.categoriesRemoved(chartView, index, categories);
            }
        }

        public void categoryKeyChanged(ChartView chartView, int index, String previousKey) {
            for (ChartViewCategoryListener listener : this) {
                listener.categoryKeyChanged(chartView, index, previousKey);
            }
        }

        public void categoryLabelChanged(ChartView chartView, int index, String previousLabel) {
            for (ChartViewCategoryListener listener : this) {
                listener.categoryLabelChanged(chartView, index, previousLabel);
            }
        }
    }

    /**
     * Chart view series listener list.
     */
    private class ChartViewSeriesListenerList extends ListenerList<ChartViewSeriesListener>
        implements ChartViewSeriesListener {
        public void seriesInserted(ChartView chartView, int index) {
            for (ChartViewSeriesListener listener : this) {
                listener.seriesInserted(chartView, index);
            }
        }

        public void seriesRemoved(ChartView chartView, int index, int count) {
            for (ChartViewSeriesListener listener : this) {
                listener.seriesRemoved(chartView, index, count);
            }
        }

        public void seriesUpdated(ChartView chartView, int index) {
            for (ChartViewSeriesListener listener : this) {
                listener.seriesUpdated(chartView, index);
            }
        }

        public void seriesSorted(ChartView chartView) {
            for (ChartViewSeriesListener listener : this) {
                listener.seriesSorted(chartView);
            }
        }
    }

    private List<?> chartData;
    private String seriesNameKey;

    private String title = null;
    private String horizontalAxisLabel = null;
    private String verticalAxisLabel = null;
    private boolean showLegend;

    private ArrayList<Category> categories = new ArrayList<Category>();
    private CategorySequence categorySequence = new CategorySequence();

    private ListHandler chartDataHandler = new ListHandler();

    private ChartViewListenerList chartViewListeners = new ChartViewListenerList();
    private ChartViewCategoryListenerList chartViewCategoryListeners = new ChartViewCategoryListenerList();
    private ChartViewSeriesListenerList chartViewSeriesListeners = new ChartViewSeriesListenerList();

    public static final String DEFAULT_SERIES_NAME_KEY = "name";
    public static final String PROVIDER_NAME = "pivot.charts.Provider";

    private static Provider provider = null;

    static {
        provider = (Provider)Service.getProvider(PROVIDER_NAME);

        if (provider == null) {
            throw new ProviderNotFoundException();
        }
    }

    public ChartView() {
        this(DEFAULT_SERIES_NAME_KEY, new ArrayList<Object>());
    }

    public ChartView(String seriesNameKey, List<?> chartData) {
        setSeriesNameKey(seriesNameKey);
        setTitle(title);
        setChartData(chartData);
        setShowLegend(showLegend);
    }

    protected void installChartSkin(Class<? extends ChartView> chartViewClass) {
        Class<? extends pivot.wtk.Skin> skinClass = provider.getSkinClass(chartViewClass);

        try {
            setSkin(skinClass.newInstance());
        } catch(InstantiationException exception) {
            throw new IllegalArgumentException(exception);
        } catch(IllegalAccessException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    public CategorySequence getCategories() {
        return categorySequence;
    }

    public List<?> getChartData() {
        return chartData;
    }

    @SuppressWarnings("unchecked")
    public void setChartData(List<?> chartData) {
        if (chartData == null) {
            throw new IllegalArgumentException("chartData is null.");
        }

        List<?> previousChartData = this.chartData;

        if (previousChartData != chartData) {
            if (previousChartData != null) {
                ((List<Object>)previousChartData).getListListeners().remove(chartDataHandler);
            }

            ((List<Object>)chartData).getListListeners().add(chartDataHandler);

            this.chartData = chartData;
            chartViewListeners.chartDataChanged(this, previousChartData);
        }
    }

    public String getSeriesNameKey() {
        return seriesNameKey;
    }

    public void setSeriesNameKey(String seriesNameKey) {
        if (seriesNameKey == null) {
            throw new IllegalArgumentException("seriesNameKey is null.");
        }

        String previousSeriesNameKey = this.seriesNameKey;

        if (previousSeriesNameKey != seriesNameKey) {
            this.seriesNameKey = seriesNameKey;
            chartViewListeners.seriesNameKeyChanged(this, previousSeriesNameKey);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        String previousTitle = this.title;

        if (previousTitle != title) {
            this.title = title;
            chartViewListeners.titleChanged(this, previousTitle);
        }
    }

    public String getHorizontalAxisLabel() {
        return horizontalAxisLabel;
    }

    public void setHorizontalAxisLabel(String horizontalAxisLabel) {
        String previousHorizontalAxisLabel = this.horizontalAxisLabel;

        if (previousHorizontalAxisLabel != horizontalAxisLabel) {
            this.horizontalAxisLabel = horizontalAxisLabel;
            chartViewListeners.horizontalAxisLabelChanged(this, previousHorizontalAxisLabel);
        }
    }

    public String getVerticalAxisLabel() {
        return verticalAxisLabel;
    }

    public void setVerticalAxisLabel(String verticalAxisLabel) {
        String previousVerticalAxisLabel = this.verticalAxisLabel;

        if (previousVerticalAxisLabel != verticalAxisLabel) {
            this.verticalAxisLabel = verticalAxisLabel;
            chartViewListeners.verticalAxisLabelChanged(this, previousVerticalAxisLabel);
        }
    }

    public boolean getShowLegend() {
        return showLegend;
    }

    public void setShowLegend(boolean showLegend) {
        if (this.showLegend != showLegend) {
            this.showLegend = showLegend;
            chartViewListeners.showLegendChanged(this);
        }
    }

    public Element getElementAt(int x, int y) {
        return ((Skin)getSkin()).getElementAt(x, y);
    }

    public ListenerList<ChartViewListener> getChartViewListeners() {
        return chartViewListeners;
    }

    public ListenerList<ChartViewCategoryListener> getChartViewCategoryListeners() {
        return chartViewCategoryListeners;
    }

    public ListenerList<ChartViewSeriesListener> getChartViewSeriesListeners() {
        return chartViewSeriesListeners;
    }
}

