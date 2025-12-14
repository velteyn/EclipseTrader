package org.eclipsetrader.core.charts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.eclipse.core.runtime.IAdaptable;

import java.util.Calendar;
import java.util.Date;

@RunWith(JUnitPlatform.class)
public class DataSeriesModernTest {

    private Date getTime(int day, int month, int year) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    @Test
    void testFirstLastHighLow() {
        IAdaptable[] sampleValues = new IAdaptable[] {
            new NumberValue(getTime(11, Calendar.NOVEMBER, 2007), 10.0),
            new NumberValue(getTime(12, Calendar.NOVEMBER, 2007), 20.0),
            new NumberValue(getTime(13, Calendar.NOVEMBER, 2007), 5.0),
        };
        DataSeries series = new DataSeries("Test", sampleValues);
        Assertions.assertSame(sampleValues[0], series.getFirst());
        Assertions.assertSame(sampleValues[2], series.getLast());
        Assertions.assertSame(sampleValues[1], series.getHighest());
        Assertions.assertSame(sampleValues[2], series.getLowest());
    }

    @Test
    void testGetSeriesRange() {
        IAdaptable[] sampleValues = new IAdaptable[] {
            new NumberValue(getTime(11, Calendar.NOVEMBER, 2007), 10.0),
            new NumberValue(getTime(12, Calendar.NOVEMBER, 2007), 20.0),
            new NumberValue(getTime(13, Calendar.NOVEMBER, 2007), 5.0),
            new NumberValue(getTime(14, Calendar.NOVEMBER, 2007), 15.0),
            new NumberValue(getTime(15, Calendar.NOVEMBER, 2007), 10.0),
        };
        DataSeries series = new DataSeries("Test", sampleValues);
        IDataSeries subSeries = series.getSeries(sampleValues[1], sampleValues[3]);
        IAdaptable[] values = subSeries.getValues();
        Assertions.assertEquals(3, values.length);
        Assertions.assertSame(sampleValues[1], values[0]);
        Assertions.assertSame(sampleValues[2], values[1]);
        Assertions.assertSame(sampleValues[3], values[2]);
    }

    @Test
    void testCrossAbove() {
        IAdaptable[] sampleValues1 = new IAdaptable[] {
            new NumberValue(getTime(11, Calendar.NOVEMBER, 2007), 3.7692),
            new NumberValue(getTime(12, Calendar.NOVEMBER, 2007), 3.7794),
        };
        DataSeries series1 = new DataSeries("Test1", sampleValues1);

        IAdaptable[] sampleValues2 = new IAdaptable[] {
            new NumberValue(getTime(11, Calendar.NOVEMBER, 2007), 3.7762),
            new NumberValue(getTime(12, Calendar.NOVEMBER, 2007), 3.7793),
        };
        DataSeries series2 = new DataSeries("Test2", sampleValues2);

        int result = series1.cross(series2, new NumberValue(getTime(12, Calendar.NOVEMBER, 2007), 3.7794));
        Assertions.assertEquals(IDataSeries.ABOVE, result);
    }
}
