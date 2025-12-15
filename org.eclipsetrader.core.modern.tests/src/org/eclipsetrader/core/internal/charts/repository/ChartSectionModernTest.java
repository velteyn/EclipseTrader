package org.eclipsetrader.core.internal.charts.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.eclipsetrader.core.charts.repository.IChartSection;
import org.eclipsetrader.core.charts.repository.IElementSection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

@RunWith(JUnitPlatform.class)
public class ChartSectionModernTest {

    private final String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    @Test
    void testMarshalName() throws Exception {
        IChartSection object = new ChartSection(null, "Test");
        Assertions.assertEquals(prefix + "<section name=\"Test\"/>", marshal(object));
    }

    @Test
    void testUnmarshalName() throws Exception {
        IChartSection object = unmarshal(prefix + "<section name=\"Test\"/>");
        Assertions.assertEquals("Test", object.getName());
    }

    @Test
    void testMarshalId() throws Exception {
        IChartSection object = new ChartSection("id1", null);
        Assertions.assertEquals(prefix + "<section id=\"id1\"/>", marshal(object));
    }

    @Test
    void testUnmarshalId() throws Exception {
        IChartSection object = unmarshal(prefix + "<section id=\"id1\"/>");
        Assertions.assertEquals("id1", object.getId());
    }

    @Test
    void testMarshalElement() throws Exception {
        IChartSection object = new ChartSection();
        object.setElements(new IElementSection[] { new ElementSection("id1", null) });
        Assertions.assertEquals(prefix + "<section><element id=\"id1\"/></section>", marshal(object));
    }

    @Test
    void testUnmarshalElement() throws Exception {
        IChartSection object = unmarshal(prefix + "<section><element id=\"id1\"/></section>");
        Assertions.assertEquals(1, object.getElements().length);
    }

    private String marshal(IChartSection object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(object, string);
        return string.toString();
    }

    private IChartSection unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(ChartSection.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (IChartSection) unmarshaller.unmarshal(new StringReader(string));
    }
}
