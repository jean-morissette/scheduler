package au.id.ajlane.time;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A clock which only advances time when prompted.
 * <p/>
 * This is useful for consistently testing clock-based functions.
 */
public final class TestClock extends Clock
{
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final TemporalAmount tickSize;
    private final ZoneId zone;
    private Instant instant;

    /**
     * Constructs a new {@code TestClock}.
     *
     * @param zone
     *         The time-zone for the clock.
     * @param tickSize
     *         The amount that the clock advances by when {@link #tick()} is called.
     * @param instant
     *         The initial time for the clock.
     */
    public TestClock(final ZoneId zone, final TemporalAmount tickSize, final Instant instant)
    {
        this.zone = zone;
        this.tickSize = tickSize;
        this.instant = instant;
    }

    /**
     * Constructs a new {@code TestClock} with the system-default time-zone.
     *
     * @param interval
     *         The amount that the clock advances by when {@link #tick()} is called.
     * @param instant
     *         The initial time for the clock.
     */
    public TestClock(final TemporalAmount interval, final Instant instant)
    {
        this(ZoneId.systemDefault(), interval, instant);
    }

    @Override
    public ZoneId getZone()
    {
        return zone;
    }

    @Override
    public Clock withZone(final ZoneId newZone)
    {
        return new TestClock(newZone, tickSize, instant);
    }

    @Override
    public Instant instant()
    {
        lock.readLock().lock();
        try
        {
            return instant;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    /**
     * Advances the clock by {@code n} ticks.
     *
     * @param n
     *         The number of ticks.
     * @see #tick()
     */
    public void tick(final int n)
    {
        lock.writeLock().lock();
        try
        {
            for (int i = 0; i < n; i++)
            {
                tick();
            }
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    /**
     * Advances the clock by one tick.
     */
    public void tick()
    {
        lock.writeLock().lock();
        try
        {
            instant = instant.plus(tickSize);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String toString()
    {
        return instant.toString();
    }
}
