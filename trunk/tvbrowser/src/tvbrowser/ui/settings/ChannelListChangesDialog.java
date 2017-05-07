/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourcceforge.net)
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
package tvbrowser.ui.settings;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import devplugin.Channel;
import devplugin.SettingsItem;
import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.ui.settings.channel.ChannelJList;
import util.ui.ChannelListCellRenderer;
import util.ui.Localizer;

public class ChannelListChangesDialog extends JDialog {
  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ChannelListChangesDialog.class);

  private ArrayList<Channel> mAddedList;
  private ArrayList<Channel> mDeletedList;

  public ChannelListChangesDialog(JDialog owner, ArrayList<Channel> addedList, ArrayList<Channel> deletedList, boolean showSettingsLink) {
    super(owner,true);
    createGui(addedList, deletedList, showSettingsLink);
  }
  
  public ChannelListChangesDialog(JFrame owner, ArrayList<Channel> addedList, ArrayList<Channel> deletedList, boolean showSettingsLink) {
    super(owner,true);
    createGui(addedList, deletedList, showSettingsLink);
  }
  
  private void createGui(ArrayList<Channel> addedList, ArrayList<Channel> deletedList, boolean showSettingsLink) {
    mAddedList = addedList;
    mDeletedList = deletedList;
    createGui(showSettingsLink);
  }

  /**
   * Creates the GUI
   */
  private void createGui(boolean showSettingsLink) {
    setTitle(mLocalizer.msg("title", "Channel changes"));

    setLocationRelativeTo(getParent());

    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new GridLayout(1, 2, 10, 0));

    JPanel panelAdded = new JPanel(new BorderLayout());
    panelAdded.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("added", "New channels: {0}", mAddedList.size())));

    DefaultListModel<Object> listModel = new DefaultListModel<>();
    for (int i = 0; i < mAddedList.size(); i++) {
      listModel.addElement(mAddedList.get(i));
    }
    ChannelJList list = new ChannelJList(listModel);
    list.setCellRenderer(new ChannelListCellRenderer(true, true));

    panelAdded.add(new JScrollPane(list), BorderLayout.CENTER);

    JPanel panelDeleted = new JPanel(new BorderLayout());
    panelDeleted.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("deleted", "Removed channels: {0}", mDeletedList.size())));

    listModel = new DefaultListModel<>();
    for (int i = 0; i < mDeletedList.size(); i++) {
      listModel.addElement(mDeletedList.get(i));
    }
    list = new ChannelJList(listModel);
    list.setCellRenderer(new ChannelListCellRenderer(true, true));

    panelDeleted.add(new JScrollPane(list), BorderLayout.CENTER);

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    
    if(showSettingsLink) {
      JButton openSettings = new JButton(mLocalizer.msg("openSettings", "Open channel settings"));
      openSettings.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          setVisible(false);
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              PluginManagerImpl.getInstance().showSettings(SettingsItem.CHANNELS);
            }
          });
          
          dispose();
        }
      });
      
      btnPanel.add(openSettings);
    }
    
    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    ok.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        dispose();
      }

    });
    btnPanel.add(ok);

    contentPanel.add(panelAdded);
    contentPanel.add(panelDeleted);
    
    JPanel pane = (JPanel) getContentPane();
    pane.add(contentPanel, BorderLayout.CENTER);
    pane.add(btnPanel, BorderLayout.SOUTH);
    pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  }

  public static void showChannelChanges(Window owner, List<Channel> channelsBefore, List<Channel> channelsAfter, boolean showSettingsLink) {
    // compute changed channels
    ArrayList<Channel> addedList = new ArrayList<Channel>();
    ArrayList<Channel> deletedList = new ArrayList<Channel>();
    for (int i = 0; i < channelsAfter.size(); i++) {
      if (!channelsBefore.contains(channelsAfter.get(i))) {
        addedList.add(channelsAfter.get(i));
      }
    }
    Collections.sort(addedList);
    for (int i = 0; i < channelsBefore.size(); i++) {
      if (!channelsAfter.contains(channelsBefore.get(i))) {
        deletedList.add(channelsBefore.get(i));
      }
    }
    Collections.sort(deletedList);
    // show changes
    if (addedList.isEmpty() && deletedList.isEmpty()) {
      if(!showSettingsLink) {
        JOptionPane.showMessageDialog(owner, mLocalizer.msg("noChanges.message", "There are no changes in the list of available channels."), mLocalizer.msg("noChanges.title", "No changes"), JOptionPane.INFORMATION_MESSAGE);
      }
    }
    else {
      ChannelListChangesDialog changesDialog = null;
      
      if(owner instanceof JDialog) {
        changesDialog = new ChannelListChangesDialog((JDialog)owner, addedList, deletedList, showSettingsLink);
      }
      else if(owner instanceof JFrame) {
        changesDialog = new ChannelListChangesDialog((JFrame)owner, addedList, deletedList, showSettingsLink);
      }
      
      if(changesDialog != null) {
        Settings.layoutWindow("channelListChangesDialog", changesDialog);
        changesDialog.setVisible(true);
      }
    }
  }
}
