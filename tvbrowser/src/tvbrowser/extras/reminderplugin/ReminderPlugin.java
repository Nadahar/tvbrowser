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
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;

import tvbrowser.core.Settings;
import tvbrowser.core.TvDataUpdateListener;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.ConfigurationHandler;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.toolbar.ToolBar;
import util.exc.ErrorHandler;
import util.io.IOUtilities;
import util.io.stream.ObjectInputStreamProcessor;
import util.io.stream.ObjectOutputStreamProcessor;
import util.io.stream.StreamUtilities;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UIThreadRunner;
import util.ui.UiUtilities;
import util.ui.persona.Persona;
import devplugin.ActionMenu;
import devplugin.AfterDataUpdateInfoPanel;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginCenterPanel;
import devplugin.PluginCenterPanelWrapper;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramItem;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsItem;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ReminderPlugin {

  /** The localizer for this class. */
  static Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ReminderPlugin.class);

  private static final java.util.logging.Logger mLog
      = Logger.getLogger(ReminderPlugin.class.getName());

  private ReminderList mReminderList;
  private Properties mSettings;

  private static ReminderPlugin mInstance;
  private static String DATAFILE_PREFIX = "reminderplugin.ReminderPlugin";
  private static String DATAFILE_NAME = "reminder.dat";

  private ConfigurationHandler mConfigurationHandler;

  private static final PluginTreeNode mRootNode = new PluginTreeNode(mLocalizer.msg("pluginName","Reminders"));

  private boolean mHasRightToStartTimer = false;

  private boolean mHasRightToSave = true;
  
  private static final String TOGGLE_ACTION_ID = "reminderPauseAction";
  public static final String REMINDER_LIST_ACTION_ID = "reminderListAction";

  /** The IDs of the plugins that should receive the favorites. */
  private ProgramReceiveTarget[] mClientPluginTargets;
  private int mMarkPriority = -2;
  
  private PluginCenterPanelWrapper mWrapper;
  private JPanel mCenterPanel;
  private ReminderListPanel mReminderListPanel;
  private AbstractAction toggleTimer;
  
  private Thread mInfoCreationThread;
  private AfterDataUpdateInfoPanel mInfoPanel;

  private ReminderPlugin() {
    mInstance = this;
    
    toggleTimer = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        if(mReminderList != null) {
          mReminderList.toggleTimer();
          
          if(mReminderList.isActive()) {
            putValue(Action.NAME, mLocalizer.msg("stopTimer", "Pause Reminder"));
            toggleTimer.putValue(Action.SHORT_DESCRIPTION, mLocalizer.msg("stopTimerDesc", "Pause Reminder until reactivation"));
            putValue(ToolBar.ACTION_IS_SELECTED, Boolean.valueOf(false));
          }
          else {
            putValue(Action.NAME, mLocalizer.msg("continueTimer", "Continue Reminder"));
            toggleTimer.putValue(Action.SHORT_DESCRIPTION, mLocalizer.msg("continueTimer", "Continue Reminder"));
            putValue(ToolBar.ACTION_IS_SELECTED, Boolean.valueOf(true));
          }
          
          MainFrame.getInstance().updateToolbar();
        }
      }
    };
    
    toggleTimer.putValue(Action.NAME, mLocalizer.msg("stopTimer", "Pause Reminder"));
    toggleTimer.putValue(Plugin.ACTION_ID_KEY, TOGGLE_ACTION_ID);
    toggleTimer.putValue(Action.SHORT_DESCRIPTION, mLocalizer.msg("stopTimerDesc", "Pause Reminder until reactivation"));
    toggleTimer.putValue(Action.SMALL_ICON, IconLoader.getInstance().getIconFromTheme("actions", "reminder-stop", 16));
    toggleTimer.putValue(Plugin.BIG_ICON, IconLoader.getInstance().getIconFromTheme("actions", "reminder-stop", 22));
    toggleTimer.putValue(ToolBar.ACTION_TYPE_KEY, ToolBar.TOOGLE_BUTTON_ACTION);
    toggleTimer.putValue(ToolBar.ACTION_IS_SELECTED, false);

    
    mWrapper = new PluginCenterPanelWrapper() {  
      ReminderCenterPanel centerPanel = new ReminderCenterPanel();
      @Override
      public PluginCenterPanel[] getCenterPanels() {
        return new PluginCenterPanel[] {centerPanel};
      }
      
      @Override
      public void scrolledToDate(Date date) {
        if(mReminderListPanel != null) {
          mReminderListPanel.scrollTo(ReminderListPanel.SCROLL_TO_DATE_TYPE, date, -1);
        }
      }
      
      @Override
      public void scrolledToNow() {
        if(mReminderListPanel != null) {
          mReminderListPanel.scrollTo(ReminderListPanel.SCROLL_TO_NOW_TYPE, null, -1);
        }
      }
      
      @Override
      public void scrolledToTime(int time) {
        if(mReminderListPanel != null) {
          int type = ReminderListPanel.SCROLL_TO_NEXT_TIME_TYPE;
          
          if(!Boolean.parseBoolean(ReminderPropertyDefaults.getPropertyDefaults().getValueFromProperties(ReminderPropertyDefaults.SCROLL_TIME_TYPE_NEXT, mSettings))) {
            type = ReminderListPanel.SCROLL_TO_TIME_TYPE;
          }
          
          mReminderListPanel.scrollTo(type, null, time);
        } super.scrolledToTime(time);
      }
    };
    
    mCenterPanel = UiUtilities.createPersonaBackgroundPanel();
    mClientPluginTargets = new ProgramReceiveTarget[0];
    mConfigurationHandler = new ConfigurationHandler(getReminderPluginId());
    loadSettings();
    mReminderList = new ReminderList();
    mReminderList.setReminderTimerListener(new ReminderTimerListener(mSettings, mReminderList));
    loadReminderData();

    TvDataUpdater.getInstance().addTvDataUpdateListener(
        new TvDataUpdateListener() {
          private boolean mCanCreateInfoPanel;
          
          public void tvDataUpdateStarted(Date until) {
            mCanCreateInfoPanel = false;
            mHasRightToSave = false;
            mInfoCreationThread = new Thread() {
              public void run() {
                while(!mCanCreateInfoPanel) {
                  try {
                    sleep(500);
                  } catch (InterruptedException e) {
                    // Ignore
                  }
                }
                
                if (mSettings.getProperty("showRemovedDialog","true").compareTo("true") == 0) {
                  Program[] removedPrograms = mReminderList.updatePrograms();
                  
                  if (removedPrograms.length > 0) {
                    mInfoPanel = new RemovedProgramsPanel(removedPrograms);
                  }
                  else {
                    mInfoPanel = null;
                  }
                } else {
                  mReminderList.updatePrograms();
                  mInfoPanel = null;
                }

                mHasRightToSave = true;
                saveReminders();

                ReminderListDialog.updateReminderList();
              }
            };
            mInfoCreationThread.start();
          }

          public void tvDataUpdateFinished() {
            mCanCreateInfoPanel = true;
          }
        });

  }

  /**
   * Gets the current instance of this class, or if
   * there is no instance creates a new one.
   *
   * @return The current instance of this class.
   */
  public static synchronized ReminderPlugin getInstance() {
    if (mInstance == null) {
      new ReminderPlugin();
    }
    return mInstance;
  }

  @Override
  public String toString() {
    return getName();
  }

  static String getName() {
    return mLocalizer.msg("pluginName","Reminder");
  }

  /**
   * Is been called by TVBrowser when the TV-Browser start is finished.
   */
  public void handleTvBrowserStartFinished() {
    updateRootNode(false);
    mHasRightToStartTimer = true;
    mReminderList.removeExpiredItems();
    mReminderList.startTimer();
    
    addPanel();
  }
  
  void addPanel() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if(mSettings.getProperty("provideTab", "true").equals("true")) {
          if(mReminderListPanel == null) {
            mReminderListPanel = new ReminderListPanel(mReminderList, null);
            Persona.getInstance().registerPersonaListener(mReminderListPanel);
            
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {        
                mCenterPanel.add(mReminderListPanel, BorderLayout.CENTER);
                mReminderListPanel.updatePersona();
                mCenterPanel.repaint();
              }
            });
          }
        }
        else {
          if(mReminderListPanel != null) {
            Persona.getInstance().removePersonaListerner(mReminderListPanel);
          }
          
          mReminderListPanel = null;
        }
      }
    });
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
        StreamUtilities.objectInputStream(newFile, 0x4000, new ObjectInputStreamProcessor() {

          @Override
          public void process(ObjectInputStream inputStream) throws IOException {
            try {
              readData(inputStream);
            } catch (ClassNotFoundException e) {
              ErrorHandler.handle("Could not load reminder data", e);
            }
          }
        });
      } else {
        tryToReadDataFromPreviousVersions();
      }
      mReminderList.removeExpiredItems();
      mReminderList.setReminderTimerListener(new ReminderTimerListener(mSettings, mReminderList));

    } catch (IOException e) {
      ErrorHandler.handle("Could not load reminder data", e);
    }
  }


  private void tryToReadDataFromPreviousVersions() {
    boolean oldDataRead = false;
    try {
      File nodeFile = new File(Settings.getUserSettingsDirName(), "java.reminderplugin.ReminderPlugin.node");
      if (nodeFile.exists()) {
        StreamUtilities.objectInputStream(nodeFile, 0x4000, new ObjectInputStreamProcessor() {

          @Override
          public void process(ObjectInputStream inputStream) throws IOException {
            try {
              readReminderFromTVBrowser21and20(inputStream);
            } catch (ClassNotFoundException e) {
              mLog.log(Level.WARNING, "Could not read data from previous version", e);
            }
          }
        });
        oldDataRead = true;
        nodeFile.delete();
      }
      File datFile = new File(Settings.getUserSettingsDirName(), "java.reminderplugin.ReminderPlugin.dat");
      if (datFile.exists()) {
        if (!oldDataRead) {
          StreamUtilities.objectInputStream(datFile, 0x4000, new ObjectInputStreamProcessor() {

            @Override
            public void process(ObjectInputStream inputStream) throws IOException {
              try {
                readReminderFromBeforeTVBrowser20(inputStream);
              } catch (ClassNotFoundException e) {
                mLog.log(Level.WARNING, "Could not read data from previous version", e);
              }
            }
          });
        }
        datFile.delete();
      }
    } catch (IOException e) {
      mLog.log(Level.WARNING, "Could not read data from previous version", e);
    }
  }

  /**
   * Gets the settings for the reminder.
   *
   * @return The settings of the reminder.
   */
  public Properties getSettings() {
    return mSettings;
  }

  /**
   * Save the reminder data.
   */
  public synchronized void store() {
    try {
      String userDirectoryName = Settings.getUserSettingsDirName();
      File userDirectory = new File(userDirectoryName);
      File tmpDatFile = new File(userDirectory, DATAFILE_NAME + ".temp");
      File datFile = new File(userDirectory, DATAFILE_NAME);
      StreamUtilities.objectOutputStream(tmpDatFile,
          new ObjectOutputStreamProcessor() {
            public void process(ObjectOutputStream out) throws IOException {
              writeData(out);
              out.flush();
              out.close();
            }
          });

      datFile.delete();
      tmpDatFile.renameTo(datFile);
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

    int version = in.readInt(); // version

    mReminderList.setReminderTimerListener(null);
    mReminderList.read(in);

    if(version == 3) {
      mClientPluginTargets = new ProgramReceiveTarget[in.readInt()];

      for(int i = 0; i < mClientPluginTargets.length; i++) {
        mClientPluginTargets[i] = new ProgramReceiveTarget(in);
      }
    }
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

        if(program != null) {
          mReminderList.add(program, new ReminderContent(minutes));
        }

        in.readInt();  // cnt (should be 0)

      }
    }
    in.close();
  }

  /**
   * read the object from an input stream.
   *
   * @param in the stream to read from
   * @throws IOException if something went wrong reading the stream
   * @throws ClassNotFoundException if the object could not be deserialized
   */
  private void readReminderFromBeforeTVBrowser20(final ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt();
    if (version == 1) {
      int size = in.readInt();
      for (int i = 0; i < size; i++) {
        in.readInt();   // read version
        int reminderMinutes = in.readInt();
        Date programDate = Date.readData(in);
        String programId = (String) in.readObject();
        Program program = Plugin.getPluginManager().getProgram(programDate, programId);

        // Only add items that were able to load their program
        if (program != null) {
          mReminderList.add(program, new ReminderContent(reminderMinutes));
        }
      }
    }
    else if (version == 2) {
      mReminderList.setReminderTimerListener(null);
      mReminderList.read(in);
    }
  }

  /**
   * Save the data of this plugin in the given stream.
   * <p>
   * @param out The stream to write the data in.
   * @throws IOException
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(3);
    mReminderList.writeData(out);
    out.writeInt(mClientPluginTargets.length);

    for(ProgramReceiveTarget target : mClientPluginTargets) {
      target.writeData(out);
    }
  }

  private void loadSettings(Properties settings) {
    if (settings == null) {
      settings = new Properties();
    }
    if (settings.getProperty("usemsgbox") == null) {
      settings.setProperty("usemsgbox", "true");
    }
    if (settings.getProperty("numberofremindoptions") != null && settings.getProperty("defaultReminderEntry") != null) {      
      int defaultRemind = Integer.parseInt(settings.getProperty("defaultReminderEntry")) - 5;
      
      settings.setProperty("defaultReminderEntry", String.valueOf(defaultRemind));
      settings.remove("numberofremindoptions");
    }
    else if(settings.getProperty("defaultReminderEntry") == null) {
      settings.setProperty("defaultReminderEntry", String.valueOf(0));
    }
    
    mSettings = settings;

    if(settings.containsKey("usethisplugin") || settings.containsKey("usesendplugin")) {
      String plugins = settings.getProperty("usethisplugin","").trim();
      boolean sendEnabled = settings.getProperty("usesendplugin","").compareToIgnoreCase("true") == 0;

      settings.remove("usethisplugin");
      settings.remove("usesendplugin");

      if(plugins.length() > 0 && sendEnabled) {
        if (plugins.indexOf(';') == -1) {
          mClientPluginTargets = new ProgramReceiveTarget[1];
          mClientPluginTargets[0] = ProgramReceiveTarget.createDefaultTargetForProgramReceiveIfId(plugins);
        }
        else {
          String[] ids = plugins.split(";");

          mClientPluginTargets = new ProgramReceiveTarget[ids.length];

          for(int i = 0; i < ids.length; i++) {
            mClientPluginTargets[i] = ProgramReceiveTarget.createDefaultTargetForProgramReceiveIfId(ids[i]);
          }
        }
      }
    }

    if(settings.containsKey("autoCloseReminderAtProgramEnd")) {
      if(settings.getProperty("autoCloseReminderAtProgramEnd","true").equalsIgnoreCase("true")) {
        settings.setProperty("autoCloseBehaviour","onEnd");
      }

      settings.remove("autoCloseReminderAtProgramEnd");
    }
  }

  protected ActionMenu getContextMenuActions(final Frame parentFrame,
                                          final Program program) {try {
    if (mReminderList.contains(program)) {
      final ReminderListItem item = mReminderList.getReminderItem(program);
      RemindValue[] values = calculatePossibleReminders(program);
      
      ArrayList<ActionMenu> actions = new ArrayList<ActionMenu>(values.length + 3);

      actions.add(new ActionMenu(new AbstractAction(ReminderFrame.DONT_REMIND_AGAIN_VALUE.toString()) {
        public void actionPerformed(ActionEvent e) {
          mReminderList.removeWithoutChecking(program);
          updateRootNode(true);
        }
      }));
      actions.add(new ActionMenu(ContextMenuSeparatorAction.getInstance()));

      for(final RemindValue value : values) {
        actions.add(new ActionMenu(new AbstractAction(value.toString()) {
          @Override
          public void actionPerformed(ActionEvent e) {
            item.setMinutes(value.getMinutes());
            saveReminders();
          }
        }, item.getMinutes() == value.getMinutes()));
      }
      
      actions.add(new ActionMenu(ContextMenuSeparatorAction.getInstance()));
      actions.add(new ActionMenu(new AbstractAction(mLocalizer.msg("comment",
          "Change comment"), TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL)) {
        @Override
        public void actionPerformed(ActionEvent e) {
          item.changeComment(parentFrame);
        }
      }));

      return new ActionMenu(getName(), IconLoader.getInstance().getIconFromTheme("apps",
          "appointment", 16), actions.toArray(new ActionMenu[actions
          .size()]));
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
          final Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
          try {
            UIThreadRunner.invokeAndWait(new Runnable() {

              @Override
              public void run() {
                ReminderDialog dlg = new ReminderDialog(w, program, mSettings);
                Settings.layoutWindow("extras.remiderContext", dlg);

                if(mSettings.getProperty("showTimeSelectionDialog","true").compareTo("true") == 0) {
                  UiUtilities.centerAndShow(dlg);

                  if (dlg.getOkPressed()) {
                    mReminderList.add(program, dlg.getReminderContent());
                    mReminderList.unblockProgram(program);
                    updateRootNode(true);
                  }
                  dlg.dispose();
                }
                else {
                  mReminderList.add(program, dlg.getReminderContent());
                  mReminderList.unblockProgram(program);
                  updateRootNode(true);
                }
              }
            });
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      });
      return new ActionMenu(action);
    }}catch(Throwable t) {t.printStackTrace();}return null;
  }

  /**
   * Gets the default reminder time.
   * <p>
   * @return The default reminder time in minutes.
   */
  public int getDefaultReminderTime() {
    String defaultReminderEntryStr = (String) mSettings
        .get("defaultReminderEntry");
    int minutes = 10;
    if (defaultReminderEntryStr != null) {
      try {
        int inx = Integer.parseInt(defaultReminderEntryStr);
        if (inx < ReminderFrame.REMIND_BEFORE_VALUE_ARR.length) {
          minutes = ReminderFrame.REMIND_BEFORE_VALUE_ARR[inx].getMinutes();
        }
      } catch (NumberFormatException e) {
        // ignore
      }
    }
    return minutes;
  }

  /**
   * Add the given programs to the reminder list.
   * <p>
   * @param programArr The programs to add.
   */
  public void addPrograms(Program[] programArr) {
    mReminderList.addAndCheckBlocked(programArr, getDefaultReminderTime());
    updateRootNode(true);
  }

  /**
   * Removes the given program from the reminder list.
   * <p>
   * @param prog The program to remove.
   * @return The reminder minutes of the program
   * or ReminderFrame.DONT_REMIND_AGAIN if the program was not in the list.
   */
  public int removeProgram(Program prog) {
    ReminderListItem item = null;

    synchronized(mReminderList) {
      item = mReminderList.getReminderItem(prog);

      mReminderList.remove(prog);
    }

    if(item != null) {
      return item.getMinutes();
    }

    return ReminderFrame.NO_REMINDER;
  }

  /**
   * Remove the given programs from the reminder list.
   * <p>
   * @param progArr The programs to remove.
   */
  public void removePrograms(Program[] progArr) {
    synchronized(mReminderList) {
      mReminderList.remove(progArr);
    }
  }

  /**
   * Adds a program to the reminder list.
   * <p>
   * @param prog The program to add.
   * @param reminderMinutes The reminder minutes for the program.
   */
  public void addProgram(Program prog, int reminderMinutes) {
    mReminderList.add(prog, new ReminderContent(reminderMinutes));
  }

  /**
   * Gets the reminder minutes for the given program.
   * <p>
   * @param prog The program to get the reminder minutes for.
   * @return The reminder minutes of the program or ReminderFrame.DONT_REMIND_AGAIN if the
   * program is not in the reminder list
   * @since 2.7
   */
  public int getReminderMinutesForProgram(Program prog) {
    ReminderListItem item = null;

    synchronized(mReminderList) {
      item = mReminderList.getReminderItem(prog);
    }

    if(item != null) {
      return item.getMinutes();
    }

    return ReminderFrame.DONT_REMIND_AGAIN;
  }

  /**
   * Gets the root node for the plugin tree.
   * <p>
   * @return The root node for the plugin tree.
   */
  public static PluginTreeNode getRootNode() {
    return mRootNode;
  }

  synchronized void updateRootNode(boolean save, boolean scroll) {
    mRootNode.removeAllActions();
    mRootNode.getMutableTreeNode().setIcon(IconLoader.getInstance().getIconFromTheme("apps", "appointment", 16));

    Action editReminders = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        ReminderListDialog dlg = new ReminderListDialog(MainFrame.getInstance(), mReminderList);
        UiUtilities.centerAndShow(dlg);
      }
    };
    editReminders.putValue(Action.SMALL_ICON, IconLoader.getInstance().getIconFromTheme("apps", "appointment", 16));
    editReminders.putValue(Action.NAME, mLocalizer.ellipsisMsg("buttonText", "Edit reminder list"));

    Action openSettings = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showSettingsDialog(SettingsItem.REMINDER);
      }
    };
    openSettings.putValue(Action.SMALL_ICON, TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL));
    openSettings.putValue(Action.NAME, Localizer.getLocalization(Localizer.I18N_SETTINGS));

    mRootNode.addAction(editReminders);
    mRootNode.addAction(null);
    mRootNode.addAction(openSettings);

    mRootNode.removeAllChildren();

    ReminderListItem[] items = mReminderList.getReminderItems();
    ArrayList<Program> listNewPrograms = new ArrayList<Program>(items.length);
    for (ReminderListItem reminderItem : items) {
      listNewPrograms.add(reminderItem.getProgram());
    }
    mRootNode.addPrograms(listNewPrograms);

    mRootNode.update();

    if(save && mHasRightToSave) {
      saveReminders();
    }
    
    if(mReminderListPanel != null) {
      mReminderListPanel.installTableModel(scroll);
    }
  }
  
  /**
   * Updates the plugin tree entry for this plugin.
   * <p>
   *
   * @param save
   *          <code>True</code> if the reminder entries should be saved.
   */
  public void updateRootNode(boolean save) {
    updateRootNode(save, true);
  }

  private void saveReminders() {
    store();
  }

  protected ActionMenu getButtonAction() {try {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        getInstance().showManageRemindersDialog();
      }
    };

    action.putValue(Action.NAME, mLocalizer.msg("showReminderList", "Show Reminder list"));
    action.putValue(Action.SMALL_ICON, IconLoader.getInstance()
        .getIconFromTheme("apps", "appointment", 16));
    action.putValue(Plugin.BIG_ICON, IconLoader.getInstance().getIconFromTheme("apps", "appointment", 22));
    action.putValue(Action.SHORT_DESCRIPTION, mLocalizer.msg("description",
        "Reminds you of programs to not miss them."));
    action.putValue(Plugin.ACTION_ID_KEY, REMINDER_LIST_ACTION_ID);
    
    return new ActionMenu(getName(),IconLoader.getInstance().getIconFromTheme("apps", "appointment", 16),new Action[] {action,toggleTimer});
  }catch(Throwable t) {t.printStackTrace();}
  return null;
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
      if (StringUtils.endsWithIgnoreCase(fileName,".mid")) {
        final Sequencer sequencer = MidiSystem.getSequencer();
        sequencer.open();

        final InputStream midiFile = new FileInputStream(fileName);
        sequencer.setSequence(MidiSystem.getSequence(midiFile));

        sequencer.start();

        new Thread("Reminder MIDI sequencer") {
          @Override
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
        final AudioInputStream ais = AudioSystem.getAudioInputStream(new File(
            fileName));

        final AudioFormat format = ais.getFormat();
        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        if(AudioSystem.isLineSupported(info)) {
          final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

          line.open(format);
          line.start();

          new Thread("Reminder audio playing") {
            private boolean stopped;
            @Override
            public void run() {
              try {
                byte[] myData = new byte[1024 * format.getFrameSize()];
                int numBytesToRead = myData.length;
                int numBytesRead = 0;
                int total = 0;
                int totalToRead = (int) (format.getFrameSize() * ais.getFrameLength());
                stopped = false;
  
                line.addLineListener(new LineListener() {
                  public void update(LineEvent event) {
                    if(event.getType() != Type.START && line != null && (event.getType() == Type.STOP || !line.isRunning())) {
                      stopped = true;
                      
                      try {
                        ais.close();
                      }catch(Exception ee) {
                        // ignore
                      }
                    }
                  }
                });
  
                try {
                  while (total < totalToRead && !stopped) {
                    numBytesRead = ais.read(myData, 0, numBytesToRead);
  
                    if (numBytesRead == -1) {
                      break;
                    }
  
                    total += numBytesRead;
                    line.write(myData, 0, numBytesRead);
                  }
                }catch(Exception e) {}
  
                if(line.isRunning()) {
                  line.drain();
                }
                
                line.stop();
                
                if(line != null) {
                  line.close();
                }
              }catch(Exception ex) {}
            }
          }.start();

          return line;
        }else {
          URL url = new File(fileName).toURI().toURL();
          AudioClip clip= Applet.newAudioClip(url);
          clip.play();
        }
      }

    } catch (Exception e) {
      if((new File(fileName)).isFile()) {
        URL url;
        try {
          url = new File(fileName).toURI().toURL();
          AudioClip clip= Applet.newAudioClip(url);
          clip.play();
        } catch (MalformedURLException e1) {
        }
      }
      else {
        String msg = mLocalizer.msg( "error.1",
          "Error loading reminder sound file!\n({0})" , fileName);
        JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(MainFrame.getInstance()),msg,Localizer.getLocalization(Localizer.I18N_ERROR),JOptionPane.ERROR_MESSAGE);
      }
    }
    return null;
  }

  protected ActionMenu getContextMenuActions(Program program) {
    return getContextMenuActions(null, program);
  }

  /**
   * get the ID of the plugin (without the need to load the plugin)
   *
   * @return the id
   * @since 3.0
   */
  public static String getReminderPluginId() {
    return DATAFILE_PREFIX;
  }

  protected ProgramReceiveTarget[] getClientPluginsTargets() {
    ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>();
    for (ProgramReceiveTarget target : mClientPluginTargets) {
      ProgramReceiveIf plugin = target.getReceifeIfForIdOfTarget();
      if (plugin != null && plugin.canReceiveProgramsWithTarget()) {
        list.add(target);
      }
    }
    return list.toArray(new ProgramReceiveTarget[list.size()]);
  }

  protected void setClientPluginsTargets(ProgramReceiveTarget[] targets) {
    if(targets != null) {
      mClientPluginTargets = targets;
    } else {
      mClientPluginTargets = new ProgramReceiveTarget[0];
    }
  }

  protected int getMarkPriority() {
    if(mMarkPriority == - 2 && mSettings != null) {
      mMarkPriority = Integer.parseInt(mSettings.getProperty("markPriority",String.valueOf(Program.MIN_MARK_PRIORITY)));
      return mMarkPriority;
    } else {
      return mMarkPriority;
    }
  }

  protected void setMarkPriority(int priority) {
    mMarkPriority = priority;

    ReminderListItem[] items = mReminderList.getReminderItems();

    for(ReminderListItem item : items) {
      item.getProgram().validateMarking();
    }

    mSettings.setProperty("markPriority",String.valueOf(priority));
    saveReminders();
  }

  protected static int getTimeToProgramStart(Program program) {
    int progMinutesAfterMidnight = program.getHours() * 60
        + program.getMinutes();
    int remainingMinutes = progMinutesAfterMidnight
        - IOUtilities.getMinutesAfterMidnight() + 1440
        * (program.getDate().getNumberOfDaysSince(Date.getCurrentDate()));
    if (remainingMinutes < 0) {
      remainingMinutes += 24 * 60;
    }
    return remainingMinutes;
  }

  protected void handleTvDataUpdateFinished() {
    mReminderList.removeExpiredItems();
    
    if(mReminderListPanel != null) {
      mReminderListPanel.updateTableEntries();
    }
    
    updateRootNode(false);
  }

  private void showManageRemindersDialog() {
    Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
    ReminderListDialog dlg = new ReminderListDialog(w, mReminderList);

    int x = Integer.parseInt(mSettings.getProperty("dlgXPos","-1"));
    int y = Integer.parseInt(mSettings.getProperty("dlgYPos","-1"));

    if(x == -1 || y == -1) {
      UiUtilities.centerAndShow(dlg);
    } else {
      dlg.setLocation(x,y);
      dlg.setVisible(true);
    }

    mSettings.setProperty("dlgXPos", String.valueOf(dlg.getX()));
    mSettings.setProperty("dlgYPos", String.valueOf(dlg.getY()));
    mSettings.setProperty("dlgWidth", String.valueOf(dlg.getWidth()));
    mSettings.setProperty("dlgHeight", String.valueOf(dlg.getHeight()));
  }

  public static void resetLocalizer() {
    mLocalizer = Localizer.getLocalizerFor(ReminderPlugin.class);
  }

  public PluginCenterPanelWrapper getPluginCenterPanelWrapper() {
    return mSettings.getProperty("provideTab", "true").equals("true") ? mWrapper : null;
  }
  
  private class ReminderCenterPanel extends PluginCenterPanel {
    @Override
    public String getName() {
      return ReminderPlugin.getName();
    }

    @Override
    public JPanel getPanel() {
      return mCenterPanel;
    }    
  }
  
  AfterDataUpdateInfoPanel getAfterDataUpdateInfoPanel() {
    if(mInfoCreationThread != null && mInfoCreationThread.isAlive()) {
      try {
        mInfoCreationThread.join();
      } catch (InterruptedException e) {
        // Ignore
      }
    }
    
    return mInfoPanel;
  }
  
  public boolean showDateSeparators() {
    return mSettings.getProperty("showDateSeparators", "true").equals("true");
  }
  
  public void setShowDateSeparators(boolean show) {
    mSettings.setProperty("showDateSeparators", String.valueOf(show));
    
    if(mReminderListPanel != null) {
      mReminderListPanel.installTableModel(false);
    }
  }
  
  public static int getStartIndexForBeforeReminders(Program program) {
    int remainingMinutes = ReminderPlugin.getTimeToProgramStart(program);
    
    if(program.isExpired()) {
      remainingMinutes = ReminderFrame.DONT_REMIND_AGAIN;
    }
    else if(program.isOnAir()) {
      if(program.getStartTime() > IOUtilities.getMinutesAfterMidnight()) {
        remainingMinutes = program.getStartTime() - 1440 - IOUtilities.getMinutesAfterMidnight();
      }
      else {
        remainingMinutes = program.getStartTime() - IOUtilities.getMinutesAfterMidnight();
      }
    }
    
    int index = 0;
    
    for(RemindValue value : ReminderFrame.REMIND_AFTER_VALUE_ARR)  {
      if(value.getMinutes() < remainingMinutes && Math.abs(value.getMinutes()) < program.getLength()) {
          index++;
        }
      }
      
      return index;
  }
  
  public static RemindValue[] calculatePossibleReminders(Program program) {
    int remainingMinutes = ReminderPlugin.getTimeToProgramStart(program);
    
    if(program.isExpired()) {
      remainingMinutes = ReminderFrame.DONT_REMIND_AGAIN;
    }
    else if(program.isOnAir()) {
      if(program.getStartTime() > IOUtilities.getMinutesAfterMidnight()) {
        remainingMinutes = program.getStartTime() - 1440 - IOUtilities.getMinutesAfterMidnight();
      }
      else {
        remainingMinutes = program.getStartTime() - IOUtilities.getMinutesAfterMidnight();
      }
    }
    
    ArrayList<RemindValue> valueList = new ArrayList<RemindValue>();
    
    for(RemindValue value : ReminderFrame.REMIND_AFTER_VALUE_ARR)  {
      if(value.getMinutes() < remainingMinutes && Math.abs(value.getMinutes()) < program.getLength()) {
        valueList.add(value);
      }
    }
    
    for(RemindValue value : ReminderFrame.REMIND_BEFORE_VALUE_ARR)  {
      if(value.getMinutes() < remainingMinutes) {
        valueList.add(value);
      }
    }
    
    return valueList.toArray(new RemindValue[valueList.size()]);
  }
}