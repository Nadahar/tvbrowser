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
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.commons.lang3.StringUtils;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SkinInfo;

import com.jgoodies.looks.LookUtils;
import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;

import ca.beq.util.win32.registry.RegistryKey;
import ca.beq.util.win32.registry.RegistryValue;
import ca.beq.util.win32.registry.RootKey;
import devplugin.Date;
import devplugin.ProgramFieldType;
import devplugin.Version;
import tvbrowser.core.ChannelList;
import tvbrowser.core.PendingMarkings;
import tvbrowser.core.PluginLoader;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.GenericFilterMap;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.programformating.GlobalPluginProgramFormatingManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.extras.common.InternalPluginProxyList;
import tvbrowser.extras.programinfo.ProgramInfo;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.extras.searchplugin.SearchPlugin;
import tvbrowser.ui.configassistant.TvBrowserPictureSettingsUpdateDialog;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.SoftwareUpdater;
import tvbrowser.ui.splashscreen.DummySplash;
import tvbrowser.ui.splashscreen.Splash;
import tvbrowser.ui.splashscreen.SplashScreen;
import tvbrowser.ui.tray.SystemTray;
import tvbrowser.ui.update.PluginAutoUpdater;
import tvbrowser.ui.update.SoftwareUpdateDlg;
import tvbrowser.ui.update.SoftwareUpdateItem;
import tvbrowser.ui.update.TvBrowserVersionChangeDlg;
import tvdataservice.MarkedProgramsMap;
import util.browserlauncher.Launch;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
import util.io.Mirror;
import util.misc.OperatingSystem;
import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.UIThreadRunner;
import util.ui.UiUtilities;
import util.ui.textcomponentpopup.TextComponentPopupEventQueue;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class TVBrowser {

  private static final String SUN_JAVA_WARNING = "TV-Browser was developed for Sun Java and may not run correctly with your Java implementation.";

  private static final Logger mLog
    = Logger.getLogger(TVBrowser.class.getName());

  /** The localizer for this class. */
  private static Localizer mLocalizer;

  private static String curLookAndFeel;
  
  public static final ArrayList<Image> ICONS_WINDOW = new ArrayList<Image>(4);

  private static final boolean mIsStable = true;
  private static final int mMajorVersion = 4;
  private static final int mMinorVersion = 00;
  private static final int mSubMinorVersion = 00;

  /* If you want to change the version string, add it to the beginning of this array.
     We need the old version strings to import the settings.
     
     !!!!!!!!!!!!
     
     ATTENTION: NEVER USE - IN THE NAME OF A VERSION. IT WILL CAUSE PROBLEMS FOR NIGHTLY
                USERS!!!
                
     !!!!!!!!!!!!
  */
  /** The string array with the names of the earlier versions. */
  private static final String[] ALL_VERSIONS = new String[] {
          "4", "3.4.4.98 RC3", "3.4.4.97 RC2", "3.4.4.95 Beta1", "3.4.4.50 SVN",
          "3.4.4", "3.4.3.96 RC1", "3.4.3.95 Beta1", "3.4.3.52 SVN", "3.4.3.51 SVN", "3.4.3.50 SVN",
	        "3.4.3", "3.4.2.50 SVN", "3.4.2", "3.4.1.96 RC1", "3.4.1.95 Beta1", "3.4.1.50-SVN",
          "3.4.1a", "3.4.1", "3.4.0.99 RC", "3.4.0.98 RC", "3.4.0.97 RC", "3.4.0.96 RC", "3.4.0.95 Beta", "3.4.0.50-SVN",
          "3.4.0.1 Hotfix",
          "3.4", "3.3.97 RC", "3.3.96 Beta", "3.3.95 Beta", "3.3.3.51 SVN", "3.3.3.50 SVN",
          "3.3.3", "3.3.3beta1", "3.3.2.50 SVN",
          "3.3.2", "3.3.2beta1", "3.3.1.50 SVN", 
          "3.3.1", "3.3.1RC1", "3.3.1beta1", "3.3.0.51 SVN", "3.3.0.50 SVN",
	      "3.3a", "3.3", "3.3RC2", "3.3RC1", "3.3beta1", "3.2.1.51 SVN", "3.2.1.50 SVN",
          "3.2.1", "3.2.1RC1", "3.2.1beta2", "3.2.1beta1", "3.2.0.50 SVN",
          "3.2", "3.2RC1", "3.2beta2", "3.2beta1", "3.1.0.50 SVN",
          "3.1", "3.1RC2", "3.1RC1", "3.1beta2", "3.1beta1",
          "3.0.2.99 SVN", "3.0.2", "3.0.2 RC2", "3.0.2 RC1", "3.0.2beta1", "3.0.1.99 SVN",
          "3.0.1",
          "3.0", "3.0 RC3", "3.0 RC2", "3.0 RC1", "3.0beta2", "3.0beta1", "3.0 (alpha2)", "3.0 (alpha1)", "3.0 (alpha)", "3.0 (SVN)",
          "2.7.6",
          "2.7.5", "2.7.5 (SVN)",
          "2.7.4", "2.7.4 (SVN)",
          "2.7.3", "2.7.3beta", "2.7.3 (SVN)",
          "2.7.2", "2.7.2 RC3", "2.7.2 RC2", "2.7.2 RC1", "2.7.2beta", "2.7.2 (SVN)",
          "2.7.1", "2.7.1 RC1", "2.7.1beta1",
          "2.7.x (SVN)",
          "2.7", "2.7 RC2", "2.7 RC1", "2.7beta2", "2.7beta1", "2.7 (SVN)",
          "2.6.3", "2.6.3beta",
          "2.6.2",
          "2.6.1", "2.6.1beta", "2.6.1 (SVN)",
          "2.6", "2.6beta2", "2.6beta1", "2.6alpha3", "2.6alpha2", "2.6alpha1", "2.6 (alpha)",
          "2.5.3", "2.5.3beta3", "2.5.3beta2", "2.5.3beta1", "2.5.3 (alpha)",
          "2.5.2",
          "2.5.1", "2.5.1beta3", "2.5.1beta2", "2.5.1beta1",
          "2.5", "2.5beta3", "2.5beta2", "2.5beta1", "2.5 alpha",
          "2.2.5",
          "2.2.4",
          "2.2.3",
          "2.2.2", "2.2.2beta2", "2.2.2beta1",
          "2.2.1", "2.2.1beta3",
          "2.2", "2.2beta2", "2.2beta1", "2.2 (SVN)"
  };

  static {
    ICONS_WINDOW.add(ImageUtilities.createImage("imgs/tvbrowser128.png"));
    ICONS_WINDOW.add(ImageUtilities.createImage("imgs/tvbrowser48.png"));
    ICONS_WINDOW.add(ImageUtilities.createImage("imgs/tvbrowser32.png"));
    ICONS_WINDOW.add(ImageUtilities.createImage("imgs/tvbrowser16.png"));
    
    File nightlyValues = new File("NIGHTLY_VALUES");

    if(!mIsStable && nightlyValues.isFile()) {
      try {
        RandomAccessFile in = new RandomAccessFile(nightlyValues, "r");

        String versionAppendix = "-" + in.readLine();
        ALL_VERSIONS[0] += versionAppendix;

        in.close();
      } catch (Exception e) {
        // ignore
      }
    }
  }

  /** The current version. */

  private static final boolean mIsTransportable = new File("settings").isDirectory();
  public static final devplugin.Version VERSION=new devplugin.Version(mMajorVersion,mMinorVersion,mSubMinorVersion,mIsStable,ALL_VERSIONS[0] + (mIsTransportable ? " transportable" : ""));

  /** The title bar string. */
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
  
  private static boolean mSaveThreadIsRunning;

  /**
   * Show the SplashScreen during startup
   */
  private static boolean mShowStartScreen = true;

  /**
   * Show TV-Browser in fullscreen
   */
  private static boolean mFullscreen = false;

  /**
   * Show only minimized
   */
  private static boolean mMinimized = false;
  
  /**
   * Start TV-Browser in safe mode (no external plugins will be loaded).
   */
  private static boolean mSafeMode = false;

  /**
   * avoid initializing the look and feel multiple times
   */
  private static boolean lookAndFeelInitialized = false;

  private static Timer mAutoDownloadWaitingTimer;

  private static boolean mIgnoreJVM = false;

  /**
   * restart functionality
   */
  private static String[] restartCMD = null;
  
  /**
   * Entry point of the application
   * @param args The arguments given in the command line.
   */
  public static void main(String[] args) {
    // Read the command line parameters
    parseCommandline(args);
    
    try {
      Toolkit.getDefaultToolkit().setDynamicLayout((Boolean) Toolkit.getDefaultToolkit().getDesktopProperty("awt.dynamicLayoutSupported"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    mLocalizer = util.ui.Localizer.getLocalizerFor(TVBrowser.class);

    // Check whether the TV-Browser was started in the right directory
    if ( !new File("imgs").exists()) {
      String msg = "Please start TV-Browser in the TV-Browser directory!";
      if (mLocalizer != null) {
        msg = mLocalizer.msg("error.2",
          "Please start TV-Browser in the TV-Browser directory!");
      }
      JOptionPane.showMessageDialog(null, msg);
      System.exit(1);
    }

    if(mIsTransportable) {
      System.getProperties().remove("propertiesfile");
    }

    // setup logging

    // Get the default Logger
    final Logger mainLogger = Logger.getLogger("");

    // Use a even simpler Formatter for console logging
    mainLogger.getHandlers()[0].setFormatter(createFormatter());
    
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread thread, Throwable t) {
        mainLogger.log(Level.SEVERE, "UNCAUGHT EXCEPTION IN THREAD '" + thread.getName() + "'", t);
      }
    });
    
    if(mIsTransportable) {
      File settingsDir = new File("settings");
      try {
        File test = File.createTempFile("write","test",settingsDir);
        test.delete();
      } catch (IOException e) {
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1) {
          //ignore
        }

        JTextArea area = new JTextArea(mLocalizer.msg("error.noWriteRightsText","You are using the transportable version of TV-Browser but you have no writing rights in the settings directory:\n\n{0}'\n\nTV-Browser will be closed.",settingsDir.getAbsolutePath()));
        area.setFont(new JLabel().getFont());
        area.setFont(area.getFont().deriveFont((float)14).deriveFont(Font.BOLD));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setPreferredSize(new Dimension(500,100));
        area.setEditable(false);
        area.setBorder(null);
        area.setOpaque(false);

        JOptionPane.showMessageDialog(null,area,mLocalizer.msg("error.noWriteRightsTitle","No write rights in settings directory"),JOptionPane.ERROR_MESSAGE);
        System.exit(1);
      }
    }
    
    restartCMD = generateRestartCMD();
    

    // Load the settings
    Settings.loadSettings();
    Locale.setDefault(new Locale(Settings.propLanguage.getString(), Settings.propCountry.getString()));

    if (Settings.propFirstStartDate.getDate() == null) {
      Settings.propFirstStartDate.setDate(Date.getCurrentDate());
    }

    if (!createLockFile()) {
      updateLookAndFeel();
      showTVBrowserIsAlreadyRunningMessageBox();
    }

    String logDirectory = Settings.propLogdirectory.getString();
    if (logDirectory != null) {
      try {
        File logDir = new File(logDirectory);
        logDir.mkdirs();
        mainLogger.addHandler(new FileLoggingHandler(logDir.getAbsolutePath()+"/tvbrowser.log", createFormatter()));
      } catch (IOException exc) {
        String msg = mLocalizer.msg("error.4", "Can't create log file.");
        ErrorHandler.handle(msg, exc);
      }
    }
    else {
      // if no logging is configured, show WARNING or worse for normal usage, show everything for unstable versions
      if (TVBrowser.isStable()) {
        mainLogger.setLevel(Level.WARNING);
      }
    }

    // log warning for OpenJDK users
    if (!isJavaImplementationSupported()) {
      mainLogger.warning(SUN_JAVA_WARNING);
    }
        
    /* Set the proxy settings
     * 
     * ATTENTION: This has to be done before all Internet connections
     */
    updateProxySettings();

    //Update plugin on version change
    if(Settings.propTVBrowserVersion.getVersion() != null && VERSION.compareTo(Settings.propTVBrowserVersion.getVersion()) > 0) {
      updateLookAndFeel();
      updatePluginsOnVersionChange();
    }

    String timezone = Settings.propTimezone.getString();
    if (timezone != null) {
      TimeZone.setDefault(TimeZone.getTimeZone(timezone));
    }
    mLog.info("Using timezone "+TimeZone.getDefault().getDisplayName());

    // refresh the localizers because we know the language now
    Localizer.emptyLocalizerCache();
    mLocalizer = Localizer.getLocalizerFor(TVBrowser.class);
    ProgramInfo.resetLocalizer();
    ReminderPlugin.resetLocalizer();
    Date.resetLocalizer();
    ProgramFieldType.resetLocalizer();

    // Set the String to use for indicating the user agent in http requests
    System.setProperty("http.agent", MAINWINDOW_TITLE);

    Version tmpVer = Settings.propTVBrowserVersion.getVersion();
    final Version currentVersion = tmpVer != null ? new Version(tmpVer.getMajor(),tmpVer.getMinor(),tmpVer.getSubMinor(),Settings.propTVBrowserVersionIsStable.getBoolean()) : tmpVer;

    /*TODO Create an update service for installed TV data services that doesn't
     *     work with TV-Browser 3.0 and updates for them are known.
     */
    if(!isTransportable() && Launch.isOsWindowsNtBranch() && currentVersion != null && currentVersion.compareTo(new Version(3,0,true)) < 0) {
      String tvDataDir = Settings.propTVDataDirectory.getString().replace("/",File.separator);

      if(!tvDataDir.startsWith(System.getenv("appdata"))) {
        StringBuilder oldDefaultTvDataDir = new StringBuilder(System.getProperty("user.home")).append(File.separator).append("TV-Browser").append(File.separator).append("tvdata");

        if(oldDefaultTvDataDir.toString().equals(tvDataDir)) {
          Settings.propTVDataDirectory.setString(Settings.propTVDataDirectory.getDefault());
        }
      }
    }
    
    Settings.propTVBrowserVersion.setVersion(VERSION);
    Settings.propTVBrowserVersionIsStable.setBoolean(VERSION.isStable());

    final AtomicReference<Splash> splashRef = new AtomicReference<Splash>();

    if (mShowStartScreen && Settings.propStartScreenShow.getBoolean()) {
      splashRef.set(new SplashScreen());
      splashRef.get().showSplash();
    }
    else {
      if(java.awt.SplashScreen.getSplashScreen() != null && java.awt.SplashScreen.getSplashScreen().isVisible()) {
        java.awt.SplashScreen.getSplashScreen().close();
      }
      splashRef.set(new DummySplash());
    }
    
    mLog.info("Deleting expired TV listings...");
    TvDataBase.getInstance().deleteExpiredFiles(TvDataBase.DEFAULT_DATA_LIFESPAN, false);

    /* Initialize the MarkedProgramsMap */
    MarkedProgramsMap.getInstance();

    if(!mSafeMode) {
      /*Maybe there are tvdataservices to install (.jar.inst files)*/
      PluginLoader.getInstance().installPendingPlugins();
    }
    
    PluginProxyManager.getInstance();
    
    if(!mSafeMode) {
      PluginLoader.getInstance().loadAllPlugins();
    }
    
    SearchPlugin.getInstance();

    mLog.info("Loading TV listings service...");
    splashRef.get().setMessage(mLocalizer.msg("startScreen.dataService", "Loading TV listings service..."));
    
    TvDataServiceProxyManager.getInstance().init();
    
    if(!Settings.propShowAssistant.getBoolean() && TvDataServiceProxyManager.getInstance().getDataServices().length < 1 && !mSafeMode) {
      splashRef.get().hideSplash();
      updateLookAndFeel();
      loadDataServicesAtStartup();
    }
    else {
      ChannelList.createForTvBrowserStart();
      ChannelList.initSubscribedChannels();
    }
    
    ChannelList.checkForJointChannels();
    
    //Preload generic filters 
    GenericFilterMap.getInstance();
    
    if (!lookAndFeelInitialized) {
      mLog.info("Loading Look&Feel...");
      splashRef.get().setMessage(mLocalizer.msg("startScreen.laf", "Loading look and feel..."));
      updateLookAndFeel();
    }
    
    mLog.info("Loading plugins...");
    splashRef.get().setMessage(mLocalizer.msg("startScreen.plugins", "Loading plugins..."));
    
    try {
      PluginProxyManager.getInstance().init();
    } catch(TvBrowserException exc) {
      ErrorHandler.handle(exc);
    }
    
    // Initialize filters of generic filter map
    GenericFilterMap.getInstance().initializeFilters();
    // Mark pending markings
    PendingMarkings.markMapEntries();
    
    splashRef.get().setMessage(mLocalizer.msg("startScreen.tvData", "Checking TV database..."));

    mLog.info("Checking TV listings inventory...");
    TvDataBase.getInstance().checkTvDataInventory(TvDataBase.DEFAULT_DATA_LIFESPAN);

    mLog.info("Starting up...");
    splashRef.get().setMessage(mLocalizer.msg("startScreen.ui", "Starting up..."));
    
    Toolkit.getDefaultToolkit().getSystemEventQueue().push(new TextComponentPopupEventQueue());

    // Init the UI
    final boolean fStartMinimized = Settings.propMinimizeAfterStartup.getBoolean() || mMinimized;
    SwingUtilities.invokeLater(() -> {
      initUi(splashRef.get(), fStartMinimized);

      new Thread("Start finished callbacks") {
        public void run() {
          setPriority(Thread.MIN_PRIORITY);

          // first reset "starting" flag of mainframe
          mainFrame.handleTvBrowserStartFinished();
          
          // first initialize the internal plugins
          InternalPluginProxyIf[] internalPlugins = InternalPluginProxyList.getInstance().getAvailableProxys();
          
          for(InternalPluginProxyIf internalPlugin : internalPlugins) {
            internalPlugin.handleTvBrowserStartFinished();
          }
          
          // now handle all plugins and services
          GlobalPluginProgramFormatingManager.getInstance();
          PluginProxyManager.getInstance().fireTvBrowserStartFinished();
          TvDataServiceProxyManager.getInstance()
              .fireTvBrowserStartFinished();

          // finally submit plugin caused updates to database
          TvDataBase.getInstance().handleTvBrowserStartFinished();
          
          mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowIconified(java.awt.event.WindowEvent e) {
              mSaveThreadShouldStop = true;
              flushSettings(true);
            }
            
            public void windowDeiconified(java.awt.event.WindowEvent e) {
              mSaveThreadShouldStop = false;
              if (mSaveThreadIsRunning == false) {
                startPeriodicSaveSettings();   
              }
            }
          });

          startPeriodicSaveSettings();

        }
      }.start();
      SwingUtilities.invokeLater(() -> {
        ChannelList.completeChannelLoading();
        initializeAutomaticDownload();
        if (Launch.isOsWindowsNtBranch()) {
          try {
            RegistryKey desktopSettings = new RegistryKey(
                RootKey.HKEY_CURRENT_USER, "Control Panel\\Desktop");
            RegistryValue autoEnd = desktopSettings
                .getValue("AutoEndTasks");

            if (autoEnd.getData().equals("1")) {
              RegistryValue killWait = desktopSettings
                  .getValue("WaitToKillAppTimeout");

              int i1 = Integer.parseInt(killWait.getData().toString());

              if (i1 < 5000) {
                JOptionPane pane = new JOptionPane();

                String cancel = mLocalizer.msg("registryCancel",
                    "Close TV-Browser");
                String dontDoIt = mLocalizer.msg("registryJumpOver",
                    "Not this time");

                pane.setOptions(new String[] {
                    Localizer.getLocalization(Localizer.I18N_OK), dontDoIt,
                    cancel });
                pane.setOptionType(JOptionPane.YES_NO_CANCEL_OPTION);
                pane.setMessageType(JOptionPane.WARNING_MESSAGE);
                pane
                    .setMessage(mLocalizer
                        .msg(
                            "registryWarning",
                            "The fast shutdown of Windows is activated.\nThe timeout to wait for before Windows is closing an application is too short,\nto give TV-Browser enough time to save all settings.\n\nThe setting hasn't the default value. It was changed by a tool or by you.\nTV-Browser will now try to change the timeout.\n\nIf you don't want to change this timeout select 'Not this time' or 'Close TV-Browser'."));

                pane.setInitialValue(mLocalizer.msg("registryCancel",
                    "Close TV-Browser"));

                JDialog d = pane.createDialog(UiUtilities
                    .getLastModalChildOf(mainFrame), UIManager
                    .getString("OptionPane.messageDialogTitle"));
                d.setModalityType(ModalityType.DOCUMENT_MODAL);
                UiUtilities.centerAndShow(d);

                if (pane.getValue() == null
                    || pane.getValue().equals(cancel)) {
                  mainFrame.quit();
                } else if (!pane.getValue().equals(dontDoIt)) {
                  try {
                    killWait.setData("5000");
                    desktopSettings.setValue(killWait);
                    JOptionPane
                        .showMessageDialog(
                            UiUtilities.getLastModalChildOf(mainFrame),
                            mLocalizer
                                .msg("registryChanged",
                                    "The timeout was changed successfully.\nPlease reboot Windows!"));
                  } catch (Exception registySetting) {
                    JOptionPane
                        .showMessageDialog(
                            UiUtilities.getLastModalChildOf(mainFrame),
                            mLocalizer
                                .msg(
                                    "registryNotChanged",
                                    "<html>The Registry value couldn't be changed. Maybe you haven't the right to do it.<br>If it is so contact you Administrator and let him do it for you.<br><br><b><Attention:/b> The following description is for experts. If you change or delete the wrong value in the Registry you could destroy your Windows installation.<br><br>To get no warning on TV-Browser start the Registry value <b>WaitToKillAppTimeout</b> in the Registry path<br><b>HKEY_CURRENT_USER\\Control Panel\\Desktop</b> have to be at least <b>5000</b> or the value for <b>AutoEndTasks</b> in the same path have to be <b>0</b>.</html>"),
                            Localizer.getLocalization(Localizer.I18N_ERROR),
                            JOptionPane.ERROR_MESSAGE);
                  }
                }
              }
            }
          } catch (Throwable registry) {
          }
        }

        if (currentVersion != null
            && currentVersion.compareTo(new Version(2, 71, false)) < 0) {
          if (Settings.propProgramPanelMarkedMinPriorityColor.getColor()
              .equals(
                  Settings.propProgramPanelMarkedMinPriorityColor
                      .getDefaultColor())) {
            Settings.propProgramPanelMarkedMinPriorityColor
                .setColor(new Color(255, 0, 0, 30));
          }
          if (Settings.propProgramPanelMarkedMediumPriorityColor.getColor()
              .equals(
                  Settings.propProgramPanelMarkedMediumPriorityColor
                      .getDefaultColor())) {
            Settings.propProgramPanelMarkedMediumPriorityColor
                .setColor(new Color(140, 255, 0, 60));
          }
          if (Settings.propProgramPanelMarkedHigherMediumPriorityColor
              .getColor().equals(
                  Settings.propProgramPanelMarkedHigherMediumPriorityColor
                      .getDefaultColor())) {
            Settings.propProgramPanelMarkedHigherMediumPriorityColor
                .setColor(new Color(255, 255, 0, 60));
          }
          if (Settings.propProgramPanelMarkedMaxPriorityColor.getColor()
              .equals(
                  Settings.propProgramPanelMarkedMaxPriorityColor
                      .getDefaultColor())) {
            Settings.propProgramPanelMarkedMaxPriorityColor
                .setColor(new Color(255, 180, 0, 110));
          }
        }

        // check if user should select picture settings
        if (currentVersion != null
            && currentVersion.compareTo(new Version(2, 22)) < 0) {
          TvBrowserPictureSettingsUpdateDialog.createAndShow(mainFrame);
        } else if (currentVersion != null
            && currentVersion.compareTo(new Version(2, 51, true)) < 0) {
          Settings.propAcceptedLicenseArrForServiceIds
              .setStringArray(new String[0]);
        }

        if (currentVersion != null
            && currentVersion.compareTo(new Version(2, 60, true)) < 0) {
          int startOfDay = Settings.propProgramTableStartOfDay.getInt();
          int endOfDay = Settings.propProgramTableEndOfDay.getInt();

          if (endOfDay - startOfDay < -1) {
            Settings.propProgramTableEndOfDay.setInt(startOfDay);

            JOptionPane
                .showMessageDialog(
                    UiUtilities.getLastModalChildOf(mainFrame),
                    mLocalizer
                        .msg(
                            "timeInfoText",
                            "The time range of the program table was corrected because the defined day was shorter than 24 hours.\n\nIf the program table should show less than 24h use a time filter for that. That time filter can be selected\nto be the default filter by selecting it in the filter settings and pressing on the button 'Default'."),
                    mLocalizer.msg("timeInfoTitle", "Times corrected"),
                    JOptionPane.INFORMATION_MESSAGE);
            Settings.handleChangedSettings();
          }
        }
        
        if(currentVersion != null 
            && currentVersion.compareTo(new Version(3,43,52,false)) < 0) {
          FilterComponentList.getInstance().store();
        }
        
        if(currentVersion != null
            && currentVersion.compareTo(new Version(3,30,51,false)) < 0) {
          Settings.updateContextMenuSettings();
        }

        if(currentVersion != null
            && currentVersion.compareTo(new Version(3,33,51,false)) < 0) {
          Settings.propSubscribedChannels.setChannelArray(ChannelList.getSubscribedChannels());
        }
        
        if(currentVersion != null
            && currentVersion.compareTo(new Version(3,39,7,false)) < 0) {
          ProgramFieldType[] typeArr = Settings.propProgramInfoFields.getProgramFieldTypeArray();
          String[] separators = Settings.propProgramInfoFieldsSeparators.getStringArray();
          
          ArrayList<String> separatorList = new ArrayList<String>();
          
          for(int i2 = 0; i2 < typeArr.length - 1; i2++) {
            if(i2 < separators.length - 1 && separators[i2].equals("\n")) {
              separatorList.add(separators[i2]);
            }
            else {
              separatorList.add(" - ");
            }
          }
          
          Settings.propProgramInfoFieldsSeparators.setStringArray(separatorList.toArray(new String[separatorList.size()]));
        }
        
        MainFrame.getInstance().getProgramTableScrollPane()
            .requestFocusInWindow();
      });
    });

     // register the shutdown hook
    Runtime.getRuntime().addShutdownHook(new Thread("Shutdown hook") {
      public void run() {
        deleteLockFile();
        MainFrame.getInstance().quit(false);
      }
     });
  }
  
  private static String[] generateRestartCMD(){
		try {
		final String SUN_JAVA_COMMAND = "sun.java.command";
		// init the command to execute, add the vm args
		final List<String> cmd = new ArrayList<String>();
		// java binary
		String java = System.getProperty("java.home");
		if (java==null) return null;
		java = java.concat("/bin/java");
		cmd.add(java);
		// vm arguments
		List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
		for (String arg : vmArguments) {
			// if it's the agent argument : we ignore it otherwise the
			// address of the old application and the new one will be in
			// conflict
			if (!arg.contains("-agentlib")) {
				cmd.add(arg);
			}
		}

		// program main and program arguments
		if (System.getProperty(SUN_JAVA_COMMAND) == null) return null;
		String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(" ");
		// program main is a jar
		if (mainCommand[0].endsWith(".jar")) {
			// if it's a jar, add -jar mainJar
			cmd.add("-jar");
			cmd.add(new File(mainCommand[0]).getPath());
		} else {
			// else it's a .class, add the classpath and mainClass
			if (System.getProperty("java.class.path")==null) return null;
			cmd.add("-cp");
			cmd.add(System.getProperty("java.class.path"));
			cmd.add(mainCommand[0]);
		}
		// finally add program arguments
		for (int i = 1; i < mainCommand.length; i++) {
			cmd.add(mainCommand[i]);
		}		
		String[] cmdarr = new String[cmd.size()];
		for(int i=0;i<cmd.size();++i){
			cmdarr[i] = cmd.get(i);
		}
		return cmdarr;
		} catch (Exception e) {			// something went wrong
			e.printStackTrace();
			return null;
		}
	  
  }
  
  public static boolean restartEnabled(){
	  return (restartCMD!=null);
  }
  
	public static void addRestart() {
		try{
			// execute the command in a shutdown hook, to be sure that all the
			// resources have been disposed before restarting the application
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						Thread.sleep(250);
						Runtime.getRuntime().exec(restartCMD);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {			// something went wrong
			e.printStackTrace();
		}

	}

  private static boolean isJavaImplementationSupported() {
    if (mIgnoreJVM) {
      return true;
    }
    String vendor = System.getProperty("java.vendor");
    if (!StringUtils.containsIgnoreCase(vendor, "sun") && !StringUtils.containsIgnoreCase(vendor, "oracle")) {
      return false;
    }
    String implementation = System.getProperty("java.vm.name");
    if (StringUtils.containsIgnoreCase(implementation, "openjdk")) {
      return false;
    }
    return true;
  }

  private static void startPeriodicSaveSettings() {
    // Every 5 minutes we store all the settings so they are stored in case of
    // an unexpected failure
    Thread saveThread = new Thread("Store settings periodically") {
      public void run() {
        mSaveThreadIsRunning = true;
        mSaveThreadShouldStop = false;
        while (! mSaveThreadShouldStop) {
          try {
            Thread.sleep(5 * 60 * 1000);
          }
          catch (Exception exc) {
            // ignore
          }

          if(!mSaveThreadShouldStop && !TvDataUpdater.getInstance().isDownloading()) {
            flushSettings(true);
          }
        }
        mSaveThreadIsRunning = false;
      }
    };
    saveThread.setPriority(Thread.MIN_PRIORITY);
    saveThread.start();
  }

  private static void showUsage(String[] args) {
    for (String argument : args) {
      if (StringUtils.containsIgnoreCase(argument, "ignorejvm") || argument.equalsIgnoreCase("-i")) {
        mIgnoreJVM = true;
      }
    }
    if (!isJavaImplementationSupported()) {
      System.out.println(SUN_JAVA_WARNING);
    }
    System.out.println("command line options:");
    System.out.println("    -minimized      The main window will be minimized after start up");
    System.out.println("    -nostartscreen  No start screen during start up");
    System.out.println("    -fullscreen     Start in fullscreen-mode");
    System.out.println("    -ignorejvm      Don't check for Sun Java");
    System.out.println("    -safemode       Don't load Plugins");
    System.out.println();
  }

  private static void parseCommandline(String[] args) {
    showUsage(args);
    for (String argument : args) {
      if (argument.equalsIgnoreCase("-help") || argument.equalsIgnoreCase("-h")) {
        System.exit(0);
      } else if (argument.equalsIgnoreCase("-minimized") || argument.equalsIgnoreCase("-m")) {
        mMinimized = true;
      } else if (argument.equalsIgnoreCase("-nostartscreen") || argument.equalsIgnoreCase("-n")) {
        mShowStartScreen = false;
      } else if (argument.equalsIgnoreCase("-fullscreen") || argument.equalsIgnoreCase("-f")) {
        mFullscreen = true;
      } else if (argument.equalsIgnoreCase("-safemode") || argument.equalsIgnoreCase("-s")) {
        mSafeMode = true;
      } else if (argument.startsWith("-D")) {
        if (argument.indexOf("=") >= 2) {
          String key = argument.substring(2, argument.indexOf("="));
          String value = StringUtils.substringAfter(argument, "=");
          if (key.equals("user.language")) {
            System.getProperties().setProperty("user.language", value);
            Locale.setDefault(new Locale(value));
          } else {
            System.setProperty(key, value);
          }
        } else {
          mLog.warning("Wrong Syntax in parameter: '" + argument + "'");
        }
      } else {
        mLog.warning("Unknown command line parameter: '" + argument + "'");
      }
    }
  }


  /**
   * Create the .lock file in the user home directory
   * @return false, if the .lock file exist and is locked or cannot be locked.
   */
  private static boolean createLockFile() {
    String dir = Settings.getUserDirectoryName();

    if(!new File(dir).isDirectory()) {
      new File(dir).mkdirs();
    }

    File lockFile = new File(dir, ".lock");

    if(lockFile.exists()) {
      try {
        mLockFile = new RandomAccessFile(lockFile.toString(),"rw");
        mLock = mLockFile.getChannel().tryLock();

        if(mLock == null) {
          return false;
        }
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
        if(e instanceof IOException) {
          mLog.log(Level.WARNING, e.getLocalizedMessage(), e);
        }
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


  private static void showTVBrowserIsAlreadyRunningMessageBox() {
    try {
      UIThreadRunner.invokeAndWait(() -> {
        Object[] options = { Localizer.getLocalization(Localizer.I18N_CLOSE),
            mLocalizer.msg("startAnyway", "start anyway") };
        if (JOptionPane.showOptionDialog(null, mLocalizer.msg("alreadyRunning", "TV-Browser is already running"),
            mLocalizer.msg("alreadyRunning", "TV-Browser is already running"), JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE, null, options, options[0]) != 1) {
          System.exit(-1);
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

  private static void initUi(Splash splash, boolean startMinimized) {
    mainFrame=MainFrame.getInstance();
    PluginProxyManager.getInstance().setParentFrame(mainFrame);
    TvDataServiceProxyManager.getInstance().setParamFrame(mainFrame);

    // Set the program icon
    
    mainFrame.setIconImages(ICONS_WINDOW);

    mTray = new SystemTray();

    if (mTray.initSystemTray()) {
        mTray.createMenus();
    } else {
      mLog.info("platform independent mode is ON");
      addTrayWindowListener();
    }

    // Set the right size
    mLog.info("Setting frame size and location");
    
    final int windowWidth = Settings.propWindowWidth.getInt();
    final int windowHeight = Settings.propWindowHeight.getInt();
    mainFrame.setSize(windowWidth, windowHeight);
    final int windowX = Settings.propWindowX.getInt();
    final int windowY = Settings.propWindowY.getInt();

    final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

    if ((windowX == -1 && windowY == -1) || windowX + windowWidth < 0 || windowX > screen.width + 10 || windowY + windowHeight < 0 || windowY > screen.height + 10 || windowWidth < 200 || windowHeight < 200) {
      UiUtilities.centerAndShow(mainFrame);
    } else {
      mainFrame.setLocation(windowX, windowY);
    }
    
    SwingUtilities.invokeLater(() -> {
      Point p = mainFrame.getLocation();
      
      if(windowX < 0 || windowY < 0 || windowX > screen.width || windowY > screen.height) {
        mainFrame.setLocationRelativeTo(null);
      }
      else if(p.x != windowX || windowY != p.y) {
        mainFrame.setLocation(windowX - Math.abs(p.x-windowX), windowY - Math.abs(p.y-windowY));
      }
    });

    mainFrame.setVisible(true);
    ErrorHandler.setFrame(mainFrame);

    splash.hideSplash();

    mainFrame.repaint();

    // maximize the frame if wanted
    if (Settings.propIsWindowMaximized.getBoolean()) {
      SwingUtilities.invokeLater(() -> {
        mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        SwingUtilities.invokeLater(() -> {
          mainFrame.repaint();
        });
      });
    }

    // minimize the frame if wanted
    if (startMinimized) {
      mainFrame.setExtendedState(Frame.ICONIFIED);
    }

    if (mFullscreen || Settings.propIsUsingFullscreen.getBoolean()) {
       SwingUtilities.invokeLater(() -> {
          mainFrame.switchFullscreenMode();
       });
    }

    if (Settings.propShowAssistant.getBoolean()) {
      mLog.info("Running setup assistant");
      mainFrame.runSetupAssistant();
    }
  }

  /**
   * initialize the automatic download timer
   */
  private static void initializeAutomaticDownload() {
    if (!Settings.propShowAssistant.getBoolean()) {
      SwingUtilities.invokeLater(() -> {
        boolean automaticDownloadStarted = handleAutomaticDownload();

        boolean dataAvailable = TvDataBase.getInstance().dataAvailable(new Date());
        if (!automaticDownloadStarted && (! dataAvailable) && (ChannelList.getNumberOfSubscribedChannels() > 0)) {
          mainFrame.askForDataUpdateNoDataAvailable();
        }
        mainFrame.scrollToNowFirst();
      });
    }
  }

  /**
   * Saves the main settings.
   *
   * @param log If it should be written into the log.
   */
  public static synchronized void flushSettings(boolean log) {
    // don't store settings if mainFrame is not available
    // may happen during debugging sessions
    if ((mainFrame == null) || (mainFrame.getWidth() == 0)) {
      return;
    }
    if(log) {
      mLog.info("Channel Settings (day light saving time corrections/icons)");
    }
    //ChannelList.storeAllSettings();

    SearchPlugin.getInstance().store();
    ProgramInfo.getInstance().store();
    mainFrame.storeSettings();

    if(log) {
      mLog.info("Storing window size and location");

      int state = mainFrame.getExtendedState();

      if(!mainFrame.isFullScreenMode()) {
        boolean maximized = (state & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
        Settings.propIsWindowMaximized.setBoolean(maximized);
  
        if (! maximized) {
          // Save the window size and location only when not maximized
          Settings.propWindowWidth.setInt(mainFrame.getWidth());
          Settings.propWindowHeight.setInt(mainFrame.getHeight());
          Settings.propWindowX.setInt(mainFrame.getX());
          Settings.propWindowY.setInt(mainFrame.getY());
        }
      }
      else { 
        
      }
    }

    if(log) {
      mLog.info("Storing settings");
    }
    try {
      Settings.storeSettings(log);
    } catch (TvBrowserException e) {
      ErrorHandler.handle(e);
    }
  }

  private static void addTrayWindowListener() {
    if(mMainWindowAdapter == null) {
      mMainWindowAdapter = new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
          if (Settings.propOnlyMinimizeWhenWindowClosing.getBoolean()) {
            MainFrame.getInstance().setExtendedState(JFrame.ICONIFIED);
          } else {
            mainFrame.quit();
          }
        }
      };
    }
    mainFrame.addWindowListener(mMainWindowAdapter);
  }

  /**
   * Gets if the system tray is used.
   *
   * @return <code>true</code> if the system tray is used, <code>false</code> otherwise.
   */
  public static boolean isUsingSystemTray() {
    return mTray.isTrayUsed();
  }

  /**
   * Loads the tray icon.
   */
  public static void loadTray() {
    if(!mTray.isTrayUsed()) {
      mTray.initSystemTray();
    }
    if(mTray.isTrayUsed()) {
      mTray.createMenus();
      if(mMainWindowAdapter != null) {
        mainFrame.removeWindowListener(mMainWindowAdapter);
      }
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
        SwingUtilities.invokeLater(() -> {
          MainFrame.getInstance().showFromTray(MainFrame.ICONIFIED);
        });
      }
    }
  }

  /**
   * Shows a balloon tip on the TV-Browser tray icon.
   * <p>
   * @param caption The caption of the displayed message.
   * @param message The message to display in the balloon tip.
   * @param messageType The type of the displayed balllon tip.
   * @return If the balloon tip could be shown.
   */
  public static boolean showBalloonTip(String caption, String message, java.awt.TrayIcon.MessageType messageType) {
    if(mTray.isTrayUsed()) {
      return mTray.showBalloonTip(caption,message,messageType);
    }

    return false;
  }

  /**
   * Starts an automatic download if required
   * @return false, if no download got started
   */
  public static boolean handleAutomaticDownload() {
    String autoDLType = Settings.propAutoDownloadType.getString();
    if ((ChannelList.getNumberOfSubscribedChannels() == 0)
      || autoDLType.equals("never"))
    {
      // Nothing to do
      return false;
    }

    if (!isAutomaticDownloadDateReached()) { // dont update yet
      return false;
    }

    if((mAutoDownloadWaitingTimer != null
          && mAutoDownloadWaitingTimer.isRunning())) {
      return true;
    }

    if(Settings.propAutoDownloadWaitingEnabled.getBoolean() && Settings.propAutoDownloadWaitingTime.getShort() > 0) {
      final long timerStart = Calendar.getInstance().getTimeInMillis();
      if(mAutoDownloadWaitingTimer == null) {
        mAutoDownloadWaitingTimer = new Timer(1000,
          e -> {
            int seconds = (int) ((Calendar.getInstance().getTimeInMillis() - timerStart) / 1000.0);
            seconds = Settings.propAutoDownloadWaitingTime.getShort() - seconds;
            if (seconds <= 0) {
              mAutoDownloadWaitingTimer.stop();
              mainFrame.getStatusBarLabel().setText("");
              performAutomaticDownload();
          } else {
            mainFrame.getStatusBarLabel().setText(
                mLocalizer.msg("downloadwait",
                    "Automatic download starts in {0} seconds.", seconds));
          }
          }
        );
        mAutoDownloadWaitingTimer.setRepeats(true);
        mAutoDownloadWaitingTimer.start();
      }
      else {
        mAutoDownloadWaitingTimer.restart();
      }
    }
    else {
      return performAutomaticDownload();
    }

    return true;
  }

  private static boolean isAutomaticDownloadDateReached() {
    String autoDLType = Settings.propAutoDownloadType.getString();
    final Date lastDownloadDate = Settings.propLastDownloadDate.getDate();
    Date today = Date.getCurrentDate();

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
    return nextDownloadDate.getNumberOfDaysSince(today) <= 0;
  }

  private static boolean performAutomaticDownload() {
    if (isAutomaticDownloadDateReached()) {
      if (Settings.propAskForAutoDownload.getBoolean()) {
        mainFrame.updateTvData();
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
        if(mainFrame.licenseForTvDataServicesWasAccepted(proxies)) {
          mainFrame.runUpdateThread(Settings.propAutoDownloadPeriod.getInt(), proxies, true);
        }
      }
      return true;
    }
    else {
      return false;
    }
  }


  private static void updateLookAndFeel() {
    try {
      if (OperatingSystem.isWindows()) {
        UIManager.installLookAndFeel("Extended Windows",  "com.jgoodies.looks.windows.WindowsLookAndFeel");
      }
      UIManager.installLookAndFeel("Plastic",           "com.jgoodies.looks.plastic.PlasticLookAndFeel");
      UIManager.installLookAndFeel("Plastic 3D",        "com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
      UIManager.installLookAndFeel("Plastic XP",        "com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
      UIManager.installLookAndFeel("Skin",              "com.l2fprod.gui.plaf.skin.SkinLookAndFeel");

      String classPath = System.getProperty("java.class.path","");
      if (!isStable() || StringUtils.containsIgnoreCase(classPath, "eclipse") || StringUtils.containsIgnoreCase(classPath, "workspace")) {
        Map<String, SkinInfo> substanceSkins = SubstanceLookAndFeel.getAllSkins();
        if (substanceSkins != null) {
          for (SkinInfo skin : substanceSkins.values()) {
            String className = skin.getClassName();
            UIManager.installLookAndFeel("Substance " + skin.getDisplayName(),
                StringUtils.replace(StringUtils.replace(className, "Skin", "LookAndFeel"), "skin.", "skin.Substance"));
          }
        }
      }
    } catch (Exception e1) {
      // ignore any exception for optional skins
      e1.printStackTrace();
    }
    
    /*
     * Workaround for GTK look and feel problems with assistive_technologies=org.GNOME.Accessibility.AtkWrapper
     * for OpenJDK under Linux. The GTK+ look and feel is removed from the selection list, if it is available
     * and OpenJDK is used with the problematic property.
     */
    if (OperatingSystem.isLinux() && System.getProperty("java.runtime.name","").startsWith("OpenJDK")) {
      String[] parts = System.getProperty("java.version","").split("\\.");
      
      if(parts.length > 1) {
        File test = new File("/etc/java-"+parts[1]+"-openjdk/accessibility.properties");
        
        if(test.isFile()) {
          BufferedReader in = null;
          
          try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(test), "UTF-8"));
            
            String line = null;
            
            while((line = in.readLine()) != null) {
              if(!line.trim().startsWith("#") && line.contains("assistive_technologies") && line.contains("org.GNOME.Accessibility.AtkWrapper")) {
                Settings.propLookAndFeel.setDefault(UiUtilities.getDefaultLookAndFeelClassName(true));
                
                LookAndFeelInfo[] lnfs = UIManager.getInstalledLookAndFeels();
                
                ArrayList<LookAndFeelInfo> cleanedLooksList = new ArrayList<LookAndFeelInfo>(lnfs.length-1);
                
                if (lnfs != null) {
                  for (LookAndFeelInfo lookAndFeel : lnfs) {
                    if (!lookAndFeel.getClassName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
                      cleanedLooksList.add(lookAndFeel);
                    }
                  }
                }
                
                UIManager.setInstalledLookAndFeels(cleanedLooksList.toArray(new LookAndFeelInfo[cleanedLooksList.size()]));
              }
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
          finally {
            if(in != null) {
              try {
                in.close();
              } catch (IOException e) {}
            }
          }
        }
      }
    }
    
    
    if (Settings.propLookAndFeel.getString().equals(
        "com.l2fprod.gui.plaf.skin.SkinLookAndFeel")) {
      String themepack = Settings.propSkinLFThemepack.getString();
      try {
        File themepackFile = new File(themepack);
        if (!themepackFile.exists()) {
          themepackFile = new File(Settings.getUserDirectoryName(), themepack);
        }

        if (!themepackFile.exists() && OperatingSystem.isMacOs()) {
          themepackFile = new File("/Library/Application Support/TV-Browser/", themepack);
        }

        SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePack(themepackFile.getAbsolutePath()));
      } catch (Exception exc) {
        ErrorHandler.handle(
          "Could not load themepack.\nSkinLF is disabled now",
          exc);
        Settings.propLookAndFeel.setString(Settings.propLookAndFeel.getDefault());
      }
    } else if (Settings.propLookAndFeel.getString().startsWith("com.jgoodies") && !Settings.propLookAndFeel.getString().startsWith("com.jgoodies.looks.windows.WindowsLookAndFeel")) {
      com.jgoodies.looks.Options.setPopupDropShadowEnabled(Settings.propJGoodiesShadow.getBoolean());
      UIManager.put("jgoodies.popupDropShadowEnabled", Boolean
          .valueOf(Settings.propJGoodiesShadow.getBoolean()));
      try {
        LookUtils.setLookAndTheme((LookAndFeel) Class.forName(Settings.propLookAndFeel.getString()).newInstance(), Class.forName(Settings.propJGoodiesTheme.getString()).newInstance());
      } catch (Throwable e) {
        ErrorHandler.handle("Could not load themepack.\nJGoodies is disabled now", e);
        Settings.propLookAndFeel.setString(Settings.propLookAndFeel.getDefault());
      }
    }

    if (curLookAndFeel == null || !curLookAndFeel.equals(Settings.propLookAndFeel.getString())) {
      try {
        curLookAndFeel = Settings.propLookAndFeel.getString();
        // check if LnF is still available
        boolean foundCurrent = lookAndFeelExists(curLookAndFeel);
        // reset look and feel?
        if (!foundCurrent) {
          if (JOptionPane
              .showConfirmDialog(
                  null,
                  mLocalizer
                      .msg(
                          "lnfMissing",
                          "The look and feel '{0}' is no longer available,\nso the default look and feel will be used.\n\nDo you want to set the look and feel option to the default look and feel?",
                          curLookAndFeel),
                  mLocalizer.msg("lnfMissing.title", "Look and feel missing"),
                  JOptionPane.WARNING_MESSAGE | JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Settings.propLookAndFeel.resetToDefault();
            curLookAndFeel = Settings.propLookAndFeel.getString();
            foundCurrent = true;
          }
        }
        if (foundCurrent) {
          UIThreadRunner.invokeAndWait(() -> {
            try {
              UIManager.setLookAndFeel(curLookAndFeel);
            } catch (Exception e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            mLog.info("setting look and feel to " + curLookAndFeel);
          });
        }
      } catch (Exception exc) {
        String msg =
          mLocalizer.msg("error.1", "Unable to set look and feel.", exc);
        ErrorHandler.handle(msg, exc);
      }
    }

    // set colors for action pane at UIManager
    UIManager.put("TaskPane.foreground",UIManager.get("Button.foreground"));

    if(UIManager.getColor("List.selectionBackground") == null) {
      UIManager.put("List.selectionBackground",UIManager.getColor("Tree.selectionBackground"));
    }
    if(UIManager.getColor("List.selectionForeground") == null) {
      UIManager.put("List.selectionForeground",UIManager.getColor("Tree.selectionForeground"));
    }
    if(UIManager.getColor("MenuItem.selectionForeground") == null) {
      UIManager.put("MenuItem.selectionForeground",UIManager.getColor("Tree.selectionForeground"));
    }
    if(UIManager.getColor("ComboBox.disabledForeground") == null) {
      UIManager.put("ComboBox.disabledForeground", Color.gray);
    }

    if (mainFrame != null) {
      SwingUtilities.updateComponentTreeUI(mainFrame);
      mainFrame.validate();
    }
    lookAndFeelInitialized = true;
  }


  /**
   * Creates a very simple Formatter for log formatting
   *
   * @return a very simple Formatter for log formatting.
   */
  private static Formatter createFormatter() {
    return new Formatter() {
      public synchronized String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        DateFormat mTimeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);

        String message = formatMessage(record);
        sb.append(mTimeFormat.format(new java.util.Date(System.currentTimeMillis())));
        sb.append(' ');
        sb.append(record.getLevel().getLocalizedName());
        sb.append(": ");
        sb.append(message);
        sb.append('\n');
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
   * @param log <code>true</code> if the log should be written, <code>false</code> if not.
   */
  public static void shutdown(boolean log) {
    mSaveThreadShouldStop = true;
    flushSettings(log);
  }


  /**
   * Updates the proxy settings.
   */
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
    System.setProperty("https.proxyHost",     httpHost);
    System.setProperty("https.proxyPort",     httpPort);
    System.setProperty("https.proxyUser",     httpUser);
    System.setProperty("https.proxyPassword", httpPassword);
  }

  /**
   * Gets if TV-Browser runs as portable version.
   *
   * @return If TV-Browser runs as portable version.
   * @since 2.2.2/2.5.1
   */
  public static boolean isTransportable() {
    return mIsTransportable;
  }

  /**
   * get whether this is a development version or a stable build
   * @return if stable
   * @since 2.7
   */
  public static boolean isStable() {
    return mIsStable;
  }

  /**
   * get the version string of this version (for use in directories)
   * @return version string suffix
   * @since 3.0
   */
  public static String getCurrentVersionString() {
    return ALL_VERSIONS[0];
  }

  /**
   * get the version names of all released versions
   * (for use in directory names)
   * @return version string suffixes
   * @since 3.0
   */
  public static String[] getAllVersionStrings() {
    return ALL_VERSIONS.clone();
  }

  private static boolean lookAndFeelExists(String lnf) {
    boolean foundLNF = false;
    for (LookAndFeelInfo lnfInfo : UIManager.getInstalledLookAndFeels()) {
      if (lnfInfo.getClassName().equals(lnf)) {
        foundLNF = true;
        break;
      }
    }
    return foundLNF;
  }

  public static void stopAutomaticDownload() {
    if (mAutoDownloadWaitingTimer != null) {
      mAutoDownloadWaitingTimer.stop();
      mainFrame.getStatusBarLabel().setText("");
      mAutoDownloadWaitingTimer = null;
    }
  }
  
  public static void loadDataServicesAtStartup() {
    try {
      SoftwareUpdateItem[] updateItems = PluginAutoUpdater.getDataServicesForFirstStartup();
      
      if(updateItems.length > 0) {
        Settings.propPluginBetaWarning.setBoolean(false);
        SoftwareUpdateDlg updateDlg = new SoftwareUpdateDlg(UiUtilities.getLastModalChildOf(mainFrame),SoftwareUpdater.ONLY_DATA_SERVICE_TYPE,updateItems,false,null);
        updateDlg.setLocationRelativeTo(null);
        updateDlg.setVisible(true);
        
        Settings.propPluginBetaWarning.setBoolean(true);
        PluginLoader.getInstance().installPendingPlugins();
        PluginLoader.getInstance().loadAllPlugins();
        
        PluginProxy epgPaid = PluginProxyManager.getInstance().getPluginForId("java.epgpaiddata.EPGpaidData");
        
        if(epgPaid != null) {
          try {
            PluginProxyManager.getInstance().activatePlugin(epgPaid);
          } catch (TvBrowserException e) { }
        }
        
        /* Download default channel lists for user country */
        try {
          PluginAutoUpdater.downloadMirrorList();
          
          final Mirror defaultChannelList = PluginAutoUpdater.getPluginUpdatesMirror();
          final File supportedChannelLists = new File(Settings.getUserSettingsDirName(),"channellist_supported.gz");
          
          if(IOUtilities.download(new URL(defaultChannelList.getUrl()+"/"+supportedChannelLists.getName()), supportedChannelLists, 5000)) {
            final String country = Settings.getCountry();
            
            BufferedReader in = null;
            
            try {
              in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(supportedChannelLists))));
              
              String line = null;
              
              while((line = in.readLine()) != null) {
                if(country.equals(line)) {
                  final File defaultCountryChannels = new File(Settings.getUserSettingsDirName(),"channellist_"+country+".gz");
                  IOUtilities.download(new URL(defaultChannelList.getUrl()+"/"+defaultCountryChannels.getName()), defaultCountryChannels, 5000);
                  break;
                }
              }
            }catch(Throwable t) {
              t.printStackTrace();
            }finally {
              IOUtilities.close(in);
            }
          }
        }catch(IOException ioe1) {
          ioe1.printStackTrace();
        }
        
        TvDataServiceProxyManager.getInstance().init();
        ChannelList.createForTvBrowserStart();
        ChannelList.initSubscribedChannels();
        
        String[] deactivatedPlugins = PluginProxyManager.getInstance().getDeactivatedPluginIds();
        
        if(deactivatedPlugins.length > 0) {
          String[] propDeactivatedPlugins = Settings.propDeactivatedPlugins.getStringArray();
          
          for(String deactivatedPlugin : deactivatedPlugins) {
            boolean activate = true;
            
            for(String test : propDeactivatedPlugins) {
              if(test.equals(deactivatedPlugin)) {
                activate = false;
                break;
              }
            }
            
            if(activate) {
              PluginProxy deactivated = PluginProxyManager.getInstance().getPluginForId(deactivatedPlugin);
              
              try {
                PluginProxyManager.getInstance().activatePlugin(deactivated);
              } catch (TvBrowserException e) {}
            }
          }
          
          mainFrame.updatePluginsMenu();
        }
      }
    } catch (IOException e1) {
  }
}

  private static void updatePluginsOnVersionChange() {
    final boolean oldBetaWarning = Settings.propPluginBetaWarning.getBoolean();
    try {
      UIThreadRunner.invokeAndWait(() -> {
        Version obligartoryUpdate = new Version(3,21,51,false);
        
        TvBrowserVersionChangeDlg versionChange = new TvBrowserVersionChangeDlg(Settings.propTVBrowserVersion.getVersion(),obligartoryUpdate);
        versionChange.pack();
        versionChange.setLocationRelativeTo(null);
        versionChange.setVisible(true);
        versionChange.toFront();
        versionChange.requestFocus();

        Settings.propPluginBetaWarning.setBoolean(oldBetaWarning);

        if(versionChange.getIsToCloseTvBrowser()) {
          System.exit(0);
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
  
  public static boolean isSafeMode() {
    return mSafeMode;
  }
}

  class FileLoggingHandler extends Handler {

    private Formatter mFormatter;
    private PrintWriter mWriter;

    /**
     * Creates an instance of FileLoggingHandler.
     *
     * @param fName The name of the log file.
     * @param formatter The formatter for the log file.
     * @throws IOException Is thrown if something goes wrong.
     */
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
