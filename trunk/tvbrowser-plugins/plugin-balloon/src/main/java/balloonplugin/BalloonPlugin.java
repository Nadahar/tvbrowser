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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import util.io.ExecutionHandler;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.Version;

public class BalloonPlugin extends Plugin {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(BalloonPlugin.class);

  private static final Version mVersion = new Version(2, 70, 0);

  private static final String BALLOON_TARGET = "BALLOON_TARGET";

  private PluginInfo mPluginInfo;

  private String mPath;

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "Balloon Plugin");
      final String desc = mLocalizer.msg("description",
          "Show balloon tip as reminder for programs.");
      final String author = "Michael Keppler";

      mPluginInfo = new PluginInfo(BalloonPlugin.class, name, desc, author);
    }

    return mPluginInfo;
  }

  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    ProgramReceiveTarget target = new ProgramReceiveTarget(this, mLocalizer
        .msg("targetName", "Show balloon tip"), BALLOON_TARGET);
    return new ProgramReceiveTarget[] { target };
  }

  public boolean receivePrograms(Program[] programArr,
      ProgramReceiveTarget receiveTarget) {
    for (Program program : programArr) {
      showBalloon(program);
    }
    return true;
  }

  private void showBalloon(Program program) {
    if (mPath == null) {
      mPath = extractNotifier();
    }
    if (mPath != null) {
      String params = "/m \"" + program.getChannel().getName() + " "
          + program.getTimeString() + "\\n" + program.getTitle()
          + "\" /p \"TV-Browser\" /d 600000";
      ExecutionHandler exec = new ExecutionHandler(params, mPath);
      try {
        exec.execute();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private String extractNotifier() {
    File executable;
    try {
      // Get input stream from jar resource
      final InputStream inputStream = getClass().getResource("notifu.exe")
          .openStream();

      // Copy resource to file system in a temporary folder with a unique name
      executable = File.createTempFile("tvbrowser_balloon_", ".exe");
      final FileOutputStream outputStream = new FileOutputStream(executable);
      final byte[] array = new byte[8192];
      int read = 0;
      while ((read = inputStream.read(array)) > 0) {
        outputStream.write(array, 0, read);
      }
      outputStream.close();

      // Delete on exit
      executable.deleteOnExit();
      // getPluginManager().deleteFileOnNextStart(executable.getPath());
    } catch (Throwable e) {
      e.printStackTrace();
      return null;
    }
    return executable.getPath();
  }

}
