package org.eclipsetrader.core.feed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.eclipsetrader.core.feed.TimeSpan.Units;

@RunWith(JUnitPlatform.class)
public class TimeSpanModernTest {

    @Test
    void testMinutesToString() {
        Assertions.assertEquals("5min", TimeSpan.minutes(5).toString());
    }

    @Test
    void testMinutesFromString() {
        TimeSpan aggr = TimeSpan.fromString("5min");
        Assertions.assertEquals(Units.Minutes, aggr.getUnits());
        Assertions.assertEquals(5, aggr.getLength());
    }

    @Test
    void testDaysToString() {
        Assertions.assertEquals("5d", TimeSpan.days(5).toString());
    }

    @Test
    void testDaysFromString() {
        TimeSpan aggr = TimeSpan.fromString("5d");
        Assertions.assertEquals(Units.Days, aggr.getUnits());
        Assertions.assertEquals(5, aggr.getLength());
    }

    @Test
    void testLowerThan() {
        TimeSpan ref = TimeSpan.minutes(30);
        Assertions.assertTrue(ref.lowerThan(TimeSpan.minutes(60)));
        Assertions.assertFalse(ref.lowerThan(TimeSpan.minutes(10)));
        Assertions.assertTrue(ref.lowerThan(TimeSpan.days(30)));
    }

    @Test
    void testHigherThan() {
        TimeSpan ref = TimeSpan.minutes(30);
        Assertions.assertFalse(ref.higherThan(TimeSpan.minutes(60)));
        Assertions.assertTrue(ref.higherThan(TimeSpan.minutes(10)));
        Assertions.assertFalse(ref.higherThan(TimeSpan.days(30)));
    }
}
