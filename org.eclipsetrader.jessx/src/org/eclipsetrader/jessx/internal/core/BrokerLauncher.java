package org.eclipsetrader.jessx.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.ILauncher;

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
        BrokerConnector.getInstance().connect();
    }

    @Override
    public void terminate(IProgressMonitor monitor) {
        BrokerConnector.getInstance().disconnect();
    }
}
