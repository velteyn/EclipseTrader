
package org.eclipsetrader.jessx.business;

import java.util.Date;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.jessx.internal.JessxActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class JessxTradeHistory {

    public static void saveDeal(Deal deal) {
        BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
        if (serviceReference != null) {
            final IRepositoryService repositoryService = (IRepositoryService) context.getService(serviceReference);

            final Deal finalDeal = deal;

            repositoryService.runInService(new IRepositoryRunnable() {
                public IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor) {
                    IStore store = repositoryService.getRepositories()[0].createObject();
                    IStoreProperties properties = store.fetchProperties(monitor);
                    properties.setProperty(IPropertyConstants.PURCHASE_DATE, new Date(finalDeal.getTimestamp()));
                    properties.setProperty(IPropertyConstants.SECURITY, finalDeal.getSecurity());
                    properties.setProperty(IPropertyConstants.PURCHASE_QUANTITY, (long) finalDeal.getQuantity());
                    properties.setProperty(IPropertyConstants.PURCHASE_PRICE, (double) finalDeal.getDealPrice());
                    store.putProperties(properties, monitor);
                    return Status.OK_STATUS;
                };
            }, null);

            context.ungetService(serviceReference);
        }
    }
}
