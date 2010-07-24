/*
 * TV-Browser
 * Copyright (C) 2003-2010 TV-Browser-Team (dev@tvbrowser.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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
 *     $Date$
 *     $Id$
 *   $Author$
 * $Revision$
 */
package tvbrowser.core.plugin;

import devplugin.Version;

/**
 * A class that contains basic informations
 * like ID and version about a Plugin.
 * <p>
 * @author René Mach
 */
public class PluginBaseInfo implements Comparable {
  private String mPluginId;
  private Version mVersion;
  
  /**
   * Creates an instance of this class.
   * <p>
   * @param pluginId The Plugin ID.
   * @param version The version of the Plugin.
   */
  public PluginBaseInfo(String pluginId, Version version) {
    mPluginId = pluginId;
    mVersion = version;
  }
  
  /**
   * Gets the Plugin ID
   * <p>
   * @return The Plugin ID
   */
  public String getPluginId() {
    return mPluginId;
  }
  
  /**
   * Gets the Plugin version
   * <p>
   * @return The Plugin version
   */
  public Version getVersion() {
    return mVersion;
  }
  
  public int compareTo(Object o) {
    if(o != null && o instanceof PluginBaseInfo) {
      if(mPluginId.equals(((PluginBaseInfo)o).mPluginId)) {
        return 0;
      }
      
      return -1;
    }
    
    return 0;
  }
  
  public String toString() {
    return new StringBuilder("PLUGIN-ID: ").append(" ").append(mPluginId).append(" VERSION: ").append(mVersion).toString();
  }
}
