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
import devplugin.Program;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ReminderList implements Serializable, ActionListener {

  private ArrayList list;
  transient private ReminderTimerListener listener=null;
  transient private javax.swing.Timer timer;


  public ReminderList() {
    list=new ArrayList();
  }

  public void add(ReminderListItem item) {
    list.add(item);
  }

  public void actionPerformed(ActionEvent event) {
    devplugin.Date date=new devplugin.Date();

    Calendar cal=new GregorianCalendar();
    cal.setTime(new java.util.Date());
    int time=cal.get(Calendar.HOUR_OF_DAY)*60+cal.get(Calendar.MINUTE);

    long minutesNow = (long)date.getDaysSince1970() * 60 * 24 + (long)time;
    long minutesItem;

    Iterator it=this.getReminderItems();
    while (it.hasNext()) {
      ReminderListItem item=(ReminderListItem)it.next();
      if (item.getReminderMinutes()<0) continue;

      int m=item.getProgram().getMinutes();
      int h=item.getProgram().getHours();
      int d=item.getReminderMinutes();

      minutesItem=item.getProgram().getDate().getDaysSince1970()*60*24 + h*60 + m;
      minutesItem-=item.getReminderMinutes();

      if (minutesItem==minutesNow) {
        listener.timeEvent(item);
      }
    }
  }
  
  

  public void setReminderTimerListener(ReminderTimerListener listener) {
    this.listener=listener;
    timer=new javax.swing.Timer(10000,this);
    timer.start();
  }

  public void remove(ReminderListItem item) {
    list.remove(item);
  }

  public void removeExpiredItems() {
    devplugin.Date date=new devplugin.Date();

    Calendar cal=new GregorianCalendar();
    cal.setTime(new java.util.Date(System.currentTimeMillis()));
    int time=cal.get(Calendar.HOUR_OF_DAY)*60+cal.get(Calendar.MINUTE);

    for (int i=0;i<list.size();i++) {
      ReminderListItem item=(ReminderListItem)list.get(i);
      Program program = item.getProgram();
      
      boolean remove = false;
      if (program == null) {
        remove = true;
      } else {
        int cmpRes = program.getDate().compareTo(date);
        if (cmpRes == -1) {
          remove = true;
        }
        else if (cmpRes==0) {
          if (program.getMinutes()+item.getProgram().getHours()*60<time) {
            remove = true;
          }
        }
      }
      
      if (remove) {
        list.remove(i);
        i--; // check this index another time
      }      
    }
  }

  public Iterator getReminderItems() {

    final Object[] obj=list.toArray();
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




}