package nextviewdataupdatewatcherplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import tvdataservice.MutableChannelDayProgram;

import devplugin.ChannelDayProgram;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.Version;

public class NextViewDataUpdateWatcherPlugin extends devplugin.Plugin {

//private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(NextViewDataUpdateWatcherPlugin.class.getName());
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(NextViewDataUpdateWatcherPlugin.class);
  private static NextViewDataUpdateWatcherPlugin mInstance;
  private String mpropFile;
  private Properties changedList;
  private String lckFileName;


  public NextViewDataUpdateWatcherPlugin() {
    mInstance = this;
    File storeDir = new File (getPluginManager().getTvBrowserSettings().getTvBrowserUserHome() + "/MixedChannels" );
    if (!storeDir.exists()){
      storeDir.mkdir();
    }
    mpropFile = storeDir + "/NxtvepgDataSavings.prop";
    changedList = new Properties();
    lckFileName = storeDir + "/datamix.lck";
    File lckFile = new File (lckFileName);
    if (!lckFile.exists()){
      try {
        lckFile.createNewFile();
      } catch (Exception e) {
      }
    }
  }

  public void handleTvDataAdded(MutableChannelDayProgram newProg) {
    File lckFile = new File (lckFileName);
    if (!lckFile.exists()){
      try {
        lckFile.createNewFile();
      } catch (Exception e) {
      }
    }
  }

  
  public void handleTvDataAdded(ChannelDayProgram newProg) {

    String channelId = newProg.getChannel().getUniqueId();
    if (!channelId.startsWith("nextviewdataservice.NextViewDataService")) {
      if (changedList.size() == 0) {
        loadSettings(null);
        storeNewSettings(new Properties());
      }
      String newDate = newProg.getDate().getDateString();
      String daysString = changedList.getProperty(channelId);
      if (daysString == null) {
        daysString = newDate;
      } else {
        if (!daysString.contains(newDate)) {
          daysString = daysString + ";" + newDate;
        }
        ArrayList<String> daysList = new ArrayList<String>(Arrays.asList(daysString.split(";")));
        Date[] days = new Date[daysList.size()];
        for (int i = 0; i < days.length; i++) {
          days[i] = Date.createYYYYMMDD(daysList.get(i).substring(0, 4) + "-" + daysList.get(i).substring(4, 6) + "-" + daysList.get(i).substring(6, 8), "-");
        }
        Date yesterDay = Date.getCurrentDate().addDays(-1);
        for (int i = daysList.size() - 1; i >= 0; i--) {
          if (yesterDay.compareTo(days[i]) > 0) {
            daysList.remove(i);
          }
        }
        if (daysList.size() == 0) {
          changedList.remove(channelId);
        } else {
          StringBuffer daysBuffer = new StringBuffer(daysList.get(0));
          for (int i = 1; i < daysList.size(); i++) {
            daysBuffer.append(";" + daysList.get(i));
          }
          daysString = daysBuffer.toString();
        }
      }
      changedList.setProperty(channelId, daysString);
    }
  }


  public void handleTvDataUpdateFinished() {
    storeNewSettings(changedList);
    changedList = new Properties();
    File lckFile = new File (lckFileName);
    if (lckFile.exists()){
      lckFile.delete();
    }
  }
 

  public void loadSettings(Properties settings) {
    changedList = new Properties();
    if (new File(mpropFile).exists()) {
      try {
        InputStream reader = new FileInputStream(mpropFile);
        changedList.load(reader);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }  
  }


  private void storeNewSettings (Properties updateList){
    try{
      updateList.store(new FileOutputStream(mpropFile), "Changed DayPrograms to be checked for Update");
    } catch (IOException e) {
    }

  }

  public Properties storeSettings() {
    return null;
  }



  /**
   * Gets the Version of the implemented API
   * Since TV-Browser 0.9.7 getAPIVersion must return 1.0 
   */
  public static Version getVersion() {
    return new Version(0, 0, 1, false, null);
  }

  public PluginInfo getInfo() {
    return new PluginInfo(NextViewDataUpdateWatcherPlugin.class,
        mLocalizer.msg("name", "Nxtvepg Update Watcher"),
        mLocalizer.msg("desc", "Blocks Auto Update of the Nxtvepg Data Plugin while TV-Browser is updating."),
    "jb");

  }

  public SettingsTab getSettingsTab() {
    return new NextViewDataUpdateWatcherPluginSettingTab();
  }

  /**
   * Returns an Instance of this Plugin
   * 
   * @return Instance of this Plugin
   */
  public static NextViewDataUpdateWatcherPlugin getInstance() {
    return mInstance;
  }


}