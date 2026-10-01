package org.eclipsetrader.market.sim.agent;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Estimates the current drift (trend) from recent trade prices, capped in
 * magnitude and pulled toward an anchor by mean reversion.
 */
public class TrendEstimator {

    private final Deque<Double> prices = new ArrayDeque<Double>();
    private final int window;
    private final double cap;
    private final double meanReversion;
    private final double anchor;

    public TrendEstimator(int window, double cap, double meanReversion, double anchor) {
        this.window = window;
        this.cap = cap;
        this.meanReversion = meanReversion;
        this.anchor = anchor;
    }

    public void addPrice(double price) {
        prices.addLast(price);
        if (prices.size() > window) {
            prices.removeFirst();
        }
    }

    public double estimate() {
        if (prices.size() < 2) {
            return 0.0;
        }
        Double first = prices.peekFirst();
        Double last = prices.peekLast();
        double raw = (last.doubleValue() - first.doubleValue()) / first.doubleValue() / prices.size();
        double capped = Math.max(-cap, Math.min(cap, raw));
        double reversion = meanReversion * (anchor - last.doubleValue()) / anchor;
        return capped + reversion;
    }
}
