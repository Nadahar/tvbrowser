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
import devplugin.ChannelGroup;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import util.exc.TvBrowserException;


public class ChannelGroupSettingsTab implements SettingsTab {

  private JPanel mContent;
  private JList mGroupList;
  private DefaultListModel mListModel;

  public ChannelGroupSettingsTab() {
    mContent = new JPanel(new BorderLayout());

    mGroupList = new JList();
    mListModel = new DefaultListModel();
    mGroupList.setModel(mListModel);

    JPanel btnPn = new JPanel(new BorderLayout());
    JButton mRefreshBtn = new JButton("refresh");
    btnPn.add(mRefreshBtn, BorderLayout.WEST);
    btnPn.add(new JButton("disable"), BorderLayout.EAST);

    mContent.add(new JScrollPane(mGroupList), BorderLayout.CENTER);
    mContent.add(btnPn, BorderLayout.SOUTH);


    TvDataServiceProxy[] services = TvDataServiceProxyManager.getInstance().getDataServices();
    for (int i=0; i<services.length; i++) {
      ChannelGroup[] groupArr = services[i].getAvailableGroups();
      addToListBox(groupArr);
    }

    mRefreshBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        TvDataServiceProxy[] services = TvDataServiceProxyManager.getInstance().getDataServices();
        mListModel.clear();
        for (int i=0; i<services.length; i++) {
          if (services[i].supportsDynamicChannelGroups()) {
            try {
              ChannelGroup[] groupArr = services[i].checkForAvailableGroups(null);
              addToListBox(groupArr);
            } catch (TvBrowserException e1) {
              e1.printStackTrace();
            }
          }
        }
      }
    });
  }


  private void addToListBox(ChannelGroup[] groupArr) {
    for (int i=0; i<groupArr.length; i++) {
      mListModel.addElement(groupArr[i]);
    }
  }

  public JPanel createSettingsPanel() {
    return mContent;
  }

  public void saveSettings() {

  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return "Channel groups";
  }

}
