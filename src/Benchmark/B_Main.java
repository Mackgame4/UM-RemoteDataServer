package Benchmark;

import java.util.ArrayList;
//import java.util.Collections;
import java.util.List;

import Shared.Notify;
import Shared.Terminal;

class BenchmarkResult {
    private String description;
    private boolean success;
    private long duration;

    public BenchmarkResult(String description, boolean success, long duration) {
        this.description = description;
        this.success = success;
        this.duration = duration;
    }

    @Override
    public String toString() {
        return description + " | Success: " + success + " | Duration: " + duration + " ms";
    }
}

public class B_Main {
    private static BenchmarkData benchmarkData = new BenchmarkData();

    public static void main(String[] args) {
        Notify.info("Starting Benchmark...");
        List<BenchmarkResult> results = new ArrayList<>();

        for (BenchmarkScenario scenario : Config.benchmarks) {
            BenchmarkResult benchmark = runBenchmark(scenario.getDescription(), scenario.getReadPercentage(), scenario.getIterations());
            results.add(benchmark);
        }

        printResults(results);
    }

    public static BenchmarkResult runBenchmark(String description, int readPercentage, int iterations) {
        Notify.info("Running: " + description);
        long startTime = System.currentTimeMillis();
        try {
            List<String> commands = new ArrayList<>();
            int writeCount = (iterations * (100 - readPercentage)) / 100;
            int readCount = iterations - writeCount;
            // Generate write commands
            for (int i = 0; i < writeCount; i++) {
                commands.add("write " + i + " " + i); // Unique key-value pair for each write
            }
            // Generate read commands
            for (int i = 0; i < readCount; i++) {
                int keyToRead = i % writeCount; // Cycle through written keys for reads
                commands.add("read " + keyToRead);
            }
            //Collections.shuffle(commands); // Shuffle commands to mix reads and writes randomly (better simulation)
            // Ensure login is always first after shuffle
            commands.add(0, "login admin admin");
            // Run the commands in a TestClient
            boolean succ = TestClient.run(commands);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            Notify.success(description + " completed in " + duration + " ms.");
            return new BenchmarkResult(description, succ, duration);
        } catch (Exception e) {
            Notify.error("Benchmark error: " + e.getMessage());
            return new BenchmarkResult(description, false, 0);
        }
    }

    private static void printResults(List<BenchmarkResult> results) {
        System.out.println(Terminal.ANSI_YELLOW + "=== Benchmark Results ===" + Terminal.ANSI_RESET);
        benchmarkData.writeLine("=Benchmark " + benchmarkData.getBenchmarkCount() + ": Threaded " + Config.RUN_THREADED + " | Results:");
        for (BenchmarkResult result : results) {
            System.out.println(result);
            benchmarkData.writeLine(result.toString());
        }
    }
}