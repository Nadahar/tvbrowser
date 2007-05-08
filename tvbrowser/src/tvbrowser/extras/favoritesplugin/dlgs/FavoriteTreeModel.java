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
package tvbrowser.extras.favoritesplugin.dlgs;

import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * The model for the favorite tree.
 * 
 * @author René Mach
 * @since 2.6
 */
public class FavoriteTreeModel extends DefaultTreeModel {

  /**
   * Creates an instance of this class.
   * 
   * @param root The root node for this model. 
   */
  public FavoriteTreeModel(TreeNode root) {
    super(root, true);
  }
  
  public void reload(TreeNode node) {
    TreePath treePath = new TreePath(getPathToRoot(node));
    Enumeration e = null;

    if (treePath != null) {
      FavoriteTree t = FavoriteTree.getInstance();
      if (t != null)
        e = FavoriteTree.getInstance().getExpandedDescendants(treePath);
    }

    super.reload(node);

    if (e != null) {
      while (e.hasMoreElements()) {
        TreePath tree = (TreePath) e.nextElement();

        Object[] o = tree.getPath();

        for (int i = 1; i < o.length; i++) {
          TreeNode[] pathNodes = getPathToRoot((TreeNode) o[i]);

          if (node == null || pathNodes[0].toString().compareTo("FAVORITES_ROOT") != 0) {
            TreeNode n1 = (TreeNode) o[i - 1];
            Enumeration e1 = n1.children();

            while (e1.hasMoreElements()) {
              TreeNode n2 = (TreeNode) e1.nextElement();
              if (n2.toString().compareTo(o[i].toString()) == 0) {
                o[i] = n2;
                break;
              }
            }
          }
        }

        tree = new TreePath(o);
        FavoriteTree.getInstance().expandPath(tree);
      }
    }
  }
  
  public void reload() {
    reload(root);
  }


}
