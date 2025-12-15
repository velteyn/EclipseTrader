package org.eclipsetrader.core.markets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ISubscriptionListener;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;

import java.util.HashSet;
import java.util.Set;

@RunWith(JUnitPlatform.class)
public class MarketPricingEnvironmentModernTest {

    static class TestFeedConnector implements IFeedConnector {
        final String id;
        final String name;
        final Set<IFeedSubscription> subscriptions = new HashSet<>();
        TestFeedConnector(String id, String name) { this.id = id; this.name = name; }
        @Override public void connect() { }
        @Override public void disconnect() { }
        @Override public String getId() { return id; }
        @Override public String getName() { return name; }
        @Override public IFeedSubscription subscribe(final IFeedIdentifier identifier) {
            IFeedSubscription s = new IFeedSubscription() {
                @Override public void dispose() { subscriptions.remove(this); }
                @Override public void addSubscriptionListener(ISubscriptionListener listener) { }
                @Override public IFeedIdentifier getIdentifier() { return identifier; }
                @Override public String getSymbol() { return identifier.getSymbol(); }
                @Override public ILastClose getLastClose() { return null; }
                @Override public IQuote getQuote() { return null; }
                @Override public ITodayOHL getTodayOHL() { return null; }
                @Override public ITrade getTrade() { return null; }
                @Override public void removeSubscriptionListener(ISubscriptionListener listener) { }
            };
            subscriptions.add(s);
            return s;
        }
        @Override public void addConnectorListener(IConnectorListener listener) { }
        @Override public void removeConnectorListener(IConnectorListener listener) { }
    }

    static class TestMarket implements IMarket {
        final String name;
        IFeedConnector live;
        final Set<ISecurity> members = new java.util.HashSet<>();
        TestMarket(String name) { this.name = name; }
        @Override public void addMembers(ISecurity[] securities) { java.util.Collections.addAll(members, securities); }
        @Override public IFeedConnector getLiveFeedConnector() { return live; }
        public void setLiveFeedConnector(IFeedConnector liveFeedConnector) { this.live = liveFeedConnector; }
        @Override public org.eclipsetrader.core.feed.IBackfillConnector getBackfillConnector() { return null; }
        @Override public org.eclipsetrader.core.feed.IBackfillConnector getIntradayBackfillConnector() { return null; }
        @Override public ISecurity[] getMembers() { return members.toArray(new ISecurity[0]); }
        @Override public String getName() { return name; }
        @Override public IMarketDay getNextDay() { return null; }
        @Override public IMarketDay getToday() { return null; }
        @Override public boolean hasMember(ISecurity security) { return members.contains(security); }
        @Override public boolean isOpen() { return true; }
        @Override public boolean isOpen(java.util.Date time) { return true; }
        @Override public void removeMembers(ISecurity[] securities) { members.removeAll(java.util.Arrays.asList(securities)); }
        @Override public Object getAdapter(Class adapter) { return null; }
    }

    @Test
    void testAddRemoveChangeSubscribeUnsubscribe() {
        ISecurity security1 = new Security("Security1", new FeedIdentifier("id1", null));
        ISecurity security2 = new Security("Security2", new FeedIdentifier("id2", null));

        TestFeedConnector connector1 = new TestFeedConnector("id1", "Connector1");
        TestFeedConnector connector2 = new TestFeedConnector("id2", "Connector2");

        TestMarket market1 = new TestMarket("Market1");
        market1.setLiveFeedConnector(connector1);
        market1.addMembers(new ISecurity[] { security1 });

        TestMarket market2 = new TestMarket("Market2");
        market2.setLiveFeedConnector(connector2);
        market2.addMembers(new ISecurity[] { security2 });

        MarketPricingEnvironment environment = new MarketPricingEnvironment() {
            @Override
            protected IMarket getMarketsForSecurity(ISecurity security) {
                if (security == security1) return market1;
                if (security == security2) return market2;
                return null;
            }
        };

        environment.addSecurities(new ISecurity[] { security1 });
        Assertions.assertEquals(1, connector1.subscriptions.size());
        Assertions.assertEquals(0, connector2.subscriptions.size());


        environment.removeSecurities(new ISecurity[] { security1 });
        Assertions.assertEquals(0, connector1.subscriptions.size());
        Assertions.assertEquals(0, connector2.subscriptions.size());
    }
}
