package org.eclipsetrader.ui.charts;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.charts.NumberValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Date;

@RunWith(JUnitPlatform.class)
public class ScalarDownsamplerModernTest {

    @Test
    void testNullInput() {
        IAdaptable[] result = ScalarDownsampler.downsample(null, 10);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.length);
    }

    @Test
    void testEmptyInput() {
        IAdaptable[] result = ScalarDownsampler.downsample(new IAdaptable[0], 10);
        Assertions.assertEquals(0, result.length);
    }

    @Test
    void testWidthZeroOrNegativeReturnsSource() {
        IAdaptable[] source = new IAdaptable[] { nv(0, 10.0) };
        Assertions.assertSame(source, ScalarDownsampler.downsample(source, 0));
        Assertions.assertSame(source, ScalarDownsampler.downsample(source, -1));
    }

    @Test
    void testWidthExceedsValuesReturnsSource() {
        IAdaptable[] source = new IAdaptable[] { nv(0, 10.0), nv(1, 20.0) };
        IAdaptable[] result = ScalarDownsampler.downsample(source, 5);
        Assertions.assertSame(source, result);
    }

    @Test
    void testWidthEqualsValuesReturnsSource() {
        IAdaptable[] source = new IAdaptable[] { nv(0, 10.0), nv(1, 20.0) };
        IAdaptable[] result = ScalarDownsampler.downsample(source, 2);
        Assertions.assertSame(source, result);
    }

    @Test
    void testBoundaryOnePerColumn() {
        IAdaptable[] source = new IAdaptable[100];
        for (int i = 0; i < 100; i++) {
            source[i] = nv(i, (double) i);
        }
        IAdaptable[] result = ScalarDownsampler.downsample(source, 13);
        Assertions.assertEquals(13, result.length);
    }

    @Test
    void testPicksMaxDeviationPoint() {
        Date d1 = new Date(0);
        Date d2 = new Date(1);
        Date d3 = new Date(2);
        Date d4 = new Date(3);

        IAdaptable[] source = new IAdaptable[] {
            new NumberValue(d1, 10.0),
            new NumberValue(d2, 11.0),
            new NumberValue(d3, 100.0),
            new NumberValue(d4, 12.0),
        };
        IAdaptable[] result = ScalarDownsampler.downsample(source, 2);

        Assertions.assertEquals(2, result.length);
        Assertions.assertEquals(100.0, ((Number) result[0].getAdapter(Number.class)).doubleValue(), 0.001);
    }

    @Test
    void testPreservesDateAndNumberAdapter() {
        Date date = new Date(42);
        IAdaptable[] source = new IAdaptable[] {
            new NumberValue(date, 10.0),
            new NumberValue(new Date(43), 11.0),
            new NumberValue(new Date(44), 12.0),
            new NumberValue(new Date(45), 13.0),
            new NumberValue(new Date(46), 14.0),
        };
        IAdaptable[] result = ScalarDownsampler.downsample(source, 2);

        Assertions.assertEquals(2, result.length);
        for (int i = 0; i < result.length; i++) {
            Assertions.assertNotNull(result[i].getAdapter(Date.class), "column " + i + " should adapt to Date");
            Assertions.assertNotNull(result[i].getAdapter(Number.class), "column " + i + " should adapt to Number");
        }
    }

    @Test
    void testEqualDeviationPicksFirst() {
        IAdaptable[] source = new IAdaptable[] {
            new NumberValue(new Date(0), 0.0),
            new NumberValue(new Date(1), 100.0),
            new NumberValue(new Date(2), 0.0),
        };
        IAdaptable[] result = ScalarDownsampler.downsample(source, 1);
        Assertions.assertEquals(1, result.length);
        Assertions.assertEquals(100.0, ((Number) result[0].getAdapter(Number.class)).doubleValue(), 0.001);
    }

    private static IAdaptable nv(long time, double value) {
        return new NumberValue(new Date(time), value);
    }

    private static IAdaptable nv(int time, double value) {
        return nv((long) time, value);
    }
}
