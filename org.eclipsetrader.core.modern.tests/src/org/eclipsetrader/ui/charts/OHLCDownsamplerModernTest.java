package org.eclipsetrader.ui.charts;

import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Date;

@RunWith(JUnitPlatform.class)
public class OHLCDownsamplerModernTest {

    @Test
    void testNullInput() {
        IOHLC[] result = OHLCDownsampler.downsample((IOHLC[]) null, 10);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.length);
    }

    @Test
    void testEmptyInput() {
        IOHLC[] result = OHLCDownsampler.downsample(new IOHLC[0], 10);
        Assertions.assertEquals(0, result.length);
    }

    @Test
    void testWidthZeroOrNegativeReturnsSource() {
        IOHLC[] source = new IOHLC[] { ohlc(0, 10.0, 11.0, 9.0, 10.5) };
        Assertions.assertSame(source, OHLCDownsampler.downsample(source, 0));
        Assertions.assertSame(source, OHLCDownsampler.downsample(source, -1));
    }

    @Test
    void testWidthExceedsValuesReturnsSource() {
        IOHLC[] source = new IOHLC[] {
            ohlc(0, 10.0, 11.0, 9.0, 10.5),
            ohlc(1, 11.0, 12.0, 10.0, 11.5),
        };
        IOHLC[] result = OHLCDownsampler.downsample(source, 3);
        Assertions.assertSame(source, result);
    }

    @Test
    void testWidthEqualsValuesReturnsSource() {
        IOHLC[] source = new IOHLC[] {
            ohlc(0, 10.0, 11.0, 9.0, 10.5),
            ohlc(1, 11.0, 12.0, 10.0, 11.5),
        };
        IOHLC[] result = OHLCDownsampler.downsample(source, 2);
        Assertions.assertSame(source, result);
    }

    @Test
    void testBoundaryOnePerColumn() {
        IOHLC[] source = new IOHLC[100];
        for (int i = 0; i < 100; i++) {
            source[i] = ohlc(i, 10.0 + i, 20.0 + i, 5.0 + i, 15.0 + i);
        }
        IOHLC[] result = OHLCDownsampler.downsample(source, 7);
        Assertions.assertEquals(7, result.length);
    }

    @Test
    void testPreservesExtremes() {
        Date d1 = new Date(0);
        Date d2 = new Date(1);
        Date d3 = new Date(2);
        Date d4 = new Date(3);

        IOHLC[] source = new IOHLC[] {
            new OHLC(d1, 10.0, 15.0, 9.0, 12.0, null),
            new OHLC(d2, 12.0, 20.0, 8.0, 14.0, null),
            new OHLC(d3, 14.0, 18.0, 10.0, 13.0, null),
            new OHLC(d4, 13.0, 16.0, 11.0, 15.0, null),
        };
        IOHLC[] result = OHLCDownsampler.downsample(source, 2);

        Assertions.assertEquals(2, result.length);

        Assertions.assertEquals(d1, result[0].getDate());
        Assertions.assertEquals(10.0, result[0].getOpen(), 0.001);
        Assertions.assertEquals(20.0, result[0].getHigh(), 0.001);
        Assertions.assertEquals(8.0, result[0].getLow(), 0.001);
        Assertions.assertEquals(14.0, result[0].getClose(), 0.001);

        Assertions.assertEquals(d3, result[1].getDate());
        Assertions.assertEquals(14.0, result[1].getOpen(), 0.001);
        Assertions.assertEquals(18.0, result[1].getHigh(), 0.001);
        Assertions.assertEquals(10.0, result[1].getLow(), 0.001);
        Assertions.assertEquals(15.0, result[1].getClose(), 0.001);
    }

    @Test
    void testSingleValuePerBin() {
        IOHLC[] source = new IOHLC[] {
            ohlc(0, 10.0, 10.5, 9.5, 10.2),
            ohlc(1, 11.0, 11.5, 10.5, 11.2),
            ohlc(2, 12.0, 12.5, 11.5, 12.2),
        };
        IOHLC[] result = OHLCDownsampler.downsample(source, 3);
        Assertions.assertEquals(3, result.length);
        Assertions.assertEquals(10.0, result[0].getOpen(), 0.001);
        Assertions.assertEquals(12.2, result[2].getClose(), 0.001);
    }

    @Test
    void testOddBinningLastColumnSmaller() {
        IOHLC[] source = new IOHLC[5];
        for (int i = 0; i < 5; i++) {
            source[i] = ohlc(i, 10.0 + i, 15.0 + i, 9.0 + i, 12.0 + i);
        }
        IOHLC[] result = OHLCDownsampler.downsample(source, 2);
        Assertions.assertEquals(2, result.length);
        Assertions.assertEquals(10.0, result[0].getOpen(), 0.001);
    }

    private static IOHLC ohlc(long time, double open, double high, double low, double close) {
        return new OHLC(new Date(time), open, high, low, close, null);
    }

    private static IOHLC ohlc(int time, double open, double high, double low, double close) {
        return ohlc((long) time, open, high, low, close);
    }
}
