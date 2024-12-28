package Benchmark;

import java.util.Arrays;
import java.util.List;

public class Config {
    public static final String PATH = "data.txt";
    public static boolean RUN_THREADED = true; // TODO: save results to a file and show them in a GUI graph
    // TODO: add more scenarios; add ram, cpu and disk usage monitoring on the server and client

    // Define benchmark scenarios
    public static final List<BenchmarkScenario> benchmarks = Arrays.asList(
        new BenchmarkScenario("Read-heavy workload", 70, 500),
        new BenchmarkScenario("Write-heavy workload", 30, 500),
        new BenchmarkScenario("Balanced workload", 50, 500)
    );
}