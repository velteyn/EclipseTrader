// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipse.trader.jessx.business.institutions;


import javax.swing.JButton;

import org.eclipse.trader.jessx.business.Order;

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
