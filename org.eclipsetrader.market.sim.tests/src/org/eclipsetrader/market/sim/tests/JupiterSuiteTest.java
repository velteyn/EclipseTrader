package org.eclipsetrader.market.sim.tests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectClasses({ MatchingTest.class, PositionsTest.class, LeverageTest.class, AdaptationTest.class, CalendarTest.class })
public class JupiterSuiteTest {
}
