package org.eclipsetrader.jessx.internal.core.connector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;

public class FeedConnector implements IFeedConnector, IExecutableExtension {

    private StreamingConnector connector;
    private String id;
    private String name;

    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        name = config.getAttribute("name");
        connector = StreamingConnector.getInstance();
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
    public IFeedSubscription subscribe(IFeedIdentifier identifier) {
        return connector.subscribe(identifier);
    }

    @Override
    public void connect() {
        // The connection is now managed by the BrokerConnector
    }

    public void doConnect() {
        connector.connect();
    }

    @Override
    public void disconnect() {
        connector.disconnect();
    }

    @Override
    public void addConnectorListener(IConnectorListener listener) {
        connector.addConnectorListener(listener);
    }

    @Override
    public void removeConnectorListener(IConnectorListener listener) {
        connector.removeConnectorListener(listener);
    }
}