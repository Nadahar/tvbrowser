/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.AbstractPluginProxy;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.SettingsTabProxy;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.TvBrowserException;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.CancelableSettingsTab;

public class ConfigPluginSettingsTab implements CancelableSettingsTab {
 
  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(ConfigPluginSettingsTab.class);

 
  private PluginProxy mPlugin;
  
  private SettingsTabProxy mSettingsTab;
  private JPanel mContentPanel;
  
  /**
   * Specifies whether the plugin was activated last time the content panel was
   * created.
   */
  private boolean mPluginWasActivatedLastTime;

  private JPanel mPluginPanel;

  public ConfigPluginSettingsTab(PluginProxy plugin) {
    mPlugin = plugin;
    if (mPlugin.isActivated()) {
      mSettingsTab = mPlugin.getSettingsTab();      
    } else {
      mSettingsTab = null;
    }
    
  }
  
  
  public JPanel createSettingsPanel() {
    mContentPanel=new JPanel(new BorderLayout());
    mContentPanel.setBorder(Borders.DIALOG_BORDER);
    PluginInfoPanel pluginInfoPanel=new PluginInfoPanel(mPlugin.getInfo(), mSettingsTab != null);
    pluginInfoPanel.setDefaultBorder(true);
    mContentPanel.add(pluginInfoPanel,BorderLayout.NORTH);
    
    updatePluginPanel();
    mContentPanel.add(mPluginPanel,BorderLayout.CENTER);
    
    return mContentPanel;
  }

  public void invalidate() {
    mPluginPanel = null;  // force to reload the content
  }


  public void updatePluginPanel() {
    // Check whether we've got something to do
    if ((mPluginPanel != null)
        && (mPlugin.isActivated() == mPluginWasActivatedLastTime))
    {
      // Nothing to do
      return;
    }
    if (mPluginPanel == null) {
      mPluginPanel = new JPanel(new BorderLayout());
    } else {
      mPluginPanel.removeAll();
    }
    if (mPlugin.isActivated()) {
      if (mSettingsTab != null) {
        mPluginPanel.add(mSettingsTab.createSettingsPanel(), BorderLayout.CENTER);
      }
    } else {
      // The plugin is not activated -> Tell it the user
      JPanel panelActivate = new JPanel();
      panelActivate.setLayout(new FormLayout("5dlu,pref,10dlu,pref,default:grow", "pref,5dlu,pref"));
      CellConstraints cc = new CellConstraints();

      panelActivate.add(DefaultComponentFactory.getInstance().createSeparator(
          mLocalizer.msg("activation", "Activation")), cc.xyw(1, 1, 5));
      
      panelActivate.add(new JLabel(mLocalizer.msg("notactivated", "This Plugin is currently not activated.")), cc.xy(2, 3));
      
      final JButton btnActivate = new JButton(mLocalizer.msg("activate", "Activate"));
      btnActivate.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          try {
            btnActivate.setEnabled(false);
            PluginProxyManager.getInstance().activatePlugin(mPlugin);
            SettingsDialog settingsDialog = SettingsDialog.getInstance();
            settingsDialog.invalidateTree();
            settingsDialog.createPluginTreeItems();
            settingsDialog.showSettingsTab(mPlugin.getId());
            MainFrame.getInstance().getToolbar().updatePluginButtons();
            // Update the settings
            String[] deactivatedPlugins = PluginProxyManager.getInstance().getDeactivatedPluginIds();
            Settings.propDeactivatedPlugins.setStringArray(deactivatedPlugins);
          } catch (TvBrowserException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
        }});
      
      panelActivate.add(btnActivate, cc.xy(4, 3));
      mPluginPanel.add(panelActivate, BorderLayout.NORTH);
    }
    
    mPluginWasActivatedLastTime = mPlugin.isActivated();

    mContentPanel.repaint();
  }

  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    if (mSettingsTab != null) {
      mSettingsTab.saveSettings();
    }
  }


  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    if ((mSettingsTab != null) && (mSettingsTab.getIcon() != null)) {
      return mSettingsTab.getIcon();
    }

    Icon icon = mPlugin.getPluginIcon();
    if (icon != null
        && icon instanceof ImageIcon
        && ((ImageIcon) icon).toString().equals(
            AbstractPluginProxy.DEFAULT_PLUGIN_ICON_NAME)) {
      return null;
    }
    return icon;
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mPlugin.getInfo().getName();
  }
  

  public void cancel() {
    if (mSettingsTab != null) {
      mSettingsTab.cancel();
    }
  }
}