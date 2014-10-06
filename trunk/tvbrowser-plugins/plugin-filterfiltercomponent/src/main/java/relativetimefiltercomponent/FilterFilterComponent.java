 /* FilterFilterComponent
 * Copyright (C) 2014 René Mach (rene@tvbrowser.org)
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
package filterfiltercomponent;

import util.ui.Localizer;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginsFilterComponent;
import devplugin.Version;

/**
 * TV-Browser plugin that provides filter component to use existing filters in new filters.
 * 
 * @author René Mach
 */
public class FilterFilterComponent extends Plugin {
  static final Localizer LOCALIZER = Localizer.getLocalizerFor(FilterFilterComponent.class);
  private static final Version VERSION = new Version(1, 0, true);
  
  public static Version getVersion() {
    return VERSION;
  }
  
  @Override
  public PluginInfo getInfo() {
    return new PluginInfo(FilterFilterComponent.class, LOCALIZER.msg("name","FilterFilterComponent"), LOCALIZER.msg("description","Allows to use filter as filtercomponent in other filters."), "René Mach", "GPL");
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
    return (Class<? extends PluginsFilterComponent>[]) new Class[] {FilterFilterComp.class};
  }
}
