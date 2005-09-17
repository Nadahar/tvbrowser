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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This Tab shows the Plugin-Manager.
 * 
 * @author Martin Oberhauser
 */

public class PluginSettingsTab implements devplugin.SettingsTab {
  /** Localizer */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(PluginSettingsTab.class);
  /** List of Plugins */
  private JList mList;
  /** Buttons of Panel */
  private JButton mStartStopBtn, mInfo, mRemove;
  /** ListModel with Plugins */
  private DefaultListModel mListModel;
  /** SettingsDialog */
  private SettingsDialog mSettingsDialog;

  public PluginSettingsTab(SettingsDialog settingsDialog) {
    mSettingsDialog = settingsDialog;
  }

  public JPanel createSettingsPanel() {

    JPanel contentPanel = new JPanel(new FormLayout("default:grow, default", "default, 3dlu, fill:default:grow, 3dlu, default"));
    contentPanel.setBorder(Borders.DLU4_BORDER);
    
    CellConstraints cc = new CellConstraints();
    
    JButton update = new JButton(mLocalizer.msg("updateInstallPlugin", "Update/Install Plugins"), new ImageIcon("imgs/Search16.gif"));
    
    update.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showUpdatePluginsDlg();
      }
    });

    contentPanel.add(update, cc.xy(2,1));
    
    contentPanel.add(new JLabel(mLocalizer.msg("installedPlugins","Installed Plugins")+":"), cc.xy(1,1));
    
    mListModel = new DefaultListModel();
    mList = new JList(mListModel);
    mList.setCellRenderer(new PluginListCellRenderer());
    mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent evt) {
        updateBtns();
      }
    });

    mList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            showInfoDialog();
        }
      }
    });

    populatePluginList();
    
    contentPanel.add(new JScrollPane(mList), cc.xyw(1,3,2));
    
    mStartStopBtn = new JButton(mLocalizer.msg("activate", ""), new ImageIcon("imgs/Refresh16.gif"));
    mStartStopBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        PluginProxy plugin = (PluginProxy) mList.getSelectedValue();
        onStartStopBtnClicked(plugin);
      }
    });
    
    ButtonBarBuilder builder = new ButtonBarBuilder();

    mInfo = new JButton(mLocalizer.msg("info","Info"), new ImageIcon("imgs/About16.gif"));
    mInfo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showInfoDialog();
      }
    });
    
    builder.addGridded(mInfo);
    builder.addRelatedGap();
    builder.addGlue();
    builder.addFixed(mStartStopBtn);
    builder.addRelatedGap();
    
    mRemove = new JButton(mLocalizer.msg("remove","Remove"),  new ImageIcon("imgs/Delete16.gif"));
    mRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removePlugin();
      }
    });
    
    builder.addGridded(mRemove);
    
    contentPanel.add(builder.getPanel(), cc.xyw(1,5,2));
    
    updateBtns();

    return contentPanel;
  }

  /**
   * Remove a selected Plugin
   */
  private void removePlugin() {
    int inx = mList.getSelectedIndex();//mList.locationToIndex(e.getPoint());
    if (inx >= 0) {
      Object item = mListModel.getElementAt(inx);
      mList.ensureIndexIsVisible(inx);
      System.out.println(item);
    }
  }

  /**
   * Show the Info-Dialog
   *
   */
  private void showInfoDialog() {
    int inx = mList.getSelectedIndex();//mList.locationToIndex(e.getPoint());
    if (inx >= 0) {
      PluginProxy item = (PluginProxy)mListModel.getElementAt(inx);
      mList.ensureIndexIsVisible(inx);
      
      PluginInfoDialog dialog = new PluginInfoDialog(mSettingsDialog.getDialog(), item.getMarkIcon(), item.getInfo());
      UiUtilities.centerAndShow(dialog);
    }
  }
  
  /**
   * Populate the Plugin-List
   */
  private void populatePluginList() {
    PluginProxy[] pluginList = PluginProxyManager.getInstance().getAllPlugins();

    Arrays.sort(pluginList, new Comparator() {

      public int compare(Object o1, Object o2) {
        return o1.toString().compareTo(o2.toString());
      }

    });

    for (int i = 0; i < pluginList.length; i++) {
      mListModel.addElement(pluginList[i]);
    }

  }

  /**
   * Updates the State of the Buttons
   *
   */
  private void updateBtns() {
    PluginProxy plugin = (PluginProxy) mList.getSelectedValue();

    if ((plugin != null) && plugin.isActivated()) {
      mStartStopBtn.setEnabled(true);
      mInfo.setEnabled(true);
      mRemove.setEnabled(true);
      mStartStopBtn.setIcon(new ImageIcon("imgs/Stop16.gif"));
      mStartStopBtn.setText(mLocalizer.msg("deactivate", ""));
    } else {
      mStartStopBtn.setEnabled(plugin != null);
      mInfo.setEnabled(plugin != null);
      mRemove.setEnabled(plugin != null);
      mStartStopBtn.setIcon(new ImageIcon("imgs/Refresh16.gif"));
      mStartStopBtn.setText(mLocalizer.msg("activate", ""));
    }
  }

  /**
   * Start/Stop was clicked
   * 
   * @param plugin Plugin that is started/stopped
   */
  private void onStartStopBtnClicked(PluginProxy plugin) {
    if (plugin != null) {
      try {
        if (plugin.isActivated()) {
          PluginProxyManager.getInstance().deactivatePlugin(plugin);
        } else {
          PluginProxyManager.getInstance().activatePlugin(plugin);
        }
      } catch (TvBrowserException exc) {
        ErrorHandler.handle(exc);
      }

      mList.updateUI();
      updateBtns();
      mSettingsDialog.invalidateTree();
    }

    // Update the settings
    String[] deactivatedPlugins = PluginProxyManager.getInstance().getDeactivatedPluginIds();
    Settings.propDeactivatedPlugins.setStringArray(deactivatedPlugins);
  }

  public void saveSettings() {

  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("plugins", "Plugins");
  }

}
