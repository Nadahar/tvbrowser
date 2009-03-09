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
package snarlplugin;

import java.io.File;
import java.io.IOException;

import util.misc.OperatingSystem;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.Version;

public class SnarlPlugin extends Plugin {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(SnarlPlugin.class);

  private static final Version mVersion = new Version(2, 70, 0);

  private static final String TARGET = "SNARL_TARGET";

  private PluginInfo mPluginInfo;

  private JarApplicationExecuter mExecuter;

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "Snarl Notifications");
      final String desc = mLocalizer.msg("description",
          "Show snarl notification as reminder for programs (only Windows).");
      mPluginInfo = new PluginInfo(SnarlPlugin.class, name, desc,
          "Michael Keppler", "GPL 3");
    }

    return mPluginInfo;
  }

  public boolean canReceiveProgramsWithTarget() {
    return OperatingSystem.isWindows();
  }

  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    if (canReceiveProgramsWithTarget()) {
      final ProgramReceiveTarget target = new ProgramReceiveTarget(this,
          mLocalizer.msg("targetName", "Snarl notification"), TARGET);
      return new ProgramReceiveTarget[] { target };
    }
    return null;
  }

  public boolean receivePrograms(final Program[] programArr,
      final ProgramReceiveTarget receiveTarget) {
    if (!canReceiveProgramsWithTarget()) {
      return false;
    }
    for (Program program : programArr) {
      showNotification(program);
    }
    return true;
  }

  private void showNotification(final Program program) {
    if (mExecuter == null) {
      mExecuter = new JarApplicationExecuter("snarl_command.exe");
    }
    final File curDir = new File(".");
    String dir = "";
    try {
      dir = "\"" + curDir.getCanonicalPath() + "\\imgs\\TVBrowser.ico\"";
    } catch (IOException e) {
      e.printStackTrace();
    }
    final String parameters = "/M \"TV-Browser\" \""
        + program.getChannel().getName() + " " + program.getTimeString()
        + "\\n" + program.getTitle() + "\" " + dir + " /T 30";
    mExecuter.execute(parameters);
  }

  @Override
  protected String getMarkIconName() {
    return "snarlplugin/icons/16x16/snarl.png";
  }

}
