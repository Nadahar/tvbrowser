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
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import tvbrowser.core.ChannelList;
import tvbrowser.core.PluginLoader;
import tvbrowser.core.PluginManager;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.TvDataServiceManager;
import tvbrowser.ui.configassistant.TvdataAssistantDlg;
import tvbrowser.ui.configassistant.TvdataImportDlg;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.UpdateDlg;
import tvbrowser.ui.splashscreen.DummySplash;
import tvbrowser.ui.splashscreen.Splash;
import tvbrowser.ui.splashscreen.SplashScreen;
import util.exc.ErrorHandler;
import util.exc.MonitoringErrorStream;
import util.exc.TvBrowserException;
import util.ui.ImageUtilities;
import util.ui.NotBoldMetalTheme;
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
  public static util.ui.Localizer mLocalizer;

  private static String curLookAndFeel;
  public static final devplugin.Version VERSION=new devplugin.Version(1,0,false,"1.0 RC05");
  public static final String MAINWINDOW_TITLE="TV-Browser v"+VERSION.toString();
  
  private static boolean mUseSystemTray;
  
  private static MainFrame mainFrame;

  /** State of the Window (max/normal) */
  private static int mState;
  
  /**
   * Specifies whether the save thread should stop. The save thread saves every
   * 5 minutes the settings.
   */
  private static boolean mSaveThreadShouldStop;


  private static void showUsage() {
    
    System.out.println("command line options:");
    System.out.println("    - minimized    The main window will be minimized after start up");
    System.out.println("    - nosplash     No splash screen during start up");
    System.out.println();
    
  }
  
  /**
   * Entry point of the application
   */
  public static void main(String[] args) {
    
    showUsage();

    // Read the command line parameters
    boolean startMinimized = false;
    boolean showSplashScreen = true;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equalsIgnoreCase("-minimized")) {
        startMinimized = true;
      } else if (args[i].equalsIgnoreCase("-nosplash")) {
        showSplashScreen = false;
      } else if (args[i].startsWith("-D")) {
          if (args[i].indexOf("=") > 0) {
              String key = args[i].substring(2, args[i].indexOf("="));
              String value = args[i].substring(args[i].indexOf("=")+1);
              if (key.equals("user.language")) {
                  Locale.setDefault(new Locale(value));
              } else {
                  System.setProperty(key, value);
              }
          } else {
              mLog.warning("Wrong Syntax in parameter: '" + args[i] + "'");
          }
      } else {
        mLog.warning("Unknown command line parameter: '" + args[i] + "'");
      }
    }

    mLocalizer = util.ui.Localizer.getLocalizerFor(TVBrowser.class);
    String msg;
    
    // Check whether the TV-Browser was started in the right directory
    if (! new File("tvbrowser.jar").exists() && ! new File("tvbrowser.exe").exists()) {
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
    
    // Capture unhandled exceptions
    System.setErr(new PrintStream(new MonitoringErrorStream()));
    
    
    // Load the settings
    Settings.loadSettings();

    // Set the proxy settings
    updateProxySettings();

    // Set the String to use for indicating the user agent in http requests
    System.setProperty("http.agent", MAINWINDOW_TITLE); 
    
    // Check whether TV-Browser is started the first time
    File f=new File(Settings.propTVDataDirectory.getString());
    if (!f.exists()) {        
      
      devplugin.Version prevVersion = Settings.propTVBrowserVersion.getVersion();  
      
      /* if we have got no tvdata and no assistant will be loaded there must exist an older
       * tv-browser. so ask the user for importing existing tv data. 
       */
      if (! Settings.propShowAssistant.getBoolean()) {
      
        /* update from 0.9.7, 0.9.7.1 to 0.9.7.2 */
        if (prevVersion==null) {    
          boolean showTvdataAssistant=true;
          while (showTvdataAssistant) {
            showTvdataAssistant=false;          
            TvdataAssistantDlg dlg=new TvdataAssistantDlg();
            UiUtilities.centerAndShow(dlg);
            int result=dlg.getSelection();
            if (result==TvdataAssistantDlg.IMPORT_DATA) {
              msg = mLocalizer.msg("importtvdata.step1","step 1: ..");
              String toDir = Settings.propTVDataDirectory.getString();
              TvdataImportDlg importDlg = new TvdataImportDlg(msg, "tvdata", toDir);
              UiUtilities.centerAndShow(importDlg);
              if (importDlg.getResult()==TvdataImportDlg.OK) {
                msg = mLocalizer.msg("importtvdata.step1","step 2: ..");
                toDir = Settings.propTVDataDirectory.getString()
                        + "/tvbrowserdataservice.TvBrowserDataService";
                importDlg = new TvdataImportDlg(msg, "tvbrowsertvdata", toDir);
                UiUtilities.centerAndShow(importDlg);
              }
              showTvdataAssistant=importDlg.getResult()!=TvdataImportDlg.OK;
                          
            } else if (result==TvdataAssistantDlg.RUN_ASSISTANT) {
              Settings.propShowAssistant.setBoolean(true);
            }
          }
        }
        else { /* update from 0.9.7.2 to 0.9.7.3 and higher */
          TvdataAssistantDlg dlg=new TvdataAssistantDlg();
          UiUtilities.centerAndShow(dlg);
          int result=dlg.getSelection();
          if (result==TvdataAssistantDlg.IMPORT_DATA) {
            msg = mLocalizer.msg("importtvdata","import tv data");
            String toDir = Settings.propTVDataDirectory.getString();
            TvdataImportDlg importDlg=new TvdataImportDlg(msg, "tvdata", toDir);
            UiUtilities.centerAndShow(importDlg);  
          }
          Settings.propShowAssistant.setBoolean(result == TvdataAssistantDlg.RUN_ASSISTANT);         
        }
      }
        
      mLog.info("Creating tv data directory...");
      
      if (!f.mkdirs()) {
        mLog.info("Could not create directory + "+f.getAbsolutePath());
      }
      
    }
    
    Splash splash;
    
    if (showSplashScreen) {
      splash = new SplashScreen("imgs/splash.jpg", 140, 220,
         new Color(63, 114, 133), Color.WHITE);      
    }
    else {
      splash = new DummySplash(); 
    }    
    
    splash.showSplash();
    Settings.propTVBrowserVersion.setVersion(VERSION);  
    
    /*Maybe there are tvdataservices to install (.jar.inst files)*/
    TvDataServiceManager.installPendingDataServices();
    
    mLog.info("Loading tv data service...");
    msg = mLocalizer.msg("splash.dataService", "Loading tv data service...");
    splash.setMessage(msg);
    TvDataServiceManager.getInstance().initDataServices();
    ChannelList.create();

    ChannelList.initSubscribedChannels();
    
    mLog.info("Loading Look&Feel...");
    msg = mLocalizer.msg("splash.laf", "Loading look and feel...");
    splash.setMessage(msg);

    // Set the NotBoldMetalTheme for the metal look and feel
    // (This won't effect other look and feels)
    javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new NotBoldMetalTheme());
    
    updateLookAndFeel();
    
    // Maybe there are plugins to install (.jar.inst files)
    PluginManager.getInstance().installPendingPlugins();
    
    mLog.info("Loading plugins...");
    msg = mLocalizer.msg("splash.plugins", "Loading plugins...");
    splash.setMessage(msg);
    try {
      PluginManager.getInstance().init();      
    } catch(TvBrowserException exc) {
      ErrorHandler.handle(exc);      
    }

    msg = mLocalizer.msg("splash.tvData", "Checking TV data base...");
    splash.setMessage(msg);

    mLog.info("Deleting expired tv data...");
    TvDataBase.getInstance().deleteExpiredFiles(1);

    mLog.info("Checking tv data inventory...");
    TvDataBase.getInstance().checkTvDataInventory();
    
    mLog.info("Starting up...");
    msg = mLocalizer.msg("splash.ui", "Starting up...");
    splash.setMessage(msg);
    
    mainFrame=MainFrame.getInstance();
    
    // Set the program icon
    Image iconImage = ImageUtilities.createImage("imgs/TVBrowser16.gif");
    mainFrame.setIconImage(iconImage);

    // Initialize the tray icon
    File iconTrayLib=new File("DesktopIndicator.dll");
    mUseSystemTray = false;
    int systrayImageHandle=-1;
    
    if (iconTrayLib.exists()) {
      mUseSystemTray = SystemTrayIconManager.initializeSystemDependent();
      if (! mUseSystemTray) {
        mLog.info("could not load library "+iconTrayLib.getAbsolutePath());
      }
      else {
        systrayImageHandle = SystemTrayIconManager.loadImage("imgs/TVBrowser.ico");
        if (systrayImageHandle == -1) {
          mLog.info("Could not load system tray icon");
          mUseSystemTray = false;
        }
      }
    }
    
// --->> Windows only
    if (mUseSystemTray) {
      mLog.info("platform independent mode is OFF");
          
      final SystemTrayIconManager mgr = new SystemTrayIconManager(systrayImageHandle, TVBrowser.MAINWINDOW_TITLE);
      mgr.setVisible(true);
      JPopupMenu trayMenu = new JPopupMenu();
      final JMenuItem openMenuItem = new JMenuItem(mLocalizer.msg("menu.open", "Open"));
      JMenuItem quitMenuItem = new JMenuItem(mLocalizer.msg("menu.quit", "Quit"));
      trayMenu.add(openMenuItem);
      trayMenu.addSeparator();
      trayMenu.add(createPluginsMenu());
      trayMenu.addSeparator();
      trayMenu.add(quitMenuItem);  
      	
      
      openMenuItem.setEnabled(false);
    
      openMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          mainFrame.show();          
          mainFrame.toFront();
          mainFrame.setExtendedState(mState);                          
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
          mainFrame.setExtendedState(mState);          
        }
        public void mouseRightDoubleClicked(Point pos, SystemTrayIconManager source) {
        } 
      });

      mgr.setRightClickView(trayMenu);

      mainFrame.addComponentListener(new ComponentListener() {

        public void componentResized(ComponentEvent e) {
            int state = mainFrame.getExtendedState();
            if ((state & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                mState = JFrame.MAXIMIZED_BOTH;
            } else {
                mState = JFrame.NORMAL;
            }
        }

        public void componentHidden(ComponentEvent e) {}
        public void componentMoved(ComponentEvent e) {}
        public void componentShown(ComponentEvent e) {}
    });       
      
      mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent evt) {
          if (Settings.propOnlyMinimizeWhenWindowClosing.getBoolean()) {
            // Only minimize the main window, don't quit
            mainFrame.hide();
            openMenuItem.setEnabled(true); 
          } else {
            mgr.setVisible(false);
            mainFrame.quit();
          }
        }
        public void windowIconified(java.awt.event.WindowEvent evt) {
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
    mLog.info("Setting frame size and location");
    int windowWidth = Settings.propWindowWidth.getInt();
    int windowHeight = Settings.propWindowHeight.getInt();
    mainFrame.setSize(windowWidth, windowHeight);
    
    int windowX = Settings.propWindowX.getInt();
    int windowY = Settings.propWindowY.getInt();
    if (windowX == -1) {
      UiUtilities.centerAndShow(mainFrame);
    } else {
      mainFrame.setLocation(windowX, windowY);
      mainFrame.show();
    }
    ErrorHandler.setFrame(mainFrame);
    
    splash.hideSplash();
    
    // maximize the frame if wanted
    if (Settings.propIsWindowMaximized.getBoolean()) {
      mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
    }

    // minimize the frame if wanted
    if (startMinimized) {
      mainFrame.setExtendedState(Frame.ICONIFIED);
    }
    
    if (Settings.propShowAssistant.getBoolean()) {
      mLog.info("Running setup assistant");    
      mainFrame.runSetupAssistant();  
    }
    else {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          handleAutomaticDownload();
       
          boolean dataAvailable = TvDataBase.getInstance().dataAvailable(new Date());
          if ((! dataAvailable) && (ChannelList.getNumberOfSubscribedChannels() > 0)) {
            mainFrame.askForDataUpdate();
          } else {
            mainFrame.scrollToNow();
          }
        }
      });
    }
    
    
    // Every 5 minutes we store all the settings so they are stored in case of
    // an unexpected failure
    Thread saveThread = new Thread() {
      public void run() {
        mSaveThreadShouldStop = false;
        while (! mSaveThreadShouldStop) {
          try {
            Thread.sleep(5 * 60 * 1000);
          }
          catch (Exception exc) {}
          
          flushSettings();
        }
      }
    };
    saveThread.setPriority(Thread.MIN_PRIORITY);
    saveThread.start();
  }
  
  
  public static synchronized void flushSettings() {
    final PluginLoader pl = PluginLoader.getInstance();
    devplugin.Plugin[] p = PluginLoader.getInstance().getActivePlugins();
    for (int i=0; i<p.length; i++) {
      mLog.info("Storing plugin settings of plugin "+p[i].getInfo().getName()+"...");
      pl.storePluginData(p[i]);
      pl.storePluginSettings(p[i]);
    }
        
    mLog.info("Storing channel day light saving time corrections");
    ChannelList.storeDayLightSavingTimeCorrections();  
    
    
    mLog.info("Storing window size and location");
    boolean maximized = mainFrame.getExtendedState() == Frame.MAXIMIZED_BOTH;
    Settings.propIsWindowMaximized.setBoolean(maximized);
    if (! maximized) {
      // Save the window size and location only when not maximized
      Settings.propWindowWidth.setInt(mainFrame.getWidth());
      Settings.propWindowHeight.setInt(mainFrame.getHeight());
      Settings.propWindowX.setInt(mainFrame.getX());
      Settings.propWindowY.setInt(mainFrame.getY());
    }
    
    mLog.info("Storing settings");
    try {
      Settings.storeSettings();
    } catch (TvBrowserException e) {
      ErrorHandler.handle(e);
    }
    
  }
  
  private static JMenu createPluginsMenu() {
      JMenu mPluginsMenu = new JMenu(mLocalizer.msg("menu.plugins", "Plugins"));
      
      Object[] plugins = PluginLoader.getInstance().getActivePlugins();
      JMenuItem item;
      HashMap map = new HashMap();
      for (int i = 0;i<plugins.length;i++) {
        
        final devplugin.Plugin plugin = (devplugin.Plugin)plugins[i];
        plugin.setParent(TVBrowser.mainFrame);
        String btnTxt = plugin.getButtonText();
        if (btnTxt != null) {
          int k = 1;
          String txt = btnTxt;
          while (map.get(txt) != null) {
            txt = btnTxt+"("+k+")";
            k++;
          }
          map.put(txt,btnTxt);

          item = new JMenuItem(btnTxt);
          item.setIcon(plugin.getButtonIcon());
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
              plugin.execute();
            }
          });
          mPluginsMenu.add(item);
        }
      }
      
      return mPluginsMenu;
    }
  
  public static boolean isUsingSystemTray() {
    return mUseSystemTray;
  }


  private static void handleAutomaticDownload() {
    String autoDLType = Settings.propAutoDownloadType.getString();

    if ((ChannelList.getNumberOfSubscribedChannels() == 0)
      || autoDLType.equals("never"))
    {
      // Nothing to do
      return;
    }              
    
    devplugin.Date lastDownloadDate=Settings.propLastDownloadDate.getDate();
    if (lastDownloadDate==null) {
      lastDownloadDate=devplugin.Date.getCurrentDate().addDays(-100);
    }
    devplugin.Date today=devplugin.Date.getCurrentDate();
    
    //int daysSinceLastDownload=today.getNumberOfDaysSince(lastDownload);
    Date nextDownloadDate=null;
    
    if (autoDLType.equals("daily")) {
      nextDownloadDate=lastDownloadDate.addDays(1);
    }
    else if (autoDLType.equals("every3days")) {
      nextDownloadDate=lastDownloadDate.addDays(3);
    }
    else if (autoDLType.equals("WEEKLY")) {
      nextDownloadDate=lastDownloadDate.addDays(7);
    }
    else { // "daily"
      nextDownloadDate=lastDownloadDate;
    }

    if (nextDownloadDate.getNumberOfDaysSince(today)<=0) {	
      if (Settings.propAskForAutoDownload.getBoolean()) {
	       UpdateDlg dlg = new UpdateDlg(mainFrame, true);
        dlg.pack();
        UiUtilities.centerAndShow(dlg);
        int daysToDownload = dlg.getResult();
        if (daysToDownload != UpdateDlg.CANCEL) {
          mainFrame.runUpdateThread(daysToDownload, TvDataServiceManager.getInstance().getTvDataServices(Settings.propDataServicesForUpdate.getStringArray()));
        }
      }
      else {
	       mainFrame.runUpdateThread(Settings.propAutoDownloadPeriod.getInt(), TvDataServiceManager.getInstance().getTvDataServices(Settings.propDataServicesForUpdate.getStringArray()));
      }
    }
  }


  public static void updateLookAndFeel() {
    if (Settings.propIsSkinLFEnabled.getBoolean()) {
      String themepack = Settings.propSkinLFThemepack.getString();
      try {
        SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePack(themepack));
        SkinLookAndFeel.enable();
      } catch (Exception exc) {
        ErrorHandler.handle(
          "Could not load themepack.\nSkinLF is disabled now",
          exc);
        Settings.propIsSkinLFEnabled.setBoolean(false);
      }
    } else {
      if (curLookAndFeel == null
        || !curLookAndFeel.equals(Settings.propLookAndFeel.getString()))
      {
        try {
          curLookAndFeel = Settings.propLookAndFeel.getString();
          UIManager.setLookAndFeel(curLookAndFeel);
        } catch (Exception exc) {
          String msg =
            mLocalizer.msg("error.1", "Unable to set look and feel.", exc);
          ErrorHandler.handle(msg, exc);
        }
      }
    }

    if (mainFrame != null) {
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

  
  /**
   * Called when TV-Browser shuts down.
   * <p>
   * Stops the save thread and saves the settings.
   */
  public static void shutdown() {
    mSaveThreadShouldStop = true;
    flushSettings();
  }
  

  public static void updateProxySettings() {
    String httpHost = "", httpPort = "", httpUser = "", httpPassword = "";
    String ftpHost = "",  ftpPort = "",  ftpUser = "",  ftpPassword = "";
    
    if (Settings.propHttpProxyUseProxy.getBoolean()) {
      httpHost = Settings.propHttpProxyHost.getString();
      httpPort = Settings.propHttpProxyPort.getString();
      
      if (Settings.propHttpProxyAuthentifyAtProxy.getBoolean()) {
        httpUser     = Settings.propHttpProxyUser.getString();
        httpPassword = Settings.propHttpProxyPassword.getString();
        if (httpPassword == null) {
          httpPassword="";
				}

				final String user=httpUser;
				final String pw=httpPassword;
				Authenticator.setDefault(
					new Authenticator() {
					  public PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication(user, pw.toCharArray());
					  }
				  }
				);
      }
    }
    
    if (Settings.propFtpProxyUseProxy.getBoolean()) {
      ftpHost = Settings.propFtpProxyHost.getString();
      ftpPort = Settings.propFtpProxyPort.getString();
      
      if (Settings.propFtpProxyAuthentifyAtProxy.getBoolean()) {
        ftpUser     = Settings.propFtpProxyUser.getString();
        ftpPassword = Settings.propFtpProxyPassword.getString();
      }
    }

    System.setProperty("http.proxyHost",     httpHost);
    System.setProperty("http.proxyPort",     httpPort);
    System.setProperty("http.proxyUser",     httpUser);
    System.setProperty("http.proxyPassword", httpPassword);
    System.setProperty("ftp.proxyHost",      ftpHost);
    System.setProperty("ftp.proxyPort",      ftpPort);
    System.setProperty("ftp.proxyUser",      ftpUser);
    System.setProperty("ftp.proxyPassword",  ftpPassword);
  }

}