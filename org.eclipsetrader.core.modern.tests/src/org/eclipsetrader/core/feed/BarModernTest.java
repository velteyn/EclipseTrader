package org.eclipsetrader.core.feed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Date;

@RunWith(JUnitPlatform.class)
public class BarModernTest {

    @Test
    void testHashCode() {
        Date date = new Date();
        Bar o1 = new Bar(date, TimeSpan.days(1), 10.0, 20.0, 30.0, 40.0, 1000L);
        Bar o2 = new Bar(date, TimeSpan.days(1), 10.0, 20.0, 30.0, 40.0, 1000L);
        Assertions.assertEquals(o1.hashCode(), o2.hashCode());
    }

    @Test
    void testEquals() {
        Date date = new Date();
        Bar o1 = new Bar(date, TimeSpan.days(1), 10.0, 20.0, 30.0, 40.0, 1000L);
        Bar o2 = new Bar(date, TimeSpan.days(1), 10.0, 20.0, 30.0, 40.0, 1000L);
        Assertions.assertTrue(o1.equals(o2));
    }
}
