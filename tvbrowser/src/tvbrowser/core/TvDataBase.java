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

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import tvdataservice.MutableChannelDayProgram;

import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Date;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class TvDataBase {

  /** The logger for this class. */
  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(TvDataBase.class.getName());
    
  /** The singleton. */
  private static TvDataBase mSingleton;

  /** The TV data cache. */  
  private HashMap mTvDataHash;
  
  private ArrayList mListenerList;


  private TvDataBase() {
    mTvDataHash = new HashMap();
    mListenerList = new ArrayList();
  }


  public static TvDataBase getInstance() {
    if (mSingleton == null) {
      mSingleton = new TvDataBase();
    }
    
    return mSingleton;
  }
  
  
  public void addTvDataListener(TvDataBaseListener listener) {
    synchronized(mListenerList) {
      mListenerList.add(listener);
    }
  }


  public void removeTvDataListener(TvDataBaseListener listener) {
    synchronized(mListenerList) {
      mListenerList.remove(listener);
    }
  }
  
  
  public ChannelDayProgram getDayProgram(Date date, Channel channel) {
    return getCacheEntry(date, channel, true);
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
      if (! file.renameTo(backupFile)) {
        // Renaming failed -> There will be no backup
        backupFile = null;
      }
    }
    
    // Save the new program
    try {
      saveDayProgram(prog);
      
      // Saving succeed -> Delete the backup
      if (backupFile != null) {
        backupFile.delete();
      }
      
      // Remove the old entry from the cache (if it exists)
      removeCacheEntry(key);
    
      // Inform the listeners
      if (oldProg != null) {
        fireDayProgramDeleted(oldProg);
      }
      fireDayProgramAdded(prog);
    }
    catch (IOException exc) {
      boolean restoredBackup = false;
      if (backupFile != null) {
        if (backupFile.renameTo(file)) {
          restoredBackup = true;
        } else {
          backupFile.delete();
        }
      }
      
      String msg = "Saving program for " + channel + " from " + date + " failed.";
      if (restoredBackup) {
        msg += " The old version was restored.";
      }
      mLog.log(Level.WARNING, msg, exc);
    }
  }


  private synchronized ChannelDayProgram getCacheEntry(Date date,
    Channel channel, boolean loadFromDisk)
  {
    String key = getDayProgramKey(date, channel);
    SoftReference entry = (SoftReference) mTvDataHash.get(key);

    // Try to get the program from the cache
    ChannelDayProgram prog = null;
    if (entry != null) {
      prog = (ChannelDayProgram) entry.get();
      if (prog == null) {
        // The program was removed by the garbage collector
        // -> Take the entry from the hash
        removeCacheEntry(key);
      }
    }
    
    // Try to load the program from disk
    if (loadFromDisk && (prog == null)) {
      prog = loadDayProgram(date, channel);
      if (prog != null) {
        addCacheEntry(key, prog);
        return prog;
      }
    }
    
    return prog;
  }
  
  
  private synchronized void addCacheEntry(String key, ChannelDayProgram prog) {
    mTvDataHash.put(key, new SoftReference(prog));
  }
  
  
  private synchronized void removeCacheEntry(String key) {
    mTvDataHash.remove(key);
  }


  private String getDayProgramKey(Date date, Channel channel) {
    // check the input
    if (date == null) {
      throw new NullPointerException("date is null");
    }
    if (channel == null) {
      throw new NullPointerException("channel is null");
    }

    return channel.getCountry() + "_" + channel.getId()
           + "_" + channel.getDataService().getClass().getPackage().getName()
           + "." + date.getDateString();   
  }


  public boolean isDayProgramAvailable(Date date, Channel channel) {
    File file = getDayProgramFile(date, channel);
    return file.exists();
  }


  /**
   * Deletes expired tvdata files older then lifespan days.
   * 
   * @param lifespan The number of days to delete from the past
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

    File fList[] = new File(Settings.getTVDataDirectory()).listFiles(filter);
    if (fList != null) {
      for (int i = 0; i < fList.length; i++) {
        fList[i].delete();
      }
    }
  }


  private ChannelDayProgram loadDayProgram(Date date, Channel channel) {
    File file = getDayProgramFile(date, channel);
    if (! file.exists()) {
      return null;
    }
    
    FileInputStream in = null;
    try {
      in = new FileInputStream(file);
      ObjectInputStream stream = new ObjectInputStream(in);

      MutableChannelDayProgram prog = new MutableChannelDayProgram(stream);

      stream.close();
      in.close();
      
      return prog;
    } catch (Exception exc) {
      mLog.log(Level.WARNING, "Loading program for " + channel + " from "
        + date + " failed. The file will be deleted...", exc);

      if (in != null) {
        try { in.close(); } catch (IOException exc2) {}
      }
      
      file.delete();
      
      return null;
    }
  }
  
  
  private void saveDayProgram(MutableChannelDayProgram prog)
    throws IOException
  {
    FileOutputStream out = null;
    try {
      File file = getDayProgramFile(prog.getDate(), prog.getChannel());
      out = new FileOutputStream(file);
      ObjectOutputStream stream = new ObjectOutputStream(out);
      
      prog.writeData(stream);
      
      stream.close();
      out.close();
    }
    finally {
      if (out != null) {
        try { out.close(); } catch (IOException exc) {}
      }
    }
  }


  private File getDayProgramFile(Date date, Channel channel) {
    String fileName = getDayProgramKey(date, channel);
      
    return new File(Settings.getTVDataDirectory(), fileName);
  }


  /**
   * Returns true, if tv data is available on disk for the given date.
   *
   * @param date The date to check.
   * @return if the data is available.
   */
  public boolean dataAvailable(Date date) {
    final String dateStr = date.getDateString();
    
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(dateStr);
      }
    }; 

    String fList[] = new File(Settings.getTVDataDirectory()).list(filter);

    return (fList != null) && (fList.length > 0);
  }
  
  
  private void fireDayProgramAdded(ChannelDayProgram prog) {
    synchronized(mListenerList) {
      for (int i = 0; i < mListenerList.size(); i++) {
        TvDataBaseListener lst = (TvDataBaseListener) mListenerList.get(i);
        lst.dayProgramAdded(prog);
      }
    }
  }

  private void fireDayProgramDeleted(ChannelDayProgram prog) {
    synchronized(mListenerList) {
      for (int i = 0; i < mListenerList.size(); i++) {
        TvDataBaseListener lst = (TvDataBaseListener) mListenerList.get(i);
        lst.dayProgramDeleted(prog);
      }
    }
  }

}
