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
 *
 */

package tvbrowser.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.core.tvdataservice.ChannelGroupManager;
import devplugin.Channel;
import devplugin.ChannelGroup;


/**
 * ChannelList contains a list of all available mAvailableChannels in the system.
 * Use this class to subscribe mAvailableChannels.
 * The available mAvailableChannels are listed in the file CHANNEL_FILE.
 *
 * @author Martin Oberhauser
 */
public class ChannelList {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(ChannelList.class.getName());

  private static ArrayList<Channel> mAvailableChannels = new ArrayList<Channel>();

  private static ArrayList<Channel> mSubscribedChannels = new ArrayList<Channel>();

  private static Thread mCompleteChannelThread;

  private static Map<Channel,String> mChannelIconMap, mChannelNameMap, mChannelWebpagesMap, mChannelDayLightCorrectionMap;

  /**
   * Load the not subscribed channels after
   * TV-Browser start was finished.
   */
  public static void completeChannelLoading() {
    mCompleteChannelThread = new Thread() {
      public void run() {
        mLog.info("Loading the not subscribed channels");
        create();
        mLog.info("Loading off all channels complete");
      }
    };
    mCompleteChannelThread.start();
  }

  /**
   * Reload the channel list.
   */
  public static void reload() {
    mAvailableChannels.clear();
    loadChannelMaps();
    create();
  }

  private static void create() {
    TvDataServiceProxy[] dataServiceArr
    = TvDataServiceProxyManager.getInstance().getDataServices();

    for (int i=0;i<dataServiceArr.length;i++)
      addDataServiceChannels(dataServiceArr[i]);

    clearChannelMaps();
  }

  /**
   * Init the subscribed channels
   */
  public static void initSubscribedChannels() {
    Channel[] channelArr = Settings.propSubscribedChannels.getChannelArray();

    for (Channel channel : channelArr)
      if (channel != null)
        subscribeChannel(channel);
  }

  /**
   * Stores all settings used for the Channels
   */
  public static void storeAllSettings() {
      storeDayLightSavingTimeCorrections();
      storeChannelIcons();
      storeChannelNames();
      storeChannelWebPages();
  }

  public static void storeDayLightSavingTimeCorrections() {
    File f=new File(Settings.getUserSettingsDirName(),"daylight_correction.txt");

    FileWriter fw;
    PrintWriter out=null;
    try {
      fw=new FileWriter(f);
      out=new PrintWriter(fw);
      Channel[] channels=getSubscribedChannels();
        for (int i=0;i<channels.length;i++) {
          int corr=channels[i].getDayLightSavingTimeCorrection();
          if (corr!=0) {
            out.println(channels[i].getDataServiceProxy().getId()+":"+ channels[i].getGroup().getId()+":"+channels[i].getCountry()+":"+channels[i].getId()+"="+corr);
          }
        }
    }catch(IOException e) {
      // ignore
    }
    if (out!=null) {
      out.close();
    }

  }

  private static void addDataServiceChannels(TvDataServiceProxy dataService) {
    Channel[] channelArr = dataService.getAvailableChannels();

    for (Channel channel : channelArr)
      addChannelToAvailableChannels(channel);
  }

  private static void addDataServiceChannelsForTvBrowserStart(TvDataServiceProxy dataService) {
    Channel[] channelArr = dataService.getChannelsForTvBrowserStart();

    for (Channel channel : channelArr)
      addChannelToAvailableChannels(channel);
  }

  private static void addChannelToAvailableChannels(Channel channel) {
    if(!mAvailableChannels.contains(channel)) {
      mAvailableChannels.add(channel);

      String key = getIdForChannel(channel);

      if(!mChannelDayLightCorrectionMap.isEmpty())
        setDayLightSavingTimeCorrectionsForChannel(channel, key);

      if(!mChannelIconMap.isEmpty())
        setChannelIconForChannel(channel, key);

      if(!mChannelNameMap.isEmpty())
        setChannelNameForChannel(channel, key);

      if(!mChannelWebpagesMap.isEmpty())
        setWebPageForChannel(channel, key);
    }
  }


  private static void loadChannelMaps() {
    mChannelIconMap = createMap(new File(Settings.getUserSettingsDirName(),"channel_icons.txt"));
    mChannelNameMap = createMap(new File(Settings.getUserSettingsDirName(),"channel_names.txt"));
    mChannelWebpagesMap = createMap(new File(Settings.getUserSettingsDirName(),"channel_webpages.txt"));
    mChannelDayLightCorrectionMap = createMap(new File(Settings.getUserSettingsDirName(),"daylight_correction.txt"));
  }

  private static void clearChannelMaps() {
    mChannelIconMap = null;
    mChannelNameMap = null;
    mChannelWebpagesMap = null;
    mChannelDayLightCorrectionMap = null;
  }

  /**
   * Creates the needed channels for TV-Browser start
   */
  public static void createForTvBrowserStart() {
    mAvailableChannels.clear();
    TvDataServiceProxy[] dataServiceArr
            = TvDataServiceProxyManager.getInstance().getDataServices();

    loadChannelMaps();

    for (int i=0;i<dataServiceArr.length;i++)
      addDataServiceChannelsForTvBrowserStart(dataServiceArr[i]);
  }

  /**
   * Subscribes a channel
   * @param channel
   */
  public static void subscribeChannel(Channel channel) {
    mSubscribedChannels.add(channel);
  }

  /**
   * Marks the specified mAvailableChannels as 'subscribed'. All other mAvailableChannels become
   * 'unsubscribed'
   * @param channelArr The channels to set as subscribed channels,
   */
  public static void setSubscribeChannels(Channel[] channelArr) {
    mSubscribedChannels = new ArrayList<Channel>(channelArr.length);
    for (int i = 0; i < channelArr.length; i++) {
      if (channelArr[i] == null)
        mLog.warning("cannot subscribe channel #" + i + " - is null");
      else
        mSubscribedChannels.add(channelArr[i]);
    }
  }

  /**
   * Returns a new Channel object with the specified IDs or null, if the
   * given IDs does not exist.
   * @param dataServiceId The id of the data service to get the channel from.
   * @param groupId The id of the channel group.
   * @param country The country of the channel.
   * @param channelId The id of the channel.
   *
   * @return The specified channel or <code>null</code> if the channel wasn't found.
   *
   * @since 2.2.1
   */
  public static Channel getChannel(String dataServiceId, String groupId, String country, String channelId) {
    Iterator iter = mAvailableChannels.iterator();

    TvDataServiceProxy dataService = null;

    if(dataServiceId != null) {
      dataService = TvDataServiceProxyManager.getInstance().findDataServiceById(dataServiceId);

      if(dataService == null)
        return null;
    }

    while (iter.hasNext()) {
      Channel channel = (Channel) iter.next();

      if (((dataServiceId != null && channel.getDataServiceProxy().getId().compareTo(dataService.getId()) == 0) ||
            dataServiceId == null)
          && ((groupId != null && channel.getGroup().getId().compareTo(groupId) == 0) ||
              groupId == null)
          && ((country != null && channel.getCountry().compareTo(country) == 0) ||
              country == null)
          && channel.getId().compareTo(channelId) == 0) {
        return channel;
      }
    }

    /* If we haven't found the channel within the 'available channels', we try to find it
       in an unsubscribed group.
       If we find it there, we subscribe the affected group and add all channels of this group to
       the 'available channels' list
    */
 /*

   ------ MERGE:  these lines were removed in trunk in revision 2501
                  but changed in branch-2-2-x   ----------
    ChannelGroup[] groupArr = dataService.getAvailableGroups();
    for (int i=0; i<groupArr.length; i++) {
      if (!ChannelGroupManager.getInstance().isSubscribedGroup(groupArr[i]) &&
          ((groupId != null && groupArr[i].getId().equals(groupId)) ||
              groupId == null)) {
        Channel[] channelArr = dataService.getAvailableChannels(groupArr[i]);
        for (int j=0; j<channelArr.length; j++) {
          if (channelId.equals(channelArr[j].getId())) {
            ChannelGroupManager.getInstance().subscribeGroup(groupArr[i]);
            for (int k=0; k<channelArr.length; k++) {
              mAvailableChannels.add(channelArr[k]);
            }
            return channelArr[j];
          }
        }
      }
    }   */

    return null;
  }

  public static int getPos(Channel channel) {
    for (int i = 0; i < mSubscribedChannels.size(); i++) {
      Channel ch = (Channel) mSubscribedChannels.get(i);
      if (ch.equals(channel))
        return i;
    }
    return -1;
  }


  /**
   * @return The available channels in an array.
   */
  public static Channel[] getAvailableChannels() {
    Channel[] result = new Channel[mAvailableChannels.size()];
    mAvailableChannels.toArray(result);
    return result;
  }


  /**
   * Returns true, if the specified channel is currently subscribed.
   *
   * @param channel The channel to check if it is subscribed,
   * @return True if the channel is subscribed, false otherwise.
   */
  public static boolean isSubscribedChannel(Channel channel) {
    if (channel==null) return false;
    for (int i=0;i<mSubscribedChannels.size();i++) {
      Channel ch=(Channel)mSubscribedChannels.get(i);
      if (ch!=null && ch.getId().equals(channel.getId()) && ch.getDataServiceProxy().equals(channel.getDataServiceProxy())) {
        return true;
      }
    }
    return false;
  }



  /**
   * Returns the number of subscribed mAvailableChannels.
   * @return The number of the subscribed channels.
   */
  public static int getNumberOfSubscribedChannels() {
  return mSubscribedChannels.size();
  }



  /**
   * Returns all subscribed mAvailableChannels.
   * @return All subscribed channels in an array.
   */
  public static Channel[] getSubscribedChannels() {
  Channel[] result=new Channel[mSubscribedChannels.size()];
  for (int i=0;i<mSubscribedChannels.size();i++) {
    result[i]=(Channel)mSubscribedChannels.get(i);
  }
  return result;
  }

  /**
   * Set the day light time correction for a channel.
   *
   * @param channel The channel to set the value.
   * @param key The id of the channel.
   */
  private static void setDayLightSavingTimeCorrectionsForChannel(Channel channel, String key) {
    String value = mChannelDayLightCorrectionMap.get(key);

    if(value != null && value.length() > 0) {
      int corr=Integer.parseInt(value);
      channel.setDayLightSavingTimeCorrection(corr);
    }
  }

  /**
   * Set the icon for a channel.
   *
   * @param channel The channel to set the value for.
   * @param key The id of the channel.
   */
  private static void setChannelIconForChannel(Channel channel, String key) {
    String value = mChannelIconMap.get(key);

    if(value != null && value.length() > 0) {
      String[] settings = value.split(";");

      if (settings.length == 2) {
        channel.setUserIconFileName(settings[1]);
        if (settings[0].equals("true")) {
          channel.useUserIcon(true);
        } else {
          channel.useUserIcon(false);
        }
      }
    }
  }

  /**
   * Sets the name for the given channel.
   *
   * @param channel The channel to set the name for.
   * @param key The id of the channel.
   */
  private static void setChannelNameForChannel(Channel channel, String key) {
    String value = mChannelNameMap.get(key);

    if(value != null && value.length() > 0)
      channel.setUserChannelName(value);
  }

  /**
   * Sets the web page for a channel.
   *
   * @param channel The channel to set the web page for.
   * @param key The id of the channel.
   */
  private static void setWebPageForChannel(Channel channel, String key) {
    String value = mChannelWebpagesMap.get(key);

    if(value != null && value.length() > 0)
      channel.setUserWebPage(value);
  }


  /**
   * Stores all Icons
   */
  private static void storeChannelIcons() {
      File f=new File(Settings.getUserSettingsDirName(),"channel_icons.txt");

      FileWriter fw;
      PrintWriter out = null;

      try {
        fw=new FileWriter(f);
        out=new PrintWriter(fw);
        Channel[] channels=getSubscribedChannels();
          for (int i=0;i<channels.length;i++) {
            String filename = channels[i].getUserIconFileName();
            if ((filename != null) && (filename.trim().length() > 0)){
              out.println(channels[i].getDataServiceProxy().getId()+":"+channels[i].getGroup().getId()+":"+channels[i].getCountry()+":"+channels[i].getId()+"=" + channels[i].isUsingUserIcon() +";"+filename.trim());
            }
          }
      }catch(IOException e) {
        // ignore
      }
      if (out!=null) {
        out.close();
      }
  }



  /**
   * Saves the channel names for all channels.
   */
  private static void storeChannelNames() {
    File f=new File(Settings.getUserSettingsDirName(),"channel_names.txt");

    FileWriter fw;
    PrintWriter out = null;

    try {
      fw=new FileWriter(f);
      out=new PrintWriter(fw);
      Channel[] channels=getSubscribedChannels();
        for (int i=0;i<channels.length;i++) {
          String userChannelName = channels[i].getUserChannelName();
          if ((userChannelName != null) && (userChannelName.trim().length() > 0)){
            out.println(channels[i].getDataServiceProxy().getId()+":"+channels[i].getGroup().getId()+":"+channels[i].getCountry()+":"+channels[i].getId()+"=" + userChannelName.trim());
          }
        }
    }catch(IOException e) {
      // ignore
    }
    if (out!=null) {
      out.close();
    }
  }

  /**
   * Saves the web pages of all channels.
   *
   */
  private static void storeChannelWebPages() {
    File f=new File(Settings.getUserSettingsDirName(),"channel_webpages.txt");

    FileWriter fw;
    PrintWriter out = null;

    try {
      fw=new FileWriter(f);
      out=new PrintWriter(fw);
      Channel[] channels=getSubscribedChannels();
        for (int i=0;i<channels.length;i++) {
          String userWebPage = channels[i].getUserWebPage();
          if ((userWebPage != null) && (userWebPage.trim().length() > 0)){
            out.println(channels[i].getId()+":"+channels[i].getGroup()+":"+channels[i].getCountry()+channels[i].getId()+"=" + userWebPage.trim());
          }
        }
    }catch(IOException e) {
      // ignore
    }
    if (out!=null) {
      out.close();
    }
  }

  private static String getIdForChannel(Channel channel) {
    return new StringBuffer(channel.getDataServiceProxy().getId()).append(":").append(channel.getId()).toString();
  }

  /**
   * Create a HashMap from a Settings-File
   * @param f File to Load
   * @return HashMap filled with Channel, Value
   */
  private static HashMap<Channel, String> createMap(File f) {
    HashMap<Channel, String> map = new HashMap<Channel, String>();

    if (!f.exists()) {
      return map;
    }

    FileReader fr;
    BufferedReader reader = null;

    try {
      fr = new FileReader(f);
      reader = new BufferedReader(fr);
      String line;

      while ((line = reader.readLine()) != null){
        int pos = line.indexOf('=');

        try {
          String key=line.substring(0,pos);
          String val=line.substring(pos+1);
          if (val!=null) {
            pos = key.indexOf(':');
            String dataServiceId = key.substring(0,pos);
            String groupId = null;
            String country = null;
            String channelId = key.substring(pos + 1);

            if(channelId.indexOf(":") != -1) {
              String[] values = channelId.split(":");
              groupId = values[0];

              if(values.length > 2) {
                country = values[1];
                channelId = values[2];
              }
              else
                channelId = values[1];
            }

            Channel ch = ChannelList.getChannel(dataServiceId, groupId, country, channelId);
            if (ch != null) {
              map.put(ch,val);
            }
          }

        }catch(IndexOutOfBoundsException e) {
          // ignore
        }
      }

    }catch(IOException e) {
      // ignore
    }
    if (reader!=null) {
      try { reader.close(); }catch(IOException exc){
        // ignore
      }
    }
    return map;
  }

  /**
   * @return The Thread of loading the not used channels.
   */
  public static Thread getChannelLoadThread() {
    return mCompleteChannelThread;
  }
}