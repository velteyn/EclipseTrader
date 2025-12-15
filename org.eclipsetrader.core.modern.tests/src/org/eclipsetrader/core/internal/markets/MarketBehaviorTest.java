package org.eclipsetrader.core.internal.markets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

public class MarketBehaviorTest {

    @Test
    void testIsOpenWindows() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 0), getTime(9, 30)),
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 9, 15)));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 11, 0)));
    }

    @Test
    void testIsOpenAtOpenTime() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 9, 30)));
    }

    @Test
    void testIsClosedAtCloseTime() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 16, 0)));
    }

    @Test
    void testIsClosedEarlyLate() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 6, 0)));
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 17, 0)));
    }

    @Test
    void testClosedOnSunday() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setWeekDays(new Integer[] {
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
        });
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 4, 1, 0)));
    }

    @Test
    void testHolidayFullAndPartial() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setHolidays(new MarketHoliday[] {
                new MarketHoliday(getTime(2007, Calendar.DECEMBER, 25), "Holiday")
        });
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 24, 10, 0)));
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.DECEMBER, 25, 10, 0)));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 26, 10, 0)));

        market.setHolidays(new MarketHoliday[] {
                new MarketHoliday(getTime(2007, Calendar.DECEMBER, 31, 9, 30), getTime(2007, Calendar.DECEMBER, 31, 12, 30), "Holiday")
        });
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 28, 10, 0)));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 28, 15, 0)));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 31, 10, 0)));
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.DECEMBER, 31, 15, 0)));
    }

    @Test
    void testTimezoneNormalization() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        Assertions.assertEquals(getTime(9, 30), market.getSchedule()[0].getOpenTime());
        Assertions.assertEquals(getTime(16, 0), market.getSchedule()[0].getCloseTime());
        market.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Assertions.assertEquals(getTime(9, 30), market.getSchedule()[0].getOpenTime());
        Assertions.assertEquals(getTime(16, 0), market.getSchedule()[0].getCloseTime());
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 10, 0)));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 18, 0)));
    }

    @Test
    void testMarketDayOpenClose() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        MarketDay day = market.getMarketDayFor(getTime(2007, Calendar.NOVEMBER, 6));
        Assertions.assertEquals(getTime(2007, Calendar.NOVEMBER, 6, 9, 30), day.getOpenTime());
        Assertions.assertEquals(getTime(2007, Calendar.NOVEMBER, 6, 16, 0), day.getCloseTime());
        Assertions.assertNull(day.getMessage());
    }

    @Test
    void testMarketDayWeekendClosed() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setWeekDays(new Integer[] {
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
        });
        MarketDay day = market.getMarketDayFor(getTime(2007, Calendar.NOVEMBER, 3));
        Assertions.assertFalse(day.isOpen());
        Assertions.assertNull(day.getOpenTime());
        Assertions.assertNull(day.getCloseTime());
        Assertions.assertNull(day.getMessage());
    }

    @Test
    void testNextMarketDayOpen() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        MarketDay day = market.getNextMarketDayFor(getTime(2007, Calendar.NOVEMBER, 6, 10, 0));
        Assertions.assertEquals(getTime(2007, Calendar.NOVEMBER, 7, 9, 30), day.getOpenTime());
        Assertions.assertEquals(getTime(2007, Calendar.NOVEMBER, 7, 16, 0), day.getCloseTime());
        Assertions.assertNull(day.getMessage());
    }

    @Test
    void testNextMarketDayWeekend() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setWeekDays(new Integer[] {
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
        });
        MarketDay day = market.getNextMarketDayFor(getTime(2007, Calendar.NOVEMBER, 3));
        Assertions.assertEquals(getTime(2007, Calendar.NOVEMBER, 5, 9, 30), day.getOpenTime());
        Assertions.assertEquals(getTime(2007, Calendar.NOVEMBER, 5, 16, 0), day.getCloseTime());
        Assertions.assertNull(day.getMessage());
    }

    @Test
    void testNextMarketDayTimezoneShift() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        MarketDay day = market.getNextMarketDayFor(getTime(2007, Calendar.NOVEMBER, 6, 10, 0));
        Assertions.assertEquals(getTime(2007, Calendar.NOVEMBER, 6, 15, 30), day.getOpenTime());
        Assertions.assertEquals(getTime(2007, Calendar.NOVEMBER, 6, 22, 0), day.getCloseTime());
        Assertions.assertNull(day.getMessage());
    }

    private java.util.Date getTime(int year, int month, int day, int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, hour, minute, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private java.util.Date getTime(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private java.util.Date getTime(int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
