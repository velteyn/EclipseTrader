package org.eclipsetrader.market.sim.feed;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.feed.Book;
import org.eclipsetrader.core.feed.BookEntry;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector2;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.core.feed.IBookEntry;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.market.sim.engine.MarketListener;
import org.eclipsetrader.market.sim.engine.Order;

/**
 * Bridges the market simulator to the EclipseTrader feed seam. It is both an
 * {@link IFeedConnector2} (so views can subscribe) and a {@link MarketListener}
 * (so it receives engine activity and pushes it into the subscriptions on the
 * display thread).
 */
public class MarketFeedConnector implements IFeedConnector2, MarketListener {

    private String id = "org.eclipsetrader.market.sim.feed";
    private String name = "Market Simulator";

    private final Map<String, MarketFeedSubscription> subscriptions = new HashMap<String, MarketFeedSubscription>();
    private final ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public IFeedSubscription subscribe(IFeedIdentifier identifier) {
        return getOrCreate(identifier);
    }

    @Override
    public IFeedSubscription2 subscribeLevel2(IFeedIdentifier identifier) {
        return getOrCreate(identifier);
    }

    @Override
    public IFeedSubscription2 subscribeLevel2(String symbol) {
        return getOrCreate(symbol);
    }

    private MarketFeedSubscription getOrCreate(IFeedIdentifier identifier) {
        synchronized (subscriptions) {
            MarketFeedSubscription sub = subscriptions.get(identifier.getSymbol());
            if (sub == null) {
                sub = new MarketFeedSubscription(this, identifier);
                subscriptions.put(identifier.getSymbol(), sub);
            }
            return sub;
        }
    }

    private MarketFeedSubscription getOrCreate(String symbol) {
        synchronized (subscriptions) {
            MarketFeedSubscription sub = subscriptions.get(symbol);
            if (sub == null) {
                sub = new MarketFeedSubscription(this, new FeedIdentifier(symbol, new FeedProperties()));
                subscriptions.put(symbol, sub);
            }
            return sub;
        }
    }

    void disposeSubscription(MarketFeedSubscription subscription) {
        synchronized (subscriptions) {
            subscriptions.remove(subscription.getSymbol());
        }
    }

    @Override
    public void connect() {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void addConnectorListener(IConnectorListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeConnectorListener(IConnectorListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onTrade(final org.eclipsetrader.market.sim.engine.Trade trade) {
        final MarketFeedSubscription sub = subscriptions.get(trade.getAsset());
        if (sub == null) {
            return;
        }
        final Trade t = new Trade(new Date(trade.getTimestamp()), trade.getPrice(), trade.getQuantity(), trade.getQuantity());
        runOnDisplay(new Runnable() {
            @Override
            public void run() {
                sub.setTrade(t);
                sub.fireNotification();
            }
        });
    }

    @Override
    public void onQuote(final String asset, final double bid, final double ask, final long bidSize, final long askSize) {
        final MarketFeedSubscription sub = subscriptions.get(asset);
        if (sub == null) {
            return;
        }
        runOnDisplay(new Runnable() {
            @Override
            public void run() {
                sub.setQuote(new Quote(bid, ask, bidSize, askSize));
                sub.fireNotification();
            }
        });
    }

    @Override
    public void onBook(final String asset, final List<Order> bids, final List<Order> asks) {
        final MarketFeedSubscription sub = subscriptions.get(asset);
        if (sub == null) {
            return;
        }
        final Book book = buildBook(bids, asks);
        runOnDisplay(new Runnable() {
            @Override
            public void run() {
                sub.setBook(book);
                sub.fireNotification();
            }
        });
    }

    private Book buildBook(List<Order> bids, List<Order> asks) {
        IBookEntry[] bidEntries = new IBookEntry[bids.size()];
        for (int i = 0; i < bids.size(); i++) {
            Order o = bids.get(i);
            bidEntries[i] = new BookEntry(null, o.getPrice(), o.getAvailable(), 1L, null);
        }
        IBookEntry[] askEntries = new IBookEntry[asks.size()];
        for (int i = 0; i < asks.size(); i++) {
            Order o = asks.get(i);
            askEntries[i] = new BookEntry(null, o.getPrice(), o.getAvailable(), 1L, null);
        }
        return new Book(bidEntries, askEntries);
    }

    private void runOnDisplay(Runnable runnable) {
        try {
            Display.getDefault().asyncExec(runnable);
        } catch (Throwable t) {
            runnable.run();
        }
    }
}
