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

import java.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import java.util.logging.*;

import tvbrowser.core.*;
import tvbrowser.ui.programtable.ProgramTableScrollPane;
import tvbrowser.ui.programtable.DefaultProgramTableModel;
import tvbrowser.ui.filter.FilterChooser;
import tvbrowser.ui.filter.FilterComponentList;
import tvbrowser.ui.finder.FinderPanel;
import tvbrowser.ui.SkinPanel;
import tvbrowser.ui.UpdateDlg;
import tvbrowser.ui.settings.SettingsDialog;
import tvbrowser.ui.splashscreen.SplashScreen;
import tvbrowser.ui.update.*;
import tvbrowser.ui.aboutbox.AboutBox;
import tvbrowser.ui.ButtonPanel;
import tvbrowser.ui.PictureButton;


import util.exc.*;
import util.ui.*;

import tvdataservice.TvDataService;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class TVBrowser extends JFrame implements ActionListener, DateListener {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(TVBrowser.class.getName());

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(TVBrowser.class);

  private static final String EXPORTED_TV_DATA_EXTENSION = ".tv.zip";

  private JButton mNowBt, mEarlyBt, mMorningBt, mMiddayBt, mEveningBt,
    updateBtn, settingsBtn;

  private ProgramTableScrollPane mProgramTableScrollPane;
  private DefaultProgramTableModel mProgramTableModel;

  private JPanel mNorthPanel;

  private Thread downloadingThread;
  private JPanel jcontentPane;
  private FinderPanel finderPanel;
  private JMenuItem settingsMenuItem, quitMenuItem, updateMenuItem,
    mImportTvDataMI, mExportTvDataMI, aboutMenuItem, helpMenuItem, mPluginDownloadMenuItem;
  private SkinPanel skinPanel;
  private JPanel/*ButtonPanel*/ mButtonPanel;
  private static String curLookAndFeel;
  public static final devplugin.Version VERSION=new devplugin.Version(0,96,false,"0.9.6");
  public static final String MAINWINDOW_TITLE="TV-Browser v"+VERSION.toString();
  
  private static TVBrowser mainFrame;

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
    createChannelList();
    
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
    
    try {
      curLookAndFeel = Settings.getLookAndFeel();
      UIManager.setLookAndFeel(curLookAndFeel);
    }
    catch (Exception exc) {
      msg = mLocalizer.msg("error.1", "Unable to set look and feel.");
      ErrorHandler.handle(msg, exc);
    }
    
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
    
    //tvbrowser.ui.filter.FilterList.load();

    mLog.info("Starting up...");
    msg = mLocalizer.msg("splash.ui", "Starting up...");
    splash.setMessage(msg);
    

    //System.out.println("current date: "+devplugin.Date.getCurrentDate().toString());
    

    mainFrame=new TVBrowser();
    
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
          askForDataUpdate();
        } else {
          mainFrame.scrollToNow();
        }
        
        DataService.getInstance().setOnlineMode(Settings.getStartupInOnlineMode());
      }
    });
  }



  public TVBrowser() {
    super(MAINWINDOW_TITLE);

    String msg;
    Icon icon;

    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);
    
 
    // TV-Browser menu
    JMenu mainMenu = new JMenu(mLocalizer.msg("menu.main", "TV-Browser"));
    mainMenu.setMnemonic(KeyEvent.VK_B);
    menuBar.add(mainMenu);
    
    icon = new ImageIcon("imgs/Preferences16.gif");
    msg = mLocalizer.msg("menuitem.settings", "Settings...");
    settingsMenuItem = new JMenuItem(msg, icon);
    settingsMenuItem.setMnemonic(KeyEvent.VK_S);
	settingsMenuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    settingsMenuItem.addActionListener(this);
    mainMenu.add(settingsMenuItem);
    
    mainMenu.addSeparator();

    msg = mLocalizer.msg("menuitem.exit", "Exit...");
    quitMenuItem = new JMenuItem(msg);
	quitMenuItem.setMnemonic(KeyEvent.VK_E);
    quitMenuItem.addActionListener(this);
    
	quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(
			KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
    
    mainMenu.add(quitMenuItem);
    
    // TV data menu
    JMenu tvDataMenu = new JMenu(mLocalizer.msg("menu.tvData", "TV data"));
    tvDataMenu.setMnemonic(KeyEvent.VK_D);
    menuBar.add(tvDataMenu);

    icon = new ImageIcon("imgs/Refresh16.gif");
    msg = mLocalizer.msg("menuitem.update", "Update...");
    updateMenuItem = new JMenuItem(msg, icon);
    updateMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));
 
    updateMenuItem.setMnemonic(KeyEvent.VK_U);
    updateMenuItem.addActionListener(this);
    tvDataMenu.add(updateMenuItem);

    tvDataMenu.addSeparator();

    icon = new ImageIcon("imgs/Import16.gif");
    msg = mLocalizer.msg("menuitem.import", "Import...");
    mImportTvDataMI = new JMenuItem(msg, icon);
	mImportTvDataMI.setMnemonic(KeyEvent.VK_I);
    mImportTvDataMI.addActionListener(this);
    tvDataMenu.add(mImportTvDataMI);

    icon = new ImageIcon("imgs/Export16.gif");
    msg = mLocalizer.msg("menuitem.export", "Export...");
    mExportTvDataMI = new JMenuItem(msg, icon);
	mExportTvDataMI.setMnemonic(KeyEvent.VK_E);
    mExportTvDataMI.addActionListener(this);
    tvDataMenu.add(mExportTvDataMI);
    
    // Plugins menu
    mPluginsMenu = new JMenu(mLocalizer.msg("menu.plugins", "Plugins"));
	mPluginsMenu.setMnemonic(KeyEvent.VK_P);
    menuBar.add(mPluginsMenu);
    
    

    //mPluginsMenu.addSeparator();
    
    icon = new ImageIcon("imgs/Search16.gif");
    msg = mLocalizer.msg("menuitem.findPluginsOnWeb", "Find plugins on the web...");
    mPluginDownloadMenuItem = new JMenuItem(msg, icon);
    mPluginDownloadMenuItem.addActionListener(this);
	createPluginsMenu();
   
    
    // Help menu
    JMenu helpMenu = new JMenu(mLocalizer.msg("menu.help", "Help"));
	helpMenu.setMnemonic(KeyEvent.VK_H);
    menuBar.add(helpMenu);

    icon = new ImageIcon("imgs/Help16.gif");
    msg = mLocalizer.msg("menuitem.help", "Help...");    
    helpMenuItem = new JMenuItem(msg, icon);
	helpMenuItem.setMnemonic(KeyEvent.VK_H);
	helpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
 
    helpMenuItem.addActionListener(this);
    helpMenu.add(helpMenuItem);
    
    helpMenu.addSeparator();
    
    icon = new ImageIcon("imgs/About16.gif");
    msg = mLocalizer.msg("menuitem.about", "About...");
    aboutMenuItem = new JMenuItem(msg, icon);
	aboutMenuItem.setMnemonic(KeyEvent.VK_B);
    aboutMenuItem.addActionListener(this);
    helpMenu.add(aboutMenuItem);
    
    // create content
    jcontentPane = (JPanel)getContentPane();
    jcontentPane.setLayout(new BorderLayout());

    int mode;
    if (Settings.useApplicationSkin()) {
      mode = SkinPanel.WALLPAPER;
    } else {
      mode = SkinPanel.NONE;
    }

    skinPanel = new SkinPanel(Settings.getApplicationSkin(),mode);
    skinPanel.setLayout(new BorderLayout());


    mNorthPanel = new JPanel(new BorderLayout());
    mNorthPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    mNorthPanel.setOpaque(false);

    JPanel eastPanel = new JPanel(new BorderLayout(0,5));

    eastPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

    mButtonPanel=createButtonPanel();
    
    mNorthPanel.add(mButtonPanel,BorderLayout.WEST);

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setOpaque(false);
    centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    mProgramTableModel = new DefaultProgramTableModel(ChannelList.getSubscribedChannels(),Settings.getProgramTableStartOfDay(),Settings.getProgramTableEndOfDay());
    mProgramTableScrollPane = new ProgramTableScrollPane(mProgramTableModel);
    centerPanel.add(mProgramTableScrollPane);

    finderPanel = new FinderPanel(this);
    
    String[] items = {
      mLocalizer.msg("offlineMode", "Offline mode"),
      mLocalizer.msg("onlineMode", "Online mode")
    };
    final JComboBox comboBox = new JComboBox(items);
    
    if (Settings.getStartupInOnlineMode()) {
    	comboBox.setSelectedIndex(1);
    }

    
    eastPanel.add(new FilterChooser(mainFrame,mProgramTableModel),BorderLayout.NORTH);
    eastPanel.add(finderPanel,BorderLayout.CENTER);

    JPanel panel1 = new JPanel();
    panel1.setOpaque(false);
    panel1.setLayout(new BoxLayout(panel1,BoxLayout.Y_AXIS));
    panel1.add(comboBox);

    panel1.add(DataService.getInstance().getProgressBar());

    eastPanel.setOpaque(false);

    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (comboBox.getSelectedIndex() == 0) {
          DataService.getInstance().setOnlineMode(false);
        }else if (comboBox.getSelectedIndex() == 1) {
          DataService.getInstance().setOnlineMode(true);
        }
      }
    }
    );

    eastPanel.add(panel1,BorderLayout.SOUTH);

    skinPanel.add(mNorthPanel,BorderLayout.NORTH);
    skinPanel.add(eastPanel,BorderLayout.EAST);
    skinPanel.add(centerPanel, BorderLayout.CENTER);

    jcontentPane.add(skinPanel,BorderLayout.CENTER);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        quit();
      }
    });
  }



  private void quit() {
    mLog.info("Storing plugin data");
    PluginManager.finalizeInstalledPlugins();
    
    mLog.info("Storing filter components");
    FilterComponentList.store();

    mLog.info("Storing dataservice settings");
    TvDataServiceManager.getInstance().finalizeDataServices();
    
    mLog.info("Storing window size and location");
    boolean maximized = mainFrame.getExtendedState() == JFrame.MAXIMIZED_BOTH;
    Settings.setWindowIsMaximized(maximized);
    if (! maximized) {
      // Save the window size and location only when not maximized
      Settings.setWindowSize(mainFrame.getSize());
      Settings.setWindowLocation(mainFrame.getLocation());
    }
    
    try {
    	Settings.storeSettings();
    } catch (TvBrowserException e) {
      ErrorHandler.handle(e);
    }

    mLog.info("Quitting");
    System.exit(0);
  }



  private static void createChannelList() {
  	TvDataService[] dataServiceArr
      = TvDataServiceManager.getInstance().getDataServices();

  	for (int i=0;i<dataServiceArr.length;i++) {
      ChannelList.addDataServiceChannels(dataServiceArr[i]);
  	}
  }

  private JPanel createButtonPanel() {
	ButtonPanel result=new ButtonPanel();
	result.setTimeButtons(createTimeBtns());
	result.setUpdateButton(createUpdateBtn());
	result.setPreferencesButton(createPreferencesBtn());
	result.setPluginButtons();
	result.update();
	return result;
  }
  
  private void createPluginsMenu() {
    mPluginsMenu.removeAll();

    Object[] plugins = PluginManager.getInstalledPlugins();
    JMenuItem item;
    HashMap map = new HashMap();
    for (int i = 0;i<plugins.length;i++) {
      final devplugin.Plugin plugin = (devplugin.Plugin)plugins[i];
      plugin.setParent(this);
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
            mLog.info("Plugin menu item pressed");
            plugin.execute();
          }
        });
        mPluginsMenu.add(item);
      }
    }
    
    mPluginsMenu.addSeparator();
    mPluginsMenu.add(mPluginDownloadMenuItem);
        
  }



  private void scrollToNow() {
    // Scroll to now
    Calendar cal = Calendar.getInstance();
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    mProgramTableScrollPane.scrollToTime(hour * 60);
  }



  public void actionPerformed(ActionEvent event) {
    Object src = event.getSource();

    if (src == mNowBt) {
      // Change to the shown day program to today if nessesary
      devplugin.Date today = new devplugin.Date();
      if (! today.equals(finderPanel.getSelectedDate())) {
        finderPanel.markDate(today);
      }

      scrollToNow();
    }
    else if (src == mEarlyBt) {
      mProgramTableScrollPane.scrollToTime(Settings.getEarlyTime());
    }
    else if (src == mMorningBt) {
      mProgramTableScrollPane.scrollToTime(Settings.getMorningTime());
    }
    else if (src == mMiddayBt) {
      mProgramTableScrollPane.scrollToTime(Settings.getMiddayTime());
    }
    else if (src == mEveningBt) {
      mProgramTableScrollPane.scrollToTime(Settings.getEveningTime());
    }
    else if (src == quitMenuItem) {
      quit(); 
    }
    else if (src == mImportTvDataMI) {
      importTvData();
    }
    else if (src == mExportTvDataMI) {
      exportTvData();
    }
    else if ((src == updateBtn) || (src == updateMenuItem)) {
      updateTvData();
    }
    else if ((src == settingsMenuItem) || (src == settingsBtn)) {
      showSettingsDialog();
    }
    else if (src==aboutMenuItem) {
      showAboutBox();
    }
    else if (src==helpMenuItem) {
      showHelpDialog();
    }
    else if (src==mPluginDownloadMenuItem) {
    	showUpdatePluginsDlg();
    }
  }



  private void onDownloadStart() {
    updateBtn.setText(mLocalizer.msg("button.stop", "Stop"));
    updateBtn.setIcon(new ImageIcon("imgs/Stop24.gif"));
    updateMenuItem.setText(mLocalizer.msg("menuitem.stopUpdate", "Stop update..."));
  }



  private void onDownloadDone() {
  	DataService.getInstance().stopDownload();
    DataService.getInstance().getProgressBar().setValue(0);
    updateBtn.setText(mLocalizer.msg("button.update", "Update"));
    updateBtn.setIcon(new ImageIcon("imgs/Refresh24.gif"));
    updateMenuItem.setText(mLocalizer.msg("menuitem.update", "Update..."));

    //newTvDataAvailable();
  }



  /**
   * Updates the program table and the finder panel.
   * <p>
   * Called when new TV data was downloaded or when TV data was imported.
   */
  private void newTvDataAvailable() {
    changeDate(finderPanel.getSelectedDate());
  }



  private JButton[] createTimeBtns() {
  	String msg;
  	JButton[] result=new JButton[5];
	msg = mLocalizer.msg("button.now", "Now");
	mNowBt = new PictureButton(msg, new ImageIcon("imgs/TimeNow24.gif"));
	mNowBt.addActionListener(this);
	result[0]=mNowBt;

	msg = mLocalizer.msg("button.early", "Early");
	mEarlyBt = new PictureButton(msg, new ImageIcon("imgs/TimeEarly24.gif"));
	mEarlyBt.addActionListener(this);
	result[1]=mEarlyBt;

	msg = mLocalizer.msg("button.morning", "Morning");
	mMorningBt = new PictureButton(msg, new ImageIcon("imgs/TimeMorning24.gif"));
	mMorningBt.addActionListener(this);
	result[2]=mMorningBt;

	msg = mLocalizer.msg("button.midday", "Midday");
	mMiddayBt = new PictureButton(msg, new ImageIcon("imgs/TimeMidday24.gif"));
	mMiddayBt.addActionListener(this);
	result[3]=mMiddayBt;

	msg = mLocalizer.msg("button.evening", "Evening");
	mEveningBt = new PictureButton(msg, new ImageIcon("imgs/TimeEvening24.gif"));
	mEveningBt.addActionListener(this);
	result[4]=mEveningBt;

	return result;
  }


  private JButton createUpdateBtn() {
    String msg = mLocalizer.msg("button.update", "Update");
    updateBtn = new PictureButton(msg, new ImageIcon("imgs/Refresh24.gif"));
    updateBtn.addActionListener(this);
    return updateBtn;
  }

  private JButton createPreferencesBtn() {
    String msg = mLocalizer.msg("button.settings", "Settings");
    settingsBtn = new PictureButton(msg, new ImageIcon("imgs/Preferences24.gif"));
    settingsBtn.addActionListener(this);
    return settingsBtn;
  }



  private void changeDate(devplugin.Date date) {    	
	//devplugin.Date nextDate = new devplugin.Date(date.getDaysSince1970() + 1);
    
    devplugin.Date nextDate=new devplugin.Date(date);
    nextDate=nextDate.addDays(1);
   // devplugin.Date nextDate=date.addDays(1);
    DataService.getInstance().getProgressBar().setMaximum(100);
    DayProgram today = DataService.getInstance().getDayProgram(date, true, 0, 49);
    DayProgram tomorrow = DataService.getInstance().getDayProgram(nextDate, true, 50, 99);
    mProgramTableModel.setDayPrograms(today, tomorrow);

    if (finderPanel != null) {
      finderPanel.update();
    }
    if (date.equals(new devplugin.Date())) {
      // If this is today -> scroll to now
      scrollToNow();
    }
	DataService.getInstance().getProgressBar().setValue(0);
	
  }



  /**
   * Implementation of Interface DateListener
   */
  public void dateChanged(final devplugin.Date date) {
  	if (DataService.getInstance().isOnlineMode()) {
      downloadingThread = new Thread() {
        public void run() {
          onDownloadStart();
          changeDate(date);
          onDownloadDone();
        }
      };
      downloadingThread.start();
    }
    else {
      changeDate(date);
    }
  }



  public void updateLookAndFeel() {
    if (curLookAndFeel == null || !curLookAndFeel.equals(Settings.getLookAndFeel())) {
      try {
        curLookAndFeel = Settings.getLookAndFeel();
        UIManager.setLookAndFeel(curLookAndFeel);
        SwingUtilities.updateComponentTreeUI(this);
        validate();
      }
      catch (Exception exc) {
        String msg = mLocalizer.msg("error.1", "Unable to set look and feel.", exc);
        ErrorHandler.handle(msg, exc);
      }
    }

  }



  public void updateApplicationSkin() {
    int mode;
    if (Settings.useApplicationSkin()) {
      mode = SkinPanel.WALLPAPER;
    }else {
      mode = SkinPanel.NONE;
    }

    skinPanel.update(Settings.getApplicationSkin(),mode);

  }


  private void importTvData() {
    JFileChooser chooser = new JFileChooser();

    String msg;

    File defaultFile = new File("tvdata" + EXPORTED_TV_DATA_EXTENSION);
	//File defaultFile = new File(Settings.getTVDataDirectory() + EXPORTED_TV_DATA_EXTENSION);
    
    chooser.setSelectedFile(defaultFile);
    msg = mLocalizer.msg("importDlgTitle", "Import TV data");
    chooser.setDialogTitle(msg);
    msg = mLocalizer.msg("tvDataFilter", "TV data ({0})",
      "*" + EXPORTED_TV_DATA_EXTENSION);
    chooser.addChoosableFileFilter(new ExtensionFileFilter(EXPORTED_TV_DATA_EXTENSION, msg));

    chooser.showOpenDialog(this);

    File targetFile = chooser.getSelectedFile();
    if ((targetFile != null) && (targetFile.exists())) {
      try {
        DataService.getInstance().importTvData(targetFile);

        newTvDataAvailable();
      }
      catch (TvBrowserException exc) {
        ErrorHandler.handle(exc);
      }
    }
  }



  private void exportTvData() {
    JFileChooser chooser = new JFileChooser();

    String msg;

    File defaultFile = new File("tvdata" + EXPORTED_TV_DATA_EXTENSION);
    chooser.setSelectedFile(defaultFile);
    msg = mLocalizer.msg("exportDlgTitle", "Export TV data");
    chooser.setDialogTitle(msg);
    msg = mLocalizer.msg("tvDataFilter", "TV data ({0})",
      "*" + EXPORTED_TV_DATA_EXTENSION);
    chooser.addChoosableFileFilter(new ExtensionFileFilter(EXPORTED_TV_DATA_EXTENSION, msg));

    chooser.showSaveDialog(this);

    File targetFile = chooser.getSelectedFile();
    if (targetFile != null) {
      try {
        DataService.getInstance().exportTvData(targetFile);
      }
      catch (TvBrowserException exc) {
        ErrorHandler.handle(exc);
      }
    }
  }

  private void runUpdateThread(final int daysToDownload) {
  	
	if (daysToDownload != UpdateDlg.CANCEL) {
		final JFrame parent = this;
		downloadingThread = new Thread() {
			public void run() {
				onDownloadStart();
				DataService.getInstance().startDownload(daysToDownload);
				onDownloadDone();
				newTvDataAvailable();
  	
			}
		};
		downloadingThread.start();
	}
  }

  
  
  /**
   * Starts the tv data update.
   */
  private void updateTvData() {
    if (DataService.getInstance().isDownloading()) {
      onDownloadDone();
      newTvDataAvailable();
    } else {
      UpdateDlg dlg = new UpdateDlg(this, true);
      dlg.pack();
      UiUtilities.centerAndShow(dlg);
      runUpdateThread(dlg.getResult());
   
    }
  }



   /**
   * Shows the settings dialog.
   */
  private void showSettingsDialog() {
    SettingsDialog dlg = new SettingsDialog(this);
    dlg.centerAndShow();
    
    
    
    
    if (Settings.settingHasChanged(new String[]{"font.programtitle","font.programinfo","font.programtime","font.channelname","usedefaultfonts"})) {
      util.ui.ProgramPanel.updateFonts();
      tvbrowser.ui.programtable.ChannelPanel.fontChanged();
      mProgramTableScrollPane.getProgramTable().fontChanged();
      mProgramTableScrollPane.tableDataChanged();
      mProgramTableScrollPane.getProgramTable().tableDataChanged();
    }
    if (Settings.settingHasChanged(new String[]{"lookandfeel"})) {
      updateLookAndFeel();
    }
    if (Settings.settingHasChanged(new String[]{"applicationskin","useapplicationskin"})) {
      updateApplicationSkin();
    }
    if (Settings.settingHasChanged(new String[]{"table.layout"})) {
      mProgramTableScrollPane.getProgramTable().setProgramTableLayout(null);
    }
    if (Settings.settingHasChanged(new String[]{"tablebgmode","tablebackground"})) {
      mProgramTableScrollPane.getProgramTable().updateBackground();
    }
    if (Settings.settingHasChanged(new String[]{"plugins"})) {
      createPluginsMenu();
    }
    if (Settings.settingHasChanged(new String[]{"timebutton","updatebutton","preferencesbutton",
    "buttontype","buttonplugins"})) {
      mNorthPanel.remove(mButtonPanel);
      mButtonPanel=createButtonPanel();
      mNorthPanel.add(mButtonPanel,BorderLayout.WEST);
      mNorthPanel.updateUI();
    }
    
    if (Settings.settingHasChanged(new String[]{"subscribedchannels"})) {
      createChannelList();      
      DataService.getInstance().subscribedChannelsChanged();
      mProgramTableModel.setShownChannels(ChannelList.getSubscribedChannels());
    }
    
    if (Settings.settingHasChanged(new String[]{"programtable.endofday","programtable.startofday"})) {
      mProgramTableModel.setTimeRange(Settings.getProgramTableStartOfDay(),Settings.getProgramTableEndOfDay());
    }
    
    if (Settings.settingHasChanged(new String[]{"columnwidth"})) {
      util.ui.ProgramPanel.updateColumnWidth();
      mProgramTableScrollPane.setColumnWidth(Settings.getColumnWidth());
      mProgramTableScrollPane.updateChannelPanel();
      mProgramTableScrollPane.getProgramTable().updateLayout();
    }
  }
  
  
  
  /**
   * Shows the about box
   */
  private void showAboutBox() {
  	AboutBox box=new AboutBox(this);
  	box.pack();
  	UiUtilities.centerAndShow(box);
  	box.dispose();
  }


private void showUpdatePluginsDlg() {
	
	PluginUpdate.updatePlugins(mainFrame);
	
	
	
}

private static void askForDataUpdate() {
	
	String msg1 = mLocalizer.msg("askforupdatedlg.1","update now");
	String msg2 = mLocalizer.msg("askforupdatedlg.2","later");
	String msg3 = mLocalizer.msg("askforupdatedlg.3","No tv data for todays program available.");
	String msg4 = mLocalizer.msg("askforupdatedlg.4","Do you want to update now?");
	String msg5 = mLocalizer.msg("askforupdatedlg.5","Update tv data");
	
	
	
	Object[] options = {msg1,msg2};
	int result = JOptionPane.showOptionDialog(mainFrame,					
		msg3+"\n\n"+
		msg4,
		msg5,
		JOptionPane.YES_NO_OPTION,
		JOptionPane.QUESTION_MESSAGE,
		null,options,options[0]);
		
	if (result==JOptionPane.YES_OPTION) {
		mainFrame.updateTvData();
	}	
}


public void showHelpDialog() {
	String msg = mLocalizer.msg("helproot", "help/default/index.html");
	util.ui.HelpDialog.showHelpPage(mainFrame,msg+"index.html",null);
}



}