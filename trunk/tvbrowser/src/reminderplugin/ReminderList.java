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

package reminderplugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.awt.event.*;

import util.io.IOUtilities;

import devplugin.Plugin;
import devplugin.Program;
import devplugin.Date;
import devplugin.TreeNode;
import devplugin.TreeLeaf;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ReminderList implements ActionListener {

  private ReminderTimerListener mListener=null;
  private javax.swing.Timer mTimer;
  private TreeNode mRootNode;


  public ReminderList(TreeNode node) {
    mRootNode = node;
    
    TreeLeaf[] leafs = node.getLeafs();
    for (int i=0; i<leafs.length; i++) {
      leafs[i].getProgram().mark(ReminderPlugin.getInstance());
    }
  }

  
  public void read(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    TreeNode tree = Plugin.getPluginManager().getTree(ReminderPlugin.getInstance().getId());        
    
      
    int version = in.readInt();
    if (version == 1) {      
      int size = in.readInt();    
      for (int i = 0; i < size; i++) {
        int v = in.readInt();
        int reminderMinutes = in.readInt();
        Date programDate = new Date(in);
        String programId = (String) in.readObject();
        Program program = Plugin.getPluginManager().getProgram(programDate, programId);
        
        // Only add items that were able to load their program
        if (program != null) {
          TreeLeaf leaf = tree.add(program);
          leaf.setProperty("reminderminutes",""+reminderMinutes);
          add(program, reminderMinutes);
        }
      }
    }
  }
  
  
  
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2); // version
  }
  
  
  
  /**
   * Searches the list for an item with the specified program.
   * <p>
   * If there is no such item, null is returned.
   */
  public ReminderListItem getItemWithProgram(Program program) {
    return getItemWithProgram(mRootNode, program);
  }
  
  private ReminderListItem getItemWithProgram(TreeNode node, Program program) {
    TreeLeaf[] leafs = node.getLeafs();
    for (int i=0; i<leafs.length; i++) {
      TreeLeaf leaf = leafs[i];
      if (leaf.getProgram().equals(program)) {
        return new ReminderListItem(leaf);
      }      
    }
    
    TreeNode[] nodes = node.getNodes();
    for (int i=0; i<nodes.length; i++) {
      ReminderListItem item = getItemWithProgram(nodes[i], program);
      if (item != null) {
        return item;
      }
    }
    return null; 
  }

  
  public void add(TreeNode node, Program program, int minutes) {
    if (!program.isExpired()) {
      ReminderListItem item = this.getItemWithProgram(program);
      // create a new entry
      if (item == null) {        
        TreeLeaf leaf = node.add(program);   
        item = new ReminderListItem(leaf);
        program.mark(ReminderPlugin.getInstance());        
      }
      item.setMinutes(minutes);
    }      
  }
  public void add(Program program, int minutes) {
    add(mRootNode, program, minutes);  
  }
  
 
  public void remove(Program program) {
    remove(mRootNode, program);
  }
  
  private void remove(TreeNode node, Program program) {
    TreeLeaf[] leafs = node.getLeafs();
    for (int i=0; i<leafs.length; i++) {
      if (leafs[i].getProgram().equals(program)) {
        node.remove(leafs[i]);
        program.unmark(ReminderPlugin.getInstance());
      }
    }
    
    TreeNode[] nodes = node.getNodes();
    for (int i=0; i<nodes.length; i++) {
      remove(nodes[i], program);
    }
  }
 
  
  public void setReminderTimerListener(ReminderTimerListener listener) {
    this.mListener = listener;
    if (listener != null) {
      mTimer = new javax.swing.Timer(10000, this);
      mTimer.start();
    }
  }

  
  
  public void removeExpiredItems() {
    removeExpiredItems(mRootNode);
  }
  
  private void removeExpiredItems(TreeNode node) {
    TreeLeaf[] leafs = node.getLeafs();
    for (int i=0; i<leafs.length; i++) {
      if (leafs[i].getProgram().isExpired()) {
        mRootNode.remove(leafs[i]);
      }
    }
    TreeNode[] nodes = node.getNodes();
    for (int i=0; i<nodes.length; i++) {
      removeExpiredItems(nodes[i]);
    }
  }

  public boolean contains(Program program) {
    return contains(mRootNode, program);
  }
  
  private boolean contains(TreeNode node, Program program) {
    TreeLeaf[] leafs = node.getLeafs();
    for (int i=0; i<leafs.length; i++) {
      if (leafs[i].getProgram().equals(program)) {
        return true;
      }
    }
    
    TreeNode[] nodes = node.getNodes();
    for (int i=0; i<nodes.length; i++) {
      boolean result = contains(nodes[i], program);
      if (result) {
        return true;
      }
    }
    
    return false;  
  }
  
  
  public ReminderListItem[] getReminderItems() {
    Collection col = getReminderItems(mRootNode);
    ReminderListItem[] result = new ReminderListItem[col.size()];
    col.toArray(result);
    
    Arrays.sort(result);
    return result;
  }
  
  private Collection getReminderItems(TreeNode node) {
      TreeLeaf[] leafs = node.getLeafs();
      //ReminderListItem[] result = new ReminderListItem[leafs.length];
      Collection result = new ArrayList();
      
      for (int i=0; i<leafs.length; i++) {
        result.add(new ReminderListItem(leafs[i]));
      }
      
      TreeNode[] nodes = node.getNodes();
      for (int i=0; i<nodes.length; i++) {
        Collection c = getReminderItems(nodes[i]);
        result.addAll(c);
      }
      
      return result; 
  }
      
      
  // implements ActionListener
  
  
  public void actionPerformed(ActionEvent event) {
    if (mListener == null) {
      mTimer.stop();
      return;
    }

    Calendar cal = new GregorianCalendar();
    cal.setTime(new java.util.Date());

    devplugin.Date today = new devplugin.Date();

    ReminderListItem[] items = getReminderItems();
    for (int i=0; i<items.length; i++) {
      if (items[i].getMinutes() < 0) {
        continue;
      }
       
      Date remindDate = items[i].getProgram().getDate(); 
      int m = items[i].getProgram().getMinutes();
      int h = items[i].getProgram().getHours();
      int d = items[i].getMinutes();
      int remindTime = h*60+m - d;
      if (remindTime<0) {
        remindTime = -remindTime;
        int days = remindTime / 1440 +1;
        remindTime = 1440 - (remindTime % 1440);
        remindDate = remindDate.addDays(-days);
      }

			int diff = today.compareTo(remindDate);
			if (diff > 0 || (diff == 0 && IOUtilities.getMinutesAfterMidnight() >= remindTime)) {
        mListener.timeEvent(items[i]);
      }
    }
  }
  
  
  
}