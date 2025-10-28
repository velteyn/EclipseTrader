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

package org.eclipsetrader.jessx.business;

public class NewsItem {

    private String priority;
    private String asset;
    private String text;

    public NewsItem(String priority, String asset, String text) {
        this.priority = priority;
        this.asset = asset;
        this.text = text;
    }

    public String getPriority() {
        return priority;
    }

    public String getAsset() {
        return asset;
    }

    public String getText() {
        return text;
    }
}
