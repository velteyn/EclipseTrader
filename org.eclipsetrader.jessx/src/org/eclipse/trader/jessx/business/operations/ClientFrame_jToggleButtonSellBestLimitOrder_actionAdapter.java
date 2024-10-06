// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipse.trader.jessx.business.operations;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ClientFrame_jToggleButtonSellBestLimitOrder_actionAdapter implements ActionListener
{
    BestLimitOrderClientPanel adaptee;
    
    ClientFrame_jToggleButtonSellBestLimitOrder_actionAdapter(final BestLimitOrderClientPanel adaptee) {
        this.adaptee = adaptee;
    }
    
    public void actionPerformed(final ActionEvent e) {
        this.adaptee.jToggleButtonSellBestLimitOrder_actionPerformed(e);
    }
}
