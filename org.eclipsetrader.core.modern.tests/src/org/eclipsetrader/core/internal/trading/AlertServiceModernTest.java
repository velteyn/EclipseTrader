package org.eclipsetrader.core.internal.trading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.trading.AbstractAlert;
import org.eclipsetrader.core.trading.AlertEvent;
import org.eclipsetrader.core.trading.IAlert;
import org.eclipsetrader.core.trading.IAlertListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class AlertServiceModernTest {

    @Test
    void testUnknownSecurityHasTriggeredAlerts() {
        AlertService service = new AlertService();
        Assertions.assertFalse(service.hasTriggeredAlerts(new Security("Test", null)));
    }

    @Test
    void testHasTriggeredAlertsWithEmptyList() {
        ISecurity security = new Security("Test", null);
        AlertService service = new AlertService();
        try {
            java.lang.reflect.Field f = AlertService.class.getDeclaredField("triggeredMap");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<ISecurity, java.util.List<IAlert>> tm = (java.util.Map<ISecurity, java.util.List<IAlert>>) f.get(service);
            tm.put(security, new java.util.ArrayList<IAlert>());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Assertions.assertFalse(service.hasTriggeredAlerts(security));
    }

    @Test
    void testFireSingleAlertEvent() {
        IMarketService marketService = new IMarketService() {
            @Override
            public IMarket[] getMarkets() { return new IMarket[0]; }
            @Override
            public IMarket getMarket(String name) { return null; }
            @Override
            public IMarket[] getOpenMarkets() { return new IMarket[0]; }
            @Override
            public IMarket[] getOpenMarkets(java.util.Date time) { return new IMarket[0]; }
            @Override
            public void addMarketStatusListener(org.eclipsetrader.core.markets.IMarketStatusListener listener) {}
            @Override
            public void removeMarketStatusListener(org.eclipsetrader.core.markets.IMarketStatusListener listener) {}
            @Override
            public IMarket getMarketForSecurity(ISecurity security) { return null; }
        };

        MarketPricingEnvironment pricingEnvironment = new MarketPricingEnvironment(marketService);

        ISecurity security = new Security("Test", null);
        IAlert alert = new AbstractAlert() {
            @Override
            public boolean isTriggered() {
                return true;
            }
        };

        PricingEvent pricingEvent = new PricingEvent(security, new PricingDelta[] {
                new PricingDelta(null, new Trade(10.0)),
                new PricingDelta(null, new Trade(10.1))
        });

        final List<AlertEvent> events = new ArrayList<AlertEvent>();

        AlertService service = new AlertService();
        try {
            java.lang.reflect.Field pef = AlertService.class.getDeclaredField("pricingEnvironment");
            pef.setAccessible(true);
            pef.set(service, pricingEnvironment);
            java.lang.reflect.Field mapf = AlertService.class.getDeclaredField("map");
            mapf.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<ISecurity, java.util.List<IAlert>> internalMap = (java.util.Map<ISecurity, java.util.List<IAlert>>) mapf.get(service);
            internalMap.put(security, Arrays.asList(new IAlert[] { alert }));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        service.addAlertListener(new IAlertListener() {
            @Override
            public void alertTriggered(AlertEvent event) {
                events.add(event);
            }
        });

        try {
            java.lang.reflect.Method m = AlertService.class.getDeclaredMethod("doPricingUpdate", org.eclipsetrader.core.feed.PricingEvent.class);
            m.setAccessible(true);
            m.invoke(service, pricingEvent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(1, events.size());
    }
}
