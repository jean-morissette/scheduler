package au.id.ajlane.cron;

final class CronSecondsValue implements CronSecondsListItem, Comparable<CronSecondsValue> {
    public static CronSecondsValue parse(final CronToken token) throws InvalidCronExpressionException {
        final int value;
        try {
            value = Integer.parseInt(token.toString());
        } catch (final NumberFormatException ignored) {
            throw new InvalidCronExpressionException("\"" + token + "\" is not an integer.", token);
        }

        if (value < 0 || value > 59) {
            throw new InvalidCronExpressionException("A seconds value must be between 0 and 59 (inclusive).", token);
        }

        return new CronSecondsValue(value);
    }

    private final int value;

    private CronSecondsValue(final int value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CronSecondsValue other = (CronSecondsValue) o;
        return value == other.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    public int toInt() {
        return value;
    }

    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public int compareTo(final CronSecondsValue other) {
        return Integer.compare(value, other.value);
    }
}
