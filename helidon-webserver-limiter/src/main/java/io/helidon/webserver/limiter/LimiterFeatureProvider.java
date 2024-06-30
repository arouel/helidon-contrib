package io.helidon.webserver.limiter;

import io.helidon.common.Weight;
import io.helidon.common.config.Config;
import io.helidon.webserver.spi.ServerFeatureProvider;

/**
 * Provider of the concurrency limiting feature for {@link io.helidon.webserver.WebServer}.
 */
@Weight(LimiterFeature.WEIGHT)
public class LimiterFeatureProvider implements ServerFeatureProvider<LimiterFeature> {

    @Override
    public String configKey() {
        return LimiterFeature.LIMITER_ID;
    }

    @Override
    public LimiterFeature create(Config config, String name) {
        return LimiterFeature.builder()
                .config(config)
                .name(name)
                .build();
    }
}
