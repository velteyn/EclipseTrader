package org.eclipsetrader.market.sim.engine;

import java.util.List;

/**
 * Callback interface notified of market activity, decoupled from any UI or
 * feed layer. Implementations (such as the feed bridge) translate these into
 * platform-specific events.
 */
public interface MarketListener {

    void onTrade(Trade trade);

    void onQuote(String asset, double bid, double ask, long bidSize, long askSize);

    void onBook(String asset, List<Order> bids, List<Order> asks);
}
