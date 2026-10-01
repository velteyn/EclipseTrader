package org.eclipsetrader.market.sim.engine;

/**
 * Time-in-force modifier for an order.
 */
public enum TimeInForce {
    /** Good till cancelled: rest in the book until filled or cancelled. */
    GTC,
    /** Immediate or cancel: fill what is available now, cancel the rest. */
    IOC,
    /** Fill or kill: fill the entire quantity now or cancel with no fill. */
    FOK
}
