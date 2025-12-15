package org.eclipsetrader.core.feed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class FeedPropertiesModernTest {

    @Test
    void testGetPropertiesIDs() {
        FeedProperties properties = new FeedProperties();
        Assertions.assertEquals(0, properties.getPropertyIDs().length);
        properties.setProperty("p1", "value1");
        Assertions.assertEquals(1, properties.getPropertyIDs().length);
        Assertions.assertEquals("p1", properties.getPropertyIDs()[0]);
    }

    @Test
    void testSetAndGetProperty() {
        FeedProperties properties = new FeedProperties();
        Assertions.assertNull(properties.getProperty("p1"));
        properties.setProperty("p1", "value1");
        Assertions.assertEquals("value1", properties.getProperty("p1"));
    }

    @Test
    void testSetNullValueRemovesProperty() {
        FeedProperties properties = new FeedProperties();
        properties.setProperty("p1", "value1");
        Assertions.assertEquals(1, properties.getPropertyIDs().length);
        properties.setProperty("p1", null);
        Assertions.assertEquals(0, properties.getPropertyIDs().length);
    }
}
