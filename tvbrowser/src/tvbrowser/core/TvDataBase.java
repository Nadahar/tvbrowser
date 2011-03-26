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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import tvbrowser.core.data.OnDemandDayProgramFile;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.ui.mainframe.MainFrame;
import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import util.misc.SoftReferenceCache;
import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Date;
import devplugin.Program;
import devplugin.ProgressMonitor;

/**
 *
 *
 * @author Til Schneider, www.murfman.de
 */
public class TvDataBase {

  /** The logger for this class. */
  private static final Logger mLog = Logger
      .getLogger(TvDataBase.class.getName());

  private static final String INVENTORY_FILE = "tv-data-inventory.dat";

  /** The singleton. */
  private static TvDataBase mSingleton;

  /** The TV data cache. */
  private SoftReferenceCache<String, OnDemandDayProgramFile> mTvDataHash;

  private ArrayList<TvDataBaseListener> mListenerList;

  /** Contains date objects for each date for which we have a TV listing */
  private HashSet<Date> mAvailableDateSet;

  private Hashtable<String, Object> mNewDayProgramsAfterUpdate;

  private TvDataInventory mTvDataInventory;
  private boolean mPendingPluginInformationAboutChangedData;

  private TvDataBase() {
    mPendingPluginInformationAboutChangedData = false;
    mTvDataHash = new SoftReferenceCache<String, OnDemandDayProgramFile>();
    mListenerList = new ArrayList<TvDataBaseListener>();
    mAvailableDateSet = new HashSet<Date>();
    mNewDayProgramsAfterUpdate = new Hashtable<String, Object>();
    updateAvailableDateSet();

/* Uncommend testwise.
 *
 * Now update of data base is started direct from TvDataUpdater to
 * make sure the data base is updated before all other TvDataUpdateListener.
 *
 *   TvDataUpdater.getInstance().addTvDataUpdateListener(
 *       new TvDataUpdateListener() {
 *         public void tvDataUpdateStarted() {}
 *
 *         public void tvDataUpdateFinished() {
 *           updateAvailableDateSet();
 *         }
 *       });
 */

    // Load inventory
    mTvDataInventory = new TvDataInventory();
    File file = new File(Settings.getUserSettingsDirName(), INVENTORY_FILE);
    if (file.exists()) {
      try {
        mTvDataInventory.readData(file);
      } catch (Exception exc) {
        mLog.log(Level.WARNING, "Loading TV data inventory failed", exc);
      }
    }
  }

  public static TvDataBase getInstance() {
    if (mSingleton == null) {
      mSingleton = new TvDataBase();
    }

    return mSingleton;
  }

  /**
   * Updates the TV data base.
   *
   * @since 2.2.2/2.5.1
   */
  protected void updateTvDataBase() {
    updateAvailableDateSet();
  }

  public void checkTvDataInventory() {
    // Get the channel of the subscribed channels
    Channel[] channelArr = ChannelList.getSubscribedChannels();
    String[] channelIdArr = new String[channelArr.length];
    for (int i = 0; i < channelArr.length; i++) {
      channelIdArr[i] = getChannelKey(channelArr[i]);
    }

    // Inventory prüfen
    boolean somethingChanged = false;

    File tvDataDir = new File(Settings.propTVDataDirectory.getString());
    File[] tvDataArr = tvDataDir.listFiles();
    if (tvDataArr == null) {
      return;
    }
    //store all files in a map for faster lookup
    HashMap<String,File> tvDataFiles=new HashMap<String,File>();
    for (File file : tvDataArr) {
		  tvDataFiles.put(file.getName(), file);
	  }

    // Check whether day programs were removed
    String[] knownProgArr = mTvDataInventory.getKnownDayPrograms();
    Date expireDate = Date.getCurrentDate().addDays(-1);
    for (String key : knownProgArr) {
      Date date = getDateFromFileName(key);
      // Check whether this file is still present AND not expired
      // (The key is equal to the file name)
      if (!tvDataFiles.containsKey(key)
          || (date != null && date.compareTo(expireDate) < 0)) {
        // This day program was deleted -> Inform the listeners

        // Get the channel and date
        Channel channel = getChannelFromFileName(key, channelArr, channelIdArr);
        if ((channel != null) && (date != null)) {
          mLog.info("Day program was deleted by third party: " + date + " on "
              + channel.getName());
          ChannelDayProgram dummyProg = new MutableChannelDayProgram(date,
              channel);
          fireDayProgramTouched(dummyProg, null);
          fireDayProgramDeleted(dummyProg);

          mTvDataInventory.setUnknown(date, channel);
        }
        else {
          // we do not know the channel of this file -> remove from list of
          // remembered files
          mTvDataInventory.setUnknown(key);
        }
      }
    }

    // Check whether day programs were added or replaced
    for (File tvDataFile : tvDataArr) {
      String fileName = tvDataFile.getName();

      // Get the channel and date
      Channel channel = getChannelFromFileName(fileName, channelArr,
          channelIdArr);
      Date date = getDateFromFileName(fileName);
      if ((channel != null) && (date != null)) {
        // Get the version
        int version = (int) tvDataFile.length();

        // Check whether this day program is known
        int knownStatus = mTvDataInventory.getKnownStatus(date, channel,
            version);

        if ((knownStatus == TvDataInventory.UNKNOWN)
            || (knownStatus == TvDataInventory.OTHER_VERSION)) {
          // do not try to read very old unknown files
          if (isValidDate(date)) {
            if (!somethingChanged) {
              // This is the first changed day program -> fire update start
              TvDataUpdater.getInstance().fireTvDataUpdateStarted();
            }

            // Inform the listeners
            mLog.info("Day program was changed by third party: " + date + " on "
                + channel.getName());
            ChannelDayProgram newDayProg = getDayProgram(date, channel, false);
            if (newDayProg != null) {
              handleKnownStatus(knownStatus, newDayProg, version);
            }

            somethingChanged = true;
          }
        }
      }
    }

    // fire update finished
    if (somethingChanged) {
      if(!MainFrame.isStarting()) {
        TvDataUpdater.getInstance().fireTvDataUpdateFinished();
      }
      else {
        mPendingPluginInformationAboutChangedData = true;
      }
    }
  }

  /**
   * Handles the pending plugin information if it is to do
   * after TV-Browser start is finished.
   * <p>
   * @since 2.7.2
   */
  public void handleTvBrowserStartFinished() {
    if(mPendingPluginInformationAboutChangedData) {
      mPendingPluginInformationAboutChangedData = false;

      Collection<Object> dayPrograms = mNewDayProgramsAfterUpdate.values();

      for(Object dayProgram : dayPrograms) {
        if(dayProgram instanceof ChannelDayProgram) {
          fireDayProgramTouched(null,(ChannelDayProgram)dayProgram);
          fireDayProgramAdded((ChannelDayProgram)dayProgram);
        }
      }

      mNewDayProgramsAfterUpdate.clear();

      TvDataUpdater.getInstance().fireTvDataUpdateFinished();
    }
  }

  public void close() throws IOException {
    File file = new File(Settings.getUserSettingsDirName(), INVENTORY_FILE);
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
    return getDayProgram(date, channel, false);
  }

  public void setDayProgramWasChangedByPlugin(Date date, Channel channel) {
    OnDemandDayProgramFile progFile = getCacheEntry(date, channel, false, false);

    if(progFile != null) {
      progFile.getDayProgram().setWasChangedByPlugin();
    }
  }

  private ChannelDayProgram getDayProgram(Date date, Channel channel, boolean update) {
    OnDemandDayProgramFile progFile = getCacheEntry(date, channel, true, update);

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
   * @param progressMonitor display status of the recalculation
   */
  public synchronized void reCalculateTvData(int days, ProgressMonitor progressMonitor) {
    Channel[] channels = ChannelList.getSubscribedChannels();

    progressMonitor.setMaximum(channels.length + 1);

    int value = 0;
    Date currentDate = Date.getCurrentDate();
    for (Channel channel : channels) {
      progressMonitor.setValue(value++);
      for (int i = -1; i < days; i++) {
        correctDayProgramFile(currentDate.addDays(i), channel);
      }
    }
    mNewDayProgramsAfterUpdate.clear();

    FavoritesPlugin.getInstance().waitForFinishingUpdateThreads();
  }

  public synchronized void setDayProgram(MutableChannelDayProgram prog) {
    Date date = prog.getDate();
    Channel channel = prog.getChannel();
    String key = getDayProgramKey(date, channel);

    if(prog.getProgramCount() > 0) {
      prog.setLastProgramHadEndOnUpdate(prog.getProgramAt(prog.getProgramCount() - 1).getLength() > 0);
    }

    // Create a backup (Rename the old file if it exists)
    File file = getDayProgramFile(date, channel);
    File backupFile = null;
    ChannelDayProgram oldProg = getDayProgram(date, channel, false);
    if (file.exists()) {
      backupFile = new File(file.getAbsolutePath() + ".backup");
      if (!file.renameTo(backupFile)) {
        // Renaming failed -> There will be no backup
        backupFile = null;
      }
    }

    // Invalidate the old program file from the cache
    OnDemandDayProgramFile oldProgFile = getCacheEntry(date, channel, false, false);
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
    //fireDayProgramAdded(prog);

    // Save the new program
    try {
      // Save the day program
      newProgFile.saveDayProgram();

      // Delete the backup
      if (backupFile != null) {
        backupFile.delete();
      }

      // save the value for informing the listeners later
      if(oldProg != null) {
        mNewDayProgramsAfterUpdate.put(key, oldProg);
      } else {
        mNewDayProgramsAfterUpdate.put(key, "null");
      }

      // Set the new program to 'known'
      int version = (int) file.length();
      mTvDataInventory.setKnown(date, channel, version);
    } catch (IOException exc) {
      // Remove the new program from the cache
      removeCacheEntry(key);

      // Remove the program from the later update list
      mNewDayProgramsAfterUpdate.remove(key);

      // Inform the listeners about removing the new program
      fireDayProgramTouched(prog, null);
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

  private OnDemandDayProgramFile getCacheEntry(Date date,
      Channel channel, boolean loadFromDisk, boolean update) {
    if(!loadFromDisk) {
      String key = getDayProgramKey(date, channel);

      //Try to get the program from the cache
      OnDemandDayProgramFile progFile =  mTvDataHash.get(key);

      if(progFile != null) {
        return progFile;
      }
    }

    return getCacheEntryBlocking(date,channel,loadFromDisk,update);
  }


  private synchronized OnDemandDayProgramFile getCacheEntryBlocking(Date date,
      Channel channel, boolean loadFromDisk, boolean update) {
    String key = getDayProgramKey(date, channel);

    // Try to get the program from the cache
    OnDemandDayProgramFile progFile =  mTvDataHash.get(key);

    // Try to load the program from disk
    if (loadFromDisk && (progFile == null || (update && progFile.isTimeLimitationData()))) {
      progFile = loadDayProgram(date, channel, update);
      if (progFile != null && !update) {
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

  protected static String getDayProgramKey(Date date, Channel channel) {
    // check the input
    if (date == null) {
      throw new NullPointerException("date is null");
    }

    return new StringBuilder(getChannelKey(channel)).append('.').append(
        date.getDateString()).toString();
  }

  private static String getChannelKey(Channel channel) {
    // check the input
    if (channel == null) {
      throw new NullPointerException("channel is null");
    }

    return new StringBuilder(channel.getCountry()).append('_').append(
        channel.getId()).append('_').append(channel.getDataServicePackageName()).toString();
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
   * @param informPlugins If the plugins should be informed about the deleting.
   */
  public void deleteExpiredFiles(int lifespan, boolean informPlugins) {
    if (lifespan < 0) {
      return; // manually
    }
    // remove the dates from the "available" set
    Date date = Date.getCurrentDate().addDays(-lifespan);
    while (dataAvailable(date)) {
      mAvailableDateSet.remove(date);
      date = date.addDays(-1);
    }

    // search files
    final Date d = Date.getCurrentDate().addDays(-lifespan);

    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        int p = name.lastIndexOf('.');
        String s = name.substring(p + 1, name.length());
        for (int i = 0; i < s.length(); i++) {
          if (! Character.isDigit(s.charAt(i))) {
            return false;
          }
        }
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

    // Get the subscribed channels
    Channel[] channelArr = ChannelList.getSubscribedChannels();
    String[] channelIdArr = new String[channelArr.length];
    for (int i = 0; i < channelArr.length; i++) {
      channelIdArr[i] = getChannelKey(channelArr[i]);
    }

    deleteFiles(informPlugins, filter, channelArr, channelIdArr);
    updateAvailableDateSet();
  }

  /**
   * @param informPlugins
   * @param filter
   * @param channelArr
   * @param channelIdArr
   * @return whether a data finished callback was triggered during file deletion
   */
  private boolean deleteFiles(boolean informPlugins, FilenameFilter filter,
      Channel[] channelArr, String[] channelIdArr) {
    File[] fileList = new File(Settings.propTVDataDirectory.getString())
        .listFiles(filter);
    boolean somethingDeleted = false;

    if (fileList != null && fileList.length > 0) {
      somethingDeleted = true;
      for (File deleteFile : fileList) {
        Channel ch = getChannelFromFileName(deleteFile.getName(), channelArr, channelIdArr);
        Date date = getDateFromFileName(deleteFile.getName());

        if(ch != null && date != null) {
          if(informPlugins) {
            ChannelDayProgram dayProgram = getDayProgram(date, ch);
            if (dayProgram != null) {
              fireDayProgramTouched(dayProgram, null);
              fireDayProgramDeleted(dayProgram);
            }
          }

          removeCacheEntry(getDayProgramKey(date,ch));
        }

        deleteFile.delete();
      }
    }

    if(informPlugins && somethingDeleted) {
      TvDataUpdater.getInstance().fireTvDataUpdateFinished();
      return true;
    }
    return false;
  }

  private synchronized void correctDayProgramFile(Date date,
      Channel channel) {
    File file = getDayProgramFile(date, channel);
    String key = getDayProgramKey(date, channel);
    if (!file.exists()) {
      return;
    }

    try {
      // Check whether this day program is known
      int version = (int) file.length();
      int knownStatus = mTvDataInventory.getKnownStatus(date, channel, version);

      OnDemandDayProgramFile oldProgFile = getCacheEntry(date, channel, false, false);
      MutableChannelDayProgram checkProg = (MutableChannelDayProgram)getDayProgram(date,channel,true);

      boolean somethingChanged = calculateMissingLengths(checkProg);

      Object oldProg = null;
      if((oldProg = mNewDayProgramsAfterUpdate.remove(key)) != null) {
        // Inform the listeners about deleting the old program
        if (oldProg instanceof ChannelDayProgram) {
          fireDayProgramDeleted((ChannelDayProgram)oldProg);
        }

        // Inform the listeners about adding the new program
        fireDayProgramAdded(checkProg);
      }
      else if(somethingChanged){
        fireDayProgramAdded(checkProg);
      }

      if (checkProg.getAndResetChangedByPluginState() || somethingChanged) {
        // Some missing lengths could now be calculated
        // -> Try to save the changes

        // We use a temporary file. If saving succeeds we rename it
        File tempFile = new File(file.getAbsolutePath() + ".changed");
        try {
          // Try to save the changed program
          OnDemandDayProgramFile newProgFile = new OnDemandDayProgramFile(
              tempFile, checkProg);
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

        // We have to load the file again to get the new data positions
        OnDemandDayProgramFile progFile = new OnDemandDayProgramFile(file, date, channel);
        progFile.loadDayProgram(false);

        // Invalidate the old program file from the cache
        if (oldProgFile != null) {
          oldProgFile.setValid(false);

          // Remove the old entry from the cache (if it exists)
          removeCacheEntry(key);
        }

        // Put the new program file in the real cache
        addCacheEntry(key, progFile);
      } else if(oldProgFile != null) {
        oldProgFile.calculateTimeLimits();
      }

      // Inform the listeners about adding the new program
      if(oldProg != null || somethingChanged) {
        OnDemandDayProgramFile dayProgramFile = getCacheEntry(date, channel, true, false);

        ChannelDayProgram dayProgram = dayProgramFile.getDayProgram();

        if(oldProg instanceof ChannelDayProgram) {
          fireDayProgramTouched((ChannelDayProgram)oldProg,dayProgram);
        }
        else {
          fireDayProgramTouched(null,dayProgram);
        }

        fireDayProgramAdded(dayProgram);
      }
    } catch (Exception exc) {
      mLog.log(Level.WARNING, "Loading program for " + channel + " from "
          + date + " failed. The file will be deleted...", exc);

      file.delete();
    }
  }


  private synchronized OnDemandDayProgramFile loadDayProgram(Date date,
      Channel channel, boolean update) {
    File file = getDayProgramFile(date, channel);
    if (!file.exists()) {
      return null;
    }

    try {
      // Load the program file
      OnDemandDayProgramFile progFile = new OnDemandDayProgramFile(file, date,
          channel);
      progFile.loadDayProgram(update);

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
    for (int i=0; i < valueAsString.length(); i++) {
      if (!Character.isDigit(valueAsString.charAt(i))) {
        return null;
      }
    }
    try {
      long value = Long.parseLong(valueAsString);
      return Date.createDateFromValue(value);
    } catch (NumberFormatException exc) {
      return null;
    }
  }

  /**
   * Returns true, if TV data is available on disk for the given date.
   *
   * @param date
   *          The date to check.
   * @return if the data is available.
   */
  public boolean dataAvailable(Date date) {
    if (date == null) {
      return false;
    }
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
        if (name.length() < 8) {
          return false;
        }
        String dateStr = name.substring(name.length() - 8);
        for (int i = 0; i< dateStr.length(); i++) {
          if (! Character.isDigit(dateStr.charAt(i))) {
            return false;
          }
        }
        try {
          Integer.parseInt(dateStr.substring(0, 4)); // year
          Integer.parseInt(dateStr.substring(4, 6)); // month
          Integer.parseInt(dateStr.substring(6, 8)); // day
        } catch (NumberFormatException e) {
          return false;
        }
        return true;
      }
    };

    String[] fileNameList = tvDataDir.list(filter);
    if (fileNameList != null) {
      for (String fileName : fileNameList) {
        if (fileName.length() > 8) {
          String dateStr = fileName.substring(fileName.length() - 8);
          try {
            int year = Integer.parseInt(dateStr.substring(0, 4));
            int month = Integer.parseInt(dateStr.substring(4, 6));
            int day = Integer.parseInt(dateStr.substring(6, 8));
            mAvailableDateSet.add(new devplugin.Date(year, month, day));
          } catch (NumberFormatException e) {}
        }
      }
    }

  }

  private void fireDayProgramAdded(MutableChannelDayProgram prog) {
    synchronized (mListenerList) {
      for (int i = 0; i < mListenerList.size(); i++) {
        TvDataBaseListener lst = mListenerList.get(i);
        lst.dayProgramAdded(prog);
      }
    }
  }

  private void fireDayProgramTouched(ChannelDayProgram removedDayProgram, ChannelDayProgram addedDayProgram) {
    synchronized (mListenerList) {
      for (int i = 0; i < mListenerList.size(); i++) {
        TvDataBaseListener lst = mListenerList.get(i);
        lst.dayProgramTouched(removedDayProgram,addedDayProgram);
      }
    }
  }

  private void fireDayProgramAdded(ChannelDayProgram prog) {
    synchronized (mListenerList) {
      for (int i = 0; i < mListenerList.size(); i++) {
        TvDataBaseListener lst = mListenerList.get(i);
        lst.dayProgramAdded(prog);
      }
    }
  }

  private void fireDayProgramDeleted(ChannelDayProgram prog) {
    synchronized (mListenerList) {
      for (int i = 0; i < mListenerList.size(); i++) {
        TvDataBaseListener lst = mListenerList.get(i);
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
        fireDayProgramTouched(dayProg, null);
        fireDayProgramDeleted(dayProg);
      }

      // Set the day program to 'known'
      mTvDataInventory.setKnown(date, channel, version);

      // The day program is new -> fire an added event
      // save the value for informing the listeners later
      if (isValidDate(date)) {
        mNewDayProgramsAfterUpdate.put(getDayProgramKey(date, channel),
            newDayProg);
      }
    }
  }

  /**
   * a day program is only valid, if it is from yesterday or newer
   *
   * @param date
   * @return
   */
  private boolean isValidDate(final Date date) {
    return (Date.getCurrentDate().getNumberOfDaysSince(date) <= 1);
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
      } else if (progIdx + 1 == channelProg.getProgramCount() && !channelProg.getLastProgramHadEndOnUpdate()) {
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
    ChannelDayProgram nextDayProg = db.getDayProgram(nextDate, channel, true);

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
        // Only allow a maximum length of 15 hours
        if (length < 15 * 60) {
          first.setLength(length);
          return (true);
        }
      }
    }
    return false;
  }

  public void unsubscribeChannels(final Channel[] channels) {
    boolean callback = false;
    for (Channel channel : channels) {
      // only trigger the plugin update after the last channel
      if (unsubscribeChannel(channel, channel == channels[channels.length - 1])) {
        callback = true;
      }
    }
    // run the callback manually
    if (!callback) {
      TvDataUpdater.getInstance().fireTvDataUpdateFinished();
    }
  }

  /**
   * delete all program files of a channel after unsubscribing it
   *
   * @param channel
   * @return whether a data update finished callback was triggered or not
   */
  private boolean unsubscribeChannel(final Channel channel, final boolean informPlugins) {
    if (channel == null) {
      return false;
    }
    final Channel[] channelArr = new Channel[] { channel };
    final String[] channelIdArr = new String[] { getChannelKey(channel) };

    // get all files of this channel, independent of the date (but they still
    // must have a date)
    FilenameFilter filter = new FilenameFilter() {

      @Override
      public boolean accept(File dir, String name) {
        Channel ch = getChannelFromFileName(name, channelArr, channelIdArr);
        Date date = getDateFromFileName(name);

        return ch != null && date != null;
      }
    };

    // delete all channel files
    return deleteFiles(informPlugins, filter, channelArr, channelIdArr);
  }

  /**
   * the maximum date any part of TV-Browser needs to be able to handle
   * @return today plus 4 weeks
   */
  public Date getMaxSupportedDate() {
    return Date.getCurrentDate().addDays(28);
  }
}
