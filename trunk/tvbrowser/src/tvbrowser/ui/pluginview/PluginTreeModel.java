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


import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import tvbrowser.core.plugin.PluginProxy;


public class PluginTreeModel extends DefaultTreeModel {

  private static PluginTreeModel sInstance; 
    
  private PluginTreeModel() {
    super(new DefaultMutableTreeNode("Plugins"));        
  }
  
  
  public void addPluginTree(PluginProxy plugin) {
    MutableTreeNode pluginRoot = plugin.getRootNode();
    MutableTreeNode root = (MutableTreeNode)this.getRoot();
    root.insert(pluginRoot, 0);
  }
    
  public static PluginTreeModel getInstance() {
    if (sInstance == null) {
      sInstance = new PluginTreeModel();
    }
    return sInstance;
  }
    
    
}


/*
public class PluginTreeModel implements TreeModel, ProgramContainerListener {

  private PluginRootNode[] mPluginRootNodes;
  private String mRoot; 
  private ArrayList mTreeModelListeners;
    
  public PluginTreeModel(PluginProxy[] plugins) {
    mTreeModelListeners = new ArrayList();
    ArrayList pluginRootNodes = new ArrayList();
    //mPluginRootNodes = new PluginRootNode[plugins.length];
    for (int i=0; i<plugins.length; i++) {
      if (plugins[i].canUseProgramTree()) {  
        PluginRootNode n = new PluginRootNode(plugins[i], PluginRootNode.VIEW_TYPE_DEFINED_BY_PLUGIN);
        pluginRootNodes.add(n);
        ProgramContainer container = Plugin.getPluginManager().getProgramContainer(plugins[i].getId());
        container.addContainerListener(this);
      }
    }
    mPluginRootNodes = new PluginRootNode[pluginRootNodes.size()];
    pluginRootNodes.toArray(mPluginRootNodes);
    mRoot = "Plugins";
  }
    
  public Object getRoot() {
   return mRoot;
  }

  
  public int getChildCount(Object node) {
    if (node == mRoot) {
      return mPluginRootNodes.length;
    }
    else {
      PluginRootNode n = (PluginRootNode)node;
      return n.getChildCount();
    }
    
  }

  
  public boolean isLeaf(Object node) {
    if (node == mRoot) {
      return false;
    }
    else if (node instanceof PluginRootNode) {
      return false;
    }
    return true;
  }

  
  
   
  public void addTreeModelListener(TreeModelListener listener) {
    mTreeModelListeners.add(listener);   
  }

  public void removeTreeModelListener(TreeModelListener listener) {
    mTreeModelListeners.remove(listener);       
  }

   
  public Object getChild(Object node, int index) {
    if (node == mRoot) {
      return mPluginRootNodes[index];
    }
    else if (node instanceof TreeNode) {
      TreeNode n = (TreeNode)node;
      return n.getChildAt(index);
    }
    return null;
  }

   
  public int getIndexOfChild(Object arg0, Object arg1) {
    return 0;
  }

   
  public void valueForPathChanged(TreePath arg0, Object arg1) {
        
  }
  


  public void programAdded(ProgramItem item) {
      
  }

 
  public void programRemoved(ProgramItem item) {
      
  }

  
  public void containerAdded(ProgramContainer parent, ProgramContainer container) {
      
  }

 
  public void containerRemoved(ProgramContainer container) {
      
  }

  

  
  class PluginRootNode implements devplugin.TreeNode{
    
    public static final int VIEW_TYPE_DEFINED_BY_PLUGIN = 1;
    public static final int VIEW_TYPE_SORTED_BY_TITLE = 2;
    public static final int VIEW_TYPE_SORTED_BY_NAME = 3;
    public static final int VIEW_TYPE_NO_SUBFOLDERS = 4;
    
    private PluginProxy mPlugin;
    private int mViewType;
    private TreeNode[] mNodes;
     
    public PluginRootNode(PluginProxy plugin, int type) {
      mPlugin = plugin;
      
      ProgramContainer container = Plugin.getPluginManager().getProgramContainer(plugin.getId());
      //ProgramItem[] items = container.getPrograms();
      
      
      mViewType = type;
      
      if (type == VIEW_TYPE_DEFINED_BY_PLUGIN) {
        mNodes = createPluginDefinedStructure(container);
      }
      else if (type == VIEW_TYPE_NO_SUBFOLDERS) {
     //   mNodes = createNoSubfolderStructure(container);
      }
      else if (type == VIEW_TYPE_SORTED_BY_TITLE) {
     //   mNodes = createSortedByTitleStructure(container);
      }
      else if (type == VIEW_TYPE_SORTED_BY_NAME) {
     //   mNodes = createSortedByNameStructure(container);
      }
    }
    
    public String toString() {
      return mPlugin.getInfo().getName();
    }
    
    public int getViewType() {
      return mViewType;  
    }
    
    public Object getChildAt(int index) {
      //if (mViewType == VIEW_TYPE_DEFINED_BY_PLUGIN)
      //return null;
      return mNodes[index];
    }
    
    public int getChildCount() {
      return mNodes.length;
    }

  
    public boolean isChild() {
      return false;
    }
    
    
    private TreeNode[] createPluginDefinedStructure(ProgramContainer container) {
      ProgramItem[] progs = container.getPrograms();
      TreeNode[] result = new TreeNode[progs.length];
      for (int i=0; i<progs.length; i++) {
        result[i] = new Node(progs[i].getProgram().getTitle());
      }        
      return result;
    }
    
    private TreeNode[] createSortedByTitleNode(Program[] progs) {
      return new TreeNode[0];   
    }
    
    private TreeNode[] createSortedByTimeNode(Program[] progs) {
      return new TreeNode[0];   
    }
    
  }





    
}
*/
