package au.id.ajlane.cron;

/**
 * A range of seconds to use as a seconds-level filter.
 * <p>An alternative to individually listing each value.</p>
 */
final class CronSecondsRange implements CronSecondsListItem, Comparable<CronSecondsRange> {

    private CronSecondsValue start;
    private CronSecondsValue end;

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CronSecondsRange other = (CronSecondsRange) o;

        return start.equals(other.start) && end.equals(other.end);
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        return result;
    }

    public static CronSecondsRange parse(final CronToken token) throws InvalidCronExpressionException {

        final CronSecondsRange result = new CronSecondsRange();

        int dash = -1;
        for (int cursor = 0; cursor < token.length(); cursor++) {
            switch (token.charAt(cursor)) {
                case '-':
                    if (dash >= 0) {
                        throw new InvalidCronExpressionException("Only one '-' is necessary to specify a range.", token.subSequenceOfLength(cursor, 1));
                    }
                    result.start = CronSecondsValue.parse(token.subSequenceOfLength(cursor));
                    dash = cursor;
                    break;
                default:
                    break;
            }
        }

        if (dash < 0) {
            throw new InvalidCronExpressionException("Missing '-'.", token.subSequenceOfLength(token.length(), 0));
        }

        result.end = CronSecondsValue.parse(token.subSequence(dash + 1));

        if (result.start.equals(result.end)) {
            throw new InvalidCronExpressionException("The start and end of the range should be different.", token);
        }

        return result;
    }

    public String toString() {
        return start + "-" + end;
    }

    @Override
    public int compareTo(final CronSecondsRange other) {
        final int startComparison = start.compareTo(other.start);
        if (startComparison != 0) {
            return startComparison;
        }
        return end.compareTo(other.end);
    }

    private CronSecondsRange() {
    }
}
