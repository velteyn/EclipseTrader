package org.eclipsetrader.market.sim.engine;

/**
 * A single executed trade between a buyer and a seller.
 */
public class Trade {

    private final long id;
    private final String asset;
    private final double price;
    private final long quantity;
    private final long buyOrderId;
    private final long sellOrderId;
    private final long timestamp;
    private final String buyer;
    private final String seller;

    public Trade(long id, String asset, double price, long quantity, long buyOrderId, long sellOrderId, long timestamp, String buyer, String seller) {
        this.id = id;
        this.asset = asset;
        this.price = price;
        this.quantity = quantity;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.timestamp = timestamp;
        this.buyer = buyer;
        this.seller = seller;
    }

    public long getId() {
        return id;
    }

    public String getAsset() {
        return asset;
    }

    public double getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getBuyOrderId() {
        return buyOrderId;
    }

    public long getSellOrderId() {
        return sellOrderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getBuyer() {
        return buyer;
    }

    public String getSeller() {
        return seller;
    }

    @Override
    public String toString() {
        return "Trade " + quantity + " " + asset + "@" + price;
    }
}
