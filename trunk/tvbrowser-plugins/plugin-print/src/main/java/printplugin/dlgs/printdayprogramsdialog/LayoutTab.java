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
 *     $Date: 2010-06-28 19:33:48 +0200 (Mo, 28 Jun 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6662 $
 */

package printplugin.dlgs.printdayprogramsdialog;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class LayoutTab extends JPanel {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(LayoutTab.class);


  private JComboBox mChannelsPerPageCB;
  private JComboBox mLayoutCB;
  private DefaultComboBoxModel mLayoutCBModel;

  public LayoutTab() {
    CellConstraints cc = new CellConstraints();
    mLayoutCBModel = new DefaultComboBoxModel();
    
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref,10dlu,pref:grow",
        "pref,5dlu,pref,2dlu,pref,10dlu"), this);
    pb.setDefaultDialogBorder();
    
    pb.addSeparator(mLocalizer.msg("channelsAndColumns","Channels and columns"), cc.xyw(1,1,4));
    pb.addLabel(mLocalizer.msg("channelsPerPage","Channels per page")+":", cc.xy(2,3));
    pb.add(mChannelsPerPageCB = new JComboBox(createIntegerArray(2,22)), cc.xy(4,3));
    pb.addLabel(mLocalizer.msg("columnsPerPage","columns")+":", cc.xy(2,5));
    pb.add(mLayoutCB = new JComboBox(mLayoutCBModel), cc.xy(4,5));

    mChannelsPerPageCB.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        int val = ((Integer)mChannelsPerPageCB.getSelectedItem()).intValue();
        updateLayoutCombobox(val);
      }
    });
  }


  public void setColumnLayout(int columnsPerPage, int channelsPerColumn) {
    int channelsPerPage = columnsPerPage*channelsPerColumn;
    mChannelsPerPageCB.setSelectedItem(channelsPerPage);
    for (int i=0; i<mLayoutCBModel.getSize(); i++) {
      LayoutOption option = (LayoutOption)mLayoutCBModel.getElementAt(i);
      if (channelsPerColumn == option.getChannelsPerColumn()) {
        mLayoutCB.setSelectedItem(option);
        break;
      }
    }
  }

  public int getColumnsPerPage() {
    LayoutOption option = (LayoutOption)mLayoutCB.getSelectedItem();
    return option.getChannelsPerPage() / option.getChannelsPerColumn();
  }

  public int getChannelsPerColumn() {
    LayoutOption option = (LayoutOption)mLayoutCB.getSelectedItem();
    return option.getChannelsPerColumn();
  }

  private Integer[] createIntegerArray(int from, int cnt) {
    Integer[] result = new Integer[cnt];
    for (int i=0; i<result.length; i++) {
      result[i] = i + from;
    }
    return result;
  }

  private void updateLayoutCombobox(int val) {
    mLayoutCBModel.removeAllElements();
    int[] primes = getPrimes(val);
    for (int prime : primes) {
      mLayoutCBModel.addElement(new LayoutOption(val, prime));
    }

  }

  private int[] getPrimes(int val) {
    ArrayList<Integer> list = new ArrayList<Integer>();
    for (int i=1; i<=val/2; i++) {
      if (val%i==0) {
        list.add(i);
      }
    }
    int[] result = new int[list.size()];
    for (int i=0; i<list.size(); i++) {
      result[i] = (list.get(i)).intValue();
    }
    return result;
  }


  private static class LayoutOption {

    private int mChannelsPerPage, mChannelsPerColumn;

    public LayoutOption(int channelsPerPage, int channelsPerColumn) {
      mChannelsPerPage = channelsPerPage;
      mChannelsPerColumn = channelsPerColumn;
    }

    public int getChannelsPerColumn() {
      return mChannelsPerColumn;
    }

    public int getChannelsPerPage() {
      return mChannelsPerPage;
    }

    public String toString() {
      int columns = mChannelsPerPage/mChannelsPerColumn;
      String s = mLocalizer.msg("layoutString",
          "{0} ({1} channels per column))", columns, mChannelsPerColumn);
      return s;
    }
  }

}
