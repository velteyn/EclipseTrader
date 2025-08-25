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
import org.osgi.framework.BundleContext;


/**
 * 
 * @author Edoardo BAROLO
 * 
 */
public class JessxActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipsetrader.jessx"; //$NON-NLS-1$

	public static final String PREFS_DRIVER = "DRIVER"; //$NON-NLS-1$
	public static final String PREFS_NEWS_UPDATE_INTERVAL = "NEWS_UPDATE_INTERVAL"; //$NON-NLS-1$
	public static final String PREFS_HOURS_AS_RECENT = "HOURS_AS_RECENT"; //$NON-NLS-1$
	public static final String PREFS_UPDATE_SECURITIES_NEWS = "UPDATE_SECURITIES_NEWS"; //$NON-NLS-1$
	public static final String PREFS_SUBSCRIBE_PREFIX = "SUBSCRIBE_"; //$NON-NLS-1$
	
	
	 
    public static final String PROP_CODE = "org.eclipsetrader.jessx.code"; //$NON-NLS-1$
    public static final String PROP_ISIN = "org.eclipsetrader.jessx.isin"; 
	
	
	  // The shared instance
    private static JessxActivator plugin;

    
    
    @Override
    public void start(BundleContext context) throws Exception {
       	super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
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
