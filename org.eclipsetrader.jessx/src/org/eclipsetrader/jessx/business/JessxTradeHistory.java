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
        JessxActivator.log(String.format("[TRADE LIFECYCLE - 2] Persistence service invoked for deal: %d @ %f", deal.getQuantity(), deal.getDealPrice()));

        BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
        if (serviceReference == null) {
            JessxActivator.log("JessxTradeHistory: Could not get IRepositoryService.");
            return;
        }

        final IRepositoryService repositoryService = (IRepositoryService) context.getService(serviceReference);
        if (repositoryService == null) {
            JessxActivator.log("JessxTradeHistory: IRepositoryService is null.");
            context.ungetService(serviceReference);
            return;
        }

        final Deal finalDeal = deal;

        repositoryService.runInService(new IRepositoryRunnable() {
            public IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor) {
                org.eclipsetrader.core.instruments.ISecurity security = finalDeal.getSecurity();
                String assetName = "Unknown";
                if (security == null) {
                    JessxActivator.log("JessxTradeHistory: ISecurity is null, attempting fallback.");
                    Institution institution = BusinessCore.getInstitution(finalDeal.getDealInstitution());
                    if (institution != null) {
                        assetName = institution.getAssetName();
                        security = repositoryService.getSecurityFromName(assetName);
                    }
                }

                if (security == null) {
                    JessxActivator.log("JessxTradeHistory: Fallback failed. Could not find ISecurity for asset: " + assetName);
                    return Status.CANCEL_STATUS;
                }

                org.eclipsetrader.core.repositories.IRepository[] repositories = repositoryService.getRepositories();
                if (repositories.length == 0) {
                    JessxActivator.log("JessxTradeHistory: No repositories found.");
                    return Status.CANCEL_STATUS;
                }

                IStore store = repositories[0].createObject();
                if (store == null) {
                    JessxActivator.log("JessxTradeHistory: Failed to create a new store object.");
                    return Status.CANCEL_STATUS;
                }

                IStoreProperties properties = store.fetchProperties(monitor);
                properties.setProperty(IPropertyConstants.PURCHASE_DATE, new Date(finalDeal.getTimestamp()));
                properties.setProperty(IPropertyConstants.SECURITY, security);
                properties.setProperty(IPropertyConstants.PURCHASE_QUANTITY, (long) finalDeal.getQuantity());
                properties.setProperty(IPropertyConstants.PURCHASE_PRICE, (double) finalDeal.getDealPrice());
                store.putProperties(properties, monitor);

                JessxActivator.log(String.format("[TRADE LIFECYCLE - 3] Deal successfully saved to repository for asset: %s", security.getName()));

                return Status.OK_STATUS;
            };
        }, null);

        context.ungetService(serviceReference);
    }
}
