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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import util.io.IOUtilities;
import devplugin.*;
import devplugin.Date;

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
  public static final int MAX_SHORT_INFO_LENGTH = 100;
  
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

  /** The channel object of this program. */
  private Channel mChannel;

  /** The date of the program in the channel's time zone. */
  private Date mLocalDate;

  /** The normalized date of this program. (in the client's time zone) */
  private Date mNormalizedDate;

  /** The normalized start time of the program. (in the client's time zone) */
  private int mNormalizedStartTime;
  
  /** Contains for a {@link ProgramFieldType} (key) the field value. */
  private HashMap mFieldHash;

  

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
  public MutableProgram(Channel channel, devplugin.Date localDate,
    int localHours, int localMinutes)
  {
    init();

    // These attributes are not mutable, because they build the ID.
    mChannel = channel;
    mLocalDate = localDate;
    
    int localStartTime = localHours * 60 + localMinutes;
    setTimeField(ProgramFieldType.START_TIME_TYPE, localStartTime);
    
    normalizeTimeZone(mLocalDate, localStartTime);
    
    // The title is not-null.
    setTextField(ProgramFieldType.TITLE_TYPE, "");
    
    mMarkedByPluginArr = EMPTY_PLUGIN_ARR;
  }


  private void normalizeTimeZone(Date localDate, int localStartTime) {
    TimeZone channelTimeZone=mChannel.getTimeZone();
    TimeZone localTimeZone=TimeZone.getDefault();
    
    int timeZoneOffset=(localTimeZone.getRawOffset()-channelTimeZone.getRawOffset())/3600000;
    timeZoneOffset+=mChannel.getDayLightSavingTimeCorrection();
 
    mNormalizedStartTime = localStartTime + timeZoneOffset * 60;
    mNormalizedDate=localDate;
    
    if (mNormalizedStartTime >= (24 * 60)) {
      mNormalizedStartTime -= (24 * 60);
      mNormalizedDate = mNormalizedDate.addDays(1);
    }
    else if (mNormalizedStartTime < 0) {
      mNormalizedStartTime += (24 * 60);
      mNormalizedDate = mNormalizedDate.addDays(-1);
    }
  }

  
  public MutableProgram(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    init();

    int version = in.readInt();
    
    if (version == 1) {
      setTitle((String) in.readObject());
      setShortInfo((String) in.readObject());
      setDescription((String) in.readObject());
      setTextField(ProgramFieldType.ACTOR_LIST_TYPE, (String) in.readObject());
      setTextField(ProgramFieldType.URL_TYPE, (String) in.readObject());
      
      int minutes = in.readInt();
      int localHours = in.readInt();
      int localStartTime = localHours * 60 + minutes;
      setTimeField(ProgramFieldType.START_TIME_TYPE, localStartTime);
      
      setLength(in.readInt());
      setInfo(in.readInt());

      mChannel = Channel.readData(in, false);
      mLocalDate = new devplugin.Date(in);
      
      setBinaryField(ProgramFieldType.IMAGE_TYPE, (byte[]) in.readObject());
    } else {
      mChannel = Channel.readData(in, false);
      mLocalDate = new devplugin.Date(in);
      
      synchronized (mFieldHash) {
        mFieldHash.clear();
        int fieldCount = in.readInt();
        for (int i = 0; i < fieldCount; i++) {
          int typeId = in.readInt();
          ProgramFieldType type = ProgramFieldType.getTypeForId(typeId);
          if (type.getFormat() == ProgramFieldType.BINARY_FORMAT) {
            setBinaryField(type, (byte[]) in.readObject());
          }
          else if (type.getFormat() == ProgramFieldType.TEXT_FORMAT) {
            setTextField(type, (String) in.readObject());
          }
          else if (type.getFormat() == ProgramFieldType.INT_FORMAT) {
            setIntField(type, in.readInt());
          }
          else if (type.getFormat() == ProgramFieldType.TIME_FORMAT) {
            setTimeField(type, in.readInt());
          }
        }
      }
    }
    
    normalizeTimeZone(mLocalDate, getLocalStartTime());
  }

  
  
  /**
   * Serialized this object.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2); // file version
    
    mChannel.writeData(out);
    mLocalDate.writeData(out);
    
    synchronized (mFieldHash) {
      Set keySet = mFieldHash.keySet();
      int fieldCount = keySet.size();
      out.writeInt(fieldCount);
      Iterator iter = keySet.iterator();
      for (int i = 0; i < fieldCount; i++) {
        ProgramFieldType type = (ProgramFieldType) iter.next();
        out.writeInt(type.getTypeId());
        
        if (type.getFormat() == ProgramFieldType.BINARY_FORMAT) {
          out.writeObject(getBinaryField(type));
        }
        else if (type.getFormat() == ProgramFieldType.TEXT_FORMAT) {
          out.writeObject(getTextField(type));
        }
        else if (type.getFormat() == ProgramFieldType.INT_FORMAT) {
          out.writeInt(getIntField(type));
        }
        else if (type.getFormat() == ProgramFieldType.TIME_FORMAT) {
          out.writeInt(getTimeField(type));
        }
      }
    }
  }



  /**
   * Initializes the program.
   */
  private void init() {
    mFieldHash = new HashMap();
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
    devplugin.Date today=new devplugin.Date();


    if (today.compareTo(getDate()) < 0) {
      return false;
    }
    if (today.compareTo(getDate()) > 0) {
      return ! isOnAir();
    }

    // This program is (or was) today -> We've got to check the time
    int currentMinutesAfterMidnight = IOUtilities.getMinutesAfterMidnight();
    int programMinutesAfterMidnight = getHours() * 60 + getMinutes() + getLength() - 1;
    return (programMinutesAfterMidnight < currentMinutesAfterMidnight);
    
  }


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
  
  
  // FieldHash
  public byte[] getBinaryField(ProgramFieldType type) {
    return (byte[]) getField(type, ProgramFieldType.BINARY_FORMAT);
  }
  
  
  public String getTextField(ProgramFieldType type) {
    return (String) getField(type, ProgramFieldType.TEXT_FORMAT);
  }
  
  
  public int getIntField(ProgramFieldType type) {
    Integer value = (Integer) getField(type, ProgramFieldType.INT_FORMAT);
    if (value == null) {
      return -1;
    } else {
      return value.intValue();
    }
  }
  
  
  public int getTimeField(ProgramFieldType type) {
    Integer value = (Integer) getField(type, ProgramFieldType.TIME_FORMAT);
    if (value == null) {
      return -1;
    } else {
      return value.intValue();
    }
  }


  /**
   * Gets the value of a time field as String of the pattern "h:mm".
   * 
   * @param type The type of the wanted field. Must have a time format.
   * @return The value of the field as String or <code>null</code>, if there is
   *         no value for this field. 
   */
  public String getTimeFieldAsString(ProgramFieldType type) {
    int value = getTimeField(type);
    if (value == -1) {
      return null;
    } else {
      int hours = value / 60;
      int minutes = value % 60;
      return hours + ":" + ((minutes < 10) ? "0" : "") + minutes;
    }
  }


  private Object getField(ProgramFieldType type, int fieldFormat) {
    if (type.getFormat() != fieldFormat) {
      throw new IllegalArgumentException("The field " + type.getName()
        + " can't be read as " + ProgramFieldType.getFormatName(fieldFormat)
        + ", because it is " + ProgramFieldType.getFormatName(type.getFormat()));
    }
    
    synchronized (mFieldHash) {
      return mFieldHash.get(type);
    }
  }
  
  
  public void setBinaryField(ProgramFieldType type, byte[] value) {
    setField(type, ProgramFieldType.BINARY_FORMAT, value);
  }
  
  
  public void setTextField(ProgramFieldType type, String value) {
    // Special field treating
    if (type == ProgramFieldType.SHORT_DESCRIPTION_TYPE) {
      value = validateShortInfo(value);
    }
    
    setField(type, ProgramFieldType.TEXT_FORMAT, value);
  }
  
  
  public void setIntField(ProgramFieldType type, int value) {
    Integer obj = null;
    if (value != -1) {
      obj = new Integer(value);
    }
    setField(type, ProgramFieldType.INT_FORMAT, obj);
  }
  
  
  public void setTimeField(ProgramFieldType type, int value) {
    if ((value < 0) || (value >= (24 * 60))) {
      mLog.warning("The time value for field " + type.getName()
        + " must be between in [0..1439], but it was set to " + value);
    }
    
    Integer obj = null;
    if (value != -1) {
      obj = new Integer(value);
    }
    setField(type, ProgramFieldType.TIME_FORMAT, obj);
  }
  
  
  private void setField(ProgramFieldType type, int fieldFormat, Object value) {
    if (type.getFormat() != fieldFormat) {
      throw new IllegalArgumentException("The field " + type.getName()
        + " can't be written as " + ProgramFieldType.getFormatName(fieldFormat)
        + ", because it is " + ProgramFieldType.getFormatName(type.getFormat()));
    }
    
    synchronized (mFieldHash) {
      if (value == null) {
        mFieldHash.remove(type);
      } else {
        mFieldHash.put(type, value);
      }
    }

    fireStateChanged();
  }
  
  
  private String validateShortInfo(String shortInfo) {
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
    
    return shortInfo;
  }


  /**
   * Sets the title of this program.
   *
   * @param title the new title of this program.
   */
  public void setTitle(String title) {
    setTextField(ProgramFieldType.TITLE_TYPE, title);
  }

  /**
   * Returns the title of this program.
   *
   * @return the title of this program.
   */
  public String getTitle() {
    return getTextField(ProgramFieldType.TITLE_TYPE);
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
    setTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE, shortInfo);
  }

  /**
   * Returns a short information about the program (about three lines). May be null.
   *
   * @return The short info.
   */
  public String getShortInfo() {
    return getTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE);
  }



  /**
   * Sets a description about the program. May be null.
   *
   * @param description The description.
   */
  public void setDescription(String description) {
    setTextField(ProgramFieldType.DESCRIPTION_TYPE, description);
  }

  /**
   * Returns a description about the program. May be null.
   *
   * @return The description.
   */
  public String getDescription() {
    return getTextField(ProgramFieldType.DESCRIPTION_TYPE);
  }



  /**
   * Returns the minute-component of the start time of the program.
   *
   * @return the minute-component of the start time.
   */
  public int getMinutes() {
    return mNormalizedStartTime % 60;
  }



  /**
   * Returns the hour-component of the start time of the program.
   *
   * @return the hour-component of the start time.
   */
  public int getHours() {
    return mNormalizedStartTime / 60;
  }
  
  public int getLocalStartTime() {
    return getTimeField(ProgramFieldType.START_TIME_TYPE);
  }


  /**
   * Sets the length of this program in minutes.
   *
   * @param length the new length.
   */
  public void setLength(int length) {
    int startTime = getTimeField(ProgramFieldType.START_TIME_TYPE);
    int endTime = startTime + length;
    if (endTime >= (24 * 60)) {
      endTime -= (24 * 60);
    }
    
    setTimeField(ProgramFieldType.END_TIME_TYPE, endTime);
  }

  /**
   * Gets the length of this program in minutes.
   *
   * @return the length.
   */
  public int getLength() {
    int endTime = getTimeField(ProgramFieldType.END_TIME_TYPE);
    if (endTime == -1) {
      return -1;
    }

    int startTime = getTimeField(ProgramFieldType.START_TIME_TYPE);
    if (endTime < startTime) {
      endTime += (24 * 60);
    }

    return endTime - startTime;
  }



  /**
   * Sets additional information of the program (or zero).
   *
   * @param info The new additional information.
   */
  public void setInfo(int info) {
    setIntField(ProgramFieldType.INFO_TYPE, info);
  }

  /**
   * Returns additional information of the program (or zero).
   *
   * @return the new additional information.
   */
  public int getInfo() {
    return getIntField(ProgramFieldType.INFO_TYPE);
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
    return mNormalizedDate;
  }

  public devplugin.Date getLocalDate() {
    return mLocalDate;
  }


  /**
   * Gets a String representation of this program for debugging.
   *
   * @return A String representation for debugging.
   */
  public String toString() {
    return "On " + mChannel.getName() + " at " + getHours() + ":" + getMinutes()
      + ", " + getDateString() + ": '" + getTitle() + "'";
  }
  
}
