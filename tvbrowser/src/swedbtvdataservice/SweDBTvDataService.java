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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;

import org.apache.commons.lang.StringUtils;

import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
import util.io.Mirror;
import util.misc.SoftReferenceCache;
import util.tvdataservice.IconLoader;
import util.ui.Localizer;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.ProgressMonitor;
import devplugin.Version;

public class SweDBTvDataService extends devplugin.AbstractTvDataService {
  /** The default plugins download URL */
  private static final String DEFAULT_PLUGINS_DOWNLOAD_URL = "http://www.tvbrowser.org/mirrorlists";

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(SweDBTvDataService.class);

  private static final Logger mLog = Logger.getLogger(SweDBTvDataService.class.getName());

  private static final Version VERSION = new Version(3,0);

  private File mWorkingDirectory;

  private DataHydraSettings mSettings = new DataHydraSettings();

  private HashMap<String, DataHydraChannelGroup> mChannelGroups;

  private HashMap<Channel, DataHydraChannelContainer> mInternalChannels = new HashMap<Channel, DataHydraChannelContainer>();

  private HashMap<ChannelGroup, Long> mLastGroupUpdate = new HashMap<ChannelGroup, Long>();

  private ArrayList<Channel> mChannels;

  private SoftReferenceCache<String, File> mIconCache = new SoftReferenceCache<String, File>();

  private boolean mHasRightToDownloadIcons;

  private DataHydraFileParser mParser = new DataHydraFileParser();

  private IconLoader iconLoader;
  static final String SHOW_REGISTER_TEXT = "showRegisterText";

  /**
   * Creates a new instance of SweDBTvDataService
   */
  public SweDBTvDataService() {
    mHasRightToDownloadIcons = false;
    mChannelGroups = new HashMap<String, DataHydraChannelGroup>();
    addGroup(new DataHydraChannelGroup("SweDB", "SweDB.se", "(c) swedb.se", "http://tv.swedb.se", "swedb_channels.xml.gz", "se"));
    addGroup(new DataHydraChannelGroup("MSPC", "mspc.no", "(c) mspc.no", "http://www.mspc.no", "mspc_channels.xml.gz", "no"));
    addGroup(new DataHydraChannelGroup("gonix", "gonix.net", "(c) gonix.net", "http://www.gonix.net", "hrv_channels.xml.gz", "hr", false));
    addGroup(new DataHydraChannelGroup("oztivo", "oztivo.net", "(c) oztivo.net", "http://www.oztivo.net/", "au_channels.xml.gz", "au"));
  }

  private void addGroup(DataHydraChannelGroup dataHydraChannelGroup) {
    mChannelGroups.put(dataHydraChannelGroup.getId(), dataHydraChannelGroup);
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
    return new DataHydraSettingsPanel(mSettings);
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
    // Check for connection
    if (!updateManager.checkConnection()) {
      return;
    }

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
   * load your data services settings from the file system.
   */
  public void loadSettings(Properties properties) {
    mLog.info("Loading settings in DataHydraTvDataService");

    for (ChannelGroup group : getAvailableGroups()) {
      long lastupdate = Long.parseLong(properties.getProperty("LastChannelUpdate-" + group.getId(), "0"));
      mLastGroupUpdate.put(group, lastupdate);
    }

    int numChannels = Integer.parseInt(properties.getProperty(
            "NumberOfChannels", "0"));

    ArrayList<Channel> channels = new ArrayList<Channel>();

    // create channels sorted by group to avoid recreation of icon loader
    for (ChannelGroup group : getAvailableGroups()) {
      String groupId = group.getId();
      DataHydraChannelGroup dataHydraChannelGroup = mChannelGroups.get(groupId);
      for (int i = 0; i < numChannels; i++) {
        String channelGroupId = properties.getProperty("ChannelGroup-" + i);
        if (channelGroupId == null) {
          channelGroupId = "SweDB";
        }
        if (groupId.equals(channelGroupId)) {
          // create the icon loader on demand to avoid disk access for non used groups
          if (iconLoader == null) {
            initializeIconLoader(dataHydraChannelGroup);
          }
          DataHydraChannelContainer container = new DataHydraChannelContainer(properties.getProperty(
                  "ChannelId-" + i, ""), properties.getProperty("ChannelTitle-" + i,
                  ""), properties.getProperty("ChannelBaseUrl-" + i, ""), properties
                  .getProperty("ChannelIconUrl-" + i, ""), properties.getProperty(
                  "ChannelLastUpdate-" + i, ""));

          Channel ch = createTVBrowserChannel(dataHydraChannelGroup, container);
          mInternalChannels.put(ch, container);
          channels.add(ch);
        }
      }
      closeIconLoader(dataHydraChannelGroup);
    }

    mChannels = channels;
    mSettings.setShowRegisterText(Boolean.parseBoolean(properties.getProperty(SweDBTvDataService.SHOW_REGISTER_TEXT, "true")));

    mLog.info("Finished loading settings for DataHydraTvDataService");
  }

  /**
   * Called by the host-application during shut-down. Implements this method to
   * store your dataservices settings to the file system.
   */
  public Properties storeSettings() {
    mLog.info("Storing settings for DataHydraTvDataService");

    Properties properties = new Properties();
    for (ChannelGroup group : getAvailableGroups()) {
      String value = "0";
      if (mLastGroupUpdate.get(group) != null) {
        value = mLastGroupUpdate.get(group).toString();
      }
      properties.setProperty("LastChannelUpdate-" + group.getId(), value);
    }

    properties.setProperty("NumberOfChannels", Integer.toString(mChannels.size()));

    for (int i = 0; i < mChannels.size(); i++) {
      final Channel channel = mChannels.get(i);
      DataHydraChannelContainer container = mInternalChannels.get(channel);
      properties.setProperty("ChannelId-" + i, container.getId());
      properties.setProperty("ChannelTitle-" + i, container.getName());
      properties.setProperty("ChannelBaseUrl-" + i, container.getBaseUrl());
      properties.setProperty("ChannelIconUrl-" + i, container.getIconUrl());
      properties.setProperty("ChannelLastUpdate-" + i, container.getLastUpdateString());
      properties.setProperty("ChannelGroup-" + i, channel.getGroup().getId());
    }
    properties.setProperty(SweDBTvDataService.SHOW_REGISTER_TEXT, String.valueOf(mSettings.getShowRegisterText()));

    mLog.info("Finished storing settings for DataHydraTvDataService. Returning properties...");
    return properties;
  }

  /**
   * Gets the list of the channels that are available for the given channel
   * group.
   */
  public Channel[] getAvailableChannels(ChannelGroup group) {
    if (mChannels == null) {
      return new Channel[0];
    }
    ArrayList<Channel> tempList = new ArrayList<Channel>();
    for (Channel channel : mChannels) {
      if (channel.getGroup().getId().equalsIgnoreCase(group.getId())) {
        tempList.add(channel);
      }
    }
    return tempList.toArray(new Channel[tempList.size()]);
  }

  public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) throws TvBrowserException {
    if (!(group instanceof DataHydraChannelGroup)) {
      return new Channel[0];
    }
    DataHydraChannelGroup hydraGroup = (DataHydraChannelGroup) group;
    mHasRightToDownloadIcons = true;

    Channel[] channels;

    try {
      if (monitor != null) {
        monitor.setMessage(mLocalizer.msg("Progressmessage.10",
                "Getting messages"));
      }

      mLog.log(Level.ALL, "Loading Channel file : " + hydraGroup.getChannelFile());

      String urlMirror = getMirror().getUrl();

      URL url = new URL(urlMirror + (urlMirror.endsWith("/") ? "" : "/") + hydraGroup.getChannelFile());

      // Download the mirror list for the next run
      try {
        IOUtilities.download(new URL(urlMirror + (urlMirror.endsWith("/") ? "" : "/") + "main_" + Mirror.MIRROR_LIST_FILE_NAME), new File(mWorkingDirectory , "main_" + Mirror.MIRROR_LIST_FILE_NAME));
      } catch(Exception ee) {}

      if (monitor != null) {
        monitor.setMessage(mLocalizer.msg("Progressmessage.20",
                "Getting channel list from")
                + " " + hydraGroup.getProviderName());
      }

      long lastUpdate = 0;
      if (mLastGroupUpdate.get(hydraGroup) != null) {
        lastUpdate = mLastGroupUpdate.get(hydraGroup);
      }

      mLog.log(Level.ALL, "Loading URL : " + url.toString());

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
        int fileSize = con.getContentLength();
        if (fileSize == 0) {
          throw new TvBrowserException(SweDBTvDataService.class,
              "errorEmptyChannelList",
              "Channel list file for group \"{0}\" is empty: {1}.", group.getName(), url);
        }
        else {
          DataHydraChannelContainer[] DataHydracontainers = DataHydraChannelParser.parse(IOUtilities.openSaveGZipInputStream(con.getInputStream()));

          if (monitor != null) {
            monitor.setMessage(mLocalizer.msg("Progressmessage.40", "Found {0} channels, downloading channel icons...", DataHydracontainers.length));
          }

          mLastGroupUpdate.put(hydraGroup, con.getLastModified());
          con.disconnect();

          ArrayList<Channel> loadedChannels = new ArrayList<Channel>();

          for (DataHydraChannelContainer container : DataHydracontainers) {
            initializeIconLoader(hydraGroup);
            Channel ch = createTVBrowserChannel(hydraGroup, container);
            closeIconLoader(hydraGroup);
            mInternalChannels.put(ch, container);
            loadedChannels.add(ch);
          }

          channels = loadedChannels.toArray(new Channel[loadedChannels.size()]);

          if (monitor != null) {
            monitor.setMessage(mLocalizer.msg("Progressmessage.50",
                    "All channels have been retrieved"));
          }

          /**
           * Update Channel list of the data plugin
           */

          // Remove all Channels of current Group
          Channel[] chs = getAvailableChannels(hydraGroup);
          for (Channel ch : chs) {
            mChannels.remove(ch);
          }

          // Add all Channels for current Group
          mChannels.addAll(loadedChannels);
        }
      } else if (responseCode == 304) {
        channels = getAvailableChannels(hydraGroup);
      } else {
        throw new TvBrowserException(SweDBTvDataService.class,
                "availableResponse",
                "Unknown response during check for available channels in Swedb plugin: {0}", responseCode);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new TvBrowserException(SweDBTvDataService.class,
              "checkAvailableError",
              "Error checking for available channels in Swedb plugin: {0}", e
              .getLocalizedMessage());
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
        String[] defaultMirrors = getDefaultMirrors();
        if(defaultMirrors.length > 0) {
          Mirror[] mirr = new Mirror[defaultMirrors.length];

          for(int i = 0; i < defaultMirrors.length; i++) {
            mirr[i] = new Mirror(defaultMirrors[i]);
          }

          return Mirror.chooseUpToDateMirror(mirr,null,"DataHydra", "main", SweDBTvDataService.class, mLocalizer.msg("error.additional"," Please inform the TV-Browser team."));
        } else {
          throw exc;
        }
      }catch (Exception exc2) {
        return new Mirror(DEFAULT_PLUGINS_DOWNLOAD_URL);
      }
    }
  }


  private Channel createTVBrowserChannel(DataHydraChannelGroup group, DataHydraChannelContainer container) {
    if (mWorkingDirectory != null) {
      int category = Channel.CATEGORY_TV;

      if (group.getId().equals("MSPC")) {
        if (container.getName().startsWith("[R]")) {
          container.setName(container.getName().substring(4));
          category = Channel.CATEGORY_RADIO;
        }
      }

      Channel channel = new Channel(this, container.getName(),
              container.getId(), TimeZone.getTimeZone("UTC"), group.getCountry(),
              group.getCopyright(), group.getUrl(), group, null, category);

      if (StringUtils.isNotEmpty(container.getIconUrl())) {
        try {
          Icon icon = iconLoader.getIcon(container.getId(), container.getIconUrl());
          channel.setDefaultIcon(icon);
        } catch (IOException e) {
          mLog.severe("Unable to load icon for "
                  + container.getId() + " on URL "
                  + container.getIconUrl());
        }
      }
      return channel;
    } else {
      mLog.info("DataHydraTvDataService: Working directory has not been initialized yet. Icons not loaded");
    }

    return null;
  }

  private void closeIconLoader(DataHydraChannelGroup group) {
    try {
      if (iconLoader != null) {
        iconLoader.close();
      }
    } catch (IOException e) {
      mLog.severe("Unable to close IconLoader for group ID "
              + group.getId() + " in working directory "
              + this.mWorkingDirectory);
    }
    iconLoader = null;
  }

  private void initializeIconLoader(DataHydraChannelGroup group) {
    iconLoader = null;
    try {
      iconLoader = getIconLoader(group.getId(), mWorkingDirectory);
    } catch (IOException e) {
      mLog.severe("Unable to initialize IconLoader for group ID "
              + group.getId() + " in working directory "
              + this.mWorkingDirectory);
    }
  }

  public boolean hasRightToDownloadIcons() {
    return mHasRightToDownloadIcons;
  }

  public SoftReferenceCache<String, File> getIconCache() {
    return mIconCache;
  }

  public ChannelGroup[] checkForAvailableChannelGroups(ProgressMonitor monitor)
          throws TvBrowserException {
    return getAvailableGroups();
  }

  public static Version getVersion() {
    return VERSION;
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

  public boolean getShowRegisterText() {
    return mSettings.getShowRegisterText();
  }

}
