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

package tvdataloader;

import java.io.*;

import util.exc.TvBrowserException;

import devplugin.*;
import tvdataloader.*;

/**
 * One program. Consists of the Channel, the time, the title and some extra
 * information.
 *
 * @author Til Schneider, www.murfman.de
 */
public class MutableProgram extends AbstractProgram {
  
  /**
   * The maximum length of a short info. Used for generating a short info out of a
   * (long) description.
   */  
  private static final int MAX_SHORT_INFO_LENGTH = 100;
  
  /** The cached ID of this program. */
  transient private String mId;
  
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
