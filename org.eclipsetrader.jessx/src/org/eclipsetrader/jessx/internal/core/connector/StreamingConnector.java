/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *     Edoardo BAROLO - virtual investor
 */

package org.eclipsetrader.jessx.internal.core.connector;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipsetrader.core.feed.BookEntry;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IBookEntry;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector2;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.jessx.internal.JessxActivator;
import org.eclipsetrader.jessx.net.ExpUpdate;
import org.eclipsetrader.jessx.preferences.PreferenceConstants;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class StreamingConnector implements Runnable, IFeedConnector2, IExecutableExtension, PropertyChangeListener {

    private static StreamingConnector instance;

    private String id;
    private String name;

    private Map<String, FeedSubscription> symbolSubscriptions;
    private Map<String, Long> symbolVolumes;
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private Thread thread;
    private Thread notificationThread;
    private boolean stopping = false;

    private String serverHost = "localhost";
    private int serverPort = 6290;
    private Socket socket;
    private DataOutputStream os;
    private DataInputStream is;
    private String username;

    private Log logger = LogFactory.getLog(getClass());

    private Runnable notificationRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (notificationThread) {
                while (!isStopping()) {
                    FeedSubscription[] subscriptions;
                    synchronized (symbolSubscriptions) {
                        Collection<FeedSubscription> c = symbolSubscriptions.values();
                        subscriptions = c.toArray(new FeedSubscription[c.size()]);
                    }
                    for (int i = 0; i < subscriptions.length; i++) {
                        subscriptions[i].fireNotification();
                    }

                    try {
                        notificationThread.wait();
                    } catch (InterruptedException e) {
                        // Ignore exception, not important at this time
                    }
                }
            }
        }
    };

    public StreamingConnector() {
        symbolSubscriptions = new HashMap<String, FeedSubscription>();
        symbolVolumes = new HashMap<String, Long>();
    }

    public synchronized static StreamingConnector getInstance() {
        if (instance == null) {
            instance = new StreamingConnector();
        }
        return instance;
    }

    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        name = config.getAttribute("name");
        instance = this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IFeedSubscription subscribe(IFeedIdentifier identifier) {
        synchronized (symbolSubscriptions) {
            String symbol = identifier.getSymbol();
            FeedSubscription subscription = symbolSubscriptions.get(symbol);
            if (subscription == null) {
                subscription = new FeedSubscription(this, identifier);
                symbolSubscriptions.put(symbol, subscription);
                symbolVolumes.put(symbol, 0L);
            }
            subscription.incrementInstanceCount();
            return subscription;
        }
    }

    protected void disposeSubscription(FeedSubscription subscription) {
        synchronized (symbolSubscriptions) {
            if (subscription.decrementInstanceCount() <= 0) {
                String symbol = subscription.getSymbol();
                symbolSubscriptions.remove(symbol);
                symbolVolumes.remove(symbol);
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public synchronized void send(Element element) throws IOException {
        if (os != null) {
            Document doc = new Document(element);
            XMLOutputter outputter = new XMLOutputter(Format.getRawFormat());
            StringWriter writer = new StringWriter();
            outputter.output(doc, writer);
            os.writeUTF(writer.toString() + "[JessX-end]");
            os.flush();
        }
    }

    @Override
    public IFeedSubscription2 subscribeLevel2(IFeedIdentifier identifier) {
        FeedSubscription subscription;
        synchronized (symbolSubscriptions) {
            String symbol = identifier.getSymbol();
            subscription = symbolSubscriptions.get(symbol);
            if (subscription == null) {
                subscription = new FeedSubscription(this, identifier);
                symbolSubscriptions.put(symbol, subscription);
                symbolVolumes.put(symbol, 0L);
            }
            subscription.incrementInstanceCount();
            subscription.incrementLevel2InstanceCount();
        }
        return subscription;
    }

    @Override
    public IFeedSubscription2 subscribeLevel2(String symbol) {
        IFeedIdentifier identifier = new FeedIdentifier(symbol, null);
        return subscribeLevel2(identifier);
    }

    protected void disposeSubscription2(FeedSubscription subscription) {
        synchronized (symbolSubscriptions) {
            if (subscription.decrementLevel2InstanceCount() <= 0) {
                // Level 2 is disposed, but we might still have level 1
            }
            if (subscription.decrementInstanceCount() <= 0) {
                String symbol = subscription.getSymbol();
                symbolSubscriptions.remove(symbol);
                symbolVolumes.remove(symbol);
            }
        }
    }

    @Override
    public synchronized void connect() {
        stopping = false;

        if (notificationThread == null || !notificationThread.isAlive()) {
            notificationThread = new Thread(notificationRunnable, name + " - Notification");
            notificationThread.start();
        }

        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this, name + " - Data Reader");
            thread.start();
        }
    }

    @Override
    public synchronized void disconnect() {
        stopping = true;

        if (thread != null) {
            try {
                if (socket != null) {
                    socket.close();
                }
                thread.join(30 * 1000);
            } catch (InterruptedException e) {
                Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error stopping thread", e);
                JessxActivator.log(status);
            } catch (IOException e) {
                Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error closing socket", e);
                JessxActivator.log(status);
            }
            thread = null;
        }

        if (notificationThread != null) {
            try {
                synchronized (notificationThread) {
                    notificationThread.notify();
                }
                notificationThread.join(30 * 1000);
            } catch (InterruptedException e) {
				Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error stopping notification thread", e);
                JessxActivator.log(status);
            }
            notificationThread = null;
        }
    }

    public boolean isStopping() {
        return stopping;
    }

    @Override
    public void run() {
        while (!isStopping()) {
            try {
                socket = new Socket(serverHost, serverPort);
                os = new DataOutputStream(socket.getOutputStream());
                is = new DataInputStream(socket.getInputStream());

                // Login
                IPreferenceStore store = JessxActivator.getDefault().getPreferenceStore();
            username = store.getString(PreferenceConstants.P_USERNAME);
                String password = store.getString(PreferenceConstants.P_PASSWORD);

                Element root = new Element("login");
                root.setAttribute("login", username);
                root.setAttribute("password", password);
                root.setAttribute("javaversion", System.getProperty("java.version"));
                Document doc = new Document(root);

                XMLOutputter outputter = new XMLOutputter(Format.getRawFormat());
                StringWriter writer = new StringWriter();
                outputter.output(doc, writer);

                os.writeUTF(writer.toString() + "[JessX-end]");
                os.flush();

                // Read response
                String response = is.readUTF();
                response = response.substring(0, response.indexOf("[JessX-end]"));

                SAXBuilder builder = new SAXBuilder();
                Document responseDoc = builder.build(new StringReader(response));
                Element responseRoot = responseDoc.getRootElement();

                if ("message".equals(responseRoot.getName()) && "Connection accepted.".equals(responseRoot.getAttributeValue("value"))) {
                    logger.info("JessX connection accepted");
                } else {
                    String reason = responseRoot.getAttributeValue("value");
                    logger.error("JessX connection failed: " + reason);
                    disconnect();
                    return;
                }

            } catch (Exception e) {
                logger.error("Error connecting to JESSX server", e);
                try {
                    Thread.sleep(10000); // Wait 10 seconds before retrying
                } catch (InterruptedException e1) {
                    // Ignore
                }
                continue;
            }

            while (!isStopping()) {
                try {
                    String response = is.readUTF();
                    if (response.endsWith("[JessX-end]")) {
                        response = response.substring(0, response.indexOf("[JessX-end]"));
                        processMessage(response);
                    }
                } catch (IOException e) {
                    if (!isStopping()) {
                        logger.error("Connection lost with JESSX server", e);
                    }
                    break;
                }
            }
        }
        disconnect();
    }

    void processMessage(String message) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(new StringReader(message));
            Element root = doc.getRootElement();
            String rootName = root.getName();

            if ("OrderBook".equals(rootName)) {
                String institution = root.getAttributeValue("institution");
                FeedSubscription subscription = getSubscriptionForInstitution(institution);
                if (subscription != null) {
                    List<IBookEntry> bids = new ArrayList<IBookEntry>();
                    List<IBookEntry> asks = new ArrayList<IBookEntry>();

                    Element bidNode = root.getChild("Bid");
                    if (bidNode != null) {
                        for (Object obj : bidNode.getChildren("Operation")) {
                            Element opElement = (Element) obj;
                            BookEntry entry = parseOrder(opElement);
                            if (entry != null) {
                                bids.add(entry);
                            }
                        }
                    }

                    Element askNode = root.getChild("Ask");
                    if (askNode != null) {
                        for (Object obj : askNode.getChildren("Operation")) {
                            Element opElement = (Element) obj;
                            BookEntry entry = parseOrder(opElement);
                            if (entry != null) {
                                asks.add(entry);
                            }
                        }
                    }

                    IBook book = new org.eclipsetrader.core.feed.Book(bids.toArray(new IBookEntry[bids.size()]), asks.toArray(new IBookEntry[asks.size()]));
                    subscription.setBook(book);
                    wakeupNotifyThread();
                }
            }
            else if ("Deal".equals(rootName)) {
                String institution = root.getAttributeValue("institution");
                FeedSubscription subscription = getSubscriptionForInstitution(institution);
                if (subscription != null) {
                    double price = Double.parseDouble(root.getAttributeValue("price"));
                    long size = Long.parseLong(root.getAttributeValue("quantity"));
                    long time = Long.parseLong(root.getAttributeValue("timestamp"));

                    long totalVolume = symbolVolumes.get(institution) + size;
                    symbolVolumes.put(institution, totalVolume);

                    Trade trade = new Trade(new Date(time), price, size, totalVolume);
                    subscription.setTrade(trade);
                    wakeupNotifyThread();
                }
            }
            else if ("ExperimentUpdate".equals(rootName)) {
                int updateType = Integer.parseInt(root.getAttributeValue("updateType"));
                if (updateType == ExpUpdate.PERIOD_FINISHING) {
                    synchronized (symbolSubscriptions) {
                        for (FeedSubscription subscription : symbolSubscriptions.values()) {
                            if (subscription.getTrade() != null) {
                                subscription.setLastClose(subscription.getTrade().getPrice());
                            }
                            subscription.resetOHL();
                        }
                    }
                }
            }
            else {
                logger.info("Received unhandled message: " + rootName);
            }
        } catch (Exception e) {
            logger.error("Error processing message from server", e);
        }
    }

    private BookEntry parseOrder(Element opElement) {
        String type = opElement.getAttributeValue("type");
        if ("Limit Order".equals(type)) {
            Element orderElement = opElement.getChild("Order");
            Element limitOrderElement = orderElement.getChild("LimitOrder");

            double price = Double.parseDouble(limitOrderElement.getAttributeValue("price"));
            long size = Long.parseLong(limitOrderElement.getAttributeValue("quantity"));

            return new BookEntry(null, price, size, 1L, null);
        }
        return null;
    }

    private FeedSubscription getSubscriptionForInstitution(String institution) {
        synchronized (symbolSubscriptions) {
            return symbolSubscriptions.get(institution);
        }
    }

    public void wakeupNotifyThread() {
        if (notificationThread != null) {
            synchronized (notificationThread) {
                notificationThread.notifyAll();
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // This is no longer needed with the simplified subscription management
    }

    @Override
    public void addConnectorListener(IConnectorListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeConnectorListener(IConnectorListener listener) {
        listeners.remove(listener);
    }
}
