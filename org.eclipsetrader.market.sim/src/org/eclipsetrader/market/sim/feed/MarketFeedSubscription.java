package org.eclipsetrader.market.sim.feed;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ISubscriptionListener;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.QuoteDelta;
import org.eclipsetrader.core.feed.QuoteEvent;

/**
 * A feed subscription populated by the market simulator. Values are pushed in
 * via the public setters and delivered to listeners as quote updates.
 */
public class MarketFeedSubscription implements IFeedSubscription2 {

    private final MarketFeedConnector connector;
    private final IFeedIdentifier identifier;

    private ITrade trade;
    private IQuote quote;
    private ITodayOHL todayOHL;
    private IBook book;

    private final ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
    private final List<QuoteDelta> deltaList = new ArrayList<QuoteDelta>();

    public MarketFeedSubscription(MarketFeedConnector connector, IFeedIdentifier identifier) {
        this.connector = connector;
        this.identifier = identifier;
    }

    @Override
    public IFeedIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public String getSymbol() {
        return identifier.getSymbol();
    }

    @Override
    public void dispose() {
        connector.disposeSubscription(this);
    }

    @Override
    public ITrade getTrade() {
        return trade;
    }

    @Override
    public IQuote getQuote() {
        return quote;
    }

    @Override
    public ITodayOHL getTodayOHL() {
        return todayOHL;
    }

    @Override
    public ILastClose getLastClose() {
        return null;
    }

    @Override
    public IBook getBook() {
        return book;
    }

    @Override
    public void addSubscriptionListener(ISubscriptionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeSubscriptionListener(ISubscriptionListener listener) {
        listeners.remove(listener);
    }

    public void setTrade(ITrade trade) {
        if (this.trade == null || !trade.equals(this.trade)) {
            addDelta(new QuoteDelta(identifier, this.trade, trade));
            this.trade = trade;
        }
    }

    public void setQuote(IQuote quote) {
        if (this.quote == null || !quote.equals(this.quote)) {
            addDelta(new QuoteDelta(identifier, this.quote, quote));
            this.quote = quote;
        }
    }

    public void setTodayOHL(ITodayOHL todayOHL) {
        if (this.todayOHL == null || !todayOHL.equals(this.todayOHL)) {
            addDelta(new QuoteDelta(identifier, this.todayOHL, todayOHL));
            this.todayOHL = todayOHL;
        }
    }

    public void setBook(IBook book) {
        addDelta(new QuoteDelta(identifier, this.book, book));
        this.book = book;
    }

    private void addDelta(QuoteDelta delta) {
        synchronized (deltaList) {
            deltaList.add(delta);
        }
    }

    public boolean hasPendingChanges() {
        synchronized (deltaList) {
            return !deltaList.isEmpty();
        }
    }

    public void fireNotification() {
        QuoteDelta[] deltas;
        synchronized (deltaList) {
            if (deltaList.isEmpty()) {
                return;
            }
            deltas = deltaList.toArray(new QuoteDelta[deltaList.size()]);
            deltaList.clear();
        }
        QuoteEvent event = new QuoteEvent(connector, identifier, deltas);
        Object[] l = listeners.getListeners();
        for (Object o : l) {
            ((ISubscriptionListener) o).quoteUpdate(event);
        }
    }
}
