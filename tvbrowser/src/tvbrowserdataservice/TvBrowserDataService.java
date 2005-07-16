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
import javax.swing.JOptionPane;


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

import util.tvdataservice.AbstractTvDataService;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;

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

  private Channel[] mChannelsTempArr;

  private static final String[][] DEFAULT_CHANNEL_GROUP_MIRRORS = new String[][] {
    new String[] {"http://www.tvbrowser.org/mirrorlists"},
    new String[] {"http://www.tvbrowser.org/mirrorlists"},
    new String[] {"http://www.tvbrowser.org/mirrorlists"},
    new String[] {"http://www.tvbrowser.org/mirrorlists"},
    new String[] {"http://www.tvbrowser.org/mirrorlists"},
    new String[] {"http://www.tvbrowser.org/mirrorlists"},   
  };
  
  private static final String[] DEFAULT_CHANNEL_GROUP_NAMES = new String[] {
    "digital",
    "austria",
    "main",
    "local",
    "others",
    "radio"  
  };
  
  
  private HashSet mChannelGroupSet;

  private Properties mSettings;
  
  private File mDataDir;
  
  private TvDataLevel[] mSubscribedLevelArr;
  
  private static TvBrowserDataService mInstance;
  
  public Version getAPIVersion() {
    return new Version(1,0); 
  }
  
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
    if (mChannelGroupSet==null) {
      return;
    }
    Iterator it=mChannelGroupSet.iterator();
    while (it.hasNext()) {
      ((ChannelGroup)it.next()).setWorkingDirectory(dataDir);
    }    
  }
  
  public File getWorkingDirectory() {
    return mDataDir;
  }

  public ChannelGroup[] getChannelGroups() {
    ChannelGroup[] result=new ChannelGroup[mChannelGroupSet.size()];
    mChannelGroupSet.toArray(result);
    return result;    
  }
  
  public void setChannelGroups(ChannelGroup[] groups) {
    mChannelGroupSet=new HashSet();
    for (int i=0;i<groups.length;i++) {
      mChannelGroupSet.add(groups[i]);  
    }
  }

  private ChannelGroup getChannelGroupById(String id) {
    Iterator it=mChannelGroupSet.iterator();
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
   * @throws TvBrowserException
   */  
  
  public void updateTvData(TvDataUpdateManager dataBase, Channel[] channelArr,
    Date startDate, int dateCount, ProgressMonitor monitor)
    throws TvBrowserException
  {    
    
    mTvDataBase=dataBase;    
    mProgressMonitor = monitor;
    
    HashSet groups=new HashSet();
    for (int i=0;i<channelArr.length;i++) {
      ChannelGroup curGroup=getChannelGroupById(channelArr[i].getGroup().getId());
      if (curGroup==null) {
        mLog.warning("Invalid channel group id: "+channelArr[i].getGroup().getId());
      }
      
      if (!groups.contains(curGroup)) {
        groups.add(curGroup);
        curGroup.resetDirectlyLoadedBytes();
        mProgressMonitor.setMessage(mLocalizer.msg("info.8","Finding mirror for group {0} ...",curGroup.getName()));
        curGroup.chooseMirrors(); 
      }
      
      curGroup.addChannel(channelArr[i]);
             
    }
    
   
    // Delete outdated files
    deleteOutdatedFiles();
    
    mProgressMonitor.setMessage(mLocalizer.msg("info.7","Preparing download..."));
   
    //  Create a download manager and add all the jobs
    mDownloadManager = new DownloadManager();
    mUpdater = new TvDataBaseUpdater(this, dataBase);
    
    // Add a receive or a update job for each channel and day
    DayProgramReceiveDH receiveDH = new DayProgramReceiveDH(this, mUpdater);
    DayProgramUpdateDH updateDH   = new DayProgramUpdateDH(this, mUpdater);
   
   
    monitor.setMaximum(groups.size());
   
    Iterator groupIt=groups.iterator();
    int i=0;
    while (groupIt.hasNext()) {
      
      monitor.setValue(i++);
      
      ChannelGroup group=(ChannelGroup)groupIt.next();
      Date date = startDate;
      for (int day = 0; day < dateCount; day++) {
        for (int levelIdx = 0; levelIdx < mSubscribedLevelArr.length; levelIdx++) {
          String level = mSubscribedLevelArr[levelIdx].getId();
          Iterator it=group.getChannels();
          while (it.hasNext()) {
            Channel ch=(Channel)it.next();
            addDownloadJob(dataBase, group.getMirror(), date, level, ch,
                ch.getCountry(), receiveDH, updateDH, group.getSummary());
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
        mProgressMonitor.setMessage(mLocalizer.msg("info.2","Updating database"));
        mUpdater.updateTvDataBase(monitor);
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
          String level, Channel channel, String country,
          DayProgramReceiveDH receiveDH, DayProgramUpdateDH updateDH,
          SummaryFile summary)
          throws TvBrowserException
  {
  // NOTE: summary is null when getting failed
  String completeFileName = DayProgramFile.getProgramFileName(date,
            country, channel.getId(), level);
  File completeFile = new File(mDataDir, completeFileName);
      
  int levelIdx = DayProgramFile.getLevelIndexForId(level);
    
  // Check whether we already have data for this day
  if (dataBase.isDayProgramAvailable(date, channel) && completeFile.exists()) {
    // We have data -> Check whether the mirror has an update
              
    // Get the version of the file
    int localVersion;
    try {
      localVersion = DayProgramFile.readVersionFromFile(completeFile);
    }
    catch (Exception exc) {
      // TODO: dont' throw an exception; try to download the file again
      throw new TvBrowserException(getClass(), "error.5",
                "Reading version of TV data file failed: {0}",
                completeFile.getAbsolutePath(), exc);
      }
      
      // Check whether the mirror has a newer version
      boolean needsUpdate = true;
      if (summary != null) {
        int mirrorVersion = summary.getDayProgramVersion(date, country,
                channel.getId(), levelIdx);
        needsUpdate = (mirrorVersion > localVersion);
      }

      if (needsUpdate) {
        // We need an update -> Add an update job
        String updateFileName = DayProgramFile.getProgramFileName(date,
                country, channel.getId(), level, localVersion);
        mDownloadManager.addDownloadJob(mirror.getUrl(),updateFileName, updateDH);
      }
    } else {
      // We have no data -> Check whether the mirror has
      boolean needsUpdate = true;
      if (summary != null) {
        int mirrorVersion = summary.getDayProgramVersion(date, country,
                channel.getId(), levelIdx);
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
    mChannelGroupSet.add(group);
  }
  
  public void removeGroup(ChannelGroup group) {
    mChannelGroupSet.remove(group);
  }
  
  public ChannelGroup[] getDefaultGroups() {
    if (DEFAULT_CHANNEL_GROUP_MIRRORS.length!=DEFAULT_CHANNEL_GROUP_NAMES.length) {
      throw new RuntimeException("invalid group names or group mirrors");
    }
    
    ChannelGroup[] result=new ChannelGroup[DEFAULT_CHANNEL_GROUP_NAMES.length];
    for (int i=0;i<DEFAULT_CHANNEL_GROUP_NAMES.length;i++) {
      result[i]=new ChannelGroup(this, DEFAULT_CHANNEL_GROUP_NAMES[i], DEFAULT_CHANNEL_GROUP_MIRRORS[i], mSettings);
    }
    return result;
    
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
    
    mChannelGroupSet=new HashSet();
    
    String groupNames = settings.getProperty("groupname");
    String[] groupNamesArr;
    
    if (groupNames==null) {
      groupNamesArr=DEFAULT_CHANNEL_GROUP_NAMES;      
      for (int i=0;i<groupNamesArr.length;i++) {
        String[] groupUrlArr=DEFAULT_CHANNEL_GROUP_MIRRORS[i];
        mChannelGroupSet.add(new ChannelGroup(this, groupNamesArr[i],groupUrlArr, mSettings));
      }
    }
    else {
      groupNamesArr=groupNames.split(":");
      for (int i=0;i<groupNamesArr.length;i++) {
        String groupUrls=settings.getProperty("group_"+groupNamesArr[i],"");
        String[] groupUrlArr=groupUrls.split(";");
        mChannelGroupSet.add(new ChannelGroup(this, groupNamesArr[i],groupUrlArr,mSettings));
      }
      
    }
    
    setWorkingDirectory(mDataDir);
     
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
    Iterator it=mChannelGroupSet.iterator();
    while (it.hasNext()) {
      ChannelGroup group=(ChannelGroup)it.next();
      Channel[] ch=group.getAvailableChannels();
      for (int j=0;j<ch.length;j++) {
        channelList.add(ch[j]);  
      }       
    }
    
    /* If we could not find any channel we check, if there is a channel file
     * from a previous tvbrowser version. If the file exists, we ask the user
     * to load the new channel files from the internet.
     */
    
    if (channelList.size()==0) {
      
      File file = new File(mDataDir, ChannelList.FILE_NAME);
      if (file.exists()) {
      
         Object[] options = {"Jetzt herunterladen (empfohlen)",
                    "Sp\u00E4ter neu einrichten"};
        int n = JOptionPane.showOptionDialog(null,
           "Es befindet sich keine aktuelle Senderliste auf dem System.\n" +
           "Senderlisten werden ben\u00F6tigt, um TV-Browser mitzuteilen,\nwelche Sender zur Verf\u00FCgung stehen.\n\n" +
           "Soll nun eine aktuelle Senderliste heruntergeladen werden? (erfordert eine Internetverbinung)",
           "Keine Senderlisten verf\u00FCgbar",
           JOptionPane.YES_NO_CANCEL_OPTION,
           JOptionPane.QUESTION_MESSAGE,
           null,
           options,
           options[0]);
      
        if (n==0) {
          final ProgressWindow win=new ProgressWindow(null);
          mChannelsTempArr=null;
          win.run(new Progress(){
            public void run() {
              try {
                mChannelsTempArr = checkForAvailableChannels(win);
              }catch (TvBrowserException exc) {
                ErrorHandler.handle(exc);
              }
            }
          });
          if (mChannelsTempArr==null) {
            return new Channel[0]; 
          }
          Channel[] result=new Channel[mChannelsTempArr.length];
          System.arraycopy(mChannelsTempArr,0,result,0,result.length);
    
          return result;
        }  
      }
    }
    
    
    Channel[] result=new Channel[channelList.size()];
    channelList.toArray(result);
      
    return result;
    
  }
  
  
  public Channel[] checkForAvailableChannels(ProgressMonitor monitor) throws TvBrowserException {
    
    ArrayList channelList=new ArrayList();
    Iterator it=mChannelGroupSet.iterator();
    int i=0;
    while (it.hasNext()) {
      i++;
      ChannelGroup group=(ChannelGroup)it.next();
      monitor.setMessage(mLocalizer.msg("checkingForAvailableChannels","Checking group {0} - ({1} of {2})",group.getName(),""+i,""+mChannelGroupSet.size()));
      Channel[] ch=group.checkForAvailableChannels(null);
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
      new Version(0, 2),
      mLocalizer.msg("license",""));
  }

}