/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
import javax.swing.UIManager;
import tvbrowser.TVBrowser;
import tvbrowser.core.plugin.DefaultSettings;
import tvbrowser.ui.finder.FinderPanel;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.programtable.DefaultProgramTableModel;
import tvbrowser.ui.programtable.ProgramTableScrollPane;
import util.exc.TvBrowserException;
import util.settings.*;
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
  private static final String OLD_USER_DIR = "tvbrowser";
  private static final String USER_DIR = mDefaultSettings.getProperty("userdir",".tvbrowser");
  
  private static final Font PROGRAMTITLEFONT=new Font("Dialog",Font.BOLD,12);
  private static final Font PROGRAMINFOFONT=new Font("Dialog",Font.PLAIN,10);
  private static final Font CHANNELNAMEFONT=new Font("Dialog",Font.BOLD,12);
  private static final Font PROGRAMTIMEFONT=new Font("Dialog",Font.BOLD,12);
  

  private static PropertyManager mProp = new PropertyManager();




  /**
   * Returns the user directory. (e.g.: ~/.tvbrowser/)
   */
  public static String getUserDirectoryName() {
    String dir = mDefaultSettings.getProperty("userhome",System.getProperty("user.home", ""));
    String oldDir = dir;
    
    if (dir.length() != 0) {
      dir += File.separator + USER_DIR;
      oldDir += File.separator + OLD_USER_DIR;
    } else {
      dir = USER_DIR;
      oldDir = OLD_USER_DIR;
    }

    // The user directory used to be "tvbrowser". Now it is ".tvbrowser"
    // (hidden on UNIX systems). -> Rename the old directory if it still exists.
    File oldUserDir = new File(oldDir);
    if (oldUserDir.exists()) {
      oldUserDir.renameTo(new File(dir));
    }
    
    return dir;
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
    File settingsFile = new File(getUserDirectoryName(), SETTINGS_FILE);
    try {
      mProp.readFromFile(settingsFile);
    }
    catch (IOException evt) {
      mLog.info("No user settings found. using default user settings");
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
    
    propArr = new Property[] {
      propLookAndFeel, propSkinLFThemepack, propIsSkinLFEnabled
    };
    if (mProp.hasChanged(propArr)) {
      TVBrowser.updateLookAndFeel();
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
     /* propShowUpdateButton, propShowPreferencesButton, */propToolbarButtonStyle,
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
      
      mainFrame.updateChannelChooser();
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
      model.setDate(FinderPanel.getInstance().getSelectedDate(), null, null);
    }
    
    /*
    if (mProp.hasChanged(propHiddenPluginButtons)) {
      mainFrame.getHorizontalToolBar().updateButtons();
    }
    */
    
    
    mProp.clearChanges();
  }
  
  
  public static final VersionProperty propTVBrowserVersion
    = new VersionProperty(mProp, "version", null);
  
  public static final BooleanProperty propUseDefaultFonts
    = new BooleanProperty(mProp, "usedefaultfonts", true);

  public static final BooleanProperty propEnableAntialiasing
  = new BooleanProperty(mProp, "enableantialiasing", false);
  
  public static final BooleanProperty propUseDefaultDirectories
    = new BooleanProperty(mProp, "usedefaultdirectories", true);
  
  public static final StringProperty propTVDataDirectory
    = new StringProperty(mProp, "directory.tvdata", mDefaultSettings.getProperty("tvdatadir","tvdata"));
  
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
                         new String[] { "timeSynchronous", "compact" });

  public static final ChoiceProperty propTableBackgroundStyle
    = new ChoiceProperty(mProp, "tablebackground.style", "oneImage",
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

  /*
  public static final BooleanProperty propShowTimeButtons
    = new BooleanProperty(mProp, "showTimeButtons", true);
    */
  
  /**
   * @deprecated
   */
//  public static final BooleanProperty propShowUpdateButton
//    = new BooleanProperty(mProp, "showUpdateButton", true);
/**
 * @deprecated
 */
//  public static final BooleanProperty propShowPreferencesButton
//    = new BooleanProperty(mProp, "showPreferencesButton", true);

  public static final BooleanProperty propShowAssistant
    = new BooleanProperty(mProp, "showassistant", true);

  public static final StringProperty propUserDefinedWebbrowser
    = new StringProperty(mProp, "webbrowser", null);

  public static final BooleanProperty propOnlyMinimizeWhenWindowClosing
    = new BooleanProperty(mProp, "onlyMinimizeWhenWindowClosing", false);

  public static final ChoiceProperty propToolbarButtonStyle
    = new ChoiceProperty(mProp, "buttontype", "text&icon",
                         new String[] { "text&icon", "text", "icon" });

  public static final BooleanProperty propIsTooolbarVisible
    = new BooleanProperty(mProp, "isToolbarVisible", true);

  public static final BooleanProperty propIsSkinLFEnabled
    = new BooleanProperty(mProp, "skinLF.enabled", false);

  public static final StringProperty propSkinLFThemepack
    = new StringProperty(mProp, "skinLF.themepack", "themepacks/themepack.zip");

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
    = new StringArrayProperty(mProp, "toolbarButtons",
            null  // we show all buttons, if this property is not set
     );

  public static final BooleanProperty propToolbarUseBigIcons
    = new BooleanProperty(mProp, "toolbarUseBigIcons", true);
  
  public static final StringProperty propToolbarLocation
    = new StringProperty(mProp, "toolbarLocation", "north");
  
  public static final StringProperty propDefaultContextMenuPlugin
    = new StringProperty(mProp, "contextmenudefaultplugin", "programinfo.ProgramInfo");

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
                                "programinfo.ProgramInfo",
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

  public static final BooleanProperty propFtpProxyUseProxy
    = new BooleanProperty(mProp, "proxy.ftp.useProxy", false);

  public static final StringProperty propFtpProxyHost
    = new StringProperty(mProp, "proxy.ftp.host", "");

  public static final StringProperty propFtpProxyPort
    = new StringProperty(mProp, "proxy.ftp.port", "");

  public static final BooleanProperty propFtpProxyAuthentifyAtProxy
    = new BooleanProperty(mProp, "proxy.ftp.authentifyAtProxy", false);

  public static final StringProperty propFtpProxyUser
    = new StringProperty(mProp, "proxy.ftp.user", "");

  public static final EncodedStringProperty propFtpProxyPassword
    = new EncodedStringProperty(mProp, "proxy.ftp.password", "", PROXY_PASSWORD_SEED);
	
  public static final StringArrayProperty propDataServicesForUpdate
    = new StringArrayProperty(mProp,"tvdataservices.update",null);
  
  
  public static final BooleanProperty propShowPluginView
    = new BooleanProperty(mProp,"show.pluginview", true);
  
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

}