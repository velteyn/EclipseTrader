package org.eclipsetrader.jessx.internal.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;

public class FeedIdentifierFactory implements IDataProviderFactory, IExecutableExtension, IExecutableExtensionFactory{

	@Override
	public Object create() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

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
	public IDataProvider createProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class[] getType() {
		// TODO Auto-generated method stub
		return null;
	}

}
