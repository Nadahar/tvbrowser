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

package tvbrowser.ui.settings;

import devplugin.SettingsTab;
import devplugin.Channel;
import javax.swing.*;

import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Comparator;

import util.ui.table.TableSorter;
import util.ui.UiUtilities;
import util.ui.TabLayout;
import tvbrowser.core.ChannelList;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 23.01.2005
 * Time: 21:16:51
 */
public class ChannelsSettingsTabNew implements SettingsTab, ActionListener {

   private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ChannelsSettingsTabNew.class);

  private JButton mUnsubscribeBt, mSubscribeBt, mUpBt, mDownBt, mUpdateChannelListBt,
                  mConfigChannelsBt;
  private JTable mTable;
  private TableSorter mTableModel;
  private JList mList;
  private DefaultListModel mListModel;

  public ChannelsSettingsTabNew() {

  }

  public JPanel createSettingsPanel() {
    JPanel topPanel = new JPanel(new GridLayout(2,1));
    topPanel.setBorder(BorderFactory.createEmptyBorder(10,10,11,11));
    JPanel northPanel = createNorthPanel();
    JPanel southPanel = createSouthPanel();

    topPanel.add(northPanel);
    topPanel.add(southPanel);
    return topPanel;
  }


  private JPanel createNorthPanel() {
    JPanel content = new JPanel(new BorderLayout());


    JPanel panel = new JPanel(new BorderLayout(3,3));

    CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(
      BorderFactory.createEtchedBorder(),
      BorderFactory.createEmptyBorder(3,3,3,3)
    );

    panel.setBorder(compoundBorder);


    mUpdateChannelListBt = new JButton("Refresh",new ImageIcon("imgs/Refresh24.gif"));
    String msg = mLocalizer.msg("tooltip.updateChannellist", "Refresh list");
    mUpdateChannelListBt.setToolTipText(msg);
    mUpdateChannelListBt.setMargin(UiUtilities.ZERO_INSETS);
    mUpdateChannelListBt.setHorizontalTextPosition(SwingConstants.LEADING);



    JPanel btnPanel = new JPanel(new BorderLayout());
    btnPanel.setBorder(BorderFactory.createEmptyBorder(6,0,6,0));
    JPanel subscribePanel = new JPanel(new GridLayout(1,2));
    mSubscribeBt = new JButton("Subscribe", new ImageIcon("imgs/Down24.gif"));
    msg = mLocalizer.msg("tooltip.subscribe", "Subscribe channel");
    mSubscribeBt.setToolTipText(msg);
    mSubscribeBt.setHorizontalTextPosition(SwingConstants.LEADING);
    mSubscribeBt.setMargin(UiUtilities.ZERO_INSETS);

    mUnsubscribeBt = new JButton("Unsubscribe", new ImageIcon("imgs/Up24.gif"));
    msg = mLocalizer.msg("tooltip.unsubscribe", "Unsubscribe channel");
    mUnsubscribeBt.setToolTipText(msg);
    mUnsubscribeBt.setMargin(UiUtilities.ZERO_INSETS);

    subscribePanel.add(mSubscribeBt);
    subscribePanel.add(mUnsubscribeBt);

    panel.add(createChannelTable(), BorderLayout.CENTER);

    btnPanel.add(subscribePanel, BorderLayout.CENTER);

    JPanel pn = new JPanel(new BorderLayout());
    pn.add(mUpdateChannelListBt, BorderLayout.EAST);
    pn.add(new JLabel("Available Channels:"), BorderLayout.WEST);
    content.add(panel, BorderLayout.CENTER);
    content.add(btnPanel, BorderLayout.SOUTH);
    panel.add(pn, BorderLayout.NORTH);


    mSubscribeBt.addActionListener(this);
    mUnsubscribeBt.addActionListener(this);


    return content;
  }

  private JPanel createChannelTable() {
    JPanel panel = new JPanel(new BorderLayout());


    AvailableChannelsTableModel model = new AvailableChannelsTableModel();
  
    mTableModel = new TableSorter(model);
    mTable = new JTable(mTableModel);

    mTableModel.setColumnComparator(Channel.class, new Comparator(){
      public int compare(Object o1, Object o2) {
         if (o1 instanceof Channel && o2 instanceof Channel) {
           String channelName1 = ((Channel)o1).getName();
           String channelName2 = ((Channel)o2).getName();
           return channelName1.toUpperCase().compareTo(channelName2.toUpperCase());
         }
         return 0;
      }
  });



    mTable.setPreferredScrollableViewportSize(new Dimension(300, 70));
    mTableModel.setTableHeader(mTable.getTableHeader());
    panel.add(new JScrollPane(mTable), BorderLayout.CENTER);
    return panel;
  }

  private JPanel createSouthPanel() {
    JPanel topPanel = new JPanel(new BorderLayout());

    CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(
      BorderFactory.createEtchedBorder(),
      BorderFactory.createEmptyBorder(3,3,3,3)
    );
    topPanel.setBorder(compoundBorder);







    JPanel content = new JPanel(new GridLayout(1,2, 3,3));
    JPanel listBoxPanel = createSubscribedChannelListBoxPanel();
    JPanel channelInfoPanel = createChannelInfoPanel();

    content.add(listBoxPanel);
    content.add(channelInfoPanel);

    //topPanel.add(new JLabel("Subscribed Channels:"), BorderLayout.NORTH);
    topPanel.add(content, BorderLayout.CENTER);

    return topPanel;
  }

  private JPanel createSubscribedChannelListBoxPanel() {

    mUpBt = new JButton(new ImageIcon("imgs/Up24.gif"));
    String msg = mLocalizer.msg("tooltip.up", "Move selected rows up");
    mUpBt.setToolTipText(msg);
    mUpBt.setMargin(UiUtilities.ZERO_INSETS);

    mDownBt = new JButton(new ImageIcon("imgs/Down24.gif"));
    msg = mLocalizer.msg("tooltip.down", "Move selected rows down");
    mDownBt.setToolTipText(msg);
    mDownBt.setMargin(UiUtilities.ZERO_INSETS);

    JPanel btnPanel = new JPanel(new GridLayout(2,1));
    JPanel topPn = new JPanel(new BorderLayout());
    topPn.add(mUpBt, BorderLayout.SOUTH);

    JPanel bottomPn = new JPanel(new BorderLayout());
    bottomPn.add(mDownBt, BorderLayout.NORTH);

    btnPanel.add(topPn);
    btnPanel.add(bottomPn);



    JPanel content = new JPanel(new BorderLayout(3,3));
    JPanel titlePn = new JPanel(new BorderLayout());
    titlePn.add(new JLabel("Subscribed Channels:"), BorderLayout.WEST);
    content.add(titlePn, BorderLayout.NORTH);
    JPanel listPanel = new JPanel(new BorderLayout(4,4));
    //mList = new JList(ChannelList.getSubscribedChannels());
    mListModel = new DefaultListModel();
    Channel[] channels = ChannelList.getSubscribedChannels();
    for (int i=0; i<channels.length; i++) {
      mListModel.addElement(channels[i]);
    }
    mList = new JList(mListModel);
 //   mList.setCellRenderer(new ChannelListCellRenderer());
    listPanel.add(new JScrollPane(mList), BorderLayout.CENTER);
    listPanel.add(btnPanel, BorderLayout.EAST);

    mConfigChannelsBt = new JButton("configure selected channels");

    content.add(listPanel, BorderLayout.CENTER);
    content.add(mConfigChannelsBt, BorderLayout.SOUTH);

    return content;
  }

  private JPanel createChannelInfoPanel() {
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setBorder(BorderFactory.createTitledBorder("Channel details:"));

    JPanel content = new JPanel(new TabLayout(2));
    content.add(new JLabel("Channel:"));
    content.add(new JLabel("Eurosport"));
    content.add(new JLabel("Category:"));
    content.add(new JLabel("Sport"));
    content.add(new JLabel("Country:"));
    content.add(new JLabel("Deutschland"));
    content.add(new JLabel("Provider:"));
    content.add(new JLabel("Bodo Tasche"));
    content.add(new JLabel("Timezone:"));
    content.add(new JLabel("GMT+1"));

    topPanel.add(content, BorderLayout.NORTH);

    return topPanel;
  }



  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source == mSubscribeBt) {
      int[] selectedRows = mTable.getSelectedRows();
      if (selectedRows!=null && selectedRows.length>0) {
        Channel[] selectedChannels = new Channel[selectedRows.length];
        for (int i=0; i<selectedRows.length; i++) {
          selectedChannels[i] = (Channel)mTableModel.getValueAt(selectedRows[i], -1);
        }
        int[] listSelections = mList.getSelectedIndices();
        int insertPosition;
        if (listSelections.length>0) {
          insertPosition = listSelections[listSelections.length-1];
        }
        else {
          insertPosition = -1;
        }
        for (int i=0; i<selectedChannels.length; i++) {
          if (insertPosition >=0) {
            mListModel.insertElementAt(selectedChannels[i], ++insertPosition);
          }
          else {
            mListModel.addElement(selectedChannels[i]);
          }
          mTableModel.removeChannel(selectedChannels[i]);
        }
        if (insertPosition < mListModel.getSize()) {
          mList.setSelectedIndex(insertPosition);
        }
        int inx = selectedRows[0];
        int rows = mTable.getRowCount();
        if (inx < rows) {
          mTable.setRowSelectionInterval(inx, inx);
        }
        else if (rows>0) {
          mTable.setRowSelectionInterval(rows-1, rows-1);
        }
      }
    }
    else if (source == mUnsubscribeBt) {
      Object[] selectedValues = mList.getSelectedValues();
      int[] inx = mList.getSelectedIndices();
      int selectedInx = -1;
      if (inx.length>0) {
        selectedInx = inx[inx.length-1];
      }
      for (int i=0; i<selectedValues.length; i++) {
        mTableModel.addRow((Channel)selectedValues[i]);
        mListModel.removeElement(selectedValues[i]);
      }
      int rows = mListModel.getSize();
      if (selectedInx < rows) {
        mList.setSelectedIndex(selectedInx);
      }
      else if (rows>0) {
        mList.setSelectedIndex(rows-1);
      }

    }

  }


  public void saveSettings() {

  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return "Channels (1)";
  }







}
