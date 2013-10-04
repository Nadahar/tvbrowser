/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.ui.customizableitems;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * An interface that supports to create an own center component as Renderer for the SelectableItemList.
 * <p>
 * @author René Mach
 * @since 2.7
 */
public interface SelectableItemRendererCenterComponentIf {
  /**
   * Creates the center component panel.
   * <p>
   * @param list The list with items.
   * @param value The currently rendered item.
   * @param index The index of the rendered item in the list.
   * @param isSelected <code>True</code> if the item is selected.
   * @param isEnabled <code>True</code> if the item is enabled.
   * @param parentScrollPane The scroll pane in that the list is embedded.
   * @param leftColumnWidth The width of the check box area until the text starts.
   * @return The JPanel with the rendered item.
   */
  public JPanel createCenterPanel(JList list, Object value, int index, boolean isSelected, boolean isEnabled, JScrollPane parentScrollPane, int leftColumnWidth);

  /**
   * Calculates the size of the content pane used to paint the whole item.
   * <p>
   * @param list The list with the items.
   * @param index The index if the currently rendered item.
   * @param contentPane The content pane with all components used to paint the item in the list.
   */
  public void calculateSize(JList list, int index, JPanel contentPane);
}
