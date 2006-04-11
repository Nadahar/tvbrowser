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
 */

package devplugin;

import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.TimeZone;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import tvbrowser.core.ChannelUserSettings;
import tvbrowser.core.tvdataservice.DeprecatedTvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvdataservice.TvDataService;
import util.ui.ImageUtilities;

public class Channel {

  public static final int CATEGORY_NONE = 0;
  public static final int CATEGORY_TV = 1;
  public static final int CATEGORY_RADIO = 1 << 1;
  public static final int CATEGORY_CINEMA = 1 << 2;
  public static final int CATEGORY_EVENTS = 1 << 3;
  public static final int CATEGORY_DIGITAL = 1 << 4;
  public static final int CATEGORY_SPECIAL_MUSIC = 1 << 5;
  public static final int CATEGORY_SPECIAL_SPORT = 1 << 6;
  public static final int CATEGORY_SPECIAL_NEWS = 1 << 7;
  public static final int CATEGORY_SPECIAL_OTHER = 1 << 8;


  /**
   * @deprecated
   */
  private TvDataService mDataService;

  private String mName;
  private String mId;
  private TimeZone mTimeZone;
  private String mCountry;
  private String mCopyrightNotice;
  private String mWebpage;
  private ChannelGroup mGroup;
  private int mCategories;
  private Icon mIcon;

  /** The Default-Icon */
  private Icon mDefaultIcon;


  public Channel(TvDataService dataService, String name, String id,
    TimeZone timeZone, String country, String copyrightNotice, String webpage, devplugin.ChannelGroup group, Icon icon, int categories)
  {
    if (country.length() != 2) {
      throw new IllegalArgumentException("country must be a two character "
        + "ISO country code (as used in top level domains, e.g. 'de' or 'us'): "
        + "'" + country + "'");
    }
    
    mDataService = dataService;
    mName = name;
    mId = id;
    mTimeZone=timeZone;
    mCountry = country;
    mCopyrightNotice=copyrightNotice;
    mWebpage=webpage;
    mGroup=group;
    mIcon=icon;
    mCategories = categories;
  }

  public Channel(TvDataService dataService, String name, String id,
    TimeZone timeZone, String country, String copyrightNotice, String webpage, devplugin.ChannelGroup group, Icon icon)
  {
    this(dataService, name, id, timeZone, country, copyrightNotice, webpage, group, icon, CATEGORY_NONE);
  }

  public Channel(TvDataService dataService, String name, String id, TimeZone timeZone, String country, String copyrightNotice, String webpage, devplugin.ChannelGroup group) {
     this(dataService,name,id,timeZone,country,copyrightNotice,webpage,group,null);  
  }
  
  public Channel(TvDataService dataService, String name, String id,
      TimeZone timeZone, String country, String copyrightNotice, String webpage) {
        
    this(dataService,name,id,timeZone,country,copyrightNotice,webpage,null);     
  }
  
  
  public Channel(TvDataService dataService, String name, String id,
      TimeZone timeZone, String country, String copyrightNotice)
  {
      this(dataService,name,id,timeZone,country, copyrightNotice,null);
  }

  public Channel(TvDataService dataService, String name, TimeZone timeZone,
    String country, String copyrightNotice)
  {
    this(dataService, name, name, timeZone, country, copyrightNotice, null);
  }


  /**
   * @deprecated
   */
  public Channel(TvDataService dataService, String name, String id,
     TimeZone timeZone, String country)
   {
      this(dataService,name,id,timeZone,country,"(no copyright notice)",null);
   }
  
  /**
    * @deprecated
    */
  public Channel(TvDataService dataService, String name, TimeZone timeZone,
      String country)
    {
      this(dataService, name, name, timeZone, country, "(no copyright given)",null);
    }
  
  /**
   * @deprecated
   */
  public Channel(TvDataService dataService, String name, String id,
    TimeZone timeZone)
  {
    this(dataService, name, id, timeZone, "de");
  }
  
  /**
   * @deprecated
   */
  public Channel(TvDataService dataService, String name, TimeZone timeZone) {
    this(dataService, name, name, timeZone);
  }
  
  /**
   * @deprecated
   */
  public Channel(TvDataService dataService, String name, String id) {
    this(dataService, name, id, TimeZone.getDefault());
  }

  /**
   * @deprecated
   */
  public Channel(TvDataService dataService, String name) {
    this(dataService, name, name);
  }

  public Channel(String id, String country) {
    this(null, id, id, null, country);
  }

  public Channel(String id) {
    this(null, null, id);
  }

  public static Channel readData(ObjectInputStream in, boolean allowNull)
  throws IOException, ClassNotFoundException
  {
    int version = in.readInt();
        
    String dataServiceClassName = (String)in.readObject();    
    
    String channelId;
    
    if (version==1) {
      channelId=""+in.readInt();
    }
    else {
      channelId=(String)in.readObject();
    }
        
    Channel channel = getChannel(dataServiceClassName, channelId);
    if ((channel == null) && (! allowNull)) {
      throw new IOException("Channel with id " + channelId + " of data service "
        + dataServiceClassName + " not found!");
    }
    return channel;
  }


  /**
   * Method for OnDemandDayProgramFile file format version 2.
   * 
   * @param in The file too read the Data from.
   * @param allowNull
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public static Channel readData(RandomAccessFile in, boolean allowNull)
    throws IOException, ClassNotFoundException
  {
    int version = in.readInt();
    
    if(version < 3)
      throw new IOException();
    
    int length = in.readInt();
    byte[] b = new byte[length];
    
    in.readFully(b);
    
    String dataServiceClassName = new String(b);    
    
    String channelId;
    
    if (version==1) {
    	channelId=""+in.readInt();
    }
    else {
      length = in.readInt();
      b = new byte[length];
      in.readFully(b);
    	channelId=new String(b);
    }
        
    Channel channel = getChannel(dataServiceClassName, channelId);
    if ((channel == null) && (! allowNull)) {
      throw new IOException("Channel with id " + channelId + " of data service "
        + dataServiceClassName + " not found!");
    }
    return channel;
  }
  
  /**
   * Method for OnDemandDayProgramFile file format version 2.
   * 
   * @param out The file to write the Data in.
   * @throws IOException
   */
  public void writeToDataFile(RandomAccessFile out) throws IOException {
    out.writeInt(3); // version
    String name = mDataService.getClass().getName();
    out.writeInt(name.length());//.writeObject();
    out.writeBytes(name);
    out.writeInt(mId.length());
    out.writeBytes(mId);
  }
  
  /**
   * Serialized this object.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2); // version
    out.writeObject(mDataService.getClass().getName());
    out.writeObject(mId);
  }
  
  public String getCopyrightNotice() {
    return mCopyrightNotice!=null?mCopyrightNotice:"";
  }
  
  public String getWebpage() {
    if (getUserWebPage() != null) {
      return getUserWebPage();
    }
    return mWebpage;
  }
  
  public ChannelGroup getGroup() {
    return mGroup;
  }

  public int getCategories() {
    return mCategories;
  }

  public static Channel getChannel(String dataServiceClassName, String channelId) {
    if (dataServiceClassName == null) {
      // Fast return
      return null;
    }
    
    Channel[] channelArr = Plugin.getPluginManager().getSubscribedChannels();
    for (int i = 0; i < channelArr.length; i++) {
      if (dataServiceClassName.equals(channelArr[i].getDataService().getClass().getName())
        && (channelArr[i].getId().equals(channelId)))
      {
        return channelArr[i];
      }      
    }
    
    return null;
  }
  
  public TimeZone getTimeZone() {
    return mTimeZone;
  }
  
  public String getCountry() {
    return mCountry;
  }
  
  public void setDayLightSavingTimeCorrection(int correction) {
    ChannelUserSettings.getSettings(this).setDaylightSavingTimeCorrection(correction);
  }
  
  public int getDayLightSavingTimeCorrection() {
    return ChannelUserSettings.getSettings(this).getDaylightSavingTimeCorrection();
  }

  /**
   * @deprecated
   */
  public TvDataService getDataService() {
    return mDataService;
  }

  public TvDataServiceProxy getDataServiceProxy() {
    return new DeprecatedTvDataServiceProxy(mDataService);
  }

  /**
   * Sets the Default-Icon. This Icon is shown if no Icon is set by the User
   * @param icon Default-Icon
   */
  public void setDefaultIcon(Icon icon) {
      mDefaultIcon = icon;
  }

  /**
   *
   * @param icon The new icon or null to remove the current icon
   */
  public void setIcon(Icon icon) {
    mIcon = icon;
  }


  public String toString() {
    return getName();
  }

  public String getName() {
    if (getUserChannelName() != null) {
      return getUserChannelName();
    } 
    return mName;
  }

  public String getId() {
    return mId;
  }

  /**
   * Copies the Settins in this Channel (DaylightSaving etc..) into
   * another Channel
   * @param to to this Channel
   * @deprecated not needed since we use the ChannelUserSettings class for storing channel user settings
   */
  public void copySettingsToChannel(Channel to) {
  }
  
  /**
   * @return the Icon for this Channel
   */
  public Icon getIcon() {
    if ((isUsingUserIcon()) && (mIcon == null) && (getUserIconFileName() != null)){
      Image img = ImageUtilities.createImage(getUserIconFileName());
      if (img != null) {
        mIcon = new ImageIcon(img);
      }
    }
      
    if (mIcon == null) {
      return mDefaultIcon;
    }
      
    return mIcon;
  }

  public boolean hasIcon() {
    if ((isUsingUserIcon()) && (mIcon == null) && (getUserIconFileName() != null)) {
      getIcon();
    }

    return (mIcon != null) || (mDefaultIcon != null);

  }
  
  /**
   * Gets the Filename for an Icon
   * @return Filename of the Icon
   * @deprecated use getUserIconFileName()
   */
  public String getIconFileName() {
      return getUserIconFileName();
  }
  
  /**
   * Sets the Filename for an Icon
   * @param filename Filename for Icon
   * @deprecated user setUserIconFileName
   */
  public void setIconFileName(String filename) {
    setUserIconFileName(filename);
  }

  public void setUserIconFileName(String filename) {
    ChannelUserSettings.getSettings(this).setIconFileName(filename);
    mIcon = null;
  }

  /**
   *
   * @return null, if no user icon filename is specified
   */
  public String getUserIconFileName() {
    return ChannelUserSettings.getSettings(this).getIconFileName();
  }

  /**
   * Use the User-Icon if available?
   * @param use true for using User-Icon
   */
  public void useUserIcon(boolean use) {
    ChannelUserSettings.getSettings(this).useUserIconFile(use);
  }


  /**
   * Is using the User-Icon if availabe?
   * @return Using User-Icon if available?
   */
  public boolean isUsingUserIcon() {
    return ChannelUserSettings.getSettings(this).useUserIconFile();
  }

  /**
   * Return the Default-Icon
   * @return Default-Icon
   */
  public Icon getDefaultIcon() {
    return mDefaultIcon;
  }
  
  /**
   * Set the ChannelName used by the User 
   * @param name new ChannelName
   * @since 2.1
   */
  public void setUserChannelName(String name) {
    ChannelUserSettings.getSettings(this).setChannelName(name);
  }
  
  /**
   * Get the ChannelName used by the User 
   * @return ChannelName
   * @since 2.1
   */
  public String getUserChannelName() {
    return ChannelUserSettings.getSettings(this).getChannelName();
  }
  
  /**
   * Get the default ChannelName
   * @return default ChannelName
   * @since 2.1
   */
  public String getDefaultName() {
    return mName;
  }
  
  /**
   * Seth the WebPage used by the User
   * @param url WebPage
   * @since 2.1
   */
  public void setUserWebPage(String url) {
    ChannelUserSettings.getSettings(this).setWebPage(url);
  }
  
  /**
   * Get the WebPage used by the User
   * @return WebPage
   * @since 2.1 
   */
  public String getUserWebPage() {
    return ChannelUserSettings.getSettings(this).getWebPage();
  }
  
  /**
   * Get the Default WebPage
   * @return WebPage
   * @since 2.1
   */
  public String getDefaultWebPage() {
    return mWebpage;
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof Channel) {
      Channel cmp = (Channel) obj;
      
      if ((cmp.mDataService == null) || (mDataService == null)) {

        return (cmp.mDataService == mDataService) && (mId.equals(cmp.mId));

      }
      return (mDataService.equals(cmp.mDataService)) && (mId.equals(cmp.mId));
    }

    return false;
  }


}
