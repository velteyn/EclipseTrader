package org.eclipsetrader.market.sim.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipsetrader.market.sim.engine.Exchange;
import org.eclipsetrader.market.sim.engine.OrderType;
import org.eclipsetrader.market.sim.engine.Side;
import org.eclipsetrader.market.sim.risk.Account;
import org.junit.jupiter.api.Test;

public class LeverageTest {

    @Test
    void buyingPowerIsCashTimesLeverage() {
        Account account = new Account("P", 100);
        account.setLeverage(4);
        assertTrue(account.canBuy(400));
        assertFalse(account.canBuy(401));
    }

    @Test
    void leveragedBuyMakesCashNegative() {
        Account account = new Account("P", 100);
        account.setLeverage(4);
        account.applyTrade("X", 30, 10.0);
        assertEquals(-200.0, account.getCash(), 0.0001);
    }

    @Test
    void marginRejectsOrderBeyondBuyingPower() {
        Exchange ex = new Exchange();
        ex.setNow(1_000_000L);
        Account account = new Account("P", 100);
        account.setLeverage(4);
        ex.addAccount(account);
        assertNull(ex.enterOrder("X", Side.BUY, OrderType.LIMIT, 100, 10, "P"));
    }

    @Test
    void equityReflectsLongAndShort() {
        Account account = new Account("P", 1000);
        account.applyTrade("X", 10, 10.0);   // long 10 @ 10
        account.applyTrade("Y", -5, 20.0);   // short 5 @ 20
        Map<String, Double> prices = new HashMap<String, Double>();
        prices.put("X", 10.0);
        prices.put("Y", 20.0);
        assertEquals(1000.0 + 100.0 - 100.0, account.equity(prices), 0.0001);
    }

    @Test
    void loanAccruesInterest() {
        Account account = new Account("P", 100);
        account.setInterestRate(0.05);
        account.applyTrade("X", 30, 10.0);   // cash -200
        account.accrueInterest();
        assertEquals(-210.0, account.getCash(), 0.0001);
    }

    @Test
    void marginCallForceLiquidates() {
        Exchange ex = new Exchange();
        ex.setNow(1_000_000L);
        Account mm = new Account("MM", 1_000_000);
        ex.addAccount(mm);
        Account account = new Account("P", 100);
        account.setLeverage(4);
        account.setMaintenanceMargin(0.6);
        ex.addAccount(account);

        // Liquidity provider quotes both sides (spread apart so they do not cross
        // each other) so the leveraged buy and the later liquidation sell have
        // a counterparty.
        ex.enterOrder("X", Side.SELL, OrderType.LIMIT, 100, 11, "MM");
        ex.enterOrder("X", Side.BUY, OrderType.LIMIT, 100, 9, "MM");

        // Buy 20 @ 11 = cost 220, cash becomes -120 (leveraged).
        ex.enterOrder("X", Side.BUY, OrderType.LIMIT, 20, 11, "P");
        assertFalse(account.isFlat());

        // Equity is 100, gross exposure 220, maintenance threshold 132.
        ex.checkMargin();
        assertTrue(account.isFlat());
    }
}
