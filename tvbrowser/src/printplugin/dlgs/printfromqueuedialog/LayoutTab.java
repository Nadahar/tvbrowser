/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package printplugin.dlgs.printfromqueuedialog;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LayoutTab extends JPanel {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
       = util.ui.Localizer.getLocalizerFor(LayoutTab.class);

  private JComboBox mColumnsPerPageCB;

  public LayoutTab() {

    super();
    setLayout(new BorderLayout());

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    JPanel columnPanel = new JPanel(new BorderLayout());
    columnPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("columns","Columns")));
    JPanel pn1 = new JPanel();
    pn1.setLayout(new BoxLayout(pn1, BoxLayout.X_AXIS));

    JLabel columnLabel = new JLabel(mLocalizer.msg("columnsPerPage","Columns per page:"));
    mColumnsPerPageCB = new JComboBox(createIntegerArray(1,4));

    pn1.add(columnLabel);
    pn1.add(mColumnsPerPageCB);
    columnPanel.add(pn1, BorderLayout.WEST);

    content.add(columnPanel);

    add(content, BorderLayout.NORTH);
  }

  public int getColumnsPerPage() {
    return ((Integer) mColumnsPerPageCB.getSelectedItem()).intValue();
  }

  public void setColumnsPerPage(int columns) {
    mColumnsPerPageCB.setSelectedItem(new Integer(columns));
  }

  private Integer[] createIntegerArray(int from, int cnt) {
    Integer[] result = new Integer[cnt];
    for (int i=0; i<result.length; i++) {
      result[i] = new Integer(i+from);
    }
    return result;
  }
}