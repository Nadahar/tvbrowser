/*
 * TV-Pearl improvement by René Mach
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvpearlplugin;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.ProgramPanel;

import devplugin.Program;

public class PearlCreationTableCellRenderer extends DefaultTableCellRenderer {
  private static final ProgramPanelSettings PANEL_SETTINGS = new ProgramPanelSettings(new PluginPictureSettings(PluginPictureSettings.NO_PICTURE_TYPE), true, true);
  
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {
    if(value instanceof Program) {
      ProgramPanel panel = new ProgramPanel((Program)value,PANEL_SETTINGS);
      
      table.setRowHeight(row, panel.getPreferredHeight());
      
      return panel;
    }
    
    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
  }
}
