package org.eclipsetrader.jessx.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.ILauncher;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.jessx.internal.JessxActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class BrokerLauncher implements ILauncher {

    public BrokerLauncher() {
    }

    @Override
    public String getId() {
        return "org.eclipsetrader.jessx.broker";
    }

    @Override
    public String getName() {
        return "JESSX Broker";
    }

    @Override
    public void launch(IProgressMonitor monitor) {
        BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
        ServiceReference ref = context.getServiceReference(ITradingService.class.getName());
        if (ref != null) {
            ITradingService trading = (ITradingService) context.getService(ref);
            IBroker broker = trading != null ? trading.getBroker("org.eclipsetrader.brokers.jessx") : null;
            if (broker != null) {
                broker.connect();
            } else {
                BrokerConnector.getInstance().connect();
            }
            context.ungetService(ref);
        } else {
            BrokerConnector.getInstance().connect();
        }
    }

    @Override
    public void terminate(IProgressMonitor monitor) {
        BrokerConnector.getInstance().disconnect();
    }
}
