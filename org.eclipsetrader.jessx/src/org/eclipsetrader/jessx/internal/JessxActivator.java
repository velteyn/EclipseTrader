package org.eclipsetrader.jessx.internal;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.trader.jessx.business.BusinessCore;
import org.eclipse.trader.jessx.business.Stock;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.jessx.internal.core.BrokerConnector;
import org.eclipsetrader.jessx.internal.core.repository.IdentifiersList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


/**
 * 
 * @author Edoardo BAROLO
 * 
 */
public class JessxActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipsetrader.jessx"; //$NON-NLS-1$
	public static final String REPOSITORY_FILE = "identifiers.xml"; //$NON-NLS-1$

	public static final String PREFS_DRIVER = "DRIVER"; //$NON-NLS-1$
	public static final String PREFS_NEWS_UPDATE_INTERVAL = "NEWS_UPDATE_INTERVAL"; //$NON-NLS-1$
	public static final String PREFS_HOURS_AS_RECENT = "HOURS_AS_RECENT"; //$NON-NLS-1$
	public static final String PREFS_UPDATE_SECURITIES_NEWS = "UPDATE_SECURITIES_NEWS"; //$NON-NLS-1$
	public static final String PREFS_SUBSCRIBE_PREFIX = "SUBSCRIBE_"; //$NON-NLS-1$
	
	
	 
    public static final String PROP_CODE = "org.eclipsetrader.jessx.code"; //$NON-NLS-1$
    public static final String PROP_ISIN = "org.eclipsetrader.jessx.isin"; 
	
	
	  // The shared instance
    private static JessxActivator plugin;

    private IdentifiersList identifiersList;
    private BrokerConnector brokerConnector;
    
    
    @Override
    public void start(BundleContext context) throws Exception {
       	super.start(context);
        plugin = this;

        this.brokerConnector = findBrokerConnector();
        if (this.brokerConnector != null) {
            this.brokerConnector.startServer();
        } else {
            log(new Status(IStatus.ERROR, PLUGIN_ID, "Jessx BrokerConnector not found."));
        }

        registerSecurities();

        startupRepository(getStateLocation().append(REPOSITORY_FILE).toFile());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    	 shutdownRepository(getStateLocation().append(REPOSITORY_FILE).toFile());

	 if (this.brokerConnector != null) {
	     this.brokerConnector.stopServer();
	 }

         plugin = null;
         super.stop(context);
    }
    
    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static JessxActivator getDefault() {
        return plugin;
    }

	private void startupRepository(File file) {
        if (file.exists() == true) {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(IdentifiersList.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                unmarshaller.setEventHandler(new ValidationEventHandler() {

                    @Override
                    public boolean handleEvent(ValidationEvent event) {
                        Status status = new Status(IStatus.WARNING, PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                        getLog().log(status);
                        return true;
                    }
                });
                identifiersList = (IdentifiersList) unmarshaller.unmarshal(file);
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, PLUGIN_ID, 0, "Error loading repository", e); //$NON-NLS-1$
                getLog().log(status);
            }
        }

        // Fail safe, create an empty repository
        if (identifiersList == null) {
            identifiersList = new IdentifiersList();
        }
		
	}
    public void shutdownRepository(File file) {
        try {
            if (file.exists()) {
                file.delete();
            }

            JAXBContext jaxbContext = JAXBContext.newInstance(IdentifiersList.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$
            marshaller.setEventHandler(new ValidationEventHandler() {

                @Override
                public boolean handleEvent(ValidationEvent event) {
                    Status status = new Status(IStatus.WARNING, PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                    getLog().log(status);
                    return true;
                }
            });
            marshaller.marshal(identifiersList, new FileWriter(file));
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, PLUGIN_ID, 0, "Error saving repository", e); //$NON-NLS-1$
            getLog().log(status);
        }
    }
    
    public static void log(IStatus status) {
        if (plugin == null) {
            if (status.getException() != null) {
                status.getException().printStackTrace();
            }
            throw new RuntimeException(status.getException());
        }
        plugin.getLog().log(status);
    }
    
    /**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

    private BrokerConnector findBrokerConnector() {
        BundleContext context = getBundle().getBundleContext();
        try {
            ServiceReference<?>[] serviceReferences = context.getServiceReferences(IBroker.class.getName(), "(id=org.eclipsetrader.brokers.jessx)");
            if (serviceReferences != null && serviceReferences.length > 0) {
                Object service = context.getService(serviceReferences[0]);
                if (service instanceof BrokerConnector) {
                    return (BrokerConnector) service;
                }
            }
        } catch (Exception e) {
            log(new Status(IStatus.ERROR, PLUGIN_ID, "Error finding BrokerConnector service", e));
        }
        return null;
    }

    private void registerSecurities() {
        BundleContext context = getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
        if (serviceReference != null) {
            IRepositoryService repositoryService = (IRepositoryService) context.getService(serviceReference);
            try {
                Map<String, Stock> stocks = BusinessCore.getStocks();
                if (stocks == null) {
                    log(new Status(IStatus.WARN, PLUGIN_ID, "BusinessCore.getStocks() returned null. Cannot register securities."));
                    return;
                }

                Set<String> existingSymbols = new HashSet<String>();
                for (ISecurity s : repositoryService.getSecurities()) {
                    existingSymbols.add(s.getName());
                }

                for (Stock stock : stocks.values()) {
                    String stockName = stock.getAssetName();
                    if (stockName != null && !existingSymbols.contains(stockName)) {
                        FeedProperties properties = new FeedProperties();
                        properties.setProperty("org.eclipsetrader.jessx.symbol", stockName);

                        IFeedIdentifier feedIdentifier = new FeedIdentifier(stockName, properties);
                        ISecurity security = new Security(stockName, feedIdentifier);

                        repositoryService.save(new ISecurity[] { security });
                        log(new Status(IStatus.INFO, PLUGIN_ID, "Registered new security from simulation: " + stockName));
                    }
                }
            }
            finally {
                context.ungetService(serviceReference);
            }
        } else {
            log(new Status(IStatus.ERROR, PLUGIN_ID, "IRepositoryService not found. Cannot register securities."));
        }
    }
}