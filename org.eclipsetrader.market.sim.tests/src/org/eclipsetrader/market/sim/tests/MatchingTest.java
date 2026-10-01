package org.eclipsetrader.market.sim.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipsetrader.market.sim.engine.Exchange;
import org.eclipsetrader.market.sim.engine.Order;
import org.eclipsetrader.market.sim.engine.OrderBook;
import org.eclipsetrader.market.sim.engine.OrderType;
import org.eclipsetrader.market.sim.engine.PegType;
import org.eclipsetrader.market.sim.engine.Side;
import org.eclipsetrader.market.sim.engine.TimeInForce;
import org.eclipsetrader.market.sim.engine.Trade;
import org.junit.jupiter.api.Test;

public class MatchingTest {

    private long id;

    private Exchange exchange() {
        Exchange ex = new Exchange();
        ex.setNow(1_000_000L);
        return ex;
    }

    private Order order(Exchange ex, String asset, Side side, OrderType type, long qty, double price, String who) {
        return new Order(ex.nextOrderId(), asset, side, type, qty, price, who);
    }

    @Test
    void priceTimePriorityFullDepthSweep() {
        Exchange ex = exchange();
        OrderBook book = ex.getBook("X");
        Order a = order(ex, "X", Side.SELL, OrderType.LIMIT, 100, 10, "S1");
        Order b = order(ex, "X", Side.SELL, OrderType.LIMIT, 100, 11, "S2");
        book.enter(a, ex);
        book.enter(b, ex);

        List<Trade> trades = book.enter(order(ex, "X", Side.BUY, OrderType.LIMIT, 150, 11, "B"), ex);

        assertEquals(2, trades.size());
        assertEquals(100, a.getCumFilled());
        assertEquals(50, b.getCumFilled());
        assertEquals(10.0, trades.get(0).getPrice());
        assertEquals(11.0, trades.get(1).getPrice());
    }

    @Test
    void samePriceTradesInArrivalOrder() {
        Exchange ex = exchange();
        OrderBook book = ex.getBook("X");
        Order first = order(ex, "X", Side.SELL, OrderType.LIMIT, 100, 10, "S1");
        Order second = order(ex, "X", Side.SELL, OrderType.LIMIT, 100, 10, "S2");
        book.enter(first, ex);
        book.enter(second, ex);

        book.enter(order(ex, "X", Side.BUY, OrderType.LIMIT, 150, 10, "B"), ex);

        assertEquals(100, first.getCumFilled());
        assertEquals(50, second.getCumFilled());
    }

    @Test
    void marketOrderSweepsAndCancelsRemainder() {
        Exchange ex = exchange();
        OrderBook book = ex.getBook("X");
        book.enter(order(ex, "X", Side.SELL, OrderType.LIMIT, 100, 10, "S"), ex);

        Order buy = order(ex, "X", Side.BUY, OrderType.MARKET, 150, 0, "B");
        List<Trade> trades = book.enter(buy, ex);

        assertEquals(1, trades.size());
        assertFalse(buy.isFilled());
        assertTrue(book.getBids().isEmpty());
    }

    @Test
    void limitRemainderRests() {
        Exchange ex = exchange();
        OrderBook book = ex.getBook("X");
        Order buy = order(ex, "X", Side.BUY, OrderType.LIMIT, 50, 10, "B");
        book.enter(buy, ex);
        assertTrue(book.getBids().contains(buy));
    }

    @Test
    void cancelRemovesRestingOrder() {
        Exchange ex = exchange();
        OrderBook book = ex.getBook("X");
        Order buy = order(ex, "X", Side.BUY, OrderType.LIMIT, 50, 10, "B");
        book.enter(buy, ex);
        assertTrue(book.cancel(buy.getId()));
        assertTrue(book.getBids().isEmpty());
    }

    @Test
    void amendPriceRequeues() {
        Exchange ex = exchange();
        OrderBook book = ex.getBook("X");
        Order buy = order(ex, "X", Side.BUY, OrderType.LIMIT, 50, 10, "B");
        book.enter(buy, ex);
        assertTrue(book.amendPrice(buy.getId(), 11));
        assertEquals(11.0, book.getBestBid());
    }

    @Test
    void iocPartialFillCancelsRemainder() {
        Exchange ex = exchange();
        OrderBook book = ex.getBook("X");
        book.enter(order(ex, "X", Side.SELL, OrderType.LIMIT, 100, 10, "S"), ex);

        Order buy = order(ex, "X", Side.BUY, OrderType.LIMIT, 150, 11, "B");
        buy.setTimeInForce(TimeInForce.IOC);
        List<Trade> trades = book.enter(buy, ex);

        assertEquals(1, trades.size());
        assertTrue(book.getBids().isEmpty());
        assertEquals(50, buy.getRemaining());
    }

    @Test
    void fokCancelsWhenNotFullyFillable() {
        Exchange ex = exchange();
        OrderBook book = ex.getBook("X");
        book.enter(order(ex, "X", Side.SELL, OrderType.LIMIT, 100, 10, "S"), ex);

        Order buy = order(ex, "X", Side.BUY, OrderType.LIMIT, 150, 11, "B");
        buy.setTimeInForce(TimeInForce.FOK);
        List<Trade> trades = book.enter(buy, ex);

        assertTrue(trades.isEmpty());
        assertTrue(book.getBids().isEmpty());
    }

    @Test
    void fokFillsWhenFullyFillable() {
        Exchange ex = exchange();
        OrderBook book = ex.getBook("X");
        book.enter(order(ex, "X", Side.SELL, OrderType.LIMIT, 100, 10, "S"), ex);

        Order buy = order(ex, "X", Side.BUY, OrderType.LIMIT, 50, 11, "B");
        buy.setTimeInForce(TimeInForce.FOK);
        List<Trade> trades = book.enter(buy, ex);

        assertEquals(1, trades.size());
        assertTrue(buy.isFilled());
    }

    @Test
    void icebergReplenishesDisplayQuantity() {
        Exchange ex = exchange();
        OrderBook book = ex.getBook("X");
        Order sell = order(ex, "X", Side.SELL, OrderType.LIMIT, 200, 10, "S");
        sell.setIceberg(50);
        book.enter(sell, ex);

        List<Trade> trades = book.enter(order(ex, "X", Side.BUY, OrderType.LIMIT, 120, 10, "B"), ex);

        assertEquals(3, trades.size());
        assertEquals(80, sell.getRemaining());
    }

    @Test
    void peggedOrderRepricesWithBook() {
        Exchange ex = exchange();
        OrderBook book = ex.getBook("X");
        book.enter(order(ex, "X", Side.SELL, OrderType.LIMIT, 100, 10, "S"), ex);

        Order peg = order(ex, "X", Side.BUY, OrderType.LIMIT, 10, 0, "B");
        peg.setPegType(PegType.BEST_ASK);
        peg.setPegOffset(-0.10);
        book.enter(peg, ex);
        assertEquals(9.90, peg.getPrice(), 0.0001);

        book.enter(order(ex, "X", Side.SELL, OrderType.LIMIT, 100, 9.95, "S2"), ex);
        assertEquals(9.85, peg.getPrice(), 0.0001);
    }

    @Test
    void stopOrderTriggersAndBecomesMarketOrder() {
        Exchange ex = exchange();
        OrderBook book = ex.getBook("X");
        book.enter(order(ex, "X", Side.BUY, OrderType.LIMIT, 100, 90, "B"), ex);

        Order stop = order(ex, "X", Side.SELL, OrderType.STOP, 100, 0, "S");
        stop.setStopPrice(90);
        book.enter(stop, ex);
        assertTrue(book.getStops().contains(stop));

        book.checkStops(89, ex);

        assertEquals(1, book.getTrades().size());
        assertTrue(book.getStops().isEmpty());
    }

    @Test
    void trailingStopFollowsMarketAndTriggersOnReversal() {
        Exchange ex = exchange();
        OrderBook book = ex.getBook("X");
        book.enter(order(ex, "X", Side.BUY, OrderType.LIMIT, 100, 98, "B"), ex);

        Order stop = order(ex, "X", Side.SELL, OrderType.TRAILING_STOP, 100, 0, "S");
        stop.setStopPrice(100);
        stop.setTrailingOffset(2);
        book.enter(stop, ex);

        book.checkStops(102, ex);
        assertTrue(book.getStops().contains(stop));

        book.checkStops(100, ex);
        assertTrue(book.getStops().isEmpty());
        assertEquals(1, book.getTrades().size());
    }
}
