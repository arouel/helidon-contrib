package io.helidon.common.concurrency.limits.limiter;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.helidon.common.concurrency.limits.limit.LimitAlgorithm;

public abstract class AbstractLimiter<C> implements Limiter<C> {

    private final Predicate<C> bypassResolver;
    private final Supplier<Long> clock;
    private final AtomicInteger concurrentRequests;
    private volatile int limit;
    private final LimitAlgorithm limitAlgorithm;

    protected AbstractLimiter(LimiterConfig config) {
        clock = config.clock();
        @SuppressWarnings("unchecked")
        Predicate<C> bypassResolver = (Predicate<C>) config.bypassResolver();
        this.bypassResolver = bypassResolver;
        concurrentRequests = new AtomicInteger(0);
        limitAlgorithm = config.limit().toAlgorithm();
        limit = limitAlgorithm.currentLimit();
        limitAlgorithm.notifyOnLimitChange(this::onNewLimit);
    }

    public int concurrentRequests() {
        return concurrentRequests.get();
    }

    protected Optional<Limiter.Token> createBypassToken() {
        return Optional.of(new Limiter.Token() {

            @Override
            public void onDropped() {
                // Do nothing
            }

            @Override
            public void onIgnore() {
                // Do nothing
            }

            @Override
            public void onSuccess() {
                // Do nothing
            }
        });
    }

    protected Optional<Limiter.Token> createRejectedToken() {
        return Optional.empty();
    }

    protected Limiter.Token createToken() {
        final long startTime = clock.get();
        final int currentRequests = concurrentRequests.incrementAndGet();
        return new Limiter.Token() {
            @Override
            public void onDropped() {
                concurrentRequests.decrementAndGet();
                limitAlgorithm.updateWithSample(startTime, clock.get() - startTime, currentRequests, true);
            }

            @Override
            public void onIgnore() {
                concurrentRequests.decrementAndGet();
            }

            @Override
            public void onSuccess() {
                concurrentRequests.decrementAndGet();
                limitAlgorithm.updateWithSample(startTime, clock.get() - startTime, currentRequests, false);
            }
        };
    }

    public int currentLimit() {
        return limit;
    }

    protected void onNewLimit(int newLimit) {
        limit = newLimit;
    }

    protected boolean shouldBypass(C context) {
        return bypassResolver.test(context);
    }
}
