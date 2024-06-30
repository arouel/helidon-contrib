package io.helidon.common.concurrency.limits.limit;

import io.helidon.builder.api.Option;
import io.helidon.builder.api.Prototype;

@Prototype.Blueprint
@Prototype.Configured(root = false, value = FixedLimitConfigProvider.CONFIG_NAME)
@Prototype.Provides(LimitConfigProvider.class)
interface FixedLimitConfigBlueprint extends LimitConfig {

    @Option.Configured
    @Option.DefaultInt(20)
    int limit();

    @Option.Configured
    @Option.Default(FixedLimitConfigProvider.CONFIG_NAME)
    @Override
    String name();

    @Override
    default LimitAlgorithm toAlgorithm() {
        return new FixedLimitAlgorithm(limit());
    }

    @Override
    default String type() {
        return FixedLimitConfigProvider.CONFIG_NAME;
    }
}
