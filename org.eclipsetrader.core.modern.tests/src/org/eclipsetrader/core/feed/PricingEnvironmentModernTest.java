package org.eclipsetrader.core.feed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.eclipsetrader.core.instruments.Security;

import java.util.concurrent.atomic.AtomicInteger;

@RunWith(JUnitPlatform.class)
public class PricingEnvironmentModernTest {

    @Test
    void testListenerReceivesQuoteAndTradeUpdates() {
        PricingEnvironment env = new PricingEnvironment();
        Security s = new Security("S", new FeedIdentifier("SYM", null));
        AtomicInteger events = new AtomicInteger();
        IPricingListener listener = new IPricingListener() {
            @Override
            public void pricingUpdate(PricingEvent event) { events.incrementAndGet(); }
        };
        env.addPricingListener(listener);
        env.setQuote(s, new Quote(1.0, 1.1, 100L, 200L));
        env.setTrade(s, new Trade(new java.util.Date(), 1.05, 100L, 1000L));
        Assertions.assertEquals(2, events.get());
    }
}
