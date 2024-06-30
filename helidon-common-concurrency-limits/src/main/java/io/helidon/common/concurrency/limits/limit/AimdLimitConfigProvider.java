package io.helidon.common.concurrency.limits.limit;

import io.helidon.common.config.Config;

/**
 * Implementation of a service provider interface to create AIMD limit configuration.
 */
public class AimdLimitConfigProvider implements LimitConfigProvider {

    static final String CONFIG_NAME = "aimd";

    @Override
    public String configKey() {
        return CONFIG_NAME;
    }

    @Override
    public AimdLimitConfig create(Config config, String name) {
        return AimdLimitConfig.builder()
                .config(config)
                .name(name)
                .build();
    }
}
