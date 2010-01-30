/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 *
 */

package tvbrowser.core;

import java.util.HashMap;

import devplugin.Channel;


/**
 * The ChannelUserSettings class holds all information of channel properties which can be changed by the user.
 */
public class ChannelUserSettings {

  private String mChannelName;
  private String mIconFileName;
  private boolean mUseUserIconFile;
  private String mWebPage;
  private int mStartTimeLimit;
  private int mEndTimeLimit;
  private int mTimeZoneOffsetMinutes;

  private static HashMap<String, ChannelUserSettings> mChannelUserSettings = new HashMap<String, ChannelUserSettings>();

  public static ChannelUserSettings getSettings(Channel ch) {
    if(ch == null || ch.getDataServiceProxy() == null || ch.getGroup() == null) {
      ChannelUserSettings settings = mChannelUserSettings.get(null);

      if (settings == null) {
        settings = new ChannelUserSettings();
        mChannelUserSettings.put(null, settings);
      }

      return settings;
    }

    String channelId = ch.getUniqueId();

    ChannelUserSettings settings = mChannelUserSettings.get(channelId);
    if (settings == null) {
      settings = new ChannelUserSettings();
      mChannelUserSettings.put(channelId, settings);
    }
    return settings;
  }


  public ChannelUserSettings() {

  }

  public void setTimeZoneCorrectionMinutes(int offset) {
    mTimeZoneOffsetMinutes = offset;
  }

  public void setChannelName(String channelName) {
    mChannelName = channelName;
  }

  public void setIconFileName(String iconFileName) {
    mIconFileName = iconFileName;
  }

  public void useUserIconFile(boolean b) {
    mUseUserIconFile = b;
  }

  public int getTimeZoneCorrectionMinutes() {
    return mTimeZoneOffsetMinutes;
  }

  public String getChannelName() {
    return mChannelName;
  }

  public String getIconFileName() {
    return mIconFileName;
  }

  public boolean useUserIconFile() {
    return mUseUserIconFile;
  }


  public String getWebPage() {
    return mWebPage;
  }

  public void setWebPage(String webpage) {
    mWebPage = webpage;
  }

  /**
   * Gets the start time limit in minutes of day.
   * This is used to exclude programs from the day program
   * that starts before the start time limit.
   *
   * @return The start time limit in minutes of day.
   * @since 2.2.4/2.6
   */
  public int getStartTimeLimit() {
    return mStartTimeLimit;
  }

  /**
   * Sets the start time limit to the new value.
   *
   * @param startTimeLimit The new value for start time
   * limit in minutes of day.
   * @since 2.2.4/2.6
   */
  public void setStartTimeLimit(int startTimeLimit) {
    mStartTimeLimit = startTimeLimit;
  }

  /**
   * Gets the end time limit in minutes of day.
   * This is used to exclude programs from the day program
   * that starts after the end time limit.
   *
   * @return The start time limit in minutes of day.
   * @since 2.2.4/2.6
   */
  public int getEndTimeLimit() {
    return mEndTimeLimit;
  }

  /**
   * Sets the end time limit to the new value.
   *
   * @param endTimeLimit The new value for end time
   * limit in minutes of day.
   * @since 2.2.4/2.6
   */
  public void setEndTimeLimit(int endTimeLimit) {
    mEndTimeLimit = endTimeLimit;
  }

  /**
   * Gets if the start and end time limit should be used.
   *
   * @return <code>True</code> if the time limit is used,
   * <code>false</code> otherwise.
   * @since 2.2.4/2.6
   */
  public boolean isTimeLimited() {
    return mStartTimeLimit != mEndTimeLimit;
  }
}
