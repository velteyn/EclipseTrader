package org.eclipsetrader.core.internal.markets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class MarketTimeExcludeModernTest {

    private final String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    @Test
    void testUnmarshalEmpty() throws Exception {
        MarketTimeExclude object = unmarshal(prefix + "<exclude/>");
        Assertions.assertNull(object.getFromDate());
        Assertions.assertNull(object.getToDate());
    }

    @Test
    void testMarshalSingleDate() throws Exception {
        MarketTimeExclude object = new MarketTimeExclude(getTime(2007, Calendar.NOVEMBER, 6));
        Assertions.assertEquals(prefix + "<exclude date=\"2007-11-06\"/>", marshal(object));
    }

    @Test
    void testUnmarshalSingleDate() throws Exception {
        MarketTimeExclude object = unmarshal(prefix + "<exclude date=\"2007-11-06\"/>");
        Assertions.assertEquals(getTime(2007, Calendar.NOVEMBER, 6), object.getFromDate());
        Assertions.assertEquals(getTime(2007, Calendar.NOVEMBER, 6), object.getToDate());
    }

    @Test
    void testMarshalFromDate() throws Exception {
        MarketTimeExclude object = new MarketTimeExclude(getTime(2007, Calendar.NOVEMBER, 6), null);
        Assertions.assertEquals(prefix + "<exclude from=\"2007-11-06\"/>", marshal(object));
    }

    @Test
    void testUnmarshalFromDate() throws Exception {
        MarketTimeExclude object = unmarshal(prefix + "<exclude from=\"2007-11-06\"/>");
        Assertions.assertEquals(getTime(2007, Calendar.NOVEMBER, 6), object.getFromDate());
        Assertions.assertNull(object.getToDate());
    }

    @Test
    void testMarshalToDate() throws Exception {
        MarketTimeExclude object = new MarketTimeExclude(null, getTime(2007, Calendar.NOVEMBER, 6));
        Assertions.assertEquals(prefix + "<exclude to=\"2007-11-06\"/>", marshal(object));
    }

    @Test
    void testUnmarshalToDate() throws Exception {
        MarketTimeExclude object = unmarshal(prefix + "<exclude to=\"2007-11-06\"/>");
        Assertions.assertNull(object.getFromDate());
        Assertions.assertEquals(getTime(2007, Calendar.NOVEMBER, 6), object.getToDate());
    }

    private Date getTime(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private String marshal(MarketTimeExclude object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(object, string);
        return string.toString();
    }

    private MarketTimeExclude unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(MarketTimeExclude.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (MarketTimeExclude) unmarshaller.unmarshal(new StringReader(string));
    }
}
