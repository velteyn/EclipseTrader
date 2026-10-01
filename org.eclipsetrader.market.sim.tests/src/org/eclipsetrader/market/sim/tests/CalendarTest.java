package org.eclipsetrader.market.sim.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;

import org.eclipsetrader.market.sim.clock.SimulatedClock;
import org.eclipsetrader.market.sim.clock.TradingCalendar;
import org.eclipsetrader.market.sim.engine.Exchange;
import org.eclipsetrader.market.sim.engine.OrderType;
import org.eclipsetrader.market.sim.engine.Side;
import org.eclipsetrader.market.sim.engine.Trade;
import org.junit.jupiter.api.Test;

public class CalendarTest {

    private static final long AFTER_YEAR_2000 = 946684800000L;

    private long startOfDay(int day) {
        Calendar c = Calendar.getInstance();
        c.set(2026, 0, day, 9, 30, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    @Test
    void simulatedClockIsNotEpoch() {
        SimulatedClock clock = new SimulatedClock(startOfDay(5));
        assertTrue(clock.now() > AFTER_YEAR_2000);
    }

    @Test
    void advanceToNextDayOpenMovesDay() {
        SimulatedClock clock = new SimulatedClock(startOfDay(5));
        int before = clock.dayOfYear();
        clock.advanceToNextDayOpen(9, 30);
        assertTrue(clock.dayOfYear() != before);
    }

    @Test
    void dayBoundaryClosesAndReopens() {
        SimulatedClock clock = new SimulatedClock(startOfDay(5));
        TradingCalendar calendar = new TradingCalendar(clock, 9, 30, 16, 0);
        calendar.open();
        assertTrue(calendar.isOpen());

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(clock.now());
        c.set(Calendar.HOUR_OF_DAY, 16);
        c.set(Calendar.MINUTE, 0);
        clock.setNow(c.getTimeInMillis());
        assertTrue(calendar.isEndOfDay());

        calendar.close();
        assertFalse(calendar.isOpen());

        calendar.advanceDay();
        assertTrue(calendar.isOpen());
        assertEquals(2, calendar.getDay());
    }

    @Test
    void tradeTimestampIsSimulatedDate() {
        Exchange ex = new Exchange();
        long start = startOfDay(5);
        ex.setNow(start);
        ex.enterOrder("X", Side.SELL, OrderType.LIMIT, 100, 10, "S");
        ex.enterOrder("X", Side.BUY, OrderType.LIMIT, 100, 10, "B");
        Trade trade = ex.getBook("X").getTrades().get(0);
        assertEquals(start, trade.getTimestamp());
        assertTrue(trade.getTimestamp() > AFTER_YEAR_2000);
    }
}
