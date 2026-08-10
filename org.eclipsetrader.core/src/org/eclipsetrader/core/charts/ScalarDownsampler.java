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

package org.eclipsetrader.core.charts;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.charts.NumberValue;

/**
 * Aggregates scalar (date, number) values into one bin per pixel column so that
 * zoomed-out charts render at most one element per column. Each bin keeps the
 * value with the greatest deviation from the column mean, which preserves
 * spikes in the rendered line, area or histogram.
 */
public final class ScalarDownsampler {

    private ScalarDownsampler() {
    }

    /**
     * Aggregates the given values into at most <code>width</code> bins.
     *
     * @param values the source values, adapting to <code>Date</code> and <code>Number</code>.
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

        IAdaptable[] result = new IAdaptable[width];
        for (int column = 0; column < width; column++) {
            int start = (int) ((long) column * values.length / width);
            int end = (int) ((long) (column + 1) * values.length / width);
            if (end <= start) {
                end = start + 1;
            }
            if (end > values.length) {
                end = values.length;
            }

            double sum = 0.0;
            int count = 0;
            for (int i = start; i < end; i++) {
                Number number = (Number) values[i].getAdapter(Number.class);
                if (number != null) {
                    sum += number.doubleValue();
                    count++;
                }
            }
            double mean = count != 0 ? sum / count : 0.0;

            int representative = -1;
            double bestDeviation = -1.0;
            for (int i = start; i < end; i++) {
                Number number = (Number) values[i].getAdapter(Number.class);
                if (number == null) {
                    continue;
                }
                double deviation = Math.abs(number.doubleValue() - mean);
                if (representative == -1 || deviation > bestDeviation) {
                    representative = i;
                    bestDeviation = deviation;
                }
            }
            if (representative == -1) {
                representative = end - 1;
            }

            IAdaptable value = values[representative];
            Date date = (Date) value.getAdapter(Date.class);
            Number number = (Number) value.getAdapter(Number.class);
            result[column] = new NumberValue(date, number);
        }
        return result;
    }
}
