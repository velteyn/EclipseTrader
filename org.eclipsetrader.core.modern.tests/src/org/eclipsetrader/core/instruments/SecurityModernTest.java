package org.eclipsetrader.core.instruments;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;

@RunWith(JUnitPlatform.class)
public class SecurityModernTest {

    @Test
    void testCreateFromProperties() {
        StoreProperties properties = new StoreProperties();
        properties.setProperty(IPropertyConstants.NAME, "Security");
        properties.setProperty(IPropertyConstants.IDENTIFIER, new FeedIdentifier("ID", null));
        properties.setProperty(IPropertyConstants.USER_PROPERTIES, new UserProperties());
        Security security = new Security(null, properties);
        Assertions.assertEquals(properties.getProperty(IPropertyConstants.NAME), security.getName());
        Assertions.assertSame(properties.getProperty(IPropertyConstants.IDENTIFIER), security.getIdentifier());
        Assertions.assertSame(properties.getProperty(IPropertyConstants.USER_PROPERTIES), security.getProperties());
    }

    @Test
    void testFillObjectTypeProperty() {
        Security security = new Security("X", null);
        IStoreProperties properties = security.getStoreProperties();
        Assertions.assertEquals(ISecurity.class.getName(), properties.getProperty(IPropertyConstants.OBJECT_TYPE));
    }

    @Test
    void testFillNameProperty() {
        Security security = new Security("MSFT", null);
        IStoreProperties properties = security.getStoreProperties();
        Assertions.assertEquals("MSFT", properties.getProperty(IPropertyConstants.NAME));
    }

    @Test
    void testFillIdentifierProperty() {
        FeedIdentifier identifier = new FeedIdentifier("ID", null);
        Security security = new Security(null, identifier);
        IStoreProperties properties = security.getStoreProperties();
        Assertions.assertSame(identifier, properties.getProperty(IPropertyConstants.IDENTIFIER));
    }

    @Test
    void testDontFillFactoryProperty() {
        Security security = new Security("X", null);
        IStoreProperties properties = security.getStoreProperties();
        Assertions.assertNull(properties.getProperty(IPropertyConstants.ELEMENT_FACTORY));
    }

    @Test
    void testKeepExistingPropertiesSet() {
        StoreProperties properties = new StoreProperties();
        Security security = new Security(null, properties);
        Assertions.assertSame(properties, security.getStoreProperties());
    }

    @Test
    void testReturnSamePropertiesSet() {
        Security security = new Security("X", null);
        IStoreProperties properties = security.getStoreProperties();
        Assertions.assertSame(properties, security.getStoreProperties());
    }
}
