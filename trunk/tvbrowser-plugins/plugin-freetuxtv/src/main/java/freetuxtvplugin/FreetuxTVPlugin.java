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
package freetuxtvplugin;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import util.io.ExecutionHandler;
import util.misc.OperatingSystem;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.Version;

/**
 * @author bananeweizen
 *
 */
public final class FreetuxTVPlugin extends Plugin {
  private static final boolean IS_STABLE = false;

  private static final Version mVersion = new Version(2, 70, 0, IS_STABLE);

  private static final String TARGET = "FREETUXTV_TARGET";

  private static final Logger mLog = Logger.getLogger(FreetuxTVPlugin.class.getName());

  /**
   * created lazily on first access
   */
  private PluginInfo mPluginInfo = null;

  /**
   * loaded lazily on first access
   */

  private ImageIcon mSmallIcon;

  private ImageIcon mLargeIcon;

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(FreetuxTVPlugin.class);

  private static final String COMMAND = "freetuxtv";

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "FreetuxTV");
      final String desc = mLocalizer.msg("description", "Shows programs in FreetuxTV.");
      mPluginInfo = new PluginInfo(FreetuxTVPlugin.class, name, desc, "Michael Keppler", "GPL 3");
    }

    return mPluginInfo;
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    if (program != null) {
      Channel channel = program.getChannel();
      if (channel != null) {
        return getContextMenuActions(channel);
      }
    }
    return null;
  }

  public ActionMenu getContextMenuActions(final Channel channel) {
    if (!OperatingSystem.isLinux()) {
      return null;
    }
    if (channel != null) {
      return getFreetuxTVAction(channel);
    }
    return null;
  }

  private ActionMenu getFreetuxTVAction(final Channel channel) {
    final Action action = new AbstractAction(mLocalizer.msg("contextMenu", "Show in FreetuxTV")) {

      public void actionPerformed(final ActionEvent e) {
        switchToChannel(channel);
      }
    };
    action.putValue(Action.SMALL_ICON, getPluginIcon(16));
    action.putValue(BIG_ICON, getPluginIcon(22));
    return new ActionMenu(action);
  }

  public Icon getPluginIcon(final int size) {
    if (mSmallIcon == null) {
      mSmallIcon = createImageIcon("actions", "freetuxtv", 16);
      mLargeIcon = createImageIcon("actions", "freetuxtv", 22);
    }
    if (size == 22) {
      return mLargeIcon;
    }
    return mSmallIcon;
  }

  public boolean canReceiveProgramsWithTarget() {
    return OperatingSystem.isLinux();
  }

  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    if (canReceiveProgramsWithTarget()) {
      final ProgramReceiveTarget target = new ProgramReceiveTarget(this, mLocalizer.msg("contextMenu",
          "Show in FreetuxTV"), TARGET);
      return new ProgramReceiveTarget[] { target };
    }
    return null;
  }

  public boolean receivePrograms(final Program[] programArr, final ProgramReceiveTarget receiveTarget) {
    if (!canReceiveProgramsWithTarget()) {
      return false;
    }
    switchToChannel(programArr[0].getChannel());
    return true;
  }

  private void switchToChannel(final Channel channel) {
    if (!freetuxtvAvailable()) {
      return;
    }
    ArrayList<String> command = new ArrayList<String>();
    command.add(COMMAND);
    command.add("--open-channel \"" + channel.getName() + "\"");
    final ExecutionHandler executer = new ExecutionHandler(command.toArray(new String[command.size()]));
    try {
      executer.execute();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean freetuxtvAvailable() {
    final ExecutionHandler executionHandler = new ExecutionHandler(COMMAND, "which");
    try {
      executionHandler.execute(true);
      try {
        executionHandler.getInputStreamReaderThread().join(2000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      String location = executionHandler.getInputStreamReaderThread().getOutput();
      if (location != null) {
        location = location.trim();
        if (location.length() > 0) {
          return true;
        }
      }
      else {
        mLog.warning("'" + COMMAND + "' command not found");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

}
