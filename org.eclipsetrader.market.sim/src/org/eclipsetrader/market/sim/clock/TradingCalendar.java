package org.eclipsetrader.market.sim.clock;

/**
 * A daily trading calendar over a {@link SimulatedClock}. Each period is one
 * trading day with an open and a close; the calendar tracks the current open
 * state and advances the day.
 */
public class TradingCalendar {

    private final SimulatedClock clock;
    private final int openHour;
    private final int openMinute;
    private final int closeHour;
    private final int closeMinute;

    private boolean open;
    private int day;

    public TradingCalendar(SimulatedClock clock, int openHour, int openMinute, int closeHour, int closeMinute) {
        this.clock = clock;
        this.openHour = openHour;
        this.openMinute = openMinute;
        this.closeHour = closeHour;
        this.closeMinute = closeMinute;
        this.day = 1;
    }

    public boolean isOpen() {
        return open;
    }

    public int getDay() {
        return day;
    }

    public void open() {
        this.open = true;
    }

    public void close() {
        this.open = false;
    }

    public boolean isEndOfDay() {
        return !clock.isWithinWindow(openHour, openMinute, closeHour, closeMinute);
    }

    /**
     * Advances to the next trading day and opens the market.
     */
    public void advanceDay() {
        clock.advanceToNextDayOpen(openHour, openMinute);
        day++;
        open = true;
    }
}
