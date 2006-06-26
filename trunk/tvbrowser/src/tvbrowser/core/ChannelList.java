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
import devplugin.Channel;


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

  private static Map<String,String> mChannelIconMap, mChannelNameMap, mChannelWebpagesMap, mChannelDayLightCorrectionMap;

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

    for (int i=0;i<dataServiceArr.length;i++) {
      addDataServiceChannels(dataServiceArr[i]);
    }

    /* remove all subscribed channels which are not available any more */
    Object[] currentSubscribedChannels = mSubscribedChannels.toArray();
    for (int i=0; i<currentSubscribedChannels.length; i++) {
      Channel ch = (Channel)currentSubscribedChannels[i];
      if (!mAvailableChannels.contains(ch)) {
        mLog.warning(ch+" is not available any more");
        mSubscribedChannels.remove(ch);
      }
    }
    
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
   * Set the day light time correction for a channel.
   * 
   * @param channel The channel to set the value.
   */
  public static void setDayLightSavingTimeCorrectionsForChannel(Channel channel) {
    String value = mChannelDayLightCorrectionMap.get(getIdForChannel(channel));
    
    if(value != null && value.length() > 0) {
      int corr=Integer.parseInt(value);
      channel.setDayLightSavingTimeCorrection(corr);
    }    
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

  /**
   * Save the day light saving time correction for all channels
   */
  public static void storeDayLightSavingTimeCorrections() {
    File f=new File(Settings.getUserSettingsDirName(),"daylight_correction.txt");

    FileWriter fw;
    PrintWriter out = null;
    
    try {
      fw = new FileWriter(f);
      out = new PrintWriter(fw);
      
      Channel[] channelArr = getAvailableChannels();
      
      for (Channel channel : channelArr) {
        int corr=channel.getDayLightSavingTimeCorrection();
        
        if (corr!=0)
          out.println(channel.getDataServiceProxy().getId()+":"+channel.getId()+"="+corr);
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
    for (Channel channel : channelArr) {
      if(!mAvailableChannels.contains(channel)) {
        mAvailableChannels.add(channel);
        setDayLightSavingTimeCorrectionsForChannel(channel);
        setChannelIconForChannel(channel);
        setChannelNameForChannel(channel);
        setWebPageForChannel(channel);
      }
    }
  }

  private static void addDataServiceChannelsForTvBrowserStart(TvDataServiceProxy dataService) {
    Channel[] channelArr = dataService.getChannelsForTvBrowserStart();
    for (Channel channel : channelArr) {
      if(!mAvailableChannels.contains(channel)) {
        mAvailableChannels.add(channel);
        setDayLightSavingTimeCorrectionsForChannel(channel);
        setChannelIconForChannel(channel);
        setChannelNameForChannel(channel);
        setWebPageForChannel(channel);
      }
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
    
    for (int i=0;i<dataServiceArr.length;i++) {
      addDataServiceChannelsForTvBrowserStart(dataServiceArr[i]);
    }

    /* remove all subscribed channels which are not available any more */
    Object[] currentSubscribedChannels = mSubscribedChannels.toArray();
    for (int i=0; i<currentSubscribedChannels.length; i++) {
      Channel ch = (Channel)currentSubscribedChannels[i];
      if (!mAvailableChannels.contains(ch)) {
        mLog.warning(ch+" is not available any more");
        mSubscribedChannels.remove(ch);
      }
    }
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
   * Returns a new Channel object with the specified ID or null, if the
   * given ID does not exist.
   * @param dataService The data service of the channel.
   * @param id ID of the channel.
   * @return The channel or <code>null</code> if there is no channel with the id.
   */
  public static Channel getChannel(TvDataServiceProxy dataService, String id) {
    if (dataService == null) {
      return null;
    }

    Iterator iter = mAvailableChannels.iterator();
    while (iter.hasNext()) {
      Channel channel = (Channel) iter.next();
      
      if (channel.getDataServiceProxy().getId().equals(dataService.getId()) && channel.getId().equals(id)) {
        return channel;
      }
    }

    return null;
  }


  /**
   * Returns the channel for the given ID.
   * 
   * @param id The ID of the channel to get.
   * @return The channel with the id or <code>null</code> if there is no channel with the id.
   */
  public static Channel getChannel(String id) {
    for (Channel channel : mAvailableChannels)      
      if (channel.getId().equals(id))
        return channel;
    
    return null;
  }


  /**
   * Returns the position of the channel.
   * 
   * @param channel The channel to get the position for.
   * @return The position of the channel or -1 if it is not in the subscribed channel list.
   */
  public static int getPos(Channel channel) {
    for (int i = 0; i < mSubscribedChannels.size(); i++) {
      Channel ch = (Channel) mSubscribedChannels.get(i);
      if (ch.equals(channel))
        return i;
    }
    return -1;
  }



  /**
   * Returns an Enumeration of all available Channel objects.
   *
   * @deprecated Use getAvailableChannels
   */
  public static Iterator getChannels() {
    return mAvailableChannels.iterator();
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
   * Loads the Icon-Filenames
   */
  private static void setChannelIconForChannel(Channel channel) {
    String value = mChannelIconMap.get(getIdForChannel(channel));
    
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
   * Stores all Icons
   */
  private static void storeChannelIcons() {
      File f=new File(Settings.getUserSettingsDirName(),"channel_icons.txt");

      FileWriter fw;
      PrintWriter out = null;
      
      try {
        fw = new FileWriter(f);
        out = new PrintWriter(fw);
        
        Channel[] channelArr = getAvailableChannels();
        
        for (Channel channel : channelArr) {
          String filename = channel.getUserIconFileName();
          
          if ((filename != null) && (filename.trim().length() > 0))
            out.println(channel.getDataServiceProxy().getId()+":"+channel.getId()+"=" + channel.isUsingUserIcon() +";"+filename.trim());
        }
      }catch(IOException e) {
        // ignore
      }
      if (out!=null) {
        out.close();
      }
  }

  /**
   * Sets the name for the given channel.
   * 
   * @param channel The channel to set the name for.
   */
  public static void setChannelNameForChannel(Channel channel) {
    String value = mChannelNameMap.get(getIdForChannel(channel));
    
    if(value != null && value.length() > 0)      
      channel.setUserChannelName(value);
  }

  /**
   * Saves the channel names for all channels.
   */
  public static void storeChannelNames() {
    File f=new File(Settings.getUserSettingsDirName(),"channel_names.txt");

    FileWriter fw;
    PrintWriter out = null;
    
    try {
      fw = new FileWriter(f);
      out = new PrintWriter(fw);
      
      Channel[] channelArr = getAvailableChannels();
      
      for (Channel channel : channelArr) {
        String userChannelName = channel.getUserChannelName();
        
        if ((userChannelName != null) && (userChannelName.trim().length() > 0))
          out.println(channel.getDataServiceProxy().getId()+":"+channel.getId()+"=" + userChannelName.trim());
      }
    }catch(IOException e) {
      // ignore
    }
    if (out!=null) {
      out.close();
    }
  }

  /**
   * Sets the web page for a channel.
   * 
   * @param channel The channel to set the web page for.
   */
  public static void setWebPageForChannel(Channel channel) {
    String value = mChannelWebpagesMap.get(getIdForChannel(channel));
    
    if(value != null && value.length() > 0)      
      channel.setUserWebPage(value);
  }

  /**
   * Saves the web pages of all channels.
   *
   */
  public static void storeChannelWebPages() {
    File f=new File(Settings.getUserSettingsDirName(),"channel_webpages.txt");

    FileWriter fw;
    PrintWriter out = null;
    
    try {
      fw = new FileWriter(f);
      out = new PrintWriter(fw);
      
      Channel[] channelArr = getAvailableChannels();
      
      for (Channel channel : channelArr) {
        String userWebPage = channel.getUserWebPage();
        
        if ((userWebPage != null) && (userWebPage.trim().length() > 0))
          out.println(channel.getDataServiceProxy().getId()+":"+channel.getId()+"=" + userWebPage.trim());
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
  private static HashMap<String, String> createMap(File f) {
    HashMap<String, String> map = new HashMap<String, String>();

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
          
          if (val!=null) 
            map.put(key,val);

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