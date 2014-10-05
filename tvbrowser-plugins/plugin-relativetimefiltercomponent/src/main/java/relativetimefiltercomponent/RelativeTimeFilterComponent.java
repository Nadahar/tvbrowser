 /* RelativeTimeFilterComponent
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
 package relativetimefiltercomponent;

import util.ui.Localizer;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginsFilterComponent;
import devplugin.Version;

/**
 * A TV-Browser plugin that provides a filter component that accepts programs around the current time.
 * 
 * @author René Mach
 */
public class RelativeTimeFilterComponent extends Plugin {
  static final Localizer LOCALIZER = Localizer.getLocalizerFor(RelativeTimeFilterComponent.class);
  private static final Version VERSION = new Version(1,0);
  
  public static Version getVersion() {
    return VERSION;
  }
  
  @Override
  public PluginInfo getInfo() {
    return new PluginInfo(RelativeTimeFilterComponent.class, LOCALIZER.msg("name","RelativeTimeFilterComponent"), LOCALIZER.msg("desc","Provides a filter component that accepts programs around the current time."), "René Mach", "GPL");
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
    return (Class<? extends PluginsFilterComponent>[]) new Class[] {RelativeTimeFilterComp.class};
  }
}
