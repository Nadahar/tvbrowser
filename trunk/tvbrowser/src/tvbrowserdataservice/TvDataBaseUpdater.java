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

import tvbrowserdataservice.file.*;
import tvbrowserdataservice.file.DayProgramFile;
import tvbrowserdataservice.file.FileFormatException;
import tvbrowserdataservice.file.ProgramFieldType;
import tvbrowserdataservice.file.ProgramFrame;
import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import tvdataservice.TvDataBase;
import util.exc.TvBrowserException;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class TvDataBaseUpdater {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(TvDataBaseUpdater.class.getName());
  
  private TvBrowserDataService mDataService;
  private TvDataBase mDataBase;
  
  private HashSet mUpdateJobSet;
  
  
  public TvDataBaseUpdater(TvBrowserDataService dataService,
    TvDataBase dataBase)
  {
    mDataService = dataService;
    mDataBase = dataBase;
    
    mUpdateJobSet = new HashSet();
  }
  
  
  public void addUpdateJobForDayProgramFile(String fileName)
    throws TvBrowserException
  {
    // Parse the information from the fileName
    // E.g. '2003-10-04_de_premiere-1_base_full.prog.gz'
    Date date;
    Channel channel;
    try {
      int year = Integer.parseInt(fileName.substring(0, 4));
      int month = Integer.parseInt(fileName.substring(5, 7));
      int day = Integer.parseInt(fileName.substring(8, 10));
      date = new Date(year, month, day);
      
      String country = fileName.substring(11, 13);
      int underscorePos = fileName.indexOf('_', 14);
      String channelName = fileName.substring(14, underscorePos);
      
      channel = mDataService.getChannel(country, channelName);
      if (channel == null) {
        throw new TvBrowserException(TvDataBaseUpdater.class, "error.1",
          "Channel not found: {0} from {1}", channelName, country);
      }
    }
    catch (Exception exc) {
      throw new TvBrowserException(TvDataBaseUpdater.class, "error.2",
        "Program file name has wrong syntax: {0}", fileName, exc);
    }
    
    addUpdateJob(date, channel);
  }
  
  
  
  public synchronized void addUpdateJob(Date date, Channel channel) {
    mUpdateJobSet.add(new UpdateJob(date, channel));
  }



  public void updateTvDataBase() {
    for (Iterator iter = mUpdateJobSet.iterator(); iter.hasNext();) {
      UpdateJob updateJob = (UpdateJob) iter.next();
      
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



  private MutableChannelDayProgram createChannelDayProgram(Date date,
    Channel channel)
    throws TvBrowserException
  {
    // Create a DayProgramFile that contains the data of all levels
    DayProgramFile prog = new DayProgramFile();
    
    for (int i = 0; i < DayProgramFile.LEVEL_ARR.length; i++) {
      String level = DayProgramFile.LEVEL_ARR[i];
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
        "Program frame with ID {0} has no start time.",
        new Integer(frame.getId()));
    }
    int startTime = field.getTimeData();
    
    MutableProgram program
      = new MutableProgram(channel, date, startTime / 60, startTime % 60);

    // end time
    field = frame.getProgramFieldOfType(ProgramFieldType.END_TIME_TYPE);
    if (field != null) {
      int endTime = field.getTimeData();
      if (endTime < startTime) {
        // program ends the next day
        endTime += 24 * 60;
      }
      int length = endTime - startTime;
      program.setLength(length);
    }

    // title
    field = frame.getProgramFieldOfType(ProgramFieldType.TITLE_TYPE);
    if (field != null) {
      program.setTitle(field.getTextData());
    }

    // TODO: Has no expression in devplugin.Program: original title
    // field = frame.getProgramFieldOfType(ProgramFieldType.ORIGINAL_TITLE_TYPE);
    
    // TODO: Has no expression in devplugin.Program: episode
    // field = frame.getProgramFieldOfType(ProgramFieldType.EPISODE_TYPE);
    
    // TODO: Has no expression in devplugin.Program: original episode
    // field = frame.getProgramFieldOfType(ProgramFieldType.ORIGINAL_EPISODE_TYPE);
    
    // short description
    field = frame.getProgramFieldOfType(ProgramFieldType.SHORT_DESCRIPTION_TYPE);
    if (field != null) {
      program.setShortInfo(field.getTextData());
    }
    
    // description
    field = frame.getProgramFieldOfType(ProgramFieldType.DESCRIPTION_TYPE);
    if (field != null) {
      program.setDescription(field.getTextData());
    }

    // image
    field = frame.getProgramFieldOfType(ProgramFieldType.IMAGE_TYPE);
    if (field != null) {
      program.setPicture(field.getBinaryData());
    }

    // actor list
    field = frame.getProgramFieldOfType(ProgramFieldType.ACTOR_LIST_TYPE);
    if (field != null) {
      program.setActors(field.getTextData());
    }

    // TODO: Has no expression in devplugin.Program: director
    // field = frame.getProgramFieldOfType(ProgramFieldType.DIRECTOR_TYPE);

    // TODO: Has no expression in devplugin.Program: showview number
    // field = frame.getProgramFieldOfType(ProgramFieldType.SHOWVIEW_NR_TYPE);
    
    // info bits
    field = frame.getProgramFieldOfType(ProgramFieldType.INFO_TYPE);
    if (field != null) {
      program.setInfo(field.getIntData());
    }

    // TODO: Has no expression in devplugin.Program: age limit
    // field = frame.getProgramFieldOfType(ProgramFieldType.AGE_LIMIT_TYPE);
    
    // film url
    field = frame.getProgramFieldOfType(ProgramFieldType.URL_TYPE);
    if (field != null) {
      program.setURL(field.getTextData());
    }

    // TODO: Has no expression in devplugin.Program: genre                         
    // field = frame.getProgramFieldOfType(ProgramFieldType.GENRE_TYPE);
    
    // TODO: Has no expression in devplugin.Program: origin
    // field = frame.getProgramFieldOfType(ProgramFieldType.ORIGIN_TYPE);
    
    // TODO: Has no expression in devplugin.Program: net playing time
    // field = frame.getProgramFieldOfType(ProgramFieldType.NET_PLAYING_TIME);

    return program;
  }
  
  
  // inner class UpdateJob
  
  
  private class UpdateJob {
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
