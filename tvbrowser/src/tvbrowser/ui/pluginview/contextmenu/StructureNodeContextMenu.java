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

import tvbrowser.ui.pluginview.PluginTree;
import util.ui.menu.MenuUtil;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 06.01.2005
 * Time: 12:01:17
 */
public class StructureNodeContextMenu extends AbstractContextMenu {

  private Action mDefaultAction;
  private TreePath mPath;
  
  public StructureNodeContextMenu(PluginTree tree, TreePath path) {
    super(tree);
    mDefaultAction = getCollapseExpandAction(path);
    mPath = path;
  }

  public JPopupMenu getPopupMenu() {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem item = new JMenuItem(mDefaultAction);
    item.setFont(MenuUtil.CONTEXT_MENU_BOLDFONT);
    menu.add(item);
    menu.add(getExpandAllMenuItem(mPath));
    menu.add(getFilterMenuItem(mPath));
    menu.add(getExportMenu( mPath));
    return menu;
  }

  public Action getDefaultAction() {
    return null;
  }

}
