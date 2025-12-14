package org.eclipsetrader.core.internal.charts.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.eclipsetrader.core.charts.repository.IElementSection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

@RunWith(JUnitPlatform.class)
public class ElementSectionModernTest {

    private final String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    @Test
    void testMarshalId() throws Exception {
        IElementSection object = new ElementSection("id1", null);
        Assertions.assertEquals(prefix + "<element id=\"id1\"/>", marshal(object));
    }

    @Test
    void testUnmarshalId() throws Exception {
        IElementSection object = unmarshal(prefix + "<element id=\"id1\"/>");
        Assertions.assertEquals("id1", object.getId());
    }

    @Test
    void testMarshalPluginId() throws Exception {
        IElementSection object = new ElementSection(null, "plug1");
        Assertions.assertEquals(prefix + "<element plugin-id=\"plug1\"/>", marshal(object));
    }

    @Test
    void testUnmarshalPluginId() throws Exception {
        IElementSection object = unmarshal(prefix + "<element plugin-id=\"plug1\"/>");
        Assertions.assertEquals("plug1", object.getPluginId());
    }

    private String marshal(IElementSection object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(object, string);
        return string.toString();
    }

    private IElementSection unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(ElementSection.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (IElementSection) unmarshaller.unmarshal(new StringReader(string));
    }
}
