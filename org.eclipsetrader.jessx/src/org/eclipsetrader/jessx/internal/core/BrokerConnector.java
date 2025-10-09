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
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import java.net.URL;

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
        try {
            Bundle bundle = Platform.getBundle(JessxActivator.PLUGIN_ID);
            URL fileURL = FileLocator.find(bundle, new Path("org/eclipsetrader/jessx/utils/default.xml"), null);
            String scenarioFilePath = FileLocator.toFileURL(fileURL).getFile();
            srv = new Server(scenarioFilePath, false);
        }
        catch (Exception e) {
            logger.error("Failed to locate scenario file, falling back to empty server.", e);
            srv = new Server("", false); // Fallback to prevent NPE
        }
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
                        if (stock.getAssetName().equals(symbol)) {
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
