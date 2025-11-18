package org.eclipsetrader.jessx.internal.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import org.eclipsetrader.jessx.internal.core.connector.StreamingConnector;
import org.eclipsetrader.jessx.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
class BrokerConnectorTest {

    @Mock
    private StreamingConnector streamingConnectorMock;

    private BrokerConnector brokerConnector;

    @BeforeEach
    void setUp() throws Exception {
        brokerConnector = BrokerConnector.getInstance();

        // Since StreamingConnector is a singleton, we need to inject the mock
        Field instanceField = StreamingConnector.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, streamingConnectorMock);
    }

    @Test
    void testServerStateChangedToOnlineStartsStreamingConnector() {
        // When
        brokerConnector.serverStateChanged(Server.SERVER_STATE_ONLINE);

        // Then
        verify(streamingConnectorMock).start();
    }

    @Test
    void testServerStateChangedToOfflineDoesNotStartStreamingConnector() {
        // When
        brokerConnector.serverStateChanged(Server.SERVER_STATE_OFFLINE);

        // Then
        verify(streamingConnectorMock, never()).start();
    }

    @Test
    void testDisconnectStopsStreamingConnector() {
        // When
        brokerConnector.disconnect();

        // Then
        verify(streamingConnectorMock).stop();
    }
}
