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

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.event.EventListenerList;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import util.exc.TvBrowserException;
import util.io.IOUtilities;

import devplugin.Channel;
import devplugin.Program;
import devplugin.Plugin;

/**
 * One program. Consists of the Channel, the time, the title and some extra
 * information.
 *
 * @author Til Schneider, www.murfman.de
 */
public class MutableProgram implements Program {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(MutableProgram.class.getName());

  /**
   * The maximum length of a short info. Used for generating a short info out of a
   * (long) description.
   */
  private static final int MAX_SHORT_INFO_LENGTH = 100;
  
  /** A plugin array that can be shared by all the programs that are not marked
   * by any plugin. */
  private static final Plugin[] EMPTY_PLUGIN_ARR = new Plugin[0];

  /** Contains all listeners that listen for events from this program. */
  private EventListenerList mListenerList;

  /** Containes all Plugins that mark this program. We use a simple array,
   * because it takes less memory. */
  private Plugin[] mMarkedByPluginArr;

  /** Contains whether this program is currently on air. */
  private boolean mOnAir;

  /** The cached ID of this program. */
  private String mId;

  /** The program's title. */
  private String mTitle;

  /** The program's short info. Shown on the program table. May be null. */
  private String mShortInfo;

  /** The program's description. May be null. */
  private String mDescription;

  /** The program's actors. May be null. */
  private String mActors;

  /** The URL, where the user can find information about this program. May be null. */
  private String mURL;

  /** The minute-component of the start time of the program. */
  private int mMinutes;

  /** The hour-component of the start time of the program. */
  private int mHours;

  /** The length of this program in minutes. */
  private int mLength = -1;

  /** The additional information of the program. May be null. */
  private int mInfo;

  /** The channel object of this program. */
  private Channel mChannel;

  /** The date of this program. */
  private devplugin.Date mDate;

  /** The picture of this program (may be null). */
  private byte[] mPicture;



  /**
   * Creates a new instance of MutableProgram.
   * <p>
   * The parameters channel, date, hours and minutes build the ID. That's why they
   * are not mutable.
   *
   * @param channel The channel object of this program.
   * @param date The date of this program.
   * @param hours The hour-component of the start time of the program.
   * @param minutes The minute-component of the start time of the program.
   */
  public MutableProgram(Channel channel, devplugin.Date date,
    int hours, int minutes)
  {
    // These attributes are not mutable, because they build the ID.
    mChannel = channel;
    mDate = date;
    mHours = hours;
    mMinutes = minutes;

    mTitle = ""; // The title is not-null.
    
    mMarkedByPluginArr = EMPTY_PLUGIN_ARR;

    init();
  }

  
  
  public MutableProgram(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    int version = in.readInt();

    mTitle = (String) in.readObject();
    mShortInfo = (String) in.readObject();
    mDescription = (String) in.readObject();
    mActors = (String) in.readObject();
    mURL = (String) in.readObject();
    mMinutes = in.readInt();
    mHours = in.readInt();
    mLength = in.readInt();
    mInfo = in.readInt();

    mChannel = Channel.readData(in, false);
    mDate = new devplugin.Date(in);
    
    mPicture = (byte[]) in.readObject();
    
    init();
  }

  
  
  /**
   * Serialized this object.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    
    out.writeObject(mTitle);
    out.writeObject(mShortInfo);
    out.writeObject(mDescription);
    out.writeObject(mActors);
    out.writeObject(mURL);
    out.writeInt(mMinutes);
    out.writeInt(mHours);
    out.writeInt(mLength);
    out.writeInt(mInfo);

    mChannel.writeData(out);
    mDate.writeData(out);
    
    out.writeObject(mPicture);
  }



  /**
   * Initializes the program.
   */
  private void init() {
    mListenerList = new EventListenerList();
    mMarkedByPluginArr = EMPTY_PLUGIN_ARR;
    mOnAir = false;
  }



  /**
   * Adds a ChangeListener to the program.
   *
   * @param listener the ChangeListener to add
   * @see #fireStateChanged
   * @see #removeChangeListener
   */
  public void addChangeListener(ChangeListener listener) {
    // TODO: The ProgramPanels to not unregister themselves
    /*
    mLog.info("mListenerList.getListenerCount(): " + mListenerList.getListenerCount());
    if (mListenerList.getListenerCount() != 0) {
      throw new RuntimeException("test");
    }
    */

    mListenerList.add(ChangeListener.class, listener);
  }



  /**
   * Removes a ChangeListener from the program.
   *
   * @param listener the ChangeListener to remove
   * @see #fireStateChanged
   * @see #addChangeListener
   */
  public void removeChangeListener(ChangeListener listener) {
    mListenerList.remove(ChangeListener.class, listener);
  }



  /**
   * Send a ChangeEvent, whose source is this program, to each listener.
   *
   * @see #addChangeListener
   * @see EventListenerList
   */
  protected void fireStateChanged() {
    Object[] listeners = mListenerList.getListenerList();
    ChangeEvent changeEvent = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i]==ChangeListener.class) {
        if (changeEvent == null) {
          changeEvent = new ChangeEvent(this);
        }
        ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
      }
    }
  }



  public final String getTimeString() {
    return getHours()+":"+((getMinutes()<10)?"0":"")+getMinutes();
  }


  public final String getDateString() {
    return getDate().toString();
  }



  /**
   * Sets whether this program is marked as "on air".
   */
  public void markAsOnAir(boolean onAir) {
    // avoid unnessesary calls of fireStateChanged()
    // call fireStateChanged() anyway if we are "on air"
    // (for updating the "progress bar" painted by the ProgramPanel)
    if (onAir || (onAir != mOnAir)) {
      mOnAir = onAir;
      fireStateChanged();
    }
  }



  /**
   * Gets whether this program is marked as "on air".
   */
  public boolean isOnAir() {
    return mOnAir;
  }



  public final void mark(devplugin.Plugin plugin) {
    boolean alreadyMarked = getMarkedByPluginIndex(plugin) != -1;
    if (! alreadyMarked) {
      // Append the new plugin
      int oldCount = mMarkedByPluginArr.length;
      Plugin[] newArr = new Plugin[oldCount + 1];
      System.arraycopy(mMarkedByPluginArr, 0, newArr, 0, oldCount);
      newArr[oldCount] = plugin;
      mMarkedByPluginArr = newArr;
      
      fireStateChanged();
    }
  }



  public final void unmark(devplugin.Plugin plugin) {
    int idx = getMarkedByPluginIndex(plugin);
    if (idx != -1) {
      if (mMarkedByPluginArr.length == 1) {
        // This was the only plugin
        mMarkedByPluginArr = EMPTY_PLUGIN_ARR;
      } else {
        int oldCount = mMarkedByPluginArr.length;
        Plugin[] newArr = new Plugin[oldCount - 1];
        System.arraycopy(mMarkedByPluginArr, 0, newArr, 0, idx);
        System.arraycopy(mMarkedByPluginArr, idx + 1, newArr, idx, oldCount - idx - 1);
        mMarkedByPluginArr = newArr;
      }
      
      fireStateChanged();
    }
  }

  
  
  private int getMarkedByPluginIndex(Plugin plugin) {
    for (int i = 0; i < mMarkedByPluginArr.length; i++) {
      if (mMarkedByPluginArr[i] == plugin) {
        return i;
      }
    }
    
    return -1;
  }


  /**
   * Gets all {@link devplugin.Plugin}s that have marked this program.
   */
  public Plugin[] getMarkedByPlugins() {
    return mMarkedByPluginArr;
  }



  /**
   * Gets whether this program is expired.
   */
  public boolean isExpired() {
    int currentDaysSince1970 = IOUtilities.getDaysSince1970();
    int programDaysSince1970 = getDate().getDaysSince1970();

    if (programDaysSince1970 < currentDaysSince1970) {
      return true;
    }
    if (programDaysSince1970 > currentDaysSince1970) {
      return false;
    }

    // This program is (or was) today -> We've got to check the time
    int currentMinutesAfterMidnight = IOUtilities.getMinutesAfterMidnight();
    int programMinutesAfterMidnight = getHours() * 60 + getMinutes() + getLength() - 1;

    return (programMinutesAfterMidnight < currentMinutesAfterMidnight);
  }



  /**
   * (This method is a kind of constructor. It reads the program from an
   * InputStream.)
   *
   * @param in The stream to read the program from.
   */
  /*
  public void readProgram(InputStream in) throws TvBrowserException {
  }
  */



  /**
   * Gets the ID of this program. This ID is unique for a certain date.
   *
   * @return The ID of this program.
   */
  public String getID() {
    if (mId == null) {
      mId = getChannel().getId() + "_" + getHours() + ":" + getMinutes();
    }
    return mId;
  }



  /**
   * Sets the title of this program.
   *
   * @param title the new title of this program.
   */
  public void setTitle(String title) {
    mTitle = title;
    fireStateChanged();
  }

  /**
   * Returns the title of this program.
   *
   * @return the title of this program.
   */
  public String getTitle() {
    return mTitle;
  }



  /**
   * Sets a short information about the program (about three lines). May be null.
   * <p>
   * If the legth of the short info exceeds 100 characters it will be cut using
   * a smart algorithm.
   *
   * @param shortInfo The new short info.
   */
  public void setShortInfo(String shortInfo) {
    if ((shortInfo != null) && (shortInfo.length() > MAX_SHORT_INFO_LENGTH)) {
      // Get the end of the last fitting sentense
      int lastDot = shortInfo.lastIndexOf('.', MAX_SHORT_INFO_LENGTH);
      int lastMidDot = shortInfo.lastIndexOf('\u00b7', MAX_SHORT_INFO_LENGTH);

      int cutIdx = Math.max(lastDot, lastMidDot);

      // But show at least half the maximum length
      if (cutIdx < (MAX_SHORT_INFO_LENGTH / 2)) {
        cutIdx = shortInfo.lastIndexOf(' ', MAX_SHORT_INFO_LENGTH);
      }

      shortInfo = shortInfo.substring(0, cutIdx + 1) + "...";
    }

    mShortInfo = shortInfo;
    fireStateChanged();
  }

  /**
   * Returns a short information about the program (about three lines). May be null.
   *
   * @return The short info.
   */
  public String getShortInfo() {
    return mShortInfo;
  }



  /**
   * Sets a description about the program. May be null.
   *
   * @param description The description.
   */
  public void setDescription(String description) {
    mDescription = description;
    fireStateChanged();
  }

  /**
   * Returns a description about the program. May be null.
   *
   * @return The description.
   */
  public String getDescription() {
    return mDescription;
  }



  /**
   * Sets the names of some actors. May be null.
   *
   * @param actors The new names of the actors.
   */
  public void setActors(String actors) {
    mActors = actors;
    fireStateChanged();
  }

  /**
   * Returns the names of some actors. May be null.
   *
   * @return The names of the actors.
   */
  public String getActors() {
    return mActors;
  }



  /**
   * Sets an URL, where the user can find information about this program.
   * May be null.
   *
   * @param url The new URL.
   */
  public void setURL(String url) {
    mURL = url;
    fireStateChanged();
  }

  /**
   * Returns an URL, where the user can find information about this program.
   * May be null.
   *
   * @return The URL.
   */
  public String getURL() {
    return mURL;
  }



  /**
   * Returns the minute-component of the start time of the program.
   *
   * @return the minute-component of the start time.
   */
  public int getMinutes() {
    return mMinutes;
  }



  /**
   * Returns the hour-component of the start time of the program.
   *
   * @return the hour-component of the start time.
   */
  public int getHours() {
    return mHours;
  }


  /**
   * Sets the length of this program in minutes.
   *
   * @param length the new length.
   */
  public void setLength(int length) {
    mLength = length;
    fireStateChanged();
  }

  /**
   * Gets the length of this program in minutes.
   *
   * @return the length.
   */
  public int getLength() {
    return mLength;
  }



  /**
   * Sets additional information of the program (or zero).
   *
   * @param info The new additional information.
   */
  public void setInfo(int info) {
    mInfo = info;
    fireStateChanged();
  }

  /**
   * Returns additional information of the program (or zero).
   *
   * @return the new additional information.
   */
  public int getInfo() {
    return mInfo;
  }



  /**
   * Returns the channel object of this program.
   *
   * @return The channel.
   */
  public Channel getChannel() {
    return mChannel;
  }



  /**
   * Returns the date of this program.
   *
   * @return the date.
   */
  public devplugin.Date getDate() {
    return mDate;
  }



  /**
   * Sets picture date to this program (may be null).
   *
   * @param picture The new picture.
   */
  public void setPicture(byte[] picture) {
    mPicture = picture;
    fireStateChanged();
  }

  /**
   * Returns picture date to this program (may be null).
   *
   * @return The picture.
   */
  public byte[] getPicture() {
    return mPicture;
  }



  /**
   * Gets a String representation of this program for debugging.
   *
   * @return A String representation for debugging.
   */
  public String toString() {
    return "On " + mChannel.getName() + " at " + mHours + ":" + mMinutes
      + ", " + mDate + ": '" + mTitle + "'";
  }
  
}
