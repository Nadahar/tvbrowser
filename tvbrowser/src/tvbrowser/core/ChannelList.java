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




  public static void initSubscribedChannels() {
    Channel[] channelArr = Settings.propSubscribedChannels.getChannelArray();

    for (int i = 0; i < channelArr.length; i++) {
      if (channelArr[i] != null) {
        subscribeChannel(channelArr[i]);
      }
    }
    loadDayLightSavingTimeCorrections();
    loadChannelIcons();
    loadChannelNames();
    loadChannelWebPages();
  }


  public static void loadDayLightSavingTimeCorrections() {
    Map map = createMap(new File(Settings.getUserSettingsDirName(),"daylight_correction.txt"));

    Iterator keyIt = map.keySet().iterator();

    while (keyIt.hasNext()) {
      Channel ch = (Channel)keyIt.next();
      int corr=Integer.parseInt((String)map.get(ch));
      ch.setDayLightSavingTimeCorrection(corr);
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
            out.println(channels[i].getDataService().getClass().getName()+":"+channels[i].getId()+"="+corr);
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
    for (int i = 0; i < channelArr.length; i++) {
      mAvailableChannels.add(channelArr[i]);
    }
  }

  public static void create() {
    mAvailableChannels.clear();
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

    loadChannelIcons();
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
   */
  public static void setSubscribeChannels(Channel[] channelArr) {
    mSubscribedChannels = new ArrayList<Channel>(channelArr.length);
    for (int i = 0; i < channelArr.length; i++) {
      if (channelArr[i] == null) {
        mLog.warning("cannot subscribe channel #" + i + " - is null");
      }
      else {
        mSubscribedChannels.add(channelArr[i]);
      }
    }
  }


  /**
   * Returns a new Channel object with the specified ID or null, if the
   * given ID does not exist.
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


  public static Channel getChannel(String id) {
    Iterator iter = mAvailableChannels.iterator();
    while (iter.hasNext()) {
      Channel channel = (Channel) iter.next();
      if (channel.getId().equals(id)) {
        return channel;
      }
    }
    return null;
  }


  public static int getPos(Channel channel) {
    for (int i = 0; i < mSubscribedChannels.size(); i++) {
      Channel ch = (Channel) mSubscribedChannels.get(i);
      if (ch.equals(channel)) {
        return i;
      }
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


  public static Channel[] getAvailableChannels() {
    Channel[] result = new Channel[mAvailableChannels.size()];
    mAvailableChannels.toArray(result);
    return result;
  }


  /**
   * Returns true, if the specified channel is currently subscribed.
   */
  public static boolean isSubscribedChannel(Channel channel) {
    if (channel==null) return false;
    for (int i=0;i<mSubscribedChannels.size();i++) {
      Channel ch=(Channel)mSubscribedChannels.get(i);
      if (ch!=null && ch.getId().equals(channel.getId()) && ch.getDataService().equals(channel.getDataService())) {
        return true;
      }
    }
    return false;
  }



  /**
   * Returns the number of subscribed mAvailableChannels.
   */
  public static int getNumberOfSubscribedChannels() {
  return mSubscribedChannels.size();
  }



  /**
   * Returns all subscribed mAvailableChannels.
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
  private static void loadChannelIcons() {
    Map map = createMap(new File(Settings.getUserSettingsDirName(),"channel_icons.txt"));

    Iterator keyIt = map.keySet().iterator();

    while (keyIt.hasNext()) {
      Channel ch = (Channel)keyIt.next();
      String val = (String)map.get(ch);

      String[] settings = val.split(";");

      if (settings.length == 2) {
        ch.setUserIconFileName(settings[1]);
        if (settings[0].equals("true")) {
          ch.useUserIcon(true);
        } else {
          ch.useUserIcon(false);
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
      PrintWriter out=null;
      try {
        fw=new FileWriter(f);
        out=new PrintWriter(fw);
        Channel[] channels=getSubscribedChannels();
          for (int i=0;i<channels.length;i++) {
            String filename = channels[i].getUserIconFileName();
            if ((filename != null) && (filename.trim().length() > 0)){
              out.println(channels[i].getDataService().getClass().getName()+":"+channels[i].getId()+"=" + channels[i].isUsingUserIcon() +";"+filename.trim());
            }
          }
      }catch(IOException e) {
        // ignore
      }
      if (out!=null) {
        out.close();
      }
  }

  public static void loadChannelNames() {
    Map map = createMap(new File(Settings.getUserSettingsDirName(),"channel_names.txt"));

    Iterator keyIt = map.keySet().iterator();

    while (keyIt.hasNext()) {
      Channel ch = (Channel)keyIt.next();
      ch.setUserChannelName((String)map.get(ch));
    }
  }

  public static void storeChannelNames() {
    File f=new File(Settings.getUserSettingsDirName(),"channel_names.txt");

    FileWriter fw;
    PrintWriter out=null;
    try {
      fw=new FileWriter(f);
      out=new PrintWriter(fw);
      Channel[] channels=getSubscribedChannels();
        for (int i=0;i<channels.length;i++) {
          String userChannelName = channels[i].getUserChannelName();
          if ((userChannelName != null) && (userChannelName.trim().length() > 0)){
            out.println(channels[i].getDataService().getClass().getName()+":"+channels[i].getId()+"=" + userChannelName.trim());
          }
        }
    }catch(IOException e) {
      // ignore
    }
    if (out!=null) {
      out.close();
    }
  }

  public static void loadChannelWebPages() {
    Map map = createMap(new File(Settings.getUserSettingsDirName(),"channel_webpages.txt"));

    Iterator keyIt = map.keySet().iterator();

    while (keyIt.hasNext()) {
      Channel ch = (Channel)keyIt.next();
      ch.setUserWebPage((String)map.get(ch));
    }
  }

  public static void storeChannelWebPages() {
    File f=new File(Settings.getUserSettingsDirName(),"channel_webpages.txt");

    FileWriter fw;
    PrintWriter out=null;
    try {
      fw=new FileWriter(f);
      out=new PrintWriter(fw);
      Channel[] channels=getSubscribedChannels();
        for (int i=0;i<channels.length;i++) {
          String userWebPage = channels[i].getUserWebPage();
          if ((userWebPage != null) && (userWebPage.trim().length() > 0)){
            out.println(channels[i].getDataService().getClass().getName()+":"+channels[i].getId()+"=" + userWebPage.trim());
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
   * Create a HashMap from a Settings-File
   * @param f File to Load
   * @return HashMap filled with Channel, Value
   */
  private static HashMap createMap(File f) {
    HashMap<Channel, String> map = new HashMap<Channel, String>();

    if (!f.exists()) {
      return map;
    }

    FileReader fr;
    BufferedReader reader=null;
    try {
      fr=new FileReader(f);
      reader=new BufferedReader(fr);
      String line;
      for (;;){
        line=reader.readLine();
        if (line==null) {
          break;
        }
        int pos=line.indexOf('=');
        try {
          String key=line.substring(0,pos);
          String val=line.substring(pos+1);
          if (val!=null) {
            pos = key.indexOf(':');
            String dataServiceClassName = key.substring(0,pos);
            String id = key.substring(pos + 1);

            TvDataServiceProxy dataService
              = TvDataServiceProxyManager.getInstance().findDataServiceById(dataServiceClassName);

            Channel ch=ChannelList.getChannel(dataService,id);
            if (ch!=null) {
              map.put(ch, val);
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



}