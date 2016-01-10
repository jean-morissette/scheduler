package au.id.ajlane.cron;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A list of seconds-level filters in a cron expression.
 */
final class CronSecondsList {

    private final SortedSet<CronSecondsListItem> items;

    private CronSecondsList(final SortedSet<CronSecondsListItem> items) {
        this.items = items;
    }

    /**
     * Parses a list of seconds-level filters from the given token in a cron expression.
     *
     * @param token The token to parse.
     * @return A valid list.
     * @throws InvalidCronExpressionException if the token is not a valid list.
     */
    public static CronSecondsList parse(final CronToken token) throws InvalidCronExpressionException {
        final CronSecondsList result = new CronSecondsList(new TreeSet<>());
        int start = 0;
        for (int cursor = 0; cursor < token.length(); cursor++) {
            switch (token.charAt(cursor)) {
                case ',':
                    if (start < cursor) {
                        result.items.add(CronSecondsListItem.parse(token.subSequence(start, cursor)));
                    }
                    start = cursor + 1;
                    break;
                default:
                    break;
            }
        }
        if (start < token.length()) {
            result.items.add(CronSecondsListItem.parse(token.subSequence(start)));
        }
        return result;
    }

    /**
     * Builds a list containing the given items.
     *
     * @param items The items in the list. The items must already be valid and normalised.
     * @return A valid list.
     */
    public static CronSecondsList of(final CronSecondsListItem... items) {
        return new CronSecondsList(new TreeSet<>(Arrays.asList(items)));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CronSecondsList other = (CronSecondsList) o;

        return items.equals(other.items);

    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    @Override
    public String toString() {
        return items.stream().map(CronSecondsListItem::toString).collect(Collectors.joining(","));
    }
}
