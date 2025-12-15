package org.eclipsetrader.core.internal.markets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class MarketInternalModernTest {

    private final String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    

    @Test
    void testIsOpen() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 0), getTime(9, 30)),
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 9, 15)));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 11, 0)));
    }

    @Test
    void testIsOpenAtOpenTime() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 9, 30)));
    }

    @Test
    void testIsOpenAtCloseTime() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 16, 0)));
    }

    @Test
    void testIsOpenEarly() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 6, 0)));
    }

    @Test
    void testIsOpenLate() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 17, 0)));
    }

    @Test
    void testIsOpenOnSunday() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setWeekDays(new Integer[] {
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
        });
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 4, 1, 0)));
    }

    @Test
    void testIsOpenOnHoliday() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setHolidays(new MarketHoliday[] {
            new MarketHoliday(getTime(2007, Calendar.DECEMBER, 25), "Holiday")
        });
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 24, 10, 0)));
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.DECEMBER, 25, 10, 0)));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 26, 10, 0)));
    }

    @Test
    void testIsPartiallyOpenOnHoliday() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setHolidays(new MarketHoliday[] {
            new MarketHoliday(getTime(2007, Calendar.DECEMBER, 31, 9, 30), getTime(2007, Calendar.DECEMBER, 31, 12, 30), "Holiday")
        });
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 28, 10, 0)));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 28, 15, 0)));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 31, 10, 0)));
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.DECEMBER, 31, 15, 0)));
    }

    @Test
    void testDontChangeTimeWithTimeZone() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        Assertions.assertEquals(getTime(9, 30), market.getSchedule()[0].getOpenTime());
        Assertions.assertEquals(getTime(16, 0), market.getSchedule()[0].getCloseTime());
        market.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Assertions.assertEquals(getTime(9, 30), market.getSchedule()[0].getOpenTime());
        Assertions.assertEquals(getTime(16, 0), market.getSchedule()[0].getCloseTime());
    }

    @Test
    void testIsOpenWithDifferentTimeZone() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 10, 0)));
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 18, 0)));
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 23, 10, 0)));
    }

    @Test
    void testMarshalDescription() throws Exception {
        Market object = new Market("Test", null, null);
        Assertions.assertEquals(prefix + "<market name=\"Test\"/>", marshal(object));
    }

    @Test
    void testUnmarshalDescription() throws Exception {
        Market object = unmarshal(prefix + "<market name=\"Test\"/>");
        Assertions.assertEquals("Test", object.getName());
    }

    @Test
    void testMarshalOpenTime() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(2007, Calendar.NOVEMBER, 6, 10, 0, 0);
        Market object = new Market(null, Arrays.asList(new MarketTime[] {
            new MarketTime(date.getTime(), null)
        }));
        Assertions.assertEquals(prefix + "<market><schedule><time open=\"10:00\"/></schedule></market>", marshal(object));
    }

    @Test
    void testUnmarshalOpenTime() throws Exception {
        Market object = unmarshal(prefix + "<market><schedule><time open=\"10:00\"/></schedule></market>");
        Assertions.assertEquals("10:00", new TimeAdapter().marshal(object.getSchedule()[0].getOpenTime()));
    }

    

    @Test
    void testMarshalTimeZone() throws Exception {
        Market object = new Market(null, null, TimeZone.getTimeZone("America/New_York"));
        Assertions.assertEquals(prefix + "<market timeZone=\"America/New_York\"/>", marshal(object));
    }

    @Test
    void testUnmarshalTimeZone() throws Exception {
        Market object = unmarshal(prefix + "<market timeZone=\"America/New_York\"/>");
        Assertions.assertEquals("America/New_York", object.getTimeZone().getID());
    }

    @Test
    void testMarshalLiveFeedConnector() throws Exception {
        Market object = new Market(null, null, null);
        object.setLiveFeedConnector(new SimpleFeedConnector("test.id", "Test"));
        Assertions.assertEquals(prefix + "<market><liveFeed id=\"test.id\"/></market>", marshal(object));
    }

    @Test
    void testUnmarshalLiveFeedConnector() throws Exception {
        Market object = unmarshal(prefix + "<market><liveFeed id=\"test.id\"/></market>");
        Assertions.assertNotNull(object.getLiveFeedConnector());
        Assertions.assertEquals("test.id", object.getLiveFeedConnector().getId());
    }

    

    @Test
    void testHolidayIsClosed() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setHolidays(new MarketHoliday[] {
            new MarketHoliday(getTime(2007, Calendar.DECEMBER, 25), "Holiday")
        });
        Assertions.assertFalse(market.isOpen(getTime(2007, Calendar.DECEMBER, 25, 10, 0)));
    }

    @Test
    void testDayAfterHolidayIsOpen() {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setHolidays(new MarketHoliday[] {
            new MarketHoliday(getTime(2007, Calendar.DECEMBER, 25), "Holiday")
        });
        Assertions.assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 26, 10, 0)));
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

    private String marshal(Market object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(object, string);
        return string.toString();
    }

    private Market unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(Market.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (Market) unmarshaller.unmarshal(new StringReader(string));
    }

    static class SimpleFeedConnector implements org.eclipsetrader.core.feed.IFeedConnector {
        private final String id;
        private final String name;
        SimpleFeedConnector(String id, String name) {
            this.id = id;
            this.name = name;
        }
        @Override
        public String getId() { return id; }
        @Override
        public String getName() { return name; }
        @Override
        public org.eclipsetrader.core.feed.IFeedSubscription subscribe(org.eclipsetrader.core.feed.IFeedIdentifier identifier) { return null; }
        @Override
        public void connect() { }
        @Override
        public void disconnect() { }
        @Override
        public void addConnectorListener(org.eclipsetrader.core.feed.IConnectorListener listener) { }
        @Override
        public void removeConnectorListener(org.eclipsetrader.core.feed.IConnectorListener listener) { }
    }
}
