/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package tvbrowserdataservice;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import tvbrowserdataservice.file.DayProgramFile;
import tvbrowserdataservice.file.ProgramField;
import tvbrowserdataservice.file.ProgramFrame;
import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import util.io.FileFormatException;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgressMonitor;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class TvDataBaseUpdater {

  private static final Logger mLog
    = Logger.getLogger(TvDataBaseUpdater.class.getName());
  
  private TvBrowserDataService mDataService;
  private TvDataUpdateManager mDataBase;
  
  private HashSet<UpdateJob> mUpdateJobSet;
  
  
  /**
   * Creates a new TvDataBaseUpdater.
   * 
   * @param dataService The dataservice of the data to update.
   * @param dataBase The TvDataBaseUpdateManager.
   */
  public TvDataBaseUpdater(TvBrowserDataService dataService,
    TvDataUpdateManager dataBase)
  {
    mDataService = dataService;
    mDataBase = dataBase;
    
    mUpdateJobSet = new HashSet<UpdateJob>();
  }
  
  
  protected void addUpdateJobForDayProgramFile(String fileName)
    throws TvBrowserException
  {
    // Parse the information from the fileName
    // E.g. '2003-10-04_de_premiere-1_base_full.prog.gz'
    Date date = DayProgramFile.getDateFromFileName(fileName);
    String country = DayProgramFile.getCountryFromFileName(fileName);
    String channelName = DayProgramFile.getChannelNameFromFileName(fileName);
    
    Channel channel = mDataService.getChannel(country, channelName);
    if (channel == null) {
      throw new TvBrowserException(TvDataBaseUpdater.class, "error.1",
        "Channel not found: {0} from {1}", channelName, country);
    }
    
    addUpdateJob(date, channel);
  }
  
  
  
  protected synchronized void addUpdateJob(Date date, Channel channel) {
    mUpdateJobSet.add(new UpdateJob(date, channel));
  }



  protected void updateTvDataBase(ProgressMonitor monitor) {
    synchronized(mUpdateJobSet) {
      monitor.setMaximum(mUpdateJobSet.size());
      int i=0;
      for (Iterator<UpdateJob> iter = mUpdateJobSet.iterator(); iter.hasNext();) {
        monitor.setValue(i++);
        UpdateJob updateJob = iter.next();
        
        try {
          MutableChannelDayProgram prog
            = createChannelDayProgram(updateJob.getDate(), updateJob.getChannel());
            
          mDataBase.updateDayProgram(prog);
        }
        catch (TvBrowserException exc) {
          mLog.log(Level.WARNING, "Updating day program of "
            + updateJob.getChannel() + " for " + updateJob.getDate() + " failed",
            exc);
        }
      }
    }
  }



  private MutableChannelDayProgram createChannelDayProgram(Date date,
    Channel channel)
    throws TvBrowserException
  {
    // Create a DayProgramFile that contains the data of all levels
    DayProgramFile prog = new DayProgramFile();
    
    for (int i = 0; i < DayProgramFile.LEVEL_ARR.length; i++) {
      String level = DayProgramFile.LEVEL_ARR[i].getId();
      String fileName = DayProgramFile.getProgramFileName(date,
        channel.getCountry(), channel.getId(), level);
      File file = new File(mDataService.getDataDir(), fileName);
      
      if (file.exists()) {
        // Load this level
        DayProgramFile levelProg = new DayProgramFile();
        try {
          levelProg.readFromFile(file);
        }
        catch (Exception exc) {
          // This file must be corrupt -> delete it
          file.delete();
          
          throw new TvBrowserException(getClass(), "error.1",
            "Could not load program file {0}. It must be currupt, so it was deleted.",
            file.getAbsolutePath(), exc);
        }
        
        // Include this level
        try {
          prog.merge(levelProg);
        }
        catch (FileFormatException exc) {
          throw new TvBrowserException(getClass(), "error.2",
            "Including level {0} failed", level, exc);
        }
      }
    }
    
    // Convert it into a MutableChannelDayProgram
    try {
      MutableChannelDayProgram target = new MutableChannelDayProgram(date, channel);
      for (int i = 0; i < prog.getProgramFrameCount(); i++) {
        ProgramFrame frame = prog.getProgramFrameAt(i);
        target.addProgram(createProgramFromFrame(frame, date, channel));
      }    
      return target;
    }
    catch (TvBrowserException exc) {
      throw new TvBrowserException(getClass(), "error.2",
        "Converting Day program of channel {0} for {1} failed",
        channel, date, exc);
    }
  }



  private Program createProgramFromFrame(ProgramFrame frame, Date date,
    Channel channel)
    throws TvBrowserException
  {
    ProgramField field;
    
    // start time
    field = frame.getProgramFieldOfType(ProgramFieldType.START_TIME_TYPE);
    if (field == null) {
      throw new TvBrowserException(getClass(), "error.3",
        "Program frame with ID {0} has no start time.", frame.getId());
    }
    int startTime = field.getTimeData();
    
    MutableProgram program
      = new MutableProgram(channel, date, startTime / 60, startTime % 60, true);
      
    int fieldCount = frame.getProgramFieldCount();
    for (int i = 0; i < fieldCount; i++) {
      field = frame.getProgramFieldAt(i);
      ProgramFieldType type = field.getType();
      if (type.getFormat() == ProgramFieldType.BINARY_FORMAT) {
        program.setBinaryField(type, field.getBinaryData());
      }
      else if (type.getFormat() == ProgramFieldType.TEXT_FORMAT) {
        program.setTextField(type, field.getTextData());
      }
      else if (type.getFormat() == ProgramFieldType.INT_FORMAT) {
        program.setIntField(type, field.getIntData());
      }
      else if (type.getFormat() == ProgramFieldType.TIME_FORMAT) {
        program.setTimeField(type, field.getTimeData());
      }
    }

    program.setProgramLoadingIsComplete();
    
    return program;
  }
  
  
  // inner class UpdateJob
  
  
  private static class UpdateJob {
    private Date mDate;
    private Channel mChannel;
    private String asString;
    
  
    /**
     * @param date
     * @param channel
     */
    public UpdateJob(Date date, Channel channel) {
      if (date == null) {
        throw new IllegalArgumentException("date is null");
      }
      if (channel == null) {
        throw new IllegalArgumentException("channel is null");
      }
      
      mDate = date;
      mChannel = channel;
    }


    /**
     * @return
     */
    public Channel getChannel() {
      return mChannel;
    }


    /**
     * @return
     */
    public Date getDate() {
      return mDate;
    }
    
    
    public String toString() {
      // NOTE: Needed to create the hash code and to compare
      if (asString == null) {
        asString = mDate.toString() + "_" + mChannel.getId() + "_"
          + mChannel.getCountry();
      }
      return asString;
    }
    
    
    public boolean equals(Object obj) {
      if (obj instanceof UpdateJob) {
        UpdateJob job = (UpdateJob) obj;
        return toString().equals(job.toString());
      } else {
        return false;
      }
    }
    
    
    public int hashCode() {
      return toString().hashCode();
    }

  }

}
