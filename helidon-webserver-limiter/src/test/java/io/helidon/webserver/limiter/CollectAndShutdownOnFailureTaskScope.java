package io.helidon.webserver.limiter;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.time.Instant;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A structured task scope that collects that collects the results of subtasks that complete successfully or captures
 * the exception of the first subtask to fail and shutdown the task.
 *
 * <p>
 * This is a customization of {@link java.util.concurrent.StructuredTaskScope.ShutdownOnFailure}.
 *
 * @param <T>
 *            the type of elements to collect
 */
public final class CollectAndShutdownOnFailureTaskScope<T> extends StructuredTaskScope<T> {

    private static final VarHandle FIRST_EXCEPTION;

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            FIRST_EXCEPTION = l.findVarHandle(CollectAndShutdownOnFailureTaskScope.class, "firstException", Throwable.class);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private volatile Throwable firstException;

    private final Queue<T> results = new ConcurrentLinkedQueue<>();

    /**
     * Constructs a new unnamed {@code ShutdownOnFailure} that creates virtual threads.
     *
     * @implSpec This constructor is equivalent to invoking the 2-arg constructor with a name of {@code null} and a
     *           thread factory that creates virtual threads.
     */
    public CollectAndShutdownOnFailureTaskScope() {
        this(null, Thread.ofVirtual().factory());
    }

    /**
     * Constructs a new {@code ShutdownOnFailure} with the given name and thread factory. The task scope is optionally
     * named for the purposes of monitoring and management. The thread factory is used to
     * {@link ThreadFactory#newThread(Runnable) create} threads when subtasks are {@linkplain #fork(Callable) forked}.
     * The task scope is owned by the current thread.
     *
     * <p>
     * Construction captures the current thread's {@linkplain ScopedValue scoped value} bindings for inheritance by
     * threads started in the task scope. The <a href="#TreeStructure">Tree Structure</a> section in the class
     * description details how parent-child relations are established implicitly for the purpose of inheritance of
     * scoped value bindings.
     *
     * @param name
     *            the name of the task scope, can be null
     * @param factory
     *            the thread factory
     */
    public CollectAndShutdownOnFailureTaskScope(String name, ThreadFactory factory) {
        super(name, factory);
    }

    @Override
    protected void handleComplete(Subtask<? extends T> subtask) {
        if (subtask.state() == Subtask.State.FAILED
                && firstException == null
                && FIRST_EXCEPTION.compareAndSet(this, null, subtask.exception())) {
            super.shutdown();
        }
        if (subtask.state() == Subtask.State.SUCCESS) {
            results.add(subtask.get());
        }
    }

    /**
     * Wait for all subtasks started in this task scope to complete or for a subtask to fail.
     *
     * <p>
     * This method waits for all subtasks by waiting for all threads {@linkplain #fork(Callable) started} in this task
     * scope to finish execution. It stops waiting when all threads finish, a subtask fails, or the current thread is
     * {@linkplain Thread#interrupt() interrupted}. It also stops waiting if the {@link #shutdown() shutdown} method is
     * invoked directly to shut down this task scope.
     *
     * <p>
     * This method may only be invoked by the task scope owner.
     *
     * @throws IllegalStateException
     *             {@inheritDoc}
     * @throws WrongThreadException
     *             {@inheritDoc}
     */
    @Override
    public CollectAndShutdownOnFailureTaskScope<T> join() throws InterruptedException {
        super.join();
        return this;
    }

    /**
     * Wait for all subtasks started in this task scope to complete or for a subtask to fail, up to the given deadline.
     *
     * <p>
     * This method waits for all subtasks by waiting for all threads {@linkplain #fork(Callable) started} in this task
     * scope to finish execution. It stops waiting when all threads finish, a subtask fails, the deadline is reached, or
     * the current thread is {@linkplain Thread#interrupt() interrupted}. It also stops waiting if the
     * {@link #shutdown() shutdown} method is invoked directly to shut down this task scope.
     *
     * <p>
     * This method may only be invoked by the task scope owner.
     *
     * @throws IllegalStateException
     *             {@inheritDoc}
     * @throws WrongThreadException
     *             {@inheritDoc}
     */
    @Override
    public CollectAndShutdownOnFailureTaskScope<T> joinUntil(Instant deadline)
            throws InterruptedException, TimeoutException {
        super.joinUntil(deadline);
        return this;
    }

    /**
     * @return a stream of results from the subtasks that completed successfully
     */
    public Stream<T> results() {
        super.ensureOwnerAndJoined();
        return results.stream();
    }

    /**
     * Throws if a subtask failed. If any subtask failed with an exception then {@code ExecutionException} is thrown
     * with the exception of the first subtask to fail as the {@linkplain Throwable#getCause() cause}. This method does
     * nothing if no subtasks failed.
     *
     * @throws ExecutionException
     *             if a subtask failed
     * @throws WrongThreadException
     *             if the current thread is not the task scope owner
     * @throws IllegalStateException
     *             if the task scope owner did not join after forking
     */
    public void throwIfFailed() throws ExecutionException {
        throwIfFailed(ExecutionException::new);
    }

    /**
     * Throws the exception produced by the given exception supplying function if a subtask failed. If any subtask
     * failed with an exception then the function is invoked with the exception of the first subtask to fail. The
     * exception returned by the function is thrown. This method does nothing if no subtasks failed.
     *
     * @param esf
     *            the exception supplying function
     * @param <X>
     *            type of the exception to be thrown
     *
     * @throws X
     *             produced by the exception supplying function
     * @throws WrongThreadException
     *             if the current thread is not the task scope owner
     * @throws IllegalStateException
     *             if the task scope owner did not join after forking
     */
    public <X extends Throwable> void throwIfFailed(Function<Throwable, ? extends X> esf) throws X {
        ensureOwnerAndJoined();
        Objects.requireNonNull(esf);
        Throwable exception = firstException;
        if (exception != null) {
            X ex = esf.apply(exception);
            Objects.requireNonNull(ex, "esf returned null");
            throw ex;
        }
    }
}
