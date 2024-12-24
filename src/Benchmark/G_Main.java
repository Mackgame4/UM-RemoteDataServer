package Benchmark;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class G_Main {
    
    public static void main(String[] args) {
        // File path for the benchmark data
        String filePath = "data.txt";
        
        // Create datasets for the graphs
        DefaultCategoryDataset benchmarkDataset = new DefaultCategoryDataset();
        DefaultCategoryDataset medianDataset = new DefaultCategoryDataset();
        DefaultCategoryDataset differenceDataset = new DefaultCategoryDataset();
        
        // Maps to store test results
        Map<String, List<Integer>> threadedData = new HashMap<>();
        Map<String, List<Integer>> nonThreadedData = new HashMap<>();
        
        // Read and process the data from the file
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentBenchmark = null;
            String currentThreaded = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Remove leading/trailing spaces
                if (line.startsWith("=Benchmark")) {
                    currentBenchmark = line.split(":")[0].trim();
                    currentThreaded = line.split("Threaded")[1].split("\\|")[0].trim();
                } else if (line.contains("workload")) {
                    String workloadType = line.split("\\|")[0].trim();
                    String duration = line.split("Duration:")[1].trim();
                    // Remove "ms" and convert to integer
                    int durationValue = Integer.parseInt(duration.replace(" ms", "").trim());
                    
                    // Store the data for later median and difference calculations
                    if (currentThreaded.equals("true")) {
                        threadedData.computeIfAbsent(workloadType, k -> new ArrayList<>()).add(durationValue);
                    } else {
                        nonThreadedData.computeIfAbsent(workloadType, k -> new ArrayList<>()).add(durationValue);
                    }
                    
                    // Add data to the benchmark dataset for plotting
                    benchmarkDataset.addValue(durationValue, currentBenchmark + " (Threaded " + currentThreaded + ")", workloadType);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Calculate median and difference datasets
        calculateMedianAndDifference(threadedData, nonThreadedData, medianDataset, differenceDataset);
        
        // Create the benchmark chart
        JFreeChart benchmarkChart = ChartFactory.createBarChart(
                "Benchmark Results",        // Chart Title
                "Workload Type",            // X-Axis Label
                "Duration (ms)",            // Y-Axis Label
                benchmarkDataset            // Dataset
        );

        // Create the median chart
        JFreeChart medianChart = ChartFactory.createBarChart(
                "Median Benchmark Results", // Chart Title
                "Workload Type",            // X-Axis Label
                "Duration (ms)",            // Y-Axis Label
                medianDataset              // Dataset
        );

        // Create the difference chart
        JFreeChart differenceChart = ChartFactory.createBarChart(
                "Benchmark Duration Difference (Threaded - Non-Threaded)", // Chart Title
                "Workload Type",            // X-Axis Label
                "Duration Difference (ms)",  // Y-Axis Label
                differenceDataset           // Dataset
        );
        
        // Create panels for each chart
        ChartPanel benchmarkChartPanel = new ChartPanel(benchmarkChart);
        benchmarkChartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        
        ChartPanel medianChartPanel = new ChartPanel(medianChart);
        medianChartPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        ChartPanel differenceChartPanel = new ChartPanel(differenceChart);
        differenceChartPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        // Create a JFrame to display the charts
        JFrame frame = new JFrame("Benchmark Graphs");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new java.awt.GridLayout(3, 1)); // Arrange the charts in a vertical layout
        frame.add(benchmarkChartPanel);
        frame.add(medianChartPanel);
        frame.add(differenceChartPanel);
        frame.pack();
        frame.setVisible(true);
    }
    
    private static void calculateMedianAndDifference(
            Map<String, List<Integer>> threadedData,
            Map<String, List<Integer>> nonThreadedData,
            DefaultCategoryDataset medianDataset,
            DefaultCategoryDataset differenceDataset) {
        
        // Iterate over all workload types
        for (String workloadType : threadedData.keySet()) {
            // Get the list of durations for both threaded and non-threaded
            List<Integer> threadedDurations = threadedData.get(workloadType);
            List<Integer> nonThreadedDurations = nonThreadedData.get(workloadType);
            
            // Calculate median values
            double threadedMedian = calculateMedian(threadedDurations);
            double nonThreadedMedian = calculateMedian(nonThreadedDurations);
            
            // Calculate the difference
            double difference = threadedMedian - nonThreadedMedian;
            
            // Add to the median and difference datasets
            medianDataset.addValue(threadedMedian, "Threaded", workloadType);
            medianDataset.addValue(nonThreadedMedian, "Non-Threaded", workloadType);
            differenceDataset.addValue(difference, "Difference", workloadType);
        }
    }
    
    private static double calculateMedian(List<Integer> durations) {
        // Sort the list and calculate the median
        Collections.sort(durations);
        int size = durations.size();
        if (size % 2 == 0) {
            return (durations.get(size / 2 - 1) + durations.get(size / 2)) / 2.0;
        } else {
            return durations.get(size / 2);
        }
    }
}
