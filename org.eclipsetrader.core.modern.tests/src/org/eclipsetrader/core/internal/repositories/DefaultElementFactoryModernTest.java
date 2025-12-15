package org.eclipsetrader.core.internal.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.eclipsetrader.core.instruments.CurrencyExchange;
import org.eclipsetrader.core.instruments.ICurrencyExchange;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.IStock;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.StoreProperties;

@RunWith(JUnitPlatform.class)
public class DefaultElementFactoryModernTest {

    @Test
    void testDontCreateUnknownObjectType() {
        DefaultElementFactory factory = DefaultElementFactory.getInstance();
        StoreProperties properties = new StoreProperties();
        Assertions.assertNull(factory.createElement(null, properties));
        properties.setProperty(IPropertyConstants.OBJECT_TYPE, StoreProperties.class.getName());
        Assertions.assertNull(factory.createElement(null, properties));
    }

    @Test
    void testCreateSecurity() {
        DefaultElementFactory factory = DefaultElementFactory.getInstance();
        StoreProperties properties = new StoreProperties();
        properties.setProperty(IPropertyConstants.OBJECT_TYPE, ISecurity.class.getName());
        Object object = factory.createElement(null, properties);
        Assertions.assertNotNull(object);
        Assertions.assertTrue(object instanceof Security);
    }

    @Test
    void testCreateCommonStock() {
        DefaultElementFactory factory = DefaultElementFactory.getInstance();
        StoreProperties properties = new StoreProperties();
        properties.setProperty(IPropertyConstants.OBJECT_TYPE, IStock.class.getName());
        Object object = factory.createElement(null, properties);
        Assertions.assertNotNull(object);
        Assertions.assertTrue(object instanceof Stock);
    }

    @Test
    void testCreateCurrencyExchange() {
        DefaultElementFactory factory = DefaultElementFactory.getInstance();
        StoreProperties properties = new StoreProperties();
        properties.setProperty(IPropertyConstants.OBJECT_TYPE, ICurrencyExchange.class.getName());
        Object object = factory.createElement(null, properties);
        Assertions.assertNotNull(object);
        Assertions.assertTrue(object instanceof CurrencyExchange);
    }
}
