package io.helidon.common.concurrency.limits.limit;

import static org.assertj.core.api.Assertions.*;

import java.util.ServiceLoader;

import org.junit.jupiter.api.Test;

class LimitConfigProviderTest {

    @Test
    void test() {
        assertThat(ServiceLoader.load(LimitConfigProvider.class))
                .extracting(o -> o.getClass().getName())
                .containsOnly(
                        AimdLimitConfigProvider.class.getName(),
                        FixedLimitConfigProvider.class.getName());
    }
}
