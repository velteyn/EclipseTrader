package org.eclipsetrader.core.internal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipsetrader.core.Cash;
import org.eclipsetrader.core.feed.History;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.instruments.CurrencyExchange;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryChangeListener;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.core.views.IWatchList;

import java.net.URI;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(JUnitPlatform.class)
public class CurrencyServiceModernTest {

    private CurrencyExchange eurusd = new CurrencyExchange(Currency.getInstance("EUR"), Currency.getInstance("USD"), 1.0);
    private final IOHLC[] history = new IOHLC[] {
            new OHLC(new Date(1000000), 1.4677, 1.4677, 1.4677, 1.4677, 0L),
            new OHLC(new Date(2000000), 1.4593, 1.4593, 1.4593, 1.4593, 0L),
    };

    private class TestRepositoryService implements IRepositoryService {
        @Override public void addRepositoryResourceListener(IRepositoryChangeListener listener) { }
        @Override public void deleteAdaptable(IAdaptable[] adaptables) { }
        @Override public IFeedIdentifier getFeedIdentifierFromSymbol(String symbol) { return null; }
        @Override public IFeedIdentifier[] getFeedIdentifiers() { return null; }
        @Override public IHistory getHistoryFor(ISecurity security) {
            if (security == eurusd) return new History(security, history);
            return null;
        }
        @Override public IRepository[] getRepositories() { return null; }
        @Override public IRepository getRepository(String scheme) { return null; }
        @Override public ISecurity[] getSecurities() { return new ISecurity[] { eurusd }; }
        @Override public ISecurity getSecurityFromName(String name) { return null; }
        @Override public ISecurity getSecurityFromURI(URI uri) { return null; }
        @Override public IWatchList getWatchListFromName(String name) { return null; }
        @Override public IWatchList getWatchListFromURI(URI uri) { return null; }
        @Override public IWatchList[] getWatchLists() { return null; }
        @Override public IHolding[] getTrades() { return null; }
        @Override public void moveAdaptable(IAdaptable[] adaptables, IRepository repository) { }
        @Override public void removeRepositoryResourceListener(IRepositoryChangeListener listener) { }
        @Override public IStatus runInService(IRepositoryRunnable runnable, IProgressMonitor monitor) { return null; }
        @Override public IStatus runInService(IRepositoryRunnable runnable, ISchedulingRule rule, IProgressMonitor monitor) { return null; }
        @Override public void saveAdaptable(IAdaptable[] adaptables) { }
        @Override public void saveAdaptable(IAdaptable[] adaptables, IRepository defaultRepository) { }
        @Override public IStoreObject getObjectFromURI(URI uri) { return null; }
        @Override public IStoreObject[] getAllObjects() { return null; }
        @Override public List<IStore> getTradesFor(ISecurity security) { return null; }
    }

    private final MarketPricingEnvironment pricingEnvironment = new MarketPricingEnvironment() {
        @Override public void addSecurity(ISecurity security) { }
        @Override public ITrade getTrade(ISecurity security) {
            if (security == eurusd) return new Trade(null, 1.4677, null, null);
            return null;
        }
    };

    private class TestCurrencyService extends CurrencyService {
        public TestCurrencyService(IRepositoryService repositoryService, MarketPricingEnvironment pricingEnvironment) {
            super(repositoryService, pricingEnvironment);
        }
    }

    @Test
    void testGetAvailableCurrencies() throws Exception {
        CurrencyService service = new TestCurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Set<Currency> set = new HashSet<>(Arrays.asList(service.getAvailableCurrencies()));
        Assertions.assertTrue(set.contains(Currency.getInstance("EUR")));
        Assertions.assertTrue(set.contains(Currency.getInstance("USD")));
        Assertions.assertFalse(set.contains(Currency.getInstance("GBP")));
    }

    @Test
    void testDirectConvert() throws Exception {
        CurrencyService service = new TestCurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.0, Currency.getInstance("EUR")), Currency.getInstance("USD"));
        Assertions.assertEquals(1.4677, cash.getAmount());
        Assertions.assertEquals(Currency.getInstance("USD"), cash.getCurrency());
    }

    @Test
    void testInverseConvert() throws Exception {
        CurrencyService service = new TestCurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.4677, Currency.getInstance("USD")), Currency.getInstance("EUR"));
        Assertions.assertEquals(1.0, cash.getAmount());
        Assertions.assertEquals(Currency.getInstance("EUR"), cash.getCurrency());
    }

    @Test
    void testConvertToUnknownCurrency() throws Exception {
        CurrencyService service = new TestCurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.0, Currency.getInstance("EUR")), Currency.getInstance("GBP"));
        Assertions.assertNull(cash);
    }

    @Test
    void testConvertFromUnknownCurrency() throws Exception {
        CurrencyService service = new TestCurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.0, Currency.getInstance("GBP")), Currency.getInstance("USD"));
        Assertions.assertNull(cash);
    }

    @Test
    void testConvertToSameCurrency() throws Exception {
        CurrencyService service = new TestCurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.0, Currency.getInstance("EUR")), Currency.getInstance("EUR"));
        Assertions.assertEquals(1.0, cash.getAmount());
        Assertions.assertEquals(Currency.getInstance("EUR"), cash.getCurrency());
    }

    @Test
    void testDirectConvertWithMultiplier() throws Exception {
        eurusd = new CurrencyExchange(Currency.getInstance("EUR"), Currency.getInstance("USD"), 100.0);
        CurrencyService service = new TestCurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.0, Currency.getInstance("EUR")), Currency.getInstance("USD"));
        Assertions.assertEquals(146.77, cash.getAmount());
        Assertions.assertEquals(Currency.getInstance("USD"), cash.getCurrency());
    }

    @Test
    void testInverseConvertWithMultiplier() throws Exception {
        eurusd = new CurrencyExchange(Currency.getInstance("EUR"), Currency.getInstance("USD"), 100.0);
        CurrencyService service = new TestCurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(146.77, Currency.getInstance("USD")), Currency.getInstance("EUR"));
        Assertions.assertEquals(1.0, cash.getAmount());
        Assertions.assertEquals(Currency.getInstance("EUR"), cash.getCurrency());
    }

    @Test
    void testDirectConvertToDate() throws Exception {
        CurrencyService service = new TestCurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.0, Currency.getInstance("EUR")), Currency.getInstance("USD"), new Date(2000000));
        Assertions.assertEquals(1.4593, cash.getAmount());
        Assertions.assertEquals(Currency.getInstance("USD"), cash.getCurrency());
    }

    @Test
    void testInverseConvertToDate() throws Exception {
        CurrencyService service = new TestCurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.4593, Currency.getInstance("USD")), Currency.getInstance("EUR"), new Date(2000000));
        Assertions.assertEquals(1.0, cash.getAmount());
        Assertions.assertEquals(Currency.getInstance("EUR"), cash.getCurrency());
    }

    @Test
    void testConvertToUnknownDate() throws Exception {
        CurrencyService service = new TestCurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.0, Currency.getInstance("EUR")), Currency.getInstance("USD"), new Date(3000000));
        Assertions.assertNull(cash);
    }
}
