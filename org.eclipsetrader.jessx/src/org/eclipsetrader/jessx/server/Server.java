package org.eclipsetrader.jessx.server;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.UIManager;

import org.eclipsetrader.jessx.business.BusinessCore;
import org.eclipsetrader.jessx.internal.JessxActivator;
import org.eclipsetrader.jessx.server.net.NetworkCore;
import org.eclipsetrader.jessx.trobot.Discreet;
import org.eclipsetrader.jessx.trobot.DiscreetIT;
import org.eclipsetrader.jessx.trobot.NotDiscreet;
import org.eclipsetrader.jessx.trobot.Robot;
import org.eclipsetrader.jessx.utils.Utils;
import org.jdom.Document;


public class Server
{
    public static int EXPERIMENT_STATE_SETUP;
    public static int EXPERIMENT_STATE_RUNNING;
    public static int EXPERIMENT_STATE_ENDED;
    public static int SERVER_STATE_OFFLINE;
    public static int SERVER_STATE_ONLINE;
    public static int NUMBER_DISCREET_BOTS = 16;
    boolean packFrame;
  //  private GeneralServerFrame frame;
    private static int experimentState;
    private static int serverState;
    private List<ServerStateListener> listeners = new ArrayList<ServerStateListener>();
    private static final FileFilter filter;
    public static final String SERVER_LOG_FILE = "./server.log";
    
    static {
        Server.EXPERIMENT_STATE_SETUP = 0;
        Server.EXPERIMENT_STATE_RUNNING = 1;
        Server.EXPERIMENT_STATE_ENDED = 2;
        Server.SERVER_STATE_OFFLINE = 3;
        Server.SERVER_STATE_ONLINE = 4;
        Server.experimentState = 0;
        Server.serverState = 3;
        filter = new FileFilter() {
            public boolean accept(final File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                }
                if (!pathname.isFile()) {
                    return false;
                }
                final String path = pathname.getAbsolutePath();
                return path.endsWith(".class") && path.indexOf(36) < 0;
            }
        };
    }
    
    public static int getExperimentState() {
        return Server.experimentState;
    }
    
    public static int getServerState() {
        return Server.serverState;
    }
    
    public void setServerState(final int servState) {
        Server.serverState = servState;
        if (servState == Server.SERVER_STATE_ONLINE) {
            NetworkCore.setServerOnline();
        }
        else {
            NetworkCore.setServerOffline();
        }
        for (ServerStateListener listener : listeners) {
            listener.serverStateChanged(servState);
        }
    }

    public void addServerStateListener(ServerStateListener listener) {
        listeners.add(listener);
    }

    public Server(final java.io.InputStream is, final boolean graphicalMode) {
    	InitLogs();
        this.packFrame = true;
        
        //BusinessCore.setGeneralParameters(new GeneralParameterSetupGui(graphicalMode));
        BusinessCore.setGeneralParameters(new GeneralParametersLocal());
        Server.experimentState = Server.EXPERIMENT_STATE_SETUP;
        Server.serverState = Server.SERVER_STATE_OFFLINE;
        this.loadServerProperties();
        this.loadJessXModules();
        if (graphicalMode) {
            this.buildFrame();
        }
        try {
            System.out.println("Scenary file loading...");
            final Document xmlDoc = Utils.readXmlFile(is);
            BusinessCore.loadFromXml(xmlDoc.getRootElement());
        }
        catch (Exception ex) {
		Utils.logger.error("Error loading default scenary");
		ex.printStackTrace();
        }
    }

 
    public void loadBots(){
        try {
            if (JessxActivator.getDefault() != null) {
                JessxActivator.log("Server: loadBots called. Starting bot connections...");
            }
        } catch (Throwable t) {}
        
        final int temp = 10;
        final int tempIT = 10;
        
        // Add delay between bot connections to avoid overwhelming server
        final int DELAY_MS = 50; // 50ms delay between bots
        
        try { 
            Thread.sleep(200); // 200ms initial delay
            System.out.println("Server socket ready, starting bot connections...");
        } catch (InterruptedException e) {}
        
        for (int i = 0; i < NUMBER_DISCREET_BOTS; ++i) {
            final Robot zitDiscreet = new Discreet(i, temp);
            System.out.println("in for, after creating the discreet "+ i +" and before start");
            zitDiscreet.start();
            System.out.println("after discreet start " + i);
            
            // Add delay to stagger connections
            try { Thread.sleep(DELAY_MS); } catch (InterruptedException e) {}
        }
        
        for (int i = 0; i < 19; ++i) {
            final Robot zitDiscreetIT = new DiscreetIT(i, tempIT);
            System.out.println("dans for, après création du discreet " + i + " et avant start");
            zitDiscreetIT.start();
            System.out.println("après start du discreetIT " + i);
            
            // Add delay to stagger connections  
            try { Thread.sleep(DELAY_MS); } catch (InterruptedException e) {}
        }
        
        for (int i = 0; i < 15; ++i) {
            final Robot zitNotDiscreet = new NotDiscreet(i, 10, 0, 100);
            zitNotDiscreet.start();
            
            // Add delay to stagger connections
            try { Thread.sleep(DELAY_MS); } catch (InterruptedException e) {}
        }
    }
    
    private void loadJessXModules() {
        Utils.logger.debug("Loading all available modules.");
        Utils.loadModules("org.eclipsetrader.jessx.business.operations.LimitOrder");
        Utils.loadModules("org.eclipsetrader.jessx.business.operations.DeleteOrder");
        Utils.loadModules("org.eclipsetrader.jessx.business.operations.MarketOrder");
        Utils.loadModules("org.eclipsetrader.jessx.business.operations.BestLimitOrder");
        Utils.loadModules("org.eclipsetrader.jessx.business.institutions.OrderMarket");
        Utils.loadModules("org.eclipsetrader.jessx.business.assets.Stock");
        Utils.logger.debug("All available modules loaded.");
    }
    
    private void buildFrame() {
    	
    }
    
    private void loadServerProperties() {
        Utils.logger.debug("Sets WaitingPort");
        Utils.SetApplicationProperties("ServerWaitingPort", "6290");
    }
    
    public void startServer() {
        setServerState(SERVER_STATE_ONLINE);
    }
    
    public static void InitLogs() {
        // Log4j configuration removed
        Utils.logger.debug("Logging enabled. Starting logging...");
        try {
            if (JessxActivator.getDefault() != null) {
                JessxActivator.log("Server: Logging initialized.");
            }
        } catch (Throwable t) {}
    }
    
    public static void main(final String[] args) {
        InitLogs();
        try {
            Utils.logger.debug("Getting and setting look and feel...");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            Utils.logger.error("Error while loading look and feel: " + e.toString());
            e.printStackTrace();
        }
        Utils.logger.debug("Understanding Parameters");
        boolean graphicalMode = true;
        String xmlfile = "";
        for (int i = 0; i < args.length; ++i) {
            if (args[i] == "textmode") {
                graphicalMode = false;
            }
            if (args[i].contains("xml")) {
                xmlfile = args[i];
            }
        }
        Utils.logger.debug("Creating core object.");
        // new Server(xmlfile, graphicalMode);
    }
}