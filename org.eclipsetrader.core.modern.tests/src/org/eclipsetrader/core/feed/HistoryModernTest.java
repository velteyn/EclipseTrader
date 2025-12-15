package org.eclipsetrader.core.feed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

@RunWith(JUnitPlatform.class)
public class HistoryModernTest {

    private static java.util.Date t(int y, int m, int d) {
        Calendar c = Calendar.getInstance();
        c.set(y, m, d, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
    private static java.util.Date t(int y, int m, int d, int hh, int mm) {
        Calendar c = Calendar.getInstance();
        c.set(y, m, d, hh, mm, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    @Test
    void testBasicsFirstLastHighLow() {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(t(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(t(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(t(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
        };
        History history = new History(new Security("Test", null), bars);
        Assertions.assertSame(bars[2], history.getFirst());
        Assertions.assertSame(bars[0], history.getLast());
        Assertions.assertSame(bars[2], history.getHighest());
        Assertions.assertSame(bars[1], history.getLowest());
    }

    @Test
    void testGetSubset() {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(t(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
            new OHLC(t(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(t(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(t(2007, Calendar.NOVEMBER, 14), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(t(2007, Calendar.NOVEMBER, 15), 200.0, 210.0, 190.0, 195.0, 100000L),
        };
        History history = new History(new Security("Test", null), bars);
        IHistory subset = history.getSubset(t(2007, Calendar.NOVEMBER, 12), t(2007, Calendar.NOVEMBER, 14));
        Assertions.assertEquals(3, subset.getOHLC().length);
        Assertions.assertSame(bars[1], subset.getOHLC()[0]);
        Assertions.assertSame(bars[2], subset.getOHLC()[1]);
        Assertions.assertSame(bars[3], subset.getOHLC()[2]);
    }

    @Test
    void testAggregatedSubsetFromStoreSingleDay() {
        StoreProperties dayProps = new StoreProperties();
        dayProps.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
        dayProps.setProperty(IPropertyConstants.BARS_DATE, t(2008, Calendar.MAY, 22));
        IOHLC[] bars = new IOHLC[] {
            new OHLC(t(2008, Calendar.MAY, 22, 9, 5), 26.55, 26.6, 26.51, 26.52, 35083L),
            new OHLC(t(2008, Calendar.MAY, 22, 9, 6), 26.52, 26.52, 26.47, 26.47, 41756L),
            new OHLC(t(2008, Calendar.MAY, 22, 9, 7), 26.47, 26.47, 26.37, 26.39, 144494L),
        };
        dayProps.setProperty(TimeSpan.minutes(1).toString(), bars);
        TestStore historyStore = new TestStore(new StoreProperties(), new IStore[] { new TestStore(dayProps, null) });
        History history = new History(historyStore, historyStore.fetchProperties(null));
        IHistory subset = history.getSubset(t(2008, Calendar.MAY, 22), t(2008, Calendar.MAY, 22), TimeSpan.minutes(1));
        Assertions.assertEquals(3, subset.getOHLC().length);
        Assertions.assertSame(bars[0], subset.getOHLC()[0]);
        Assertions.assertSame(bars[1], subset.getOHLC()[1]);
        Assertions.assertSame(bars[2], subset.getOHLC()[2]);
    }

    @Test
    void testSplitsAndDividendsAdjustments() {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(t(2003, Calendar.FEBRUARY, 14), 47.25, 48.50, 46.77, 48.30, 90446400L),
            new OHLC(t(2003, Calendar.FEBRUARY, 18), 24.62, 24.99, 24.40, 24.96, 57415500L),
            new OHLC(t(2003, Calendar.FEBRUARY, 19), 24.82, 24.88, 24.17, 24.53, 46902700L),
        };
        ISplit[] splits = new ISplit[] { new Split(t(2003, Calendar.FEBRUARY, 18), 1.0, 2.0) };
        IDividend[] dividends = new IDividend[] { new Dividend(t(2003, Calendar.FEBRUARY, 19), 0.08) };
        Stock security = new Stock("Test", null, Currency.getInstance("USD"));
        security.setDividends(dividends);
        History history = new History(security, bars, splits, null);
        IOHLC[] adjusted = history.getAdjustedOHLC();
        Assertions.assertEquals(3, adjusted.length);
        Assertions.assertEquals(new OHLC(t(2003, Calendar.FEBRUARY, 14), 47.25 / 2 - 0.08, 48.50 / 2 - 0.08, 46.77 / 2 - 0.08, 48.30 / 2 - 0.08, 90446400L * 2), adjusted[0]);
        Assertions.assertEquals(new OHLC(t(2003, Calendar.FEBRUARY, 18), 24.62 - 0.08, 24.99 - 0.08, 24.40 - 0.08, 24.96 - 0.08, 57415500L), adjusted[1]);
        Assertions.assertEquals(new OHLC(t(2003, Calendar.FEBRUARY, 19), 24.82, 24.88, 24.17, 24.53, 46902700L), adjusted[2]);
    }

    @Test
    void testNotifyUpdatesOfIntradaySubsets() {
        StoreProperties day22 = new StoreProperties();
        day22.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
        day22.setProperty(IPropertyConstants.BARS_DATE, t(2008, Calendar.MAY, 22));
        IOHLC[] bars22 = new IOHLC[] {
            new OHLC(t(2008, Calendar.MAY, 22, 9, 5), 26.55, 26.6, 26.51, 26.52, 35083L),
            new OHLC(t(2008, Calendar.MAY, 22, 9, 6), 26.52, 26.52, 26.47, 26.47, 41756L),
            new OHLC(t(2008, Calendar.MAY, 22, 9, 7), 26.47, 26.47, 26.37, 26.39, 144494L),
        };
        day22.setProperty(TimeSpan.minutes(1).toString(), bars22);

        StoreProperties day23 = new StoreProperties();
        day23.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
        day23.setProperty(IPropertyConstants.BARS_DATE, t(2008, Calendar.MAY, 23));
        IOHLC[] bars23 = new IOHLC[] {
            new OHLC(t(2008, Calendar.MAY, 23, 9, 5), 26.55, 26.6, 26.51, 26.52, 35083L),
            new OHLC(t(2008, Calendar.MAY, 23, 9, 6), 26.52, 26.52, 26.47, 26.47, 41756L),
            new OHLC(t(2008, Calendar.MAY, 23, 9, 7), 26.47, 26.47, 26.37, 26.39, 144494L),
        };
        day23.setProperty(TimeSpan.minutes(1).toString(), bars23);

        TestStore historyStore = new TestStore(new StoreProperties(), new IStore[] {
            new TestStore(day22, null),
            new TestStore(day23, null),
        });
        History history = new History(historyStore, historyStore.fetchProperties(null));

        final Set<IHistory> updates = new HashSet<IHistory>();
        IHistory subset1 = history.getSubset(t(2008, Calendar.MAY, 22), t(2008, Calendar.MAY, 23), TimeSpan.minutes(1));
        Assertions.assertEquals(6, subset1.getOHLC().length);
        PropertyChangeSupport pcs1 = (PropertyChangeSupport) subset1.getAdapter(PropertyChangeSupport.class);
        pcs1.addPropertyChangeListener(IPropertyConstants.BARS, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updates.add((IHistory) evt.getSource());
            }
        });
        IHistory subset2 = history.getSubset(t(2008, Calendar.MAY, 23), t(2008, Calendar.MAY, 23), TimeSpan.minutes(1));
        Assertions.assertEquals(3, subset2.getOHLC().length);
        PropertyChangeSupport pcs2 = (PropertyChangeSupport) subset2.getAdapter(PropertyChangeSupport.class);
        pcs2.addPropertyChangeListener(IPropertyConstants.BARS, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updates.add((IHistory) evt.getSource());
            }
        });
        ((HistoryDay) subset2).setOHLC(new IOHLC[] {
            new OHLC(t(2008, Calendar.MAY, 23, 9, 5), 26.55, 26.6, 26.51, 26.52, 35083L),
            new OHLC(t(2008, Calendar.MAY, 23, 9, 6), 26.52, 26.52, 26.47, 26.47, 41756L),
            new OHLC(t(2008, Calendar.MAY, 23, 9, 7), 26.47, 26.47, 26.37, 26.39, 144494L),
            new OHLC(t(2008, Calendar.MAY, 23, 9, 9), 26.47, 26.47, 26.37, 26.39, 144494L),
        });
        Assertions.assertEquals(4, subset2.getOHLC().length);
        Assertions.assertEquals(7, subset1.getOHLC().length);
        Assertions.assertEquals(2, updates.size());
        Assertions.assertTrue(updates.contains(subset1));
        Assertions.assertTrue(updates.contains(subset2));
    }

    @Test
    void testSameSubsetInstance() {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(t(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(t(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(t(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
        };
        History history = new History(new Security("Test", null), bars);
        IHistory subset1 = history.getSubset(t(2007, Calendar.NOVEMBER, 12), t(2007, Calendar.NOVEMBER, 13));
        IHistory subset2 = history.getSubset(t(2007, Calendar.NOVEMBER, 12), t(2007, Calendar.NOVEMBER, 13));
        Assertions.assertSame(subset1, subset2);
    }

    static class TestStore implements IStore {
        private IStoreProperties storeProperties;
        private IStore[] childs;
        TestStore(IStoreProperties storeProperties, IStore[] childs) {
            this.storeProperties = storeProperties;
            this.childs = childs != null ? childs : new IStore[0];
        }
        @Override
        public void delete(org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException { }
        @Override
        public IStoreProperties fetchProperties(org.eclipse.core.runtime.IProgressMonitor monitor) { return storeProperties; }
        @Override
        public IStore[] fetchChilds(org.eclipse.core.runtime.IProgressMonitor monitor) { return childs; }
        @Override
        public IStore createChild() { return null; }
        @Override
        public IRepository getRepository() { return null; }
        @Override
        public void putProperties(IStoreProperties properties, org.eclipse.core.runtime.IProgressMonitor monitor) { this.storeProperties = properties; }
        @Override
        public URI toURI() { return null; }
    }
}
