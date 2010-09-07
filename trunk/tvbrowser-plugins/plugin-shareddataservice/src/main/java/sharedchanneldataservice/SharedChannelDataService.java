package sharedchanneldataservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import devplugin.AbstractTvDataService;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgressMonitor;
import devplugin.Version;

public class SharedChannelDataService extends AbstractTvDataService{

  private static final Logger mLog = java.util.logging.Logger.getLogger(SharedChannelDataService.class.getName());
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SharedChannelDataService.class);

  public final ChannelGroup mSharedChannelDataChannelGroup = new devplugin.ChannelGroupImpl("SharedChannelData", "sharedchanneldata", "Shared Channel", mLocalizer.msg("name", "Shared Channel Data"));

  private static SharedChannelDataService mInstance;
  private SharedChannelDataServiceData data;
  private boolean isAutoUpdate = false;
  public Properties [] channelDefinitions;
  public File mDataDir;
  public String mixedChannelsDirName;
  public Properties mProp = new Properties();
  private HashMap <String, MutableChannelDayProgram> sharedSourcesUpdate;


  public SharedChannelDataService()
  {
    mInstance = this;
    data = new SharedChannelDataServiceData(this);
    mProp.setProperty("cutInfoIndex", "0");
  }

  public static SharedChannelDataService getInstance(){
    if (mInstance == null) {
      throw new RuntimeException("no instance of SharedChannelPlugin class available");
    }
    return mInstance;
  }


  /**
   * Gets information about this TvDataService
   * @return Plugin Info containing the class object of this dataService and
   * name, description and author information of this data service to be displayed  in the settings panel.
   */
  public PluginInfo getInfo() {
    return new PluginInfo(SharedChannelDataService.class,
        mLocalizer.msg("name", "Shared Channel Data"),
        mLocalizer.msg("desc", "This plugin creates a virtual channel for broadcasters sharing one frequenzy."),
    "jb");

  }

  /**
   * Gets the Version of the implemented API
   * Since TV-Browser 0.9.7 getAPIVersion must return 1.0 
   */
  public static Version getVersion() {
    return new Version(0, 0, 1, false, null);
  }

  /**
   * Called by the host-application during start-up. 
   * Loads settings for this data service.
   * @param p ; poperty list to handle the settings
   *
   */
  public void loadSettings(Properties p) {
    String propKey;
    for (Enumeration<?> e = p.keys(); e.hasMoreElements();) {
      propKey = e.nextElement().toString();
      this.mProp.setProperty(propKey, p.getProperty(propKey));
    }
  }

  /**
   * Called by the host-application during shut-down, 
   * to store the settings returned to the file system.
   * @return a property list with settings of this data service.
   */
  public Properties storeSettings() {
    return mProp;
  }

  @Override
  public boolean supportsAutoUpdate() {
    // time interval triggered in setting.prop with 'dataServiceAutoUpdateTime'

    File lckFile = new File (mixedChannelsDirName + "/datamix.lck");
    if (lckFile.exists()){
      return false;
    }

    isAutoUpdate = false;

    String updatesFileName = mixedChannelsDirName  + "/SharedChannelSavings.prop";
    if (getPluginManager().getActivatedPluginForId("java.sharedchannelautoupdateplugin.SharedChannelAutoUpdatePlugin")!=null) {
      File upDatesFile = new File(updatesFileName);
      if (upDatesFile.exists()) {
        try {
          Properties changedList = new Properties();
          InputStream reader = new FileInputStream(updatesFileName);
          changedList.load(reader);
          if (changedList.size()>0)   {
            String[] changedChannels = new String[changedList.size()];
            int counter = 0;
            for (Enumeration<?> ecl = changedList.keys(); ecl.hasMoreElements();) {
              changedChannels[counter] = ecl.nextElement().toString();
              counter++;
            }
            Properties channelDescription = new Properties();
            String propFileName = mixedChannelsDirName + "/sharedChannels.properties";
            if (new File(propFileName).exists()) {
              channelDescription.load(new FileInputStream(propFileName));
              Channel[]subScribedChannels = getPluginManager().getSubscribedChannels();
              counter = 0;
              while (!isAutoUpdate && counter < changedChannels.length) {
                Enumeration<?> eNum = channelDescription.keys();
                while (!isAutoUpdate && eNum.hasMoreElements()) {
                  String propKey = eNum.nextElement().toString();
                  String chnDesc = channelDescription.getProperty(propKey);
                  if (chnDesc.contains(changedChannels[counter])) {
                    for (int i=0; i<subScribedChannels.length;i++){
                      String [] subscribedId = subScribedChannels[i].getUniqueId().split("_");
                      if (subscribedId[0].equals("sharedchanneldataservice.SharedChannelDataService")&& subscribedId[3].equals(propKey)){
                        isAutoUpdate = true;
                        i= subScribedChannels.length; // no further itertion needed
                      }
                    }
                  }
                }
                counter++;
              }
              if (!isAutoUpdate){
                try{
                  changedList.clear();
                  changedList.store(new FileOutputStream(updatesFileName), "Changed DayPrograms to be checked for Update");
                } catch (IOException e) {mLog.warning("Clearing mixed channel Update list failed");
                }
              }
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return isAutoUpdate;
  }




  public boolean hasSettingsPanel() {
    return true;
  }


  public void setWorkingDirectory(File dataDir) {
    mDataDir = dataDir;
    try {
      mDataDir = mDataDir.getCanonicalFile();
    } catch (Exception ioe) {
      mLog.warning("SharedChannelDataService.setWorkingDirectory Exception: " + ioe.toString());
    }
    File iconDir = new File (mDataDir + "/icons");
    if (!iconDir.exists()){
      iconDir.mkdir();
    }
    mixedChannelsDirName =getPluginManager().getTvBrowserSettings().getTvBrowserUserHome() + "/MixedChannels"; 
    File mixedChannelsDir = new File (mixedChannelsDirName);
    if (!mixedChannelsDir.exists()){
      mixedChannelsDir.mkdir();
    }
  }

  /**
   * @return the working directory of this data service.
   */
  public File getWorkingDirectory() {
    return mDataDir;
  }


  public SharedChannelDataServicePanel getSettingsPanel()
  {
    return SharedChannelDataServicePanel.getInstance(mInstance);
  }

  public void handleTvBrowserStartFinished() {
    getAvailableChannels(null);
    File lckFile = new File (mixedChannelsDirName + "/datamix.lck");
    if (lckFile.exists()){
      lckFile.delete();
    }
  }


  /**
   * @return array of channels
   */  
  public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) {
    return getAvailableChannels(group);
  }


  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#supportsDynamicChannelGroups()
   */
  public boolean supportsDynamicChannelGroups() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#supportsDynamicChannelList()
   */
  public ChannelGroup[] checkForAvailableChannelGroups(ProgressMonitor monitor) throws TvBrowserException {
    return new ChannelGroup[]{mSharedChannelDataChannelGroup};

  }


  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#getAvailableGroups()
   */
  public ChannelGroup[] getAvailableGroups() {
    return new ChannelGroup[]{mSharedChannelDataChannelGroup};
  }



  /**
   * Gets the list of the channels that are available by this data service.
   * Reads channel list stored in file 'channels.properties'.
   * If no pre-stored channellist is available, a new one will be created by using the last nxtvepg data exported.
   * @return array of channels
   */
  /* (non Javadoc)
   * @see tvdataservice.TvDataService#getAvailableChannels()
   */
  public Channel[] getAvailableChannels(ChannelGroup group) {
    Properties channelDefinitions = new Properties();
    if (new File(mixedChannelsDirName + "/sharedChannels.properties").exists()) {
      try {
        InputStream reader = new FileInputStream(mixedChannelsDirName + "/sharedChannels.properties");
        channelDefinitions.load(reader);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (channelDefinitions.size() == 0) {
      return new Channel[0];
    } 
    String channelID;
    for (Enumeration<?> e = channelDefinitions.keys(); e.hasMoreElements();) {
      channelID = e.nextElement().toString();
      data.addChannel(channelID, channelDefinitions.getProperty(channelID).split(";"));
    }
    return data.getChannels(channelDefinitions);
  }

  public boolean supportsDynamicChannelList() {
    return true;
  }


  /**
   * Called by the host application during the update process and
   * updates the TV data provided by this data service.
   * @param updateManager ; the TvDataUpdateManager given by TV-Browser
   * @param channelArr ; a list of channels to be updated
   * @param startDate ; the day the update to start with
   * @param dateCount ; the amount of days to be updated
   * @param monitor ; the progress monitor of the main application
   * @throws TvBrowserException
   */
  public void updateTvData(TvDataUpdateManager updateManager,
      Channel[] channelArr, Date startDate, int dateCount, ProgressMonitor monitor)
  throws TvBrowserException {

    File lckFile = new File (mixedChannelsDirName + "/datamix.lck");
    if (lckFile.exists()){
      JOptionPane.showMessageDialog(null, mLocalizer.msg("msg_update_canceled_reason", "A previous update process has not finished yet!"), mLocalizer.msg("msg_update_canceled", "Shared Channel Data Update Canceled:"), JOptionPane.ERROR_MESSAGE);
      return;
    }

    String propFileName = mixedChannelsDirName  + "/SharedChannelSavings.prop";
    Properties changedList = new Properties();
    Properties channelDescription = new Properties();
    try {
      channelDescription.load(new FileInputStream(mixedChannelsDirName + "/sharedChannels.properties"));
    } catch (IOException e) {
    }

    if (getPluginManager().getActivatedPluginForId("java.sharedchannelautoupdateplugin.SharedChannelAutoUpdatePlugin")!=null) {
      File propFile = new File(propFileName);
      if (propFile.exists()) {
        try {
          InputStream reader = new FileInputStream(propFileName);
          changedList.load(reader);
          new Properties().store(new FileOutputStream(propFileName), "Changed DayPrograms to be checked for Update");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    Date [] dateArray = new Date [dateCount];
    if (isAutoUpdate) {
      ArrayList <Channel> toBeUpdated = new ArrayList <Channel>();
      String changedKey;
      String propKey;
      for (Enumeration<?> ecl = changedList.keys(); ecl.hasMoreElements();) {
        changedKey = ecl.nextElement().toString();
        for (Enumeration<?> ecd = channelDescription.keys(); ecd.hasMoreElements();) {
          propKey = ecd.nextElement().toString();
          if (channelDescription.getProperty(propKey).contains(changedKey)){
            int index = 0;
            while (index < channelArr.length){
              if (channelArr[index].getId().equals(propKey)) {
                Channel nextChannel = channelArr[index];
                if (!toBeUpdated.contains(nextChannel)) {
                  toBeUpdated.add(nextChannel);
                }
                index = channelArr.length;
              }
              index++;
            }
          }           
        }

      }
      channelArr = toBeUpdated.toArray(new Channel[toBeUpdated.size()]);

    } else {
      Date date = startDate;
      for (int day = 0; day < dateCount; day++) {
        dateArray[day] = date;
        date = date.addDays(1);
      }
    }

    if (channelArr.length>0) {
      monitor.setMaximum(channelArr.length);
      monitor.setMessage(mLocalizer.msg("msg_update", "Processing Update"));
      monitor.setValue(0);
    }
    Channel[] subScribedChannels = getPluginManager().getSubscribedChannels();
    sharedSourcesUpdate = new HashMap<String, MutableChannelDayProgram>();

    // update tv data

    for (int i = 0; i < channelArr.length; i++) {

      String descString = channelDescription.getProperty(channelArr[i].getId()).split(";",6)[5];
      String[]origDesc;
      if (descString.equals("")){
        origDesc = new String[0];
      }else {
        origDesc = descString.split(";");
      }


      if (origDesc.length>1) {
        String []alienID  = new String [origDesc.length/2];
        int []alienStart  = new int [origDesc.length/2];
        for (int j = 0; j < alienID.length;j++){
          String []time = origDesc[j*2].split(":");
          alienStart[j]=(Integer.parseInt((String)time[0])*60)+Integer.parseInt((String)time[1]);
          alienID[j]=origDesc[(j*2)+1];
        }
        if (isAutoUpdate) {
          SortedSet<String> dateSet = new TreeSet<String>();
          for (int j = 0; j < alienID.length; j++) {
            String dateString = changedList.getProperty(alienID[j]);
            if (dateString != null) {
              String[] dateStrings = dateString.split(";");
              for (int k = 0; k < dateStrings.length; k++) {
                dateSet.add(dateStrings[k]);
              }
            }
          }

          dateArray = new Date[dateSet.size()];
          Iterator<String> it = dateSet.iterator();
          int counter = 0;
          while (it.hasNext()) {
            String dayString = it.next();
            dateArray[counter] = Date.createYYYYMMDD(dayString.substring(0, 4) + "-" + dayString.substring(4, 6) + "-" + dayString.substring(6, 8), "-");
            counter++;
          }
        }
        for (int day = 0; day < dateArray.length; day++) {
          if (dateArray[day].compareTo(startDate) >= 0) {
            if (!updateDayProgram (channelArr, channelArr[i], dateArray[day], alienID, alienStart, subScribedChannels, channelDescription, updateManager)) {
              break;
            }
          }
        }
      }
      monitor.setValue(i+1);
    }

    if (isAutoUpdate){
      // reset autoUpdateFlag
      isAutoUpdate = false;
    }
    monitor.setMessage(mLocalizer.msg("msg_update_done", "Update Done."));
  }

  private boolean updateDayProgram (Channel[] channelArr, Channel channel, Date date, String[]alienID, int[]alienStart, Channel [] subScribedChannels, Properties channelDescription, TvDataUpdateManager updateManager){
    boolean noBreakFlg = true;
    // Update sources before current day program is updated
    for (int ai = 0; ai < alienID.length; ai++) {
      if (alienID[ai].startsWith("sharedchanneldataservice.SharedChannelDataService")) {
        Channel subChannel= HelperMethods.getChannelFromId(alienID[ai], subScribedChannels); 
        if (!(subChannel == null || sharedSourcesUpdate.containsKey(subChannel.getUniqueId() + date.getDateString()))) {
          for (int i = 0; i < channelArr.length; i++) {
            if (alienID[ai].equals(channelArr[i].getUniqueId())) {
              String descString = channelDescription.getProperty(channelArr[i].getId()).split(";",6)[5];
              String[]subDesc;
              if (descString.equals("")){
                subDesc = new String[0];
              }else {
                subDesc = descString.split(";");
              }
              if (subDesc.length>1) {
                String []subAlienID  = new String [subDesc.length/2];
                int []subAlienStart  = new int [subDesc.length/2];
                for (int j = 0; j < subAlienID.length;j++){
                  String []time = subDesc[j*2].split(":");
                  alienStart[j]=(Integer.parseInt((String)time[0])*60)+Integer.parseInt((String)time[1]);
                  subAlienID[j]=subDesc[(j*2)+1];
                }
                noBreakFlg = updateDayProgram (channelArr, channelArr[i], date, subAlienID, subAlienStart, subScribedChannels, channelDescription, updateManager);
                if (!noBreakFlg) {
                  return true;
                }
                break;
              }
            }
          }
        }
      }
    }

    // continue: update current day program
    MutableChannelDayProgram dayProg;
    if (!sharedSourcesUpdate.containsKey(channel.getUniqueId()+date.getDateString())){
      dayProg = getSharedDayProgram(channel, date, alienID, alienStart, subScribedChannels); 
      sharedSourcesUpdate.put(channel.getUniqueId()+date.getDateString(), dayProg);
      if (dayProg != null && dayProg.getProgramCount() > 0) {
        updateManager.updateDayProgram(dayProg);
      }
      // Check whether the download should be canceled
      if (updateManager.cancelDownload()) {
        noBreakFlg=false;
      }
    }

    if (isAutoUpdate){
      // Update all day programs, were sources have been updated
      String propKey;
      for (Enumeration<?> ecd = channelDescription.keys(); ecd.hasMoreElements();) {
        propKey = ecd.nextElement().toString();
        if (channelDescription.getProperty(propKey).contains(channel.getUniqueId())){
          String[] nextDesc = channelDescription.getProperty(propKey).split(";",6)[5].split(";");
          String []nextAlienID  = new String [nextDesc.length/2];
          int []nextAlienStart  = new int [nextDesc.length/2];
          for (int j = 0; j < nextAlienID.length;j++){
            String []time = nextDesc[j*2].split(":");
            nextAlienStart[j]=(Integer.parseInt((String)time[0])*60)+Integer.parseInt((String)time[1]);
            nextAlienID[j]=nextDesc[(j*2)+1];
          }         
          for (int j = 0; j < nextAlienID.length; j++) {
            if (nextAlienID[j].equals(channel.getUniqueId())) {
              Channel[] availableChannel = getAvailableChannels(null);
              for (int i = 0; i < availableChannel.length; i++) {
                if (availableChannel[i].getId().equals(propKey)) {
                  Channel nextChannel = HelperMethods.getChannelFromId(availableChannel[i].getUniqueId(), subScribedChannels);
                  if (!(nextChannel == null || sharedSourcesUpdate.containsKey(nextChannel.getUniqueId() + date.getDateString()))) {
                    noBreakFlg = updateDayProgram (channelArr, nextChannel, date, nextAlienID, nextAlienStart, subScribedChannels, channelDescription, updateManager);
                    if (!noBreakFlg) {
                      return true;
                    }
                  }
                  break;
                }
              }
            }
          }
        }
      }     
    }      return noBreakFlg;
  }


  private MutableChannelDayProgram getSharedDayProgram(final Channel channel, final Date date, final String[]alienID, final int[]alienStart, Channel[] subScribedChannels){
    MutableChannelDayProgram dayProgram = new MutableChannelDayProgram(date, channel);
    if (alienStart[0]>0){
      getSharedDayPartProgram (dayProgram, channel, date, alienID[alienID.length-1], 0, alienStart[0], subScribedChannels, false);
    }
    for (int i = 0; i < alienID.length-1; i++){
      getSharedDayPartProgram (dayProgram, channel, date, alienID[i], alienStart[i], alienStart[i+1], subScribedChannels, true);
    }
    getSharedDayPartProgram (dayProgram, channel, date, alienID[alienID.length-1], alienStart[alienID.length-1], alienStart[0]+1440, subScribedChannels, true);
    return dayProgram;
  }

  private void getSharedDayPartProgram (MutableChannelDayProgram dayProgram, final Channel channel, final Date date, final String alienID, final int startTime, final int endTime, Channel[] subScribedChannels, boolean fillStartFlg){
    Channel alienChannel = HelperMethods.getChannelFromId(alienID, subScribedChannels);
    Iterator<Program> alienDayProgram;
    MutableChannelDayProgram  changedAlienDayProgram = sharedSourcesUpdate.get(alienID+date.getDateString());
       if (changedAlienDayProgram!=null && changedAlienDayProgram.getProgramCount()>0) {
         alienDayProgram = changedAlienDayProgram.getPrograms();
       } else {
      alienDayProgram = getPluginManager().getChannelDayProgram(date, alienChannel);
    }
    if (alienDayProgram == null || !alienDayProgram.hasNext()){
      dayProgram.addProgram(dummyProgram(channel, date, startTime, endTime, alienChannel.getName()));
    } else {
      boolean isFirst = true;
      Program alienProg = null;
      while (alienDayProgram.hasNext()) {
    	  alienProg = alienDayProgram.next();
    	  if (alienProg.getStartTime()+alienProg.getLength() > startTime && alienProg.getStartTime() < endTime) {
    		  if (isFirst){
    			  if (alienProg.getStartTime()>startTime && fillStartFlg){
    				  	// check for yesterdays last program to fill the gap after midnight
     					  Iterator<Program> alienYesterdayProgram;
    					  Date yesterday = date.addDays(-1);
    					  MutableChannelDayProgram  changedAlienYesterdayProgram = sharedSourcesUpdate.get(alienID+yesterday.getDateString());
    					  if (changedAlienYesterdayProgram!=null && changedAlienYesterdayProgram.getProgramCount()>0) {
    						  alienYesterdayProgram = changedAlienYesterdayProgram.getPrograms();
    					  } else {
    						  alienYesterdayProgram = getPluginManager().getChannelDayProgram(yesterday, alienChannel);
    						  if (alienDayProgram == null || !alienDayProgram.hasNext()){
    							  dayProgram.addProgram(dummyProgram(channel, date, startTime, endTime, alienChannel.getName()));
    						  } else {
    							  Program alienStartProg = null;
    							  while (alienYesterdayProgram.hasNext()) {
     								  alienStartProg = alienYesterdayProgram.next();

    							  }
    							  if (alienStartProg==null){
    								  dayProgram.addProgram(dummyProgram(channel, date, startTime, alienProg.getStartTime(), alienChannel.getName()));

    							  } else {
    								  dayProgram.addProgram(copiedProgram(alienStartProg, channel, date, startTime, endTime, true));
    							  }
    						  }
    					  }
    			  }
    			  isFirst= false;
    		  }
    		  dayProgram.addProgram(copiedProgram(alienProg, channel, date, startTime, endTime, false));
        }
      }
      if (alienProg!= null && alienProg.getStartTime()+alienProg.getLength()<endTime){
        dayProgram.addProgram(dummyProgram(channel, date, startTime, alienProg.getStartTime(), alienChannel.getName()));
      }
    }
  }

  private MutableProgram dummyProgram (Channel channel, Date date, int start, int end, String title){
    int hhStart = start/60;
    int mmStart = start-(hhStart*60);
    MutableProgram dummyProg = new MutableProgram(channel, date, hhStart, mmStart, false);
    dummyProg.setTitle(title);
    dummyProg.setDescription(mLocalizer.msg("missingProg", "No program information available."));
    int duration=end - start;
    if (duration < 0){
      duration = duration + 1440;
    }
    dummyProg.setLength(duration);
    return dummyProg;
  }

  private MutableProgram copiedProgram(Program alienProg, Channel channel, Date date, int minStart, int maxEnd, boolean midNightFlg){

    int progStart;
    if (alienProg.getStartTime() >= minStart && !midNightFlg) {
      progStart = alienProg.getStartTime();
    } else {
      progStart = minStart;
    }
    int hhStart = progStart / 60;
    int mmStart = progStart - (hhStart * 60);

    if (alienProg.getTitle().trim().length()==0) {
      return dummyProgram (channel, date, progStart, Math.min(alienProg.getStartTime()+alienProg.getLength(), maxEnd), alienProg.getChannel().getName());
    } else {
      MutableProgram newProgram = new MutableProgram(channel, date, hhStart, mmStart, true);
      int version = (tvbrowser.TVBrowser.VERSION.getMajor() * 100) + tvbrowser.TVBrowser.VERSION.getMinor();
      setTextField(newProgram, alienProg, ProgramFieldType.TITLE_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.ORIGINAL_TITLE_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.EPISODE_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.ORIGINAL_EPISODE_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.SHORT_DESCRIPTION_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.DESCRIPTION_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.ACTOR_LIST_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.DIRECTOR_TYPE);
      setInfo(newProgram, alienProg, ProgramFieldType.INFO_TYPE);
      setIntField(newProgram, alienProg, ProgramFieldType.AGE_LIMIT_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.URL_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.GENRE_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.ORIGIN_TYPE);
      setIntField(newProgram, alienProg, ProgramFieldType.NET_PLAYING_TIME_TYPE);
      setTimeField(newProgram, alienProg, ProgramFieldType.VPS_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.SCRIPT_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.REPETITION_OF_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.MUSIC_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.MODERATION_TYPE);
      setIntField(newProgram, alienProg, ProgramFieldType.PRODUCTION_YEAR_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.REPETITION_ON_TYPE);
      setBinaryField(newProgram, alienProg, ProgramFieldType.PICTURE_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.PICTURE_COPYRIGHT_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.PICTURE_DESCRIPTION_TYPE);
      setIntField(newProgram, alienProg, ProgramFieldType.EPISODE_NUMBER_TYPE);
      setIntField(newProgram, alienProg, ProgramFieldType.EPISODE_TOTAL_NUMBER_TYPE);
      setIntField(newProgram, alienProg, ProgramFieldType.SEASON_NUMBER_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.PRODUCER_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.CAMERA_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.CUTTER_TYPE);
      setTextField(newProgram, alienProg, ProgramFieldType.ADDITIONAL_PERSONS_TYPE);
      setIntField(newProgram, alienProg, ProgramFieldType.RATING_TYPE);
      if (version >= 300) {
        setTextField(newProgram, alienProg, ProgramFieldType.CUSTOM_TYPE);
        setTextField(newProgram, alienProg, ProgramFieldType.PRODUCTION_COMPANY_TYPE);
      }


      String prevText;
      String infoText;
      int infoTextLength;

      int cutInfo = Integer.parseInt((String)mProp.getProperty("cutInfoIndex"));

      if (midNightFlg){
    	  maxEnd = maxEnd+1440;
    	  minStart = minStart + 1440;
      }
      switch (cutInfo) { 
      case 0: // Custom Information before
        if (alienProg.getStartTime() + alienProg.getLength() > maxEnd) {
          if (newProgram.getTextField(ProgramFieldType.CUSTOM_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd);
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.CUSTOM_TYPE);
            infoText = mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd) + System.getProperty("line.separator");
          }
          newProgram.setTextField(ProgramFieldType.CUSTOM_TYPE, infoText + prevText);
        }
        if (alienProg.getStartTime() < minStart) {
          if (newProgram.getTextField(ProgramFieldType.CUSTOM_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime()));
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.CUSTOM_TYPE);
            infoText = mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime())) + System.getProperty("line.separator");
          }
          newProgram.setTextField(ProgramFieldType.CUSTOM_TYPE, infoText + prevText);
        }
        break;

      case 1: // Description Field before
        if (alienProg.getStartTime() + alienProg.getLength() > maxEnd) {
          if (newProgram.getTextField(ProgramFieldType.DESCRIPTION_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd);
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.DESCRIPTION_TYPE);
            infoText = mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd) + System.getProperty("line.separator") + System.getProperty("line.separator");
          }
          newProgram.setTextField(ProgramFieldType.DESCRIPTION_TYPE, infoText + prevText);
        }
        if (alienProg.getStartTime() < minStart) {
          if (newProgram.getTextField(ProgramFieldType.DESCRIPTION_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime()));
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.DESCRIPTION_TYPE);
            infoText = mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime())) + System.getProperty("line.separator") + System.getProperty("line.separator");
          }
          newProgram.setTextField(ProgramFieldType.DESCRIPTION_TYPE, infoText + prevText);
        }
        break;

      case 2: // ShortDescription before
        if (alienProg.getStartTime() + alienProg.getLength() > maxEnd) {
          if (newProgram.getTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd);
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE);
            infoText = mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd) + System.getProperty("line.separator") + System.getProperty("line.separator");
            infoTextLength = infoText.length();
            if (prevText.length() > MutableProgram.MAX_SHORT_INFO_LENGTH - (infoTextLength + 3)) {
              prevText = prevText.substring(0, MutableProgram.MAX_SHORT_INFO_LENGTH - (infoTextLength + 3)) + "...";
            }
          }
          newProgram.setTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE, infoText + prevText);
        }
        if (alienProg.getStartTime() < minStart) {
          if (newProgram.getTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime()));
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE);
            infoText = mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime())) + System.getProperty("line.separator") + System.getProperty("line.separator");
            infoTextLength = infoText.length();
            if (prevText.length() > MutableProgram.MAX_SHORT_INFO_LENGTH - (infoTextLength + 3)) {
              prevText = prevText.substring(0, MutableProgram.MAX_SHORT_INFO_LENGTH - (infoTextLength + 3)) + "...";
            }
          }
          newProgram.setTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE, infoText + prevText);
        }
        break;

      case 3:  // Episode Field before
        if (alienProg.getStartTime() + alienProg.getLength() > maxEnd) {
          if (newProgram.getTextField(ProgramFieldType.EPISODE_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd);
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.EPISODE_TYPE);
            infoText = mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd) + " \u2013 ";
          }
          newProgram.setTextField(ProgramFieldType.EPISODE_TYPE, infoText + prevText);
        }
        if (alienProg.getStartTime() < minStart) {
          if (newProgram.getTextField(ProgramFieldType.EPISODE_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime()));
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.EPISODE_TYPE);
            infoText = mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime())) + " \u2013 ";
          }
          newProgram.setTextField(ProgramFieldType.EPISODE_TYPE, infoText + prevText);
        }
        break;


      case 4: // Custom Information after
        if (version >= 300) {
			if (alienProg.getStartTime() < minStart) {
				if (newProgram.getTextField(ProgramFieldType.CUSTOM_TYPE) == null) {
					prevText = "";
					infoText = mLocalizer.msg("missingStart", "Missing Start",
							(minStart - alienProg.getStartTime()));
				} else {
					prevText = newProgram
							.getTextField(ProgramFieldType.CUSTOM_TYPE);
					infoText = System.getProperty("line.separator")
							+ mLocalizer.msg("missingStart", "Missing Start",
									(minStart - alienProg.getStartTime()));
				}
				newProgram.setTextField(ProgramFieldType.CUSTOM_TYPE, prevText
						+ infoText);
			}
			if (alienProg.getStartTime() + alienProg.getLength() > maxEnd) {
				if (newProgram.getTextField(ProgramFieldType.CUSTOM_TYPE) == null) {
					prevText = "";
					infoText = mLocalizer.msg("missingEnd", "Missing End",
							alienProg.getStartTime() + alienProg.getLength()
									- maxEnd);
				} else {
					prevText = newProgram
							.getTextField(ProgramFieldType.CUSTOM_TYPE);
					infoText = System.getProperty("line.separator")
							+ mLocalizer.msg("missingEnd", "Missing End",
									alienProg.getStartTime()
											+ alienProg.getLength() - maxEnd);
				}
				newProgram.setTextField(ProgramFieldType.CUSTOM_TYPE, prevText
						+ infoText);
			}
		}
		break;

      case 5: // Description Field after
        if (alienProg.getStartTime() < minStart) {
          if (newProgram.getTextField(ProgramFieldType.DESCRIPTION_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime()));
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.DESCRIPTION_TYPE);
            infoText = System.getProperty("line.separator") + System.getProperty("line.separator") + mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime()));
          }
          newProgram.setTextField(ProgramFieldType.DESCRIPTION_TYPE, prevText + infoText);
        }
        if (alienProg.getStartTime() + alienProg.getLength() > maxEnd) {
          if (newProgram.getTextField(ProgramFieldType.DESCRIPTION_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd);
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.DESCRIPTION_TYPE);
            infoText = System.getProperty("line.separator") + System.getProperty("line.separator") + mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd);
          }
          newProgram.setTextField(ProgramFieldType.DESCRIPTION_TYPE, prevText + infoText);
        }
        break;

      case 6: // ShortDescription after
        if (alienProg.getStartTime() < minStart) {
          if (newProgram.getTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime()));
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE);
            infoText = System.getProperty("line.separator") + System.getProperty("line.separator") + mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime()));
            infoTextLength = infoText.length();
            if (prevText.length() > MutableProgram.MAX_SHORT_INFO_LENGTH - (infoTextLength + 3)) {
              prevText = prevText.substring(0, MutableProgram.MAX_SHORT_INFO_LENGTH - (infoTextLength + 3)) + "...";
            }
          }
          newProgram.setTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE, prevText + infoText);
        }
        if (alienProg.getStartTime() + alienProg.getLength() > maxEnd) {
          if (newProgram.getTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd);
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE);
            infoText = System.getProperty("line.separator") + System.getProperty("line.separator") + mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd);
            infoTextLength = infoText.length();
            if (prevText.length() > MutableProgram.MAX_SHORT_INFO_LENGTH - (infoTextLength + 3)) {
              prevText = prevText.substring(0, MutableProgram.MAX_SHORT_INFO_LENGTH - (infoTextLength + 3)) + "...";
            }
          }
          newProgram.setTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE, prevText + infoText);
        }
        break;

      case 7: // Episode Field after
        if (alienProg.getStartTime() < minStart) {
          if (newProgram.getTextField(ProgramFieldType.EPISODE_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime()));
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.EPISODE_TYPE);
            infoText = " \u2013 " + mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime()));
          }
          newProgram.setTextField(ProgramFieldType.EPISODE_TYPE, prevText + infoText);
        }
        if (alienProg.getStartTime() + alienProg.getLength() > maxEnd) {
          if (newProgram.getTextField(ProgramFieldType.EPISODE_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd);
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.EPISODE_TYPE);
            infoText = " \u2013 " + mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd);
          }
          newProgram.setTextField(ProgramFieldType.EPISODE_TYPE, prevText + infoText);
        }
        break; 

      case 8: // Title after
        if (alienProg.getStartTime() < minStart) {
          if (newProgram.getTextField(ProgramFieldType.TITLE_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime()));
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.TITLE_TYPE);
            infoText = " \u2013 " + mLocalizer.msg("missingStart", "Missing Start", (minStart - alienProg.getStartTime()));
          }
          newProgram.setTextField(ProgramFieldType.TITLE_TYPE, prevText + infoText);
        }
        if (alienProg.getStartTime() + alienProg.getLength() > maxEnd) {
          if (newProgram.getTextField(ProgramFieldType.TITLE_TYPE) == null) {
            prevText = "";
            infoText = mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd);
          } else {
            prevText = newProgram.getTextField(ProgramFieldType.TITLE_TYPE);
            infoText = " \u2013 " + mLocalizer.msg("missingEnd", "Missing End", alienProg.getStartTime() + alienProg.getLength() - maxEnd);
          }
          newProgram.setTextField(ProgramFieldType.TITLE_TYPE, prevText + infoText);
        }
        break;

      default:

      }

      newProgram.setProgramLoadingIsComplete();
      return newProgram;
    }
  }



  /**
   * Fill a program field of type text with mixed data
   * @param currentProgram the program to be created
   * @param prog the primary data source
   * @param type the name of the text field
   */

  private void setTextField (MutableProgram currentProgram, Program prog, ProgramFieldType type){

    String info = getTextField (prog, type);

    if (info.length()>0){
      currentProgram.setTextField(type, info);
    }
  }


  /**
   * Set info bits with mixed data
   * @param currentProgram the program to be created
   * @param prog the primary data source
   * @param type the name of the int field
   */

  private void setInfo (MutableProgram currentProgram, Program prog, ProgramFieldType type){

    int info = getIntField (prog, type);

    if (info >=0 ){
      currentProgram.setInfo(info);
    }
  }

  /**
   * Fill a program field of type text with mixed data
   * @param currentProgram the program to be created
   * @param prog the primary data source
   * @param type the name of the int field
   */
  private void setIntField (MutableProgram currentProgram, Program prog, ProgramFieldType type){

    int info = getIntField (prog, type);

    if (info >=0){
      currentProgram.setIntField(type,info);
    } 
  }

  /**
   * Fill a program field of type text with mixed data
   * @param currentProgram the program to be created
   * @param prog the data source
   * @param type the name of the time field
   */

  private void setTimeField (MutableProgram currentProgram, Program prog, ProgramFieldType type){

    int info = getTimeField (prog, type);

    if (info >=0){
      currentProgram.setTimeField(type,info);
    } 
  }


  /**
   * Fill a program field of type binary with mixed data
   * @param currentProgram the program to be created
   * @param prog the data source
   * @param type the name of the binary field
   */
  private void setBinaryField (MutableProgram currentProgram, Program prog, ProgramFieldType type){

    byte[] info = getBinaryField (prog, type);

    if (info != null){
      currentProgram.setBinaryField(type,info);
    } 
  }

  private String getTextField (Program prog, ProgramFieldType type){
    if (prog==null || prog.getTextField(type)==null){
      return "";
    } else{
      return prog.getTextField(type);
    }
  }

  private int getIntField (Program prog, ProgramFieldType type){
    if (prog==null){
      return -1;
    } else{
      return prog.getIntField(type);
    }
  }      
  private int getTimeField (Program prog, ProgramFieldType type){
    if (prog==null){
      return -1;
    } else{
      return prog.getTimeField(type);
    }
  }   

  private byte[] getBinaryField (Program prog, ProgramFieldType type){
    if (prog==null){
      return null;
    } else{
      return prog.getBinaryField(type);
    }
  }      

}
