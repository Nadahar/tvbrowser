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


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */


package reminderplugin;

import java.io.*;

import util.exc.*;

import devplugin.*;

public class ReminderListItem implements Serializable, Comparable {

  private int reminderSelection;
  private Program program;

  private static int[] listValues={0,1,2,3,5,10,15,30,60};



  public ReminderListItem(devplugin.Program prog, int reminderSelection) {
    this.reminderSelection=reminderSelection;
    this.program=prog;
  }
  
  
  
  /**
   * Serializes this item.
   */
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    out.writeInt(reminderSelection);
    out.writeObject(program.getDate());
    out.writeObject(program.getID());
  }

  
  
  /**
   * Deserializes this item.
   */
  private void readObject(ObjectInputStream in)
    throws IOException, ClassCastException
  {
    int version = in.readInt();
    reminderSelection = in.readInt();
    
    try {
      devplugin.Date programDate = (devplugin.Date) in.readObject();
      String programId = (String) in.readObject();

      program = Plugin.getPluginManager().getProgram(programDate, programId);
    }
    catch (ClassNotFoundException exc) {
      throw new IOException("Class not found: " + exc.getMessage());
    }
  }
  
  

  public devplugin.Program getProgram() {
    return program;
  }

  public int getReminderSelection() {
    return reminderSelection;
  }

  public int getReminderMinutes() {
    if (reminderSelection < listValues.length) {
      return listValues[reminderSelection];
    }
    throw new RuntimeException("invalid reminder selection");

  }


  public void setReminderSelection(int value) {
    reminderSelection=value;
  }

  public int compareTo(Object obj) {
    ReminderListItem item=(ReminderListItem)obj;

    int res=program.getDate().compareTo(item.getProgram().getDate());
    if (res!=0) return res;

    int minThis=program.getHours()*60+program.getMinutes();
    int minOther=item.getProgram().getHours()*60+item.getProgram().getMinutes();

    if (minThis<minOther) {
      return -1;
    }else if (minThis>minOther) {
      return 1;
    }

    return 0;
  }

}