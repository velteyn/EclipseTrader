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

package org.eclipsetrader.jessx.internal.core;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.ListenerList;
import org.eclipsetrader.core.Cash;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.trading.IPositionListener;
import org.eclipsetrader.core.trading.ITransaction;
import org.eclipsetrader.core.trading.PositionEvent;

public class Account implements IAccount {

    private String id;
    private String description;
    private Cash balance;
    private List<ITransaction> transactions = new ArrayList<ITransaction>();
    private List<IPosition> positions = new ArrayList<IPosition>();
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    public Account(String id, String description, Cash balance) {
        this.id = id;
        this.description = description;
        this.balance = balance;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getBalance()
     */
    @Override
    public Cash getBalance() {
        return balance;
    }

    public void setBalance(Cash balance) {
        this.balance = balance;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getTransactions()
     */
    @Override
    public ITransaction[] getTransactions() {
        return transactions.toArray(new ITransaction[transactions.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getPositions()
     */
    @Override
    public IPosition[] getPositions() {
        return positions.toArray(new IPosition[positions.size()]);
    }

    public void setPositions(IPosition[] newPositions) {
        List<IPosition> added = new ArrayList<IPosition>();
        List<IPosition> removed = new ArrayList<IPosition>();
        List<IPosition> currentPositions = new ArrayList<IPosition>(this.positions);

        for (IPosition position : newPositions) {
            if (!currentPositions.contains(position)) {
                added.add(position);
            }
        }

        for (IPosition position : currentPositions) {
            boolean found = false;
            for (IPosition newPosition : newPositions) {
                if (newPosition.equals(position)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                removed.add(position);
            }
        }

        this.positions = new ArrayList<IPosition>();
        for (IPosition position : newPositions) {
            this.positions.add(position);
        }

        for (IPosition position : added) {
            firePositionOpened(new PositionEvent(this, position));
        }
        for (IPosition position : removed) {
            firePositionClosed(new PositionEvent(this, position));
        }
    }


    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#addPositionListener(org.eclipsetrader.core.trading.IPositionListener)
     */
    @Override
    public void addPositionListener(IPositionListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#removePositionListener(org.eclipsetrader.core.trading.IPositionListener)
     */
    @Override
    public void removePositionListener(IPositionListener listener) {
        listeners.remove(listener);
    }

    protected void firePositionOpened(PositionEvent e) {
        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            ((IPositionListener) l[i]).positionOpened(e);
        }
    }

    protected void firePositionClosed(PositionEvent e) {
        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            ((IPositionListener) l[i]).positionClosed(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IAccount) {
            String otherId = ((IAccount) obj).getId();
            return id != null && id.equals(otherId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
