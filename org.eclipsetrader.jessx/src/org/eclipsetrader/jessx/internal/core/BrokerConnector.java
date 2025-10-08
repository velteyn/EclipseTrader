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
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.trader.jessx.business.BusinessCore;
import org.eclipse.trader.jessx.business.PlayerType;
import org.eclipse.trader.jessx.business.Scenario;
import org.eclipse.trader.jessx.business.assets.Stock;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
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
import org.eclipsetrader.jessx.client.ClientCore;
import org.eclipsetrader.jessx.internal.JessxActivator;
import org.eclipsetrader.jessx.internal.ui.StatusLineContributionItem;
import org.eclipsetrader.jessx.server.Server;
import org.eclipsetrader.jessx.server.net.NetworkCore;
import org.eclipsetrader.jessx.server.net.Player;
import org.eclipsetrader.jessx.utils.gui.MessageTimer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class BrokerConnector implements IBroker, IExecutableExtension {

    public static final IOrderRoute Immediate = new OrderRoute("1", "immed"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final IOrderRoute MTA = new OrderRoute("2", "MTA"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final IOrderRoute CloseMTA = new OrderRoute("4", "clos-MTA"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final IOrderRoute AfterHours = new OrderRoute("5", "AfterHours"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final IOrderRoute Open = new OrderRoute("7", "open//"); //$NON-NLS-1$ //$NON-NLS-2$
   
    private String id;
    private String name;

    private Server srv;

    Set<OrderMonitor> orders = new HashSet<OrderMonitor>();
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private NumberFormat amountParser = NumberFormat.getInstance(Locale.ITALY);
    private NumberFormat amountFormatter = NumberFormat.getInstance();

    private Log logger = LogFactory.getLog(getClass());
    public static final IOrderValidity Valid30Days = new OrderValidity("30days", Messages.BrokerConnector_30Days); //$NON-NLS-1$

    public BrokerConnector() {
        amountFormatter.setMinimumFractionDigits(2);
        amountFormatter.setMaximumFractionDigits(2);
        amountFormatter.setGroupingUsed(true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id"); //$NON-NLS-1$
        name = config.getAttribute("name"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#connect()
     */
    @Override
    public void connect() {
        // The server is started by the JessxActivator, so this method does nothing.
        // It is kept for compliance with the IBroker interface.
        logger.info("BrokerConnector connect called, but server is managed by Activator.");
    }

    public void startServer() {
       srv = new Server("default.xml",false);
       Server.setServerState(Server.SERVER_STATE_ONLINE);
       srv.loadBots();
       srv.startServer();
       
       Map pList = NetworkCore.getPlayerList();
       
		Map<String, Player> playerList = NetworkCore.getPlayerList();

		// Itera sulla lista per accedere a ciascun giocatore
		Iterator<Map.Entry<String, Player>> pIter = playerList.entrySet().iterator();
		
    	Scenario  scn = BusinessCore.getScenario();
		Map plTypes = scn.getPlayerTypes();
		List<PlayerType> categories = new ArrayList<PlayerType>(plTypes.values());
		Random random = new Random();
		// setta una categoria randomica di players
		while (pIter.hasNext()) {
			Map.Entry<String, Player> entry = pIter.next();
			Player player = entry.getValue();
		     
		    int index = random.nextInt(categories.size());
			
			player.setPlayerCategory(categories.get(index).getPlayerTypeName());
			  
		}
		
		//Qua dovrebbe collegarsi il client fatto da nuovo del brooker (come in jessx) caricare i propri assets e iniziare a fare trading
		 try {
			ClientCore.connecToServer("localhost", "ThePlayer", "he-man");
			Player thePlayer = NetworkCore.getPlayer( "ThePlayer");
			thePlayer.setPlayerCategory(categories.get(0).getPlayerTypeName());
			
		} catch (IOException e) {
			System.out.println("Client connect error "+e.getMessage());
		}
		// sul server 
		
		
		//faccio partire l'essperimento (credo)
		System.out.println("-- LAUNCH EXPERIMENT ! --");
        if (NetworkCore.getExperimentManager().beginExperiment()) {
            new MessageTimer((Vector)BusinessCore.getScenario().getListInformation().clone()).start();
            
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#disconnect()
     */
    @Override
    public void disconnect() {
        // The server is stopped by the JessxActivator, so this method does nothing.
        // It is kept for compliance with the IBroker interface.
        logger.info("BrokerConnector disconnect called, but server is managed by Activator.");
    }

    public void stopServer() {
        if (srv != null) {
            srv.shutdown();
            srv = null;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#canTrade(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public boolean canTrade(ISecurity security) {
        IFeedIdentifier identifier = security.getIdentifier();
        if (identifier == null) {
            return false;
        }
        return BusinessCore.getAssets().get(security.getName()) != null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getSymbolFromSecurity(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public String getSymbolFromSecurity(ISecurity security) {
        IFeedIdentifier identifier = security.getIdentifier();
        if (identifier == null) {
            return null;
        }
        return identifier.getSymbol();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getSecurityFromSymbol(java.lang.String)
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
                    Object asset = BusinessCore.getAssets().get(securities[i].getName());
                    if (asset instanceof Stock) {
                        Stock stock = (Stock) asset;
                        if (stock.getCode().equals(symbol)) {
                            security = securities[i];
                            break;
                        }
                    }
                }

                context.ungetService(serviceReference);
            }
        }

        return security;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#prepareOrder(org.eclipsetrader.core.trading.IOrder)
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

        return new OrderMonitor(WebConnector.getInstance(), this, order);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAllowedSides()
     */
    @Override
    public IOrderSide[] getAllowedSides() {
        return new IOrderSide[] {
            IOrderSide.Buy, IOrderSide.Sell,
        };
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAllowedTypes()
     */
    @Override
    public IOrderType[] getAllowedTypes() {
        return new IOrderType[] {
            IOrderType.Limit, IOrderType.Market,
        };
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAllowedValidity()
     */
    @Override
    public IOrderValidity[] getAllowedValidity() {
        return new IOrderValidity[] {
            IOrderValidity.Day, Valid30Days,
        };
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAllowedRoutes()
     */
    @Override
    public IOrderRoute[] getAllowedRoutes() {
        return new IOrderRoute[] {};
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getOrders()
     */
    @Override
    public IOrderMonitor[] getOrders() {
        synchronized (orders) {
            return orders.toArray(new IOrderMonitor[orders.size()]);
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
            Order order = new Order(null, !item[IDX_PRICE].equals("") ? IOrderType.Limit : IOrderType.Market, item[IDX_SIDE].equalsIgnoreCase("V") ? IOrderSide.Sell : IOrderSide.Buy, getSecurityFromSymbol(item[IDX_SYMBOL]), quantity, !item[IDX_PRICE].equals("") ? numberFormatter.parse(item[IDX_PRICE]).doubleValue() : null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
            }
            else {
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
        }
        else if (item[IDX_STATUS].equals("n") || item[IDX_STATUS].equals("n ") || item[IDX_STATUS].equals("j")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            status = IOrderStatus.PendingNew;
        }
        else if (item[IDX_STATUS].equals("zA") || item[IDX_STATUS].equals("z ")) { //$NON-NLS-1$ //$NON-NLS-2$
            status = IOrderStatus.Canceled;
        }
        else if (item[IDX_STATUS].equals("na")) { //$NON-NLS-1$
            status = IOrderStatus.PendingCancel;
        }
        else {
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
                }
                else if (status == IOrderStatus.Canceled) {
                    sb.append("Order Canceled:");
                }
                else if (status == IOrderStatus.Rejected) {
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
        }
        else {
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

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#addOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
     */
    @Override
    public void addOrderChangeListener(IOrderChangeListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#removeOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
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
        fireUpdateNotifications(new OrderDelta[] {
            new OrderDelta(OrderDelta.KIND_ADDED, orderMonitor),
        });
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAccounts()
     */
    @Override
    public IAccount[] getAccounts() {
        return new IAccount[] {
            WebConnector.getInstance().getAccount(),
        };
    }
}
