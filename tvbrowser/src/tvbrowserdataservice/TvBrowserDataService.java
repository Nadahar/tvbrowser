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


import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.net.URL;
import java.net.MalformedURLException;


import devplugin.*;
import devplugin.Channel;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.Version;


import tvbrowserdataservice.file.*;
import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.io.DownloadManager;
import util.io.IOUtilities;


/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class TvBrowserDataService extends devplugin.AbstractTvDataService {

  private static java.util.logging.Logger mLog
          = java.util.logging.Logger.getLogger(TvBrowserDataService.class.getName());

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer
          = util.ui.Localizer.getLocalizerFor(TvBrowserDataService.class);

  private static final String CHANNEL_GROUPS_FILENAME = "groups.txt";
  private static final String CHANNEL_GROUPS_URL = "http://tvbrowser.org/listings/" + CHANNEL_GROUPS_FILENAME;

  private DownloadManager mDownloadManager;
  private TvDataUpdateManager mTvDataBase;
  private int mTotalDownloadJobCount;
  private ProgressMonitor mProgressMonitor;

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


  private HashSet mAvailableChannelGroupsSet;

  private Properties mSettings;

  private File mDataDir;

  private TvDataLevel[] mSubscribedLevelArr;

  private static TvBrowserDataService mInstance;


  public TvBrowserDataService() {
    mSettings = new Properties();
    mInstance=this;
  }


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
    Iterator it=mAvailableChannelGroupsSet.iterator();
    while (it.hasNext()) {
      ((ChannelGroup)it.next()).setWorkingDirectory(dataDir);
    }
  }

  public File getWorkingDirectory() {
    return mDataDir;
  }


  private ChannelGroup getChannelGroupById(String id) {
    Iterator it=mAvailableChannelGroupsSet.iterator();
    while (it.hasNext()) {
      ChannelGroup group=(ChannelGroup)it.next();
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
  public void updateTvData(TvDataUpdateManager dataBase, Channel[] channelArr,
                           Date startDate, int dateCount, ProgressMonitor monitor) {

    mTvDataBase=dataBase;
    mProgressMonitor = monitor;

    HashSet groups=new HashSet();
    for (int i=0;i<channelArr.length;i++) {
      ChannelGroup curGroup=getChannelGroupById(channelArr[i].getGroup().getId());
      if (curGroup==null) {
        mLog.warning("Invalid channel group id: "+channelArr[i].getGroup().getId());
        continue;
      }

      if (!groups.contains(curGroup)) {
        groups.add(curGroup);
        curGroup.resetDirectlyLoadedBytes();
        mProgressMonitor.setMessage(mLocalizer.msg("info.8","Finding mirror for group {0} ...",curGroup.getName()));

        try {
          curGroup.chooseMirrors();
        }catch(TvBrowserException e) {
          ErrorHandler.handle(e);
        }
      }

      curGroup.addChannel(channelArr[i]);

    }


    // Delete outdated files
    deleteOutdatedFiles();

    mProgressMonitor.setMessage(mLocalizer.msg("info.7","Preparing download..."));

    // Create a download manager and add all the jobs
    mDownloadManager = new DownloadManager();
    TvDataBaseUpdater updater = new TvDataBaseUpdater(this, dataBase);

    // Add a receive or a update job for each channel and day
    DayProgramReceiveDH receiveDH = new DayProgramReceiveDH(this, updater);
    DayProgramUpdateDH updateDH   = new DayProgramUpdateDH(this, updater);


    monitor.setMaximum(groups.size());

    Iterator groupIt=groups.iterator();
    int i=0;
    while (groupIt.hasNext()) {

      monitor.setValue(i++);

      ChannelGroup group=(ChannelGroup)groupIt.next();
      SummaryFile summaryFile = group.getSummary();
      if (summaryFile != null) {
        Date date = startDate;
        for (int day = 0; day < dateCount; day++) {
          for (int levelIdx = 0; levelIdx < mSubscribedLevelArr.length; levelIdx++) {
            String level = mSubscribedLevelArr[levelIdx].getId();
            Iterator it=group.getChannels();
            while (it.hasNext()) {
              Channel ch=(Channel)it.next();
              addDownloadJob(dataBase, group.getMirror(), date, level, ch,
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
      // Update the programs for which the update suceed in every case
      mProgressMonitor.setMessage(mLocalizer.msg("info.2","Updating database"));
      updater.updateTvDataBase(monitor);
      mProgressMonitor.setMessage("");

      // Clean up ressources
      mDownloadManager = null;
      mTvDataBase = null;
      mProgressMonitor = null;
    }

  }


  private void deleteOutdatedFiles() {
    // Delete all day programs older than 3 days
    Date deadlineDay = new Date().addDays(- 3);

    File[] fileArr = mDataDir.listFiles();
    for (int i = 0; i < fileArr.length; i++) {
      String fileName = fileArr[i].getName();
      if (fileName.endsWith(".prog.gz")) {
        try {
          int year = Integer.parseInt(fileName.substring(0, 4));
          int month = Integer.parseInt(fileName.substring(5, 7));
          int day = Integer.parseInt(fileName.substring(8, 10));
          Date date = new Date(year, month, day);

          // Is this day program older than the deadline day?
          if (date.compareTo(deadlineDay) < 0) {
            // It is -> delete the file
            fileArr[i].delete();
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
//      // dont' throw an exception; try to download the file again
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
    if (mTvDataBase.cancelDownload()) {
      mDownloadManager.removeAllDownloadJobs();
    }

    // Update the ProgressMonitor
    int jobCount = mDownloadManager.getDownloadJobCount();
    mProgressMonitor.setValue(mTotalDownloadJobCount - jobCount);
  }


  boolean isDayProgramInDataBase(Date date, Channel channel) {
    return mTvDataBase.isDayProgramAvailable(date, channel);
  }

  File getDataDir() {
    return mDataDir;
  }



  Channel getChannel(String country, String channelName) {
    devplugin.ChannelGroup[] groups = getAvailableGroups();
    for (int i=0; i<groups.length; i++) {
      Channel[] ch = getAvailableChannels(groups[i]);
      for (int k=0; k<ch.length; k++) {
        if (ch[k].getCountry().equals(country)
                && ch[k].getId().equals(channelName)) {
          return ch[k];
        }
      }
    }

    return null;
  }

  public void addGroup(ChannelGroup group) {
    mAvailableChannelGroupsSet.add(group);
  }

  public void removeGroup(ChannelGroup group) {
    mAvailableChannelGroupsSet.remove(group);
  }

  /**
   * Called by the host-application during start-up. Implement this method to
   * load your dataservices settings from the file system.
   */
  public void loadSettings(Properties settings) {
    mSettings = settings;

    /* Load data level settings */
    String tvDataLevel=settings.getProperty("level");
    if (tvDataLevel==null) {
      settings.setProperty("level","base:::more00-16:::base:::more16-00");
    }

    String[] levelIds=settings.getProperty("level").split(":::");
    ArrayList levelList=new ArrayList();
    for (int i=0;i<DayProgramFile.LEVEL_ARR.length;i++) {
      if (DayProgramFile.LEVEL_ARR[i].isRequired()) {
        levelList.add(DayProgramFile.LEVEL_ARR[i]);
      }
      else{
        for (int j=0;j<levelIds.length;j++) {
          if (levelIds[j].equals(DayProgramFile.LEVEL_ARR[i].getId())) {
            levelList.add(DayProgramFile.LEVEL_ARR[i]);
          }
        }
      }
    }

    mSubscribedLevelArr=new TvDataLevel[levelList.size()];
    for (int i=0;i<levelList.size();i++) {
      mSubscribedLevelArr[i]=(TvDataLevel)levelList.get(i);
    }


    /* load channel groups settings */

    mAvailableChannelGroupsSet =new HashSet();
    mAvailableChannelGroupsSet.addAll(getUserDefinedChannelGroupsCollection());

    setWorkingDirectory(mDataDir);

  }

  public ChannelGroup[] getUserDefinedChannelGroups() {
    Collection col1 = getUserDefinedChannelGroupsCollection();
    Collection col2 = getServerDefinedChannelGroupsCollection();

    ArrayList list = new ArrayList(col1);
    list.removeAll(col2);

    return (ChannelGroup[])list.toArray(new ChannelGroup[list.size()]);
  }

  private Collection getUserDefinedChannelGroupsCollection() {
    HashSet result = new HashSet();
    String groupNames = mSettings.getProperty("groupname");
    String[] groupNamesArr;

    if (groupNames!=null) {
      groupNamesArr=groupNames.split(":");
      for (int i=0;i<groupNamesArr.length;i++) {
        if (groupNamesArr[i].trim().length()>0) {
          String groupUrls=mSettings.getProperty("group_"+groupNamesArr[i],"");
          String[] groupUrlArr=groupUrls.split(";");
          result.add(new ChannelGroup(this, groupNamesArr[i],groupUrlArr,mSettings));
        }
      }
    }

    return result;
  }

  private Collection getServerDefinedChannelGroupsCollection() {
    BufferedReader in;
    ArrayList list = new ArrayList();
    try {
        in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(mDataDir, CHANNEL_GROUPS_FILENAME)), "utf-8"));
        String line = in.readLine();
        while (line != null) {
          String[] s = line.split(";");
          if (s.length>=5) {
            String id = s[0];
            String name = s[1];
            String providername = s[2];
            String description = s[3];
            String url = s[4];
            ChannelGroup group = new ChannelGroup(this, id, name, description, providername, new String[]{url}, mSettings);
            group.setWorkingDirectory(mDataDir);
            list.add(group);
          }
          line = in.readLine();
        }
      } catch (IOException e) {
        mLog.log(Level.SEVERE, "Could not read group list "+CHANNEL_GROUPS_FILENAME, e);
      }
  return list;

  }


  /**
   * Called by the host-application during shut-down. Implements this method to
   * store your dataservices settings to the file system.
   */
  public Properties storeSettings() {
    return mSettings;
  }

  public boolean hasSettingsPanel() {
    return true;
  }



  public SettingsPanel getSettingsPanel() {
    return TvBrowserDataServiceSettingsPanel.getInstance(mSettings);
  }


  public devplugin.ChannelGroup[] getAvailableGroups() {

    Collection serverDefinedGroups = getServerDefinedChannelGroupsCollection();
   
    mAvailableChannelGroupsSet.clear();
    mAvailableChannelGroupsSet.addAll(serverDefinedGroups);
    mAvailableChannelGroupsSet.addAll(getUserDefinedChannelGroupsCollection());

    for (int i=0;i<DEFAULT_CHANNEL_GROUP_NAMES.length;i++) {
      ChannelGroup g =new ChannelGroup(this, DEFAULT_CHANNEL_GROUP_NAMES[i], DEFAULT_CHANNEL_GROUP_MIRRORS[i], mSettings);
      if (!mAvailableChannelGroupsSet.contains(g)) {
        mAvailableChannelGroupsSet.add(g);
      }
    }

    return (ChannelGroup[])mAvailableChannelGroupsSet.toArray(new ChannelGroup[mAvailableChannelGroupsSet.size()]);


  }

  /**
   * Gets the list of the channels that are available by this data service.
   */
  public Channel[] getAvailableChannels(devplugin.ChannelGroup g) {

    ChannelGroup group = getChannelGroupById(g.getId());
    return group.getAvailableChannels();
  }


  private void downloadChannelGroupFile() throws TvBrowserException {

    try {
      IOUtilities.download(new URL(CHANNEL_GROUPS_URL), new File(mDataDir, CHANNEL_GROUPS_FILENAME));
    } catch (MalformedURLException e) {
      throw new TvBrowserException(TvBrowserDataService.class, "invalidURL", "Invalid URL: {0}", CHANNEL_GROUPS_URL, e);
    } catch (IOException e) {
      throw new TvBrowserException(TvBrowserDataService.class, "downloadGroupFileFailed","Could not download group file {0}", CHANNEL_GROUPS_URL, e);
    }

  }

  public devplugin.ChannelGroup[] checkForAvailableChannelGroups(ProgressMonitor monitor) throws TvBrowserException {
    downloadChannelGroupFile();
    return getAvailableGroups();
  }

  public Channel[] checkForAvailableChannels(devplugin.ChannelGroup g, ProgressMonitor monitor) throws TvBrowserException {

    ChannelGroup group = getChannelGroupById(g.getId());
    if (group == null) {
      mLog.warning("Unknown group: "+g.getId());
      return new Channel[]{};
    }

    ArrayList channelList=new ArrayList();
    monitor.setMessage(mLocalizer.msg("checkingForAvailableChannels","Checking for froup {0}", g.getName()));
    Channel[] ch=group.checkForAvailableChannels(null);
    for (int j=0;j<ch.length;j++) {
      channelList.add(ch[j]);
    }

    Channel[] result=new Channel[channelList.size()];
    channelList.toArray(result);
    return result;
  }

  public boolean supportsDynamicChannelList() {
    return true;
  }

  public boolean supportsDynamicChannelGroups() {
    return true;
  }



  /**
   * Gets information about this TvDataService
   */
  public PluginInfo getInfo() {
    return new devplugin.PluginInfo(
            "TV-Browser",
            mLocalizer.msg("description", "Die eigenen TV-Daten des TV-Browser-Projektes"),
            "Til Schneider, www.murfman.de",
            new Version(0, 2),
            mLocalizer.msg("license",""));
  }

}