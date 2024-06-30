package io.helidon.common.concurrency.limits.limit;

import java.util.function.Consumer;

record FixedLimitAlgorithm(int currentLimit) implements LimitAlgorithm {

    @Override
    public void notifyOnLimitChange(Consumer<Integer> consumer) {
        // do nothing
    }

    @Override
    public void updateWithSample(long startTime, long rtt, int inflight, boolean didDrop) {
        // do nothing
    }
}
