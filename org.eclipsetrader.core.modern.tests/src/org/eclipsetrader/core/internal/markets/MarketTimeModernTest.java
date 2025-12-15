package org.eclipsetrader.core.internal.markets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;

@RunWith(JUnitPlatform.class)
public class MarketTimeModernTest {

    private final String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    @Test
    void testIsExcludedWithEmptyDates() {
        MarketTime object = new MarketTime(null, null, "Test");
        Assertions.assertFalse(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 5)));
    }

    @Test
    void testIsExcludedSingleDay() {
        MarketTime object = new MarketTime(null, null, "Test");
        object.setExclude(new MarketTimeExclude[] {
                new MarketTimeExclude(getTime(2007, Calendar.NOVEMBER, 6))
        });
        Assertions.assertFalse(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 5)));
        Assertions.assertTrue(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 6)));
        Assertions.assertFalse(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 7)));
    }

    @Test
    void testIsExcludedRange() {
        MarketTime object = new MarketTime(null, null, "Test");
        object.setExclude(new MarketTimeExclude[] {
                new MarketTimeExclude(getTime(2007, Calendar.NOVEMBER, 6), getTime(2007, Calendar.NOVEMBER, 8))
        });
        Assertions.assertFalse(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 5)));
        Assertions.assertTrue(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 6)));
        Assertions.assertTrue(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 7)));
        Assertions.assertTrue(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 8)));
        Assertions.assertFalse(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 9)));
    }

    @Test
    void testMarshalDescription() throws Exception {
        MarketTime object = new MarketTime(null, null, "Test");
        Assertions.assertEquals(prefix + "<time description=\"Test\"/>", marshal(object));
    }

    @Test
    void testUnmarshalDescription() throws Exception {
        MarketTime object = unmarshal(prefix + "<time description=\"Test\"/>");
        Assertions.assertEquals("Test", object.getDescription());
    }

    @Test
    void testMarshalOpenTime() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(2007, Calendar.NOVEMBER, 6, 10, 0, 0);
        MarketTime object = new MarketTime(date.getTime(), null, null);
        Assertions.assertEquals(prefix + "<time open=\"10:00\"/>", marshal(object));
    }

    @Test
    void testUnmarshalOpenTime() throws Exception {
        MarketTime object = unmarshal(prefix + "<time open=\"10:00\"/>");
        Assertions.assertEquals("10:00", new TimeAdapter().marshal(object.getOpenTime()));
    }

    @Test
    void testMarshalCloseTime() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(2007, Calendar.NOVEMBER, 6, 10, 30, 0);
        MarketTime object = new MarketTime(null, date.getTime(), null);
        Assertions.assertEquals(prefix + "<time close=\"10:30\"/>", marshal(object));
    }

    @Test
    void testUnmarshalCloseTime() throws Exception {
        MarketTime object = unmarshal(prefix + "<time close=\"10:30\"/>");
        Assertions.assertEquals("10:30", new TimeAdapter().marshal(object.getCloseTime()));
    }

    private String marshal(MarketTime object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(object, string);
        return string.toString();
    }

    private MarketTime unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(MarketTime.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (MarketTime) unmarshaller.unmarshal(new StringReader(string));
    }

    private Date getTime(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
