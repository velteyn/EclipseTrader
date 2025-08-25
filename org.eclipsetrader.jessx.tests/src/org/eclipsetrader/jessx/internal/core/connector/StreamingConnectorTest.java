package org.eclipsetrader.jessx.internal.core.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StreamingConnectorTest {

    private StreamingConnector connector;
    private MockServer mockServer;

    @Before
    public void setUp() throws Exception {
        connector = new StreamingConnector();
        mockServer = new MockServer();
        mockServer.start();
        // The connector reads the port from the preferences, so we need to set it here.
        // For now, let's assume the test can modify the connector directly.
        // In a real scenario, we would use a mock preference store.
        connector.serverPort = mockServer.getPort();
    }

    @After
    public void tearDown() throws Exception {
        connector.disconnect();
        mockServer.stopServer();
    }

    @Test
    public void testGetInstance() {
        StreamingConnector connector = StreamingConnector.getInstance();
        assertNotNull(connector);
    }

    @Test
    public void testConnectAndLogin() throws Exception {
        connector.connect();
        Thread.sleep(1000); // Wait for connection and login
        assertEquals("login", mockServer.getReceivedMessage());
    }

    @Test
    public void testReceiveOrderBook() throws Exception {
        IFeedIdentifier identifier = new FeedIdentifier("TEST", null);
        IFeedSubscription subscription = connector.subscribe(identifier);

        connector.connect();
        Thread.sleep(1000);

        String orderBookXml = "<OrderBook institution=\"TEST\"><Bid><Operation type=\"Limit Order\"><Order id=\"1\" side=\"1\" timestamp=\"12345\"><LimitOrder price=\"10.0\" quantity=\"100\"/></Order></Operation></Bid><Ask><Operation type=\"Limit Order\"><Order id=\"2\" side=\"0\" timestamp=\"12346\"><LimitOrder price=\"10.1\" quantity=\"200\"/></Order></Operation></Ask></OrderBook>";
        mockServer.sendMessage(orderBookXml);

        Thread.sleep(1000);

        assertNotNull(subscription.getBook());
        assertEquals(1, subscription.getBook().getBidProposals().length);
        assertEquals(10.0, subscription.getBook().getBidProposals()[0].getPrice(), 0.0);
        assertEquals(100, subscription.getBook().getBidProposals()[0].getSize().longValue());
        assertEquals(1, subscription.getBook().getAskProposals().length);
        assertEquals(10.1, subscription.getBook().getAskProposals()[0].getPrice(), 0.0);
        assertEquals(200, subscription.getBook().getAskProposals()[0].getSize().longValue());
    }

    @Test
    public void testReceiveDeal() throws Exception {
        IFeedIdentifier identifier = new FeedIdentifier("TEST", null);
        IFeedSubscription subscription = connector.subscribe(identifier);

        connector.connect();
        Thread.sleep(1000);

        String dealXml = "<Deal institution=\"TEST\" price=\"10.05\" quantity=\"50\" timestamp=\"12347\"/>";
        mockServer.sendMessage(dealXml);

        Thread.sleep(1000);

        assertNotNull(subscription.getTrade());
        assertEquals(10.05, subscription.getTrade().getPrice(), 0.0);
        assertEquals(50, subscription.getTrade().getSize().longValue());
    }

    private class MockServer extends Thread {
        private ServerSocket serverSocket;
        private Socket clientSocket;
        private DataOutputStream out;
        private DataInputStream in;
        private volatile String receivedMessage;
        private volatile boolean running = true;

        public MockServer() throws IOException {
            serverSocket = new ServerSocket(0); // 0 means any free port
        }

        public int getPort() {
            return serverSocket.getLocalPort();
        }

        @Override
        public void run() {
            try {
                clientSocket = serverSocket.accept();
                out = new DataOutputStream(clientSocket.getOutputStream());
                in = new DataInputStream(clientSocket.getInputStream());

                // Handle login
                String loginMsg = in.readUTF();
                if (loginMsg.contains("<login")) {
                    receivedMessage = "login";
                    out.writeUTF("<message value=\"Connection accepted.\"/>[JessX-end]");
                    out.flush();
                }

                while (running) {
                    try {
                        // Keep listening for other messages if needed, but do nothing for now
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

            } catch (IOException e) {
                if(running) e.printStackTrace();
            } finally {
                stopServer();
            }
        }

        public void sendMessage(String message) throws IOException {
            if (out != null) {
                out.writeUTF(message + "[JessX-end]");
                out.flush();
            }
        }

        public String getReceivedMessage() {
            return receivedMessage;
        }

        public void stopServer() {
            running = false;
            try {
                if (serverSocket != null) serverSocket.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
