package org.eclipsetrader.core.feed;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

@RunWith(JUnitPlatform.class)
public class TodayOHLModernTest {

    @Test
    void testSerialize() throws Exception {
        ObjectOutputStream os = new ObjectOutputStream(new ByteArrayOutputStream());
        os.writeObject(new TodayOHL(2.5, 2.8, 2.4));
        os.close();
    }
}
