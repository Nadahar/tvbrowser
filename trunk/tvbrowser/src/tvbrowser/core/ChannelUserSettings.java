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
 * The ChannelUserSettings class holds all information of channel properties which can changed by the user.
 */
public class ChannelUserSettings {

  private int mDaylightSavingTimeCorrection;
  private String mChannelName;
  private String mIconFileName;
  private boolean mUseUserIconFile;
  private String mWebPage;

  private static HashMap mChannelUserSettings = new HashMap();

  public static ChannelUserSettings getSettings(Channel ch) {
    if(ch == null || ch.getDataServiceProxy() == null || ch.getGroup() == null) {
      ChannelUserSettings settings = (ChannelUserSettings)mChannelUserSettings.get(null);
      
      if (settings == null) {
        settings = new ChannelUserSettings();
        mChannelUserSettings.put(null, settings);
      }
      
      return settings;
    }
      
    String idValue = (new StringBuffer(ch.getDataServiceProxy().getId()).append(ch.getGroup().getId()).append(":").append(ch.getId())).toString();
    
    ChannelUserSettings settings = (ChannelUserSettings)mChannelUserSettings.get(idValue);
    if (settings == null) {
      settings = new ChannelUserSettings();
      mChannelUserSettings.put(idValue, settings);
    }
    return settings;
  }


  public ChannelUserSettings() {

  }

  public void setDaylightSavingTimeCorrection(int correction) {
    mDaylightSavingTimeCorrection = correction;
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

  public int getDaylightSavingTimeCorrection() {
    return mDaylightSavingTimeCorrection;
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
  
}
