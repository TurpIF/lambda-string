package fr.pturpin.lambdaString;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class SimpleInjectionBenchmark {

    @Benchmark
    public void dummy(Blackhole bh) {
        double n = 0;
        for (int i = 0; i < 1000; i++) {
            n = n * 2 / 3 + i;
        }
        bh.consume(n);
    }
}
