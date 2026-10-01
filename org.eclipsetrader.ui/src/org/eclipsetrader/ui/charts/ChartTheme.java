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

import org.eclipse.swt.graphics.RGB;

/**
 * Immutable chart color theme.
 *
 * @since 1.0
 */
public class ChartTheme {

    private final RGB line;
    private final RGB positive;
    private final RGB negative;
    private final RGB outline;
    private final RGB grid;
    private final RGB background;

    public ChartTheme(RGB line, RGB positive, RGB negative, RGB outline, RGB grid, RGB background) {
        this.line = line;
        this.positive = positive;
        this.negative = negative;
        this.outline = outline;
        this.grid = grid;
        this.background = background;
    }

    public RGB getLine() {
        return line;
    }

    public RGB getPositive() {
        return positive;
    }

    public RGB getNegative() {
        return negative;
    }

    public RGB getOutline() {
        return outline;
    }

    public RGB getGrid() {
        return grid;
    }

    public RGB getBackground() {
        return background;
    }
}
