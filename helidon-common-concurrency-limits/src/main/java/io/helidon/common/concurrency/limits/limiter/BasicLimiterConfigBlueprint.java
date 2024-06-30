package io.helidon.common.concurrency.limits.limiter;

import io.helidon.builder.api.Option;
import io.helidon.builder.api.Prototype;
import io.helidon.common.concurrency.limits.limit.LimitConfigProvider;

@Prototype.Blueprint
@Prototype.Configured(root = false, value = BasicLimiterConfigProvider.CONFIG_NAME)
@Prototype.Provides(LimitConfigProvider.class)
interface BasicLimiterConfigBlueprint extends LimiterConfig {

    @Option.Configured
    @Option.Default(BasicLimiterConfigProvider.CONFIG_NAME)
    @Override
    String name();

    @Override
    default <C> Limiter<C> toLimiter(Class<C> contextType) {
        return new BasicLimiter<>(this);
    }

    @Override
    default String type() {
        return BasicLimiterConfigProvider.CONFIG_NAME;
    }
}
