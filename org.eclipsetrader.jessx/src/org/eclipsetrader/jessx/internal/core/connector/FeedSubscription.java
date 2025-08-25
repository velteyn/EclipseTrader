/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.jessx.internal.core.connector;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IPrice;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ISubscriptionListener;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.LastClose;
import org.eclipsetrader.core.feed.QuoteDelta;
import org.eclipsetrader.core.feed.QuoteEvent;
import org.eclipsetrader.core.feed.TodayOHL;
import org.eclipsetrader.jessx.internal.JessxActivator;

public class FeedSubscription implements IFeedSubscription2 {

    private StreamingConnector connector;
    private IFeedIdentifier identifier;
    private IPrice price;
    private ITrade trade;
    private IQuote quote;
    private ITodayOHL todayOHL;
    private ILastClose lastClose;
    private IBook book;
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
    private List<QuoteDelta> deltaList = new ArrayList<QuoteDelta>();
    private int instanceCount = 0;
    private int level2InstanceCount = 0;

    private double open;
    private double high;
    private double low;

    public FeedSubscription(StreamingConnector connector, IFeedIdentifier identifier) {
        this.connector = connector;
        this.identifier = identifier;
        resetOHL();
    }

    public void resetOHL() {
        this.open = 0.0;
        this.high = 0.0;
        this.low = Double.MAX_VALUE;
    }

    @Override
    public void dispose() {
        // This needs to be adapted in StreamingConnector
        // connector.disposeSubscription(this);
    }

    protected int incrementInstanceCount() {
        instanceCount++;
        return instanceCount;
    }

    protected int decrementInstanceCount() {
        instanceCount--;
        return instanceCount;
    }

    protected int getInstanceCount() {
        return instanceCount;
    }

    protected int incrementLevel2InstanceCount() {
        level2InstanceCount++;
        return level2InstanceCount;
    }

    protected int decrementLevel2InstanceCount() {
        level2InstanceCount--;
        return level2InstanceCount;
    }

    public int getLevel2InstanceCount() {
        return level2InstanceCount;
    }

    @Override
    public void addSubscriptionListener(ISubscriptionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeSubscriptionListener(ISubscriptionListener listener) {
        listeners.remove(listener);
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
    public IQuote getQuote() {
        return quote;
    }

    public void setQuote(IQuote quote) {
        if (this.quote == null || !this.quote.equals(quote)) {
            addDelta(new QuoteDelta(identifier, this.quote, quote));
            this.quote = quote;
        }
    }

    @Override
    public ITodayOHL getTodayOHL() {
        return todayOHL;
    }

    public void setTodayOHL(ITodayOHL todayOHL) {
        if (this.todayOHL == null || !this.todayOHL.equals(todayOHL)) {
            addDelta(new QuoteDelta(identifier, this.todayOHL, todayOHL));
            this.todayOHL = todayOHL;
        }
    }

    public void updateOHL(double price) {
        if (open == 0.0) {
            open = price;
        }
        if (price > high) {
            high = price;
        }
        if (price < low) {
            low = price;
        }
        setTodayOHL(new TodayOHL(open, high, low));
    }

    @Override
    public ITrade getTrade() {
        return trade;
    }

    public void setTrade(ITrade trade) {
        if (this.trade == null || !this.trade.equals(trade)) {
            addDelta(new QuoteDelta(identifier, this.trade, trade));
            this.trade = trade;
            updateOHL(trade.getPrice());
        }
    }

    public IPrice getPrice() {
        return price;
    }

    public void setPrice(IPrice price) {
        this.price = price;
        addDelta(new QuoteDelta(identifier, null, price));
    }

    @Override
    public ILastClose getLastClose() {
        return lastClose;
    }

    public void setLastClose(double price) {
        setLastClose(new LastClose(price, null));
    }

    public void setLastClose(ILastClose lastClose) {
        if (this.lastClose == null || !this.lastClose.equals(lastClose)) {
            addDelta(new QuoteDelta(identifier, this.lastClose, lastClose));
            this.lastClose = lastClose;
        }
    }

    @Override
    public IBook getBook() {
        return book;
    }

    public void setBook(IBook book) {
        addDelta(new QuoteDelta(identifier, this.book, book));
        this.book = book;
    }

    public void addDelta(QuoteDelta delta) {
        synchronized (deltaList) {
            deltaList.add(delta);
        }
    }

    public boolean hasListeners() {
        return listeners.size() != 0;
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
        QuoteEvent event = new QuoteEvent(connector, getIdentifier(), deltas);
        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((ISubscriptionListener) l[i]).quoteUpdate(event);
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error notifying a quote update", e);
                JessxActivator.log(status);
            } catch (LinkageError e) {
                Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error notifying a quote update", e);
                JessxActivator.log(status);
            }
        }
    }

    public boolean hasPendingChanges() {
        synchronized (deltaList) {
            return deltaList.size() != 0;
        }
    }
}
