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
 *
 * CVS information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.searchplugin;

import javax.swing.Icon;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.InternalPluginProxyIf;
import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Program;
import devplugin.SettingsItem;
import devplugin.SettingsTab;

/**
 * Encapsulates the SearchPlugin and manages the access to it.
 *
 * @author Ren� Mach
 */
public class SearchPluginProxy implements ContextMenuIf, InternalPluginProxyIf {
  
  private static SearchPluginProxy mInstance;

  private SearchPluginProxy() {
    mInstance = this;
  }
  
  /**
   * @return The instance of the SearchPluginProxy
   */
  public static synchronized SearchPluginProxy getInstance() {
    if(mInstance == null)
      new SearchPluginProxy();
    
    return mInstance;
  }
  
  public ActionMenu getContextMenuActions(Program program) {
    return SearchPlugin.getInstance().getContextMenuActions(program);
  }

  public String getId() {
    return SearchPlugin.getInstance().getId();
  }
  
  public String toString() {
    return SearchPlugin.getInstance().toString();
  }

  public String getDescription() {
    return SearchPlugin.mLocalizer.msg("description", "Allows searching programs containing a certain text.");
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("actions", "system-search", 16);
  }

  public String getName() {
    return toString();
  }

  public SettingsTab getSettingsTab() {
    return new SearchSettingsTab();
  }

  public String getSettingsId() {
    return SettingsItem.SEARCH;
  }

}
