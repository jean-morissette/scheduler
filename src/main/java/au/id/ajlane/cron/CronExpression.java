package au.id.ajlane.cron;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.util.Optional;

/**
 * A concise description of a schedule for executing tasks.
 * <p>Cron expressions are made up of 7 whitespace-delimited components:
 * <em>seconds</em>, <em>minutes</em>, <em>hours</em>, <em>days-of-month</em>, <em>months</em>, <em>days-of-week</em>, and <em>years</em></p>
 * <p>Each component is a whitelist of values. A task is scheduled for whenever the clock-time matches at least one of the whitelisted values in each component.</p>
 * <table>
 *     <tr><th>Example</th><th>Description</th></tr>
 *     <tr><td><pre>* * * * * * *</pre></td><td>Any second, of any minute, of any hour, of any day, of any month, on any day of the week, of any year.</td></tr>
 *     <tr><td><pre>0 0 0 * * * *</pre></td><td>Midnight, every day.</td></tr>
 *     <tr><td><pre>0 30 2 * * TUE *</pre></td><td>2:30 am, every Tuesday.</td></tr>
 * </table>
 * <p>Use {@link #parse(CharSequence)} to parse a cron expression.</p>
 */
public final class CronExpression implements TemporalAdjuster {

    private CronSecondsList seconds;
    private CronMinutesList minutes;
    private CronHoursList hours;
    private CronDaysOfMonthList daysOfMonth;
    private CronMonthsList months;
    private CronDaysOfWeekList daysOfWeek;
    private CronYearsList years;

    private CronExpression() {
    }

    public static Optional<CronExpression> tryParse(final CharSequence expression) {
        try {
            return Optional.of(parse(expression));
        } catch (final InvalidCronExpressionException ignored) {
            return Optional.empty();
        }
    }

    public static CronExpression parse(final CharSequence expression) throws InvalidCronExpressionException {

        final CronExpression result = new CronExpression();

        int components = 0;
        int start = 0;
        for (int cursor = 0; cursor < expression.length(); cursor++) {
            switch (expression.charAt(cursor)) {
                case ' ':
                case '\t':
                    if (start < cursor) {
                        final CronToken component = CronToken.at(start, expression.subSequence(start, cursor));
                        parseNextComponent(result, components, component);
                        components++;
                    }
                    start = cursor + 1;
                    break;
                default:
                    if (components > 6) {
                        throw new InvalidCronExpressionException("All of the allowed components have already been specified.", CronToken.at(cursor, expression.subSequence(cursor, expression.length())));
                    }
                    break;
            }
        }

        if (start < expression.length()) {
            final CronToken component = CronToken.at(start, expression.subSequence(start, expression.length()));
            parseNextComponent(result, components, component);
        }

        if (components == 0) {
            throw new InvalidCronExpressionException("An expression should at least contain a seconds component.", CronToken.of(expression));
        }

        fillUnspecifiedComponents(result, components);

        return result;
    }

    private static void fillUnspecifiedComponents(final CronExpression result, final int components) {
        switch (components) {
            case 1:
                result.minutes = CronMinutesList.of(CronMinutesWildcard.get());
                // Fall through
            case 2:
                result.hours = CronHoursList.of(CronHoursWildcard.get());
                // Fall through
            case 3:
                result.daysOfMonth = CronDaysOfMonthList.of(CronDaysOfMonthWildcard.get());
                // Fall through
            case 4:
                result.months = CronMonthsList.of(CronMonthsWildcard.get());
                // Fall through
            case 5:
                result.daysOfWeek = CronDaysOfWeekList.of(CronDaysOfWeekWildcard.get());
                // Fall through
            case 6:
                result.years = CronYearsList.of(CronYearsWildcard.get());
                break;
            case 7:
                break;
            case 0:
            default:
                throw new IllegalArgumentException("Invalid number of components:" + components);
        }
    }

    private static void parseNextComponent(final CronExpression result, final int components, final CronToken component) throws InvalidCronExpressionException {
        switch (components) {
            case 0:
                result.seconds = CronSecondsList.parse(component);
                break;
            case 1:
                result.minutes = CronMinutesList.parse(component);
                break;
            case 2:
                result.hours = CronHoursList.parse(component);
                break;
            case 3:
                result.daysOfMonth = CronDaysOfMonthList.parse(component);
                break;
            case 4:
                result.months = CronMonthsList.parse(component);
                break;
            case 5:
                result.daysOfWeek = CronDaysOfWeekList.parse(component);
                break;
            case 6:
                result.years = CronYearsList.parse(component);
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CronExpression other = (CronExpression) o;

        return seconds.equals(other.seconds) &&
                minutes.equals(other.minutes) &&
                hours.equals(other.hours) &&
                daysOfMonth.equals(other.daysOfMonth) &&
                months.equals(other.months) &&
                daysOfWeek.equals(other.daysOfWeek) &&
                years.equals(other.years);

    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        int result = seconds.hashCode();
        result = 31 * result + minutes.hashCode();
        result = 31 * result + hours.hashCode();
        result = 31 * result + daysOfMonth.hashCode();
        result = 31 * result + months.hashCode();
        result = 31 * result + daysOfWeek.hashCode();
        result = 31 * result + years.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return seconds +
                " " + minutes +
                " " + hours +
                " " + daysOfMonth +
                " " + months +
                " " + daysOfWeek +
                " " + years;
    }

    @Override
    public Temporal adjustInto(final Temporal temporal) {
        // TODO: Figure out how to actually do this.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
