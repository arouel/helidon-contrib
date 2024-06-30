package io.helidon.common.concurrency.limits.limit;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgsAppend = "-XX:StartFlightRecording=dumponexit=true,filename=/tmp/AimdLimitAlgorithmSuccessBenchmark.jfr")
@Warmup(iterations = 5, time = 3)
@Measurement(iterations = 5, time = 3)
public class AimdLimitAlgorithmSuccessBenchmark {

    private AimdLimitAlgorithm algorithm;

    @Param({ "0.9", "0.75" })
    private double backoffRatio;

    private int concurrentRequests;

    private long rtt;

    private long startTime;

    @Benchmark
    public void benchmarkSuccessfulRequest(Blackhole blackhole) {
        int newLimit = algorithm.calculateNewLimit(startTime, rtt, concurrentRequests, false);
        blackhole.consume(newLimit);
    }

    @Setup
    public void setup() {
        AimdLimitConfig config = AimdLimitConfig.builder()
                .initialLimit(1)
                .backoffRatio(backoffRatio)
                .timeout(Duration.ofMillis(500))
                .minLimit(1)
                .maxLimit(Integer.MAX_VALUE)
                .build();
        algorithm = new AimdLimitAlgorithm(config);
    }

    @Setup(Level.Invocation)
    public void setupIteration() {
        startTime = System.nanoTime();
        rtt = ThreadLocalRandom.current().nextLong(Duration.ofMillis(1).toNanos(), Duration.ofMillis(400).toNanos());
        concurrentRequests = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE / 4, Integer.MAX_VALUE / 2);
    }
}
