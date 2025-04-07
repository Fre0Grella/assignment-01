package util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ChartGenerator {

    /**
     * Creates and saves a line chart with a variable number of data series.
     *
     * @param outputPath    The file path where the chart image will be saved.
     * @param title         The title of the chart.
     * @param xAxisLabel    The label for the X-axis.
     * @param yAxisLabel    The label for the Y-axis.
     * @param isPercentage  Format Y-axis to percentage format style.
     * @param dataSeries    A list of tuples containing the X values, Y values, and label for each series.
     */
    public void createLineChart(String outputPath, String title, String xAxisLabel, String yAxisLabel,
                                       boolean isPercentage, List<DataSeries> dataSeries) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (var seriesData : dataSeries) {
            XYSeries series = new XYSeries(seriesData.label);
            var xData = seriesData.xData;
            var yData = seriesData.yData;
            for (int i = 0; i < xData.size(); i++) {
                series.add(xData.get(i), yData.get(i));
            }
            dataset.addSeries(series);
        }

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false
        );

        XYPlot plot = chart.getXYPlot();

        // Configure X-axis for integer values
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // Configure Y-axis
        if (isPercentage) {
            NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
            yAxis.setRange(0, 110);
            yAxis.setLabel(yAxisLabel);
        }
        //else {
        //    NumberAxis yAxis = (NumberAxis) plot.getDomainAxis();
        //    yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        //}

        // Set white background
        plot.setBackgroundPaint(Color.WHITE);

        // Configure dotted grid lines
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setDomainGridlineStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2.0f, 2.0f}, 0));
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setRangeGridlineStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2.0f, 2.0f}, 0));

        // Configure line styles and markers with rainbow colors
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        Color[] rainbowColors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN};
        for (int i = 0; i < dataSeries.size(); i++) {
            renderer.setSeriesShapesVisible(i, true);
            renderer.setSeriesPaint(i, rainbowColors[i % rainbowColors.length]);
            if (i % 2 == 0) {
                renderer.setSeriesShape(i, new Rectangle(5, 5));
            } else {
                renderer.setSeriesShape(i, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6));
            }
        }
        plot.setRenderer(renderer);

        // Save image
        try {
            ChartUtils.saveChartAsPNG(new File(outputPath), chart, 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exampleUsage() {
        // Example usage
        var ciao = new DataSeries(List.of(3.0,4.0,5.0),List.of(4.0,5.0,6.0),"ciao");
        new ChartGenerator().createLineChart(
                "strongScalingChart.png",
                "Strong scaling efficiency",
                "N. of cores",
                "Efficiency",
                false,
                List.of(

                        new DataSeries(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0),
                                List.of(100.0, 99.0, 99.5, 99.7, 99.8, 99.9, 99.6, 99.5, 99.3, 99.2, 99.0, 97.0),
                                "N large, IT small"),

                        new DataSeries(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0),
                                List.of(95.0, 95.0, 92.0, 90.0, 87.0, 85.0, 80.0, 75.0, 70.0, 65.0, 60.0, 50.0),
                                "N "),

                        new DataSeries(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0),
                                List.of(150.0, 95.0, 92.0, 90.0, 87.0, 85.0, 80.0, 75.0, 70.0, 65.0, 60.0, 50.0),
                                "Na"),

                        new DataSeries(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0),
                                List.of(10.0, 95.0, 92.0, 90.0, 87.0, 85.0, 80.0, 75.0, 70.0, 65.0, 60.0, 50.0),
                                "N small, IT "),

                        new DataSeries(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0),
                                List.of(130.0, 95.0, 92.0, 90.0, 87.0, 85.0, 80.0, 75.0, 70.0, 65.0, 60.0, 50.0),
                                "N small,  large"),

                        new DataSeries(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0),
                                List.of(90.0, 95.0, 92.0, 90.0, 87.0, 85.0, 80.0, 75.0, 70.0, 65.0, 60.0, 50.0),
                                "N , IT large"),

                        new DataSeries(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0),
                                List.of(180.0, 95.0, 92.0, 90.0, 87.0, 85.0, 80.0, 75.0, 70.0, 65.0, 60.0, 50.0),
                                "N smalle"),

                        new DataSeries(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0),
                                List.of(120.0, 95.0, 92.0, 90.0, 87.0, 85.0, 80.0, 75.0, 70.0, 65.0, 60.0, 50.0),
                                "N smrge")
                )
        );
    }

    /**
         * A helper class to store three related objects together.
         */
        public record DataSeries(List<Double> xData, List<Double> yData, String label) {
    }
}
