/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.jessx.internal.core;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;


import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.jessx.internal.JessxActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Util {

    public static final String snapshotFeedHost = "localhost"; //$NON-NLS-1$
    public static final String streamingFeedHost = "localhost"; //$NON-NLS-1$
    public static final String historyFeedHost = "localhost"; //$NON-NLS-1$

    private Util() {
    }

    public static String getSymbol(IFeedIdentifier identifier) {
        String symbol = identifier.getSymbol();

        IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
        if (properties != null) {
            if (properties.getProperty("org.eclipsetrader.yahoo.symbol") != null) {
                symbol = properties.getProperty("org.eclipsetrader.yahoo.symbol");
            }
        }

        return symbol;
    }

    /**
     * Builds the http method for live prices snapshot download.
     *
     * @return the method.
     */
    public static Object getSnapshotFeedMethod(String[] symbols) {
        /*GetMethod method = new GetMethod("http://" + snapshotFeedHost + "/download/javasoft.beans");
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < symbols.length; i++) {
            if (i != 0) {
                s.append(" "); //$NON-NLS-1$
            }
            s.append(symbols[i]);
        }
        method.setQueryString(new NameValuePair[] {
                new NameValuePair("symbols", s.toString()),
                new NameValuePair("format", "sl1d1t1c1ohgvbapb6a5"),
        });*/
        return null;
    }

    /**
     * Builds the http method for streaming prices download.
     *
     * @return the method.
     */
    public static Object getStreamingFeedMethod(String[] symbols) {
        /*GetMethod method = new GetMethod("http://" + streamingFeedHost + "/streamer/1.0");

        StringBuffer s = new StringBuffer();
        for (int i = 0; i < symbols.length; i++) {
            if (i != 0) {
                s.append(","); //$NON-NLS-1$
            }
            s.append(symbols[i]);
        }

        method.setQueryString(new NameValuePair[] {
                new NameValuePair("s", s.toString()),
                new NameValuePair("k", "a00,a50,b00,b60,g00,h00,j10,l10,t10,v00"),
                new NameValuePair("j", "c10,l10,p20,t10"),
                new NameValuePair("r", "0"),
                new NameValuePair("marketid", "us_market"),
                new NameValuePair("callback", "parent.yfs_u1f"),
                new NameValuePair("mktmcb", "parent.yfs_mktmcb"),
                new NameValuePair("gencallback", "parent.yfs_gencb"),
        });
        method.setFollowRedirects(true);
*/
        return null;
    }

    /**
     * Builds the http method for historycal prices download.
     *
     * @return the method.
     */
    public static Object getHistoryFeedMethod(IFeedIdentifier identifier, Date from, Date to)  {
        String symbol = getSymbol(identifier);

        Calendar fromDate = Calendar.getInstance();
        fromDate.setTime(from);

        Calendar toDate = Calendar.getInstance();
        toDate.setTime(to);

        String prefix = "/instrument/1.0/";
        String suffix = "/chartdata;type=quote";
        /*URI uri = new URI("http", "chartapi.finance.yahoo.com", prefix + symbol.toLowerCase() + suffix, "");

        GetMethod method = new GetMethod();
        method.setURI(uri);
        method.setQueryString(new NameValuePair[] {
                new NameValuePair("s", symbol),
                new NameValuePair("d", String.valueOf(toDate.get(Calendar.MONTH))),
                new NameValuePair("e", String.valueOf(toDate.get(Calendar.DAY_OF_MONTH))),
                new NameValuePair("f", String.valueOf(toDate.get(Calendar.YEAR))),
                new NameValuePair("g", "d"),
                new NameValuePair("a", String.valueOf(fromDate.get(Calendar.MONTH))),
                new NameValuePair("b", String.valueOf(fromDate.get(Calendar.DAY_OF_MONTH))),
                new NameValuePair("c", String.valueOf(fromDate.get(Calendar.YEAR))),
                new NameValuePair("ignore", ".csv"),
        });
        method.setFollowRedirects(true);
        try {
            System.out.println(method.getURI().toString());
        } catch (URIException e) {
            e.printStackTrace();
        }*/

        return null;
    }

    public static Object get1DayHistoryFeedMethod(IFeedIdentifier identifier)  {
        String symbol = getSymbol(identifier);
        
        //https://www.google.com/finance/getprices?i=[PERIOD]&p=[DAYS]d&f=d,o,h,l,c,v&df=cpct&q=[TICKER]
        
        // API key: XXXXXXX
        
        //https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=MSFT&interval=1min&apikey=demo&datatype=csv
        
        //https://iextrading.com/developer/docs/
/*
        String prefix = "/query?function=TIME_SERIES_INTRADAY&symbol=";
        String suffix = "&interval=1min&apikey=demo&datatype=csv";
        URI uri = new URI("https", "www.alphavantage.co", prefix + symbol.toUpperCase()+suffix, "");

        GetMethod method = new GetMethod();
        method.setURI(uri);
        method.setFollowRedirects(true);
*/
        return null;
    }

    public static Object get5DayHistoryFeedMethod(IFeedIdentifier identifier)  {
        /*String symbol = getSymbol(identifier);

        String prefix = "/instrument/1.0/";
        String suffix = "/chartdata;type=quote;range=5d/csv/";
        URI uri = new URI("http", "chartapi.finance.yahoo.com", prefix + symbol.toLowerCase() + suffix, "");

        GetMethod method = new GetMethod();
        method.setURI(uri);
        method.setFollowRedirects(true);
*/
        return null;
    }

    public static Object get1YearHistoryFeedMethod(IFeedIdentifier identifier, int year)  {
     /*   String symbol = getSymbol(identifier);

        String prefix = "/instrument/1.0/";
        String suffix = "/chartdata;type=quote;ys=" + year + ";yz=1/csv/";
        URI uri = new URI("http", "chartapi.finance.yahoo.com", prefix + symbol.toLowerCase() + suffix, "");

        GetMethod method = new GetMethod();
        method.setURI(uri);
        method.setFollowRedirects(true);
*/
        return null;
    }

    public static Object getDividendsHistoryMethod(IFeedIdentifier identifier, Date from, Date to) {
        String symbol = getSymbol(identifier);

        Calendar fromDate = Calendar.getInstance();
        fromDate.setTime(from);

        Calendar toDate = Calendar.getInstance();
        toDate.setTime(to);
/*
        GetMethod method = new GetMethod("http://" + historyFeedHost + "/table.csv");
        method.setQueryString(new NameValuePair[] {
                new NameValuePair("s", symbol),
                new NameValuePair("d", String.valueOf(toDate.get(Calendar.MONTH))),
                new NameValuePair("e", String.valueOf(toDate.get(Calendar.DAY_OF_MONTH))),
                new NameValuePair("f", String.valueOf(toDate.get(Calendar.YEAR))),
                new NameValuePair("g", "v"),
                new NameValuePair("a", String.valueOf(fromDate.get(Calendar.MONTH))),
                new NameValuePair("b", String.valueOf(fromDate.get(Calendar.DAY_OF_MONTH))),
                new NameValuePair("c", String.valueOf(fromDate.get(Calendar.YEAR))),
                new NameValuePair("ignore", ".csv"),
        });
        method.setFollowRedirects(true);
*/
        return null;
    }

    public static URL getRSSNewsFeedForSecurity(ISecurity security) throws MalformedURLException,  NullPointerException {
        IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
        if (identifier == null) {
            return null;
        }

        String symbol = getSymbol(identifier);

       //URI feedUrl = new URI("http://finance.yahoo.com/rss/headline?s=" + symbol, false);

        return null;//new URL(feedUrl.toString());
    }


}
