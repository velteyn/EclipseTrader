package org.eclipsetrader.jessx.internal.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipsetrader.jessx.server.Server;
import org.junit.jupiter.api.Test;

class BrokerConnectorTest {

    @Test
    void testServerStateChangedToOnlineCountsDownLatch() throws InterruptedException {
        // Given
        BrokerConnector connector = new BrokerConnector();

        // When
        connector.serverStateChanged(Server.SERVER_STATE_ONLINE);

        // Then
        // The latch is static, so we can check it here.
        // It should have counted down to 0.
        assertEquals(0, BrokerConnector.serverReadyLatch.getCount());
    }
}
