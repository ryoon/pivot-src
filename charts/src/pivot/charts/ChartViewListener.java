package pivot.charts;

import org.jfree.chart.JFreeChart;

public interface ChartViewListener {
    public void chartChanged(ChartView chartView, JFreeChart previousChart);
}
