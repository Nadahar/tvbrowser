
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

package tvbrowser.ui.mainframe;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.KeyStroke;


import tvbrowser.TVBrowser;
import tvbrowser.core.ChannelList;
import tvbrowser.core.DataService;
import tvbrowser.core.DateListener;
import tvbrowser.core.DayProgram;
import tvbrowser.core.PluginManager;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataServiceManager;
import tvbrowser.ui.SkinPanel;

import tvbrowser.ui.aboutbox.AboutBox;
import tvbrowser.ui.filter.FilterChooser;
import tvbrowser.ui.filter.FilterComponentList;
import tvbrowser.ui.finder.FinderPanel;
import tvbrowser.ui.licensebox.LicenseBox;
import tvbrowser.ui.programtable.DefaultProgramTableModel;
import tvbrowser.ui.programtable.ProgramTableScrollPane;
import tvbrowser.ui.settings.SettingsDialog;
import tvbrowser.ui.update.PluginUpdate;
import tvdataservice.TvDataService;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.UiUtilities;
import devplugin.Channel;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class MainFrame extends JFrame implements ActionListener, DateListener {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(tvbrowser.TVBrowser.class.getName());

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(MainFrame.class);

  private static final String EXPORTED_TV_DATA_EXTENSION = ".tv.zip";

  private ProgramTableScrollPane mProgramTableScrollPane;
  private DefaultProgramTableModel mProgramTableModel;
  private Thread downloadingThread;
  private JPanel jcontentPane;
  private FinderPanel finderPanel;
  private JMenuItem settingsMenuItem, quitMenuItem, updateMenuItem,
   aboutMenuItem, helpMenuItem, mPluginDownloadMenuItem, /*licenseMenuItem,*/ donorMenuItem,
   faqMenuItem, forumMenuItem, websiteMenuItem;
  private SkinPanel skinPanel;
  private HorizontalToolBar mDefaultToolBar;
  private VerticalToolBar mDateTimeToolBar;
  private StatusBar mStatusBar;

  private JMenu mPluginsMenu;
  
  private static MainFrame mSingleton;

  
 
  private MainFrame() {
    super(TVBrowser.MAINWINDOW_TITLE);

    String msg;
    Icon icon;

    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);
    
    
    mStatusBar=new StatusBar(DataService.getInstance().getProgressBar());
    
 
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
    
    msg = mLocalizer.msg("menuitem.license","Terms of Use");
    //JMenu licenseMenuItem=new JMenu(msg,new ImageIcon("imgs/About16.gif"));
    JMenu licenseMenu=new JMenu(msg);
    //licenseMenuItem.addActionListener(this);
    tvDataMenu.add(licenseMenu);
    
    addLicenseMenuItems(licenseMenu);
    
    //licenseMenu.add(new JMenu("TV-Browser"));
    
    // Plugins menu
    mPluginsMenu = new JMenu(mLocalizer.msg("menu.plugins", "Plugins"));
    mPluginsMenu.setMnemonic(KeyEvent.VK_P);
    menuBar.add(mPluginsMenu);
    
  
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
    
    icon = new ImageIcon("imgs/WebComponent16.gif");
    msg=mLocalizer.msg("menuitem.donors","Donors");
    donorMenuItem=new JMenuItem(msg,icon);
    donorMenuItem.addActionListener(this);
    helpMenu.add(donorMenuItem);
    
    faqMenuItem=new JMenuItem("FAQ",icon);
    faqMenuItem.addActionListener(this);
    helpMenu.add(faqMenuItem);
    
    forumMenuItem=new JMenuItem("Forum",icon);
    forumMenuItem.addActionListener(this);
    helpMenu.add(forumMenuItem);
    
    websiteMenuItem=new JMenuItem("Website",icon);
    websiteMenuItem.addActionListener(this);
    helpMenu.add(websiteMenuItem);
    
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

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setOpaque(false);
    centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    mProgramTableModel = new DefaultProgramTableModel(ChannelList.getSubscribedChannels(),Settings.getProgramTableStartOfDay(),Settings.getProgramTableEndOfDay());
    mProgramTableScrollPane = new ProgramTableScrollPane(mProgramTableModel);
    centerPanel.add(mProgramTableScrollPane);

   finderPanel=FinderPanel.getInstance();
   finderPanel.setDateListener(this);
   dateChanged(new devplugin.Date());
    
    mDefaultToolBar=new HorizontalToolBar(this,new FilterChooser(this,mProgramTableModel));
    mDateTimeToolBar=new VerticalToolBar(this,finderPanel);
    
   
  
    
    JLabel lb=mStatusBar.getLabel();
    new MenuHelpTextAdapter(settingsMenuItem, mLocalizer.msg("menuinfo.settings",""), lb); 
    new MenuHelpTextAdapter(quitMenuItem, mLocalizer.msg("menuinfo.quit",""), lb);
    new MenuHelpTextAdapter(updateMenuItem, mLocalizer.msg("menuinfo.update",""), lb);
    new MenuHelpTextAdapter(mPluginDownloadMenuItem, mLocalizer.msg("menuinfo.findplugins",""), lb); 
    new MenuHelpTextAdapter(helpMenuItem, mLocalizer.msg("menuinfo.help",""), lb); 
    new MenuHelpTextAdapter(aboutMenuItem, mLocalizer.msg("menuinfo.about",""), lb);
    new MenuHelpTextAdapter(donorMenuItem,mLocalizer.msg("website.donors",""),lb); 
    new MenuHelpTextAdapter(faqMenuItem,mLocalizer.msg("website.faq",""),lb); 
    new MenuHelpTextAdapter(forumMenuItem,mLocalizer.msg("website.forum",""),lb); 
    new MenuHelpTextAdapter(websiteMenuItem,mLocalizer.msg("website.tvbrowser",""),lb); 
    

    skinPanel.add(mDefaultToolBar,BorderLayout.NORTH);
    skinPanel.add(mDateTimeToolBar,BorderLayout.EAST);
    skinPanel.add(centerPanel, BorderLayout.CENTER);
    skinPanel.add(mStatusBar,BorderLayout.SOUTH);

    jcontentPane.add(skinPanel,BorderLayout.CENTER);

  }

  public JLabel getStatusBarLabel() {
    return mStatusBar.getLabel();
  }

  public static MainFrame getInstance() {
    if (mSingleton==null) {
      mSingleton=new MainFrame();
    }
    return mSingleton;
  }

  private void addLicenseMenuItems(JMenu licenseMenu) {
    
    TvDataService services[]=TvDataServiceManager.getInstance().getDataServices();
    for (int i=0;i<services.length;i++) {
      final String license=services[i].getInfo().getLicense();
      if (license!=null) {
        JMenuItem item=new JMenuItem(services[i].getInfo().getName(),new ImageIcon("imgs/About16.gif"));
        item.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e) {
            LicenseBox box=new LicenseBox(null, license, false);
            util.ui.UiUtilities.centerAndShow(box);
          }        
        });
        licenseMenu.add(item);
      }
    }
    
  }

  public void quit() {
    mLog.info("Storing plugin data");
    PluginManager.finalizeInstalledPlugins();
    
    mLog.info("Storing filter components");
    FilterComponentList.store();

    mLog.info("Storing dataservice settings");
    TvDataServiceManager.getInstance().finalizeDataServices();
    
    mLog.info("Storing channel day light saving time corrections");
    ChannelList.storeDayLightSavingTimeCorrections();
    
    mLog.info("Storing window size and location");
    boolean maximized = getExtendedState() == JFrame.MAXIMIZED_BOTH;
    Settings.setWindowIsMaximized(maximized);
    if (! maximized) {
      // Save the window size and location only when not maximized
      Settings.setWindowSize(getSize());
      Settings.setWindowLocation(getLocation());
    }
    
    try {
      Settings.storeSettings();
    } catch (TvBrowserException e) {
      ErrorHandler.handle(e);
    }

    mLog.info("Quitting");
    System.exit(0);
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
            plugin.execute();
          }
        });
        mPluginsMenu.add(item);
        new MenuHelpTextAdapter(item, plugin.getInfo().getDescription(), mStatusBar.getLabel()); 

      }
    }
    
    mPluginsMenu.addSeparator();
    mPluginsMenu.add(mPluginDownloadMenuItem);
        
  }



  public void scrollToNow() {
    // Scroll to now
    Calendar cal = Calendar.getInstance();
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    mProgramTableScrollPane.scrollToTime(hour * 60);
  }



  public void actionPerformed(ActionEvent event) {
    Object src = event.getSource();
    if (src == quitMenuItem) {
      quit(); 
    }
    else if (src == updateMenuItem) {
      updateTvData();
    }
    else if (src == settingsMenuItem) {
      showSettingsDialog();
    }
    else if (src==donorMenuItem) {
      try {
        util.ui.BrowserLauncher.openURL(mLocalizer.msg("website.donors",""));
      }catch(java.io.IOException e) {
        ErrorHandler.handle(e.getLocalizedMessage(),e);
      }
    }
    else if (src==faqMenuItem) {
      try {
        util.ui.BrowserLauncher.openURL(mLocalizer.msg("website.faq",""));
      }catch(java.io.IOException e) {
        ErrorHandler.handle(e.getLocalizedMessage(),e);
      }
    }
    else if (src==forumMenuItem) {
      try {
        util.ui.BrowserLauncher.openURL(mLocalizer.msg("website.forum",""));
      }catch(java.io.IOException e) {
        ErrorHandler.handle(e.getLocalizedMessage(),e);
      }
    }
    else if (src==websiteMenuItem) {
      try {
        util.ui.BrowserLauncher.openURL(mLocalizer.msg("website.tvbrowser",""));
      }catch(java.io.IOException e) {
        ErrorHandler.handle(e.getLocalizedMessage(),e);
      }
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


  public void onEarlyBtn() {
    mProgramTableScrollPane.scrollToTime(Settings.getEarlyTime());
  }
  
  public void onMiddayBtn() {
    mProgramTableScrollPane.scrollToTime(Settings.getMiddayTime());
  }
  
  public void onAfternoonBtn() {
     mProgramTableScrollPane.scrollToTime(Settings.getAfternoonTime());
   }
  
  public void onEveningBtn() {
    mProgramTableScrollPane.scrollToTime(Settings.getEveningTime());
  }
  
  public void onNowBtn() {
    // Change to the shown day program to today if nessesary
    devplugin.Date today = new devplugin.Date();
    if (! today.equals(finderPanel.getSelectedDate())) {
      finderPanel.markDate(today);
    }

    scrollToNow();
  }


  private void onDownloadStart() {
    JButton updateBtn=mDefaultToolBar.getUpdateBtn();
    updateBtn.setText(TVBrowser.mLocalizer.msg("button.stop", "Stop"));
    updateBtn.setIcon(new ImageIcon("imgs/Stop24.gif"));
    updateMenuItem.setText(mLocalizer.msg("menuitem.stopUpdate", "Stop update..."));
  }



  private void onDownloadDone() {
    DataService.getInstance().stopDownload();
    DataService.getInstance().getProgressBar().setValue(0);
    
    JButton updateBtn=mDefaultToolBar.getUpdateBtn();
    updateBtn.setText(TVBrowser.mLocalizer.msg("button.update", "Update"));
    updateBtn.setIcon(new ImageIcon("imgs/Refresh24.gif"));
    updateMenuItem.setText(mLocalizer.msg("menuitem.update", "Update..."));

  }


  public void showChannel(Channel ch) {
    mProgramTableScrollPane.scrollToChannel(ch);
  }

  /**
   * Updates the program table and the finder panel.
   * <p>
   * Called when new TV data was downloaded or when TV data was imported.
   */
  private void newTvDataAvailable() {
    changeDate(finderPanel.getSelectedDate());
  }


  private void changeDate(devplugin.Date date) {      
    devplugin.Date nextDate = date.addDays(1);
    DataService.getInstance().getProgressBar().setMaximum(100);
    DayProgram today = DataService.getInstance().getDayProgram(date, 0, 49);
    DayProgram tomorrow = DataService.getInstance().getDayProgram(nextDate, 50, 99);
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
      changeDate(date);
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



  public void runUpdateThread(final int daysToDownload) {
    
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
  public void updateTvData() {
    if (DataService.getInstance().isDownloading()) {
      DataService.getInstance().stopDownload();
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
  public void showSettingsDialog() {
    SettingsDialog dlg = new SettingsDialog(this);
    dlg.centerAndShow();
    
    
    
    
    if (Settings.settingHasChanged(new String[]{"font.programtitle","font.programinfo","font.programtime","font.channelname","usedefaultfonts"})) {
      util.ui.ProgramPanel.updateFonts();
      tvbrowser.ui.programtable.ChannelPanel.fontChanged();
      mProgramTableScrollPane.getProgramTable().fontChanged();
      mProgramTableScrollPane.tableDataChanged();
      mProgramTableScrollPane.getProgramTable().tableDataChanged();
    }
    if (Settings.settingHasChanged(new String[]{"lookandfeel","skinLF.themepack","skinLF.enabled"})) {
      TVBrowser.updateLookAndFeel();
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
    
    if (Settings.settingHasChanged(new String[]{"updatebutton","preferencesbutton",
    "buttontype","hiddenbuttonplugins","plugins"})) {
      mDefaultToolBar.updateButtons();
    }
    
    if (Settings.settingHasChanged(new String[]{"timebutton"})) {
      mDateTimeToolBar.updateButtons();
    }
    if (Settings.settingHasChanged(new String[]{"subscribedchannels"})) {
      onSubscribedChannelsChanged();
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
  
  public void onSubscribedChannelsChanged() {
    ChannelList.create();    
    DataService.getInstance().subscribedChannelsChanged();
    mProgramTableModel.setShownChannels(ChannelList.getSubscribedChannels());
    mDefaultToolBar.updateChannelChooser();    
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
  
  PluginUpdate.updatePlugins(this);
  
  
  
}

public void askForDataUpdate() {
  
  String msg1 = mLocalizer.msg("askforupdatedlg.1","update now");
  String msg2 = mLocalizer.msg("askforupdatedlg.2","later");
  String msg3 = mLocalizer.msg("askforupdatedlg.3","No tv data for todays program available.");
  String msg4 = mLocalizer.msg("askforupdatedlg.4","Do you want to update now?");
  String msg5 = mLocalizer.msg("askforupdatedlg.5","Update tv data");
  
  
  
  Object[] options = {msg1,msg2};
  int result = JOptionPane.showOptionDialog(this,          
    msg3+"\n\n"+
    msg4,
    msg5,
    JOptionPane.YES_NO_OPTION,
    JOptionPane.QUESTION_MESSAGE,
    null,options,options[0]);
    
  if (result==JOptionPane.YES_OPTION) {
    updateTvData();
  } 
}


public void showHelpDialog() {
  String msg = mLocalizer.msg("helproot", "help/default/index.html");
  //util.ui.HelpDialog.showHelpPage(this,msg+"index.html",null);
  
  try {
    java.io.File indexFile=new java.io.File(mLocalizer.msg("helproot", "help/default/index.html"));
    util.ui.BrowserLauncher.openURL("file://"+indexFile.getAbsolutePath());
  }catch(java.io.IOException e) {
    e.printStackTrace();
    ErrorHandler.handle(e.getLocalizedMessage(),e);
  }
  
}



}