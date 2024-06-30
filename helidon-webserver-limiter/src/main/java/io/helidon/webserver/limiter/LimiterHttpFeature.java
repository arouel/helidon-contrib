package io.helidon.webserver.limiter;

import java.util.Optional;

import io.helidon.common.Weighted;
import io.helidon.common.concurrency.limits.limiter.Limiter;
import io.helidon.webserver.http.FilterChain;
import io.helidon.webserver.http.HttpFeature;
import io.helidon.webserver.http.HttpRouting.Builder;
import io.helidon.webserver.http.RoutingRequest;
import io.helidon.webserver.http.RoutingResponse;

class LimiterHttpFeature implements HttpFeature, Weighted {

    private final Limiter<RoutingRequest> limiter;

    private final int throttleStatus;

    private final double weight;

    LimiterHttpFeature(
            double weight,
            Limiter<RoutingRequest> limiter,
            int throttleStatus) {
        this.weight = weight;
        this.limiter = limiter;
        this.throttleStatus = throttleStatus;
    }

    private void filter(FilterChain chain, RoutingRequest request, RoutingResponse response) {
        Optional<Limiter.Token> listener = limiter.tryAcquire(request);
        if (listener.isPresent()) {
            try {
                chain.proceed();
                listener.get().onSuccess();
            } catch (Exception e) {
                listener.get().onIgnore();
                throw e;
            }
        } else {
            response.status(throttleStatus);
            response.send("Too Many Concurrent Requests");
        }
    }

    @Override
    public void setup(Builder routing) {
        routing.addFilter(this::filter);
    }

    @Override
    public double weight() {
        return weight;
    }
}
