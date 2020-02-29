package org.eclipsetrader.jessx.internal.core.connector;

import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IDividend;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.ISplit;
import org.eclipsetrader.core.feed.TimeSpan;

public class BackfillConnector implements IBackfillConnector, IExecutableExtension{

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
	public IOHLC[] backfillHistory(IFeedIdentifier identifier, Date from, Date to, TimeSpan timeSpan) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDividend[] backfillDividends(IFeedIdentifier identifier, Date from, Date to) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISplit[] backfillSplits(IFeedIdentifier identifier, Date from, Date to) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canBackfill(IFeedIdentifier identifier, TimeSpan timeSpan) {
		// TODO Auto-generated method stub
		return false;
	}

}
