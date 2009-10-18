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
package notifyosdplugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.Icon;

import util.io.ExecutionHandler;
import util.misc.OperatingSystem;
import util.paramhandler.ParamParser;
import util.program.LocalPluginProgramFormating;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.Version;

public class NotifyOSDPlugin extends Plugin {
  private static final boolean IS_STABLE = false;
  private static final Version mVersion = new Version(2, 70, 0, IS_STABLE);

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(NotifyOSDPlugin.class);

  private static final String TARGET = "NOTIFYOSD_TARGET";

  private PluginInfo mPluginInfo;

  private NotifyOSDSettings mSettings;

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "NotifyOSD");
      final String desc = mLocalizer.msg("description", "Show notifications using NotifyOSD.");
      mPluginInfo = new PluginInfo(NotifyOSDPlugin.class, name, desc, "Michael Keppler", "GPL 3");
    }

    return mPluginInfo;
  }

  public boolean canReceiveProgramsWithTarget() {
    return OperatingSystem.isLinux();
  }

  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    if (canReceiveProgramsWithTarget()) {
      final ProgramReceiveTarget target = new ProgramReceiveTarget(this, mLocalizer.msg("targetName",
          "Show notification"), TARGET);
      return new ProgramReceiveTarget[] { target };
    }
    return null;
  }

  public boolean receivePrograms(final Program[] programArr, final ProgramReceiveTarget receiveTarget) {
    if (!canReceiveProgramsWithTarget()) {
      return false;
    }
    // notify-osd will always show only a single notification!
    if (programArr.length == 1) {
      showSingleNotification(programArr[0]);
    } else {
      showMultiNotification(programArr);
    }
    return true;
  }

  private void showMultiNotification(final Program[] programArr) {
    ParamParser parser = new ParamParser();
    final LocalPluginProgramFormating format = new LocalPluginProgramFormating(mLocalizer
        .msg("name", "NotifyOSD Multi"), "",
        "{leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")} {title}", "UTF-8");
    StringBuilder builder = new StringBuilder();
    for (Program program : programArr) {
      String entry = parser.analyse(format.getContentValue(), program);
      if (entry != null && entry.trim().length() > 0) {
        if (builder.length() > 0) {
          builder.append("\n");
        }
        builder.append(entry.trim());
      }
    }
    showNotification("TV-Browser", builder.toString());
  }

  protected void showSingleNotification(final Program program) {
    ParamParser parser = new ParamParser();
    final LocalPluginProgramFormating format = new LocalPluginProgramFormating(mLocalizer.msg("name",
        "NotifyOSD Single"), mSettings.getTitle(), mSettings.getDescription(),
        "UTF-8");
    String title = parser.analyse(format.getTitleValue(), program);
    if (title == null) {
      title = program.getTitle();
    }
    String description = parser.analyse(format.getContentValue(), program);
    if (description == null) {
      description = program.getShortInfo();
    }
    showNotification(title, description);
  }

  private void showNotification(String title, String body) {
    if (!notifyAvailable()) {
      return;
    }
    final File curDir = new File(".");
    ArrayList<String> command = new ArrayList<String>();
    command.add("notify-send");
    try {
      String iconPath = "--icon=" + curDir.getCanonicalPath() + "/imgs/tvbrowser128.png";
      command.add(iconPath);
    } catch (IOException e) {
      e.printStackTrace();
    }
    title = title.replace("\n", " ").trim();
    if (title.length() > 0) {
      command.add(encode(title));
    }
    body = body.trim();
    command.add(encode(body));
    final ExecutionHandler executer = new ExecutionHandler(command.toArray(new String[command.size()]));
    try {
      executer.execute();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String encode(final String text) {
    String sEncoding = "UTF8";
    ByteArrayOutputStream osByteArray = new ByteArrayOutputStream();
    Writer w;
    try {
      w = new OutputStreamWriter(osByteArray, sEncoding);
      w.write(text);
      w.close();
    } catch (UnsupportedEncodingException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return osByteArray.toString();
  }

  private boolean notifyAvailable() {
    final ExecutionHandler executionHandler = new ExecutionHandler("notify-send", "which");
    try {
      executionHandler.execute(true);
      String location = executionHandler.getInputStreamReaderThread().getOutput();
      if (location != null) {
        location = location.trim();
        if (location.length() > 0) {
          return true;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return false;
  }

  @Override
  protected String getMarkIconName() {
    return "notifyosdplugin/icons/16x16/actions/notify.png";
  }

  protected Icon getPluginIcon() {
    return Plugin.getPluginManager().getIconFromTheme(this, "actions", "notify", 16);
  }

  @Override
  public void loadSettings(final Properties properties) {
    mSettings = new NotifyOSDSettings(properties);
  }
  
  @Override
  public Properties storeSettings() {
    return mSettings.storeSettings();
  }
  
  @Override
  public SettingsTab getSettingsTab() {
    return new NotifyOSDSettingsTab(this, mSettings);
  }
}
