package org.eclipsetrader.core.feed;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

@RunWith(JUnitPlatform.class)
public class LastCloseModernTest {

    @Test
    void testSerialize() throws Exception {
        ObjectOutputStream os = new ObjectOutputStream(new ByteArrayOutputStream());
        os.writeObject(new LastClose(3.5, new Date()));
        os.close();
    }
}
