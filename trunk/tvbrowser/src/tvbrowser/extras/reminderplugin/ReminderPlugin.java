/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package tvbrowser.extras.reminderplugin;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Properties;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.DataLine.Info;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;

import tvbrowser.core.TvDataUpdateListener;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.ConfigurationHandler;
import tvbrowser.extras.common.DataDeserializer;
import tvbrowser.extras.common.DataSerializer;
import util.exc.ErrorHandler;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuIf;
import devplugin.Plugin;
import devplugin.Program;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class ReminderPlugin implements ContextMenuIf {

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ReminderPlugin.class);

  private ReminderList mReminderList;
  private Properties mSettings;

  private static ReminderPlugin mInstance;
  private static String DATAFILE_PREFIX = "reminderplugin.ReminderPlugin";

  private ConfigurationHandler mConfigurationHandler;

  private ReminderPlugin() {
    mInstance = this;
    mConfigurationHandler = new ConfigurationHandler(DATAFILE_PREFIX);
    loadSettings();
    mReminderList = new ReminderList();
    mReminderList.setReminderTimerListener(new ReminderTimerListener(null,
        mSettings, mReminderList));
    loadFavorites();

    TvDataUpdater.getInstance().addTvDataUpdateListener(
        new TvDataUpdateListener() {
          public void tvDataUpdateStarted() {

          }

          public void tvDataUpdateFinished() {
            Program[] removedPrograms = mReminderList.updatePrograms();
            if (removedPrograms.length > 0) {
              RemovedProgramsDialog dlg;
              Window parent = UiUtilities.getBestDialogParent(null);
              if (parent instanceof JFrame) {
                dlg = new RemovedProgramsDialog((JFrame) parent,
                    removedPrograms);
              } else {
                dlg = new RemovedProgramsDialog((JDialog) parent,
                    removedPrograms);
              }
              util.ui.UiUtilities.centerAndShow(dlg);
            }
          }
        });

  }

  public static synchronized ReminderPlugin getInstance() {
    if (mInstance == null)
      new ReminderPlugin();
    return mInstance;
  }

  private void loadSettings() {

    try {
      Properties prop = mConfigurationHandler.loadSettings();
      loadSettings(prop);
    } catch (IOException e) {
      ErrorHandler.handle("Could not load reminder data.", e);
    }
  }

  private void loadFavorites() {
    try {
      mConfigurationHandler.loadData(new DataDeserializer() {
        public void read(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
          readData(in);
        }
      });
    } catch (IOException e) {
      ErrorHandler.handle("Could not load reminder data", e);
    }
  }

  public Properties getSettings() {
    return mSettings;
  }

  public void store() {

    try {
      mConfigurationHandler.storeData(new DataSerializer() {
        public void write(ObjectOutputStream out) throws IOException {
          writeData(out);
        }
      });
    } catch (IOException e) {
      ErrorHandler.handle("Could not store reminder data.", e);
    }

    try {
      mConfigurationHandler.storeSettings(mSettings);
    } catch (IOException e) {
      ErrorHandler.handle("Could not store reminder settings.", e);
    }
  }

  private void readData(ObjectInputStream in) throws IOException,
      ClassNotFoundException {

    in.readInt(); // version

    mReminderList.setReminderTimerListener(null);
    mReminderList.read(in);
    mReminderList.removeExpiredItems();
    mReminderList.setReminderTimerListener(new ReminderTimerListener(null,
        mSettings, mReminderList));
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2);
    mReminderList.writeData(out);
  }

  private void loadSettings(Properties settings) {
    if (settings == null) {
      settings = new Properties();
    }
    if (settings.getProperty("usemsgbox") == null) {
      settings.setProperty("usemsgbox", "true");
    }
    mSettings = settings;

  }

  public ActionMenu getContextMenuActions(final Frame parentFrame,
      final Program program) {
    if (mReminderList.contains(program)) {
      ContextMenuAction action = new ContextMenuAction();
      action.setText(mLocalizer.msg("pluginName", "Reminder"));
      action.setSmallIcon(IconLoader.getInstance().getIconFromTheme("apps",
          "appointment", 16));

      final ReminderListItem item = mReminderList.getReminderItem(program);
      String[] entries = ReminderFrame.REMIND_MSG_ARR;
      ActionMenu[] actions = new ActionMenu[entries.length];
      for (int i = 0; i < actions.length; i++) {
        final int minutes = ReminderFrame.REMIND_VALUE_ARR[i];
        ContextMenuAction a = new ContextMenuAction();
        a.setText(entries[i]);
        a.setActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (minutes == -1) {
              mReminderList.remove(program);
            } else {
              item.setMinutes(minutes);
            }
          }
        });
        actions[i] = new ActionMenu(a, minutes == item.getMinutes());
      }

      return new ActionMenu(action, actions);
    } else if ((program.isExpired() || program.isOnAir())
        && (!program.equals(Plugin.getPluginManager().getExampleProgram()))) {
      return null;
    } else {
      ContextMenuAction action = new ContextMenuAction();
      action.setText(mLocalizer.msg("contextMenuText", "Remind me"));
      action.setSmallIcon(IconLoader.getInstance().getIconFromTheme("actions",
          "appointment-new", 16));
      action.setActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          ReminderDialog dlg = new ReminderDialog(parentFrame, program,
              mSettings);
          UiUtilities.centerAndShow(dlg);
          if (dlg.getOkPressed()) {
            int minutes = dlg.getReminderMinutes();
            mReminderList.add(program, minutes);
            mReminderList.unblockProgram(program);
          }
          dlg.dispose();
        }
      });
      return new ActionMenu(action);
    }
  }

  /**
   * This method is invoked for multiple program execution.
   */
  public void receivePrograms(Program[] programArr) {
    String defaultReminderEntryStr = (String) mSettings
        .get("defaultReminderEntry");
    int minutes = 10;
    if (defaultReminderEntryStr != null) {
      try {
        int inx = Integer.parseInt(defaultReminderEntryStr);
        if (inx < ReminderDialog.SMALL_REMIND_VALUE_ARR.length) {
          minutes = ReminderDialog.SMALL_REMIND_VALUE_ARR[inx];
        }
      } catch (NumberFormatException e) {
        // ignore
      }
    }

    mReminderList.addAndCheckBlocked(programArr, minutes);
  }

  public ActionMenu getButtonAction(final Frame parentFrame) {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        JDialog dlg = new ReminderListDialog(parentFrame, mReminderList);
        dlg.setSize(600, 350);
        UiUtilities.centerAndShow(dlg);
        dlg.dispose();
      }
    };

    action.putValue(Action.NAME, mLocalizer.msg("buttonText", "Reminder list"));
    action.putValue(Action.SMALL_ICON, IconLoader.getInstance()
        .getIconFromTheme("apps", "appointment", 16));
    // action.putValue(BIG_ICON, createImageIcon("apps", "appointment", 22));
    action.putValue(Action.SHORT_DESCRIPTION, mLocalizer.msg("description",
        "Eine einfache Implementierung einer Erinnerungsfunktion."));

    return new ActionMenu(action);
  }

  /**
   * Plays a sound.
   * 
   * @param fileName
   *          The file name of the sound to play.
   * @return The sound Object.
   */
  public static Object playSound(final String fileName) {
    try {
      if (fileName.toLowerCase().endsWith(".mid")) {
        final Sequencer sequencer = MidiSystem.getSequencer();
        sequencer.open();

        final InputStream midiFile = new FileInputStream(fileName);
        sequencer.setSequence(MidiSystem.getSequence(midiFile));

        sequencer.start();

        new Thread() {
          public void run() {
            setPriority(Thread.MIN_PRIORITY);
            while (sequencer.isRunning()) {
              try {
                Thread.sleep(100);
              } catch (Exception ee) {}
            }

            try {
              sequencer.close();
              midiFile.close();
            } catch (Exception ee) {}
          }
        }.start();

        return sequencer;
      } else {
        AudioInputStream ais = AudioSystem.getAudioInputStream(new File(
            fileName));

        javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(
            Clip.class, ais.getFormat(), ((int) ais.getFrameLength() * ais
                .getFormat().getFrameSize()));

        final Clip clip = (Clip) AudioSystem.getLine(info);
        clip.open(ais);
        clip.start();
        return clip;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public ActionMenu getContextMenuActions(Program program) {
    return getContextMenuActions(null, program);
  }

  public String getId() {
    return DATAFILE_PREFIX;
  }

}