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

package tvbrowser.ui.pluginview;



import javax.swing.tree.DefaultTreeModel;

import devplugin.TreeLeaf;
import devplugin.TreeNode;


public class PluginTreeModel extends DefaultTreeModel {

 
  private TreeNodeImpl mRoot;
  private static PluginTreeModel mInstance;
    
  private PluginTreeModel() {
    super(null);
    mRoot = createRootNode("plugins", "Plugins");
    setRoot(mRoot);
  }
 
  public static PluginTreeModel getInstance() {
    if (mInstance == null) {
      mInstance = new PluginTreeModel();
    }
    return mInstance;
  }
 
  public TreeNodeImpl getPluginNode() {
    return mRoot;
  }
  
  
  public Object getRoot() {
    return mRoot;
  }
  
  private TreeNodeImpl createRootNode(String key, String title) {
    TreeNodeImpl n = new TreeNodeImpl(key, title);
    n.setModel(this);
    setRoot(n);
    return n;
  }
  
  public TreeNodeImpl createNode(String key, String title) {
    TreeNodeImpl n = new TreeNodeImpl(key, title);
    n.setModel(this);
    n.setParent(mRoot);
    insert(n, mRoot);
    return n;
  }
  
  public void addNode(TreeNode node, TreeNode parent) {
    insert(node, parent);
  }
  
  public void addItem(TreeLeaf item, TreeNode parent) {
    insert(item, parent);  
  }
  
  public void removeNode(TreeNode node) {
    removeNodeFromParent(node);  
  }
  
  public void removeItem(TreeLeaf item) {
    removeNodeFromParent(item);    
  }
  
 
  
  private void insert(TreeItem item, TreeNode parent) {
    this.insertNodeInto(item, parent, findInsertPosition(item, parent));
  }
  
  private int findInsertPosition(TreeItem item, TreeNode parent) {      
 
    if (parent.getChildCount()==0) {
      return 0;
    }
      
    int comp = 0;
    int lowBnd = 0;
    int upBnd = parent.getChildCount() - 1;
    int i;
    boolean found = false;
    for(;;) {
      if (upBnd == lowBnd) {
          
        if (upBnd == parent.getChildCount() - 1) {
          return upBnd+1;
        }
        return upBnd;
      }        
      i = (upBnd + lowBnd) / 2;
      javax.swing.tree.TreeNode n = parent.getChildAt(i);
      comp = item.compareTo(n);
      if (comp < 0) {
        upBnd = i;          
      }else {
        lowBnd = i+1;
      }
    }
    
    
  }
    
}