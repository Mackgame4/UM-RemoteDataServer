package Benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class BenchmarkData {
    public static final String PATH = "data.txt";

    // Method to append a line to the file
    public void writeLine(String line) {
        try {
            File file = new File(PATH);
            // Check if file exists, if not create a new one
            if (!file.exists()) {
                file.createNewFile();
            }
            // Open the file in append mode (true means append)
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(line + System.lineSeparator()); // Append the line and add a new line
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getBenchmarkCount() {
        int count = 0;
        try {
            File file = new File(PATH);
            if (file.exists()) {
                // Count number of lines starting with "=Benchmark"
                count = (int) Files.lines(file.toPath()).filter(line -> line.startsWith("=Benchmark")).count();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }
}