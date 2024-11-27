package signals;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ACC {
        private List<Integer> signalData;
        private String filename;
        private String path;
        private List<Integer> timestamp;

        public ACC(List<Integer> signalData, String filename, String path, List<Integer> timestamp) {
            this.signalData = signalData;
            this.filename = filename;
            this.path = path;
            this.timestamp = timestamp;
        }


    public ACC(List<Integer> signalData, List<Integer> timestamp) {
        this.signalData = signalData;
        this.timestamp = timestamp;
    }

    public ACC() {
        this.signalData = new ArrayList<>();
        this.filename = null;
        this.path = null;
        this.timestamp = new ArrayList<>();
    }

    public List<Integer> getSignalData() {
            return signalData;
        }
    public void setSignalData(List<Integer> signalData) {
            this.signalData = signalData;
        }

    public String getFilename() {
            return filename;
        }

    public void setFilename(String filename) {
            this.filename = filename;
        }

    public String getPath() {
            return path;
        }

    public void setPath(String path) {
            this.path = path;
        }

    public List<Integer> getTimestamp() {
            return timestamp;
        }

    public void setTimestamp(List<Integer> timestamp) {
            this.timestamp = timestamp;
        }

        @Override
    public String toString() {
       return "ACC [filename=" + filename + ", path=" + path + ", timestamp=" + timestamp + ", signalData=" + signalData+"]";
    }

    public static String listToString(List<Integer> list) {
        return list.stream()
                .map(String::valueOf)  // Convierte cada Integer a String
                .collect(Collectors.joining(","));  // Junta tod separado por comas
    }

    public void plotSignal() {
        XYSeries series = new XYSeries("Acceleration Signal");

        for (int i = 0; i < signalData.size(); i++) {
            series.add(timestamp.get(i), signalData.get(i));
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Acceleration Signal over Time",
                "Time (ms)",
                "Acceleration",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame frame = new JFrame("Signal Plot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }
}
