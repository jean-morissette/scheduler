package au.id.ajlane.concurrent;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import au.id.ajlane.time.TestClock;
import org.junit.Assert;
import org.junit.Test;

public final class CalendarExecutorServiceTest
{
    @Test
    public void simpleScheduling() throws Exception
    {
        final Duration interval = Duration.ofMinutes(1L);

        final Instant t0 = Instant.now();
        final Instant tN1 = t0.minus(interval);
        final Instant t1 = t0.plus(interval);
        final Instant t2 = t1.plus(interval);
        final Instant t3 = t2.plus(interval);

        final TestClock clock = new TestClock(interval, t0);
        @SuppressWarnings("CastToConcreteClass")
        final DelayBasedCalendarExecutorService executor = (DelayBasedCalendarExecutorService) CalendarExecutorService.threadPool(
                clock
        );
        executor.setAdjustmentPeriod(Duration.ofMillis(500L));

        final CalendarFuture<Instant> tN1Future = executor.schedule(() -> tN1, tN1);
        final CalendarFuture<Instant> t0Future = executor.schedule(() -> t0, t0);
        final CalendarFuture<Instant> t1Future = executor.schedule(() -> t1, t1);
        final CalendarFuture<Instant> t2Future = executor.schedule(() -> t2, t2);
        final CalendarFuture<Instant> t3Future = executor.schedule(() -> t3, t3);

        Assert.assertEquals(tN1, tN1Future.get(1L, TimeUnit.SECONDS));
        Assert.assertEquals(t0, t0Future.get(1L, TimeUnit.SECONDS));
        Assert.assertFalse(t1Future.isDone());
        Assert.assertFalse(t2Future.isDone());
        Assert.assertFalse(t3Future.isDone());

        clock.tick();

        Assert.assertEquals(t1, t1Future.get(1L, TimeUnit.SECONDS));
        Assert.assertFalse(t2Future.isDone());
        Assert.assertFalse(t3Future.isDone());

        clock.tick(3);

        Assert.assertEquals(t2, t2Future.get(1L, TimeUnit.SECONDS));
        Assert.assertEquals(t3, t3Future.get(1L, TimeUnit.SECONDS));
    }
}
