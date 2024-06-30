package io.helidon.common.concurrency.limits.limiter;

import io.helidon.common.config.ConfiguredProvider;

/**
 * Limiter configuration provider.
 *
 * @param <T>
 *            type of configuration supported by this provider
 */
public interface LimiterConfigProvider<T extends LimiterConfig> extends ConfiguredProvider<T> {
}
