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
 */
package devplugin;

/**
 * Overwrite this class to support filters for your plugin.
 * 
 * @author René Mach
 */
public abstract class PluginsProgramFilter implements ProgramFilter {
  private String mPluginId;
  
  /**
   * Creates an instance of PluginsProgramFilter.
   * 
   * @param plugin The plugin to create for.
   */
  public PluginsProgramFilter(Plugin plugin) {
    mPluginId = plugin.getId();
  }
  
  /**
   * @return The name part that the plugin sets.
   */
  public abstract String getSubName();

  /**
   * @return The name of this filter component.
   */
  public final String getName() {
    String subName = getSubName();
    if (subName != null && subName.length() > 0) {
      return getPluginAccessOfFilter().getInfo().getName() + ": " + subName;
    } else {
      return getPluginAccessOfFilter().getInfo().getName();
    }
  }
  
  /**
   * @return The plugin access for this filter.
   */
  public PluginAccess getPluginAccessOfFilter() {
    return Plugin.getPluginManager().getActivatedPluginForId(mPluginId);
  }
  
  public final String toString() {
    return getName();
  }
}
