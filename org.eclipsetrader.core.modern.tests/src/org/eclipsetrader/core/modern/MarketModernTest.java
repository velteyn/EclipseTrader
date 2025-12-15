package org.eclipsetrader.core.modern;

import org.eclipsetrader.core.internal.markets.Market;
import org.eclipsetrader.core.internal.markets.MarketTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

public class MarketModernTest {

    @Test
    void testIsOpenBasic() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 11, 0)));
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 17, 0)));
    }

    @Test
    void testTimezoneNormalization() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 10, 0)));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 18, 0)));
    }

    private java.util.Date getTime(int year, int month, int day, int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, hour, minute, 0);
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
