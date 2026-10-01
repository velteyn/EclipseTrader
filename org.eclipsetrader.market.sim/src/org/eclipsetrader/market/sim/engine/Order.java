package org.eclipsetrader.market.sim.engine;

/**
 * A single order in the simulated market. Supports limit, market, stop,
 * stop-limit, and trailing-stop types, plus iceberg (display quantity),
 * pegged (reference price) modifiers, and time-in-force (GTC/IOC/FOK).
 */
public class Order {

    private final long id;
    private final String asset;
    private final Side side;
    private final OrderType type;
    private TimeInForce timeInForce = TimeInForce.GTC;
    private PegType pegType = PegType.NONE;

    private double price;           // limit price (ignored for MARKET)
    private double stopPrice;       // trigger for STOP / STOP_LIMIT
    private double trailingOffset;  // offset for TRAILING_STOP
    private double trailingBest;    // best observed price for trailing stop
    private double pegOffset;       // offset for PEGGED orders

    private final long quantity;    // original total quantity
    private long remaining;         // total unfilled (including hidden)
    private long display;           // currently visible quantity
    private long displayQuantity;   // iceberg chunk (0 = no iceberg)

    private long cumFilled;
    private double avgFillPrice;

    private long timestamp;         // simulated clock time
    private final String participant;

    public Order(long id, String asset, Side side, OrderType type, long quantity, double price, String participant) {
        this.id = id;
        this.asset = asset;
        this.side = side;
        this.type = type;
        this.quantity = quantity;
        this.remaining = quantity;
        this.price = price;
        this.participant = participant;
        this.displayQuantity = 0;
        this.display = quantity;
    }

    public long getId() {
        return id;
    }

    public String getAsset() {
        return asset;
    }

    public Side getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public TimeInForce getTimeInForce() {
        return timeInForce;
    }

    public void setTimeInForce(TimeInForce timeInForce) {
        this.timeInForce = timeInForce;
    }

    public PegType getPegType() {
        return pegType;
    }

    public void setPegType(PegType pegType) {
        this.pegType = pegType;
    }

    public double getPegOffset() {
        return pegOffset;
    }

    public void setPegOffset(double pegOffset) {
        this.pegOffset = pegOffset;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(double stopPrice) {
        this.stopPrice = stopPrice;
    }

    public double getTrailingOffset() {
        return trailingOffset;
    }

    public void setTrailingOffset(double trailingOffset) {
        this.trailingOffset = trailingOffset;
    }

    public double getTrailingBest() {
        return trailingBest;
    }

    public void setTrailingBest(double trailingBest) {
        this.trailingBest = trailingBest;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getRemaining() {
        return remaining;
    }

    public long getDisplayQuantity() {
        return displayQuantity;
    }

    public boolean isIceberg() {
        return displayQuantity > 0;
    }

    public long getCumFilled() {
        return cumFilled;
    }

    public double getAvgFillPrice() {
        return avgFillPrice;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getParticipant() {
        return participant;
    }

    public boolean isMarket() {
        return type == OrderType.MARKET;
    }

    public boolean isContingent() {
        return type == OrderType.STOP || type == OrderType.STOP_LIMIT || type == OrderType.TRAILING_STOP;
    }

    /**
     * The quantity currently available to be matched against (the visible portion).
     */
    public long getAvailable() {
        if (displayQuantity > 0) {
            return Math.min(display, remaining);
        }
        return remaining;
    }

    /**
     * Sets the iceberg display chunk and initializes the visible quantity.
     */
    public void setIceberg(long chunk) {
        this.displayQuantity = chunk;
        initializeDisplay();
    }

    public void initializeDisplay() {
        if (displayQuantity > 0) {
            display = Math.min(displayQuantity, remaining);
        } else {
            display = remaining;
        }
    }

    /**
     * Fills the given quantity, replenishing the visible portion for iceberg orders.
     */
    public void fill(long qty) {
        if (qty <= 0) {
            return;
        }
        if (qty > remaining) {
            qty = remaining;
        }
        long filledBefore = cumFilled;
        avgFillPrice = (avgFillPrice * filledBefore + getPrice() * qty) / (filledBefore + qty);
        remaining -= qty;
        cumFilled += qty;
        if (displayQuantity > 0) {
            display -= qty;
            if (display <= 0 && remaining > 0) {
                display = Math.min(displayQuantity, remaining);
            }
        } else {
            display = remaining;
        }
    }

    /**
     * Reduces the remaining quantity for an amendment (down) without recording a fill.
     */
    public void reduce(long qty) {
        if (qty > remaining) {
            qty = remaining;
        }
        remaining -= qty;
        if (displayQuantity <= 0) {
            display = remaining;
        } else {
            display = Math.min(display, remaining);
        }
    }

    public boolean isFilled() {
        return remaining <= 0;
    }

    @Override
    public String toString() {
        return side + " " + type + " " + remaining + "@" + price;
    }
}
