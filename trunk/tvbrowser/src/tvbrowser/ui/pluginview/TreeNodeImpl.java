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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;

import devplugin.Program;
import devplugin.TreeNode;
import devplugin.TreeLeaf;
import javax.swing.tree.DefaultMutableTreeNode;


public class TreeNodeImpl extends DefaultMutableTreeNode implements TreeNode {
    
  private PluginTreeModel mModel;  
  private String mKey, mTitle;  
  private TreeNode mParent;
  private ArrayList mNodes, mLeafs; 
  
  private TreeNodeImpl(PluginTreeModel model, TreeNode parent, String key, String title) {
    super(title);
    mModel = model;
    mParent = parent;
    mKey = key;
    mTitle = title;
    mNodes = new ArrayList();
    mLeafs = new ArrayList();
  }
  
  public TreeNodeImpl(String key, String title) {
    this(null, null, key, title);  
  }
  
  public String getKey() {
    return mKey;
  }
  
  public String getTitle() {
    return mTitle;
  }
  
  public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int numOfItems = in.readInt();
    for (int i=0; i<numOfItems; i++) {
      TreeLeafImpl leaf = new TreeLeafImpl();
      leaf.read(in);
      add(leaf); 
    }

    int numOfNodes = in.readInt();
    for (int i=0; i<numOfNodes; i++) {
      String id = (String)in.readObject();
      String title = (String)in.readObject(); 
        
      TreeNodeImpl n = new TreeNodeImpl(mModel, this, id, title);
      n.read(in);
      add(n);
    }
  }
    
  public void write(ObjectOutputStream out) throws IOException {
    TreeNodeImpl[] nodes = (TreeNodeImpl[])getNodes();
    TreeLeafImpl[] items = (TreeLeafImpl[])getLeafs();
    out.writeInt(items.length);
    for (int i=0; i<items.length; i++) {
      items[i].write(out);
    }
    out.writeInt(nodes.length);
    for (int i=0; i<nodes.length; i++) {
      out.writeObject(nodes[i].getKey());
      out.writeObject(nodes[i].getTitle());
      nodes[i].write(out);
    } 
  }
    
  
  public TreeNode createNode(String key, String title) {
    TreeNodeImpl node = new TreeNodeImpl(mModel, this, key, title);
    add(node);
    return node;  
  }
  
  public void add(TreeNodeImpl node) {
    node.setModel(mModel);
    node.setParent(this);
    mNodes.add(node); 
    mModel.addNode(node, this);
  }
  
  private void add(TreeLeaf item) {
    mLeafs.add(item);
    mModel.addItem(item, this);
  }
  public void add(Program program) {
    add(new TreeLeafImpl(program));
  }
  
  public void remove(TreeNode node) {
    mNodes.remove(node);
    mModel.removeNode(node);
  }
  
  public void remove(TreeLeaf item) {
    mLeafs.remove(item);
    mModel.removeItem(item);
  }
  
  public TreeLeaf[] getLeafs() {
    TreeLeafImpl[] result = new TreeLeafImpl[mLeafs.size()];
    mLeafs.toArray(result);
    return result;
  }
  
  public TreeNode[] getNodes() {
    TreeNodeImpl[] result = new TreeNodeImpl[mNodes.size()];
    mNodes.toArray(result);
    return result;
  }
  
  public String toString() {
    return mTitle;
  }


  public void setModel(PluginTreeModel model) {
    mModel = model;
    TreeNodeImpl[] nodes = (TreeNodeImpl[])getNodes();
    for (int i=0; i<nodes.length; i++) {
      nodes[i].setModel(model);
    }
  }


  public void setParent(TreeNode node) {
     mParent = node;
     TreeNode[] nodes = getNodes();
     for (int i=0; i<nodes.length; i++) {
       nodes[i].setParent(node);
     }
   }
    
}