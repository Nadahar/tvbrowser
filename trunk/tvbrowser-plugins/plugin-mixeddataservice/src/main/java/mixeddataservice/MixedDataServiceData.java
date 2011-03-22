package mixeddataservice;

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

import devplugin.Channel;

/**
 * Handles channel and program data
 * @author jb
 */
public class MixedDataServiceData {
//private static final Logger mLog = java.util.logging.Logger.getLogger(MixedDataServiceData.class.getName());

  private HashMap<String, Channel> channels = new HashMap<String, Channel>(); // ID - Channel
  private MixedDataService mService;

  /**
   * Initialize an instance of NextViewDataServiceData
   * @param service ; the current instance of this data service
   */
  public MixedDataServiceData(MixedDataService service) {
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
   */
  public void setChannelCRN(String ID, String notice) {
    Channel c = (Channel) channels.get(ID);
    c.setChannelCopyrightNotice(notice);
  }



  /**
   * Add channel with given ID and name
   * @param channelId
   */
  public void addChannel(String channelId, String [] channelDescriptor) {

    Channel [] subscribedChannels = MixedDataService.getPluginManager().getSubscribedChannels();
    Channel alienChannel1 = HelperMethods.getChannelFromId(channelDescriptor[1], subscribedChannels);
    Channel alienChannel2 = HelperMethods.getChannelFromId(channelDescriptor[2], subscribedChannels);

    int categoryIndex;
    if ("primary".equals(channelDescriptor[4]) && alienChannel1!=null){
      categoryIndex = alienChannel1.getCategories();
    } else{
      if ("additional".equals(channelDescriptor[4]) && alienChannel2!=null){
        categoryIndex = alienChannel2.getCategories();
      } else{
        categoryIndex = 0;
      }
    }



    String webPage;
    if ("primary".equals(channelDescriptor[5]) && alienChannel1!=null){
      webPage = alienChannel1.getWebpage();
    } else{
      if ("additional".equals(channelDescriptor[5]) && alienChannel2!=null){
        webPage = alienChannel2.getWebpage();
      } else{
        webPage = null;
      }
    }

    if (webPage==null || webPage.equals("")) {
      webPage = "http://www.google.de/search?q=%22" + channelDescriptor[0].trim().replace(' ','+') + "%22";
    }

    StringBuffer cBuffer = new StringBuffer();
    if (alienChannel1!=null && !"".equals(alienChannel1.getCopyrightNotice())){
      cBuffer.append(alienChannel1.getCopyrightNotice());
    }

    if (alienChannel2!=null && !"".equals(alienChannel2.getCopyrightNotice())){
      if (cBuffer.length()>0){
        cBuffer.append("; ");
      }
      cBuffer.append(alienChannel2.getCopyrightNotice());
    }


    String copyright;
    if (cBuffer.length()>0){
      copyright = cBuffer.toString();
    }
    else{
      copyright = "© " + channelDescriptor[0].trim() + " / mixed";
    }
    Channel prevChannel = channels.get(channelId);
    if (prevChannel == null || !(channelDescriptor[0].trim().equals(prevChannel.getDefaultName())&& copyright.equals(prevChannel.getCopyrightNotice())&& webPage.equals(prevChannel.getDefaultWebPage())&& categoryIndex==prevChannel.getCategories())){
      channels.put(channelId, new Channel(mService, channelDescriptor[0].trim(), channelId, TimeZone.getDefault(), channelDescriptor[6], copyright, webPage, mService.mMixedDataChannelGroup, getChannelIcon (channelId), categoryIndex));
    } else{
      prevChannel.setDefaultIcon(getChannelIcon (channelId));
    }

  }

  private Icon getChannelIcon (String channelId){
    Icon icon = new ImageIcon (getClass().getResource("icons/mixed.png"));
    URL iconUrl =null;

    File file = new File(mService.mDataDir + "/icons/" + channelId + ".png");
    if (file.canRead()) {
      try {
        iconUrl = file.toURI().toURL();
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

}
