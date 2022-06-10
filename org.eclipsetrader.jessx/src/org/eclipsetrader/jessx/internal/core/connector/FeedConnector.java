package org.eclipsetrader.jessx.internal.core.connector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;


public class FeedConnector implements IFeedConnector, IExecutableExtension{
	
	private SnapshotConnector connector;

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFeedSubscription subscribe(IFeedIdentifier identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void connect() {
		connector.connect();
	}

	@Override
	public void disconnect() {
		connector.disconnect();
	}

	@Override
	public void addConnectorListener(IConnectorListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeConnectorListener(IConnectorListener listener) {
		// TODO Auto-generated method stub
		
	}

}
