package org.eclipsetrader.core.internal.markets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;

@RunWith(JUnitPlatform.class)
public class MarketDayModernTest {

    @Test
    void testGetOpenTime() {
        MarketDay day = new MarketDay(getTime(9, 30), null, null);
        Assertions.assertEquals(getTime(9, 30), day.getOpenTime());
    }

    @Test
    void testGetCloseTime() {
        MarketDay day = new MarketDay(null, getTime(16, 0), null);
        Assertions.assertEquals(getTime(16, 0), day.getCloseTime());
    }

    @Test
    void testGetMessage() {
        MarketDay day = new MarketDay(null, null, "Message");
        Assertions.assertEquals("Message", day.getMessage());
    }

    @Test
    void testIsOpenAtOpenTime() {
        MarketDay day = new MarketDay(getTime(2007, Calendar.NOVEMBER, 11, 9, 30), getTime(2007, Calendar.NOVEMBER, 11, 16, 0), null);
        Assertions.assertTrue(day.isOpen(getTime(2007, Calendar.NOVEMBER, 11, 9, 30)));
    }

    @Test
    void testIsClosedAtCloseTime() {
        MarketDay day = new MarketDay(getTime(2007, Calendar.NOVEMBER, 11, 9, 30), getTime(2007, Calendar.NOVEMBER, 11, 16, 0), null);
        Assertions.assertFalse(day.isOpen(getTime(2007, Calendar.NOVEMBER, 11, 16, 0)));
    }

    @Test
    void testIsClosedEarlyLate() {
        MarketDay day = new MarketDay(getTime(2007, Calendar.NOVEMBER, 11, 9, 30), getTime(2007, Calendar.NOVEMBER, 11, 16, 0), null);
        Assertions.assertFalse(day.isOpen(getTime(2007, Calendar.NOVEMBER, 11, 6, 0)));
        Assertions.assertFalse(day.isOpen(getTime(2007, Calendar.NOVEMBER, 11, 17, 0)));
    }

    @Test
    void testIsOpenWithNullTime() {
        MarketDay day = new MarketDay(null, null, null);
        Assertions.assertFalse(day.isOpen(getTime(12, 30)));
    }

    private Date getTime(int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private Date getTime(int year, int month, int day, int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, hour, minute, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
