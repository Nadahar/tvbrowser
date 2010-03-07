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
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import util.io.ExecutionHandler;
import util.misc.OperatingSystem;
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
  private static final Logger mLog = Logger.getLogger(KNotifyPlugin.class.getName());

  /**
   * translation
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(KNotifyPlugin.class);

  /**
   * plugin version
   */
  private static final Version mVersion = new Version(3,0);

  private static final String TARGET = "KNOTIFY_TARGET";

  boolean mInitialized = false;

  private KNotifySettings mSettings;

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
    return OperatingSystem.isKDE();
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

  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    if (canReceiveProgramsWithTarget()) {
      final ProgramReceiveTarget target = new ProgramReceiveTarget(this,
          mLocalizer.msg("targetName", "Show with KNotify"), TARGET);
      return new ProgramReceiveTarget[] { target };
    }
    return null;
  }


  public static Version getVersion() {
    return mVersion;
  }

  public Icon[] getMarkIconsForProgram(final Program p) {
    return new Icon[] {new ImageIcon(ImageUtilities.createImageFromJar("knotifyplugin/knotify.png", KNotifySettingsTab.class))};
  }

  @Override
  public boolean receivePrograms(final Program[] programArr,
      final ProgramReceiveTarget receiveTarget) {
    if (mInitialized) {
      for (Program program : programArr) {
        sendToKNotify(program);
      }
    }
    return true;
  }

  public void sendToKNotify(final String titleFormat, final String descriptionFormat, final Program program) {
    try {
      final ExecutionHandler executionHandler = new ExecutionHandler("dcop",
          "which");
      executionHandler.execute(true);

      String dcopLocation = executionHandler.getOutput();

      if (dcopLocation != null) {
        dcopLocation = dcopLocation.trim();
        if (dcopLocation.length() > 0) {
          // create the notification message
          if (mParser == null) {
            mParser = new ParamParser();
          }
          final String title = mParser.analyse(titleFormat,program);
          final String message = mParser.analyse(descriptionFormat, program);

          // run the notification command
          final String[] command = { dcopLocation, "knotify", "Notify",
              "notify", "event", title, message, "", "", "16", "0" };
          new ExecutionHandler(command).execute();
        }
      }
      else {
        mLog.warning("'dcop' command not found");
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

public void sendToKNotify(final Program program) {
  sendToKNotify(mSettings.getTitle(), mSettings.getDescription(), program);
}

  @Override
  public SettingsTab getSettingsTab() {
    return new KNotifySettingsTab(this, mInitialized, mSettings);
  }

  /**
   * Load the settings for this plugin and create default values if nothing was set
   */
  public void loadSettings(final Properties properties) {
    mSettings = new KNotifySettings(properties);
  }

  @Override
  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

}
