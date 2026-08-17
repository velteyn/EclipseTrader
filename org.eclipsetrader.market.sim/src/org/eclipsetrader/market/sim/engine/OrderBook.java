package org.eclipsetrader.market.sim.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The limit order book for a single asset. Implements price-time priority
 * insertion and full-depth matching across price levels. Contingent (stop)
 * orders rest off-book until triggered.
 */
public class OrderBook {

    private final String asset;

    private final List<Order> bids = new ArrayList<Order>();
    private final List<Order> asks = new ArrayList<Order>();
    private final List<Order> stops = new ArrayList<Order>();

    private final List<Trade> trades = new ArrayList<Trade>();
    private double tradedVolume;
    private double vwap;
    private double lastPrice;

    public OrderBook(String asset) {
        this.asset = asset;
    }

    public String getAsset() {
        return asset;
    }

    public List<Order> getBids() {
        return bids;
    }

    public List<Order> getAsks() {
        return asks;
    }

    public List<Order> getStops() {
        return stops;
    }

    public List<Trade> getTrades() {
        return trades;
    }

    public double getBestBid() {
        return bids.isEmpty() ? 0.0 : bids.get(0).getPrice();
    }

    public double getBestAsk() {
        return asks.isEmpty() ? 0.0 : asks.get(0).getPrice();
    }

    public long getBestBidVol() {
        return levelVolume(bids);
    }

    public long getBestAskVol() {
        return levelVolume(asks);
    }

    private long levelVolume(List<Order> orders) {
        if (orders.isEmpty()) {
            return 0;
        }
        double price = orders.get(0).getPrice();
        long total = 0;
        for (Order o : orders) {
            if (o.getPrice() == price) {
                total += o.getAvailable();
            } else {
                break;
            }
        }
        return total;
    }

    public double getLast() {
        return lastPrice;
    }

    public double getVwap() {
        return vwap;
    }

    public double getTradedVolume() {
        return tradedVolume;
    }

    /**
     * Enters a non-contingent order, matching it against the opposite side and
     * queuing any residual. Returns the trades generated.
     */
    public List<Trade> enter(Order order, Exchange exchange) {
        order.setTimestamp(exchange.now());
        if (order.isContingent()) {
            order.setTrailingBest(lastPrice != 0.0 ? lastPrice : order.getStopPrice());
            stops.add(order);
            exchange.notifyOrderPlaced(order);
            return new ArrayList<Trade>();
        }

        if (order.getPegType() != PegType.NONE) {
            applyPeg(order);
        }

        List<Trade> result = new ArrayList<Trade>();

        if (order.getTimeInForce() == TimeInForce.FOK) {
            List<Order> opposite = order.getSide() == Side.BUY ? asks : bids;
            if (!canFillFully(order, opposite)) {
                exchange.notifyOrderCancelled(order);
                return result;
            }
        }

        match(order, exchange, result);

        if (order.isMarket()) {
            if (!order.isFilled()) {
                exchange.notifyOrderCancelled(order);
            }
        } else if (order.getTimeInForce() == TimeInForce.IOC) {
            if (!order.isFilled()) {
                exchange.notifyOrderCancelled(order);
            }
        } else if (order.getTimeInForce() == TimeInForce.FOK) {
            if (!order.isFilled()) {
                exchange.notifyOrderCancelled(order);
            }
        } else if (!order.isFilled()) {
            insertSorted(order);
            exchange.notifyOrderPlaced(order);
        }

        for (Trade t : result) {
            addTrade(t);
            exchange.notifyTrade(t);
        }

        refreshPegged();
        exchange.notifyBook(this);
        return result;
    }

    private void match(Order order, Exchange exchange, List<Trade> result) {
        List<Order> opposite = order.getSide() == Side.BUY ? asks : bids;
        int i = 0;
        while (!order.isFilled() && i < opposite.size()) {
            Order resting = opposite.get(i);
            if (!crosses(resting, order)) {
                break;
            }
            long tradeQty = Math.min(order.getRemaining(), resting.getAvailable());
            double tradePrice = resting.getPrice();
            resting.fill(tradeQty);
            order.fill(tradeQty);
            long buyOrderId = order.getSide() == Side.BUY ? order.getId() : resting.getId();
            long sellOrderId = order.getSide() == Side.BUY ? resting.getId() : order.getId();
            String buyer = order.getSide() == Side.BUY ? order.getParticipant() : resting.getParticipant();
            String seller = order.getSide() == Side.BUY ? resting.getParticipant() : order.getParticipant();
            Trade trade = new Trade(exchange.nextTradeId(), asset, tradePrice, tradeQty, buyOrderId, sellOrderId, order.getTimestamp(), buyer, seller);
            result.add(trade);
            exchange.applyTrade(trade);
            if (resting.isFilled()) {
                opposite.remove(i);
            }
        }
    }

    private boolean crosses(Order resting, Order incoming) {
        if (incoming.isMarket()) {
            return true;
        }
        if (incoming.getSide() == Side.BUY) {
            return incoming.getPrice() >= resting.getPrice();
        }
        return incoming.getPrice() <= resting.getPrice();
    }

    private boolean canFillFully(Order order, List<Order> opposite) {
        long needed = order.getRemaining();
        long avail = 0;
        for (Order o : opposite) {
            if (!crosses(o, order)) {
                break;
            }
            avail += o.getAvailable();
            if (avail >= needed) {
                return true;
            }
        }
        return avail >= needed;
    }

    private void insertSorted(Order order) {
        List<Order> list = order.getSide() == Side.BUY ? bids : asks;
        int i = 0;
        if (order.getSide() == Side.BUY) {
            while (i < list.size() && list.get(i).getPrice() >= order.getPrice()) {
                i++;
            }
        } else {
            while (i < list.size() && list.get(i).getPrice() <= order.getPrice()) {
                i++;
            }
        }
        list.add(i, order);
    }

    private void applyPeg(Order order) {
        double reference = pegReference(order);
        order.setPrice(reference + order.getPegOffset());
    }

    private double pegReference(Order order) {
        switch (order.getPegType()) {
            case BEST_BID:
                return getBestBid() != 0.0 ? getBestBid() : order.getPrice();
            case BEST_ASK:
                return getBestAsk() != 0.0 ? getBestAsk() : order.getPrice();
            case MID:
                return mid();
            default:
                return order.getPrice();
        }
    }

    private double mid() {
        double bid = getBestBid();
        double ask = getBestAsk();
        if (bid != 0.0 && ask != 0.0) {
            return (bid + ask) / 2.0;
        }
        if (bid != 0.0) {
            return bid;
        }
        if (ask != 0.0) {
            return ask;
        }
        return lastPrice;
    }

    /**
     * Re-prices all pegged orders against the current reference and re-sorts.
     */
    public void refreshPegged() {
        List<Order> pegged = new ArrayList<Order>();
        Iterator<Order> it = bids.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o.getPegType() != PegType.NONE) {
                pegged.add(o);
                it.remove();
            }
        }
        it = asks.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o.getPegType() != PegType.NONE) {
                pegged.add(o);
                it.remove();
            }
        }
        for (Order o : pegged) {
            applyPeg(o);
            insertSorted(o);
        }
    }

    /**
     * Evaluates stop orders against the given reference price, triggering those
     * whose trigger has been crossed by submitting the resulting order.
     */
    public void checkStops(double marketPrice, Exchange exchange) {
        List<Order> triggered = new ArrayList<Order>();
        Iterator<Order> it = stops.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            boolean fire = false;
            if (o.getType() == OrderType.TRAILING_STOP) {
                if (o.getSide() == Side.SELL) {
                    o.setTrailingBest(Math.max(o.getTrailingBest(), marketPrice));
                    fire = marketPrice <= o.getTrailingBest() - o.getTrailingOffset();
                } else {
                    o.setTrailingBest(Math.min(o.getTrailingBest(), marketPrice));
                    fire = marketPrice >= o.getTrailingBest() + o.getTrailingOffset();
                }
            } else if (o.getSide() == Side.SELL) {
                fire = marketPrice <= o.getStopPrice();
            } else {
                fire = marketPrice >= o.getStopPrice();
            }
            if (fire) {
                it.remove();
                triggered.add(o);
            }
        }
        for (Order o : triggered) {
            OrderType resultType = o.getType() == OrderType.STOP_LIMIT ? OrderType.LIMIT : OrderType.MARKET;
            Order replacement = new Order(exchange.nextOrderId(), asset, o.getSide(), resultType, o.getRemaining(), o.getPrice(), o.getParticipant());
            replacement.setTimeInForce(TimeInForce.GTC);
            enter(replacement, exchange);
        }
    }

    public Order findOrder(long id) {
        for (Order o : bids) {
            if (o.getId() == id) {
                return o;
            }
        }
        for (Order o : asks) {
            if (o.getId() == id) {
                return o;
            }
        }
        for (Order o : stops) {
            if (o.getId() == id) {
                return o;
            }
        }
        return null;
    }

    public boolean cancel(long orderId) {
        Iterator<Order> it = bids.iterator();
        while (it.hasNext()) {
            if (it.next().getId() == orderId) {
                it.remove();
                return true;
            }
        }
        it = asks.iterator();
        while (it.hasNext()) {
            if (it.next().getId() == orderId) {
                it.remove();
                return true;
            }
        }
        it = stops.iterator();
        while (it.hasNext()) {
            if (it.next().getId() == orderId) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Amends the price of a resting order, re-queuing it at the new price.
     */
    public boolean amendPrice(long orderId, double newPrice) {
        Order order = removeFromBook(orderId);
        if (order == null) {
            return false;
        }
        order.setPrice(newPrice);
        order.initializeDisplay();
        insertSorted(order);
        return true;
    }

    /**
     * Reduces the quantity of a resting order (time priority retained).
     */
    public boolean reduceQuantity(long orderId, long newQty) {
        Order order = findOrder(orderId);
        if (order == null) {
            return false;
        }
        long filled = order.getQuantity() - order.getRemaining();
        long reduceTo = Math.max(filled, newQty);
        long reduction = order.getRemaining() - (reduceTo - filled);
        if (reduction > 0) {
            order.reduce(reduction);
        }
        return true;
    }

    private Order removeFromBook(long orderId) {
        Iterator<Order> it = bids.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o.getId() == orderId) {
                it.remove();
                return o;
            }
        }
        it = asks.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o.getId() == orderId) {
                it.remove();
                return o;
            }
        }
        return null;
    }

    private void addTrade(Trade trade) {
        trades.add(trade);
        lastPrice = trade.getPrice();
        vwap = (vwap * tradedVolume + trade.getPrice() * trade.getQuantity()) / (tradedVolume + trade.getQuantity());
        tradedVolume += trade.getQuantity();
    }
}
