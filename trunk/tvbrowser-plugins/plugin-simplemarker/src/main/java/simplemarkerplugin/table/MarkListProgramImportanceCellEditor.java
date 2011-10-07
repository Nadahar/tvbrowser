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
 *     $Date: 2011-03-26 21:21:11 +0100 (Sa, 26 Mrz 2011) $
 *   $Author: bananeweizen $
 * $Revision: 6974 $
 */
package simplemarkerplugin.table;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import simplemarkerplugin.MarkList;
import simplemarkerplugin.SimpleMarkerPlugin;
import devplugin.Program;

/**
 * The cell editor for the program importance column
 * 
 * @author René Mach
 */
public class MarkListProgramImportanceCellEditor extends AbstractCellEditor implements
    TableCellEditor {

  private static final long serialVersionUID = 1L;

  final static String[] importanceValues = {
      SimpleMarkerPlugin.getLocalizer().msg("settings.importance.default","Default importance"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.importance.min","Mininum importance"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.importance.lowerMedium","Lower medium importance"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.importance.medium","Medium importance"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.importance.higherMedium","Higher medium importance"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.importance.max","Maximum importance")};

  private JComboBox mComboBox;
  private MarkList mItem;
  
  /**
   * Creates an instance of this class.
   */
  public MarkListProgramImportanceCellEditor() {
    mComboBox = new JComboBox(importanceValues);
  }

  public boolean isCellEditable(EventObject evt) {
    return !(evt instanceof MouseEvent) || ((MouseEvent) evt).getClickCount() >= 2;
  }

  public Object getCellEditorValue() {
    switch(mComboBox.getSelectedIndex()) {
      case 1: mItem.setProgramImportance(Program.MIN_PROGRAM_IMPORTANCE);break;
      case 2: mItem.setProgramImportance(Program.LOWER_MEDIUM_PROGRAM_IMPORTANCE);break;
      case 3: mItem.setProgramImportance(Program.MEDIUM_PROGRAM_IMPORTANCE);break;
      case 4: mItem.setProgramImportance(Program.HIGHER_MEDIUM_PROGRAM_IMPORTANCE);break;
      case 5: mItem.setProgramImportance(Program.MAX_PROGRAM_IMPORTANCE);break;
      
      default: mItem.setProgramImportance(Program.DEFAULT_PROGRAM_IMPORTANCE);
    }
    
    return mItem.getProgramImportance();
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
    boolean isSelected, int row, int column) {
    mItem = (MarkList)table.getValueAt(table.getSelectedRow(),0);
    
    switch(mItem.getProgramImportance()) {
      case Program.MIN_PROGRAM_IMPORTANCE: mComboBox.setSelectedIndex(1);break;
      case Program.LOWER_MEDIUM_PROGRAM_IMPORTANCE: mComboBox.setSelectedIndex(2);break;
      case Program.MEDIUM_PROGRAM_IMPORTANCE: mComboBox.setSelectedIndex(3);break;
      case Program.HIGHER_MEDIUM_PROGRAM_IMPORTANCE: mComboBox.setSelectedIndex(4);break;
      case Program.MAX_PROGRAM_IMPORTANCE: mComboBox.setSelectedIndex(5);break;
      
      default: mComboBox.setSelectedIndex(0);
    }
    
    return mComboBox;
  }
}
