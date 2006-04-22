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
package tvbrowser.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;

import tvbrowser.core.data.OnDemandDayProgramFile;
import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Date;
import devplugin.Program;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class TvDataBase {

  /** The logger for this class. */
  private static java.util.logging.Logger mLog = java.util.logging.Logger
      .getLogger(TvDataBase.class.getName());

  private static final String INVENTORY_FILE = "tv-data-inventory.dat";

  /** The singleton. */
  private static TvDataBase mSingleton;

  /** The TV data cache. */
  private ValueCache mTvDataHash;

  private ArrayList mListenerList;

  /** Contains date objects for each date for which we have a tv listing */
  private HashSet mAvailableDateSet;

  private TvDataInventory mTvDataInventory;

  private TvDataBase() {
    mTvDataHash = new ValueCache();
    mListenerList = new ArrayList();
    mAvailableDateSet = new HashSet();
    updateAvailableDateSet();

    TvDataUpdater.getInstance().addTvDataUpdateListener(
        new TvDataUpdateListener() {
          public void tvDataUpdateStarted() {}

          public void tvDataUpdateFinished() {
            updateAvailableDateSet();
          }
        });

    // Load inventory
    mTvDataInventory = new TvDataInventory();
    File file = new File(Settings.getUserDirectoryName(), INVENTORY_FILE);
    if (file.exists()) {
      try {
        mTvDataInventory.readData(file);
      } catch (Exception exc) {
        mLog.log(Level.WARNING, "Loading tv data inventory failed", exc);
      }
    }
  }

  public static TvDataBase getInstance() {
    if (mSingleton == null) {
      mSingleton = new TvDataBase();
    }

    return mSingleton;
  }

  public void checkTvDataInventory() {
    // Get the channel of the subscribed channels
    Channel[] channelArr = ChannelList.getSubscribedChannels();
    String[] channelIdArr = new String[channelArr.length];
    for (int i = 0; i < channelArr.length; i++) {
      channelIdArr[i] = getChannelKey(channelArr[i]);
    }

    // Inventory pr�fen
    boolean somethingChanged = false;

    File tvDataDir = new File(Settings.propTVDataDirectory.getString());
    File[] tvDataArr = tvDataDir.listFiles();
    if (tvDataArr == null) {
      return;
    }

    // Check whether day programs were removed
    String[] knownProgArr = mTvDataInventory.getKnownDayPrograms();
    for (int progIdx = 0; progIdx < knownProgArr.length; progIdx++) {
      String key = knownProgArr[progIdx];

      // Check whether this file is still present
      // (The key is equal to the file name)
      boolean stillPresent = false;
      for (int i = 0; i < tvDataArr.length; i++) {
        if (tvDataArr[i].getName().equals(key)) {
          stillPresent = true;
          break;
        }
      }

      if (!stillPresent) {
        // This day program was deleted -> Inform the listeners

        // Get the channel and date
        Channel channel = getChannelFromFileName(key, channelArr, channelIdArr);
        Date date = getDateFromFileName(key);
        if ((channel != null) && (date != null)) {
          mLog.info("Day program was deleted by third party: " + date + " on "
              + channel.getName());
          ChannelDayProgram dummyProg = new MutableChannelDayProgram(date,
              channel);
          fireDayProgramDeleted(dummyProg);

          mTvDataInventory.setUnknown(date, channel);
        }
      }
    }

    // Check whether day programs were added or replaced
    for (int fileIdx = 0; fileIdx < tvDataArr.length; fileIdx++) {
      String fileName = tvDataArr[fileIdx].getName();

      // Get the channel and date
      Channel channel = getChannelFromFileName(fileName, channelArr,
          channelIdArr);
      Date date = getDateFromFileName(fileName);
      if ((channel != null) && (date != null)) {
        // Get the version
        int version = (int) tvDataArr[fileIdx].length();

        // Check whether this day program is known
        int knownStatus = mTvDataInventory.getKnownStatus(date, channel,
            version);

        if ((knownStatus == TvDataInventory.UNKNOWN)
            || (knownStatus == TvDataInventory.OTHER_VERSION)) {
          if (!somethingChanged) {
            // This is the first changed day program -> fire update start
            TvDataUpdater.getInstance().fireTvDataUpdateStarted();
          }

          // Inform the listeners
          mLog.info("Day program was changed by third party: " + date + " on "
              + channel.getName());
          ChannelDayProgram newDayProg = getDayProgram(date, channel);
          handleKnownStatus(knownStatus, newDayProg, version);

          somethingChanged = true;
        }
      }
    }

    // fire update finished
    if (somethingChanged) {
      TvDataUpdater.getInstance().fireTvDataUpdateFinished();
    }
  }

  public void close() throws IOException {
    File file = new File(Settings.getUserDirectoryName(), INVENTORY_FILE);
    mTvDataInventory.writeData(file);
  }

  public void addTvDataListener(TvDataBaseListener listener) {
    synchronized (mListenerList) {
      mListenerList.add(listener);
    }
  }

  public void removeTvDataListener(TvDataBaseListener listener) {
    synchronized (mListenerList) {
      mListenerList.remove(listener);
    }
  }

  public ChannelDayProgram getDayProgram(Date date, Channel channel) {

    OnDemandDayProgramFile progFile = getCacheEntry(date, channel, true);

    if (progFile != null) {
      return progFile.getDayProgram();
    } else {
      return null;
    }
  }

  /**
   * Checks all TV-Data for missing length.
   * 
   * @param days The number of days to recalculate
   */
  public synchronized void reCalculateTvData(int days) {
    Channel[] ch = ChannelList.getSubscribedChannels();

    for (int j = 0; j < ch.length; j++)
      for (int i = 0; i < days; i++)
        correctDayProgramFile(Date.getCurrentDate().addDays(i), ch[j]);
  }

  public synchronized void setDayProgram(MutableChannelDayProgram prog) {
    Date date = prog.getDate();
    Channel channel = prog.getChannel();
    String key = getDayProgramKey(date, channel);

    // Create a backup (Rename the old file if it exists)
    File file = getDayProgramFile(date, channel);
    File backupFile = null;
    ChannelDayProgram oldProg = getDayProgram(date, channel);
    if (file.exists()) {
      backupFile = new File(file.getAbsolutePath() + ".backup");
      if (!file.renameTo(backupFile)) {
        // Renaming failed -> There will be no backup
        backupFile = null;
      }
    }

    // Invalidate the old program file from the cache
    OnDemandDayProgramFile oldProgFile = getCacheEntry(date, channel, false);
    if (oldProgFile != null) {
      oldProgFile.setValid(false);

      // Remove the old entry from the cache (if it exists)
      removeCacheEntry(key);
    }

    // Create a new program file
    OnDemandDayProgramFile newProgFile = new OnDemandDayProgramFile(file, prog);

    // Put the new program file in the cache
    addCacheEntry(key, newProgFile);

    // Inform the listeners about adding the new program
    // NOTE: This must happen before saving to give the listeners the chance to
    // change the data and have those changes saved to disk.
    fireDayProgramAdded(prog);

    // Save the new program
    try {
      // Save the day program
      newProgFile.saveDayProgram();

      // Delete the backup
      if (backupFile != null) {
        backupFile.delete();
      }

      // Inform the listeners about deleting the old program
      if (oldProg != null) {
        fireDayProgramDeleted(oldProg);
      }

      // Set the new program to 'known'
      int version = (int) file.length();
      mTvDataInventory.setKnown(date, channel, version);
    } catch (IOException exc) {
      // Remove the new program from the cache
      removeCacheEntry(key);

      // Inform the listeners about removing the new program
      fireDayProgramDeleted(prog);

      // Try to restore the backup
      boolean restoredBackup = false;
      if (backupFile != null) {
        if (backupFile.renameTo(file)) {
          restoredBackup = true;
        } else {
          backupFile.delete();
        }
      }

      // Log the error
      String msg = "Saving program for " + channel + " from " + date
          + " failed.";
      if (restoredBackup) {
        msg += " The old version was restored.";
      }
      mLog.log(Level.WARNING, msg, exc);
    }
  }

  private synchronized OnDemandDayProgramFile getCacheEntry(Date date,
      Channel channel, boolean loadFromDisk) {
    String key = getDayProgramKey(date, channel);

    // Try to get the program from the cache
    OnDemandDayProgramFile progFile = (OnDemandDayProgramFile) mTvDataHash
        .get(key);

    // Try to load the program from disk
    if (loadFromDisk && (progFile == null)) {
      progFile = loadDayProgram(date, channel);
      if (progFile != null) {
        addCacheEntry(key, progFile);
      }
    }

    return progFile;
  }

  private synchronized void addCacheEntry(String key,
      OnDemandDayProgramFile progFile) {
    mTvDataHash.put(key, progFile);
  }

  private synchronized void removeCacheEntry(String key) {
    mTvDataHash.remove(key);
  }

  public static String getDayProgramKey(Date date, Channel channel) {
    // check the input
    if (date == null) {
      throw new NullPointerException("date is null");
    }

    return new StringBuffer(getChannelKey(channel)).append('.').append(
        date.getDateString()).toString();
  }

  public static String getChannelKey(Channel channel) {
    // check the input
    if (channel == null) {
      throw new NullPointerException("channel is null");
    }

    return new StringBuffer(channel.getCountry()).append('_').append(
        channel.getId()).append('_').append(
        channel.getDataService().getClass().getPackage().getName()).toString();
  }

  public boolean isDayProgramAvailable(Date date, Channel channel) {
    File file = getDayProgramFile(date, channel);
    return file.exists();
  }

  /**
   * Deletes expired tvdata files older then lifespan days.
   * 
   * @param lifespan
   *          The number of days to delete from the past
   */
  public void deleteExpiredFiles(int lifespan) {
    if (lifespan < 0) {
      return; // manually
    }
    devplugin.Date d1 = new devplugin.Date();
    final devplugin.Date d = d1.addDays(-lifespan);

    FilenameFilter filter = new java.io.FilenameFilter() {
      public boolean accept(File dir, String name) {
        int p = name.lastIndexOf('.');
        String s = name.substring(p + 1, name.length());
        int val;
        try {
          val = Integer.parseInt(s);
        } catch (NumberFormatException e) {
          return false;
        }
        int year = val / 10000;
        int r = val % 10000;
        int month = r / 100;
        int day = r % 100;
        Date curDate = new Date(year, month, day);
        return curDate.getValue() < d.getValue();
      }
    };

    String tvDataDir = Settings.propTVDataDirectory.getString();
    File fList[] = new File(tvDataDir).listFiles(filter);
    if (fList != null) {
      for (int i = 0; i < fList.length; i++) {
        fList[i].delete();
      }
    }
  }
  
  private synchronized void correctDayProgramFile(Date date,
      Channel channel) {
    File file = getDayProgramFile(date, channel);
    String key = getDayProgramKey(date, channel);
    if (!file.exists())
      return;

    try {
      // Check whether this day program is known
      int version = (int) file.length();
      int knownStatus = mTvDataInventory.getKnownStatus(date, channel, version);

        boolean somethingChanged = calculateMissingLengths(getDayProgram(date,
            channel));
        if (somethingChanged) {
          // Some missing lengths could now be calculated
          // -> Try to save the changes
          
          // We use a temporary file. If saving suceeds we rename it
          File tempFile = new File(file.getAbsolutePath() + ".changed");
          try {
            // Try to save the changed program
            OnDemandDayProgramFile newProgFile = new OnDemandDayProgramFile(
                tempFile, (MutableChannelDayProgram) getDayProgram(date,
                    channel));
            newProgFile.saveDayProgram();
            
            // Saving the changed version succeed -> Delete the original
            file.delete();

            // Use the changed version now
            tempFile.renameTo(file);
            
            // If the old version was known -> Set the new version to known too
            if (knownStatus == TvDataInventory.KNOWN) {
              version = (int) file.length();
              mTvDataInventory.setKnown(date, channel, version);
            }
          } catch (Exception exc) {
            // Saving the changes failed
            // -> remove the temp file and keep the old one
            tempFile.delete();
          }
                    
          OnDemandDayProgramFile progFile = new OnDemandDayProgramFile(file, (MutableChannelDayProgram) getDayProgram(date,
              channel));
          progFile.loadDayProgram();
          
          // Invalidate the old program file from the cache
          OnDemandDayProgramFile oldProgFile = getCacheEntry(date, channel, false);
          if (oldProgFile != null) {
            oldProgFile.setValid(false);

            // Remove the old entry from the cache (if it exists)
            removeCacheEntry(key);
          }
          
          // Put the new program file in the cache
          addCacheEntry(key, progFile);
        }
    } catch (Exception exc) {
      mLog.log(Level.WARNING, "Loading program for " + channel + " from "
          + date + " failed. The file will be deleted...", exc);

      file.delete();
    }
  }
  

  private synchronized OnDemandDayProgramFile loadDayProgram(Date date,
      Channel channel) {
    File file = getDayProgramFile(date, channel);
    if (!file.exists()) {
      return null;
    }

    try {
      // Load the program file
      OnDemandDayProgramFile progFile = new OnDemandDayProgramFile(file, date,
          channel);
      progFile.loadDayProgram();

      return progFile;
    } catch (Exception exc) {
      mLog.log(Level.WARNING, "Loading program for " + channel + " from "
          + date + " failed. The file will be deleted...", exc);

      file.delete();

      return null;
    }
  }

  private File getDayProgramFile(Date date, Channel channel) {
    String fileName = getDayProgramKey(date, channel);

    String tvDataDir = Settings.propTVDataDirectory.getString();
    return new File(tvDataDir, fileName);
  }

  private Channel getChannelFromFileName(String fileName, Channel[] channelArr,
      String[] channelIdArr) {
    for (int i = 0; i < channelIdArr.length; i++) {
      if (fileName.startsWith(channelIdArr[i])) {
        return channelArr[i];
      }
    }

    return null;
  }

  private Date getDateFromFileName(String fileName) {
    int dotIdx = fileName.lastIndexOf('.');
    if (dotIdx == -1) {
      return null;
    }

    String valueAsString = fileName.substring(dotIdx + 1);
    try {
      long value = Long.parseLong(valueAsString);
      return Date.createDateFromValue(value);
    } catch (NumberFormatException exc) {
      return null;
    }
  }

  /**
   * Returns true, if tv data is available on disk for the given date.
   * 
   * @param date
   *          The date to check.
   * @return if the data is available.
   */
  public boolean dataAvailable(Date date) {

    return mAvailableDateSet.contains(date);
  }

  private void updateAvailableDateSet() {

    String tvDataDirStr = Settings.propTVDataDirectory.getString();
    File tvDataDir = new File(tvDataDirStr);
    if (!tvDataDir.exists()) {
      return;
    }

    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        if (name.length() < 8)
          return false;
        String dateStr = name.substring(name.length() - 8);
        try {
          int year = Integer.parseInt(dateStr.substring(0, 4));
          int month = Integer.parseInt(dateStr.substring(4, 6));
          int day = Integer.parseInt(dateStr.substring(6, 8));
        } catch (NumberFormatException e) {
          return false;
        }
        return true;
      }
    };

    String fList[] = tvDataDir.list(filter);
    for (int i = 0; i < fList.length; i++) {
      if (fList[i].length() > 8) {
        String dateStr = fList[i].substring(fList[i].length() - 8);
        try {
          int year = Integer.parseInt(dateStr.substring(0, 4));
          int month = Integer.parseInt(dateStr.substring(4, 6));
          int day = Integer.parseInt(dateStr.substring(6, 8));
          mAvailableDateSet.add(new devplugin.Date(year, month, day));
        } catch (NumberFormatException e) {}
      }
    }

  }

  private void fireDayProgramAdded(ChannelDayProgram prog) {
    synchronized (mListenerList) {
      for (int i = 0; i < mListenerList.size(); i++) {
        TvDataBaseListener lst = (TvDataBaseListener) mListenerList.get(i);
        lst.dayProgramAdded(prog);
      }
    }
  }

  private void fireDayProgramDeleted(ChannelDayProgram prog) {
    synchronized (mListenerList) {
      for (int i = 0; i < mListenerList.size(); i++) {
        TvDataBaseListener lst = (TvDataBaseListener) mListenerList.get(i);
        lst.dayProgramDeleted(prog);
      }
    }
  }

  private void handleKnownStatus(int knownStatus, ChannelDayProgram newDayProg,
      int version) {
    if (knownStatus != TvDataInventory.KNOWN) {
      Date date = newDayProg.getDate();
      Channel channel = newDayProg.getChannel();

      if (knownStatus == TvDataInventory.OTHER_VERSION) {
        // The day program was replaced -> fire a deleted event
        // (And later an added event)

        // Since we don't have the old day program we use a dummy program
        ChannelDayProgram dayProg = new MutableChannelDayProgram(date, channel);
        fireDayProgramDeleted(dayProg);
      }

      // Set the day program to 'known'
      mTvDataInventory.setKnown(date, channel, version);

      // The day program is new -> fire an added event
      fireDayProgramAdded(newDayProg);

    }
  }

  /**
   * Checks whether all programs have a length. If not, the length will be
   * calculated.
   * 
   * @param channelProg
   *          The day program to calculate the lengths for.
   * @return <code>true</code> when at least one length was missing.
   */
  private boolean calculateMissingLengths(ChannelDayProgram channelProg) {
    boolean somethingChanged = false;

    // Go through all programs and correct them
    // (This is fast, if no correction is needed)
    for (int progIdx = 0; progIdx < channelProg.getProgramCount(); progIdx++) {
      Program program = channelProg.getProgramAt(progIdx);
      if (!(program instanceof MutableProgram)) {
        continue;
      }

      MutableProgram prog = (MutableProgram) program;

      if (prog.getLength() <= 0) {
        // Try to get the next program
        Program nextProgram = null;
        if ((progIdx + 1) < channelProg.getProgramCount()) {
          // Try to get it from this ChannelDayProgram
          nextProgram = channelProg.getProgramAt(progIdx + 1);
        } else {
          // This is the last program -> Try to get the first program of the
          // next ChannelDayProgram
          nextProgram = getFirstNextDayProgram(channelProg);
        }

        somethingChanged = calculateLength(prog, nextProgram) || somethingChanged;
      } else if (progIdx + 1 == channelProg.getProgramCount()) {
        // This is the last program that has a length but it could be wrong.
        somethingChanged = calculateLength(prog,
            getFirstNextDayProgram(channelProg)) || somethingChanged;
      }
    }

    return somethingChanged;
  }

  private Program getFirstNextDayProgram(ChannelDayProgram channelProg) {
    Date nextDate = channelProg.getDate().addDays(1);
    Channel channel = channelProg.getChannel();
    TvDataBase db = TvDataBase.getInstance();
    ChannelDayProgram nextDayProg = db.getDayProgram(nextDate, channel);

    if ((nextDayProg != null) && (nextDayProg.getProgramCount() > 0)) {
      return (nextDayProg.getProgramAt(0));
    }

    return null;
  }

  private boolean calculateLength(MutableProgram first, Program second) {
    // Calculate the Length
    if (second != null) {
      int startTime = first.getHours() * 60 + first.getMinutes();
      int endTime = second.getHours() * 60 + second.getMinutes();
      if (endTime < startTime) {
        // The program ends the next day
        endTime += 24 * 60;
      }

      if ((startTime + first.getLength()) != endTime) {
        int length = endTime - startTime;
        // Only allow a maximum length of 12 hours
        if (length < 12 * 60) {
          first.setLength(length);
          return (true);
        }
      }
    }
    return false;
  }
}
