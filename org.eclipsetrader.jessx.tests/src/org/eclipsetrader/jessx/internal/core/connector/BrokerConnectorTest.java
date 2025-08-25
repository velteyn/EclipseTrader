package org.eclipsetrader.jessx.internal.core.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IOrderValidity;
import org.eclipsetrader.core.trading.Order;
import org.eclipsetrader.jessx.internal.core.BrokerConnector;
import org.eclipsetrader.jessx.internal.core.OrderMonitor;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

public class BrokerConnectorTest {

    private BrokerConnector brokerConnector;
    private MockStreamingConnector streamingConnector;

    @Before
    public void setUp() throws Exception {
        brokerConnector = new BrokerConnector();
        streamingConnector = new MockStreamingConnector();
        // Replace the singleton instance with our mock
        // This is a common pattern in testing code that uses singletons.
        // In a real application, a dependency injection framework would be better.
        java.lang.reflect.Field instanceField = StreamingConnector.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, streamingConnector);
    }

    @Test
    public void testPrepareAndSubmitOrder() throws Exception {
        ISecurity security = new Security("TEST", new FeedIdentifier("TEST", null));
        IOrder order = new Order(security, IOrderType.Limit, IOrderSide.Buy, 100L, 10.0, null);

        IOrderMonitor monitor = brokerConnector.prepareOrder(order);
        assertNotNull(monitor);

        monitor.submit();

        assertNotNull(streamingConnector.sentElement);
        assertEquals("Operation", streamingConnector.sentElement.getName());
        assertEquals("Limit Order", streamingConnector.sentElement.getAttributeValue("type"));
        assertEquals("TEST", streamingConnector.sentElement.getAttributeValue("institution"));

        Element orderElement = streamingConnector.sentElement.getChild("Order");
        assertNotNull(orderElement);

        Element limitOrderElement = orderElement.getChild("LimitOrder");
        assertNotNull(limitOrderElement);
        assertEquals("10.0", limitOrderElement.getAttributeValue("price"));
        assertEquals("100", limitOrderElement.getAttributeValue("quantity"));
    }

    private class MockStreamingConnector extends StreamingConnector {
        public Element sentElement;

        @Override
        public synchronized void send(Element element) throws IOException {
            this.sentElement = element;
        }
    }
}
