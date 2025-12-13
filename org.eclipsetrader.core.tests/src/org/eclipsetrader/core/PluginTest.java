package org.eclipsetrader.core;

import junit.framework.Test;
import junit.framework.TestSuite;

public class PluginTest {
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.eclipsetrader.core");
        //$JUnit-BEGIN$
        suite.addTestSuite(org.eclipsetrader.core.instruments.SecurityTest.class);
        suite.addTestSuite(org.eclipsetrader.core.markets.MarketPricingEnvironmentTest.class);
        suite.addTestSuite(org.eclipsetrader.core.CashTest.class);
        suite.addTestSuite(org.eclipsetrader.core.charts.DataSeriesTest.class);
        suite.addTestSuite(org.eclipsetrader.core.feed.HistoryTest.class);
        suite.addTestSuite(org.eclipsetrader.core.feed.QuoteTest.class);
        suite.addTestSuite(org.eclipsetrader.core.feed.BookTest.class);
        suite.addTestSuite(org.eclipsetrader.core.feed.TradeTest.class);
        suite.addTestSuite(org.eclipsetrader.core.feed.SingleFeedPricingEnvironmentTest.class);
        suite.addTestSuite(org.eclipsetrader.core.feed.FeedPropertiesTest.class);
        suite.addTestSuite(org.eclipsetrader.core.feed.PricingEnvironmentTest.class);
        suite.addTestSuite(org.eclipsetrader.core.feed.TimeSpanTest.class);
        suite.addTestSuite(org.eclipsetrader.core.feed.HistoryDayTest.class);
        suite.addTestSuite(org.eclipsetrader.core.feed.BarTest.class);
        suite.addTestSuite(org.eclipsetrader.core.feed.LastCloseTest.class);
        suite.addTestSuite(org.eclipsetrader.core.feed.TodayOHLTest.class);
        suite.addTestSuite(org.eclipsetrader.core.feed.BarGeneratorTest.class);
        suite.addTestSuite(org.eclipsetrader.core.feed.BookEntryTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.markets.TimeAdapterTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.markets.DateAdapterTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.markets.MarketTimeTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.markets.MarketHolidayTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.markets.MarketListTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.markets.MarketTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.markets.MarketDayTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.markets.WeekdaysAdapterTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.markets.TimeZoneAdapterTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.markets.MarketTimeExcludeTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.markets.MarketServiceTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.views.WatchListTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.repositories.RepositoryServiceTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.repositories.DefaultElementFactoryTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.CurrencyServiceTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.charts.repository.ChartSectionTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.charts.repository.ChartTemplateTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.charts.repository.ElementSectionTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.feed.ConnectorOverrideAdapterTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.ats.TradingSystemTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.ats.BarFactoryTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.trading.TradingServiceTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.trading.TargetPriceTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.trading.AlertServiceTest.class);
        suite.addTestSuite(org.eclipsetrader.core.ats.engines.LimitOrderFunctionTest.class);
        suite.addTestSuite(org.eclipsetrader.core.ats.engines.HasPositionFunctionTest.class);
        suite.addTestSuite(org.eclipsetrader.core.ats.engines.MarketOrderFunctionTest.class);
        suite.addTestSuite(org.eclipsetrader.core.ats.engines.JavaScriptEngineTest.class);
        suite.addTestSuite(org.eclipsetrader.core.ats.engines.JavaScriptEngineInstrumentTest.class);
        suite.addTestSuite(org.eclipsetrader.core.ats.engines.BarsDataSeriesFunctionTest.class);
        suite.addTestSuite(org.eclipsetrader.core.ats.simulation.BrokerTest.class);
        suite.addTestSuite(org.eclipsetrader.core.ats.simulation.AccountTest.class);
        //$JUnit-END$
        return suite;
    }
}
