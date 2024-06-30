package io.helidon.common.concurrency.limits.limit;

import io.helidon.common.config.Config;

/**
 * Implementation of a service provider interface to create AIMD limit configuration.
 */
public class FixedLimitConfigProvider implements LimitConfigProvider {

    static final String CONFIG_NAME = "fixed";

    @Override
    public String configKey() {
        return CONFIG_NAME;
    }

    @Override
    public FixedLimitConfig create(Config config, String name) {
        return FixedLimitConfig.builder()
                .config(config)
                .name(name)
                .build();
    }
}
