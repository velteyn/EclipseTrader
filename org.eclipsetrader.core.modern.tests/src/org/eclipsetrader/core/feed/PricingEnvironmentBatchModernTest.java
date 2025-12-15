package org.eclipsetrader.core.feed;

import org.eclipsetrader.core.instruments.Security;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

@RunWith(JUnitPlatform.class)
public class PricingEnvironmentBatchModernTest {

    @Test
    void testBatchNotifiesOnceForMultipleDeltas() {
        PricingEnvironment env = new PricingEnvironment();
        AtomicInteger notifications = new AtomicInteger();
        env.addPricingListener(event -> notifications.incrementAndGet());
        Security sec = new Security("SEC", null);

        env.runBatch(() -> {
            env.setQuote(sec, new Quote(10.0, 11.0, 1L, 1L));
            env.setTrade(sec, new Trade(new java.util.Date(), 10.5, 1L, null));
            env.setBar(sec, new Bar(new java.util.Date(), TimeSpan.minutes(1), 10.0, 11.0, 9.5, 10.8, 100L));
        });

        Assertions.assertEquals(1, notifications.get());
    }
}
