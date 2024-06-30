package io.helidon.common.concurrency.limits.limit;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FixedLimitAlgorithmTest {

    @Test
    void updateWithSample() {
        FixedLimitAlgorithm limiter = new FixedLimitAlgorithm(21);
        limiter.updateWithSample(0, 1_000_000, 10, false);
        assertThat(limiter.currentLimit()).isEqualTo(21);
    }

    @Test
    void updateWithSample_dropped() {
        FixedLimitAlgorithm limiter = new FixedLimitAlgorithm(30);
        limiter.updateWithSample(0, 0, 0, true);
        assertThat(limiter.currentLimit()).isEqualTo(30);
    }
}
