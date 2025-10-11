/*
 * Copyright (calendar) 2004-2.011 Marco Maccaferri and others.
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
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
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
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.feed.BookEntry;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IBookEntry;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector2;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.jessx.client.ClientCore;
import org.eclipsetrader.jessx.client.event.NetworkListener;
import org.eclipsetrader.jessx.internal.JessxActivator;
import org.eclipsetrader.jessx.internal.core.repository.IdentifierType;
import org.eclipsetrader.jessx.internal.core.repository.IdentifiersList;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

public class StreamingConnector implements IFeedConnector2, IExecutableExtension, PropertyChangeListener, NetworkListener {

    private static StreamingConnector instance;

    private String id;
    private String name;

    private Map<String, FeedSubscription> symbolSubscriptions;
    private Map<String, FeedSubscription2> symbolSubscriptions2;
    private boolean subscriptionsChanged = false;
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private boolean stopping = false;

    private Log logger = LogFactory.getLog(getClass());

    public StreamingConnector() {
        symbolSubscriptions = new HashMap<String, FeedSubscription>();
        symbolSubscriptions2 = new HashMap<String, FeedSubscription2>();
    }

    public synchronized static StreamingConnector getInstance() {
        if (instance == null) {
            instance = new StreamingConnector();
        }
        return instance;
    }

    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id"); //$NON-NLS-1$
        name = config.getAttribute("name"); //$NON-NLS-1$
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
            IdentifierType identifierType = IdentifiersList.getInstance().getIdentifierFor(identifier);
            FeedSubscription subscription = symbolSubscriptions.get(identifierType.getSymbol());
            if (subscription == null) {
                subscription = new FeedSubscription(this, identifierType);

                PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
                if (propertyChangeSupport != null) {
                    propertyChangeSupport.addPropertyChangeListener(this);
                }

                symbolSubscriptions.put(identifierType.getSymbol(), subscription);
                subscriptionsChanged = true;
            }
            if (identifierType.getIdentifier() == null) {
                identifierType.setIdentifier(identifier);

                PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
                if (propertyChangeSupport != null) {
                    propertyChangeSupport.addPropertyChangeListener(this);
                }
            }
            if (subscription.incrementInstanceCount() == 1) {
                subscriptionsChanged = true;
            }
            return subscription;
        }
    }

    protected void disposeSubscription(FeedSubscription subscription) {
        synchronized (symbolSubscriptions) {
            if (subscription.decrementInstanceCount() <= 0) {
                IdentifierType identifierType = subscription.getIdentifierType();

                if (subscription.getIdentifier() != null) {
                    PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) subscription.getIdentifier().getAdapter(PropertyChangeSupport.class);
                    if (propertyChangeSupport != null) {
                        propertyChangeSupport.removePropertyChangeListener(this);
                    }
                }

                symbolSubscriptions.remove(identifierType.getSymbol());
                subscriptionsChanged = true;
            }
        }
    }

    @Override
    public IFeedSubscription2 subscribeLevel2(IFeedIdentifier identifier) {
        FeedSubscription subscription;
        IdentifierType identifierType;

        synchronized (symbolSubscriptions) {
            identifierType = IdentifiersList.getInstance().getIdentifierFor(identifier);
            subscription = symbolSubscriptions.get(identifierType.getSymbol());
            if (subscription == null) {
                subscription = new FeedSubscription(this, identifierType);

                PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
                if (propertyChangeSupport != null) {
                    propertyChangeSupport.addPropertyChangeListener(this);
                }

                symbolSubscriptions.put(identifierType.getSymbol(), subscription);
                subscriptionsChanged = true;
            }
            if (subscription.incrementInstanceCount() == 1) {
                subscriptionsChanged = true;
            }
        }

        synchronized (symbolSubscriptions2) {
            FeedSubscription2 subscription2 = symbolSubscriptions2.get(identifierType.getSymbol());
            if (subscription2 == null) {
                subscription2 = new FeedSubscription2(this, subscription);
                symbolSubscriptions2.put(identifierType.getSymbol(), subscription2);
                subscriptionsChanged = true;
            }
            if (subscription.incrementLevel2InstanceCount() == 1) {
                subscriptionsChanged = true;
            }
            return subscription;
        }
    }

    @Override
    public IFeedSubscription2 subscribeLevel2(String symbol) {
        FeedSubscription subscription;
        IdentifierType identifierType;

        synchronized (symbolSubscriptions) {
            FeedProperties prop = new FeedProperties();
            FeedIdentifier fi = new FeedIdentifier(symbol, prop);
            identifierType = IdentifiersList.getInstance().getIdentifierFor(fi);
            subscription = symbolSubscriptions.get(identifierType.getSymbol());
            if (subscription == null) {
                subscription = new FeedSubscription(this, identifierType);
                symbolSubscriptions.put(identifierType.getSymbol(), subscription);
                subscriptionsChanged = true;
            }
            if (subscription.incrementInstanceCount() == 1) {
                subscriptionsChanged = true;
            }
        }

        synchronized (symbolSubscriptions2) {
            FeedSubscription2 subscription2 = symbolSubscriptions2.get(identifierType.getSymbol());
            if (subscription2 == null) {
                subscription2 = new FeedSubscription2(this, subscription);
                symbolSubscriptions2.put(identifierType.getSymbol(), subscription2);
                subscriptionsChanged = true;
            }
            if (subscription.incrementLevel2InstanceCount() == 1) {
                subscriptionsChanged = true;
            }
            return subscription;
        }
    }

    protected void disposeSubscription2(FeedSubscription subscription, FeedSubscription2 subscription2) {
        synchronized (symbolSubscriptions2) {
            if (subscription.decrementLevel2InstanceCount() <= 0) {
                IdentifierType identifierType = subscription.getIdentifierType();
                symbolSubscriptions2.remove(identifierType.getSymbol());
                subscriptionsChanged = true;
            }
        }
        synchronized (symbolSubscriptions) {
            if (subscription.decrementInstanceCount() <= 0) {
                IdentifierType identifierType = subscription.getIdentifierType();

                if (subscription.getIdentifier() != null) {
                    PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) subscription.getIdentifier().getAdapter(PropertyChangeSupport.class);
                    if (propertyChangeSupport != null) {
                        propertyChangeSupport.removePropertyChangeListener(this);
                    }
                }

                symbolSubscriptions.remove(identifierType.getSymbol());
                subscriptionsChanged = true;
            }
        }
    }

    @Override
    public synchronized void connect() {
        // The connection is now managed by the BrokerConnector
    }

    public synchronized void doConnect() {
        stopping = false;
        try {
            ClientCore.connecToServer("localhost", "EclipseTraderFeed", "password");
            ClientCore.addNetworkListener(this, "OrderBook");
            logger.info("StreamingConnector connected to JESSX server and listening for OrderBook updates.");
        }
        catch (IOException e) {
            JessxActivator.log(new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error connecting to JESSX server", e));
        }
    }

    @Override
    public synchronized void disconnect() {
        stopping = true;
        ClientCore.removeNetworkListener(this);
        logger.info("StreamingConnector disconnected from JESSX server.");
    }

    public boolean isStopping() {
        return stopping;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof IFeedIdentifier) {
            IFeedIdentifier identifier = (IFeedIdentifier) evt.getSource();
            synchronized (symbolSubscriptions) {
                for (FeedSubscription subscription : symbolSubscriptions.values()) {
                    if (subscription.getIdentifier() == identifier) {
                        symbolSubscriptions.remove(subscription.getIdentifierType().getSymbol());
                        IdentifierType identifierType = IdentifiersList.getInstance().getIdentifierFor(identifier);
                        subscription.setIdentifierType(identifierType);
                        symbolSubscriptions.put(identifierType.getSymbol(), subscription);
                        subscriptionsChanged = true;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void addConnectorListener(IConnectorListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeConnectorListener(IConnectorListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void objectReceived(Document doc) {
        if (doc == null || doc.getRootElement() == null) {
            logger.warn("Received null or empty document from server.");
            return;
        }

        Element root = doc.getRootElement();
        String rootName = root.getName();
        logger.info("Received XML from JESSX Server. Root element: " + rootName);

        try {
            XMLOutputter xmlOutputter = new XMLOutputter();
            logger.debug("Full XML content: " + xmlOutputter.outputString(doc));
        }
        catch (Exception e) {
            logger.error("Error logging received XML content", e);
        }

        if ("OrderBook".equals(rootName)) {
            updateOrderBookData(root);
        } else {
            logger.warn("Received unexpected XML document type: " + rootName);
        }
    }

    private void updateOrderBookData(Element orderBookElement) {
        String symbol = orderBookElement.getAttributeValue("institution");
        if (symbol == null) {
            return;
        }

        FeedSubscription subscription;
        synchronized (symbolSubscriptions) {
            subscription = symbolSubscriptions.get(symbol);
            if (subscription == null) {
                logger.warn("Received update for unsubscribed symbol: " + symbol + ". Subscribing dynamically.");
                FeedIdentifier identifier = new FeedIdentifier(symbol, new FeedProperties());
                subscribe(identifier);
                subscription = symbolSubscriptions.get(symbol);
                if (subscription == null) {
                    logger.error("Failed to dynamically subscribe to symbol: " + symbol);
                    return;
                }
            }
        }

        try {
            List<IBookEntry> bids = new ArrayList<IBookEntry>();
            Element bidNode = orderBookElement.getChild("Bid");
            if (bidNode != null) {
                for (Object obj : bidNode.getChildren("Operation")) {
                    Element opElement = (Element) obj;
                    bids.add(parseOrder(opElement));
                }
            }

            List<IBookEntry> asks = new ArrayList<IBookEntry>();
            Element askNode = orderBookElement.getChild("Ask");
            if (askNode != null) {
                for (Object obj : askNode.getChildren("Operation")) {
                    Element opElement = (Element) obj;
                    asks.add(parseOrder(opElement));
                }
            }

            IBook book = new org.eclipsetrader.core.feed.Book(bids.toArray(new IBookEntry[0]), asks.toArray(new IBookEntry[0]));
            final FeedSubscription finalSubscription = subscription;
            subscription.setBook(book);

            if (!bids.isEmpty() && !asks.isEmpty()) {
                IBookEntry bestBid = bids.get(0);
                IBookEntry bestAsk = asks.get(0);
                Quote quote = new Quote(bestBid.getPrice(), bestAsk.getPrice(), bestBid.getQuantity(), bestAsk.getQuantity());
                subscription.setQuote(quote);
            }

            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    finalSubscription.fireNotification();
                }
            });

        }
        catch (Exception e) {
            logger.error("Error parsing order book data from XML for symbol: " + symbol, e);
        }
    }

    private IBookEntry parseOrder(Element operationElement) {
        Element orderElement = operationElement.getChild("Order");
        long timestamp = Long.parseLong(orderElement.getAttributeValue("timestamp"));

        Element limitOrderElement = operationElement.getChild("LimitOrder");
        double price = Double.parseDouble(limitOrderElement.getAttributeValue("price"));
        long quantity = Long.parseLong(limitOrderElement.getAttributeValue("quantity"));

        return new BookEntry(new Date(timestamp), price, quantity, 1L, "");
    }
}