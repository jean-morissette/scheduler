package au.id.ajlane.concurrent;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjuster;

import org.junit.Assert;
import org.junit.Test;

public final class SchedulingAdjustersTest
{
    @Test
    public void testNextMonth()
    {
        final OffsetDateTime jan = OffsetDateTime.of(2000, 1, 31, 3, 0, 0, 0, ZoneOffset.ofHours(0));
        final OffsetDateTime feb = OffsetDateTime.of(2000, 2, 29, 4, 0, 0, 0, ZoneOffset.ofHours(1));
        final OffsetDateTime mar = OffsetDateTime.of(2000, 3, 31, 5, 0, 0, 0, ZoneOffset.ofHours(2));
        final OffsetDateTime apr = OffsetDateTime.of(2000, 4, 30, 6, 0, 0, 0, ZoneOffset.ofHours(3));
        final OffsetDateTime may = OffsetDateTime.of(2000, 5, 31, 7, 0, 0, 0, ZoneOffset.ofHours(4));
        final OffsetDateTime jun = OffsetDateTime.of(2000, 6, 30, 8, 0, 0, 0, ZoneOffset.ofHours(5));
        final OffsetDateTime jul = OffsetDateTime.of(2000, 7, 31, 9, 0, 0, 0, ZoneOffset.ofHours(6));
        final OffsetDateTime aug = OffsetDateTime.of(2000, 8, 31, 10, 0, 0, 0, ZoneOffset.ofHours(7));
        final OffsetDateTime sep = OffsetDateTime.of(2000, 9, 30, 11, 0, 0, 0, ZoneOffset.ofHours(8));
        final OffsetDateTime oct = OffsetDateTime.of(2000, 10, 31, 12, 0, 0, 0, ZoneOffset.ofHours(9));
        final OffsetDateTime nov = OffsetDateTime.of(2000, 11, 30, 13, 0, 0, 0, ZoneOffset.ofHours(10));
        final OffsetDateTime dec = OffsetDateTime.of(2000, 12, 31, 14, 0, 0, 0, ZoneOffset.ofHours(11));
        final OffsetDateTime jan2 = OffsetDateTime.of(2001, 1, 31, 15, 0, 0, 0, ZoneOffset.ofHours(12));
        final OffsetDateTime feb2 = OffsetDateTime.of(2001, 2, 28, 16, 0, 0, 0, ZoneOffset.ofHours(13));

        final TemporalAdjuster adjuster = SchedulingAdjusters.nextMonth();

        OffsetDateTime current = jan;
        Assert.assertEquals(jan.toInstant(), current.toInstant());
        current = current.with(adjuster);
        Assert.assertEquals(feb.toInstant(), current.toInstant());
        current = current.with(adjuster);
        Assert.assertEquals(mar.toInstant(), current.toInstant());
        current = current.with(adjuster);
        Assert.assertEquals(apr.toInstant(), current.toInstant());
        current = current.with(adjuster);
        Assert.assertEquals(may.toInstant(), current.toInstant());
        current = current.with(adjuster);
        Assert.assertEquals(jun.toInstant(), current.toInstant());
        current = current.with(adjuster);
        Assert.assertEquals(jul.toInstant(), current.toInstant());
        current = current.with(adjuster);
        Assert.assertEquals(aug.toInstant(), current.toInstant());
        current = current.with(adjuster);
        Assert.assertEquals(sep.toInstant(), current.toInstant());
        current = current.with(adjuster);
        Assert.assertEquals(oct.toInstant(), current.toInstant());
        current = current.with(adjuster);
        Assert.assertEquals(nov.toInstant(), current.toInstant());
        current = current.with(adjuster);
        Assert.assertEquals(dec.toInstant(), current.toInstant());
        current = current.with(adjuster);
        Assert.assertEquals(jan2.toInstant(), current.toInstant());
        current = current.with(adjuster);
        Assert.assertEquals(feb2.toInstant(), current.toInstant());
    }

    @Test
    public void testNextTime()
    {
        final OffsetTime time = OffsetTime.of(18, 0, 0, 0, ZoneOffset.UTC);

        final OffsetDateTime t1 = OffsetDateTime.of(2000, 1, 1, 16, 0, 0, 0, ZoneOffset.UTC);
        final OffsetDateTime t2 = OffsetDateTime.of(2000, 1, 1, 20, 0, 0, 0, ZoneOffset.UTC);

        final TemporalAdjuster adjuster = SchedulingAdjusters.nextTime(time);

        final OffsetDateTime t1e = OffsetDateTime.of(2000, 1, 1, 18, 0, 0, 0, ZoneOffset.UTC);
        final OffsetDateTime t2e = OffsetDateTime.of(2000, 1, 2, 18, 0, 0, 0, ZoneOffset.UTC);

        Assert.assertEquals(t1e, t1.with(adjuster));
        Assert.assertEquals(t2e, t2.with(adjuster));
    }
}

