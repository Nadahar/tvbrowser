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

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import simplemarkerplugin.MarkList;
import devplugin.Program;

/**
 * The cell renderer for the importance column
 * 
 * @author René Mach
 */
public class MarkerProgramImportanceRenderer extends DefaultTableCellRenderer {

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(table, value,
        isSelected, hasFocus, row, column);
    ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
    
    switch(((MarkList)value).getProgramImportance()) {
      case Program.MIN_PROGRAM_IMPORTANCE: ((JLabel)c).setText(MarkListProgramImportanceCellEditor.importanceValues[1]);break;
      case Program.LOWER_MEDIUM_PROGRAM_IMPORTANCE: ((JLabel)c).setText(MarkListProgramImportanceCellEditor.importanceValues[2]);break;
      case Program.MEDIUM_PROGRAM_IMPORTANCE: ((JLabel)c).setText(MarkListProgramImportanceCellEditor.importanceValues[3]);break;
      case Program.HIGHER_MEDIUM_PROGRAM_IMPORTANCE: ((JLabel)c).setText(MarkListProgramImportanceCellEditor.importanceValues[4]);break;
      case Program.MAX_PROGRAM_IMPORTANCE: ((JLabel)c).setText(MarkListProgramImportanceCellEditor.importanceValues[5]);break;
    
      default: ((JLabel)c).setText(MarkListProgramImportanceCellEditor.importanceValues[0]);
    }
    
    return c;
  }
}
