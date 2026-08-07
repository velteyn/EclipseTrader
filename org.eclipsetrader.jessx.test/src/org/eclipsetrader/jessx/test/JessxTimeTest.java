package org.eclipsetrader.jessx.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.eclipsetrader.jessx.internal.core.JessxTime;
import org.junit.jupiter.api.Test;

public class JessxTimeTest {

    @Test
    void ciVerificationProbe() {
        assertTrue(false, "CI test-execution probe: this must run and fail");
    }

    @Test
    void elapsedValueResolvesToCurrentEraArrivalTime() {
        long elapsed = 3_600_000L; // one hour into the period
        Date result = JessxTime.toAbsoluteDate(elapsed);
        long now = System.currentTimeMillis();
        assertTrue(result.getTime() >= now - 60_000L, "arrival time should be within a minute of now");
        assertTrue(result.getTime() <= now + 60_000L, "arrival time should not be in the future");
        assertTrue(result.getTime() > 946684800000L, "elapsed value must not resolve to a 1970 date");
    }

    @Test
    void epochValuePassesThroughUnchanged() {
        long epoch = System.currentTimeMillis();
        assertEquals(new Date(epoch), JessxTime.toAbsoluteDate(epoch));
    }

    @Test
    void thresholdBoundaryIsTreatedAsEpoch() {
        assertEquals(new Date(JessxTime.ELAPSED_THRESHOLD), JessxTime.toAbsoluteDate(JessxTime.ELAPSED_THRESHOLD));
    }

    @Test
    void justBelowThresholdResolvesToArrivalTime() {
        long elapsed = JessxTime.ELAPSED_THRESHOLD - 1;
        Date result = JessxTime.toAbsoluteDate(elapsed);
        long now = System.currentTimeMillis();
        assertTrue(result.getTime() >= now - 60_000L, "arrival time should be within a minute of now");
        assertTrue(result.getTime() <= now + 60_000L, "arrival time should not be in the future");
    }
}
