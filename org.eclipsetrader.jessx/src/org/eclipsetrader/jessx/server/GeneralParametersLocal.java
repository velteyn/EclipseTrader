/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *     Edoardo BAROLO   - virtual investor 
 *     
 */

package org.eclipsetrader.jessx.server;

import javax.swing.JSpinner;
import javax.swing.event.ChangeListener;

import org.eclipse.trader.jessx.business.GeneralParameters;
import org.jdom.Element;

public class GeneralParametersLocal implements GeneralParameters {

	@Override
	public void loadFromXml(Element p0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveToXml(Element p0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addPeriodCountChangeListener(ChangeListener p0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePeriodCountChangeListener(ChangeListener p0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addPeriodDurationChangeListener(ChangeListener p0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePeriodDurationChangeListener(ChangeListener p0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getAfterSetupJoiningAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAfterSetupJoiningAllowed(boolean p0) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSetupFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSetupFileName(String p0) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getXMLVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setXMLVersion(String p0) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getLoggingFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLoggingFileName(String p0) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPeriodCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPeriodCount(int p0) {
		// TODO Auto-generated method stub

	}

	@Override
	public JSpinner getPeriodCountSpinner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSpinner getPeriodDurationSpinner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWorkingDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWorkingDirectory(String p0) {
		// TODO Auto-generated method stub

	}

	@Override
	public float getInterestRate(int p0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setInterestRate(int p0, float p1) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPeriodDuration() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPeriodDuration(int p0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeGeneralParameters() {
		// TODO Auto-generated method stub

	}

}
