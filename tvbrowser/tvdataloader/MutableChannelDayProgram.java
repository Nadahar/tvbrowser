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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package tvdataloader;

import java.util.*;

import devplugin.*;
import tvdataloader.*;

/**
 * A list of the programs of one channel and one day.
 * <p>
 * This implementation is mutable, meaning you can add programs. These programs are
 * automatically sorted by time.
 *
 * @author Til Schneider, www.murfman.de
 */
public class MutableChannelDayProgram extends AbstractChannelDayProgram {
  
  /** The date of this program list. */  
  private devplugin.Date mDate;
  
  /** The channel of this program list. */  
  private Channel mChannel;
  
  /** The program list itself. */  
  private ArrayList mProgramList;
  
  
  
  /**
   * Creates a new instance of MutableChannelDayProgram.
   *
   * @param date The date
   * @param channel The channel
   */
  public MutableChannelDayProgram(devplugin.Date date,
    devplugin.Channel channel)
  {
    mDate = date;
    mChannel = channel;
    
    mProgramList = new ArrayList();
  }
  
  
  
  /**
   * Returns the channel of this day program.
   *
   * @return  the channel of this day program.
   */
  public devplugin.Channel getChannel() {
    return mChannel;
  }
  
  
  
  /**
   * Returns the date of this day program.
   *
   * @return  the date of this day program.
   */
  public devplugin.Date getDate() {
    return mDate;
  }
  
  
  
  /**
   * Returns the program object having the specified ID.
   *
   * @param progID The ID of the wanted program.
   * @return  the program object having the specified ID.
   */
  public Program getProgram(String progID) {
    Iterator iter = mProgramList.iterator();
    while(iter.hasNext()) {
      Program prog = (Program) iter.next();
      if (progID.equals(prog.getID())) {
        return prog;
      }
    }
    
    // nothing found
    return null;
  }
  
  
  
  /**
   * Gets the number of programs in this list.
   *
   * @return the number of programs.
   */
  public int getProgramCount() {
    return mProgramList.size();
  }
  
  
  
  /**
   * Returns an array containing all programms. Each element is a
   * devplugin.Program object.
   *
   * @return The programs of this list. Ordered by time.
   */
  public Object[] getProgramList() {
    Program[] programArr = new Program[mProgramList.size()];
    mProgramList.toArray(programArr);
    return programArr;
  }
  
  
  
  /**
   * Adds a program.
   *
   * @param program The program to add. This program will automatically be put
   *        in the right position in the list, so the list stays ordered.
   */
  public void addProgram(Program program) {
    // find the index where to add the program
    // We search backwards, because the data may come already ordered. And if
    // this is the case we only have to compare once.
    int addIdx;
    int time = program.getHours() * 60 + program.getMinutes();
    for (addIdx = mProgramList.size(); addIdx > 0; addIdx--) {
      Program cmp = (Program) mProgramList.get(addIdx - 1);
      int cmpTime = cmp.getHours() * 60 + cmp.getMinutes();
      if (cmpTime < time) {
        break;
      }
    }
    
    mProgramList.add(addIdx, program);
  }
  
  
  
  /**
   * Returns an iterator containing all programms. Each iterator item is a
   * devplugin.Program object.
   *
   * @return An iterator through the program list.
   */
  public java.util.Iterator getPrograms() {
    /*
    System.out.println(">>> Dump:");
    Iterator iter = mProgramList.iterator();
    while (iter.hasNext()) {
      System.out.println("  " + iter.next());
    }
    System.out.println("<<< Dump finished");
    */
    
    return mProgramList.iterator();
  }
  
}
