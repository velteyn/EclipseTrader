package org.eclipsetrader.core.internal.markets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;

@RunWith(JUnitPlatform.class)
public class TimeAdapterModernTest {

    @Test
    void testMarshal() throws Exception {
        Assertions.assertEquals("15:30", new TimeAdapter().marshal(getTime(15, 30)));
    }

    @Test
    void testMarshalNull() throws Exception {
        Assertions.assertNull(new TimeAdapter().marshal(null));
    }

    @Test
    void testUnmarshal() throws Exception {
        Assertions.assertEquals(getTime(15, 30), new TimeAdapter().unmarshal("15:30"));
    }

    @Test
    void testUnmarshalNull() throws Exception {
        Assertions.assertNull(new TimeAdapter().unmarshal(null));
    }

    private Date getTime(int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(0);
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, 0);
        return date.getTime();
    }
}
