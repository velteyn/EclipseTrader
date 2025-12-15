package org.eclipsetrader.core.ats.engines;

import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Date;

@RunWith(JUnitPlatform.class)
public class MarketOrderFunctionModernTest {

    private static class TestBroker extends org.eclipsetrader.core.ats.simulation.Broker {
        public TestBroker(PricingEnvironment env) { super(env); }
        public void trigger(Security sec, Date time, double price) {
            processTrade(sec, new Trade(time, price, 1L, null));
        }
    }

    @Test
    void testSendAndFillMarketOrder() throws Exception {
        PricingEnvironment env = new PricingEnvironment();
        TestBroker broker = new TestBroker(env);
        org.eclipsetrader.core.ats.simulation.Account account = new org.eclipsetrader.core.ats.simulation.Account();
        Security security = new Security("SEC", null);

        MarketOrderFunction fn = new MarketOrderFunction(broker, account, security);
        fn.jsSet_type(BaseOrderFunction.Market);
        fn.jsSet_side(BaseOrderFunction.Buy);
        fn.jsSet_quantity(100);
        fn.jsSet_text("test");
        fn.jsFunction_send();

        org.eclipsetrader.core.trading.IOrderMonitor[] orders = broker.getOrders();
        Assertions.assertEquals(1, orders.length);
        org.eclipsetrader.core.ats.simulation.OrderMonitor monitor = (org.eclipsetrader.core.ats.simulation.OrderMonitor) orders[0];
        Assertions.assertEquals(IOrderStatus.PendingNew, monitor.getStatus());

        broker.trigger(security, new Date(), 10.0);
        Assertions.assertEquals(IOrderStatus.Filled, monitor.getStatus());
        Assertions.assertEquals(100L, monitor.getFilledQuantity());
        Assertions.assertEquals(10.0, monitor.getAveragePrice());
    }
}
