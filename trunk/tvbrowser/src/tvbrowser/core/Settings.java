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
package tvbrowser.core;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import devplugin.ProgramFieldType;
import tvbrowser.TVBrowser;
import tvbrowser.core.plugin.DefaultSettings;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.programtable.DefaultProgramTableModel;
import tvbrowser.ui.programtable.ProgramTableScrollPane;
import tvbrowser.ui.waiting.dlgs.TvDataCopyWaitingDlg;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
import util.settings.BooleanProperty;
import util.settings.ChannelArrayProperty;
import util.settings.ChoiceProperty;
import util.settings.ColorProperty;
import util.settings.DateProperty;
import util.settings.EncodedStringProperty;
import util.settings.FontProperty;
import util.settings.IntArrayProperty;
import util.settings.IntProperty;
import util.settings.PluginPictureSettings;
import util.settings.ProgramFieldTypeArrayProperty;
import util.settings.ProgramPanelSettings;
import util.settings.Property;
import util.settings.PropertyManager;
import util.settings.StringArrayProperty;
import util.settings.StringProperty;
import util.settings.VersionProperty;
import util.ui.Localizer;
import util.ui.view.SplitViewProperty;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

/**
 * The Settings class provides access to the settings of the whole application
 * (except the plugins).
 * 
 * @author Martin Oberhauser
 */
public class Settings {  
  public static final String INFO_ID = "info.id";
  public static final String PICTURE_ID = "picture.id";

  private static java.util.logging.Logger mLog = java.util.logging.Logger
      .getLogger(Settings.class.getName());

  private static DefaultSettings mDefaultSettings = new DefaultSettings();

  private static final long PROXY_PASSWORD_SEED = 6528587292713416704L;

  public static final int GET_DATA_FROM_SERVER = 0,
      GET_DATA_FROM_LOCAL_DISK = 1;

  public static final int NEVER = 0, DAILY = 1, ONSTARTUP = DAILY,
      EVERY3DAYS = 2, WEEKLY = 3;

  private static final String SETTINGS_FILE = "settings.prop";
  private static final String DEFAULT_USER_DIR = ".tvbrowser";

  private static final Font PROGRAMTITLEFONT = new Font("Dialog", Font.BOLD, 12);
  private static final Font PROGRAMINFOFONT = new Font("Dialog", Font.PLAIN, 10);
  private static final Font CHANNELNAMEFONT = new Font("Dialog", Font.BOLD, 12);
  private static final Font PROGRAMTIMEFONT = new Font("Dialog", Font.BOLD, 12);

  private static PropertyManager mProp = new PropertyManager();

  private static boolean mShowWaiting;
  
 /**
   * Returns the Default-Settings. These Settings are stored in the mac, windows
   * and linux.properties-Files
   * 
   * @return Default-Settings
   */
  public static DefaultSettings getDefaultSettings() {
    return mDefaultSettings;
  }

  /**
   * Returns the user directory. (e.g.: ~/.tvbrowser/)
   */
  public static String getUserDirectoryName() {
    String dir = new StringBuffer(System.getProperty("user.home")).append(
        File.separator).append(DEFAULT_USER_DIR).toString();
    return TVBrowser.isTransportable() ? new File("settings").getAbsolutePath() : mDefaultSettings.getProperty("userdir", dir);
  }

  public static String getUserSettingsDirName() {
    return new StringBuffer(getUserDirectoryName())
        .append(File.separator).append(TVBrowser.ALL_VERSIONS[0]).toString();
  }

  /**
   * Store all settings. This method is called on quitting the application.
   * @throws util.exc.TvBrowserException Exception while saving the settings
   */
  public static void storeSettings() throws TvBrowserException {
    File f = new File(getUserSettingsDirName());
    if (!f.exists()) {
      f.mkdirs();
    }
    
    File settingsFile = new File(getUserSettingsDirName(), SETTINGS_FILE);
    File firstSettingsBackupFile = new File(getUserSettingsDirName(), SETTINGS_FILE+ "_backup1");
    File secondsSettingsBackupFile = new File(getUserSettingsDirName(), SETTINGS_FILE+ "_backup2");
    
    // Create backup of settings file backup
    try {
      if(firstSettingsBackupFile.isFile()) {
        IOUtilities.copy(firstSettingsBackupFile,secondsSettingsBackupFile);
      }
    }catch(Exception e) {}
    
    try {
      mProp.writeToFile(settingsFile);
      
      try {
        if(settingsFile.isFile()) {
          IOUtilities.copy(settingsFile,firstSettingsBackupFile);
        }
      }catch (Exception e) {}
      
    } catch (IOException exc) {
      throw new TvBrowserException(Settings.class, "error.1",
          "Error when saving settings!\n({0})", settingsFile.getAbsolutePath(),
          exc);
    }
  }

  /**
   * Reads the settings from settings file. If there is no settings file,
   * default settings are used.
   */
  public static void loadSettings() {

    String oldDirectoryName = System.getProperty("user.home", "")
        + File.separator + ".tvbrowser";
    String newDirectoryName = getUserSettingsDirName();

    File settingsFile = new File(newDirectoryName, SETTINGS_FILE);
    File firstSettingsBackupFile = new File(getUserSettingsDirName(), SETTINGS_FILE+ "_backup1");
    File secondSettingsBackupFile = new File(getUserSettingsDirName(), SETTINGS_FILE+ "_backup2");

    if (settingsFile.exists() || firstSettingsBackupFile.exists() || secondSettingsBackupFile.exists()) {
      try {
        mProp.readFromFile(settingsFile);
        mLog.info("Using settings from file " + settingsFile.getAbsolutePath());
      } catch (IOException evt) {
        
        if(firstSettingsBackupFile.isFile() || secondSettingsBackupFile.isFile()) {
          Localizer localizer = Localizer.getLocalizerFor(Settings.class);
          if(JOptionPane.showConfirmDialog(null,localizer.msg("settingBroken","Settings file broken.\nWould you like to load the backup file?\n\n(If you select No, the\ndefault settings are used)"),Localizer.getLocalization(Localizer.I18N_ERROR),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
            boolean loadSecondBackup = !firstSettingsBackupFile.isFile();
            
            if(firstSettingsBackupFile.isFile()) {
              try {
                mProp.readFromFile(firstSettingsBackupFile);
                mLog.info("Using settings from file " + firstSettingsBackupFile.getAbsolutePath());
              }catch(Exception e) {
                loadSecondBackup = true;
              }
            }
            if(loadSecondBackup && secondSettingsBackupFile.isFile()) {
              try {
                mProp.readFromFile(secondSettingsBackupFile);
                mLog.info("Using settings from file " + secondSettingsBackupFile.getAbsolutePath());                
              }catch(Exception e) {
                loadSecondBackup = true;
              }
            }
            
            if(loadSecondBackup) {
              mLog.info("Could not read settings - using default user settings");
            } else {
              try {
                storeSettings();
              }catch(Exception e) {}
            }
          }
        } else {
          mLog.info("Could not read settings - using default user settings");
        }
      }
    }
    /*
     * If the settings file doesn't exist, we try to import the settings created
     * by a previous version of TV-Browser
     */
    else if (!oldDirectoryName.equals(newDirectoryName)) {
      mLog.info("Try to load settings from a previous version of TV-Browser");
            
      File oldDir = null;
      File testFile = null;
      
      String[] directories = {getUserDirectoryName() ,System.getProperty("user.home") + "/TV-Browser",System.getProperty("user.home") + "/Library/Preferences/TV-Browser", System.getProperty("user.home") + "/.tvbrowser"};      

      for(int j = 0; j < (TVBrowser.isTransportable() ? directories.length : 1); j++) {        
        for (int i = (j == 0 ? 1 : 0); i < TVBrowser.ALL_VERSIONS.length; i++) {
          testFile = new File(directories[j] + File.separator + 
              TVBrowser.ALL_VERSIONS[i], SETTINGS_FILE);
          if(testFile.isFile()) {
            oldDir = new File(directories[j],TVBrowser.ALL_VERSIONS[i]);
            break;
          }
        }
        
        if(oldDir == null) {
          testFile = new File(directories[j], SETTINGS_FILE);
          
          if(testFile.isFile()) {
            oldDir = new File(directories[j]);
          } else {
            testFile = new File(oldDirectoryName, SETTINGS_FILE);
            
            if(testFile.isFile()) {
              oldDir = new File(oldDirectoryName);
            }
          }  
        }
        
        if(oldDir != null) {
          break;
        }
      }      
      
      if (oldDir != null && oldDir.isDirectory() && oldDir.exists()) {
        final File newDir = new File(getUserSettingsDirName());

        File oldTvDataDir = null;
        
        if(TVBrowser.isTransportable() && !(new File(getUserDirectoryName(),"tvdata").isDirectory())) {
          try {
            Properties p = new Properties();
            FileInputStream in = new FileInputStream(testFile);
            p.load(in);
            
            String temp = p.getProperty("dir.tvdata",null);
            
            if(temp != null) {
              oldTvDataDir = new File(temp);
            } else if(new File(oldDir, "tvdata").isDirectory()) {
              oldTvDataDir = new File(oldDir, "tvdata");
            } else if(new File(oldDir.getParent(), "tvdata").isDirectory()) {
              oldTvDataDir = new File(oldDir.getParent(), "tvdata");
            }
            
            in.close();
          }catch(Exception e) {}
        }
        
        if (newDir.mkdirs()) {
          try {
            IOUtilities.copy(oldDir.listFiles(new FilenameFilter() {
              public boolean accept(File dir, String name) {
                return name.compareToIgnoreCase("tvdata") != 0
                    && name.compareTo(newDir.getName()) != 0
                    && name.compareTo("BACKUP") != 0;
              }
            }), newDir);

            mLog.info("settings form previous version copied successfully");
            File newSettingsFile = new File(newDir, SETTINGS_FILE);
            mProp.readFromFile(newSettingsFile);
            mLog.info("settings form previous version read succesfully");

            /*
             * This is the .tvbrowser dir, if there are settings form version
             * 1.0 change the name to start with java.
             */
            if (oldDirectoryName.equals(oldDir.getAbsolutePath())) {
              File[] settings = newDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                  return (name.toLowerCase().endsWith(".prop") && name
                           .toLowerCase().indexOf("settings") == -1)
                         || (name.toLowerCase().endsWith(".dat") && name
                           .toLowerCase().indexOf("tv-data-inventory") == -1);
                }
              });

              boolean version1 = false;

              if (settings != null) {
                for (int i = 0; i < settings.length; i++) {
                  String name = "java." + settings[i].getName();
  
                  if (!settings[i].getName().toLowerCase().startsWith("java.")) {
                    version1 = true;
                    settings[i].renameTo(new File(settings[i].getParent(), name));
                  }
                }
              }

              if (version1
                  && !(new File(oldDirectoryName, newDir.getName()))
                      .isDirectory()) {
                oldDir.renameTo(new File(System.getProperty("user.home", "")
                    + File.separator + "tvbrowser_BACKUP"));
              }
            }
            
            /*
             * Test if and copy TV data for the portable version.
             */
            if(oldTvDataDir != null && oldTvDataDir.isDirectory()) {
              final File targetDir = new File(getUserDirectoryName(),"tvdata");
              
              if(!oldTvDataDir.equals(targetDir)) {
                targetDir.mkdirs();
                
                final TvDataCopyWaitingDlg waiting = new TvDataCopyWaitingDlg(new JFrame(), false);
                
                mShowWaiting = true;
                
                final File srcDir = oldTvDataDir;
                
                new Thread() {
                  public void run() {
                    try {
                      IOUtilities.copy(srcDir.listFiles(), targetDir, true);
                    }catch(Exception e) {}
                    
                    mShowWaiting = false;
                    waiting.setVisible(false);
                  }
                }.start();
                
                waiting.setVisible(mShowWaiting);
              }
            }
            
            /*
             * Test if a settings file exist in the user directory, move the 
             * settings to backup.
             */
            if ((new File(getUserDirectoryName(), SETTINGS_FILE)).isFile()) {
              final File backupDir = new File(getUserDirectoryName(), "BACKUP");
              if (backupDir.mkdirs()) {
                mLog.info("moving the settings of old settings dir to backup");
                File[] files = oldDir.listFiles(new FileFilter() {
                  public boolean accept(File pathname) {
                    return pathname.compareTo(newDir) != 0
                        && pathname.getName().compareToIgnoreCase("tvdata") != 0
                        && pathname.compareTo(backupDir) != 0;
                  }
                });

                if (files != null) {
                  for (int i = 0; i < files.length; i++) {
                    files[i].renameTo(new File(backupDir,files[i].getName()));
                  }
                }
              }
            }
          } catch (IOException e) {
            mLog.log(Level.WARNING, "Could not import user settings from '"
                + oldDir.getAbsolutePath() + "' to '"
                + newDir.getAbsolutePath() + "'", e);
          }
        } else {
          mLog.info("Could not create directory '" + newDir.getAbsolutePath()
              + "' - using default user settings");
        }
      } else {
        mLog
            .info("No previous version of TV-Browser found - using default user settings");
      }

    }

    File settingsDir = new File(newDirectoryName);

    if (!settingsDir.exists()) {
      mLog.info("Creating " + newDirectoryName);
      settingsDir.mkdir();
    }
  }

  public static void handleChangedSettings() {
    Property[] propArr;

    MainFrame mainFrame = MainFrame.getInstance();

    propArr = new Property[] { propProgramTitleFont, propProgramInfoFont,
        propProgramTimeFont, propChannelNameFont, propUseDefaultFonts,
        propEnableAntialiasing, propProgramTableOnAirProgramsShowingBorder,
        propProgramPanelUsesExtraSpaceForMarkIcons, 
        propProgramPanelWithMarkingsShowingBoder, propProgramPanelUsedDefaultMarkPriority,
        propProgramPanelMarkedLowerMediumPriorityColor, propProgramPanelMarkedMinPriorityColor,
        propProgramPanelMarkedMediumPriorityColor, propProgramPanelMarkedMaxPriorityColor,
        propProgramTableColorOnAirLight, propProgramTableColorOnAirDark};

    if (mProp.hasChanged(propArr)) {
      util.ui.ProgramPanel.updateFonts();
      tvbrowser.ui.programtable.ChannelPanel.fontChanged();
      ProgramTableScrollPane scrollPane = mainFrame.getProgramTableScrollPane();
      scrollPane.forceRepaintAll();
    }
    
    propArr = new Property[] {propPictureType, propPictureStartTime,
        propPictureEndTime, propIsPictureShowingDescription, propPicturePluginIds,
        propPictureDuration};
    
    if(mProp.hasChanged(propArr)) {
      mainFrame.getProgramTableScrollPane().forceRepaintAll();
    }

    if (mProp.hasChanged(propColumnWidth)) {
      util.ui.ProgramPanel.updateColumnWidth();
      ProgramTableScrollPane scrollPane = mainFrame.getProgramTableScrollPane();
      scrollPane.setColumnWidth(propColumnWidth.getInt());
      scrollPane.forceRepaintAll();
    }

    if (mProp.hasChanged(propTableLayout)) {
      ProgramTableScrollPane scrollPane = mainFrame.getProgramTableScrollPane();
      scrollPane.getProgramTable().setProgramTableLayout(null);
    }

    if (mProp.hasChanged(propDeactivatedPlugins)) {
      mainFrame.updatePluginsMenu();
      mainFrame.updateToolbar();
    }

    propArr = new Property[] { propTableBackgroundStyle,
        propOneImageBackground, propTimeBlockSize, propTimeBlockBackground1,
        propTimeBlockBackground2, propTimeBlockShowWest,
        propTimeBlockWestImage1, propTimeBlockWestImage2,
        propTimeOfDayBackgroundEdge, propTimeOfDayBackgroundEarly,
        propTimeOfDayBackgroundMidday, propTimeOfDayBackgroundAfternoon,
        propTimeOfDayBackgroundEvening };
    if (mProp.hasChanged(propArr)) {
      ProgramTableScrollPane scrollPane = mainFrame.getProgramTableScrollPane();
      scrollPane.getProgramTable().updateBackground();
    }

    propArr = new Property[] { propToolbarButtonStyle, propToolbarButtons,
        propToolbarLocation, propIsTooolbarVisible, propToolbarUseBigIcons };
    if (mProp.hasChanged(propArr)) {
      mainFrame.updateToolbar();
    }

    if (mProp.hasChanged(propTimeButtons)) {
      mainFrame.updateButtons();
    }

    if (mProp.hasChanged(propSubscribedChannels)) {
      ChannelList.reload();
      DefaultProgramTableModel model = mainFrame.getProgramTableModel();
      model.setChannels(ChannelList.getSubscribedChannels());
      mainFrame.updateChannellist();
    }

    propArr = new Property[] { propProgramTableStartOfDay,
        propProgramTableEndOfDay };
    if (mProp.hasChanged(propArr)) {
      DefaultProgramTableModel model = mainFrame.getProgramTableModel();
      int startOfDay = propProgramTableStartOfDay.getInt();
      int endOfDay = propProgramTableEndOfDay.getInt();
      model.setTimeRange(startOfDay, endOfDay);
      model.setDate(mainFrame.getCurrentSelectedDate(), null, null);
    }

    propArr = new Property[] { propProgramTableIconPlugins,
        propProgramInfoFields };
    if (mProp.hasChanged(propArr)) {
      // Force a recreation of the table content
      DefaultProgramTableModel model = mainFrame.getProgramTableModel();
      model.setDate(mainFrame.getCurrentSelectedDate(), null, null);
    }

    propArr = new Property[] { propEnableChannelIcons, propShowChannelNames,
        propShowChannelIconsInProgramTable, propShowChannelIconsInChannellist,
        propShowChannelNamesInProgramTable, propShowChannelNamesInChannellist };
    if (mProp.hasChanged(propArr)) {
      mainFrame.getProgramTableScrollPane().updateChannelPanel();
      mainFrame.updateChannelChooser();
    }
    
    if(mProp.hasChanged(propTVDataDirectory)) {
      TvDataServiceProxyManager.getInstance().setTvDataDir(new File(propTVDataDirectory.getString()));
      
      TvDataBase.getInstance().updateTvDataBase();      
      TvDataBase.getInstance().checkTvDataInventory();
      
      MainFrame.getInstance().handleChangedTvDataDir();      
    }
    
    if (mProp.hasChanged(propEnableChannelIcons)) {
      mainFrame.updateChannellist();
    }

    mProp.clearChanges();
    
    try {
      storeSettings();
    }catch(Exception e) {}
  }

  /**
   * @return The Time-Pattern for SimpleFormatter's
   */
  public static String getTimePattern() {
    if (propTwelveHourFormat.getBoolean()) {
      return "hh:mm a";
    } else {
      return "HH:mm";
    }
  }
  
  public static final VersionProperty propTVBrowserVersion = new VersionProperty(
      mProp, "version", null);
  
  public static final BooleanProperty propTVBrowserVersionIsStable = new BooleanProperty(
      mProp, "versionIsStable", false);

  public static final BooleanProperty propUseDefaultFonts = new BooleanProperty(
      mProp, "usedefaultfonts", true);

  public static final BooleanProperty propEnableAntialiasing = new BooleanProperty(
      mProp, "enableantialiasing", false);

  private static String getDefaultTvDataDir() {
    return TVBrowser.isTransportable() ? "./settings/tvdata" : getUserDirectoryName() + File.separator + "tvdata";
  }

  private static String getDefaultPluginsDir() {
    return getUserSettingsDirName() + "/plugins";
  }

  public static final StringProperty propTVDataDirectory = new StringProperty(
      mProp, "dir.tvdata", mDefaultSettings.getProperty("tvdatadir",
          getDefaultTvDataDir()));

  public static final StringProperty propPluginsDirectory = new StringProperty(
      mProp, "dir.plugins", mDefaultSettings.getProperty("pluginsdir",
          getDefaultPluginsDir()));

  public static final StringProperty propFilterDirectory = new StringProperty(
      mProp, "directory.filters", getUserSettingsDirName() + File.separator
          + "filters");

  public static final ChannelArrayProperty propSubscribedChannels = new ChannelArrayProperty(
      mProp, "subscribedchannels", new devplugin.Channel[] {});

  /**
   * @deprecated
   */
  public static final IntProperty propEarlyTime = new IntProperty(mProp,
      "timebutton.early", 6 * 60);

  /**
   * @deprecated
   */
  public static final IntProperty propMiddayTime = new IntProperty(mProp,
      "timebutton.midday", 12 * 60);

  /**
   * @deprecated
   */
  public static final IntProperty propAfternoonTime = new IntProperty(mProp,
      "timebutton.afternoon", 18 * 60);

  /**
   * @deprecated
   */
  public static final IntProperty propEveningTime = new IntProperty(mProp,
      "timebutton.evening", 20 * 60);

  public static final ChoiceProperty propTableLayout = new ChoiceProperty(
      mProp, "table.layout", "timeSynchronous", new String[] {
          "timeSynchronous", "compact", "realSynchronous" , "realCompact"});

  public static final ChoiceProperty propTableBackgroundStyle = new ChoiceProperty(
      mProp, "tablebackground.style", "timeBlock", new String[] { "white",
          "oneImage", "timeBlock", "timeOfDay" });

  public static final StringProperty propOneImageBackground = new StringProperty(
      mProp, "tablebackground.oneImage.image", "imgs/columns_evening.jpg");

  public static final IntProperty propTimeBlockSize = new IntProperty(mProp,
      "tablebackground.timeBlock.size", 2);

  public static final StringProperty propTimeBlockBackground1 = new StringProperty(
      mProp, "tablebackground.timeBlock.image1", "imgs/columns_evening.jpg");

  public static final StringProperty propTimeBlockBackground2 = new StringProperty(
      mProp, "tablebackground.timeBlock.image2", "imgs/columns_afternoon.jpg");

  public static final StringProperty propTimeBlockWestImage1 = new StringProperty(
      mProp, "tablebackground.timeBlock.west1", "imgs/columns_evening.jpg");

  public static final StringProperty propTimeBlockWestImage2 = new StringProperty(
      mProp, "tablebackground.timeBlock.west2", "imgs/columns_afternoon.jpg");

  public static final BooleanProperty propTimeBlockShowWest = new BooleanProperty(
      mProp, "tablebackground.timeBlock.showWest", false);

  public static final StringProperty propTimeOfDayBackgroundEdge = new StringProperty(
      mProp, "tablebackground.timeofday.edge", "imgs/columns_edge.jpg");

  public static final StringProperty propTimeOfDayBackgroundEarly = new StringProperty(
      mProp, "tablebackground.timeofday.early", "imgs/columns_early.jpg");

  public static final StringProperty propTimeOfDayBackgroundMidday = new StringProperty(
      mProp, "tablebackground.timeofday.midday", "imgs/columns_midday.jpg");

  public static final StringProperty propTimeOfDayBackgroundAfternoon = new StringProperty(
      mProp, "tablebackground.timeofday.afternoon",
      "imgs/columns_afternoon.jpg");

  public static final StringProperty propTimeOfDayBackgroundEvening = new StringProperty(
      mProp, "tablebackground.timeofday.evening", "imgs/columns_evening.jpg");

  public static final BooleanProperty propShowAssistant = new BooleanProperty(
      mProp, "showassistant", true);

  public static final StringProperty propUserDefinedWebbrowser = new StringProperty(
      mProp, "webbrowser", null);

  /*
   * Basic tray settings
   */

  public static final BooleanProperty propTrayIsEnabled = new BooleanProperty(
      mProp, "trayIsEnabled", true);

  public static final BooleanProperty propTrayMinimizeTo = new BooleanProperty(
      mProp, "MinimizeToTray", false);

  public static final BooleanProperty propOnlyMinimizeWhenWindowClosing = new BooleanProperty(
      mProp, "onlyMinimizeWhenWindowClosing", false);

  public static final BooleanProperty propNowOnRestore = new BooleanProperty(
  		mProp, "jumpNowOnRestore",true);
  /*
   * Channel settings for the tray.
   */

  public static final BooleanProperty propTrayUseSpecialChannels = new BooleanProperty(
      mProp, "trayUseSpecialChannels", false);

  public static final ChannelArrayProperty propTraySpecialChannels = new ChannelArrayProperty(
      mProp, "traySpecialChannels", new devplugin.Channel[] {});
  
  public static final IntProperty propTrayChannelWidth = new IntProperty(
      mProp, "trayChannelWidth", 72);

  /*
   * Settings for the ON_TIME_TYPE of the ProgramMenuItem.
   */

  public static final BooleanProperty propTrayOnTimeProgramsEnabled = new BooleanProperty(
      mProp, "trayOnTimeProgramsEnabled", true);
  
  public static final BooleanProperty propTrayOnTimeProgramsInSubMenu = new BooleanProperty(
      mProp, "trayOnTimeProgramsInSubMenus", true);

  public static final BooleanProperty propTrayOnTimeProgramsContainsTime = new BooleanProperty(
      mProp, "trayOnTimeProgramsContainsTime", false);

  public static final BooleanProperty propTrayOnTimeProgramsContainsIcon = new BooleanProperty(
      mProp, "trayOnTimeProgramsContainsIcon", true);

  public static final BooleanProperty propTrayOnTimeProgramsContainsName = new BooleanProperty(
      mProp, "trayOnTimeProgramsContainsName", true);

  public static final BooleanProperty propTrayOnTimeProgramsContainsToolTip = new BooleanProperty(
      mProp, "trayOnTimeProgramsContainsToolTip", true);

  public static final BooleanProperty propTrayOnTimeProgramsShowProgress = new BooleanProperty(
      mProp, "trayOnTimeProgramsShowProgress", true);

  public static final ColorProperty propTrayOnTimeProgramsDarkBackground = new ColorProperty(
      mProp, "trayOnTimeProgramsDarkBackground", new Color(255, 150, 0, 80));

  public static final ColorProperty propTrayOnTimeProgramsLightBackground = new ColorProperty(
      mProp, "trayOnTimeProgramsLightBackground", new Color(255, 150, 0, 40));

  /*
   * Settings for the NOW_TYPE of the ProgramMenuItem.
   */

  public static final BooleanProperty propTrayNowProgramsEnabled = new BooleanProperty(
      mProp, "trayNowProgramsEnabled", true);

  public static final BooleanProperty propTrayNowProgramsInSubMenu = new BooleanProperty(
      mProp, "trayNowProgramsInSubMenus", false);

  public static final BooleanProperty propTrayNowProgramsContainsTime = new BooleanProperty(
      mProp, "trayNowProgramsContainsTime", false);

  public static final BooleanProperty propTrayNowProgramsContainsIcon = new BooleanProperty(
      mProp, "trayNowProgramsContainsIcon", true);

  public static final BooleanProperty propTrayNowProgramsContainsName = new BooleanProperty(
      mProp, "trayNowProgramsContainsName", true);

  public static final BooleanProperty propTrayNowProgramsContainsToolTip = new BooleanProperty(
      mProp, "trayNowProgramsContainsToolTip", true);

  /*
   * Settings for the SOON_TYPE of the ProgramMenuItem.
   */

  public static final BooleanProperty propTraySoonProgramsEnabled = new BooleanProperty(
      mProp, "traySoonProgramsEnabled", true);

  public static final BooleanProperty propTraySoonProgramsContainsTime = new BooleanProperty(
      mProp, "traySoonProgramsContainsTime", true);

  public static final BooleanProperty propTraySoonProgramsContainsIcon = new BooleanProperty(
      mProp, "traySoonProgramsContainsIcon", true);

  public static final BooleanProperty propTraySoonProgramsContainsName = new BooleanProperty(
      mProp, "traySoonProgramsContainsName", true);

  public static final BooleanProperty propTraySoonProgramsContainsToolTip = new BooleanProperty(
      mProp, "traySoonProgramsContainsToolTip", true);

  /*
   * Settings for the IMPORTANT_TYPE of the ProgramMenuItem.
   */

  public static final IntProperty propTrayImportantProgramsPriority = new IntProperty(
      mProp, "trayImportantProgramsPriority", 0
      );
  
  public static final BooleanProperty propTrayImportantProgramsEnabled = new BooleanProperty(
      mProp, "trayImportantProgramsEnabled", true);

  public static final BooleanProperty propTrayImportantProgramsInSubMenu = new BooleanProperty(
      mProp, "trayImportantProgramsInSubMenu", false);

  public static final IntProperty propTrayImportantProgramsSize = new IntProperty(
      mProp, "trayImportantProgramsSize", 5);

  public static final BooleanProperty propTrayImportantProgramsContainsIcon = new BooleanProperty(
      mProp, "trayImportantProgramsContainsIcon", true);

  public static final BooleanProperty propTrayImportantProgramsContainsName = new BooleanProperty(
      mProp, "trayImportantProgramsContainsName", true);

  public static final BooleanProperty propTrayImportantProgramsContainsDate = new BooleanProperty(
      mProp, "trayImportantProgramsContainsDate", true);

  public static final BooleanProperty propTrayImportantProgramsContainsTime = new BooleanProperty(
      mProp, "trayImportantProgramsContainsTime", true);

  public static final BooleanProperty propTrayImportantProgramsContainsToolTip = new BooleanProperty(
      mProp, "trayImportantProgramsContainsToolTip", true);

  /*
   * Toolbar settings
   */

  public static final ChoiceProperty propToolbarButtonStyle = new ChoiceProperty(
      mProp, "buttontype", "icon", new String[] { "text&icon", "text", "icon" });

  public static final BooleanProperty propIsTooolbarVisible = new BooleanProperty(
      mProp, "isToolbarVisible", true);

  public static final BooleanProperty propIsStatusbarVisible = new BooleanProperty(
      mProp, "isStatusbarVisible", true);

  /**
   * @deprecated Deprecated with Version 2.2, now propLookAndFeel is used
   */
  public static final BooleanProperty propIsSkinLFEnabled = new BooleanProperty(
      mProp, "skinLF.enabled", false);

  public static final StringProperty propSkinLFThemepack = new StringProperty(
      mProp, "skinLF.themepack", "themepacks/themepack.zip");

  public static final StringProperty propJGoodiesTheme = new StringProperty(
      mProp, "jgoodies.theme", "");

  public static final BooleanProperty propJGoodiesShadow = new BooleanProperty(
      mProp, "jgoodies.dropshadow", false);

  public static final StringProperty propLookAndFeel = new StringProperty(
      mProp, "lookandfeel1_1", mDefaultSettings.getProperty("lookandfeel",
          UIManager.getCrossPlatformLookAndFeelClassName()));

  public static final IntProperty propColumnWidth = new IntProperty(mProp,
      "columnwidth", 200);

  public static final IntArrayProperty propTimeButtons = new IntArrayProperty(
      mProp, "timeButtons", new int[] { 6 * 60, 12 * 60, 18 * 60, 20 * 60 });

  /**
   * @deprecated
   */
  public static final StringArrayProperty propHiddenPluginButtons = new StringArrayProperty(
      mProp, "hiddenPluginButtons", new String[0]);

  public static final StringArrayProperty propToolbarButtons = new StringArrayProperty(
      mProp, "toolbarButtons_2.0", null // we show all buttons, if this property
  // is not set
  );

  public static final BooleanProperty propToolbarUseBigIcons = new BooleanProperty(
      mProp, "toolbarUseBigIcons", true);

  public static final StringProperty propToolbarLocation = new StringProperty(
      mProp, "toolbarLocation", "north");

  public static final StringProperty propDefaultContextMenuIf = new StringProperty(
      mProp, "contextmenudefaultplugin", "programinfo.ProgramInfo");

  public static final StringProperty propMiddleClickIf = new StringProperty(
      mProp, "middleclickplugin", "programinfo.ProgramInfo");

  public static final StringProperty propLastUsedFilter = new StringProperty(
      mProp, "lastusedfilter", null);

  /**
   * The order of the plugin IDs.
   * <p>
   * In former times this property hold the list of plugin class names that
   * should be activated on startup (in the right order). Now it holds IDs, not
   * class names and activation is controlled by {@link #propDeactivatedPlugins}.
   */
  public static final StringArrayProperty propPluginOrder = new StringArrayProperty(
      mProp, "plugins", null);

  /**
   * Order of the Plugins in the Context-Menu.
   */
  public static final StringArrayProperty propContextMenuOrder = new StringArrayProperty(
      mProp, "contextMenuOrder", null);

  /**
   * All disabled Items of the ContextMenu
   */
  public static final StringArrayProperty propContextMenuDisabledItems = new StringArrayProperty(
      mProp, "contextMenuDisabledItems", null);

  /**
   * The ID's of the plugins that have been deactivated.
   * <p>
   * NOTE: By remembering the deactivated plugins rather the activated plugins
   * new plugins are activated automatically.
   */
  public static final StringArrayProperty propDeactivatedPlugins = new StringArrayProperty(
      mProp, "deactivatedPlugins", null);

  public static final IntProperty propDownloadPeriod = new IntProperty(mProp,
      "downloadperiod", 1);

  public static final ChoiceProperty propAutoDownloadType = new ChoiceProperty(
      mProp, "autodownload", "never", new String[] { "startup", "daily",
          "every3days", "weekly", "never" });

  public static final IntProperty propAutoDownloadPeriod = new IntProperty(
      mProp, "autodownloadperiod", 0);

  public static final BooleanProperty propAskForAutoDownload = new BooleanProperty(
      mProp, "askForAutoDownload", false);

  public static final DateProperty propLastDownloadDate = new DateProperty(
      mProp, "lastdownload", null);

  public static final FontProperty propProgramTitleFont = new FontProperty(
      mProp, "font.programtitle", PROGRAMTITLEFONT);

  public static final FontProperty propProgramInfoFont = new FontProperty(
      mProp, "font.programinfo", PROGRAMINFOFONT);

  public static final FontProperty propChannelNameFont = new FontProperty(
      mProp, "font.channelname", CHANNELNAMEFONT);

  public static final FontProperty propProgramTimeFont = new FontProperty(
      mProp, "font.programtime", PROGRAMTIMEFONT);

  public static final ProgramFieldTypeArrayProperty propProgramInfoFields = new ProgramFieldTypeArrayProperty(
      mProp, "programpanel.infoFields", new ProgramFieldType[] {
          ProgramFieldType.GENRE_TYPE, ProgramFieldType.EPISODE_TYPE,
          ProgramFieldType.ORIGIN_TYPE, ProgramFieldType.PRODUCTION_YEAR_TYPE,
          ProgramFieldType.SHOWVIEW_NR_TYPE,
          ProgramFieldType.SHORT_DESCRIPTION_TYPE });

  public static final StringArrayProperty propProgramTableIconPlugins = new StringArrayProperty(
      mProp, "programpanel.iconPlugins", new String[] { PICTURE_ID,INFO_ID,
          "tvraterplugin.TVRaterPlugin", });

  /** Used to enable border on on air programs */
  public static final BooleanProperty propProgramTableOnAirProgramsShowingBorder = new BooleanProperty(
      mProp, "programpanel.onAirProgramsShowingBorder", false);
  /** Color for Program on Air - This shows how much was shown til now */
  public static final ColorProperty propProgramTableColorOnAirDark = new ColorProperty(
      mProp, "programpanel.ColorOnAirDark", new Color(0, 0, 255, 60));
  /** Color for Program on Air - This shows how much is not shown til now */
  public static final ColorProperty propProgramTableColorOnAirLight = new ColorProperty(
      mProp, "programpanel.ColorOnAirLight", new Color(0, 0, 255, 30));
  
  /** Used to enable color marking for default mark priority 
    * @deprecated since 2.5.3 */
  public static final BooleanProperty propProgramTableMarkedDefaultPriorityShowsColor = new BooleanProperty(
      mProp, "programpanel.markedShowsColor", true);
  /** Used to track if a program panel should use addtional space for the mark icons */
  public static final BooleanProperty propProgramPanelUsesExtraSpaceForMarkIcons = new BooleanProperty(
      mProp, "programpanel.usesExtraSpaceForMarkIcons", true
      );
  /** Used to enable border on marked programs */
  public static final BooleanProperty propProgramPanelWithMarkingsShowingBoder = new BooleanProperty(
      mProp, "programpanel.markingsShowingBorder", true);
  /** Used default mark priority for markings of plugins. */
  public static final IntProperty propProgramPanelUsedDefaultMarkPriority = new IntProperty(
      mProp, "programpanel.defaultMarkPriority", 0);  
  /** Color for Programs marked with MIN_PRIORITY */
  public static final ColorProperty propProgramPanelMarkedMinPriorityColor = new ColorProperty(
      mProp, "programpanel.ColorMarked", new Color(255, 0, 0, 30));
  /** Color for Programs marked with LOWER_MEDIUM_PRIORITY */
  public static final ColorProperty propProgramPanelMarkedLowerMediumPriorityColor = new ColorProperty(
      mProp, "programpanel.ColorMarkedLowerMedium", new Color(0, 255, 255, 50));
  /** Color for Programs marked with MEDIUM_PRIORITY */
  public static final ColorProperty propProgramPanelMarkedMediumPriorityColor = new ColorProperty(
      mProp, "programpanel.ColorMarkedMedium", new Color(140, 255, 0, 60));
  /** Color for Programs marked with HIGHER_MEDIUM_PRIORITY */
  public static final ColorProperty propProgramPanelMarkedHigherMediumPriorityColor = new ColorProperty(
      mProp, "programpanel.ColorMarkedHigherMedium", new Color(255, 255, 0, 60));
  /** Color for Programs marked with MAX_PRIORITY */
  public static final ColorProperty propProgramPanelMarkedMaxPriorityColor = new ColorProperty(
      mProp, "programpanel.ColorMarkedMax", new Color(255, 180, 0, 110));

  public static final BooleanProperty propMouseOver = new BooleanProperty(
      mProp, "programpanel.MouseOver", false);

  /** Color for Mouse-Over */
  public static final ColorProperty propMouseOverColor = new ColorProperty(
      mProp, "programpanel.MouseOverColor", new Color(200, 200, 0, 60));

  /** Color for selected Program */
  public static final ColorProperty propKeyboardSelectedColor = new ColorProperty(
      mProp, "programpanel.KeyboardSelectedColor", new Color(130, 255, 0, 120));

  public static final BooleanProperty propTitelAlwaysVisible = new BooleanProperty(
      mProp, "programpanel.TitelAlwaysVisible", false);

  public static final BooleanProperty propIsWindowMaximized = new BooleanProperty(
      mProp, "window.isMaximized", false);

  public static final IntProperty propWindowWidth = new IntProperty(mProp,
      "window.width", 770);

  public static final IntProperty propWindowHeight = new IntProperty(mProp,
      "window.height", 500);

  public static final IntProperty propWindowX = new IntProperty(mProp,
      "window.x", -1);

  public static final IntProperty propWindowY = new IntProperty(mProp,
      "window.y", -1);
  
  public static final IntProperty propPluginChooserDlgWidth = new IntProperty(mProp,
      "pluginChooserDlg.width", -1);

  public static final IntProperty propPluginChooserDlgHeight = new IntProperty(mProp,
      "pluginChooserDlg.height", -1);

  public static final IntProperty propSettingsDialogDividerLocation = new IntProperty(mProp,
      "settingsDialogDividerLocation", 200);
  
  /** Settings-Window Width */
  public static final IntProperty propSettingsWindowWidth = new IntProperty(
      mProp, "settingsWindow.width", -1);
  /** Settings-Window Height */
  public static final IntProperty propSettingsWindowHeight = new IntProperty(
      mProp, "settingsWindow.height", -1);
  /** Settings-Window X-Position */
  public static final IntProperty propSettingsWindowX = new IntProperty(mProp,
      "settingsWindow.x", -1);
  /** Settings-Window Y-Position */
  public static final IntProperty propSettingsWindowY = new IntProperty(mProp,
      "settingsWindow.y", -1);

  public static final IntProperty propUpdateDialogDividerLocation = new IntProperty(
      mProp, "updateDialog.devider", -1);
  /** Update-Dialog Width */
  public static final IntProperty propUpdateDialogWidth = new IntProperty(
      mProp, "updateDialog.width", -1);
  /** Update-Dialog Height */
  public static final IntProperty propUpdateDialogHeight = new IntProperty(
      mProp, "updateDialog.height", -1);

  public static final IntProperty propProgramTableStartOfDay = new IntProperty(
      mProp, "programtable.startofday", 0);

  public static final IntProperty propProgramTableEndOfDay = new IntProperty(
      mProp, "programtable.endofday", 5 * 60);
  public static final BooleanProperty propHttpProxyUseProxy = new BooleanProperty(
      mProp, "proxy.http.useProxy", false);
  
  public static final IntProperty propDefaultNetworkConnectionTimeout = new IntProperty(
      mProp, "network.defaultConnectionTimeout", 60000);

  public static final IntProperty propNetworkCheckTimeout = new IntProperty(
      mProp, "network.checkTimeout", 10000);

  public static final IntProperty propPictureType = new IntProperty(
      mProp, "pictures.type", ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE);
  
  public static final StringArrayProperty propPicturePluginIds = new StringArrayProperty(
      mProp, "pictures.pluginIds", new String[0]);
  
  public static final IntProperty propPictureStartTime = new IntProperty(
      mProp, "pictures.startTime", 18 * 60);
  
  public static final IntProperty propPictureEndTime = new IntProperty(
      mProp, "pictures.endTime", 23 * 60);

  public static final IntProperty propPictureDuration = new IntProperty(
      mProp, "pictures.duration", 10);

  public static final BooleanProperty propIsPictureShowingDescription = new BooleanProperty(
      mProp, "pictures.showDescription", true);
  
  public static final StringProperty propHttpProxyHost = new StringProperty(
      mProp, "proxy.http.host", "");

  public static final StringProperty propHttpProxyPort = new StringProperty(
      mProp, "proxy.http.port", "");

  public static final BooleanProperty propHttpProxyAuthentifyAtProxy = new BooleanProperty(
      mProp, "proxy.http.authentifyAtProxy", false);

  public static final StringProperty propHttpProxyUser = new StringProperty(
      mProp, "proxy.http.user", "");

  public static final EncodedStringProperty propHttpProxyPassword = new EncodedStringProperty(
      mProp, "proxy.http.password", "", PROXY_PASSWORD_SEED);

  public static final StringArrayProperty propDataServicesForUpdate = new StringArrayProperty(
      mProp, "tvdataservices.update", null);

  public static final BooleanProperty propShowPluginView = new BooleanProperty(
      mProp, "show.pluginview", false);

  public static final BooleanProperty propShowTimeButtons = new BooleanProperty(
      mProp, "show.timebuttons", true);

  public static final BooleanProperty propShowChannels = new BooleanProperty(
      mProp, "show.channels", true);

  public static final BooleanProperty propShowDatelist = new BooleanProperty(
      mProp, "show.datelist", true);

  public static final BooleanProperty propShowFilterBar = new BooleanProperty(
      mProp, "show.filterbar", true);

  public static final SplitViewProperty propViewRoot = new SplitViewProperty(
      mProp, "view.root", false, true, 50);

  public static final SplitViewProperty propViewMainframe = new SplitViewProperty(
      mProp, "view.mainframe", false, false, 100);

  public static final SplitViewProperty propViewNavigation = new SplitViewProperty(
      mProp, "view.navigation", true, true, 100);

  public static final SplitViewProperty propViewDateChannel = new SplitViewProperty(
      mProp, "view.date_channel", true, true, 100);

  public static final BooleanProperty propSplashShow = new BooleanProperty(
      mProp, "splash.show", true);
  /**
   * The Splash-Image
   */
  public static final StringProperty propSplashImage = new StringProperty(
      mProp, "splash.file", "imgs/splash.png");

  /**
   * The X-Position of the Text in the Splash
   */
  public static final IntProperty propSplashTextPosX = new IntProperty(mProp,
      "splash.textPosX", 10);

  /**
   * The Y-Position of the Text in the Splash
   */
  public static final IntProperty propSplashTextPosY = new IntProperty(mProp,
      "splash.textPosY", 262);

  /**
   * Background-Color
   */
  public static final ColorProperty propSplashBackgroundColor = new ColorProperty(
      mProp, "splash.BackgroundColor", new Color(63, 114, 133));

  /**
   * Foreground-Color
   */
  public static final ColorProperty propSplashForegroundColor = new ColorProperty(
      mProp, "splash.ForegroundColor", Color.WHITE);

  public static final StringProperty propLanguage = new StringProperty(mProp,
      "language", System.getProperty("user.language"));

  public static final StringProperty propCountry = new StringProperty(mProp,
      "country", System.getProperty("user.country", ""));  
  
  public static final StringProperty propVariant = new StringProperty(mProp,
      "variant", System.getProperty("user.variant",""));  
  
  public static final StringProperty propTimezone = new StringProperty(mProp,
      "timeZone", null);

  public static final BooleanProperty propMinimizeAfterStartup = new BooleanProperty(
      mProp, "minimizeAfterStartup", false);

  public static final StringProperty propLogdirectory = new StringProperty(
      mProp, "logdirectory", mDefaultSettings.getProperty("logdirectory", null));

  public static final BooleanProperty propEnableChannelIcons = new BooleanProperty(
      mProp, "enableChannelIcons", true);

  public static final BooleanProperty propShowChannelNames = new BooleanProperty(
      mProp, "showChannelNames", true);

  public static final BooleanProperty propShowChannelIconsInProgramTable = new BooleanProperty(
      mProp, "showChannelIconsInProgramtable", true);

  public static final BooleanProperty propShowChannelNamesInProgramTable = new BooleanProperty(
      mProp, "showChannelNamesInProgramtable", true);
  
  public static final BooleanProperty propShowChannelIconsInChannellist = new BooleanProperty(
      mProp, "showChannelIconsInChannellist", true);

  public static final BooleanProperty propShowChannelNamesInChannellist = new BooleanProperty(
      mProp, "showChannelNamesInChannellist", true);
  
  public static final StringArrayProperty propUsedChannelGroups = new StringArrayProperty(
      mProp, "usedChannelGroups", null);
  
  public static final StringArrayProperty propDeleteFilesAtStart = new StringArrayProperty(
      mProp, "deleteFilesAtStart", new String[0]);

  /**
   * The IconTheme
   */
  public static final StringProperty propIcontheme = new StringProperty(mProp,
      "icontheme", mDefaultSettings.getProperty("icontheme", null));

  /**
   * Show the "The Browser was opened"-Dialog
   */
  public static final BooleanProperty propShowBrowserOpenDialog = new BooleanProperty(
      mProp, "showBrowserOpenDialog", true);

  /**
   * Show the SearchField in the Toolbar
   */
  public static final BooleanProperty propIsSearchFieldVisible = new BooleanProperty(
      mProp, "isSearchFieldVisible", true);

  /**
   * Use 12-Hour Format?
   */
  public static final BooleanProperty propTwelveHourFormat = new BooleanProperty(
      mProp, "uswTwelveHourFormat", false);


    static {
        if (!GraphicsEnvironment.isHeadless()) {
            propJGoodiesTheme.setDefault(PlasticLookAndFeel.createMyDefaultTheme().getClass().getName());        
        }
    }
  
    
  /** An array with the ids of the tv data service which license was accepted. */
  public static final StringArrayProperty propAcceptedLicenseArrForServiceIds = new StringArrayProperty(
      mProp, "licnseIds", new String[] {});

  /** the class name of the last settings tab that has been closed with OK before */ 
  public static final StringProperty propLastUsedSettingsPath = new StringProperty(mProp, "lastUsedSettingsTabClassName", "#channels");
  public static final int MAX_COLUMN_WIDTH = 300;
  public static final int MIN_COLUMN_WIDTH = 60;
  
  /** The setting that contains the global picture settings value */
  public static final IntProperty propPluginsPictureSetting = new IntProperty(
      mProp, "pluginsPictureSetting", PluginPictureSettings.PICTURE_AND_DISCRIPTION_TYPE);
  
  /** The user selected default filter */
  public static final StringProperty propDefaultFilter = new StringProperty(
      mProp, "defaultFilter", null);
}