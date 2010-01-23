/*
 * Data Mixer Plugin by jb (j.bollwahn@arcor.de)
 * 
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

package mixeddataservice;


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

public class MixedDataService extends AbstractTvDataService{

  private static final Logger mLog = java.util.logging.Logger.getLogger(MixedDataService.class.getName());
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(MixedDataService.class);

  public final ChannelGroup mMixedDataChannelGroup = new devplugin.ChannelGroupImpl("MixedData", "mixeddata", "Mixed Data of two subscribed channel", mLocalizer.msg("name", "Mixed Data"));

  private static MixedDataService mInstance;
  private MixedDataServiceData data;
  private boolean isAutoUpdate = false;
  public File mDataDir;
  public String mixedChannelsDirName;
  final private String [] noMix = {"primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary","primary"};
  private HashMap <String, MutableChannelDayProgram>mixedSourcesUpdate;


  public MixedDataService()
  {
    mInstance = this;
    data = new MixedDataServiceData(this);
  }

  public static MixedDataService getInstance(){
    if (mInstance == null) {
      throw new RuntimeException("no instance of DataMixerPlugin class available");
    }
    return mInstance;
  }


  /**
   * Gets information about this TvDataService
   * @return Plugin Info containing the class object of this dataService and
   * name, description and author information of this data service to be displayed  in the settings panel.
   */
  public PluginInfo getInfo() {
    return new PluginInfo(MixedDataService.class,
        mLocalizer.msg("name", "Data Mixer Plugin"),
        mLocalizer.msg("desc", "This plugin allows to add program information from another subscribed channel"),
    "jb");

  }

  /**
   * Gets the Version of the implemented API
   * Since TV-Browser 0.9.7 getAPIVersion must return 1.0 
   */
  public static Version getVersion() {
    return new Version(0, 0, 3, false, null);
  }

  /**
   * Called by the host-application during start-up. 
   * Loads settings for this data service.
   * @param p ; poperty list to handle the settings
   *
   */
  public void loadSettings(Properties p) {
  }

  /**
   * Called by the host-application during shut-down, 
   * to store the settings returned to the file system.
   * @return a property list with settings of this data service.
   */
  public Properties storeSettings() {
    return null;

  }

  /**
   * Gets if the data service supports auto upate of data.
   * @return <code>True</code> if the data service supports the auto update,
   * <code>false</code> otherwise.
   * @since 2.7
   */
  @Override
  public boolean supportsAutoUpdate() {
    // time interval triggered in setting.prop with 'dataServiceAutoUpdateTime'

    File lckFile = new File (mixedChannelsDirName + "/datamix.lck");
    if (lckFile.exists()){
      return false;
    }

    isAutoUpdate = false;

    String updatesFileName = mixedChannelsDirName  + "/MixedDataSavings.prop";
    if (getPluginManager().getActivatedPluginForId("java.mixeddataautoupdateplugin.MixedDataAutoUpdatePlugin")!=null) {
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
            String propFileName = mixedChannelsDirName + "/mixedChannels.properties";
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
                      if (subscribedId[0].equals("mixeddataservice.MixedDataService")&& subscribedId[3].equals(propKey)){
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
      mLog.warning("MixedDataService.setWorkingDirectory Exception: " + ioe.toString());
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


  public MixedDataServicePanel getSettingsPanel()
  {
    return MixedDataServicePanel.getInstance(mInstance);
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
   return new ChannelGroup[]{mMixedDataChannelGroup};

  }


  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#getAvailableGroups()
   */
  public ChannelGroup[] getAvailableGroups() {
    return new ChannelGroup[]{mMixedDataChannelGroup};
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
    if (new File(mixedChannelsDirName + "/mixedChannels.properties").exists()) {
      try {
        InputStream reader = new FileInputStream(mixedChannelsDirName + "/mixedChannels.properties");
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
      JOptionPane.showMessageDialog(null, mLocalizer.msg("msg_update_canceled_reason", "A previous update process has not finished yet!"), mLocalizer.msg("msg_update_canceled", "Mixed Data Update Canceled:"), JOptionPane.ERROR_MESSAGE);
      return;
    }

    String propFileName = mixedChannelsDirName  + "/MixedDataSavings.prop";
    Properties changedList = new Properties();
    Properties channelDescription = new Properties();

    try {
      channelDescription.load(new FileInputStream(mixedChannelsDirName + "/mixedChannels.properties"));
    } catch (IOException e) {
    }

    if (getPluginManager().getActivatedPluginForId("java.mixeddataautoupdateplugin.MixedDataAutoUpdatePlugin")!=null) {
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
    String [] mixFlags = new String [30];
    Channel[] subScribedChannels = getPluginManager().getSubscribedChannels();

    // update tv data

    mixedSourcesUpdate = new HashMap<String, MutableChannelDayProgram>();
    for (int i = 0; i < channelArr.length; i++) {
      String []buffer =  channelDescription.getProperty(channelArr[i].getId()).split(";");
      for (int j=0; j< 30; j++){
        mixFlags[j]= buffer[j+7];
      }

      if (isAutoUpdate){
        String dateString = changedList.getProperty(buffer[1]);
        String dateString2 = changedList.getProperty(buffer[2]);
        if (dateString != null ){
          if (dateString2!=null){
            String [] day = dateString2.split(";");
            for (int index = 0; index < day.length; index++){
              if (!dateString.contains(day[index])){
                dateString = dateString + ";" + day[index];
              }
            }
          }
        } else {
          if (dateString2 != null){
            dateString = dateString2;
          }
        }
        String [] dateStringArray;
        if (dateString == null){
          dateStringArray = new String [0];
        } else {
          dateStringArray = dateString.split(";");
        }
        dateArray = new Date [dateStringArray.length];
        for (int j = 0; j < dateArray.length; j++){
          dateArray[j] = Date.createYYYYMMDD(dateStringArray[j].substring(0, 4) + "-" + dateStringArray[j].substring(4, 6)+ "-" + dateStringArray[j].substring(6, 8),"-");
        }
      } 

      for (int day = 0; day < dateArray.length; day++) {
        if (dateArray[day].compareTo(startDate)>=0) {
          if (!updateDayProgram (channelArr, channelArr[i], dateArray[day], buffer, subScribedChannels, mixFlags, channelDescription, updateManager)) {
            break;
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


  private boolean updateDayProgram (Channel[] channelArr, Channel channel, Date date, String []channelDesc, Channel [] subScribedChannels, String [] mixFlags, Properties channelDescription, TvDataUpdateManager updateManager){

    boolean noBreakFlg = true;
    // Update sources before current day program is updated
    Channel [] subChannel= {HelperMethods.getChannelFromId(channelDesc[1], subScribedChannels),HelperMethods.getChannelFromId(channelDesc[2], subScribedChannels)}; 
    for (int ci = 0; ci < 2; ci++) {
      if (channelDesc[ci + 1].startsWith("mixeddataservice.MixedDataService")) {
        if (!(subChannel[ci] == null || mixedSourcesUpdate.containsKey(subChannel[ci].getUniqueId() + date.getDateString()))) {
          for (int i = 0; i < channelArr.length; i++) {
            if (channelDesc[ci + 1].equals(channelArr[i].getUniqueId())) {
              String[] newMixFlags = new String[30];
              String[] buffer = channelDescription.getProperty(channelArr[i].getId()).split(";");
              for (int j = 0; j < 30; j++) {
                newMixFlags[j] = buffer[j + 7];
              }
              noBreakFlg = updateDayProgram(channelArr, channelArr[i], date, buffer, subScribedChannels, newMixFlags, channelDescription, updateManager);
              if (!noBreakFlg) {
                return true;
              }
              break;
            }
          }
        }
      }
    }
    // continue: update current day program
    MutableChannelDayProgram dayProg1;
    MutableChannelDayProgram dayProg2;
    if (subChannel[0]==null){
      dayProg1=null;
      }
    else {
      dayProg1= mixedSourcesUpdate.get(subChannel[0].getUniqueId()+date.getDateString());
      }
    if (subChannel[1]==null){
      dayProg2=null;
      }
    else {
      dayProg2= mixedSourcesUpdate.get(subChannel[1].getUniqueId()+date.getDateString());
     }
    if (dayProg1==null){
      dayProg1  = getDayProgram (subChannel[0],  date);
    }
    if (dayProg2==null){
      dayProg2  = getDayProgram (subChannel[1],  date);
    }
    
    MutableChannelDayProgram dayProg;
    if (!mixedSourcesUpdate.containsKey(channel.getUniqueId()+date.getDateString())){
      dayProg = mixedDayProgram(channel, dayProg1, dayProg2, date, mixFlags); 
      mixedSourcesUpdate.put(channel.getUniqueId()+date.getDateString(), dayProg);
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
          String []nextDesc =  channelDescription.getProperty(propKey).split(";");
          for (int j = 0; j < 2; j++) {
            if (nextDesc[j+1].equals(channel.getUniqueId())) {
              Channel[] availableChannel = getAvailableChannels(null);
              for (int i = 0; i < availableChannel.length; i++) {
                if (availableChannel[i].getId().equals(propKey)) {
                  Channel nextChannel = HelperMethods.getChannelFromId(availableChannel[i].getUniqueId(), subScribedChannels);
                  if (!(nextChannel == null || mixedSourcesUpdate.containsKey(nextChannel.getUniqueId() + date.getDateString()))) {
                    String[] nextMixFlags = new String[30];
                    for (int nj = 0; nj < 30; nj++) {
                      nextMixFlags[nj] = nextDesc[nj + 7];
                    }
                    noBreakFlg = updateDayProgram(channelArr, nextChannel, date, nextDesc, subScribedChannels, nextMixFlags, channelDescription, updateManager);
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
   }
    return noBreakFlg;
  }


  /**
   * Mix program data of two different channel and create a new day program
   * 
   * @param currentChannel the Mixed Data channel of the dayProg
   * @param alternativeChannel the channel with the additional data
   * @param date the day of the dayprogram
   * @return a dayprogramm with mixed data
   */
  private MutableChannelDayProgram mixedDayProgram (Channel currentChannel, MutableChannelDayProgram dayProg1, MutableChannelDayProgram dayProg2, Date date, String[] mixFlags){

    MutableChannelDayProgram dayProg = new MutableChannelDayProgram(date, currentChannel);
    int lastEndTime = 0;

    if (dayProg1.getProgramCount()>0   && dayProg2.getProgramCount()>0 ) {
      for (int i=0; i < dayProg1.getProgramCount(); i++) {
        lastEndTime = dayProg1.getProgramAt(i).getStartTime() + dayProg1.getProgramAt(i).getLength();
        MutableProgram currentProgram = new MutableProgram(currentChannel, date, dayProg1.getProgramAt(i).getHours(), dayProg1.getProgramAt(i).getMinutes(), true);

        int occurrences2 = 0;
        int maxDiff = 1440;
        Program program2 = null;
        // test how often program is in dayprogram and find nearest alternative as best fit
        for (int j = 0; j <dayProg2.getProgramCount(); j++) {
          if (compareTitle(dayProg1.getProgramAt(i).getTitle(), dayProg2.getProgramAt(j).getTitle())) {
            occurrences2++;
            int aktDiff = Math.abs(dayProg1.getProgramAt(i).getStartTime() - dayProg2.getProgramAt(j).getStartTime());
            if (aktDiff < maxDiff) {
              program2 = dayProg2.getProgramAt(j);
              maxDiff = aktDiff;
            }
          }
        }
        // if alternativeProgram was the only match it's done. Otherwise...
        if (occurrences2 > 1) {
          // compare if number of transmissions in day program
          int occurrences1 = 0;

          for (int ii=0; ii < dayProg1.getProgramCount(); ii++) {
            if (compareTitle(dayProg1.getProgramAt(i).getTitle(), dayProg1.getProgramAt(ii).getTitle())) {
              occurrences1++;
            }
          }
          if (occurrences2 == occurrences1) {
            // if the number of transmissions is equal, then match transmissions in pairs
            int counter1 = 0;
            int counter2 = 0;
            for (int ii=0; ii < dayProg1.getProgramCount(); ii++) {
              if (compareTitle(dayProg1.getProgramAt(i).getTitle(), dayProg1.getProgramAt(ii).getTitle())) {
                counter1++;
              }
            }
            int index = 0;
            while (index < dayProg2.getProgramCount()&& counter2 < counter1){
              if (compareTitle(dayProg1.getProgramAt(i).getTitle(), dayProg2.getProgramAt(index).getTitle())) {
                counter2++;
              }
              if (counter1 == counter2) {
                program2 = dayProg2.getProgramAt(index);
              }
              index++;
            }
          }
          //else{use best fit from above};
        }
        // when the best program match is found, the data mix finally can be done
        mixInfos(currentProgram, dayProg1.getProgramAt(i), program2, mixFlags);
        currentProgram.setProgramLoadingIsComplete();
        dayProg.addProgram(currentProgram);
      }
      // if dayprogram is not complete, add the data from the additional program for the rest of the day
      if (lastEndTime < 1440) {
        fillDay(dayProg, currentChannel, dayProg2, date, lastEndTime);
      }
    } else {
      if (dayProg1.getProgramCount()>0 ){
        fillDay (dayProg, currentChannel, dayProg1, date);
      }
      if (dayProg2.getProgramCount()>0 ){
        fillDay (dayProg, currentChannel, dayProg2, date);
      }
    }

    return dayProg;
  }

  private void fillDay (MutableChannelDayProgram dayProg, Channel currentChannel, MutableChannelDayProgram fillProg, Date date){
    fillDay (dayProg, currentChannel, fillProg, date, 0);
  }

  private void fillDay (MutableChannelDayProgram dayProg, Channel currentChannel, MutableChannelDayProgram fillProg, Date date, int startTime){

    for (int i = 0; i< fillProg.getProgramCount(); i++){
      Program currentProg = fillProg.getProgramAt(i);
      if (currentProg.getStartTime() > startTime) {
        MutableProgram currentProgram = new MutableProgram(currentChannel, date, currentProg.getHours(), currentProg.getMinutes(), true);
        mixInfos(currentProgram, currentProg, currentProg, noMix);
        currentProgram.setProgramLoadingIsComplete();
        dayProg.addProgram(currentProgram);
      }     
    }
  }



  private MutableChannelDayProgram getDayProgram (Channel channel, Date date) {
    MutableChannelDayProgram dayProg  = new MutableChannelDayProgram(date, channel);
    if (channel != null){
      Iterator<Program> dayIter1 = getPluginManager().getChannelDayProgram(date, channel);
      if (dayIter1!=null && dayIter1.hasNext()) {
        while (dayIter1.hasNext()){
          dayProg.addProgram(dayIter1.next());
        }
      }
    }
    return dayProg;
  }

  /**
   * add additional info to current program
   * @param currentProgram the program to be added on
   * @param altProg the program with the additional information
   */
  private void mixInfos (MutableProgram currentProgram, Program prog1, Program prog2, String []mixFlags){
    int version = (tvbrowser.TVBrowser.VERSION.getMajor()*100) +  tvbrowser.TVBrowser.VERSION.getMinor();

    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.TITLE_TYPE, mixFlags[0]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.ORIGINAL_TITLE_TYPE, mixFlags[1]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.EPISODE_TYPE, mixFlags[2]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.ORIGINAL_EPISODE_TYPE, mixFlags[3]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.SHORT_DESCRIPTION_TYPE, mixFlags[4], System.getProperty("line.separator") +  System.getProperty("line.separator"));
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.DESCRIPTION_TYPE, mixFlags[5], System.getProperty("line.separator") +  System.getProperty("line.separator"));
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.ACTOR_LIST_TYPE, mixFlags[6]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.DIRECTOR_TYPE, mixFlags[7]);
    setMixedInfo(currentProgram, prog1, prog2, ProgramFieldType.INFO_TYPE, mixFlags[9]);
    setMixedIntField(currentProgram, prog1, prog2, ProgramFieldType.AGE_LIMIT_TYPE, mixFlags[10]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.URL_TYPE, mixFlags[11]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.GENRE_TYPE, mixFlags[12], ", ");
    setMixedIntField(currentProgram, prog1, prog2, ProgramFieldType.NET_PLAYING_TIME_TYPE, mixFlags[13]);
    setMixedTimeField(currentProgram, prog1, prog2, ProgramFieldType.VPS_TYPE, mixFlags[14]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.SCRIPT_TYPE, mixFlags[15]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.REPETITION_OF_TYPE, mixFlags[16]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.MUSIC_TYPE, mixFlags[17]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.MODERATION_TYPE, mixFlags[18]);
    setMixedIntField(currentProgram, prog1, prog2, ProgramFieldType.PRODUCTION_YEAR_TYPE, mixFlags[19]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.REPETITION_ON_TYPE, mixFlags[20]);
    setMixedBinaryField(currentProgram, prog1, prog2, ProgramFieldType.PICTURE_TYPE, mixFlags[21]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.PICTURE_COPYRIGHT_TYPE, mixFlags[21]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.PICTURE_DESCRIPTION_TYPE, mixFlags[21]);
    setMixedIntField(currentProgram, prog1, prog2, ProgramFieldType.EPISODE_NUMBER_TYPE, mixFlags[22]);
    setMixedIntField(currentProgram, prog1, prog2, ProgramFieldType.EPISODE_TOTAL_NUMBER_TYPE, mixFlags[22]);
    setMixedIntField(currentProgram, prog1, prog2, ProgramFieldType.SEASON_NUMBER_TYPE, mixFlags[22]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.PRODUCER_TYPE, mixFlags[23]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.CAMERA_TYPE, mixFlags[24]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.CUTTER_TYPE, mixFlags[25]);
    setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.ADDITIONAL_PERSONS_TYPE, mixFlags[26]);
    setMixedIntField(currentProgram, prog1, prog2, ProgramFieldType.RATING_TYPE, mixFlags[27]);
    if (version >= 300) {
    	setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.CUSTOM_TYPE, mixFlags[8], System.getProperty("line.separator") +  System.getProperty("line.separator"));
    	setMixedTextField(currentProgram, prog1, prog2, ProgramFieldType.PRODUCTION_COMPANY_TYPE, mixFlags[28]);
    }
  }

  /**
   * Fill a program field of type text with mixed data
   * @param currentProgram the program to be created
   * @param prog1 the primary data source
   * @param prog2 the additional data source
   * @param type the name of the text field
   * @param mixFlag "primary" or "additional"
   */

  private void setMixedTextField (MutableProgram currentProgram, Program prog1, Program prog2, ProgramFieldType type, String mixFlag){
    setMixedTextField (currentProgram, prog1, prog2, type, mixFlag, "");
  }



  /**
   * Fill a program field of type text with mixed data
   * @param currentProgram the program to be created
   * @param prog1 the primary data source
   * @param prog2 the additional data source
   * @param type the name of the text field
   * @param mixFlag on of "primary", "additional", "after" or "before"
   * @param delimiter the delimeter between current and alternative data (when using "before" or "after!)
   */

  private void setMixedTextField (MutableProgram currentProgram, Program prog1, Program prog2, ProgramFieldType type, String mixFlag, String delimiter){

    String info1 = getTextField (prog1, type);
    String info2 = getTextField (prog2, type);
    String retInfo = "";

    if (info1.length()>0 && info2.length()>0){
      if (mixFlag.equals("after")){
        retInfo = info1 + delimiter + info2;
      }
      if (mixFlag.equals("before")){
        retInfo = info2 + delimiter + info1;
      }
    }

    if (info1.length()>0 && (mixFlag.equals("primary") ||  info2.length()<1)){
      retInfo = info1;
    } else {
      if (info2.length()>0&& (mixFlag.equals("additional") ||  info1.length()<1)) {
        retInfo = info2;
      }
    }   
    if (type == ProgramFieldType.GENRE_TYPE){
      retInfo = HelperMethods.cleanUpCategories(retInfo);
    }
    if (type == ProgramFieldType.SHORT_DESCRIPTION_TYPE && retInfo.length()>MutableProgram.MAX_SHORT_INFO_LENGTH){
      retInfo = retInfo.substring(0, MutableProgram.MAX_SHORT_INFO_LENGTH-3)+"...";
    } 

    if (retInfo.length()>0){
      currentProgram.setTextField(type, retInfo);
    }
  }


  /**
   * Set info bits with mixed data
   * @param currentProgram the program to be created
   * @param prog1 the primary data source
   * @param prog2 the additional data source
   * @param type the name of the int field
   * @param mixFlag one of "primary", "additional" or "mix"
   */

  private void setMixedInfo (MutableProgram currentProgram, Program prog1, Program prog2, ProgramFieldType type, String mixFlag){

    int info1 = getIntField (prog1, type);
    int info2 = getIntField (prog2, type);

    if ((mixFlag.equals("mix")&& info1 >=0 && info2 >=0)){
      currentProgram.setInfo(HelperMethods.mixInfoBits(info1,info2));
    } else {
      if (info1 >=0 && (mixFlag.equals("primary") || info2<0)){
        currentProgram.setInfo(info1);
      } else {
        if (info2>=0) {
          currentProgram.setInfo(info2);
        }
      } 
    }
  }

  /**
   * Fill a program field of type text with mixed data
   * @param currentProgram the program to be created
   * @param prog1 the primary data source
   * @param prog2 the additional data source
   * @param type the name of the int field
   * @param mixFlag "primary" or "additional
   */
  private void setMixedIntField (MutableProgram currentProgram, Program prog1, Program prog2, ProgramFieldType type, String mixFlag){

    int info1 = getIntField (prog1, type);
    int info2 = getIntField (prog2, type);

    if (info1 >=0 && (mixFlag.equals("primary") || info2<0)){
      currentProgram.setIntField(type,info1);
    } else {
      if (info2>=0) {
        currentProgram.setIntField(type, info2);
      }
    }
  }

  /**
   * Fill a program field of type text with mixed data
   * @param currentProgram the program to be created
   * @param prog1 the primary data source
   * @param prog2 the additional data source
   * @param type the name of the time field
   * @param mixFlag "primary" or "additional"
   */

  private void setMixedTimeField (MutableProgram currentProgram, Program prog1, Program prog2, ProgramFieldType type, String mixFlag){

    int info1 = getTimeField (prog1, type);
    int info2 = getTimeField (prog2, type);

    if (info1 >=0 && (mixFlag.equals("primary") || info2<0)){
      currentProgram.setTimeField(type,info1);
    } else {
      if (info2>=0) {
        currentProgram.setTimeField(type, info2);
      }
    }
  }


  /**
   * Fill a program field of type binary with mixed data
   * @param currentProgram the program to be created
   * @param prog1 the primary data source
   * @param prog2 the additional data source
   * @param type the name of the binary field
   * @param mixFlag "primary" or "additional"
   */
  private void setMixedBinaryField (MutableProgram currentProgram, Program prog1, Program prog2, ProgramFieldType type, String mixFlag){

    byte[] info1 = getBinaryField (prog1, type);
    byte[] info2 = getBinaryField (prog2, type);

    if (info1 != null && (mixFlag.equals("primary") || info2==null)){
      currentProgram.setBinaryField(type,info1);
    } else {
      if (info2!=null) {
        currentProgram.setBinaryField(type, info2);
      }
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

  /**
   * Test, whether on program title is the short version of the other
   * @param title1
   * @param title2
   * @return
   */
  private boolean compareTitle(final String title1, final String title2){

    String test1 = title1.toLowerCase();
    String test2 = title2.toLowerCase();
    if (test1.equals(test2) || test1.indexOf(title2) >= 0 || test2.indexOf(test1) >= 0){
      return true;
    }

    int count1 = 0;
    int count2 = 0;
    int max1 = test1.length()-1;
    int max2 = test2.length()-1;

    while (count1<=max1 & count2<=max2){
      char char1 = test1.charAt(count1);
      while (count1 <= max1 && (char1 ==' '||char1 =='!'||char1 =='\"'||char1 =='#'||char1 =='$'||char1 =='%'||char1 =='&'||char1 =='\''||char1 =='('||char1 ==')'||char1 =='*'||char1 =='+'||char1 ==','||char1 =='-'||char1 =='.'||char1 =='/'||char1 ==':'||char1 ==';'||char1 =='<'||char1 =='='||char1 =='>'||char1 =='?'||char1 =='['||char1 =='\\'||char1 ==']'||char1 =='_'||char1 =='')){
        count1++;
        if (count1 <= max1){
          char1 = test1.charAt(count1);
        }
      }
      char char2 = test2.charAt(count2);
      while (count2 <= max2 && (char2 ==' '||char2 =='!'||char2 =='\"'||char2 =='#'||char2 =='$'||char2 =='%'||char2 =='&'||char2 =='\''||char2 =='('||char2 ==')'||char2 =='*'||char2 =='+'||char2 ==','||char2 =='-'||char2 =='.'||char2 =='/'||char2 ==':'||char2 ==';'||char2 =='<'||char2 =='='||char2 =='>'||char2 =='?'||char2 =='['||char2 =='\\'||char2 ==']'||char2 =='_'||char2 =='')){
        count2++;
        if (count2 <= max2){
          char2 = test2.charAt(count2);
        }     }

      if (count1>max1&& count2>max2){
        return true;
      }
      if (char1==char2){
        count1++;
        count2++;
        if (count2>max2){
          while (count1 <= max1 && (char1 ==' '||char1 =='!'||char1 =='\"'||char1 =='#'||char1 =='$'||char1 =='%'||char1 =='&'||char1 =='\''||char1 =='('||char1 ==')'||char1 =='*'||char1 =='+'||char1 ==','||char1 =='-'||char1 =='.'||char1 =='/'||char1 ==':'||char1 ==';'||char1 =='<'||char1 =='='||char1 =='>'||char1 =='?'||char1 =='['||char1 =='\\'||char1 ==']'||char1 =='_'||char1 =='')){
            count1++;
            if (count1 <= max1){
              char1 = test1.charAt(count1);
            }
          }
          if (count1 <= max1){
            return false;
          }
        }
        if (count1>max1){
          while (count2 <= max2 && (char2 ==' '||char2 =='!'||char2 =='\"'||char2 =='#'||char2 =='$'||char2 =='%'||char2 =='&'||char2 =='\''||char2 =='('||char2 ==')'||char2 =='*'||char2 =='+'||char2 ==','||char2 =='-'||char2 =='.'||char2 =='/'||char2 ==':'||char2 ==';'||char2 =='<'||char2 =='='||char2 =='>'||char2 =='?'||char2 =='['||char2 =='\\'||char2 ==']'||char2 =='_'||char2 =='')){
            count2++;
            if (count2 <= max2){
              char2 = test2.charAt(count2);
            }
          }
          if (count2 <= max2){
            return false;
          }
        }
      } else{
        if (char1=='á' || char1=='à' || char1=='â'){
          char1 = 'a';
        } else{
          if (char1=='é' || char1=='è' || char1=='ê'){
            char1 = 'e';
          }else{
            if (char1=='í' || char1=='ì' || char1=='î'){
              char1 = 'i';
            }else{
              if (char1=='ó' || char1=='ò' || char1=='ô'){
                char1 = 'o';
              }else{
                if (char1=='ú' || char1=='ù' || char1=='û'){
                  char1 = 'u';
                }else{
                  if (char1=='ç'){
                    char1 = 'c';
                  }else{
                    if (char1=='ñ'){
                      char1 = 'n';
                    }
                  }
                }
              }
            }
          }
        }
        if (char2=='é' || char2=='è' || char2=='ê'){
          char2 = 'e';
        }else{
          if (char2=='í' || char2=='ì' || char2=='î'){
            char2 = 'i';
          }else{
            if (char2=='ó' || char2=='ò' || char2=='ô'){
              char2 = 'o';
            }else{
              if (char2=='ú' || char2=='ù' || char2=='û'){
                char2 = 'u';
              }else{
                if (char2=='ç'){
                  char2 = 'c';
                }else{
                  if (char2=='ñ'){
                    char2 = 'n';
                  }
                }
              }
            }
          }
        }
        if (char1==char2){
          count1++;
          count2++;
          if (count2>max2){
            while (count1 <= max1 && (char1 ==' '||char1 =='!'||char1 =='\"'||char1 =='#'||char1 =='$'||char1 =='%'||char1 =='&'||char1 =='\''||char1 =='('||char1 ==')'||char1 =='*'||char1 =='+'||char1 ==','||char1 =='-'||char1 =='.'||char1 =='/'||char1 ==':'||char1 ==';'||char1 =='<'||char1 =='='||char1 =='>'||char1 =='?'||char1 =='['||char1 =='\\'||char1 ==']'||char1 =='_'||char1 =='')){
              count1++;
              if (count1 <= max1){
                char1 = test1.charAt(count1);
              }
            }
            if (count1 <= max1){
              return false;
            }
          }
          if (count1>max1){
            while (count2 <= max2 && (char2 ==' '||char2 =='!'||char2 =='\"'||char2 =='#'||char2 =='$'||char2 =='%'||char2 =='&'||char2 =='\''||char2 =='('||char2 ==')'||char2 =='*'||char2 =='+'||char2 ==','||char2 =='-'||char2 =='.'||char2 =='/'||char2 ==':'||char2 ==';'||char2 =='<'||char2 =='='||char2 =='>'||char2 =='?'||char2 =='['||char2 =='\\'||char2 ==']'||char2 =='_'||char2 =='')){
              count2++;
              if (count2 <= max2){
                char2 = test2.charAt(count2);
              }
            }
            if (count2 <= max2){
              return false;
            }
          }        } else{
            return false;
          }
      }

    }
    return true;
  }




}
