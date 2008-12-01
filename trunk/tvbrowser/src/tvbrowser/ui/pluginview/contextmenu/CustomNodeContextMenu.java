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



package tvbrowser.ui.pluginview.contextmenu;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import tvbrowser.ui.pluginview.Node;
import tvbrowser.ui.pluginview.PluginTree;
import util.ui.menu.MenuUtil;
import devplugin.ActionMenu;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 03.01.2005
 * Time: 22:08:17
 */
public class CustomNodeContextMenu extends AbstractContextMenu {

  private Action mDefaultAction;
  private ActionMenu[] mActionMenus;
  private TreePath mPath;
  
  public CustomNodeContextMenu(PluginTree tree, TreePath path, ActionMenu[] menus) {
    super(tree);
    if (((Node) path.getLastPathComponent()).getAllowsChildren()) {
      mDefaultAction = getCollapseExpandAction(path);
    }
    mActionMenus = menus;
    mPath = path;
    if (mActionMenus == null) {
      mActionMenus = new ActionMenu[]{};
    }
  }

  public JPopupMenu getPopupMenu() {
    JPopupMenu menu = new JPopupMenu();
    if (mDefaultAction != null) {
      JMenuItem defaultMI = new JMenuItem(mDefaultAction);
      menu.add(defaultMI);
      defaultMI.setFont(MenuUtil.CONTEXT_MENU_BOLDFONT);
    }

    if (((Node) mPath.getLastPathComponent()).getAllowsChildren()) {
      menu.add(getExpandAllMenuItem(mPath));
      menu.add(getCollapseAllMenuItem(mPath));
      menu.add(getFilterMenuItem(mPath));
      menu.add(getExportMenu(mPath));
    }

    if (mActionMenus.length>0) {
      if (mDefaultAction != null) {
        menu.addSeparator();
      }
      for (ActionMenu actionMenu : mActionMenus) {
        if (actionMenu.getAction() != mDefaultAction) {
          JMenuItem menuItem = MenuUtil.createMenuItem(actionMenu);
          if (menuItem == null) {
            menu.addSeparator();
          } else {
            menu.add(menuItem);
          }
        }
      }
    }
    return menu;
  }

  public Action getDefaultAction() {
    return null;
  }

}
