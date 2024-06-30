package io.helidon.common.concurrency.limits.limit;

class AimdLimitAlgorithm extends AbstractLimitAlgorithm {

    private final double backoffRatio;
    private final long timeoutInNanos;
    private final int minLimit;
    private final int maxLimit;

    AimdLimitAlgorithm(AimdLimitConfigBlueprint config) {
        super(config.initialLimit());
        backoffRatio = config.backoffRatio();
        timeoutInNanos = config.timeout().toNanos();
        maxLimit = config.maxLimit();
        minLimit = config.minLimit();
    }

    @Override
    protected int calculateNewLimit(long startTime, long rtt, int concurrentRequests, boolean didDrop) {
        int currentLimit = currentLimit();
        if (didDrop || rtt > timeoutInNanos) {
            currentLimit = (int) (currentLimit * backoffRatio);
        } else if (concurrentRequests * 2 >= currentLimit) {
            currentLimit = currentLimit + 1;
        }
        return Math.min(maxLimit, Math.max(minLimit, currentLimit));
    }
}
