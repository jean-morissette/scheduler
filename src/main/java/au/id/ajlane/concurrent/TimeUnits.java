package au.id.ajlane.concurrent;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

/**
 * Utilities for working with values of {@link java.util.concurrent.TimeUnit}.
 */
public abstract class TimeUnits
{
    /**
     * Converts a {@link java.util.concurrent.TimeUnit} to the corresponding {@link java.time.temporal.TemporalUnit}.
     *
     * @param units
     *         The units to convert.
     * @return The corresponding {@link java.time.temporal.TemporalUnit}.
     */
    public static TemporalUnit toTemporalUnit(final TimeUnit units)
    {
        switch (units)
        {
            case DAYS:
                return ChronoUnit.DAYS;
            case HOURS:
                return ChronoUnit.HOURS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case MICROSECONDS:
                return ChronoUnit.MICROS;
            case NANOSECONDS:
                return ChronoUnit.NANOS;
            default:
                assert false;
                throw new UnsupportedOperationException(units.name());
        }
    }

    private TimeUnits() throws InstantiationException
    {
        throw new InstantiationException("This class cannot be instantiated.");
    }
}

