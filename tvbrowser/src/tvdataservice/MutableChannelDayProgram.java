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

package tvdataservice;

import java.util.*;
import java.io.*;

import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Program;

/**
 * A list of the programs of one channel and one day.
 * <p>
 * This implementation is mutable, meaning you can add programs. These programs are
 * automatically sorted by time.
 *
 * @author Til Schneider, www.murfman.de
 */
public class MutableChannelDayProgram implements ChannelDayProgram {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(MutableChannelDayProgram.class.getName());

  private Program programOnAir;

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
  
  
  
  public MutableChannelDayProgram(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    int version = in.readInt();

    mDate = new devplugin.Date(in);
    mChannel = Channel.readData(in, false);
    
    int size = in.readInt();
    System.out.println("mDate: " + mDate + ", mChannel: " + mChannel
      + ", size: " + size);
    mProgramList = new ArrayList(size);
    for (int i = 0; i < size; i++) {
      MutableProgram prog = new MutableProgram(in);
      mProgramList.add(prog);
    }
  }

  
  
  /**
   * Writes this instance to a stream.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    
    mDate.writeData(out);
    mChannel.writeData(out);
    
    out.writeInt(mProgramList.size());
    for (int i = 0; i < mProgramList.size(); i++) {
      MutableProgram prog = (MutableProgram) mProgramList.get(i);
      prog.writeData(out);
    }
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
    mLog.info(">>> Dump:");
    Iterator iter = mProgramList.iterator();
    while (iter.hasNext()) {
      mLog.info("  " + iter.next());
    }
    mLog.info("<<< Dump finished");
    */

    return mProgramList.iterator();
  }



  /**
   * Returns whether this channel day program is complete.
   * <p>
   * Return true if the last program ends afer midnight. Future implementations
   * may check for gaps too.
   */
  public boolean isComplete() {
    int size = mProgramList.size();
    if (size == 0) {
      return false;
    } else {
      Program lastProgram = (Program) mProgramList.get(size - 1);
      int endTime = lastProgram.getHours() * 60 + lastProgram.getMinutes()
        + lastProgram.getLength();

      return endTime >= (23 * 60);
    }
  }



  /**
   * Marks the program that is currently on air.
   */
  public final void markProgramOnAir() {
    if (!new devplugin.Date().equals(getDate())) {
      return;
    }
    Iterator it = getPrograms();
    Program p;
    int diff = Integer.MAX_VALUE;
    int nDiff;
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));

    int time = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    Program newOnAir = null;
    while (it.hasNext()) {
      p = (Program)it.next();
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
