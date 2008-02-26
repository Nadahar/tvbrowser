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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.importantprogramsplugin;

import javax.swing.Icon;

import devplugin.ActionMenu;
import devplugin.SettingsTab;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.ButtonActionIf;
import tvbrowser.extras.common.InternalPluginProxyIf;

/**
 * The proxy class for the important programs plugin.
 * 
 * @author René Mach
 * @since 2.7
 */
public class ImportantProgramsPluginProxy implements InternalPluginProxyIf, ButtonActionIf {
  private static ImportantProgramsPluginProxy mInstance;
  
  private ImportantProgramsPluginProxy() {
    mInstance = this;
  }
  
  /**
   * Gets the instance of this plugin proxy.
   * @return The instance of this plugin proxy.
   */
  public static synchronized ImportantProgramsPluginProxy getInstance() {
    if(mInstance == null) {
      new ImportantProgramsPluginProxy();
    }
    
    return mInstance;
  }
  
  public String getDescription() {
    return ImportantProgramsPlugin.mLocalizer.msg("description", "Shows all important programs sorted by time.");
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("emblems","emblem-important",16);
  }

  public String getId() {
    return ImportantProgramsPlugin.DATAFILE_PREFIX;
  }

  public String getName() {
    return ImportantProgramsPlugin.mLocalizer.msg("name", "Important programs");
  }

  public String getSettingsId() {
    return null;
  }

  public SettingsTab getSettingsTab() {
    return null;
  }
  
  public String toString() {
    return getName();
  }

  public ActionMenu getButtonAction() {
    return ImportantProgramsPlugin.getInstance().getButtonAction();
  }

}
