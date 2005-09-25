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
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.ArrayList;

import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.Settings;
import util.exc.TvBrowserException;
import util.exc.ErrorHandler;
import util.ui.UiUtilities;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.factories.Borders;


public class ChannelGroupSettingsTab implements SettingsTab {

  private JPanel mContent;
  private DefaultListModel mListModel;
  private JList mGroupList;
  private JButton mEnableBtn;

  private static final util.ui.Localizer mLocalizer
          = util.ui.Localizer.getLocalizerFor(ChannelGroupSettingsTab.class);

  public ChannelGroupSettingsTab() {
    mContent = new JPanel(new BorderLayout());

    mGroupList = new JList();
    mGroupList.setCellRenderer(new ChannelGroupListCellRenderer());
    mGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mListModel = new DefaultListModel();
    mGroupList.setModel(mListModel);

    JPanel btnPn = new JPanel(new BorderLayout());
    JButton mRefreshBtn = new JButton(mLocalizer.msg("update","Update"));
    btnPn.add(mRefreshBtn, BorderLayout.WEST);

    mEnableBtn = new JButton(mLocalizer.msg("disable","Disable"));
    btnPn.add(mEnableBtn, BorderLayout.EAST);

    mContent.add(new JScrollPane(mGroupList), BorderLayout.CENTER);
    mContent.add(btnPn, BorderLayout.SOUTH);


    TvDataServiceProxy[] services = TvDataServiceProxyManager.getInstance().getDataServices();
    for (int i=0; i<services.length; i++) {
      ChannelGroup[] groupArr = services[i].getAvailableGroups();
      addToListBox(services[i], groupArr);
    }

    updateEnableButton();
    updateGroupStatus();

    mRefreshBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        TvDataServiceProxy[] services = TvDataServiceProxyManager.getInstance().getDataServices();
        mListModel.clear();
        for (int i=0; i<services.length; i++) {
          if (services[i].supportsDynamicChannelGroups()) {
            try {
              ChannelGroup[] groupArr = services[i].checkForAvailableGroups(null);
              addToListBox(services[i], groupArr);
            } catch (TvBrowserException e) {
              ErrorHandler.handle(mLocalizer.msg("error.1","Could not receive grouplist for service '{0}'", services[i].getInfo().getName()), e);
            }
          }
        }
        updateGroupStatus();
      }
    });

    mEnableBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        Object value = mGroupList.getSelectedValue();
        ChannelGroupWrapper w = (ChannelGroupWrapper)value;
        w.setEnabled(!w.isEnabled());
        updateEnableButton();
        mGroupList.updateUI();
      }
    });

    mGroupList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          updateEnableButton();
        }
      }
    });
  }

  private void updateEnableButton() {
    Object value = mGroupList.getSelectedValue();
    if (value == null) {
      mEnableBtn.setEnabled(false);
    }
    else {
      mEnableBtn.setEnabled(true);
      ChannelGroupWrapper w = (ChannelGroupWrapper)value;
      if (w.isEnabled()) {
        mEnableBtn.setText(mLocalizer.msg("disable","Disable"));
      }
      else {
        mEnableBtn.setText(mLocalizer.msg("enable","Enable"));
      }
    }
  }

  private void updateGroupStatus() {
    String[] groupIdArr = Settings.propSubscribedChannelGroups.getStringArray();
    if (groupIdArr != null) {
      setEnabledGroups(groupIdArr);
    }else{
      enableAllGroups();
    }
  }

  private void addToListBox(TvDataServiceProxy service, ChannelGroup[] groupArr) {
    for (int i=0; i<groupArr.length; i++) {
      mListModel.addElement(new ChannelGroupWrapper(service, groupArr[i]));
    }
  }

  private void enableAllGroups() {
    Enumeration e = mListModel.elements();
    while (e.hasMoreElements()) {
      ChannelGroupWrapper elem = (ChannelGroupWrapper)e.nextElement();
      elem.setEnabled(false);
    }
  }

  private void setEnabledGroups(String[] groupIdArr) {
    HashMap map = new HashMap();
    Enumeration e = mListModel.elements();
    while (e.hasMoreElements()) {
      ChannelGroupWrapper elem = (ChannelGroupWrapper)e.nextElement();
      elem.setEnabled(false);
      map.put(getId(elem.getService(), elem.getGroup()), elem);
    }

    for (int i=0; i<groupIdArr.length; i++) {
      ChannelGroupWrapper wrapper = (ChannelGroupWrapper)map.get(groupIdArr[i]);
      if (wrapper != null) {
        wrapper.setEnabled(true);
      }
    }

  }

  public JPanel createSettingsPanel() {
    return mContent;
  }

  public void saveSettings() {
    ArrayList subscribedGroupIdList = new ArrayList();
    Enumeration e = mListModel.elements();
    while (e.hasMoreElements()) {
      ChannelGroupWrapper wrapper = (ChannelGroupWrapper)e.nextElement();
      if (wrapper.isEnabled()) {
        subscribedGroupIdList.add(getId(wrapper.getService(),wrapper.getGroup()));
      }
    }
    String[] subscribedGroupIdArr = (String[])subscribedGroupIdList.toArray(new String[subscribedGroupIdList.size()]);
    Settings.propSubscribedChannelGroups.setStringArray(subscribedGroupIdArr);
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return "Channel groups";
  }


  private String getId(TvDataServiceProxy service, ChannelGroup group) {
    return service.getId()+"."+group.getId();
  }

}


class ChannelGroupWrapper {

  private ChannelGroup mGroup;
  private boolean mIsEnabled;
  private TvDataServiceProxy mService;

  public ChannelGroupWrapper(TvDataServiceProxy service, ChannelGroup group) {
    mGroup = group;
    mService = service;
  }

  public ChannelGroup getGroup() {
    return mGroup;
  }

  public TvDataServiceProxy getService() {
    return mService;
  }

  public void setEnabled(boolean enable) {
    mIsEnabled = enable;
  }

  public boolean isEnabled() {
    return mIsEnabled;
  }

}


class ChannelGroupListCellRenderer extends DefaultListCellRenderer {

  private static final util.ui.Localizer mLocalizer
          = util.ui.Localizer.getLocalizerFor(ChannelGroupListCellRenderer.class);

  private JPanel mPanel;
  private JLabel mNameLb;
  private JTextArea mDescTa;
  private CellConstraints cc = new CellConstraints();

  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

    JLabel defaultLabel = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    if (value instanceof ChannelGroupWrapper) {
      ChannelGroupWrapper wrapper = (ChannelGroupWrapper)value;
      ChannelGroup group = wrapper.getGroup();

      if (mPanel == null) {
        mNameLb = new JLabel();
        mNameLb.setFont(defaultLabel.getFont().deriveFont(Font.BOLD, defaultLabel.getFont().getSize2D()+2));

        mPanel = new JPanel(new FormLayout("fill:pref:grow","default, 2dlu, default"));
        mPanel.setBorder(Borders.DLU2_BORDER);

        mPanel.add(mNameLb, cc.xy(1,1));
      }

      if (mDescTa != null) {
        mPanel.remove(mDescTa);
      }
      mDescTa = UiUtilities.createHelpTextArea(group.getDescription());
      mDescTa.setMinimumSize(new Dimension(100, 10));
      mDescTa.setOpaque(defaultLabel.isOpaque());
      mDescTa.setBackground(defaultLabel.getBackground());
      mDescTa.setEnabled(wrapper.isEnabled());
      mPanel.add(mDescTa, cc.xy(1,3));

      mNameLb.setOpaque(defaultLabel.isOpaque());
      mNameLb.setBackground(defaultLabel.getBackground());

      if (wrapper.isEnabled()) {
        mNameLb.setText(group.getName());
        mNameLb.setEnabled(true);
      } else {
        mNameLb.setText(group.getName() +" ["+mLocalizer.msg("deactivated", "Deactivated")+"]");
        mNameLb.setEnabled(false);
      }

      mPanel.setOpaque(defaultLabel.isOpaque());
      mPanel.setBackground(defaultLabel.getBackground());

      return mPanel;
    }

    return defaultLabel;
  }
}

