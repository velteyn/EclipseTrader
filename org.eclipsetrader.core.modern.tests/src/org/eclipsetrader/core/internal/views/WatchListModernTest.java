package org.eclipsetrader.core.internal.views;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.core.views.IWatchListColumn;
import org.eclipsetrader.core.views.WatchList;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

@RunWith(JUnitPlatform.class)
public class WatchListModernTest {

    @Test
    void testFireNamePropertyChange() {
        final PropertyChangeEvent[] holder = new PropertyChangeEvent[1];
        PropertyChangeListener listener = evt -> holder[0] = evt;

        WatchList list = new WatchList("List", new IWatchListColumn[0]);
        ((PropertyChangeSupport) list.getAdapter(PropertyChangeSupport.class)).addPropertyChangeListener(listener);
        list.setName("List 1");

        Assertions.assertNotNull(holder[0]);
        Assertions.assertEquals(IWatchList.NAME, holder[0].getPropertyName());
        Assertions.assertEquals("List", holder[0].getOldValue());
        Assertions.assertEquals("List 1", holder[0].getNewValue());
        Assertions.assertSame(list, holder[0].getSource());
    }
}
