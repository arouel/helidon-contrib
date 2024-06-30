package io.helidon.common.concurrency.limits.limiter;

import static org.assertj.core.api.Assertions.*;

import java.util.ServiceLoader;

import org.junit.jupiter.api.Test;

class LimiterConfigProviderTest {

    @Test
    void test() {
        assertThat(ServiceLoader.load(LimiterConfigProvider.class))
                .extracting(o -> o.getClass().getName())
                .containsOnly(BasicLimiterConfigProvider.class.getName());
    }
}
