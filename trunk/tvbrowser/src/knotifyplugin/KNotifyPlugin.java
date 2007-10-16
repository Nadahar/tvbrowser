/*
 * KNotifyPlugin by Michael Keppler
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
 *     $Date:  $
 *   $Author:  $
 * $Revision:  $
 */

package knotifyplugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import util.paramhandler.ParamParser;
import util.ui.ImageUtilities;
import util.ui.Localizer;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * @author bananeweizen
 *
 */
public class KNotifyPlugin extends Plugin {
  /**
   * logging
   */
  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(KNotifyPlugin.class.getName());
  
  /**
   * translation
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(KNotifyPlugin.class);
  
  /**
   * plugin version
   */
  private static final Version mVersion = new Version(2,60);

  boolean mInitialized = false;

  private Properties mSettings;

  private ParamParser mParser;

  public KNotifyPlugin() {
    try {
      if (isKDE()) {
        mInitialized = true;
      }      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private boolean isKDE() {
    try {
      String kdeSession = System.getenv("KDE_FULL_SESSION");
      if (kdeSession != null) {
        return kdeSession.compareToIgnoreCase("true") == 0;
      }
    }catch(Exception e) {}
    return false;
  }

  @Override
  public PluginInfo getInfo() {
    String name = mLocalizer.msg("pluginName", "KNotify");
    String desc = mLocalizer.msg("description", "Sends all received programs to KNotify.");
    String author = "Michael Keppler";
    return new PluginInfo(this.getClass(), name, desc, author);
  }

  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  public static Version getVersion() {
    return mVersion;
  }

  /* (non-Javadoc)
   * @see devplugin.Plugin#getMarkIconsForProgram(devplugin.Program)
   */
  public Icon[] getMarkIconsForProgram(Program p) {
    return new Icon[] {new ImageIcon(ImageUtilities.createImageFromJar("knotifyplugin/knotify.png", KNotifySettingsTab.class))};
  }

  @Override
  public boolean receivePrograms(Program[] programArr,
      ProgramReceiveTarget receiveTarget) {
    if (mInitialized) {
      for (Program program : programArr) {
        sendToKNotify(mSettings, program);
      }
    }
    return true;
  }

  public void sendToKNotify(Properties settings, Program program) {
    try {
      Process whichProc = Runtime.getRuntime().exec("which dcop");
      InputStream in = whichProc.getInputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(in));                
      String dcopLocation = br.readLine();
      br.close();
      if (dcopLocation != null) {
        dcopLocation = dcopLocation.trim();
        if (dcopLocation.length() > 0) {
          // create the notification message
          if (mParser == null) {
            mParser = new ParamParser();
          }
          String title = mParser.analyse(settings.getProperty("title"), program);
          String message = mParser.analyse(settings.getProperty("description"), program);
          
          // run the notification command
          String[] command = {dcopLocation, "knotify", "Notify", "notify", "event", title, message, "", "", "16", "0"};
          Runtime.getRuntime().exec(command);
        }
      }
      else {
        mLog.warning("dcop command not found");
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new KNotifySettingsTab(this, mInitialized, mSettings);
  }

  /**
   * Load the settings for this plugin and create default values if nothing was set
   */
  public void loadSettings(Properties settings) {
    mSettings = settings;
    
    mSettings.setProperty("title",       mSettings.getProperty("title", "{channel_name}, {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")} - {title}"));
    mSettings.setProperty("description", mSettings.getProperty("description", "{splitAt(short_info,\"80\")}"));
  }

  @Override
  public Properties storeSettings() {
    return mSettings;
  }
  
}
