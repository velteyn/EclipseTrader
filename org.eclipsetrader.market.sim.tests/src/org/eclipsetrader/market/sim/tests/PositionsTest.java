package org.eclipsetrader.market.sim.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipsetrader.market.sim.engine.Exchange;
import org.eclipsetrader.market.sim.engine.OrderType;
import org.eclipsetrader.market.sim.engine.Side;
import org.eclipsetrader.market.sim.risk.Account;
import org.junit.jupiter.api.Test;

public class PositionsTest {

    @Test
    void buyOpensLongPosition() {
        Account account = new Account("P", 1000);
        account.applyTrade("X", 100, 10.0);
        assertEquals(100, account.getPosition("X"));
    }

    @Test
    void sellWithoutHoldingsOpensShort() {
        Account account = new Account("P", 1000);
        account.applyTrade("X", -100, 10.0);
        assertEquals(-100, account.getPosition("X"));
    }

    @Test
    void buyCoversShortBeforeOpeningLong() {
        Account account = new Account("P", 1000);
        account.applyTrade("X", -100, 10.0);
        account.applyTrade("X", 150, 10.0);
        assertEquals(50, account.getPosition("X"));
    }

    @Test
    void sellFromLongReducesPosition() {
        Account account = new Account("P", 1000);
        account.applyTrade("X", 100, 10.0);
        account.applyTrade("X", -60, 10.0);
        assertEquals(40, account.getPosition("X"));
    }

    @Test
    void shortLimitRejectsExcessSelling() {
        Account account = new Account("P", 1000);
        account.setShortLimit(50);
        account.applyTrade("X", -50, 10.0);
        assertEquals(-50, account.getPosition("X"));
        assertEquals(false, account.canSell("X", 1));
    }

    @Test
    void exchangeRejectsOrderBeyondShortLimit() {
        Exchange ex = new Exchange();
        ex.setNow(1_000_000L);
        Account account = new Account("P", 1000);
        account.setShortLimit(50);
        ex.addAccount(account);

        assertNull(ex.enterOrder("X", Side.SELL, OrderType.LIMIT, 100, 10, "P"));
    }

    @Test
    void tradeUpdatesBothSides() {
        Exchange ex = new Exchange();
        ex.setNow(1_000_000L);
        Account buyer = new Account("B", 1000);
        Account seller = new Account("S", 1000);
        ex.addAccount(buyer);
        ex.addAccount(seller);

        ex.enterOrder("X", Side.SELL, OrderType.LIMIT, 100, 10, "S");
        ex.enterOrder("X", Side.BUY, OrderType.LIMIT, 100, 10, "B");

        assertEquals(100, buyer.getPosition("X"));
        assertEquals(-100, seller.getPosition("X"));
    }
}
