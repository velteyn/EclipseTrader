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

public class IndexMessage extends DataMessage {

    public double val_ult;
    public long ora_ult;
    public double percent;
    public double max;
    public double min;

    public IndexMessage() {
        val_ult = 0.0F;
        ora_ult = 0L;
        percent = 0.0F;
        max = 0.0F;
        min = 0.0F;
    }
}
