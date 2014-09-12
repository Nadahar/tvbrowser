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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.io.DataInput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.TimeZone;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import tvbrowser.core.ChannelList;
import tvbrowser.core.ChannelUserSettings;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import util.io.IOUtilities;
import util.misc.StringPool;
import util.ui.ImageUtilities;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

/**
 * A class that defines a TV-Browser channel
 */
public class Channel implements Comparable<Channel> {
  /** The identifier for a channel that fits in no other category */
  public static final int CATEGORY_NONE = 0;
  /** The identifier for a channel that is in the category TV */
  public static final int CATEGORY_TV = 1;
  /** The identifier for a channel that is in the category radio */
  public static final int CATEGORY_RADIO = 1 << 1;
  /** The identifier for a channel that is in the category cinema */
  public static final int CATEGORY_CINEMA = 1 << 2;
  /** The identifier for a channel that is in the category events */
  public static final int CATEGORY_EVENTS = 1 << 3;
  /** The identifier for a channel that is in the category digital */
  public static final int CATEGORY_DIGITAL = 1 << 4;
  /** The identifier for a channel that is in the category music */
  public static final int CATEGORY_SPECIAL_MUSIC = 1 << 5;
  /** The identifier for a channel that is in the category sport */
  public static final int CATEGORY_SPECIAL_SPORT = 1 << 6;
  /** The identifier for a channel that is in the category news */
  public static final int CATEGORY_SPECIAL_NEWS = 1 << 7;
  /** The identifier for a channel that is in the category other */
  public static final int CATEGORY_SPECIAL_OTHER = 1 << 8;
  /** The identifier for a channel that is in the category pay TV */
  public static final int CATEGORY_PAY_TV = 1 << 9;
  /** The identifier for a channel that is in the category payed data TV */
  public static final int CATEGORY_PAYED_DATA_TV = 1 << 10;

  private static HashMap<Integer, String> categoryName;
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(Channel.class);
  
  private String mDataServiceID;

  private String mName;
  private String mUnescapedName;
  private String mId;
  private TimeZone mTimeZone;
  private String mBaseCountry;
  private String[] mAllCountries;
  private String mCopyrightNotice;
  private String mWebpage;
  private ChannelGroup mGroup;
  private int mCategories;
  private Icon mIcon;

  /** The Default-Icon */
  private Icon mDefaultIcon;

  private TvDataServiceProxy mProxy;
  private String mUniqueId;

  private String mSharedChannelId;
  
  private Channel mJointChannel;
  private Channel mBaseChannel;
  private Icon mJointChannelIcon;
  
  private boolean mAccessControl;
  
  private static final String getDataServiceIDFromDataService(AbstractTvDataService dataService) {
    if(dataService != null) {
      return dataService.getId();
    }
    
    return null;
  }
  
  /**
   * Creates an instance of this class.
   * <p>
   * @param dataService The data service of this channel.
   * @param name The name of this channel.
   * @param id The id of this channel.
   * @param timeZone The time zone of this channel.
   * @param baseCountry The base country of this channel.
   * @param copyrightNotice The copyright notice for this channel.
   * @param webpage The webpage of this channel.
   * @param group The group of this channel.
   * @param icon The icon for this channel.
   * @param categories The categories for this channel.
   * @param unescapedName The unescaped name for this channel.
   * @param allCountries All supported countries of this channel.
   *        ATTENTION: Have to contain the base country too.
   * @param sharedChannelId The id of the shared channel.
   * @since 3.2.1
   */
  public Channel(AbstractTvDataService dataService, String name, String id,
    TimeZone timeZone, String baseCountry, String copyrightNotice, String webpage,
    devplugin.ChannelGroup group, Icon icon, int categories, String unescapedName,
    String[] allCountries, String sharedChannelId) {
    this(dataService, name, id, timeZone, baseCountry, copyrightNotice, webpage, group, icon, categories, unescapedName, allCountries, sharedChannelId, false);
  }
  
  /**
   * Creates an instance of this class.
   * <p>
   * @param dataService The data service of this channel.
   * @param name The name of this channel.
   * @param id The id of this channel.
   * @param timeZone The time zone of this channel.
   * @param baseCountry The base country of this channel.
   * @param copyrightNotice The copyright notice for this channel.
   * @param webpage The webpage of this channel.
   * @param group The group of this channel.
   * @param icon The icon for this channel.
   * @param categories The categories for this channel.
   * @param unescapedName The unescaped name for this channel.
   * @param allCountries All supported countries of this channel.
   *        ATTENTION: Have to contain the base country too.
   * @param sharedChannelId The id of the shared channel.
   * @param accessControl If access of certain plugins to this
   *                      channel should be prevented by TV-Browser.
   * @since 3.3.4
   */
  public Channel(AbstractTvDataService dataService, String name, String id,
    TimeZone timeZone, String baseCountry, String copyrightNotice, String webpage,
    devplugin.ChannelGroup group, Icon icon, int categories, String unescapedName,
    String[] allCountries, String sharedChannelId, boolean accessControl)
  {
    this(getDataServiceIDFromDataService(dataService), name, id, timeZone, baseCountry, copyrightNotice, webpage, group, icon, categories, unescapedName, allCountries, sharedChannelId, accessControl);
  }
  
  /**
   * Creates an instance of this class.
   * <p>
   * @param dataServiceID The ID of the data service of this channel.
   * @param name The name of this channel.
   * @param id The id of this channel.
   * @param timeZone The time zone of this channel.
   * @param baseCountry The base country of this channel.
   * @param copyrightNotice The copyright notice for this channel.
   * @param webpage The webpage of this channel.
   * @param group The group of this channel.
   * @param icon The icon for this channel.
   * @param categories The categories for this channel.
   * @param unescapedName The unescaped name for this channel.
   * @param allCountries All supported countries of this channel.
   *        ATTENTION: Have to contain the base country too.
   * @param sharedChannelId The id of the shared channel.
   * @param accessControl If access of certain plugins to this
   *                      channel should be prevented by TV-Browser.
   * @since 3.3.4
   */
  public Channel(String dataServiceID, String name, String id,
    TimeZone timeZone, String baseCountry, String copyrightNotice, String webpage,
    devplugin.ChannelGroup group, Icon icon, int categories, String unescapedName,
    String[] allCountries, String sharedChannelId, boolean accessControl)
  {
    if(allCountries != null) {
      for(String testCountry : allCountries) {
        if (testCountry.length() != 2) {
          throw new IllegalArgumentException("all contries must be a two character "
            + "ISO country code (as used in top level domains, e.g. 'de' or 'us'): "
            + "'" + testCountry + "'");
        }        
      }
    }
    
    if (baseCountry.length() != 2) {
      throw new IllegalArgumentException("country must be a two character "
        + "ISO country code (as used in top level domains, e.g. 'de' or 'us'): "
        + "'" + baseCountry + "'");
    }
    
    mDataServiceID = dataServiceID;
    
    mName = name;
    mId = id;
    mTimeZone = timeZone;
    // country, webpage and copyright will often be the same, so filter duplicates
    mBaseCountry = StringPool.getString(baseCountry);
    
    if(allCountries != null) {
      mAllCountries = new String[allCountries.length];
      
      for(int i = 0; i < mAllCountries.length; i++) {
        mAllCountries[i] = StringPool.getString(allCountries[i]);
      }
    }
    else {
      mAllCountries = new String[] {mBaseCountry};
    }
    
    if(copyrightNotice != null && copyrightNotice.toLowerCase().startsWith("(c)")) {
      copyrightNotice = "\u00A9" + copyrightNotice.substring(3);
    }
    
    mCopyrightNotice = StringPool.getString(copyrightNotice);
    mWebpage = StringPool.getString(webpage);
    mGroup = group;
    mDefaultIcon = icon;
    mCategories = categories;
    mUnescapedName = unescapedName;
    mSharedChannelId = sharedChannelId;
    mAccessControl = false;
  }
  
  /**
   * Creates an instance of this class.
   * <p>
   * @param dataService The data service of this channel.
   * @param name The name of this channel.
   * @param id The id of this channel.
   * @param timeZone The time zone of this channel.
   * @param baseCountry The base country of this channel.
   * @param copyrightNotice The copyright notice for this channel.
   * @param webpage The webpage of this channel.
   * @param group The group of this channel.
   * @param icon The icon for this channel.
   * @param categories The categories for this channel.
   * @param unescapedName The unescaped name for this channel.
   * @param allCountries All supported countries of this channel.
   *        ATTENTION: Have to contain the base country too.
   * @since 3.2.1
   */
  public Channel(AbstractTvDataService dataService, String name, String id,
    TimeZone timeZone, String baseCountry, String copyrightNotice, String webpage,
    devplugin.ChannelGroup group, Icon icon, int categories, String unescapedName,
    String[] allCountries)
  {
    this(dataService, name, id, timeZone, baseCountry, copyrightNotice, webpage, group, icon, categories, unescapedName, allCountries, null);
  }

  /**
   * Creates an instance of this class.
   * <p>
   * @param dataService The data service of this channel.
   * @param name The name of this channel.
   * @param id The id of this channel.
   * @param timeZone The time zone of this channel.
   * @param country The base country of this channel.
   * @param copyrightNotice The copyright notice for this channel.
   * @param webpage The webpage of this channel.
   * @param group The group of this channel.
   * @param icon The icon for this channel.
   * @param categories The categories for this channel.
   * @param unescapedName The unescaped name for this channel.
   */
  public Channel(AbstractTvDataService dataService, String name, String id,
    TimeZone timeZone, String country, String copyrightNotice, String webpage, devplugin.ChannelGroup group, Icon icon, int categories, String unescapedName)
  {
    this(dataService, name, id, timeZone, country, copyrightNotice, webpage, group, icon, categories, unescapedName, null);
  }
  
  /**
   * Creates an instance of this class.
   * <p>
   * @param dataService The data service of this channel.
   * @param name The name of this channel.
   * @param id The id of this channel.
   * @param timeZone The time zone of this channel.
   * @param country The base country of this channel.
   * @param copyrightNotice The copyright notice for this channel.
   * @param webpage The webpage of this channel.
   * @param group The group of this channel.
   * @param icon The icon for this channel.
   * @param categories The categories for this channel.
   */
  public Channel(AbstractTvDataService dataService, String name, String id,
    TimeZone timeZone, String country, String copyrightNotice, String webpage, devplugin.ChannelGroup group, Icon icon, int categories)
  {
    this(dataService, name, id, timeZone, country, copyrightNotice, webpage, group, icon, categories, null);
  }

  /**
   * Creates an instance of this class.
   * <p>
   * @param dataService The data service of this channel.
   * @param name The name of this channel.
   * @param id The id of this channel.
   * @param timeZone The time zone of this channel.
   * @param country The base country of this channel.
   * @param copyrightNotice The copyright notice for this channel.
   * @param webpage The webpage of this channel.
   * @param group The group of this channel.
   * @param icon The icon for this channel.
   */
  public Channel(AbstractTvDataService dataService, String name, String id,
    TimeZone timeZone, String country, String copyrightNotice, String webpage, devplugin.ChannelGroup group, Icon icon)
  {
    this(dataService, name, id, timeZone, country, copyrightNotice, webpage, group, icon, CATEGORY_NONE);
  }

  /**
   * Creates an instance of this class.
   * <p>
   * @param dataService The data service of this channel.
   * @param name The name of this channel.
   * @param id The id of this channel.
   * @param timeZone The time zone of this channel.
   * @param country The base country of this channel.
   * @param copyrightNotice The copyright notice for this channel.
   * @param webpage The webpage of this channel.
   * @param group The group of this channel.
   */
  public Channel(AbstractTvDataService dataService, String name, String id, TimeZone timeZone, String country, String copyrightNotice, String webpage, devplugin.ChannelGroup group) {
     this(dataService,name,id,timeZone,country,copyrightNotice,webpage,group,null);
  }

  /**
   * Creates an instance of this class.
   * <p>
   * @param dataService The data service of this channel.
   * @param name The name of this channel.
   * @param id The id of this channel.
   * @param timeZone The time zone of this channel.
   * @param country The base country of this channel.
   * @param copyrightNotice The copyright notice for this channel.
   * @param webpage The webpage of this channel.
   */
  public Channel(AbstractTvDataService dataService, String name, String id,
      TimeZone timeZone, String country, String copyrightNotice, String webpage) {

    this(dataService,name,id,timeZone,country,copyrightNotice,webpage,null);
  }

  /**
   * Creates an instance of this class.
   * <p>
   * @param dataService The data service of this channel.
   * @param name The name of this channel.
   * @param id The id of this channel.
   * @param timeZone The time zone of this channel.
   * @param country The base country of this channel.
   * @param copyrightNotice The copyright notice for this channel.
   */
  public Channel(AbstractTvDataService dataService, String name, String id,
      TimeZone timeZone, String country, String copyrightNotice)
  {
      this(dataService,name,id,timeZone,country, copyrightNotice,null);
  }

  /**
   * Creates an instance of this class.
   * <p>
   * @param dataService The data service of this channel.
   * @param name The name of this channel.
   * @param timeZone The time zone of this channel.
   * @param country The base country of this channel.
   * @param copyrightNotice The copyright notice for this channel.
   */
  public Channel(AbstractTvDataService dataService, String name, TimeZone timeZone,
    String country, String copyrightNotice)
  {
    this(dataService, name, name, timeZone, country, copyrightNotice, null);
  }
  
  /**
   * Creates an instance of this class.
   * <p>
   * @param id The id of this channel.
   * @param timeZone The time zone of this channel.
   * @param country The base country of this channel.
   */
  public Channel(String id, TimeZone timeZone, String country)
   {
      this(null,id,id,timeZone,country,"(no copyright notice)",null);
   }

  /**
   * Creates an instance of this class.
   * <p>
   * @param name The name of this channel.
   * @param id The id of this channel.
   * @param country The base country of this channel.
   */
  public Channel(String name, String id, String country)
   {
      this(null,name,id,TimeZone.getDefault(),country,"(no copyright notice)",null);
   }

  /**
   * Creates an instance of this class.
   * <p>
   * @param id The id of this channel.
   * @param country The base country of this channel.
   */
  public Channel(String id, String country)
   {
      this(null,id,id,TimeZone.getDefault(),country,"(no copyright notice)",null);
   }

  /**
   * Creates an instance of this class from a stream.
   * <p>
   * @param in The stream to load the data from.
   * @param allowNull <code>True</code> if the method is allowed to return <code>null</code>.
   * @return The load channel or <code>null</code> if <code>allowNull</code> is <code>true</code>
   * and the channel could not be load.
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public static Channel readData(ObjectInputStream in, boolean allowNull)
  throws IOException, ClassNotFoundException
  {
    int version = in.readInt();

    String dataServiceId = null;
    String groupId = null;
    String baseCountry = null;
    String channelId;

    if (version==1) {
      dataServiceId = (String)in.readObject();
      channelId = Integer.toString(in.readInt());
    }
    else if (version < 3){
      dataServiceId = (String)in.readObject();
      channelId=(String)in.readObject();
    }
    else if (version == 3){
      dataServiceId = in.readUTF();
      groupId = in.readUTF();
      channelId = in.readUTF();
    }
    else {
      dataServiceId = in.readUTF();
      groupId = in.readUTF();
      baseCountry = in.readUTF();
      channelId = in.readUTF();
    }

    Channel channel = getChannel(dataServiceId, groupId, baseCountry, channelId);
    if ((channel == null) && (! allowNull)) {
      throw new IOException("Channel with id " + channelId + " of data service "
        + dataServiceId + " not found!");
    }
    return channel;
  }


  /**
   * Method for OnDemandDayProgramFile file format version 2.
   *
   * @param in The file too read the Data from.
   * @param allowNull
   * @return channel
   * @throws IOException
   * @throws ClassNotFoundException
   *
   * @since 2.2
   */
  public static Channel readData(DataInput in, boolean allowNull)
    throws IOException, ClassNotFoundException
  {
    int version = in.readInt();
    Channel channel = null;

    String dataServiceId = null;
    String groupId = null;
    String baseCountry = null;
    String channelId = null;

    if(version < 3) {
      throw new IOException();
    }

    if(version == 3) {
      int length = in.readInt();
      byte[] b = new byte[length];

      in.readFully(b);

      dataServiceId = new String(b);

      length = in.readInt();
      b = new byte[length];
      in.readFully(b);
      channelId=new String(b);
    }
    else if(version == 4) {
      dataServiceId = in.readUTF();
      groupId = in.readUTF();
      channelId = in.readUTF();
    }
    else {
      dataServiceId = in.readUTF();
      groupId = in.readUTF();
      baseCountry = in.readUTF();
      channelId = in.readUTF();
    }
    
    channel = getChannel(dataServiceId, groupId, baseCountry, channelId);

    if ((channel == null) && (! allowNull)) {
      throw new IOException("Channel with id " + channelId + " of data service "
        + dataServiceId + " not found!");
    }
    return channel;
  }

  /**
   * Method for OnDemandDayProgramFile file format version 2.
   *
   * @param out The file to write the Data in.
   * @throws IOException
   *
   * @since 2.2
   */
  public void writeToDataFile(RandomAccessFile out) throws IOException {
    out.writeInt(5); // version
    out.writeUTF(mDataServiceID);
    out.writeUTF(getGroup().getId());
    out.writeUTF(getBaseCountry());
    out.writeUTF(mId);
  }

  /**
   * Serialized this object.
   * @param out The stream to write the values of this channel to.
   * @throws IOException
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(5); // version
    out.writeUTF(mDataServiceID);
    out.writeUTF(getGroup().getId());
    out.writeUTF(getBaseCountry());
    out.writeUTF(mId);
  }

  /**
   * Gets the copyright notice for this channel.
   * <p>
   * @return The copyright notice for this channel.
   */
  public String getCopyrightNotice() {
    return mCopyrightNotice!=null?mCopyrightNotice:"";
  }

  /**
   * Gets the webpage for this channel.
   * <p>
   * @return The webpage for this channel.
   */
  public String getWebpage() {
    if (getUserWebPage() != null) {
      return getUserWebPage();
    }
    return mWebpage;
  }

  /**
   * Gets the channel group of this channel.
   * <p>
   * @return The channel group of this channel.
   */
  public ChannelGroup getGroup() {
    return mGroup;
  }

  /**
   * Gets the categories of this channel.
   * <p>
   * @return The categories of this channel.
   */
  public int getCategories() {
    return mCategories;
  }

  /**
   * @param dataServiceId The id of the data service of the channel to get.
   * @param groupId The group id of the channel to get.
   * @param country The country of the channel to get.
   * @param channelId The id of the channel to get.
   *
   * @return The channel with the given ids or <code>null</code> if no channel with the ids was found.
   */
  public static Channel getChannel(String dataServiceId, String groupId, String country, String channelId) {
    if (dataServiceId == null) {
      // Fast return
      return null;
    }

    Channel[] channelArr = Plugin.getPluginManager().getSubscribedChannels();
    for (Channel channel : channelArr) {
      String chDataServiceId = channel.getDataServiceId();
      String chGroupId = channel.getGroup().getId();
      String chChannelId = channel.getId();
      String chCountry = channel.getBaseCountry();
      
      if (dataServiceId.compareTo(chDataServiceId) == 0 &&
          ((groupId != null && groupId.compareTo(chGroupId) == 0) || groupId == null) &&
          ((country != null && country.compareTo(chCountry) == 0) || country == null) &&
          channelId.compareTo(chChannelId) == 0)
      {
        return channel;
      }
    }

    return null;
  }

  /**
   * Gets the time zone of this channel.
   * <p>
   * @return The time zone of this channel.
   */
  public TimeZone getTimeZone() {
    return mTimeZone;
  }

  /**
   * Gets the country of this channel.
   * <p>
   * @return The country of this channel.
   * @deprecated since 3.2.1 use {@link #getBaseCountry()} instead.
   */
  public String getCountry() {
    return getBaseCountry();
  }
  
  /**
   * Gets the base country of this channel.
   * <p>
   * @return The country of this channel.
   * @since 3.2.1
   */
  public String getBaseCountry() {
    return mBaseCountry;
  }
  
  /**
   * @return All countries of this channel.
   */
  public String[] getAllCountries() {
    return mAllCountries;
  }
  
  /**
   * Corrects the time zone offset of the channel.
   * <p>
   * @param minutes The offset in minutes. Valid values are only half or full hours (positive and negative).
   * @since 3.0
   */
  public void setTimeZoneCorrectionMinutes(int minutes) {
    ChannelUserSettings.getSettings(this).setTimeZoneCorrectionMinutes(minutes);
  }
  
  /**
   * Gets the time zone offset of this channel.
   * <p>
   * @return time zone offset
   * @since 3.0
   */
  public int getTimeZoneCorrectionMinutes() {
    return ChannelUserSettings.getSettings(this).getTimeZoneCorrectionMinutes();
  }

  /**
   * Gets the data service proxy of this channel
   * <p>
   * @return The data service proxy of this channel
   */
  public TvDataServiceProxy getDataServiceProxy() {
    if(mDataServiceID != null) {
      if (mProxy == null) {
        mProxy = TvDataServiceProxyManager.getInstance().findDataServiceById(mDataServiceID);
      }
      return mProxy;
    } else {
      return null;
    }
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


  @Override
  public String toString() {
    return (getSortNumber().trim().length() > 0 ? getSortNumber() + ". " : "") + getName();
  }

  /**
   * Gets the name of this channel.
   * <p>
   * @return The name of this channel.
   */
  public String getName() {
    if (getUserChannelName() != null) {
      return getUserChannelName();
    }
    return mName;
  }

  /**
   * Gets the id of this channel.
   * <p>
   * @return The id of this channel.
   */
  public String getId() {
    return mId;
  }

  /**
   * @return the Icon for this Channel
   */
  public Icon getIcon() {
    if (isUsingUserIcon() && (mIcon == null) && (getUserIconFileName() != null)){
      Image img = ImageUtilities.createImageAsynchronous(getUserIconFileName());
      if (img != null) {
        mIcon = new ImageIcon(img);
      }
    }

    if (mIcon == null && mDefaultIcon != null) {
      return mDefaultIcon;
    }
    else if(mIcon == null) {
      return UiUtilities.createChannelIcon(null);
    }

    return mIcon;
  }

  /**
   * Gets if this channel has an icon.
   * <p>
   * @return <code>True</code> if this channel has an icon,
   * <code>false</code> otherwise.
   */
  public boolean hasIcon() {
    if (isUsingUserIcon() && (mIcon == null) && (getUserIconFileName() != null)) {
      getIcon();
    }

    return (mIcon != null) || (mDefaultIcon != null);

  }

  /**
   * Sets the user icon file name.
   * <p>
   *
   * @param filename
   *          The file name of the user icon file.
   */
  public void setUserIconFileName(String filename) {
    ChannelUserSettings.getSettings(this).setIconFileName(filename);
    mIcon = null;
  }

  /**
   *
   * @return null, if no user icon filename is specified
   */
  public String getUserIconFileName() {
    return IOUtilities.translateRelativePath(ChannelUserSettings.getSettings(this).getIconFileName());
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
   * Set the WebPage used by the User
   *
   * @param url
   *          WebPage
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

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Channel) {
      Channel cmp = (Channel) obj;

      // this is for the example program
      if((mDataServiceID == null && cmp.getDataServiceProxy() == null) &&
        (getGroup() == null && cmp.getGroup() == null) &&
        (getId().compareTo(cmp.getId())) == 0) {
        return true;
      }

      try {
        String channelId = getId();
        String cmpChannelId = cmp.getId();

    	  if (channelId.compareTo(cmpChannelId) != 0) {
    		  return false;
    	  }

    	  String groupId = getGroup().getId();
        String cmpGroupId = cmp.getGroup().getId();

        if (groupId.compareTo(cmpGroupId) != 0) {
          return false;
        }

        String dataServiceId = mDataServiceID;
        String cmpDataServiceId = cmp.getDataServiceId();

        if(dataServiceId.compareTo(cmpDataServiceId) != 0) {
          return false;
        }

        String country = getBaseCountry();
        String cmpCountry = cmp.getBaseCountry();

        return country.compareTo(cmpCountry) == 0;
      }catch(Exception e) {
      }
    }

    return false;
  }

  /**
   * Gets if this channel is limited in start and end time.
   *
   * @return <code>True</code> if this channel is time limited.
   * @since 2.2.4/2.6
   */
  public boolean isTimeLimited() {
    return ChannelUserSettings.getSettings(this).isTimeLimited();
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
    return ChannelUserSettings.getSettings(this).getStartTimeLimit();
  }

  /**
   * Sets the start time limit to the new value.
   *
   * @param startTimeLimit The new value for start time
   * limit in minutes of day.
   * @since 2.2.4/2.6
   */
  public void setStartTimeLimit(int startTimeLimit) {
    ChannelUserSettings.getSettings(this).setStartTimeLimit(startTimeLimit);
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
    return ChannelUserSettings.getSettings(this).getEndTimeLimit();
  }

  /**
   * Sets the end time limit to the new value.
   *
   * @param endTimeLimit The new value for end time
   * limit in minutes of day.
   * @since 2.2.4/2.6
   */
  public void setEndTimeLimit(int endTimeLimit) {
    ChannelUserSettings.getSettings(this).setEndTimeLimit(endTimeLimit);
  }

  public int compareTo(Channel other) {
    return getName().compareToIgnoreCase(other.getName());
  }

  /**
   * Sets the name of this channel.
   * If this is used by a Plugin nothing will happen.
   *
   * @param name The new name for the channel.
   * @since 2.2.5/2.6.3
   */
  public void setChannelName(String name) {
    if(ChannelList.hasCalledChannelValueChangeForChannel(this)) {
      mName = name;
    }
  }

  /**
   * Sets the copyright notice of this channel.
   * If this is used by a Plugin nothing will happen.
   *
   * @param copyrightNotice The new copyright notice for the channel.
   * @since 2.2.5/2.6.3
   */
  public void setChannelCopyrightNotice(String copyrightNotice) {
    if(ChannelList.hasCalledChannelValueChangeForChannel(this)) {
      mCopyrightNotice = copyrightNotice;
    }
  }

  /**
   * Sets the webpage of this channel.
   * If this is used by a Plugin nothing will happen.
   *
   * @param webpage The new webpage for the channel.
   * @since 2.2.5/2.6.3
   */
  public void setChannelWebpage(String webpage) {
    if(ChannelList.hasCalledChannelValueChangeForChannel(this)) {
      mWebpage = webpage;
    }
  }

  /**
   * Gets the unescaped name of this channel.
   * <p>
   * @return The unescaped name of this channel.
   */
  public String getUnescapedName() {
    if (mUnescapedName != null) {
      return mUnescapedName;
    }
    return mName;
  }

  /**
   * Gets a unique identifier of this channel.
   * <p>
   * @return The unique identifier of this channel.
   * @since 2.7
   */
  public String getUniqueId() {
    if (mUniqueId == null) {
      mUniqueId = new StringBuilder(mDataServiceID).append('_').append(getGroup().getId()).append('_').append(getBaseCountry()).append('_').append(getId()).toString();
    }
    return mUniqueId;
  }

  /**
   * get the localized name of the given category
   *
   * @param category
   *          category bit, see Channel.CATEGORY_XYZ
   * @return localized name
   * @since 3.0
   */
  public static String getLocalizedCategory(final int category) {
    if (categoryName == null) {
      HashMap<Integer, String> catName = new HashMap<Integer, String>(12);
      catName.put(CATEGORY_NONE, mLocalizer.msg("categoryNone",
          "Not categorized"));
      catName.put(CATEGORY_TV, mLocalizer.msg("categoryTVAll", "TV"));
      catName
          .put(CATEGORY_RADIO, mLocalizer.msg("categoryRadio", "Radio"));
      catName.put(CATEGORY_CINEMA, mLocalizer.msg("categoryCinema",
          "Cinema"));
      catName.put(CATEGORY_EVENTS, mLocalizer.msg("categoryEvents",
          "Events"));
      catName.put(CATEGORY_DIGITAL, mLocalizer.msg("categoryDigital",
          "Digitale"));
      catName.put(CATEGORY_SPECIAL_MUSIC, mLocalizer.msg("categoryMusic",
          "Musik"));
      catName.put(CATEGORY_SPECIAL_SPORT, mLocalizer.msg("categorySport",
          "Sport"));
      catName.put(CATEGORY_SPECIAL_NEWS, mLocalizer.msg("categoryNews",
          "Nachrichten"));
      catName.put(CATEGORY_SPECIAL_OTHER, mLocalizer.msg("categoryOthers",
          "Sonstige Sparten"));
      catName.put(CATEGORY_PAY_TV, mLocalizer.msg("categoryPayTV",
          "Pay TV"));
      catName.put(CATEGORY_PAYED_DATA_TV, mLocalizer.msg(
          "categoryPayedData", "Payed Data"));
      categoryName = catName;
    }
    return categoryName.get(category);
  }

  public String getDataServicePackageName() {
    return mDataServiceID.substring(0, mDataServiceID.indexOf("."));
  }
  
  public String getDataServiceId() {
    return mDataServiceID;
  }
  
  /**
   * Gets the countries String for this channel.
   * <p>
   * @return The countries String for this channel.
   * @since 3.2.1
   */
  public String getCountriesString() {
    StringBuilder builder = new StringBuilder();
    
    for(int i = 0; i < mAllCountries.length; i++) {
      builder.append(mAllCountries[i]);
      
      if(i < mAllCountries.length-1) {
        builder.append("$");
      }
    }
    
    return builder.toString();
  }
  
  /**
   * Sets the joint channel of this channel.
   * <p>
   * @param ch The joint channel or <code>null</code> if there is no joint channel.
   */
  public void setJointChannel(Channel ch) {
    if(ch == null && mJointChannel != null) {
      mJointChannel.mBaseChannel = null;
    }
    
    mJointChannel = ch;
    
    if(mJointChannel != null) {
      mJointChannel.mBaseChannel = this;
      mJointChannelIcon = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
          int y1 = y + getIconHeight()/2 - getIcon().getIconHeight()/2;
          getIcon().paintIcon(c, g, x, y1);
          g.setColor(Color.black);
          g.drawLine(x+getIcon().getIconWidth()+3, y, x+getIcon().getIconWidth()+3, getIconHeight());
          g.drawLine(x+getIcon().getIconWidth()+4, y, x+getIcon().getIconWidth()+4, getIconHeight());
          
          y1 = y + getIconHeight()/2 - mJointChannel.getIcon().getIconHeight()/2;
          
          mJointChannel.getIcon().paintIcon(c, g, x+7+getIcon().getIconWidth(), y1);
        }
  
        @Override
        public int getIconWidth() {
          return getIcon().getIconWidth() + mJointChannel.getIcon().getIconWidth() + 7;
        }
  
        @Override
        public int getIconHeight() {
          return Math.max(getIcon().getIconHeight(), mJointChannel.getIcon().getIconHeight());
        }
        
      };
    }
    else {
      mJointChannelIcon = null;
    }
  }
  
  /**
   * Gets the joint channel name of this channel
   * <p>
   * @return The joint channel name of this channel or <code>null</code> if there is no joint channel name.
   * @since 3.2.1
   */
  public String getJointChannelName() {
    if(mJointChannel != null) {
      return getName() + "/" + mJointChannel.getName();
    }
    
    return null;
  }
  
  /**
   * Gets the joint channel icon of this channel
   * <p>
   * @return The joint channel icon of this channel or <code>null</code> if there is no joint channel icon.
   * @since 3.2.1
   */
  public Icon getJointChannelIcon() {
    return mJointChannelIcon;
  }
  
  /**
   * Gets the joint channel of this channel.
   * <p>
   * @return The joint channel of this channel or <code>null</code> if there is no joint channel.
   * @since 3.2.1
   */
  public Channel getJointChannel() {
    return mJointChannel;
  }
  
  /**
   * Gets the base channel of this channel.
   * <p>
   * @return The base channel of this channel or <code>null</code> if there is no base channel.
   * @since 3.2.1
   */
  public Channel getBaseChannel() {
    return mBaseChannel;
  }
  
  /**
   * Gets the shared channel id for this channel.
   * <p>
   * @return The shared channel id or <code>null</code> if there is none.
   * @since 3.2.1
   */
  public String getSharedChannelId() {
    return mSharedChannelId;
  }
  
  /**
   * Gets the base channel for the given channel if joined or returns the channel.
   * <p>
   * @since 3.2.1
   * @param ch The channel to check.
   * @return The given channel if not joined or the base channel if joined.
   */
  public static Channel getChannelForChannel(Channel ch) {
    if(ch.getBaseChannel() != null) {
      return ch.getBaseChannel();
    }
    
    return ch;
  }
  

  /**
   * Gets the sort number of this channel.
   * 
   * @return The sort number of this channel.
   * @since 3.3.4
   */
  public String getSortNumber() {
    return ChannelUserSettings.getSettings(this).getSortNumber();
  }
  
  /**
   * Sets the sort number of this channel to the given value.
   * <p>
   * @param number The sort number for this channel or <code>null</code> if channel has no sort number.
   * @since 3.3.4
   */
  public void setSortNumber(String number) {
    ChannelUserSettings.getSettings(this).setSortNumber(number);
  }
  
  /**
   * @return If channel is access controled.
   * @since 3.3.4
   */
  public boolean isAccessControl() {
    return mAccessControl;
  }
}
