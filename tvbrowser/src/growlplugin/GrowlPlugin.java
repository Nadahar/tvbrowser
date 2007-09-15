/*
 * GrowlPlugin by Bodo Tasche
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
package growlplugin;

import java.util.Properties;

import util.ui.Localizer;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * This is the Growl-Plugin
 * 
 * It sends Growl-Notifications for each Program it receives.
 * @see http://growl.info
 * 
 * @author bodum
 */
public class GrowlPlugin extends Plugin {
  private static final Version mVersion = new Version(2,60);
  
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GrowlPlugin.class);
  /** The Growl-Container */
  private GrowlContainer mContainer;
  /** Was the System initialized correctly ? */
  private boolean mInitialized = false;
  /** Settings for this Plugin */
  private Properties mSettings;
  
  /**
   * Checks the OS and inititializes the System accordingly.
   *
   */
  public GrowlPlugin() {
    try {
      if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
        mContainer = new GrowlContainer();
        mInitialized = true;
      }      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#getInfo()
   */
  public PluginInfo getInfo() {
      String name = mLocalizer.msg("pluginName", "Growl Notification");
      String desc = mLocalizer.msg("description",
              "Sends all received Programs to Growl.");
      String author = "Bodo Tasche";
      return new PluginInfo(this, name, desc, author);
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#canReceivePrograms()
   */
  public boolean canReceivePrograms() {
      return mInitialized;
  }    
  
  /**
   * This method is invoked for multiple program execution.
   * 
   * @see #canReceivePrograms()
   */
  public void receivePrograms(Program[] programArr) {
    
    if (mInitialized) {
      for (int i = 0; i < programArr.length;i++)
        mContainer.notifyGrowl(mSettings, programArr[i]);
    }
     
  }

  /**
   * Create the Settings-Tab
   */
  public SettingsTab getSettingsTab() {
    return new GrowlSettingsTab(this, mInitialized, mSettings);
  }
  
  /**
   * Load the Settings for this Plugin and
   * create Default-Values if nothing was set
   */
  public void loadSettings(Properties settings) {
    mSettings = settings;
    
    mSettings.setProperty("title",       mSettings.getProperty("title", "{leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")} {title}"));
    mSettings.setProperty("description", mSettings.getProperty("description", "{channel_name}\n{short_info}"));
  }
  
  /**
   * Return the GrowlContainer that is currently used
   * @return GrowlContainer that is used
   */
  public GrowlContainer getContainer() {
    return mContainer;
  }
  
  /**
   * Store the Settings
   */
  public Properties storeSettings() {
    return mSettings;
  }
}