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

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.UIManager;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;

import tvbrowser.core.plugin.DefaultSettings;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.programtable.DefaultProgramTableModel;
import tvbrowser.ui.programtable.ProgramTableScrollPane;
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
import util.settings.ProgramFieldTypeArrayProperty;
import util.settings.Property;
import util.settings.PropertyManager;
import util.settings.StringArrayProperty;
import util.settings.StringProperty;
import util.settings.VersionProperty;
import util.ui.view.SplitViewProperty;
import devplugin.ProgramFieldType;

/**
 * The Settings class provides access to the settings of the whole application
 * (except the plugins).
 *
 * @author Martin Oberhauser
 */
public class Settings {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(Settings.class.getName());

  private static DefaultSettings mDefaultSettings = new DefaultSettings();

  private static final long PROXY_PASSWORD_SEED = 6528587292713416704L;

  public static final int GET_DATA_FROM_SERVER=0, GET_DATA_FROM_LOCAL_DISK=1;

  public static final int NEVER=0, DAILY=1, ONSTARTUP=DAILY, EVERY3DAYS=2, WEEKLY=3;

  private static final String SETTINGS_FILE="settings.prop";
  private static final String DEFAULT_USER_DIR = ".tvbrowser";

  private static final Font PROGRAMTITLEFONT=new Font("Dialog",Font.BOLD,12);
  private static final Font PROGRAMINFOFONT=new Font("Dialog",Font.PLAIN,10);
  private static final Font CHANNELNAMEFONT=new Font("Dialog",Font.BOLD,12);
  private static final Font PROGRAMTIMEFONT=new Font("Dialog",Font.BOLD,12);


  private static PropertyManager mProp = new PropertyManager();


  /**
   * Returns the Default-Settings. These Settings are stored in the
   * mac, windows and linux.properties-Files
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
    String dir = new StringBuffer(System.getProperty("user.home")).append(File.separator).append(DEFAULT_USER_DIR).toString();
	return mDefaultSettings.getProperty("userdir", dir);
  }


  /**
   * Store all settings. This method is called on quitting the application.
   */
  public static void storeSettings() throws TvBrowserException {
    File f=new File(getUserDirectoryName());
    if (!f.exists()) {
      f.mkdirs();
    }

    File settingsFile = new File(getUserDirectoryName(), SETTINGS_FILE);
    try {
      mProp.writeToFile(settingsFile);
    }
    catch (IOException exc) {
      throw new TvBrowserException(Settings.class, "error.1",
        "Error when saving settings!\n({0})", settingsFile.getAbsolutePath(), exc);
    }
  }


  /**
   * Reads the settings from settings file. If there is no settings file, default
   * settings are used.
   */
  public static void loadSettings() {

    String oldDirectoryName = System.getProperty("user.home", "") + File.separator + ".tvbrowser";
    String newDirectoryName = getUserDirectoryName();
    File settingsFile = new File(newDirectoryName, SETTINGS_FILE);

    if (settingsFile.exists()) {
      try {
        mProp.readFromFile(settingsFile);
        mLog.info("Using settings from file "+settingsFile.getAbsolutePath());
      }
      catch (IOException evt) {
        mLog.info("Could not read settings - using default user settings");
      }
    }
    /* If the settings file doesn't exist, we try to import the settings created
       by a previous version of TV-Browser */
    else if (!oldDirectoryName.equals(newDirectoryName)) {
      mLog.info("Try to load settings from a previous version of TV-Browser");
      File oldDir = new File(oldDirectoryName);
      if (oldDir.isDirectory() && oldDir.exists()) {
        File newDir = new File(getUserDirectoryName());
        if (newDir.mkdirs()) {
          try {
            IOUtilities.copy(oldDir.listFiles(), newDir);
            mLog.info("settings form previous version copied successfully");
            File newSettingsFile = new File(newDir, SETTINGS_FILE);
            mProp.readFromFile(newSettingsFile);
            mLog.info("settings form previous version read succesfully");
            oldDir.renameTo(new File(System.getProperty("user.home", "") + File.separator + "tvbrowser_BACKUP"));
          } catch(IOException e) {
            mLog.log(Level.WARNING, "Could not import user settings from '" + oldDir.getAbsolutePath()+ "' to '"+newDir.getAbsolutePath()+"'", e);
          }
        }
        else {
          mLog.info("Could not create directory '"+newDir.getAbsolutePath()+"' - using default user settings");
        }
      }
      else {
        mLog.info("No previous version of TV-Browser found - using default user settings");
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

    propArr = new Property[] {
      propProgramTitleFont, propProgramInfoFont, propProgramTimeFont,
      propChannelNameFont, propUseDefaultFonts, propEnableAntialiasing
    };

    if (mProp.hasChanged(propArr)) {
      util.ui.ProgramPanel.updateFonts();
      tvbrowser.ui.programtable.ChannelPanel.fontChanged();
      ProgramTableScrollPane scrollPane = mainFrame.getProgramTableScrollPane();
      scrollPane.forceRepaintAll();
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

    propArr = new Property[] {
      propTableBackgroundStyle, propOneImageBackground,
      propTimeBlockSize, propTimeBlockBackground1, propTimeBlockBackground2,
      propTimeBlockShowWest, propTimeBlockWestImage1, propTimeBlockWestImage2,
      propTimeOfDayBackgroundEdge, propTimeOfDayBackgroundEarly,
      propTimeOfDayBackgroundMidday, propTimeOfDayBackgroundAfternoon,
      propTimeOfDayBackgroundEvening
    };
    if (mProp.hasChanged(propArr)) {
      ProgramTableScrollPane scrollPane = mainFrame.getProgramTableScrollPane();
      scrollPane.getProgramTable().updateBackground();
    }

    propArr = new Property[] {
      propToolbarButtonStyle,
      propToolbarButtons, propToolbarLocation, propIsTooolbarVisible,
      propToolbarUseBigIcons
    };
    if (mProp.hasChanged(propArr)) {
      mainFrame.updateToolbar();
    }

    if (mProp.hasChanged(propTimeButtons)) {
      mainFrame.updateButtons();
    }

    if (mProp.hasChanged(propSubscribedChannels)) {
      ChannelList.create();
      DefaultProgramTableModel model = mainFrame.getProgramTableModel();
      model.setChannels(ChannelList.getSubscribedChannels());
      mainFrame.updateChannellist();
    }

    propArr = new Property[] {
      propProgramTableStartOfDay, propProgramTableEndOfDay
    };
    if (mProp.hasChanged(propArr)) {
      DefaultProgramTableModel model = mainFrame.getProgramTableModel();
      int startOfDay = propProgramTableStartOfDay.getInt();
      int endOfDay   = propProgramTableEndOfDay.getInt();
      model.setTimeRange(startOfDay, endOfDay);
    }



    propArr = new Property[] {
      propProgramTableIconPlugins, propProgramInfoFields
    };
    if (mProp.hasChanged(propArr)) {
      // Force a recreation of the table content
      DefaultProgramTableModel model = mainFrame.getProgramTableModel();
      model.setDate(mainFrame.getCurrentSelectedDate(), null, null);
    }

    propArr = new Property[] {
      propEnableChannelIcons, propShowChannelIconsInProgramTable, propShowChannelIconsInChannellist, propShowChannelNames
    };
    if (mProp.hasChanged(propArr)) {
      mainFrame.getProgramTableScrollPane().updateChannelPanel();
      mainFrame.updateChannelChooser();
    }



    mProp.clearChanges();
  }


  public static final VersionProperty propTVBrowserVersion
    = new VersionProperty(mProp, "version", null);

  public static final BooleanProperty propUseDefaultFonts
    = new BooleanProperty(mProp, "usedefaultfonts", true);

  public static final BooleanProperty propEnableAntialiasing
  = new BooleanProperty(mProp, "enableantialiasing", false);

  private static String getDefaultTvDataDir() {
    return getUserDirectoryName() + File.separator + "tvdata"; 
  }

  private static String getDefaultPluginsDir() {
    return getUserDirectoryName() + File.separator + "plugins";
  }

  public static final StringProperty propTVDataDirectory
    = new StringProperty(mProp, "dir.tvdata", mDefaultSettings.getProperty("tvdatadir",getDefaultTvDataDir()));

  public static final StringProperty propPluginsDirectory
      = new StringProperty(mProp, "dir.plugins", mDefaultSettings.getProperty("pluginsdir",getDefaultPluginsDir()));


  public static final StringProperty propFilterDirectory
    = new StringProperty(mProp, "directory.filters",
                         getUserDirectoryName() + File.separator + "filters");

  public static final ChannelArrayProperty propSubscribedChannels
    = new ChannelArrayProperty(mProp, "subscribedchannels", new devplugin.Channel[]{});

  /**
   * @deprecated
   */
  public static final IntProperty propEarlyTime
    = new IntProperty(mProp, "timebutton.early", 6 * 60);

  /**
   * @deprecated
   */
  public static final IntProperty propMiddayTime
    = new IntProperty(mProp, "timebutton.midday", 12 * 60);

  /**
   * @deprecated
   */
  public static final IntProperty propAfternoonTime
    = new IntProperty(mProp, "timebutton.afternoon", 18 * 60);

  /**
   * @deprecated
   */
  public static final IntProperty propEveningTime
    = new IntProperty(mProp, "timebutton.evening", 20 * 60);



  public static final ChoiceProperty propTableLayout
    = new ChoiceProperty(mProp, "table.layout", "timeSynchronous",
                         new String[] { "timeSynchronous", "compact", "realSynchronous" });

  public static final ChoiceProperty propTableBackgroundStyle
    = new ChoiceProperty(mProp, "tablebackground.style", "timeOfDay",
                new String[] { "white", "oneImage", "timeBlock", "timeOfDay" });

  public static final StringProperty propOneImageBackground
    = new StringProperty(mProp, "tablebackground.oneImage.image", "imgs/columns_evening.jpg");

  public static final IntProperty propTimeBlockSize
    = new IntProperty(mProp, "tablebackground.timeBlock.size", 2);

  public static final StringProperty propTimeBlockBackground1
    = new StringProperty(mProp, "tablebackground.timeBlock.image1", "imgs/columns_evening.jpg");

  public static final StringProperty propTimeBlockBackground2
    = new StringProperty(mProp, "tablebackground.timeBlock.image2", "imgs/columns_afternoon.jpg");

  public static final StringProperty propTimeBlockWestImage1
    = new StringProperty(mProp, "tablebackground.timeBlock.west1", "imgs/columns_evening.jpg");

  public static final StringProperty propTimeBlockWestImage2
    = new StringProperty(mProp, "tablebackground.timeBlock.west2", "imgs/columns_afternoon.jpg");

  public static final BooleanProperty propTimeBlockShowWest
    = new BooleanProperty(mProp, "tablebackground.timeBlock.showWest", true);

  public static final StringProperty propTimeOfDayBackgroundEdge
    = new StringProperty(mProp, "tablebackground.timeofday.edge", "imgs/columns_edge.jpg");

  public static final StringProperty propTimeOfDayBackgroundEarly
    = new StringProperty(mProp, "tablebackground.timeofday.early", "imgs/columns_early.jpg");

  public static final StringProperty propTimeOfDayBackgroundMidday
    = new StringProperty(mProp, "tablebackground.timeofday.midday", "imgs/columns_midday.jpg");

  public static final StringProperty propTimeOfDayBackgroundAfternoon
    = new StringProperty(mProp, "tablebackground.timeofday.afternoon", "imgs/columns_afternoon.jpg");

  public static final StringProperty propTimeOfDayBackgroundEvening
    = new StringProperty(mProp, "tablebackground.timeofday.evening", "imgs/columns_evening.jpg");


  public static final BooleanProperty propShowAssistant
    = new BooleanProperty(mProp, "showassistant", true);

  public static final StringProperty propUserDefinedWebbrowser
    = new StringProperty(mProp, "webbrowser", null);

  public static final BooleanProperty propOnlyMinimizeWhenWindowClosing
    = new BooleanProperty(mProp, "onlyMinimizeWhenWindowClosing", false);

  public static final BooleanProperty propTrayIsEnabled
  = new BooleanProperty(mProp, "trayIsEnabled", true);  
  
  public static final BooleanProperty propMinimizeToTray
  = new BooleanProperty(mProp, "MinimizeToTray", false);

  public static final BooleanProperty propUseSingeClickInTray
  = new BooleanProperty(mProp, "SingeClickInTray", false);
  
  public static final BooleanProperty propShowProgramsInTrayWasConfigured
  = new BooleanProperty(mProp, "showProgramsInTrayWasConfigured", false);    
  
  public static final BooleanProperty propShowTimeProgramsInTray
  = new BooleanProperty(mProp, "showTimeProgramsInTray", true); 

  public static final BooleanProperty propTimeProgramsInTrayContainsTime
  = new BooleanProperty(mProp, "timeProgramsInTrayContainsTime", false); 

  public static final ColorProperty propTimeProgramsDarkBackground
    = new ColorProperty(mProp, "timeProgramsDarkBackground", new Color(255, 150, 0, 80));
  
  public static final ColorProperty propTimeProgramsLightBackground
    = new ColorProperty(mProp, "timeProgramsLightBackground", new Color(255, 150, 0, 40));  
  
  public static final BooleanProperty propShowNowRunningProgramsInTray
  = new BooleanProperty(mProp, "showRunningProgramsInTray", true);

  public static final BooleanProperty propShowNowRunningProgramsInTrayInSubMenu
  = new BooleanProperty(mProp, "showRunningProgramsInTrayInSubMenus", false);
  
  public static final BooleanProperty propNowRunningProgramsInTrayContainsStartTime
  = new BooleanProperty(mProp, "nowRunningProgramsInTrayContainsStartTime", false);
  
  public static final BooleanProperty propShowImportantProgramsInTray
  = new BooleanProperty(mProp, "showImportantProgramsInTray", true);
  
  public static final BooleanProperty propShowImportantProgramsInTrayInSubMenu
  = new BooleanProperty(mProp, "showImportantProgramsInTrayInSubMenu", false);  
  
  public static final BooleanProperty propImportantProgramsInTrayContainsStartTime
  = new BooleanProperty(mProp, "importantProgramsInTrayContainsStartTime", true);

  public static final BooleanProperty propImportantProgramsInTrayContainsDate
  = new BooleanProperty(mProp, "importantProgramsInTrayContainsDate", true);

  public static final IntProperty propImportantProgramsInTraySize
  = new IntProperty(mProp, "importantProgramsInTraySize", 5);
  
  public static final IntProperty propImportantProgramsInTrayHours
  = new IntProperty(mProp, "importantProgramsInTrayHours", 2);  
  
  public static final BooleanProperty propProgramsInTrayContainsChannel
  = new BooleanProperty(mProp, "programsInTrayContainsChannel", true);

  public static final BooleanProperty propProgramsInTrayContainsChannelIcon
  = new BooleanProperty(mProp, "programsInTrayContainsChannelIcon", true);

  public static final BooleanProperty propProgramsInTrayShowTooltip
  = new BooleanProperty(mProp, "programsInTrayShowTooltip", true);
 
  public static final ChannelArrayProperty propNowRunningProgramsInTrayChannels
  = new ChannelArrayProperty(mProp, "nowRunningProgramsInTrayChannels", new devplugin.Channel[]{}); 
  
  public static final ChoiceProperty propToolbarButtonStyle
    = new ChoiceProperty(mProp, "buttontype", "icon",
                         new String[] { "text&icon", "text", "icon" });

  public static final BooleanProperty propIsTooolbarVisible
    = new BooleanProperty(mProp, "isToolbarVisible", true);

  public static final BooleanProperty propIsStatusbarVisible
  = new BooleanProperty(mProp, "isStatusbarVisible", true);

  /**
   * @deprecated Deprecated with Version 2.2, now propLookAndFeel is used
   */
  public static final BooleanProperty propIsSkinLFEnabled
    = new BooleanProperty(mProp, "skinLF.enabled", false);

  public static final StringProperty propSkinLFThemepack
    = new StringProperty(mProp, "skinLF.themepack", "themepacks/themepack.zip");

  public static final StringProperty propJGoodiesTheme
  = new StringProperty(mProp, "jgoodies.theme", PlasticLookAndFeel.createMyDefaultTheme().getClass().getName());

  public static final BooleanProperty propJGoodiesShadow
  = new BooleanProperty(mProp, "jgoodies.dropshadow", false);
  
  public static final StringProperty propLookAndFeel
    = new StringProperty(mProp, "lookandfeel1_1",mDefaultSettings.getProperty("lookandfeel",UIManager.getCrossPlatformLookAndFeelClassName()));

  public static final IntProperty propColumnWidth
    = new IntProperty(mProp, "columnwidth", 200);

  public static final IntArrayProperty propTimeButtons
  = new IntArrayProperty(mProp, "timeButtons", new int[]{6*60, 12*60, 18*60, 20*60});

  /**
   * @deprecated
   */
  public static final StringArrayProperty propHiddenPluginButtons
    = new StringArrayProperty(mProp, "hiddenPluginButtons", new String[0]);



  public static final StringArrayProperty propToolbarButtons
    = new StringArrayProperty(mProp, "toolbarButtons_2.0",
            null  // we show all buttons, if this property is not set
     );

  public static final BooleanProperty propToolbarUseBigIcons
    = new BooleanProperty(mProp, "toolbarUseBigIcons", true);

  public static final StringProperty propToolbarLocation
    = new StringProperty(mProp, "toolbarLocation", "north");

  public static final StringProperty propDefaultContextMenuIf
    = new StringProperty(mProp, "contextmenudefaultplugin", "programinfo.ProgramInfo");

  public static final StringProperty propMiddleClickIf
    = new StringProperty(mProp, "middleclickplugin", "programinfo.ProgramInfo");

  public static final StringProperty propLastUsedFilter
  = new StringProperty(mProp, "lastusedfilter", null);

  /**
   * The order of the plugin IDs.
   * <p>
   * In former times this property hold the list of plugin class names that
   * should be activated on startup (in the right order). Now it holds IDs, not
   * class names and activation is controlled by {@link #propDeactivatedPlugins}.
   */
  public static final StringArrayProperty propPluginOrder
    = new StringArrayProperty(mProp, "plugins", null);

  /**
   * Order of the Plugins in the Context-Menu. 
   */
  public static final StringArrayProperty propContextMenuOrder
  = new StringArrayProperty(mProp, "contextMenuOrder", null);

  /**
   * All disabled Items of the ContextMenu
   */
  public static final StringArrayProperty propContextMenuDisabledItems
  = new StringArrayProperty(mProp, "contextMenuDisabledItems", null);

  /**
   * The ID's of the plugins that have been deactivated.
   * <p>
   * NOTE: By remembering the deactivated plugins rather the activated plugins
   *       new plugins are activated automatically.
   */
  public static final StringArrayProperty propDeactivatedPlugins
    = new StringArrayProperty(mProp, "deactivatedPlugins", null);

  public static final IntProperty propDownloadPeriod
    = new IntProperty(mProp, "downloadperiod", 0);

  public static final ChoiceProperty propAutoDownloadType
    = new ChoiceProperty(mProp, "autodownload", "never",
          new String[] { "startup", "daily", "every3days", "weekly", "never" });

  public static final IntProperty propAutoDownloadPeriod
    = new IntProperty(mProp, "autodownloadperiod", 0);

  public static final BooleanProperty propAskForAutoDownload
    = new BooleanProperty(mProp, "askForAutoDownload", false);

  public static final DateProperty propLastDownloadDate
    = new DateProperty(mProp, "lastdownload", null);

  public static final FontProperty propProgramTitleFont
    = new FontProperty(mProp, "font.programtitle", PROGRAMTITLEFONT);

  public static final FontProperty propProgramInfoFont
    = new FontProperty(mProp, "font.programinfo", PROGRAMINFOFONT);

  public static final FontProperty propChannelNameFont
    = new FontProperty(mProp, "font.channelname", CHANNELNAMEFONT);

  public static final FontProperty propProgramTimeFont
    = new FontProperty(mProp, "font.programtime", PROGRAMTIMEFONT);

  public static final ProgramFieldTypeArrayProperty propProgramInfoFields
    = new ProgramFieldTypeArrayProperty(mProp, "programpanel.infoFields",
                           new ProgramFieldType[] {
                             ProgramFieldType.GENRE_TYPE,
                             ProgramFieldType.EPISODE_TYPE,
                             ProgramFieldType.ORIGIN_TYPE,
                             ProgramFieldType.PRODUCTION_YEAR_TYPE,
                             ProgramFieldType.SHOWVIEW_NR_TYPE,
                             ProgramFieldType.SHORT_DESCRIPTION_TYPE
                           });

  public static final StringArrayProperty propProgramTableIconPlugins
    = new StringArrayProperty(mProp, "programpanel.iconPlugins",
                              new String[] {
                                "info.id",
                                "tvraterplugin.TVRaterPlugin",
                              });


  /** Color for Program on Air - This shows how much was shown til now */
  public static final ColorProperty propProgramTableColorOnAirDark
    = new ColorProperty(mProp, "programpanel.ColorOnAirDark", new Color(128, 128, 255, 80));
  /** Color for Program on Air - This shows how much is not shown til now */
  public static final ColorProperty propProgramTableColorOnAirLight
    = new ColorProperty(mProp, "programpanel.ColorOnAirLight", new Color(128, 128, 255, 40));
  /** Color for marked Program */
  public static final ColorProperty propProgramTableColorMarked
    = new ColorProperty(mProp, "programpanel.ColorMarked", new Color(255, 0, 0, 40));

  public static final BooleanProperty propMouseOver
    = new BooleanProperty(mProp, "programpanel.MouseOver", false);

  /** Color for Mouse-Over */
  public static final ColorProperty propMouseOverColor
    = new ColorProperty(mProp, "programpanel.MouseOverColor", new Color(200, 200, 0, 40));

  /** Color for selected Program*/
  public static final ColorProperty propKeyboardSelectedColor
    = new ColorProperty(mProp, "programpanel.KeyboardSelectedColor", new Color(130, 255, 0, 100));

  
  public static final BooleanProperty propTitelAlwaysVisible
    = new BooleanProperty(mProp, "programpanel.TitelAlwaysVisible", false);

  public static final BooleanProperty propIsWindowMaximized
    = new BooleanProperty(mProp, "window.isMaximized", false);

  public static final IntProperty propWindowWidth
    = new IntProperty(mProp, "window.width", 770);

  public static final IntProperty propWindowHeight
    = new IntProperty(mProp, "window.height", 500);

  public static final IntProperty propWindowX
    = new IntProperty(mProp, "window.x", -1);

  public static final IntProperty propWindowY
    = new IntProperty(mProp, "window.y", -1);

  /** Settings-Window Width  */
  public static final IntProperty propSettingsWindowWidth
    = new IntProperty(mProp, "settingsWindow.width", -1);
  /** Settings-Window Height  */
  public static final IntProperty propSettingsWindowHeight
    = new IntProperty(mProp, "settingsWindow.height", -1);
  /** Settings-Window X-Position  */
  public static final IntProperty propSettingsWindowX
    = new IntProperty(mProp, "settingsWindow.x", -1);
  /** Settings-Window Y-Position  */
  public static final IntProperty propSettingsWindowY
    = new IntProperty(mProp, "settingsWindow.y", -1);
  
  
  public static final IntProperty propProgramTableStartOfDay
    = new IntProperty(mProp, "programtable.startofday", 0);

  public static final IntProperty propProgramTableEndOfDay
    = new IntProperty(mProp, "programtable.endofday", 5 * 60);
  public static final BooleanProperty propHttpProxyUseProxy
    = new BooleanProperty(mProp, "proxy.http.useProxy", false);

  public static final StringProperty propHttpProxyHost
    = new StringProperty(mProp, "proxy.http.host", "");

  public static final StringProperty propHttpProxyPort
    = new StringProperty(mProp, "proxy.http.port", "");

  public static final BooleanProperty propHttpProxyAuthentifyAtProxy
    = new BooleanProperty(mProp, "proxy.http.authentifyAtProxy", false);

  public static final StringProperty propHttpProxyUser
    = new StringProperty(mProp, "proxy.http.user", "");

  public static final EncodedStringProperty propHttpProxyPassword
    = new EncodedStringProperty(mProp, "proxy.http.password", "", PROXY_PASSWORD_SEED);

  public static final StringArrayProperty propDataServicesForUpdate
    = new StringArrayProperty(mProp,"tvdataservices.update",null);


  public static final BooleanProperty propShowPluginView
    = new BooleanProperty(mProp,"show.pluginview", false);

  public static final BooleanProperty propShowTimeButtons
    = new BooleanProperty(mProp,"show.timebuttons", true);

  public static final BooleanProperty propShowChannels
  = new BooleanProperty(mProp,"show.channels", true);

  public static final BooleanProperty propShowDatelist
  = new BooleanProperty(mProp,"show.datelist", true);


  public static final SplitViewProperty propViewRoot
    = new SplitViewProperty(mProp,"view.root", false, true, 50);

  public static final SplitViewProperty propViewMainframe
    = new SplitViewProperty(mProp,"view.mainframe", false, false, 50);

  public static final SplitViewProperty propViewNavigation
  = new SplitViewProperty(mProp,"view.navigation", true, true, 30);

  public static final SplitViewProperty propViewDateChannel
  = new SplitViewProperty(mProp,"view.date_channel", true, true, 100);


  public static final BooleanProperty propSplashShow
  = new BooleanProperty(mProp, "splash.show", true);
  /**
   * The Splash-Image
   */
  public static final StringProperty propSplashImage
  = new StringProperty(mProp,"splash.file", "imgs/splash.png");

  /**
   * The X-Position of the Text in the Splash
   */
  public static final IntProperty propSplashTextPosX
  = new IntProperty(mProp, "splash.textPosX", 10);

  /**
   * The Y-Position of the Text in the Splash
   */
  public static final IntProperty propSplashTextPosY
  = new IntProperty(mProp, "splash.textPosY", 262);

  /**
   * Background-Color
   */
  public static final ColorProperty propSplashBackgroundColor
  = new ColorProperty(mProp, "splash.BackgroundColor", new Color(63, 114, 133));

  /**
   * Foreground-Color
   */
  public static final ColorProperty propSplashForegroundColor
  = new ColorProperty(mProp, "splash.ForegroundColor", Color.WHITE);

  public static final StringProperty propLanguage
  = new StringProperty(mProp, "language", System.getProperty("user.language"));

  public static final StringProperty propTimezone
  = new StringProperty(mProp, "timeZone", null);

  public static final BooleanProperty propMinimizeAfterStartup
  = new BooleanProperty(mProp, "minimizeAfterStartup", false);

  public static final StringProperty propLogdirectory
  = new StringProperty(mProp, "logdirectory", mDefaultSettings.getProperty("logdirectory", null));

  public static final BooleanProperty propEnableChannelIcons
  = new BooleanProperty(mProp, "enableChannelIcons", true);

  public static final BooleanProperty propShowChannelNames
  = new BooleanProperty(mProp, "showChannelNames", true);

  public static final BooleanProperty propShowChannelIconsInProgramTable
  = new BooleanProperty(mProp, "showChannelIconsInProgramtable", true);

  public static final BooleanProperty propShowChannelIconsInChannellist
    = new BooleanProperty(mProp, "showChannelIconsInChannellist", true);

  public static final StringArrayProperty propSubscribedChannelGroups
    = new StringArrayProperty(mProp, "subscribedChannelGroups", null);

  public static final StringArrayProperty propDeleteFilesAtStart
    = new StringArrayProperty(mProp, "deleteFilesAtStart", new String[0]);
    
  /**
   * The IconTheme
   */
  public static final StringProperty propIcontheme
    = new StringProperty(mProp, "icontheme", mDefaultSettings.getProperty("icontheme", null));
  
  /**
   * Show the "The Browser was opened"-Dialog
   */
  public static final BooleanProperty propShowBrowserOpenDialog
    = new BooleanProperty(mProp, "showBrowserOpenDialog", true);

  /**
   * Show the SearchField in the Toolbar
   */
  public static final BooleanProperty propIsSearchFieldVisible
  = new BooleanProperty(mProp, "isSearchFieldVisible", true);
}