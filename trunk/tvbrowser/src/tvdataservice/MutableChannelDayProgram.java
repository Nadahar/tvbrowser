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
import java.util.logging.Logger;

import util.program.ProgramUtilities;
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

  /** Logger */
  private static final Logger mLog = Logger.getLogger(MutableChannelDayProgram.class
      .getName());

  /** The date of this program list. */
  private devplugin.Date mDate;

  /** The channel of this program list. */
  private Channel mChannel;

  /** The program list itself. */
  private ArrayList<Program> mProgramList;

  private boolean mLastProgramHadEndOnUpdate, mWasChangedByPlugin;

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
    mWasChangedByPlugin = false;
    
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
    progID = ProgramUtilities.getTimeZoneCorrectedProgramId(progID);
    
    // avoid split operation
    int strLen = progID.length();
    int idlength = 1;
    for (int i = 0; i < strLen; i++) {
      if (progID.charAt(i) == '_') {
        idlength++;
      }
    }

    for(Program prog : mProgramList) {
      String id = prog.getID();
      if (idlength > 4) {
        if (progID.compareTo(id) == 0) {
          return prog;
        }
      }
      else if(idlength < 4) {
        String[] temp = id.split("_");
       
        id = temp[temp.length - 2] + "_" + temp[temp.length - 1];
      }
      else if(idlength == 4) {
        String[] temp = id.split("_");
        
        id = temp[0] + "_" + temp[1] + "_" + temp[temp.length - 2] + "_" + temp[temp.length - 1];        
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
    if (!program.getChannel().equals(mChannel)) {
      mLog.severe("Trying to add program of channel " + program.getChannel().getName() + " to day program of channel " + mChannel.getName());
      return;
    }
    // find the index where to add the program
    // We search backwards, because the data may come already ordered. And if
    // this is the case we only have to compare once.
    int addIdx;
    int time = program.getStartTime();
    for (addIdx = mProgramList.size(); addIdx > 0; addIdx--) {
      Program cmp = mProgramList.get(addIdx - 1);
      int cmpTime = cmp.getStartTime();
	  	  
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
   * Returns an iterator containing all programs. Each iterator item is a
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
   * Return true if the last program ends after midnight. Future implementations
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
   * Sets the last program end time state on data update.
   * 
   * @param value the last program end time on data update
   * @since 2.2
   */
  public void setLastProgramHadEndOnUpdate(boolean value) {
    mLastProgramHadEndOnUpdate = value;
  }
  
  /**
   * Gets the last program end time state on data update.
   * 
   * @return If the last program had end time on data update
   * @since 2.2
   */
  public boolean getLastProgramHadEndOnUpdate() {
    return mLastProgramHadEndOnUpdate;
  }

  /**
   * Sets the changed state to let the day program 
   * be saved again to take over the changes.
   *
   * This works only if called from 
   * devplugin.Plugin#handleTvDataAdded(ChannelDayProgram)
   * otherwise the changes won't be saved.
   * 
   * @since 2.2.2
   */
  public void setWasChangedByPlugin() {
    mWasChangedByPlugin = true;
  }
  
  /**
   * Get if the day program was changed by a 
   * plugin and reset the changed state.
   * 
   * @return If this day program was changed by a plugin.
   * @since 2.2.2
   */
  public boolean getAndResetChangedByPluginState() {
    boolean temp = mWasChangedByPlugin;
    mWasChangedByPlugin = false;
    
    return temp;
  }

  /**
   * Compare with the given dayProgram
   * 
   * @param dayProgram
   * @return <code>true</code>, if all programs of the day match (with each field)
   * @since 2.6
   */
  public boolean equals(ChannelDayProgram dayProgram) {
    if (getProgramCount() != dayProgram.getProgramCount()) {
      return false;
    }
    if (!getChannel().equals(dayProgram.getChannel())) {
      return false;
    }
    for (int i = 0; i < getProgramCount(); i++) {
      Program program = getProgramAt(i);
      Program otherProgram = dayProgram.getProgramAt(i);
      if (program instanceof MutableProgram && otherProgram instanceof MutableProgram) {
        if (!((MutableProgram) program).equalsAllFields((MutableProgram) otherProgram)) {
          return false;
        }
      }
      else {
        return false; // no mutable programs, we can't compare
      }
    }
    return true; // everything checked, so they are the same
  }


}
