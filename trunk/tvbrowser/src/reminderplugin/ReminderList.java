/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
import java.util.*;
import java.awt.event.*;

import util.io.IOUtilities;
import devplugin.Program;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ReminderList implements ActionListener {

  private ArrayList mList;
  private ReminderTimerListener mListener=null;
  private javax.swing.Timer mTimer;


  public ReminderList() {
    mList = new ArrayList();
  }

  
  
  public ReminderList(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    int version = in.readInt();
    
    int size = in.readInt();
    
    mList = new ArrayList(size);
    for (int i = 0; i < size; i++) {
      ReminderListItem item = new ReminderListItem(in);
      add(item.getProgram(), item.getReminderMinutes());
    }
  }
  
  
  
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    
    out.writeInt(mList.size());
    for (int i = 0; i < mList.size(); i++) {
      ReminderListItem item = (ReminderListItem) mList.get(i);
      item.writeData(out);
    }
  }
  
  
  
  /**
   * Searches the list for an item with the specified program.
   * <p>
   * If there is no such item, null is returned.
   */
  private ReminderListItem getItemWithProgram(Program program) {
    Iterator iter = mList.iterator();
    while (iter.hasNext()) {
      ReminderListItem item = (ReminderListItem) iter.next();
      if (item.getProgram().equals(program)) {
        return item;
      }
    }
    
    return null;
  }

  
  
  public void add(Program program, int minutes) {
    ReminderListItem item = getItemWithProgram(program);
    if (item == null) {
      item = new ReminderListItem(program, minutes);
      if (! item.isExpired()) {
        mList.add(item);
        program.mark(ReminderPlugin.getInstance());
      }
    } else {
      item.setReminderMinutes(minutes);
    }
  }

  
  
  public void remove(ReminderListItem item) {
    mList.remove(item);
    item.getProgram().unmark(ReminderPlugin.getInstance());
  }
  
  
  
  public void remove(Program program) {
    Iterator itemIter = mList.iterator();
    while (itemIter.hasNext()) {
      ReminderListItem item = (ReminderListItem) itemIter.next();
      if (item.getProgram().equals(program)) {
        itemIter.remove();
        program.unmark(ReminderPlugin.getInstance());
        return;
      }
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
    Iterator itemIter = mList.iterator();
    while (itemIter.hasNext()) {
      ReminderListItem item = (ReminderListItem) itemIter.next();
      
      if (item.isExpired()) {
        itemIter.remove();
      }
    }
  }

  
  
  public Iterator getReminderItems() {
    final Object[] obj=mList.toArray();
    Arrays.sort(obj);
    return new Iterator() {
      private int pos=0;
      public boolean hasNext() {
        return (pos<obj.length);
      }

      public Object next() {
        return obj[pos++];
      }

      public void remove() {}
    };
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

    Iterator it = this.getReminderItems();
    while (it.hasNext()) {
      ReminderListItem item = (ReminderListItem) it.next();
      if (item.getReminderMinutes() < 0) {
        continue;
      }

      int m = item.getProgram().getMinutes();
      int h = item.getProgram().getHours();
      int d = item.getReminderMinutes();

      int diff = today.compareTo(item.getProgram().getDate());

      if (diff > 0) {
        mListener.timeEvent(item);
      } else {
        int currentMinutesAfterMidnight = IOUtilities.getMinutesAfterMidnight();
        int remindMinutesAfterMidnight = h * 60 + m - d;

        if (diff < 0) {
          currentMinutesAfterMidnight -= 1440;
        }

        if (currentMinutesAfterMidnight >= remindMinutesAfterMidnight) {
          mListener.timeEvent(item);
        }
      }
    }
  }
  
}