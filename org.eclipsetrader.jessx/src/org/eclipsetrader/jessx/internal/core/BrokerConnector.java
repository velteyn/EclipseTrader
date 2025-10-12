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
import java.net.URL;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.trader.jessx.business.BusinessCore;
import org.eclipse.trader.jessx.business.PlayerType;
import org.eclipse.trader.jessx.business.Scenario;
import org.eclipse.trader.jessx.business.assets.Stock;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderChangeListener;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderRoute;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IOrderValidity;
import org.eclipsetrader.core.trading.Order;
import org.eclipsetrader.core.trading.OrderChangeEvent;
import org.eclipsetrader.core.trading.OrderDelta;
import org.eclipsetrader.jessx.client.ClientCore;
import org.eclipsetrader.jessx.client.event.NetworkListener;
import org.eclipsetrader.jessx.internal.JessxActivator;
import org.eclipsetrader.jessx.internal.core.connector.FeedConnector;
import org.eclipsetrader.jessx.server.Server;
import org.eclipsetrader.jessx.server.net.NetworkCore;
import org.eclipsetrader.jessx.server.net.Player;
import org.eclipsetrader.jessx.utils.gui.MessageTimer;
import org.jdom.Document;
import org.jdom.Element;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class BrokerConnector implements IBroker, IExecutableExtension, NetworkListener {

    public static final IOrderRoute Immediate = new OrderRoute("1", "immed"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final IOrderRoute MTA = new OrderRoute("2", "MTA"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final IOrderRoute CloseMTA = new OrderRoute("4", "clos-MTA"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final IOrderRoute AfterHours = new OrderRoute("5", "AfterHours"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final IOrderRoute Open = new OrderRoute("7", "open//"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final IOrderValidity Valid30Days = new OrderValidity("30days", "30 Days");

    private String id;
    private String name;
    private Server srv;
    private Set<OrderMonitor> orders = new HashSet<OrderMonitor>();
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
    private Log logger = LogFactory.getLog(getClass());
    private Account account;

    public BrokerConnector() {
    }

    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        name = config.getAttribute("name");
        account = new Account("JessX Portfolio", null);
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
    public void connect() {
        startServer();
        registerSecurities();
        startFeedConnector();
        ClientCore.addNetworkListener(this, "Portfolio");
    }

    @Override
    public void disconnect() {
        stopServer();
        ClientCore.removeNetworkListener(this);
    }

    public void startServer() {
        // 1. Start the server
        try {
            Bundle bundle = Platform.getBundle(JessxActivator.PLUGIN_ID);
            URL fileURL = FileLocator.find(bundle, new Path("default.xml"), null);
            if (fileURL == null) {
                throw new IOException("default.xml not found in plugin bundle");
            }
            String scenarioFilePath = FileLocator.toFileURL(fileURL).getFile();
            srv = new Server(scenarioFilePath, false);
        }
        catch (Exception e) {
            logger.fatal("Could not initialize JESSX server due to missing scenario file.", e);
            // Stop further execution if the server cannot be initialized.
            return;
        }
        Server.setServerState(Server.SERVER_STATE_ONLINE);
        srv.startServer();

        // 2. Load and connect the bots
        srv.loadBots();

        // 3. Connect the main client
        try {
            ClientCore.connecToServer("localhost", "ThePlayer", "he-man");
        }
        catch (IOException e) {
            logger.error("Client connect error", e);
        }

        // 4. Assign player types to all connected players (bots and main client)
        Map<String, Player> playerList = NetworkCore.getPlayerList();
        Scenario scn = BusinessCore.getScenario();
        if (scn != null && !scn.getPlayerTypes().isEmpty()) {
            List<PlayerType> categories = new ArrayList<PlayerType>(scn.getPlayerTypes().values());
            Random random = new Random();
            Iterator<Map.Entry<String, Player>> pIter = playerList.entrySet().iterator();
            while (pIter.hasNext()) {
                Map.Entry<String, Player> entry = pIter.next();
                Player player = entry.getValue();
                if (!categories.isEmpty()) {
                    int index = random.nextInt(categories.size());
                    player.setPlayerCategory(categories.get(index).getPlayerTypeName());
                }
            }
        }

        // 5. Start the experiment
        if (NetworkCore.getExperimentManager().beginExperiment()) {
            new MessageTimer((Vector) BusinessCore.getScenario().getListInformation().clone()).start();
        }
    }

    public void stopServer() {
        if (srv != null) {
            srv.shutdown();
            srv = null;
        }
    }

    private void registerSecurities() {
        BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
        if (serviceReference != null) {
            IRepositoryService repositoryService = (IRepositoryService) context.getService(serviceReference);
            try {
                Map<String, Object> assets = BusinessCore.getAssets();
                if (assets == null) {
                    return;
                }
                Set<String> existingSymbols = new HashSet<String>();
                for (ISecurity s : repositoryService.getSecurities()) {
                    existingSymbols.add(s.getName());
                }
                for (Object asset : assets.values()) {
                    if (asset instanceof Stock) {
                        Stock stock = (Stock) asset;
                        String stockName = stock.getAssetName();
                        if (stockName != null && !existingSymbols.contains(stockName)) {
                            FeedProperties properties = new FeedProperties();
                            properties.setProperty("org.eclipsetrader.jessx.symbol", stockName);
                            IFeedIdentifier feedIdentifier = new FeedIdentifier(stockName, properties);
                            ISecurity security = new Security(stockName, feedIdentifier);
                            repositoryService.saveAdaptable(new IAdaptable[] { security });
                        }
                    }
                }
            }
            finally {
                context.ungetService(serviceReference);
            }
        }
    }

    public static void startFeedConnector() {
        BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IFeedConnector.class.getName());
        if (serviceReference != null) {
            IFeedConnector service = (IFeedConnector) context.getService(serviceReference);
            if (service instanceof FeedConnector) {
                ((FeedConnector) service).doConnect();
            }
            context.ungetService(serviceReference);
        }
    }

    @Override
    public boolean canTrade(ISecurity security) {
        IFeedIdentifier identifier = security.getIdentifier();
        if (identifier == null) {
            return false;
        }
        return BusinessCore.getAssets().get(security.getName()) != null;
    }

    @Override
    public String getSymbolFromSecurity(ISecurity security) {
        IFeedIdentifier identifier = security.getIdentifier();
        if (identifier == null) {
            return null;
        }
        return identifier.getSymbol();
    }


    @Override
    public ISecurity getSecurityFromSymbol(String symbol) {
        ISecurity security = null;
        BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
        if (serviceReference != null) {
            IRepositoryService service = (IRepositoryService) context.getService(serviceReference);
            try {
                for (ISecurity s : service.getSecurities()) {
                    if (s.getName().equals(symbol)) {
                        security = s;
                        break;
                    }
                }
            }
            finally {
                context.ungetService(serviceReference);
            }
        }
        return security;
    }

    @Override
    public IOrderMonitor prepareOrder(IOrder order) throws BrokerException {
        return new OrderMonitor(WebConnector.getInstance(), this, order);
    }

    @Override
    public IOrderSide[] getAllowedSides() {
        return new IOrderSide[] {
            IOrderSide.Buy, IOrderSide.Sell
        };
    }

    @Override
    public IOrderType[] getAllowedTypes() {
        return new IOrderType[] {
            IOrderType.Limit, IOrderType.Market
        };
    }

    @Override
    public IOrderValidity[] getAllowedValidity() {
        return new IOrderValidity[] {
            IOrderValidity.Day, new OrderValidity("30days", "30 Days")
        };
    }

    @Override
    public IOrderRoute[] getAllowedRoutes() {
        return new IOrderRoute[0];
    }

    @Override
    public IOrderMonitor[] getOrders() {
        synchronized (orders) {
            return orders.toArray(new IOrderMonitor[orders.size()]);
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

    @Override
    public void addOrderChangeListener(IOrderChangeListener listener) {
        listeners.add(listener);
    }

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
                }
                catch (Throwable e) {
                    JessxActivator.log(new Status(IStatus.ERROR, JessxActivator.PLUGIN_ID, 0, "Error running listener", e));
                }
            }
        }
    }

    @Override
    public IAccount[] getAccounts() {
        return new IAccount[] { account };
    }

    @Override
    public void objectReceived(Document doc) {
        Element root = doc.getRootElement();
        if ("Portfolio".equals(root.getName())) {
            try {
                double cash = Double.parseDouble(root.getAttributeValue("cash"));
                account.setBalance(cash);

                List<Position> positions = new ArrayList<Position>();
                List<?> ownings = root.getChildren("Owning");
                for (Object obj : ownings) {
                    Element owningElement = (Element) obj;
                    String assetName = owningElement.getAttributeValue("asset");
                    long quantity = Long.parseLong(owningElement.getAttributeValue("qtty"));

                    ISecurity security = getSecurityFromSymbol(assetName);
                    if (security != null) {
                        positions.add(new Position(security, quantity, null));
                    }
                }
                account.setPositions(positions.toArray(new Position[positions.size()]));
            }
            catch(Exception e) {
                logger.error("Error parsing portfolio XML", e);
            }
        }
    }
}