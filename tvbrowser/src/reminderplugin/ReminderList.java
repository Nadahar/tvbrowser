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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.awt.event.*;

import util.io.IOUtilities;

import devplugin.Plugin;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.Date;
import devplugin.ProgramItem;


/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ReminderList implements ActionListener {

  private ReminderTimerListener mListener=null;
  private javax.swing.Timer mTimer;
  private PluginTreeNode mRoot;
  

  
  public ReminderList(PluginTreeNode root) {
    mRoot = root;
    ProgramItem[] items = root.getProgramItems();
    for (int i=0; i<items.length; i++) {
      Program prog = items[i].getProgram();
      if (prog == null) {
        throw new NullPointerException("ups");
      }
    }
  }

  
  public void read(ObjectInputStream in)
    throws IOException, ClassNotFoundException {
     
    int version = in.readInt();
    if (version == 1) {      
      int size = in.readInt();    
      for (int i = 0; i < size; i++) {
        in.readInt();   // read version
        int reminderMinutes = in.readInt();
        Date programDate = new Date(in);
        String programId = (String) in.readObject();
        Program program = Plugin.getPluginManager().getProgram(programDate, programId);
        
        // Only add items that were able to load their program
        if (program != null) {
          add(program, reminderMinutes);
        }
      }
    }
  }  
  
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2); // version   
  }

  public void add(Program[] programs, int minutes) {
    for (int i=0; i<programs.length; i++) {
      if (!mRoot.contains(programs[i], true) && (!programs[i].isExpired())) {
        ReminderListItem item = new ReminderListItem(programs[i], minutes);
        mRoot.addProgram(item.getProgramItem());
      }
      
    }
    mRoot.update();
  }

  public void add(Program program, int minutes) {
    if (!mRoot.contains(program, true)&& (!program.isExpired())) {
      ReminderListItem item = new ReminderListItem(program, minutes);
      mRoot.addProgram(item.getProgramItem());
      mRoot.update();
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
    ProgramItem[] items = mRoot.getProgramItems();
    for (int i=0; i<items.length; i++) {
      if (items[i].getProgram().isExpired()) {
        mRoot.removeProgram(items[i]);
      }
    }
    mRoot.update();
  }
 
  
  public void remove(ProgramItem item) {
    mRoot.removeProgram(item);
    mRoot.update();
  }
  
  public boolean contains(Program program) {
    return mRoot.contains(program, true);
  }
  
 
 
  
  public void remove(Program program) {
       
    ProgramItem[] items = mRoot.getProgramItems();
    for (int i=0; i<items.length; i++) {
      if (program.equals(items[i].getProgram())) {
        remove(items[i]);
      }
    }
    mRoot.update();
  }


  public ReminderListItem getReminderItem(Program prog) {
    ProgramItem[] items = mRoot.getProgramItems();
    for (int i=0; i<items.length; i++) {
      if (items[i].getProgram().equals(prog)) {
        return new ReminderListItem(items[i]);
      }
    }
    return null;
  }

  public ReminderListItem[] getReminderItems() {
     ProgramItem[] items = mRoot.getProgramItems();
     ReminderListItem[] result = new ReminderListItem[items.length];
     for (int i=0; i<items.length; i++) {
       result[i] = new ReminderListItem(items[i]);
     }
     return result;
  }
  
  
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