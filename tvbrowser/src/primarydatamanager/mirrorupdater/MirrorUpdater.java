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
package primarydatamanager.mirrorupdater;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import primarydatamanager.mirrorupdater.config.Configuration;
import primarydatamanager.mirrorupdater.config.PropertiesConfiguration;
import primarydatamanager.mirrorupdater.data.DataSource;
import primarydatamanager.mirrorupdater.data.DataTarget;
import tvbrowserdataservice.file.*;
import tvbrowserdataservice.file.ChannelList;
import tvbrowserdataservice.file.DayProgramFile;
import tvbrowserdataservice.file.Mirror;
import util.io.VerySimpleFormatter;
import devplugin.Channel;
import devplugin.Date;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class MirrorUpdater {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(MirrorUpdater.class.getName());
  
  private static final int MAX_DAYS_WITHOUT_DATA = 7;

  private DataSource mDataSource;
  private DataTarget mDataTarget;
  private String mPrimaryServerUrl;
  private int mMirrorWeight;
  
  private Date mDeadlineDay;
  
  private String[] mTargetFileArr;
  
  private static final String PROGRAM_TITLE = "MirrorUpdater for TV-Browser v0.2";



  public MirrorUpdater(Configuration config) {
    mDataSource = config.getDataSource();
    mDataTarget = config.getDataTarget();
    mPrimaryServerUrl = config.getPrimaryServerUrl();
    mMirrorWeight = config.getMirrorWeight();
    
    mDeadlineDay = new Date().addDays(-2);
  }



  public void updateMirror() throws UpdateException {
    try {
      // First have a look what we already have on the mirror
      mTargetFileArr = mDataTarget.listFiles();
      
      // Delete the outdated files
      for (int i = 0; i < mTargetFileArr.length; i++) {
        if (isDayProgramFile(mTargetFileArr[i])) {
          if (! dayProgramFileIsUpToDate(mTargetFileArr[i])) {
            mDataTarget.deleteFile(mTargetFileArr[i]);
          }
        }
      }
      
      // Get the channellist
      Channel[] channelArr = updateChannelList();
      
      // Update the day programs for all channels
      for (int i = 0; i < channelArr.length; i++) {
        for (int j = 0; j < DayProgramFile.LEVEL_ARR.length; j++) {
          updateDayProgramsFor(channelArr[i], DayProgramFile.LEVEL_ARR[j].getId());
        }
      }
      
      // Update the meta files
      updateMetaFiles(channelArr);
    }
    finally {
      // Close data source and data target
      mDataSource.close(); 
      mDataTarget.close();
    }
  }
  
  
  
  private boolean isDayProgramFile(String fileName) {
    return fileName.endsWith(".prog.gz");
  }



  private boolean dayProgramFileIsUpToDate(String fileName)
    throws UpdateException
  {
    try {
      int year = Integer.parseInt(fileName.substring(0, 4));
      int month = Integer.parseInt(fileName.substring(5, 7));
      int day = Integer.parseInt(fileName.substring(8, 10));
      Date date = new Date(year, month, day);
      
      return date.compareTo(mDeadlineDay) >= 0;
    }
    catch (Exception exc) {
      throw new UpdateException("Day program file name has wrong pattern: "
        + fileName, exc);
    }
  }



  private Channel[] updateChannelList() throws UpdateException {
    byte[] data = mDataSource.loadFile(ChannelList.FILE_NAME);
    
    // Read the channel list
    Channel[] channelArr;
    try {
      ByteArrayInputStream stream = new ByteArrayInputStream(data);
      
      ChannelList list = new ChannelList();
      list.readFromStream(stream, null);
      channelArr = list.createChannelArray();
    }
    catch (Exception exc) {
      throw new UpdateException("Reading channel list failed", exc);
    }
    
    // Store the (new) channel list
    mDataTarget.writeFile(ChannelList.FILE_NAME, data);
    
    return channelArr;
  }



  public void updateDayProgramsFor(Channel channel, String level)
    throws UpdateException
  {
    Date date = new Date();
    int daysWithNoData = 0;
    
    while (daysWithNoData < MAX_DAYS_WITHOUT_DATA) {
      String completeFileName = DayProgramFile.getProgramFileName(date,
        channel.getCountry(), channel.getId(), level);
        
      // Check which version is on the source
      // version -1 means there is no version
      int versionAtSource = -1;
      if (mDataSource.fileExists(completeFileName)) {
        versionAtSource = 1;
        boolean finished = false;
        do {
          String updateFileName = DayProgramFile.getProgramFileName(date,
            channel.getCountry(), channel.getId(), level, versionAtSource);
          if (mDataSource.fileExists(updateFileName)) {
            // There is an update file for the current version
            // -> The version of the complete file must be at least one higher
            versionAtSource++;
          } else {
            finished = true;
          }
        } while (! finished);
      }
      
      if (versionAtSource == -1) {
        daysWithNoData++;
      } else {
        daysWithNoData = 0;
        
        // Check which version is on the mirror
        // version -1 means there is no version
        int versionOnMirror = -1;
        if (fileIsOnMirror(completeFileName)) {
          versionOnMirror = 1;
          boolean finished = false;
          do {
            String updateFileName = DayProgramFile.getProgramFileName(date,
              channel.getCountry(), channel.getId(), level, versionOnMirror);
            if (fileIsOnMirror(updateFileName)) {
              // There is an update file for the current version
              // -> The version of the complete file must be at least one higher
              versionOnMirror++;
            } else {
              finished = true;
            }
          } while (! finished);
        }
        
        // Check whether the mirror needs an update
        if (versionOnMirror < versionAtSource) {
          if (versionOnMirror == -1) {
            mLog.fine("Adding version " + versionAtSource
              + ": " + completeFileName);
          } else {
            mLog.fine("Updating from version "
              + versionOnMirror + " to " + versionAtSource
              + ": " + completeFileName);
          }
          // Update the mirror
          byte[] data = mDataSource.loadFile(completeFileName);
          mDataTarget.writeFile(completeFileName, data);
          
          for (int version = 0; version < versionAtSource; version++) {
            String updateFileName = DayProgramFile.getProgramFileName(date,
              channel.getCountry(), channel.getId(), level, version);

            data = mDataSource.loadFile(updateFileName);
            mDataTarget.writeFile(updateFileName, data);
          }
        } else {
          mLog.fine("File already up to date (version " + versionOnMirror + "): "
            + completeFileName);
        }
      }
      
      // Go on with the next day
      date = date.addDays(1);
    }
  }



  private boolean fileIsOnMirror(String fileName) {
    for (int i = 0; i < mTargetFileArr.length; i++) {
      if (mTargetFileArr[i].equals(fileName)) {
        return true;
      }
    }
    
    return false;
  }



  private void updateMetaFiles(Channel[] channelArr) throws UpdateException {
    byte[] data;
    
    // Create the primaryserver file
    data = mPrimaryServerUrl.getBytes();
    mDataTarget.writeFile("primaryserver", data);

    // Copy the mirrorlist.gz
    data = mDataSource.loadFile(Mirror.MIRROR_LIST_FILE_NAME);
    mDataTarget.writeFile(Mirror.MIRROR_LIST_FILE_NAME, data);

    // Copy the summary.gz
    data = mDataSource.loadFile(SummaryFile.SUMMARY_FILE_NAME);
    mDataTarget.writeFile(SummaryFile.SUMMARY_FILE_NAME, data);

    // Create the weight file
    if (mMirrorWeight>=0) {  // don't change the weight file, if the weight is invalid
      data = Integer.toString(mMirrorWeight).getBytes();
      mDataTarget.writeFile("weight", data);
    }
    
    // Create the lastupdate file
    Calendar cal = Calendar.getInstance();
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH) + 1;
    int day = cal.get(Calendar.DAY_OF_MONTH);
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    int minute = cal.get(Calendar.MINUTE);
    int second = cal.get(Calendar.SECOND);
    String lastUpdate = year + "-"
      + ((month < 10)  ? "0" : "") + month  + "-"
      + ((day < 10)    ? "0" : "") + day    + " "
      + ((hour < 10)   ? "0" : "") + hour   + ":"
      + ((minute < 10) ? "0" : "") + minute + ":"
      + ((second < 10) ? "0" : "") + second;
    data = lastUpdate.getBytes();
    mDataTarget.writeFile("lastupdate", data);
    
    // Create the index.html
    String html = createIndexHtml(channelArr);
    mDataTarget.writeFile("index.html", html.getBytes());
  }



  private String createIndexHtml(Channel[] channelArr) {
    StringBuffer buffer = new StringBuffer();
    
    buffer.append("<html><head>");
    buffer.append("<title>TV-Browser Mirror</title>");
    buffer.append("</head><body>");
    buffer.append("<p>This is a mirror of <code>");
    buffer.append(mPrimaryServerUrl + "</code> for the ");
    buffer.append("<a href=\"http://tvbrowser.sourceforge.net\">TV-Browser project</a>.</p>");
    
    buffer.append("<p><b>Warning:</b> The data provided here may only be used ");
    buffer.append("by the TV-Browser project. Any other use is illegal!</p>");
    
    buffer.append("The mirror has a weight of <code>" + mMirrorWeight + "</code> ");
    buffer.append("and was last updated on <code>");
    DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
      DateFormat.MEDIUM, Locale.UK);
    buffer.append(format.format(new java.util.Date()));
    buffer.append("</code>.</p>");

    buffer.append("<p>It contains the TV-Data of the following channels:");
    buffer.append("<ul>");
    for (int i = 0; i < channelArr.length; i++) {
      buffer.append("<li><code>" + channelArr[i].getName()
        + "</code> from <code>" + channelArr[i].getCountry() + "</code></li>");
    }
    buffer.append("</ul></p>");

    buffer.append("</body></html>");
    
    return buffer.toString();
  }



  public static void main(String[] args) {
    System.out.println(PROGRAM_TITLE);
    
    // setup logging
    try {
      // Get the default Logger
      Logger mainLogger = Logger.getLogger("");
      mainLogger.setLevel(Level.FINEST);
      
      Handler consoleHandler = mainLogger.getHandlers()[0];
      consoleHandler.setLevel(Level.FINEST);
      consoleHandler.setFormatter(new VerySimpleFormatter());
      
      // Add a file handler
      new File("log").mkdir();
      Handler fileHandler = new FileHandler("log/mirrorupdater.log", 50000, 2, true);
      fileHandler.setLevel(Level.INFO);
      mainLogger.addHandler(fileHandler);
    }
    catch (IOException exc) {
      System.out.println("Can't create log file");
    }

    // Set the String to use for indicating the user agent in http requests
    System.setProperty("http.agent", PROGRAM_TITLE); 

    // Start the update    
    String propertiesFileName = "MirrorUpdater.ini";
    if (args.length > 0) {
      propertiesFileName = args[0];
    }
    
    try {
      Configuration config = new PropertiesConfiguration(propertiesFileName);
      MirrorUpdater updater = new MirrorUpdater(config);
      updater.updateMirror();
    }
    catch (UpdateException exc) {
      exc.printStackTrace();
    }
  }

}
