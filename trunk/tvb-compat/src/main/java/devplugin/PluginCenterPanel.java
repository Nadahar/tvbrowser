/*
 * TV-Browser
 * Copyright (C) 2012 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date: 2017-05-22 20:33:50 +0200 (Mo, 22 Mai 2017) $
 *   $Author: ds10 $
 * $Revision: 8692 $
 */
package devplugin;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * A class that can be used to add panels to the
 * TV-Browser main window.
 * <p>
 * ATTENTION: Always extend this class to create own classes
 *            for your Plugin. Else way it won't work because
 *            you will not have an unique ID for your center
 *            panel.
 *            
 * @author René Mach
 * @since 3.2
 */
public abstract class PluginCenterPanel {
  private String mSettingsId;
  private Icon mIcon;
  
  /**
   * @param settingsId The settings ID of the plugin of this center panel.
   * @since 3.4.5
   */
  public final void setSettingsId(String settingsId) {
    mSettingsId = settingsId;
  }
  
  /**
   * @return The settings ID of this center panels plugin or <code>null</code>
   * if there are no settings.
   * @since 3.4.5 
   */
  public final String getSettingsId() {
    return mSettingsId;
  }
  
  /**
   * @param icon The icon to use for tab
   * @since 3.4.5
   */
  public final void setIcon(Icon icon) {
    mIcon = icon;
  }
  
  /**
   * @return The icon for this panel or <code>null</code>
   * @since 3.4.5
   */
  public final Icon getIcon() {
    return mIcon;
  }
  
  /**
   * Gets the name of this PluginCenterPanel
   * that is used to show in the tab bar of
   * the main window of TV-Browser.
   * 
   * @return The name of this PluginCenterPanel
   */
  public abstract String getName();
  
  /**
   * Gets the JPanel that should be shown
   * in the tab bar of the TV-Browser main window. 
   * 
   * @return The JPanel that is used in the main window.
   */
  public abstract JPanel getPanel();
  
  /**
   * Gets the ID (class name) of this PluginCenterPanel.
   * <p>
   * @return The ID (class name) of this PluginCenterPanel.
   */
  public final String getId() {
    return getClass().getCanonicalName();
  }
  
  public final String toString() {
    return getName();
  }
}
