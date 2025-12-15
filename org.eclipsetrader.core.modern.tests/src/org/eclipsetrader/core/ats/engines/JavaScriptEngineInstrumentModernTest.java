package org.eclipsetrader.core.ats.engines;

import org.eclipsetrader.core.IScript;
import org.eclipsetrader.core.ats.*;
import org.eclipsetrader.core.feed.Bar;
import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Date;

@RunWith(JUnitPlatform.class)
public class JavaScriptEngineInstrumentModernTest {

    private static class StrategyStub implements IScriptStrategy {
        @Override public String getLanguage() { return "javascript"; }
        @Override public String getText() { return "function onQuote(q){}; function onTrade(t){}; function onBar(b){};"; }
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
        private final Security s;
        InstrumentStub(Security s) { this.s = s; }
        @Override public org.eclipsetrader.core.instruments.ISecurity getInstrument() { return s; }
        @Override public Object getAdapter(Class adapter) { return null; }
    }

    private static class ContextStub implements ITradingSystemContext {
        private final PricingEnvironment env;
        private final IBroker broker;
        private final IAccount account;
        ContextStub(PricingEnvironment env, IBroker broker, IAccount account) { this.env = env; this.broker = broker; this.account = account; }
        @Override public IBroker getBroker() { return broker; }
        @Override public IAccount getAccount() { return account; }
        @Override public org.eclipsetrader.core.feed.IPricingEnvironment getPricingEnvironment() { return env; }
        @Override public void dispose() { env.dispose(); }
        @Override public int getInitialBackfillSize() { return 0; }
    }

    @Test
    void testQuoteTradeBarPropertiesAndBarsList() throws Exception {
        PricingEnvironment env = new PricingEnvironment();
        StrategyStub strategy = new StrategyStub();
        Security security = new Security("SEC", null);
        TradingSystemStub ts = new TradingSystemStub(strategy, new ITradingSystemInstrument[]{new InstrumentStub(security)});
        ContextStub ctx = new ContextStub(env, new org.eclipsetrader.core.ats.simulation.Broker(env), new org.eclipsetrader.core.ats.simulation.Account());
        JavaScriptEngine engine = new JavaScriptEngine(ts, ctx);
        engine.start();

        JavaScriptEngineInstrument eng = engine.getContextFor(security);

        Quote q = new Quote(10.0, 11.0, 1L, 1L);
        eng.onQuote(q);
        Assertions.assertSame(q, eng.get("quote"));

        Trade t = new Trade(new Date(), 10.5, 1L, null);
        eng.onTrade(t);
        Assertions.assertSame(t, eng.get("trade"));

        Bar b = new Bar(new Date(), TimeSpan.minutes(1), 10.0, 11.0, 9.5, 10.8, 100L);
        eng.onBar(b);
        Assertions.assertSame(b, eng.get("bar"));
        Assertions.assertEquals(1, eng.getBars().length);
    }
}
