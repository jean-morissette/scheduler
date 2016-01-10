package au.id.ajlane.cron;

final class CronSecondsInterval implements CronSecondsListItem, Comparable<CronSecondsInterval> {
    private CronSecondsValue start;
    private int increment;

    private CronSecondsInterval() {
    }

    public static CronSecondsInterval parse(final CronToken token) throws InvalidCronExpressionException {

        final CronSecondsInterval result = new CronSecondsInterval();

        int divider = -1;
        for (int cursor = 0; cursor < token.length(); cursor++) {
            switch (token.charAt(cursor)) {
                case '/':
                    if (divider >= 0) {
                        throw new InvalidCronExpressionException("Only one '/' is necessary to specify an interval.", token.subSequenceOfLength(cursor, 1));
                    }
                    result.start = CronSecondsValue.parse(token.subSequenceOfLength(cursor));
                    divider = cursor;
                    break;
                default:
                    break;
            }
        }

        if (divider < 0) {
            throw new InvalidCronExpressionException("Missing '/'.", token.subSequenceOfLength(token.length(), 0));
        }

        final CronToken incrementToken = token.subSequence(divider + 1);
        try {
            result.increment = Integer.parseInt(incrementToken.toString());
        } catch (final NumberFormatException ignored) {
            throw new InvalidCronExpressionException("The increment \"" + incrementToken + "\" is not an integer.", incrementToken);
        }

        return result;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CronSecondsInterval other = (CronSecondsInterval) o;

        return increment == other.increment && start.equals(other.start);
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + increment;
        return result;
    }

    public String toString() {
        return start + "/" + increment;
    }

    @SuppressWarnings("CompareToUsesNonFinalVariable")
    @Override
    public int compareTo(final CronSecondsInterval other) {
        final int startComparison = start.compareTo(other.start);
        if (startComparison != 0) {
            return startComparison;
        }
        return Integer.compare(increment, other.increment);
    }
}
