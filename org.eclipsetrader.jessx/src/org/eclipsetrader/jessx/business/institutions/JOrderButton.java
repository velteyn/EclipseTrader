// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipsetrader.jessx.business.institutions;


import javax.swing.JButton;

import org.eclipsetrader.jessx.business.Order;

class JOrderButton extends JButton
{
    private Order order;
    
    public Order getOrder() {
        return this.order;
    }
    
    JOrderButton(final Order order) {
        this.order = order;
        this.setText("Delete");
    }
}
