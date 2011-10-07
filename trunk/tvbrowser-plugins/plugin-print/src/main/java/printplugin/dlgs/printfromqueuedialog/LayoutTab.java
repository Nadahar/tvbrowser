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
 *     $Date: 2009-01-13 18:34:41 +0100 (Di, 13 Jan 2009) $
 *   $Author: Bananeweizen $
 * $Revision: 5410 $
 */

package printplugin.dlgs.printfromqueuedialog;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class LayoutTab extends JPanel {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
       = util.ui.Localizer.getLocalizerFor(LayoutTab.class);

  private JComboBox mColumnsPerPageCB;

  public LayoutTab() {
    CellConstraints cc = new CellConstraints();
    
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref,2dlu,pref,0dlu:grow",
        "pref,5dlu,pref,10dlu"), this);
    pb.setDefaultDialogBorder();
    
    pb.addSeparator(mLocalizer.msg("columns","Columns"), cc.xyw(1,1,5));
    pb.addLabel(mLocalizer.msg("columnsPerPage","Columns per page:"), cc.xy(2,3));
    pb.add(mColumnsPerPageCB = new JComboBox(createIntegerArray(1,4)), cc.xy(4,3));
  }

  public int getColumnsPerPage() {
    return ((Integer) mColumnsPerPageCB.getSelectedItem()).intValue();
  }

  public void setColumnsPerPage(int columns) {
    mColumnsPerPageCB.setSelectedItem(columns);
  }

  private Integer[] createIntegerArray(int from, int cnt) {
    Integer[] result = new Integer[cnt];
    for (int i=0; i<result.length; i++) {
      result[i] = i + from;
    }
    return result;
  }
}