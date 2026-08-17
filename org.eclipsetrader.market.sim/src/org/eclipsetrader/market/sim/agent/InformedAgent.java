package org.eclipsetrader.market.sim.agent;

import java.util.Random;

import org.eclipsetrader.market.sim.engine.Exchange;
import org.eclipsetrader.market.sim.engine.OrderType;
import org.eclipsetrader.market.sim.engine.Side;

/**
 * An informed trader that tilts its orders toward the current trend and any
 * recent news, hitting the quoted price to generate activity.
 */
public class InformedAgent {

    private final String participant;
    private final long size;
    private double newsBias;

    public InformedAgent(String participant, long size) {
        this.participant = participant;
        this.size = size;
    }

    public void reactToNews(NewsEvent event) {
        newsBias += event.getSentiment() * event.getMagnitude();
    }

    public void act(Exchange exchange, String asset, double fundamental, double trend, Random random) {
        double noise = (random.nextDouble() - 0.5) * 0.002;
        double bias = trend + newsBias + noise;
        if (bias > 0) {
            double ask = exchange.getBook(asset).getBestAsk();
            double price = ask != 0.0 ? ask : fundamental * 1.001;
            exchange.enterOrder(asset, Side.BUY, OrderType.LIMIT, size, price, participant);
        } else {
            double bid = exchange.getBook(asset).getBestBid();
            double price = bid != 0.0 ? bid : fundamental * 0.999;
            exchange.enterOrder(asset, Side.SELL, OrderType.LIMIT, size, price, participant);
        }
        newsBias *= 0.9;
    }
}
