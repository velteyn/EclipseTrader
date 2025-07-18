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

public class ErrorMessage {

    public short nTipeError;
    public String sMessageError;

    public ErrorMessage(byte packet[]) {
        nTipeError = 0;
        sMessageError = ""; //$NON-NLS-1$
        if (packet == null || packet.length < 1) {
            return;
        }
        nTipeError = Util.getByte(packet[0]);
        if (packet.length > 1) {
            sMessageError = new String(packet, 1, packet.length - 1);
        }
    }
}
