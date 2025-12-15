package org.eclipsetrader.core.internal.trading;

import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.Trade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

@RunWith(JUnitPlatform.class)
public class TargetPriceModernTest {

    private static Map<String, Object> params(int field, double price, boolean cross) {
        Map<String, Object> m = new HashMap<>();
        m.put(TargetPrice.K_FIELD, field);
        m.put(TargetPrice.K_PRICE, price);
        m.put(TargetPrice.K_CROSS, cross);
        return m;
    }

    @Test
    void testLastReachesTriggers() {
        TargetPrice alert = new TargetPrice();
        alert.setParameters(params(TargetPrice.F_LAST, 100.0, false));
        alert.setInitialValues(new Trade(null, 95.0, 1L, null), null);
        alert.setTrade(new Trade(null, 100.0, 1L, null));
        Assertions.assertTrue(alert.isTriggered());
    }

    @Test
    void testBidCrossesTriggers() {
        TargetPrice alert = new TargetPrice();
        alert.setParameters(params(TargetPrice.F_BID, 100.0, true));
        alert.setInitialValues(null, new Quote(95.0, 96.0, 1L, 1L));
        alert.setQuote(new Quote(101.0, 102.0, 1L, 1L));
        Assertions.assertTrue(alert.isTriggered());
    }
}
