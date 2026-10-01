package org.eclipsetrader.market.sim.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Bundle activator for the market simulator. Headless-safe: performs no
 * platform or display work on startup, so the bundle can be loaded in test and
 * CI environments without a display.
 */
public class MarketSimActivator implements BundleActivator {

    private static MarketSimActivator instance;

    public static MarketSimActivator getDefault() {
        return instance;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        instance = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        instance = null;
    }
}
