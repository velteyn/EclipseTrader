package org.eclipsetrader.core.internal.markets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;

@RunWith(JUnitPlatform.class)
public class DateAdapterModernTest {

    @Test
    void testMarshal() throws Exception {
        Assertions.assertEquals("2007-11-05", new DateAdapter().marshal(getTime(2007, Calendar.NOVEMBER, 5)));
    }

    @Test
    void testMarshalNull() throws Exception {
        Assertions.assertNull(new DateAdapter().marshal(null));
    }

    @Test
    void testUnmarshal() throws Exception {
        Assertions.assertEquals(getTime(2007, Calendar.NOVEMBER, 5), new DateAdapter().unmarshal("2007-11-05"));
    }

    @Test
    void testUnmarshalNull() throws Exception {
        Assertions.assertNull(new DateAdapter().unmarshal(null));
    }

    private Date getTime(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
