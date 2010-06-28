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

package devplugin;

import java.util.ArrayList;

import javax.swing.Action;


/**
 * This class represents a structure for context menus.
 */
public class ActionMenu {

  private Action mAction;
  private ActionMenu[] mSubItems;
  private boolean mIsSelected;

  /**
   * Creates a menu item having sub menu items. Theses items can have
   * further sub menu items.
   *
   * @see ContextMenuAction
   *
   * @param action
   * @param subItems
   */
  public ActionMenu(Action action, ActionMenu[] subItems) {
    mAction = action;
    mSubItems = subItems;
  }

  /**
   * Creates a menu item having sub menu items.
   * @param action
   * @param subItems
   */
  public ActionMenu(Action action, Action[] subItems) {
    mAction = action;
    mSubItems = new ActionMenu[subItems.length];
    for (int i=0; i<mSubItems.length; i++) {
      mSubItems[i] = new ActionMenu(subItems[i]);
    }
  }

  /**
   * Creates a menu item having sub menu items.
   * @param action
   * @param subItems sub menu items, can be Actions or ActionMenus in mixed order
   */
  public ActionMenu(Action action, Object[] subItems) {
    mAction = action;
    ArrayList<ActionMenu> subMenus = new ArrayList<ActionMenu>();
    for (Object subItem : subItems) {
      if (subItem instanceof Action) {
        subMenus.add(new ActionMenu((Action) subItem));
      }
      else if (subItem instanceof ActionMenu) {
        subMenus.add((ActionMenu) subItem);
      }
    }
    mSubItems = new ActionMenu[subMenus.size()];
    subMenus.toArray(mSubItems);
  }

  /**
   * Creates a new single checkbox menu entry.
   * @param action
   * @param isSelected state of the check box (checked/unchecked)
   */
  public ActionMenu(Action action, boolean isSelected) {
    this(action,(ActionMenu[])null);
    mIsSelected = isSelected;
  }

  /**
   * Creates a new single menu entry
   * @param action
   */
  public ActionMenu(Action action) {
    this(action, false);
  }

  /**
   * Clone an existing ActionMenu
   * @param actionMenu clone this ActionMenu
   */
  public ActionMenu(ActionMenu actionMenu) {
    this(actionMenu.getAction(), actionMenu.getSubItems());
    mIsSelected = actionMenu.isSelected();
  }

  public String getTitle() {
    return mAction.getValue(Action.NAME).toString();
  }

  public ActionMenu[] getSubItems() {
    return mSubItems;
  }

  public boolean hasSubItems() {
    return mSubItems!=null;
  }


  public boolean isSelected() {
    return mIsSelected;
  }

  public Action getAction() {
    return mAction;
  }

}
