package io.helidon.webserver.limiter;

import static java.lang.System.Logger.Level.*;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import io.helidon.builder.api.RuntimeType;
import io.helidon.common.Weighted;
import io.helidon.common.concurrency.limits.limiter.Limiter;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.RoutingRequest;
import io.helidon.webserver.spi.ServerFeature;

@RuntimeType.PrototypedBy(LimiterFeatureConfig.class)
public final class LimiterFeature
        implements
        RuntimeType.Api<LimiterFeatureConfig>,
        ServerFeature,
        Weighted {

    static final String DEFAULT_LOGGER_NAME = "io.helidon.webserver.limiter.LimiterFeature";

    static final String LIMITER_ID = "concurrency-limiter";

    private static final System.Logger LOGGER = System.getLogger(LimiterFeature.class.getName());

    static final int STATUS_TOO_MANY_REQUESTS = 429;

    static final double WEIGHT = 700;

    private final LimiterFeatureConfig config;

    private final Limiter<RoutingRequest> limiter;

    private final int throttleStatus;

    private final double weight;

    private LimiterFeature(LimiterFeatureConfig config) {
        this.config = config;
        limiter = config.limiter().map(c -> c.toLimiter(RoutingRequest.class)).orElse(NoOpLimiter.INSTANCE);
        throttleStatus = config.throttleStatus();
        weight = config.weight();
    }

    public static LimiterFeatureConfig.Builder builder() {
        return LimiterFeatureConfig.builder();
    }

    public static LimiterFeature create(Consumer<LimiterFeatureConfig.Builder> consumer) {
        return builder().update(consumer).build();
    }

    public static LimiterFeature create(LimiterFeatureConfig config) {
        return new LimiterFeature(config);
    }

    @Override
    public String name() {
        return config.name();
    }

    @Override
    public LimiterFeatureConfig prototype() {
        return config;
    }

    @Override
    public void setup(ServerFeatureContext featureContext) {
        if (!config.enabled()) {
            LOGGER.log(INFO, "Concurrency Limiter is disabled.");
            return;
        }

        // Set<String> sockets = new HashSet<>(config.sockets());
        Set<String> sockets = new HashSet<>();
        if (sockets.isEmpty()) {
            sockets.addAll(featureContext.sockets());
            sockets.add(WebServer.DEFAULT_SOCKET_NAME);
        }

        LOGGER.log(INFO, "Concurrency Limiter is enabled: {0}", config.limiter().map(c -> c.limit().type()).orElse("noop"));

        for (String socket : sockets) {
            var httpRouting = featureContext.socket(socket).httpRouting();
            var httpFeature = new LimiterHttpFeature(weight, limiter, throttleStatus);
            httpRouting.addFeature(httpFeature);
        }
    }

    @Override
    public String type() {
        return LIMITER_ID;
    }

    @Override
    public double weight() {
        return weight;
    }
}
