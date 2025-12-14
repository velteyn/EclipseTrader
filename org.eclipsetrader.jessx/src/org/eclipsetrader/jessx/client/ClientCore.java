// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipsetrader.jessx.client;


import org.jdom.Document;

import java.util.Hashtable;
import java.util.Iterator;

import java.io.IOException;
import java.util.Properties;
import java.io.File;

import org.eclipsetrader.jessx.business.Institution;
import org.eclipsetrader.jessx.business.Operation;
import org.eclipsetrader.jessx.business.Operator;
import org.eclipsetrader.jessx.business.Portfolio;
import org.eclipsetrader.jessx.client.event.ConnectionListener;
import org.eclipsetrader.jessx.client.event.NetworkListener;
import org.eclipsetrader.jessx.client.event.OperatorPlayedListener;
import org.eclipsetrader.jessx.net.NetworkWritable;
import org.eclipsetrader.jessx.utils.Utils;
import java.util.Vector;

import java.util.HashMap;

public abstract class ClientCore
{
    public static final String CLIENT_LOG_FILE = "./client.log";
    public static final int CONNECTED = 1;
    public static final int DISCONNECTED = 0;
    private static HashMap operatorsPlayed;
    private static HashMap institutions;
    private static Portfolio portfolio;
    private static CommClient commModule;
    private static ClientExperimentManager experimentManager;
    private static String login;
    private static HashMap networkListener;
    private static Vector operatorPlayedListeners;
    private static Vector connectionListeners;
    
    static {
        InitLogs();
        Utils.logger.debug("Setting application properties...");
        Utils.logger.debug("Sets WaitingPort");
        Utils.SetApplicationProperties("ServerWaitingPort", "6290");
        Utils.logger.info("Loading operations and institutions modules...");
        Utils.loadModules("org.eclipsetrader.jessx.business.operations.LimitOrder");
        Utils.loadModules("org.eclipsetrader.jessx.business.operations.DeleteOrder");
        Utils.loadModules("org.eclipsetrader.jessx.business.operations.MarketOrder");
        Utils.loadModules("org.eclipsetrader.jessx.business.operations.BestLimitOrder");
        Utils.loadModules("org.eclipsetrader.jessx.business.institutions.OrderMarket");
        Utils.logger.info("Loading modules done.");
        ClientCore.networkListener = new HashMap();
        ClientCore.operatorPlayedListeners = new Vector();
        ClientCore.experimentManager = new ClientExperimentManager();
        new DataManager();
        new LogSender(24456);
        ClientCore.operatorsPlayed = new HashMap();
        ClientCore.institutions = new HashMap();
        ClientCore.portfolio = new Portfolio(0.0f, new HashMap());
        ClientCore.connectionListeners = new Vector();
    }
    
    private static void InitLogs() {
        // Log4j configuration removed
        Utils.logger.debug("Logging enabled.");
    }
    
    public static Portfolio getPortfolio() {
        return ClientCore.portfolio;
    }
    
    public static String getLogin() {
        return ClientCore.login;
    }
    
    public static ClientExperimentManager getExperimentManager() {
        return ClientCore.experimentManager;
    }
    
    public static void addInstitution(final Institution institution) {
        ClientCore.institutions.put(institution.getName(), institution);
    }
    
    public static void addOperatorPlayed(final Operator oper) {
        if (!ClientCore.operatorsPlayed.containsKey(oper.getCompleteName())) {
            ClientCore.operatorsPlayed.put(oper.getCompleteName(), oper);
            fireNewOperatorPlayed(oper);
        }
    }
    
    public static HashMap getInstitutions() {
        return ClientCore.institutions;
    }
    
    public static HashMap getOperators() {
        return ClientCore.operatorsPlayed;
    }
    
    public static Institution getInstitution(final String name) {
        return (Institution) ClientCore.institutions.get(name);
    }
    
    public static Operator getOperator(final String name) {
        return (Operator) ClientCore.operatorsPlayed.get(name);
    }
    
    public static void initializeForNewExperiment() {
        ClientCore.institutions.clear();
        ClientCore.operatorsPlayed.clear();
    }
    
    public static void connecToServer(final String hostname, final String login, final String password) throws IOException {
        (ClientCore.commModule = new CommClient()).connect(hostname, login, password);
        ClientCore.login = login;
    }
    
    public static void executeOperation(final Operation op) {
        send(op);
    }
    
    public static void send(final NetworkWritable message) {
        ClientCore.commModule.send(message);
    }
    
    public static void addNetworkListener(final NetworkListener listener, final String expectedRootNode) {
        Utils.logger.debug("Adding a network listener on object " + expectedRootNode);
        if (!ClientCore.networkListener.containsKey(expectedRootNode)) {
            ClientCore.networkListener.put(expectedRootNode, new Vector());
        }
        ((Vector) ClientCore.networkListener.get(expectedRootNode)).add(listener);
    }
    
    public static void removeNetworkListener(final NetworkListener listener) {
        Utils.logger.debug("Removing a network listener from all classes it was registered for.");
        for ( Object key : ClientCore.networkListener.keySet()) {
            removeNetworkListener(listener, (String) key);
        }
    }
    
    public static void removeNetworkListener(final NetworkListener listener, final String expectedRootNode) {
        Utils.logger.debug("removing a network listener from object: " + expectedRootNode);
        ((Hashtable<Object, Object>) ClientCore.networkListener.get(expectedRootNode)).remove(listener);
    }
    
    static void fireObjectReceived(final Document object) {
        Utils.logger.debug("Dispatching object received to listener...");
        final Vector vect = (Vector) ClientCore.networkListener.get(object.getRootElement().getName());
        if (vect != null) {
            for (int i = 0; i < vect.size(); ++i) {
                ((NetworkListener) vect.elementAt(i)).objectReceived(object);
            }
        }
    }
    
    public static void addOperatorPLayedListener(final OperatorPlayedListener listener) {
        ClientCore.operatorPlayedListeners.add(listener);
    }
    
    public static void removeOperatorPlayedListener(final OperatorPlayedListener listener) {
        ClientCore.operatorPlayedListeners.remove(listener);
    }
    
    private static void fireNewOperatorPlayed(final Operator op) {
        for (int i = 0; i < ClientCore.operatorPlayedListeners.size(); ++i) {
            ((OperatorPlayedListener) ClientCore.operatorPlayedListeners.elementAt(i)).newOperator(op);
        }
    }
    
    public static void addConnectionListener(final ConnectionListener listener) {
        ClientCore.connectionListeners.add(listener);
    }
    
    public static void removeConnectionListener(final ConnectionListener listener) {
        ClientCore.connectionListeners.remove(listener);
    }
    
    public static void fireConnectionStateChanged(final int newState) {
        for (int i = 0; i < ClientCore.connectionListeners.size(); ++i) {
            ((ConnectionListener) ClientCore.connectionListeners.elementAt(i)).connectionStateChanged(newState);
        }
    }
    
    public static boolean isConnected() {
        return ClientCore.commModule.isConnected();
    }
}
