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
package tvbrowserdataservice;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import tvbrowserdataservice.file.DayProgramFile;
import tvbrowserdataservice.file.SummaryFile;
import tvbrowserdataservice.file.TvDataLevel;
import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.io.DownloadManager;
import util.io.IOUtilities;
import util.io.Mirror;
import devplugin.Channel;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.ProgressMonitor;
import devplugin.Version;


/**
 *
 *
 * @author Til Schneider, www.murfman.de
 */
public class TvBrowserDataService extends devplugin.AbstractTvDataService {

  private static final Logger mLog
          = Logger.getLogger(TvBrowserDataService.class.getName());

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer
          = util.ui.Localizer.getLocalizerFor(TvBrowserDataService.class);

  private static final Version VERSION = new Version(3,0);

  protected static final String CHANNEL_GROUPS_FILENAME = "groups.txt";
  private static final String DEFAULT_CHANNEL_GROUPS_URL = "http://tvbrowser.org/listings";

  private DownloadManager mDownloadManager;
  private TvDataUpdateManager mUpdateManager;
  private int mTotalDownloadJobCount;
  private ProgressMonitor mProgressMonitor;
  private boolean mHasRightToDownloadIcons;

  private static final String[][] DEFAULT_CHANNEL_GROUP_MIRRORS = new String[][] {
          new String[] {"http://www.tvbrowser.org/mirrorlists"},
          new String[] {"http://www.tvbrowser.org/mirrorlists"},
          new String[] {"http://www.tvbrowser.org/mirrorlists"},
          new String[] {"http://www.tvbrowser.org/mirrorlists"},
          new String[] {"http://www.tvbrowser.org/mirrorlists"},
          new String[] {"http://www.tvbrowser.org/mirrorlists"},
          new String[] {"http://sender.wannawork.de"},

  };

  private static final String[] DEFAULT_CHANNEL_GROUP_NAMES = new String[] {
          "digital",
          "austria",
          "main",
          "local",
          "others",
          "radio",
          "bodostv"
  };


  private HashSet<TvBrowserDataServiceChannelGroup> mAvailableChannelGroupsSet;

  private TvBrowserDataServiceSettings mSettings;

  private File mDataDir;

  private TvDataLevel[] mSubscribedLevelArr;

  private static TvBrowserDataService mInstance;

  private boolean mGroupFileWasLoaded = false;


  /**
   * Creates a new TvBrowserDataService.
   */
  public TvBrowserDataService() {
    mHasRightToDownloadIcons = false;
    mSettings = new TvBrowserDataServiceSettings(new Properties());
    mInstance=this;
    mAvailableChannelGroupsSet =new HashSet<TvBrowserDataServiceChannelGroup>();
  }


  /**
   * Get the instance of the TvBrowserDataService.
   * If the instance isn't available it will be created.
   *
   * @return The instance of the TvBrowserDataService.
   */
  public static TvBrowserDataService getInstance() {
    if (mInstance==null) {
      throw new RuntimeException("no instance of TvBrowserDataService class available");
    }
    return mInstance;
  }

  public void setWorkingDirectory(File dataDir) {
    mDataDir=dataDir;
    if (mAvailableChannelGroupsSet ==null) {
      return;
    }
    Iterator<TvBrowserDataServiceChannelGroup> it=mAvailableChannelGroupsSet.iterator();
    while (it.hasNext()) {
      (it.next()).setWorkingDirectory(dataDir);
    }
  }

  /**
   * @return The data directory of the TvBrowserDataService.
   */
  public File getWorkingDirectory() {
    return mDataDir;
  }


  private TvBrowserDataServiceChannelGroup getChannelGroupById(String id) {
    Iterator<TvBrowserDataServiceChannelGroup> it=mAvailableChannelGroupsSet.iterator();
    while (it.hasNext()) {
      TvBrowserDataServiceChannelGroup group=it.next();
      if (group.getId().equalsIgnoreCase(id)) {
        return group;
      }
    }
    return null;
  }


  /**
   * Updates the TV listings provided by this data service.
   *
   *
   */
  public void updateTvData(TvDataUpdateManager updateManager, Channel[] channelArr,
                           Date startDate, int dateCount, ProgressMonitor monitor) {
    boolean groupsWereAlreadyUpdated = false;
    mHasRightToDownloadIcons = true;
    // Check for connection
    if (!updateManager.checkConnection()) {
      return;
    }

    // Reset list of banned Servers
    Mirror.resetBannedServers();

    mUpdateManager=updateManager;
    mProgressMonitor = monitor;

    HashSet<TvBrowserDataServiceChannelGroup> groups=new HashSet<TvBrowserDataServiceChannelGroup>();
    for (Channel element : channelArr) {
      TvBrowserDataServiceChannelGroup curGroup=getChannelGroupById(element.getGroup().getId());
      if (curGroup==null) {
        mLog.warning("Invalid channel group id: "+element.getGroup().getId());
        continue;
      }

      if (!groups.contains(curGroup)) {
        groups.add(curGroup);
        mProgressMonitor.setMessage(mLocalizer.msg("info.8","Finding mirror for group {0} ...",curGroup.getName()));

        try {
          curGroup.chooseMirrors();
        } catch(TvBrowserException e) {
          try {
            if(!groupsWereAlreadyUpdated) {
              groupsWereAlreadyUpdated = true;
              downloadChannelGroupFile();
            }
            setMirrorUrlForServerDefinedChannelGroup(curGroup);
            curGroup.chooseMirrors();
          }catch(TvBrowserException de) {
            ErrorHandler.handle(de);
          }
        }
      }

      curGroup.addChannel(element);

    }


    // Delete outdated files
    deleteOutdatedFiles();

    mProgressMonitor.setMessage(mLocalizer.msg("info.7","Preparing download..."));

    // Create a download manager and add all the jobs
    mDownloadManager = new DownloadManager();
    TvDataBaseUpdater updater = new TvDataBaseUpdater(this, updateManager);

    // Add a receive or a update job for each channel and day
    DayProgramReceiveDH receiveDH = new DayProgramReceiveDH(this, updater);
    DayProgramUpdateDH updateDH   = new DayProgramUpdateDH(this, updater);


    monitor.setMaximum(groups.size());

    Iterator<TvBrowserDataServiceChannelGroup> groupIt=groups.iterator();
    int i=0;
    while (groupIt.hasNext()) {

      monitor.setValue(i++);

      TvBrowserDataServiceChannelGroup group=groupIt.next();
      SummaryFile summaryFile = group.getSummary();
      if (summaryFile != null) {
        Date date = startDate;
        for (int day = 0; day < dateCount; day++) {
          for (TvDataLevel element : mSubscribedLevelArr) {
            String level = element.getId();
            Iterator<Channel> it=group.getChannels();
            while (it.hasNext()) {
              Channel ch=it.next();
              addDownloadJob(updateManager, group.getMirror(), date, level, ch,
                      ch.getCountry(), receiveDH, updateDH, summaryFile);
            }
          }
          date = date.addDays(1);
        }
      }
    }




    mProgressMonitor.setMessage(mLocalizer.msg("info.1","Downloading..."));


    // Initialize the ProgressMonitor
    mTotalDownloadJobCount = mDownloadManager.getDownloadJobCount();
    mProgressMonitor.setMaximum(mTotalDownloadJobCount);

    // Let the download begin
    try {
      mDownloadManager.runDownload();
    }
    finally {
      // Update the programs for which the update succeeded in every case
      mProgressMonitor.setMessage(mLocalizer.msg("info.2","Updating database"));
      updater.updateTvDataBase(monitor);
      mProgressMonitor.setMessage("");

      // Clean up resources
      mDownloadManager = null;
      mUpdateManager = null;
      mProgressMonitor = null;
    }

    mHasRightToDownloadIcons = false;
  }


  private void deleteOutdatedFiles() {
    // Delete all day programs older than 3 days
    Date deadlineDay = new Date().addDays(- 3);

    File[] fileArr = mDataDir.listFiles();
    if (fileArr == null) {
      mLog.warning("Cannot read data dir for file deletion: " + mDataDir);
      return;
    }
    for (File element : fileArr) {
      String fileName = element.getName();
      if (fileName.endsWith(".prog.gz")) {
        try {
          int year = Integer.parseInt(fileName.substring(0, 4));
          int month = Integer.parseInt(fileName.substring(5, 7));
          int day = Integer.parseInt(fileName.substring(8, 10));
          Date date = new Date(year, month, day);

          // Is this day program older than the deadline day?
          if (date.compareTo(deadlineDay) < 0) {
            // It is -> delete the file
            element.delete();
          }
        }
        catch (Exception exc) {
          // ignore
        }
      }
    }
  }

  private void addDownloadJob(TvDataUpdateManager dataBase, Mirror mirror, Date date,
                              String level, Channel channel, String country,
                              DayProgramReceiveDH receiveDH, DayProgramUpdateDH updateDH,
                              SummaryFile summary)
  {
    // NOTE: summary is null when getting failed
    if (summary == null) {
      return;
    }
    String completeFileName = DayProgramFile.getProgramFileName(date,
            country, channel.getId(), level);
    File completeFile = new File(mDataDir, completeFileName);

    int levelIdx = DayProgramFile.getLevelIndexForId(level);


    boolean downloadTheWholeDayProgram;
    // Check whether we already have data for this day
    downloadTheWholeDayProgram = !(dataBase.isDayProgramAvailable(date, channel) && completeFile.exists());
    if (!downloadTheWholeDayProgram) {
      // We have data -> Check whether the mirror has an update

      // Get the version of the file
      int localVersion;
      try {
        localVersion = DayProgramFile.readVersionFromFile(completeFile);


        // Check whether the mirror has a newer version
        boolean needsUpdate;
        int mirrorVersion = summary.getDayProgramVersion(date, country,
                channel.getId(), levelIdx);
        needsUpdate = (mirrorVersion > localVersion);


        if (needsUpdate) {
          // We need an update -> Add an update job
          String updateFileName = DayProgramFile.getProgramFileName(date,
                  country, channel.getId(), level, localVersion);
          mDownloadManager.addDownloadJob(mirror.getUrl(),updateFileName, updateDH);
        }

      } catch (Exception exc) {
//      // don't throw an exception; try to download the file again
//      throw new TvBrowserException(getClass(), "error.5",
//                "Reading version of TV data file failed: {0}",
//                completeFile.getAbsolutePath(), exc);
        downloadTheWholeDayProgram = true;
      }

    }

    if (downloadTheWholeDayProgram)
    {
      // We have no data -> Check whether the mirror has
      boolean needsUpdate;
      int mirrorVersion = summary.getDayProgramVersion(date, country,
              channel.getId(), levelIdx);
      needsUpdate = (mirrorVersion != -1);

      if (needsUpdate) {
        // We need an receive -> Add a download job
        mDownloadManager.addDownloadJob(mirror.getUrl(),completeFileName, receiveDH);
      }
    }
  }



  void checkCancelDownload() {
    if (mUpdateManager != null && mDownloadManager != null) {
      if (mUpdateManager.cancelDownload()) {
        mDownloadManager.removeAllDownloadJobs();
      }

      // Update the ProgressMonitor
      int jobCount = mDownloadManager.getDownloadJobCount();
      mProgressMonitor.setValue(mTotalDownloadJobCount - jobCount);
    }
  }


  boolean isDayProgramInDataBase(Date date, Channel channel) {
    return mUpdateManager.isDayProgramAvailable(date, channel);
  }

  File getDataDir() {
    return mDataDir;
  }



  protected Channel getChannel(String country, String channelName) {
    Iterator<TvBrowserDataServiceChannelGroup> it = mAvailableChannelGroupsSet.iterator();
    while (it.hasNext()) {
      Channel[] chArr = getAvailableChannels(it.next());
      for (Channel element : chArr) {
        if (element.getCountry().equals(country) && element.getId().equals(channelName)) {
          return element;
        }
      }
    }
    return null;
  }

  /**
   * Adds a new channel group to the TvBrowserDataService.
   *
   * @param group The channel group to add.
   */
  public void addGroup(TvBrowserDataServiceChannelGroup group) {
    mAvailableChannelGroupsSet.add(group);
  }

  /**
   * Removes a channel group from the TvBrowserDataService.
   *
   * @param group The channel group to remove.
   */
  public void removeGroup(TvBrowserDataServiceChannelGroup group) {
    mAvailableChannelGroupsSet.remove(group);
    group.deleteAllFiles();
  }

  /**
   * Called by the host-application during start-up. Implement this method to
   * load your dataservices settings from the file system.
   */
  public void loadSettings(Properties properties) {
    mSettings = new TvBrowserDataServiceSettings(properties);

    String[] levelIds=mSettings.getLevelIds();
    ArrayList<TvDataLevel> levelList=new ArrayList<TvDataLevel>();
    for (int i=0;i<DayProgramFile.getLevels().length;i++) {
      if (DayProgramFile.getLevels()[i].isRequired()) {
        levelList.add(DayProgramFile.getLevels()[i]);
      }
      else{
        for (String levelId : levelIds) {
          if (levelId.equals(DayProgramFile.getLevels()[i].getId())) {
            levelList.add(DayProgramFile.getLevels()[i]);
          }
        }
      }
    }

    setTvDataLevel(levelList.toArray(new TvDataLevel[levelList.size()]));


    /* load channel groups settings */
    refreshAvailableChannelGroups();

    setWorkingDirectory(mDataDir);
  }


  /**
   * Sets the level of the TV data.
   *
   * @param levelArr The array with the TV data levels.
   */
  public void setTvDataLevel(TvDataLevel[] levelArr) {
    mSubscribedLevelArr = levelArr;
  }

  public TvBrowserDataServiceChannelGroup[] getUserDefinedChannelGroups() {
    Collection<TvBrowserDataServiceChannelGroup> col1 = getUserDefinedChannelGroupsCollection();
    Collection<TvBrowserDataServiceChannelGroup> col2 = getServerDefinedChannelGroupsCollection();

    ArrayList<TvBrowserDataServiceChannelGroup> list = new ArrayList<TvBrowserDataServiceChannelGroup>(col1);
    list.removeAll(col2);

    return list.toArray(new TvBrowserDataServiceChannelGroup[list.size()]);
  }

  private Collection<TvBrowserDataServiceChannelGroup> getUserDefinedChannelGroupsCollection() {
    HashSet<TvBrowserDataServiceChannelGroup> result = new HashSet<TvBrowserDataServiceChannelGroup>();
    String groupNames = mSettings.getGroupName();
    String[] groupNamesArr;

    /* If there are no groups defined in the settings file, we return all default groups */
    if (groupNames == null) {
      for (int i=0;i<DEFAULT_CHANNEL_GROUP_NAMES.length;i++) {
        result.add(new TvBrowserDataServiceChannelGroup(this, DEFAULT_CHANNEL_GROUP_NAMES[i], DEFAULT_CHANNEL_GROUP_MIRRORS[i], mSettings));
      }
    }
    else {

      groupNamesArr=groupNames.split(":");
      for (String element : groupNamesArr) {
        if (element.trim().length()>0) {
          String groupUrls=mSettings.getGroupUrls(element);
          String[] groupUrlArr=groupUrls.split(";");
          result.add(new TvBrowserDataServiceChannelGroup(this, element,groupUrlArr,mSettings));
        }
      }

    }
    return result;
  }

  private Collection<TvBrowserDataServiceChannelGroup> getServerDefinedChannelGroupsCollection() {
    File groupFile = new File(mDataDir, CHANNEL_GROUPS_FILENAME);
    if (!groupFile.exists()) {
      mLog.info("Group file '"+CHANNEL_GROUPS_FILENAME+"' does not exist");
      return new ArrayList<TvBrowserDataServiceChannelGroup>();
    }
    BufferedReader in = null;
    ArrayList<TvBrowserDataServiceChannelGroup> list = new ArrayList<TvBrowserDataServiceChannelGroup>();

    try {
  	  in = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(groupFile), 0x1000), "utf-8"));
      String line = in.readLine();
      while (line != null) {
        String[] s = line.split(";");
        if (s.length>=5) {
          String id = s[0];
          String name = s[1];
          String providername = s[2];
          String description = s[3];

          int n = s.length - 4;

          String[] mirrors = new String[n];

          for(int i = 0; i < n; i++) {
            mirrors[i] = s[i+4];
          }

          TvBrowserDataServiceChannelGroup group = new TvBrowserDataServiceChannelGroup(this, id, name, description, providername, mirrors, mSettings);
          group.setWorkingDirectory(mDataDir);
          list.add(group);
        }
        line = in.readLine();
      }
      in.close();
    } catch (IOException e) {
      mLog.log(Level.SEVERE, "Could not read group list "+CHANNEL_GROUPS_FILENAME, e);
    }
    finally {
      if(in != null) {
        try {
          in.close();
        }catch(Exception ee) {}
      }
    }

    return list;

  }

  private void setMirrorUrlForServerDefinedChannelGroup(TvBrowserDataServiceChannelGroup group) {
    File groupFile = new File(mDataDir, CHANNEL_GROUPS_FILENAME);
    if (!groupFile.exists()) {
      mLog.info("Group file '"+CHANNEL_GROUPS_FILENAME+"' does not exist");
      return;
    }

    BufferedReader in = null;

    try {
      in = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(groupFile), 0x1000), "utf-8"));
      String line = in.readLine();
      while (line != null) {
        String[] s = line.split(";");
        if (s.length>=5) {
          if(s[0].compareTo(group.getId()) == 0) {
            group.setBaseMirror(new String[] {s[4]});
            break;
          }
        }
        line = in.readLine();
      }
      in.close();
    } catch (IOException e) {
      mLog.log(Level.SEVERE, "Could not read group list "+CHANNEL_GROUPS_FILENAME, e);
    }
    finally {
      if(in != null) {
        try {
          in.close();
        }catch(Exception ee) {}
      }
    }
  }

  /**
   * Called by the host-application during shut-down. Implements this method to
   * store your data service settings to the file system.
   */
  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  public boolean hasSettingsPanel() {
    return true;
  }



  public SettingsPanel getSettingsPanel() {
    return TvBrowserDataServiceSettingsPanel.getInstance(mSettings);
  }


  public devplugin.ChannelGroup[] getAvailableGroups() {
    return mAvailableChannelGroupsSet.toArray(new TvBrowserDataServiceChannelGroup[mAvailableChannelGroupsSet.size()]);
  }

  /**
   * Gets the list of the channels that are available by this data service.
   */
  public Channel[] getAvailableChannels(devplugin.ChannelGroup g) {

    TvBrowserDataServiceChannelGroup group = getChannelGroupById(g.getId());
    if (group == null) {
      mLog.warning("Invalid group: "+g.getId() +" - returning empty channel array");
      return new Channel[]{};
    }
    return group.getAvailableChannels();
  }

  private Mirror getChannelGroupsMirror() {
    File file = new File(mDataDir, CHANNEL_GROUPS_FILENAME.substring(0,
        CHANNEL_GROUPS_FILENAME.indexOf('.'))
        + "_" + Mirror.MIRROR_LIST_FILE_NAME);

    if(file.isFile()) {
      try {
        return Mirror.chooseUpToDateMirror(Mirror.readMirrorListFromFile(file),null,"Groups.txt", "groups", TvBrowserDataService.class, "  Please inform the TV-Browser team.");
      } catch (Exception exc) {}
    }

    return getDefaultChannelGroupsMirror();
  }

  private Mirror getDefaultChannelGroupsMirror() {
    try {
      String[] defaultMirrors = getDefaultMirrors();
      if(defaultMirrors.length > 0) {
        Mirror[] mirr = new Mirror[defaultMirrors.length];

        for(int i = 0; i < defaultMirrors.length; i++) {
          mirr[i] = new Mirror(defaultMirrors[i]);
        }

        Mirror choosenMirror = Mirror.chooseUpToDateMirror(mirr,null,"Groups.txt", "groups",TvBrowserDataService.class, " Please inform the TV-Browser team.");

        if(choosenMirror != null) {
          return choosenMirror;
        }
      }
    }catch (Exception exc2) {}

    return new Mirror(DEFAULT_CHANNEL_GROUPS_URL);
  }

  protected void downloadChannelGroupFile() throws TvBrowserException {
    if(!mGroupFileWasLoaded) {
      String url = getChannelGroupsMirror().getUrl();

      try {
        String name = CHANNEL_GROUPS_FILENAME.substring(0,
            CHANNEL_GROUPS_FILENAME.indexOf('.'))
            + "_" + Mirror.MIRROR_LIST_FILE_NAME;
        IOUtilities.download(new URL(url + (url.endsWith("/") ? "" : "/") + name), new File(mDataDir, name));
      } catch(Exception ee) {}

      try {
        try {
          IOUtilities.download(new URL(url + (url.endsWith("/") ? "" : "/") + CHANNEL_GROUPS_FILENAME), new File(mDataDir, CHANNEL_GROUPS_FILENAME));
        }catch(Exception ex) {
          url = DEFAULT_CHANNEL_GROUPS_URL;
          IOUtilities.download(new URL(url + (url.endsWith("/") ? "" : "/") + CHANNEL_GROUPS_FILENAME), new File(mDataDir, CHANNEL_GROUPS_FILENAME));
        }
        mGroupFileWasLoaded = true;
      } catch (MalformedURLException e) {
        throw new TvBrowserException(TvBrowserDataService.class, "invalidURL", "Invalid URL: {0}", url, e);
      } catch (IOException e) {
        throw new TvBrowserException(TvBrowserDataService.class, "downloadGroupFileFailed","Could not download group file {0}", url, e);
      }
    }
  }


  /**
   * Read the available channel groups from the file system
   */
  private void refreshAvailableChannelGroups() {
    Collection<TvBrowserDataServiceChannelGroup> serverDefinedGroups = getServerDefinedChannelGroupsCollection();

    mAvailableChannelGroupsSet.clear();
    mAvailableChannelGroupsSet.addAll(serverDefinedGroups);
    mAvailableChannelGroupsSet.addAll(getUserDefinedChannelGroupsCollection());

    for (int i=0;i<DEFAULT_CHANNEL_GROUP_NAMES.length;i++) {
      TvBrowserDataServiceChannelGroup g =new TvBrowserDataServiceChannelGroup(this, DEFAULT_CHANNEL_GROUP_NAMES[i], DEFAULT_CHANNEL_GROUP_MIRRORS[i], mSettings);
      if (!mAvailableChannelGroupsSet.contains(g)) {
        mAvailableChannelGroupsSet.add(g);
      }
    }
  }

  public devplugin.ChannelGroup[] checkForAvailableChannelGroups(ProgressMonitor monitor) throws TvBrowserException {
    mGroupFileWasLoaded = false;
    downloadChannelGroupFile();
    refreshAvailableChannelGroups();
    return getAvailableGroups();
  }

  public Channel[] checkForAvailableChannels(devplugin.ChannelGroup g, ProgressMonitor monitor) throws TvBrowserException {
    mHasRightToDownloadIcons = true;
    downloadChannelGroupFile();

    TvBrowserDataServiceChannelGroup group = getChannelGroupById(g.getId());
    if (group == null) {
      mLog.warning("Unknown group: "+g.getId());
      return new Channel[]{};
    }

    ArrayList<Channel> channelList=new ArrayList<Channel>();
    if (monitor != null) {
      monitor.setMessage(mLocalizer.msg("checkingForAvailableChannels","Checking for group {0}", g.getName()));
    }
    Channel[] ch=group.checkForAvailableChannels(null);
    for (Channel element : ch) {
      channelList.add(element);
    }

    mHasRightToDownloadIcons = false;

    return channelList.toArray(new Channel[channelList.size()]);
  }

  public boolean supportsDynamicChannelList() {
    return true;
  }

  public boolean supportsDynamicChannelGroups() {
    return true;
  }


  public static Version getVersion() {
    return VERSION;
  }


  /**
   * Gets information about this TvDataService
   */
  public PluginInfo getInfo() {
    return new devplugin.PluginInfo(TvBrowserDataService.class,
            "TV-Browser",
            mLocalizer.msg("description", "Die eigenen TV-Daten des TV-Browser-Projektes"),
            "Til Schneider, www.murfman.de",
            mLocalizer.msg("license","Terms of Use:\n=============\nAll TV/Radio listings provided by TV-Browser (http://www.tvbrowser.org) are protected by copyright laws and may only be used within TV-Browser or other name like applications authorizied by the manufacturer of TV-Browser (http://www.tvbrowser.org) for information about the upcoming program of the available channels.\nEvery other manner of using, reproducing or redistributing of the TV/Radio listings is illegal and may be prosecuted on civil or criminal law.\n\nOn downloading the TV/Radio listings you declare your agreement to these terms.\n\nIf you have any questions concerning these terms please contact dev@tvbrowser.org"));
  }

  /**
   * Gets if it is allowed to download the channel icons.
   *
   * @return <code>True</code> if the download of the channel icons is allowed.
   */
  public boolean hasRightToDownloadIcons() {
    return mHasRightToDownloadIcons;
  }

}