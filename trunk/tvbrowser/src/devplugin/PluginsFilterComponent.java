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

import tvbrowser.core.filters.FilterComponent;


/**
 * Overwrite this class to support filter
 * components for your plugin.
 * 
 * @author René Mach
 */
public abstract class PluginsFilterComponent implements FilterComponent {
  private String mPluginId;
  
  /**
   * Creates an instance of PluginsFilterComponent.
   * 
   * @param plugin The plugin to create for.
   */
  public PluginsFilterComponent(Plugin plugin) {
    mPluginId = plugin.getId();
  }
  
  /**
   * @return The plugin access for this filter component.
   */
  public PluginAccess getPluginAccessOfComponent() {
    return Plugin.getPluginManager().getActivatedPluginForId(mPluginId);
  }
  
  /**
   * @return The name of this filter compoent.
   */
  public final String getName() {
    return (getPluginAccessOfComponent().getInfo().getName()+ "_" +getSubName()).replaceAll("\\s+","_");
  }
  
  /**
   * @return The name part that the plugin sets.
   */
  protected abstract String getSubName();
  
  /**
   * @param name Set the name part that the plugin creates.
   */
  protected abstract void setSubName(String name);
  
  public final void setName(String name) {
    setSubName(name);
  }
}
