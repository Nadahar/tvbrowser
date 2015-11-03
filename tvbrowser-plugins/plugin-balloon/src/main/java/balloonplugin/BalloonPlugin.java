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
package balloonplugin;

import java.awt.TrayIcon.MessageType;

import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.Version;

public class BalloonPlugin extends Plugin {

  private static final boolean PLUGIN_IS_STABLE = true;
  private static final Version PLUGIN_VERSION = new Version(2, 80, 0, PLUGIN_IS_STABLE);

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
  .getLocalizerFor(BalloonPlugin.class);

  private static final String TARGET = "BALLOON_TARGET";

  private PluginInfo mPluginInfo;

  public static Version getVersion() {
    return PLUGIN_VERSION;
  }

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "Balloon Tips");
      final String desc = mLocalizer
          .msg("description",
              "Show balloon tip as reminder for programs.");
      mPluginInfo = new PluginInfo(BalloonPlugin.class, name, desc,
          "Michael Keppler", "GPL 3");
    }

    return mPluginInfo;
  }

  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    if (canReceiveProgramsWithTarget()) {
      final ProgramReceiveTarget target = new ProgramReceiveTarget(this,
          mLocalizer.msg("targetName", "Show balloon tip"), TARGET);
      return new ProgramReceiveTarget[] { target };
    }
    return null;
  }

  public boolean receivePrograms(final Program[] programArr,
      final ProgramReceiveTarget receiveTarget) {
    if (!canReceiveProgramsWithTarget()) {
      return false;
    }
    
    Thread showNotifications = new Thread() {
	  @Override
	  public void run() {
        for (Program program : programArr) {
          showNotification(program);
          try {
            sleep(5000);
          } catch (InterruptedException e) {}
        }
      }
    };
    showNotifications.start();
    
    return true;
  }

  private void showNotification(final Program program) {
    getPluginManager().showBalloonTip(program.getChannel().getName() + " "
        + program.getTimeString() + " (" + program.getDateString()+")", program.getTitle(), MessageType.INFO);
  }

  @Override
  protected String getMarkIconName() {
    return "balloonplugin/icons/16x16/balloon.png";
  }

}
