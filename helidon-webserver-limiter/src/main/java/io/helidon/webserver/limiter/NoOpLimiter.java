package io.helidon.webserver.limiter;

import java.util.Optional;

import io.helidon.common.concurrency.limits.limiter.Limiter;
import io.helidon.webserver.http.RoutingRequest;

class NoOpLimiter implements Limiter<RoutingRequest> {

    static final NoOpLimiter INSTANCE = new NoOpLimiter();

    @Override
    public Optional<Limiter.Token> tryAcquire(RoutingRequest context) {
        return Optional.of(NoOpToken.INSTANCE);
    }

    private static class NoOpToken implements Limiter.Token {

        static final Limiter.Token INSTANCE = new NoOpToken();

        @Override
        public void onDropped() {
            // do nothing
        }

        @Override
        public void onIgnore() {
            // do nothing
        }

        @Override
        public void onSuccess() {
            // do nothing
        }
    }
}
