package org.eclipsetrader.core.feed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class BarGeneratorModernTest {

    @Test
    void testSetInitialValues() {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));
        final List<Object> events = new ArrayList<Object>();
        generator.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                events.add(arg);
            }
        });
        generator.addTrade(new Trade(new Date(0), 1.0, 100L, 1000L));
        IBarOpen barOpen = (IBarOpen) events.get(0);
        Assertions.assertEquals(1.0, barOpen.getOpen());
    }

    @Test
    void testSetHighestValue() {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));
        final List<Object> events = new ArrayList<Object>();
        generator.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                events.add(arg);
            }
        });
        generator.addTrade(new Trade(new Date(0), 1.0, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 1.1, 100L, 1000L));
        generator.forceBarClose();
        IBar bar = (IBar) events.get(1);
        Assertions.assertEquals(1.1, bar.getHigh());
        Assertions.assertEquals(1.0, bar.getLow());
    }

    @Test
    void testSetLowestValue() {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));
        final List<Object> events = new ArrayList<Object>();
        generator.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                events.add(arg);
            }
        });
        generator.addTrade(new Trade(new Date(0), 1.0, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 0.9, 100L, 1000L));
        generator.forceBarClose();
        IBar bar = (IBar) events.get(1);
        Assertions.assertEquals(1.0, bar.getHigh());
        Assertions.assertEquals(0.9, bar.getLow());
    }

    @Test
    void testSetCloseToLatestTrade() {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));
        final List<Object> events = new ArrayList<Object>();
        generator.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                events.add(arg);
            }
        });
        generator.addTrade(new Trade(new Date(0), 1.0, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 1.1, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 0.9, 100L, 1000L));
        generator.forceBarClose();
        IBar bar = (IBar) events.get(1);
        Assertions.assertEquals(0.9, bar.getClose());
    }

    @Test
    void testSetOpenToFirstTrade() {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));
        final List<Object> events = new ArrayList<Object>();
        generator.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                events.add(arg);
            }
        });
        generator.addTrade(new Trade(new Date(0), 1.0, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 1.1, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 0.9, 100L, 1000L));
        generator.forceBarClose();
        IBar bar = (IBar) events.get(1);
        Assertions.assertEquals(1.0, bar.getOpen());
    }

    @Test
    void testAddTradeGeneratesBarOpen() {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));
        final List<Object> events = new ArrayList<Object>();
        generator.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                events.add(arg);
            }
        });
        generator.addTrade(new Trade(new Date(0 * 1000), 1.0, 100L, 1000L));
        IBarOpen barOpen = (IBarOpen) events.get(0);
        Assertions.assertEquals(new Date(0), barOpen.getDate());
        Assertions.assertEquals(TimeSpan.minutes(1), barOpen.getTimeSpan());
        Assertions.assertEquals(1.0, barOpen.getOpen());
    }

    @Test
    void testAddTradeGeneratesBar() {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));
        generator.addTrade(new Trade(new Date(0 * 1000), 1.0, 100L, 1000L));
        generator.addTrade(new Trade(new Date(30 * 1000), 1.1, 100L, 1000L));
        generator.addTrade(new Trade(new Date(59 * 1000), 0.9, 100L, 1000L));

        final List<Object> events = new ArrayList<Object>();
        generator.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                events.add(arg);
            }
        });
        generator.addTrade(new Trade(new Date(60 * 1000), 1.0, 100L, 1000L));

        IBar bar = (IBar) events.get(0);
        Assertions.assertEquals(new Date(0), bar.getDate());
        Assertions.assertEquals(TimeSpan.minutes(1), bar.getTimeSpan());
        Assertions.assertEquals(1.0, bar.getOpen());
        Assertions.assertEquals(1.1, bar.getHigh());
        Assertions.assertEquals(0.9, bar.getLow());
        Assertions.assertEquals(0.9, bar.getClose());
        Assertions.assertEquals(Long.valueOf(300), bar.getVolume());
    }

    @Test
    void testDontGenerateBarOnAggregatedTrades() {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));
        generator.addTrade(new Trade(new Date(0 * 1000), 1.0, 100L, 1000L));

        final List<Object> events = new ArrayList<Object>();
        generator.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                events.add(arg);
            }
        });

        generator.addTrade(new Trade(new Date(30 * 1000), 1.1, 100L, 1000L));
        generator.addTrade(new Trade(new Date(59 * 1000), 0.9, 100L, 1000L));

        Assertions.assertEquals(0, events.size());
    }

    @Test
    void testForceBarClose() {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));
        generator.addTrade(new Trade(new Date(0), 1.0, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 1.1, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 0.9, 100L, 1000L));

        final List<Object> events = new ArrayList<Object>();
        generator.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                events.add(arg);
            }
        });

        generator.forceBarClose();

        IBar bar = (IBar) events.get(0);
        Assertions.assertEquals(new Date(0), bar.getDate());
        Assertions.assertEquals(TimeSpan.minutes(1), bar.getTimeSpan());
        Assertions.assertEquals(1.0, bar.getOpen());
        Assertions.assertEquals(1.1, bar.getHigh());
        Assertions.assertEquals(0.9, bar.getLow());
        Assertions.assertEquals(0.9, bar.getClose());
        Assertions.assertEquals(Long.valueOf(300), bar.getVolume());
    }

    @Test
    void testBarNotExpired() {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));
        generator.addTrade(new Trade(new Date(), 1.0, 100L, 1000L));
        Assertions.assertFalse(generator.isBarExpired());
    }

    @Test
    void testBarExpired() {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));
        generator.addTrade(new Trade(new Date(new Date().getTime() - 60 * 1000), 1.0, 100L, 1000L));
        Assertions.assertTrue(generator.isBarExpired());
    }

    @Test
    void testIgnoreTradeWithNullDate() {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));
        final List<Object> events = new ArrayList<Object>();
        generator.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                events.add(arg);
            }
        });
        generator.addTrade(new Trade(null, 1.0, 100L, 1000L));
        Assertions.assertEquals(0, events.size());
    }
}
