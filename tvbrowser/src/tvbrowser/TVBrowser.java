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
import java.awt.Image;
import java.awt.Point;
import java.io.*;
import java.util.logging.*;

import javax.swing.*;

import tvbrowser.core.*;
import tvbrowser.ui.filter.FilterComponentList;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.splashscreen.SplashScreen;
import util.exc.ErrorHandler;
import util.ui.ImageUtilities;
import util.ui.UiUtilities;

import com.gc.systray.SystemTrayIconListener;
import com.gc.systray.SystemTrayIconManager;
import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;

import devplugin.Date;

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

  private static String curLookAndFeel;
  public static final devplugin.Version VERSION=new devplugin.Version(0,97,false,"0.9.7.2");
  public static final String MAINWINDOW_TITLE="TV-Browser v"+VERSION.toString();
  
  private static MainFrame mainFrame;

  
  
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

      // Use a even simpler Formatter for console logging
      mainLogger.getHandlers()[0].setFormatter(createFormatter());
      
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
    tvbrowser.core.ChannelList.create();
    
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
    
    // Maybe there are plugins to install (.jar.inst files)
    PluginManager.installPendingPlugins();
    
    mLog.info("Loading plugins...");
    msg = mLocalizer.msg("splash.plugins", "Loading plugins...");
    splash.setMessage(msg);
    PluginManager.getInstance().loadPlugins();
    
    mLog.info("Deleting expired tv data...");
    TvDataBase.getInstance().deleteExpiredFiles(1);
    
    mLog.info("Loading filters...");
    FilterComponentList.init();
  
    mLog.info("Starting up...");
    msg = mLocalizer.msg("splash.ui", "Starting up...");
    splash.setMessage(msg);
    
    mainFrame=MainFrame.getInstance();
    
    // Set the program icon
    Image iconImage = ImageUtilities.createImage("imgs/TVBrowser16.gif");
    mainFrame.setIconImage(iconImage);

    // Initialize the tray icon
    File iconTrayLib=new File("DesktopIndicator.dll");
    boolean useWindowsIconTray=false;
    int systrayImageHandle=-1;
    
    if (iconTrayLib.exists()) {
      useWindowsIconTray=SystemTrayIconManager.initializeSystemDependent();
      if (!useWindowsIconTray) {
        mLog.info("could not load library "+iconTrayLib.getAbsolutePath());
      }
      else {
        systrayImageHandle = SystemTrayIconManager.loadImage("imgs/taskicon.ico");
        if (systrayImageHandle == -1) {
          mLog.info("Could load system tray icon");
          useWindowsIconTray=false;
        }
      }
    }
    
// --->> Windows only
    if (useWindowsIconTray) {
      mLog.info("platform independent mode is OFF");
          
      final SystemTrayIconManager mgr = new SystemTrayIconManager(systrayImageHandle, TVBrowser.MAINWINDOW_TITLE);
      mgr.setVisible(true);
      JPopupMenu trayMenu = new JPopupMenu();
      final JMenuItem openMenuItem = new JMenuItem("Open");
      JMenuItem quitMenuItem = new JMenuItem("Quit");
      trayMenu.add(openMenuItem);
      trayMenu.add(quitMenuItem);  
    
      openMenuItem.setEnabled(false);
    
      openMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          mainFrame.show();          
          mainFrame.toFront();
          mainFrame.setExtendedState(java.awt.Frame.NORMAL);          
        }
      }); 
    
      quitMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          mgr.setVisible(false);
          mainFrame.quit();  
        }
        });      

      mgr.addSystemTrayIconListener(new SystemTrayIconListener() {
        public void mouseClickedLeftButton(Point pos, SystemTrayIconManager source) {
        }
        public void mouseClickedRightButton(Point pos, SystemTrayIconManager ssource) {
        }
        public void mouseLeftDoubleClicked(Point pos, SystemTrayIconManager source) {
          if (!mainFrame.isVisible()) {
            mainFrame.show();
          }
          mainFrame.toFront();
          mainFrame.setExtendedState(java.awt.Frame.NORMAL);          
        }
        public void mouseRightDoubleClicked(Point pos, SystemTrayIconManager source) {
        } 
      });

      mgr.setRightClickView(trayMenu);
    
      mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
          mgr.setVisible(false);
          mainFrame.quit();
        }
        public void windowIconified(java.awt.event.WindowEvent e) {
          mainFrame.hide();
          openMenuItem.setEnabled(true);  
        }
      });
    }
    
    
    else {
      mLog.info("platform independent mode is ON");    
      
      mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
          mainFrame.quit();
        }
      });  
    }
    
    
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
    
    if (Settings.getShowAssistant()) {
      mainFrame.runSetupAssistant();  

    }
    else { 
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        
        if (Settings.getAutomaticDownload()==Settings.ONSTARTUP && ChannelList.getNumberOfSubscribedChannels()>0) {
          mainFrame.runUpdateThread(Settings.getDownloadPeriod());
        }        
     
        boolean dataAvailable = TvDataBase.getInstance().dataAvailable(new Date());
        if ((! dataAvailable) && (ChannelList.getNumberOfSubscribedChannels() > 0)) {
          mainFrame.askForDataUpdate();
        } else {
          mainFrame.scrollToNow();
        }
        
     }
    });
    }
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


  /**
   * Creates a very simple Formatter for log formatting
   * 
   * @return a very simple Formatter for log formatting.
   */
  private static Formatter createFormatter() {
    return new Formatter() {
      public synchronized String format(LogRecord record) {
        StringBuffer sb = new StringBuffer();

        String message = formatMessage(record);
        sb.append(record.getLevel().getLocalizedName());
        sb.append(": ");
        sb.append(message);
        sb.append("\n");
        if (record.getThrown() != null) {
          try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            record.getThrown().printStackTrace(pw);
            pw.close();
            sb.append(sw.toString());
          } catch (Exception ex) {
          }
        }
        return sb.toString();
      }
    };
  }

}