package org.eclipsetrader.core.ats.simulation;

import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.Order;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class AccountSimulationModernTest {

    @Test
    void testProcessCompletedOrderOpensAndClosesPosition() {
        PricingEnvironment env = new PricingEnvironment();
        Broker broker = new Broker(env);
        Account account = new Account();
        Security security = new Security("SEC", null);

        Order buyOrder = new Order(account, IOrderType.Market, IOrderSide.Buy, security, 100L, null);
        OrderMonitor monitor = new OrderMonitor(broker, buyOrder);
        monitor.setFilledQuantity(100L);
        monitor.setAveragePrice(10.0);
        account.processCompletedOrder(monitor);
        Assertions.assertEquals(1, account.getPositions().length);
        Assertions.assertEquals(100L, account.getPositions()[0].getQuantity());

        Order sellOrder = new Order(account, IOrderType.Market, IOrderSide.Sell, security, 100L, null);
        OrderMonitor monitor2 = new OrderMonitor(broker, sellOrder);
        monitor2.setFilledQuantity(100L);
        monitor2.setAveragePrice(10.0);
        account.processCompletedOrder(monitor2);
        Assertions.assertEquals(0, account.getPositions().length);
    }
}
