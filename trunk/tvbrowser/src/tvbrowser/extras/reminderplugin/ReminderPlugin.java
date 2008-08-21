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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;


import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;
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
import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.*;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class ReminderPlugin {

  /** The localizer for this class. */
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
  
  private boolean mHasRightToSave = true;
  
  /** The IDs of the plugins that should receive the favorites. */
  private ProgramReceiveTarget[] mClientPluginTargets;
  private int mMarkPriority = -2;

  private ReminderPlugin() {
    mInstance = this;
    mClientPluginTargets = new ProgramReceiveTarget[0];
    mConfigurationHandler = new ConfigurationHandler(DATAFILE_PREFIX);
    loadSettings();
    mReminderList = new ReminderList();
    mReminderList.setReminderTimerListener(new ReminderTimerListener(mSettings, mReminderList));
    loadReminderData();

    mRootNode = new PluginTreeNode(mLocalizer.msg("pluginName","Reminder"));

    TvDataUpdater.getInstance().addTvDataUpdateListener(
        new TvDataUpdateListener() {
          public void tvDataUpdateStarted() {
            mHasRightToSave = false;
          }

          public void tvDataUpdateFinished() {
        	  if (mSettings.getProperty("showRemovedDialog","true").compareTo("true") == 0) {
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
            
            mHasRightToSave = true;
            saveReminders();
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

  public String toString() {
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

  /**
   * Gets the settings for the reminder.
   * 
   * @return The settings of the reminder.
   */
  public Properties getSettings() {
    return mSettings;
  }
  
  /**
   * Halt the remider listener.
   */
  public void pauseRemider() {
    mReminderList.stopTimer();
  }

  /**
   * Save the reminder data.
   */
  public synchronized void store() {
    ObjectOutputStream out = null;
    try {
      String userDirectoryName = Settings.getUserSettingsDirName();
      File userDirectory = new File(userDirectoryName);
      File tmpDatFile = new File(userDirectory, DATAFILE_NAME + ".temp");
      File datFile = new File(userDirectory, DATAFILE_NAME);
      out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tmpDatFile)));
      writeData(out);
      out.flush();
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
          mReminderList.add(program, minutes);
        }

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
    else if(version == 2) {
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
    mSettings = settings;

    if(settings.containsKey("usethisplugin") || settings.containsKey("usesendplugin")) {
      String plugins = settings.getProperty("usethisplugin","").trim();
      boolean sendEnabled = settings.getProperty("usesendplugin","").compareToIgnoreCase("true") == 0;

      settings.remove("usethisplugin");
      settings.remove("usesendplugin");
      
      if(plugins.length() > 0 && sendEnabled) {
        if(plugins.indexOf(";") == -1) {
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
                                          final Program program) {
    if (mReminderList.contains(program)) {
      ContextMenuAction action = new ContextMenuAction();
      action.setText(mLocalizer.msg("pluginName", "Reminder"));
      action.setSmallIcon(IconLoader.getInstance().getIconFromTheme("apps",
          "appointment", 16));

      int remainingMinutes = getTimeToProgramStart(program);
      if (remainingMinutes < 0) {
        remainingMinutes = 1;
      }
      int maxIndex = 1;
      for (int i=1; i < ReminderFrame.REMIND_VALUE_ARR.length; i++) {
        if (ReminderFrame.REMIND_VALUE_ARR[i] < remainingMinutes) {
          maxIndex = i;
        }
      }
      final ReminderListItem item = mReminderList.getReminderItem(program);
      String[] entries = ReminderFrame.REMIND_MSG_ARR;
      ActionMenu[] actions = new ActionMenu[maxIndex];
      ActionMenu[] sub = new ActionMenu[2];
      
      for (int i = 0; i <= maxIndex; i++) {
        final int minutes = ReminderFrame.REMIND_VALUE_ARR[i];
        ContextMenuAction a = new ContextMenuAction();
        a.setText(entries[i]);
        a.setActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (minutes == -1) {
              mReminderList.removeWithoutChecking(program);
              updateRootNode(true);
            } else {
              item.setMinutes(minutes);
            }
          }
        });
        
        if(i == 0) {
          sub[0] = new ActionMenu(a);
        } else {
          actions[i - 1] = new ActionMenu(a, minutes == item.getMinutes());
        }
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
          
          if(w instanceof JFrame) {
            dlg = new ReminderDialog((JFrame) w, program,
              mSettings);
          } else {
            dlg = new ReminderDialog((JDialog) w, program,
                mSettings);
          }
          
          Settings.layoutWindow("extras.remiderContext", dlg);
          
          if(mSettings.getProperty("showTimeSelectionDialog","true").compareTo("true") == 0) {
            UiUtilities.centerAndShow(dlg);
            
            if (dlg.getOkPressed()) {
              int minutes = dlg.getReminderMinutes();
              mReminderList.add(program, minutes);
              mReminderList.unblockProgram(program);
              updateRootNode(true);
            }
            dlg.dispose();
          }
          else {
            int minutes = dlg.getReminderMinutes();
            mReminderList.add(program, minutes);
            mReminderList.unblockProgram(program);
            updateRootNode(true);
          }
        }
      });
      return new ActionMenu(action);
    }
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
        if (inx < ReminderDialog.SMALL_REMIND_VALUE_ARR.length) {
          minutes = ReminderDialog.SMALL_REMIND_VALUE_ARR[inx];
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
   * or -1 if the program was not in the list.
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
    
    return -2;
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
    mReminderList.add(prog, reminderMinutes);
  }
  
  /**
   * Gets the reminder minutes for the given program.
   * <p>
   * @param prog The program to get the reminder minutes for.
   * @return The reminder minutes of the program or -1 if the
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
    
    return -1;
  }

  /**
   * Gets the root node for the plugin tree.
   * <p> 
   * @return The root node for the plugin tree.
   */
  public PluginTreeNode getRootNode() {
    return mRootNode;
  }

  /**
   * Updates the plugin tree entry for this plugin.
   * <p>
   * @param save <code>True</code> if the remider entries should be saved.
   */
  public void updateRootNode(boolean save) {
    mRootNode.removeAllActions();
    mRootNode.getMutableTreeNode().setIcon(IconLoader.getInstance().getIconFromTheme("apps", "appointment", 16));
    
    Action editReminders = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        ReminderListDialog dlg = new ReminderListDialog(MainFrame.getInstance(), mReminderList);
        UiUtilities.centerAndShow(dlg);
      }
    };
    editReminders.putValue(Action.SMALL_ICON, IconLoader.getInstance().getIconFromTheme("apps", "appointment", 16));
    editReminders.putValue(Action.NAME, mLocalizer.msg("buttonText", "Edit reminder list..."));

    Action openSettings = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showSettingsDialog(SettingsItem.REMINDER);
      }
    };
    openSettings.putValue(Action.SMALL_ICON, IconLoader.getInstance().getIconFromTheme("categories", "preferences-system", 16));
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
  }

  private void saveReminders() {
    Thread thread = new Thread("Save reminders") {
      public void run() {
        store();
      }
    };
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }


  protected ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        ReminderListDialog dlg;
        
        Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
        
        if(w instanceof JFrame) {
          dlg = new ReminderListDialog((JFrame)w, mReminderList);
        }
        else {
          dlg = new ReminderListDialog((JDialog)w, mReminderList);
        }
        
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
    };

    action.putValue(Action.NAME, mLocalizer.msg("pluginName", "Reminder"));
    action.putValue(Action.SMALL_ICON, IconLoader.getInstance()
        .getIconFromTheme("apps", "appointment", 16));
    action.putValue(Plugin.BIG_ICON, IconLoader.getInstance().getIconFromTheme("apps", "appointment", 22));
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

        new Thread("Reminder MIDI sequencer") {
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
            public void run() {
              byte[] myData = new byte[1024 * format.getFrameSize()];
              int numBytesToRead = myData.length;
              int numBytesRead = 0;
              int total = 0;
              int totalToRead = (int) (format.getFrameSize() * ais.getFrameLength());
              stopped = false;
              
              line.addLineListener(new LineListener() {
                public void update(LineEvent event) {                  
                  if(line != null && !line.isRunning()) {
                    stopped = true;
                    line.close();
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
              
              line.drain();
              line.stop();
            }
          }.start();
          
          return line;         
        }else {
          URL url = new File(fileName).toURI().toURL();
          AudioClip clip= Applet.newAudioClip(url);
          clip.play();
        }
      }

    } catch (Exception e) {e.printStackTrace();
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
   * Gets the id of this plugin.
   * <p>
   * @return The id of this plugin.
   */
  public String getId() {
    return DATAFILE_PREFIX;
  }

  protected ProgramReceiveTarget[] getClientPluginsTargets() {
    ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>();
    for (ProgramReceiveTarget target : mClientPluginTargets) {
      ProgramReceiveIf plugin = target.getReceifeIfForIdOfTarget();
      if (plugin != null && (plugin.canReceivePrograms() || plugin.canReceiveProgramsWithTarget())) {
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
    int progMinutesAfterMidnight = program.getHours() * 60 + program.getMinutes();
    int remainingMinutes = progMinutesAfterMidnight - IOUtilities.getMinutesAfterMidnight();
    if ((remainingMinutes < 0) || (Date.getCurrentDate().compareTo(program.getDate()) < 0)) {
      remainingMinutes += 24 * 60;
      
      if(Date.getCurrentDate().addDays(1).compareTo(program.getDate()) < 0) {
        remainingMinutes += 24 * 60;
      }
    }
    return remainingMinutes;
  }

}