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

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IPosition;

public class Position implements IPosition {

    private ISecurity security;
    private Long quantity;
    private Double price;

    public Position(ISecurity security, Long quantity, Double price) {
        this.security = security;
        this.quantity = quantity;
        this.price = price;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IPosition#getSecurity()
     */
    @Override
    public ISecurity getSecurity() {
        return security;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IPosition#getQuantity()
     */
    @Override
    public Long getQuantity() {
        return quantity;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IPosition#getPrice()
     */
    @Override
    public Double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Position other = (Position) obj;
        if (security == null) {
            if (other.security != null) {
                return false;
            }
        } else if (!security.equals(other.security)) {
            return false;
        }
        if (quantity == null) {
            if (other.quantity != null) {
                return false;
            }
        } else if (!quantity.equals(other.quantity)) {
            return false;
        }
        if (price == null) {
            if (other.price != null) {
                return false;
            }
        } else if (!price.equals(other.price)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((security == null) ? 0 : security.hashCode());
        result = prime * result + ((quantity == null) ? 0 : quantity.hashCode());
        result = prime * result + ((price == null) ? 0 : price.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Position [security=" + (security != null ? security.getName() : "null") + ", quantity=" + quantity + ", price=" + price + "]";
    }
}
