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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.util.Enumeration;
import java.util.ArrayList;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.ChannelGroupManager;
import tvbrowser.core.Settings;
import tvbrowser.ui.settings.channel.ChannelGroupInfoDialog;
import util.ui.UiUtilities;
import util.ui.LinkButton;
import util.ui.progress.ProgressWindow;
import util.ui.progress.Progress;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.builder.ButtonBarBuilder;

public class ChannelGroupSettingsTab implements SettingsTab {

  private JPanel mContent;
  private DefaultListModel mListModel;
  private JList mGroupList;
  private JButton mEnableBtn, mInfoBtn;

  private SettingsDialog mSettingsDialog;

  private static final util.ui.Localizer mLocalizer
          = util.ui.Localizer.getLocalizerFor(ChannelGroupSettingsTab.class);

  public ChannelGroupSettingsTab(SettingsDialog settingsDialog) {

    mSettingsDialog = settingsDialog;

    mContent = new JPanel(new FormLayout("default:grow, default", "default, 3dlu, fill:default:grow, 3dlu, default,3dlu,default"));
    mContent.setBorder(Borders.DLU4_BORDER);

    mListModel = new DefaultListModel();
    mGroupList = new JList(mListModel);
    mGroupList.setCellRenderer(new ChannelGroupListCellRenderer());
    mGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    JButton refreshBtn = new JButton(mLocalizer.msg("update","Update"), IconLoader.getInstance().getIconFromTheme("actions", "web-search", 16));

    mInfoBtn = new JButton(mLocalizer.msg("info","Info"), IconLoader.getInstance().getIconFromTheme("status", "dialog-information", 16));

    mEnableBtn = new JButton(mLocalizer.msg("disable","Disable"));

    CellConstraints cc = new CellConstraints();

    mContent.add(refreshBtn, cc.xy(2,1));
    mContent.add(new JLabel(mLocalizer.msg("availableGroups","Available channel groups:")), cc.xy(1,1));
    mContent.add(new JScrollPane(mGroupList), cc.xyw(1,3,2));



    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGridded(mInfoBtn);
    builder.addRelatedGap();
    builder.addGlue();
    builder.addFixed(mEnableBtn);


    mContent.add(builder.getPanel(), cc.xyw(1,5,2));

    LinkButton urlLabel = new LinkButton(
           mLocalizer.msg("addMoreChannels","Ihnen fehlt Ihr Lieblings-Sender? Clicken Sie hier für eine Liste weiterer Sender."),
           mLocalizer.msg("addMoreChannelsUrl", "http://wiki.tvbrowser.org/index.php/Senderliste"));

    mContent.add(urlLabel, cc.xyw(1,7,2));

    fillListBox();

    updateEnableButton();
    updateGroupStatus();

    refreshBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        refreshList();
      }
    });

    mInfoBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        Object value = mGroupList.getSelectedValue();
        ChannelGroupWrapper w = (ChannelGroupWrapper)value;
        ChannelGroupInfoDialog dlg = new ChannelGroupInfoDialog(mSettingsDialog.getDialog(), w.getGroup());
        UiUtilities.centerAndShow(dlg);
      }
    });

    mEnableBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        Object value = mGroupList.getSelectedValue();
        ChannelGroupWrapper w = (ChannelGroupWrapper)value;
        subscribeGroup(w, !w.isEnabled());
      }
    });

    mGroupList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          updateEnableButton();
        }
      }
    });

    mGroupList.addMouseListener(new MouseAdapter(){

      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          int index = mGroupList.locationToIndex(e.getPoint());
          if (index >=0) {
            mGroupList.setSelectedIndex(index);
            Object value = mGroupList.getModel().getElementAt(index);
            ChannelGroupWrapper w = (ChannelGroupWrapper)value;
            JPopupMenu menu = createContextMenu(w);
            menu.show(mGroupList, e.getX(),  e.getY());
          }
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          int index = mGroupList.locationToIndex(e.getPoint());
          if (index >=0) {
            mGroupList.setSelectedIndex(index);
            Object value = mGroupList.getModel().getElementAt(index);
            ChannelGroupWrapper w = (ChannelGroupWrapper)value;
            JPopupMenu menu = createContextMenu(w);
            menu.show(mGroupList, e.getX(),  e.getY());
          }
        }
      }
      
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
          int index = mGroupList.locationToIndex(e.getPoint());
          Object value = mGroupList.getModel().getElementAt(index);
          ChannelGroupWrapper w = (ChannelGroupWrapper)value;
          ChannelGroupInfoDialog dlg = new ChannelGroupInfoDialog(mSettingsDialog.getDialog(), w.getGroup());
          UiUtilities.centerAndShow(dlg);
        }
      }
    });
  }


  private JPopupMenu createContextMenu(final ChannelGroupWrapper groupWrapper) {
    JPopupMenu menu = new JPopupMenu();

    JMenuItem infoMI = new JMenuItem(mLocalizer.msg("info","Info"), IconLoader.getInstance().getIconFromTheme("status", "dialog-information", 16));
    infoMI.setFont(infoMI.getFont().deriveFont(Font.BOLD));
    infoMI.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        ChannelGroupInfoDialog dlg = new ChannelGroupInfoDialog(mSettingsDialog.getDialog(), groupWrapper.getGroup());
        UiUtilities.centerAndShow(dlg);
      }
    });
    menu.add(infoMI);

    JMenuItem enableMI;
    if (groupWrapper.isEnabled()) {
      enableMI = new JMenuItem(mLocalizer.msg("disable","Disable"), IconLoader.getInstance().getIconFromTheme("actions", "process-stop", 16));
      enableMI.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          subscribeGroup(groupWrapper, false);
        }
      });
    }
    else {
      enableMI = new JMenuItem(mLocalizer.msg("enable","Enable"), IconLoader.getInstance().getIconFromTheme("actions", "view-refresh", 16));
      enableMI.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          subscribeGroup(groupWrapper, true);
        }
      });
    }
    menu.add(enableMI);

    menu.addSeparator();

    JMenuItem updateMI = new JMenuItem(mLocalizer.msg("update","Update"), IconLoader.getInstance().getIconFromTheme("actions", "web-search", 16));
    updateMI.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        refreshList();
      }
    });
    menu.add(updateMI);

    return menu;

  }

  private void refreshList() {
    final ProgressWindow progressWindow = new ProgressWindow(mSettingsDialog.getDialog());
    progressWindow.run(new Progress(){
      public void run() {
       ChannelGroupManager.getInstance().checkForAvailableGroups(progressWindow);
       fillListBox();
       updateGroupStatus();
      }
    });

  }

  private void updateEnableButton() {
    Object value = mGroupList.getSelectedValue();
    if (value == null) {
      mEnableBtn.setEnabled(false);
      mInfoBtn.setEnabled(false);
    }
    else {
      mInfoBtn.setEnabled(true);
      mEnableBtn.setEnabled(true);
      ChannelGroupWrapper w = (ChannelGroupWrapper)value;
      if (w.isEnabled()) {
        mEnableBtn.setText(mLocalizer.msg("disable","Disable"));
        mEnableBtn.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "process-stop", 16));
      }
      else {
        mEnableBtn.setText(mLocalizer.msg("enable","Enable"));
        mEnableBtn.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "view-refresh", 16));
      }
    }
  }

  private void updateGroupStatus() {
    setEnabledGroups(ChannelGroupManager.getInstance().getSubscribedGroups());
  }


  private void fillListBox() {
    mListModel.clear();

    ChannelGroup[] groups = ChannelGroupManager.getInstance().getAvailableGroups();
    for (int i=0; i<groups.length; i++) {
      TvDataServiceProxy service = ChannelGroupManager.getInstance().getTvDataService(groups[i]);
      mListModel.addElement(new ChannelGroupWrapper(service, groups[i]));
    }

  }

  private void setEnabledGroups(ChannelGroup[] groupArr) {
    Enumeration e = mListModel.elements();
    while (e.hasMoreElements()) {
      ChannelGroupWrapper elem = (ChannelGroupWrapper)e.nextElement();
      elem.setEnabled(false);
      for (int i=0; i<groupArr.length; i++) {
        if (groupArr[i].equals(elem.getGroup())) {
          elem.setEnabled(true);
          break;
        }
      }
    }

  }

  private void subscribeGroup(ChannelGroupWrapper group, boolean subscribe) {
    if (subscribe) {
      ChannelGroupManager.getInstance().subscribeGroup(group.getGroup());
    }
    else {
      ChannelGroupManager.getInstance().unsubscribeGroup(group.getGroup());
    }
    group.setEnabled(subscribe);
    updateEnableButton();
    mGroupList.updateUI();
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
    return mLocalizer.msg("title","Channel groups");
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
      mDescTa = UiUtilities.createHelpTextArea(group.getDescription()+"\n");
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

