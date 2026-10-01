package org.eclipsetrader.core.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipsetrader.core.internal.CoreActivator;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    private static final String JESSX_STREAMING_CONNECTOR = "org.eclipsetrader.jessx.connector";

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences node = DefaultScope.INSTANCE.getNode(CoreActivator.PLUGIN_ID);
        node.put(CoreActivator.DEFAULT_CONNECTOR_ID, JESSX_STREAMING_CONNECTOR);
    }
}
