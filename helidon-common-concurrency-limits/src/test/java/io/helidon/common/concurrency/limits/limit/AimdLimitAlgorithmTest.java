package io.helidon.common.concurrency.limits.limit;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class AimdLimitAlgorithmTest {

    @RepeatedTest(10)
    void concurrentUpdatesAndReads() throws InterruptedException {
        AimdLimitConfig config = AimdLimitConfig.builder()
                .initialLimit(1)
                .backoffRatio(0.9)
                .timeout(Duration.ofMillis(100))
                .minLimit(1)
                .minLimit(200)
                .build();
        AimdLimitAlgorithm algorithm = new AimdLimitAlgorithm(config);

        int threadCount = 100;
        int operationsPerThread = 1_000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger timeoutCount = new AtomicInteger(0);
        AtomicInteger dropCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    for (int j = 0; j < operationsPerThread; j++) {
                        long startTime = System.nanoTime();
                        long rtt = (long) (Math.random() * 200_000_000); // 0-200ms
                        int concurrentRequests = (int) (Math.random() * algorithm.currentLimit() * 2);
                        boolean didDrop = Math.random() < 0.01; // 1% chance of drop

                        algorithm.updateWithSample(startTime, rtt, concurrentRequests, didDrop);

                        if (didDrop) {
                            dropCount.incrementAndGet();
                        } else if (rtt > config.timeout().toNanos()) {
                            timeoutCount.incrementAndGet();
                        } else {
                            successCount.incrementAndGet();
                        }

                        // Read the current limit
                        int currentLimit = algorithm.currentLimit();
                        assertThat(currentLimit).isGreaterThanOrEqualTo(config.minLimit());
                        assertThat(currentLimit).isLessThanOrEqualTo(config.maxLimit());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Start all threads
        boolean finished = endLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished).as("Test did not complete in time").isTrue();

        System.out.println("Final limit: " + algorithm.currentLimit());
        System.out.println("Successes:   " + successCount.get());
        System.out.println("Timeouts:    " + timeoutCount.get());
        System.out.println("Drops:       " + dropCount.get());

        assertThat(threadCount * operationsPerThread)
                .as("Total operations mismatch")
                .isEqualTo(successCount.get() + timeoutCount.get() + dropCount.get());
    }

    @Test
    void decreaseOnDrops() {
        AimdLimitConfig config = AimdLimitConfig.builder().initialLimit(30).build();
        AimdLimitAlgorithm limiter = new AimdLimitAlgorithm(config);
        limiter.updateWithSample(0, 0, 0, true);
        assertThat(limiter.currentLimit()).isEqualTo(27);
    }

    @Test
    void decreaseOnTimeoutExceeded() {
        Duration timeout = Duration.ofSeconds(1);
        AimdLimitConfig config = AimdLimitConfig.builder().initialLimit(30).timeout(timeout).build();
        AimdLimitAlgorithm limiter = new AimdLimitAlgorithm(config);
        limiter.updateWithSample(0, timeout.toNanos() + 1, 0, false);
        assertThat(limiter.currentLimit()).isEqualTo(27);
    }

    @Test
    void increaseOnSuccess() {
        AimdLimitConfig config = AimdLimitConfig.builder().initialLimit(20).build();
        AimdLimitAlgorithm limiter = new AimdLimitAlgorithm(config);
        limiter.updateWithSample(0, Duration.ofMillis(1).toNanos(), 10, false);
        assertThat(limiter.currentLimit()).isEqualTo(21);
    }

    @Test
    void successOverflow() {
        AimdLimitConfig config = AimdLimitConfig.builder().initialLimit(21).maxLimit(21).minLimit(0).build();
        AimdLimitAlgorithm limiter = new AimdLimitAlgorithm(config);
        limiter.updateWithSample(0, Duration.ofMillis(1).toNanos(), 10, false);
        // after success limit should still be at the max.
        assertThat(limiter.currentLimit()).isEqualTo(21);
    }

    @Test
    void testDefault() {
        AimdLimitConfig config = AimdLimitConfig.builder().initialLimit(10).build();
        AimdLimitAlgorithm limiter = new AimdLimitAlgorithm(config);
        assertThat(limiter.currentLimit()).isEqualTo(10);
    }
}
