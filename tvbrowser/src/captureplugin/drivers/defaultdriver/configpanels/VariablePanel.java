/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package captureplugin.drivers.defaultdriver.configpanels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.TableCellEditor;

import util.ui.Localizer;
import captureplugin.drivers.defaultdriver.DeviceConfig;

/**
 * The Panel for configuration of the Variables
 * 
 * @author bodum
 */
public class VariablePanel extends JPanel {

  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(VariablePanel.class);

  /** Table */
  private JTable mVariableTable = new JTable();

  /** Settings */
  private DeviceConfig mData;

  /**
   * Creates the Panel
   * @param data Data to use
   */
  public VariablePanel(DeviceConfig data) {
      mData = data;
      createPanel();
  }
  
  /**
   * creates a JPanel for managing the variables
   */
  private void createPanel() {
      setLayout(new GridBagLayout());
      setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("Variables", "Variables")));

      VariableTableModel variableTableModel = new VariableTableModel(mData);
      mVariableTable.setModel(variableTableModel);
      mVariableTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      mVariableTable.getTableHeader().setReorderingAllowed(false);
      mVariableTable.getColumnModel().getColumn(0).setPreferredWidth(40);
      
      JScrollPane sp = new JScrollPane(mVariableTable);

      GridBagConstraints c = new GridBagConstraints();

      c.fill = GridBagConstraints.BOTH;
      c.weightx = 1;
      c.weighty = 0.8;

      addAncestorListener(new AncestorListener() {

        public void ancestorAdded(AncestorEvent event) {
          // TODO Auto-generated method stub
          
        }

        public void ancestorMoved(AncestorEvent event) {
          // TODO Auto-generated method stub
          
        }

        public void ancestorRemoved(AncestorEvent event) {
          if (mVariableTable.isEditing()) {
            TableCellEditor editor = mVariableTable.getCellEditor();
            if (editor != null)
              editor.stopCellEditing();
          }
        }
        
      });
      
      add(sp, c);
  }

}