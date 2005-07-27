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

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.SettingsTabProxy;
import devplugin.ActionMenu;
import devplugin.SettingsTab;

public class ConfigPluginSettingsTab implements SettingsTab {
 
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
    mContentPanel.setBorder(BorderFactory.createEmptyBorder(5,8,5,8));
    PluginInfoPanel pluginInfoPanel=new PluginInfoPanel(mPlugin.getInfo());
    pluginInfoPanel.setDefaultBorder();
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
      String msg = mLocalizer.msg("notactivated", "This Plugin is currently not activated.");
      
      JTextArea msgArea =new JTextArea(3,40);
      msgArea.setText(msg);
      msgArea.setLineWrap(true);
      msgArea.setWrapStyleWord(true);
      msgArea.setEditable(false);
      msgArea.setOpaque(false);
      
      mPluginPanel.add(msgArea, BorderLayout.WEST);
    }
    
    mPluginWasActivatedLastTime = mPlugin.isActivated();

    mContentPanel.updateUI();
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

    
    ActionMenu actionMenu = mPlugin.getButtonAction();
    Action action = null;
    if (actionMenu !=null) {
      action = actionMenu.getAction();
    }
    Icon icon = null;
    if (action != null) {
      icon = (Icon) action.getValue(Action.SMALL_ICON);
    }
    
    if (icon == null) {
      // The plugin has no button icon -> Try the mark icon
      icon = mPlugin.getMarkIcon();
    }
    
    return icon;
  }


  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    if ((mSettingsTab != null) && (mSettingsTab.getTitle() != null)) {
      return mSettingsTab.getTitle();
    }
    return mPlugin.getInfo().getName();
  }
  
}