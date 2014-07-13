package au.id.ajlane.concurrent;

import java.time.Instant;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledFuture;

/**
 * A result-bearing, cancellable action which is scheduled to occur at a particular time.
 * <p/>
 * All instances of {@code CalendarFuture} are also instances of {@link CompletionStage}.
 *
 * @see CalendarExecutorService
 */
public interface CalendarFuture<V> extends ScheduledFuture<V>, CompletionStage<V>
{
    public Instant getInstant();
}
