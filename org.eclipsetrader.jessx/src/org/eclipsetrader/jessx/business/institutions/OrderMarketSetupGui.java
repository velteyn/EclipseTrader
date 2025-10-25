package org.eclipsetrader.jessx.business.institutions;

/***************************************************************/
/*                     SOFTWARE SECTION                        */
/***************************************************************/
/*
 * <p>Name: Jessx</p>
 * <p>Description: Financial Market Simulation Software</p>
 * <p>Licence: GNU General Public License</p>
 * <p>Organisation: EC Lille / USTL</p>
 * <p>Persons involved in the project : group T.E.A.M.</p>
 * <p>More details about this source code at :
 *    http://eleves.ec-lille.fr/~ecoxp03  </p>
 * <p>Current version: 1.0</p>
 */

/***************************************************************/
/*                      LICENCE SECTION                        */
/***************************************************************/
/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/***************************************************************/
/*                       IMPORT SECTION                        */
/***************************************************************/
 

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.eclipsetrader.jessx.business.BusinessCore;
import org.eclipsetrader.jessx.business.Institution;
import org.eclipsetrader.jessx.business.Operator;
import org.eclipsetrader.jessx.business.event.InstitutionEvent;
import org.eclipsetrader.jessx.business.event.InstitutionListener;
import org.eclipsetrader.jessx.utils.JessXTableModel;
import org.eclipsetrader.jessx.utils.PopupListener;
 

/***************************************************************/
/*            OrderMarketSetupGui CLASS SECTION                */
/***************************************************************/
/**
 * <p>Title : OrderMarketSetupGui</p>
 * <p>Description : </p>
 * @author Thierry Curtil
 * @version 1.0
 */
//TODO EDOZ  convert this to a eclipse config panel

public class OrderMarketSetupGui extends JPanel implements ActionListener,InstitutionListener {

  private OrderMarket orderMarket;

  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JPanel jPanel1 = new JPanel();
  JPanel jPanel2 = new JPanel();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JLabel jLabel2 = new JLabel();
  JScrollPane jScrollPane1 = new JScrollPane();
  JTable jTable1;
  JScrollPane jScrollPane2 = new JScrollPane();
  GridBagLayout gridBagLayout3 = new GridBagLayout();
  JTable jTable2 = new JTable();
  JPopupMenu popup;
  MouseListener popupListener;
  TableModelOperationsCost tableModelOperationsCost;
  OrderMarketOperatorsTableModel orderMarketOperatorsTableModel;
  JMenuItem menuItemAdd;
  JMenuItem menuItemDelete;
  JCheckBox keepingOrderBook = new JCheckBox("Check here if you want to keep the order book between two periods");

  public OrderMarketSetupGui(OrderMarket anOrderMarket)  {

    super();
    this.orderMarket = anOrderMarket;
    this.initOperationsTables();
    keepingOrderBook.setSelected(orderMarket.getKeepingOrderBook());
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    BusinessCore.addInstitutionListener(this);
  }

  public JPanel getPanel() {
    return this;
  }

  public void desactive(){
    tableModelOperationsCost.setUneditable();
    orderMarketOperatorsTableModel.setUneditable();
    menuItemAdd.setEnabled(false);
    menuItemDelete.setEnabled(false);
    keepingOrderBook.setEnabled(false);
  }

  public void active(){
    tableModelOperationsCost.setEditable();
    orderMarketOperatorsTableModel.setEditable();
    menuItemAdd.setEnabled(true);
    menuItemDelete.setEnabled(true);
    keepingOrderBook.setEnabled(true);
  }

  private void jbInit() throws Exception {
    this.setLayout(gridBagLayout1);
    jPanel1.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"General Parameters"));
    jPanel1.setLayout(gridBagLayout2);
    jPanel2.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Add operators"));
    jPanel2.setLayout(gridBagLayout3);
    jLabel2.setText("Cost of the operations:");
    keepingOrderBook.addActionListener(new
        OrderMarketSetupGui_KeepingOrderBook_actionAdapter(this));

    popup = new JPopupMenu();

    menuItemAdd = new JMenuItem("Add an operator");
    menuItemAdd.setActionCommand("addOperator");
    menuItemAdd.addActionListener(this);
    jTable1.setToolTipText("Specify here the cost of each operation");
    jScrollPane1.setToolTipText("Specify here the cost of each operation");
    jScrollPane2.setToolTipText("Right-click here to add or remove an operator");
    jTable2.setToolTipText("Right-click here to add or remove an operator");
    popup.add(menuItemAdd);

    menuItemDelete = new JMenuItem("Delete an operator");
    menuItemDelete.setActionCommand("deleteOperator");
    menuItemDelete.addActionListener(this);
    popup.add(menuItemDelete);


    //Add listener to components that can bring up popup menus.
    popupListener = new PopupListener(popup);
    jScrollPane2.addMouseListener(popupListener);
    jTable2.addMouseListener(popupListener);


    jTable1.setRowSelectionAllowed(false);
    jTable2.setRowSelectionAllowed(false);

    jScrollPane1.setMinimumSize(new Dimension(23, 64));
    jPanel1.add(this.keepingOrderBook, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                            , GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(10, 6, 3, 6), 0, 0));
    jPanel1.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                                , GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(10, 6, 3, 6), 0, 0));
    jPanel1.add(jScrollPane1,        new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 6, 6, 6), 0, 0));
    jScrollPane1.getViewport().add(jTable1, null);
    jPanel2.add(jScrollPane2,   new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    jScrollPane2.getViewport().add(jTable2, null);
    this.add(jPanel2, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.7
                                             , GridBagConstraints.CENTER,
                                             GridBagConstraints.BOTH,
                                             new Insets(3, 6, 3, 6), 0, 0));
    this.add(jPanel1, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.3
                                             , GridBagConstraints.NORTH,
                                             GridBagConstraints.BOTH,
                                             new Insets(6, 6, 3, 6), 0, 0));
  }



// ======================================= //
// ------ listeners  implementation ------ //
// ======================================= //

  public void institutionsModified(InstitutionEvent e) {
    if (e.getEvent() == InstitutionEvent.INSTITUTION_ADDED) {

    }
    else if (e.getEvent() == InstitutionEvent.INSTITUTION_REMOVED) {
      if (e.getInstitutionName().equalsIgnoreCase(orderMarket.getName())) {
        orderMarket.removeAllOperators();
      }
    }
  }

// ======================================= //
// --- End of listeners implementation --- //
// ======================================= //

  private void initOperationsTables() {
    tableModelOperationsCost=new TableModelOperationsCost(this.orderMarket);
    this.jTable1 = new JTable(tableModelOperationsCost);
    orderMarketOperatorsTableModel=new OrderMarketOperatorsTableModel(orderMarket);
    this.jTable2 = new JTable(orderMarketOperatorsTableModel);
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() instanceof JMenuItem) {
      if (((JMenuItem)e.getSource()).getActionCommand().equalsIgnoreCase("addOperator")) {
        JPanel jPanel = new JPanel(new GridLayout(3,2));
        JTextField jTextField = new JTextField("");
        JLabel jLabelOperatorName = new JLabel("Enter a new operator title here :");
        jPanel.add(jLabelOperatorName);
        jPanel.add(jTextField);
        int answer;
        boolean wrongName;
        do {
          wrongName = false;
          answer = JOptionPane.showConfirmDialog(this, jPanel, "New Operator",
                                                 JOptionPane.OK_CANCEL_OPTION,
                                                 JOptionPane.
                                                 INFORMATION_MESSAGE);
      if (answer == JOptionPane.OK_OPTION) {
        wrongName = (jTextField.getText().equals("")) ||
            this.orderMarket.getOperators().containsKey(
                jTextField.getText());
        if (wrongName) {
          String mess = new String("The name '" +
                                   jTextField.getText() +
                                   "' is already used. Please choose an other name.");
          if (!jTextField.getText().equals("")) {
            JOptionPane.showMessageDialog(this, mess, "Name conflict",
                                          JOptionPane.WARNING_MESSAGE);
          }
        }
      }
    }
    while (answer == JOptionPane.OK_OPTION && wrongName);
    if ( (answer == JOptionPane.OK_OPTION)) {
        this.orderMarket.addOperator(new Operator(this.orderMarket.getName() ,jTextField.getText(),new Vector(), 5));
    }




      }
      else if (((JMenuItem)e.getSource()).getActionCommand().equalsIgnoreCase("deleteOperator")) {
        int rowToBeRemoved = this.askUserOperatorToBeDeleted();
        if (rowToBeRemoved != -1) {
          this.orderMarket.removeOperator(this.jTable2.getValueAt(rowToBeRemoved,0).toString());
        }
      }
    }
  }

  public String toString() {
    return this.orderMarket.getName();
  }
/*
  public void updateInstitutionModel() {
    this.orderMarket.removeAllOperators();

    // stop editing cell in case the user forgot to get out of one.
    if (this.jTable1.isEditing()) {
      this.jTable1.editingStopped(null);
    }

    if (this.jTable2.isEditing()) {
      this.jTable2.editingStopped(null);
    }


    // init operators with table value.
    for(int i = 0; i < jTable2.getRowCount(); i++) {

      Vector grantedOps = new Vector();
      for(int j = 2; j < jTable2.getColumnCount(); j++) {
        grantedOps.add(jTable2.getValueAt(i,j));
      }
      this.orderMarket.addOperator(new Operator(jTable2.getValueAt(i,0).toString(),grantedOps,((Integer)jTable2.getValueAt(i,1)).intValue()));
    }

    // saves cost to tables
    for( int i = 0; i < jTable1.getRowCount(); i++) {
      this.orderMarket.setCost((String)jTable1.getValueAt(i,0),(Float)jTable1.getValueAt(i,1));
    }
  }*/

  public void stopEditingInsitutionPanel() {

    for(int i=0; i < jTable1.getColumnCount(); i++) {
      ((JessXTableModel)jTable1.getModel()).setEditable(i,false);
    }

    for(int i=0; i < jTable2.getColumnCount(); i++) {
      ((JessXTableModel)jTable2.getModel()).setEditable(i,false);
    }

    // saving gui values to model
    //this.updateInstitutionModel();

    // removing gui listeners.
    this.jScrollPane2.removeMouseListener(popupListener);
    this.jTable2.removeMouseListener(popupListener);
  }

/*
  private Object[] initNewOperatorRow(String operatorName) {
    Object[] row = new Object[this.jTable2.getColumnCount()];
    row[0] = operatorName;
    row[1] = new Integer(5);
    for(int i = 2; i < row.length; i++) {
      row[i] = new Boolean(false);
    }
    return row;
  }*/

  private int askUserOperatorToBeDeleted() {

    if (jTable2.getRowCount() == 0) {
      JOptionPane.showMessageDialog(null,"There is no operator to delete.","No operator.",JOptionPane.INFORMATION_MESSAGE);
      return -1;
    }

    JPanel dialogPane = new JPanel(new GridLayout(2,1));
    JLabel question = new JLabel("Which operator do you want to delete ?");
    JComboBox jComboBox = new JComboBox();
    for(int i = 0; i < this.jTable2.getRowCount(); i++) {
      jComboBox.addItem(this.jTable2.getValueAt(i,0));
    }

    dialogPane.add(question);
    dialogPane.add(jComboBox);

    int chosenOption = JOptionPane.showConfirmDialog(null,dialogPane,"Delete an operator",JOptionPane.OK_CANCEL_OPTION);

    if (chosenOption == JOptionPane.OK_OPTION) {
      return jComboBox.getSelectedIndex();
    }
    else {
      return -1;
    }
  }

  public void KeepingOrderBook_actionPerformed(ActionEvent e) {
    ((Institution) orderMarket).setKeepingOrderBook(this.keepingOrderBook.isSelected());
  }

}

/***************************************************************/
/*                   EVENT CLASSES SECTION                     */
/***************************************************************/
class OrderMarketSetupGui_KeepingOrderBook_actionAdapter
    implements ActionListener {
  private OrderMarketSetupGui adaptee;
  OrderMarketSetupGui_KeepingOrderBook_actionAdapter(OrderMarketSetupGui
      adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.KeepingOrderBook_actionPerformed(e);
  }
}
