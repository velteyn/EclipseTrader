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

import java.text.NumberFormat;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipsetrader.core.feed.IOHLC;

public class SummaryOHLCItem {

    Label label;
    Label changeLabel;

    NumberFormat numberFormat = NumberFormat.getInstance();
    NumberFormat percentFormat = NumberFormat.getInstance();
    Color foreground;
    Color positiveForeground;
    Color negativeForeground;

    public SummaryOHLCItem(Composite parent, int style) {
        changeLabel = new Label(parent, SWT.NONE);
        label = new Label(parent, SWT.NONE);

        label.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                dispose();
            }
        });

        foreground = new Color(parent.getDisplay(), 33, 150, 243);
        positiveForeground = new Color(parent.getDisplay(), 38, 166, 154);
        negativeForeground = new Color(parent.getDisplay(), 239, 83, 80);

        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(4);
        numberFormat.setGroupingUsed(true);

        percentFormat.setMinimumFractionDigits(2);
        percentFormat.setMaximumFractionDigits(2);
        percentFormat.setGroupingUsed(false);
    }

    public Color getForeground() {
        return foreground;
    }

    public void setForeground(Color color) {
        this.foreground = color;
    }

    public Color getPositiveForeground() {
        return positiveForeground;
    }

    public void setPositiveForeground(Color positiveColor) {
        this.positiveForeground = positiveColor;
    }

    public Color getNegativeForeground() {
        return negativeForeground;
    }

    public void setNegativeForeground(Color negativeColor) {
        this.negativeForeground = negativeColor;
    }

    public void setOHLC(IOHLC currentOHLC, IOHLC previousOHLC) {
        if (currentOHLC != null) {
            label.setText(NLS.bind("O:{0} H:{1} L:{2} C:{3}", new Object[] { //$NON-NLS-1$
                    numberFormat.format(currentOHLC.getOpen()),
                    numberFormat.format(currentOHLC.getHigh()),
                    numberFormat.format(currentOHLC.getLow()),
                    numberFormat.format(currentOHLC.getClose()),
            }));

            if (previousOHLC != null) {
                double change = (currentOHLC.getClose() - previousOHLC.getClose()) / previousOHLC.getClose() * 100.0;
                changeLabel.setText(NLS.bind("{0}%", new Object[] { //$NON-NLS-1$
                    (change > 0 ? "+" : "") + percentFormat.format(change), //$NON-NLS-1$ //$NON-NLS-2$
                }));
                if (change > 0) {
                    changeLabel.setForeground(positiveForeground);
                }
                else if (change < 0) {
                    changeLabel.setForeground(negativeForeground);
                }
                else {
                    changeLabel.setForeground(null);
                }
            }
        }
        else {
            label.setText(""); //$NON-NLS-1$
            changeLabel.setText(""); //$NON-NLS-1$
            changeLabel.setForeground(null);
        }
        label.setForeground(foreground);

        label.getParent().layout();
    }

    public void dispose() {
        if (foreground != null) {
            foreground.dispose();
        }
        if (positiveForeground != null) {
            positiveForeground.dispose();
        }
        if (negativeForeground != null) {
            negativeForeground.dispose();
        }
    }
}
