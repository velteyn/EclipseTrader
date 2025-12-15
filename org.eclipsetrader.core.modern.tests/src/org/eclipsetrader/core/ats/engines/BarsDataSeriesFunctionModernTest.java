package org.eclipsetrader.core.ats.engines;

import org.eclipsetrader.core.feed.Bar;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Date;

@RunWith(JUnitPlatform.class)
public class BarsDataSeriesFunctionModernTest {

    @Test
    void testAppendPrependFirstLastSize() {
        BarsDataSeriesFunction fn = new BarsDataSeriesFunction();
        fn.append(new Bar(new Date(1000), org.eclipsetrader.core.feed.TimeSpan.minutes(1), 1.0, 2.0, 0.5, 1.5, 100L));
        fn.append(new Bar(new Date(2000), org.eclipsetrader.core.feed.TimeSpan.minutes(1), 1.6, 2.1, 1.3, 1.8, 200L));
        fn.prepend(new Bar(new Date(500), org.eclipsetrader.core.feed.TimeSpan.minutes(1), 0.9, 1.2, 0.7, 1.0, 50L));
        Assertions.assertEquals(3, fn.jsFunction_size());
        Assertions.assertEquals(new Date(500), ((Bar) fn.jsFunction_first()).getDate());
        Assertions.assertEquals(new Date(2000), ((Bar) fn.jsFunction_last()).getDate());
    }
}
