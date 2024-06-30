package io.helidon.common.concurrency.limits.limiter;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.helidon.common.concurrency.limits.limit.AimdLimitConfig;
import io.helidon.common.concurrency.limits.limit.LimitConfig;
import io.helidon.common.concurrency.limits.limiter.Limiter.Token;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgsAppend = "-XX:StartFlightRecording=dumponexit=true,filename=/tmp/BasicLimiterConcurrencyBenchmark.jfr")
@Warmup(iterations = 5, time = 3)
@Measurement(iterations = 5, time = 3)
public class BasicLimiterConcurrencyBenchmark {

    private BasicLimiter<Object> limiter;

    private Consumer<Limiter.Token> tokenReleaseFunction;

    private static Consumer<Limiter.Token> tokenReleaseFunction() {
        boolean didDrop = Math.random() < 0.01; // 1% chance of drop
        if (didDrop) {
            return Token::onDropped;
        }
        boolean ignored = Math.random() < 0.1; // 10% chance of ignore
        if (ignored) {
            return Token::onIgnore;
        }
        return Token::onSuccess;
    }

    @Benchmark
    @Threads(Threads.MAX)
    public void benchmarkConcurrentRequests() {
        Optional<Limiter.Token> token = limiter.tryAcquire(new Object());
        token.ifPresent(tokenReleaseFunction);
    }

    @Setup
    public void setup() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        LimitConfig limitConfig = AimdLimitConfig.builder()
                .initialLimit(availableProcessors)
                .backoffRatio(0.9)
                .timeout(Duration.ofMillis(100))
                .minLimit(availableProcessors)
                .maxLimit(1000)
                .build();
        BasicLimiterConfigBlueprint limiterConfig = new BasicLimiterConfigBlueprint() {
            @Override
            public LimitConfig limit() {
                return limitConfig;
            }

            @Override
            public String name() {
                return "test";
            }
        };
        limiter = new BasicLimiter<>(limiterConfig);
    }

    @Setup(Level.Invocation)
    public void setupIteration() {
        tokenReleaseFunction = tokenReleaseFunction();
    }
}
