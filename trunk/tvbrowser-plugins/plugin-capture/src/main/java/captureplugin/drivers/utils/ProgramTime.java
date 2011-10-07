/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *     $Date: 2007-01-13 22:08:02 +0100 (Sa, 13 Jan 2007) $
 *   $Author: ds10 $
 * $Revision: 3016 $
 */
package captureplugin.drivers.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import devplugin.Channel;
import devplugin.Plugin;
import devplugin.Program;


/**
 * A combination of Start/End-Time and the Program
 */
public final class ProgramTime implements Cloneable {
    /** Start-Time*/
    private Calendar mStart;
    /** End-Time */
    private Calendar mEnd;
    /** Program to record */
    private Program mProgram;
    /** Additional programs that are in the time range */
    private Program[] mAdditionalPrograms;

    /** Title of the Program */
    private String mTitle;

    /**
     * Create ProgramTime
     */
    public ProgramTime() {
        mStart = Calendar.getInstance();
        mEnd = Calendar.getInstance();
        mTitle = "";
    }

    /**
     * Create ProgramTime
     * @param prg Program
     */
    public ProgramTime(Program prg) {
        setProgram(prg);
    }

    /**
     * Create ProgramTime
     * @param prg Program
     * @param start Start-Time
     * @param end End-Time
     */
    public ProgramTime(Program prg, Date start, Date end) {
        mProgram = prg;
        mStart = Calendar.getInstance();
        mStart.setTime((Date)start.clone());
        mEnd = Calendar.getInstance();
        mEnd.setTime((Date)end.clone());
        mTitle = prg.getTitle();
    }

    /**
     * Create ProgramTime
     * @param time copy from this ProgramTime
     */
    public ProgramTime(ProgramTime time) {
        mProgram = time.getProgram();
        mStart = Calendar.getInstance();
        mStart.setTime((Date)time.getStart().clone());
        mEnd = Calendar.getInstance();
        mEnd.setTime((Date)time.getEnd().clone());
        mTitle = time.getTitle();
    }

    /**
     * Sets the Program
     * @param prg Program
     */
    public void setProgram(Program prg) {
      mProgram = prg;

      Calendar c = (Calendar) prg.getDate().getCalendar().clone();

      c.set(Calendar.HOUR_OF_DAY, prg.getHours());
      c.set(Calendar.MINUTE, prg.getMinutes());
      c.set(Calendar.SECOND, 0);

      mStart = c;

      c = (Calendar) prg.getDate().getCalendar().clone();

      c.set(Calendar.HOUR_OF_DAY, prg.getHours());
      c.set(Calendar.MINUTE, prg.getMinutes());

      if (prg.getLength() <= 0) {
        c.add(Calendar.MINUTE, 1);
      } else {
        c.add(Calendar.MINUTE, prg.getLength());
      }
      c.set(Calendar.SECOND, 0);

      mEnd = c;

      mTitle = prg.getTitle();
    }

    /**
     * Returns the Program
     * @return Program
     */
    public Program getProgram() {
        return mProgram;
    }
    
    /**
     * Returns all programs that are contained in this program time.
     * @return A Program array.
     */
    public Program[] getAllPrograms() {
      ArrayList<Program> list = new ArrayList<Program>();
      if (mProgram != null) {
        list.add(mProgram);
      }
      if(mAdditionalPrograms != null && mAdditionalPrograms.length > 0) {
        list.addAll(Arrays.asList(mAdditionalPrograms));
      }
      return list.toArray(new Program[list.size()]);
    }

    /**
     * Set the Start-Time
     * @param start new start-time
     */
    public void setStart(Date start) {
        mStart = Calendar.getInstance();
        mStart.setTime(start);
    }

    /**
     * Returns the Start-Time
     * @return current start-time
     */
    public Date getStart() {
        return mStart.getTime();
    }

    /**
     * Returns the Start-Time as Calendar
     * @return current start-time
     */
    public Calendar getStartAsCalendar() {
        return mStart;
    }

    /**
     * Sets the End-Time
     * @param end new end-time
     */
    public void setEnd(Date end) {
        mEnd = Calendar.getInstance();
        mEnd.setTimeInMillis(end.getTime());
        findAddionalProgramForEnd();
    }
    
    private void findAddionalProgramForEnd() {
      /* Taken from WtvcgScheduler2 and changed for CapturePlugin */
      if (mProgram == null) {
        return;
      }
      ArrayList<Program> additionalPrograms = new ArrayList<Program>(0);
      
      int startTime = mStart.get(Calendar.HOUR_OF_DAY) * 60 + mStart.get(Calendar.MINUTE);
      int day = mStart.get(Calendar.DAY_OF_MONTH);
      
      int endTime = mEnd.get(Calendar.HOUR_OF_DAY) * 60 + mEnd.get(Calendar.MINUTE);
      
      if(mEnd.get(Calendar.DAY_OF_MONTH) != day) {
        endTime += 60 * 24;
      }
      
      devplugin.Date programDate = mProgram.getDate();
      Channel programChannel = mProgram.getChannel();
      
      Iterator<Program> channelDayProgram = Plugin.getPluginManager().getChannelDayProgram(programDate, programChannel);
      
      boolean found = false;
      
      while(channelDayProgram != null && channelDayProgram.hasNext()) {
        Program prog = channelDayProgram.next();

        if(prog.equals(mProgram)) {
          found = true;
        }
        else if(found && prog.getStartTime() >= startTime && (prog.getStartTime() + prog.getLength()) <= endTime) {
          additionalPrograms.add(prog);
        }
        else if(found) {
          break;
        }
      }
      
      if(endTime > 60 * 24) {
        if(programDate.getDayOfMonth() == day) {
          programDate = programDate.addDays(1);
          
          channelDayProgram = Plugin.getPluginManager().getChannelDayProgram(programDate, programChannel);
          
          endTime -= 60 * 24;
          
          while(channelDayProgram != null && channelDayProgram.hasNext()) {
            Program prog = channelDayProgram.next();
            
            if((prog.getStartTime() + prog.getLength()) <= endTime) {
              additionalPrograms.add(prog);
            }
            else {
              break;
            }
          }
        }
      }
      
      mAdditionalPrograms = additionalPrograms.toArray(new Program[additionalPrograms.size()]);
    }

    /**
     * Returns the End-Time
     * @return current end-time
     */
    public Date getEnd() {
        return mEnd.getTime();
    }

    /**
     * @return current end-time as calendar
     */
    public Calendar getEndAsCalendar() {
        return mEnd;
    }

    /**
     * Add Minutes to the Start-Time
     * @param min Minutes to add
     */
    public void addMinutesToStart(int min) {
        mStart.add(Calendar.MINUTE, min);
    }

    /**
     * Add Minutes to End-Time
     * @param min Minutes to add
     */
    public void addMinutesToEnd(int min) {
        mEnd.add(Calendar.MINUTE, min);
    }

    /**
     * Get the Title
     * @return Title
     */
    public String getTitle() {
      return mTitle;
    }

    /**
     * Set the Title
     * @param title new Title
     */
    public void setTitle(String title) {
      mTitle = title;
    }

    /**
     * Clone
     */
    @Override
    public Object clone() {
        return new ProgramTime(this);
    }

  /**
   * Save Data into Stream.
   * @param out save to this stream
   * @throws IOException problems during save operation
   */
  public void writeData(final ObjectOutputStream out) throws IOException {
    out.writeInt(3);
    out.writeObject(mStart.getTime());
    out.writeObject(mEnd.getTime());
    out.writeObject(mProgram.getID());
    mProgram.getDate().writeData((java.io.DataOutput)out);
    out.writeObject(mTitle);
    
    out.writeBoolean(mAdditionalPrograms != null);
    
    if(mAdditionalPrograms != null) {
      out.writeInt(mAdditionalPrograms.length);
      
      for(Program prog : mAdditionalPrograms) {
        out.writeUTF(prog.getID());
        prog.getDate().writeData((java.io.DataOutput)out);
      }
    }
  }

  /**
   * Read Data from Stream.
   * @param in read data from this stream
   * @throws IOException problems during load operation
   * @throws ClassNotFoundException problem during class creation
   */
  public void readData(final ObjectInputStream in) throws IOException, ClassNotFoundException {

    int version = in.readInt();

    Date start = (Date) in.readObject();
    Date end = (Date) in.readObject();

    String id = (String) in.readObject();
    devplugin.Date date = devplugin.Date.readData(in);

    Program aktP = Plugin.getPluginManager().getProgram(date, id);
    if (aktP != null) {
      mProgram = aktP;
    }

    setStart(start);
    setEnd(end);

    if (version > 1) {
      setTitle((String) in.readObject());
    }
    
    if(version >= 3) {
      if(in.readBoolean()) {
        int n = in.readInt();
        
        ArrayList<Program> tempList = new ArrayList<Program>();
        
        for(int i = 0; i < n; i++) {
          id = in.readUTF();
          devplugin.Date programDate = devplugin.Date.readData(in);
          
          Program prog = Plugin.getPluginManager().getProgram(programDate, id);
          
          if(prog != null) {
            tempList.add(prog);
          }
        }
        
        if(!tempList.isEmpty()) {
          mAdditionalPrograms = tempList.toArray(new Program[tempList.size()]);
        }
      }
    }
  }

    /**
     * Checks if the program of this ProgramTime was deleted
     * or if it has not updated instead renew the instance of it.
     *
     * @return <code>true</code> if the program was deleted, <code>false</code> instead.
     * @since 2.11
     */
    public boolean checkIfRemovedOrUpdateInstead() {
      if (mProgram == null) {
        return true;
      }
      if(mProgram.getProgramState() == Program.WAS_UPDATED_STATE) {
        mProgram = Plugin.getPluginManager().getProgram(mProgram.getDate(), mProgram.getID());
      } else if(mProgram.getProgramState() == Program.WAS_DELETED_STATE) {
        return true;
      }

      return false;
    }

    /**
     * Calculates the time difference between start and end in minutes
     *
     * @return Time in Minutes between start and end
     * @since 2.6.1
     */
    public int getLength() {
        long diff = mEnd.getTimeInMillis() - mStart.getTimeInMillis();
        return Math.round((float)diff / (60000));
    }
}