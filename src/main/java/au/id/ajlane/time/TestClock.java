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

package au.id.ajlane.time;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A clock which only advances time when prompted.
 * <p>
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
     *     The time-zone for the clock.
     * @param tickSize
     *     The amount that the clock advances by when {@link #tick()} is called.
     * @param instant
     *     The initial time for the clock.
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
     *     The amount that the clock advances by when {@link #tick()} is called.
     * @param instant
     *     The initial time for the clock.
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
        lock.readLock()
            .lock();
        try
        {
            return instant;
        }
        finally
        {
            lock.readLock()
                .unlock();
        }
    }

    /**
     * Advances the clock by {@code n} ticks.
     *
     * @param n
     *     The number of ticks.
     *
     * @see #tick()
     */
    public void tick(final int n)
    {
        lock.writeLock()
            .lock();
        try
        {
            for (int i = 0; i < n; i++)
            {
                tick();
            }
        }
        finally
        {
            lock.writeLock()
                .unlock();
        }
    }

    /**
     * Advances the clock by one tick.
     */
    public void tick()
    {
        lock.writeLock()
            .lock();
        try
        {
            instant = instant.plus(tickSize);
        }
        finally
        {
            lock.writeLock()
                .unlock();
        }
    }

    @Override
    public String toString()
    {
        return instant.toString();
    }
}
