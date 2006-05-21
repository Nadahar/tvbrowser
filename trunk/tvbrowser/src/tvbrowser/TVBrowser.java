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

import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tvbrowser.core.ChannelList;
import tvbrowser.core.PluginLoader;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.ui.SystemTray;
import tvbrowser.ui.configassistant.TvBrowserUpdateAssistant;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.UpdateDlg;
import tvbrowser.ui.splashscreen.DummySplash;
import tvbrowser.ui.splashscreen.Splash;
import tvbrowser.ui.splashscreen.SplashScreen;
import tvdataservice.MarkedProgramsList;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.misc.JavaVersion;
import util.misc.OperatingSystem;
import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.NotBoldMetalTheme;
import util.ui.UiUtilities;
import util.ui.textcomponentpopup.TextComponentPopupEventQueue;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;

import devplugin.Date;
import devplugin.Version;

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

  /* If you want to change the version string, add it to the beginning of this array.
     We need the old version strings to import the settings.
  */
  public static final String[] ALL_VERSIONS = new String[]{
          "2.2beta2",
          "2.2beta1",
          "2.2 (SVN)"
  };

  public static final devplugin.Version VERSION=new devplugin.Version(2,20,true,ALL_VERSIONS[0]);


  public static final String MAINWINDOW_TITLE="TV-Browser "+VERSION.toString();

  private static SystemTray mTray;

  private static MainFrame mainFrame;

  private static RandomAccessFile mLockFile;

  private static FileLock mLock;

  private static WindowAdapter mMainWindowAdapter;



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
    boolean showSplashScreen = true;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equalsIgnoreCase("-minimized")) {
        Settings.propMinimizeAfterStartup.setBoolean(true);
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
    if ( !new File("imgs").exists()) {
      msg = mLocalizer.msg("error.2",
        "Please start TV-Browser in the TV-Browser directory!");
      JOptionPane.showMessageDialog(null, msg);
      System.exit(1);
    }

    // setup logging

    // Get the default Logger
    Logger mainLogger = Logger.getLogger("");

    // Use a even simpler Formatter for console logging
    mainLogger.getHandlers()[0].setFormatter(createFormatter());

    // Load the settings
    Settings.loadSettings();

    boolean lookAndFeelInitialized = false;

    if (!createLockFile()) {
      javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new NotBoldMetalTheme());
      updateLookAndFeel();
      lookAndFeelInitialized = true;
      showTVBrowserIsAlreadyRunningMessageBox();
    }

    String logDirectory = Settings.propLogdirectory.getString();
    if (logDirectory != null) {
      try {
        File logDir = new File(logDirectory);
        logDir.mkdirs();
        mainLogger.addHandler(new FileLoggingHandler(logDir.getAbsolutePath()+"/tvbrowser.log", createFormatter()));
      } catch (IOException exc) {
        msg = mLocalizer.msg("error.4", "Can't create log file.");
        ErrorHandler.handle(msg, exc);
      }
    }

    // Capture unhandled exceptions
    //System.setErr(new PrintStream(new MonitoringErrorStream()));

    Locale.setDefault(new Locale(Settings.propLanguage.getString()));

    String timezone = Settings.propTimezone.getString();
    if (timezone != null) {
      TimeZone.setDefault(TimeZone.getTimeZone(timezone));
    }
    mLog.info("Using timezone "+TimeZone.getDefault().getDisplayName());

    // refresh the localizer because we know the language now
    Localizer.emptyLocalizerCache();
    mLocalizer = Localizer.getLocalizerFor(TVBrowser.class);

    // Set the proxy settings
    updateProxySettings();

    // Set the String to use for indicating the user agent in http requests
    System.setProperty("http.agent", MAINWINDOW_TITLE);

    Version currentVersion = Settings.propTVBrowserVersion.getVersion();
    Settings.propTVBrowserVersion.setVersion(VERSION);
    if (currentVersion != null && currentVersion.compareTo(new Version(1,11))<0) {
      mLog.info("Running tvbrowser update assistant");
      javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new NotBoldMetalTheme());
      updateLookAndFeel();
      lookAndFeelInitialized = true;
      showUpdateAssistant();
    }

    final Splash splash;

    if (showSplashScreen && Settings.propSplashShow.getBoolean()) {
      splash = new SplashScreen(
          Settings.propSplashImage.getString(),
          Settings.propSplashTextPosX.getInt(),
          Settings.propSplashTextPosY.getInt(),
          Settings.propSplashBackgroundColor.getColor(),
          Settings.propSplashForegroundColor.getColor());
    }
    else {
      splash = new DummySplash();
    }

    splash.showSplash();

    /* Initialize the MarkedProgramsList to be the first
     * that reacts on a tv data update.
     */
    MarkedProgramsList.getInstance();
    
    /*Maybe there are tvdataservices to install (.jar.inst files)*/
    PluginLoader.getInstance().installPendingPlugins();

    PluginLoader.getInstance().loadAllPlugins();

    mLog.info("Loading TV listings service...");
    msg = mLocalizer.msg("splash.dataService", "Loading TV listings service...");
    splash.setMessage(msg);
    TvDataServiceProxyManager.getInstance().init();
    ChannelList.create();

    ChannelList.initSubscribedChannels();

    if (!lookAndFeelInitialized) {
      mLog.info("Loading Look&Feel...");
      msg = mLocalizer.msg("splash.laf", "Loading look and feel...");
      splash.setMessage(msg);

      // Set the NotBoldMetalTheme for the metal look and feel if Java < 1.5 is used
      // (This won't effect other look and feels)
      if (JavaVersion.getVersion() < JavaVersion.VERSION_1_5) {
        javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new NotBoldMetalTheme());
      }
      updateLookAndFeel();
    }

    mLog.info("Deleting expired TV listings...");
    TvDataBase.getInstance().deleteExpiredFiles(1);
    
    mLog.info("Loading plugins...");
    msg = mLocalizer.msg("splash.plugins", "Loading plugins...");
    splash.setMessage(msg);
    try {
      PluginProxyManager.getInstance().init();
    } catch(TvBrowserException exc) {
      ErrorHandler.handle(exc);
    }

    msg = mLocalizer.msg("splash.tvData", "Checking TV database...");
    splash.setMessage(msg);

    mLog.info("Checking TV listings inventory...");
    TvDataBase.getInstance().checkTvDataInventory();

    mLog.info("Starting up...");
    msg = mLocalizer.msg("splash.ui", "Starting up...");
    splash.setMessage(msg);

    Toolkit.getDefaultToolkit().getSystemEventQueue().push(new TextComponentPopupEventQueue());

    // Init the UI
    final boolean fStartMinimized = Settings.propMinimizeAfterStartup.getBoolean();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        initUi(splash, fStartMinimized);
        SwingUtilities.invokeLater(new Runnable(){
          public void run() {
            mainFrame.updateUI();
            PluginProxyManager.getInstance().fireTvBrowserStartFinished();
            ReminderPlugin.getInstance().handleTvBrowserStartFinished();
          }
        });
      }
    });

    // Every 5 minutes we store all the settings so they are stored in case of
    // an unexpected failure
    Thread saveThread = new Thread() {
      public void run() {
        mSaveThreadShouldStop = false;
        while (! mSaveThreadShouldStop) {
          try {
            Thread.sleep(5 * 60 * 1000);
          }
          catch (Exception exc) {
            // ignore
          }

          if(!mSaveThreadShouldStop)
            flushSettings(true);
        }
      }
    };
    saveThread.setPriority(Thread.MIN_PRIORITY);
    saveThread.start();

     // register the shutdown hook
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        deleteLockFile();
        MainFrame.getInstance().quit(false);
      }
     });
  }


  /**
   * Create the .lock file in the user home directory
   * @return false, if the .lock file exist and is locked or cannot be locked.
   */
  private static boolean createLockFile() {
    String dir = Settings.getUserDirectoryName();
    File lockFile = new File(dir, ".lock");

    if(lockFile.exists()) {
      try {
        mLockFile = new RandomAccessFile(lockFile.toString(),"rw");
        mLock = mLockFile.getChannel().tryLock();

        if(mLock == null)
          return false;
      }catch(Exception e) {
        return false;
      }
    }
    else {
      try {
        lockFile.createNewFile();
        mLockFile = new RandomAccessFile(lockFile.toString(),"rw");
        mLock = mLockFile.getChannel().tryLock();
      }catch(Exception e){
        if(e instanceof IOException)
          mLog.log(Level.WARNING, e.getLocalizedMessage(), e);
      }
    }

    return true;
  }

  private static void deleteLockFile() {
    String dir = Settings.getUserDirectoryName();
    File lockFile = new File(dir, ".lock");

    try {
      mLock.release();
    }catch(Exception e) {
      // ignore
    }

    try {
      mLockFile.close();
    }catch(Exception e) {
      // ignore
    }

    lockFile.delete();
  }


  private static void showUpdateAssistant() {
    TvBrowserUpdateAssistant dlg = new TvBrowserUpdateAssistant(null);
    UiUtilities.centerAndShow(dlg);
    if (dlg.getResult() == TvBrowserUpdateAssistant.CONFIGURE_TVBROWSER) {
      Settings.propShowAssistant.setBoolean(true);
    }
    else if (dlg.getResult() == TvBrowserUpdateAssistant.CANCEL) {
      System.exit(2);
    }
  }


  private static void showTVBrowserIsAlreadyRunningMessageBox() {

    Object[] options = {mLocalizer.msg("close","Close"),
                    mLocalizer.msg("startAnyway","start anyway")};
    if (JOptionPane.showOptionDialog(null,
    mLocalizer.msg("alreadyRunning","TV-Browser is already running"),
    mLocalizer.msg("alreadyRunning","TV-Browser is already running"),
    JOptionPane.DEFAULT_OPTION,
    JOptionPane.WARNING_MESSAGE,
    null, options, options[0])==0) {
      System.exit(-1);
    }

  }

  private static void initUi(Splash splash, boolean startMinimized) {
    mainFrame=MainFrame.getInstance();
    PluginProxyManager.getInstance().setParentFrame(mainFrame);

    // Set the program icon
    Image iconImage = ImageUtilities.createImage("imgs/tvbrowser16.png");
    mainFrame.setIconImage(iconImage);

    mTray = new SystemTray();

    if (mTray.initSystemTray()) {
        mTray.createMenus();
    } else {
      mLog.info("platform independent mode is ON");
      addTrayWindowListener();
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
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (Settings.propIsWindowMaximized.getBoolean()) {
          mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
          SwingUtilities.invokeLater(new Runnable(){
            public void run() {
              mainFrame.updateUI();
            }
          });
        }
      }
    });

    // minimize the frame if wanted
    if (startMinimized) {
      mainFrame.setExtendedState(Frame.ICONIFIED);
    }

    if (Settings.propShowAssistant.getBoolean()) {
      mLog.info("Running setup assistant");
      mainFrame.runSetupAssistant();
    }
    else {
      boolean automaticDownloadStarted = handleAutomaticDownload();

      boolean dataAvailable = TvDataBase.getInstance().dataAvailable(new Date());
      if (!automaticDownloadStarted && (! dataAvailable) && (ChannelList.getNumberOfSubscribedChannels() > 0)) {
        mainFrame.askForDataUpdate();
      } else {
        mainFrame.scrollToNow();
      }
    }
  }


  public static synchronized void flushSettings(boolean log) {
    if(log)
      mLog.info("Channel Settings (day light saving time corrections/icons)");
    ChannelList.storeAllSettings();

    mainFrame.storeSettings();

    if(log) {
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
    }

    if(log)
      mLog.info("Storing settings");
    try {
      Settings.storeSettings();
    } catch (TvBrowserException e) {
      ErrorHandler.handle(e);
    }
  }

  private static void addTrayWindowListener() {
    if(mMainWindowAdapter == null) {
      mMainWindowAdapter = new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
          mainFrame.quit();
        }
      };
    }
    mainFrame.addWindowListener(mMainWindowAdapter);
  }
  
  public static boolean isUsingSystemTray() {
    return mTray.isTrayUsed();
  }
  
  /**
   * Loads the tray icon.
   */
  public static void loadTray() {
    if(!mTray.isTrayUsed())
      mTray.initSystemTray();
    if(mTray.isTrayUsed()) {
      mTray.createMenus();
      if(mMainWindowAdapter != null)
        mainFrame.removeWindowListener(mMainWindowAdapter);
    }
  }
  
  /**
   * Remove the tray icon.
   */
  public static void removeTray() {
    if(mTray.isTrayUsed()) {
      mTray.setVisible(false);
      addTrayWindowListener();
      
      if(!MainFrame.getInstance().isVisible()) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            MainFrame.getInstance().showFromTray(MainFrame.ICONIFIED);
          }
        });
      }
    }
  }

  /**
   * Starts an automatic download if required
   * @return false, if no download got started
   */
  private static boolean handleAutomaticDownload() {
    String autoDLType = Settings.propAutoDownloadType.getString();

    if ((ChannelList.getNumberOfSubscribedChannels() == 0)
      || autoDLType.equals("never"))
    {
      // Nothing to do
      return false;
    }

    devplugin.Date lastDownloadDate=Settings.propLastDownloadDate.getDate();
    if (lastDownloadDate==null) {
      lastDownloadDate=devplugin.Date.getCurrentDate().addDays(-100);
    }
    devplugin.Date today=devplugin.Date.getCurrentDate();

    Date nextDownloadDate;

    if (autoDLType.equals("daily")) {
      nextDownloadDate=lastDownloadDate.addDays(1);
    }
    else if (autoDLType.equals("every3days")) {
      nextDownloadDate=lastDownloadDate.addDays(3);
    }
    else if (autoDLType.equals("weekly")) {
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
          mainFrame.runUpdateThread(daysToDownload, dlg.getSelectedTvDataServices());
        }
      }
      else {
        String[] dataServiceIDs = Settings.propDataServicesForUpdate.getStringArray();
        TvDataServiceProxy[] proxies;
        if (dataServiceIDs == null) {
          proxies = TvDataServiceProxyManager.getInstance().getDataServices();
        }
        else {
          proxies = TvDataServiceProxyManager.getInstance().getTvDataServices(dataServiceIDs);
        }
        mainFrame.runUpdateThread(Settings.propAutoDownloadPeriod.getInt(), proxies);
      }
      return true;
    }
    else {
      return false;
    }
  }

  public static void updateLookAndFeel() {
    if (OperatingSystem.isWindows()) {
      UIManager.installLookAndFeel("Extended Windows Look And Feel",  "com.jgoodies.looks.windows.WindowsLookAndFeel");
    }
    UIManager.installLookAndFeel("Plastic Look And Feel",           "com.jgoodies.looks.plastic.PlasticLookAndFeel");
    UIManager.installLookAndFeel("Plastic 3D Look And Feel",        "com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
    UIManager.installLookAndFeel("Plastic XP Look And Feel",        "com.jgoodies.looks.plastic.PlasticXPLookAndFeel");    
    UIManager.installLookAndFeel("Skin Look And Feel",              "com.l2fprod.gui.plaf.skin.SkinLookAndFeel");    

    if (Settings.propIsSkinLFEnabled.getBoolean() || Settings.propLookAndFeel.getString().equals("com.l2fprod.gui.plaf.skin.SkinLookAndFeel")) {
      Settings.propLookAndFeel.setString("com.l2fprod.gui.plaf.skin.SkinLookAndFeel");
      String themepack = Settings.propSkinLFThemepack.getString();
      try {
        SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePack(themepack));
      } catch (Exception exc) {
        ErrorHandler.handle(
          "Could not load themepack.\nSkinLF is disabled now",
          exc);
        Settings.propLookAndFeel.setString(Settings.propLookAndFeel.getDefault());
      }
    } else if (Settings.propLookAndFeel.getString().startsWith("com.jgoodies")) {
      Options.setPopupDropShadowEnabled(Settings.propJGoodiesShadow.getBoolean());
      UIManager.put("jgoodies.popupDropShadowEnabled", Boolean
          .valueOf(Settings.propJGoodiesShadow.getBoolean()));
      try {
        LookUtils.setLookAndTheme((LookAndFeel) Class.forName(Settings.propLookAndFeel.getString()).newInstance(), Class.forName(Settings.propJGoodiesTheme.getString()).newInstance());
      } catch (Exception e) {
        ErrorHandler.handle("Could not load themepack.\nJGoodies is disabled now", e);
        Settings.propLookAndFeel.setString(Settings.propLookAndFeel.getDefault());
      }
    }
    
    if (curLookAndFeel == null || !curLookAndFeel.equals(Settings.propLookAndFeel.getString())) {
      try {
        curLookAndFeel = Settings.propLookAndFeel.getString();
        UIManager.setLookAndFeel(curLookAndFeel);
        mLog.info("setting look and feel to "+curLookAndFeel);
      } catch (Exception exc) {
        String msg =
          mLocalizer.msg("error.1", "Unable to set look and feel.", exc);
        ErrorHandler.handle(msg, exc);
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

        DateFormat mTimeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
        
        String message = formatMessage(record);
        sb.append(mTimeFormat.format(new java.util.Date(System.currentTimeMillis())));
        sb.append(" ");
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
            // ignore
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
  public static void shutdown(boolean log) {
    mSaveThreadShouldStop = true;
    flushSettings(log);
  }


  public static void updateProxySettings() {
    String httpHost = "", httpPort = "", httpUser = "", httpPassword = "";

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

    System.setProperty("http.proxyHost",     httpHost);
    System.setProperty("http.proxyPort",     httpPort);
    System.setProperty("http.proxyUser",     httpUser);
    System.setProperty("http.proxyPassword", httpPassword);
  }

}
  class FileLoggingHandler extends Handler {

    private Formatter mFormatter;
    private PrintWriter mWriter;

    public FileLoggingHandler(String fName, Formatter formatter) throws IOException {
      mFormatter = formatter;
      File f = new File(fName);
      mWriter = new PrintWriter(new FileOutputStream(f));
    }


    public void close() throws SecurityException {
      mWriter.close();
    }

    public void flush() {
      mWriter.flush();
    }

    public void publish(LogRecord record) {
      mWriter.println(mFormatter.format(record));
      flush();
    }
  }
