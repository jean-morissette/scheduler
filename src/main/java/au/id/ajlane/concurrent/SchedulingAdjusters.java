package au.id.ajlane.concurrent;

import java.time.DayOfWeek;
import java.time.OffsetTime;
import java.time.YearMonth;
import java.time.temporal.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Utilities for producing instances of {@link java.time.temporal.TemporalAdjuster} that are useful for scheduling
 * regular tasks.
 */
public abstract class SchedulingAdjusters
{
    /**
     * Composes a {@code TemporalAdjusters} from a series of existing adjusters.
     *
     * @param adjusters
     *         The series of adjusters to combine.
     * @return A {@code TemporalAdjuster}.
     */
    public static TemporalAdjuster chain(final TemporalAdjuster... adjusters)
    {
        return chain(Arrays.asList(adjusters));
    }

    /**
     * Composes a {@code TemporalAdjusters} from a series of existing adjusters.
     *
     * @param adjusters
     *         The series of adjusters to combine.
     * @return A {@code TemporalAdjuster}.
     */
    public static TemporalAdjuster chain(final Collection<TemporalAdjuster> adjusters)
    {
        return chain(adjusters.stream());
    }

    /**
     * Composes a {@code TemporalAdjusters} from a series of existing adjusters.
     *
     * @param adjusters
     *         The series of adjusters to combine.
     * @return A {@code TemporalAdjuster}.
     */
    public static TemporalAdjuster chain(final Stream<TemporalAdjuster> adjusters)
    {
        return t0 -> adjusters.reduce(t0, (t1, a) -> a.adjustInto(t1), (t1, t2) -> t2);
    }

    public static TemporalAdjuster nearestWeekday()
    {
        return t -> {
            switch (DayOfWeek.from(t))
            {
                case SATURDAY:
                    return t.minus(1, ChronoUnit.DAYS);
                case SUNDAY:
                    return t.plus(1, ChronoUnit.DAYS);
                default:
                    return t;
            }
        };
    }

    /**
     * Advances to the same time on the next day.
     *
     * @return A {@link java.time.temporal.TemporalAdjuster}.
     */
    public static TemporalAdjuster nextDay()
    {
        return t -> t.plus(1L, ChronoUnit.DAYS);
    }

    public static TemporalAdjuster nextDayOfMonth(final int dayOfMonth)
    {
        return t -> {
            YearMonth yearMonth = YearMonth.from(t);
            if (t.get(ChronoField.DAY_OF_MONTH) <= dayOfMonth)
            {
                yearMonth = yearMonth.plusMonths(1);
            }

            // We adjust to the first day of the month first, to avoid creating a transitory date which is invalid.
            return t.with(ChronoField.DAY_OF_MONTH, 1)
                    .with(ChronoField.MONTH_OF_YEAR, yearMonth.getMonthValue())
                    .with(ChronoField.YEAR, yearMonth.getYear())
                    .with(ChronoField.DAY_OF_MONTH, Math.min(dayOfMonth, yearMonth.lengthOfMonth()));
        };
    }

    /**
     * Advances to the same time on the next instance of a day-of-week.
     *
     * @param dayOfWeek
     *         The day-of-week to advance to. May not be {@code null}.
     * @return A {@link java.time.temporal.TemporalAdjuster}.
     */
    public static TemporalAdjuster nextDayOfWeek(final DayOfWeek dayOfWeek)
    {
        return TemporalAdjusters.next(dayOfWeek);
    }

    /**
     * Advances to the same time in the next hour.
     *
     * @return A {@link java.time.temporal.TemporalAdjuster}.
     */
    public static TemporalAdjuster nextHour()
    {
        return t -> t.plus(1L, ChronoUnit.HOURS);
    }

    /**
     * Advances to the same day in the next month.
     * <p/>
     * If the day does not exist, then the nearest date in next month will be used. For example, if the first date is
     * the 31st of January, the next two dates will be the 28th or 29th of February (as appropriate) and the 31st of
     * March.
     *
     * @return A {@link java.time.temporal.TemporalAdjuster}.
     */
    public static TemporalAdjuster nextMonth()
    {
        return new TemporalAdjuster()
        {
            private final AtomicInteger dayOfMonth = new AtomicInteger(-1);

            @Override
            public Temporal adjustInto(final Temporal temporal)
            {
                dayOfMonth.compareAndSet(-1, temporal.get(ChronoField.DAY_OF_MONTH));

                final YearMonth yearMonth = YearMonth.from(temporal).plusMonths(1);

                // We adjust to the first day of the month first, to avoid creating a transitory date which is invalid.
                return temporal.with(ChronoField.DAY_OF_MONTH, 1)
                               .with(ChronoField.MONTH_OF_YEAR, yearMonth.getMonthValue())
                               .with(ChronoField.YEAR, yearMonth.getYear())
                               .with(ChronoField.DAY_OF_MONTH, Math.min(dayOfMonth.get(), yearMonth.lengthOfMonth()));
            }
        };
    }

    /**
     * Advances to a particular time.
     *
     * @param time
     *         The time to advance to. May not be {@code null}.
     * @return A {@link java.time.temporal.TemporalAdjuster}.
     */
    public static TemporalAdjuster nextTime(final OffsetTime time)
    {
        return previous -> {
            Temporal next = previous;
            final boolean wrap = next.getLong(ChronoField.NANO_OF_DAY) > time.getLong(ChronoField.NANO_OF_DAY);
            next = next.with(ChronoField.NANO_OF_DAY, time.getLong(ChronoField.NANO_OF_DAY));
            if (wrap) { next = next.plus(1L, ChronoUnit.DAYS); }
            return next;
        };
    }

    /**
     * Advances to the same time next week.
     *
     * @return A {@link java.time.temporal.TemporalAdjuster}.
     */
    public static TemporalAdjuster nextWeek()
    {
        return t -> t.plus(1L, ChronoUnit.WEEKS);
    }

    /**
     * Advances to the same time next year.
     *
     * @return A {@link java.time.temporal.TemporalAdjuster}.
     */
    public static TemporalAdjuster nextYear()
    {
        return t -> t.plus(1L, ChronoUnit.YEARS);
    }
}
