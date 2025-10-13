package org.eclipsetrader.jessx.internal;

import java.io.File;
import java.io.FileWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipsetrader.jessx.internal.core.repository.IdentifiersList;
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

        startupRepository(getStateLocation().append(REPOSITORY_FILE).toFile());
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
}
