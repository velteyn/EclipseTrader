// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipse.trader.jessx.business;

import javax.swing.JPanel;

public interface ClientInputPanel
{
    JPanel getPanel();
    
    void stopEdition();
    
    void startEdition();
}
