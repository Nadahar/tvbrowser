/*
 * SimpleMarkerPlugin by Ren� Mach
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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

/**
 * The renderer for the mark list name column.
 * 
 * @author Ren� Mach
 */
public class MarkerIDRenderer extends DefaultTableCellRenderer {
  private JPanel mPanel;
  private JLabel mLabel;

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(table, value,
        isSelected, hasFocus, row, column);

    if (mPanel == null) {
      mPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      mLabel = new JLabel();
      mPanel.add(mLabel);
    }

    Color color = c.getBackground();

    int count = ((MarkList)value).size();

    if(count > 0) {
      mLabel.setText(value.toString() + " [" + count + "]");
    } else {
      mLabel.setText(value.toString());
    }

    mPanel.setBackground(color);
    mPanel.setOpaque(true);
    mLabel.setForeground(c.getForeground());

    return mPanel;
  }
}
