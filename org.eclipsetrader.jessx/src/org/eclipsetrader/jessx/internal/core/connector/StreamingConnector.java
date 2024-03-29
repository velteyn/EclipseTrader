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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.TodayOHL;
import org.eclipsetrader.core.feed.Trade;


import org.eclipsetrader.jessx.internal.JessxActivator;
import org.eclipsetrader.jessx.internal.core.Util;
import org.eclipsetrader.jessx.internal.core.WebConnector;
import org.eclipsetrader.jessx.internal.core.repository.IdentifierType;
import org.eclipsetrader.jessx.internal.core.repository.PriceDataType;

public class StreamingConnector extends SnapshotConnector {

    public static final String K_SYMBOL = "s";
    public static final String K_LAST = "l10";
    public static final String K_VOLUME = "v00";
    public static final String K_ASK_PRICE = "a00";
    public static final String K_ASK_SIZE = "a50";
    public static final String K_BID_PRICE = "b00";
    public static final String K_BID_SIZE = "b60";
    public static final String K_HIGH = "h00";
    public static final String K_LOW = "g00";
    public static final String K_TIME = "t10";
    private static StreamingConnector instance;
    private StringBuilder line;
    private StringBuilder script;
    private boolean inTag;
    private boolean inScript;
    
    

    private String id;
    private String name;
    
    private boolean stopping = false;
    
    private Thread notificationThread;
      
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    public StreamingConnector() {
    }
    
    
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

    public synchronized static StreamingConnector getInstance() {
        if (instance == null) {
            instance = new StreamingConnector();
        }
        return instance;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.jessx.internal.feed.SnapshotMarketFeed#run()
     */
    @Override
    public void run() {
        BufferedReader in = null;
        char[] buffer = new char[512];

        try {
           
           
         

            while (!isStopping()) {
                // Check if the connection was not yet initialized or there are changed in the subscriptions.
                if (in == null || isSubscriptionsChanged()) {
                    try {
                        
                        if (in != null) {
                            in.close();
                        }
                    } catch (Exception e) {
                        // We can't do anything at this time, ignore
                    }

                    String[] symbols;
                    synchronized (symbolSubscriptions) {
                        Set<String> s = new HashSet<String>(symbolSubscriptions.keySet());
                        s.add("MSFT");
                        symbols = s.toArray(new String[s.size()]);
                        setSubscriptionsChanged(false);
                        if (symbols.length == 0) {
                            break;
                        }
                    }
                 

                    

                    in = new BufferedReader(new InputStreamReader(null));

                    line = new StringBuilder();
                    script = new StringBuilder();
                    inTag = false;
                    inScript = false;

                    fetchLatestSnapshot(null, false);
                }

                if (in.ready()) {
                    int length = in.read(buffer);
                    if (length == -1) {
                        in.close();
                        in = null;
                        continue;
                    }
                    processIncomingChars(buffer, length);
                }
                else {
                    // Check stale data
                    List<String> updateList = new ArrayList<String>();
                    synchronized (symbolSubscriptions) {
                        long currentTime = System.currentTimeMillis();
                        for (FeedSubscription subscription : symbolSubscriptions.values()) {
                            long elapsedTime = currentTime - subscription.getIdentifierType().getLastUpdate();
                            if (elapsedTime >= 60000) {
                                updateList.add(subscription.getIdentifierType().getSymbol());
                                subscription.getIdentifierType().setLastUpdate(currentTime / 60000 * 60000);
                            }
                        }
                    }
                    if (updateList.size() != 0) {
                        fetchLatestSnapshot(null, true);
                    }
                }

                Thread.sleep(100);
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error reading data", e);
            JessxActivator.log(status);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                // We can't do anything at this time, ignore
            }
        }
    }

    protected void processIncomingChars(char[] chars, int length) {
        for (int i = 0; i < length; i++) {
            char ch = chars[i];
            if (ch == '<' && !inTag) {
                inTag = true;
            }
            if (inTag) {
                line.append(ch);
            }
            if (inScript) {
                script.append(ch);
            }
            if (ch == '>' && inTag) {
                inTag = false;
                String tag = line.toString();
                if (tag.equals("<script>")) {
                    inScript = true;
                }
                if (tag.equals("</script>")) {
                    inScript = false;
                    if (script.length() >= tag.length()) {
                        script.delete(script.length() - tag.length(), script.length());
                    }

                    Map<String, String> valueMap = parseScript(script.toString());
                    processValues(valueMap);

                    script = new StringBuilder();
                }
                line = new StringBuilder();
            }
        }
    }

    protected void processValues(Map<String, String> valueMap) {
        String symbol = valueMap.get(K_SYMBOL);
        FeedSubscription subscription = symbolSubscriptions.get(symbol);
        if (subscription != null) {
            IdentifierType identifierType = subscription.getIdentifierType();
            PriceDataType priceData = identifierType.getPriceData();

            if (valueMap.containsKey(K_TIME)) {
                priceData.setTime(new Date(getLongValue(valueMap.get(K_TIME)).longValue() * 1000));
            }
            long tradeSize = 0;
            if (valueMap.containsKey(K_VOLUME)) {
                tradeSize = getLongValue(valueMap.get(K_VOLUME)) - (priceData.getVolume() != null ? priceData.getVolume() : 0);
                priceData.setLastSize(tradeSize);
            }
            if (valueMap.containsKey(K_LAST)) {
                priceData.setLast(getDoubleValue(valueMap.get(K_LAST)));
            }
            subscription.setTrade(new Trade(priceData.getTime(), priceData.getLast(), priceData.getLastSize(), priceData.getVolume()));

            if (valueMap.containsKey(K_BID_PRICE)) {
                priceData.setBid(getDoubleValue(valueMap.get(K_BID_PRICE)));
            }
            if (valueMap.containsKey(K_BID_SIZE)) {
                priceData.setBidSize(getLongValue(valueMap.get(K_BID_SIZE)));
            }
            if (valueMap.containsKey(K_ASK_PRICE)) {
                priceData.setAsk(getDoubleValue(valueMap.get(K_ASK_PRICE)));
            }
            if (valueMap.containsKey(K_ASK_SIZE)) {
                priceData.setAskSize(getLongValue(valueMap.get(K_ASK_SIZE)));
            }
            subscription.setQuote(new Quote(priceData.getBid(), priceData.getAsk(), priceData.getBidSize(), priceData.getAskSize()));

            if (valueMap.containsKey(K_HIGH)) {
                priceData.setHigh(getDoubleValue(valueMap.get(K_HIGH)));
            }
            if (valueMap.containsKey(K_LOW)) {
                priceData.setLow(getDoubleValue(valueMap.get(K_LOW)));
            }
            if (valueMap.containsKey(K_VOLUME)) {
                priceData.setVolume(getLongValue(valueMap.get(K_VOLUME)));
            }
            if (priceData.getOpen() != null && priceData.getOpen() != 0.0 && priceData.getHigh() != null && priceData.getHigh() != 0.0 && priceData.getLow() != null && priceData.getLow() != 0.0) {
                subscription.setTodayOHL(new TodayOHL(priceData.getOpen(), priceData.getHigh(), priceData.getLow()));
            }

            subscription.fireNotification();
        }
    }

    protected Map<String, String> parseScript(String script) {
        Map<String, String> map = new HashMap<String, String>();

        int e = 0;
        int s = script.indexOf("unixtime");
        if (s != -1) {
            s += 10;
            e = script.indexOf(',', s);
            if (e == -1) {
                e = script.indexOf('}', s);
            }
            map.put("unixtime", script.substring(s, e));
        }

        s = script.indexOf("open");
        if (s != -1) {
            s += 6;
            e = script.indexOf(',', s);
            if (e == -1) {
                e = script.indexOf('}', s);
            }
            map.put("open", script.substring(s, e));
        }

        s = script.indexOf("close");
        if (s != -1) {
            s += 7;
            e = script.indexOf(',', s);
            if (e == -1) {
                e = script.indexOf('}', s);
            }
            map.put("close", script.substring(s, e));
        }

        s = script.indexOf('"', e);
        if (s != -1) {
            s++;
            e = script.indexOf('"', s);
            String symbol = script.substring(s, e);
            map.put(K_SYMBOL, symbol);

            boolean inExpression = false;
            boolean inValue = false;
            int vs = -1;
            int ve = -1;
            for (int i = e + 1; i < script.length(); i++) {
                char ch = script.charAt(i);
                if (inExpression) {
                    if (ch == ':') {
                        e = i;
                    }
                    if (ch == '"') {
                        inValue = !inValue;
                        if (inValue) {
                            vs = i + 1;
                        }
                        else {
                            ve = i;
                            try {
                                String key = script.substring(s, e);
                                String value = script.substring(vs, ve);
                                map.put(key, value);
                            } catch (RuntimeException e1) {
                                System.err.println(script);
                                e1.printStackTrace();
                            }
                        }
                    }
                    if ((ch == ',' || ch == '}') && !inValue) {
                        inExpression = false;
                    }
                }
                else {
                    if (Character.isLetter(ch)) {
                        inExpression = true;
                        s = i;
                    }
                }
            }
        }

        return map;
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

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#getName()
     */
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public synchronized void disconnect() {
    	   stopping = true;

           if (thread != null) {
               try {
                   thread.join(30 * 1000);
               } catch (InterruptedException e) {
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
    
    
    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#connect()
     */
    @Override
    public synchronized void connect() {
    	
    	
    	 
        WebConnector.getInstance().login();

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
    
    public boolean isStopping() {
        return stopping;
    }

}
