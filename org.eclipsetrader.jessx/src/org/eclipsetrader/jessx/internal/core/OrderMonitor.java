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

import java.beans.PropertyChangeSupport;
import java.io.IOException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderMonitorListener;
import org.eclipsetrader.core.trading.IOrderStatus;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.OrderMonitorEvent;
import org.eclipsetrader.jessx.internal.core.connector.StreamingConnector;
import org.jdom.Element;

public class OrderMonitor implements IOrderMonitor, IAdaptable {

    private IOrder order;

    private BrokerConnector brokerConnector;
    private String id;
    private String username;

    private Long filledQuantity;
    private Double averagePrice;
    private IOrderStatus status = IOrderStatus.New;
    private String message;

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    public OrderMonitor(BrokerConnector brokerConnector, IOrder order, String username) {
        this.brokerConnector = brokerConnector;
        this.order = order;
        this.username = username;
    }

    @Override
    public IBroker getBrokerConnector() {
        return brokerConnector;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String assignedId) {
        this.id = assignedId;
    }

    @Override
    public IOrder getOrder() {
        return order;
    }

    @Override
    public void submit() throws BrokerException {
        try {
            StreamingConnector.getInstance().send(getOrderElement());
        } catch (IOException e) {
            throw new BrokerException(e);
        }
    }

    private Element getOrderElement() {
        Element root = new Element("Operation");
        root.setAttribute("type", "Limit Order");

        root.setAttribute("emitter", username);

        root.setAttribute("institution", order.getSecurity().getIdentifier().getSymbol());

        Element orderElement = new Element("Order");
        orderElement.setAttribute("id", "0"); // Server will assign ID
        orderElement.setAttribute("side", String.valueOf(order.getSide().getId()));
        orderElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));

        Element limitOrderElement = new Element("LimitOrder");
        if (order.getType() == IOrderType.Limit) {
            limitOrderElement.setAttribute("price", String.valueOf(order.getPrice()));
        }
        limitOrderElement.setAttribute("quantity", String.valueOf(order.getQuantity()));

        orderElement.addContent(limitOrderElement);
        root.addContent(orderElement);

        return root;
    }

    @Override
    public void cancel() throws BrokerException {
        // TODO: Implement order cancellation
    }

    @Override
    public boolean allowModify() {
        return false;
    }

    @Override
    public void modify(IOrder order) throws BrokerException {
        throw new BrokerException(Messages.OrderMonitor_ModifyNotAllowed);
    }

    @Override
    public void addOrderMonitorListener(IOrderMonitorListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeOrderMonitorListener(IOrderMonitorListener listener) {
        listeners.remove(listener);
    }

    @Override
    public IOrderStatus getStatus() {
        return status;
    }

    public void setStatus(IOrderStatus status) {
        IOrderStatus oldValue = this.status;
        if (this.status != status) {
            this.status = status;
            propertyChangeSupport.firePropertyChange(PROP_STATUS, oldValue, this.status);
        }
    }

    @Override
    public Long getFilledQuantity() {
        return filledQuantity;
    }

    public void setFilledQuantity(Long filledQuantity) {
        Long oldValue = this.filledQuantity;
        if (filledQuantity != null && !filledQuantity.equals(this.filledQuantity)) {
            this.filledQuantity = filledQuantity;
            propertyChangeSupport.firePropertyChange(PROP_FILLED_QUANTITY, oldValue, this.filledQuantity);
        }
    }

    @Override
    public Double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(Double averagePrice) {
        Double oldValue = this.averagePrice;
        if (averagePrice != null && !averagePrice.equals(this.averagePrice)) {
            this.averagePrice = averagePrice;
            propertyChangeSupport.firePropertyChange(PROP_AVERAGE_PRICE, oldValue, this.averagePrice);
        }
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(propertyChangeSupport.getClass())) {
            return propertyChangeSupport;
        }
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }
        return null;
    }

    protected void fireOrderCompletedEvent() {
        OrderMonitorEvent event = new OrderMonitorEvent(this, order);

        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((IOrderMonitorListener) l[i]).orderCompleted(event);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OrderMonitor)) {
            return false;
        }
        return id != null && id.equals(((OrderMonitor) obj).id);
    }

    @Override
    public String toString() {
        return "OrderMonitor: id=" + getId() + ", status=" + getStatus() + ", filledQuantity=" + getFilledQuantity() + ", averagePrice=" + getAveragePrice() + " [" + order.toString() + "]";
    }
}
