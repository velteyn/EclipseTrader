package org.eclipsetrader.core.internal.charts.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.eclipsetrader.core.charts.repository.IChartTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

@RunWith(JUnitPlatform.class)
public class ChartTemplateModernTest {

    private final String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    @Test
    void testMarshalName() throws Exception {
        ChartTemplate object = new ChartTemplate("Test");
        Assertions.assertEquals(prefix + "<chart><name>Test</name></chart>", marshal(object));
    }

    @Test
    void testUnmarshalName() throws Exception {
        ChartTemplate object = unmarshal(prefix + "<chart><name>Test</name></chart>");
        Assertions.assertEquals("Test", object.getName());
        Assertions.assertEquals(0, object.getSections().length);
    }

    @Test
    void testMarshalSection() throws Exception {
        ChartTemplate object = new ChartTemplate(null);
        object.setSections(new ChartSection[] {
                new ChartSection(null, "Section 1"),
                new ChartSection(null, "Section 2"),
        });
        Assertions.assertEquals(prefix + "<chart><section name=\"Section 1\"/><section name=\"Section 2\"/></chart>", marshal(object));
    }

    @Test
    void testUnmarshalSection() throws Exception {
        IChartTemplate object = unmarshal(prefix + "<chart><section name=\"Section 1\"/><section name=\"Section 2\"/></chart>");
        Assertions.assertEquals(2, object.getSections().length);
    }

    private String marshal(ChartTemplate object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(object, string);
        return string.toString();
    }

    private ChartTemplate unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(ChartTemplate.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (ChartTemplate) unmarshaller.unmarshal(new StringReader(string));
    }
}
