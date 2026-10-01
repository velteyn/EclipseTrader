package org.eclipsetrader.market.sim.agent;

import org.eclipsetrader.market.sim.engine.Exchange;
import org.eclipsetrader.market.sim.engine.OrderType;
import org.eclipsetrader.market.sim.engine.Side;

/**
 * The market maker quotes both sides around the fundamental value, providing
 * continuous liquidity so trades happen without any player action.
 */
public class MarketMaker {

    private final String participant;
    private final double spread;   // total spread as a fraction of price
    private final long size;

    public MarketMaker(String participant, double spread, long size) {
        this.participant = participant;
        this.spread = spread;
        this.size = size;
    }

    public void act(Exchange exchange, String asset, double fundamental) {
        double bid = fundamental * (1.0 - spread / 2.0);
        double ask = fundamental * (1.0 + spread / 2.0);
        exchange.enterOrder(asset, Side.BUY, OrderType.LIMIT, size, bid, participant);
        exchange.enterOrder(asset, Side.SELL, OrderType.LIMIT, size, ask, participant);
    }
}
