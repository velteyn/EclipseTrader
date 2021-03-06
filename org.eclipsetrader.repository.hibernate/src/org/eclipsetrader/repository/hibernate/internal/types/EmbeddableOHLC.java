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

package org.eclipsetrader.repository.hibernate.internal.types;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.eclipsetrader.core.feed.IOHLC;

@Embeddable
public class EmbeddableOHLC implements IOHLC {

    @Column(name = "date")
    private Date date;

    @Column(name = "open")
    private Double open;

    @Column(name = "high")
    private Double high;

    @Column(name = "low")
    private Double low;

    @Column(name = "close")
    private Double close;

    @Column(name = "volume")
    private Long volume;

    public EmbeddableOHLC() {
    }

    public EmbeddableOHLC(IOHLC ohlc) {
        date = ohlc.getDate();
        open = ohlc.getOpen();
        high = ohlc.getHigh();
        low = ohlc.getLow();
        close = ohlc.getClose();
        volume = ohlc.getVolume();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getDate()
     */
    @Override
    public Date getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getOpen()
     */
    @Override
    public Double getOpen() {
        return open;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getHigh()
     */
    @Override
    public Double getHigh() {
        return high;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getLow()
     */
    @Override
    public Double getLow() {
        return low;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getClose()
     */
    @Override
    public Double getClose() {
        return close;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getVolume()
     */
    @Override
    public Long getVolume() {
        return volume;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IOHLC)) {
            return false;
        }
        IOHLC other = (IOHLC) obj;
        return (getDate() == other.getDate() || getDate() != null && getDate().equals(other.getDate())) && (getOpen() == other.getOpen() || getOpen() != null && getOpen().equals(other.getOpen())) && (getHigh() == other.getHigh() || getHigh() != null && getHigh().equals(other.getHigh())) && (getLow() == other.getLow() || getLow() != null && getLow().equals(other.getLow())) && (getClose() == other.getClose() || getClose() != null && getClose().equals(other.getClose())) && (getVolume() == other.getVolume() || getVolume() != null && getVolume().equals(other.getVolume()));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 3 * (date != null ? date.hashCode() : 0) + 7 * (open != null ? open.hashCode() : 0) + 11 * (high != null ? high.hashCode() : 0) + 13 * (low != null ? low.hashCode() : 0) + 17 * (close != null ? close.hashCode() : 0) + 19 * (volume != null ? volume.hashCode() : 0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[" + date + " O=" + open + " H=" + high + " L=" + low + " C=" + close + " V=" + volume + "]";
    }
}
