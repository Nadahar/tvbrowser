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

package printplugin.dlgs.printdayprogramsdialog;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.TabLayout;

public class LayoutTab extends JPanel {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(LayoutTab.class);


  private JComboBox mChannelsPerPageCB;
  private JComboBox mLayoutCB;
  private DefaultComboBoxModel mLayoutCBModel;

  public LayoutTab() {
    super();
    setLayout(new BorderLayout());

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    mChannelsPerPageCB = new JComboBox(createIntegerArray(2,22));
    mLayoutCBModel = new DefaultComboBoxModel();
    mLayoutCB = new JComboBox(mLayoutCBModel);
    JPanel columnsPanel = new JPanel(new TabLayout(2));
    columnsPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("channelsAndColumns","Channels and columns")));
    columnsPanel.add(new JLabel(mLocalizer.msg("channelsPerPage","Channels per page")+":"));
    columnsPanel.add(mChannelsPerPageCB);
    columnsPanel.add(new JLabel(mLocalizer.msg("columnsPerPage","columns")+":"));
    columnsPanel.add(mLayoutCB);

    mChannelsPerPageCB.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        int val = ((Integer)mChannelsPerPageCB.getSelectedItem()).intValue();
        updateLayoutCombobox(val);
      }
    });

    content.add(columnsPanel);
    add(content, BorderLayout.NORTH);
  }


  public void setColumnLayout(int columnsPerPage, int channelsPerColumn) {
    int channelsPerPage = columnsPerPage*channelsPerColumn;
    mChannelsPerPageCB.setSelectedItem(new Integer(channelsPerPage));
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
      result[i] = new Integer(i+from);
    }
    return result;
  }

  private void updateLayoutCombobox(int val) {
    mLayoutCBModel.removeAllElements();
    int[] primes = getPrimes(val);
    for (int i=0; i<primes.length; i++) {
      mLayoutCBModel.addElement(new LayoutOption(val, primes[i]));
    }

  }

  private int[] getPrimes(int val) {
    ArrayList list = new ArrayList();
    for (int i=1; i<=val/2; i++) {
      if (val%i==0) {
        list.add(new Integer(i));
      }
    }
    int[] result = new int[list.size()];
    for (int i=0; i<list.size(); i++) {
      result[i] = ((Integer)list.get(i)).intValue();
    }
    return result;
  }


  class LayoutOption {

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
      String s = mLocalizer.msg("layoutString", "{0} ({1} channels per column))", new Integer(columns), new Integer(mChannelsPerColumn));
      return s;
    }
  }

}
