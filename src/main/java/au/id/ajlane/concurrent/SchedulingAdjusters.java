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

import java.time.DayOfWeek;
import java.time.OffsetTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Utilities for producing instances of {@link java.time.temporal.TemporalAdjuster} that are useful for scheduling
 * regular tasks.
 * <p>
 * A key feature of the adjusters provided by these utilities is that they never adjust a given {@link Temporal} to be
 * earlier than it was before. This allows them to be arbitrarily combined using the {@link #chain} methods without
 * creating any infinite loops.
 */
public abstract class SchedulingAdjusters
{
    /**
     * Composes a {@code TemporalAdjusters} from a series of existing adjusters.
     *
     * @param adjusters
     *     The series of adjusters to combine.
     *
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
     *     The series of adjusters to combine.
     *
     * @return A {@code TemporalAdjuster}.
     */
    public static TemporalAdjuster chain(final Collection<TemporalAdjuster> adjusters)
    {
        return chain(adjusters::stream);
    }

    /**
     * Composes a {@code TemporalAdjusters} from a series of existing adjusters.
     *
     * @param adjusters
     *     The series of adjusters to combine.
     *
     * @return A {@code TemporalAdjuster}.
     */
    public static TemporalAdjuster chain(final Supplier<Stream<TemporalAdjuster>> adjusters)
    {
        return t0 ->
        {
            try (final Stream<TemporalAdjuster> stream = adjusters.get())
            {
                return stream.reduce(t0, (t1, a) -> a.adjustInto(t1), (t1, t2) -> t2);
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

    /**
     * Advances to the same time on the next instance of a numbered day of month.
     * <p>
     * If the day has already past for the current month or is the current day, the next month will be used.
     * <p>
     * If the month is too short for the given day to exist, the last day of the month will be used.
     *
     * @param dayOfMonth
     *     The day-of-month to advance to. Must be between 1 and 31 (inclusive).
     *
     * @return A {@link java.time.temporal.TemporalAdjuster}.
     */
    public static TemporalAdjuster nextDayOfMonth(final int dayOfMonth)
    {
        return t ->
        {
            YearMonth yearMonth = YearMonth.from(t);
            if (t.get(ChronoField.DAY_OF_MONTH) >= dayOfMonth)
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
     *     The day-of-week to advance to. May not be {@code null}.
     *
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
     * <p>
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

                final YearMonth yearMonth = YearMonth.from(temporal)
                    .plusMonths(1);

                // We adjust to the first day of the month first, to avoid creating a transitory date which is invalid.
                return temporal.with(ChronoField.DAY_OF_MONTH, 1)
                    .with(ChronoField.MONTH_OF_YEAR, yearMonth.getMonthValue())
                    .with(ChronoField.YEAR, yearMonth.getYear())
                    .with(ChronoField.DAY_OF_MONTH, Math.min(dayOfMonth.get(), yearMonth.lengthOfMonth()));
            }
        };
    }

    /**
     * Advances to the same time on the next instance of a numbered day of month.
     * <p>
     * If the day has already past for the current month, the next month will be used.
     * <p>
     * If the month is too short for the given day to exist, the last day of the month will be used.
     *
     * @param dayOfMonth
     *     The day-of-month to advance to. Must be between 1 and 31 (inclusive).
     *
     * @return A {@link java.time.temporal.TemporalAdjuster}.
     */
    public static TemporalAdjuster nextOrSameDayOfMonth(final int dayOfMonth)
    {
        return t ->
        {
            YearMonth yearMonth = YearMonth.from(t);
            if (t.get(ChronoField.DAY_OF_MONTH) > dayOfMonth)
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
     * <p>
     * If the current day is the given day-of-week, no change is made.
     *
     * @param dayOfWeek
     *     The day-of-week to advance to. May not be {@code null}.
     *
     * @return A {@link java.time.temporal.TemporalAdjuster}.
     */
    public static TemporalAdjuster nextOrSameDayOfWeek(final DayOfWeek dayOfWeek)
    {
        return TemporalAdjusters.nextOrSame(dayOfWeek);
    }

    /**
     * Advances to a particular time.
     *
     * @param time
     *     The time to advance to. May not be {@code null}.
     *
     * @return A {@link java.time.temporal.TemporalAdjuster}.
     */
    public static TemporalAdjuster nextOrSameTime(@SuppressWarnings("TypeMayBeWeakened") final OffsetTime time)
    {
        return previous ->
        {
            Temporal next = changeOffset(previous, time.getOffset());
            final boolean wrap = next.getLong(ChronoField.NANO_OF_DAY) > time.getLong(ChronoField.NANO_OF_DAY);
            next = next.with(ChronoField.NANO_OF_DAY, time.getLong(ChronoField.NANO_OF_DAY));
            if (wrap)
            {
                next = next.plus(1L, ChronoUnit.DAYS);
            }
            return next;
        };
    }

    /**
     * Shifts the time forwards to the next weekday.
     *
     * @return A {@code TemporalAdjuster}.
     */
    public static TemporalAdjuster nextOrSameWeekday()
    {
        return t ->
        {
            switch (DayOfWeek.from(t))
            {
                case SATURDAY:
                    return t.plus(2, ChronoUnit.DAYS);
                case SUNDAY:
                    return t.plus(1, ChronoUnit.DAYS);
                default:
                    return t;
            }
        };
    }

    /**
     * Advances to a particular time.
     *
     * @param time
     *     The time to advance to. May not be {@code null}.
     *
     * @return A {@link java.time.temporal.TemporalAdjuster}.
     */
    public static TemporalAdjuster nextTime(@SuppressWarnings("TypeMayBeWeakened") final OffsetTime time)
    {
        return previous ->
        {
            Temporal next = changeOffset(previous, time.getOffset());
            final boolean wrap = next.getLong(ChronoField.NANO_OF_DAY) >= time.getLong(ChronoField.NANO_OF_DAY);
            next = next.with(ChronoField.NANO_OF_DAY, time.getLong(ChronoField.NANO_OF_DAY));
            if (wrap)
            {
                next = next.plus(1L, ChronoUnit.DAYS);
            }
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
     * Shifts the time forwards to the next weekday.
     *
     * @return A {@code TemporalAdjuster}.
     */
    public static TemporalAdjuster nextWeekday()
    {
        return t ->
        {
            switch (DayOfWeek.from(t))
            {
                case FRIDAY:
                    return t.plus(3, ChronoUnit.DAYS);
                case SATURDAY:
                    return t.plus(2, ChronoUnit.DAYS);
                default:
                    return t.plus(1, ChronoUnit.DAYS);
            }
        };
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

    private static Temporal changeOffset(final Temporal t, final ZoneOffset offset)
    {
        return t.isSupported(ChronoField.OFFSET_SECONDS) ?
            t.minus(t.get(ChronoField.OFFSET_SECONDS), ChronoUnit.SECONDS)
                .plus(offset.getTotalSeconds(), ChronoUnit.SECONDS)
                .with(offset) :
            t;
    }
}
