package au.id.ajlane.concurrent;

import java.text.MessageFormat;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A straight-forward {@link au.id.ajlane.concurrent.CalendarExecutorService} that uses an internal {@link
 * java.util.concurrent.ScheduledExecutorService} to delay task execution according to difference between the current
 * time and the scheduled time.
 * <p/>
 * This is the default {@code CalendarExecutorService} provided by utility methods on the {@code
 * CalendarExecutorService} interface.
 * <p/>
 * Pending tasks are re-scheduled according to the {@link #getAdjustmentPeriod() adjustment period} (every 2 hours by
 * default). This allows the service to self-correct if the {@link Clock} falls out-of-sync with the internal timer. A
 * shorter adjustment period will allow the service to self-correct faster, but will incur greater runtime costs.
 */
public final class DelayBasedCalendarExecutorService implements CalendarExecutorService
{
    @SuppressWarnings("ComparableImplementedButEqualsNotOverridden")
    private static final class WrappedCompletableCalendarFuture<V> implements CalendarFuture<V>
    {
        public static <T> WrappedCompletableCalendarFuture<T> wrap(
                final Instant instant, final CompletableFuture<T> future
        )
        {
            return wrap(Clock.systemDefaultZone(), instant, future);
        }

        public static <T> WrappedCompletableCalendarFuture<T> wrap(
                final Clock clock, final Instant instant, final CompletableFuture<T> future
        )
        {
            return new WrappedCompletableCalendarFuture<>(clock, instant, future);
        }

        private final Clock clock;
        private final CompletableFuture<V> future;
        private final Instant instant;

        private WrappedCompletableCalendarFuture(
                final Clock clock, final Instant instant, final CompletableFuture<V> future
        )
        {
            this.clock = clock;
            this.instant = instant;
            this.future = future;
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning)
        {
            return future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled()
        {
            return future.isCancelled();
        }

        @Override
        public boolean isDone()
        {
            return future.isDone();
        }

        @Override
        public V get() throws InterruptedException, ExecutionException
        {
            return future.get();
        }

        @Override
        public V get(final long timeout, final TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException
        {
            return future.get(timeout, unit);
        }

        @Override
        public int compareTo(final Delayed o)
        {
            return Long.compare(getDelay(TimeUnit.NANOSECONDS), o.getDelay(TimeUnit.NANOSECONDS));
        }

        @Override
        public long getDelay(final TimeUnit unit)
        {
            final Instant now = clock.instant();
            return Duration.between(instant, now).get(TimeUnits.toTemporalUnit(unit));
        }

        @Override
        public Instant getInstant()
        {
            return instant;
        }

        @Override
        public <U> CalendarFuture<U> thenApply(final Function<? super V, ? extends U> fn)
        {
            return wrap(future.thenApply(fn));
        }

        @Override
        public <U> CalendarFuture<U> thenApplyAsync(final Function<? super V, ? extends U> fn)
        {
            return wrap(future.thenApplyAsync(fn));
        }

        @Override
        public <U> CalendarFuture<U> thenApplyAsync(
                final Function<? super V, ? extends U> fn, final Executor executor
        )
        {
            return wrap(future.thenApplyAsync(fn, executor));
        }

        @Override
        public CalendarFuture<Void> thenAccept(final Consumer<? super V> action)
        {
            return wrap(future.thenAccept(action));
        }

        @Override
        public CalendarFuture<Void> thenAcceptAsync(final Consumer<? super V> action)
        {
            return wrap(future.thenAcceptAsync(action));
        }

        @Override
        public CalendarFuture<Void> thenAcceptAsync(
                final Consumer<? super V> action, final Executor executor
        )
        {
            return wrap(future.thenAcceptAsync(action, executor));
        }

        @Override
        public CalendarFuture<Void> thenRun(final Runnable action)
        {
            return wrap(future.thenRun(action));
        }

        @Override
        public CalendarFuture<Void> thenRunAsync(final Runnable action)
        {
            return wrap(future.thenRunAsync(action));
        }

        @Override
        public CalendarFuture<Void> thenRunAsync(
                final Runnable action, final Executor executor
        )
        {
            return wrap(future.thenRunAsync(action, executor));
        }

        @Override
        public <U, V1> CalendarFuture<V1> thenCombine(
                final CompletionStage<? extends U> other, final BiFunction<? super V, ? super U, ? extends V1> fn
        )
        {
            return wrap(future.thenCombine(other, fn));
        }

        @Override
        public <U, V1> CalendarFuture<V1> thenCombineAsync(
                final CompletionStage<? extends U> other, final BiFunction<? super V, ? super U, ? extends V1> fn
        )
        {
            return wrap(future.thenCombineAsync(other, fn));
        }

        @Override
        public <U, V1> CalendarFuture<V1> thenCombineAsync(
                final CompletionStage<? extends U> other,
                final BiFunction<? super V, ? super U, ? extends V1> fn,
                final Executor executor
        )
        {
            return wrap(future.thenCombineAsync(other, fn, executor));
        }

        @Override
        public <U> CalendarFuture<Void> thenAcceptBoth(
                final CompletionStage<? extends U> other, final BiConsumer<? super V, ? super U> action
        )
        {
            return wrap(future.thenAcceptBoth(other, action));
        }

        @Override
        public <U> CalendarFuture<Void> thenAcceptBothAsync(
                final CompletionStage<? extends U> other, final BiConsumer<? super V, ? super U> action
        )
        {
            return wrap(future.thenAcceptBothAsync(other, action));
        }

        @Override
        public <U> CalendarFuture<Void> thenAcceptBothAsync(
                final CompletionStage<? extends U> other,
                final BiConsumer<? super V, ? super U> action,
                final Executor executor
        )
        {
            return wrap(future.thenAcceptBothAsync(other, action, executor));
        }

        @Override
        public CalendarFuture<Void> runAfterBoth(
                final CompletionStage<?> other, final Runnable action
        )
        {
            return wrap(future.runAfterBoth(other, action));
        }

        @Override
        public CalendarFuture<Void> runAfterBothAsync(
                final CompletionStage<?> other, final Runnable action
        )
        {
            return wrap(future.runAfterBothAsync(other, action));
        }

        @Override
        public CalendarFuture<Void> runAfterBothAsync(
                final CompletionStage<?> other, final Runnable action, final Executor executor
        )
        {
            return wrap(future.runAfterBothAsync(other, action, executor));
        }

        @Override
        public <U> CalendarFuture<U> applyToEither(
                final CompletionStage<? extends V> other, final Function<? super V, U> fn
        )
        {
            return wrap(future.applyToEither(other, fn));
        }

        @Override
        public <U> CalendarFuture<U> applyToEitherAsync(
                final CompletionStage<? extends V> other, final Function<? super V, U> fn
        )
        {
            return wrap(future.applyToEitherAsync(other, fn));
        }

        @Override
        public <U> CalendarFuture<U> applyToEitherAsync(
                final CompletionStage<? extends V> other, final Function<? super V, U> fn, final Executor executor
        )
        {
            return wrap(future.applyToEitherAsync(other, fn, executor));
        }

        @Override
        public CalendarFuture<Void> acceptEither(
                final CompletionStage<? extends V> other, final Consumer<? super V> action
        )
        {
            return wrap(future.acceptEither(other, action));
        }

        @Override
        public CalendarFuture<Void> acceptEitherAsync(
                final CompletionStage<? extends V> other, final Consumer<? super V> action
        )
        {
            return wrap(future.acceptEitherAsync(other, action));
        }

        @Override
        public CalendarFuture<Void> acceptEitherAsync(
                final CompletionStage<? extends V> other, final Consumer<? super V> action, final Executor executor
        )
        {
            return wrap(future.acceptEitherAsync(other, action, executor));
        }

        @Override
        public CalendarFuture<Void> runAfterEither(
                final CompletionStage<?> other, final Runnable action
        )
        {
            return wrap(future.runAfterEither(other, action));
        }

        @Override
        public CalendarFuture<Void> runAfterEitherAsync(
                final CompletionStage<?> other, final Runnable action
        )
        {
            return wrap(future.runAfterEitherAsync(other, action));
        }

        @Override
        public CalendarFuture<Void> runAfterEitherAsync(
                final CompletionStage<?> other, final Runnable action, final Executor executor
        )
        {
            return wrap(future.runAfterEitherAsync(other, action, executor));
        }

        @Override
        public <U> CalendarFuture<U> thenCompose(final Function<? super V, ? extends CompletionStage<U>> fn)
        {
            return wrap(future.thenCompose(fn));
        }

        @Override
        public <U> CalendarFuture<U> thenComposeAsync(final Function<? super V, ? extends CompletionStage<U>> fn)
        {
            return wrap(future.thenComposeAsync(fn));
        }

        @Override
        public <U> CalendarFuture<U> thenComposeAsync(
                final Function<? super V, ? extends CompletionStage<U>> fn, final Executor executor
        )
        {
            return wrap(future.thenComposeAsync(fn, executor));
        }

        @Override
        public CalendarFuture<V> exceptionally(final Function<Throwable, ? extends V> fn)
        {
            return wrap(future.exceptionally(fn));
        }

        @Override
        public CalendarFuture<V> whenComplete(final BiConsumer<? super V, ? super Throwable> action)
        {
            return wrap(future.whenComplete(action));
        }

        @Override
        public CalendarFuture<V> whenCompleteAsync(final BiConsumer<? super V, ? super Throwable> action)
        {
            return wrap(future.whenCompleteAsync(action));
        }

        @Override
        public CalendarFuture<V> whenCompleteAsync(
                final BiConsumer<? super V, ? super Throwable> action, final Executor executor
        )
        {
            return wrap(future.whenCompleteAsync(action, executor));
        }

        @Override
        public <U> CalendarFuture<U> handle(final BiFunction<? super V, Throwable, ? extends U> fn)
        {
            return wrap(future.handle(fn));
        }

        @Override
        public <U> CalendarFuture<U> handleAsync(final BiFunction<? super V, Throwable, ? extends U> fn)
        {
            return wrap(future.handleAsync(fn));
        }

        @Override
        public <U> CalendarFuture<U> handleAsync(
                final BiFunction<? super V, Throwable, ? extends U> fn, final Executor executor
        )
        {
            return wrap(future.handleAsync(fn, executor));
        }

        @Override
        public CompletableFuture<V> toCompletableFuture()
        {
            return future;
        }

        @Override
        public String toString()
        {
            return MessageFormat.format(
                    "WrappedCompletableCalendarFuture'{'clock={0}, future={1}, instant={2}'}'", clock, future, instant
            );
        }

        public <T> WrappedCompletableCalendarFuture<T> wrap(final CompletableFuture<T> future)
        {
            return wrap(clock, instant, future);
        }
    }

    /**
     * Wraps an existing {@link java.util.concurrent.ScheduledExecutorService} to create a new {@code
     * DelayBasedCalendarExecutorService}.
     *
     * @param executor
     *         The underlying executor. Must not be {@code null}.
     * @return A new {@code DelayBasedCalendarExecutorService}.
     */
    public static DelayBasedCalendarExecutorService wrap(final ScheduledExecutorService executor)
    {
        return wrap(Clock.systemDefaultZone(), executor);
    }

    /**
     * Wraps an existing {@link java.util.concurrent.ScheduledExecutorService} to create a new {@code
     * DelayBasedCalendarExecutorService}.
     *
     * @param clock
     *         The clock to use to determine the current time. Must not be {@code null}.
     * @param executor
     *         The underlying executor. Must not be {@code null}.
     * @return A new {@code DelayBasedCalendarExecutorService}.
     */
    public static DelayBasedCalendarExecutorService wrap(final Clock clock, final ScheduledExecutorService executor)
    {
        if (clock == null) { throw new NullPointerException("The clock must not be null."); }
        if (executor == null) { throw new NullPointerException("The executor must not be null."); }
        return new DelayBasedCalendarExecutorService(clock, executor);
    }

    private final Clock clock;
    private final ScheduledExecutorService executor;
    private Duration adjustmentPeriod = Duration.ofHours(2);

    /**
     * Constructs a new {@code DelayBasedCalendarExecutorService}.
     *
     * @param executor
     *         The underlying executor.
     */
    private DelayBasedCalendarExecutorService(
            final Clock clock, final ScheduledExecutorService executor
    )
    {
        this.clock = clock;
        this.executor = executor;
    }

    /**
     * Gets the adjustment period for this service.
     *
     * @return The current adjustment period. The period is never {@code null} or negative.
     */
    public Duration getAdjustmentPeriod()
    {
        return adjustmentPeriod;
    }

    /**
     * Sets a new adjustment period for this service.
     * <p/>
     * All tasks are re-scheduled every adjustment period, in case the internal timer falls out-of-sync with the clock.
     * <p/>
     * The default adjustment period is 2 hours.
     *
     * @param adjustmentPeriod
     *         The new adjustment period. Must not be {@code null} or negative.
     */
    public void setAdjustmentPeriod(final Duration adjustmentPeriod)
    {
        if (adjustmentPeriod == null) { throw new NullPointerException("The adjustment period cannot be null."); }
        if (adjustmentPeriod.isNegative())
        {
            throw new IllegalArgumentException("The adjustment period cannot be negative.");
        }
        this.adjustmentPeriod = adjustmentPeriod;
    }

    /**
     * Gets the {@link Clock} being used by this service to determine the current time.
     *
     * @return A {@link java.time.Clock}.
     */
    public Clock getClock()
    {
        return clock;
    }

    public ScheduledExecutorService getExecutor()
    {
        return executor;
    }

    @Override
    public <V> CalendarFuture<V> scheduleDynamically(
            final Callable<V> action, final Instant initial, final ScheduleCallback<V> callback
    )
    {
        final AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>();
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final CompletableFuture<V> completable = new CompletableFuture<V>()
        {
            @Override
            public boolean cancel(final boolean mayInterruptIfRunning)
            {
                lock.readLock().lock();
                try
                {
                    future.get().cancel(mayInterruptIfRunning);
                    return super.cancel(mayInterruptIfRunning);
                }
                finally
                {
                    lock.readLock().unlock();
                }
            }
        };
        lock.writeLock().lock();
        try
        {
            future.set(
                    executor.schedule(
                            new Callable<Void>()
                            {
                                @Override
                                public Void call()
                                {
                                    try
                                    {
                                        final Duration countdown = Duration.between(initial, clock.instant());
                                        if (countdown.isNegative())
                                        {
                                            lock.writeLock().lock();
                                            try
                                            {
                                                future.lazySet(
                                                        executor.schedule(
                                                                this, Math.min(
                                                                        countdown.negated().toNanos(),
                                                                        adjustmentPeriod.toNanos()
                                                                ), TimeUnit.NANOSECONDS
                                                        )
                                                );
                                            }
                                            finally
                                            {
                                                lock.writeLock().unlock();
                                            }
                                        }
                                        else
                                        {
                                            final V result = action.call();
                                            if (callback != null)
                                            {
                                                final Instant next = callback.getNext(initial, result);
                                                if (next != null)
                                                {
                                                    lock.writeLock().lock();
                                                    try
                                                    {
                                                        future.lazySet(
                                                                executor.schedule(
                                                                        this, Math.min(
                                                                                Duration.between(clock.instant(), next)
                                                                                        .toNanos(),
                                                                                adjustmentPeriod.toNanos()
                                                                        ), TimeUnit.NANOSECONDS
                                                                )
                                                        );
                                                    }
                                                    finally
                                                    {
                                                        lock.writeLock().unlock();
                                                    }
                                                }
                                            }
                                            completable.complete(result);
                                        }
                                    }
                                    catch (final Exception ex)
                                    {
                                        completable.completeExceptionally(ex);
                                    }
                                    return null;
                                }
                            },
                            Math.min(Duration.between(clock.instant(), initial).toNanos(), adjustmentPeriod.toNanos()),
                            TimeUnit.NANOSECONDS
                    )
            );
        }
        finally
        {
            lock.writeLock().unlock();
        }
        return WrappedCompletableCalendarFuture.wrap(clock, initial, completable);
    }

    @Override
    public void shutdown()
    {
        executor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow()
    {
        return executor.shutdownNow();
    }

    @Override
    public boolean isShutdown()
    {
        return executor.isShutdown();
    }

    @Override
    public boolean isTerminated()
    {
        return executor.isTerminated();
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException
    {
        return executor.awaitTermination(timeout, unit);
    }

    @Override
    public String toString()
    {
        return MessageFormat.format(
                "DelayBasedCalendarExecutorService'{'clock={0}, executor={1}, adjustmentPeriod={2}'}'",
                clock,
                executor,
                adjustmentPeriod
        );
    }
}
