package au.id.ajlane.cron;

final class CronSecondsWildcard implements CronSecondsListItem {
    private static final CronSecondsWildcard INSTANCE = new CronSecondsWildcard();

    public static CronSecondsWildcard get() {
        return INSTANCE;
    }

    public static CronSecondsWildcard parse(final CronToken token) throws InvalidCronExpressionException {
        if (token.length() == 0 || token.charAt(0) != '*') {
            throw new InvalidCronExpressionException("'*' character missing.", token);
        }
        if (token.length() > 1) {
            throw new InvalidCronExpressionException("Unexpected characters.", token.subSequence(1));
        }
        return get();
    }

    private CronSecondsWildcard() {
    }

    @Override
    public String toString() {
        return "*";
    }
}
