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

import java.io.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import devplugin.Channel;
import devplugin.Date;
import primarydatamanager.mirrorupdater.config.Configuration;
import primarydatamanager.mirrorupdater.config.PropertiesConfiguration;
import primarydatamanager.mirrorupdater.data.DataSource;
import primarydatamanager.mirrorupdater.data.DataTarget;
import tvbrowserdataservice.file.ChannelList;
import tvbrowserdataservice.file.DayProgramFile;
import tvbrowserdataservice.file.Mirror;
import util.io.IOUtilities;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class MirrorUpdater {

  private DataSource mDataSource;
  private DataTarget mDataTarget;
  private String mPrimaryServerUrl;
  private int mMirrorWeight;
  
  private Date mDeadlineDay;
  
  private String[] mTargetFileArr;



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
          updateDayProgramsFor(channelArr[i], DayProgramFile.LEVEL_ARR[j]);
        }
      }
      
      // Update the meta files
      updateMetaFiles(channelArr);
      
      // Check for a mirror list
      updateMirrorList();
    }
    finally {
      // Close the data target
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
      Date date = new Date();
      
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
    Date date = mDeadlineDay;
    int daysWithNoData = 0;
    
    while (daysWithNoData < 7) {
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
            System.out.println("Adding version " + versionAtSource
              + ": " + completeFileName);
          } else {
            System.out.println("Updating from version "
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
    byte[] data = mPrimaryServerUrl.getBytes();
    mDataTarget.writeFile("primaryserver", data);

    data = Integer.toString(mMirrorWeight).getBytes();
    mDataTarget.writeFile("weight", data);
    
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



  private void updateMirrorList() throws UpdateException {
    // Check whether there is a mirrorlist.txt
    if (! mDataSource.fileExists("mirrorlist.txt")) {
      // Nothing to do
      return;
    }
     
    // Load the mirrorlist.txt
    ArrayList mirrorList = new ArrayList();
    try {
      byte[] data = mDataSource.loadFile("mirrorlist.txt");
      ByteArrayInputStream stream = new ByteArrayInputStream(data);
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.length() != 0) {
          mirrorList.add(new Mirror(line, 100));
        }
      }
    }
    catch (Exception exc) {
      throw new UpdateException("Loading mirror list failed", exc);
    }
    
    // Now update the weights. Use the old mirror list if a mirror is not
    // available
    Mirror[] oldMirrorArr = null;
    for (int listIdx = 0; listIdx < mirrorList.size(); listIdx++) {
      Mirror mirror = (Mirror) mirrorList.get(listIdx);
      
      int weight = getMirrorWeight(mirror);
      if (weight >= 0) {
        mirror.setWeight(weight);
      } else {
        // We didn't get the weight -> Try to get the old weight
        if (oldMirrorArr == null) {
          oldMirrorArr = loadMirrorList();
        }
        
        for (int i = 0; i < oldMirrorArr.length; i++) {
          if (oldMirrorArr[i].getUrl().equals(mirror.getUrl())) {
            // This is the same mirror -> use the old weight
            mirror.setWeight(oldMirrorArr[i].getWeight());
          }
        }
      }
    }
    
    // Save the mirrorlist
    try {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      Mirror[] mirrorArr = new Mirror[mirrorList.size()];
      mirrorList.toArray(mirrorArr);
      
      Mirror.writeMirrorListToStream(stream, mirrorArr);
      byte[] data = stream.toByteArray();
      
      mDataTarget.writeFile(Mirror.MIRROR_LIST_FILE_NAME, data);
    }
    catch (Exception exc) {
      throw new UpdateException("Saving mirror list failed", exc);
    }
  }
    
    
  private Mirror[] loadMirrorList() throws UpdateException {
    try {
      byte[] data = mDataSource.loadFile(Mirror.MIRROR_LIST_FILE_NAME);
      ByteArrayInputStream stream = new ByteArrayInputStream(data);
      return Mirror.readMirrorListFromStream(stream);
    }
    catch (Exception exc) {
      throw new UpdateException("Loading mirror list failed", exc);
    }
  }


  private int getMirrorWeight(Mirror mirror) {
    try {
      String url = mirror.getUrl() + "/weight";
      byte[] data = IOUtilities.loadFileFromHttpServer(new URL(url));
      String asString = new String(data);
      return Integer.parseInt(asString);
    }
    catch (Exception exc) {
      System.out.println("Getting mirror weight of " + mirror.getUrl()
        + " failed");
      return -1;
    }
  }



  public static void main(String[] args) {
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
