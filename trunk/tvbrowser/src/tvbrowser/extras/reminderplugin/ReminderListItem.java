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


import devplugin.Program;
import devplugin.ProgramItem;
import util.program.ProgramUtilities;


public class ReminderListItem implements Comparable<ReminderListItem> {


  private ProgramItem mProgramItem;

  public ReminderListItem(ProgramItem item) {
    mProgramItem = item;
  }

  public ReminderListItem(Program prog, int minutes) {
    mProgramItem = new ProgramItem(prog);
    setMinutes(minutes);
  }

  public void setReferenceCount(int refCnt) {
    mProgramItem.setProperty("refCnt",""+ refCnt);
  }

  public ProgramItem getProgramItem() {
    return mProgramItem;
  }


  public int getReferenceCount() {
    String cnt = mProgramItem.getProperty("refCnt");
    if (cnt != null) {
      try {
        return Integer.parseInt(cnt);
      }catch(NumberFormatException e) {
        return 1;
      }
    }
    return 1;
  }

  public void incReferenceCount() {
    int cnt = getReferenceCount() + 1;
    mProgramItem.setProperty("refCnt",""+ cnt);
  }

  public void decReferenceCount() {
    int cnt = getReferenceCount() - 1;
    if (cnt >= 0) {
      mProgramItem.setProperty("refCnt",""+ cnt);
    }
  }

  public int getMinutes() {
    String m = mProgramItem.getProperty("minutes");
    if (m!=null) {
      try {
        return Integer.parseInt(m);
      }catch(NumberFormatException e) {
        return 10;
      }
    }
    return 10;
  }

  public void setMinutes(int minutes) {
      mProgramItem.setProperty("minutes",""+minutes);
  }

  public Program getProgram() {
    return mProgramItem.getProgram();
  }

  public int compareTo(ReminderListItem other) {
    return ProgramUtilities.getProgramComparator().compare(getProgram(), other.getProgram());
  }
}
