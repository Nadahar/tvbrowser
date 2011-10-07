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
 *     $Date: 2009-09-04 11:15:55 +0200 (Fr, 04 Sep 2009) $
 *   $Author: bananeweizen $
 * $Revision: 5953 $
 */
package captureplugin.drivers.defaultdriver.configpanels;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import util.ui.Localizer;
import captureplugin.drivers.defaultdriver.DeviceConfig;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref:grow,5dlu","pref,5dlu,fill:default:grow"),this);
    pb.setDefaultDialogBorder();
    
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JPanel background = new JPanel(new FormLayout("default:grow","fill:default:grow"));
        JLabel label = new JLabel(value.toString());
        label.setOpaque(false);
        
        if(isSelected) {
          background.setBackground(table.getSelectionBackground());
          label.setForeground(table.getSelectionForeground());
        }
        else {
          background.setBackground(table.getBackground());
        }
        
        background.add(label, new CellConstraints().xy(1,1));
        
        return background;
      }
    };
    
    VariableTableModel variableTableModel = new VariableTableModel(mData);
    mVariableTable.setModel(variableTableModel);
    mVariableTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mVariableTable.getTableHeader().setReorderingAllowed(false);
    mVariableTable.getColumnModel().getColumn(0).setPreferredWidth(40);
    mVariableTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
    mVariableTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
    mVariableTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
    
    JScrollPane sp = new JScrollPane(mVariableTable);

    addAncestorListener(new AncestorListener() {
      public void ancestorAdded(AncestorEvent event) {}
      
      public void ancestorMoved(AncestorEvent event) {}

      public void ancestorRemoved(AncestorEvent event) {
        if (mVariableTable.isEditing()) {
          TableCellEditor editor = mVariableTable.getCellEditor();
          if (editor != null) {
            editor.stopCellEditing();
          }
        }
      }
    });
    
    pb.addSeparator(mLocalizer.msg("Variables", "Variables"), cc.xyw(1,1,3));
    pb.add(sp, cc.xy(2,3));
  }

}