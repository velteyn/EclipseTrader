package org.eclipsetrader.core.feed;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

@RunWith(JUnitPlatform.class)
public class QuoteModernTest {

    @Test
    void testSerializable() throws Exception {
        ObjectOutputStream os = new ObjectOutputStream(new ByteArrayOutputStream());
        os.writeObject(new Quote(3.5, 3.6, 100L, 200L));
        os.close();
    }
}
