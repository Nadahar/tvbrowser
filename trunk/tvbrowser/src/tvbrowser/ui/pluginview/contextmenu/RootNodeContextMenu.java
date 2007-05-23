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
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import tvbrowser.ui.pluginview.PluginTree;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 03.01.2005
 * Time: 22:43:03
 */
public class RootNodeContextMenu extends AbstractContextMenu {
  
  private TreePath mPath;

  public RootNodeContextMenu(PluginTree tree, TreePath path) {
    super(tree);
    mPath = path;
  }

  public JPopupMenu getPopupMenu() {
    JPopupMenu menu = new JPopupMenu();
    menu.add(getExpandAllMenuItem(mPath));
    menu.add(getCollapseAllMenuItem(mPath));
    return menu;
  }

  public Action getDefaultAction() {
    return null;
  }
}
