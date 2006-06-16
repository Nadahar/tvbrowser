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

import java.util.ArrayList;
import java.util.Iterator;

import util.io.IOUtilities;
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

  /** The program that is currently on air. May be null. */
  private Program mProgramOnAir;

  /** The date of this program list. */
  private devplugin.Date mDate;

  /** The channel of this program list. */
  private Channel mChannel;

  /** The program list itself. */
  private ArrayList<Program> mProgramList;

  private boolean mLastProgramHadEndOnUpdate;

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
    mLastProgramHadEndOnUpdate = false;

    mProgramList = new ArrayList<Program>();
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
    for(Program prog : mProgramList) {
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
   * Returns the program at the specified index.
   *
   * @param index The index of the wanted program.
   * @return The program at the specified index.
   */
  public Program getProgramAt(int index) {
    return mProgramList.get(index);
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
      Program cmp = mProgramList.get(addIdx - 1);
      int cmpTime = cmp.getHours() * 60 + cmp.getMinutes();
	  	  
	  if (program.getDate().compareTo(cmp.getDate())>0) {
        break; // insert here
      }
      else if (program.getDate().compareTo(cmp.getDate())<0) {
        continue;
      } 
	  
      if (cmpTime == time) {
        // We already have this program
        return;
      }
      if (cmpTime < time) {
        break;
      }
    }

    mProgramList.add(addIdx, program);
  }


  /**
   * Removes all programs from this day program.
   */
  public void removeAllPrograms() {
    mProgramList.clear();
  }


  /**
   * Returns an iterator containing all programms. Each iterator item is a
   * devplugin.Program object.
   *
   * @return An iterator through the program list.
   */
  public java.util.Iterator<Program> getPrograms() {

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
      Program lastProgram = mProgramList.get(size - 1);
      int endTime = lastProgram.getHours() * 60 + lastProgram.getMinutes()
        + lastProgram.getLength();

      return endTime >= (23 * 60);
    }
  }



  /**
   * Marks the program that is currently on air.
   */
  public synchronized void markProgramOnAir() {
    Program newProgramOnAir = null;
    
    devplugin.Date today = new devplugin.Date();
    int time = IOUtilities.getMinutesAfterMidnight();
     
    if (today.equals(getDate())) {
      // This is the program for today -> Get the program that is currently on air
      Iterator iter = getPrograms();
      while (iter.hasNext()) {
        
        Program prog = (Program) iter.next();
        if (!prog.getDate().equals(today)) {
          continue;
        }
        
      //  System.out.println("trying program: "+prog.getHours()+":"+prog.getMinutes()+" "+prog.getTitle()+", date: "+prog.getDate());
        
        /*
        int startTime = prog.getHours() * 60 + prog.getMinutes();
        int endTime = startTime + prog.getLength();
        
        if ((time >= startTime) && (time < endTime)) {
          newProgramOnAir = prog;
          break;
        }
        */
        int startTime = prog.getHours() * 60 + prog.getMinutes();
      //  System.out.println("time: "+time+", startTime: "+startTime);
        if (startTime>time) {
          break;
        }
        newProgramOnAir=prog;    
      }
    } else {
      // Check whether this program was for yesterday
      //devplugin.Date yesterday = new devplugin.Date(today.getDaysSince1970() - 1);
      
      devplugin.Date yesterday=today.addDays(-1);
      
      
      if (yesterday.equals(getDate())) {
        // This program was for yesterday -> Check whether the last program is
        // still on air. This is the case, when it starts before midnight and
        // reaches into the next day.
        
        
        int programCount = getProgramCount();
        if (programCount > 0) {
          int lastIdx = programCount - 1;
          Program lastProgram = getProgramAt(lastIdx);
          
          int startTime = lastProgram.getHours() * 60 + lastProgram.getMinutes();
          int endTime = startTime + lastProgram.getLength();
          
          // Take the end time modulo 24 hours
          int modEndTime = endTime % (24 * 60);
          
          if ((endTime > (24 * 60)) && (time < modEndTime)) {
            newProgramOnAir = lastProgram;
          }
        }
        
      }
    }
    
   // System.out.println("new program on air: "+newProgramOnAir);
    
    // check program length:
    if (newProgramOnAir!=null) {
      int endTime=newProgramOnAir.getHours()*60 + newProgramOnAir.getMinutes() + newProgramOnAir.getLength();
      if (endTime<time) {
        newProgramOnAir=null;
      }
    }
    
    
    // Update the "on air" state of the programs
    if (mProgramOnAir != newProgramOnAir) {
      if (mProgramOnAir != null) {
        mProgramOnAir.markAsOnAir(false);
      }
      if (newProgramOnAir != null) {
        newProgramOnAir.markAsOnAir(true);
      }
      
      mProgramOnAir = newProgramOnAir;
    }
  }

  /**
   * Sets the last program end time state on data update.
   * 
   * @param value If the last program had end time on data update.
   * @since 2.2
   */
  public void setLastProgramHadEndOnUpdate(boolean value) {
    mLastProgramHadEndOnUpdate = value;
  }
  
  /**
   * Gets the last program end time state on data update.
   * 
   * @return If the last program had ent time on data update
   * @since 2.2
   */
  public boolean getLastProgramHadEndOnUpdate() {
    return mLastProgramHadEndOnUpdate;
  }
}
