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

package tvbrowser.ui.mainframe;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import tvbrowser.TVBrowser;
import tvbrowser.core.ChannelList;
import tvbrowser.core.DateListener;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.TvDataServiceManager;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.ShowAllFilter;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.PluginStateAdapter;
import tvbrowser.ui.aboutbox.AboutBox;
import tvbrowser.ui.filter.dlgs.SelectFilterDlg;
import tvbrowser.ui.finder.FinderPanel;
import tvbrowser.ui.mainframe.toolbar.DefaultToolBarModel;
import tvbrowser.ui.mainframe.toolbar.ToolBar;
import tvbrowser.ui.pluginview.PluginView;
import tvbrowser.ui.programtable.DefaultProgramTableModel;
import tvbrowser.ui.programtable.FilterPanel;
import tvbrowser.ui.programtable.ProgramTableScrollPane;
import tvbrowser.ui.settings.SettingsDialog;
import tvbrowser.ui.update.SoftwareUpdateDlg;
import tvbrowser.ui.update.SoftwareUpdateItem;
import tvbrowser.ui.update.SoftwareUpdater;
import tvdataservice.TvDataService;
import util.ui.UiUtilities;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;
import util.ui.view.Node;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.ProgressMonitor;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class MainFrame extends JFrame implements DateListener {

  private static java.util.logging.Logger mLog = java.util.logging.Logger
      .getLogger(tvbrowser.TVBrowser.class.getName());

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(MainFrame.class);

  private Node mTimebuttonsNode, mDateNode, mRootNode, mChannelNode;

  private Node mPluginsNode;

  private JDialog mConfigAssistantDialog;

  private SoftwareUpdateItem[] mSoftwareUpdateItems = null;

  private ProgramTableScrollPane mProgramTableScrollPane;

  private DefaultProgramTableModel mProgramTableModel;

  private Thread downloadingThread;

  private JPanel jcontentPane;

  private DefaultToolBarModel mToolBarModel;

  private ToolBar mToolBar;

  private StatusBar mStatusBar;

  private FinderPanel mFinderPanel;

  private static MainFrame mSingleton;

  private ChannelChooserPanel mChannelChooser;

  private MenuBar mMenuBar;

  private Component mCenterComponent;

  private boolean mIsVisible;

  private Node mMainframeNode;

  private Node mNavigationNode;

  private Node mDateChannelNode;

  private Date mCurrentDay;

  private PluginView mPluginView;
  
  private FilterPanel mFilterPanel;

  private MainFrame() {
    super(TVBrowser.MAINWINDOW_TITLE);
    mIsVisible = false;

    mStatusBar = new StatusBar();

    if (System.getProperty("mrj.version") != null) {
      /* create the menu bar for MacOS X */
      try {
        Class impl = Class.forName("tvbrowser.ui.mainframe.macosx.MacOSXMenuBar");
        Class mainFrameClass = this.getClass();
        Class jlabelClass = Class.forName("javax.swing.JLabel");
        Constructor cons = impl.getConstructor(new Class[] { mainFrameClass, jlabelClass });
        mMenuBar = (MenuBar) cons.newInstance(new Object[] { this, mStatusBar.getLabel() });
      } catch (Exception e) {
        mLog.warning("Could not instantiate MacOSXMenuBar\n" + e.toString());
        mMenuBar = new DefaultMenuBar(this, mStatusBar.getLabel());
        mLog.info("Using default menu bar");
      }

    } else {
      mMenuBar = new DefaultMenuBar(this, mStatusBar.getLabel());
    }



    // create content
    jcontentPane = (JPanel) getContentPane();
    jcontentPane.setLayout(new BorderLayout());

    JPanel skinPanel = new JPanel();
    skinPanel.setLayout(new BorderLayout());

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setOpaque(false);
    centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    mFilterPanel = new FilterPanel();
    mFilterPanel.setVisible(false);
    
    centerPanel.add(mFilterPanel, BorderLayout.NORTH);
    
    Channel[] channelArr = ChannelList.getSubscribedChannels();
    int startOfDay = Settings.propProgramTableStartOfDay.getInt();
    int endOfDay = Settings.propProgramTableEndOfDay.getInt();
    mProgramTableModel = new DefaultProgramTableModel(channelArr, startOfDay, endOfDay);
    mProgramTableScrollPane = new ProgramTableScrollPane(mProgramTableModel);
    centerPanel.add(mProgramTableScrollPane, BorderLayout.CENTER);

    mFinderPanel = new FinderPanel();

    mFinderPanel.setDateListener(this);
    dateChanged(new devplugin.Date(), null, null);

    skinPanel.add(centerPanel, BorderLayout.CENTER);

    mChannelChooser = new ChannelChooserPanel(this);

    /* create structure */
    mRootNode = new Node(null);
    mPluginsNode = new Node(mRootNode);
    mMainframeNode = new Node(mRootNode);
    Node programtableNode = new Node(mMainframeNode);
    mNavigationNode = new Node(mMainframeNode);
    mTimebuttonsNode = new Node(mNavigationNode);
    mDateChannelNode = new Node(mNavigationNode);
    mDateNode = new Node(mDateChannelNode);
    mChannelNode = new Node(mDateChannelNode);

    mRootNode.setProperty(Settings.propViewRoot);
    mMainframeNode.setProperty(Settings.propViewMainframe);
    mNavigationNode.setProperty(Settings.propViewNavigation);
    mDateChannelNode.setProperty(Settings.propViewDateChannel);

    /* create views */
    programtableNode.setLeaf(skinPanel);
    this.setShowPluginOverview(Settings.propShowPluginView.getBoolean());
    this.setShowTimeButtons(Settings.propShowTimeButtons.getBoolean());
    this.setShowDatelist(Settings.propShowDatelist.getBoolean());
    this.setShowChannellist(Settings.propShowChannels.getBoolean());

    updateToolbar();

    mCenterComponent = mRootNode.getComponent();
    if (mCenterComponent != null) {
      jcontentPane.add(mCenterComponent, BorderLayout.CENTER);
    }

    if (Settings.propIsStatusbarVisible.getBoolean())
      jcontentPane.add(mStatusBar, BorderLayout.SOUTH);
    PluginProxyManager.getInstance().addPluginStateListener(new PluginStateAdapter() {
      public void pluginActivated(Plugin p) {
        updatePluginsMenu();
      }

      public void pluginDeactivated(Plugin p) {
        updatePluginsMenu();
      }
    });

    setJMenuBar(mMenuBar);

    FilterList filterList = FilterList.getInstance();

    ProgramFilter filter = filterList.getFilterByName(Settings.propLastUsedFilter.getString());

    if (filter == null) {
      filter = filterList.getDefaultFilter();
    }

    setProgramFilter(filter);

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        scrollToNow();
      }

    });

    Timer timer = new Timer(10000, new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        handleTimerEvent();
      }
    });
    timer.start();


  }

  public JLabel getStatusBarLabel() {
    return mStatusBar.getLabel();
  }

  public void updateToolbar() {
    JPanel contentPane = (JPanel) getContentPane();

    if (mToolBar != null) {
      contentPane.remove(mToolBar);
    }

    mToolBarModel = DefaultToolBarModel.getInstance();
    mToolBar = new ToolBar(mToolBarModel);
    String location = mToolBar.getToolbarLocation();
    if (Settings.propIsTooolbarVisible.getBoolean()) {
      contentPane.add(mToolBar, location);
    }

    contentPane.invalidate();
    contentPane.updateUI();

  }

  public ProgramTableScrollPane getProgramTableScrollPane() {
    return mProgramTableScrollPane;
  }

  public ToolBar getToolbar() {
    return mToolBar;
  }

  public DefaultProgramTableModel getProgramTableModel() {
    return mProgramTableModel;
  }

  public static MainFrame getInstance() {
    if (mSingleton == null) {
      mSingleton = new MainFrame();
    }
    return mSingleton;
  }

  public boolean isShowAllFilterActivated() {
    return mProgramTableModel.getProgramFilter() instanceof ShowAllFilter;
  }

  public void setProgramFilter(ProgramFilter filter) {
    mProgramTableModel.setProgramFilter(filter);
    mMenuBar.updateFiltersMenu();
    mToolBarModel.setFilterButtonSelected(!isShowAllFilterActivated());
    mFilterPanel.setCurrentFilter(filter);
    mFilterPanel.setVisible(!isShowAllFilterActivated());
    
    mToolBar.update();
  }

  public ProgramFilter getProgramFilter() {
    if (mProgramTableModel == null) {
      return null;
    }

    return mProgramTableModel.getProgramFilter();
  }

  public void quit() {
    mLog.info("Finishing plugins");
    PluginProxyManager.getInstance().shutdownAllPlugins();

    mLog.info("Storing dataservice settings");
    TvDataServiceManager.getInstance().finalizeDataServices();

    TVBrowser.shutdown();

    mLog.info("Closing tv data base");
    try {
      TvDataBase.getInstance().close();
    } catch (Exception exc) {
      mLog.log(Level.WARNING, "Closing database failed", exc);
    }

    mLog.info("Quitting");
    System.exit(0);
  }


  private void handleTimerEvent() {
    Date date=Date.getCurrentDate();
    if (date.equals(mCurrentDay)) {
      return;
    }
    mCurrentDay = date;
    if (mFinderPanel != null) {
      mFinderPanel.updateContent();
    }
    if (mPluginView != null) {
      mPluginView.update();
    }
  }

  public void updatePluginsMenu() {
    mMenuBar.updatePluginsMenu();
  }

  public void scrollToProgram(Program program) {
    scrollTo(program.getDate(), program.getHours());
    mProgramTableScrollPane.scrollToChannel(program.getChannel());
  }

  public void scrollToTime(int time) {
    mProgramTableScrollPane.scrollToTime(time);
  }

  public void scrollToNow() {
    Calendar cal = Calendar.getInstance();
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    devplugin.Date day = new devplugin.Date();
    scrollTo(day, hour);
  }

  private void scrollTo(Date day, int hour) {

    // Choose the day.
    // NOTE: If its early in the morning before the setted "day start" we should
    // stay at the last day - otherwise the user won't see the current
    // program. But until when should we stay at the old day?
    // Example: day start: 0:00, day end: 6:00
    // Directly after the day start is not a good choice, because the new
    // day program table will not contain programs that started before 0:00.
    // Directly after the day end is also not a good choice, because a
    // minute before the old day program will not contain the coming programs.
    // So I think the best choice will be the middle, in this case 3:00.
    // If the day start is later as the day end, then the table have to show the
    // old date until the day end is reached.

    int dayStart = Settings.propProgramTableStartOfDay.getInt();
    int dayEnd = Settings.propProgramTableEndOfDay.getInt();
    int splitHour = (dayEnd - dayStart) / 60;
    if (hour < (splitHour/2 + splitHour%2) || (splitHour <= 0 && hour < dayEnd/60)
        || (hour < dayStart/60 && dayStart < dayEnd)) {
      // It's early in the morning -> use the program table of yesterday
      day = day.addDays(-1);
      hour += 24;
    }

    // Change to the shown day program to today if nessesary
    // and scroll to "now" afterwards
    final int fHour = hour;
    mFinderPanel.markDate(day, new Runnable() {
      public void run() {
        // Scroll to now
        mProgramTableScrollPane.scrollToTime(fHour * 60);
      }
    });
  }

  public void runSetupAssistant() {

    ProgressWindow progWin = new ProgressWindow(this, mLocalizer.msg("loadingAssistant", ""));
    final JFrame parent = this;
    progWin.run(new Progress() {
      public void run() {
        mConfigAssistantDialog = new tvbrowser.ui.configassistant.ConfigAssistant(parent);
      }
    });

    util.ui.UiUtilities.centerAndShow(mConfigAssistantDialog);
    mConfigAssistantDialog.setVisible(false);
    mConfigAssistantDialog.dispose();
    mConfigAssistantDialog = null;

    boolean dataAvailable = TvDataBase.getInstance().dataAvailable(new Date());
    if (!dataAvailable) {
      askForDataUpdate();
    }

    Settings.handleChangedSettings();

  }

//  public void runTvBrowserUpdateAssistant() {
//    TvBrowserUpdateAssistant dlg = new TvBrowserUpdateAssistant(this);
//    UiUtilities.centerAndShow(dlg);
//  }

  public void storeSettings() {
    mToolBarModel.store();
    mToolBar.storeSettings();
    mRootNode.storeProperties();
    Settings.propLastUsedFilter.setString(getProgramFilter().getName());
  }

  private void onDownloadStart() {
    mToolBarModel.showStopButton();    
    mToolBar.update();
    mMenuBar.showStopMenuItem();
  }

  private void onDownloadDone() {
    TvDataUpdater.getInstance().stopDownload();
    mStatusBar.getProgressBar().setValue(0);

    mToolBarModel.showUpdateButton();
    mToolBar.update();
    mMenuBar.showUpdateMenuItem();

    mFinderPanel.updateUI();
    Settings.propLastDownloadDate.setDate(Date.getCurrentDate());

  }

  public void showChannel(Channel ch) {
    mProgramTableScrollPane.scrollToChannel(ch);
  }

  /**
   * Updates the program table and the finder panel.
   * <p>
   * Called when new TV listings was downloaded or when TV data was imported.
   */
  private void newTvDataAvailable() {
    changeDate(mFinderPanel.getSelectedDate(), null, null);
    mMenuBar.updateDateItems();
  }

  public void goTo(Date date) {
    mFinderPanel.markDate(date);
  }

  public void goToNextDay() {
    mFinderPanel.markNextDate();
  }

  public void goToPreviousDay() {
    mFinderPanel.markPreviousDate();
  }


  public Date getCurrentSelectedDate() {
    return mFinderPanel.getSelectedDate();
  }

  private void changeDate(final Date date, final ProgressMonitor monitor, final Runnable callback) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mProgramTableModel.setDate(date, monitor, callback);
      }
    });
  }

  /**
   * Implementation of Interface DateListener
   */
  public void dateChanged(final devplugin.Date date, devplugin.ProgressMonitor monitor, Runnable callback) {
    changeDate(date, monitor, callback);
    super.setTitle(TVBrowser.MAINWINDOW_TITLE + " - " + date.getLongDateString());
  }

  public void runUpdateThread(final int daysToDownload, final TvDataService[] services) {

    downloadingThread = new Thread() {
      public void run() {
        onDownloadStart();
        JProgressBar progressBar = mStatusBar.getProgressBar();
        TvDataUpdater.getInstance().downloadTvData(daysToDownload, services, progressBar, mStatusBar.getLabel());

        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            onDownloadDone();
            newTvDataAvailable();
          }

        });

      }
    };
    downloadingThread.start();
  }

  public void updateChannellist() {
    updateChannelChooser();
    mMenuBar.updateChannelItems();
  }

  public void updateChannelChooser() {
    mChannelChooser.updateChannelChooser();
  }

  /**
   * Starts the TV listings update.
   */
  public void updateTvData() {
    if (TvDataUpdater.getInstance().isDownloading()) {
      TvDataUpdater.getInstance().stopDownload();
    } else {
      UpdateDlg dlg = new UpdateDlg(this, true);
      dlg.pack();
      UiUtilities.centerAndShow(dlg);
      int daysToDownload = dlg.getResult();
      if (daysToDownload != UpdateDlg.CANCEL) {
        runUpdateThread(daysToDownload, dlg.getSelectedTvDataServices());
      }
    }
  }

  /**
   * Shows the settings dialog.
   */
  public void showSettingsDialog() {
    showSettingsDialog("#channels");
  }

  public void showSettingsDialog(String visibleTabId) {
    SettingsDialog dlg = new SettingsDialog(this, visibleTabId);
    dlg.centerAndShow();
    Settings.handleChangedSettings();
  }

  /**
   * Shows the about box
   */
  public void showAboutBox() {
    AboutBox box = new AboutBox(this);
    box.setSize(500, 520);
    UiUtilities.centerAndShow(box);
    box.dispose();
  }

  public void showUpdatePluginsDlg() {
    Object[] options = { mLocalizer.msg("checknow", "Check now"), mLocalizer.msg("cancel", "Cancel") };
    String msg = mLocalizer.msg("question.1", "do you want to check for new plugins");
    int answer = JOptionPane.showOptionDialog(this, msg, mLocalizer.msg("title.1", "update plugins"),
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

    if (answer == JOptionPane.YES_OPTION) {

      ProgressWindow progWin = new ProgressWindow(this, mLocalizer.msg("title.2", "searching for new plugins..."));

      progWin.run(new Progress() {
        public void run() {
          try {
            java.net.URL url = null;
            url = new java.net.URL("http://www.tvbrowser.org/plugins/plugins.txt");
            SoftwareUpdater softwareUpdater = new SoftwareUpdater(url);
            mSoftwareUpdateItems = softwareUpdater.getAvailableSoftwareUpdateItems();
          } catch (java.io.IOException e) {
            e.printStackTrace();
          }
        }
      });

      if (mSoftwareUpdateItems == null) {
        JOptionPane.showMessageDialog(this, mLocalizer.msg("error.1", "software check failed."));
      } else if (mSoftwareUpdateItems.length == 0) {
        JOptionPane.showMessageDialog(this, mLocalizer.msg("error.2", "No new items available"));
      } else {
        SoftwareUpdateDlg dlg = new SoftwareUpdateDlg(this);
        dlg.setSoftwareUpdateItems(mSoftwareUpdateItems);
        UiUtilities.centerAndShow(dlg);
      }
    }

  }
  
  public void showFromTray(int state) {   
    super.show();
    toFront();
    setExtendedState(state);
    mIsVisible = true;
  }

  public void show() {
    super.show();
    mIsVisible = true;
    mRootNode.update();
  }

  public void askForDataUpdate() {

    String msg1 = mLocalizer.msg("askforupdatedlg.1", "update now");
    String msg2 = mLocalizer.msg("askforupdatedlg.2", "later");
    String msg3 = mLocalizer.msg("askforupdatedlg.3", "No tv data for todays program available.");
    String msg4 = mLocalizer.msg("askforupdatedlg.4", "Do you want to update now?");
    String msg5 = mLocalizer.msg("askforupdatedlg.5", "Update tv data");

    Object[] options = { msg1, msg2 };
    int result = JOptionPane.showOptionDialog(this, msg3 + "\n\n" + msg4, msg5, JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

    if (result == JOptionPane.YES_OPTION) {
      updateTvData();
    }
  }

  public void showFilterDialog() {
    SelectFilterDlg dlg = new SelectFilterDlg(this);
    util.ui.UiUtilities.centerAndShow(dlg);
    mMenuBar.updateFiltersMenu();
  }

  public void showHelpDialog() {

    Locale locale = Locale.getDefault();
    String language = locale.getLanguage();

    java.io.File indexFile = new java.io.File("help/" + language + "/index.html");
    if (!indexFile.exists()) {
      indexFile = new java.io.File("help/default/index.html");
    }
    util.ui.BrowserLauncher.openURL(indexFile.getAbsolutePath());
  }

  /**
   * Updates the TimeChooser-Buttons
   */
  public void updateButtons() {
    mMenuBar.updateTimeItems();
    if (mTimebuttonsNode.getLeaf() != null) {
      ((TimeChooserPanel)mTimebuttonsNode.getLeaf()).updateButtons();
    }
  }




  public void setShowToolbar(boolean visible) {
    Settings.propIsTooolbarVisible.setBoolean(visible);
    updateToolbar();
  }

  private void updateViews() {
    if (mIsVisible) {
      jcontentPane = (JPanel) getContentPane();
      jcontentPane.remove(mCenterComponent);
      mCenterComponent = mRootNode.getComponent();
      jcontentPane.add(mCenterComponent, BorderLayout.CENTER);
      jcontentPane.validate();
      jcontentPane.requestFocus();
    }
  }

  public void setShowTimeButtons(boolean visible) {
    if (visible) {
      mTimebuttonsNode.setLeaf(new TimeChooserPanel(this));
    } else {
      mTimebuttonsNode.setLeaf(null);
    }
    Settings.propShowTimeButtons.setBoolean(visible);
    updateViews();

  }

  public void setShowDatelist(boolean visible) {
    if (visible) {
      mDateNode.setLeaf(new DateChooserPanel(this, mFinderPanel));
    } else {
      mDateNode.setLeaf(null);
    }
    Settings.propShowDatelist.setBoolean(visible);
    updateViews();
  }

  public void setShowChannellist(boolean visible) {
    if (visible) {
      mChannelNode.setLeaf(mChannelChooser);
    } else {
      mChannelNode.setLeaf(null);
    }
    Settings.propShowChannels.setBoolean(visible);
    updateViews();
  }

  /*
   * public void setPluginViewToolbarButtonSelected(boolean selected) {
   * mToolBar.setPluginViewToolbarButtonSelected(selected); }
   */

  public void setPluginViewButton(boolean selected) {
    if (mToolBarModel != null) {
      mToolBarModel.setPluginViewButtonSelected(selected);
      mToolBar.update();
    }
  }

  public void setShowPluginOverview(boolean visible) {
    if (visible) {
      mPluginView = new PluginView();
    } else {
      mPluginView = null;
    }
    mPluginsNode.setLeaf(mPluginView);
    mMenuBar.setPluginViewItemChecked(visible);
    Settings.propShowPluginView.setBoolean(visible);
    
    updateViews();
  }

  public void restoreViews() {
    mRootNode.setProperty(Settings.propViewRoot.getDefault());
    mMainframeNode.setProperty(Settings.propViewMainframe.getDefault());
    mNavigationNode.setProperty(Settings.propViewNavigation.getDefault());
    mDateChannelNode.setProperty(Settings.propViewDateChannel.getDefault());
    mRootNode.update();
  }

  /**
   * Makes the StatusBar visible
   * @param visible true if Statusbar should be visible
   */
  public void setShowStatusbar(boolean visible) {
    JPanel contentPane = (JPanel) getContentPane();

    Settings.propIsStatusbarVisible.setBoolean(visible);

    if (visible && !contentPane.isAncestorOf(mStatusBar)) {
      jcontentPane.add(mStatusBar, BorderLayout.SOUTH);
    }
    else if (contentPane.isAncestorOf(mStatusBar)) {
      jcontentPane.remove(mStatusBar);
    }

    contentPane.invalidate();
    contentPane.updateUI();
  
  }

}