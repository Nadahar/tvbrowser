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

package tvbrowser.core;

import java.util.Iterator;
import java.util.HashMap;

/**
 * The DayProgram class holds the whole tv program data of one day.
 *
 * @author Martin Oberhauser
 */
public class DayProgram {

  /** Contains for a channel ID (key) an AbstractChannelDayProgram (value) */
  private HashMap mDayProgramHash;
  
  /** The date of this day program. */
  private devplugin.Date mDate;
  
  
  
  /**
   * Creates a new instance of DayProgram.
   *
   * @param date The date of the day program.
   */  
  public DayProgram(devplugin.Date date) {
    mDayProgramHash = new HashMap();
    mDate = date;
  }
  

  
  /**
   * Adds the day program of a single channel.
   *
   * @param prog The program to add.
   * @throws IllegalArgumentException if the specified ChannelDayProgram has
   *         another date than this day program.
   */
  public void addChannelDayProgram(tvdataloader.AbstractChannelDayProgram prog)
    throws IllegalArgumentException
  {
    if (mDate != null) {
      if (! mDate.equals(prog.getDate())) {
        throw new IllegalArgumentException("The specified ChannelDayProgram "
          + "(from " + prog.getDate() + ") does not belong to this day program "
          + "(from " + mDate + ")!");
      }
    }

    Object key = new Integer(prog.getChannel().getId());
    mDayProgramHash.put(key, prog);
  }
  
  
  
  /**
   * Gets the program for the specified channel.
   *
   * @param channel The channel to get the program for.
   * @return the program for the specified channel.
   */  
  public tvdataloader.AbstractChannelDayProgram getChannelDayProgram(
    devplugin.Channel channel)
  {
    Object key = new Integer(channel.getId());
    return (tvdataloader.AbstractChannelDayProgram) mDayProgramHash.get(key);
  }

  
  
  /**
   * Gets the date of this day program.
   *
   * @return the date of this day program.
   */
  public devplugin.Date getDate() {
    return mDate;
  }

  
  
  /**
   * Gets all channel day programs of this day program.
   *
   * @return an iterator of AbstractChannelDayProgram objects.
   * @see AbstractChannelDayProgram
   */
  public Iterator iterator() {
    return mDayProgramHash.values().iterator();
  }

  

  /**
   * Marks all programs which are currently on air.
   */
  public void markProgramsOnAir() {
    Iterator it=mDayProgramHash.values().iterator();
    tvdataloader.AbstractChannelDayProgram cur;
    while (it.hasNext()) {
      cur=(tvdataloader.AbstractChannelDayProgram)it.next();
      cur.markProgramOnAir();
    }
  }

  
  
  /**
   * Gets the program object having the specified ID.
   * <p>
   * If there is no such program, <CODE>null</CODE> is returned.
   *
   * @param progID The ID of the program to get.
   * @return the program object having the specified ID.
   */
  public devplugin.Program getProgram(String progID) {

    Iterator it=mDayProgramHash.values().iterator();
    devplugin.Program prog=null;
    while (it.hasNext()) {
      tvdataloader.AbstractChannelDayProgram acdp =
        (tvdataloader.AbstractChannelDayProgram)it.next();

      prog=acdp.getProgram(progID);
      if (prog!=null) {
        return prog;
      }
    }
    return prog;
  }
  
  
  
  /**
   * Gets whether this day program is empty
   */
  public boolean isEmpty() {
    return mDayProgramHash.isEmpty();
  }

}