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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import tvbrowser.core.TvDataBase;
import tvbrowser.core.plugin.PluginProxyManager;
import util.io.IOUtilities;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Marker;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.Program;
import devplugin.ProgramFieldType;

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
  protected static final Marker[] EMPTY_MARKER_ARR = new Marker[0];

  /** Contains all listeners that listen for events from this program. */
  private Vector<ChangeListener> mListenerList;

  /** Containes all Plugins that mark this program. We use a simple array,
   * because it takes less memory. */
  private Marker[] mMarkerArr;

  /** Contains whether this program is currently on air. */
  private boolean mOnAir;
  
  /** Tracks if the program is current loading/ being created. */
  private boolean mIsLoading;

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
  private HashMap<ProgramFieldType,Object> mFieldHash;

  /** The state of this program */
  private int mState;
  
  /** Contains the title of the program if the program is marked */
  private String mTitle;
  
  /**
   * Creates a new instance of MutableProgram.
   * <p>
   * The parameters channel, date, hours and minutes build the ID. That's why they
   * are not mutable.
   *
   * @param channel The channel object of this program.
   * @param localDate The date of this program.
   * @param localHours The hour-component of the start time of the program.
   * @param localMinutes The minute-component of the start time of the program.
   * 
   * @deprecated Since 2.2.2. Use {@link #MutableProgram(Channel, devplugin.Date, int, int, boolean)} instead.
   */
  public MutableProgram(Channel channel, devplugin.Date localDate,
    int localHours, int localMinutes)
  {
    this (channel, localDate, localHours, localMinutes, false);
  }
  
  /**
   * Creates a new instance of MutableProgram.
   * <p>
   * The parameters channel, date, hours and minutes build the ID. That's why they
   * are not mutable.
   *
   * @param channel The channel object of this program.
   * @param localDate The date of this program.
   * @param localHours The hour-component of the start time of the program.
   * @param localMinutes The minute-component of the start time of the program.
   * @param isLoading If the program is curently being created.
   * 
   * @see #setProgramLoadingIsComplete()
   */
  public MutableProgram(Channel channel, devplugin.Date localDate,
    int localHours, int localMinutes, boolean isLoading)
  {
    this (channel, localDate, isLoading);
    
    int localStartTime = localHours * 60 + localMinutes;
    setTimeField(ProgramFieldType.START_TIME_TYPE, localStartTime);
  }

  /**
   * Creates a new instance of MutableProgram.
   * <p>
   * The parameters channel, date, hours and minutes build the ID. That's why they
   * are not mutable.
   *
   * @param channel The channel object of this program.
   * @param localDate The date of this program.
   * @param isLoading If the program is curently loading.
   * @see #setProgramLoadingIsComplete()
   */
  public MutableProgram(Channel channel, devplugin.Date localDate, boolean isLoading) {
    if (channel == null) {
      throw new NullPointerException("channel is null");
    }
    if (localDate == null) {
      throw new NullPointerException("localDate is null");
    }
    
    mFieldHash = new HashMap<ProgramFieldType,Object>();
    mListenerList = new Vector<ChangeListener>();
    mMarkerArr = EMPTY_MARKER_ARR;
    mOnAir = false;
    mIsLoading = isLoading;

    mTitle = null;

    // These attributes are not mutable, because they build the ID.
    mChannel = channel;
    mLocalDate = localDate;

    // The title is not-null.
    setTextField(ProgramFieldType.TITLE_TYPE, "");

    mMarkerArr = EMPTY_MARKER_ARR;
    mState = IS_VALID_STATE;
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


  /**
   * Adds a ChangeListener to the program.
   *
   * @param listener the ChangeListener to add
   * @see #fireStateChanged
   * @see #removeChangeListener
   */
  public void addChangeListener(ChangeListener listener) {
    if (!mListenerList.contains(listener))
      mListenerList.add(listener);
  }



  /**
   * Removes a ChangeListener from the program.
   *
   * @param listener the ChangeListener to remove
   * @see #fireStateChanged
   * @see #addChangeListener
   */
  public void removeChangeListener(ChangeListener listener) {
    mListenerList.remove(listener);
  }



  /**
   * Send a ChangeEvent, whose source is this program, to each listener.
   *
   * @see #addChangeListener
   * @see EventListenerList
   */
  protected void fireStateChanged() {
    ChangeEvent changeEvent = new ChangeEvent(this);

    for (int i = 0; i < mListenerList.size(); i++)
      mListenerList.get(i).stateChanged(changeEvent);
  }


  public final String getTimeString() {
    return IOUtilities.timeToString(getStartTime());
  }


  public final String getDateString() {
    Date d = getDate();
    if (d == null) {
      mLog.info(mChannel.getName() + " at " + getHours() + ":" + getMinutes()
        + ", NO DATE : '" + getTitle() + "'");
      return "";
    }
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


  /**
   * Marks the program for a Java plugin.
   *
   * @param javaPlugin The plugin to mark the program for.
   */
  public final void mark(Plugin javaPlugin) {
    PluginAccess plugin = PluginProxyManager.getInstance().getPluginForId(javaPlugin.getId());
    mark(plugin);
  }


  /**
   * Removes the marks from the program for a Java plugin.
   * <p>
   * If the program wasn't marked for the plugin, nothing happens.
   *
   * @param javaPlugin The plugin to remove the mark for.
   */
  public final void unmark(Plugin javaPlugin) {
    PluginAccess plugin = PluginProxyManager.getInstance().getPluginForId(javaPlugin.getId());
    unmark(plugin);
  }


  /**
   * Marks the program for a plugin.
   *
   * @param marker The plugin to mark the program for.
   */
  public final void mark(Marker marker) {

    boolean alreadyMarked = getMarkedByPluginIndex(marker) != -1;
    int oldCount = mMarkerArr.length;

    if (! alreadyMarked) {
      // Append the new plugin
      Marker[] newArr = new Marker[oldCount + 1];
      System.arraycopy(mMarkerArr, 0, newArr, 0, oldCount);
      newArr[oldCount] = marker;
      mMarkerArr = newArr;

      fireStateChanged();
    }

    if(oldCount < 1) {
      mTitle = getTitle();
      MarkedProgramsList.getInstance().addProgram(this);
    }
  }

  /**
   * Removes the marks from the program for a plugin.
   * <p>
   * If the program wasn't marked for the plugin, nothing happens.
   *
   * @param marker The plugin to remove the mark for.
   */
  public final void unmark(Marker marker) {

    int idx = getMarkedByPluginIndex(marker);
    if (idx != -1) {
      if (mMarkerArr.length == 1) {
        // This was the only plugin
        mMarkerArr = EMPTY_MARKER_ARR;
      }
      else {
        int oldCount = mMarkerArr.length;
        Marker[] newArr = new Marker[oldCount - 1];
        System.arraycopy(mMarkerArr, 0, newArr, 0, idx);
        System.arraycopy(mMarkerArr, idx + 1, newArr, idx, oldCount - idx - 1);
        mMarkerArr = newArr;
      }

      fireStateChanged();
    }

    if(mMarkerArr.length < 1) {
      mTitle = null;
      MarkedProgramsList.getInstance().removeProgram(this);
    }
  }



  private int getMarkedByPluginIndex(Marker plugin) {
    for (int i = 0; i < mMarkerArr.length; i++) {
      if (mMarkerArr[i].getId().compareTo(plugin.getId()) == 0) {
        return i;
      }
    }

    return -1;
  }


  /**
   * Gets all {@link devplugin.Plugin}s that have marked this program.
   * @deprecated use {@link #getMarkerArr}
   */
  public PluginAccess[] getMarkedByPlugins() {
    PluginAccess plugin;
    ArrayList<PluginAccess> list = new ArrayList<PluginAccess>();
    for (int i=0; i<mMarkerArr.length; i++) {
      plugin = PluginProxyManager.getInstance().getPluginForId(mMarkerArr[i].getId());
      if (plugin != null) {
        list.add(plugin);
      }
    }
    return (PluginAccess[])list.toArray(new PluginAccess[list.size()]);
  }

  public Marker[] getMarkerArr() {
    return mMarkerArr;
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
      if  (mChannel.getDataServiceProxy() != null) {
        String dataServiceId = mChannel.getDataServiceProxy().getId();
        String groupId = mChannel.getGroup().getId();
        String channelId = mChannel.getId();
        String country = mChannel.getCountry();

        mId = (new StringBuffer(dataServiceId).append("_").append(groupId).append("_").append(country).append("_").append(channelId).append("_").append(getHours()).append(":").append(getMinutes())).toString();
      }
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



  /**
   * Gets the value of a int field as String.
   *
   * @param type The type of the wanted field. Must have a int format.
   * @return The value of the field as String or <code>null</code>, if there is
   *         no value for this field.
   */
  public String getIntFieldAsString(ProgramFieldType type) {
    int value = getIntField(type);
    if (value == -1) {
      return null;
    } else {
      return Integer.toString(value);
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

      // Correct the TimeZone
      TimeZone channelTimeZone=mChannel.getTimeZone();
      TimeZone localTimeZone=TimeZone.getDefault();

      int timeZoneOffset=(localTimeZone.getRawOffset()-channelTimeZone.getRawOffset())/3600000;
      timeZoneOffset+=mChannel.getDayLightSavingTimeCorrection();

      hours = hours + timeZoneOffset;

      if (hours >= 24) {
        hours -= 24;
      }
      else if (hours < 0) {
        hours += 24;
      }

      return new StringBuffer().append(hours).append(":").append((minutes < 10) ? "0" : "").append(minutes).toString();
    }
  }


  protected Object getField(ProgramFieldType type, int fieldFormat) {
    if (type.getFormat() != fieldFormat) {
      throw new IllegalArgumentException("The field " + type.getName()
        + " can't be read as " + ProgramFieldType.getFormatName(fieldFormat)
        + ", because it is " + ProgramFieldType.getFormatName(type.getFormat()));
    }

    synchronized (mFieldHash) {
      return mFieldHash.get(type);
    }
  }


  /**
   * Gets the number of fields this program has.
   *
   * @return the number of fields this program has.
   */
  public int getFieldCount() {
    return mFieldHash.size();
  }


  /**
   * Gets an iterator over the types of all fields this program has.
   *
   * @return an iterator over {@link ProgramFieldType}s.
   */
  public Iterator getFieldIterator() {
    return mFieldHash.keySet().iterator();
  }

  /**
   * Set a binary field.
   *
   * @param type The type of the field.
   * @param value The binary value to set.
   */
  public void setBinaryField(ProgramFieldType type, byte[] value) {
    setField(type, ProgramFieldType.BINARY_FORMAT, value);
  }

  /**
   * Set a text field.
   *
   * @param type The type of the field.
   * @param value The text value to set.
   */
  public void setTextField(ProgramFieldType type, String value) {
    // Special field treating
    if (type == ProgramFieldType.SHORT_DESCRIPTION_TYPE) {
      value = validateShortInfo(value);
    }

    setField(type, ProgramFieldType.TEXT_FORMAT, value);
  }


  /**
   * Set an int field.
   *
   * @param type The type of the field.
   * @param value The int value to set.
   */
  public void setIntField(ProgramFieldType type, int value) {
    Integer obj = null;
    if (value != -1) {
      obj = new Integer(value);
    }
    setField(type, ProgramFieldType.INT_FORMAT, obj);
  }


  /**
   * Set a time field.
   *
   * @param type The type of the field.
   * @param value The time value to set.
   */
  public void setTimeField(ProgramFieldType type, int value) {
    if ((value < 0) || (value >= (24 * 60))) {
      mLog.warning("The time value for field " + type.getName()
        + " must be between in [0..1439], but it was set to " + value+"; program: "+toString());
    }

    Integer obj = null;
    if (value != -1) {
      obj = new Integer(value);
    }
    setField(type, ProgramFieldType.TIME_FORMAT, obj);

    if (type == ProgramFieldType.START_TIME_TYPE) {
      normalizeTimeZone(mLocalDate, value);
    }
  }


  protected void setField(ProgramFieldType type, int fieldFormat, Object value) {
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

    try {
      if(!mIsLoading)
        ((MutableChannelDayProgram)TvDataBase.getInstance().getDayProgram(getDate(),getChannel())).setWasChangedByPlugin();
    }catch(Exception e) {}

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
    if(mTitle != null)
      mTitle = title;

    setTextField(ProgramFieldType.TITLE_TYPE, title);
  }

  /**
   * Returns the title of this program.
   *
   * @return the title of this program.
   */
  public String getTitle() {
    if(mTitle != null)
      return mTitle;
    else
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
   * Gets the the start time of the program in minutes after midnight.
   *
   * @return the start time.
   */
  public int getStartTime() {
    return mNormalizedStartTime;
  }


  /**
   * Gets the hour-component of the start time of the program.
   *
   * @return the hour-component of the start time.
   */
  public int getHours() {
    return mNormalizedStartTime / 60;
  }


  /**
   * Gets the minute-component of the start time of the program.
   *
   * @return the minute-component of the start time.
   */
  public int getMinutes() {
    return mNormalizedStartTime % 60;
  }


  /**
   * @return The local start time.
   */
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

  /**
   * @return The local date
   */
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


  public boolean equals(Object o) {
    if (o instanceof devplugin.Program) {
      devplugin.Program program = (devplugin.Program)o;
      return program!=null
             && equals(mChannel, program.getChannel())
             && equals(getDate(), program.getDate())
             && getHours() == program.getHours()
             && getMinutes()  == program.getMinutes()
             && getTitle().compareTo(program.getTitle()) == 0;
    }
    return false;
  }


  /**
   * Gets whether two objects are equal. Can handle null values.
   *
   * @param o1 The first object.
   * @param o2 The second object.
   * @return Whether the two objects are equal.
   */
  private boolean equals(Object o1, Object o2) {
    if ((o1 == null) || (o2 == null)) {
      return (o1 == o2);
    } else {
      return o1.equals(o2);
    }
  }

  /**
   * Sets the state of this program to a
   * program state.
   *
   * @param state The state of this program.
   * @since 2.2
   */
  protected void setProgramState(int state) {
    mState = state;
  }

  /**
   * Returns the state of this program.
   *
   * @return The program state.
   * @since 2.2
   */
  public int getProgramState() {
    return mState;
  }

  /**
   * Informs the ChangeListeners for repainting if a Plugin
   * uses more than one Icon for the Program.
   *
   * @since 2.3
   */
  public final void validateMarking() {
    fireStateChanged();
  }

  /**
   * Sets the marker array of this program.
   *
   * @param marker The marker array.
   * @since 2.2.1
   */
  protected void setMarkerArr(Marker[] marker) {
    mMarkerArr = marker;

    if(marker.length > 0)
      mTitle = getTitle();

    fireStateChanged();
  }

  /**
   * Sets the loading state to false.
   * Call this after creation of the program from the data service.
   *
   * @since 2.2.2
   */
  public void setProgramLoadingIsComplete() {
    mIsLoading = false;
  }
}
