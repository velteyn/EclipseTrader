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

package org.eclipsetrader.ui.charts;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;

/**
 * Aggregates OHLC values into one bin per pixel column so that zoomed-out
 * charts render at most one element per column while preserving each column's
 * high, low, first open and last close.
 */
public final class OHLCDownsampler {

    private OHLCDownsampler() {
    }

    /**
     * Aggregates the given values into at most <code>width</code> bins.
     *
     * @param values the source values.
     * @param width the maximum number of bins (pixel columns).
     * @return the aggregated values, or the source array if no aggregation is needed.
     */
    public static IOHLC[] downsample(IOHLC[] values, int width) {
        if (values == null || values.length == 0) {
            return new IOHLC[0];
        }
        if (width <= 0 || values.length <= width) {
            return values;
        }

        IOHLC[] result = new IOHLC[width];
        for (int column = 0; column < width; column++) {
            int start = (int) ((long) column * values.length / width);
            int end = (int) ((long) (column + 1) * values.length / width);
            if (end <= start) {
                end = start + 1;
            }
            if (end > values.length) {
                end = values.length;
            }

            IOHLC first = values[start];
            IOHLC last = values[end - 1];
            Double high = first.getHigh();
            Double low = first.getLow();
            for (int i = start + 1; i < end; i++) {
                if (values[i].getHigh() != null && (high == null || values[i].getHigh() > high)) {
                    high = values[i].getHigh();
                }
                if (values[i].getLow() != null && (low == null || values[i].getLow() < low)) {
                    low = values[i].getLow();
                }
            }
            result[column] = new OHLC(first.getDate(), first.getOpen(), high, low, last.getClose(), null);
        }
        return result;
    }

    /**
     * Aggregates the given adaptable OHLC values into at most <code>width</code>
     * bins, returning values that adapt to <code>IOHLC</code>, <code>Date</code>
     * and <code>Number</code>.
     *
     * @param values the source values.
     * @param width the maximum number of bins (pixel columns).
     * @return the aggregated values, or the source array if no aggregation is needed.
     */
    public static IAdaptable[] downsample(IAdaptable[] values, int width) {
        if (values == null || values.length == 0) {
            return new IAdaptable[0];
        }
        if (width <= 0 || values.length <= width) {
            return values;
        }

        IOHLC[] source = new IOHLC[values.length];
        int count = 0;
        for (int i = 0; i < values.length; i++) {
            IOHLC ohlc = (IOHLC) values[i].getAdapter(IOHLC.class);
            if (ohlc != null) {
                source[count++] = ohlc;
            }
        }
        if (count == 0) {
            return new IAdaptable[0];
        }
        if (count < source.length) {
            IOHLC[] compact = new IOHLC[count];
            System.arraycopy(source, 0, compact, 0, count);
            source = compact;
        }

        IOHLC[] aggregated = downsample(source, width);

        IAdaptable[] result = new IAdaptable[aggregated.length];
        for (int i = 0; i < aggregated.length; i++) {
            result[i] = new Value(aggregated[i]);
        }
        return result;
    }

    private static class Value implements IAdaptable {

        private final IOHLC ohlc;

        public Value(IOHLC ohlc) {
            this.ohlc = ohlc;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @Override
        @SuppressWarnings({
            "unchecked", "rawtypes"
        })
        public Object getAdapter(Class adapter) {
            if (ohlc != null && adapter.isAssignableFrom(ohlc.getClass())) {
                return ohlc;
            }
            if (adapter.isAssignableFrom(Date.class)) {
                return ohlc != null ? ohlc.getDate() : null;
            }
            if (adapter.isAssignableFrom(Double.class) || adapter.isAssignableFrom(Number.class)) {
                return ohlc != null ? ohlc.getClose() : null;
            }
            return null;
        }
    }
}
