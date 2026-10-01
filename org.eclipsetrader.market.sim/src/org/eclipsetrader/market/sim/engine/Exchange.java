package org.eclipsetrader.market.sim.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipsetrader.market.sim.risk.Account;

/**
 * The central market. Holds the order books per asset, the participant accounts,
 * and dispatches market activity to registered listeners.
 */
public class Exchange {

    private final Map<String, OrderBook> books = new HashMap<String, OrderBook>();
    private final Map<String, Account> accounts = new HashMap<String, Account>();
    private final List<MarketListener> listeners = new ArrayList<MarketListener>();

    private long orderIdSeed = 1;
    private long tradeIdSeed = 1;
    private long now;

    public long now() {
        return now;
    }

    public void setNow(long now) {
        this.now = now;
    }

    public long nextOrderId() {
        return orderIdSeed++;
    }

    public long nextTradeId() {
        return tradeIdSeed++;
    }

    public OrderBook getBook(String asset) {
        OrderBook book = books.get(asset);
        if (book == null) {
            book = new OrderBook(asset);
            books.put(asset, book);
        }
        return book;
    }

    public Map<String, OrderBook> getBooks() {
        return books;
    }

    public void addAccount(Account account) {
        accounts.put(account.getParticipant(), account);
    }

    public Account getAccount(String participant) {
        return accounts.get(participant);
    }

    public void addListener(MarketListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MarketListener listener) {
        listeners.remove(listener);
    }

    /**
     * Enters an order, returning null if it is rejected by the risk checks
     * (insufficient margin or short limit). Otherwise the order is matched
     * against the book and any residual rests.
     */
    public Order enterOrder(String asset, Side side, OrderType type, long quantity, double price, String participant) {
        Account account = accounts.get(participant);
        if (account != null) {
            if (side == Side.BUY) {
                if (type != OrderType.MARKET && !account.canBuy(price * quantity)) {
                    return null;
                }
            } else {
                if (!account.canSell(asset, quantity)) {
                    return null;
                }
            }
        }
        Order order = new Order(nextOrderId(), asset, side, type, quantity, price, participant);
        getBook(asset).enter(order, this);
        return order;
    }

    public boolean cancelOrder(long orderId, String asset) {
        OrderBook book = books.get(asset);
        if (book == null) {
            return false;
        }
        boolean result = book.cancel(orderId);
        if (result) {
            notifyBook(book);
        }
        return result;
    }

    public boolean amendOrder(long orderId, String asset, double newPrice, long newQty) {
        OrderBook book = books.get(asset);
        if (book == null) {
            return false;
        }
        Order order = book.findOrder(orderId);
        if (order == null) {
            return false;
        }
        boolean changed = false;
        if (newPrice != 0.0 && newPrice != order.getPrice()) {
            changed = book.amendPrice(orderId, newPrice) || changed;
        }
        if (newQty > 0 && newQty < order.getRemaining()) {
            changed = book.reduceQuantity(orderId, newQty) || changed;
        }
        if (changed) {
            notifyBook(book);
        }
        return changed;
    }

    /**
     * Called by the order book after each match to update participant accounts.
     */
    void applyTrade(Trade trade) {
        Account buyer = accounts.get(trade.getBuyer());
        Account seller = accounts.get(trade.getSeller());
        if (buyer != null) {
            buyer.applyTrade(trade.getAsset(), trade.getQuantity(), trade.getPrice());
        }
        if (seller != null) {
            seller.applyTrade(trade.getAsset(), -trade.getQuantity(), trade.getPrice());
        }
    }

    /**
     * Evaluates margin for every account and force-liquidates (flattens) any
     * account whose equity is below the maintenance margin.
     */
    public void checkMargin() {
        for (Account account : new ArrayList<Account>(accounts.values())) {
            Map<String, Double> prices = currentPrices();
            if (!account.isFlat() && account.equity(prices) < account.getMaintenanceMargin() * account.grossExposure(prices)) {
                liquidate(account);
            }
        }
    }

    private Map<String, Double> currentPrices() {
        Map<String, Double> prices = new HashMap<String, Double>();
        for (OrderBook book : books.values()) {
            double last = book.getLast();
            if (last != 0.0) {
                prices.put(book.getAsset(), last);
            } else if (book.getBestBid() != 0.0 || book.getBestAsk() != 0.0) {
                double bid = book.getBestBid();
                double ask = book.getBestAsk();
                double mid = bid != 0.0 && ask != 0.0 ? (bid + ask) / 2.0 : (bid != 0.0 ? bid : ask);
                prices.put(book.getAsset(), mid);
            }
        }
        return prices;
    }

    private void liquidate(Account account) {
        for (Map.Entry<String, Long> en : account.getPositions().entrySet()) {
            long qty = en.getValue().longValue();
            if (qty > 0) {
                enterOrder(en.getKey(), Side.SELL, OrderType.MARKET, qty, 0, account.getParticipant());
            } else if (qty < 0) {
                enterOrder(en.getKey(), Side.BUY, OrderType.MARKET, -qty, 0, account.getParticipant());
            }
        }
    }

    void notifyTrade(Trade trade) {
        for (MarketListener listener : listeners) {
            listener.onTrade(trade);
        }
    }

    void notifyBook(OrderBook book) {
        List<Order> bids = new ArrayList<Order>(book.getBids());
        List<Order> asks = new ArrayList<Order>(book.getAsks());
        for (MarketListener listener : listeners) {
            listener.onBook(book.getAsset(), bids, asks);
        }
    }

    void notifyQuote(String asset) {
        OrderBook book = books.get(asset);
        if (book == null) {
            return;
        }
        double bid = book.getBestBid();
        double ask = book.getBestAsk();
        long bidSize = book.getBestBidVol();
        long askSize = book.getBestAskVol();
        for (MarketListener listener : listeners) {
            listener.onQuote(asset, bid, ask, bidSize, askSize);
        }
    }

    void notifyOrderPlaced(Order order) {
        notifyQuote(order.getAsset());
    }

    void notifyOrderCancelled(Order order) {
        notifyQuote(order.getAsset());
    }
}
