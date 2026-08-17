package org.eclipsetrader.market.sim.clock;

import java.util.Calendar;
import java.util.Date;

/**
 * A simulated clock whose "now" is an absolute timestamp on a simulated daily
 * calendar (not wall-clock and not the 1970 epoch). All engine timestamps come
 * from this clock.
 */
public class SimulatedClock {

    private long now;

    public SimulatedClock(long startEpochMillis) {
        this.now = startEpochMillis;
    }

    public long now() {
        return now;
    }

    public void setNow(long now) {
        this.now = now;
    }

    public Date time() {
        return new Date(now);
    }

    public void advance(long millis) {
        now += millis;
    }

    /**
     * Advances the clock to the market open of the next trading day.
     */
    public void advanceToNextDayOpen(int openHour, int openMinute) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        c.add(Calendar.DAY_OF_YEAR, 1);
        c.set(Calendar.HOUR_OF_DAY, openHour);
        c.set(Calendar.MINUTE, openMinute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        now = c.getTimeInMillis();
    }

    /**
     * True if the current time is within the given intraday window (inclusive).
     */
    public boolean isWithinWindow(int openHour, int openMinute, int closeHour, int closeMinute) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        int minutes = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        return minutes >= openHour * 60 + openMinute && minutes < closeHour * 60 + closeMinute;
    }

    public int dayOfYear() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        return c.get(Calendar.DAY_OF_YEAR);
    }
}
