package au.id.ajlane.cron;

/**
 * A single seconds-level filter in a list of filters in a cron expression.
 */
interface CronSecondsListItem {
    /**
     * Parses the given token in a cron expression as a list of seconds-level filters.
     *
     * @param cronToken The token to parse.
     * @return A valid list.
     * @throws InvalidCronExpressionException If the token is invalid.
     */
    static CronSecondsListItem parse(final CronToken cronToken) throws InvalidCronExpressionException {
        for (int cursor = 0; cursor < cronToken.length(); cursor++) {
            switch (cronToken.charAt(cursor)) {
                case '*':
                    return CronSecondsWildcard.parse(cronToken);
                case '/':
                    return CronSecondsInterval.parse(cronToken);
                case '-':
                    return CronSecondsRange.parse(cronToken);
                default:
                    break;
            }
        }
        return CronSecondsValue.parse(cronToken);
    }
}
