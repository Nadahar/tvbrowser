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
 */
package tvbrowserdataservice;

import java.io.File;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import java.util.Properties;


import devplugin.*;
import devplugin.Channel;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.Version;

import tvbrowserdataservice.file.*;
import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import util.io.DownloadManager;


import util.tvdataservice.AbstractTvDataService;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class TvBrowserDataService extends AbstractTvDataService {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(TvBrowserDataService.class.getName());
    
  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(TvBrowserDataService.class);

  
  private DownloadManager mDownloadManager;
  private TvDataBaseUpdater mUpdater;
  private TvDataUpdateManager mTvDataBase;
  private int mTotalDownloadJobCount;
  private ProgressMonitor mProgressMonitor;


  private static final String[][] DEFAULT_CHANNEL_GROUP_MIRRORS = new String[][] {
    new String[] {"http://tvbrowser.dyndns.tv", "http://tvbrowser.waidi.net", "http://tvbrowser.powered-by-hetzner.de","http://tvbrowser.wannawork.de","http://www.watchersnet.de/tv-browser"},
    new String[] {"http://tvbrowser.dyndns.tv", "http://tvbrowser.waidi.net", "http://tvbrowser.powered-by-hetzner.de","http://tvbrowser.wannawork.de","http://www.watchersnet.de/tv-browser"},
    new String[] {"http://tvbrowser.dyndns.tv", "http://tvbrowser.waidi.net", "http://tvbrowser.powered-by-hetzner.de","http://tvbrowser.wannawork.de","http://www.watchersnet.de/tv-browser"},
    new String[] {"http://tvbrowser.dyndns.tv", "http://tvbrowser.waidi.net", "http://tvbrowser.powered-by-hetzner.de","http://tvbrowser.wannawork.de","http://www.watchersnet.de/tv-browser"},
    new String[] {"http://tvbrowser.dyndns.tv", "http://tvbrowser.waidi.net", "http://tvbrowser.powered-by-hetzner.de","http://tvbrowser.wannawork.de","http://www.watchersnet.de/tv-browser"},
 //   new String[] {"http://tvbrowser.dyndns.tv", "http://tvbrowser.waidi.net", "http://tvbrowser.powered-by-hetzner.de","http://tvbrowser.wannawork.de","http://www.watchersnet.de/tv-browser"},   
  };
  
  private static final String[] DEFAULT_CHANNEL_GROUP_NAMES = new String[] {
    "digital",
    "austria",
    "main",
    "local",
    "others",
 //   "radio"  
  };
  
  
  private ChannelGroup[] mChannelGroupArr;

  private Properties mSettings;
  
  private File mDataDir;
  
  private Channel[] mAvailableChannelArr;
  
  private String[] mSubsribedLevelArr;
  private TvDataLevel[] mSubscribedLevelArr;
  
  private int mDirectlyLoadedBytes;  
  
  private static TvBrowserDataService mInstance;
  
  public Version getAPIVersion() {
    return new Version(1,0); 
  }
  
  public TvBrowserDataService() {
    mSettings = new Properties();
    mInstance=this;
    /*
    if (DEFAULT_CHANNEL_GROUP_MIRRORS.length!=DEFAULT_CHANNEL_GROUP_NAMES.length) {
      throw new RuntimeException("invalid group names or group mirrors");
    }
    
    mChannelGroupArr=new ChannelGroup[DEFAULT_CHANNEL_GROUP_NAMES.length];
    for (int i=0;i<DEFAULT_CHANNEL_GROUP_NAMES.length;i++) {
      mChannelGroupArr[i]=new ChannelGroup(this, DEFAULT_CHANNEL_GROUP_NAMES[i], DEFAULT_CHANNEL_GROUP_MIRRORS[i]);
    }
    */
  }
  
  
  public static TvBrowserDataService getInstance() {
    if (mInstance==null) {
      throw new RuntimeException("no instance of TvBrowserDataService class available");
    }
    return mInstance;
  }

  public void setWorkingDirectory(File dataDir) {
    mDataDir=dataDir;
    //for (int i=0;i<DEFAULT_CHANNEL_GROUP_NAMES.length;i++) {
    if (mChannelGroupArr!=null) {
      for (int i=0;i<mChannelGroupArr.length;i++) {
        mChannelGroupArr[i].setWorkingDirectory(dataDir);
      }
    } 
  }

  public ChannelGroup[] getChannelGroups() {
    
    return mChannelGroupArr; 
  }
  
  public void setChannelGroups(ChannelGroup[] groups) {
    mChannelGroupArr=groups;
  }


  /**
   * Updates the TV data provided by this data service.
   * 
   * @throws TvBrowserException
   */  
  
  public void updateTvData(TvDataUpdateManager dataBase, Channel[] channelArr,
    Date startDate, int dateCount, ProgressMonitor monitor)
    throws TvBrowserException
  {    
    
    mTvDataBase=dataBase;    
    mProgressMonitor = monitor;
  
    mProgressMonitor.setMessage(mLocalizer.msg("info.7","Preparing download..."));
    
    ArrayList[] channelList=new ArrayList[mChannelGroupArr.length];
    for (int i=0;i<channelList.length;i++) {
      mChannelGroupArr[i].resetDirectlyLoadedBytes();
      channelList[i]=new ArrayList();
    }
    
    // Delete outdated files
    deleteOutdatedFiles();
    
    for (int i=0;i<channelArr.length;i++) {
      String groupName=channelArr[i].getGroup().getId();
      for (int j=0;j<mChannelGroupArr.length;j++) {       
        if (mChannelGroupArr[j].isGroupMember(channelArr[i])) {       
          channelList[j].add(channelArr[i]);
        }        
      }       
    }
    
    
    for (int i=0;i<mChannelGroupArr.length;i++) {
      mProgressMonitor.setMessage(mLocalizer.msg("info.8","Finding mirror for group {0} ({1} of {2})...",mChannelGroupArr[i].getName(),""+i,""+mChannelGroupArr.length));
      mChannelGroupArr[i].chooseMirrors();
    }
    
    mProgressMonitor.setMessage(mLocalizer.msg("info.7","Preparing download..."));
    
    
    //  Create a download manager and add all the jobs
    mDownloadManager = new DownloadManager();
    mUpdater = new TvDataBaseUpdater(this, dataBase);
    
    // Add a receive or a update job for each channel and day
    DayProgramReceiveDH receiveDH = new DayProgramReceiveDH(this, mUpdater);
    DayProgramUpdateDH updateDH   = new DayProgramUpdateDH(this, mUpdater);
   
    for (int i=0;i<mChannelGroupArr.length;i++) {
      
      Date date = startDate;
      for (int day = 0; day < dateCount; day++) {

        for (int levelIdx = 0; levelIdx < mSubscribedLevelArr.length; levelIdx++) {
          
          String level = mSubscribedLevelArr[levelIdx].getId();
          Iterator it=channelList[i].iterator();
          while (it.hasNext()) {
            Channel ch=(Channel)it.next();
            addDownloadJob(dataBase, mChannelGroupArr[i].getMirror(), date, level, ch.getId(),
                                   ch.getCountry(), receiveDH, updateDH, mChannelGroupArr[i].getSummary());
            }
          }
          date = date.addDays(1);
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
        mProgressMonitor.setMessage(mLocalizer.msg("info.2","Updating tv data base"));
        mUpdater.updateTvDataBase();
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
        catch (Exception exc) {}
      }
    }
  }
    
  private void addDownloadJob(TvDataUpdateManager dataBase, Mirror mirror, Date date,
          String level, String channelName, String country,
          DayProgramReceiveDH receiveDH, DayProgramUpdateDH updateDH,
          SummaryFile summary)
          throws TvBrowserException
  {
  // NOTE: summary is null when getting failed
      
  String completeFileName = DayProgramFile.getProgramFileName(date,
            country, channelName, level);
  File completeFile = new File(mDataDir, completeFileName);
      
  int levelIdx = DayProgramFile.getLevelIndexForId(level);
    
  // Check whether we already have data for this day
  if (completeFile.exists()) {
    // We have data -> Check whether the mirror has an update
              
    // Get the version of the file
    int localVersion;
    try {
      localVersion = DayProgramFile.readVersionFromFile(completeFile);
    }
    catch (Exception exc) {
      throw new TvBrowserException(getClass(), "error.5",
                "Reading version of TV data file failed: {0}",
                completeFile.getAbsolutePath(), exc);
      }
      
      // Check whether the mirror has a newer version
      boolean needsUpdate = true;
      if (summary != null) {
        int mirrorVersion = summary.getDayProgramVersion(date, country,
                channelName, levelIdx);
        needsUpdate = (mirrorVersion > localVersion);
      }

      if (needsUpdate) {
        // We need an update -> Add an update job
        String updateFileName = DayProgramFile.getProgramFileName(date,
                country, channelName, level, localVersion);
        mDownloadManager.addDownloadJob(mirror.getUrl(),updateFileName, updateDH);
      }
    } else {
      // We have no data -> Check whether the mirror has
      boolean needsUpdate = true;
      if (summary != null) {
        int mirrorVersion = summary.getDayProgramVersion(date, country,
                channelName, levelIdx);
        needsUpdate = (mirrorVersion != -1);
      }
       
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
    Channel[] channelArr = getAvailableChannels();
    
    for (int i = 0; i < channelArr.length; i++) {
      Channel channel = channelArr[i];
      if (channel.getCountry().equals(country)
        && channel.getId().equals(channelName))
      {
        return channel;
      }
    }
    
    return null;
  }
  
  public void addGroup(ChannelGroup group) {
    
    HashSet newChannelGroups=new HashSet();
    for (int i=0;i<mChannelGroupArr.length;i++) {
      newChannelGroups.add(mChannelGroupArr[i]);
    }  
    newChannelGroups.add(group);
    mChannelGroupArr=new ChannelGroup[newChannelGroups.size()];
    newChannelGroups.toArray(mChannelGroupArr);    
  }
  
  public void removeGroup(ChannelGroup group) {
    
     HashSet newChannelGroups=new HashSet();
     for (int i=0;i<mChannelGroupArr.length;i++) {
       if (!group.equals(mChannelGroupArr[i])) {
         newChannelGroups.add(mChannelGroupArr[i]);
       }else {
         System.out.println("group "+group+" removed"); 
       }
     }  
     mChannelGroupArr=new ChannelGroup[newChannelGroups.size()];
     newChannelGroups.toArray(mChannelGroupArr);  
     
     
     for (int i=0;i<mChannelGroupArr.length;i++) {
       System.out.println("group: "+mChannelGroupArr[i]); 
     }  
    
  }
  
  public ChannelGroup[] getDefaultGroups() {
    if (DEFAULT_CHANNEL_GROUP_MIRRORS.length!=DEFAULT_CHANNEL_GROUP_NAMES.length) {
      throw new RuntimeException("invalid group names or group mirrors");
    }
    
    ChannelGroup[] result=new ChannelGroup[DEFAULT_CHANNEL_GROUP_NAMES.length];
    for (int i=0;i<DEFAULT_CHANNEL_GROUP_NAMES.length;i++) {
      result[i]=new ChannelGroup(this, DEFAULT_CHANNEL_GROUP_NAMES[i], DEFAULT_CHANNEL_GROUP_MIRRORS[i]);
    }
    return result;
    
  }
  
/*
  public void addChannelGroupByURL(String url, ProgressMonitor monitor) throws TvBrowserException {
    
    // create the new channel group 
    int pos=url.lastIndexOf('/');
    String groupId=url.substring(pos+1,url.length());
    
    String groupUrl=url.substring(0,pos);
    
    System.out.println(groupId);
    System.out.println(groupUrl);
    
    ChannelGroup group=new ChannelGroup(this, groupId, new String[]{groupUrl});
    
    // read the channellist for the new group 
    group.checkForAvailableChannels(monitor);
    
    // add the group to the group list
    HashSet newChannelGroups=new HashSet();
    for (int i=0;i<mChannelGroupArr.length;i++) {
      newChannelGroups.add(mChannelGroupArr[i]);
    }  
		newChannelGroups.add(group);
    mChannelGroupArr=new ChannelGroup[newChannelGroups.size()];
    newChannelGroups.toArray(mChannelGroupArr);
    
  }

*/
  /**
   * Called by the host-application during start-up. Implements this method to
   * load your dataservices settings from the file system.
   */
  public void loadSettings(Properties settings) {
    mSettings = settings;
    
    /* Load data level settings */
    String tvDataLevel=settings.getProperty("level");
    if (tvDataLevel==null) {
      settings.setProperty("level","base:::more16-00");
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
    
    String groupNames=settings.getProperty("groupname");
    String[] groupNamesArr;
    if (groupNames==null) {
      groupNamesArr=DEFAULT_CHANNEL_GROUP_NAMES;
    }else{
      groupNamesArr=groupNames.split(":");
    }
    
    mChannelGroupArr=new ChannelGroup[groupNamesArr.length];
    for (int i=0;i<groupNamesArr.length;i++) {
      String[] groupUrlArr;
      if (groupNames==null) {
        groupUrlArr=DEFAULT_CHANNEL_GROUP_MIRRORS[i];
      }
      else {
        String groupUrls=settings.getProperty("group_"+groupNamesArr[i],"");
        groupUrlArr=groupUrls.split(";");
      }
      mChannelGroupArr[i]=new ChannelGroup(this, groupNamesArr[i],groupUrlArr);
      mChannelGroupArr[i].setWorkingDirectory(mDataDir);
    }
    
     
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


  /**
   * Gets the list of the channels that are available by this data service.
   */
  public Channel[] getAvailableChannels() {
    
    ArrayList channelList=new ArrayList();
    for (int i=0;i<mChannelGroupArr.length;i++) {
      Channel[] ch=mChannelGroupArr[i].getAvailableChannels();
      for (int j=0;j<ch.length;j++) {
        channelList.add(ch[j]);  
      }    
    }
    
    Channel[] result=new Channel[channelList.size()];
    channelList.toArray(result);
    return result;
  }
  
  
  public Channel[] checkForAvailableChannels(ProgressMonitor monitor) throws TvBrowserException {
    
    ArrayList channelList=new ArrayList();
    for (int i=0;i<mChannelGroupArr.length;i++) {
      
      monitor.setMessage(mLocalizer.msg("checkingForAvailableChannels","Checking group {0} - ({1} of {2})",mChannelGroupArr[i].getName(),""+i,""+mChannelGroupArr.length));
      Channel[] ch=mChannelGroupArr[i].checkForAvailableChannels(null);
      for (int j=0;j<ch.length;j++) {
        channelList.add(ch[j]);  
      }    
    }
    
    Channel[] result=new Channel[channelList.size()];
    channelList.toArray(result);
    return result;
  }
  
  public boolean supportsDynamicChannelList() {
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
      new Version(0, 1),
      mLocalizer.msg("license",""));
  }

}
