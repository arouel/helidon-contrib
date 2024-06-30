package io.helidon.common.concurrency.limits.limiter;

import java.util.Optional;
import java.util.concurrent.Semaphore;

public class BasicLimiter<ContextT> extends AbstractLimiter<ContextT> {

    private final AdjustableSemaphore semaphore;

    public BasicLimiter(BasicLimiterConfigBlueprint config) {
        super(config);
        semaphore = new AdjustableSemaphore(currentLimit());
    }

    @Override
    protected void onNewLimit(int newLimit) {
        int oldLimit = currentLimit();
        super.onNewLimit(newLimit);

        if (newLimit > oldLimit) {
            semaphore.release(newLimit - oldLimit);
        } else {
            semaphore.reducePermits(oldLimit - newLimit);
        }
    }

    @Override
    public Optional<Limiter.Token> tryAcquire(ContextT context) {
        if (shouldBypass(context)) {
            return createBypassToken();
        }
        if (semaphore.tryAcquire()) {
            return Optional.of(new ReleasingToken(createToken()));
        }
        return createRejectedToken();
    }

    /**
     * Basic Semaphore subclass that allows access to its reducePermits method.
     */
    private static final class AdjustableSemaphore extends Semaphore {
        private static final long serialVersionUID = 1L;

        AdjustableSemaphore(int permits) {
            super(permits);
        }

        @Override
        public void reducePermits(int reduction) {
            super.reducePermits(reduction);
        }
    }

    private class ReleasingToken implements Limiter.Token {
        private final Limiter.Token delegate;

        ReleasingToken(Limiter.Token delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onDropped() {
            delegate.onDropped();
            semaphore.release();
        }

        @Override
        public void onIgnore() {
            delegate.onIgnore();
            semaphore.release();
        }

        @Override
        public void onSuccess() {
            delegate.onSuccess();
            semaphore.release();
        }
    }
}
