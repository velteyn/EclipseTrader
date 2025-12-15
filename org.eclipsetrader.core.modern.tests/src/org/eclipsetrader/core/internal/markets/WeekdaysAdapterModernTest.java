package org.eclipsetrader.core.internal.markets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

@RunWith(JUnitPlatform.class)
public class WeekdaysAdapterModernTest {

    @Test
    void testMarshalWorkdays() throws Exception {
        WeekdaysAdapter adapter = new WeekdaysAdapter();
        Assertions.assertEquals("-MTWTF-", adapter.marshal(new HashSet<Integer>(Arrays.asList(new Integer[] {
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
        }))));
    }

    @Test
    void testUnmarshalWorkdays() throws Exception {
        WeekdaysAdapter adapter = new WeekdaysAdapter();
        Set<Integer> set = adapter.unmarshal("-MTWTF-");
        Assertions.assertFalse(set.contains(Calendar.SUNDAY));
        Assertions.assertTrue(set.contains(Calendar.MONDAY));
        Assertions.assertTrue(set.contains(Calendar.TUESDAY));
        Assertions.assertTrue(set.contains(Calendar.WEDNESDAY));
        Assertions.assertTrue(set.contains(Calendar.THURSDAY));
        Assertions.assertTrue(set.contains(Calendar.FRIDAY));
        Assertions.assertFalse(set.contains(Calendar.SATURDAY));
    }

    @Test
    void testMarshalFull() throws Exception {
        WeekdaysAdapter adapter = new WeekdaysAdapter();
        Assertions.assertEquals("SMTWTFS", adapter.marshal(new HashSet<Integer>(Arrays.asList(new Integer[] {
                Calendar.SUNDAY,
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
                Calendar.SATURDAY,
        }))));
    }

    @Test
    void testUnmarshalFull() throws Exception {
        WeekdaysAdapter adapter = new WeekdaysAdapter();
        Set<Integer> set = adapter.unmarshal("SMTWTFS");
        Assertions.assertTrue(set.contains(Calendar.SUNDAY));
        Assertions.assertTrue(set.contains(Calendar.MONDAY));
        Assertions.assertTrue(set.contains(Calendar.TUESDAY));
        Assertions.assertTrue(set.contains(Calendar.WEDNESDAY));
        Assertions.assertTrue(set.contains(Calendar.THURSDAY));
        Assertions.assertTrue(set.contains(Calendar.FRIDAY));
        Assertions.assertTrue(set.contains(Calendar.SATURDAY));
    }

    @Test
    void testMarshalNull() throws Exception {
        WeekdaysAdapter adapter = new WeekdaysAdapter();
        Assertions.assertNull(adapter.marshal(null));
    }

    @Test
    void testUnmarshalNull() throws Exception {
        WeekdaysAdapter adapter = new WeekdaysAdapter();
        Assertions.assertNull(adapter.unmarshal(null));
    }
}
