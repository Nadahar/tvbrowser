/*
 * Copyright Michael Keppler
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
 */
package mediathekplugin;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ItemListCellRenderer extends JLabel implements ListCellRenderer {

  public Component getListCellRendererComponent(final JList list,
      final Object value, final int index, final boolean isSelected,
      final boolean cellHasFocus) {
    if (value == null) {
      return new JLabel("");
    }

    if (value instanceof MediathekProgram) {
      final MediathekProgram program = (MediathekProgram) value;
      this.setText(program.getTitle());
      return this;
    }

    return new JLabel(value.toString());
  }

}
