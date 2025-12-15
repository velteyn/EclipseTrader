package org.eclipsetrader.core;

import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class CashModernTest {

    @Test
    void testEquals() {
        Cash cash = new Cash(1.0, Currency.getInstance("USD"));
        Assertions.assertTrue(cash.equals(new Cash(1.0, Currency.getInstance("USD"))));
    }

    @Test
    void testEqualsToOtherObjects() {
        Cash cash = new Cash(1.0, Currency.getInstance("USD"));
        Assertions.assertFalse(cash.equals(new Object()));
    }

    @Test
    void testAddToHashSet() {
        Set<Cash> set = new HashSet<Cash>();
        set.add(new Cash(1.0, Currency.getInstance("USD")));
        Assertions.assertTrue(set.contains(new Cash(1.0, Currency.getInstance("USD"))));
        Assertions.assertFalse(set.contains(new Cash(1.0, Currency.getInstance("EUR"))));
    }
}
