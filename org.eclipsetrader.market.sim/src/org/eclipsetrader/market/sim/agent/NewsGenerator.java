package org.eclipsetrader.market.sim.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates procedural news events on a configurable schedule, drawn from a
 * seeded random source so scenarios are replayable.
 */
public class NewsGenerator {

    private final Random random;
    private final List<String> assets = new ArrayList<String>();
    private final long intervalMillis;
    private final double baseMagnitude;
    private long nextNewsTime;

    public NewsGenerator(long seed, long intervalMillis, double baseMagnitude) {
        this.random = new Random(seed);
        this.intervalMillis = intervalMillis;
        this.baseMagnitude = baseMagnitude;
        this.nextNewsTime = intervalMillis;
    }

    public void addAsset(String asset) {
        assets.add(asset);
    }

    /**
     * Returns a news event if one is due at the given time, otherwise null.
     */
    public NewsEvent maybeGenerate(long now) {
        if (assets.isEmpty()) {
            return null;
        }
        if (now >= nextNewsTime) {
            nextNewsTime = now + intervalMillis;
            String asset = assets.get(random.nextInt(assets.size()));
            int sentiment = random.nextBoolean() ? 1 : -1;
            double magnitude = baseMagnitude * (0.5 + random.nextDouble());
            return new NewsEvent(asset, sentiment, magnitude, now);
        }
        return null;
    }
}
