package io.helidon.common.concurrency.limits.limit;

import java.util.function.Consumer;

/**
 * Defines the contract for an algorithm that dynamically calculates a concurrency limit based on round-trip time (RTT)
 * measurements and other factors.
 *
 * <p>
 * Implementations of this interface are used to adaptively adjust the maximum number of concurrent requests allowed,
 * helping to optimize system performance and prevent overload.
 *
 * <p>
 * Note: All time-related parameters in this interface are expected to be in nanoseconds.
 */
public interface LimitAlgorithm {

    /**
     * Retrieves the current estimated upper limit for concurrent requests.
     *
     * @return The current estimated upper limit as an integer.
     */
    int currentLimit();

    /**
     * Registers a callback to receive notifications when the concurrency limit is updated.
     *
     * <p>
     * The provided consumer will be invoked with the new limit value whenever it changes.
     *
     * @param consumer
     *            A Consumer that accepts the new limit value as an Integer.
     */
    void notifyOnLimitChange(Consumer<Integer> consumer);

    /**
     * Updates the algorithm with a new sample of request data.
     *
     * <p>
     * This method should be called for each request to provide the algorithm with the necessary data to adjust the
     * concurrency limit.
     *
     * @param startTime
     *            The start time of the request in nanoseconds.
     * @param rtt
     *            The round-trip time of the request in nanoseconds, calculated as (endTime - startTime).
     * @param concurrentRequests
     *            The number of requests currently being processed when this request was initiated.
     * @param wasRejected
     *            A boolean indicating whether this request was rejected due to rate limiting.
     */
    void updateWithSample(long startTime, long rtt, int concurrentRequests, boolean wasRejected);
}
