package org.eclipsetrader.core.internal.markets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class TimeZoneAdapterModernTest {

    @Test
    void testMarshal() throws Exception {
        Assertions.assertEquals("America/New_York", new TimeZoneAdapter().marshal(TimeZone.getTimeZone("America/New_York")));
    }

    @Test
    void testMarshalNull() throws Exception {
        Assertions.assertNull(new TimeZoneAdapter().marshal(null));
    }

    @Test
    void testUnmarshal() throws Exception {
        Assertions.assertEquals(TimeZone.getTimeZone("America/New_York"), new TimeZoneAdapter().unmarshal("America/New_York"));
    }

    @Test
    void testUnmarshalNull() throws Exception {
        Assertions.assertNull(new TimeZoneAdapter().unmarshal(null));
    }
}
