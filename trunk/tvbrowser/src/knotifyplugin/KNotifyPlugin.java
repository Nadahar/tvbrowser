/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package knotifyplugin;

import java.io.IOException;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import util.io.ExecutionHandler;
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
  private static final Version mVersion = new Version(2,61);

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
      final String kdeSession = System.getenv("KDE_FULL_SESSION");
      if (kdeSession != null) {
        return kdeSession.compareToIgnoreCase("true") == 0;
      }
    }catch(Exception e) {}
    return false;
  }

  @Override
  public PluginInfo getInfo() {
    final String name = mLocalizer.msg("pluginName", "KNotify");
    final String desc = mLocalizer.msg("description",
        "Sends all received programs to KNotify.");
    return new PluginInfo(this.getClass(), name, desc, "Michael Keppler",
        "GPL 3");
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
  public Icon[] getMarkIconsForProgram(final Program p) {
    return new Icon[] {new ImageIcon(ImageUtilities.createImageFromJar("knotifyplugin/knotify.png", KNotifySettingsTab.class))};
  }

  @Override
  public boolean receivePrograms(final Program[] programArr,
      final ProgramReceiveTarget receiveTarget) {
    if (mInitialized) {
      for (Program program : programArr) {
        sendToKNotify(mSettings, program);
      }
    }
    return true;
  }

  public void sendToKNotify(final Properties settings, final Program program) {
    try {
      final ExecutionHandler executionHandler = new ExecutionHandler("dcop",
          "which");
      executionHandler.execute(true);
      
      String dcopLocation = executionHandler.getInputStreamReaderThread().getOutput();
      
      if (dcopLocation != null) {
        dcopLocation = dcopLocation.trim();
        if (dcopLocation.length() > 0) {
          // create the notification message
          if (mParser == null) {
            mParser = new ParamParser();
          }
          final String title = mParser.analyse(settings.getProperty("title"),
              program);
          final String message = mParser.analyse(settings
              .getProperty("description"), program);
          
          // run the notification command
          final String[] command = { dcopLocation, "knotify", "Notify",
              "notify", "event", title, message, "", "", "16", "0" };
          new ExecutionHandler(command).execute();
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
  public void loadSettings(final Properties settings) {
    mSettings = settings;
    
    mSettings.setProperty("title",       mSettings.getProperty("title", "{channel_name}, {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")} - {title}"));
    mSettings.setProperty("description", mSettings.getProperty("description", "{splitAt(short_info,\"80\")}"));
  }

  @Override
  public Properties storeSettings() {
    return mSettings;
  }
  
}
