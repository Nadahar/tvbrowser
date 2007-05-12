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
    super.reload(node);
    FavoriteNode parent = (FavoriteNode)node;
    Enumeration e = node.children();
    
    while(e.hasMoreElements()) {
      FavoriteNode child = (FavoriteNode)e.nextElement();
      
      if(child.isDirectoryNode())
        reload(child);
    }
    
    if(parent.wasExpanded())
      FavoriteTree.getInstance().expandPath(new TreePath(((DefaultTreeModel)FavoriteTree.getInstance().getModel()).getPathToRoot(node)));
    else
      FavoriteTree.getInstance().collapsePath(new TreePath(((DefaultTreeModel)FavoriteTree.getInstance().getModel()).getPathToRoot(node)));
  }
  
  public void reload() {
    reload(root);
  }
  
  public boolean isLeaf(Object nodeObject) {
    if (nodeObject instanceof FavoriteNode) {
      FavoriteNode node = (FavoriteNode) nodeObject;
      return node.getChildCount() == 0;
    }
    return super.isLeaf(nodeObject);
  }
}
