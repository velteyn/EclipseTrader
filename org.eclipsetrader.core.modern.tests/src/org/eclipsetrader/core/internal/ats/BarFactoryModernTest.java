package org.eclipsetrader.core.internal.ats;

import java.util.Calendar;
import java.util.Date;

import org.eclipsetrader.core.ats.BarFactoryEvent;
import org.eclipsetrader.core.ats.IBarFactoryListener;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.instruments.Security;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import java.util.ArrayList;

@RunWith(JUnitPlatform.class)
public class BarFactoryModernTest {

    @Test
    void testSetInitialValues() {
        Security security = new Security("Test", new FeedIdentifier("TST", null));
        BarFactory factory = new BarFactory();
        PricingEnvironment pricingEnvironment = new PricingEnvironment();
        pricingEnvironment.addPricingListener(new IPricingListener() {
            @Override
            public void pricingUpdate(PricingEvent event) {
                factory.pricingUpdate(event);
            }
        });
        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.MILLISECOND, 0);

        final java.util.List<BarFactoryEvent> events = new java.util.ArrayList<>();
        IBarFactoryListener listener = new IBarFactoryListener() {
            @Override
            public void barOpen(BarFactoryEvent event) {
                events.add(event);
            }
            @Override
            public void barClose(BarFactoryEvent event) {
                events.add(event);
            }
        };
        factory.addBarFactoryListener(listener);
        factory.add(security, TimeSpan.minutes(1));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        BarFactoryEvent openEvent = events.get(0);
        Assertions.assertEquals(1.0, openEvent.open);
        factory.dispose();
    }

    @Test
    void testSetHighestValue() {
        Security security = new Security("Test", new FeedIdentifier("TST", null));
        BarFactory factory = new BarFactory();
        PricingEnvironment pricingEnvironment = new PricingEnvironment();
        pricingEnvironment.addPricingListener(event -> factory.pricingUpdate(event));
        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.MILLISECOND, 0);

        final java.util.List<BarFactoryEvent> events = new java.util.ArrayList<>();
        IBarFactoryListener listener = new IBarFactoryListener() {
            @Override
            public void barOpen(BarFactoryEvent event) {
            }
            @Override
            public void barClose(BarFactoryEvent event) {
                events.add(event);
            }
        };
        factory.addBarFactoryListener(listener);
        factory.add(security, TimeSpan.minutes(1));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        pricingEnvironment.setTrade(security, new Trade(new Date(System.currentTimeMillis()), 1.1, 100L, 1000L));
        currentTime.add(Calendar.SECOND, 60);
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        BarFactoryEvent closeEvent = events.get(0);
        Assertions.assertEquals(1.1, closeEvent.high);
        Assertions.assertEquals(1.0, closeEvent.low);
        factory.dispose();
    }

    @Test
    void testSetLowestValue() {
        Security security = new Security("Test", new FeedIdentifier("TST", null));
        BarFactory factory = new BarFactory();
        PricingEnvironment pricingEnvironment = new PricingEnvironment();
        pricingEnvironment.addPricingListener(event -> factory.pricingUpdate(event));
        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.MILLISECOND, 0);

        final java.util.List<BarFactoryEvent> events = new java.util.ArrayList<>();
        IBarFactoryListener listener = new IBarFactoryListener() {
            @Override
            public void barOpen(BarFactoryEvent event) {
            }
            @Override
            public void barClose(BarFactoryEvent event) {
                events.add(event);
            }
        };
        factory.addBarFactoryListener(listener);
        factory.add(security, TimeSpan.minutes(1));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 0.9, 100L, 1000L));
        currentTime.add(Calendar.SECOND, 60);
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        BarFactoryEvent closeEvent = events.get(0);
        Assertions.assertEquals(1.0, closeEvent.high);
        Assertions.assertEquals(0.9, closeEvent.low);
        factory.dispose();
    }

    @Test
    void testSetCloseToLatestTrade() {
        Security security = new Security("Test", new FeedIdentifier("TST", null));
        BarFactory factory = new BarFactory();
        PricingEnvironment pricingEnvironment = new PricingEnvironment();
        pricingEnvironment.addPricingListener(event -> factory.pricingUpdate(event));
        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.MILLISECOND, 0);

        final java.util.List<BarFactoryEvent> events = new java.util.ArrayList<>();
        IBarFactoryListener listener = new IBarFactoryListener() {
            @Override
            public void barOpen(BarFactoryEvent event) {
            }
            @Override
            public void barClose(BarFactoryEvent event) {
                events.add(event);
            }
        };
        factory.addBarFactoryListener(listener);
        factory.add(security, TimeSpan.minutes(1));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.1, 100L, 1000L));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 0.9, 100L, 1000L));
        currentTime.add(Calendar.SECOND, 60);
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        BarFactoryEvent closeEvent = events.get(0);
        Assertions.assertEquals(0.9, closeEvent.close);
        factory.dispose();
    }

    @Test
    void testSetOpenToFirstTrade() {
        Security security = new Security("Test", new FeedIdentifier("TST", null));
        BarFactory factory = new BarFactory();
        PricingEnvironment pricingEnvironment = new PricingEnvironment();
        pricingEnvironment.addPricingListener(event -> factory.pricingUpdate(event));
        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.MILLISECOND, 0);

        final java.util.List<BarFactoryEvent> events = new java.util.ArrayList<>();
        IBarFactoryListener listener = new IBarFactoryListener() {
            @Override
            public void barOpen(BarFactoryEvent event) {
            }
            @Override
            public void barClose(BarFactoryEvent event) {
                events.add(event);
            }
        };
        factory.addBarFactoryListener(listener);
        factory.add(security, TimeSpan.minutes(1));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.1, 100L, 1000L));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 0.9, 100L, 1000L));
        currentTime.add(Calendar.SECOND, 60);
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        BarFactoryEvent closeEvent = events.get(0);
        Assertions.assertEquals(1.0, closeEvent.open);
        factory.dispose();
    }

    @Test
    void testTradesGeneratesBarOpen() {
        Security security = new Security("Test", new FeedIdentifier("TST", null));
        BarFactory factory = new BarFactory();
        PricingEnvironment pricingEnvironment = new PricingEnvironment();
        pricingEnvironment.addPricingListener(event -> factory.pricingUpdate(event));
        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.MILLISECOND, 0);

        final java.util.List<BarFactoryEvent> events = new java.util.ArrayList<>();
        IBarFactoryListener listener = new IBarFactoryListener() {
            @Override
            public void barOpen(BarFactoryEvent event) {
                events.add(event);
            }
            @Override
            public void barClose(BarFactoryEvent event) {
            }
        };
        factory.addBarFactoryListener(listener);

        factory.add(security, TimeSpan.minutes(1));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));

        Assertions.assertEquals(1, events.size());
        factory.dispose();
    }

    @Test
    void testTradesGeneratesBarClose() {
        Security security = new Security("Test", new FeedIdentifier("TST", null));
        BarFactory factory = new BarFactory();
        PricingEnvironment pricingEnvironment = new PricingEnvironment();
        pricingEnvironment.addPricingListener(event -> factory.pricingUpdate(event));
        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.MILLISECOND, 0);

        final java.util.List<BarFactoryEvent> closeEvents = new java.util.ArrayList<>();
        IBarFactoryListener listener = new IBarFactoryListener() {
            @Override
            public void barOpen(BarFactoryEvent event) {
            }
            @Override
            public void barClose(BarFactoryEvent event) {
                closeEvents.add(event);
            }
        };
        factory.addBarFactoryListener(listener);

        factory.add(security, TimeSpan.minutes(1));

        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        currentTime.add(Calendar.SECOND, 30);
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.1, 100L, 1000L));
        currentTime.add(Calendar.SECOND, 29);
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 0.9, 100L, 1000L));
        currentTime.add(Calendar.SECOND, 1);
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));

        Assertions.assertEquals(1, closeEvents.size());
        factory.dispose();
    }

    @Test
    void testDontGenerateBarOnAggregatedTrades() {
        Security security = new Security("Test", new FeedIdentifier("TST", null));
        BarFactory factory = new BarFactory();
        PricingEnvironment pricingEnvironment = new PricingEnvironment();
        pricingEnvironment.addPricingListener(event -> factory.pricingUpdate(event));
        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.MILLISECOND, 0);

        factory.add(security, TimeSpan.minutes(1));

        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));

        final java.util.List<BarFactoryEvent> openEvents = new java.util.ArrayList<>();
        final java.util.List<BarFactoryEvent> closeEvents = new java.util.ArrayList<>();
        IBarFactoryListener listener = new IBarFactoryListener() {
            @Override
            public void barOpen(BarFactoryEvent event) {
                openEvents.add(event);
            }
            @Override
            public void barClose(BarFactoryEvent event) {
                closeEvents.add(event);
            }
        };
        factory.addBarFactoryListener(listener);

        currentTime.add(Calendar.SECOND, 30);
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.1, 100L, 1000L));
        currentTime.add(Calendar.SECOND, 29);
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 0.9, 100L, 1000L));

        Assertions.assertEquals(0, openEvents.size());
        Assertions.assertEquals(0, closeEvents.size());
        factory.dispose();
    }

    @Test
    void testSetBarCloseTime() {
        Security security = new Security("Test", new FeedIdentifier("TST", null));
        BarFactory factory = new BarFactory();
        PricingEnvironment pricingEnvironment = new PricingEnvironment();
        pricingEnvironment.addPricingListener(event -> factory.pricingUpdate(event));
        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.MILLISECOND, 0);

        factory.add(security, TimeSpan.minutes(1));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        currentTime.add(Calendar.SECOND, 60);
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        factory.dispose();
    }

    @Test
    void testIgnoreTradeWithNullDate() {
        Security security = new Security("Test", new FeedIdentifier("TST", null));
        BarFactory factory = new BarFactory();
        PricingEnvironment pricingEnvironment = new PricingEnvironment();
        pricingEnvironment.addPricingListener(event -> factory.pricingUpdate(event));

        factory.add(security, TimeSpan.minutes(1));
        final java.util.List<BarFactoryEvent> openEvents2 = new java.util.ArrayList<>();
        final java.util.List<BarFactoryEvent> closeEvents2 = new java.util.ArrayList<>();
        IBarFactoryListener listener2 = new IBarFactoryListener() {
            @Override
            public void barOpen(BarFactoryEvent event) {
                openEvents2.add(event);
            }
            @Override
            public void barClose(BarFactoryEvent event) {
                closeEvents2.add(event);
            }
        };
        factory.addBarFactoryListener(listener2);
        pricingEnvironment.setTrade(security, new Trade(null, 1.0, 100L, 1000L));
        Assertions.assertEquals(0, openEvents2.size());
        Assertions.assertEquals(0, closeEvents2.size());
        factory.dispose();
    }
}
