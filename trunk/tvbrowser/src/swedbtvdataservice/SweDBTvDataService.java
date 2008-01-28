/*
 * SweDBTvDataService.java
 *
 * Created on den 31 oktober 2005, 13:09
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package swedbtvdataservice;

import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.ProgressMonitor;
import devplugin.Version;
import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import util.ui.Localizer;
import util.io.IOUtilities;
import util.io.Mirror;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.swing.Icon;

public class SweDBTvDataService extends devplugin.AbstractTvDataService {
  /** The default plugins download url */
  public static final String DEFAULT_PLUGINS_DOWNLOAD_URL = "http://www.tvbrowser.org/mirrorlists";

  /** Contains the mirror urls useable for receiving the channellist.xml.gz from. */
  private static final String[] DEFAULT_MIRRORS = {
    "http://tvbrowser.dyndns.tv",
    "http://hdtv-online.org/TVB",
    "http://www.tvbrowserserver.de/"
  };

  public static final Localizer mLocalizer = Localizer.getLocalizerFor(SweDBTvDataService.class);

  private static Logger mLog = Logger.getLogger(SweDBTvDataService.class.getName());

  private Properties mProperties;

  private File mWorkingDirectory;

  private HashMap<String, DataHydraChannelGroup> mChannelGroups;

  private HashMap<Channel, DataHydraChannelContainer> mInternalChannels = new HashMap<Channel, DataHydraChannelContainer>();

  private HashMap<ChannelGroup, Long> mLastGroupUpdate = new HashMap<ChannelGroup, Long>();

  private ArrayList<Channel> mChannels;

  private WeakHashMap<String, File> mIconCache = new WeakHashMap<String, File>();

  private boolean mHasRightToDownloadIcons;

  private DataHydraFileParser mParser = new DataHydraFileParser();
  static final String SHOW_REGISTER_TEXT = "showRegisterText";

  /**
   * Creates a new instance of SweDBTvDataService
   */
  public SweDBTvDataService() {
    mHasRightToDownloadIcons = false;
    mProperties = new Properties();

    mChannelGroups = new HashMap<String, DataHydraChannelGroup>();
    mChannelGroups.put("SweDB", new DataHydraChannelGroup("SweDB", "SweDB.se", "(c) swedb.se", "http://tv.swedb.se", "swedb_channels.xml.gz", "se"));
    mChannelGroups.put("MSPC", new DataHydraChannelGroup("MSPC", "mspc.no", "(c) mspc.no", "http://www.mspc.no", "mspc_channels.xml.gz", "no"));
    mChannelGroups.put("gonix", new DataHydraChannelGroup("gonix", "gonix.net", "(c) gonix.net", "http://www.gonix.net", "hrv_channels.xml.gz", "hr", false));
  }

  public boolean supportsDynamicChannelList() {
    return true;
  }

  public boolean supportsDynamicChannelGroups() {
    return true;
  }

  public boolean hasSettingsPanel() {
    return false;
  }

  public SettingsPanel getSettingsPanel() {
    return new DataHydraSettingsPanel(mProperties);
  }

  public void setWorkingDirectory(File dataDir) {
    mLog.info("DataHydraTvDataService setting directory to " + dataDir.toString());
    mWorkingDirectory = dataDir;
  }

  /**
   * @return an array of the available channel groups.
   */
  public ChannelGroup[] getAvailableGroups() {
    Set<String> keys = mChannelGroups.keySet();

    final ArrayList<ChannelGroup> groups = new ArrayList<ChannelGroup>();

    for (String key : keys) {
      groups.add(mChannelGroups.get(key));
    }

    return groups.toArray(new ChannelGroup[groups.size()]);
  }

  /**
   * Updates the TV listings provided by this data service.
   *
   * @throws util.exc.TvBrowserException
   */
  public void updateTvData(TvDataUpdateManager updateManager,
                           Channel[] channelArr, Date startDate, int dateCount,
                           ProgressMonitor monitor) throws TvBrowserException {

    mHasRightToDownloadIcons = true;
    mLog.info("Starting update for DataHydraTvDataService from " + startDate.toString() + " for " + dateCount + " days");

    monitor.setMaximum(channelArr.length);
    devplugin.Date testStart = new devplugin.Date(startDate);

    int counter = 0;
    for (Channel channel : channelArr) {
      mParser.loadDataForChannel(this, updateManager, startDate, dateCount, monitor, testStart, mInternalChannels.get(channel), channel);
      monitor.setValue(counter++);
    }

    mHasRightToDownloadIcons = false;
  }

  /**
   * Called by the host-application during start-up. Implement this method to
   * load your dataservices settings from the file system.
   */
  public void loadSettings(Properties settings) {
    mLog.info("Loading settings in DataHydraTvDataService");
    mProperties = settings;

    for (ChannelGroup group : getAvailableGroups()) {
      long lastupdate = Long.parseLong(mProperties.getProperty("LastChannelUpdate-" + group.getId(), "0"));
      mLastGroupUpdate.put(group, lastupdate);
    }

    int numChannels = Integer.parseInt(mProperties.getProperty(
            "NumberOfChannels", "0"));

    ArrayList<Channel> channels = new ArrayList<Channel>();

    for (int i = 0; i < numChannels; i++) {
      DataHydraChannelContainer container = new DataHydraChannelContainer(mProperties.getProperty(
              "ChannelId-" + i, ""), mProperties.getProperty("ChannelTitle-" + i,
              ""), mProperties.getProperty("ChannelBaseUrl-" + i, ""), mProperties
              .getProperty("ChannelIconUrl-" + i, ""), mProperties.getProperty(
              "ChannelLastUpdate-" + i, ""));

      String groupId = mProperties.getProperty("ChannelGroup-" + i);
      if (groupId == null) {
        groupId = "SweDB";
      }

      Channel ch = createTVBrowserChannel(mChannelGroups.get(groupId), container);
      mInternalChannels.put(ch, container);
      channels.add(ch);
    }

    mChannels = channels;

    mLog.info("Finished loading settings for DataHydraTvDataService");
  }

  /**
   * Called by the host-application during shut-down. Implements this method to
   * store your dataservices settings to the file system.
   */
  public Properties storeSettings() {
    mLog.info("Storing settings for DataHydraTvDataService");

    for (ChannelGroup group : getAvailableGroups()) {
      String value = "0";
      if (mLastGroupUpdate.get(group) != null) {
        value = mLastGroupUpdate.get(group).toString();
      }
      mProperties.setProperty("LastChannelUpdate-" + group.getId(), value);
    }

    mProperties.setProperty("NumberOfChannels", Integer.toString(mChannels.size()));

    for (int i = 0; i < mChannels.size(); i++) {
      DataHydraChannelContainer container = mInternalChannels.get(mChannels.get(i));
      mProperties.setProperty("ChannelId-" + i, container.getId());
      mProperties.setProperty("ChannelTitle-" + i, container.getName());
      mProperties.setProperty("ChannelBaseUrl-" + i, container.getBaseUrl());
      mProperties.setProperty("ChannelIconUrl-" + i, container.getIconUrl());
      mProperties.setProperty("ChannelLastUpdate-" + i, container.getLastUpdateString());
      mProperties.setProperty("ChannelGroup-" + i, mChannels.get(i).getGroup().getId());
    }
    mLog.info("Finished storing settings for DataHydraTvDataService. Returning properties...");
    return mProperties;
  }

  /**
   * Gets the list of the channels that are available for the given channel
   * group.
   */
  public Channel[] getAvailableChannels(ChannelGroup group) {
    ArrayList<Channel> tempList = new ArrayList<Channel>();
    for (Channel channel : mChannels) {
      if (channel.getGroup().getId().equalsIgnoreCase(group.getId())) {
        tempList.add(channel);
      }
    }
    return tempList.toArray(new Channel[tempList.size()]);
  }

  public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) throws TvBrowserException {
    mHasRightToDownloadIcons = true;

    Channel[] channels = new Channel[0];

    try {
      if (monitor != null) {
        monitor.setMessage(mLocalizer.msg("Progressmessage.10",
                "Getting messages"));
      }

      mLog.log(Level.ALL, "Loading Channel file : " + ((DataHydraChannelGroup) group).getChannelFile());

      String urlMirror = getMirror().getUrl();

      URL url = new URL(urlMirror + (urlMirror.endsWith("/") ? "" : "/") + ((DataHydraChannelGroup) group).getChannelFile());

      // Download the mirror list for the next run
      try {
        IOUtilities.download(new URL(urlMirror + (urlMirror.endsWith("/") ? "" : "/") + "main_" + Mirror.MIRROR_LIST_FILE_NAME), new File(mWorkingDirectory , "main_" + Mirror.MIRROR_LIST_FILE_NAME));
      } catch(Exception ee) {}
      
      if (monitor != null) {
        monitor.setMessage(mLocalizer.msg("Progressmessage.20",
                "Getting channel list from")
                + " " + group.getProviderName());
      }

      long lastUpdate = 0;
      if (mLastGroupUpdate.get(group) != null) {
        lastUpdate = mLastGroupUpdate.get(group);
      }

      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setReadTimeout(Plugin.getPluginManager().getTvBrowserSettings()
              .getDefaultNetworkConnectionTimeout());
      con.setIfModifiedSince(lastUpdate);

      int responseCode = con.getResponseCode();

      if (responseCode == 200) {
        if (monitor != null) {
          monitor.setMessage(mLocalizer.msg("Progressmessage.30",
                  "Parsing channel list"));
        }
        DataHydraChannelContainer[] DataHydracontainers = DataHydraChannelParser.parse(new GZIPInputStream(con.getInputStream()));

        if (monitor != null) {
          monitor.setMessage(mLocalizer.msg("Progressmessage.40", "Found {0} channels, downloading channel icons...", DataHydracontainers.length));
        }

        mLastGroupUpdate.put(group, con.getLastModified());
        con.disconnect();

        ArrayList<Channel> loadedChannels = new ArrayList<Channel>();

        for (DataHydraChannelContainer container : DataHydracontainers) {
          Channel ch = createTVBrowserChannel((DataHydraChannelGroup) group, container);
          mInternalChannels.put(ch, container);
          loadedChannels.add(ch);
        }

        channels = loadedChannels.toArray(new Channel[loadedChannels.size()]);

        if (monitor != null) {
          monitor.setMessage(mLocalizer.msg("Progressmessage.50",
                  "All channels have been retrieved"));
        }

        /**
         * Update Channellist of the data plugin
         */

        // Remove all Channels of current Group
        Channel[] chs = getAvailableChannels(group);
        for (Channel ch : chs) {
          mChannels.remove(ch);
        }

        // Add all Channels for current Group
        mChannels.addAll(loadedChannels);
      } else if (responseCode == 304) {
        channels = getAvailableChannels(group);
      } else {
        throw new TvBrowserException(SweDBTvDataService.class,
                "availableResponse",
                "Unknown response during check for available channels in Swedb plugin: {0}", responseCode);
      }
    } catch (Exception e) {
      throw new TvBrowserException(SweDBTvDataService.class,
              "checkAvailableError",
              "Error checking for available channels in Swedb plugin: {0}", e
              .getMessage());
    }

    mHasRightToDownloadIcons = false;

    return channels;
  }

  private Mirror getMirror() {
    File file = new File(mWorkingDirectory , "main_" + Mirror.MIRROR_LIST_FILE_NAME);

    try {
      return Mirror.chooseUpToDateMirror(Mirror.readMirrorListFromFile(file),null,"DataHydra", "main", SweDBTvDataService.class, mLocalizer.msg("error.additional"," Please inform the TV-Browser team."));
    } catch (Exception exc) {
      try {
        if(DEFAULT_MIRRORS.length > 0) {
          Mirror[] mirr = new Mirror[DEFAULT_MIRRORS.length];

          for(int i = 0; i < DEFAULT_MIRRORS.length; i++)
            mirr[i] = new Mirror(DEFAULT_MIRRORS[i]);

          return Mirror.chooseUpToDateMirror(mirr,null,"DataHydra", "main", SweDBTvDataService.class, mLocalizer.msg("error.additional"," Please inform the TV-Browser team."));
        }
        else
          throw exc;
      }catch (Exception exc2) {
        return new Mirror(DEFAULT_PLUGINS_DOWNLOAD_URL);
      }
    }
  }


  private Channel createTVBrowserChannel(DataHydraChannelGroup group, DataHydraChannelContainer container) {
    if (mWorkingDirectory != null) {
      IconLoader iconLoader = null;
      try {
        iconLoader = new IconLoader(this, group.getId(), mWorkingDirectory);
      } catch (IOException e) {
        mLog.severe("Unable to initialize IconLoader for group ID "
                + group.getId() + " in working directory "
                + this.mWorkingDirectory);
      }

      int category = Channel.CATEGORY_TV;

      if (group.getId().equals("MSPC")) {
        if (container.getName().startsWith("[R]")) {
          container.setName(container.getName().substring(4));
          category = Channel.CATEGORY_RADIO;
        }
      }

      Channel channel = new Channel(this, container.getName(),
              container.getId(), TimeZone.getTimeZone("CET"), group.getCountry(),
              group.getCopyright(), group.getUrl(), group, null, category);

      if (!container.getIconUrl().equals("")) {
        try {
          Icon icon = iconLoader.getIcon(container.getId(), container.getIconUrl());
          channel.setDefaultIcon(icon);
        } catch (IOException e) {
          mLog.severe("Unable to load icon for "
                  + container.getId() + " on URL "
                  + container.getIconUrl());
        }
      }
      try {
        iconLoader.close();
      } catch (IOException e) {
        mLog.severe("Unable to close IconLoader for group ID "
                + group.getId() + " in working directory "
                + this.mWorkingDirectory);
      }

      return channel;
    } else {
      mLog.info("DataHydraTvDataService: Working directory has not been initialized yet. Icons not loaded");
    }

    return null;
  }

  public boolean hasRightToDownloadIcons() {
    return mHasRightToDownloadIcons;
  }

  public WeakHashMap<String, File> getIconCache() {
    return mIconCache;
  }


  public Properties getProperties() {
    return mProperties;
  }

  public ChannelGroup[] checkForAvailableChannelGroups(ProgressMonitor monitor)
          throws TvBrowserException {
    return getAvailableGroups();
  }

  public static Version getVersion() {
    return new Version(2, 62);
  }

  public PluginInfo getInfo() {
    return new devplugin.PluginInfo(
            SweDBTvDataService.class,
            mLocalizer.msg("PluginInfo.name", "DataHydra TV-Data Plugin"),
            mLocalizer.msg("PluginInfo.description",
                    "A TV Data Service plugin which uses XMLTV-data from TV.SWEDB.SE and mspc.no"),
            "TV-Browser Team",
            mLocalizer
                    .msg("PluginInfo.support",
                    "Support the SWEDB and the mspc crew - Don't forget to register with http://tv.swedb.se/ and http://www.mspc.no"));
  }

}