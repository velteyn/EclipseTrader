package org.eclipsetrader.jessx.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.ILauncher;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.ITradingService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class BrokerLauncher implements ILauncher, IExecutableExtension {

    private String id;
    private String name;

    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        name = config.getAttribute("name");
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
    public void launch(IProgressMonitor monitor) {
        ITradingService service = getTradingService();
        if (service == null) {
            return;
        }
        IBroker[] brokers = service.getBrokers();
        for (IBroker b : brokers) {
            if ("org.eclipsetrader.brokers.jessx".equals(b.getId())) {
                b.connect();
            }
        }
    }

    @Override
    public void terminate(IProgressMonitor monitor) {
        ITradingService service = getTradingService();
        if (service == null) {
            return;
        }
        IBroker[] brokers = service.getBrokers();
        for (IBroker b : brokers) {
            if ("org.eclipsetrader.brokers.jessx".equals(b.getId())) {
                b.disconnect();
            }
        }
    }

    private ITradingService getTradingService() {
        try {
            BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
            ServiceReference<ITradingService> ref = context.getServiceReference(ITradingService.class);
            ITradingService service = context.getService(ref);
            context.ungetService(ref);
            return service;
        }
        catch (Exception e) {
            return null;
        }
    }
}