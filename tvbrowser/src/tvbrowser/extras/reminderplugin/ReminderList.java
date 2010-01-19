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
import java.util.Collections;

import tvbrowser.core.ChannelList;
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

  private ArrayList<ReminderListItem> mList;

  /** List of Blocked Programs. These Programs don't trigger a reminder anymore */
  private ArrayList<Program> mBlockedPrograms = new ArrayList<Program>();

  /**
   * only sort the list if necessary
   */
  private boolean needsSort = false;

  public ReminderList() {
    mList = new ArrayList<ReminderListItem>();
  }


  /**
   * reads the object from an input stream.
   *
   * @param in the input stream to read from.
   * @throws IOException if something went wrong reading the input stream
   * @throws ClassNotFoundException if the object could not be deserialized
   */
  public void read(final ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt();
    if (version == 1 || version >= 3) { // version == 2 ==> read from plugin
      // tree
      int size = in.readInt();
      for (int i = 0; i < size; i++) {
        in.readInt(); // read version
        int reminderMinutes = in.readInt();
        String comment = null;
        if (version >= 5) {
          comment = in.readUTF();
        }
        Date programDate = (Date) in.readObject();
        String programId = (String) in.readObject();
        Program program = Plugin.getPluginManager().getProgram(programDate,
                programId);

        int referenceCount = 1;

        if (version >= 4) {
          referenceCount = in.readInt();
        }

        // Only add items that were able to load their program
        if (program != null) {
          add(program, new ReminderContent(reminderMinutes, comment), referenceCount);
        }
      }
    }
  }


  /**
   * serialize this object to a stream.
   *
   * @param out the stream to write to
   * @throws IOException if something went wrong writing to the stream
   */
  public void writeData(final ObjectOutputStream out) throws IOException {
    out.writeInt(5); // version
    ReminderListItem[] items = getReminderItems();
    out.writeInt(items.length);
    for (ReminderListItem item : items) {
      out.writeInt(3);
      out.writeInt(item.getMinutes());
      out.writeUTF(item.getComment());
      Date date = item.getProgram().getDate();
      out.writeObject(date);
      out.writeObject(item.getProgram().getID());
      out.writeInt(item.getReferenceCount());
    }
  }

  public void add(Program program, ReminderContent reminderContent) {
    add(program, reminderContent, 1);
  }

  private void add(Program program, int minutes, int referenceCount) {
    if (!program.isExpired()) {
      ReminderListItem item = getReminderItem(program);

      if (item != null) {
        item.incReferenceCount();
      } else {
        item = new ReminderListItem(program, minutes);
        item.setReferenceCount(referenceCount);

        synchronized (mList) {
          mList.add(item);
        }

        needsSort = true;
        program.mark(ReminderPluginProxy.getInstance());
      }
    }
  }

  private void add(Program program, ReminderContent reminderContent, int referenceCount) {
    if (!program.isExpired()) {
      ReminderListItem item = getReminderItem(program);

      if (item != null) {
        item.incReferenceCount();
      } else {
        item = new ReminderListItem(program, reminderContent);
        item.setReferenceCount(referenceCount);

        synchronized (mList) {
          mList.add(item);
        }

        needsSort = true;
        program.mark(ReminderPluginProxy.getInstance());
      }
    }
  }

  /**
   * Only adds a Program if it's not blocked
   *
   * @param programs Programs to add
   * @param minutes  remind x Minutes before start
   */
  public void addAndCheckBlocked(Program[] programs, int minutes) {
    for (Program program : programs) {
      if (!contains(program) && !mBlockedPrograms.contains(program)
              && (!program.isExpired())) {
        ReminderListItem item = new ReminderListItem(program, minutes);

        synchronized (mList) {
          mList.add(item);
        }

        needsSort = true;
        program.mark(ReminderPluginProxy.getInstance());
      } else if (contains(program)) {
        getReminderItem(program).incReferenceCount();
      }
    }
  }

  public void setReminderTimerListener(ReminderTimerListener listener) {
    this.mListener = listener;

    if (ReminderPlugin.getInstance().isAllowedToStartTimer()) {
      startTimer();
    }
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
    ArrayList<ReminderListItem> localItems = (ArrayList<ReminderListItem>) mList
            .clone();
    for (ReminderListItem item : localItems) {
      final Program program = item.getProgram();
      if (program == null || program.isExpired() || !ChannelList.isSubscribedChannel(program.getChannel())) {
        remove(item);
      }
    }
  }

  private void remove(ReminderListItem item) {
    item.decReferenceCount();
    if (item.getReferenceCount() < 1) {
      synchronized (mList) {
        mList.remove(item);
      }
      needsSort = true;
      item.getProgram().unmark(ReminderPluginProxy.getInstance());
    }
  }

  public void remove(ProgramItem item) {
    remove(item.getProgram());
  }

  public boolean contains(Program program) {
    synchronized (mList) {
      for (ReminderListItem item : mList) {
        if (item.getProgram().equals(program)) {
          return true;
        }
      }
    }

    return false;
  }

  public void remove(Program program) {
    remove(new Program[]{program});
  }

  public void remove(Program[] programs) {
    ArrayList<ReminderListItem> localItems = (ArrayList<ReminderListItem>) mList
        .clone();
    for (Program program : programs) {
      for (ReminderListItem item : localItems) {
        if (item.getProgram().equals(program)) {
          remove(item);
          break;
        }
      }
    }
  }

  public void removeWithoutChecking(ProgramItem item) {
    removeWithoutChecking(item.getProgram());
  }

  public ReminderListItem removeWithoutChecking(Program program) {
    synchronized (mList) {
      for (ReminderListItem item : mList) {
        if (item.getProgram().equals(program)) {
          mList.remove(item);
          needsSort = true;
          item.getProgram().unmark(ReminderPluginProxy.getInstance());
          return item;
        }
      }
    }

    return null;
  }

  public void addWithoutChecking(ReminderListItem item) {
    synchronized (mList) {
      mList.add(item);
    }

    needsSort = true;
  }

  public ReminderListItem getReminderItem(Program program) {
    synchronized (mList) {
      for (ReminderListItem item : mList) {
        if (item.getProgram().equals(program)) {
          return item;
        }
      }
    }
    return null;
  }

  public ReminderListItem[] getReminderItems() {
    // avoid sorting the reminder list with every timer call
    synchronized (mList) {
      if (needsSort) {
        Collections.sort(mList);
        needsSort = false;
      }
      ReminderListItem[] items = mList
              .toArray(new ReminderListItem[mList.size()]);

      return items;
    }
  }

  /**
   * Checks all programs, if they currently exists
   *
   * @return all removed programs
   */
  @SuppressWarnings("unchecked")
  public Program[] updatePrograms() {
    ArrayList<ReminderListItem> localItems;

    synchronized (mList) {
      localItems = (ArrayList<ReminderListItem>) mList.clone();
      mList.clear();
    }

    ArrayList<Program> removedPrograms = new ArrayList<Program>();

    for (ReminderListItem item : localItems) {
      if (item.getProgram().getProgramState() == Program.WAS_DELETED_STATE) {
        removedPrograms.add(item.getProgram());
      } else if (item.getProgram().getProgramState() == Program.WAS_UPDATED_STATE) {
        Program p = item.getProgram();
        add(Plugin.getPluginManager().getProgram(p.getDate(), p.getID()),
                item.getMinutes(), item.getReferenceCount());
      } else {
        synchronized (mList) {
          mList.add(item);
        }

        needsSort = true;
      }
    }

    return removedPrograms.toArray(new Program[removedPrograms.size()]);
  }

  public void actionPerformed(ActionEvent event) {
    if (mListener == null) {
      mTimer.stop();
      return;
    }

    // calculate today only once for the complete list
    devplugin.Date today = Date.getCurrentDate();
    ArrayList<ReminderListItem> reminders = new ArrayList<ReminderListItem>();
    for (ReminderListItem item : getReminderItems()) {
      if (isRemindEventRequired(item.getProgram(), item.getMinutes(), today)) {
        reminders.add(item);
      }
    }
    if (reminders.size() > 0) {
      mListener.timeEvent(reminders);
    }
  }

  private boolean isRemindEventRequired(Program prog, int remindMinutes, Date today) {

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
    int diff = today.compareTo(remindDate);

    return (diff > 0 || (diff == 0 && IOUtilities.getMinutesAfterMidnight() >= remindTime))
            && !isBlocked(prog);

  }

  /**
   * Block a Program. This Program won't get reminded
   *
   * @param prg Program to block
   */
  public void blockProgram(Program prg) {
    mBlockedPrograms.add(prg);
  }

  /**
   * Remove a Program from the Block-List
   *
   * @param prg Program to remove from Block-List
   */
  public void unblockProgram(Program prg) {
    synchronized (mBlockedPrograms) {
      mBlockedPrograms.remove(prg);
    }
  }

  /**
   * Is Program Blocked?
   *
   * @param prg Check if this Program is blocked
   * @return true, if Program is blocked
   */
  public boolean isBlocked(Program prg) {
    synchronized (mBlockedPrograms) {
      return mBlockedPrograms.contains(prg);
    }
  }

  /**
   * Stop the Timer
   */
  public void stopTimer() {
    mTimer.stop();
  }

}