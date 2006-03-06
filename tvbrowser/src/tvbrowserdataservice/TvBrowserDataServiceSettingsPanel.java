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

package tvbrowserdataservice;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowserdataservice.file.DayProgramFile;
import tvbrowserdataservice.file.TvDataLevel;
import tvdataservice.SettingsPanel;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.ImageUtilities;
import util.ui.UiUtilities;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;

public class TvBrowserDataServiceSettingsPanel extends SettingsPanel implements ActionListener {

  private Properties mSettings;

  private JCheckBox[] mLevelCheckboxes;

  private JTextArea mGroupDescriptionTA;

  private JButton mAddBtn, mRemoveBtn, mInfoBtn;

  private JList mGroupList;

  private DefaultListModel mGroupListModel;

  private ChannelGroup mGroup;

  private static SettingsPanel mInstance;

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TvBrowserDataServiceSettingsPanel.class);

  protected TvBrowserDataServiceSettingsPanel(Properties settings) {

    mSettings = settings;
    setLayout(new BorderLayout());

    JTabbedPane tabbedPane = new JTabbedPane();

    /* level list pane */
    JPanel levelList = new JPanel();
    levelList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    levelList.setLayout(new BoxLayout(levelList, BoxLayout.Y_AXIS));

    levelList.add(new JLabel(mLocalizer.msg("downloadLevel", "Download this data")));

    TvDataLevel[] levelArr = DayProgramFile.LEVEL_ARR;

    String[] levelIds = settings.getProperty("level", "").split(":::");

    mLevelCheckboxes = new JCheckBox[levelArr.length];
    for (int i = 0; i < levelArr.length; i++) {
      mLevelCheckboxes[i] = new JCheckBox(levelArr[i].getDescription());
      levelList.add(mLevelCheckboxes[i]);
      if (levelArr[i].isRequired()) {
        mLevelCheckboxes[i].setSelected(true);
        mLevelCheckboxes[i].setEnabled(false);
      } else {
        for (int j = 0; j < levelIds.length; j++) {
          if (levelIds[j].equals(levelArr[i].getId())) {
            mLevelCheckboxes[i].setSelected(true);
          }
        }
      }
    }

    /* group list pane */

    JPanel groupListPanel = new JPanel(new BorderLayout(0, 10));
    groupListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JTextArea ta = UiUtilities.createHelpTextArea(mLocalizer.msg("channelgroup.description","description"));
    groupListPanel.add(ta, BorderLayout.NORTH);

    JPanel panel2 = new JPanel(new BorderLayout(10, 0));

    mGroupListModel = new DefaultListModel();

    mGroupList = new JList(mGroupListModel);
    mGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    panel2.add(new JScrollPane(mGroupList), BorderLayout.CENTER);

    JPanel panel3 = new JPanel(new BorderLayout());
    JPanel btnPn = new JPanel();
    btnPn.setLayout(new GridLayout(0, 1, 0, 4));

    mAddBtn = new JButton(mLocalizer.msg("add", "Add"), ImageUtilities.createImageIconFromJar("tvbrowserdataservice/Add24.gif",
            getClass()));
    mRemoveBtn = new JButton(mLocalizer.msg("remove", "Remove"), ImageUtilities.createImageIconFromJar(
            "tvbrowserdataservice/Remove24.gif", getClass()));

    mInfoBtn =  new JButton("Information", ImageUtilities.createImageIconFromJar(
            "tvbrowserdataservice/Information24.gif", getClass()));

    mAddBtn.setHorizontalAlignment(JButton.LEFT);
    mRemoveBtn.setHorizontalAlignment(JButton.LEFT);
    mInfoBtn.setHorizontalAlignment(JButton.LEFT);

    btnPn.add(mAddBtn);
    btnPn.add(mRemoveBtn);
    btnPn.add(mInfoBtn);

    mAddBtn.addActionListener(this);
    mRemoveBtn.addActionListener(this);
    mInfoBtn.addActionListener(this);


    panel3.add(btnPn, BorderLayout.NORTH);
    panel2.add(panel3, BorderLayout.EAST);

    JPanel groupInfoPanel = new JPanel(new BorderLayout(3, 0));
    JPanel westPn = new JPanel(new BorderLayout());

    westPn.add(new JLabel(mLocalizer.msg("description", "Description:")), BorderLayout.NORTH);
    groupInfoPanel.add(westPn, BorderLayout.WEST);

    mGroupDescriptionTA = UiUtilities.createHelpTextArea("");
    mGroupDescriptionTA.setPreferredSize(new Dimension(0, 30));

    groupInfoPanel.add(mGroupDescriptionTA, BorderLayout.CENTER);

    groupListPanel.add(panel2, BorderLayout.CENTER);

    groupListPanel.add(groupInfoPanel, BorderLayout.SOUTH);

    tabbedPane.add(mLocalizer.msg("datalevel", "data level"), levelList);
    tabbedPane.add(mLocalizer.msg("channelgroups", "channel groups"), groupListPanel);

    add(tabbedPane, BorderLayout.CENTER);

    mGroupList.addListSelectionListener(new ListSelectionListener() {

      public void valueChanged(ListSelectionEvent arg0) {
        mRemoveBtn.setEnabled(mGroupList.getSelectedIndex() >= 0);
        mInfoBtn.setEnabled(mGroupList.getSelectedIndex() >= 0);
        ChannelGroup group = (ChannelGroup) mGroupList.getSelectedValue();
        if (group == null) {
          mGroupDescriptionTA.setText("");
        } else {
          mGroupDescriptionTA.setText(group.getDescription());
        }
      }
    });

    mRemoveBtn.setEnabled(mGroupList.getSelectedIndex() >= 0);
    mInfoBtn.setEnabled(mGroupList.getSelectedIndex() >= 0);
    fillGroupList(TvBrowserDataService.getInstance().getUserDefinedChannelGroups());

  }

  private void fillGroupList(devplugin.ChannelGroup[] groups) {
    Arrays.sort(groups,new Comparator() {
      public int compare(Object o1, Object o2) {
        devplugin.ChannelGroup g1 = (devplugin.ChannelGroup) o1;
        devplugin.ChannelGroup g2 = (devplugin.ChannelGroup) o2;
        return g1.getName().compareToIgnoreCase(g2.getName());
      }
    });
    mGroupListModel.removeAllElements();
    for (int i = 0; i < groups.length; i++) {
      mGroupListModel.addElement(groups[i]);
    }
  }

  public static SettingsPanel getInstance(Properties settings) {
    if (mInstance == null) {
      mInstance = new TvBrowserDataServiceSettingsPanel(settings);
    }
    return mInstance;
  }

  public void ok() {
    String setting = "";
    ArrayList levelList = new ArrayList();

    for (int i = 0; i < mLevelCheckboxes.length; i++) {
      if (mLevelCheckboxes[i].isSelected()) {
        setting += ":::" + DayProgramFile.LEVEL_ARR[i].getId();
        levelList.add(DayProgramFile.LEVEL_ARR[i]);
      }
    }
    TvBrowserDataService.getInstance().setTvDataLevel((TvDataLevel[])levelList.toArray(new TvDataLevel[levelList.size()]));

    if (setting.length() > 3) {
      setting = setting.substring(3);
    }
    mSettings.setProperty("level", setting);

    storeGroups();

  }

  private void storeGroups() {
    StringBuffer buf = new StringBuffer();
    Object[] groups = mGroupListModel.toArray();

    for (int i = 0; i < groups.length - 1; i++) {
      buf.append(((ChannelGroup) groups[i]).getId()).append(":");
    }
    if (groups.length > 0) {
      buf.append(((ChannelGroup) groups[groups.length - 1]).getId());
    }
    mSettings.setProperty("groupname", buf.toString());
    for (int i = 0; i < groups.length; i++) {
      StringBuffer urlBuf = new StringBuffer();
      String[] mirrorArr = ((ChannelGroup) groups[i]).getMirrorArr();
      for (int j = 0; j < mirrorArr.length - 1; j++) {
        urlBuf.append(mirrorArr[j]).append(";");
      }
      if (mirrorArr.length > 0) {
        urlBuf.append(mirrorArr[mirrorArr.length - 1]);
      }
      mSettings.setProperty("group_" + ((ChannelGroup) groups[i]).getId(), urlBuf.toString());
    }
  }

  private ChannelGroup getChannelGroupByURL(String url, devplugin.ProgressMonitor monitor) throws TvBrowserException {
    int pos = url.lastIndexOf('/');
    String groupId = url.substring(pos + 1, url.length());

    String groupUrl = url.substring(0, pos);

    ChannelGroup group = new ChannelGroup(TvBrowserDataService.getInstance(), groupId, new String[] { groupUrl }, mSettings);
    group.checkForAvailableChannels(monitor);
    return group;
  }

  private void addGroupUrl(final String url) {
    try {
      new URL(url);
    } catch (MalformedURLException e) {
      JOptionPane.showMessageDialog(this, mLocalizer.msg("invalidUrl", "'{0}' is not a valid URL", url));
      return;
    }

    final ProgressWindow progressWindow = new ProgressWindow(this);
    mGroup = null;
    progressWindow.run(new Progress() {

      public void run() {
        try {
          mGroup = getChannelGroupByURL(url.trim(), progressWindow);
        } catch (TvBrowserException exc) {
          ErrorHandler.handle(exc);
        }
      }
    });
    if (mGroup != null) {
      if (!mGroupListModel.contains(mGroup)) {
        mGroupListModel.addElement(mGroup);
        TvBrowserDataService.getInstance().addGroup(mGroup);
      }
    }

  }

  public void actionPerformed(ActionEvent event) {

    Object source = event.getSource();
    if (source == mAddBtn) {
      String groupUrl = (String) JOptionPane.showInputDialog(this, mLocalizer.msg("enterGroupUrl",
              "Please enter the URL of the new group"), mLocalizer.msg("enterGroupDlgTitle", "Add group"), JOptionPane.PLAIN_MESSAGE,
              null, null, "");
      if (groupUrl != null && groupUrl.length() > 0) {
        addGroupUrl(groupUrl);
        storeGroups();
      }
    } else if (source == mRemoveBtn) {
      ChannelGroup group = (ChannelGroup) mGroupList.getSelectedValue();
      Object[] options = { mLocalizer.msg("removeGroup", "yes,remove"), mLocalizer.msg("keepGroup", "Keep!") };
      int deleteGroup = JOptionPane.showOptionDialog(this, mLocalizer.msg("removeGroupQuestion",
              "Do you want to remove group '{0}' ?", group.getName()), mLocalizer.msg("removeGroupDlgTitle", "Remove group"),
              JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

      if (deleteGroup == JOptionPane.YES_OPTION) {
        mGroupListModel.removeElement(group);
        TvBrowserDataService.getInstance().removeGroup(group);
        storeGroups();
      }
    } else if (source == mInfoBtn) {
      ChannelGroup group = (ChannelGroup) mGroupList.getSelectedValue();

      if (group != null) {
        ChannelGroupDialog dialog;

        Window parent = UiUtilities.getBestDialogParent(this);
        if (parent instanceof JFrame) {
          dialog = new ChannelGroupDialog((JFrame)parent, group);
        } else {
          dialog = new ChannelGroupDialog((JDialog)parent, group);
        }
        dialog.show();
      }
    }

  }

}