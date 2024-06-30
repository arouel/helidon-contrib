package io.helidon.common.concurrency.limits.limit;

import io.helidon.common.config.NamedService;

/**
 * Limit configuration abstraction, used to setup a limit algorithm.
 */
public interface LimitConfig extends NamedService {

    /**
     * @return the algorithm based on this configuration
     */
    LimitAlgorithm toAlgorithm();
}
