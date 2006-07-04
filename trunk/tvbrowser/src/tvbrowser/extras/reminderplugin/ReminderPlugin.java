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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;


import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import tvbrowser.core.TvDataUpdateListener;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.ConfigurationHandler;

import tvbrowser.ui.mainframe.MainFrame;
import util.exc.ErrorHandler;
import util.ui.UiUtilities;
import devplugin.*;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class ReminderPlugin {

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ReminderPlugin.class);

   private java.util.logging.Logger mLog
      = java.util.logging.Logger.getLogger(ReminderPlugin.class.getName());

  private ReminderList mReminderList;
  private Properties mSettings;

  private static ReminderPlugin mInstance;
  private static String DATAFILE_PREFIX = "reminderplugin.ReminderPlugin";
  private static String DATAFILE_NAME = "reminder.dat";

  private ConfigurationHandler mConfigurationHandler;

  private PluginTreeNode mRootNode;
  
  private boolean mHasRightToStartTimer = false;
  
  /** The IDs of the plugins that should receive the favorites. */
  private String[] mClientPluginIdArr;

  private ReminderPlugin() {
    mInstance = this;
    mConfigurationHandler = new ConfigurationHandler(DATAFILE_PREFIX);
    loadSettings();
    mReminderList = new ReminderList();
    mReminderList.setReminderTimerListener(new ReminderTimerListener(mSettings, mReminderList));
    loadReminderData();

    mRootNode = new PluginTreeNode(mLocalizer.msg("pluginName","Reminder"));
    updateRootNode();

    TvDataUpdater.getInstance().addTvDataUpdateListener(
        new TvDataUpdateListener() {
          public void tvDataUpdateStarted() {

          }

          public void tvDataUpdateFinished() {
            Program[] removedPrograms = mReminderList.updatePrograms();
            if (removedPrograms.length > 0) {
              RemovedProgramsDialog dlg;
              Window parent = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
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

  public String toString() {
    return mLocalizer.msg("pluginName","Reminder");
  }
  
  /**
   * Is been called by TVBrowser when the TV-Browser start is finished.
   */
  public void handleTvBrowserStartFinished() {
    mHasRightToStartTimer = true;
    mReminderList.removeExpiredItems();
    mReminderList.startTimer();
  }
  
  /**
   * Is used by the ReminderList to track if
   * the TV-Browser start was finished.
   * (When it's finished then the Timer is allowed to start.)
   * 
   * @return If the Timer is allowed to start.
   */
  protected boolean isAllowedToStartTimer() {
    return mHasRightToStartTimer; 
  }
  
  private void loadSettings() {

    try {
      Properties prop = mConfigurationHandler.loadSettings();
      loadSettings(prop);
    } catch (IOException e) {
      ErrorHandler.handle("Could not load reminder data.", e);
    }

  }


  private ObjectInputStream getObjectInputStream(File f) throws IOException {
    return new ObjectInputStream(new BufferedInputStream(new FileInputStream(f), 0x4000));
  }

  private void loadReminderData() {
    try {


      File newFile = new File(Settings.getUserSettingsDirName(), DATAFILE_NAME);

      if (newFile.exists()) {
        readData(getObjectInputStream(newFile));
      }
      else {
        tryToReadDataFromPreviousVersions();
      }
    mReminderList.removeExpiredItems();
    mReminderList.setReminderTimerListener(new ReminderTimerListener(
        mSettings, mReminderList));

    } catch (ClassNotFoundException e) {
      ErrorHandler.handle("Could not load reminder data", e);
    } catch (IOException e) {
      ErrorHandler.handle("Could not load reminder data", e);
    }
  }


  private void tryToReadDataFromPreviousVersions() {
    boolean oldDataRead = false;
    try {
      File nodeFile = new File(Settings.getUserSettingsDirName(), "java.reminderplugin.ReminderPlugin.node");

      if (nodeFile.exists()) {
        readReminderFromTVBrowser21and20(getObjectInputStream(nodeFile));
        oldDataRead = true;
        nodeFile.delete();
      }
      File datFile = new File(Settings.getUserSettingsDirName(), "java.reminderplugin.ReminderPlugin.dat");
      if (datFile.exists()) {
        if (!oldDataRead) {
          readReminderFromBeforeTVBrowser20(getObjectInputStream(datFile));
        }
        datFile.delete();
      }
    }catch(IOException e) {
      mLog.log(Level.WARNING, "Could not read data from previous version", e);
    }catch(ClassNotFoundException e) {
      mLog.log(Level.WARNING, "Could not read data from previous version", e);
    }
  }

  public Properties getSettings() {
    return mSettings;
  }
  
  public void pauseRemider() {
    mReminderList.stopTimer();
  }

  public void store() {
    ObjectOutputStream out = null;
    try {
      String userDirectoryName = Settings.getUserSettingsDirName();
      File userDirectory = new File(userDirectoryName);
      File tmpDatFile = new File(userDirectory, DATAFILE_NAME + ".temp");
      File datFile = new File(userDirectory, DATAFILE_NAME);
      out = new ObjectOutputStream(new FileOutputStream(tmpDatFile));
      writeData(out);
      out.close();
      
      datFile.delete();
      tmpDatFile.renameTo(datFile);
    } catch (IOException e) {
      ErrorHandler.handle("Could not store reminder data.", e);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch(IOException e) {
          // ignore
        }
      }
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

  }

  private void readReminderFromTVBrowser21and20(ObjectInputStream in) throws IOException, ClassNotFoundException {

    int cnt = in.readInt();
    for (int i=0; i<cnt; i++) {
      int type = in.readInt();
      if (type == 2) {     // Node.PROGRAM
        ProgramItem item = new ProgramItem();
        item.read(in);
        String m = item.getProperty("minutes");
        int minutes;
        try {
          minutes = Integer.parseInt(m);
        }catch(NumberFormatException e) {
          minutes = 10;
        }
        Program program = item.getProgram();
        
        if(program != null)
          mReminderList.add(program, minutes);

        in.readInt();  // cnt (should be 0)

      }
    }
    in.close();
  }

  private void readReminderFromBeforeTVBrowser20(ObjectInputStream in) throws IOException, ClassNotFoundException {
     int version = in.readInt();
    if (version == 1) {
      int size = in.readInt();
      for (int i = 0; i < size; i++) {
        in.readInt();   // read version
        int reminderMinutes = in.readInt();
        Date programDate = new Date(in);
        String programId = (String) in.readObject();
        Program program = Plugin.getPluginManager().getProgram(programDate, programId);

        // Only add items that were able to load their program
        if (program != null) {
          mReminderList.add(program, reminderMinutes);
        }
      }
    }
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

    String plugins = settings.getProperty("usethisplugin","").trim();
    boolean sendEnabled = settings.getProperty("usesendplugin","").compareToIgnoreCase("true") == 0;
    
    if(plugins.length() > 0 && sendEnabled) {
      if(plugins.indexOf(";") == -1) {
        mClientPluginIdArr = new String[1];
        mClientPluginIdArr[0] = plugins;
      }
      else
        mClientPluginIdArr = plugins.split(";");
    }
    else
      mClientPluginIdArr = new String[0];
  }
  
  public String[] getClientPluginIds() {
    return mClientPluginIdArr;
  }
  
  public void setClientPluginIds(String[] clientPluginArr) {
    mClientPluginIdArr = clientPluginArr;
    
    String property = "";
    
    if(clientPluginArr.length > 0)
      property = clientPluginArr[0];
    
    for(int i = 1; i < clientPluginArr.length; i++)
      property += ";" + clientPluginArr[i];
    
    mSettings.setProperty("usethisplugin",property);
    mSettings.setProperty("usesendplugin",String.valueOf(clientPluginArr.length > 0));
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
      ActionMenu[] actions = new ActionMenu[entries.length - 1];
      ActionMenu[] sub = new ActionMenu[2];
      
      for (int i = 0; i < entries.length; i++) {
        final int minutes = ReminderFrame.REMIND_VALUE_ARR[i];
        ContextMenuAction a = new ContextMenuAction();
        a.setText(entries[i]);
        a.setActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (minutes == -1) {
              mReminderList.removeWithoutChecking(program);
              updateRootNode();
            } else {
              item.setMinutes(minutes);
            }
          }
        });
        
        if(i == 0)
          sub[0] = new ActionMenu(a);
        else
          actions[i - 1] = new ActionMenu(a, minutes == item.getMinutes());
      }
      
      ContextMenuAction times = new ContextMenuAction();
      times.setText(mLocalizer.msg("timeMenu", "Reminder time"));
      
      sub[1] = new ActionMenu(times,actions);

      return new ActionMenu(action, sub);
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
          Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
          ReminderDialog dlg;
          
          if(w instanceof JFrame)
            dlg = new ReminderDialog((JFrame) w, program,
              mSettings);
          else
            dlg = new ReminderDialog((JDialog) w, program,
                mSettings);
          
          if(mSettings.getProperty("showTimeSelectionDialog","true").compareTo("true") == 0) {
            UiUtilities.centerAndShow(dlg);
            
            if (dlg.getOkPressed()) {
              int minutes = dlg.getReminderMinutes();
              mReminderList.add(program, minutes);
              mReminderList.unblockProgram(program);
              updateRootNode();
            }
            dlg.dispose();
          }
          else {
            int minutes = dlg.getReminderMinutes();
            mReminderList.add(program, minutes);
            mReminderList.unblockProgram(program);
            updateRootNode();
          }
        }
      });
      return new ActionMenu(action);
    }
  }

  private int getDefaultReminderTime() {
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
    return minutes;
  }


  public void addPrograms(Program[] programArr) {
    mReminderList.addAndCheckBlocked(programArr, getDefaultReminderTime());
    updateRootNode();
  }



  public void removeProgram(Program prog) {
    mReminderList.remove(prog);
  }

  public void removePrograms(Program[] progArr) {
    for (int i=0; i<progArr.length; i++) {
      removeProgram(progArr[i]);
    }

  }

  public void addProgram(Program prog) {
    mReminderList.add(prog, getDefaultReminderTime());
  }


   public PluginTreeNode getRootNode() {
    return mRootNode;
  }

  public void updateRootNode() {

    mRootNode.removeAllActions();
    
    Action editReminders = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        ReminderListDialog dlg = new ReminderListDialog(MainFrame.getInstance(), mReminderList);
        UiUtilities.centerAndShow(dlg);
      }
    };
    editReminders.putValue(Action.SMALL_ICON, IconLoader.getInstance().getIconFromTheme("action", "appointment", 16));
    editReminders.putValue(Action.NAME, mLocalizer.msg("buttonText", "Edit reminder list..."));

    Action openSettings = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showSettingsDialog(SettingsItem.REMINDER);
      }
    };
    openSettings.putValue(Action.SMALL_ICON, IconLoader.getInstance().getIconFromTheme("categories", "preferences-desktop", 16));
    openSettings.putValue(Action.NAME, mLocalizer.msg("settingsTree", "Settings"));
    
    mRootNode.addAction(editReminders);
    mRootNode.addAction(null);
    mRootNode.addAction(openSettings);
    
    mRootNode.removeAllChildren();

    ReminderListItem[] items = mReminderList.getReminderItems();
    for (int i=0; i<items.length; i++) {
      mRootNode.addProgram(items[i].getProgram());
    }

    mRootNode.update();

  }


  public ActionMenu getButtonAction(final Frame parentFrame) {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        ReminderListDialog dlg = new ReminderListDialog(parentFrame, mReminderList);
        UiUtilities.centerAndShow(dlg);
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
              } catch (Exception ee) {
                // ignore
              }
            }

            try {
              sequencer.close();
              midiFile.close();
            } catch (Exception ee) {
              // ignore
            }
          }
        }.start();

        return sequencer;
      } else {
        AudioInputStream ais = AudioSystem.getAudioInputStream(new File(
            fileName));

        AudioFormat format = ais.getFormat();
        // ALAW/ULAW samples in PCM konvertieren
        if (format.getEncoding() == AudioFormat.Encoding.ALAW || 
            format.getEncoding() == AudioFormat.Encoding.ULAW) {
          AudioFormat tmp = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
              format.getSampleRate(), format.getSampleSizeInBits() * 2, format
                  .getChannels(), format.getFrameSize() * 2, format
                  .getFrameRate(), true);
          ais = AudioSystem.getAudioInputStream(tmp, ais);
          format = tmp;
        }
        final AudioInputStream stream = ais;

        javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(
            Clip.class, format);

        if(AudioSystem.isLineSupported(info)) {
          final Clip clip = (Clip) AudioSystem.getLine(info);
          clip.open(ais);
          clip.start();

          clip.addLineListener(new LineListener() {
            public void update(LineEvent event) {
              if(clip != null && !clip.isRunning()) {
                clip.close();
                try {
                  stream.close();
                }catch(Exception ee) {
                  // ignore
                }
              }
            }
          });

          return clip;
        }else {
          URL url = new File(fileName).toURL();
          AudioClip clip= Applet.newAudioClip(url);
          clip.play();
        }
      }

    } catch (Exception e) {
      String msg = mLocalizer.msg( "error.1",
          "Error loading reminder sound file!\n({0})" , fileName);
      JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(MainFrame.getInstance()),msg,mLocalizer.msg("error","Error"),JOptionPane.ERROR_MESSAGE);
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