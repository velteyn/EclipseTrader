/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.jessx.internal.core;

import java.util.Date;

/**
 * Interprets JESSX deal and order timestamps.
 *
 * The JESSX simulation sends deal/order timestamps as elapsed milliseconds
 * within the current period, not as absolute epoch millis. Values below
 * {@link #ELAPSED_THRESHOLD} are elapsed-in-period values and resolve to the
 * arrival time; values at or above the threshold are treated as epoch millis.
 */
public final class JessxTime {

    /** Maximum plausible period duration (one day, in milliseconds). */
    public static final long ELAPSED_THRESHOLD = 24L * 60L * 60L * 1000L;

    private JessxTime() {
    }

    /**
     * Converts a JESSX deal/order timestamp to an absolute date.
     *
     * @param timestamp the timestamp as sent by the JESSX simulation
     * @return the arrival time for elapsed-in-period values, or the epoch-based
     *         date for values at or above the elapsed threshold
     */
    public static Date toAbsoluteDate(long timestamp) {
        if (timestamp < ELAPSED_THRESHOLD) {
            return new Date(System.currentTimeMillis());
        }
        return new Date(timestamp);
    }
}
