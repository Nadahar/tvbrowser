package sharedchanneldataservice;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.TimeZone;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import util.tvdataservice.ProgramDispatcher;
import devplugin.Channel;

public class SharedChannelDataServiceData {
//private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(SharedChannelDataServiceData.class.getName());

  private HashMap<String, Channel> channels = new HashMap<String, Channel>(); // ID - Channel
  private ProgramDispatcher dispatcher = new ProgramDispatcher(); // takes the programs
  private SharedChannelDataService mService;

  /**
   * Initialize an instance of NextViewDataServiceData
   * @param service ; the current instance of this data service
   */
  public SharedChannelDataServiceData(SharedChannelDataService service) {
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
   * Set Copyright notice of a given channel ID
   * @param ID
   * @param notice
   * @return the channel name as String
   */
  public void setChannelCRN(String ID, String notice) {
    Channel c = (Channel) channels.get(ID);
    c.setChannelCopyrightNotice(notice);
  } 



  /**
   * Add channel with given ID and name
   * @param channelId
   * @param channelName
   */
  public void addChannel(String channelId, String [] channelDescriptor) {

    Channel [] subscribedChannels = SharedChannelDataService.getPluginManager().getSubscribedChannels();

    int categoryIndex = Integer.parseInt((String)channelDescriptor[2]);;
    
    Channel alienChannel;
    String webPage = null;
    int webpageIndex=Integer.parseInt((String)channelDescriptor[3]);
    if (webpageIndex>=0) {
      alienChannel = HelperMethods.getChannelFromId(channelDescriptor[6+(webpageIndex*2)], subscribedChannels);
      if (alienChannel != null) {
        webPage = alienChannel.getWebpage();
      }
    }
    if (webPage==null || webPage.equals("")) {
      webPage = "http://www.google.de/search?q=%22" + channelDescriptor[0].trim().replace(' ','+') + "%22";
    }

    StringBuffer cBuffer = new StringBuffer();
    for (int i = 6; i < channelDescriptor.length; i+=2){
      alienChannel = HelperMethods.getChannelFromId(channelDescriptor[i], subscribedChannels);
      if (alienChannel != null) {
      cBuffer.append("; " + alienChannel.getCopyrightNotice());
      }
    }

   String copyright;
    if (cBuffer.length()>0){
      copyright = cBuffer.substring(2);
    }
    else{
      copyright = "© " + channelDescriptor[0].trim() + " / shared";
    }

    Channel prevChannel = channels.get(channelId);
    if (prevChannel == null || !(channelDescriptor[0].trim().equals(prevChannel.getDefaultName())&& copyright.equals(prevChannel.getCopyrightNotice())&& webPage.equals(prevChannel.getDefaultWebPage())&& categoryIndex==prevChannel.getCategories())){
      channels.put(channelId, new Channel(mService, channelDescriptor[0].trim(), channelId, TimeZone.getDefault(), channelDescriptor[4], copyright, webPage, mService.mSharedChannelDataChannelGroup, getChannelIcon (channelId), categoryIndex));
    } else{
      prevChannel.setDefaultIcon(getChannelIcon (channelId)); 
    }

  }

  private Icon getChannelIcon (String channelId){
    Icon icon = new ImageIcon (getClass().getResource("icons/shared.png"));
    URL iconUrl =null;

    File file = new File(mService.mDataDir + "/icons/" + channelId + ".png");
    if (file.canRead()) {
      try {
        iconUrl = file.toURL();
      } catch (MalformedURLException e) {
      }
    }

    if (iconUrl != null){
      icon = new ImageIcon(iconUrl);
    }
    return icon;
  }

  /**
   * @return array of the actual channels
   */
  public Channel[] getChannels(Properties channelDefinitions) {
    Collection<Channel> c = channels.values();
    Iterator<Channel> i = c.iterator();
    ArrayList <Channel> newChannels = new ArrayList <Channel>();

    while (i.hasNext()) {
      Channel nextChannel = (Channel) i.next();
      if (channelDefinitions.getProperty(nextChannel.getId())!=null){
        newChannels.add(nextChannel);
      }
    }
    return newChannels.toArray(new Channel[newChannels.size()]);
  }

  /**
   * @return TV-Browsers program dispatcher for this data service
   */
  public ProgramDispatcher getDispatcher() {
    return dispatcher;
  }
}
