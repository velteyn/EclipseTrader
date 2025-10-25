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

import java.io.IOException;
import java.io.InputStream;
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
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.feed.IBookEntry;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.TodayOHL;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
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
import org.eclipsetrader.core.trading.Order;
import org.eclipsetrader.core.trading.OrderChangeEvent;
import org.eclipsetrader.core.trading.OrderDelta;
import org.eclipsetrader.jessx.business.BusinessCore;
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

		// EDOZ TODO vedi quello di directa , qua facciamo partire il server
		// incvece di "collegarci"
		// e i read che facciamo partire � il server i JESSX !

        try {
            URL url = JessxActivator.getDefault().getBundle().getEntry("src/org/eclipsetrader/jessx/utils/default.xml");
            InputStream is = url.openStream();
            srv = new Server(is, false);
            srv.addServerStateListener(this);
            srv.startServer();
        }
        catch (Exception e) {
            Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, "Error loading default scenario", e);
            JessxActivator.log(status);
        }

		if (thread == null || !thread.isAlive()) {
			thread = new Thread(this, getName() + " - Orders Monitor"); //$NON-NLS-1$
			logger.info("Starting " + thread.getName()); //$NON-NLS-1$
			thread.start();
		}

        ClientCore.addConnectionListener(this);
        ClientCore.addNetworkListener(this, "Portfolio");
        ClientCore.addNetworkListener(this, "OrderBook");
        ClientCore.addNetworkListener(this, "Trade");
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

		// TODO !! Cazzo � sta rob a ?
		IFeedIdentifier identifier = security.getIdentifier();
		if (identifier == null) {
			return false;
		}

		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		if (properties != null) {
			for (int p = 0; p < WebConnector.PROPERTIES.length; p++) {
				if (properties.getProperty(WebConnector.PROPERTIES[p]) != null) {
					return true;
				}
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipsetrader.core.trading.IBroker#getSymbolFromSecurity(org.
	 * eclipsetrader.core.instruments.ISecurity)
	 */
	@Override
	public String getSymbolFromSecurity(ISecurity security) {
		IFeedIdentifier identifier = security.getIdentifier();
		if (identifier == null) {
			return null;
		}

		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		if (properties != null) {
            String symbol = properties.getProperty("org.eclipsetrader.jessx.symbol");
            if (symbol != null) {
                return symbol;
            }
			for (int p = 0; p < WebConnector.PROPERTIES.length; p++) {
				if (properties.getProperty(WebConnector.PROPERTIES[p]) != null) {
					return properties.getProperty(WebConnector.PROPERTIES[p]);
				}
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
		if (doc.getRootElement().getName().equals("Portfolio")) {
			Element portfolio = doc.getRootElement();
			List<Element> secList = portfolio.getChildren("Owning");
			for (Element sec : secList) {
				String secName = sec.getAttributeValue("asset");
				registerSecurity(secName);
			}
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

                                ((org.eclipsetrader.jessx.internal.core.connector.FeedSubscription)subscription).setBook(new org.eclipsetrader.core.feed.Book(bids.toArray(new IBookEntry[bids.size()]), asks.toArray(new IBookEntry[asks.size()])));
                                StreamingConnector.getInstance().wakeupNotifyThread();
                            }
		                }
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
                        ((org.eclipsetrader.jessx.internal.core.connector.FeedSubscription)subscription).setTrade(tradeData);

                        BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
                        ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
                        if (serviceReference != null) {
                            IRepositoryService repositoryService = (IRepositoryService) context.getService(serviceReference);
                            IHistory history = repositoryService.getHistoryFor(security);
                            if (history instanceof org.eclipsetrader.core.feed.History) {
                                IOHLC[] bars = history.getOHLC();
                                IOHLC last = bars.length > 0 ? bars[bars.length - 1] : null;

                                if (last != null && last.getDate().equals(tradeData.getTime())) {
                                    last = new OHLC(last.getDate(), last.getOpen(), Math.max(last.getHigh(), tradeData.getPrice()), Math.min(last.getLow(), tradeData.getPrice()), tradeData.getPrice(), last.getVolume() + tradeData.getSize());
                                    bars[bars.length - 1] = last;
                                }
                                else {
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
                        ((org.eclipsetrader.jessx.internal.core.connector.FeedSubscription)subscription).setQuote(new Quote(bid, ask, bidSize, askSize));
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
                        ((org.eclipsetrader.jessx.internal.core.connector.FeedSubscription)subscription).setTodayOHL(new TodayOHL(open, high, low));
                        StreamingConnector.getInstance().wakeupNotifyThread();
                    }
                }
            }
        }
	}

    private void registerSecurity(final String name) {
        logger.info("########## I'm going to register security " + name);
        BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
        ServiceReference marketServiceReference = context.getServiceReference(IMarketService.class.getName());
        if (serviceReference != null) {
            final IRepositoryService repositoryService = (IRepositoryService) context.getService(serviceReference);
            final IMarketService marketService = (IMarketService) context.getService(marketServiceReference);
            try {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        IRepositoryRunnable runnable = new IRepositoryRunnable() {
                            @Override
                            public IStatus run(IProgressMonitor monitor) throws Exception {
                                ISecurity security = repositoryService.getSecurityFromName(name);
                                if (security == null) {
                                    FeedProperties properties = new FeedProperties();
                                    properties.setProperty("org.eclipsetrader.jessx.symbol", name);
                                    FeedIdentifier identifier = new FeedIdentifier("org.eclipsetrader.jessx.feed", properties);
                                    security = new Stock(name, identifier, Currency.getInstance("USD"));
                                    IAdaptable[] adaptables = new IAdaptable[] {
                                        (IAdaptable) security,
                                    };
                                    IRepository repository = repositoryService.getRepository("local");
                                    repositoryService.moveAdaptable(adaptables, repository);

                                    IMarket[] markets = marketService.getMarkets();
                                    if (markets.length > 0) {
                                        markets[0].addMembers(new ISecurity[] {
                                            security
                                        });
                                    }
                                }
                                return Status.OK_STATUS;
                            }
                        };
                        repositoryService.runInService(runnable, null);
                    }
                });
            }
            finally {
                context.ungetService(serviceReference);
            }
        }
    }

    @Override
    public void serverStateChanged(int state) {
        if (state == Server.SERVER_STATE_ONLINE) {
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
                            logger.error("JessX-Setup: Timed out waiting for ThePlayer to connect. Setup aborted.");
                            return;
                        }
                        logger.info("JessX-Setup: ThePlayer is connected. Now loading bots...");

                        // 3. Load bots ONLY after ThePlayer is connected
                        srv.loadBots();

                        // 4. Wait for all bots to connect
                        int expectedTotalPlayers = 42; // 41 bots + 1 ThePlayer
                        logger.info("JessX-Setup: Waiting for all " + (expectedTotalPlayers - 1) + " bots to connect...");
                        for (int i = 0; i < 200; i++) { // Wait up to 20 seconds
                            if (NetworkCore.getPlayerList().size() >= expectedTotalPlayers) {
                                break;
                            }
                            Thread.sleep(100);
                        }

                        if (NetworkCore.getPlayerList().size() < expectedTotalPlayers) {
                            logger.error("JessX-Setup: Timed out waiting for all bots to connect. " + NetworkCore.getPlayerList().size() + "/" + expectedTotalPlayers + " connected. Aborting.");
                            return;
                        }
                        logger.info("JessX-Setup: All " + NetworkCore.getPlayerList().size() + " players are connected.");

                        // 5. Initialize General Parameters
                        logger.info("JessX-Setup: Initializing general parameters...");
                        GeneralParametersLocal generalParams = new GeneralParametersLocal();
                        generalParams.initializeGeneralParameters();
                        BusinessCore.setGeneralParameters(generalParams);
                        logger.info("JessX-Setup: General parameters initialized and set in BusinessCore.");

                        // 6. Assign categories and start experiment (with retry logic)
                        logger.info("JessX-Setup: All players connected. Assigning categories and attempting to start experiment...");

                        Scenario scn = BusinessCore.getScenario();
                        Map plTypes = scn.getPlayerTypes();
                        List<PlayerType> categories = new ArrayList<PlayerType>(plTypes.values());
                        Random random = new Random();

                        boolean experimentStarted = false;
                        for (int i = 0; i < 100; i++) { // Poll for up to 10 seconds
                            // On each attempt, snapshot the current player list and assign categories to any player that is missing one.
                            // This ensures that even if a player's state update was delayed, it will be retried.
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
                                logger.info("JessX-Setup: Experiment started successfully.");
                                experimentStarted = true;
                                break; // Exit the retry loop on success
                            }

                            // If it failed, wait a moment before the next retry.
                            Thread.sleep(100);
                        }

                        if (!experimentStarted) {
                            logger.error("JessX-Setup: Failed to start experiment after multiple retries. Preconditions not met. Check server logs.");
                        }
                    } catch (Exception e) {
                        logger.error("Error in JessX setup thread", e);
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
		return new IOrderRoute[] {

		// per ora il nulla...
		/*
		 * BrokerConnector.Immediate, BrokerConnector.MTA,
		 * BrokerConnector.CloseMTA, BrokerConnector.Open,
		 * BrokerConnector.AfterHours,
		 */
		};
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
			Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error connecting to orders monitor", e); //$NON-NLS-1$
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

										if (monitor.getFilledQuantity() != null && monitor.getAveragePrice() != null) {
											Account account = WebConnector.getInstance().getAccount();
											account.updatePosition(monitor);
										}
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
										positions.add(new Position(s[i]));
									} catch (Exception e) {
										Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error parsing line: " + s[i], e); //$NON-NLS-1$
										JessxActivator.log(status);
									}
								}
								if (s[i].indexOf(";7;9;") != -1) { //$NON-NLS-1$
									Account account = WebConnector.getInstance().getAccount();
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
			Order order = new Order(
					null,
					!item[IDX_PRICE].equals("") ? IOrderType.Limit : IOrderType.Market, item[IDX_SIDE].equalsIgnoreCase("V") ? IOrderSide.Sell : IOrderSide.Buy, getSecurityFromSymbol(item[IDX_SYMBOL]), quantity, !item[IDX_PRICE].equals("") ? numberFormatter.parse(item[IDX_PRICE]).doubleValue() : null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			tracker = new OrderMonitor(WebConnector.getInstance(), BrokerConnector.getInstance(), order);
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
				Status status = new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error parsing line: " + line, e); //$NON-NLS-1$
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

    public void sendOrder(IOrder order) {
        org.eclipsetrader.jessx.business.Operation op = null;
        if (order.getType() == IOrderType.Limit) {
            op = BusinessCore.getOperation("LimitOrder");
            op.setSecurity(BusinessCore.getInstitution(getSymbolFromSecurity(order.getSecurity())));
            op.setParam("price", String.valueOf(order.getPrice()));
            op.setParam("quantity", String.valueOf(order.getQuantity()));
            op.setParam("side", order.getSide() == IOrderSide.Buy ? "BUY" : "SELL");
        }
        else if (order.getType() == IOrderType.Market) {
            op = BusinessCore.getOperation("MarketOrder");
            op.setSecurity(BusinessCore.getInstitution(getSymbolFromSecurity(order.getSecurity())));
            op.setParam("quantity", String.valueOf(order.getQuantity()));
            op.setParam("side", order.getSide() == IOrderSide.Buy ? "BUY" : "SELL");
        }
        if (op != null) {
            ClientCore.executeOperation(op);
        }
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipsetrader.core.trading.IBroker#getAccounts()
	 */
	@Override
	public IAccount[] getAccounts() {
		return new IAccount[] { WebConnector.getInstance().getAccount(), };
	}
}