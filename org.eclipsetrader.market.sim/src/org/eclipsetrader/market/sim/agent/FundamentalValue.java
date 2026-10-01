package org.eclipsetrader.market.sim.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * The per-asset fundamental value, evolved by a drift (trend), a news shock,
 * and noise: {@code F = F * (1 + drift + news + noise)}.
 */
public class FundamentalValue {

    private final Map<String, Double> values = new HashMap<String, Double>();

    public void set(String asset, double value) {
        values.put(asset, value);
    }

    public double get(String asset) {
        Double v = values.get(asset);
        return v == null ? 0.0 : v.doubleValue();
    }

    /**
     * Applies a news shock to the asset's fundamental value.
     */
    public void applyNews(NewsEvent event) {
        Double v = values.get(event.getAsset());
        if (v != null) {
            values.put(event.getAsset(), v.doubleValue() * (1.0 + event.getSentiment() * event.getMagnitude()));
        }
    }

    /**
     * Steps the fundamental value by the drift and a small noise term.
     */
    public void step(String asset, double drift, double noise, Random random) {
        Double v = values.get(asset);
        if (v == null) {
            return;
        }
        double shock = drift + noise * random.nextGaussian();
        values.put(asset, v.doubleValue() * (1.0 + shock));
    }
}
