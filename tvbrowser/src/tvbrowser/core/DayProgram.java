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

import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Calendar;
import java.util.ArrayList;
import java.awt.event.*;
import devplugin.Date;

/**
 * The DayProgram class holds the whole tv program data of one specified day.
 *
 * @author Martin Oberhauser
 */
public class DayProgram {

  private HashMap map;
  private devplugin.Date date;
  public DayProgram() {
    map=new HashMap();
    date=null;
  }

  /**
   * Adds the day program of a single channel.
   */
  public void addChannelDayProgram(tvdataloader.AbstractChannelDayProgram prog) {

    map.put(""+prog.getChannel().getId(),prog);
    if (date==null) {
      date=prog.getDate();
    }
  }

  /**
   * Returns the date of this day program.
   */
  public devplugin.Date getDate() {
    return date;
  }

  /**
   * Returns an iterator of AbstractChannelDayProgram object items.
   */
  public Iterator iterator() {
    return map.values().iterator();
  }


  /**
   * Call this function to mark all programs which are currently on air
   */
  public void markProgramsOnAir() {
    Iterator it=map.values().iterator();
    tvdataloader.AbstractChannelDayProgram cur;
    while (it.hasNext()) {
      cur=(tvdataloader.AbstractChannelDayProgram)it.next();
      cur.markProgramOnAir();
    }

  }

  /**
   * Returns the program object having the specified ID.
   */
  public devplugin.Program getProgram(String progID) {

    Iterator it=map.values().iterator();
    devplugin.Program prog=null;
    while (it.hasNext()) {
      tvdataloader.AbstractChannelDayProgram acdp=(tvdataloader.AbstractChannelDayProgram)it.next();

      prog=acdp.getProgram(progID);
      if (prog!=null) {
        return prog;
      }
    }
    return prog;

  }


}