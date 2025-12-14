package org.eclipsetrader.core.feed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.StoreProperties;

import java.util.Calendar;

@RunWith(JUnitPlatform.class)
public class HistoryDayModernTest {

    private static java.util.Date t(int y, int m, int d, int hh, int mm) {
        Calendar c = Calendar.getInstance();
        c.set(y, m, d, hh, mm, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
    private static java.util.Date d(int y, int m, int day) {
        Calendar c = Calendar.getInstance();
        c.set(y, m, day, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    @Test
    void testCreateStoreObjects() {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(t(2008, Calendar.MAY, 22, 9, 3), 26.56, 26.56, 26.56, 26.56, 3043159L),
            new OHLC(t(2008, Calendar.MAY, 23, 9, 3), 26.55, 26.6, 26.51, 26.52, 35083L),
        };
        HistoryDay history = new HistoryDay(null, TimeSpan.minutes(1));
        IStoreObject[] storeObjects = (IStoreObject[]) history.getAdapter(IStoreObject[].class);
        Assertions.assertEquals(0, storeObjects.length);
        history.setOHLC(bars);
        storeObjects = (IStoreObject[]) history.getAdapter(IStoreObject[].class);
        Assertions.assertEquals(2, storeObjects.length);
        Assertions.assertNotNull(storeObjects[0].getStoreProperties().getProperty(TimeSpan.minutes(1).toString()));
        Assertions.assertNotNull(storeObjects[1].getStoreProperties().getProperty(TimeSpan.minutes(1).toString()));
    }

    @Test
    void testFillStoreObjects() {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(t(2008, Calendar.MAY, 22, 9, 3), 26.56, 26.56, 26.56, 26.56, 3043159L),
            new OHLC(t(2008, Calendar.MAY, 23, 9, 3), 26.55, 26.6, 26.51, 26.52, 35083L),
        };
        Security security = new Security("Test", null);
        HistoryDay history = new HistoryDay(security, TimeSpan.minutes(1));
        history.setOHLC(bars);
        IStoreObject[] storeObjects = (IStoreObject[]) history.getAdapter(IStoreObject[].class);
        Assertions.assertSame(security, storeObjects[0].getStoreProperties().getProperty(IPropertyConstants.SECURITY));
        Assertions.assertEquals(d(2008, Calendar.MAY, 22), storeObjects[0].getStoreProperties().getProperty(IPropertyConstants.BARS_DATE));
        IOHLC[] propBars1 = (IOHLC[]) storeObjects[0].getStoreProperties().getProperty(TimeSpan.minutes(1).toString());
        Assertions.assertEquals(1, propBars1.length);
        Assertions.assertSame(bars[0], propBars1[0]);
        Assertions.assertSame(security, storeObjects[1].getStoreProperties().getProperty(IPropertyConstants.SECURITY));
        Assertions.assertEquals(d(2008, Calendar.MAY, 23), storeObjects[1].getStoreProperties().getProperty(IPropertyConstants.BARS_DATE));
        IOHLC[] propBars2 = (IOHLC[]) storeObjects[1].getStoreProperties().getProperty(TimeSpan.minutes(1).toString());
        Assertions.assertEquals(1, propBars2.length);
        Assertions.assertSame(bars[1], propBars2[0]);
    }

    
}
