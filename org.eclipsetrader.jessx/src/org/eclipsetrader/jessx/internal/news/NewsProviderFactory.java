package org.eclipsetrader.jessx.internal.news;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

public class NewsProviderFactory implements IExecutableExtensionFactory, IExecutableExtension {

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object create() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
