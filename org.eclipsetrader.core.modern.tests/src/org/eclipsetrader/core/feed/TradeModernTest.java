package org.eclipsetrader.core.feed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;

@RunWith(JUnitPlatform.class)
public class TradeModernTest {

    @Test
    void testGetTime() {
        Calendar now = Calendar.getInstance();
        Trade trade = new Trade(now.getTime(), null, null, null);
        Assertions.assertEquals(now.getTime(), trade.getTime());
    }

    @Test
    void testGetPrice() {
        Trade trade = new Trade(null, 14.5, null, null);
        Assertions.assertEquals(14.5, trade.getPrice());
    }

    @Test
    void testGetSize() {
        Trade trade = new Trade(null, null, 15000L, null);
        Assertions.assertEquals(Long.valueOf(15000), trade.getSize());
    }

    @Test
    void testGetVolume() {
        Trade trade = new Trade(null, null, null, 2500000L);
        Assertions.assertEquals(Long.valueOf(2500000), trade.getVolume());
    }

    @Test
    void testSerializable() throws Exception {
        ObjectOutputStream os = new ObjectOutputStream(new ByteArrayOutputStream());
        os.writeObject(new Trade(new Date(), 3.5, 100L, 2500000L));
        os.close();
    }
}
