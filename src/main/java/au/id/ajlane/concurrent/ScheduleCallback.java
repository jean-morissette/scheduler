package au.id.ajlane.concurrent;

import java.time.Instant;

/**
 * A function which can provide the time to schedule the next in a series of tasks.
 *
 * @param <V>
 *         The type of the value returned by the scheduled action.
 * @see CalendarExecutorService
 */
@FunctionalInterface
public interface ScheduleCallback<V>
{
    /**
     * Get the time to schedule the next action.
     *
     * @param previousInstant
     *         The scheduled time of the previous task in the series.
     * @param previousValue
     *         The return value of the previous task in the series. {@code null} if the task does not have a return
     *         value.
     * @return An {@link java.time.Instant}. If the time has already passed, then the action should be scheduled to
     * occur as soon as possible. May not be {@code null}.
     */
    Instant getNext(final Instant previousInstant, final V previousValue);
}
