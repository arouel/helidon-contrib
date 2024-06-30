package io.helidon.webserver.limiter;

import java.util.Optional;

import io.helidon.builder.api.Option;
import io.helidon.builder.api.Prototype;
import io.helidon.common.concurrency.limits.limiter.LimiterConfig;
import io.helidon.common.concurrency.limits.limiter.LimiterConfigProvider;
import io.helidon.webserver.spi.ServerFeatureProvider;

@Prototype.Blueprint
@Prototype.Configured(value = LimiterFeature.LIMITER_ID, root = false)
@Prototype.Provides(ServerFeatureProvider.class)
interface LimiterFeatureConfigBlueprint extends Prototype.Factory<LimiterFeature> {

    /**
     * Whether this feature will be enabled.
     *
     * @return whether enabled
     */
    @Option.Configured
    @Option.DefaultBoolean(true)
    boolean enabled();

    /**
     * Configuration of the underlying limit algorithm to use.
     *
     * @return the limit configuration
     */
    @Option.Configured
    @Option.Provider(value = LimiterConfigProvider.class, discoverServices = false)
    Optional<LimiterConfig> limiter();

    /**
     * Name of this instance.
     *
     * @return instance name
     */
    @Option.Default(LimiterFeature.LIMITER_ID)
    String name();

    /**
     * An integer representing the HTTP status code sent back to the client when a request is rejected due to
     * throttling.
     *
     * <p>
     * Common HTTP status codes used for throttling:
     * <ul>
     * <li>429 Too Many Requests: This is the most appropriate and widely used status code for rate limiting.</li>
     * <li>503 Service Unavailable: Sometimes used to indicate the service is temporarily unavailable due to
     * overload.</li>
     * </ul>
     *
     * @return weight of the feature
     */
    @Option.DefaultInt(LimiterFeature.STATUS_TOO_MANY_REQUESTS)
    @Option.Configured
    int throttleStatus();

    /**
     * Weight of the access log feature. We need to log access for anything happening on the server, so weight is high:
     * {@value LimiterFeature#WEIGHT}.
     *
     * @return weight of the feature
     */
    @Option.DefaultDouble(LimiterFeature.WEIGHT)
    @Option.Configured
    double weight();

}
