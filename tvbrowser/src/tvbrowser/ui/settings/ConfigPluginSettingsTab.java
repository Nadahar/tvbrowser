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

import javax.swing.*;
import java.awt.*;

import devplugin.SettingsTab;
import devplugin.Plugin;



public class ConfigPluginSettingsTab implements SettingsTab {
 
  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(ConfigPluginSettingsTab.class);

 
  private Plugin mPlugin;
  private SettingsTab mSettingsTab;
  
  public ConfigPluginSettingsTab(devplugin.Plugin plugin) {
    mPlugin=plugin;
    mSettingsTab=mPlugin.getSettingsTab();
  }
 
  public JPanel createSettingsPanel() {
    
    if (mSettingsTab!=null) {
      return mSettingsTab.createSettingsPanel();
    }
    else {
      JPanel mainPanel=new JPanel(new BorderLayout());
      mainPanel.add(new JLabel(mLocalizer.msg("notsupported","This plugin does not support any settings")));  
      return mainPanel;
    }
    
  }

  
    /**
     * Called by the host-application, if the user wants to save the settings.
     */
    public void saveSettings() {
      if (mSettingsTab!=null) {
        mSettingsTab.saveSettings();
      }
    }

  
    /**
     * Returns the name of the tab-sheet.
     */
    public Icon getIcon() {
      return mPlugin.getMarkIcon();
    }
  
  
    /**
     * Returns the title of the tab-sheet.
     */
    public String getTitle() {
      return mPlugin.getInfo().getName();
    }
  
}