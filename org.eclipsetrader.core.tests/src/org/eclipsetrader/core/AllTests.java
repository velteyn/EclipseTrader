package org.eclipsetrader.core;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.eclipsetrader.core");
        //$JUnit-BEGIN$
        suite.addTestSuite(CashTest.class);
        suite.addTestSuite(org.eclipsetrader.core.internal.CurrencyServiceTest.class);
        //$JUnit-END$
        return suite;
    }
}
