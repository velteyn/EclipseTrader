package org.eclipsetrader.market.sim.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipsetrader.market.sim.clock.SimulatedClock;
import org.eclipsetrader.market.sim.clock.TradingCalendar;
import org.eclipsetrader.market.sim.engine.Exchange;
import org.eclipsetrader.market.sim.risk.Account;

/**
 * Orchestrates the adaptive market simulation: ties together the exchange, the
 * simulated daily calendar, the fundamental value process, procedural news, and
 * the trading agents. A seeded scenario entry point; all randomness comes from a
 * single seed so runs are replayable.
 */
public class MarketSimulator {

    public static final String MARKET_MAKER = "MARKET_MAKER";
    public static final String INFORMED = "INFORMED";
    public static final String PLAYER = "PLAYER";

    private final Exchange exchange = new Exchange();
    private final SimulatedClock clock;
    private final TradingCalendar calendar;
    private final Random random;

    private final Map<String, Double> fundamentals = new HashMap<String, Double>();
    private final Map<String, TrendEstimator> trends = new HashMap<String, TrendEstimator>();

    private final NewsGenerator newsGenerator;
    private final MarketMaker marketMaker;
    private final InformedAgent informedAgent;

    private final double noise = 0.0005;
    private final int trendWindow = 10;
    private final double trendCap = 0.01;
    private final double meanReversion = 0.0005;
    private final double spread = 0.001;
    private final long orderSize = 100;

    private long stepMillis = 1000;
    private Thread thread;
    private volatile boolean running;

    public MarketSimulator(long seed, long startEpochMillis, long newsIntervalMillis, double newsMagnitude) {
        this.random = new Random(seed);
        this.clock = new SimulatedClock(startEpochMillis);
        this.calendar = new TradingCalendar(clock, 9, 30, 16, 0);
        this.calendar.open();
        this.newsGenerator = new NewsGenerator(seed, newsIntervalMillis, newsMagnitude);
        this.marketMaker = new MarketMaker(MARKET_MAKER, spread, orderSize);
        this.informedAgent = new InformedAgent(INFORMED, orderSize);

        exchange.addAccount(new Account(MARKET_MAKER, 1_000_000_000.0));
        exchange.addAccount(new Account(INFORMED, 1_000_000_000.0));
    }

    public Exchange getExchange() {
        return exchange;
    }

    public SimulatedClock getClock() {
        return clock;
    }

    public TradingCalendar getCalendar() {
        return calendar;
    }

    public void setStepMillis(long stepMillis) {
        this.stepMillis = stepMillis;
    }

    public void addAsset(String asset, double initialPrice) {
        fundamentals.put(asset, initialPrice);
        trends.put(asset, new TrendEstimator(trendWindow, trendCap, meanReversion, initialPrice));
        newsGenerator.addAsset(asset);
    }

    public Account addPlayer(double cash, double leverage, long shortLimit) {
        Account account = new Account(PLAYER, cash);
        account.setLeverage(leverage);
        account.setShortLimit(shortLimit);
        exchange.addAccount(account);
        return account;
    }

    /**
     * Advances the simulation by one step: moves the clock, generates news,
     * has agents trade, updates trend estimates, and checks margin.
     */
    public void step() {
        if (!calendar.isOpen()) {
            return;
        }
        clock.advance(stepMillis);
        if (calendar.isEndOfDay()) {
            endOfDay();
            return;
        }
        exchange.setNow(clock.now());

        NewsEvent news = newsGenerator.maybeGenerate(clock.now());
        if (news != null) {
            applyNews(news);
        }

        for (String asset : new ArrayList<String>(fundamentals.keySet())) {
            double f = fundamentals.get(asset);
            TrendEstimator te = trends.get(asset);
            double trend = te.estimate();
            f = f * (1.0 + trend + noise * random.nextGaussian());
            fundamentals.put(asset, f);

            marketMaker.act(exchange, asset, f);
            informedAgent.act(exchange, asset, f, trend, random);

            double last = exchange.getBook(asset).getLast();
            if (last != 0.0) {
                te.addPrice(last);
            }
            exchange.getBook(asset).checkStops(last != 0.0 ? last : f, exchange);
        }

        exchange.checkMargin();
    }

    private void applyNews(NewsEvent news) {
        double before = fundamentals.containsKey(news.getAsset()) ? fundamentals.get(news.getAsset()) : 0.0;
        double after = before * (1.0 + news.getSentiment() * news.getMagnitude());
        fundamentals.put(news.getAsset(), after);
        informedAgent.reactToNews(news);
    }

    private void endOfDay() {
        calendar.close();
        for (Account account : exchangeAccounts()) {
            account.accrueInterest();
        }
        calendar.advanceDay();
        exchange.setNow(clock.now());
    }

    private List<Account> exchangeAccounts() {
        // Accounts are not exposed by Exchange; accrue interest on known participants.
        List<Account> result = new ArrayList<Account>();
        Account mm = exchange.getAccount(MARKET_MAKER);
        Account inf = exchange.getAccount(INFORMED);
        Account player = exchange.getAccount(PLAYER);
        if (mm != null) {
            result.add(mm);
        }
        if (inf != null) {
            result.add(inf);
        }
        if (player != null) {
            result.add(player);
        }
        return result;
    }

    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    step();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }, "MarketSimulator");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public boolean isRunning() {
        return running;
    }
}
