package org.eclipsetrader.jessx.internal.core.connector;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IDividend;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.ISplit;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.TimeSpan.Units;
import org.eclipsetrader.jessx.internal.JessxActivator;

public class BackfillConnector implements IBackfillConnector, IExecutableExtension{
	
	 private String id;
	 private String name;

	 private String host = "localhost"; //$NON-NLS-1$
	 private NumberFormat nf = NumberFormat.getInstance(Locale.US);
	 private SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss"); //$NON-NLS-1$

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		id = config.getAttribute("id"); //$NON-NLS-1$
        name = config.getAttribute("name"); //$NON-NLS-1$
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
	public IOHLC[] backfillHistory(IFeedIdentifier identifier, Date from, Date to, TimeSpan timeSpan) {
		System.out.println("EDOZ TODO !! backfillHistory to be inplemented !!!");
		 List<OHLC> list = new ArrayList<OHLC>();
		return list.toArray(new IOHLC[list.size()]);
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
        String code = identifier.getSymbol();
        String isin = null;

        IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
        if (properties != null) {
            if (properties.getProperty(JessxActivator.PROP_ISIN) != null) {
                isin = properties.getProperty(JessxActivator.PROP_ISIN);
            }
            if (properties.getProperty(JessxActivator.PROP_CODE) != null) {
                code = properties.getProperty(JessxActivator.PROP_CODE);
            }
        }

        if (code == null || isin == null) {
            return false;
        }

        if (timeSpan.getUnits() == Units.Days && timeSpan.getLength() != 1) {
            return false;
        }
        if (timeSpan.getUnits() != Units.Days && timeSpan.getUnits() != Units.Minutes) {
            return false;
        }

        return true;
	}

}
