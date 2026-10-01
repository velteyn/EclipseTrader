package org.eclipsetrader.market.sim.agent;

/**
 * A procedurally generated news event carrying an asset, a sentiment direction,
 * and a magnitude (as a fraction of price).
 */
public class NewsEvent {

    private final String asset;
    private final int sentiment;   // +1 good, -1 bad
    private final double magnitude;
    private final long timestamp;

    public NewsEvent(String asset, int sentiment, double magnitude, long timestamp) {
        this.asset = asset;
        this.sentiment = sentiment;
        this.magnitude = magnitude;
        this.timestamp = timestamp;
    }

    public String getAsset() {
        return asset;
    }

    public int getSentiment() {
        return sentiment;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
