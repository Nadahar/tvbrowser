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
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mär 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */
package captureplugin.drivers.elgatodriver.configdialog;

import captureplugin.drivers.elgatodriver.ElgatoConfig;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * The Editor for the Elgato-Channel Mapping
 *  
 * @author bodum
 */
public class ElgatoChannelEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
  private JComboBox mComboBox;
  private ElgatoConfig mConfig;
  
  public ElgatoChannelEditor(ElgatoConfig config) {
    mConfig = config;
  }
  
  public Object getCellEditorValue() {
    return mComboBox.getSelectedItem();
  }

  public boolean stopCellEditing() {
    if (mComboBox.isEditable()) {
      // Commit edited value.
      mComboBox.actionPerformed(new ActionEvent(this, 0, ""));
    }
    return super.stopCellEditing();
  }
  
  public void actionPerformed(ActionEvent e) {
    stopCellEditing();
  }

  public boolean isCellEditable(EventObject evt) {
    if (evt instanceof MouseEvent) {
      return ((MouseEvent)evt).getClickCount() >= 2;
    }
    return true;
  }
  
  
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    mComboBox = new JComboBox(mConfig.getAllElgatoChannels());
    mComboBox.setSelectedItem(value);
    mComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
    return mComboBox;
  }


}
