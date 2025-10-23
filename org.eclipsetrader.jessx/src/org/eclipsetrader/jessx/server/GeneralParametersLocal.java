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

    private String loggingFileName;
    private String workingDirectory;
    private boolean afterSetupJoiningAllowed;
    private String setupFileName;
    private String xmlVersion;
    private int periodCount = 5;
    private int periodDuration;
    private float[] interestRates;

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
		return afterSetupJoiningAllowed;
	}

	@Override
	public void setAfterSetupJoiningAllowed(boolean p0) {
        this.afterSetupJoiningAllowed = p0;
	}

	@Override
	public String getSetupFileName() {
		return setupFileName;
	}

	@Override
	public void setSetupFileName(String p0) {
        this.setupFileName = p0;
	}

	@Override
	public String getXMLVersion() {
		return xmlVersion;
	}

	@Override
	public void setXMLVersion(String p0) {
        this.xmlVersion = p0;
	}

	@Override
	public String getLoggingFileName() {
		return loggingFileName;
	}

	@Override
	public void setLoggingFileName(String p0) {
		this.loggingFileName = p0;
	}

	@Override
	public int getPeriodCount() {
		return periodCount;
	}

	@Override
	public void setPeriodCount(int p0) {
        this.periodCount = p0;
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
		return workingDirectory;
	}

	@Override
	public void setWorkingDirectory(String p0) {
		this.workingDirectory = p0;
	}

	@Override
	public float getInterestRate(int p0) {
		if (interestRates == null || p0 < 0 || p0 >= interestRates.length) {
            return 0;
        }
        return interestRates[p0];
	}

	@Override
	public void setInterestRate(int p0, float p1) {
		if (interestRates == null) {
            interestRates = new float[getPeriodCount()];
        }
        if (p0 >= 0 && p0 < interestRates.length) {
            interestRates[p0] = p1;
        }
	}

	@Override
	public int getPeriodDuration() {
		return periodDuration;
	}

	@Override
	public void setPeriodDuration(int p0) {
        this.periodDuration = p0;
	}

	@Override
	public void initializeGeneralParameters() {
        this.workingDirectory = System.getProperty("java.io.tmpdir");
        this.loggingFileName = "jessx-experiment.log";
        this.xmlVersion = "1.0";
        this.setupFileName = "default.xml";
        this.afterSetupJoiningAllowed = false;
        this.periodCount = 5;
        this.periodDuration = 300;
        this.interestRates = new float[this.periodCount];
        for (int i = 0; i < this.periodCount; i++) {
            this.interestRates[i] = 0.05f;
        }
	}
}
