package au.id.ajlane.cron;

import java.text.ParseException;

public class InvalidCronExpressionException extends ParseException {
    private static final long serialVersionUID = 1810237160081714196L;
    private final int length;

    InvalidCronExpressionException(final String message, final CronToken range) {
        super(message, range.offset());
        length = range.length();
    }

    /**
     * The length of the part of the expression which is in error.
     * <p>May be zero if the error has a position, but no particular length.</p>
     *
     * @return A non-negative integer.
     * @see #getErrorOffset()
     */
    public int getErrorLength() {
        return length;
    }
}
