package mixeddataautoupdateplugin;


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


public class MixedDataAutoUpdatePlugin extends devplugin.Plugin {

//private static final Logger mLog = java.util.logging.Logger.getLogger(MixedDataAutoUpdatePlugin.class.getName());
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(MixedDataAutoUpdatePlugin.class);
  private static MixedDataAutoUpdatePlugin mInstance;
  private String mpropFile;
  private Properties changedList;
  private String lckFileName;

  public MixedDataAutoUpdatePlugin() {
    mInstance = this;
    File storeDir = new File (getPluginManager().getTvBrowserSettings().getTvBrowserUserHome() + "/MixedChannels" );
    if (!storeDir.exists()){
      storeDir.mkdir();
    }
    mpropFile = storeDir + "/MixedDataSavings.prop";
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
    if (!channelId.startsWith("mixeddataservice.MixedDataService")) {
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
        Date yesterDay = Date.getCurrentDate().addDays(-1);
        for (int i = 0; i < daysList.size(); i++) {
          if (daysList.get(i).length()==8) {
            if (yesterDay.compareTo(Date.createYYYYMMDD(daysList.get(i).substring(0, 4) + "-" + daysList.get(i).substring(4, 6) + "-" + daysList.get(i).substring(6, 8), "-")) > 0) {
              daysList.remove(i);
            }
          } else {
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
    try {
      FileOutputStream propfile = new FileOutputStream(mpropFile);
      updateList.store(propfile, "Changed DayPrograms to be checked for Update");
      propfile.close();     
    } catch (IOException e) {
      e.printStackTrace();
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
    return new Version(1, 0, 1, true, null);
  }

  public PluginInfo getInfo() {
    return new PluginInfo(MixedDataAutoUpdatePlugin.class,
        mLocalizer.msg("name", "Mixed Data Auto Update"),
        mLocalizer.msg("desc", "Auto Update for the Mixed Data Plugin"),
    "jb");

  }

  public SettingsTab getSettingsTab() {
    return new MixedDataAutoUpdatePluginSettingTab();
  }

  /**
   * Returns an Instance of this Plugin
   * 
   * @return Instance of this Plugin
   */
  public static MixedDataAutoUpdatePlugin getInstance() {
    return mInstance;
  }


}
