/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.charts;

import org.eclipsetrader.core.charts.IDataSeries;

/**
 * A lightweight chart object.
 * Charts are rendered to an <code>IGraphics</code> object.  Objects can be composed to
 * create complex charts.
 *
 * @since 1.0
 */
public interface IChartObject {

	public IDataSeries getDataSeries();

	public void setDataBounds(DataBounds bounds);

	public void paint(IGraphics graphics);

	public String getToolTip();

	/**
	 * Returns the tooltip string for the element at the given location.
	 * <p>A value of <code>SWT.DEFAULT</code> for either x or y means that that value
	 * should not be considered.</p>
	 *
	 * @param x the X coordinate.
	 * @param y the Y coordinate.
	 * @return the tooltip text, or <code>null</code>.
	 */
	public String getToolTip(int x, int y);

	/**
	 * Returns true if the point (x, y) is contained within this object's bounds.
	 * <p>A value of <code>SWT.DEFAULT</code> for either x or y means that that value
	 * should not be considered.</p>
	 *
	 * @param x the X coordinate.
	 * @param y the Y coordinate.
	 * @return <code>true</code> if the point (x, y) is contained in this IChartObject's bounds.
	 */
	public boolean containsPoint(int x, int y);

	/**
	 * Adds the given object as a child of this object.
	 *
	 * @param object the object to add.
	 */
	public void add(IChartObject object);

	/**
	 * Removes the given object from this object's children.
	 *
	 * @param object the object to remove.
	 */
	public void remove(IChartObject object);

	/**
	 * Returns a possibly empty array of children by reference.
	 *
	 * @return the children array.
	 */
	public IChartObject[] getChildren();

	public IChartObject getParent();

	public void setParent(IChartObject parent);

	/**
	 * Called when this object has gained focus.
	 *
	 * @param event the focus event.
	 */
	public void handleFocusGained(ChartObjectFocusEvent event);

	/**
	 * Called when this object has lost focus.
	 *
	 * @param event the focus event.
	 */
	public void handleFocusLost(ChartObjectFocusEvent event);

	public void accept(IChartObjectVisitor visitor);
}
