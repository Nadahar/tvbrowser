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

package tvbrowser;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tvbrowser.core.ChannelList;
import tvbrowser.core.DataService;
import tvbrowser.core.PluginManager;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataServiceManager;
import tvbrowser.ui.filter.FilterComponentList;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.splashscreen.SplashScreen;
import util.exc.ErrorHandler;
import util.ui.UiUtilities;

import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class TVBrowser {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(TVBrowser.class.getName());

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(TVBrowser.class);

 // private static final String EXPORTED_TV_DATA_EXTENSION = ".tv.zip";

 // private ProgramTableScrollPane mProgramTableScrollPane;
 // private DefaultProgramTableModel mProgramTableModel;
 // private Thread downloadingThread;
 // private JPanel jcontentPane;
 // private FinderPanel finderPanel;
//  private JMenuItem settingsMenuItem, quitMenuItem, updateMenuItem,
//    mImportTvDataMI, mExportTvDataMI, aboutMenuItem, helpMenuItem, mPluginDownloadMenuItem;
//  private SkinPanel skinPanel;
//  private HorizontalToolBar mDefaultToolBar;
//  private VerticalToolBar mDateTimeToolBar;
//  private StatusBar mStatusBar;
  private static String curLookAndFeel;
  public static final devplugin.Version VERSION=new devplugin.Version(0,96,false,"0.9.6.1");
  public static final String MAINWINDOW_TITLE="TV-Browser v"+VERSION.toString();
  
  private static MainFrame mainFrame;

  private JMenu mPluginsMenu;

  
  
  /**
   * Entry point of the application
   */
  public static void main(String[] args) {
    String msg;
    
    // Check whether the TV-Browser was started in the right directory
    if (! new File("tvbrowser.jar").exists()) {
      msg = mLocalizer.msg("error.2",
        "Please start TV-Browser in the TV-Browser directory!");
      JOptionPane.showMessageDialog(null, msg);
      System.exit(1);
    }
    
    // setup logging
    try {
      // Get the default Logger
      Logger mainLogger = Logger.getLogger("");
      
      // Add a file handler
      new File("log").mkdir();
      Handler fileHandler = new FileHandler("log/tvbrowser.log", 50000, 2, true);
      fileHandler.setLevel(Level.WARNING);
      mainLogger.addHandler(fileHandler);
    }
    catch (IOException exc) {
      msg = mLocalizer.msg("error.4", "Can't create log file.");
      ErrorHandler.handle(msg, exc);
    }
    
    
    
    SplashScreen splash = new SplashScreen("imgs/splash.jpg", 140, 220,
      new Color(63, 114, 133), Color.WHITE);
    UiUtilities.centerAndShow(splash);
    
    
	/*Maybe there are tvdataservices to install (.jar.inst files)*/
	TvDataServiceManager.installPendingDataServices();
    
    mLog.info("Loading tv data service...");
    msg = mLocalizer.msg("splash.dataService", "Loading tv data service...");
    splash.setMessage(msg);
    TvDataServiceManager.getInstance().initDataServices();
    MainFrame.createChannelList();
    
    Settings.loadSettings();
    
	 File f=new File(Settings.getTVDataDirectory());
		if (!f.exists()) {
			mLog.info("Creating tv data directory...");
			if (!f.mkdirs()) {
				mLog.info("Could not create directory + "+f.getAbsolutePath());
			}
		}
    
    
    mLog.info("Loading Look&Feel...");
    msg = mLocalizer.msg("splash.laf", "Loading look and feel...");
    splash.setMessage(msg);
    
    updateLookAndFeel();
    /*
    if (Settings.isSkinLFEnabled()) {
      String themepack=Settings.getSkinLFThemepack();
      try {
        SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePack(themepack));
        SkinLookAndFeel.enable();
      }catch(Exception exc) {
        ErrorHandler.handle("Could not load themepack",exc);
      }
    }
    else {
      try {
        curLookAndFeel = Settings.getLookAndFeel();
        UIManager.setLookAndFeel(curLookAndFeel);
      }
      catch (Exception exc) {
        msg = mLocalizer.msg("error.1", "Unable to set look and feel.");
        ErrorHandler.handle(msg, exc);
      }
    }
    */
    
    
    devplugin.Plugin.setPluginManager(DataService.getInstance());
    
    /*Maybe there are plugins to install (.jar.inst files)*/
	  PluginManager.installPendingPlugins();
    
    mLog.info("Loading plugins...");
    msg = mLocalizer.msg("splash.plugins", "Loading plugins...");
    splash.setMessage(msg);
    PluginManager.initInstalledPlugins();
    
    mLog.info("Deleting expired tv data...");

    DataService.deleteExpiredFiles(Settings.getTVDataLifespan());
    
    
    mLog.info("Loading filters...");
    FilterComponentList.init();
  
    mLog.info("Starting up...");
    msg = mLocalizer.msg("splash.ui", "Starting up...");
    splash.setMessage(msg);
    

    mainFrame=new MainFrame();
    
    // Set the right size
    mainFrame.setSize(Settings.getWindowSize());
    Point location = Settings.getWindowLocation();
    if (location == null) {
      UiUtilities.centerAndShow(mainFrame);
    } else {
      mainFrame.setLocation(location);
      mainFrame.show();
    }
    ErrorHandler.setFrame(mainFrame);
    
    splash.hide();
    
    // maximize the frame
    if (Settings.isWindowMaximized()) {
      mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    // scroll to now
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {      	
        if (Settings.getAutomaticDownload()==Settings.ONSTARTUP) {
          mainFrame.runUpdateThread(Settings.getDownloadPeriod());
        }
        
        if (ChannelList.getNumberOfSubscribedChannels()==0) {
        	JOptionPane.showMessageDialog(mainFrame,"There are no channels selected for download.");
        }
        else
        if (! DataService.dataAvailable(new devplugin.Date())) {
          mainFrame.askForDataUpdate();
        } else {
          mainFrame.scrollToNow();
        }
        
     }
    });
  }

  public static void updateLookAndFeel() {
 
  if (Settings.isSkinLFEnabled()) {
    String themepack=Settings.getSkinLFThemepack();
    try {
      SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePack(themepack));
      SkinLookAndFeel.enable();
    }catch(Exception exc) {
       ErrorHandler.handle("Could not load themepack.\nSkinLF is disabled now",exc);
       Settings.setSkinLFEnabled(false);
    }
  }
  else {
    if (curLookAndFeel == null || !curLookAndFeel.equals(Settings.getLookAndFeel())) {
      try {
        curLookAndFeel = Settings.getLookAndFeel();
        UIManager.setLookAndFeel(curLookAndFeel);
      }catch (Exception exc) {
        String msg = mLocalizer.msg("error.1", "Unable to set look and feel.", exc);
        ErrorHandler.handle(msg, exc);
      }
    }
  }
  
  if (mainFrame!=null) {
    SwingUtilities.updateComponentTreeUI(mainFrame);
    mainFrame.validate();
  }
  
  }

}