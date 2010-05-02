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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.ui.mainframe.MainFrame;
import util.io.stream.ObjectInputStreamProcessor;
import util.io.stream.ObjectOutputStreamProcessor;
import util.io.stream.StreamUtilities;
import devplugin.Channel;

/**
 * ChannelList contains a list of all available mAvailableChannels in the
 * system. Use this class to subscribe mAvailableChannels. The available
 * mAvailableChannels are listed in the file CHANNEL_FILE.
 *
 * @author Martin Oberhauser
 */
public class ChannelList {

  private static final String[] DEFAULT_CHANNELS_DE = new String[] {
      "tvbrowserdataservice.TvBrowserDataService_main_de_ard", "tvbrowserdataservice.TvBrowserDataService_main_de_zdf",
      "tvbrowserdataservice.TvBrowserDataService_main_de_rtl",
      "tvbrowserdataservice.TvBrowserDataService_main_de_sat1",
      "tvbrowserdataservice.TvBrowserDataService_main_de_pro7",
      "tvbrowserdataservice.TvBrowserDataService_main_de_kabel1",
      "tvbrowserdataservice.TvBrowserDataService_main_de_rtl2",
      "tvbrowserdataservice.TvBrowserDataService_main_de_vox",
      "tvbrowserdataservice.TvBrowserDataService_main_de_superrtl",
      "tvbrowserdataservice.TvBrowserDataService_others_at_3sat" };

  private static final String[] DEFAULT_CHANNELS_AT = new String[] {
    "tvbrowserdataservice.TvBrowserDataService_austria_at_orf1",
    "tvbrowserdataservice.TvBrowserDataService_austria_at_orf2",
    "tvbrowserdataservice.TvBrowserDataService_main_de_ard",
    "tvbrowserdataservice.TvBrowserDataService_main_de_zdf",
    "tvbrowserdataservice.TvBrowserDataService_main_de_rtl",
    "tvbrowserdataservice.TvBrowserDataService_main_de_sat1",
    "tvbrowserdataservice.TvBrowserDataService_main_de_pro7",
    "tvbrowserdataservice.TvBrowserDataService_main_de_kabel1",
    "tvbrowserdataservice.TvBrowserDataService_main_de_rtl2",
    "tvbrowserdataservice.TvBrowserDataService_main_de_vox",
    "tvbrowserdataservice.TvBrowserDataService_main_de_superrtl",
    "tvbrowserdataservice.TvBrowserDataService_others_at_3sat" };

  private static final String[] DEFAULT_CHANNELS_CH = new String[] {
    "tvbrowserdataservice.TvBrowserDataService_others_ch_sfdrs1",
    "tvbrowserdataservice.TvBrowserDataService_others_ch_sfdrs2",
    "tvbrowserdataservice.TvBrowserDataService_others_ch_sfinfo",
    "tvbrowserdataservice.TvBrowserDataService_main_de_ard",
    "tvbrowserdataservice.TvBrowserDataService_main_de_zdf",
    "tvbrowserdataservice.TvBrowserDataService_main_de_rtl",
    "tvbrowserdataservice.TvBrowserDataService_main_de_sat1",
    "tvbrowserdataservice.TvBrowserDataService_main_de_pro7",
    "tvbrowserdataservice.TvBrowserDataService_main_de_kabel1",
    "tvbrowserdataservice.TvBrowserDataService_main_de_rtl2",
    "tvbrowserdataservice.TvBrowserDataService_main_de_vox",
    "tvbrowserdataservice.TvBrowserDataService_main_de_superrtl",
    "tvbrowserdataservice.TvBrowserDataService_others_at_3sat" };

  private static final String FILENAME_DAYLIGHT_CORRECTION = "daylight_correction.txt";

  private static final String FILENAME_CHANNEL_NAMES = "channel_names.txt";

  private static final String FILENAME_CHANNEL_ICONS = "channel_icons.txt";

  private static final String FILENAME_CHANNEL_WEBPAGES = "channel_webpages.txt";

  private static final String FILENAME_CHANNEL_TIME_LIMIT = "channelTimeLimit.dat";

  private static final Logger mLog = Logger
      .getLogger(ChannelList.class.getName());

  private static ArrayList<Channel> mAvailableChannels = new ArrayList<Channel>();

  private static HashMap<String, Channel> mAvailableChannelsMap = new HashMap<String, Channel>();

  /**
   * list of subscribed channels
   */
  private static ArrayList<Channel> mSubscribedChannels = new ArrayList<Channel>();

  /**
   * map of channel position per subscribed channels, needed for fast channel
   * comparison
   */
  private static HashMap<String, Integer> mSubscribedChannelPosition = new HashMap<String, Integer>();

  private static Thread mCompleteChannelThread;

  private static HashMap<String, String> mChannelIconMap, mChannelNameMap,
      mChannelWebpagesMap, mChannelDayLightCorrectionMap;

  private static Channel mCurrentChangeChannel = null;

  /**
   * Load the not subscribed channels after TV-Browser start was finished.
   */
  public static void completeChannelLoading() {
    getChannelLoadThread();
  }

  /**
   * Reload the channel list.
   */
  public static void reload() {
    loadChannelMaps();
    create();
  }

  private static void create() {
    TvDataServiceProxy[] dataServiceArr = TvDataServiceProxyManager
        .getInstance().getDataServices();

    HashMap<Channel, Channel> availableChannels = new HashMap<Channel, Channel>(mAvailableChannelsMap.size());

    for (TvDataServiceProxy proxy : dataServiceArr) {
      addDataServiceChannels(proxy,availableChannels);
    }

    boolean removed = false;

    for(int i = mAvailableChannels.size()-1; i >= 0; i--) {
      Channel ch = (Channel)mAvailableChannels.get(i);

      if(!availableChannels.containsKey(ch)) {
        mAvailableChannels.remove(i);
        mAvailableChannelsMap.remove(ch.getUniqueId());

        /* remove subscribed channels which are not available any more */
        if(mSubscribedChannels.contains(ch)) {
          mLog.warning(ch+" is not available any more");
          unsubscribeChannel(ch);

          removed = true;
        }
      }
    }

    if(removed) {
      Settings.propSubscribedChannels.setChannelArray(mSubscribedChannels.toArray(new Channel[mSubscribedChannels.size()]));
      calculateChannelPositions();
    }

    MainFrame.resetOnAirArrays();
  }

  private static void unsubscribeChannel(Channel channel) {
    mSubscribedChannels.remove(channel);
    mSubscribedChannelPosition.remove(channel.getUniqueId());
    handleChannelUnsubscribed(channel);
  }

  /**
   * Init the subscribed channels
   */
  public static void initSubscribedChannels() {
    Channel[] channelArr = Settings.propSubscribedChannels.getChannelArray();
    if (channelArr.length == 0) {
      channelArr = getDefaultChannels(Settings.propSelectedChannelCountry.getString());
    }

    for (Channel channel : channelArr) {
      if (channel != null) {
        subscribeChannel(channel);
      }
    }

    loadChannelTimeLimits();
  }

  /**
   * Stores all settings used for the Channels
   */
  public static void storeAllSettings() {try {
    storeDayLightSavingTimeCorrections();
    storeChannelIcons();
    storeChannelNames();
    storeChannelWebPages();
    storeChannelTimeLimits();}catch(Throwable t) {t.printStackTrace();}
  }

  private static void addDataServiceChannels(TvDataServiceProxy dataService,
      HashMap<Channel, Channel> availableChannels) {
    Channel[] channelArr = dataService.getAvailableChannels();

    for (Channel channel : channelArr) {
      addChannelToAvailableChannels(channel,availableChannels);
    }
  }

  private static void addDataServiceChannelsForTvBrowserStart(
      TvDataServiceProxy dataService) {
    Channel[] channelArr = dataService.getChannelsForTvBrowserStart();

    for (Channel channel : channelArr) {
      addChannelToAvailableChannels(channel, new HashMap<Channel, Channel>());
    }
  }

  private static void addChannelToAvailableChannels(Channel channel, HashMap<Channel, Channel> availableChannels) {
    final String channelId = channel.getUniqueId();
    mCurrentChangeChannel = mAvailableChannelsMap.get(channelId);

    if (mCurrentChangeChannel == null) {
      availableChannels.put(channel, channel);
      mAvailableChannels.add(channel);
      mAvailableChannelsMap.put(channelId, channel);

      if (!mChannelDayLightCorrectionMap.isEmpty()) {
        setDayLightSavingTimeCorrectionsForChannel(channel);
      }

      if (!mChannelIconMap.isEmpty()) {
        setChannelIconForChannel(channel);
      }

      if (!mChannelNameMap.isEmpty()) {
        setChannelNameForChannel(channel);
      }

      if (!mChannelWebpagesMap.isEmpty()) {
        setWebPageForChannel(channel);
      }
    }
    else {
      mCurrentChangeChannel.setChannelName(channel.getDefaultName());
      mCurrentChangeChannel.setDefaultIcon(channel.getDefaultIcon());
      mCurrentChangeChannel.setChannelCopyrightNotice(channel.getCopyrightNotice());
      mCurrentChangeChannel.setChannelWebpage(channel.getDefaultWebPage());

      availableChannels.put(mCurrentChangeChannel, mCurrentChangeChannel);
    }

    mCurrentChangeChannel = null;
  }

  private static void loadChannelMaps() {
    mChannelIconMap = createMap(new File(Settings.getUserSettingsDirName(),
        FILENAME_CHANNEL_ICONS));
    mChannelNameMap = createMap(new File(Settings.getUserSettingsDirName(),
        FILENAME_CHANNEL_NAMES));
    mChannelWebpagesMap = createMap(new File(Settings.getUserSettingsDirName(),
        FILENAME_CHANNEL_WEBPAGES));
    mChannelDayLightCorrectionMap = createMap(new File(Settings
        .getUserSettingsDirName(), FILENAME_DAYLIGHT_CORRECTION));
  }

  /**
   * Creates the needed channels for TV-Browser start
   */
  public static void createForTvBrowserStart() {
    mAvailableChannels.clear();
    mAvailableChannelsMap.clear();
    TvDataServiceProxy[] dataServiceArr = TvDataServiceProxyManager
        .getInstance().getDataServices();

    loadChannelMaps();

    for (TvDataServiceProxy proxy : dataServiceArr) {
      addDataServiceChannelsForTvBrowserStart(proxy);
    }
  }

  /**
   * Subscribes a channel
   *
   * @param channel
   */
  public static void subscribeChannel(Channel channel) {
    mSubscribedChannels.add(channel);
    calculateChannelPositions();
  }

  private static void calculateChannelPositions() {
    mSubscribedChannelPosition = new HashMap<String, Integer>();
    for (int i = 0; i < mSubscribedChannels.size(); i++) {
      Channel ch = mSubscribedChannels.get(i);

      if (ch != null) {
        mSubscribedChannelPosition.put(ch.getUniqueId(), i);
      }
    }
  }

  /**
   * Marks the specified mAvailableChannels as 'subscribed'. All other
   * mAvailableChannels become 'unsubscribed'
   *
   * @param channelArr
   *          The channels to set as subscribed channels,
   */
  public static void setSubscribeChannels(Channel[] channelArr) {
    setSubscribeChannels(channelArr, false);
  }

  /**
   * Sets the subscribed channels.
   *
   * @param channelArr The array with the subscribed channels.
   * @param update ?
   */
  public static void setSubscribeChannels(Channel[] channelArr, boolean update) {
    boolean channelsAdded = false;
    if (update) {
      for (Channel channel : channelArr) {
        if (!mSubscribedChannels.contains(channel)) {
          channelsAdded = true;
        }
      }
    }

    // remember channels which are no longer subscribed
    ArrayList<Channel> unsubscribedChannels = new ArrayList<Channel>();
    for (Channel channel : mSubscribedChannels) {
      boolean found = false;
      for (Channel newChannel : channelArr) {
        if (channel == newChannel) {
          found = true;
        }
      }
      if (!found) {
        unsubscribedChannels.add(channel);
      }
    }

    mSubscribedChannels = new ArrayList<Channel>(channelArr.length);
    for (int i = 0; i < channelArr.length; i++) {
      final Channel channel = channelArr[i];
      if (channel == null) {
        mLog.warning("cannot subscribe channel #" + i + " - is null");
      } else {
        mSubscribedChannels.add(channel);
      }
    }

    calculateChannelPositions();

    // now remove all unsubscribed TV data
    for (Channel channel : unsubscribedChannels) {
      handleChannelUnsubscribed(channel);
    }

    if (channelsAdded && update) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          MainFrame.getInstance().askForDataUpdateChannelsAdded();
        }
      });
    }
  }

  private static void handleChannelUnsubscribed(Channel channel) {
    if (channel == null) {
      return;
    }
    TvDataBase.getInstance().unsubscribeChannel(channel);
  }

  /**
   * Returns a new Channel object with the specified IDs or null, if the given
   * IDs does not exist.
   *
   * @param dataServiceId
   *          The id of the data service to get the channel from.
   * @param groupId
   *          The id of the channel group.
   * @param country
   *          The country of the channel.
   * @param channelId
   *          The id of the channel.
   *
   * @return The specified channel or <code>null</code> if the channel wasn't
   *         found.
   *
   * @since 2.2.1
   */
  public static Channel getChannel(String dataServiceId, String groupId,
      String country, String channelId) {
    TvDataServiceProxy dataService = null;
    if (dataServiceId != null) {
      dataService = TvDataServiceProxyManager.getInstance()
          .findDataServiceById(dataServiceId);

      if (dataService == null) {
        return null;
      }
    }

    int n = mAvailableChannels.size();

    for(int i = 0; i < n; i++) {
      Channel channel = mAvailableChannels.get(i);

      if (channel.getId().compareTo(channelId) == 0
          && ((dataServiceId != null && channel.getDataServiceProxy().getId()
              .compareTo(dataService.getId()) == 0) || dataServiceId == null)
          && ((groupId != null && channel.getGroup().getId().compareTo(groupId) == 0) || groupId == null)
          && ((country != null && channel.getCountry().compareTo(country) == 0) || country == null)) {
        return channel;
      }
    }
    /*
     * merge-conflict: do we need these lines?
     *  // If we haven't found the channel within the 'available channels', we
     * try to find it // in an unsubscribed group. // If we find it there, we
     * subscribe the affected group and add all channels of this group to // the
     * 'available channels' list
     *
     * if(dataService != null) { ChannelGroup[] groupArr =
     * dataService.getAvailableGroups(); for (int i=0; i<groupArr.length; i++) {
     * if (!ChannelGroupManager.getInstance().isSubscribedGroup(groupArr[i]) &&
     * ((groupId != null && groupArr[i].getId().equals(groupId)) || groupId ==
     * null)) { Channel[] channelArr =
     * dataService.getAvailableChannels(groupArr[i]); for (int j=0; j<channelArr.length;
     * j++) { if (((country != null &&
     * channelArr[j].getCountry().compareTo(country) == 0) || country == null) &&
     * channelId.equals(channelArr[j].getId())) {
     * ChannelGroupManager.getInstance().subscribeGroup(groupArr[i]); for (int
     * k=0; k<channelArr.length; k++) { mAvailableChannels.add(channelArr[k]); }
     * return channelArr[j]; } } } } }
     */

    return null;
  }

  /**
   * Gets the position of the channel in the subscribed channel array, or -1 if
   * the channel isn't a subscribed channel.
   *
   * @param channel
   *          The channel to get the position for.
   * @return The position or -1
   */
  public static int getPos(Channel channel) {
    if(channel != null) {
      Integer pos = mSubscribedChannelPosition.get(channel.getUniqueId());
      if (pos == null) {
        return -1;
      }
      else {
        return pos.intValue();
      }
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
   * @param channel
   *          The channel to check if it is subscribed,
   * @return True if the channel is subscribed, false otherwise.
   */
  public static boolean isSubscribedChannel(Channel channel) {
    if (channel == null) {
      return false;
    }
    for (Channel subscribed : mSubscribedChannels) {
      if (subscribed != null && subscribed.equals(channel)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the number of subscribed mAvailableChannels.
   *
   * @return The number of the subscribed channels.
   */
  public static int getNumberOfSubscribedChannels() {
    return mSubscribedChannels.size();
  }

  /**
   * Returns all subscribed mAvailableChannels.
   *
   * @return All subscribed channels in an array.
   */
  public static Channel[] getSubscribedChannels() {
    Channel[] result = new Channel[mSubscribedChannels.size()];
    for (int i = 0; i < mSubscribedChannels.size(); i++) {
      result[i] = mSubscribedChannels.get(i);
    }
    return result;
  }

  /**
   * Set the day light time correction for a channel.
   *
   * @param channel
   *          The channel to set the value.
   */
  private static void setDayLightSavingTimeCorrectionsForChannel(Channel channel) {
    String value = getMapValueForChannel(channel, mChannelDayLightCorrectionMap);

    if (value != null && value.length() > 0) {
      Double corr = Double.valueOf(value);
      channel.setTimeZoneCorrectionMinutes((int) (corr * 60));
    }
  }

  /**
   * Set the icon for a channel.
   *
   * @param channel
   *          The channel to set the value for.
   */
  private static void setChannelIconForChannel(Channel channel) {
    String value = getMapValueForChannel(channel, mChannelIconMap);

    if (value != null && value.length() > 0) {
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
   * @param channel
   *          The channel to set the name for.
   */
  private static void setChannelNameForChannel(Channel channel) {
    String value = getMapValueForChannel(channel, mChannelNameMap);

    if (value != null && value.length() > 0) {
      channel.setUserChannelName(value);
    }
  }

  /**
   * Sets the web page for a channel.
   *
   * @param channel
   *          The channel to set the web page for.
   */
  private static void setWebPageForChannel(Channel channel) {
    String value = getMapValueForChannel(channel, mChannelWebpagesMap);

    if (value != null && value.length() > 0) {
      channel.setUserWebPage(value);
    }
  }

  private static void storeDayLightSavingTimeCorrections() {
    File f = new File(Settings.getUserSettingsDirName(),
        FILENAME_DAYLIGHT_CORRECTION);

    FileWriter fw;
    PrintWriter out = null;
    try {
      fw = new FileWriter(f);
      out = new PrintWriter(fw);
      Channel[] channels = getSubscribedChannels();
      for (Channel channel : channels) {
        int corr = channel.getTimeZoneCorrectionMinutes();
        if (corr != 0) {
          out.println(createPropertyForChannel(channel, String.valueOf(corr / 60.0)));
        }
      }
    } catch (IOException e) {
      // ignore
    }
    if (out != null) {
      out.close();
    }
  }

  /**
   * Stores all Icons
   */
  private static void storeChannelIcons() {
    File f = new File(Settings.getUserSettingsDirName(), FILENAME_CHANNEL_ICONS);

    FileWriter fw;
    PrintWriter out = null;

    try {
      fw = new FileWriter(f);
      out = new PrintWriter(fw);
      Channel[] channels = getSubscribedChannels();
      for (Channel channel : channels) {
        String filename = channel.getUserIconFileName();
        if ((filename != null) && (filename.trim().length() > 0)) {
          out.println(createPropertyForChannel(channel, channel
              .isUsingUserIcon()
              + ";" + filename.trim()));
        }
      }

      if(mChannelIconMap != null) {
        Set<String> keys = mChannelIconMap.keySet();

        for(String key : keys) {
          if(!isSubscribedChannel(getChannelForKey(key))) {
            out.print(key);
            out.print("=");
            out.println(mChannelIconMap.get(key));
          }
        }
      }
    } catch (IOException e) {
      // ignore
    }
    if (out != null) {
      out.close();
    }
  }

  /**
   * Saves the channel names for all channels.
   */
  private static void storeChannelNames() {
    HashSet<String> subscribedServices = new HashSet<String>();
    File f = new File(Settings.getUserSettingsDirName(), FILENAME_CHANNEL_NAMES);

    FileWriter fw;
    PrintWriter out = null;

    try {
      fw = new FileWriter(f);
      out = new PrintWriter(fw);
      for (Channel channel : getSubscribedChannels()) {
        String userChannelName = channel.getUserChannelName();
        if ((userChannelName != null) && (userChannelName.trim().length() > 0) && (channel.getDefaultName() == null || !channel.getDefaultName().equalsIgnoreCase(userChannelName))) {
          out
              .println(createPropertyForChannel(channel, userChannelName.trim()));
        }
        subscribedServices.add(channel.getDataServiceProxy().getId());
      }
      // remember the currently active services for faster startup
      Settings.propCurrentlyUsedDataServiceIds.setStringArray(subscribedServices.toArray(new String[subscribedServices.size()]));

      if(mChannelNameMap != null) {
        Set<String> keys = mChannelNameMap.keySet();

        for(String key : keys) {
          if(!isSubscribedChannel(getChannelForKey(key))) {
            out.print(key);
            out.print("=");
            out.println(mChannelNameMap.get(key));
          }
        }
      }
    } catch (IOException e) {
      // ignore
    }
    if (out != null) {
      out.close();
    }
  }

  /**
   * Saves the web pages of all channels.
   *
   */
  private static void storeChannelWebPages() {
    File f = new File(Settings.getUserSettingsDirName(), FILENAME_CHANNEL_WEBPAGES);

    FileWriter fw;
    PrintWriter out = null;

    try {
      fw = new FileWriter(f);
      out = new PrintWriter(fw);
      Channel[] channels = getSubscribedChannels();
      for (Channel channel : channels) {
        String userWebPage = channel.getUserWebPage();
        if ((userWebPage != null) && (userWebPage.trim().length() > 0) && (channel.getDefaultWebPage() == null || !channel.getDefaultWebPage().equalsIgnoreCase(userWebPage))) {
          out.println(createPropertyForChannel(channel, userWebPage.trim()));
        }
      }

      if(mChannelWebpagesMap != null) {
        Set<String> keys = mChannelWebpagesMap.keySet();

        for(String key : keys) {
          if(!isSubscribedChannel(getChannelForKey(key))) {
            out.print(key);
            out.print("=");
            out.println(mChannelWebpagesMap.get(key));
          }
        }
      }
    } catch (IOException e) {
      // ignore
    }
    if (out != null) {
      out.close();
    }
  }

  private static String createPropertyForChannel(Channel channel, String value) {
    return new StringBuilder(channel.getDataServiceProxy().getId()).append(":")
        .append(channel.getGroup().getId()).append(":").append(
            channel.getCountry()).append(":").append(channel.getId()).append(
            "=").append(value).toString();
  }

  private static String getMapValueForChannel(Channel channel,
      HashMap<String, String> map) {
    String value = map.get(new StringBuilder(channel.getDataServiceProxy()
        .getId()).append(":").append(channel.getGroup().getId()).append(":")
        .append(channel.getCountry()).append(":").append(channel.getId())
        .toString());

    if (value == null) {
      value = map.get(new StringBuilder(channel.getDataServiceProxy().getId())
          .append(":").append(channel.getGroup().getId()).append(":").append(
              channel.getId()).toString());
    }
    if (value == null) {
      value = map.get(new StringBuilder(channel.getDataServiceProxy().getId())
          .append(":").append(channel.getId()).toString());
    }

    return value;
  }

  /**
   * Create a HashMap from a Settings-File
   *
   * @param f
   *          File to Load
   * @return HashMap filled with Channel-Key, Value
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

      while ((line = reader.readLine()) != null) {
        int pos = line.indexOf('=');

        try {
          String key = line.substring(0, pos);
          String val = line.substring(pos + 1);

          if (val != null) {
            map.put(key, val);
          }

        } catch (IndexOutOfBoundsException e) {
          // ignore
        }
      }

    } catch (IOException e) {
      // ignore
    }
    if (reader != null) {
      try {
        reader.close();
      } catch (IOException exc) {
        // ignore
      }
    }
    return map;
  }

  /**
   * @return The Thread of loading the not used channels.
   */
  synchronized public static Thread getChannelLoadThread() {
    if (mCompleteChannelThread == null) {
      mCompleteChannelThread = new Thread("Load not subscribed channels") {
        public void run() {
          mLog.info("Loading the not subscribed services and channels");
          TvDataServiceProxyManager.getInstance().loadNotSubscribed();
          create();
          mLog.info("Loading of all channels complete");
        }
      };
      mCompleteChannelThread.setPriority(Thread.MIN_PRIORITY);
      mCompleteChannelThread.start();
    }
    return mCompleteChannelThread;
  }

  /**
   * Writes the channels time limits to data file.
   *
   * @since 2.2.4/2.6
   */
  public static void loadChannelTimeLimits() {
    File f = new File(Settings.getUserSettingsDirName(), FILENAME_CHANNEL_TIME_LIMIT);

    if (f.isFile() && f.canRead()) {
      StreamUtilities.objectInputStreamIgnoringExceptions(f,
          new ObjectInputStreamProcessor() {
            public void process(ObjectInputStream in) throws IOException {
              in.readShort(); // version

              short n = in.readShort(); // write number of channels

              for (int i = 0; i < n; i++) {
                try {
                  Channel ch = Channel.readData(in, true);
                  short startTimeLimit = in.readShort();
                  short endTimeLimit = in.readShort();

                  if (ch != null) {
                    ch.setStartTimeLimit(startTimeLimit);
                    ch.setEndTimeLimit(endTimeLimit);
                  }
                } catch (ClassNotFoundException e) {
                  e.printStackTrace();
                }

              }
              in.close();
            }
          });
    }
  }

  /**
   * Writes the channels time limits to data file.
   *
   * @since 2.2.4/2.6
   */
  public static void storeChannelTimeLimits() {
    File f = new File(Settings.getUserSettingsDirName(), FILENAME_CHANNEL_TIME_LIMIT);
    StreamUtilities.objectOutputStreamIgnoringExceptions(f,
        new ObjectOutputStreamProcessor() {
          public void process(ObjectOutputStream out) throws IOException {
            out.writeShort(1); // version

            Channel[] channels = getSubscribedChannels();

            out.writeShort(channels.length); // write number of channels

            for (int i = 0; i < channels.length; i++) {
              channels[i].writeData(out);
              out.writeShort(channels[i].getStartTimeLimit());
              out.writeShort(channels[i].getEndTimeLimit());
            }

            out.close();
          }
        });
  }

  private static Channel getChannelForKey(String key) {
    Channel ch = null;

    if(key != null) {
      String[] keyParts = key.split(":");

      if(keyParts.length == 4) {
        ch = getChannel(keyParts[0],keyParts[1],keyParts[2],keyParts[3]);
      }
      else if(keyParts.length == 3) {
        ch = getChannel(keyParts[0],keyParts[1],null,keyParts[2]);
      }
      else {
        ch = getChannel(keyParts[0],null,null,keyParts[1]);
      }
    }

    return ch;
  }

  /**
   * Gets if the channel values are allowed to be changed for the given channel.
   *
   * @param ch The channel to check if the value change is allowed.
   * @return <code>True</code> if the channel value are allowed to be changed, <code>false</code> otherwise.
   */
  public static boolean hasCalledChannelValueChangeForChannel(Channel ch) {
    return mCurrentChangeChannel != null && ch != null && mCurrentChangeChannel.equals(ch);
  }

  public static Channel getChannel(final String uniqueId) {
    return mAvailableChannelsMap.get(uniqueId);
  }

  /**
   * get the list of channels subscribed by default
   * @param country
   * @return
   * @since 3.0
   */
  private static Channel[] getDefaultChannels(final String country) {
    ArrayList<Channel> list = new ArrayList<Channel>();
    if (country.equalsIgnoreCase("de")) {
      addChannels(list, DEFAULT_CHANNELS_DE);
    }
    else if (country.equalsIgnoreCase("at")) {
      addChannels(list, DEFAULT_CHANNELS_AT);
    }
    else if (country.equalsIgnoreCase("ch")) {
      addChannels(list, DEFAULT_CHANNELS_CH);
    }
    return list.toArray(new Channel[list.size()]);
  }

  private static void addChannels(ArrayList<Channel> list, String[] channels) {
    for (String id : channels) {
      Channel channel = getChannel(id);
      if (channel != null) {
        list.add(channel);
      }
    }
  }

}