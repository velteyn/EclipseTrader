package org.eclipsetrader.market.sim.risk;

import java.util.HashMap;
import java.util.Map;

/**
 * A participant account: signed cash (negative = borrowed) and signed positions
 * (negative = short). Provides leverage buying power, margin, short-limit, and
 * equity computations. Pure Java, no platform dependencies.
 */
public class Account {

    private final String participant;
    private double cash;
    private double leverage = 1.0;
    private long shortLimit = Long.MAX_VALUE;
    private double maintenanceMargin = 0.0;
    private double interestRate = 0.0;

    private final Map<String, Long> positions = new HashMap<String, Long>();

    public Account(String participant, double cash) {
        this.participant = participant;
        this.cash = cash;
    }

    public String getParticipant() {
        return participant;
    }

    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    public double getLeverage() {
        return leverage;
    }

    public void setLeverage(double leverage) {
        this.leverage = leverage;
    }

    public long getShortLimit() {
        return shortLimit;
    }

    public void setShortLimit(long shortLimit) {
        this.shortLimit = shortLimit;
    }

    public double getMaintenanceMargin() {
        return maintenanceMargin;
    }

    public void setMaintenanceMargin(double maintenanceMargin) {
        this.maintenanceMargin = maintenanceMargin;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public long getPosition(String asset) {
        Long pos = positions.get(asset);
        return pos == null ? 0L : pos.longValue();
    }

    public Map<String, Long> getPositions() {
        return new HashMap<String, Long>(positions);
    }

    /**
     * Applies a signed position delta and the corresponding cash movement.
     * A positive delta (buy) reduces cash; a negative delta (sell) increases cash.
     */
    public void applyTrade(String asset, long signedDelta, double price) {
        long newPos = getPosition(asset) + signedDelta;
        positions.put(asset, newPos);
        cash -= signedDelta * price;
    }

    /**
     * True if the account has enough buying power (cash times leverage) to
     * afford the given purchase cost.
     */
    public boolean canBuy(double cost) {
        return cost <= cash * leverage;
    }

    /**
     * True if selling the given quantity does not breach the short limit.
     */
    public boolean canSell(String asset, long qty) {
        long after = getPosition(asset) - qty;
        return after >= -shortLimit;
    }

    /**
     * Cash plus mark-to-market value of long positions minus the value of short
     * positions.
     */
    public double equity(Map<String, Double> prices) {
        double e = cash;
        for (Map.Entry<String, Long> en : positions.entrySet()) {
            Double px = prices.get(en.getKey());
            if (px != null) {
                e += en.getValue().longValue() * px.doubleValue();
            }
        }
        return e;
    }

    /**
     * Sum of the absolute mark-to-market value of all positions.
     */
    public double grossExposure(Map<String, Double> prices) {
        double g = 0.0;
        for (Map.Entry<String, Long> en : positions.entrySet()) {
            Double px = prices.get(en.getKey());
            if (px != null) {
                g += Math.abs(en.getValue().longValue()) * px.doubleValue();
            }
        }
        return g;
    }

    public boolean isFlat() {
        for (long v : positions.values()) {
            if (v != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Accrues one period of interest on any borrowed cash (negative balance).
     */
    public void accrueInterest() {
        if (cash < 0) {
            cash += cash * interestRate;
        }
    }
}
