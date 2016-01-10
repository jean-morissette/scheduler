package au.id.ajlane.cron;

import org.junit.Assert;
import org.junit.Test;

public class CronExpressionTest {
    @Test
    public void testNormalisation() throws InvalidCronExpressionException {
        Assert.assertEquals("* * * * * * *", CronExpression.parse("*").toString());
        Assert.assertEquals("0/15 * * * * * *", CronExpression.parse("*/15").toString());
        Assert.assertEquals("50/1 59 24 24,31 12 * *", CronExpression.parse("50/1 59 24 31,24,24 12, 5,*").toString());
    }
}
