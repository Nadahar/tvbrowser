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
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import tvbrowser.ui.pluginview.PluginTreeModel;



public class PluginTreeNode extends DefaultMutableTreeNode {

  private static int TYPE_NODE = 1;
  private static int TYPE_PROGRAM = 2;
  
  private int mType;
    
  public PluginTreeNode(String title) {
    super(title);   
    mType = TYPE_NODE;
  }
    
  public PluginTreeNode(ProgramItem item) {
    super(item);
    mType = TYPE_PROGRAM;
  }
  
  public void update() {
    PluginTreeModel.getInstance().reload(this);   
  }
  
  public PluginTreeNode addProgram(Program program) {
    return addProgram(new ProgramItem(program));
  }
  
  public PluginTreeNode addProgram(ProgramItem item) {
    PluginTreeNode node = new PluginTreeNode(item);
    add(node);
    update();
    return node;
  }
  
  private PluginTreeNode getProgramTreeNode(ProgramItem item) {
    Enumeration enum = children();
    while (enum.hasMoreElements()) {
      PluginTreeNode n = (PluginTreeNode)enum.nextElement();
      if (n.mType == TYPE_PROGRAM) {
        if (item.equals(n.getUserObject())) {
          return n;
        }
      }
    }
    return null;
  }
  
  public void removeProgram(ProgramItem item) {
    PluginTreeNode node = getProgramTreeNode(item);
    if (node != null) {
      remove(node);
      update();
    }
  }
  
  public PluginTreeNode addNode(String title) {
    PluginTreeNode node = new PluginTreeNode(title);
    add(node);
    update();
    return node;
  }
  
  public ProgramItem[] getPrograms() {
    ArrayList list = new ArrayList();
    Enumeration enum = children();
    while (enum.hasMoreElements()) {
      PluginTreeNode n = (PluginTreeNode)enum.nextElement();
      if (n.mType == TYPE_PROGRAM) {
        list.add(n.getUserObject());
      }
    }
    ProgramItem[] result = new ProgramItem[list.size()];
    list.toArray(result);
    return result;    
  }
  
  
  public void store(ObjectOutputStream out) throws IOException {
    int childrenCnt = getChildCount();
    out.writeInt(childrenCnt);
    for (int i=0; i<childrenCnt; i++) {
      PluginTreeNode n = (PluginTreeNode)getChildAt(i);
      out.writeInt(n.mType);
      if (n.mType == TYPE_NODE) {
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
      if (type == TYPE_NODE) {
        String title = (String)in.readObject();
        n = new PluginTreeNode(title);
        add(n);
      }
      else if (type == TYPE_PROGRAM) {
        ProgramItem item = new ProgramItem();
        item.read(in);
        n = new PluginTreeNode(item);
        if (item.getProgram() != null) {
          add(n);
        }
      }
      else {
        throw new IOException("invalid type: "+type);
      }

      n.load(in);

    }
  }
    
}