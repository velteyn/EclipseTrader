package org.eclipsetrader.core.internal.markets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(JUnitPlatform.class)
public class MarketServiceModernTest {

    static class CountingObserver implements Observer {
        final AtomicInteger updates = new AtomicInteger(0);
        @Override
        public void update(Observable o, Object arg) {
            updates.incrementAndGet();
        }
    }

    @Test
    void testNotifyObserversOnAddMarket() {
        MarketService service = new MarketService();
        CountingObserver observer = new CountingObserver();
        service.addObserver(observer);
        service.addMarket(new Market("Test", new ArrayList<MarketTime>()));
        Assertions.assertTrue(observer.updates.get() > 0);
    }

    @Test
    void testNotifyObserversOnDeleteMarket() {
        Market market = new Market("Test", new ArrayList<MarketTime>());
        MarketService service = new MarketService();
        service.addMarket(market);

        CountingObserver observer = new CountingObserver();
        service.addObserver(observer);
        service.deleteMarket(market);
        Assertions.assertTrue(observer.updates.get() > 0);
    }
}
