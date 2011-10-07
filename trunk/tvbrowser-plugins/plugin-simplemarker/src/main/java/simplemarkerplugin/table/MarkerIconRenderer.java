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
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import simplemarkerplugin.MarkList;

/**
 * The renderer for the mark icon column
 * 
 * @author René Mach
 */
public class MarkerIconRenderer extends DefaultTableCellRenderer {
  private JPanel mPanel;
  private JLabel mLabel;

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(table, value,
        isSelected, hasFocus, row, column);

    if (mPanel == null) {
      mPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      mLabel = new JLabel();
      mPanel.add(mLabel);
    }

    mLabel.setIcon(((MarkList)value).getMarkIcon());
    mPanel.setOpaque(true);
    mPanel.setBackground(c.getBackground());

    return mPanel;
  }
}