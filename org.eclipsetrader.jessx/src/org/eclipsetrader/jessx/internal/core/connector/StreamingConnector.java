/*
 * Copyright (calendar) 2004-2011 Marco Maccaferri and others.
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
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.Bar;
import org.eclipsetrader.core.feed.BarOpen;
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
import org.eclipsetrader.core.feed.LastClose;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.QuoteDelta;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.TodayOHL;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.jessx.internal.JessxActivator;
import org.eclipsetrader.jessx.internal.core.messages.AstaApertura;
import org.eclipsetrader.jessx.internal.core.messages.AstaChiusura;
import org.eclipsetrader.jessx.internal.core.messages.BidAsk;
import org.eclipsetrader.jessx.internal.core.messages.Book;
import org.eclipsetrader.jessx.internal.core.messages.CreaMsg;
import org.eclipsetrader.jessx.internal.core.messages.DataMessage;
import org.eclipsetrader.jessx.internal.core.messages.ErrorMessage;
import org.eclipsetrader.jessx.internal.core.messages.Header;
import org.eclipsetrader.jessx.internal.core.messages.Message;
import org.eclipsetrader.jessx.internal.core.messages.Price;
import org.eclipsetrader.jessx.internal.core.messages.Util;
import org.eclipsetrader.jessx.internal.core.repository.IdentifierType;
import org.eclipsetrader.jessx.internal.core.repository.IdentifiersList;
import org.eclipsetrader.jessx.internal.core.repository.PriceDataType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class StreamingConnector implements Runnable, IFeedConnector2, IExecutableExtension, PropertyChangeListener {

    private static StreamingConnector instance;

    private String id;
    private String name;

    private Map<String, FeedSubscription> symbolSubscriptions;
    private Map<String, FeedSubscription2> symbolSubscriptions2;
    private boolean subscriptionsChanged = false;
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private TimeZone timeZone;
    private SimpleDateFormat df;
    private SimpleDateFormat df2;
    private SimpleDateFormat df3;

    private Thread thread;
    private Thread notificationThread;
    private volatile boolean stopping = false;

    private String streamingServer = "localhost"; //$NON-NLS-1$
    private int streamingPort = 6290;
    private Socket socket;
    private OutputStream os;
    private DataInputStream is;
    private Set<String> sTit;
    private Set<String> sTit2;

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
        symbolSubscriptions2 = new HashMap<String, FeedSubscription2>();

        timeZone = TimeZone.getTimeZone("Europe/Rome"); //$NON-NLS-1$

        df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); //$NON-NLS-1$
        df.setTimeZone(timeZone);
        df2 = new SimpleDateFormat("dd.MM.yyyy HHmmss"); //$NON-NLS-1$
        df2.setTimeZone(timeZone);
        df3 = new SimpleDateFormat("yyyyMMdd HH:mm:ss"); //$NON-NLS-1$
        df3.setTimeZone(timeZone);
    }

    public synchronized static StreamingConnector getInstance() {
        if (instance == null) {
            instance = new StreamingConnector();
        }
        return instance;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id"); //$NON-NLS-1$
        name = config.getAttribute("name"); //$NON-NLS-1$
        instance = this;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#subscribe(org.eclipsetrader.core.feed.IFeedIdentifier)
     */
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

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector2#subscribeLevel2(org.eclipsetrader.core.feed.IFeedIdentifier)
     */
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

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector2#subscribeLevel2(java.lang.String)
     */
    @Override
    public IFeedSubscription2 subscribeLevel2(String symbol) {
        FeedSubscription subscription;
        IdentifierType identifierType;

        synchronized (symbolSubscriptions) {
        	
        	FeedProperties prop = new FeedProperties();
        	
        	FeedIdentifier fi = new FeedIdentifier(symbol,prop);
        	
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

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#connect()
     */
    @Override
    public synchronized void connect() {
        // Do nothing, connection is handled manually via start()
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#disconnect()
     */
    @Override
    public synchronized void disconnect() {
        // Do nothing, connection is handled manually via stop()
    }

    public synchronized void start() {
        logger.info("StreamingConnector starting...");
        stopping = false;

        if (notificationThread == null || !notificationThread.isAlive()) {
            notificationThread = new Thread(notificationRunnable, name + " - Notification"); //$NON-NLS-1$
            notificationThread.start();
        }

        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this, name + " - Data Reader"); //$NON-NLS-1$
            thread.start();
        }
    }

    public synchronized void stop() {
        logger.info("StreamingConnector stopping...");
        stopping = true;

        if (thread != null) {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                thread.join(30 * 1000);
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error stopping thread", e); //$NON-NLS-1$
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
                Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error stopping notification thread", e); //$NON-NLS-1$
                JessxActivator.log(status);
            }
            notificationThread = null;
        }
    }

    public boolean isStopping() {
        return stopping;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    @SuppressWarnings({
        "rawtypes", "unchecked"
    })
    public void run() {
        int n = 0;
        byte bHeader[] = new byte[4];

        sTit = new HashSet<String>();
        sTit2 = new HashSet<String>();

        // Apertura del socket verso il server
        try {
            Proxy socksProxy = Proxy.NO_PROXY;
            if (JessxActivator.getDefault() != null) {
                BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
                ServiceReference reference = context.getServiceReference(IProxyService.class.getName());
                if (reference != null) {
                    IProxyService proxyService = (IProxyService) context.getService(reference);
                    IProxyData[] proxyData = proxyService.select(new URI(null, streamingServer, null, null));
                    for (int i = 0; i < proxyData.length; i++) {
                        if (IProxyData.SOCKS_PROXY_TYPE.equals(proxyData[i].getType()) && proxyData[i].getHost() != null) {
                            socksProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyData[i].getHost(), proxyData[i].getPort()));
                            break;
                        }
                    }
                    context.ungetService(reference);
                }
            }
            socket = new Socket(socksProxy);
            socket.connect(new InetSocketAddress(streamingServer, streamingPort));
            os = socket.getOutputStream();
            is = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            JessxActivator.log(new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error connecting to streaming server", e)); //$NON-NLS-1$
            try {
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
            } catch (Exception e1) {
                // Do nothing
            }
            return;
        }

        while (!isStopping()) {
            if (subscriptionsChanged) {
                try {
                    updateStreamSubscriptions();
                } catch (Exception e) {
                    thread = null;
                    JessxActivator.log(new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error updating stream subscriptions", e)); //$NON-NLS-1$
                    break;
                }
            }

            // Legge l'header di un messaggio (se c'e')
            try {
                if ((n = is.read(bHeader)) == -1) {
                    continue;
                }
                while (n < 4) {
                    int r = is.read(bHeader, n, 4 - n);
                    n += r;
                }
            } catch (Exception e) {
                if (!isStopping() && !(e instanceof java.net.SocketException)) {
                    JessxActivator.log(new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error reading data", e)); //$NON-NLS-1$
                }
                break;
            }

            // Verifica la correttezza dell'header e legge il resto del messaggio
            Header h = new Header();
            h.start = (char) Util.byteToInt(bHeader[0]);
            if (h.start == '#') {
                h.tipo = Util.getByte(bHeader[1]);
                h.len = Util.getMessageLength(bHeader, 2);
                byte mes[] = new byte[h.len];
                try {
                    n = is.read(mes);
                    while (n < h.len) {
                        int r = is.read(mes, n, h.len - n);
                        n += r;
                    }
                } catch (Exception e) {
                }

                if (h.tipo == CreaMsg.ERROR_MSG) {
                    ErrorMessage eMsg = new ErrorMessage(mes);
                    JessxActivator.log(new Status(IStatus.WARNING, JessxActivator.PLUGIN_ID, 0, "Message from server: " + eMsg.sMessageError, null)); //$NON-NLS-1$
                }
                else if (h.tipo == Message.TIP_ECHO) {
                    try {
                        os.write(new byte[] {
                            bHeader[0], bHeader[1], bHeader[2], bHeader[3], mes[0], mes[1]
                        });
                        os.flush();
                    } catch (Exception e) {
                        // Do nothing
                    }
                }
                else if (h.len > 0) {
                    DataMessage obj;
                    try {
                        obj = Message.decodeMessage(mes);
                        if (obj == null) {
                            continue;
                        }
                    } catch (Exception e) {
                        JessxActivator.log(new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error decoding incoming message", e)); //$NON-NLS-1$
                        continue;
                    }

                    processMessage(obj);
                }
            }
        }

        try {
            os.close();
            is.close();
            socket.close();
        } catch (Exception e) {
            JessxActivator.log(new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error closing connection to streaming server", e)); //$NON-NLS-1$
        }

        os = null;
        is = null;
        socket = null;

        if (!isStopping()) {
            thread = new Thread(this, name + " - Data Reader"); //$NON-NLS-1$
            try {
                Thread.sleep(2 * 1000);
            } catch (Exception e) {
                // Do nothing
            }
            thread.start();
        }
    }

    void updateStreamSubscriptions() throws IOException {
        Set<String> toAdd = new HashSet<String>();
        Set<String> toRemove = new HashSet<String>();
        Set<String> toAdd2 = new HashSet<String>();
        Set<String> toRemove2 = new HashSet<String>();

        synchronized (symbolSubscriptions) {
            for (FeedSubscription s : symbolSubscriptions.values()) {
                if (!sTit.contains(s.getIdentifierType().getSymbol())) {
                    toAdd.add(s.getIdentifierType().getSymbol());
                }
                if (s.getLevel2InstanceCount() != 0) {
                    if (!sTit2.contains(s.getIdentifierType().getSymbol())) {
                        toAdd2.add(s.getIdentifierType().getSymbol());
                    }
                }
            }
            for (String s : sTit) {
                if (!symbolSubscriptions.containsKey(s)) {
                    toRemove.add(s);
                }
            }
            for (String s : sTit2) {
                if (!symbolSubscriptions2.containsKey(s)) {
                    toRemove2.add(s);
                }
            }
            subscriptionsChanged = false;
        }

        if (toRemove.size() != 0) {
            logger.info("Removing " + toRemove); //$NON-NLS-1$
            String[] symbols = toRemove.toArray(new String[toRemove.size()]);
            int[] flags = new int[symbols.length];
            byte[] msg = CreaMsg.creaPortMsg(CreaMsg.PORT_DEL, symbols, flags);
            os.write(msg);
            os.flush();
        }

        if (toAdd.size() != 0) {
            logger.info("Adding " + toAdd); //$NON-NLS-1$
            String[] symbols = toAdd.toArray(new String[toAdd.size()]);
            int[] flags = new int[symbols.length];
            for (int i = 0; i < symbols.length; i++) {
                flags[i] = toAdd2.contains(symbols[i]) ? 105 : 0;
            }
            byte[] msg = CreaMsg.creaPortMsg(CreaMsg.PORT_ADD, symbols, flags);
            os.write(msg);
            os.flush();
        }

        Map<String, Integer> toMod = new HashMap<String, Integer>();
        if (toAdd2.size() != 0 || toRemove2.size() != 0) {
            for (String s : toAdd2) {
                if (!toAdd.contains(s)) {
                    toMod.put(s, new Integer(105));
                }
            }
            for (String s : toRemove2) {
                toMod.put(s, new Integer(0));
            }

            if (toMod.size() != 0) {
                logger.info("Modifying " + toMod); //$NON-NLS-1$
                String[] symbols = toMod.keySet().toArray(new String[toMod.keySet().size()]);
                int[] flags = new int[symbols.length];
                for (int i = 0; i < symbols.length; i++) {
                    flags[i] = toMod.get(symbols[i]);
                }
                byte[] msg = CreaMsg.creaPortMsg(CreaMsg.PORT_MOD, symbols, flags);
                os.write(msg);
                os.flush();
            }
        }

        sTit.removeAll(toRemove);
        sTit.addAll(toAdd);
        sTit2.removeAll(toRemove2);
        sTit2.addAll(toAdd2);
    }

    void processMessage(DataMessage obj) {
        FeedSubscription subscription = symbolSubscriptions.get(obj.head.key);
        if (subscription == null) {
            return;
        }

        PriceDataType priceData = subscription.getIdentifierType().getPriceData();

        if (obj instanceof Price) {
            Price pm = (Price) obj;

            priceData.setLast(pm.val_ult);
            priceData.setLastSize(pm.qta_ult);
            priceData.setVolume(pm.qta_prgs);
            priceData.setTime(new Date(pm.ora_ult));
            subscription.setTrade(new Trade(priceData.getTime(), priceData.getLast(), priceData.getLastSize(), priceData.getVolume()));

            priceData.setHigh(pm.max);
            priceData.setLow(pm.min);

            if ((priceData.getOpen() == null || priceData.getOpen() == 0.0) && pm.val_ult != 0.0) {
                priceData.setOpen(pm.val_ult);

                Calendar c = Calendar.getInstance();
                c.setTime(priceData.getTime());
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                BarOpen bar = new BarOpen(c.getTime(), TimeSpan.days(1), priceData.getOpen());
                subscription.addDelta(new QuoteDelta(subscription.getIdentifier(), null, bar));
            }

            if (priceData.getOpen() != null && priceData.getOpen() != 0.0 && priceData.getHigh() != 0.0 && priceData.getLow() != 0.0) {
                subscription.setTodayOHL(new TodayOHL(priceData.getOpen(), priceData.getHigh(), priceData.getLow()));
            }
        }
        else if (obj instanceof Book) {
            Book bm = (Book) obj;

            IBook oldValue = subscription.getBook();

            int levels = bm.offset + 5;
            if (oldValue != null) {
                IBookEntry[] oldEntry = oldValue.getBidProposals();
                if (oldEntry != null) {
                    levels = Math.max(levels, oldEntry.length);
                }
                oldEntry = oldValue.getAskProposals();
                if (oldEntry != null) {
                    levels = Math.max(levels, oldEntry.length);
                }
            }

            IBookEntry[] bidEntry = new IBookEntry[levels];
            IBookEntry[] askEntry = new IBookEntry[levels];

            if (oldValue != null) {
                IBookEntry[] oldEntry = oldValue.getBidProposals();
                if (oldEntry != null) {
                    System.arraycopy(oldEntry, 0, bidEntry, 0, oldEntry.length);
                }
                oldEntry = oldValue.getAskProposals();
                if (oldEntry != null) {
                    System.arraycopy(oldEntry, 0, askEntry, 0, oldEntry.length);
                }
            }

            int index = bm.offset;
            for (int i = 0; i < 5; i++) {
                bidEntry[index + i] = new BookEntry(null, bm.val_c[i], new Long(bm.q_pdn_c[i]), new Long(bm.n_pdn_c[i]), null);
                askEntry[index + i] = new BookEntry(null, bm.val_v[i], new Long(bm.q_pdn_v[i]), new Long(bm.n_pdn_v[i]), null);
            }
            IBook newValue = new org.eclipsetrader.core.feed.Book(bidEntry, askEntry);
            subscription.setBook(newValue);
        }
        else if (obj instanceof BidAsk) {
            BidAsk bam = (BidAsk) obj;

            priceData.setBid(bam.bid);
            priceData.setBidSize(bam.num_bid);
            priceData.setAsk(bam.ask);
            priceData.setAskSize(bam.num_ask);
            subscription.setQuote(new Quote(priceData.getBid(), priceData.getAsk(), priceData.getBidSize(), priceData.getAskSize()));
        }
        else if (obj instanceof AstaApertura) {
            AstaApertura ap = (AstaApertura) obj;

            if (ap.val_aper != 0.0) {
                subscription.setPrice(new org.eclipsetrader.core.feed.Price(new Date(ap.ora_aper), ap.val_aper));
            }

            if (priceData.getClose() != null) {
                priceData.setLastClose(priceData.getClose());
                priceData.setClose(null);
                subscription.setLastClose(new LastClose(priceData.getLastClose(), null));
            }
            if (priceData.getOpen() != null) {
                priceData.setOpen(null);
                priceData.setHigh(null);
                priceData.setLow(null);
                subscription.setTodayOHL(new TodayOHL(priceData.getOpen(), priceData.getHigh(), priceData.getLow()));
            }
        }
        else if (obj instanceof AstaChiusura) {
            AstaChiusura ac = (AstaChiusura) obj;

            if (priceData.getClose() == null && priceData.getLast() != null) {
                priceData.setClose(priceData.getLast());

                Calendar c = Calendar.getInstance();
                c.setTime(priceData.getTime());
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                Bar bar = new Bar(c.getTime(), TimeSpan.days(1), priceData.getOpen(), priceData.getHigh(), priceData.getLow(), priceData.getClose(), priceData.getVolume());
                subscription.addDelta(new QuoteDelta(subscription.getIdentifier(), null, bar));
            }

            if (ac.val_chiu != 0.0) {
                subscription.setPrice(new org.eclipsetrader.core.feed.Price(new Date(ac.ora_chiu), ac.val_chiu));
            }
        }

        if (subscription.hasPendingChanges()) {
            wakeupNotifyThread();
        }
    }

    public void wakeupNotifyThread() {
        if (notificationThread != null) {
            synchronized (notificationThread) {
                notificationThread.notifyAll();
            }
        }
    }

    public void setTrade(IFeedIdentifier identifier, Trade trade) {
        IdentifierType identifierType = IdentifiersList.getInstance().getIdentifierFor(identifier);
        FeedSubscription subscription = symbolSubscriptions.get(identifierType.getSymbol());
        if (subscription != null) {
            subscription.setTrade(trade);
        }
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

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#addConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
     */
    @Override
    public void addConnectorListener(IConnectorListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#removeConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
     */
    @Override
    public void removeConnectorListener(IConnectorListener listener) {
        listeners.remove(listener);
    }
}