// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipsetrader.jessx.server;

import java.io.File;
import java.io.FileFilter;
import java.util.Properties;

import javax.swing.UIManager;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.trader.jessx.business.BusinessCore;
import org.eclipse.trader.jessx.trobot.Discreet;
import org.eclipse.trader.jessx.trobot.DiscreetIT;
import org.eclipse.trader.jessx.trobot.NotDiscreet;
import org.eclipse.trader.jessx.trobot.Robot;
import org.eclipsetrader.jessx.server.net.NetworkCore;
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
    
    public static void setServerState(final int servState) {
        Server.serverState = servState;
        if (servState == Server.SERVER_STATE_ONLINE) {
            NetworkCore.setServerOnline();
        }
        else {
            NetworkCore.setServerOffline();
        }
    }
    
    public Server(final String scenarioFile, final boolean graphicalMode) {
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
            if (scenarioFile != "") {
                System.out.println("Scenary file loading...");
                final Document xmlDoc = Utils.readXmlFile(scenarioFile);
                BusinessCore.loadFromXml(xmlDoc.getRootElement());
            }
        }
        catch (Exception ex) {
        	Utils.logger.error("Error loading default scenary");
        	ex.printStackTrace();
        }
    }
    
    public void loadBots(){
        final int temp = 10;
        final int tempIT = 10;
        for (int i = 0; i < NUMBER_DISCREET_BOTS; ++i) {
            final Robot zitDiscreet = new Discreet(i, temp);
            System.out.println("in for, after creating the discreet "+ i +" and before start");
            zitDiscreet.start();
            System.out.println("after discreet start " + i);
        }
        for (int i = 0; i < 19; ++i) {
            final Robot zitDiscreetIT = new DiscreetIT(i, tempIT);
            System.out.println("dans for, apr\u00e8s cr\u00e9ation du discreet " + i + " et avant start");
            zitDiscreetIT.start();
            System.out.println("apr\u00e8s start du discreetIT " + i);
        }
        for (int i = 0; i < 15; ++i) {
            final Robot zitNotDiscreet = new NotDiscreet(i, 10, 0, 100);
            zitNotDiscreet.start();
        }

    }
    
    private void loadJessXModules() {
        Utils.logger.debug("Loading all available modules.");
        Utils.loadModules("org.eclipse.trader.jessx.business.operations.LimitOrder");
        Utils.loadModules("org.eclipse.trader.jessx.business.operations.DeleteOrder");
        Utils.loadModules("org.eclipse.trader.jessx.business.operations.MarketOrder");
        Utils.loadModules("org.eclipse.trader.jessx.business.operations.BestLimitOrder");
        Utils.loadModules("org.eclipse.trader.jessx.business.institutions.OrderMarket");
        Utils.loadModules("org.eclipse.trader.jessx.business.assets.Stock");
        Utils.logger.debug("All available modules loaded.");
    }
    
    private void buildFrame() {
    	
    }
    
    private void loadServerProperties() {
        Utils.logger.debug("Sets WaitingPort");
        Utils.SetApplicationProperties("ServerWaitingPort", "6290");
    }
    
    public void startServer() {
    }
    
    public static void InitLogs() {
        final File file = new File("./server.log");
        if (file.exists()) {
            file.delete();
        }
        final Properties log4jconf = new Properties();
        log4jconf.setProperty("log4j.rootCategory", "debug, stdout, R");
        log4jconf.setProperty("log4j.category.your.category.name", "DEBUG");
        log4jconf.setProperty("log4j.category.your.category.name", "INHERITED");
        log4jconf.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        log4jconf.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        log4jconf.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%5p [%t] (%F:%L) - %m%n");
        log4jconf.setProperty("log4j.appender.R", "org.apache.log4j.RollingFileAppender");
        log4jconf.setProperty("log4j.appender.R.File", "./server.log");
        log4jconf.setProperty("log4j.appender.R.MaxFileSize", "500000KB");
        log4jconf.setProperty("log4j.appender.R.MaxBackupIndex", "1");
        log4jconf.setProperty("log4j.appender.R.layout", "org.apache.log4j.PatternLayout");
        log4jconf.setProperty("log4j.appender.R.layout.ConversionPattern", "%r [%p] %m  [%t] (%F:%L) \r\n");
        PropertyConfigurator.configure(log4jconf);
        Utils.logger.debug("Logging enabled. Starting logging...");
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
        new Server(xmlfile, graphicalMode);
    }
}
