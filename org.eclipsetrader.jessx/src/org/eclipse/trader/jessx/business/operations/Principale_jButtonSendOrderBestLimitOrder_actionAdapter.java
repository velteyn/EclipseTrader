// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipse.trader.jessx.business.operations;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class Principale_jButtonSendOrderBestLimitOrder_actionAdapter implements ActionListener
{
    BestLimitOrderClientPanel adaptee;
    
    Principale_jButtonSendOrderBestLimitOrder_actionAdapter(final BestLimitOrderClientPanel adaptee) {
        this.adaptee = adaptee;
    }
    
    public void actionPerformed(final ActionEvent e) {
        this.adaptee.jButtonSendOrderBestLimitOrder_actionPerformed(e);
    }
}
