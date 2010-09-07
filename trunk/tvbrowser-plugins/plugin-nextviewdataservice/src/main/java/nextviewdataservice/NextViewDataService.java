/*
 * NextViewDataService Plugin by jb (j.bollwahn@arcor.de) / Original written by Andreas Hessel (Vidrec@gmx.de)
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
package nextviewdataservice;

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

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import util.misc.OperatingSystem;
import util.ui.ChannelLabel;
import devplugin.AbstractTvDataService;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgressMonitor;
import devplugin.Version;

/**
 * Main class of the 'nxtvepg data plugin'
 * @author jb
 */
public final class NextViewDataService extends AbstractTvDataService {

  private static final Logger mLog = java.util.logging.Logger.getLogger(NextViewDataService.class.getName());
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(NextViewDataService.class);

  public static final String PATH = OS_specific_Name("PATH");
  public static final String PROVIDER = OS_specific_Name("PROVIDER");
  public static final String RCFILE = OS_specific_Name("RCFILE");
  public static final String DBDIR = OS_specific_Name("DBDIR");
  public static final String AUTORUN = OS_specific_Name("AUTORUN");
  public static final String AUTOSTART = OS_specific_Name("AUTOSTART");
  public static final String AUTOREPETITION = OS_specific_Name("AUTOREPETITION");
  public static final String DATAMIX = OS_specific_Name("DATAMIX");
  public static final String ALTERNATIVEICONS = OS_specific_Name("ALTERNATIVEICONS");

  private static NextViewDataService mInstance;

  public final ChannelGroup mNextViewChannelGroup = new NextViewChannelGroup("NextView", "nextview", "Data from nxtvepg", mLocalizer.msg("name", "Nxtvepg Data"));

  public File mDataDir;
  public String mixedChannelsDirName;
  public long firstUpdate = (new java.util.Date()).getTime();
  public long nextUpdate;
  private boolean isPartialUpdate = false;

  private Properties prop = new Properties();
  private NextViewDataServiceData data;
  final private String [] repairMix = {"replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace","replace"};
  private HashMap <String, MutableChannelDayProgram> nxtvepgSourcesUpdate;


  /**
   * Initialize new instance of NextViewDataService
   */
  public NextViewDataService() {
    mInstance = this;
    data = new NextViewDataServiceData(this);
  }

  /**
   *
   * @return current instance of NextViewDataService
   * throws RuntimeException if  no instance available
   */
  public static NextViewDataService getInstance() {
    if (mInstance == null) {
      throw new RuntimeException("no instance of NextViewDataService class available");
    }
   return mInstance;
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
      JOptionPane.showMessageDialog(null, mLocalizer.msg("msg_update_canceled_reason", "A previous update process has not finished yet!"), mLocalizer.msg("msg_update_canceled", "Nxtvepg Data Update Canceled:"), JOptionPane.ERROR_MESSAGE);
      return;
    }

    String propFileName = mixedChannelsDirName  + "/NxtvepgDataSavings.prop";
    Properties changedList = new Properties();
    Properties alternativesDescription = new Properties();

    try {
      alternativesDescription.load(new FileInputStream(mixedChannelsDirName + "/nxtvepgAlternatives.properties"));
    } catch (IOException e) {
    }

    if (getPluginManager().getActivatedPluginForId("java.nextviewdataupdatewatcherplugin.NextViewDataUpdateWatcherPlugin")!=null) {
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
    if (isPartialUpdate) {
      ArrayList <Channel> toBeUpdated = new ArrayList <Channel>();
      String changedKey;
      String propKey;
      for (Enumeration<?> ecl = changedList.keys(); ecl.hasMoreElements();) {
        changedKey = ecl.nextElement().toString();
        for (Enumeration<?> ecd = alternativesDescription.keys(); ecd.hasMoreElements();) {
          propKey = ecd.nextElement().toString();
          String altDesc = alternativesDescription.getProperty(propKey);
          if (altDesc.contains(changedKey)&& altDesc.startsWith("1;")){
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
      monitor.setMaximum(channelArr.length+2);
      monitor.setMessage(mLocalizer.msg("msg_update", "Processing Update"));
      monitor.setValue(0);
    }


    // Clear MutableChannelDayProgram to avoid 'Shadow-Programs'
    for (Channel element : channelArr) {
      if (isPartialUpdate){
        dateArray = getDateArray (changedList, alternativesDescription.getProperty(element.getId()).split(";")[2]);
      }
      for (Date element2 : dateArray) {
        if (element2.compareTo(startDate)>=0) {
          MutableChannelDayProgram prog = data.getDispatcher().getChannelDayProgram(element2, element);
          if (prog != null) {
            prog.removeAllPrograms();
          }
        }
      }
    }


    this.getTvData(true, true, monitor);

    monitor.setValue(2);

    // get alternative chanels for adding additional data to nxtvepg data
    HashMap<String, Channel> alternativeChannels0 = new HashMap<String, Channel>();
    HashMap<String, Channel> alternativeChannels1 = new HashMap<String, Channel>();
    Channel[]subScribedChannels = getPluginManager().getSubscribedChannels();

    if (useAlternativeData()) {
      alternativeChannels0 = HelperMethods.getAlternativeChannels(0);
    }
    if (useAlternativeIcons()) {
      alternativeChannels1 = HelperMethods.getAlternativeChannels(1);
    }

    // update tv data
    nxtvepgSourcesUpdate = new HashMap<String, MutableChannelDayProgram>();
    for (int i = 0; i < channelArr.length; i++) {
      if (isPartialUpdate){
        dateArray = getDateArray (changedList, alternativesDescription.getProperty(channelArr[i].getId()).split(";")[2]);
      }
      for (int day = 0; day < dateArray.length; day++) {
        if (dateArray[day].compareTo(startDate)>=0) {
          if (!updateDayProgram (channelArr, channelArr[i], dateArray[day], alternativeChannels0, subScribedChannels, updateManager)) {
            break;
          }

        }
      }

      // Use icon of alternative channel
      if (useAlternativeIcons()){
        if (alternativeChannels1.get(channelArr[i].getId())!= null && alternativeChannels1.get(channelArr[i].getId()).hasIcon()){
          Icon icon =alternativeChannels1.get(channelArr[i].getId()).getIcon();
          if (icon == null){
            icon = new ChannelLabel(alternativeChannels1.get(channelArr[i].getId())).getIcon();
          }
          JbUtilities.storeIcon(icon, "png", mDataDir + "/alternative_icons/" + channelArr[i].getId() + ".png");
          channelArr[i].setDefaultIcon(icon);
        }
      }

      monitor.setValue(i + 2);

    }
    if (isPartialUpdate){
      isPartialUpdate = false;
    }
    monitor.setMessage(mLocalizer.msg("msg_update_done", "Update Done"));
  }

  private boolean updateDayProgram (Channel[] channelArr, Channel channel, Date date, HashMap<String, Channel> alternativeChannels, Channel [] subScribedChannels, TvDataUpdateManager updateManager){

   boolean noBreakFlg = true;

    if (useAlternativeData()) {
      // Update sources before current day program is updated
      Channel altChannel = alternativeChannels.get(channel.getId());
      if (altChannel != null && altChannel.getUniqueId().startsWith("nextviewdataservice.NextViewDataService")) {
        if (!(altChannel == null || nxtvepgSourcesUpdate.containsKey(altChannel.getUniqueId() + date.getDateString()))) {
          noBreakFlg = updateDayProgram (channelArr, altChannel, date, alternativeChannels, subScribedChannels, updateManager);
          if (!noBreakFlg) {
            return true;
          }
        }
      }
    }

    // continue: update current day program
    MutableChannelDayProgram dayProg;
    if (!nxtvepgSourcesUpdate.containsKey(channel.getUniqueId()+date.getDateString())){
      dayProg = data.getDispatcher().getChannelDayProgram(date, channel);
      // programs starting before blockTime should not be mixed with alternative data.
      // These programs are handled by the unsufficient work around and are "mixed up" by a previous update
      int blockTime = 0;
      // Work around for unsufficient nextvepg's "expired display" settings
      if (date.compareTo(Date.getCurrentDate()) <= 0) {
        if (dayProg == null || dayProg.getProgramCount() == 0) {
          blockTime = 1440;
          dayProg = replaceWithStoredDayProgram(channel, date);
        } else {
          blockTime = dayProg.getProgramAt(0).getStartTime();
          dayProg = fillWithStoredDayProgram(dayProg, channel, date);
        }
      }
      // add additional data from other channels to nxtvepg data
      if (useAlternativeData()) {
        dayProg = mixedDayProgram(dayProg, channel, alternativeChannels.get(channel.getId()), date, blockTime);
      }
      // repair damaged sat1 transmissions
      if (sat1Error(dayProg)) {
        dayProg = repairedSat1DayProgram(dayProg, channel, date);
      }
      // Finally the day program can be updated!
      nxtvepgSourcesUpdate.put(channel.getUniqueId()+date.getDateString(), dayProg);
      if (dayProg != null && dayProg.getProgramCount() > 0) {
        updateManager.updateDayProgram(dayProg);
      }
      // Check whether the download should be canceled
      if (updateManager.cancelDownload()) {
        noBreakFlg = false;
      }
    }

    if (isPartialUpdate && useAlternativeData()){
      // Update all day programs, were sources have been updated
      String key;
      for (Object name : alternativeChannels.keySet()) {
        key = name.toString();
        if (alternativeChannels.get(key).getUniqueId().equals(channel.getUniqueId())){
          Channel[] availableChannel = getAvailableChannels(null);
          for (Channel element : availableChannel) {
            if (element.getId().equals(key)) {
              Channel nextChannel = HelperMethods.getChannelFromId(element.getUniqueId(), subScribedChannels);
              if (!(nextChannel == null || nxtvepgSourcesUpdate.containsKey(nextChannel.getUniqueId() + date.getDateString()))) {
                noBreakFlg = updateDayProgram(channelArr, nextChannel, date, alternativeChannels, subScribedChannels, updateManager);
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
    return noBreakFlg;
  }


  private Date [] getDateArray (Properties changedList, String altChnId){
    String dateString = changedList.getProperty(altChnId);
    String [] dateStringArray;
    if (dateString == null){
      dateStringArray = new String [0];
    } else {
      dateStringArray = dateString.split(";");
    }
    Date [] dateArray = new Date [dateStringArray.length];
    for (int j = 0; j < dateArray.length; j++){
      dateArray[j] = Date.createYYYYMMDD(dateStringArray[j].substring(0, 4) + "-" + dateStringArray[j].substring(4, 6)+ "-" + dateStringArray[j].substring(6, 8),"-");
    }
    return dateArray;
  }


  /**
   * Called by the host-application during start-up.
   * Loads settings for this data service.
   * @param p ; property list to handle the settings
   *
   */
  public void loadSettings(Properties p) {
	  if (p.isEmpty()){
		  prop.put(PATH, NextViewDataServicePanel.getNxtvApplication(""));
		  prop.put(PROVIDER, "merged");
		  prop.put(AUTORUN, "NO");
		  prop.put(AUTOSTART, "30");
		  prop.put(AUTOREPETITION, "60");
		  prop.put(DATAMIX, "NO");
		  prop.put(ALTERNATIVEICONS, "NO");

	  } else {
		  String propKey;
		  for (Enumeration<?> e = p.keys(); e.hasMoreElements();) {
			  propKey = e.nextElement().toString();
			  this.prop.setProperty(propKey, p.getProperty(propKey));
		  }
	  }
	  firstUpdate = firstUpdate + Integer.parseInt(prop.getProperty(AUTOSTART, "30")) * 60000L;
	  nextUpdate = firstUpdate;

  }

  /**
   * Called by the host-application during shut-down,
   * to store the settings returned to the file system.
   * @return a property list with settings of this data service.
   */
  public Properties storeSettings() {
    return prop;
  }

  public boolean hasSettingsPanel() {
    return true;
  }


  public SettingsPanel getSettingsPanel() {
    return NextViewDataServicePanel.getInstance(prop);
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
    Properties channelList = new Properties();
    if (new File(mDataDir + "/channels.properties").exists()) {
      try {
        InputStream reader = new FileInputStream(mDataDir + "/channels.properties");
        channelList.load(reader);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (channelList.size() == 0) {
      this.getTvData(false, false, null);
    } else {
      String channelID;
      for (Enumeration<?> e = channelList.keys(); e.hasMoreElements();) {
        channelID = e.nextElement().toString();
        data.addChannel(channelID, channelList.getProperty(channelID));
      }
    }
    return data.getChannels();
  }

  /**
   * Prompt nxtvepg to export program data an read the available channels from this file.
   * Stores available channels in file 'channels.properties' for future use.
   * @return array of channels
   */
  public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) {
    monitor.setMessage(mLocalizer.msg("msg_channels", "reading channels from 'nxtvepg'"));
    this.getTvData(true, false, monitor);
    Channel [] availableList=data.getChannels();
    Properties newProp = new Properties();
    Properties alternativeChannelsDesc = new Properties();
    try {
      alternativeChannelsDesc.load(new FileInputStream(NextViewDataService.getInstance().mixedChannelsDirName + "/nxtvepgAlternatives.properties"));
      String defaultsFileName = "files/alternative_sources.properties";
      final InputStream stream = getClass().getResourceAsStream(defaultsFileName);
      Properties defaults = new Properties();
      try {
        defaults.load(stream);
        String propKey;
        for (Enumeration<?> e = alternativeChannelsDesc.keys(); e.hasMoreElements();) {
          propKey = e.nextElement().toString();
          int index = 0;
          boolean notAvailable = true;
          while (notAvailable && index < availableList.length){
            if (availableList[index].getId().equals(propKey)){
              notAvailable = false;
            } else{
              index ++;
            }
          }
          if (notAvailable){
            String defaultValue = defaults.getProperty(propKey);
            String alternativeChn;
            if (defaultValue == null){
              alternativeChn = "";
            } else{
              alternativeChn = defaultValue.split(";",4)[2];
            }
            String [] currentValue = alternativeChannelsDesc.getProperty(propKey).split(";");
            if (currentValue.length>1) {
              StringBuffer newValue = new StringBuffer(currentValue[0] + ";" + currentValue[1]);
              newValue.append(";" + alternativeChn);
              for (int i = 3; i<currentValue.length; i++){
                newValue.append(";" + currentValue[i]);
              }
              newProp.setProperty(propKey, newValue.toString());
            }
          } else {
            newProp.setProperty(propKey, alternativeChannelsDesc.getProperty(propKey));
          }
        }
        try{
          newProp.store(new FileOutputStream(mixedChannelsDirName + "/nxtvepgAlternatives.properties"), "Nxtvepg Alternative Sources");
        } catch (IOException e) {
        }
      } catch (IOException ioe) {
        mLog.warning(ioe.toString());
      }
    } catch (IOException e) {
    }

    return availableList;
  }


  public boolean supportsDynamicChannelList() {
    return true;
  }

  /**
   * Gets information about this TvDataService
   * @return Plugin Info containing the class object of this dataService and
   * name, description and author information of this data service to be displayed  in the settings panel.
   */
  public PluginInfo getInfo() {
    return new devplugin.PluginInfo(NextViewDataService.class,
        mLocalizer.msg("name", "Nxtvepg Data Plugin"),
        mLocalizer.msg("desc", "This data plugin imports television data from the program nxtvepg"),
    "jb");

  }

  /**
   * Gets the Version of the implemented API
   * Since TV-Browser 0.9.7 getAPIVersion must return 1.0
   */
  public static Version getVersion() {
    return new Version(2, 19, 6, false, null);
  }

  public void setWorkingDirectory(java.io.File dataDir) {
    mDataDir = dataDir;
    try {
      mDataDir = mDataDir.getCanonicalFile();
    } catch (Exception ioe) {
      mLog.warning("NextViewDataService.setWorkingDirectory Exception: " + ioe.toString());
    }

    String lckFile = mDataDir + "/autoUpLck.bak";
    if (!(new File(lckFile)).exists()) {
      JbUtilities.getFileFromThisJar(mInstance.getClass(), "files/autoUpLck.bak", lckFile);
    }

    String cni_map_File = mDataDir + "/cni.map.properties";
    String channelDesc = mDataDir + "/cni.desc.properties";
    JbUtilities.getFileFromThisJar(mInstance.getClass(), "files/cni.map.properties", cni_map_File);
    JbUtilities.getFileFromThisJar(mInstance.getClass(), "files/cni.desc.properties", channelDesc);

    File iconDir = new File (mDataDir + "/alternative_icons");
    if (!iconDir.exists()){
      iconDir.mkdir();
    }

    mixedChannelsDirName =getPluginManager().getTvBrowserSettings().getTvBrowserUserHome() + "/MixedChannels" ;
    File mixedChannelsDir = new File (mixedChannelsDirName);
    if (!mixedChannelsDir.exists()){
      mixedChannelsDir.mkdir();
    }
    String altMixedDesc = mDataDir + "/alternative_sources.properties";
    String newMixedDesc = mixedChannelsDirName + "/nxtvepgAlternatives.properties";
    File newMixedFile = new File(newMixedDesc);
    if (!newMixedFile.exists()){
      File altMixedFile = new File(altMixedDesc);
      if (altMixedFile.exists()) {
        // to be removed in future versions!
        try {
          util.io.IOUtilities.copy(altMixedFile, newMixedFile);
          altMixedFile.delete();
        } catch (Exception e) {
        }
        // end of removal
      }
      else{
        JbUtilities.getFileFromThisJar(mInstance.getClass(), "files/alternative_sources.properties", newMixedDesc);
      }
    }
  }

  /**
   * @return the working directory of this data service.
   */
  public File getWorkingDirectory() {
    return mDataDir;
  }

  public void handleTvBrowserStartFinished() {
    getAvailableChannels(null);
    File lckFile = new File (mixedChannelsDirName + "/datamix.lck");
    if (lckFile.exists()){
      lckFile.delete();
    }
  }


  /**
   * Gets if the data service supports auto update of data.
   * @return <code>True</code> if the data service supports the auto update,
   * <code>false</code> otherwise.
   * @since 2.7
   */
  @Override
  public boolean supportsAutoUpdate() {
//  triggered in setting.prop with 'dataServiceAutoUpdateTime'

    File lckFile = new File (mixedChannelsDirName + "/datamix.lck");
    if (lckFile.exists()){
      return false;
    }

    long aktTime = (new java.util.Date()).getTime();
    if (useAlternativeData()) {
      isPartialUpdate = false;
      String updatesFileName = mixedChannelsDirName + "/NxtvepgDataSavings.prop";
      if (getPluginManager().getActivatedPluginForId("java.nextviewdataupdatewatcherplugin.NextViewDataUpdateWatcherPlugin")!=null) {
        File upDatesFile = new File(updatesFileName);
        if (upDatesFile.exists()) {
          try {
            Properties changedList = new Properties();
            InputStream reader = new FileInputStream(updatesFileName);
            changedList.load(reader);
            if (changedList.size() > 0) {
              String[] changedChannels = new String[changedList.size()];
              int counter = 0;
              for (Enumeration<?> ecl = changedList.keys(); ecl.hasMoreElements();) {
                changedChannels[counter] = ecl.nextElement().toString();
                counter++;
              }
              Properties channelDescription = new Properties();
              String propFileName = mixedChannelsDirName + "/nxtvepgAlternatives.properties";
              if (new File(propFileName).exists()) {
                channelDescription.load(new FileInputStream(propFileName));
                Channel[]subScribedChannels = getPluginManager().getSubscribedChannels();
                counter = 0;
                while (!isPartialUpdate && counter < changedChannels.length) {
                  Enumeration<?> eNum = channelDescription.keys();
                  while (!isPartialUpdate && eNum.hasMoreElements()) {
                    String propKey = eNum.nextElement().toString();
                    String chnDesc = channelDescription.getProperty(propKey);
                    if (chnDesc.contains(changedChannels[counter])&& chnDesc.startsWith("1;")) {
                      for (int i=0; i<subScribedChannels.length;i++){
                        String [] subscribedId = subScribedChannels[i].getUniqueId().split("_");
                        if (subscribedId[0].equals("nextviewdataservice.NextViewDataService")&& subscribedId[3].equals(propKey)){
                          isPartialUpdate = true;
                          i= subScribedChannels.length; // no further itertion needed
                        }
                      }
                    }
                  }
                  counter++;
                }
                if (!isPartialUpdate) {
                  try {
                    changedList.clear();
                    changedList.store(new FileOutputStream(updatesFileName), "Changed DayPrograms to be checked for Update");
                  } catch (IOException e) {
                    mLog.warning("Clearing mixed channel Update list failed");
                  }
                }
              }
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
      if (isPartialUpdate) {
        if (aktTime >= nextUpdate && getAutoRun()) {
          isPartialUpdate = false;
        }
        return true;
      }
    }


    if (!getAutoRun()) {
      return false;
    }

    if (new File(mDataDir + "/autoUpLck.lck").exists()) {
      return false;
    }

    if (aktTime < nextUpdate) {
      return false;
    }

    nextUpdate = aktTime + Integer.parseInt((String) prop.getProperty(AUTOREPETITION, "60")) * 60000L;
    return true;
  }

  private boolean getAutoRun() {
    return prop.getProperty(AUTORUN, "NO").toString().equals("YES");
  }


  /**
   * Updates the TV-Data by starting nxtvepg and dumping its
   * database as xml-file. Then this file is parsed.
   * @param update ; if true, nxtvepg is prompted to export a new data file
   * @param full ; if false, the channel data will be parsed only. Otherwise the program data will be read as well.
   * @param monitor ; the progressmonitor of the main application
   */
  private void getTvData(final Boolean update, final Boolean full, final ProgressMonitor monitor) {

	  String nxtAppPath = (String) prop
	  .getProperty(PATH, NextViewDataServicePanel
			  .getNxtvApplication(""));

	  File nxtvApp = new File (nxtAppPath);



    if (nxtvApp.exists()) {
		// Definition of the thread to export nxtvepg data and to parse it
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					boolean updateError = false;
					String outFileDir = mDataDir.getCanonicalPath();
					String outFile = outFileDir + "/xmldata";
					File fOutFile = new File(outFile);

					// prompt nxtvepg to export a new data file

					if (update || !fOutFile.exists()) {

						// backup previous export file

						String backFile = outFileDir + "/xmlback";
						File fBackFile = new File(backFile);
						if (fOutFile.exists() && (fOutFile.length() > 1024)) {
							fOutFile.renameTo(fBackFile);
						}

						// create command string to call application nxtvegp

						String nxtApp = (String) prop
								.getProperty(PATH, NextViewDataServicePanel
										.getNxtvApplication(""));

						String nxtIni = (String) prop.getProperty(RCFILE, "");
						String nxtDBDir = (String) prop.getProperty(DBDIR, "");
						String nxtProv = (String) prop.getProperty(PROVIDER,
								"merged");

						String[] next2xmlCmd = new String[11];

						next2xmlCmd[0] = nxtApp;
						next2xmlCmd[1] = "-dump";
						next2xmlCmd[2] = "xml5ltz";

						next2xmlCmd[3] = "-outfile";
						next2xmlCmd[4] = outFile;

						if (nxtProv.equals("") || nxtProv.equals("FF")) {
							nxtProv = "merged";
						}

						next2xmlCmd[5] = "-prov";
						next2xmlCmd[6] = nxtProv;

						int argCounter = 7;

						if (!nxtIni.equals("")) {
							next2xmlCmd[7] = "-rcfile";
							next2xmlCmd[8] = (new File(nxtIni))
									.getCanonicalPath();
							argCounter = 9;
						}

						if (!nxtDBDir.equals("")) {
							next2xmlCmd[argCounter] = "-dbdir";
							next2xmlCmd[argCounter + 1] = (new File(nxtDBDir))
									.getCanonicalPath();
							argCounter = argCounter + 2;
						}

						fOutFile.delete();

						String[] cmdParameters = new String[argCounter];
						System.arraycopy(next2xmlCmd, 0, cmdParameters, 0,
								argCounter);

						// run external nxtvepg process
						try {
							if (!fOutFile.exists()) {
								JbUtilities.runExtProcess(cmdParameters,
										outFileDir);
								if (!fOutFile.exists()) {
									mLog
											.warning("nxtvepg export Error: Unable to create: "
													+ fOutFile
															.getCanonicalFile());
									updateError = true;
								}
							} else {
								mLog
										.warning("nxtvepg export Error: file already exists: "
												+ fOutFile.getCanonicalFile());
								updateError = true;
							}
						} catch (Exception e) {
							mLog.warning("CatchError: nxtvepg-Export -- " + e);
							updateError = true;
						}
						if (!fOutFile.exists() || (fOutFile.length() < 1024)) {
							if (fBackFile.exists()) {
								if (fOutFile.exists()) {
									fOutFile.delete();
								}
								fBackFile.renameTo(fOutFile);
								String failedMessage = mLocalizer
										.msg(
												"failedExportMessage",
												"Nxtvepg: Data export failed!"
														+ System
																.getProperty("line.separator")
														+ System
																.getProperty("line.separator")
														+ "Please check:"
														+ System
																.getProperty("line.separator")
														+ "1.) The settings of the Nxtvepg Data Plugin here in 'TV-Browser'"
														+ System
																.getProperty("line.separator")
														+ "2.) The settings in the application the nxtvepg itself");
								JOptionPane
										.showMessageDialog(
												null,
												failedMessage,
												mLocalizer
														.msg("errorTitle",
																"Severe Error in: Nxtvepg Data Plugin"),
												JOptionPane.ERROR_MESSAGE);
							}

						}
						fBackFile.delete();

					}

					// parse nxtvepg data

					if (!updateError) {
						SAXParserFactory factory = SAXParserFactory
								.newInstance();
						if (full) {
							monitor.setValue(1);
							NextViewDataServiceXMLHandler handler = new NextViewDataServiceXMLHandler(
									data);
							SAXParser saxParser = factory.newSAXParser();
							saxParser.parse(new File(outFile).toURI()
									.toString(), handler);
						} else {
							NextViewChannelFinder handler = new NextViewChannelFinder(
									data);
							SAXParser saxParser = factory.newSAXParser();
							saxParser.parse(new File(outFile).toURI()
									.toString(), handler);
						}
					}

				} catch (Exception e) {
					mLog.warning(e.toString());
				} catch (OutOfMemoryError oome) {
					String wMessage = "Java VM out of Memory in NextViewDataService"
							+ System.getProperty("line.separator")
							+ "Please try one of these:"
							+ System.getProperty("line.separator")
							+ "- reduce the filter 'expired display' in your nxtvepg rcfile to max. 2 days."
							+ System.getProperty("line.separator")
							+ "- increase memory of Java VM, i.e. run TV-Browser with 'javaw.exe -Xmx256M -jar tvbrowser.jar'."
							+ System.getProperty("line.separator")
							+ "- reduce the amount of channels(networks) in your nxtvepg rcfile (ini-file)."
							+ System.getProperty("line.separator");
					mLog.warning(wMessage);

					wMessage = mLocalizer
							.msg(
									"outOfMemMessage",
									"Java VM Out Of Memory!"
											+ System
													.getProperty("line.separator")
											+ System
													.getProperty("line.separator")
											+ "If this message appears on a regular basis, try the proposals for"
											+ System
													.getProperty("line.separator")
											+ "solution from page 'Nxtvepg Data Plugin' in the TV-Browser Wiki."
											+ System
													.getProperty("line.separator")
											+ "(You'll find a direct link to this page in the 'Nxtvepg Data Plugin' settings help.)");

					JOptionPane.showMessageDialog(null, wMessage, mLocalizer
							.msg("errorTitle",
									"Severe Error in: Nxtvepg Data Plugin"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		// Start thread:
		t.start();
		try {
			// 3000*100ms = 300 Sek max
			int count = 3000;
			while (t.isAlive() && (count > 0)) {
				Thread.sleep(100);
				count--;
			}
			if (t.isAlive()) {
				mLog
						.warning("NextViewDataService: update failed -- forcing thread to terminated");

				String wMessage = mLocalizer
						.msg("updateErrorMessage",
								"Receiving Program Data in Nxtvepg Data Plugin failed.");

				JOptionPane.showMessageDialog(null, wMessage, mLocalizer.msg(
						"errorTitle", "Error in: Nxtvepg Data Plugin"),
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e) {
		}
	}
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
    return new ChannelGroup[]{mNextViewChannelGroup};

  }

  /*
   * (non-Javadoc)
   *
   * @see devplugin.TvDataService#getAvailableGroups()
   */
  public ChannelGroup[] getAvailableGroups() {
    return new ChannelGroup[]{mNextViewChannelGroup};
  }


  /**
   * Add program data of alternative channel to the given day program
   *
   * @param dayProg the dayprog with nxtvepg data
   * @param actChannel the nxtvepg channel of the dayProg
   * @param alternativeChannel the channel with the additional data
   * @param date the day of the dayprogram
   * @return a dayprogramm with mixed data
   */
  private MutableChannelDayProgram mixedDayProgram (MutableChannelDayProgram dayProg, Channel actChannel, Channel alternativeChannel, Date date, int blockTime){

    if (alternativeChannel != null) {
      MutableChannelDayProgram changedAlt = nxtvepgSourcesUpdate.get(alternativeChannel.getUniqueId()+date.getDateString());
      Iterator<Program> alterIt;
      if (changedAlt!=null && changedAlt.getProgramCount()>0) {
        alterIt = changedAlt.getPrograms();
      } else{
        alterIt = getPluginManager().getChannelDayProgram(date, alternativeChannel);
      }
      if (alterIt != null && alterIt.hasNext()) {

        ArrayList <Program> altDayProg  = new ArrayList <Program>();
        while (alterIt.hasNext()){
          altDayProg.add(alterIt.next());
        }

        String[] mixFlags = new String[30];
        Properties alternativeChannelsDesc = new Properties();

        try {
          alternativeChannelsDesc.load(new FileInputStream(mixedChannelsDirName + "/nxtvepgAlternatives.properties"));
        } catch (IOException e) {
        }

        String[] buffer = alternativeChannelsDesc.getProperty(actChannel.getId()).split(";");

        for (int i = 0; i < 30; i++) {
          mixFlags[i] = buffer[i + 3];
        }

        if (dayProg == null || dayProg.getProgramCount() == 0) {
          // fill empty day program with alternative data
          if (dayProg == null) {
            dayProg = new MutableChannelDayProgram(date, actChannel);
          }
          for (int i = 0; i < altDayProg.size(); i++){
            MutableProgram currentProgram = new MutableProgram(actChannel, date, altDayProg.get(i).getHours(), altDayProg.get(i).getMinutes(), true);
            addAlternativeInfo(currentProgram, altDayProg.get(i), repairMix);
            currentProgram.setProgramLoadingIsComplete();
            dayProg.addProgram(currentProgram);
          }

        } else {
          // add alternative data to existing day program
          MutableChannelDayProgram prevDayProg = dayProg;
          dayProg = new MutableChannelDayProgram(date, actChannel);
          for (int i = 0; i < prevDayProg.getProgramCount(); i++) {
            Program prevProgram = prevDayProg.getProgramAt(i);
            MutableProgram currentProgram = new MutableProgram(actChannel, date, prevProgram.getHours(), prevProgram.getMinutes(), true);
            addAlternativeInfo(currentProgram, prevProgram, repairMix);

            // Add info, if program has not been repaired in the "insufficient work around"
            if (currentProgram.getStartTime() >= blockTime) {
              // looking for best fit
              int maxDiff = 1440;
              Program alternativeProgram = null;

              // test how often program is in dayprogram and find nearest alternative as best fit
              int occurrences2 = 0;
              for (int j = 0; j < altDayProg.size(); j++){
                if (compareTitle(currentProgram.getTitle(), altDayProg.get(j).getTitle())) {
                  occurrences2++;
                  int aktDiff = Math.abs(currentProgram.getStartTime() - altDayProg.get(j).getStartTime());
                  if (aktDiff < maxDiff) {
                    alternativeProgram = altDayProg.get(j);
                    maxDiff = aktDiff;
                  }
                }
              }

              // if alternativeProgram was the only match it's done. Otherwise...
              if (occurrences2 > 1) {
                // compare if number of transmissions in day program
                int occurrences1 = 0;
                for (int ii = 0; ii < prevDayProg.getProgramCount(); ii++) {
                  if (compareTitle(currentProgram.getTitle(), prevDayProg.getProgramAt(ii).getTitle())) {
                    occurrences1++;
                  }
                }
                if (occurrences2 == occurrences1) {
                  // if the number of transmissions is equal, then match transmissions in pairs
                  int counter1 = 0;
                  int counter2 = 0;
                  for (int ii = 0; ii <= i; ii++) {
                    if (compareTitle(currentProgram.getTitle(), prevDayProg.getProgramAt(ii).getTitle())) {
                      counter1++;
                    }
                  }
                  int index = 0;
                  while (index < altDayProg.size()&& counter2 < counter1){
                    if (compareTitle(currentProgram.getTitle(), altDayProg.get(index).getTitle())) {
                      counter2++;
                    }
                    if (counter1 == counter2) {
                      alternativeProgram = altDayProg.get(index);
                    }
                    index++;
                  }
                }
                //else{use best fit from above};
              }
              // when the best program match is found, the data mix finally can be done
              if (alternativeProgram != null) {
                addAlternativeInfo(currentProgram, alternativeProgram, mixFlags);
              }

            }
            currentProgram.setProgramLoadingIsComplete();
            dayProg.addProgram(currentProgram);
          }

          // if dayprogram is not complete, add the data from the alternative program for the rest of the day
          if (!prevDayProg.isComplete()) {
            Program lastProgram = prevDayProg.getProgramAt(prevDayProg.getProgramCount() - 1);
            int lastProgIndex = lastProgram.getStartTime() + lastProgram.getLength();
            for (int i = 0; i < altDayProg.size(); i++){
              if (altDayProg.get(i).getStartTime() > lastProgIndex) {
                MutableProgram currentProgram = new MutableProgram(actChannel, date, altDayProg.get(i).getHours(), altDayProg.get(i).getMinutes(), true);
                addAlternativeInfo(currentProgram, altDayProg.get(i), repairMix);
                currentProgram.setProgramLoadingIsComplete();
                dayProg.addProgram(currentProgram);
              }
            }
          }
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
  private void addAlternativeInfo (MutableProgram currentProgram, Program altProg, String []mixFlags){
    int version = (tvbrowser.TVBrowser.VERSION.getMajor()*100) +  tvbrowser.TVBrowser.VERSION.getMinor();

    setMixedTextField(currentProgram, altProg, ProgramFieldType.TITLE_TYPE, mixFlags[0]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.ORIGINAL_TITLE_TYPE, mixFlags[1]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.EPISODE_TYPE, mixFlags[2]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.ORIGINAL_EPISODE_TYPE, mixFlags[3]);
    if (altProg.getTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE)!= null && !altProg.getTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE).endsWith("WirSchauen")){
      setMixedTextField(currentProgram, altProg, ProgramFieldType.SHORT_DESCRIPTION_TYPE, mixFlags[4], System.getProperty("line.separator") +  System.getProperty("line.separator"));
    }
    if (altProg.getTextField(ProgramFieldType.DESCRIPTION_TYPE)!= null && !altProg.getTextField(ProgramFieldType.DESCRIPTION_TYPE).endsWith("WirSchauen")){
      setMixedTextField(currentProgram, altProg, ProgramFieldType.DESCRIPTION_TYPE, mixFlags[5], System.getProperty("line.separator") +  System.getProperty("line.separator"));
    }
    setMixedTextField(currentProgram, altProg, ProgramFieldType.ACTOR_LIST_TYPE, mixFlags[6]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.DIRECTOR_TYPE, mixFlags[7]);
    setMixedInfo(currentProgram, altProg, ProgramFieldType.INFO_TYPE, mixFlags[9]);
    setMixedIntField(currentProgram, altProg, ProgramFieldType.AGE_LIMIT_TYPE, mixFlags[10]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.URL_TYPE, mixFlags[11]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.GENRE_TYPE, mixFlags[12], ", ");
    setMixedTextField(currentProgram, altProg, ProgramFieldType.ORIGIN_TYPE, mixFlags[13]);
    setMixedIntField(currentProgram, altProg, ProgramFieldType.NET_PLAYING_TIME_TYPE, mixFlags[14]);
    setTimeField(currentProgram, altProg, ProgramFieldType.VPS_TYPE, mixFlags[15]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.SCRIPT_TYPE, mixFlags[16]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.REPETITION_OF_TYPE, mixFlags[17]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.MUSIC_TYPE, mixFlags[18]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.MODERATION_TYPE, mixFlags[19]);
    setMixedIntField(currentProgram, altProg, ProgramFieldType.PRODUCTION_YEAR_TYPE, mixFlags[20]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.REPETITION_ON_TYPE, mixFlags[21]);
    setMixedBinaryField(currentProgram, altProg, ProgramFieldType.PICTURE_TYPE, mixFlags[22]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.PICTURE_COPYRIGHT_TYPE, mixFlags[22]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.PICTURE_DESCRIPTION_TYPE, mixFlags[22]);
    setMixedIntField(currentProgram, altProg, ProgramFieldType.EPISODE_NUMBER_TYPE, mixFlags[23]);
    setMixedIntField(currentProgram, altProg, ProgramFieldType.EPISODE_TOTAL_NUMBER_TYPE, mixFlags[23]);
    setMixedIntField(currentProgram, altProg, ProgramFieldType.SEASON_NUMBER_TYPE, mixFlags[23]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.PRODUCER_TYPE, mixFlags[24]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.CAMERA_TYPE, mixFlags[25]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.CUTTER_TYPE, mixFlags[26]);
    setMixedTextField(currentProgram, altProg, ProgramFieldType.ADDITIONAL_PERSONS_TYPE, mixFlags[27]);
    setMixedIntField(currentProgram, altProg, ProgramFieldType.RATING_TYPE, mixFlags[28]);
    if (version >= 300) {
      setMixedTextField(currentProgram, altProg, ProgramFieldType.CUSTOM_TYPE, mixFlags[8], System.getProperty("line.separator") +  System.getProperty("line.separator"));
      setMixedTextField(currentProgram, altProg, ProgramFieldType.PRODUCTION_COMPANY_TYPE, mixFlags[29]);
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

  private void setMixedTextField (MutableProgram currentProgram, Program additionalProg, ProgramFieldType type, String mixFlag){
    setMixedTextField (currentProgram, additionalProg, type, mixFlag, "");
  }


  /**
   * Fill a program field of type text with mixed data
   * @param currentProgram the program to be created
   * @param prog1 the primary data source
   * @param prog2 the additional data source
   * @param type the name of the text field
   * @param mixFlag on of "replace", "none", "after" or "before"
   * @param delimiter the delimeter between current and alternative data (when using "before" or "after!)
   */

  private void setMixedTextField (MutableProgram currentProgram, Program additionalProg, ProgramFieldType type, String mixFlag, String delimiter){

    String info1 = currentProgram.getTextField(type);
    String info2 = additionalProg.getTextField(type);
    String retInfo = "";

    if (info1 != null && info2 != null && info1.length()>0 && info2.length()>0){
      if (mixFlag.equals("after")){
        retInfo = info1 + delimiter + info2;
      }
      if (mixFlag.equals("before")){
        retInfo = info2 + delimiter + info1;
      }
    }

    if (info2 != null && info2.length()>0 && (mixFlag.equals("replace") || info1==null || info1.length()<1)){
      retInfo = info2;
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
   * @param mixFlag one of "replace", "none" or "mix"
   */

  private void setMixedInfo (MutableProgram currentProgram, Program additionalProg, ProgramFieldType type, String mixFlag){

    int info1 = currentProgram.getIntField(type);
    int info2 = additionalProg.getIntField(type);

    if ((mixFlag.equals("mix")&& info1 >=0 && info2 >=0)){
      currentProgram.setInfo(HelperMethods.mixInfoBits(info1,info2));
    } else {
      if (info2 >=0 && (mixFlag.equals("replace") || info1<0)){
        currentProgram.setInfo(info2);
      }
    }
  }

  /**
   * Fill a program field of type text with mixed data
   * @param currentProgram the program to be created
   * @param prog1 the primary data source
   * @param prog2 the additional data source
   * @param type the name of the int field
   * @param mixFlag "replace" or "none
   */
  private void setMixedIntField (MutableProgram currentProgram, Program additionalProg, ProgramFieldType type, String mixFlag){

    int info1 = currentProgram.getIntField(type);
    int info2 = additionalProg.getIntField(type);

    if (info2 >=0 && (mixFlag.equals("replace") || info1<0)){
      currentProgram.setIntField(type,info2);
    }
  }

  /**
   * Fill a program field of type text with mixed data
   * @param currentProgram the program to be created
   * @param prog1 the primary data source
   * @param prog2 the additional data source
   * @param type the name of the time field
   * @param mixFlag "replace" or "none"
   */

  private void setTimeField (MutableProgram currentProgram, Program additionalProg, ProgramFieldType type, String mixFlag){

    int info1 = currentProgram.getTimeField(type);
    int info2 = additionalProg.getTimeField(type);

    if (info2 >=0 && (mixFlag.equals("replace") || info1<0)){
      currentProgram.setTimeField(type,info2);
    }
  }


  /**
   * Fill a program field of type binary with mixed data
   * @param currentProgram the program to be created
   * @param prog1 the primary data source
   * @param prog2 the additional data source
   * @param type the name of the binary field
   * @param mixFlag "replace" or "none"
   */
  private void setMixedBinaryField (MutableProgram currentProgram, Program additionalProg, ProgramFieldType type, String mixFlag){

    byte[] info1 = currentProgram.getBinaryField(type);
    byte[] info2 = additionalProg.getBinaryField(type);

    if (info2 != null && (mixFlag.equals("replace") || info1==null)){
      currentProgram.setBinaryField(type,info2);
    }
  }

  /**
   * replace diacritic characters by their respective counterpart without diacritics
   * @param char1
   * @return
   */
  private char replaceDiacritics(final char char1) {
    if (char1 == 'á' || char1 == 'à' || char1 == 'â') {
      return 'a';
    }
    if (char1 == 'é' || char1 == 'è' || char1 == 'ê') {
      return 'e';
    }
    if (char1 == 'í' || char1 == 'ì' || char1 == 'î') {
      return 'i';
    }
    if (char1 == 'ó' || char1 == 'ò' || char1 == 'ô') {
      return 'o';
    }
    if (char1 == 'ú' || char1 == 'ù' || char1 == 'û') {
      return 'u';
    }
    if (char1 == 'ç') {
      return 'c';
    }
    if (char1 == 'ñ') {
      return 'n';
    }
    return char1;
  }

  /**
   * Test, whether one program title is the short version of the other
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

    while (count1<=max1 && count2<=max2){
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
        char1 = replaceDiacritics(char1);
        char2 = replaceDiacritics(char2);
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


  /**
   * Repairs bad program entries like "Navy CIS 1,90 10,2 1,13 13,6"
   * @param dayProg the day program to be repaired
   * @param actChannel the channel of the day program
   * @param date the day of the damaged day program
   * @return the repaired day program
   */
  private MutableChannelDayProgram repairedSat1DayProgram(MutableChannelDayProgram dayProg, Channel actChannel, Date date){
    MutableChannelDayProgram newDayProg = new MutableChannelDayProgram (date, actChannel);
    Iterator <Program> oldDayProg = getPluginManager().getChannelDayProgram(date, actChannel);
    if (oldDayProg != null) {
      for (int j = 0; j < dayProg.getProgramCount(); j++) {
        String[] titleCheck = dayProg.getProgramAt(j).getTitle().split(" ");
        int lastIndex = titleCheck.length - 1;
        if (lastIndex > 0 && titleCheck[lastIndex].contains(",") && titleCheck[lastIndex - 1].contains(",") && !titleCheck[lastIndex].contains(" ") && !titleCheck[lastIndex - 1].contains(" ")) {
          int start = 0;
          int end = 1440;
          int akt = 0;
          if (newDayProg.getProgramCount() > 0) {
            Program lastProgram = newDayProg.getProgramAt(newDayProg.getProgramCount() - 1);
            start = lastProgram.getStartTime() + lastProgram.getLength();
          }
          if (j < dayProg.getProgramCount() - 1) {
            end = dayProg.getProgramAt(j + 1).getStartTime();
          }
          while (oldDayProg.hasNext() && akt < start) {
            Program nextTransmission = oldDayProg.next();
            akt = nextTransmission.getStartTime();
          }
          while (oldDayProg.hasNext() && akt < end) {
            Program nextTransmission = oldDayProg.next();
            akt = nextTransmission.getStartTime();
            MutableProgram newProg = new MutableProgram (actChannel, date, nextTransmission.getHours(), nextTransmission.getMinutes(), true);
            addAlternativeInfo(newProg, nextTransmission, repairMix);
            newProg.setProgramLoadingIsComplete();
            newDayProg.addProgram(newProg);
          }
        } else {
          MutableProgram newProg = new MutableProgram (actChannel, date, dayProg.getProgramAt(j).getHours(), dayProg.getProgramAt(j).getMinutes(), true);
          addAlternativeInfo(newProg, dayProg.getProgramAt(j),repairMix);
          newProg.setProgramLoadingIsComplete();
          newDayProg.addProgram(newProg);
        }
      }
    }
    return newDayProg;
  }


  /**
   * Searches for bad program titles like "Navy CIS 1,90 10,2 1,13 13,6"
   * Checks whether the last two words contain ","
   *
   * @param dayProg The dayprogram to be checked
   * @return true bad pattern is found on Sat1
   */
  private boolean sat1Error (MutableChannelDayProgram dayProg){
    if (dayProg != null && dayProg.getChannel().getId().equals("CNI0DB9")){
      int i = 0;
      while (i < dayProg.getProgramCount()){
        String [] titleCheck = dayProg.getProgramAt(i).getTitle().split(" ");
        int lastIndex = titleCheck.length -1;
        if (lastIndex>0 && titleCheck[lastIndex].contains(",") && titleCheck[lastIndex-1].contains(",") && !titleCheck[lastIndex].contains(" ") && !titleCheck[lastIndex-1].contains(" ")){
          return true;
        }
        i++;
      }
    }
    return false;
  }

  /**
   * Work around for insufficient nextvepg's "expired display" settings;
   * fills day program with stored data up if the is a gap at the beginning of the day
   * @param dayProg the day program to be repaired
   * @param channel of the day program
   * @param date the day (i.e normally today)
   * @return the repaired day program
   */
  private MutableChannelDayProgram fillWithStoredDayProgram (MutableChannelDayProgram dayProg, Channel channel, Date date){
    Iterator<Program> alterIt = getPluginManager().getChannelDayProgram(date, channel);
    if (alterIt != null) {
      int firstStart = dayProg.getProgramAt(0).getStartTime();
      int currentEnd = 0;
      while (alterIt.hasNext() && currentEnd < firstStart) {
        Program nextTransmission = alterIt.next();
        if (nextTransmission.getStartTime() < firstStart) {
          MutableProgram newProg = new MutableProgram (channel, date, nextTransmission.getHours(), nextTransmission.getMinutes(), true);
          addAlternativeInfo(newProg, nextTransmission, repairMix);
          newProg.setProgramLoadingIsComplete();
          dayProg.addProgram(newProg);
        }
        currentEnd = nextTransmission.getStartTime() + nextTransmission.getLength();
      }
    }
    return dayProg;
  }


  /**
   * Work around for insufficient nextvepg's "expired display" settings;
   * creates a valid day program with stored program data
   * @param channel the channel of the day program
   * @param date the day (i.e normally yesterday)
   * @return a valid day program
   */
  private MutableChannelDayProgram replaceWithStoredDayProgram (Channel channel, Date date){

    Iterator<Program> alterIt = getPluginManager().getChannelDayProgram(date, channel);
    if (alterIt != null && alterIt.hasNext()) {
      MutableChannelDayProgram newProg = new MutableChannelDayProgram (date, channel);
      while (alterIt.hasNext()) {
        Program altProg = alterIt.next();
        MutableProgram currentProgram = new MutableProgram(channel, date, altProg.getHours(), altProg.getMinutes(), true);
        addAlternativeInfo(currentProgram, altProg, repairMix);
        currentProgram.setProgramLoadingIsComplete();
        newProg.addProgram(currentProgram);
      }
      return newProg;
    }
    else{
      return null;
    }
  }

  /**
   * Return whether alternative icons should be used
   * @return true or false
   */
  public boolean useAlternativeIcons (){
    if (prop.getProperty(ALTERNATIVEICONS, "NO").equals("YES")){
      return true;
    }
    return false;
  }

  /**
   * Return whether alternative icons should be used
   * @return true or false
   */
  public boolean useAlternativeData (){
    if (prop.getProperty(DATAMIX, "NO").equals("YES")){
      return true;
    }
    return false;
  }

  /**
   * Generate OS specific property keys
   * @param name ; the original property key
   * @return the generated OS specific key
   */
  public static String OS_specific_Name(String name) {
    if (OperatingSystem.isWindows()) {
      return "WIN_" + name;
    } else if (OperatingSystem.isLinux()) {
      return "LIN_" + name;
    } else {
      return name;
    }
  }


}
