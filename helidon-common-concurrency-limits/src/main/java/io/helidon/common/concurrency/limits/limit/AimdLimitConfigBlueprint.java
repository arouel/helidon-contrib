package io.helidon.common.concurrency.limits.limit;

import java.time.Duration;

import io.helidon.builder.api.Option;
import io.helidon.builder.api.Prototype;

@Prototype.Blueprint
@Prototype.Configured(root = false, value = AimdLimitConfigProvider.CONFIG_NAME)
@Prototype.Provides(LimitConfigProvider.class)
interface AimdLimitConfigBlueprint extends LimitConfig {

    @Option.Configured
    @Option.DefaultDouble(0.9)
    double backoffRatio();

    @Option.Configured
    @Option.DefaultInt(20)
    int initialLimit();

    @Option.Configured
    @Option.DefaultInt(200)
    int maxLimit();

    @Option.Configured
    @Option.DefaultInt(20)
    int minLimit();

    @Option.Configured
    @Option.Default("aimd")
    @Override
    String name();

    @Option.Configured
    @Option.DefaultCode("Duration.ofSeconds(5)")
    Duration timeout();

    @Override
    default LimitAlgorithm toAlgorithm() {
        return new AimdLimitAlgorithm(this);
    }

    @Override
    default String type() {
        return AimdLimitConfigProvider.CONFIG_NAME;
    }

}
