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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.lang.reflect.Array;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.*;

import tvbrowser.ui.pluginview.PluginTreeModel;
import tvbrowser.ui.pluginview.Node;



public class PluginTreeNode {

  private int mNodeType;
  private ArrayList mChildNodes;
  private Object mObject;


private Node mDefaultNode;

  private PluginTreeNode(int type, Object o) {
    mChildNodes = new ArrayList();
    mNodeType = type;
    mObject = o;
    mDefaultNode = new Node(type, mObject);
  }

  public PluginTreeNode(String title) {
    this(Node.CUSTOM_NODE, title);
  }

  public PluginTreeNode(Plugin plugin) {
    this(Node.PLUGIN_ROOT, plugin);
  }

  public PluginTreeNode(ProgramItem item) {
    this(Node.PROGRAM, item);
    mDefaultNode.setAllowsChildren(false);

  }

  public Node getMutableTreeNode() {
    return mDefaultNode;
  }

  public void addActionMenu(ActionMenu menu) {
    mDefaultNode.addActionMenu(menu);
  }


  public void removeAllActions() {
    mDefaultNode.removeAllActionMenus();
  }

  public void addAction(Action action) {
    addActionMenu(new ActionMenu(action));
  }

  public ActionMenu[] getActionMenus() {
    return mDefaultNode.getActionMenus();
  }



  private void switchToSortByDateView() {
    Map dateMap = new HashMap();
    mDefaultNode.removeAllChildren();

    Iterator it = mChildNodes.iterator();
    while (it.hasNext()) {
      PluginTreeNode n = (PluginTreeNode)it.next();
      if (!n.isLeaf()) {
        n.switchToSortByDateView();
        mDefaultNode.add(n.getMutableTreeNode());
      }
      else {
        ProgramItem progItem = (ProgramItem)n.getUserObject();
        Date date = progItem.getProgram().getDate();
        ArrayList list = (ArrayList)dateMap.get(date);
        if (list == null) {
          list = new ArrayList();
          dateMap.put(date, list);
        }
        list.add(progItem);
      }
    }

    // Create the new nodes
    Set keySet = dateMap.keySet();
    Date[] dates = new Date[keySet.size()];
    keySet.toArray(dates);
    Arrays.sort(dates);
    Date today = Date.getCurrentDate();
    Date nextDay = today.addDays(1);
    for (int i=0; i<dates.length; i++) {
      String dateStr;
      if (today.equals(dates[i])) {
        dateStr = "heute";
      }
      else if (nextDay.equals(dates[i])) {
        dateStr = "morgen";
      }
      else {
        dateStr = dates[i].toString();
      }

      Node node = new Node(Node.STRUCTURE_NODE, dateStr);
      mDefaultNode.add(node);
      List list = (List)dateMap.get(dates[i]);
      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        ProgramItem progItem = (ProgramItem)iterator.next();
        node.add(new Node(progItem));
      }
    }
  }


  public Object getUserObject() {
    return mObject;
  }

  public void removeAllChildren() {
    mChildNodes.clear();
    mDefaultNode.removeAllChildren();
  }


  public void add(PluginTreeNode node) {
    mChildNodes.add(node);
  }

  public boolean contains(Program prog, boolean recursive) {
    PluginTreeNode node = findProgramTreeNode(prog, recursive);
    return node != null;
  }

  public boolean contains(Program prog) {
    return contains(prog, false);
  }

  public void update() {

    switchToSortByDateView();
    PluginTreeModel.getInstance().reload(mDefaultNode);

  }


  public PluginTreeNode addProgram(Program program) {
    return addProgram(new ProgramItem(program));
  }
  
  public PluginTreeNode addProgram(ProgramItem item) {
    PluginTreeNode node = new PluginTreeNode(item);
    add(node);
    return node;
  }


  private PluginTreeNode findProgramTreeNode(PluginTreeNode root, Program prog, boolean recursive) {
    Iterator it = root.mChildNodes.iterator();
    while (it.hasNext()) {
      PluginTreeNode node = (PluginTreeNode)it.next();
      //if (node.mNodeType == TYPE_NODE) {
      if (!node.isLeaf()) {
        if (recursive) {
          PluginTreeNode n = findProgramTreeNode(node, prog, recursive);
          if (n!=null) {
            return n;
          }
        }
      }
      else { //if (node.mNodeType == TYPE_PROGRAM) {
        ProgramItem item = (ProgramItem)node.getUserObject();
        if (item.getProgram().equals(prog)) {
          return node;
        }
      }
    }
    return null;
  }

  private PluginTreeNode findProgramTreeNode(Program prog, boolean recursive) {
    return findProgramTreeNode(this, prog, recursive);
  }

  
  public void removeProgram(ProgramItem item) {
    removeProgram(item.getProgram());
  }

  public void removeProgram(Program program) {
    PluginTreeNode node = findProgramTreeNode(program, false);
    if (node != null) {
      mChildNodes.remove(node);
    }
  }

  public PluginTreeNode addNode(String title) {
    PluginTreeNode node = new PluginTreeNode(title);
    add(node);
    return node;
  }
  
  public ProgramItem[] getProgramItems() {
    ArrayList list = new ArrayList();
    Iterator it = mChildNodes.iterator();
    while (it.hasNext()) {
      PluginTreeNode n = (PluginTreeNode)it.next();
      //if (n.mNodeType == TYPE_PROGRAM) {
      if (n.isLeaf()) {
        list.add(n.getUserObject());
      }
    }

    ProgramItem[] result = new ProgramItem[list.size()];
    list.toArray(result);
    return result;    
  }

  public Program[] getPrograms() {
    ArrayList list = new ArrayList();
    Iterator it = mChildNodes.iterator();
    while (it.hasNext()) {
      PluginTreeNode n = (PluginTreeNode)it.next();
      if (n.isLeaf()) {
        ProgramItem item = (ProgramItem)n.getUserObject();
        list.add(item.getProgram());
      }
    }

    Program[] result = new Program[list.size()];
    list.toArray(result);
    return result;
  }


  public void store(ObjectOutputStream out) throws IOException {
    int childrenCnt = mChildNodes.size();
    out.writeInt(childrenCnt);

    for (int i=0; i<childrenCnt; i++) {
      //PluginTreeNode n = (PluginTreeNode)getChildAt(i);
      PluginTreeNode n = (PluginTreeNode)mChildNodes.get(i);
      out.writeInt(n.mNodeType);
      //if (n.mNodeType == TYPE_NODE) {
      if (!n.isLeaf()) {
        String title = (String)n.getUserObject();
        out.writeObject(title);
      }
      else {
        ProgramItem item = (ProgramItem)n.getUserObject();
        item.write(out);
      }
      n.store(out);            
    }
  }
  
  public void load(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int cnt = in.readInt();
    for (int i=0; i<cnt; i++) {
      int type = in.readInt();
      PluginTreeNode n = null;
      if (type == Node.PROGRAM) {
        ProgramItem item = new ProgramItem();
        item.read(in);
        n = new PluginTreeNode(item);
        if (item.getProgram() != null) {
          add(n);
        }
      }
      else {
        String title = (String)in.readObject();
        n = new PluginTreeNode(title);
        add(n);
      }
      n.load(in);

    }
  }


  public int size() {
    return mChildNodes.size();
  }

  public void clear() {
    mChildNodes.clear();
  }

  public boolean isEmpty() {
    return mChildNodes.isEmpty();
  }

  public boolean isLeaf() {
    return (mDefaultNode.getType() == Node.PROGRAM);
  }

   /*
  class Node extends DefaultMutableTreeNode {

    public Node(Object o) {
      super(o);
    }

    public boolean isLeaf() {
      return !getAllowsChildren();
    }

  }  */

}