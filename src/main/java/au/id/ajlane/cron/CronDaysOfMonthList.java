package au.id.ajlane.cron;

import java.util.List;

final class CronDaysOfMonthList {

    static CronDaysOfMonthList parse(final CronToken token){
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    private final List<? extends CronDaysOfMonthListItem> items;

    private CronDaysOfMonthList(final List<? extends CronDaysOfMonthListItem> items) {
        this.items = items;
    }
}
