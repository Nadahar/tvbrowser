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

package util.ui.view;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;

public class Node {
 
  private Node mParent;
  private Component mLeafComponent;
  private Component mContent;
  private ArrayList mNodes;  
  private String mName;
  private ViewProperties mProperties;
  private View mView;
  private boolean mIsValid;
  
  private String mTitle;
  
  public Node(Node parent) {
    mParent = parent;
    mNodes = new ArrayList();
    if (mParent!=null) {
      mParent.add(this);
    }
    mIsValid = false;
  }
  
  private void markAsInvalid() {
    mIsValid = false;
    if (mParent!=null) {
      mParent.markAsInvalid();
    }
  }
   
  
  public Component getComponent() {
    if (mIsValid) {
      return mContent;
    }
      
    Node[] nodes = getNodes();
    if (nodes.length == 0) {
      if (mLeafComponent == null) {
        throw new RuntimeException("non-empty leaf expected");
      }
      mContent = mLeafComponent;
    }
    else if (nodes.length == 1) {
      return nodes[0].getComponent();
    }
    else {
      Component[] comps = new Component[nodes.length];
      for (int i=0; i<comps.length; i++) {
        comps[i] = nodes[i].getComponent();
      }
      View view = new SplitView();
      
      view.setComponents(comps);
      ViewProperties prop = getProperties();
      view.setProperties(prop);
      mContent = view.getContent();
    }    
    mIsValid = true;
    return mContent;
  }
  
  public void setProperties(ViewProperties prop) {
    mProperties = prop;
  }
  
  public ViewProperties getProperties() {
    return mProperties;
  }
  
  public void setLeaf(Component comp) {
    markAsInvalid();
    mLeafComponent = comp;
  }
  
  public Component getLeaf() {
    return mLeafComponent;
  }
  
  public void add(Node node) {
    mNodes.add(node);
  }
    
  /**
   * 
   * @return an array of nodes with at least one leaf (components)
   */
  public Node[] getNodes() {
    ArrayList nodesList = new ArrayList();
    Iterator it = mNodes.iterator();
    while (it.hasNext()) {
      Node n = (Node)it.next();
      if (n.hasComponent()) {
        nodesList.add(n);
      }
    }
    Node[] result = new Node[nodesList.size()];
    nodesList.toArray(result);
    return result;
      
  }
  
  public boolean hasComponent() {
    if (getLeaf()!=null) {
      return true;
    }
    Iterator it = mNodes.iterator();
    while (it.hasNext()) {
      Node n = (Node)it.next();
      if (n.hasComponent()) {
        return true;
      }
    }
    return false;
    
  }
  
}