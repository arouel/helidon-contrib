package io.helidon.common.concurrency.limits.limiter;

import java.util.Optional;

import io.helidon.common.concurrency.limits.limit.LimitAlgorithm;

/**
 * Contract for a concurrency limiter. The caller is expected to acquire a token for each operation call and must
 * release it when the operation completes. Releasing the token may trigger an update to the concurrency limit based on
 * error rate or latency measurement.
 *
 * @param <C>
 *            Some limiters take a context to perform more fine grained limits.
 */
public interface Limiter<C> {

    /**
     * Attempts to acquire a token from the limiter.
     *
     * <p>
     * If acquired the caller must call one of the Listener methods when the operation has been completed to release the
     * count.
     *
     * @param context
     *            Context for the operation. Use {@code null} if the limiter doesn't use a context.
     * @return An {@code Optional} containing a {@code Token} if acquired, or empty if the limit has been exceeded.
     */
    Optional<Token> tryAcquire(C context);

    /**
     * Tracks the outcome of an operation controlled by the limiter.
     */
    interface Token {
        /**
         * The operation failed and was dropped due to being rejected by an external limit or hitting a timeout. Loss
         * based {@link LimitAlgorithm} implementations will likely do an aggressive reducing in limit when this
         * happens.
         */
        void onDropped();

        /**
         * The operation failed before any meaningful RTT measurement could be made and should be ignored to not
         * introduce an artificially low RTT.
         */
        void onIgnore();

        /**
         * Notification that the operation succeeded and internally measured latency should be used as an RTT sample.
         */
        void onSuccess();
    }
}
