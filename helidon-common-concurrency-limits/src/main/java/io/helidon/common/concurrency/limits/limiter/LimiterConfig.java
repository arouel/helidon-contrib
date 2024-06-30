package io.helidon.common.concurrency.limits.limiter;

import java.util.function.Predicate;
import java.util.function.Supplier;

import io.helidon.builder.api.Option;
import io.helidon.common.concurrency.limits.limit.LimitConfig;
import io.helidon.common.concurrency.limits.limit.LimitConfigProvider;
import io.helidon.common.config.NamedService;

/**
 * Limiter configuration abstraction, used to setup a limiter.
 */
public interface LimiterConfig extends NamedService {

    /**
     * @return filter function which may decide based on a given context object what shouldn't be restricted by the
     *         limiter
     */
    default Predicate<Object> bypassResolver() {
        return ignore -> false;
    }

    /**
     * @return a time source that works in nanosecond resolution
     */
    default Supplier<Long> clock() {
        return System::nanoTime;
    }

    /**
     * @return the configuration of the limit algorithm to use
     */
    @Option.Configured
    @Option.Provider(value = LimitConfigProvider.class, discoverServices = false)
    LimitConfig limit();

    /**
     * @param <C>
     *            context type
     * @param contextType
     *            class of context type
     * @return a new limiter based on this configuration
     */
    <C> Limiter<C> toLimiter(Class<C> contextType);
}
