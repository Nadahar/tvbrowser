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

package tvbrowser.extras.reminderplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.DefaultMarker;
import util.io.IOUtilities;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramItem;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class ReminderList implements ActionListener {

  private ReminderTimerListener mListener = null;
  private javax.swing.Timer mTimer;
  public static DefaultMarker MARKER = new DefaultMarker(
      "reminderplugin.ReminderPlugin", IconLoader.getInstance()
          .getIconFromTheme("apps", "appointment", 16), ReminderPlugin.mLocalizer.msg("pluginName","Reminder"), Program.DEFAULT_MARK_PRIORITY);

  private ArrayList<ReminderListItem> mList;

  /** List of Blocked Programs. These Programs don't trigger a reminder anymore */
  private ArrayList<Program> mBlockedPrograms = new ArrayList<Program>();

  public ReminderList() {
    mList = new ArrayList<ReminderListItem>();
  }

  public void read(ObjectInputStream in) throws IOException,
      ClassNotFoundException {

    int version = in.readInt();
    if (version == 1 || version >= 3) { // version == 2 ==> read from plugin
                                        // tree
      int size = in.readInt();
      for (int i = 0; i < size; i++) {
        in.readInt(); // read version
        int reminderMinutes = in.readInt();
        Date programDate = new Date(in);
        String programId = (String) in.readObject();
        Program program = Plugin.getPluginManager().getProgram(programDate,
            programId);
        
        int referenceCount = 1;

        if(version == 4)
          referenceCount = in.readInt();          

        // Only add items that were able to load their program
        if (program != null)
          add(program, reminderMinutes, referenceCount);
      }
    }
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(4); // version
    ReminderListItem[] items = getReminderItems();
    out.writeInt(items.length);
    for (int i = 0; i < items.length; i++) {
      out.writeInt(3);
      out.writeInt(items[i].getMinutes());
      Date date = items[i].getProgram().getDate();
      date.writeData(out);
      out.writeObject(items[i].getProgram().getID());
      out.writeInt(items[i].getReferenceCount());
    }
  }

  public void add(Program[] programs, int minutes) {
    for (int i = 0; i < programs.length; i++) {
      add(programs[i], minutes);
    }
  }

  public void add(Program program, int minutes) {
    add(program, minutes, 1);
  }
  
  private void add(Program program, int minutes, int referenceCount) {
    if (!program.isExpired()) {
      ReminderListItem item = getReminderItem(program);
      
      if (item != null) {
        item.incReferenceCount();
      } else {
        item = new ReminderListItem(program, minutes);
        item.setReferenceCount(referenceCount);
        mList.add(item);
        program.mark(MARKER);
      }
    }
  }

  /**
   * Only adds a Program if it's not blocked
   * 
   * @param programs
   *          Programs to add
   * @param minutes
   *          remind x Minutes before start
   */
  public void addAndCheckBlocked(Program[] programs, int minutes) {
    for (int i = 0; i < programs.length; i++) {
      if (!contains(programs[i]) && !mBlockedPrograms.contains(programs[i])
          && (!programs[i].isExpired())) {
        ReminderListItem item = new ReminderListItem(programs[i], minutes);
        mList.add(item);
        programs[i].mark(MARKER);
      }
      else if(contains(programs[i]))
        getReminderItem(programs[i]).incReferenceCount();
    }
  }

  public void setReminderTimerListener(ReminderTimerListener listener) {
    this.mListener = listener;
    
    if(ReminderPlugin.getInstance().isAllowedToStartTimer())
      startTimer();
  }
  
  protected void startTimer() {
    if (mListener != null && mTimer == null) {
      mTimer = new javax.swing.Timer(10000, this);
      mTimer.start();
    } else if (!mTimer.isRunning()) {
      mTimer.start();
    }    
  }

  public void removeExpiredItems() {
    ReminderListItem[] items = getReminderItems();
    for (int i = 0; i < items.length; i++) {
      if (items[i].getProgram().isExpired()) {
        remove(items[i]);
      }
    }
  }

  private void remove(ReminderListItem item) {
    item.decReferenceCount();
    if (item.getReferenceCount() < 1) {
      mList.remove(item);
      item.getProgram().unmark(MARKER);
    }
  }

  public void remove(ProgramItem item) {
    remove(item.getProgram());
  }

  public boolean contains(Program program) {
    ReminderListItem[] items = getReminderItems();
    for (int i = 0; i < items.length; i++) {
      if (items[i].getProgram().equals(program)) {
        return true;
      }
    }
    return false;
  }

  public void remove(Program program) {
    ReminderListItem[] items = getReminderItems();
    for (int i = 0; i < items.length; i++) {
      if (items[i].getProgram().equals(program)) {
        remove(items[i]);
      }
    }
  }
  
  public void removeWithoutChecking(ProgramItem item) {
    removeWithoutChecking(item.getProgram());
  }
  
  public void removeWithoutChecking(Program program) {
    ReminderListItem[] items = getReminderItems();
    for (int i = 0; i < items.length; i++) {
      if (items[i].getProgram().equals(program)) {
        mList.remove(items[i]);
        items[i].getProgram().unmark(MARKER);
      }
    }
  }  

  public ReminderListItem getReminderItem(Program program) {
    ReminderListItem[] items = getReminderItems();
    for (int i = 0; i < items.length; i++) {
      if (items[i].getProgram().equals(program)) {
        return items[i];
      }
    }

    return null;
  }

  public ReminderListItem[] getReminderItems() {
    ReminderListItem[] items = mList
        .toArray(new ReminderListItem[mList.size()]);
    Arrays.sort(items);
    return items;
  }

  /**
   * Checks all programs, if they currently exists
   * 
   * @return all remove programs
   */
  public Program[] updatePrograms() {
    ReminderListItem[] items = getReminderItems();
    mList.clear();
    ArrayList<Program> removedPrograms = new ArrayList<Program>();
    
    for (int i = 0; i < items.length; i++) {
      if(items[i].getProgram().getProgramState() == Program.WAS_DELETED_STATE)
        removedPrograms.add(items[i].getProgram());
      else if(items[i].getProgram().getProgramState() == Program.WAS_UPDATED_STATE) {
        Program p = items[i].getProgram();
        add(Plugin.getPluginManager().getProgram(p.getDate(), p.getID()),items[i].getMinutes(), items[i].getReferenceCount());
      }
      else
        mList.add(items[i]);
    }
    
    return removedPrograms.toArray(new Program[removedPrograms
        .size()]);
  }

  public void actionPerformed(ActionEvent event) {
    if (mListener == null) {
      mTimer.stop();
      return;
    }

    Calendar cal = new GregorianCalendar();
    cal.setTime(new java.util.Date());

    ReminderListItem[] items = getReminderItems();
    for (int i = 0; i < items.length; i++) {
      if (isRemindEventRequired(items[i].getProgram(), items[i].getMinutes())) {
        mListener.timeEvent(items[i]);
      }
    }

  }

  private boolean isRemindEventRequired(Program prog, int remindMinutes) {

    if (remindMinutes < 0) {
      return false;
    }

    Date remindDate = prog.getDate();

    int remindTime = prog.getStartTime() - remindMinutes;
    if (remindTime < 0) {
      remindTime = -remindTime;
      int days = remindTime / 1440 + 1;
      remindTime = 1440 - (remindTime % 1440);
      remindDate = remindDate.addDays(-days);
    }
    devplugin.Date today = new devplugin.Date();
    int diff = today.compareTo(remindDate);

    return (diff > 0
        || (diff == 0 && IOUtilities.getMinutesAfterMidnight() >= remindTime)) 
        && !isBlocked(prog);

  }

  /**
   * Block a Program. This Program won't get reminded
   * 
   * @param prg
   *          Program to block
   */
  public void blockProgram(Program prg) {
    mBlockedPrograms.add(prg);
  }

  /**
   * Remove a Program from the Block-List
   * 
   * @param prg
   *          Program to remove from Block-List
   */
  public void unblockProgram(Program prg) {
    mBlockedPrograms.remove(prg);
  }

  /**
   * Is Program Blocked?
   * 
   * @param prg
   *          Check if this Program is blocked
   * @return true, if Program is blocked
   */
  public boolean isBlocked(Program prg) {
    return mBlockedPrograms.contains(prg);
  }

  /**
   * Stop the Timer
   */
  public void stopTimer() {
    mTimer.stop();
  }

}