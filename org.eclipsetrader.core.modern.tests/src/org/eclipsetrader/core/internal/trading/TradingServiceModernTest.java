package org.eclipsetrader.core.internal.trading;

import org.eclipsetrader.core.ats.simulation.Broker;
import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.OrderChangeEvent;
import org.eclipsetrader.core.trading.OrderDelta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class TradingServiceModernTest {

    private static class ExposedTradingService extends TradingService {
        void exposeProcess(OrderChangeEvent e) { processOrderChangedEvent(e); }
        IOrderMonitor[] exposeOrders() { return getOrders(); }
    }

    @Test
    void testProcessOrderChangedEventAddsAndRemoves() {
        ExposedTradingService svc = new ExposedTradingService();
        Broker broker = new Broker(new PricingEnvironment());
        Security sec = new Security("SEC", null);
        org.eclipsetrader.core.trading.Order order = new org.eclipsetrader.core.trading.Order(null, org.eclipsetrader.core.trading.IOrderType.Market, org.eclipsetrader.core.trading.IOrderSide.Buy, sec, 10L, null);
        org.eclipsetrader.core.ats.simulation.OrderMonitor monitor = new org.eclipsetrader.core.ats.simulation.OrderMonitor(broker, order);

        svc.exposeProcess(new OrderChangeEvent(broker, new OrderDelta[]{new OrderDelta(OrderDelta.KIND_ADDED, monitor)}));
        Assertions.assertEquals(1, svc.exposeOrders().length);

        svc.exposeProcess(new OrderChangeEvent(broker, new OrderDelta[]{new OrderDelta(OrderDelta.KIND_REMOVED, monitor)}));
        Assertions.assertEquals(0, svc.exposeOrders().length);

        svc.exposeProcess(new OrderChangeEvent(broker, new OrderDelta[]{new OrderDelta(OrderDelta.KIND_UPDATED, monitor)}));
        Assertions.assertEquals(1, svc.exposeOrders().length);
    }
}
