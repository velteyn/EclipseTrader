package org.eclipsetrader.core.internal.ats;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipsetrader.core.ats.ScriptStrategy;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class TradingSystemModernTest {

    @Test
    void testAddNewInstruments() {
        Security instrument1 = new Security("Test1", new FeedIdentifier("TST1", null));
        Security instrument2 = new Security("Test2", new FeedIdentifier("TST2", null));

        ScriptStrategy strategy = new ScriptStrategy("Test Strategy");
        strategy.setInstruments(new ISecurity[] { instrument1 });

        TradingSystem tradingSystem = new TradingSystem(strategy);
        Assertions.assertEquals(1, tradingSystem.getInstruments().length);

        strategy.setInstruments(new ISecurity[] { instrument1, instrument2 });
        Assertions.assertEquals(2, tradingSystem.getInstruments().length);
    }

    @Test
    void testRemoveOldInstruments() {
        Security instrument1 = new Security("Test1", new FeedIdentifier("TST1", null));
        Security instrument2 = new Security("Test2", new FeedIdentifier("TST2", null));

        ScriptStrategy strategy = new ScriptStrategy("Test Strategy");
        strategy.setInstruments(new ISecurity[] { instrument1, instrument2 });

        TradingSystem tradingSystem = new TradingSystem(strategy);
        Assertions.assertEquals(2, tradingSystem.getInstruments().length);

        strategy.setInstruments(new ISecurity[] { instrument1 });
        Assertions.assertEquals(1, tradingSystem.getInstruments().length);
    }

    @Test
    void testNotifyInstrumentChanges() {
        Security instrument1 = new Security("Test1", new FeedIdentifier("TST1", null));
        Security instrument2 = new Security("Test2", new FeedIdentifier("TST2", null));

        AtomicInteger events = new AtomicInteger(0);
        PropertyChangeListener changeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                events.incrementAndGet();
            }
        };

        ScriptStrategy strategy = new ScriptStrategy("Test Strategy");
        TradingSystem tradingSystem = new TradingSystem(strategy);
        tradingSystem.addPropertyChangeListener(changeListener);

        strategy.setInstruments(new ISecurity[] { instrument1, instrument2 });
        Assertions.assertTrue(events.get() > 0);
    }
}
