/*
 * SimpleMarkerPlugin by René Mach
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package simplemarkerplugin.table;

import simplemarkerplugin.MarkList;
import simplemarkerplugin.SimpleMarkerPlugin;
import util.ui.MarkPriorityComboBoxRenderer;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * The cell editor for the priority column
 * 
 * @author René Mach
 */
public class MarkListPriorityCellEditor extends AbstractCellEditor implements
    TableCellEditor {

  private static final long serialVersionUID = 1L;

  private final String[] prioValues = {
      SimpleMarkerPlugin.getLocalizer().msg("settings.noPriority","None"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.min","Minimum"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.lowerMedium","Lower Medium"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.medium","Medium"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.higherMedium","Higher Medium"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.max","Maximum")};

  private JComboBox mComboBox;
  private MarkList mItem;

  /**
   * Creates an instance of this class.
   */
  public MarkListPriorityCellEditor() {
    mComboBox = new JComboBox(prioValues);
    mComboBox.setRenderer(new MarkPriorityComboBoxRenderer());
  }

  public boolean isCellEditable(EventObject evt) {
    return !(evt instanceof MouseEvent) || ((MouseEvent) evt).getClickCount() >= 2;
  }

  public Object getCellEditorValue() {
    mItem.setMarkPriority(mComboBox.getSelectedIndex()-1);
    return mComboBox.getSelectedIndex() - 1;
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
    boolean isSelected, int row, int column) {
    mItem = (MarkList)table.getValueAt(table.getSelectedRow(),0);
    mComboBox.setSelectedIndex(mItem.getMarkPriority()+1);
    return mComboBox;
  }
}
