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

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import simplemarkerplugin.MarkList;

/**
 * Renderer class for the selection of show deleted state of a mark list.
 * <p>
 * @author René Mach
 */
public class DeleteShowSelectionRenderer extends DefaultTableCellRenderer {
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {
    final JPanel background = new JPanel(new FormLayout(
        "0dlu:grow,default,0dlu:grow", "0dlu:grow,default,0dlu:grow"));
    background.setOpaque(true);
    
    if(!isSelected) {
      background.setBackground(table.getBackground());
    }
    else {
      background.setBackground(table.getSelectionBackground());
    }
    
    final JCheckBox checkBox = new JCheckBox();
    checkBox.setSelected(((MarkList)value).isShowingDeletedPrograms());
    checkBox.setOpaque(false);
    checkBox.setContentAreaFilled(false); 
    
    background.add(checkBox, new CellConstraints().xy(2,2));
    
    return background;
  }
}
