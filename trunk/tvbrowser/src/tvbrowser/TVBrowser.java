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
import tvbrowser.ui.programtable.ProgramTablePanel;
import tvbrowser.ui.finder.FinderPanel;
import tvbrowser.ui.SkinPanel;
import tvbrowser.ui.UpdateDlg;
import tvbrowser.ui.settings.SettingsDlg;
import tvbrowser.ui.splashscreen.SplashScreen;
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
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(TVBrowser.class);

  private static final String EXPORTED_TV_DATA_EXTENSION = ".tv.zip";

  private JButton mNowBt, mEarlyBt, mMorningBt, mMiddayBt, mEveningBt,
    updateBtn, settingsBtn;
  private ProgramTablePanel programTablePanel;
  //private JPanel buttonPanel;
  private Thread downloadingThread;
  private JPanel jcontentPane;
  private FinderPanel finderPanel;
  private JMenuItem settingsMenuItem, quitMenuItem, updateMenuItem,
    mImportTvDataMI, mExportTvDataMI, aboutMenuItem;
  private SkinPanel skinPanel;
  private ButtonPanel buttonPanel;
  private static String curLookAndFeel;
  public static final String VERSION="0.9.4";
  public static final String MAINWINDOW_TITLE="TV-Browser v"+VERSION;
  
  private static TVBrowser mainFrame;

  private JMenu pluginsMenu;

  
  
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
    
    mLog.info("Loading tv data service...");
    msg=mLocalizer.msg("splash.dataService","Loading tv data service...");
    TvDataServiceManager.getInstance().initDataServices();
    createChannelList();
    
    Settings.loadSettings();
    
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
    
    mLog.info("Loading plugins...");
    msg = mLocalizer.msg("splash.plugins", "Loading plugins...");
    splash.setMessage(msg);
    PluginManager.initInstalledPlugins();
    
    mLog.info("Deleting expired tv data...");

    DataService.deleteExpiredFiles(Settings.getTVDataLifespan());

    mLog.info("Starting up...");
    msg = mLocalizer.msg("splash.ui", "Starting up...");
    splash.setMessage(msg);
    
    mainFrame=new TVBrowser();
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
    //frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    
    // scroll to now
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mainFrame.scrollToNow();
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
    menuBar.add(mainMenu);
    
    icon = new ImageIcon("imgs/Preferences16.gif");
    msg = mLocalizer.msg("menuitem.settings", "Settings...");
    settingsMenuItem = new JMenuItem(msg, icon);
    settingsMenuItem.addActionListener(this);
    mainMenu.add(settingsMenuItem);
    
    mainMenu.addSeparator();

    msg = mLocalizer.msg("menuitem.exit", "Exit...");
    quitMenuItem = new JMenuItem(msg);
    quitMenuItem.addActionListener(this);
    mainMenu.add(quitMenuItem);
    
    // TV data menu
    JMenu tvDataMenu = new JMenu(mLocalizer.msg("menu.tvData", "TV data"));
    menuBar.add(tvDataMenu);

    icon = new ImageIcon("imgs/Refresh16.gif");
    msg = mLocalizer.msg("menuitem.update", "Update...");
    updateMenuItem = new JMenuItem(msg, icon);
    updateMenuItem.addActionListener(this);
    tvDataMenu.add(updateMenuItem);

    tvDataMenu.addSeparator();

    icon = new ImageIcon("imgs/Import16.gif");
    msg = mLocalizer.msg("menuitem.import", "Import...");
    mImportTvDataMI = new JMenuItem(msg, icon);
    mImportTvDataMI.addActionListener(this);
    tvDataMenu.add(mImportTvDataMI);

    icon = new ImageIcon("imgs/Export16.gif");
    msg = mLocalizer.msg("menuitem.export", "Export...");
    mExportTvDataMI = new JMenuItem(msg, icon);
    mExportTvDataMI.addActionListener(this);
    tvDataMenu.add(mExportTvDataMI);
    
    // Plugins menu
    pluginsMenu = new JMenu(mLocalizer.msg("menu.plugins", "Plugins"));
    menuBar.add(pluginsMenu);
    
    updatePluginMenu(pluginsMenu);

    pluginsMenu.addSeparator();
    
    icon = new ImageIcon("imgs/Search16.gif");
    msg = mLocalizer.msg("menuitem.findPluginsInWeb", "Find plugins in the web...");
    JMenuItem pluginDownloadMenuItem = new JMenuItem(msg, icon);
    pluginDownloadMenuItem.setEnabled(false);
    pluginsMenu.add(pluginDownloadMenuItem);
    
    // Help menu
    JMenu helpMenu = new JMenu(mLocalizer.msg("menu.help", "Help"));
    menuBar.add(helpMenu);

    icon = new ImageIcon("imgs/Help16.gif");
    msg = mLocalizer.msg("menuitem.help", "Help...");
    JMenuItem helpMenuItem = new JMenuItem(msg, icon);
    helpMenuItem.setEnabled(false);
    helpMenu.add(helpMenuItem);
    
    helpMenu.addSeparator();
    
    icon = new ImageIcon("imgs/About16.gif");
    msg = mLocalizer.msg("menuitem.about", "About...");
    aboutMenuItem = new JMenuItem(msg, icon);
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


    JPanel northPanel = new JPanel(new BorderLayout());
    northPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    northPanel.setOpaque(false);

    JPanel eastPanel = new JPanel(new BorderLayout(0,5));

    eastPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

    //buttonPanel = createButtonPanel();
    buttonPanel=new ButtonPanel();
   buttonPanel.setTimButtons(createTimeBtns());
   buttonPanel.setUpdateButton(createUpdateBtn());
   buttonPanel.setPreferencesButton(createPreferencesBtn());
   buttonPanel.update();

   /* if (Settings.isTimeBtnVisible()) {
    	JComponents[]=this.createTimeBtns();

    }
    */

    northPanel.add(buttonPanel,BorderLayout.WEST);

    programTablePanel = new ProgramTablePanel(this);
    programTablePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    finderPanel = new FinderPanel(this);

    String[] items = {
      mLocalizer.msg("offlineMode", "Offline mode"),
      mLocalizer.msg("onlineMode", "Online mode")
    };
    final JComboBox comboBox = new JComboBox(items);
    
    if (Settings.getStartupInOnlineMode()) {
    	comboBox.setSelectedIndex(1);
    }

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

    skinPanel.add(northPanel,BorderLayout.NORTH);
    skinPanel.add(eastPanel,BorderLayout.EAST);
    skinPanel.add(programTablePanel,BorderLayout.CENTER);
    programTablePanel.setOpaque(false);

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

    mLog.info("Storing dataservice settings");
    TvDataServiceManager.getInstance().finalizeDataServices();
    
    mLog.info("Storing window size and location");
    Settings.setWindowSize(mainFrame.getSize());
    Settings.setWindowLocation(mainFrame.getLocation());
    
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



  private void updatePluginMenu(JMenu theMenu) {
    theMenu.removeAll();

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
        theMenu.add(item);
      }
    }
  }



  private void scrollToNow() {
    // TODO: change to the shown day program to today if nessesary

    Calendar cal = Calendar.getInstance();
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    programTablePanel.scrollTo(hour);
  }



  public void actionPerformed(ActionEvent event) {
    Object src = event.getSource();

    if (src == mNowBt) {
      scrollToNow();
    }
    else if (src == mEarlyBt) {
      programTablePanel.scrollTo(4);
    }
    else if (src == mMorningBt) {
      programTablePanel.scrollTo(8);
    }
    else if (src == mMiddayBt) {
      programTablePanel.scrollTo(12);
    }
    else if (src == mEveningBt) {
      programTablePanel.scrollTo(18);
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

    newTvDataAvailable();
  }



  /**
   * Updates the program table and the finder panel.
   * <p>
   * Called when new TV data was downloaded or when TV data was imported.
   */
  private void newTvDataAvailable() {
    try {
      devplugin.Date showingDate = finderPanel.getSelectedDate();
      DayProgram dayProgram = DataService.getInstance().getDayProgram(showingDate);
      programTablePanel.setDayProgram(dayProgram);
    } catch(TvBrowserException exc) {
      ErrorHandler.handle(exc);
    }
    if (finderPanel != null) {
      finderPanel.update();
    }
  }



  private JButton[] createTimeBtns() {
  	String msg;
  	JButton[] result=new JButton[5];
	msg = mLocalizer.msg("botton.now", "Now");
	mNowBt = new PictureButton(msg, new ImageIcon("imgs/TimeNow24.gif"));
	mNowBt.addActionListener(this);
	result[0]=mNowBt;

	msg = mLocalizer.msg("botton.early", "Early");
	mEarlyBt = new PictureButton(msg, new ImageIcon("imgs/TimeEarly24.gif"));
	mEarlyBt.addActionListener(this);
	result[1]=mEarlyBt;

	msg = mLocalizer.msg("botton.morning", "Morning");
	mMorningBt = new PictureButton(msg, new ImageIcon("imgs/TimeMorning24.gif"));
	mMorningBt.addActionListener(this);
	result[2]=mMorningBt;

	msg = mLocalizer.msg("botton.midday", "Midday");
	mMiddayBt = new PictureButton(msg, new ImageIcon("imgs/TimeMidday24.gif"));
	mMiddayBt.addActionListener(this);
	result[3]=mMiddayBt;

	msg = mLocalizer.msg("botton.evening", "Evening");
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
    try {
      DayProgram prog = DataService.getInstance().getDayProgram(date);
      programTablePanel.setDayProgram(prog);
      if (finderPanel != null) {
        finderPanel.update();
      }
      if (date.equals(new devplugin.Date())) {
        // If this is today -> scroll to now
        scrollToNow();
      }
    }
    catch (TvBrowserException exc) {
      ErrorHandler.handle(exc);
    }
  }



  /**
   * Implementation of Interface DateListener
   *
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


/*
  public void updateProgramTableSkin() {
    programTablePanel.updateBackground();
  }

  */

  private void importTvData() {
    JFileChooser chooser = new JFileChooser();

    String msg;

    File defaultFile = new File("tvdata" + EXPORTED_TV_DATA_EXTENSION);
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



  /**
   * Starts the tv data update.
   */
  private void updateTvData() {
    if (DataService.getInstance().isDownloading()) {
      onDownloadDone();
    } else {
      UpdateDlg dlg = new UpdateDlg(this, true);
      dlg.pack();
      UiUtilities.centerAndShow(dlg);
      final int daysToDownload = dlg.getResult();
      if (daysToDownload != UpdateDlg.CANCEL) {
        final JFrame parent = this;
        downloadingThread = new Thread() {
          public void run() {
            onDownloadStart();
            DataService.getInstance().startDownload(daysToDownload);
            onDownloadDone();
          }
        };
        downloadingThread.start();
      }
    }
  }



   /**
   * Shows the settings dialog.
   */
  private void showSettingsDialog() {
    SettingsDlg dlg = new SettingsDlg(this);
    dlg.pack();
    UiUtilities.centerAndShow(dlg);
    if (Settings.settingHasChanged(new String[]{"lookandfeel"})) {
		updateLookAndFeel();
    }
    if (Settings.settingHasChanged(new String[]{"applicationskin","useapplicationskin"})) {
		updateApplicationSkin();
    }
    if (Settings.settingHasChanged(new String[]{"tablebgmode","tablebackground"})) {
		programTablePanel.updateBackground();
    }
    if (Settings.settingHasChanged(new String[]{"plugins"})) {
    	programTablePanel.setPluginContextMenu(DataService.getInstance().createPluginContextMenu(this));
   		updatePluginMenu(pluginsMenu);
    }
    if (Settings.settingHasChanged(new String[]{"timebutton","updatebutton","preferencesbutton",
    "buttontype","buttonplugins"})) {
    	buttonPanel.update();
    }

    if (Settings.settingHasChanged(new String[]{"subscribedchannels"})) {
    	createChannelList();
    	programTablePanel.subscribedChannelsChanged();
		DataService.getInstance().subscribedChannelsChanged();
		devplugin.Date showingDate = finderPanel.getSelectedDate();
		DayProgram dayProgram = DataService.getInstance().getDayProgram(showingDate);
		try {
			programTablePanel.setDayProgram(dayProgram);
		} catch(TvBrowserException exc) {
	  		ErrorHandler.handle(exc);
		}
    }

  }
  /**
   *
   * Shows the about box
   * @author darras
   *
   */
  private void showAboutBox() {
  	AboutBox box=new AboutBox(this);
  	box.pack();
  	UiUtilities.centerAndShow(box);
  	box.dispose();
  }

}