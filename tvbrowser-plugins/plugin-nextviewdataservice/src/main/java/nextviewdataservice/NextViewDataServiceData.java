/*
 * NextViewDataService Plugin by Andreas Hessel (Vidrec@gmx.de)
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
 */
package nextviewdataservice;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.TimeZone;

import javax.swing.ImageIcon;
import javax.swing.Icon;

import util.tvdataservice.ProgramDispatcher;

import devplugin.Channel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

/**
 * Handles channel and program data 
 * @author jb
 */
public class NextViewDataServiceData {

//private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(NextViewDataServiceData.class.getName());

  private HashMap<String, Channel> channels = new HashMap<String, Channel>(); // ID - Channel
  private ProgramDispatcher dispatcher = new ProgramDispatcher(); // takes the programs
  private NextViewDataService mService;

  /**
   * Initialize an instance of NextViewDataServiceData
   * @param service ; the current instance of this data service
   */
  public NextViewDataServiceData(NextViewDataService service) {
    this.mService = service;
  }

  /**
   * Get the name of a given channel ID
   * @param ID
   * @return the channel name as String
   */
  public String getChannelName(String ID) {
    Channel c = (Channel) channels.get(ID);
    if (c == null) {
      return null;
    } else {
      return c.getName().trim();
    }
  }


  /**
   * Returns a channel with given ID
   * @param ID
   * @return the channel itself
   */
  public Channel getChannel(String ID) {
    return (Channel) channels.get(ID);
  }

  /**
   * Add channel with given ID and name
   * @param channelId
   * @param channelName
   */
  public void addChannel(String channelId, String channelName) {

    if (mService.useAlternativeData()){
      setAlternativeCopyright(channelId);
    }

    if (!channels.containsKey(channelId)) {

      Properties channelDescriptions = new Properties();
      try {
        channelDescriptions.load(new FileInputStream(mService.mDataDir.toString() + "/cni.desc.properties"));
      } catch (IOException e) {

      }
      String channelDesc = channelDescriptions.getProperty(channelId);

      String fixedName = channelName.trim();
      String country = "de";
      String category = "1";
      String webPage = "";

      if (channelDesc != null) {
        fixedName = channelDesc.split(",", 4)[0].trim();
        country = channelDesc.split(",", 4)[1];
        category = channelDesc.split(",", 4)[2];
        webPage = channelDesc.split(",", 4)[3];
      }

      if (webPage==null || webPage.equals("")) {
        webPage = "http://www.google.de/search?q=%22" + channelName.trim().replace(' ','+') + "%22";
      }

      int categoryIndex;
      categoryIndex = Integer.parseInt(category);

      channels.put(channelId, new Channel(mService, fixedName, channelId, TimeZone.getDefault(), country, getCopyright (channelId, fixedName), webPage, mService.mNextViewChannelGroup, getChannelIcon (channelId), categoryIndex));
    }
  }

  private Icon getChannelIcon (String channelId){
    Icon icon = new ImageIcon (getClass().getResource("icons/nxtvepg.png"));
    URL iconUrl = getClass().getResource("icons/" + channelId + ".png");
    if (mService.useAlternativeIcons()){
      Properties alternativeChannelsDesc = new Properties();
      try {
        alternativeChannelsDesc.load(new FileInputStream(NextViewDataService.getInstance().mixedChannelsDirName + "/nxtvepgAlternatives.properties"));
        if (alternativeChannelsDesc != null) {
          String check = alternativeChannelsDesc.getProperty(channelId);
          if (check != null && check.split(";")[1].equals("1")) {
            File file = new File(mService.mDataDir + "/alternative_icons/" + channelId + ".png");
            if (file.canRead()) {
              try {
                iconUrl = file.toURL();
              } catch (MalformedURLException e) {
              }
            }

          }
        }
      } catch (IOException e) {
      }


    }
    if (iconUrl != null){
      icon = new ImageIcon(iconUrl);
    }
    return icon;
  }


  public String getCopyright (String channelId, String channelName){
    String copyright = "";
    if (mService.useAlternativeData()){
      Properties alternativeChannelsDesc = new Properties();
      try {
        alternativeChannelsDesc.load(new FileInputStream(mService.mixedChannelsDirName + "/nxtvepgAlternatives.properties"));

        if (alternativeChannelsDesc!=null) {
          String alternativeDesc = alternativeChannelsDesc.getProperty(channelId);

          if (alternativeDesc!=null && "1".equals(alternativeDesc.split(";",4)[0])) {
            String alterId = alternativeDesc.split(";",4)[2];
            Properties alternativeCopyrights = new Properties();
            alternativeCopyrights.load(new FileInputStream(mService.mDataDir.toString() + "/alternative_copyrights.properties"));
            if (alternativeCopyrights != null) {
              String alternativeCopyright = alternativeCopyrights.getProperty(alterId);
              if (alternativeCopyright != null) {
                copyright = "; " + alternativeCopyright;
              }
            }
          }
        }

      } catch (IOException e) {
      }


    }
    return "© " + channelName.trim() + " / nxtvepg" + copyright;
  }

  private void setAlternativeCopyright (String channelId){

    Properties alternativeChannelsDesc = new Properties();
    try {
      alternativeChannelsDesc.load(new FileInputStream(mService.mixedChannelsDirName + "/nxtvepgAlternatives.properties"));
    } catch (IOException e) {
    }

    if (alternativeChannelsDesc!=null){
      String alterDesc = alternativeChannelsDesc.getProperty(channelId);
      if (alterDesc!=null){
        String alterId = alterDesc.split(";",4)[2];
        Channel [] subScribedChannels = NextViewDataService.getPluginManager().getSubscribedChannels();
        if (subScribedChannels!=null){
          int index = 0;
          while (index < subScribedChannels.length){
            if (subScribedChannels[index].getUniqueId().equals(alterId)){
              Properties alternativeCopyrights = new Properties();
              try {
                alternativeCopyrights.load(new FileInputStream(NextViewDataService.getInstance().mDataDir.toString() + "/alternative_copyrights.properties"));
              } catch (IOException e) {
              }
              alternativeCopyrights.setProperty(alterId, subScribedChannels[index].getCopyrightNotice());
              try{
                alternativeCopyrights.store(new FileOutputStream(NextViewDataService.getInstance().mDataDir.toString() + "/alternative_copyrights.properties"), "Copyright Notices of Nxtvepg's Alternative Sources");
              } catch (IOException e) {
              }
              index = subScribedChannels.length;
            } else {
              index++;
            }
          }
        }
      }
    }
  }

  /**
   * @return array of the actual channels
   */
  public Channel[] getChannels() {
    Collection<Channel> c = channels.values();
    Iterator<Channel> i = c.iterator();
    Channel[] newChannels = new Channel[c.size()];

    int index = 0;
    while (i.hasNext()) {
      newChannels[index] = (Channel) i.next();
      index++;
    }
    return newChannels;
  }

  /**
   * @return TV-Browsers program dispatcher for this data service
   */
  public ProgramDispatcher getDispatcher() {
    return dispatcher;
  }
}
