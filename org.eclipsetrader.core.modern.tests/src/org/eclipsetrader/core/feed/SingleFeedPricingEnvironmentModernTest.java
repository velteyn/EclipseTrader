package org.eclipsetrader.core.feed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.eclipsetrader.core.instruments.Security;

import java.util.HashSet;
import java.util.Set;

@RunWith(JUnitPlatform.class)
public class SingleFeedPricingEnvironmentModernTest {

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
                boolean disposed = false;
                @Override public void dispose() { disposed = true; subscriptions.remove(this); }
                @Override public void addSubscriptionListener(ISubscriptionListener listener) { }
                @Override public IFeedIdentifier getIdentifier() { return identifier; }
                @Override public String getSymbol() { return identifier.getSymbol(); }
                @Override public ILastClose getLastClose() { return null; }
                @Override public IQuote getQuote() { return null; }
                @Override public ITodayOHL getTodayOHL() { return null; }
                @Override public ITrade getTrade() { return null; }
                @Override public void removeSubscriptionListener(ISubscriptionListener listener) { }
                @Override public String toString() { return "sub:" + getSymbol(); }
            };
            subscriptions.add(s);
            return s;
        }
        @Override public void addConnectorListener(IConnectorListener listener) { }
        @Override public void removeConnectorListener(IConnectorListener listener) { }
    }

    @Test
    void testAddRemoveSecurityCreatesAndDisposesSubscription() {
        TestFeedConnector connector = new TestFeedConnector("id", "Test");
        SingleFeedPricingEnvironment env = new SingleFeedPricingEnvironment(connector);

        Security security = new Security("S1", new FeedIdentifier("SYM", null));
        env.addSecurity(security);
        Assertions.assertEquals(1, connector.subscriptions.size());

        env.removeSecurity(security);
        Assertions.assertEquals(0, connector.subscriptions.size());
    }

    @Test
    void testRemoveAllSecuritiesDisposesSubscriptions() {
        TestFeedConnector connector = new TestFeedConnector("id", "Test");
        SingleFeedPricingEnvironment env = new SingleFeedPricingEnvironment(connector);

        env.addSecurities(new Security[] {
            new Security("S1", new FeedIdentifier("SYM1", null)),
            new Security("S2", new FeedIdentifier("SYM2", null)),
        });
        Assertions.assertEquals(2, connector.subscriptions.size());
        env.removeSecurities(new Security[] {
            new Security("S1", new FeedIdentifier("SYM1", null)),
            new Security("S2", new FeedIdentifier("SYM2", null)),
        });
        Assertions.assertEquals(0, connector.subscriptions.size());
    }
}
