package Benchmark;

import java.util.Arrays;
import java.util.List;

public class Config {
    public static boolean RUN_THREADED = true;

    // Define benchmark scenarios
    public static final List<BenchmarkScenario> benchmarks = Arrays.asList(
        new BenchmarkScenario("Read-heavy workload", 70, 500),
        new BenchmarkScenario("Write-heavy workload", 30, 500),
        new BenchmarkScenario("Balanced workload", 50, 500)
    );
}