package org.eclipsetrader.core.internal.markets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class MarketListModernTest {

    private final String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    @Test
    void testMarshalEmptyList() throws Exception {
        Assertions.assertEquals(prefix + "<list/>", marshal(new MarketList()));
    }

    @Test
    void testUnmarshalEmptyList() throws Exception {
        MarketList object = unmarshal(prefix + "<list/>");
        Assertions.assertEquals(0, object.getList().size());
    }

    @Test
    void testMarshalList() throws Exception {
        MarketList object = new MarketList();
        object.getList().add(new Market("Test", null, null));
        Assertions.assertEquals(prefix + "<list><market name=\"Test\"/></list>", marshal(object));
    }

    @Test
    void testUnmarshalList() throws Exception {
        MarketList object = unmarshal(prefix + "<list><market name=\"Test\"/></list>");
        Assertions.assertEquals(1, object.getList().size());
        Assertions.assertEquals("Test", object.getList().get(0).getName());
    }

    private String marshal(MarketList object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(MarketList.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(object, string);
        return string.toString();
    }

    private MarketList unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(MarketList.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (MarketList) unmarshaller.unmarshal(new StringReader(string));
    }
}
