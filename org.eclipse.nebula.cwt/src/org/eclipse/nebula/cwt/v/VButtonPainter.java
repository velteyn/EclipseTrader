/****************************************************************************
 * Copyright (c) 2008, 2009 Jeremy Dowdall
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeremy Dowdall <jeremyd@aspencloud.com> - initial API and implementation
 *****************************************************************************/

package org.eclipse.nebula.cwt.v;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;

public class VButtonPainter extends VControlPainter {

	@Override
	public void paintBackground(VControl control, Event e) {
		VButton button = (VButton) control;
		if (button.paintNative) {
			Rectangle b = button.getBounds();
			e.gc.setAntialias(SWT.ON);

			// Determine colors based on state
			org.eclipse.swt.graphics.Color bg;
			org.eclipse.swt.graphics.Color border = e.display.getSystemColor(SWT.COLOR_WIDGET_BORDER);

			if (button.hasState(VButton.STATE_SELECTED)) {
				bg = e.display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
			} else if (button.hasState(VButton.STATE_ACTIVE)) {
				bg = e.display.getSystemColor(SWT.COLOR_LIST_SELECTION);
			} else {
				bg = button.getBackground();
			}

			// Paint the button background
			e.gc.setBackground(bg);
			e.gc.fillRoundRectangle(b.x, b.y, b.width, b.height, 6, 6);

			// Paint the border
			e.gc.setForeground(border);
			e.gc.drawRoundRectangle(b.x, b.y, b.width - 1, b.height - 1, 6, 6);

			// Paint focus rectangle if necessary
			if (button == VTracker.getFocusControl()) {
				e.gc.drawFocus(b.x + 2, b.y + 2, b.width - 4, b.height - 4);
			}
		} else {
			super.paintBackground(control, e);
		}
	}

}
