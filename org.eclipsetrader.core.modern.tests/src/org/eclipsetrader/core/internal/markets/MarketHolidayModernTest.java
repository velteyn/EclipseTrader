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
public class MarketHolidayModernTest {

    private final String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    @Test
    void testMarshalDate() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(2007, Calendar.NOVEMBER, 6, 10, 0, 0);
        MarketHoliday object = new MarketHoliday(date.getTime(), null);
        Assertions.assertEquals(prefix + "<day date=\"2007-11-06\"/>", marshal(object));
    }

    @Test
    void testUnmarshalDate() throws Exception {
        MarketHoliday object = unmarshal(prefix + "<day date=\"2007-11-06\"/>");
        Assertions.assertEquals("2007-11-06", new DateAdapter().marshal(object.getDate()));
    }

    @Test
    void testMarshalDescription() throws Exception {
        MarketHoliday object = new MarketHoliday(null, "Holiday description");
        Assertions.assertEquals(prefix + "<day>Holiday description</day>", marshal(object));
    }

    @Test
    void testUnmarshalDescription() throws Exception {
        MarketHoliday object = unmarshal(prefix + "<day>Holiday description</day>");
        Assertions.assertEquals("Holiday description", object.getDescription());
    }

    @Test
    void testMarshalOpenTime() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(2007, Calendar.NOVEMBER, 6, 10, 0, 0);
        MarketHoliday object = new MarketHoliday(null, null, date.getTime(), null);
        Assertions.assertEquals(prefix + "<day open=\"10:00\"/>", marshal(object));
    }

    @Test
    void testUnmarshalOpenTime() throws Exception {
        MarketHoliday object = unmarshal(prefix + "<day open=\"10:00\"/>");
        Assertions.assertEquals("10:00", new TimeAdapter().marshal(object.getOpenTime()));
    }

    @Test
    void testGetCombinedDateAndOpenTime() {
        MarketHoliday object = new MarketHoliday(getTime(2007, 11, 1), "Test", getTime(10, 0), getTime(16, 0));
        Assertions.assertEquals(getTime(2007, 11, 1, 10, 0), object.getOpenTime());
    }

    @Test
    void testGetCombinedDateAndCloseTime() {
        MarketHoliday object = new MarketHoliday(getTime(2007, 11, 1), "Test", getTime(10, 0), getTime(16, 0));
        Assertions.assertEquals(getTime(2007, 11, 1, 16, 0), object.getCloseTime());
    }

    @Test
    void testMarshalCloseTime() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(2007, Calendar.NOVEMBER, 6, 10, 30, 0);
        MarketHoliday object = new MarketHoliday(null, null, null, date.getTime());
        Assertions.assertEquals(prefix + "<day close=\"10:30\"/>", marshal(object));
    }

    @Test
    void testUnmarshalCloseTime() throws Exception {
        MarketHoliday object = unmarshal(prefix + "<day close=\"10:30\"/>");
        Assertions.assertEquals("10:30", new TimeAdapter().marshal(object.getCloseTime()));
    }

    private String marshal(MarketHoliday object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(object, string);
        return string.toString();
    }

    private MarketHoliday unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(MarketHoliday.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (MarketHoliday) unmarshaller.unmarshal(new StringReader(string));
    }

    private Date getTime(int year, int month, int day, int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, hour, minute, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private Date getTime(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private Date getTime(int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
