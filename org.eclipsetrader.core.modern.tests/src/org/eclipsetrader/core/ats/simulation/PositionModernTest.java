package org.eclipsetrader.core.ats.simulation;

import org.eclipsetrader.core.instruments.Security;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class PositionModernTest {

    @Test
    void testAveragePriceOnSameSignAdds() {
        Position p = new Position(new Security("SEC", null), 100L, 10.0);
        p.add(50L, 12.0);
        Assertions.assertEquals(150L, p.getQuantity().longValue());
        Assertions.assertEquals((100 * 10.0 + 50 * 12.0) / 150.0, p.getPrice());
    }

    @Test
    void testPriceResetOnSignChangeToSameDirection() {
        Position p = new Position(new Security("SEC", null), 100L, 10.0);
        p.add(-80L, 11.0);
        Assertions.assertEquals(20L, p.getQuantity().longValue());
        p.add(10L, 13.0);
        Assertions.assertEquals(30L, p.getQuantity().longValue());
        Assertions.assertEquals((20 * 10.0 + 10 * 13.0) / 30.0, p.getPrice());
    }
}
