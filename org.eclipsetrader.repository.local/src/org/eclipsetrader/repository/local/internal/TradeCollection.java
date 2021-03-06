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

package org.eclipsetrader.repository.local.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipsetrader.repository.local.internal.stores.TradeStore;

@XmlRootElement(name = "list")
public class TradeCollection {

    private static TradeCollection instance;

    @XmlAttribute(name = "next_id")
    private Integer nextId = new Integer(1);

    @XmlElementRef
    private List<TradeStore> list;

    private Map<URI, TradeStore> uriMap;

    public TradeCollection() {
        instance = this;
        list = new ArrayList<TradeStore>();
    }

    public static TradeCollection getInstance() {
        return instance;
    }

    public TradeStore get(URI uri) {
        synchronized (this) {
            if (uriMap == null) {
                uriMap = new HashMap<URI, TradeStore>();
                for (TradeStore securityStore : list) {
                    uriMap.put(securityStore.toURI(), securityStore);
                }
            }
        }
        return uriMap.get(uri);
    }

    public TradeStore create() {
        TradeStore securityStore = new TradeStore(nextId);
        list.add(securityStore);
        if (uriMap != null) {
            uriMap.put(securityStore.toURI(), securityStore);
        }
        nextId = new Integer(nextId + 1);
        return securityStore;
    }

    public void delete(TradeStore tradeStore) {
        for (Iterator<TradeStore> iter = list.iterator(); iter.hasNext();) {
            if (iter.next() == tradeStore) {
                iter.remove();
                if (uriMap != null) {
                    uriMap.remove(tradeStore.toURI());
                }
                break;
            }
        }
    }

    public TradeStore[] getAll() {
        return list.toArray(new TradeStore[list.size()]);
    }

    public List<TradeStore> getList() {
        return list;
    }

    public TradeStore[] toArray() {
        return list.toArray(new TradeStore[list.size()]);
    }
}
