﻿// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipsetrader.jessx.business.institutions;

import java.awt.Component;
import javax.swing.JTable;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.AbstractCellEditor;

import org.eclipsetrader.jessx.business.Order;
import org.eclipsetrader.jessx.business.operations.DeleteOrder;
import org.eclipsetrader.jessx.client.ClientCore;
import org.eclipsetrader.jessx.utils.Utils;

class JButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
{
    JOrderButton button;
    JLabel componentDisplayed;
    protected static final String EDIT = "edit";
    
    public JButtonEditor() {
        this.button = new JOrderButton((Order)null);
    }
    
    public void actionPerformed(final ActionEvent e) {
        Utils.logger.debug("Clic! Bouton delete");
        if ("edit".equals(e.getActionCommand())) {
            this.fireEditingStopped();
            try {
                final Order order = this.button.getOrder();
                final DeleteOrder deleteOrder = new DeleteOrder(order.getId());
                deleteOrder.setEmitter(ClientCore.getLogin());
                deleteOrder.setInstitutionName(order.getInstitutionName());
                ClientCore.send(deleteOrder);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public Object getCellEditorValue() {
        return this.button;
    }
    
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        this.button = (JOrderButton)value;
        if (this.button != null) {
            this.button.removeActionListener(this);
            this.button.setActionCommand("edit");
            this.button.addActionListener(this);
            this.button.setBorderPainted(false);
        }
        return this.button;
    }
}
