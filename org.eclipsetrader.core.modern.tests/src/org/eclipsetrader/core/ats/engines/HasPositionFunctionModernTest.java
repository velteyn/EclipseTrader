package org.eclipsetrader.core.ats.engines;

import org.eclipsetrader.core.IScript;
import org.eclipsetrader.core.ats.*;
import org.eclipsetrader.core.ats.simulation.Account;
import org.eclipsetrader.core.ats.simulation.Broker;
import org.eclipsetrader.core.ats.simulation.OrderMonitor;
import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.Order;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Currency;
import java.util.Date;

@RunWith(JUnitPlatform.class)
public class HasPositionFunctionModernTest {

    private static class StrategyStub implements IScriptStrategy {
        @Override public String getLanguage() { return "javascript"; }
        @Override public String getText() { return "function onStrategyStart(){ testHasStart = hasPosition(); } function onTrade(t){ testHasAfter = hasPosition(); }"; }
        @Override public IScript[] getIncludes() { return new IScript[0]; }
        @Override public String getName() { return "TestStrategy"; }
        @Override public org.eclipsetrader.core.instruments.ISecurity[] getInstruments() { return new org.eclipsetrader.core.instruments.ISecurity[0]; }
        @Override public TimeSpan[] getBarsTimeSpan() { return new TimeSpan[]{TimeSpan.minutes(1)}; }
        @Override public Object getAdapter(Class adapter) { return null; }
    }

    private static class TradingSystemStub implements ITradingSystem {
        private final IScriptStrategy strategy;
        private final ITradingSystemInstrument[] instruments;
        TradingSystemStub(IScriptStrategy strategy, ITradingSystemInstrument[] instruments) { this.strategy = strategy; this.instruments = instruments; }
        @Override public IStrategy getStrategy() { return strategy; }
        @Override public ITradingSystemInstrument[] getInstruments() { return instruments; }
        @Override public int getStatus() { return STATUS_STOPPED; }
        @Override public void start(ITradingSystemContext context) {}
        @Override public void stop() {}
        @Override public Object getAdapter(Class adapter) { return null; }
    }

    private static class InstrumentStub implements ITradingSystemInstrument {
        private final Stock s;
        InstrumentStub(Stock s) { this.s = s; }
        @Override public org.eclipsetrader.core.instruments.ISecurity getInstrument() { return s; }
        @Override public Object getAdapter(Class adapter) { return null; }
    }

    private static class ContextStub implements ITradingSystemContext {
        private final PricingEnvironment env;
        private final org.eclipsetrader.core.trading.IBroker broker;
        private final org.eclipsetrader.core.trading.IAccount account;
        ContextStub(PricingEnvironment env, org.eclipsetrader.core.trading.IBroker broker, org.eclipsetrader.core.trading.IAccount account) { this.env = env; this.broker = broker; this.account = account; }
        @Override public org.eclipsetrader.core.trading.IBroker getBroker() { return broker; }
        @Override public org.eclipsetrader.core.trading.IAccount getAccount() { return account; }
        @Override public org.eclipsetrader.core.feed.IPricingEnvironment getPricingEnvironment() { return env; }
        @Override public void dispose() { env.dispose(); }
        @Override public int getInitialBackfillSize() { return 0; }
    }

    @Test
    void testHasPositionFunctionViaScript() throws Exception {
        PricingEnvironment env = new PricingEnvironment();
        StrategyStub strategy = new StrategyStub();
        Stock sec = new Stock("SEC", null, Currency.getInstance("USD"));
        TradingSystemStub ts = new TradingSystemStub(strategy, new ITradingSystemInstrument[]{new InstrumentStub(sec)});
        Account account = new Account();
        Broker broker = new Broker(env);
        ContextStub ctx = new ContextStub(env, broker, account);
        JavaScriptEngine engine = new JavaScriptEngine(ts, ctx);
        engine.start();

        JavaScriptEngineInstrument context = engine.getContextFor(sec);
        Object startHas = context.get("testHasStart");
        Assertions.assertTrue(startHas == null || Boolean.FALSE.equals(startHas));

        Order buy = new Order(account, IOrderType.Market, IOrderSide.Buy, sec, 10L, null);
        OrderMonitor monitor = new OrderMonitor(broker, buy);
        monitor.setFilledQuantity(10L);
        monitor.setAveragePrice(10.0);
        account.processCompletedOrder(monitor);

        env.setTrade(sec, new Trade(new Date(), 10.5, 1L, null));
        Object afterHas = context.get("testHasAfter");
        Assertions.assertEquals(Boolean.TRUE, afterHas);
    }
}
