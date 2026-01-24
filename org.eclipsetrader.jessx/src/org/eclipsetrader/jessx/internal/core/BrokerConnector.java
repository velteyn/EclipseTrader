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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.core.Cash;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.feed.IBookEntry;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.TodayOHL;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderChangeListener;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderRoute;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderStatus;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IOrderValidity;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.trading.Order;
import org.eclipsetrader.core.trading.OrderChangeEvent;
import org.eclipsetrader.core.trading.OrderDelta;
import org.eclipsetrader.jessx.business.BusinessCore;
import org.eclipsetrader.jessx.business.Deal;
import org.eclipsetrader.jessx.business.JessxTradeHistory;
import org.eclipsetrader.jessx.business.PlayerType;
import org.eclipsetrader.jessx.business.Scenario;
import org.eclipsetrader.jessx.client.ClientCore;
import org.eclipsetrader.jessx.client.event.ConnectionListener;
import org.eclipsetrader.jessx.client.event.NetworkListener;
import org.eclipsetrader.jessx.internal.JessxActivator;
import org.eclipsetrader.jessx.internal.core.connector.StreamingConnector;
import org.eclipsetrader.jessx.internal.ui.StatusLineContributionItem;
import org.eclipsetrader.jessx.server.GeneralParametersLocal;
import org.eclipsetrader.jessx.server.Server;
import org.eclipsetrader.jessx.server.ServerStateListener;
import org.eclipsetrader.jessx.server.net.ClientConnectionPoint;
import org.eclipsetrader.jessx.server.net.NetworkCore;
import org.eclipsetrader.jessx.server.net.Player;
import org.eclipsetrader.jessx.utils.gui.MessageTimer;
import org.jdom.Document;
import org.jdom.Element;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class BrokerConnector implements IBroker, IExecutableExtension, IExecutableExtensionFactory, Runnable, ConnectionListener, NetworkListener, ServerStateListener {

	public static final IOrderRoute Immediate = new OrderRoute("1", "immed"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final IOrderRoute MTA = new OrderRoute("2", "MTA"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final IOrderRoute CloseMTA = new OrderRoute("4", "clos-MTA"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final IOrderRoute AfterHours = new OrderRoute("5", "AfterHours"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final IOrderRoute Open = new OrderRoute("7", "open//"); //$NON-NLS-1$ //$NON-NLS-2$

	private static BrokerConnector instance;

	private String id;
	private String name;
	private String server = "213.92.13.4"; //$NON-NLS-1$
	private int port = 1080;

	private Account account;
	Set<OrderMonitor> orders = new HashSet<OrderMonitor>();
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	private NumberFormat amountParser = NumberFormat.getInstance(Locale.ITALY);
	private NumberFormat amountFormatter = NumberFormat.getInstance();

	private SocketChannel socketChannel;
	private Thread thread;
	private Log logger = LogFactory.getLog(getClass());
	public static final IOrderValidity Valid30Days = new OrderValidity("30days", Messages.BrokerConnector_30Days); //$NON-NLS-1$
	private Server srv;

	public BrokerConnector() {
		amountFormatter.setMinimumFractionDigits(2);
		amountFormatter.setMaximumFractionDigits(2);
		amountFormatter.setGroupingUsed(true);

		account = new Account("JESSX", "JESSX Account", new Cash(0.0, Currency.getInstance("USD")));
	}

	public static BrokerConnector getInstance() {
		if (instance == null) {
			instance = new BrokerConnector();
		}
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org
	 * .eclipse.core.runtime.IConfigurationElement, java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		id = config.getAttribute("id"); //$NON-NLS-1$
		name = config.getAttribute("name"); //$NON-NLS-1$
		account = new Account(getId(), getName(), new Cash(100000.0, Currency.getInstance("USD")));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IExecutableExtensionFactory#create()
	 */
	@Override
	public Object create() throws CoreException {
		if (instance == null) {
			instance = this;
		} else {
			instance.id = id;
			instance.name = name;
			if (instance.account == null) {
				instance.account = new Account(instance.getId(), instance.getName(), new Cash(100000.0, Currency.getInstance("USD")));
			}
		}
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipsetrader.core.trading.IBroker#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipsetrader.core.trading.IBroker#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipsetrader.core.trading.IBroker#connect()
	 */
	@Override
	public void connect() {

		if (srv != null && Server.getServerState() == Server.SERVER_STATE_ONLINE) {
			logger.info("JESSX Server is already online. Skipping duplicate start.");
			return;
		}

		try {
			Bundle bundle = Platform.getBundle(JessxActivator.PLUGIN_ID);
			File stateLocation = Platform.getStateLocation(bundle).toFile();
			File file = new File(stateLocation, "default.xml");
			if (file.exists()) {
				try (InputStream is = new FileInputStream(file)) {
					srv = new Server(is, false);
					srv.addServerStateListener(this);
					srv.startServer();
				}
			} else {
				Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, "Default scenario file not found. The plugin activator should have created it.");
				Platform.getLog(bundle).log(status);
			}
		} catch (Exception e) {
			Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, "Error loading default scenario", e);
			Bundle bundle = Platform.getBundle(JessxActivator.PLUGIN_ID);
			if (bundle != null) {
				Platform.getLog(bundle).log(status);
			} else {
				e.printStackTrace();
			}
		}

		if (thread == null || !thread.isAlive()) {
			thread = new Thread(this, getName() + " - Orders Monitor"); //$NON-NLS-1$
			thread.setDaemon(true);
			logger.info("Starting " + thread.getName()); //$NON-NLS-1$
			thread.start();
		}

		ClientCore.addConnectionListener(this);
		ClientCore.addNetworkListener(this, "Portfolio");
		ClientCore.addNetworkListener(this, "OrderBook");
		ClientCore.addNetworkListener(this, "Trade");
		ClientCore.addNetworkListener(this, "Deal");
		ClientCore.addNetworkListener(this, "Quote");
		ClientCore.addNetworkListener(this, "TodayOHL");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipsetrader.core.trading.IBroker#disconnect()
	 */

	@Override
	public void disconnect() {
		logger.info("Broker disconnecting, stopping StreamingConnector.");
		StreamingConnector.getInstance().stop();

		if (srv != null && Server.getServerState() == Server.SERVER_STATE_ONLINE) {
			logger.info("Stopping JESSX Server...");
			srv.setServerState(Server.SERVER_STATE_OFFLINE);
		}

		if (srv != null && Server.getServerState() == Server.SERVER_STATE_ONLINE) {
			logger.info("Stopping JESSX Server...");
			srv.setServerState(Server.SERVER_STATE_OFFLINE);
		}

		// TODO qua c'� da spegnere il server di JESSX (un bel kill e tutto si
		// risolve)
		if (thread != null) {
			try {
				if (socketChannel != null) {
					socketChannel.close();
				}
			} catch (IOException e) {
				// Do nothing
			}
			try {
				thread.interrupt();
				thread.join(30 * 1000);
			} catch (InterruptedException e) {
				// Do nothing
			}
			logger.info("Stopped " + thread.getName()); //$NON-NLS-1$
			thread = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipsetrader.core.trading.IBroker#canTrade(org.eclipsetrader.core
	 * .instruments.ISecurity)
	 */
	@Override
	public boolean canTrade(ISecurity security) {
		if (security == null) {
			return false;
		}
		IFeedIdentifier identifier = security.getIdentifier();
		if (identifier == null) {
			return false;
		}
		// A security is tradable by JESSX if it was assigned a JESSX feed
		// symbol.
		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		return properties != null && properties.getProperty("org.eclipsetrader.jessx.symbol") != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipsetrader.core.trading.IBroker#getSymbolFromSecurity(org.
	 * eclipsetrader.core.instruments.ISecurity)
	 */
	@Override
	public String getSymbolFromSecurity(ISecurity security) {
		if (security == null) {
			return null;
		}
		IFeedIdentifier identifier = security.getIdentifier();
		if (identifier == null) {
			return null;
		}

		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		if (properties != null) {
			String symbol = properties.getProperty("org.eclipsetrader.jessx.symbol");
			if (symbol != null && !symbol.isEmpty()) {
				return symbol;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipsetrader.core.trading.IBroker#getSecurityFromSymbol(java.lang
	 * .String)
	 */
	@Override
	public ISecurity getSecurityFromSymbol(String symbol) {
		ISecurity security = null;

		if (JessxActivator.getDefault() != null) {
			BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
			ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
			if (serviceReference != null) {
				IRepositoryService service = (IRepositoryService) context.getService(serviceReference);

				ISecurity[] securities = service.getSecurities();
				for (int i = 0; i < securities.length; i++) {
					String feedSymbol = getSymbolFromSecurity(securities[i]);
					if (feedSymbol != null && feedSymbol.equals(symbol)) {
						security = securities[i];
						break;
					}
				}

				context.ungetService(serviceReference);
			}
		}

		return security;
	}

	@Override
	public void connectionStateChanged(int newState) {
		if (newState == ClientCore.CONNECTED) {
			logger.info("Client connected to server");
		}
	}

	@Override
	public void objectReceived(Document doc) {
		logger.info("Broker received " + doc.getRootElement().getName());
		if (doc.getRootElement().getName().equals("Warn")) {
			final String message = doc.getRootElement().getText();
			logger.warn("Server Warning: " + message);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					Shell shell = Display.getDefault().getActiveShell();
					if (shell != null) {
						MessageDialog.openWarning(shell, "JessX Server Warning", message);
					}
				}
			});
		}
		if (doc.getRootElement().getName().equals("Portfolio")) {
			logger.info("Broker received Portfolio update: " + doc.getRootElement().toString());
			final List<Position> list = new ArrayList<Position>();
			Element portfolio = doc.getRootElement();

			Double cashUpdate = null;
			if (portfolio.getAttributeValue("cash") != null) {
				cashUpdate = Double.parseDouble(portfolio.getAttributeValue("cash"));
				logger.info("Updated Account Cash: " + cashUpdate);
			}
			final Double finalCash = cashUpdate;

			List<Element> secList = portfolio.getChildren("Owning");
			for (Element sec : secList) {
				String secName = sec.getAttributeValue("asset");
				ISecurity security = getSecurityFromSymbol(secName);
				if (security == null) {
					registerSecurity(secName);
					security = getSecurityFromSymbol(secName);
				}
				if (security != null) {
					String amount = sec.getAttributeValue("qtty");
					if (amount != null) {
						long quantity = Long.parseLong(amount);
						double price = 0.0;
						if (sec.getAttributeValue("price") != null) {
							price = Double.parseDouble(sec.getAttributeValue("price"));
						}
						list.add(new Position(security, quantity, price));
						logger.info("Updated Position: " + secName + ", Qty=" + quantity + ", Price=" + price);
					}
				}
			}

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (finalCash != null) {
						account.setBalance(new Cash(finalCash, Currency.getInstance("USD")));
						account.setDescription("JESSX Account (Cash: " + BrokerConnector.this.amountFormatter.format(finalCash) + ")");
					}
					account.setPositions(list.toArray(new Position[list.size()]));
				}
			});
			
			// Notify listeners about portfolio change (if any logic depends on it, although Account typically doesn't notify views directly)
			// But we can check if we need to refresh anything.
		}
		if (doc.getRootElement().getName().equals("OrderBook")) {
			Element orderBook = doc.getRootElement();
			String institutionName = orderBook.getAttributeValue("institution");
			if (institutionName != null) {
				org.eclipsetrader.jessx.business.Institution institution = BusinessCore.getInstitution(institutionName);
				if (institution != null) {
					String securityName = institution.getAssetName();
					ISecurity security = getSecurityFromSymbol(securityName);
					if (security != null) {
						IFeedIdentifier identifier = security.getIdentifier();
						if (identifier != null) {
							IFeedSubscription2 subscription = StreamingConnector.getInstance().subscribeLevel2(identifier);
							if (subscription != null) {
								List<IBookEntry> bids = new ArrayList<IBookEntry>();
								Element bidElement = orderBook.getChild("Bid");
								if (bidElement != null) {
									for (Object o : bidElement.getChildren("Operation")) {
										Element op = (Element) o;
										Element limitOrder = op.getChild("LimitOrder");
										double price = Double.parseDouble(limitOrder.getAttributeValue("price"));
										long quantity = Long.parseLong(limitOrder.getAttributeValue("quantity"));
										bids.add(new org.eclipsetrader.core.feed.BookEntry(null, price, quantity, 1L, null));
									}
								}

								List<IBookEntry> asks = new ArrayList<IBookEntry>();
								Element askElement = orderBook.getChild("Ask");
								if (askElement != null) {
									for (Object o : askElement.getChildren("Operation")) {
										Element op = (Element) o;
										Element limitOrder = op.getChild("LimitOrder");
										double price = Double.parseDouble(limitOrder.getAttributeValue("price"));
										long quantity = Long.parseLong(limitOrder.getAttributeValue("quantity"));
										asks.add(new org.eclipsetrader.core.feed.BookEntry(null, price, quantity, 1L, null));
									}
								}

								((org.eclipsetrader.jessx.internal.core.connector.FeedSubscription) subscription)
										.setBook(new org.eclipsetrader.core.feed.Book(bids.toArray(new IBookEntry[bids.size()]), asks.toArray(new IBookEntry[asks.size()])));

								if (!bids.isEmpty() || !asks.isEmpty()) {
									double bestBid = bids.isEmpty() ? 0.0 : bids.get(0).getPrice();
									double bestAsk = asks.isEmpty() ? 0.0 : asks.get(0).getPrice();
									long bidSize = bids.isEmpty() ? 0 : bids.get(0).getQuantity();
									long askSize = asks.isEmpty() ? 0 : asks.get(0).getQuantity();

									((org.eclipsetrader.jessx.internal.core.connector.FeedSubscription) subscription)
											.setQuote(new Quote(bestBid, bestAsk, bidSize, askSize));
								}

								StreamingConnector.getInstance().wakeupNotifyThread();
							}
						}
					}
				}
			}
		}
		if (doc.getRootElement().getName().equals("Deal")) {
			Element dealElement = doc.getRootElement();
			Deal deal = new Deal("", 0.0F, 0, 0L, "", "", 0.0F, "", "");
			if (deal.initFromNetworkInput(dealElement)) {
				org.eclipsetrader.jessx.business.Institution institution = BusinessCore.getInstitution(deal.getDealInstitution());
				if (institution != null) {
					String securityName = institution.getAssetName();
					ISecurity security = getSecurityFromSymbol(securityName);
					if (security != null) {
						deal.setSecurity(security);

						IFeedIdentifier identifier = security.getIdentifier();
						if (identifier != null) {
							IFeedSubscription2 subscription = StreamingConnector.getInstance().subscribeLevel2(identifier);
							if (subscription != null) {
								Trade tradeData = new Trade(new Date(deal.getTimestamp()), (double) deal.getDealPrice(), (long) deal.getQuantity(), (long) deal.getQuantity());
								((org.eclipsetrader.jessx.internal.core.connector.FeedSubscription) subscription).setTrade(tradeData);

								BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
								ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
								if (serviceReference != null) {
									IRepositoryService repositoryService = (IRepositoryService) context.getService(serviceReference);
									IHistory history = repositoryService.getHistoryFor(security);
									if (history instanceof org.eclipsetrader.core.feed.History) {
										IOHLC[] bars = history.getOHLC();
										IOHLC last = bars.length > 0 ? bars[bars.length - 1] : null;

										if (last != null && last.getDate().equals(tradeData.getTime())) {
											last = new OHLC(last.getDate(), last.getOpen(), Math.max(last.getHigh(), tradeData.getPrice()), Math.min(last.getLow(), tradeData.getPrice()), tradeData.getPrice(),
													last.getVolume() + tradeData.getSize());
											bars[bars.length - 1] = last;
										} else {
											IOHLC[] newBars = new IOHLC[bars.length + 1];
											System.arraycopy(bars, 0, newBars, 0, bars.length);
											newBars[bars.length] = new OHLC(tradeData.getTime(), tradeData.getPrice(), tradeData.getPrice(), tradeData.getPrice(), tradeData.getPrice(), tradeData.getSize());
											bars = newBars;
										}
										((org.eclipsetrader.core.feed.History) history).setOHLC(bars);
									}
									context.ungetService(serviceReference);
								}

								StreamingConnector.getInstance().wakeupNotifyThread();
							}
						}
						JessxTradeHistory.saveDeal(deal);
					}
				}
			}
		}
		if (doc.getRootElement().getName().equals("Trade")) {
			Element trade = doc.getRootElement();
			String securityName = trade.getAttributeValue("security");
			ISecurity security = getSecurityFromSymbol(securityName);
			if (security != null) {
				IFeedIdentifier identifier = security.getIdentifier();
				if (identifier != null) {
					IFeedSubscription2 subscription = StreamingConnector.getInstance().subscribeLevel2(identifier);
					if (subscription != null) {
						double price = Double.parseDouble(trade.getAttributeValue("price"));
						long quantity = Long.parseLong(trade.getAttributeValue("quantity"));
						long volume = Long.parseLong(trade.getAttributeValue("volume"));
						Trade tradeData = new Trade(new Date(), price, quantity, volume);
						((org.eclipsetrader.jessx.internal.core.connector.FeedSubscription) subscription).setTrade(tradeData);

						BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
						ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
						if (serviceReference != null) {
							IRepositoryService repositoryService = (IRepositoryService) context.getService(serviceReference);
							IHistory history = repositoryService.getHistoryFor(security);
							if (history instanceof org.eclipsetrader.core.feed.History) {
								IOHLC[] bars = history.getOHLC();
								IOHLC last = bars.length > 0 ? bars[bars.length - 1] : null;

								if (last != null && last.getDate().equals(tradeData.getTime())) {
									last = new OHLC(last.getDate(), last.getOpen(), Math.max(last.getHigh(), tradeData.getPrice()), Math.min(last.getLow(), tradeData.getPrice()), tradeData.getPrice(),
											last.getVolume() + tradeData.getSize());
									bars[bars.length - 1] = last;
								} else {
									IOHLC[] newBars = new IOHLC[bars.length + 1];
									System.arraycopy(bars, 0, newBars, 0, bars.length);
									newBars[bars.length] = new OHLC(tradeData.getTime(), tradeData.getPrice(), tradeData.getPrice(), tradeData.getPrice(), tradeData.getPrice(), tradeData.getSize());
									bars = newBars;
								}
								((org.eclipsetrader.core.feed.History) history).setOHLC(bars);
							}
							context.ungetService(serviceReference);
						}

						StreamingConnector.getInstance().wakeupNotifyThread();
					}
				}
			}
		}
		if (doc.getRootElement().getName().equals("Quote")) {
			Element quote = doc.getRootElement();
			String securityName = quote.getAttributeValue("security");
			ISecurity security = getSecurityFromSymbol(securityName);
			if (security != null) {
				IFeedIdentifier identifier = security.getIdentifier();
				if (identifier != null) {
					IFeedSubscription2 subscription = StreamingConnector.getInstance().subscribeLevel2(identifier);
					if (subscription != null) {
						double bid = Double.parseDouble(quote.getAttributeValue("bid"));
						double ask = Double.parseDouble(quote.getAttributeValue("ask"));
						long bidSize = Long.parseLong(quote.getAttributeValue("bidSize"));
						long askSize = Long.parseLong(quote.getAttributeValue("askSize"));
						((org.eclipsetrader.jessx.internal.core.connector.FeedSubscription) subscription).setQuote(new Quote(bid, ask, bidSize, askSize));
						StreamingConnector.getInstance().wakeupNotifyThread();
					}
				}
			}
		}
		if (doc.getRootElement().getName().equals("TodayOHL")) {
			Element todayOHL = doc.getRootElement();
			String securityName = todayOHL.getAttributeValue("security");
			ISecurity security = getSecurityFromSymbol(securityName);
			if (security != null) {
				IFeedIdentifier identifier = security.getIdentifier();
				if (identifier != null) {
					IFeedSubscription2 subscription = StreamingConnector.getInstance().subscribeLevel2(identifier);
					if (subscription != null) {
						double open = Double.parseDouble(todayOHL.getAttributeValue("open"));
						double high = Double.parseDouble(todayOHL.getAttributeValue("high"));
						double low = Double.parseDouble(todayOHL.getAttributeValue("low"));
						((org.eclipsetrader.jessx.internal.core.connector.FeedSubscription) subscription).setTodayOHL(new TodayOHL(open, high, low));
						StreamingConnector.getInstance().wakeupNotifyThread();
					}
				}
			}
		}
	}

	/**
	 * Public wrapper to register a JESSX security if it doesn't already exist.
	 * Used during plugin startup to pre-register securities from default.xml.
	 * 
	 * @param name
	 *            The asset name to register (e.g., "AAT", "GPLRF", "MSFT",
	 *            "PLS")
	 */
	public void registerSecurityIfNeeded(String name) {
		ISecurity security = getSecurityFromSymbol(name);
		if (security == null) {
			registerSecurity(name);
		}
	}

	private void registerSecurity(final String name) {
		logger.info("########## I'm going to register security " + name);
		BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
		ServiceReference marketServiceReference = context.getServiceReference(IMarketService.class.getName());
		ServiceReference feedServiceReference = context.getServiceReference(org.eclipsetrader.core.feed.IFeedService.class.getName());
		if (serviceReference != null) {
			final IRepositoryService repositoryService = (IRepositoryService) context.getService(serviceReference);
			final IMarketService marketService = (IMarketService) context.getService(marketServiceReference);
			final org.eclipsetrader.core.feed.IFeedService feedService = (org.eclipsetrader.core.feed.IFeedService) context.getService(feedServiceReference);
			try {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						IRepositoryRunnable runnable = new IRepositoryRunnable() {
							@Override
							public IStatus run(IProgressMonitor monitor) throws Exception {
								ISecurity security = repositoryService.getSecurityFromName(name);
								if (security == null) {
									FeedProperties properties = new FeedProperties();
									properties.setProperty("org.eclipsetrader.jessx.symbol", name);
									// Use the actual security name (e.g.,
									// "AAT") as the symbol
									FeedIdentifier identifier = new FeedIdentifier(name, properties);
									security = new Stock(name, identifier, Currency.getInstance("USD"));
									IAdaptable[] adaptables = new IAdaptable[] { (IAdaptable) security, };
									IRepository repository = repositoryService.getRepository("local");
									repositoryService.moveAdaptable(adaptables, repository);
									IMarket jessxMarket = marketService.getMarket("JESSX");
									if (jessxMarket == null) {
										org.eclipsetrader.core.internal.markets.Market newMarket = new org.eclipsetrader.core.internal.markets.Market("JESSX", null);
										if (feedService != null) {
											org.eclipsetrader.core.feed.IFeedConnector connector = feedService.getConnector("org.eclipsetrader.jessx.feed");
											if (connector != null) {
												newMarket.setLiveFeedConnector(connector);
											}
										}
										((org.eclipsetrader.core.internal.markets.MarketService) org.eclipsetrader.core.internal.markets.MarketService.getInstance()).addMarket(newMarket);
										jessxMarket = newMarket;
									}
									if (jessxMarket != null) {
										jessxMarket.addMembers(new ISecurity[] { security });
										logger.info("Added security " + name + " to market: " + jessxMarket.getName());
									}
								}
								return Status.OK_STATUS;
							}
						};
						repositoryService.runInService(runnable, null);
					}
				});
			} finally {
				context.ungetService(serviceReference);
				if (marketServiceReference != null) {
					context.ungetService(marketServiceReference);
				}
				if (feedServiceReference != null) {
					context.ungetService(feedServiceReference);
				}
			}
		}
	}

	@Override
	public void serverStateChanged(int state) {
		if (state == Server.SERVER_STATE_ONLINE) {
			logger.info("JESSX Server is online, starting StreamingConnector.");
			StreamingConnector.getInstance().start();
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						logger.info("JessX-Setup: Server is online. Waiting for connection point to be ready...");
						if (!ClientConnectionPoint.serverReadyLatch.await(10, TimeUnit.SECONDS)) {
							logger.error("JessX-Setup: Timed out waiting for server connection point. Setup aborted.");
							return;
						}
						logger.info("JessX-Setup: Connection point is ready. Connecting ThePlayer...");

						// 1. Connect ThePlayer FIRST
						ClientCore.connecToServer("localhost", "ThePlayer", "he-man");

						// 2. Wait for ThePlayer to appear in the NetworkCore
						Player thePlayer = null;
						for (int i = 0; i < 100; i++) { // Wait up to 10 seconds
							thePlayer = NetworkCore.getPlayer("ThePlayer");
							if (thePlayer != null) {
								break;
							}
							Thread.sleep(100);
						}
						if (thePlayer == null) {
							String msg = "JessX-Setup: Timed out waiting for ThePlayer to connect. Setup aborted.";
							logger.error(msg);
							JessxActivator.log(msg);
							return;
						}
						JessxActivator.log("JessX-Setup: ThePlayer is connected. Now loading bots...");

						// 3. Load bots ONLY after ThePlayer is connected
						srv.loadBots();

						// 4. Wait for all bots to connect
						int expectedTotalPlayers = 42; // 41 bots + 1 ThePlayer
						JessxActivator.log("JessX-Setup: Waiting for all " + (expectedTotalPlayers - 1) + " bots to connect...");
						for (int i = 0; i < 200; i++) { // Wait up to 20 seconds
							if (NetworkCore.getPlayerList().size() >= expectedTotalPlayers) {
								break;
							}
							Thread.sleep(100);
						}

						if (NetworkCore.getPlayerList().size() < expectedTotalPlayers) {
							String msg = "JessX-Setup: Timed out waiting for all bots to connect. " + NetworkCore.getPlayerList().size() + "/" + expectedTotalPlayers + " connected. Aborting.";
							logger.error(msg);
							JessxActivator.log(msg);
							return;
						}
						JessxActivator.log("JessX-Setup: All " + NetworkCore.getPlayerList().size() + " players are connected.");

						// 5. Initialize General Parameters
						JessxActivator.log("JessX-Setup: Initializing general parameters...");
						GeneralParametersLocal generalParams = new GeneralParametersLocal();
						generalParams.initializeGeneralParameters();
						BusinessCore.setGeneralParameters(generalParams);
						JessxActivator.log("JessX-Setup: General parameters initialized and set in BusinessCore.");

						// 6. Assign categories and start experiment (with retry
						// logic)
						JessxActivator.log("JessX-Setup: All players connected. Assigning categories and attempting to start experiment...");

						Scenario scn = BusinessCore.getScenario();
						Map plTypes = scn.getPlayerTypes();
						List<PlayerType> categories = new ArrayList<PlayerType>(plTypes.values());
						Random random = new Random();

						boolean experimentStarted = false;
						for (int i = 0; i < 100; i++) { // Poll for up to 10
														// seconds
							// On each attempt, snapshot the current player list
							// and assign categories to
							// any player that is missing one.
							// This ensures that even if a player's state update
							// was delayed, it will be
							// retried.
							List<Player> playerSnapshot = new ArrayList<Player>(NetworkCore.getPlayerList().values());
							for (Player player : playerSnapshot) {
								if (player.getPlayerCategory() == null || player.getPlayerCategory().isEmpty()) {
									PlayerType assignedCategory;
									if (player.getLogin().equals("ThePlayer")) {
										assignedCategory = categories.get(0);
									} else {
										assignedCategory = categories.get(random.nextInt(categories.size()));
									}
									player.setPlayerCategory(assignedCategory.getPlayerTypeName());
									logger.info(String.format("Assigned category '%s' to player '%s'", player.getPlayerCategory(), player.getLogin()));
								}
							}

							// Try to start the experiment.
							if (NetworkCore.getExperimentManager().beginExperiment()) {
								new MessageTimer((Vector) BusinessCore.getScenario().getListInformation().clone()).start();
								JessxActivator.log("JessX-Setup: Experiment started successfully.");
								experimentStarted = true;
								break; // Exit the retry loop on success
							}

							// If it failed, wait a moment before the next
							// retry.
							Thread.sleep(100);
						}

						if (!experimentStarted) {
							String msg = "JessX-Setup: Failed to start experiment after multiple retries. Preconditions not met. Check server logs.";
							logger.error(msg);
							JessxActivator.log(msg);
						}
					} catch (Exception e) {
						logger.error("Error in JessX setup thread", e);
						JessxActivator.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, JessxActivator.PLUGIN_ID, "Error in JessX setup thread", e));
					}
				}
			}, "JessX-Setup").start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipsetrader.core.trading.IBroker#prepareOrder(org.eclipsetrader
	 * .core.trading.IOrder)
	 */
	@Override
	public IOrderMonitor prepareOrder(IOrder order) throws BrokerException {
		if (order.getType() != IOrderType.Limit && order.getType() != IOrderType.Market) {
			throw new BrokerException(Messages.BrokerConnector_InvalidOrderType);
		}
		if (order.getSide() != IOrderSide.Buy && order.getSide() != IOrderSide.Sell) {
			throw new BrokerException(Messages.BrokerConnector_InvalidOrderSide);
		}
		if (order.getValidity() != IOrderValidity.Day && order.getValidity() != Valid30Days) {
			throw new BrokerException(Messages.BrokerConnector_InvalidOrderValidity);
		}

		return new OrderMonitor(this, order);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipsetrader.core.trading.IBroker#getAllowedSides()
	 */
	@Override
	public IOrderSide[] getAllowedSides() {
		return new IOrderSide[] { IOrderSide.Buy, IOrderSide.Sell, };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipsetrader.core.trading.IBroker#getAllowedTypes()
	 */
	@Override
	public IOrderType[] getAllowedTypes() {
		return new IOrderType[] { IOrderType.Limit, IOrderType.Market, };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipsetrader.core.trading.IBroker#getAllowedValidity()
	 */
	@Override
	public IOrderValidity[] getAllowedValidity() {
		return new IOrderValidity[] { IOrderValidity.Day, Valid30Days, };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipsetrader.core.trading.IBroker#getAllowedRoutes()
	 */
	@Override
	public IOrderRoute[] getAllowedRoutes() {
		if (BusinessCore.getInstitutions() == null) {
			return new IOrderRoute[0];
		}

		List<IOrderRoute> routes = new ArrayList<IOrderRoute>();
		
		// Filter based on PlayerType permissions
		try {
			String login = ClientCore.getLogin();
			if (login != null) {
				Player player = NetworkCore.getPlayer(login);
				if (player != null && player.getPlayerCategory() != null) {
					Scenario scenario = BusinessCore.getScenario();
					if (scenario != null) {
						PlayerType pt = scenario.getPlayerType(player.getPlayerCategory());
						if (pt != null) {
							Vector<?> allowed = pt.getInstitutionsWherePlaying();
							if (allowed != null) {
								for (Object obj : allowed) {
									String instName = (String) obj;
									if (BusinessCore.getInstitution(instName) != null) {
										routes.add(new OrderRoute(instName, instName));
									}
								}
								// If we found allowed routes, return them. 
								// If the list is empty, it means the player has no access, so returning empty is correct.
								return routes.toArray(new IOrderRoute[routes.size()]);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error determining allowed routes for player", e);
		}

		// Fallback: return all institutions if we can't determine player permissions
		for (Object institution : BusinessCore.getInstitutions().values()) {
			routes.add(new OrderRoute(((org.eclipsetrader.jessx.business.Institution) institution).getName(), ((org.eclipsetrader.jessx.business.Institution) institution).getName()));
		}

		return routes.toArray(new IOrderRoute[routes.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipsetrader.core.trading.IBroker#getOrders()
	 */
	@Override
	public IOrderMonitor[] getOrders() {
		synchronized (orders) {
			return orders.toArray(new IOrderMonitor[orders.size()]);
		}
	}

	private static final String LOGIN = "21"; //$NON-NLS-1$
	private static final String UNKNOWN55 = "55"; //$NON-NLS-1$
	private static final String UNKNOWN70 = "70"; //$NON-NLS-1$
	private static final String HEARTBEAT = "40"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		Selector socketSelector;
		ByteBuffer dst = ByteBuffer.wrap(new byte[2048]);
		List<Position> positions = new ArrayList<Position>();

		try {
			// Create a non-blocking socket channel
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);

			socketChannel.socket().setReceiveBufferSize(32768);
			socketChannel.socket().setSoLinger(true, 1);
			socketChannel.socket().setSoTimeout(0x15f90);
			socketChannel.socket().setReuseAddress(true);

			// Kick off connection establishment
			socketChannel.connect(new InetSocketAddress(server, port));

			// Create a new selector
			socketSelector = SelectorProvider.provider().openSelector();

			// Register the server socket channel, indicating an interest in
			// accepting new connections
			socketChannel.register(socketSelector, SelectionKey.OP_READ | SelectionKey.OP_CONNECT);
		} catch (Exception e) {
			Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error connecting to orders monitor", //$NON-NLS-1$
					e);
			JessxActivator.log(status);
			return;
		}

		for (;;) {
			try {
				if (socketSelector.select(30 * 1000) == 0) {
					logger.trace(">" + HEARTBEAT); //$NON-NLS-1$
					socketChannel.write(ByteBuffer.wrap(new String(HEARTBEAT + "\r\n").getBytes())); //$NON-NLS-1$
				}
			} catch (Exception e) {
				break;
			}

			// Iterate over the set of keys for which events are available
			Iterator<SelectionKey> selectedKeys = socketSelector.selectedKeys().iterator();
			while (selectedKeys.hasNext()) {
				SelectionKey key = selectedKeys.next();
				selectedKeys.remove();

				if (!key.isValid()) {
					continue;
				}

				try {
					// Check what event is available and deal with it
					if (key.isConnectable()) {
						// Finish the connection. If the connection operation
						// failed
						// this will raise an IOException.
						try {
							socketChannel.finishConnect();
						} catch (IOException e) {
							// Cancel the channel's registration with our
							// selector
							key.cancel();
							return;
						}

						// Register an interest in writing on this channel
						key.interestOps(SelectionKey.OP_WRITE);
					}
					if (key.isWritable()) {
						logger.trace(">" + LOGIN + WebConnector.getInstance().getUser()); //$NON-NLS-1$
						socketChannel.write(ByteBuffer.wrap(new String(LOGIN + WebConnector.getInstance().getUser() + "\r\n").getBytes())); //$NON-NLS-1$

						// Register an interest in reading on this channel
						key.interestOps(SelectionKey.OP_READ);
					}
					if (key.isReadable()) {
						dst.clear();
						int readed = socketChannel.read(dst);
						if (readed > 0) {
							String[] s = new String(dst.array(), 0, readed).split("\r\n"); //$NON-NLS-1$
							for (int i = 0; i < s.length; i++) {
								logger.trace("<" + s[i]); //$NON-NLS-1$

								if (s[i].endsWith(";" + WebConnector.getInstance().getUser() + ";")) { //$NON-NLS-1$ //$NON-NLS-2$
									logger.trace(">" + UNKNOWN70); //$NON-NLS-1$
									socketChannel.write(ByteBuffer.wrap(new String(UNKNOWN70 + "\r\n").getBytes())); //$NON-NLS-1$
									logger.trace(">" + UNKNOWN55); //$NON-NLS-1$
									socketChannel.write(ByteBuffer.wrap(new String(UNKNOWN55 + "\r\n").getBytes())); //$NON-NLS-1$
								}

								if (s[i].indexOf(";6;5;") != -1 || s[i].indexOf(";8;0;") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
									try {
										OrderMonitor monitor = parseOrderLine(s[i]);

										OrderDelta[] delta;
										synchronized (orders) {
											if (!orders.contains(monitor)) {
												orders.add(monitor);
												delta = new OrderDelta[] { new OrderDelta(OrderDelta.KIND_ADDED, monitor) };
											} else {
												delta = new OrderDelta[] { new OrderDelta(OrderDelta.KIND_UPDATED, monitor) };
											}
										}
										fireUpdateNotifications(delta);
									} catch (ParseException e) {
										Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error parsing line: " + s[i], e); //$NON-NLS-1$
										JessxActivator.log(status);
									}
								}
								if (s[i].indexOf(";6;0;") != -1) { //$NON-NLS-1$
									updateStatusLine(s[i]);
								}
								if (s[i].indexOf(";7;0;") != -1) { //$NON-NLS-1$
									try {
										positions.add(parsePositionLine(s[i]));
									} catch (Exception e) {
										Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error parsing line: " + s[i], e); //$NON-NLS-1$
										JessxActivator.log(status);
									}
								}
								if (s[i].indexOf(";7;9;") != -1) { //$NON-NLS-1$
									account.setPositions(positions.toArray(new Position[positions.size()]));
									positions.clear();
								}
							}
						}
					}
				} catch (Exception e) {
					Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Connection error", e); //$NON-NLS-1$
					JessxActivator.log(status);
				}
			}
		}
	}

	private static final int IDX_ID = 3;
	private static final int IDX_STATUS = 4;
	private static final int IDX_SYMBOL = 5;
	private static final int IDX_PF_QUANTITY = 9;
	private static final int IDX_AVERAGE_PRICE = 10;
	private static final int IDX_QUANTITY = 15;
	private static final int IDX_PRICE = 16;
	private static final int IDX_SIDE = 18;
	private static final int IDX_DATE = 19;
	private static final int IDX_TIME = 20;
	private static final int IDX_FILLED_QUANTITY = 25;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd HHmmss"); //$NON-NLS-1$
	private NumberFormat numberFormatter = NumberFormat.getInstance(Locale.US);

	protected OrderMonitor parseOrderLine(String line) throws ParseException {
		String[] item = line.split(";"); //$NON-NLS-1$

		OrderMonitor tracker = null;
		synchronized (orders) {
			for (OrderMonitor m : orders) {
				if (item[IDX_ID].equals(m.getId())) {
					tracker = m;
					break;
				}
			}
			if (tracker == null) {
				for (OrderMonitor m : orders) {
					if (m.getId() == null && getSymbolFromSecurity(m.getOrder().getSecurity()).equals(item[IDX_SYMBOL])) {
						tracker = m;
						tracker.setId(item[IDX_ID]);
						break;
					}
				}
			}
		}
		if (tracker == null) {
			Long quantity = !item[IDX_QUANTITY].equals("") ? Long.parseLong(item[IDX_QUANTITY]) : null; //$NON-NLS-1$
			if (quantity == null && item.length > IDX_FILLED_QUANTITY && !item[IDX_FILLED_QUANTITY].equals("")) { //$NON-NLS-1$
				try {
					quantity = numberFormatter.parse(item[IDX_FILLED_QUANTITY]).longValue();
				} catch (Exception e) {
				}
			}
			Order order = new Order(null, !item[IDX_PRICE].equals("") ? IOrderType.Limit : IOrderType.Market, //$NON-NLS-1$
					item[IDX_SIDE].equalsIgnoreCase("V") ? IOrderSide.Sell : IOrderSide.Buy, //$NON-NLS-1$
					getSecurityFromSymbol(item[IDX_SYMBOL]), quantity, !item[IDX_PRICE].equals("") ? numberFormatter.parse(item[IDX_PRICE]).doubleValue() : null); //$NON-NLS-1$
			tracker = new OrderMonitor(BrokerConnector.getInstance(), order);
			tracker.setId(item[IDX_ID]);
		}

		IOrder order = tracker.getOrder();

		try {
			Method classMethod = order.getClass().getMethod("setDate", Date.class); //$NON-NLS-1$
			if (classMethod != null) {
				if (item[IDX_TIME].length() < 6) {
					item[IDX_TIME] = "0" + item[IDX_TIME]; //$NON-NLS-1$
				}
				classMethod.invoke(order, dateFormatter.parse(item[IDX_DATE] + " " + item[IDX_TIME])); //$NON-NLS-1$
			}
		} catch (Exception e) {
		}

		if (item[IDX_STATUS].equals("e") || item[IDX_STATUS].equals("e ")) { //$NON-NLS-1$ //$NON-NLS-2$
			if (!item[IDX_AVERAGE_PRICE].equals("")) { //$NON-NLS-1$
				try {
					tracker.setAveragePrice(numberFormatter.parse(item[IDX_AVERAGE_PRICE]).doubleValue());
				} catch (Exception e) {
				}
			} else {
				tracker.setAveragePrice(numberFormatter.parse(item[IDX_PRICE]).doubleValue());
			}

			if (!item[IDX_FILLED_QUANTITY].equals("")) { //$NON-NLS-1$
				try {
					tracker.setFilledQuantity(numberFormatter.parse(item[IDX_FILLED_QUANTITY]).longValue());
				} catch (Exception e) {
				}
			}
		}

		IOrderStatus status = tracker.getStatus();
		if (item[IDX_STATUS].equals("e") || item[IDX_STATUS].equals("e ")) { //$NON-NLS-1$ //$NON-NLS-2$
			status = IOrderStatus.Filled;
		} else if (item[IDX_STATUS].equals("n") || item[IDX_STATUS].equals("n ") || item[IDX_STATUS].equals("j")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			status = IOrderStatus.PendingNew;
		} else if (item[IDX_STATUS].equals("zA") || item[IDX_STATUS].equals("z ")) { //$NON-NLS-1$ //$NON-NLS-2$
			status = IOrderStatus.Canceled;
		} else if (item[IDX_STATUS].equals("na")) { //$NON-NLS-1$
			status = IOrderStatus.PendingCancel;
		} else {
			status = IOrderStatus.PendingNew;
		}

		if (status != IOrderStatus.Canceled) {
			if (tracker.getFilledQuantity() != null && !tracker.getFilledQuantity().equals(order.getQuantity())) {
				status = IOrderStatus.Partial;
			}
		}

		if ((status == IOrderStatus.Filled || status == IOrderStatus.Canceled || status == IOrderStatus.Rejected) && tracker.getStatus() != status) {
			tracker.setStatus(status);

			if (logger.isInfoEnabled()) {
				StringBuilder sb = new StringBuilder();
				if (status == IOrderStatus.Filled) {
					sb.append("Order Filled:");
				} else if (status == IOrderStatus.Canceled) {
					sb.append("Order Canceled:");
				} else if (status == IOrderStatus.Rejected) {
					sb.append("Order Rejected:");
				}
				if (tracker.getId() != null) {
					sb.append(" id=" + tracker.getId() + ",");
				}
				sb.append(" instrument=" + tracker.getOrder().getSecurity().getName());
				sb.append(", type=" + tracker.getOrder().getType());
				sb.append(", side=" + tracker.getOrder().getSide());
				sb.append(", qty=" + tracker.getOrder().getQuantity());
				if (tracker.getOrder().getPrice() != null) {
					sb.append(", price=" + tracker.getOrder().getPrice());
				}
				if (tracker.getOrder().getReference() != null) {
					sb.append(", reference=" + tracker.getOrder().getReference());
				}
				logger.info(sb.toString());
			}

			tracker.fireOrderCompletedEvent();
		} else {
			tracker.setStatus(status);
		}

		return tracker;
	}

	protected Position parsePositionLine(String line) {
		String[] item = line.split(";"); //$NON-NLS-1$

		ISecurity security = getSecurityFromSymbol(item[IDX_SYMBOL]);
		Long quantity = Long.parseLong(item[IDX_PF_QUANTITY]);
		Double price = Double.parseDouble(item[IDX_AVERAGE_PRICE]);

		return new Position(security, quantity, price);
	}

	protected void updateStatusLine(String line) {
		BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(IStatusLineManager.class.getName());
		if (serviceReference != null) {
			IStatusLineManager statusLine = (IStatusLineManager) context.getService(serviceReference);
			final StatusLineContributionItem contributionItem = (StatusLineContributionItem) statusLine.find(JessxActivator.PLUGIN_ID);
			try {
				String[] item = line.split("\\;"); //$NON-NLS-1$
				final double liquidity = amountParser.parse(item[3]).doubleValue();
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						contributionItem.setText(Messages.BrokerConnector_Liquidity + amountFormatter.format(liquidity));
					}
				});
			} catch (Exception e) {
				Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error parsing line: " + line, //$NON-NLS-1$
						e);
				JessxActivator.log(status);
			}
			context.ungetService(serviceReference);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipsetrader.core.trading.IBroker#addOrderChangeListener(org.
	 * eclipsetrader.core.trading.IOrderChangeListener)
	 */
	@Override
	public void addOrderChangeListener(IOrderChangeListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipsetrader.core.trading.IBroker#removeOrderChangeListener(org.
	 * eclipsetrader.core.trading.IOrderChangeListener)
	 */
	@Override
	public void removeOrderChangeListener(IOrderChangeListener listener) {
		listeners.remove(listener);
	}

	protected void fireUpdateNotifications(OrderDelta[] deltas) {
		if (deltas.length != 0) {
			OrderChangeEvent event = new OrderChangeEvent(this, deltas);
			Object[] l = listeners.getListeners();
			for (int i = 0; i < l.length; i++) {
				try {
					((IOrderChangeListener) l[i]).orderChanged(event);
				} catch (Throwable e) {
					Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error running listener", e); //$NON-NLS-1$
					JessxActivator.log(status);
				}
			}
		}
	}

	public void addWithNotification(OrderMonitor orderMonitor) {
		synchronized (orders) {
			if (!orders.contains(orderMonitor)) {
				orders.add(orderMonitor);
			}
		}
		fireUpdateNotifications(new OrderDelta[] { new OrderDelta(OrderDelta.KIND_ADDED, orderMonitor), });
	}

	public void sendOrder(final IOrder order) {
		try {
			// Validate that the selected Institution (Route) trades the requested Security
			String institutionName = order.getRoute().getId();
			org.eclipsetrader.jessx.business.Institution institution = BusinessCore.getInstitution(institutionName);
			if (institution != null) {
				String quotedAsset = institution.getAssetName();
				String orderSymbol = getSymbolFromSecurity(order.getSecurity());
				if (orderSymbol != null && !orderSymbol.equals(quotedAsset)) {
					String msg = "Invalid Route: Institution " + institutionName + " trades " + quotedAsset + ", but order is for " + orderSymbol;
					logger.error(msg);
					Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, msg);
					JessxActivator.log(status);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							Shell shell = Display.getDefault().getActiveShell();
							MessageDialog.openError(shell, "Order Routing Error", 
								"Cannot send order to " + institutionName + ".\n" +
								"This market only trades " + quotedAsset + ", but your order is for " + orderSymbol + ".");
						}
					});
					return;
				}
			}

			// Validate Portfolio for Sell Orders
			if (order.getSide() == IOrderSide.Sell) {
				long quantityOwned = 0;
				if (account.getPositions() != null) {
					for (IPosition p : account.getPositions()) {
						if (p.getSecurity().equals(order.getSecurity())) {
							quantityOwned = p.getQuantity();
							break;
						}
					}
				}
				
				if (quantityOwned < order.getQuantity()) {
					final long owned = quantityOwned;
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							Shell shell = Display.getDefault().getActiveShell();
							MessageDialog.openError(shell, "Order Error", 
								"Cannot sell " + order.getQuantity() + " shares of " + order.getSecurity().getName() + ".\n" +
								"You only own " + owned + " shares.");
						}
					});
					return; // Abort order
				}
			}

			Element root = new Element("Operation");
			root.setAttribute("emitter", ClientCore.getLogin());
			root.setAttribute("institution", order.getRoute().getId());

			Element orderElement = new Element("Order");
			orderElement.setAttribute("id", String.valueOf(new Random().nextInt()));
			orderElement.setAttribute("side", order.getSide() == IOrderSide.Buy ? "1" : "0");
			orderElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
			root.addContent(orderElement);

			if (order.getType() == IOrderType.Limit) {
				root.setAttribute("type", "LimitOrder");
				Element limitOrder = new Element("LimitOrder");
				limitOrder.setAttribute("price", String.valueOf(order.getPrice()));
				limitOrder.setAttribute("quantity", String.valueOf(order.getQuantity()));
				root.addContent(limitOrder);
			} else if (order.getType() == IOrderType.Market) {
				root.setAttribute("type", "MarketOrder");
				Element marketOrder = new Element("MarketOrder");
				marketOrder.setAttribute("quantity", String.valueOf(order.getQuantity()));
				root.addContent(marketOrder);
			}

			if (root.getAttribute("type") != null) {
				org.eclipsetrader.jessx.business.Operation op = org.eclipsetrader.jessx.business.Operation.initOperationFromXml(root);
				ClientCore.executeOperation(op);
			}
		} catch (Exception e) {
			logger.error("Error sending order", e);
		}
	}

	public void cancelOrder(IOrderMonitor monitor) {
		try {
			Element root = new Element("Operation");
			root.setAttribute("type", "DeleteOrder");
			root.setAttribute("emitter", ClientCore.getLogin());
			root.setAttribute("institution", monitor.getOrder().getRoute().getId());

			Element deleteOrder = new Element("DeleteOrder");
			deleteOrder.setAttribute("orderId", monitor.getId());
			root.addContent(deleteOrder);

			org.eclipsetrader.jessx.business.Operation op = org.eclipsetrader.jessx.business.Operation.initOperationFromXml(root);
			ClientCore.executeOperation(op);
		} catch (Exception e) {
			logger.error("Error sending order cancellation", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipsetrader.core.trading.IBroker#getAccounts()
	 */
	@Override
	public IAccount[] getAccounts() {
		return new IAccount[] { account, };
	}
}