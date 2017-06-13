/*
 * TV-Browser Compat
 * Copyright (C) 2017 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date: 2014-06-17 15:59:09 +0200 (Di, 17 Jun 2014) $
 *   $Author: ds10 $
 * $Revision: 8152 $
 */
package compat;

import java.lang.reflect.Constructor;

import javax.swing.Action;
import javax.swing.Icon;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Version;
import tvbrowser.TVBrowser;

/**
 * Compatibility class for TV-Browser context menu entries.
 * 
 * @author René Mach
 * @since 0.1
 */
public final class MenuCompat {
  /**
   * ID for menu action which shouldn't be available for mouse actions.
   */
  public static final int ID_ACTION_NONE = -1;
  
  /**
   * Creates a new single checkbox menu entry.
   * @param actionId The ID for the action for mouse functions.
   * @param action The action for the context menu.
   * @since 0.1
   */
  public static ActionMenu createActionMenu(final int actionId, final Action action) {
    return createActionMenu(actionId, action, false);
  }
  
  /**
   * Creates a new single checkbox menu entry.
   * @param actionId The ID for the action for mouse functions.
   * @param action The action for the context menu.
   * @param isSelected state of the check box (checked/unchecked).
   * @since 0.1
   */
  public static ActionMenu createActionMenu(final int actionId, final Action action, final boolean isSelected) {
    ActionMenu menu = new ActionMenu(action);
    
    if(TVBrowser.VERSION.compareTo(new Version(3,44,95,false)) >= 0) {
      try {
        final Constructor<ActionMenu> c = ActionMenu.class.getConstructor(int.class, Action.class, boolean.class);
        
        menu = c.newInstance(actionId, action, isSelected);
      } catch (Exception e) {
        // Ignore
        e.printStackTrace();
      }
    }
    
    return menu;
  }
  
  /**
   * Creates a menu item having sub menu items. These items can have
   * further sub menu items.
   *
   * @see ContextMenuAction
   *
   * @param actionId an ID to identify this action menu for mouse actions
   * @param menuTitle title of the sub menu
   * @param menuIcon icon of the sub menu
   * @since 0.1
   */
  public static ActionMenu createActionMenu(final int actionId, final String menuTitle, final Icon menuIcon) {
    return createActionMenu(actionId, menuTitle, menuIcon, null, false);
  }
  
  /**
   * Creates a menu item having sub menu items. These items can have
   * further sub menu items.
   *
   * @see ContextMenuAction
   *
   * @param actionId an ID to identify this action menu for mouse actions
   * @param menuTitle title of the sub menu
   * @param menuIcon icon of the sub menu
   * @param subItems sub menu items
   * @since 0.1
   */
  public static ActionMenu createActionMenu(final int actionId, final String menuTitle, final Icon menuIcon, final ActionMenu[] subItems) {
    return createActionMenu(actionId, menuTitle, menuIcon, subItems, false);
  }
  
  /**
   * Creates a menu item having sub menu items. These items can have
   * further sub menu items.
   *
   * @see ContextMenuAction
   *
   * @param actionId an ID to identify this action menu for mouse actions
   * @param menuTitle title of the sub menu
   * @param menuIcon icon of the sub menu
   * @param subItems sub menu items
   * @param showOnlySubMenus if the sub items of this menu should be shown directly in TV-Browser context menu
   * @since 0.1
   */
  public static ActionMenu createActionMenu(final int actionId, final String menuTitle, final Icon menuIcon, final ActionMenu[] subItems, final boolean showOnlySubMenu) {
    ActionMenu menu = new ActionMenu(menuTitle, menuIcon, subItems);
    
    if(TVBrowser.VERSION.compareTo(new Version(3,44,95,false)) >= 0) {
      try {
        final Constructor<ActionMenu> c = ActionMenu.class.getConstructor(int.class, String.class, Icon.class, ActionMenu[].class, boolean.class);
        
        menu = c.newInstance(actionId, menuTitle, menuIcon, subItems, showOnlySubMenu);
      } catch (Exception e) {
        // Ignore
        e.printStackTrace();
      }
    }
    
    return menu;
  }
}
