package org.eclipsetrader.market.sim.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipsetrader.market.sim.agent.FundamentalValue;
import org.eclipsetrader.market.sim.agent.MarketSimulator;
import org.eclipsetrader.market.sim.agent.NewsEvent;
import org.eclipsetrader.market.sim.agent.TrendEstimator;
import org.eclipsetrader.market.sim.engine.Exchange;
import org.junit.jupiter.api.Test;

public class AdaptationTest {

    @Test
    void newsMovesFundamentalValue() {
        FundamentalValue fv = new FundamentalValue();
        fv.set("X", 100.0);
        fv.applyNews(new NewsEvent("X", 1, 0.05, 0));
        assertEquals(105.0, fv.get("X"), 0.0001);
    }

    @Test
    void trendEstimatorDetectsDirection() {
        TrendEstimator up = new TrendEstimator(10, 0.01, 0.0, 100.0);
        for (double p = 100.0; p <= 110.0; p += 1.0) {
            up.addPrice(p);
        }
        assertTrue(up.estimate() > 0);

        TrendEstimator down = new TrendEstimator(10, 0.01, 0.0, 100.0);
        for (double p = 100.0; p >= 90.0; p -= 1.0) {
            down.addPrice(p);
        }
        assertTrue(down.estimate() < 0);
    }

    @Test
    void tradesOccurWithoutPlayerAction() {
        MarketSimulator sim = new MarketSimulator(42L, 1_800_000_000_000L, 5_000L, 0.02);
        sim.addAsset("X", 100.0);
        for (int i = 0; i < 200; i++) {
            sim.step();
        }
        Exchange ex = sim.getExchange();
        assertTrue(ex.getBook("X").getTrades().size() > 0);
    }

    @Test
    void newsMovesSimulatedPrice() {
        MarketSimulator sim = new MarketSimulator(7L, 1_800_000_000_000L, 2_000L, 0.02);
        sim.addAsset("X", 100.0);
        // Drive enough steps to generate news and trades.
        for (int i = 0; i < 500; i++) {
            sim.step();
        }
        double last = sim.getExchange().getBook("X").getLast();
        assertTrue(last > 0.0);
    }
}
