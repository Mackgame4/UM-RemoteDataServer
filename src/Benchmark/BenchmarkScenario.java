package Benchmark;

public class BenchmarkScenario {
    private String description;
    private int readPercentage;
    private int iterations;

    public BenchmarkScenario(String description, int readPercentage, int iterations) {
        this.description = description;
        this.readPercentage = readPercentage;
        this.iterations = iterations;
    }

    public String getDescription() {
        return description;
    }

    public int getReadPercentage() {
        return readPercentage;
    }

    public int getIterations() {
        return iterations;
    }

    @Override
    public String toString() {
        return "BenchmarkScenario{" +
                "description='" + description + '\'' +
                ", readPercentage=" + readPercentage +
                ", iterations=" + iterations +
                '}';
    }
}