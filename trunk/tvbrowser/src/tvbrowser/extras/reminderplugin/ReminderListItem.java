/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
 *
 * This mProgram is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This mProgram is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this mProgram; if not, write to the Free Software
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


import java.awt.Frame;

import javax.swing.JOptionPane;

import util.program.ProgramUtilities;
import devplugin.Program;
import devplugin.ProgramItem;

/**
 * A class that contains the program for
 * which a reminder should be shown at some time.
 */
public class ReminderListItem implements Comparable<ReminderListItem> {
  private static final String KEY_REF_CNT = "refCnt";
  private static final String KEY_MINUTES = "minutes";
  private static final String KEY_COMMENT = "comment";

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ReminderListItem.class);

  private ProgramItem mProgramItem;

  /**
   * Creates an instance of this class.
   * <p>
   * @param item The item for this list item.
   */
  public ReminderListItem(ProgramItem item) {
    mProgramItem = item;
  }

  /**
   * Creates an instance of this class.
   * <p>
   * 
   * @param prog
   *          The program for this item.
   * @param minutes
   *          The reminder time for this item.
   */
  public ReminderListItem(Program prog, int minutes) {
    mProgramItem = new ProgramItem(prog);
    setMinutes(minutes);
  }

  public ReminderListItem(Program prog, ReminderContent reminderContent) {
    this(prog, reminderContent.getReminderMinutes());
    setComment(reminderContent.getReminderComment());
  }

  /**
   * Sets the number of reminders set for this item.
   * <p>
   * @param refCnt The number of reminders for this item.
   */
  public void setReferenceCount(int refCnt) {
    mProgramItem.setProperty(KEY_REF_CNT, Integer.toString(refCnt));
  }

  /**
   * Gets the program item of this list item.
   * <p>
   * @return The program item.
   */
  public ProgramItem getProgramItem() {
    return mProgramItem;
  }

  /**
   * Gets the number of set reminders of this item.
   * <p>
   * @return The number of set reminders of this item.
   */
  public int getReferenceCount() {
    String cnt = mProgramItem.getProperty(KEY_REF_CNT);
    if (cnt != null) {
      try {
        return Integer.parseInt(cnt);
      }catch(NumberFormatException e) {
        return 1;
      }
    }
    return 1;
  }

  /**
   * Increase the number of set reminders of this item about 1.
   */
  public void incReferenceCount() {
    int cnt = getReferenceCount() + 1;
    mProgramItem.setProperty(KEY_REF_CNT, Integer.toString(cnt));
  }

  /**
   * Decrease the number of set reminders of this item about 1.
   */
  public void decReferenceCount() {
    int cnt = getReferenceCount() - 1;
    if (cnt >= 0) {
      mProgramItem.setProperty(KEY_REF_CNT, Integer.toString(cnt));
    }
  }

  /**
   * Gets the reminder minutes of this list item.
   * <p>
   * 
   * @return The reminder minutes of this list item.
   */
  public int getMinutes() {
    String m = mProgramItem.getProperty(KEY_MINUTES);
    if (m!=null) {
      try {
        return Integer.parseInt(m);
      }catch(NumberFormatException e) {
        return 10;
      }
    }
    return 10;
  }

  /**
   * Sets the minutes of this list item.
   * <p>
   * @param minutes The minutes of this list item.
   */
  public void setMinutes(int minutes) {
    if(minutes < 0) {
      minutes = ReminderPlugin.getInstance().getDefaultReminderTime();
    }
    
    mProgramItem.setProperty(KEY_MINUTES, Integer.toString(minutes));
  }

  /**
   * Gets the reminder comment of this list item.
   * <p/>
   *
   * @return The reminder comment of this list item.
   */
  public String getComment() {
    final String comment = mProgramItem.getProperty(KEY_COMMENT);
    if (comment != null) {
      return comment;
    }
    return "";
  }

  /**
   * Sets the comment of this list item.
   * <p/>
   *
   * @param comment The comment of this list item.
   */
  public void setComment(final String comment) {
    if (comment != null) {
      mProgramItem.setProperty(KEY_COMMENT, comment);
    }
  }

  /**
   * Gets the program of this list item.
   * <p>
   * @return The program of this list item.
   */
  public Program getProgram() {
    return mProgramItem.getProgram();
  }

  public int compareTo(ReminderListItem other) {
    return ProgramUtilities.getProgramComparator().compare(getProgram(), other.getProgram());
  }

  public void changeComment(final Frame parentFrame) {
    String comment = getComment();
    if (comment == null) {
      comment = "";
    }
    comment = JOptionPane.showInputDialog(parentFrame, mLocalizer.msg(
        "comment.message", "Enter new comment"), comment);
    if (comment != null) {
      setComment(comment);
    }
  }
}
