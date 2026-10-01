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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

/**
 * Chart rendering utilities.
 *
 * @since 1.0
 */
public final class ChartUtils {

    private ChartUtils() {
    }

    /**
     * Creates the offscreen image backing a chart canvas. The image is sized in
     * logical (device-independent) pixels; SWT 4.31 auto-scales it to the
     * device's native resolution at creation time.
     *
     * @param canvas the canvas the image backs
     * @param bounds the logical size of the image
     * @return the offscreen image
     */
    public static Image createBackingImage(Canvas canvas, Rectangle bounds) {
        return new Image(canvas.getDisplay(), bounds.width, bounds.height);
    }

    /**
     * Returns the display zoom (percent) of the canvas's monitor, falling back
     * to the DPI-derived zoom when the monitor zoom is unavailable.
     *
     * @param canvas the canvas
     * @return the zoom factor in percent (100 = no scaling)
     */
    public static int getZoom(Canvas canvas) {
        int zoom = canvas.getMonitor().getZoom();
        if (zoom <= 0) {
            zoom = canvas.getDisplay().getDPI().x * 100 / 72;
        }
        return zoom;
    }
}
