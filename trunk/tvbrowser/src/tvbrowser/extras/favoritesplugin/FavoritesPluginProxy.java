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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.favoritesplugin;

import javax.swing.Icon;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.ButtonActionIf;
import tvbrowser.extras.common.InternalPluginProxyIf;

import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Marker;
import devplugin.Program;
import devplugin.SettingsItem;
import devplugin.SettingsTab;

/**
 * Encapsulates the FavoritesPlugin and manages the access to it.
 *
 * @author René Mach
 */
public class FavoritesPluginProxy implements ButtonActionIf, ContextMenuIf, Marker, InternalPluginProxyIf {
  
  private static FavoritesPluginProxy mInstance;
  private static FavoritesPlugin mFavoritesInstance;
  private Icon mMarkIcon;
  
  private FavoritesPluginProxy() {
    mInstance = this;
  }
  
  /**
   * @return The instance of the FavoritesPluginProxy
   */
  public static FavoritesPluginProxy getInstance() {
    if(mInstance == null) {
      mFavoritesInstance = FavoritesPlugin.getInstance();
      new FavoritesPluginProxy();
    }
    
    return mInstance;
  }
  
  public ActionMenu getContextMenuActions(Program program) {
    return mFavoritesInstance.getContextMenuActions(program);
  }

  public String getId() {
    return mFavoritesInstance.getId();
  }

  public String toString() {
    return mFavoritesInstance.toString();
  }

  public Icon getMarkIcon() {
    if(mMarkIcon == null)
      mMarkIcon = IconLoader.getInstance().getIconFromTheme("apps", "bookmark", 16);
    
    return mMarkIcon;
  }

  public Icon[] getMarkIcons(Program p) {
    return new Icon[] {getMarkIcon()};
  }

  public int getMarkPriorityForProgram(Program p) {
    return mFavoritesInstance.getMarkPriority();
  }

  public Icon getIcon() {
    return getMarkIcon();
  }

  public String getName() {
    return toString();
  }
  
  public String getButtonActionDescription() {
    return FavoritesPlugin.mLocalizer.msg("description","Automatically marks your favorite programs and passes them to other plugins.");
  }

  public SettingsTab getSettingsTab() {
    return new FavoritesSettingTab();
  }

  public String getSettingsId() {
    return SettingsItem.FAVORITE;
  }

  public ActionMenu getButtonAction() {
    return mFavoritesInstance.getButtonAction();
  }
}
