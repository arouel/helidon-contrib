package io.helidon.common.concurrency.limits.limiter;

import io.helidon.common.config.Config;

/**
 * Implementation of a service provider interface to create basic limiter.
 */
public class BasicLimiterConfigProvider implements LimiterConfigProvider<LimiterConfig> {

    static final String CONFIG_NAME = "basic";

    @Override
    public String configKey() {
        return CONFIG_NAME;
    }

    @Override
    public BasicLimiterConfig create(Config config, String name) {
        return BasicLimiterConfig.builder()
                .config(config)
                .name(name)
                .build();
    }
}
