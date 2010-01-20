package mixedupdatelockplugin;

import java.io.File;
import java.util.Properties;

import tvdataservice.MutableChannelDayProgram;

import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.Version;

public class MixedUpdateLockPlugin extends devplugin.Plugin {

//private static final Logger mLog = java.util.logging.Logger.getLogger(MixedUpdateLockPlugin.class.getName());
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(MixedUpdateLockPlugin.class);
  private static MixedUpdateLockPlugin mInstance;
  private String lckFileName;

  public MixedUpdateLockPlugin() {
    mInstance = this;
    File storeDir = new File (getPluginManager().getTvBrowserSettings().getTvBrowserUserHome() + "/MixedChannels" );
    if (!storeDir.exists()){
      storeDir.mkdir();
    }
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

  public void handleTvDataUpdateFinished() {
    File lckFile = new File (lckFileName);
    if (lckFile.exists()){
      lckFile.delete();
    }
  }
  
  public void loadSettings(Properties settings) {
  }

  public Properties storeSettings() {
    return null;
  }


  /**
   * Gets the Version of the implemented API
   * Since TV-Browser 0.9.7 getAPIVersion must return 1.0 
   */
  public static Version getVersion() {
    return new Version(0, 0, 0, false, null);
  }

  public PluginInfo getInfo() {
    return new PluginInfo(MixedUpdateLockPlugin.class,
        mLocalizer.msg("name", "Data Mixer Lock"),
        mLocalizer.msg("desc", "Lock Update for the Data Mixing Plugins"),
    "jb");

  }

  public SettingsTab getSettingsTab() {
    return new MixedUpdateLockPluginSettingTab();
  }

  /**
   * Returns an Instance of this Plugin
   * 
   * @return Instance of this Plugin
   */
  public static MixedUpdateLockPlugin getInstance() {
    return mInstance;
  }


}