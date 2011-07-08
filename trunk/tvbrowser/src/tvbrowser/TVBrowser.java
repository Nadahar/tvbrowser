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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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

import org.apache.commons.lang.StringUtils;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SkinInfo;

import tvbrowser.core.ChannelList;
import tvbrowser.core.PluginLoader;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.programformating.GlobalPluginProgramFormatingManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
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
import tvdataservice.MarkedProgramsList;
import util.browserlauncher.Launch;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.misc.OperatingSystem;
import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.UIThreadRunner;
import util.ui.UiUtilities;
import util.ui.textcomponentpopup.TextComponentPopupEventQueue;
import ca.beq.util.win32.registry.RegistryKey;
import ca.beq.util.win32.registry.RegistryValue;
import ca.beq.util.win32.registry.RootKey;

import com.jgoodies.looks.LookUtils;
import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;

import devplugin.Date;
import devplugin.ProgramFieldType;
import devplugin.Version;

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

  private static final boolean mIsStable = false;
  private static final int mMajorVersion = 3;
  private static final int mMinorVersion = 1;
  private static final int mSubMinorVersion = 99;

  /* If you want to change the version string, add it to the beginning of this array.
     We need the old version strings to import the settings.
  */
  /** The string array with the names of the earlier versions. */
  private static final String[] ALL_VERSIONS = new String[]{
          "3.0.1.99 SVN",
          "3.0.1",
          "3.0",
          "3.0 RC3",
          "3.0 RC2",
          "3.0 RC1",
          "3.0beta2",
          "3.0beta1",
          "3.0 (alpha2)",
          "3.0 (alpha1)",
          "3.0 (alpha)",
          "3.0 (SVN)",
          "2.7.6",
          "2.7.5",
          "2.7.5 (SVN)",
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

  /**
   * Show the SplashScreen during startup
   */
  private static boolean mShowSplashScreen = true;

  /**
   * Show TV-Browser in fullscreen
   */
  private static boolean mFullscreen = false;

  /**
   * Show only minimized
   */
  private static boolean mMinimized = false;

  /**
   * avoid initializing the look and feel multiple times
   */
  private static boolean lookAndFeelInitialized = false;

  private static Timer mAutoDownloadWaitingTimer;

  private static boolean mIgnoreJVM = false;

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
    Logger mainLogger = Logger.getLogger("");

    // Use a even simpler Formatter for console logging
    mainLogger.getHandlers()[0].setFormatter(createFormatter());

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

    // Set the proxy settings
    updateProxySettings();

    // Set the String to use for indicating the user agent in http requests
    System.setProperty("http.agent", MAINWINDOW_TITLE);

    Version tmpVer = Settings.propTVBrowserVersion.getVersion();
    final Version currentVersion = tmpVer != null ? new Version(tmpVer.getMajor(),tmpVer.getMinor(),Settings.propTVBrowserVersionIsStable.getBoolean()) : tmpVer;

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

    if (mShowSplashScreen && Settings.propSplashShow.getBoolean()) {
      /*UIThreadRunner.invokeLater(new Runnable() {

        @Override
        public void run() {*/
          splashRef.set(new SplashScreen(
              Settings.propSplashImage.getString(),
              Settings.propSplashTextPosX.getInt(),
              Settings.propSplashTextPosY.getInt(),
              Settings.propSplashForegroundColor.getColor()));
          splashRef.get().showSplash();
        /*}
      });*/
    }
    else {
      splashRef.set(new DummySplash());
    }

    /* Initialize the MarkedProgramsList */
    MarkedProgramsList.getInstance();

    /*Maybe there are tvdataservices to install (.jar.inst files)*/
    PluginLoader.getInstance().installPendingPlugins();

    PluginProxyManager.getInstance();
    PluginLoader.getInstance().loadAllPlugins();

    mLog.info("Loading TV listings service...");
    splashRef.get().setMessage(mLocalizer.msg("splash.dataService", "Loading TV listings service..."));
    
    TvDataServiceProxyManager.getInstance().init();
    
    if(!Settings.propShowAssistant.getBoolean() && TvDataServiceProxyManager.getInstance().getDataServices().length < 1) {
      splashRef.get().hideSplash();
      updateLookAndFeel();
      loadDataServicesAtStartup();
    }
    
    ChannelList.createForTvBrowserStart();

    ChannelList.initSubscribedChannels();

    if (!lookAndFeelInitialized) {
      mLog.info("Loading Look&Feel...");
      splashRef.get().setMessage(mLocalizer.msg("splash.laf", "Loading look and feel..."));
      updateLookAndFeel();
    }

    mLog.info("Loading plugins...");
    splashRef.get().setMessage(mLocalizer.msg("splash.plugins", "Loading plugins..."));
    
    try {
      PluginProxyManager.getInstance().init();
    } catch(TvBrowserException exc) {
      ErrorHandler.handle(exc);
    }
    
    splashRef.get().setMessage(mLocalizer.msg("splash.tvData", "Checking TV database..."));

    mLog.info("Checking TV listings inventory...");
    TvDataBase.getInstance().checkTvDataInventory();

    mLog.info("Starting up...");
    splashRef.get().setMessage(mLocalizer.msg("splash.ui", "Starting up..."));
    
    Toolkit.getDefaultToolkit().getSystemEventQueue().push(new TextComponentPopupEventQueue());

    // Init the UI
    final boolean fStartMinimized = Settings.propMinimizeAfterStartup.getBoolean() || mMinimized;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        initUi(splashRef.get(), fStartMinimized);

        new Thread("Start finished callbacks") {
          public void run() {
            setPriority(Thread.MIN_PRIORITY);

            mLog.info("Deleting expired TV listings...");
            TvDataBase.getInstance().deleteExpiredFiles(1, false);

            // first reset "starting" flag of mainframe
            mainFrame.handleTvBrowserStartFinished();

            // initialize program info for fast reaction to program table click
            ProgramInfo.getInstance().handleTvBrowserStartFinished();

            // load reminders and favorites
            ReminderPlugin.getInstance().handleTvBrowserStartFinished();
            FavoritesPlugin.getInstance().handleTvBrowserStartFinished();

            // now handle all plugins and services
            GlobalPluginProgramFormatingManager.getInstance();
            PluginProxyManager.getInstance().fireTvBrowserStartFinished();
            TvDataServiceProxyManager.getInstance()
                .fireTvBrowserStartFinished();

            // finally submit plugin caused updates to database
            TvDataBase.getInstance().handleTvBrowserStartFinished();

            startPeriodicSaveSettings();

          }
        }.start();
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
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

                  int i = Integer.parseInt(killWait.getData().toString());

                  if (i < 5000) {
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
                    d.setModal(true);
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
            MainFrame.getInstance().getProgramTableScrollPane()
                .requestFocusInWindow();
          }
        });
      }
    });

     // register the shutdown hook
    Runtime.getRuntime().addShutdownHook(new Thread("Shutdown hook") {
      public void run() {
        deleteLockFile();
        MainFrame.getInstance().quit(false);
      }
     });
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
    System.out.println("    -minimized    The main window will be minimized after start up");
    System.out.println("    -nosplash     No splash screen during start up");
    System.out.println("    -fullscreen   Start in fullscreen-mode");
    System.out.println("    -ignorejvm    Don't check for Sun Java");
    System.out.println();
  }

  private static void parseCommandline(String[] args) {
    showUsage(args);
    for (String argument : args) {
      if (argument.equalsIgnoreCase("-help") || argument.equalsIgnoreCase("-h")) {
        System.exit(0);
      } else if (argument.equalsIgnoreCase("-minimized") || argument.equalsIgnoreCase("-m")) {
        mMinimized = true;
      } else if (argument.equalsIgnoreCase("-nosplash") || argument.equalsIgnoreCase("-n")) {
        mShowSplashScreen = false;
      } else if (argument.equalsIgnoreCase("-fullscreen") || argument.equalsIgnoreCase("-f")) {
        mFullscreen = true;
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
      UIThreadRunner.invokeAndWait(new Runnable() {
        @Override
        public void run() {
          Object[] options = { Localizer.getLocalization(Localizer.I18N_CLOSE),
              mLocalizer.msg("startAnyway", "start anyway") };
          if (JOptionPane.showOptionDialog(null, mLocalizer.msg("alreadyRunning", "TV-Browser is already running"),
              mLocalizer.msg("alreadyRunning", "TV-Browser is already running"), JOptionPane.DEFAULT_OPTION,
              JOptionPane.WARNING_MESSAGE, null, options, options[0]) != 1) {
            System.exit(-1);
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

  private static void initUi(Splash splash, boolean startMinimized) {
    mainFrame=MainFrame.getInstance();
    PluginProxyManager.getInstance().setParentFrame(mainFrame);
    TvDataServiceProxyManager.getInstance().setParamFrame(mainFrame);

    // Set the program icon
    ArrayList<Image> iconImages = new ArrayList<Image>(2);
    iconImages.add(ImageUtilities.createImage("imgs/tvbrowser128.png"));
    iconImages.add(ImageUtilities.createImage("imgs/tvbrowser48.png"));
    iconImages.add(ImageUtilities.createImage("imgs/tvbrowser32.png"));
    iconImages.add(ImageUtilities.createImage("imgs/tvbrowser16.png"));
    mainFrame.setIconImages(iconImages);

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

    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

    if ((windowX == -1 && windowY == -1) || windowX + windowWidth < 0 || windowX > screen.width + 10 || windowY + windowHeight < 0 || windowY > screen.height + 10 || windowWidth < 200 || windowHeight < 200) {
      UiUtilities.centerAndShow(mainFrame);
    } else {
      mainFrame.setLocation(windowX, windowY);

    }

    mainFrame.setVisible(true);
    ErrorHandler.setFrame(mainFrame);

    splash.hideSplash();

    mainFrame.repaint();

    // maximize the frame if wanted
    if (Settings.propIsWindowMaximized.getBoolean()) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        }
      });
    }

    // minimize the frame if wanted
    if (startMinimized) {
      mainFrame.setExtendedState(Frame.ICONIFIED);
    }

    if (mFullscreen || Settings.propIsUsingFullscreen.getBoolean()) {
       SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            mainFrame.switchFullscreenMode();
         }
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
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          boolean automaticDownloadStarted = handleAutomaticDownload();

          boolean dataAvailable = TvDataBase.getInstance().dataAvailable(new Date());
          if (!automaticDownloadStarted && (! dataAvailable) && (ChannelList.getNumberOfSubscribedChannels() > 0)) {
            mainFrame.askForDataUpdateNoDataAvailable();
          } else {
            mainFrame.scrollToNow();
          }
        }
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
    ChannelList.storeAllSettings();

    SearchPlugin.getInstance().store();
    ProgramInfo.getInstance().store();
    mainFrame.storeSettings();

    if(log) {
      mLog.info("Storing window size and location");

      int state = mainFrame.getExtendedState();

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
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            MainFrame.getInstance().showFromTray(MainFrame.ICONIFIED);
          }
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
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
      if (StringUtils.containsIgnoreCase(classPath, "eclipse") || StringUtils.containsIgnoreCase(classPath, "workspace")) {
        Map<String, SkinInfo> substanceSkins = SubstanceLookAndFeel.getAllSkins();
        if (substanceSkins != null) {
          for (SkinInfo skin : substanceSkins.values()) {
            String className = skin.getClassName();
            UIManager.installLookAndFeel("Substance " + skin.getDisplayName(),
                StringUtils.replace(StringUtils.replace(className, "Skin", "LookAndFeel"), "skin.", "skin.Substance"));
          }
        }
        UIManager.installLookAndFeel("Quaqua","ch.randelshofer.quaqua.QuaquaLookAndFeel");
      }
    } catch (Exception e1) {
      // ignore any exception for optional skins
      e1.printStackTrace();
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
    } else if (Settings.propLookAndFeel.getString().startsWith("com.jgoodies")) {
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
          UIThreadRunner.invokeAndWait(new Runnable() {

            @Override
            public void run() {
              try {
                UIManager.setLookAndFeel(curLookAndFeel);
              } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
              mLog.info("setting look and feel to " + curLookAndFeel);
            }
          });
        }
      } catch (Exception exc) {
        String msg =
          mLocalizer.msg("error.1", "Unable to set look and feel.", exc);
        ErrorHandler.handle(msg, exc);
      }
    }

    // set colors for action pane at UIManager
    UIManager.put("TaskPane.foreGround",UIManager.get("Button.foreground"));

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
   * @param log If it should be written to the log.
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
        SoftwareUpdateDlg updateDlg = new SoftwareUpdateDlg(null,SoftwareUpdater.ONLY_DATA_SERVICE_TYPE,updateItems,true);
        updateDlg.setLocationRelativeTo(null);
        updateDlg.setVisible(true);
        
        Settings.propPluginBetaWarning.setBoolean(true);
        PluginLoader.getInstance().installPendingPlugins();
        PluginLoader.getInstance().loadAllPlugins();
        TvDataServiceProxyManager.getInstance().init();
      }
    } catch (IOException e1) {
    }
  }

  private static void updatePluginsOnVersionChange() {
    final boolean oldBetaWarning = Settings.propPluginBetaWarning.getBoolean();
    try {
      UIThreadRunner.invokeAndWait(new Runnable() {

        @Override
        public void run() {
          TvBrowserVersionChangeDlg versionChange = new TvBrowserVersionChangeDlg(Settings.propTVBrowserVersion.getVersion());
          versionChange.pack();
          versionChange.setLocationRelativeTo(null);
          versionChange.setVisible(true);
          versionChange.toFront();
          versionChange.requestFocus();

          Settings.propPluginBetaWarning.setBoolean(oldBetaWarning);

          if(versionChange.getIsToCloseTvBrowser()) {
            System.exit(0);
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
