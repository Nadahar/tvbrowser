/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

package reminderplugin;

import java.io.*;

import util.exc.*;
import util.io.IOUtilities;

import devplugin.*;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ReminderListItem implements Serializable, Comparable {

  private int mReminderMinutes;
  private Program mProgram;



  public ReminderListItem(devplugin.Program prog, int reminderMinutes) {
    mReminderMinutes = reminderMinutes;
    mProgram = prog;
  }
  
  
  
  /**
   * Serializes this item.
   */
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    out.writeInt(mReminderMinutes);
    out.writeObject(mProgram.getDate());
    out.writeObject(mProgram.getID());
  }

  
  
  /**
   * Deserializes this item.
   */
  private void readObject(ObjectInputStream in)
    throws IOException, ClassCastException
  {
    int version = in.readInt();
    mReminderMinutes = in.readInt();
    
    try {
      devplugin.Date programDate = (devplugin.Date) in.readObject();
      String programId = (String) in.readObject();

      mProgram = Plugin.getPluginManager().getProgram(programDate, programId);
    }
    catch (ClassNotFoundException exc) {
      throw new IOException("Class not found: " + exc.getMessage());
    }
  }
  
  

  public devplugin.Program getProgram() {
    return mProgram;
  }

  public int getReminderMinutes() {
    return mReminderMinutes;
  }


  public void setReminderMinutes(int minutes) {
    mReminderMinutes = minutes;
  }
  
  
  
  public boolean isExpired() {
    if (mProgram == null) {
      // The program wasn't found after deserialization.
      return true;
    } else {
      return mProgram.isExpired();
    }
  }

  
  
  public int compareTo(Object obj) {
    ReminderListItem item=(ReminderListItem)obj;

    int res=mProgram.getDate().compareTo(item.getProgram().getDate());
    if (res!=0) return res;

    int minThis=mProgram.getHours()*60+mProgram.getMinutes();
    int minOther=item.getProgram().getHours()*60+item.getProgram().getMinutes();

    if (minThis<minOther) {
      return -1;
    }else if (minThis>minOther) {
      return 1;
    }

    return 0;
  }

}