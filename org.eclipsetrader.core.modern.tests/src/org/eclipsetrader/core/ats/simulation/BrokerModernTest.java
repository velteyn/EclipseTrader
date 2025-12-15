package org.eclipsetrader.core.ats.simulation;

import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderStatus;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.Order;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Date;

@RunWith(JUnitPlatform.class)
public class BrokerModernTest {

    private static class TestBroker extends Broker {
        public TestBroker(PricingEnvironment env) { super(env); }
        public void trigger(Security sec, Date time, double price) {
            processTrade(sec, new Trade(time, price, 1L, null));
        }
    }

    @Test
    void testMarketOrderFilledByTrade() throws Exception {
        PricingEnvironment env = new PricingEnvironment();
        TestBroker broker = new TestBroker(env);
        Account account = new Account();
        Security security = new Security("SEC", null);

        Order order = new Order(account, IOrderType.Market, IOrderSide.Buy, security, 100L, null);
        OrderMonitor monitor = (OrderMonitor) broker.prepareOrder(order);
        monitor.submit();

        broker.trigger(security, new Date(), 10.0);
        Assertions.assertEquals(IOrderStatus.Filled, monitor.getStatus());
        Assertions.assertEquals(100L, monitor.getFilledQuantity());
        Assertions.assertEquals(10.0, monitor.getAveragePrice());
        Assertions.assertEquals(1, account.getPositions().length);
    }

    @Test
    void testLimitBuyFilledWhenTradeAtOrBelowPrice() throws Exception {
        PricingEnvironment env = new PricingEnvironment();
        TestBroker broker = new TestBroker(env);
        Account account = new Account();
        Security security = new Security("SEC", null);

        Order order = new Order(account, IOrderType.Limit, IOrderSide.Buy, security, 50L, 10.0);
        OrderMonitor monitor = (OrderMonitor) broker.prepareOrder(order);
        monitor.submit();

        broker.trigger(security, new Date(), 9.5);
        Assertions.assertEquals(IOrderStatus.Filled, monitor.getStatus());
        Assertions.assertEquals(50L, monitor.getFilledQuantity());
        Assertions.assertEquals(9.5, monitor.getAveragePrice());
    }

    @Test
    void testLimitSellFilledWhenTradeAtOrAbovePrice() throws Exception {
        PricingEnvironment env = new PricingEnvironment();
        TestBroker broker = new TestBroker(env);
        Account account = new Account();
        Security security = new Security("SEC", null);

        Order order = new Order(account, IOrderType.Limit, IOrderSide.Sell, security, 75L, 10.0);
        OrderMonitor monitor = (OrderMonitor) broker.prepareOrder(order);
        monitor.submit();

        broker.trigger(security, new Date(), 10.5);
        Assertions.assertEquals(IOrderStatus.Filled, monitor.getStatus());
        Assertions.assertEquals(75L, monitor.getFilledQuantity());
        Assertions.assertEquals(10.5, monitor.getAveragePrice());
    }
}
