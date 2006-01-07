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


import devplugin.*;


public class ReminderListItem implements Comparable {
    

  private ProgramItem mProgramItem;
    
  public ReminderListItem(ProgramItem item) {
    mProgramItem = item;
  }
  
  public ReminderListItem(Program prog, int minutes) {
    mProgramItem = new ProgramItem(prog);
    setMinutes(minutes);
  }
  
 /* public ReminderListItem(ProgramItem item) {
    mProgramItem = item;  
  }*/
    
 /* public Program getProgram() {
   return mProgramItem.getProgram();
  }*/
  
  public ProgramItem getProgramItem() {
    return mProgramItem;
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
  
  public int compareTo(Object obj) {
    ReminderListItem item=(ReminderListItem)obj;

    int res=getProgram().getDate().compareTo(item.getProgram().getDate());
    if (res!=0) return res;

    int minThis=getProgram().getHours()*60+getProgram().getMinutes();
    int minOther=item.getProgram().getHours()*60+item.getProgram().getMinutes();

    if (minThis<minOther) {
      return -1;
    }else if (minThis>minOther) {
      return 1;
    }

    return 0;
  }
}
