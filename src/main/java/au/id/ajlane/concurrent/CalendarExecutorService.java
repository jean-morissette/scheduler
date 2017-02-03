/*
 * Copyright 2016 Aaron Lane
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package au.id.ajlane.concurrent;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.cronutils.model.Cron;
import com.cronutils.model.time.ExecutionTime;
import org.joda.time.DateTime;


/**
 * A {@link ScheduledExecutorService} which can schedule tasks to occur at particular times.
 * <p>
 * Implementations <em>may</em> delay tasks to execute after their scheduled time, but <em>must not</em> execute tasks
 * before their scheduled time.
 * <p>
 * If there are no other scheduling concerns (such as priority), a {@code au.id.ajlane.concurrent
 * .CalendarExecutorService} should make a best-effort attempt to ensure that tasks are executed in time-sorted order.
 */
public interface CalendarExecutorService extends ScheduledExecutorService
{
    /**
     * Creates a new {@code CalendarExecutorService} which uses an underlying {@link ScheduledExecutorService} to
     * schedule and execute tasks.
     *
     * @param executor
     *     The service to decorate.
     *
     * @return A new {@code CalendarExecutorService}.
     */
    static CalendarExecutorService decorate(final ScheduledExecutorService executor)
    {
        return DelayBasedCalendarExecutorService.wrap(executor);
    }

    /**
     * Creates a new {@code CalendarExecutorService} which executes scheduled tasks sequentially on a single thread.
     *
     * @param threadFactory
     *     A factory to create the executor thread.
     *
     * @return A new {@code CalendarExecutorService}.
     */
    static CalendarExecutorService singleThread(final ThreadFactory threadFactory)
    {
        return DelayBasedCalendarExecutorService.wrap(
            Executors.newSingleThreadScheduledExecutor(
                threadFactory
            )
        );
    }

    /**
     * Creates a new {@code CalendarExecutorService} which executes scheduled tasks sequentially on a single thread.
     *
     * @return A new {@code CalendarExecutorService}.
     */
    static CalendarExecutorService singleThread()
    {
        return DelayBasedCalendarExecutorService.wrap(
            Executors.newSingleThreadScheduledExecutor()
        );
    }

    /**
     * Creates a new {@code CalendarExecutorService} which services tasks with a pool of threads equal to the number of
     * available processors.
     *
     * @return A new {@code CalendarExecutorService}.
     */
    static CalendarExecutorService threadPool()
    {
        return threadPool(Runtime.getRuntime()
            .availableProcessors());
    }

    /**
     * Creates a new {@code CalendarExecutorService} which services tasks with a pool of threads equal to the number of
     * available processors.
     *
     * @param threadFactory
     *     A factory to create the pool threads.
     *
     * @return A new {@code CalendarExecutorService}.
     */
    static CalendarExecutorService threadPool(final ThreadFactory threadFactory)
    {
        return threadPool(Runtime.getRuntime()
            .availableProcessors(), threadFactory);
    }

    /**
     * Creates a new {@code CalendarExecutorService} which services tasks with a pool of threads.
     *
     * @param poolSize
     *     The number of threads in the pool.
     * @param threadFactory
     *     A factory to create the pool threads.
     *
     * @return A new {@code CalendarExecutorService}.
     */
    static CalendarExecutorService threadPool(
        final int poolSize, final ThreadFactory threadFactory
    )
    {
        return DelayBasedCalendarExecutorService.wrap(
            Executors.newScheduledThreadPool(
                poolSize, threadFactory
            )
        );
    }

    /**
     * Creates a new {@code CalendarExecutorService} which services tasks with a pool of threads.
     *
     * @param poolSize
     *     The number of threads in the pool.
     *
     * @return A new {@code CalendarExecutorService}.
     */
    static CalendarExecutorService threadPool(final int poolSize)
    {
        return DelayBasedCalendarExecutorService.wrap(Executors.newScheduledThreadPool(poolSize));
    }

    @Override
    default void execute(final Runnable command)
    {
        schedule(command, Instant.now(getClock()));
    }

    /**
     * Gets the {@link Clock} being used by this service to determine the current time.
     *
     * @return A {@link Clock}.
     */
    Clock getClock();

    @Override
    default <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks)
        throws InterruptedException
    {
        final List<Future<T>> futures = new ArrayList<>(tasks.size());
        for (final Callable<T> task : tasks)
        {
            if (Thread.interrupted())
            {
                throw new InterruptedException("The thread was interrupted while scheduling tasks.");
            }
            futures.add(submit(task));
        }
        for (final Future<T> future : futures)
        {
            try
            {
                future.get();
            }
            catch (final ExecutionException e)
            {
                // Ignore - the caller can check the future themselves if they care about the exception.
            }
        }
        return futures;
    }

    @Override
    default <T> List<Future<T>> invokeAll(
        final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit
    ) throws InterruptedException
    {
        final List<Future<T>> futures = new ArrayList<>(tasks.size());
        for (final Callable<T> task : tasks)
        {
            if (Thread.interrupted())
            {
                throw new InterruptedException("The thread was interrupted while submitting tasks.");
            }
            futures.add(submit(task));
        }

        final long end = System.nanoTime() + unit.toNanos(timeout);

        for (final Future<T> future : futures)
        {
            final long diff = end - System.nanoTime();
            if (diff < 0L)
            {
                return futures;
            }
            try
            {
                future.get(diff, TimeUnit.NANOSECONDS);
            }
            catch (final TimeoutException ex)
            {
                // Ignore and return the list of futures as they are.
                return futures;
            }
            catch (final ExecutionException ex)
            {
                // Ignore - the caller can check the future themselves if they care about the exception.
            }
        }

        return futures;
    }

    @Override
    default <T> T invokeAny(final Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException
    {
        final Collection<CalendarFuture<T>> futures = new ArrayList<>(tasks.size());
        final Lock lock = new ReentrantLock();
        lock.lock();
        try
        {
            final Condition wait = lock.newCondition();
            final AtomicReference<CalendarFuture<T>> success = new AtomicReference<>();
            final List<Throwable> failures = Collections.synchronizedList(new ArrayList<>(tasks.size()));

            for (final Callable<T> task : tasks)
            {
                final CalendarFuture<T> future = submit(task);
                futures.add(future);
                future.whenComplete(
                    (value, failure) ->
                    {
                        if (failure != null)
                        {
                            failures.add(failure);
                        }
                        else
                        {
                            success.set(future);
                            wait.signalAll();
                        }
                    }
                );
            }


            while (success.get() == null && failures.size() < tasks.size())
            {
                wait.await();
                // Loop in case of spurious wake-ups.
            }

            final CalendarFuture<T> successFuture = success.get();
            if (successFuture != null)
            {
                return successFuture.get();
            }
            else
            {
                throw new MultipleExecutionException(failures);
            }
        }
        finally
        {
            lock.unlock();
            for (final CalendarFuture<T> future : futures)
            {
                future.cancel(true);
            }
        }
    }

    @Override
    default <T> T invokeAny(
        final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit
    ) throws InterruptedException, ExecutionException, TimeoutException
    {
        final Collection<CalendarFuture<T>> futures = new ArrayList<>(tasks.size());
        final Lock lock = new ReentrantLock();
        lock.lock();
        try
        {
            final Condition wait = lock.newCondition();
            final AtomicReference<CalendarFuture<T>> success = new AtomicReference<>();
            final List<Throwable> failures = Collections.synchronizedList(new ArrayList<>(tasks.size()));

            for (final Callable<T> task : tasks)
            {
                final CalendarFuture<T> future = submit(task);
                futures.add(future);
                future.whenComplete(
                    (value, failure) ->
                    {
                        if (failure != null)
                        {
                            failures.add(failure);
                        }
                        else
                        {
                            success.set(future);
                            wait.signalAll();
                        }
                    }
                );
            }

            long remaining = unit.toNanos(timeout);
            while (success.get() == null
                && failures.size() < tasks.size()
                && (remaining = wait.awaitNanos(remaining)) > 0L)
            {
                // Loop in case of spurious wake-ups.
            }

            final CalendarFuture<T> successFuture = success.get();
            if (successFuture != null)
            {
                return successFuture.get();
            }
            else if (remaining <= 0L)
            {
                throw new TimeoutException(
                    "No task completed within " + timeout + " " + unit.toString()
                        .toLowerCase(Locale.ROOT) + "."
                );
            }
            else
            {
                throw new MultipleExecutionException(failures);
            }
        }
        finally
        {
            lock.unlock();
            for (final CalendarFuture<T> future : futures)
            {
                future.cancel(true);
            }
        }
    }

    /**
     * Schedules a task to be executed at a particular time.
     *
     * @param action
     *     The action to execute.
     * @param instant
     *     The time to execute the action.
     *
     * @return A {@link CalendarFuture} representing the scheduled task.
     */
    default CalendarFuture<?> schedule(final Runnable action, final Instant instant)
    {
        return schedule(Executors.callable(action), instant);
    }

    /**
     * Schedules a task to be executed at a particular time.
     *
     * @param <V>
     *     The type of the value returned by the task. Must not be null.
     * @param action
     *     The action to execute. Must not be null.
     * @param instant
     *     The time to execute the action.
     *
     * @return A {@link CalendarFuture} representing the scheduled task.
     */
    default <V> CalendarFuture<V> schedule(final Callable<V> action, final Instant instant)
    {
        return scheduleDynamically(action, instant, (t, v) -> null);
    }

    /**
     * Schedules a task to be executed according to a cron expression.
     *
     * @param action
     *     The action to execute. Must not be null.
     * @param expression
     *     The cron expression describing the execution schedule. Must not be null.
     * @param <V>
     *     The type of the value returned by the task.
     *
     * @return A future representing the scheduled task.
     */
    default <V> CalendarFuture<V> schedule(final Callable<V> action, final Cron expression)
    {
        return schedule(action, Instant.now(getClock()), expression);
    }

    /**
     * Schedules a task to be executed according to a cron expression.
     *
     * @param action
     *     The action to execute. Must not be null. Must not be null.
     * @param after
     *     A reference time, after which the schedule will begin. Must not be null.
     * @param expression
     *     The cron expression describing the execution schedule. Must not be null.
     * @param <V>
     *     The type of value returned by the task.
     *
     * @return A future representing the scheduled task.
     */
    default <V> CalendarFuture<V> schedule(final Callable<V> action, final Instant after, final Cron expression)
    {
        final ExecutionTime calculator = ExecutionTime.forCron(expression);
        final boolean useAfter =
            calculator.timeToNextExecution(new DateTime(Date.from(after.minusMillis(1)))).getMillis() == 1;
        final Instant initial = useAfter ?
            after :
            calculator.nextExecution(new DateTime(Date.from(after)))
                .toDate()
                .toInstant();
        return scheduleDynamically(
            action,
            initial,
            (previousInstant, previousValue) ->
                calculator.nextExecution(new DateTime(Date.from(previousInstant)))
                    .toDate()
                    .toInstant()
        );
    }

    @Override
    default CalendarFuture<?> schedule(
        final Runnable command, final long delay, final TimeUnit unit
    )
    {
        return schedule(Executors.callable(command), delay, unit);
    }

    @Override
    default <V> CalendarFuture<V> schedule(
        final Callable<V> callable, final long delay, final TimeUnit unit
    )
    {
        return scheduleDynamically(
            callable,
            Instant.now(getClock())
                .plus(delay, TimeUnits.toTemporalUnit(unit)),
            (t, v) -> null
        );
    }

    @Override
    default CalendarFuture<?> scheduleAtFixedRate(
        final Runnable command, final long initialDelay, final long period, final TimeUnit unit
    )
    {
        return scheduleDynamically(
            Executors.callable(command),
            Instant.now(getClock())
                .plus(initialDelay, TimeUnits.toTemporalUnit(unit)),
            (t, v) -> t.plus(period, TimeUnits.toTemporalUnit(unit))
        );
    }

    /**
     * Schedules a periodic task that runs at regular intervals.
     *
     * @param command
     *     A task to execute. Must not be null.
     * @param period
     *     A period between successive executions
     * @param unit
     *     The time unit of the period parameter
     *
     * @return A future representing the scheduled task.
     */
    default CalendarFuture<?> scheduleAtFixedRate(
        final Runnable command, final long period, final TimeUnit unit
    )
    {
        return scheduleDynamically(
            Executors.callable(command),
            Instant.now(getClock())
                .plus(period, TimeUnits.toTemporalUnit(unit)),
            (t, v) -> t.plus(period, TimeUnits.toTemporalUnit(unit))
        );
    }

    /**
     * Schedules an action to be executed at regular intervals from a particular time.
     * <p>
     * The service will not wait for the first task to finish before executing subsequent tasks. To schedule a fixed
     * delay between tasks, use {@code scheduleWithFixedDelay(Runnable, Instant, long, TimeUnit)}.
     *
     * @param action
     *     The action to execute.
     * @param from
     *     The time to first execute the action.
     * @param period
     *     The time to wait.
     * @param units
     *     The units for the period.
     *
     * @return A {@link CalendarFuture} representing the scheduled task.
     */
    default CalendarFuture<?> scheduleAtFixedRate(
        final Runnable action, final Instant from, final long period, final TimeUnit units
    )
    {
        return scheduleDynamically(
            Executors.callable(action), from, (t, v) -> t.plus(period, TimeUnits.toTemporalUnit(units))
        );
    }

    /**
     * Schedules an action to be executed at regular intervals from a particular time.
     * <p>
     * The service will not wait for the first task to finish before executing subsequent tasks. To schedule a fixed
     * delay between tasks, use {@code scheduleWithFixedDelay(Runnable, Instant, TemporalAmount)}.
     *
     * @param action
     *     The action to execute.
     * @param from
     *     The time to first execute the action.
     * @param period
     *     The time to wait.
     *
     * @return A {@link CalendarFuture} representing the scheduled task.
     */
    default CalendarFuture<?> scheduleAtFixedRate(
        final Runnable action, final Instant from, final TemporalAmount period
    )
    {
        return scheduleDynamically(Executors.callable(action), from, (t, v) -> t.plus(period));
    }

    /**
     * Schedules a periodic task to execute at a fixed rate.
     *
     * @param action
     *     The action to execute.
     * @param period
     *     The time between tasks.
     *
     * @return A future representing
     */
    default CalendarFuture<?> scheduleAtFixedRate(
        final Runnable action, final TemporalAmount period
    )
    {
        return scheduleDynamically(Executors.callable(action), (t, v) -> t.plus(period));
    }

    /**
     * Schedules an action to be executed many times, but delegates calculating the appropriate time for subsequent
     * tasks to a {@link ScheduleCallback}.
     *
     * @param action
     *     The action to execute.
     * @param initial
     *     The time for the first task to be executed.
     * @param callback
     *     A {@link ScheduleCallback} which will provide the times for subsequent tasks to be executed.
     * @param <V>
     *     The type of value returned by the action.
     *
     * @return A {@link CalendarFuture} representing the scheduled task.
     */
    <V> CalendarFuture<V> scheduleDynamically(
        final Callable<V> action, final Instant initial, final ScheduleCallback<V> callback
    );

    /**
     * Schedules an action to be executed many times, but delegates calculating the appropriate time for subsequent
     * tasks to a {@link ScheduleCallback}.
     *
     * @param action
     *     The action to execute.
     * @param callback
     *     A {@link ScheduleCallback} which will provide the times for subsequent tasks to be executed.
     * @param <V>
     *     The type of value returned by the action.
     *
     * @return A {@link CalendarFuture} representing the scheduled task.
     */
    default <V> CalendarFuture<V> scheduleDynamically(
        final Callable<V> action, final ScheduleCallback<V> callback
    )
    {
        final Instant initial = callback.getNext(Instant.now(getClock()), null);
        return scheduleDynamically(action, initial, callback);
    }

    /**
     * Schedules an action to be executed many times, but delegates calculating the appropriate time for subsequent
     * tasks to a {@link TemporalAdjuster}.
     *
     * @param action
     *     The action to execute.
     * @param initial
     *     The time for the first task to be executed.
     * @param adjuster
     *     A {@link TemporalAdjuster} which will provide the times for subsequent tasks to be executed.
     * @param <V>
     *     The type of value returned by the action.
     *
     * @return A {@link CalendarFuture} representing the scheduled task.
     */
    default <V> CalendarFuture<V> scheduleDynamically(
        final Callable<V> action, final Instant initial, final TemporalAdjuster adjuster
    )
    {
        return scheduleDynamically(action, initial, (t, v) -> Instant.from(adjuster.adjustInto(t)));
    }

    /**
     * Schedules an action to be executed many times, but delegates calculating the appropriate time for subsequent
     * tasks to a {@link TemporalAdjuster}.
     *
     * @param action
     *     The action to execute.
     * @param adjuster
     *     A {@link TemporalAdjuster} which will provide the times for subsequent tasks to be executed.
     * @param <V>
     *     The type of value returned by the action.
     *
     * @return A {@link CalendarFuture} representing the scheduled task.
     */
    default <V> CalendarFuture<V> scheduleDynamically(
        final Callable<V> action, final TemporalAdjuster adjuster
    )
    {
        return scheduleDynamically(action, (t, v) -> Instant.from(adjuster.adjustInto(t)));
    }

    @Override
    default CalendarFuture<?> scheduleWithFixedDelay(
        final Runnable command, final long initialDelay, final long delay, final TimeUnit unit
    )
    {
        return scheduleDynamically(
            Executors.callable(command),
            Instant.now(getClock())
                .plus(initialDelay, TimeUnits.toTemporalUnit(unit)),
            (t, v) -> Instant.now(getClock())
                .plus(delay, TimeUnits.toTemporalUnit(unit))
        );
    }

    /**
     * Schedules a task to execute with a fixed delay between executions.
     *
     * @param command
     *     The task to execute.
     * @param delay
     *     The delay to wait.
     * @param unit
     *     The units for the delay.
     *
     * @return A future representing the scheduled task.
     */
    default CalendarFuture<?> scheduleWithFixedDelay(
        final Runnable command, final long delay, final TimeUnit unit
    )
    {
        return scheduleDynamically(
            Executors.callable(command),
            Instant.now(getClock())
                .plus(delay, TimeUnits.toTemporalUnit(unit)),
            (t, v) -> Instant.now(getClock())
                .plus(delay, TimeUnits.toTemporalUnit(unit))
        );
    }

    @Override
    default <T> CalendarFuture<T> submit(final Callable<T> task)
    {
        return scheduleDynamically(task, Instant.now(getClock()), (t, v) -> null);
    }

    @Override
    default <T> CalendarFuture<T> submit(final Runnable task, final T result)
    {
        return submit(Executors.callable(task, result));
    }

    @Override
    default CalendarFuture<?> submit(final Runnable task)
    {
        return submit(Executors.callable(task));
    }
}
