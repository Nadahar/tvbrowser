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

package tvdataloader;

import java.io.*;
import java.util.*;

/**
 * This abstract class represents the program of a channel on one day.
 *
 * @author Martin Oberhauser
 */
public abstract class AbstractChannelDayProgram implements Serializable {

  transient private AbstractProgram programOnAir;

  
  
  /**
   * Returns an iterator containing all programms. Each iterator item is a
   * devplugin.Program object.
   */
  public abstract java.util.Iterator getPrograms();

  /**
   * Returns an array containing all programms. Each element is a
   * devplugin.Program object.
   */
  public abstract Object[] getProgramList();

  /**
   * Returns the channel of this day program.
   */
  public abstract devplugin.Channel getChannel();

  /**
   * Returns the date of this day program.
   */
  public abstract devplugin.Date getDate();

  /**
   * Returns the program object having the specified ID.
   */
  public abstract devplugin.Program getProgram(String progID);


  public final void markProgramOnAir() {
    if (!new devplugin.Date().equals(getDate())) {
      return;
    }
    Iterator it=getPrograms();
    AbstractProgram p;
    int diff=Integer.MAX_VALUE;
    int nDiff;
    Calendar cal=new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));

    int time=cal.get(Calendar.HOUR_OF_DAY)*60+cal.get(Calendar.MINUTE);
    AbstractProgram newOnAir=null;
    while (it.hasNext()) {
      p=(AbstractProgram)it.next();
      nDiff=time-(p.getHours()*60+p.getMinutes());
      if (nDiff>=0 && nDiff<diff) {
        diff=nDiff;
        newOnAir=p;
      }
    }
    
    if (programOnAir!=null) {
      programOnAir.markAsOnAir(false);
    }
    programOnAir=newOnAir;
    if (programOnAir!=null) {
      programOnAir.markAsOnAir(true);
    }
  }

}