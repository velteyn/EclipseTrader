package org.eclipsetrader.jessx.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipsetrader.jessx.internal.core.BrokerConnector;
import org.eclipsetrader.jessx.internal.core.repository.IdentifiersList;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.osgi.framework.BundleContext;


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
    
    
    @Override
    public void start(BundleContext context) throws Exception {
        	super.start(context);
        plugin = this;
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        startupRepository(getStateLocation().append(REPOSITORY_FILE).toFile());

        migrateInvalidJessxIdentifiers();
        ensureJessxMarketAndReassign();
        
        preRegisterJessxSecurities();
        
        // Populate Tickers view with Jessx securities if empty
        preRegisterJessxSecurities();
        
        // Populate Tickers view with Jessx securities if empty
        populateTickersView();
        
        // Initialize BusinessCore with data from default.xml to ensure institutions are loaded
        initializeBusinessCore();
    }
    
    /**
     * Initialize BusinessCore with data from default.xml
     */
    private void initializeBusinessCore() {
        try {
            File file = getStateLocation().append("default.xml").toFile();
            if (file.exists()) {
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(file);
                Element root = doc.getRootElement();
                
                log("Initializing BusinessCore from " + file.getAbsolutePath());
                
                // Initialize GeneralParameters before loading from XML
                org.eclipsetrader.jessx.server.GeneralParametersLocal generalParams = new org.eclipsetrader.jessx.server.GeneralParametersLocal();
                log("Created GeneralParametersLocal instance: " + generalParams);
                generalParams.initializeGeneralParameters();
                org.eclipsetrader.jessx.business.BusinessCore.setGeneralParameters(generalParams);
                
                if (org.eclipsetrader.jessx.business.BusinessCore.getGeneralParameters() == null) {
                    log("ERROR: BusinessCore.getGeneralParameters() is null after setting it!");
                } else {
                    log("BusinessCore.getGeneralParameters() is set correctly.");
                }
                
                org.eclipsetrader.jessx.business.BusinessCore.loadFromXml(root);
                
                // Log loaded institutions for debugging
                java.util.HashMap institutions = org.eclipsetrader.jessx.business.BusinessCore.getInstitutions();
                if (institutions != null) {
                    log("BusinessCore loaded " + institutions.size() + " institutions: " + institutions.keySet());
                } else {
                    log("BusinessCore loaded 0 institutions (map is null)");
                }
            } else {
                log("default.xml not found at " + file.getAbsolutePath() + " - BusinessCore not initialized");
            }
        } catch (Exception e) {
            log(new Status(IStatus.ERROR, PLUGIN_ID, "Error initializing BusinessCore", e));
        }
    }


    @Override
    public void stop(BundleContext context) throws Exception {
    	 shutdownRepository(getStateLocation().append(REPOSITORY_FILE).toFile());

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

            System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");
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

    private void migrateInvalidJessxIdentifiers() {
        try {
            BundleContext context = getBundle().getBundleContext();
            org.osgi.framework.ServiceReference sr = context.getServiceReference(org.eclipsetrader.core.repositories.IRepositoryService.class.getName());
            if (sr == null) {
                return;
            }
            org.eclipsetrader.core.repositories.IRepositoryService repositoryService = (org.eclipsetrader.core.repositories.IRepositoryService) context.getService(sr);
            if (repositoryService == null) {
                return;
            }

            org.eclipsetrader.core.instruments.ISecurity[] securities = repositoryService.getSecurities();
            for (org.eclipsetrader.core.instruments.ISecurity security : securities) {
                org.eclipsetrader.core.feed.IFeedIdentifier identifier = security.getIdentifier();
                if (identifier == null) {
                    continue;
                }
                String symbol = identifier.getSymbol();
                if ("org.eclipsetrader.jessx.feed".equals(symbol)) {
                    org.eclipsetrader.core.feed.IFeedProperties props = (org.eclipsetrader.core.feed.IFeedProperties) identifier.getAdapter(org.eclipsetrader.core.feed.IFeedProperties.class);
                    String newSymbol = security.getName();
                    if (props != null) {
                        String s = props.getProperty("org.eclipsetrader.jessx.symbol");
                        if (s != null && s.length() != 0) {
                            newSymbol = s;
                        }
                    }
                    org.eclipsetrader.core.feed.FeedProperties fp = props instanceof org.eclipsetrader.core.feed.FeedProperties ? (org.eclipsetrader.core.feed.FeedProperties) props : null;
                    org.eclipsetrader.core.feed.FeedIdentifier newId = new org.eclipsetrader.core.feed.FeedIdentifier(newSymbol, fp);
                    if (security instanceof org.eclipsetrader.core.instruments.Security) {
                        ((org.eclipsetrader.core.instruments.Security) security).setIdentifier(newId);
                        repositoryService.saveAdaptable(new org.eclipse.core.runtime.IAdaptable[]{ (org.eclipse.core.runtime.IAdaptable) security });
                    }
                }
            }
            context.ungetService(sr);
        }
        catch (Exception e) {
            IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, 0, "Error migrating identifiers", e);
            getLog().log(status);
        }
    }
    
    private void ensureJessxMarketAndReassign() {
        try {
            BundleContext context = getBundle().getBundleContext();
            org.osgi.framework.ServiceReference marketRef = context.getServiceReference(org.eclipsetrader.core.markets.IMarketService.class.getName());
            org.osgi.framework.ServiceReference feedRef = context.getServiceReference(org.eclipsetrader.core.feed.IFeedService.class.getName());
            org.osgi.framework.ServiceReference repoRef = context.getServiceReference(org.eclipsetrader.core.repositories.IRepositoryService.class.getName());
            if (marketRef == null || repoRef == null) {
                return;
            }
            org.eclipsetrader.core.markets.IMarketService marketService = (org.eclipsetrader.core.markets.IMarketService) context.getService(marketRef);
            org.eclipsetrader.core.feed.IFeedService feedService = feedRef != null ? (org.eclipsetrader.core.feed.IFeedService) context.getService(feedRef) : null;
            org.eclipsetrader.core.repositories.IRepositoryService repositoryService = (org.eclipsetrader.core.repositories.IRepositoryService) context.getService(repoRef);
            if (marketService == null || repositoryService == null) {
                return;
            }
            org.eclipsetrader.core.markets.IMarket jessx = marketService.getMarket("JESSX");
            if (jessx == null) {
                org.eclipsetrader.core.internal.markets.Market newMarket = new org.eclipsetrader.core.internal.markets.Market("JESSX", null);
                if (feedService != null) {
                    org.eclipsetrader.core.feed.IFeedConnector connector = feedService.getConnector("org.eclipsetrader.jessx.feed");
                    if (connector != null) {
                        newMarket.setLiveFeedConnector(connector);
                    }
                }
                ((org.eclipsetrader.core.internal.markets.MarketService) org.eclipsetrader.core.internal.markets.MarketService.getInstance()).addMarket(newMarket);
                jessx = newMarket;
            }
            org.eclipsetrader.core.instruments.ISecurity[] securities = repositoryService.getSecurities();
            for (org.eclipsetrader.core.instruments.ISecurity security : securities) {
                org.eclipsetrader.core.feed.IFeedIdentifier id = security.getIdentifier();
                if (id == null) {
                    continue;
                }
                org.eclipsetrader.core.feed.IFeedProperties props = (org.eclipsetrader.core.feed.IFeedProperties) id.getAdapter(org.eclipsetrader.core.feed.IFeedProperties.class);
                if (props != null && props.getProperty("org.eclipsetrader.jessx.symbol") != null) {
                    boolean alreadyMember = jessx != null && jessx.hasMember(security);
                    if (!alreadyMember && jessx != null) {
                        jessx.addMembers(new org.eclipsetrader.core.instruments.ISecurity[] { security });
                    }
                    org.eclipsetrader.core.markets.IMarket[] markets = marketService.getMarkets();
                    for (org.eclipsetrader.core.markets.IMarket m : markets) {
                        if (m != null && m != jessx && m.hasMember(security)) {
                            m.removeMembers(new org.eclipsetrader.core.instruments.ISecurity[] { security });
                        }
                    }
                }
            }
            if (marketRef != null) {
                context.ungetService(marketRef);
            }
            if (feedRef != null) {
                context.ungetService(feedRef);
            }
            if (repoRef != null) {
                context.ungetService(repoRef);
            }
        }
        catch (Exception e) {
            IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, 0, "Error ensuring JESSX market", e);
            getLog().log(status);
        }
    }
    
    public static void log(String message) {
        log(new Status(IStatus.INFO, PLUGIN_ID, message));
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
	
	/**
	 * Pre-register JESSX securities from default.xml during plugin startup.
	 * This ensures securities exist before chart views initialize, preventing NPEs.
	 */
	private void preRegisterJessxSecurities() {
		try {
			// Copy default.xml to state location if needed
			File file = getStateLocation().append("default.xml").toFile();
			if (!file.exists()) {
				URL url = FileLocator.find(getBundle(), new Path("resources/default.xml"), null);
				if (url != null) {
					try (InputStream in = url.openStream(); 
						 OutputStream out = new FileOutputStream(file)) {
						byte[] buf = new byte[1024];
						int len;
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
					}
				}
			}
			if (file.exists()) {
				// Parse default.xml to extract asset names
				SAXBuilder builder = new SAXBuilder();
				Document doc = builder.build(file);
				Element root = doc.getRootElement();
				List<Element> assets = root.getChildren("Asset");
				
				if (!assets.isEmpty()) {
					log("Pre-registering " + assets.size() + " JESSX securities from default.xml");
					
					// Get BrokerConnector instance
					BrokerConnector broker = BrokerConnector.getInstance();
					
					// Pre-register each asset as a security
					for (Element asset : assets) {
						String assetName = asset.getAttributeValue("name");
						if (assetName != null && !assetName.isEmpty()) {
							log("Pre-registering JESSX security: " + assetName);
							broker.registerSecurityIfNeeded(assetName);
						}
					}
					
					log("JESSX security pre-registration complete");
				}
			}
		} catch (Exception e) {
			IStatus status = new Status(IStatus.WARNING, PLUGIN_ID, 
				"Could not pre-register JESSX securities. Charts may fail to initialize.", e);
			getLog().log(status);
		}
	}
	
	private void populateTickersView() {
		org.eclipse.swt.widgets.Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					// Access UIActivator safely
					org.eclipse.jface.dialogs.IDialogSettings settings = org.eclipsetrader.ui.internal.UIActivator.getDefault().getDialogSettings();
					org.eclipse.jface.dialogs.IDialogSettings section = settings.getSection("org.eclipsetrader.ui.views.tickers");
					if (section == null) {
						section = settings.addNewSection("org.eclipsetrader.ui.views.tickers");
					}
					
					String[] existing = section.getArray("securities");
					if (existing == null || existing.length == 0) {
						BundleContext context = getBundle().getBundleContext();
						org.osgi.framework.ServiceReference serviceReference = context.getServiceReference(org.eclipsetrader.core.repositories.IRepositoryService.class.getName());
						if (serviceReference != null) {
							org.eclipsetrader.core.repositories.IRepositoryService service = (org.eclipsetrader.core.repositories.IRepositoryService) context.getService(serviceReference);
							org.eclipsetrader.core.instruments.ISecurity[] securities = service.getSecurities();
							
							java.util.List<String> uris = new java.util.ArrayList<String>();
							for (org.eclipsetrader.core.instruments.ISecurity security : securities) {
								if (security.getName().equals("AAT") || security.getName().equals("GPLRF") || security.getName().equals("MSFT") || security.getName().equals("PLS")) {
									org.eclipsetrader.core.repositories.IStoreObject storeObject = (org.eclipsetrader.core.repositories.IStoreObject) security.getAdapter(org.eclipsetrader.core.repositories.IStoreObject.class);
									if (storeObject != null) {
										uris.add(storeObject.getStore().toURI().toString());
									}
								}
							}
							
							if (!uris.isEmpty()) {
								section.put("securities", uris.toArray(new String[uris.size()]));
								log("Populated Tickers view with " + uris.size() + " JESSX securities");
							}
							
							context.ungetService(serviceReference);
						}
					}
				} catch (Throwable e) {
					log("Error populating Tickers view: " + e.getMessage());
				}
			}
		});
	}
}
