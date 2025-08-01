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

package org.eclipsetrader.jessx.internal.core.messages;

public class AstaChiusura extends DataMessage {

    public double val_chiu;
    public long ora_chiu;

    public AstaChiusura(byte[] arr, int i, int decade) {
        val_chiu = Util.getFloat(arr, i);
        i += 4;
        ora_chiu = Util.getDataOra(arr, i, decade);
    }
}
