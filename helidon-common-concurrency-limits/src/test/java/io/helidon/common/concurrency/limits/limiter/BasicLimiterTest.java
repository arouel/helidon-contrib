package io.helidon.common.concurrency.limits.limiter;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import io.helidon.common.concurrency.limits.limit.AimdLimitConfig;
import io.helidon.common.concurrency.limits.limit.FixedLimitConfig;
import io.helidon.common.concurrency.limits.limit.LimitConfig;
import io.helidon.common.concurrency.limits.limiter.Limiter.Token;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class BasicLimiterTest {

    @Test
    void test_bypassResolver() {
        Predicate<Object> bypassResolver = context -> context.equals("bypass");
        BasicLimiterConfigBlueprint config = new TestConfig(bypassResolver);
        BasicLimiter<Object> limiter = new BasicLimiter<>(config);

        assertThat(limiter.tryAcquire("bypass")).isPresent();
        assertThat(limiter.tryAcquire("bypass")).isPresent();
        assertThat(limiter.tryAcquire(new Object())).isPresent();
        assertThat(limiter.tryAcquire(new Object())).isEmpty();
    }

    @Test
    void test_currentLimit() {
        BasicLimiterConfigBlueprint config = new TestConfig(100);
        BasicLimiter<Object> limiter = new BasicLimiter<>(config);
        assertThat(limiter.currentLimit()).isEqualTo(100);
    }

    @RepeatedTest(10)
    void test_tryAcquire_concurrently() throws InterruptedException {
        int threadCount = Runtime.getRuntime().availableProcessors();
        int operationsPerThread = 10_000;
        BasicLimiterConfigBlueprint config = new TestConfig(threadCount / 2);
        BasicLimiter<Object> limiter = new BasicLimiter<>(config);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < operationsPerThread; j++) {
                        Optional<Limiter.Token> token = limiter.tryAcquire(new Object());
                        if (token.isPresent()) {
                            successCount.incrementAndGet();
                            token.get().onSuccess();
                        } else {
                            rejectCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finished = endLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.println("Final limit: " + limiter.currentLimit());
        System.out.println("Success:     " + successCount.get());
        System.out.println("Reject:      " + rejectCount.get());

        assertThat(finished).as("Test did not complete in time").isTrue();
        assertThat(successCount.get() + rejectCount.get()).isEqualTo(threadCount * operationsPerThread);
        assertThat(successCount.get()).isGreaterThan(operationsPerThread);
        assertThat(rejectCount.get()).isGreaterThan(operationsPerThread);
    }

    @Test
    void test_tryAcquire_limitChange_withAimd() {
        AimdLimitConfig limit = AimdLimitConfig.builder()
                .initialLimit(2)
                .minLimit(1)
                .maxLimit(4)
                .timeout(Duration.ofMillis(100))
                .build();
        BasicLimiterConfigBlueprint config = new TestConfig(limit);
        BasicLimiter<Object> limiter = new BasicLimiter<>(config);

        assertThat(limiter.currentLimit()).isEqualTo(limit.initialLimit());

        limiter.tryAcquire(new Object()).get().onSuccess();
        assertThat(limiter.currentLimit()).isEqualTo(3);

        limiter.tryAcquire(new Object()).get().onSuccess();
        assertThat(limiter.currentLimit()).isEqualTo(3);

        Token concurrentRequest = limiter.tryAcquire(new Object()).get();
        limiter.tryAcquire(new Object()).get().onSuccess();
        assertThat(limiter.currentLimit()).isEqualTo(4);
        concurrentRequest.onSuccess();

        limiter.tryAcquire(new Object()).get().onDropped();
        assertThat(limiter.currentLimit()).isEqualTo(3);

        limiter.tryAcquire(new Object()).get().onIgnore();
        assertThat(limiter.currentLimit()).isEqualTo(3);

        limiter.tryAcquire(new Object()).get().onIgnore();
        assertThat(limiter.currentLimit()).isEqualTo(3);

        limiter.tryAcquire(new Object()).get().onDropped();
        assertThat(limiter.currentLimit()).isEqualTo(2);

        limiter.tryAcquire(new Object()).get().onDropped();
        assertThat(limiter.currentLimit()).isEqualTo(1);
    }

    @Test
    void test_tryAcquire_release_onDropped() {
        BasicLimiterConfigBlueprint config = new TestConfig(1);
        BasicLimiter<Object> limiter = new BasicLimiter<>(config);

        Optional<Limiter.Token> token = limiter.tryAcquire(new Object());
        assertThat(token).isPresent();
        assertThat(limiter.tryAcquire(new Object())).isEmpty();

        token.get().onDropped();
        assertThat(limiter.tryAcquire(new Object())).isPresent();
    }

    @Test
    void test_tryAcquire_release_onIgnore() {
        BasicLimiterConfigBlueprint config = new TestConfig(1);
        BasicLimiter<Object> limiter = new BasicLimiter<>(config);

        Optional<Limiter.Token> token = limiter.tryAcquire(new Object());
        assertThat(token).isPresent();
        assertThat(limiter.tryAcquire(new Object())).isEmpty();

        token.get().onIgnore();
        assertThat(limiter.tryAcquire(new Object())).isPresent();
    }

    @Test
    void test_tryAcquire_release_onSuccess() {
        BasicLimiterConfigBlueprint config = new TestConfig(1);
        BasicLimiter<Object> limiter = new BasicLimiter<>(config);

        Optional<Limiter.Token> token = limiter.tryAcquire(new Object());
        assertThat(token).isPresent();
        assertThat(limiter.tryAcquire(new Object())).isEmpty();

        token.get().onSuccess();
        assertThat(limiter.tryAcquire(new Object())).isPresent();
    }

    @Test
    void test_tryAcquire_upToLimit() {
        int fixedLimit = 5;
        BasicLimiterConfigBlueprint config = new TestConfig(fixedLimit);
        BasicLimiter<Object> limiter = new BasicLimiter<>(config);

        Limiter.Token lastToken = limiter.tryAcquire(new Object()).get();
        for (int i = 0; i < fixedLimit - 1; i++) {
            lastToken = limiter.tryAcquire(new Object()).get();
        }

        assertThat(limiter.tryAcquire(new Object())).isEmpty();
        lastToken.onSuccess();
        assertThat(limiter.tryAcquire(new Object())).isPresent();
        assertThat(limiter.tryAcquire(new Object())).isEmpty();
    }

    record TestConfig(LimitConfig limit, Predicate<Object> bypassResolver) implements BasicLimiterConfigBlueprint {

        TestConfig(Predicate<Object> bypassResolver) {
            this(FixedLimitConfig.builder().limit(1).build(), bypassResolver);
        }

        TestConfig() {
            this(1);
        }

        TestConfig(int fixedLimit) {
            this(FixedLimitConfig.builder().limit(fixedLimit).build(), ignored -> false);
        }

        TestConfig(LimitConfig limit) {
            this(limit, ignored -> false);
        }

        @Override
        public String name() {
            return "test";
        }
    }

}
