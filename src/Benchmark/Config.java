package Benchmark;

import java.util.Arrays;
import java.util.List;

public class Config {
    public static boolean RUN_CONCURRENTLY = false;

    // Define benchmark scenarios
    public static final List<BenchmarkScenario> benchmarks = Arrays.asList(
        new BenchmarkScenario("Read-heavy workload", 70, 300),
        new BenchmarkScenario("Write-heavy workload", 30, 300),
        new BenchmarkScenario("Balanced workload", 50, 300)
    );
}