package io.helidon.common.concurrency.limits.limit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class AbstractLimitAlgorithm implements LimitAlgorithm {

    private final AtomicInteger limit;
    private final List<Consumer<Integer>> listeners = new CopyOnWriteArrayList<>();

    protected AbstractLimitAlgorithm(int initialLimit) {
        limit = new AtomicInteger(initialLimit);
    }

    protected abstract int calculateNewLimit(long startTime, long rtt, int concurrentRequests, boolean didDrop);

    @Override
    public final int currentLimit() {
        return limit.get();
    }

    @Override
    public void notifyOnLimitChange(Consumer<Integer> consumer) {
        listeners.add(consumer);
    }

    protected void setLimit(int newLimit) {
        int oldLimit = limit.get();
        while (oldLimit != newLimit) {
            if (limit.compareAndSet(oldLimit, newLimit)) {
                listeners.forEach(listener -> listener.accept(newLimit));
                return;
            }
            oldLimit = limit.get();
        }
    }

    @Override
    public final void updateWithSample(long startTime, long rtt, int concurrentRequests, boolean didDrop) {
        setLimit(calculateNewLimit(startTime, rtt, concurrentRequests, didDrop));
    }
}
