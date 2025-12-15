package org.eclipsetrader.core;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipsetrader.core.ats.engines.BarsDataSeriesFunctionTest;
import org.eclipsetrader.core.ats.engines.HasPositionFunctionTest;
import org.eclipsetrader.core.ats.engines.JavaScriptEngineInstrumentTest;
import org.eclipsetrader.core.ats.engines.JavaScriptEngineTest;
import org.eclipsetrader.core.ats.engines.LimitOrderFunctionTest;
import org.eclipsetrader.core.ats.engines.MarketOrderFunctionTest;
import org.eclipsetrader.core.ats.simulation.AccountTest;
import org.eclipsetrader.core.ats.simulation.BrokerTest;
import org.eclipsetrader.core.CashTest;
import org.eclipsetrader.core.charts.DataSeriesTest;
import org.eclipsetrader.core.feed.BarGeneratorTest;
import org.eclipsetrader.core.feed.BarTest;
import org.eclipsetrader.core.feed.BookEntryTest;
import org.eclipsetrader.core.feed.BookTest;
import org.eclipsetrader.core.feed.FeedPropertiesTest;
import org.eclipsetrader.core.feed.HistoryDayTest;
import org.eclipsetrader.core.feed.HistoryTest;
import org.eclipsetrader.core.feed.LastCloseTest;
import org.eclipsetrader.core.feed.PricingEnvironmentTest;
import org.eclipsetrader.core.feed.QuoteTest;
import org.eclipsetrader.core.feed.SingleFeedPricingEnvironmentTest;
import org.eclipsetrader.core.feed.TimeSpanTest;
import org.eclipsetrader.core.feed.TodayOHLTest;
import org.eclipsetrader.core.feed.TradeTest;
import org.eclipsetrader.core.instruments.SecurityTest;
import org.eclipsetrader.core.internal.ats.BarFactoryTest;
import org.eclipsetrader.core.internal.ats.TradingSystemTest;
import org.eclipsetrader.core.internal.charts.repository.ChartSectionTest;
import org.eclipsetrader.core.internal.charts.repository.ChartTemplateTest;
import org.eclipsetrader.core.internal.charts.repository.ElementSectionTest;
import org.eclipsetrader.core.internal.CurrencyServiceTest;
import org.eclipsetrader.core.internal.feed.ConnectorOverrideAdapterTest;
import org.eclipsetrader.core.internal.markets.DateAdapterTest;
import org.eclipsetrader.core.internal.markets.MarketDayTest;
import org.eclipsetrader.core.internal.markets.MarketHolidayTest;
import org.eclipsetrader.core.internal.markets.MarketListTest;
import org.eclipsetrader.core.internal.markets.MarketServiceTest;
import org.eclipsetrader.core.internal.markets.MarketTest;
import org.eclipsetrader.core.internal.markets.MarketTimeExcludeTest;
import org.eclipsetrader.core.internal.markets.MarketTimeTest;
import org.eclipsetrader.core.internal.markets.TimeAdapterTest;
import org.eclipsetrader.core.internal.markets.TimeZoneAdapterTest;
import org.eclipsetrader.core.internal.markets.WeekdaysAdapterTest;
import org.eclipsetrader.core.internal.repositories.DefaultElementFactoryTest;
import org.eclipsetrader.core.internal.repositories.RepositoryServiceTest;
import org.eclipsetrader.core.internal.trading.AlertServiceTest;
import org.eclipsetrader.core.internal.trading.TargetPriceTest;
import org.eclipsetrader.core.internal.trading.TradingServiceTest;
import org.eclipsetrader.core.internal.views.WatchListTest;
import org.eclipsetrader.core.markets.MarketPricingEnvironmentTest;
import org.eclipsetrader.core.PluginTest;
import org.eclipsetrader.core.TestFeedConnector;
import org.eclipsetrader.core.TestMarket;

public class AllCoreTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("All Core Tests");

        
        suite.addTestSuite(HasPositionFunctionTest.class);
        suite.addTestSuite(JavaScriptEngineInstrumentTest.class);
        
        
        
        
        suite.addTestSuite(BrokerTest.class);
        suite.addTestSuite(CashTest.class);
        suite.addTestSuite(DataSeriesTest.class);
        suite.addTestSuite(BarGeneratorTest.class);
        suite.addTestSuite(BarTest.class);
        suite.addTestSuite(BookEntryTest.class);
        suite.addTestSuite(BookTest.class);
        suite.addTestSuite(FeedPropertiesTest.class);
        suite.addTestSuite(HistoryDayTest.class);
        
        suite.addTestSuite(LastCloseTest.class);
        suite.addTestSuite(PricingEnvironmentTest.class);
        suite.addTestSuite(QuoteTest.class);
        suite.addTestSuite(SingleFeedPricingEnvironmentTest.class);
        suite.addTestSuite(TimeSpanTest.class);
        suite.addTestSuite(TodayOHLTest.class);
        suite.addTestSuite(TradeTest.class);
        suite.addTestSuite(SecurityTest.class);
        
        suite.addTestSuite(ChartSectionTest.class);
        suite.addTestSuite(ChartTemplateTest.class);
        suite.addTestSuite(ElementSectionTest.class);
        suite.addTestSuite(CurrencyServiceTest.class);
        suite.addTestSuite(ConnectorOverrideAdapterTest.class);
        suite.addTestSuite(DateAdapterTest.class);
        suite.addTestSuite(MarketDayTest.class);
        suite.addTestSuite(MarketHolidayTest.class);
        suite.addTestSuite(MarketListTest.class);
        
        suite.addTestSuite(MarketTest.class);
        suite.addTestSuite(MarketTimeExcludeTest.class);
        suite.addTestSuite(MarketTimeTest.class);
        suite.addTestSuite(TimeAdapterTest.class);
        suite.addTestSuite(TimeZoneAdapterTest.class);
        suite.addTestSuite(WeekdaysAdapterTest.class);
        suite.addTestSuite(DefaultElementFactoryTest.class);
        suite.addTestSuite(RepositoryServiceTest.class);
        
        suite.addTestSuite(TargetPriceTest.class);
        
        suite.addTestSuite(WatchListTest.class);
        
        // do not include PluginTest or helper classes here

        return suite;
    }
}
