/*
 * TV-Pearl by Reinhard Lehrbaum
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
 */
package tvpearlplugin;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;

public class TVPearlListCellRenderer extends DefaultListCellRenderer {
  private static final long serialVersionUID = 1L;

  private static Color SECOND_ROW_COLOR = null;
  
  public TVPearlListCellRenderer() {
    if(SECOND_ROW_COLOR == null) {
      Color background = UIManager.getDefaults().getColor("List.background");
      
      double test = (0.2126 * background.getRed()) + (0.7152 * background.getGreen()) + (0.0722 * background.getBlue());
      
      if(test < 20) {
        background = background.brighter().brighter().brighter().brighter().brighter().brighter().brighter().brighter().brighter();
      }
      else if(test < 40) {
        background = background.brighter().brighter().brighter();
      }
      else if(test < 80) {
        background = background.brighter().brighter();
      }
      else if(test < 180) {
        background = background.brighter();
      }
      else {
        background = background.darker();
      }
      
      SECOND_ROW_COLOR = background;
    }
  }

  public Component getListCellRendererComponent(final JList list, final Object value, final int index,
      final boolean isSelected, final boolean cellHasFocus) {
    final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    if (value instanceof TVPProgram) {
      final TVPProgram program = (TVPProgram) value;

      final TVPearlProgramPanel panel = new TVPearlProgramPanel(program);
      
      panel.setOpaque(isSelected || index % 2 != 0);
      
      if(isSelected) {
        panel.setBackground(UIManager.getColor("List.selectionBackground"));
        panel.setForeground(UIManager.getColor("List.selectionForeground"));
      }
      else {
        panel.setBackground(list.getBackground());
        panel.setForeground(list.getForeground());
      }
      
      if ((index % 2 != 0) && (!isSelected)) {
        panel.setBackground(SECOND_ROW_COLOR);
      }

      return panel;
    }

    return label;
  }
}
