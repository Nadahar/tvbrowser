
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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;

import javax.swing.*;

import tvbrowser.TVBrowser;
import tvbrowser.core.*;
import tvbrowser.ui.SkinPanel;
import tvbrowser.ui.aboutbox.AboutBox;
import tvbrowser.ui.filter.FilterChooser;
import tvbrowser.ui.finder.FinderPanel;
import tvbrowser.ui.licensebox.LicenseBox;
import tvbrowser.ui.programtable.DefaultProgramTableModel;
import tvbrowser.ui.programtable.ProgramTableScrollPane;
import tvbrowser.ui.settings.SettingsDialog;
import tvbrowser.ui.update.SoftwareUpdateDlg;
import tvbrowser.ui.update.SoftwareUpdateItem;
import tvbrowser.ui.update.SoftwareUpdater;
import tvdataservice.TvDataService;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.UiUtilities;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;

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

 
  private JDialog mConfigAssistantDialog;
  private SoftwareUpdateItem[] mSoftwareUpdateItems=null;
  private ProgramTableScrollPane mProgramTableScrollPane;
  private DefaultProgramTableModel mProgramTableModel;
  private Thread downloadingThread;
  private JPanel jcontentPane;
  private JMenuItem settingsMenuItem, quitMenuItem, updateMenuItem,
   aboutMenuItem, helpMenuItem, mPluginDownloadMenuItem, donorMenuItem,
   faqMenuItem, forumMenuItem, websiteMenuItem, configAssistantMenuItem;
  private SkinPanel skinPanel;
  private HorizontalToolBar mHorizontalToolBar;
  private VerticalToolBar mVerticalToolBar;
  private StatusBar mStatusBar;

  private JMenu mPluginsMenu;
  
  private static MainFrame mSingleton;

  
 
  private MainFrame() {
    super(TVBrowser.MAINWINDOW_TITLE);

    String msg;
    Icon icon;

    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);
    
    mStatusBar = new StatusBar();
 
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
    
    configAssistantMenuItem=new JMenuItem(mLocalizer.msg("menuitem.configAssistant","setup assistant"),new ImageIcon("imgs/Preferences16.gif"));
    configAssistantMenuItem.addActionListener(this);
    helpMenu.add(configAssistantMenuItem);
    
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
    if (Settings.propUseApplicationSkin.getBoolean()) {
      mode = SkinPanel.WALLPAPER;
    } else {
      mode = SkinPanel.NONE;
    }

    skinPanel = new SkinPanel(Settings.propApplicationSkin.getString(),mode);
    skinPanel.setLayout(new BorderLayout());

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setOpaque(false);
    centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    Channel[] channelArr = ChannelList.getSubscribedChannels();
    int startOfDay = Settings.propProgramTableStartOfDay.getInt();
    int endOfDay = Settings.propProgramTableEndOfDay.getInt();
    mProgramTableModel = new DefaultProgramTableModel(channelArr, startOfDay, endOfDay);
    mProgramTableScrollPane = new ProgramTableScrollPane(mProgramTableModel);
    centerPanel.add(mProgramTableScrollPane);

    FinderPanel.getInstance().setDateListener(this);
    dateChanged(new devplugin.Date(), null);
    
    mHorizontalToolBar=new HorizontalToolBar(this,new FilterChooser(this,mProgramTableModel));
    mVerticalToolBar=new VerticalToolBar(this, FinderPanel.getInstance());
    
      
    
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
    new MenuHelpTextAdapter(configAssistantMenuItem,mLocalizer.msg("menuinfo.configAssistant",""),lb);

    skinPanel.add(mHorizontalToolBar,BorderLayout.NORTH);
    skinPanel.add(mVerticalToolBar,BorderLayout.EAST);
    skinPanel.add(centerPanel, BorderLayout.CENTER);
    skinPanel.add(mStatusBar,BorderLayout.SOUTH);

    jcontentPane.add(skinPanel,BorderLayout.CENTER);
    
    
    PluginLoader.getInstance().addPluginStateListener(new PluginStateListener(){

			public void pluginActivated(Plugin p) {
        createPluginsMenu();				
			}

			public void pluginDeactivated(Plugin p) {
        createPluginsMenu();
			}

			public void pluginLoaded(Plugin p) {
        createPluginsMenu();
			}

			public void pluginUnloaded(Plugin p) {
        createPluginsMenu();
			}
    });
    
  }

  public JLabel getStatusBarLabel() {
    return mStatusBar.getLabel();
  }
  
  
  public ProgramTableScrollPane getProgramTableScrollPane() {
    return mProgramTableScrollPane;
  }
  
  
  public HorizontalToolBar getHorizontalToolBar() {
    return mHorizontalToolBar;
  }
  
  
  public VerticalToolBar getVerticalToolBar() {
    return mVerticalToolBar;
  }
  
  
  public DefaultProgramTableModel getProgramTableModel() {
    return mProgramTableModel;
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
    PluginManager.getInstance().storeSettings();
    
    mLog.info("Finishing plugins");
    PluginLoader.getInstance().shutdownAllPlugins();
    
    mLog.info("Storing dataservice settings");
    TvDataServiceManager.getInstance().finalizeDataServices();
    
    mLog.info("Storing channel day light saving time corrections");
    ChannelList.storeDayLightSavingTimeCorrections();
    
    mLog.info("Storing window size and location");
    boolean maximized = getExtendedState() == Frame.MAXIMIZED_BOTH;
    Settings.propIsWindowMaximized.setBoolean(maximized);
    if (! maximized) {
      // Save the window size and location only when not maximized
      Settings.propWindowWidth.setInt(getWidth());
      Settings.propWindowHeight.setInt(getHeight());
      Settings.propWindowX.setInt(getX());
      Settings.propWindowY.setInt(getY());
    }
    
    mLog.info("Storing settings");
    try {
      Settings.storeSettings();
    } catch (TvBrowserException e) {
      ErrorHandler.handle(e);
    }

    mLog.info("Closing tv data base");
    try {
      TvDataBase.getInstance().close();
    }
    catch (Exception exc) {
      mLog.log(Level.WARNING, "Closing TV data base failed", exc);
    }

    mLog.info("Quitting");
    System.exit(0);
  }



  
  
 
  private void createPluginsMenu() {
    mPluginsMenu.removeAll();
    
    Object[] plugins = PluginLoader.getInstance().getActivePlugins();
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

  public void runSetupAssistant() {
    
    ProgressWindow progWin=new ProgressWindow(this,mLocalizer.msg("loadingAssistant",""));
    final JFrame parent=this;
    progWin.run(new Progress(){    
      public void run() {
        mConfigAssistantDialog=new tvbrowser.ui.configassistant.ConfigAssistant(parent);   
      }    
    });
    
    util.ui.UiUtilities.centerAndShow(mConfigAssistantDialog);  
    mConfigAssistantDialog.hide();
    mConfigAssistantDialog.dispose();
    mConfigAssistantDialog=null;
              
    boolean dataAvailable = TvDataBase.getInstance().dataAvailable(new Date());
    if (! dataAvailable) {
      askForDataUpdate();
    }
    
    Settings.handleChangedSettings();
    
   
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
      util.ui.BrowserLauncher.openURL(mLocalizer.msg("website.donors",""));
    }
    else if (src==faqMenuItem) {
      util.ui.BrowserLauncher.openURL(mLocalizer.msg("website.faq",""));
    }
    else if (src==forumMenuItem) {
      util.ui.BrowserLauncher.openURL(mLocalizer.msg("website.forum",""));      
    }
    else if (src==websiteMenuItem) {
      util.ui.BrowserLauncher.openURL(mLocalizer.msg("website.tvbrowser",""));
    }
    else if (src==configAssistantMenuItem) {
      runSetupAssistant();
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
    mProgramTableScrollPane.scrollToTime(Settings.propEarlyTime.getInt());
  }
  
  public void onMiddayBtn() {
    mProgramTableScrollPane.scrollToTime(Settings.propMiddayTime.getInt());
  }
  
  public void onAfternoonBtn() {
     mProgramTableScrollPane.scrollToTime(Settings.propAfternoonTime.getInt());
   }
  
  public void onEveningBtn() {
    mProgramTableScrollPane.scrollToTime(Settings.propEveningTime.getInt());
  }
  
  public void onNowBtn() {
    // Change to the shown day program to today if nessesary
    devplugin.Date today = new devplugin.Date();
    if (! today.equals(FinderPanel.getInstance().getSelectedDate())) {
      FinderPanel.getInstance().markDate(today);
    }

    scrollToNow();
  }


  private void onDownloadStart() {
    JButton updateBtn=mHorizontalToolBar.getUpdateBtn();
    updateBtn.setText(TVBrowser.mLocalizer.msg("button.stop", "Stop"));
    updateBtn.setIcon(new ImageIcon("imgs/Stop24.gif"));
    updateMenuItem.setText(mLocalizer.msg("menuitem.stopUpdate", "Stop update..."));
  }



  private void onDownloadDone() {
    TvDataUpdater.getInstance().stopDownload();
    mStatusBar.getProgressBar().setValue(0);
    
    JButton updateBtn=mHorizontalToolBar.getUpdateBtn();
    updateBtn.setText(TVBrowser.mLocalizer.msg("button.update", "Update"));
    updateBtn.setIcon(new ImageIcon("imgs/Refresh24.gif"));
    updateMenuItem.setText(mLocalizer.msg("menuitem.update", "Update..."));

    FinderPanel.getInstance().updateUI();

    Settings.propLastDownloadDate.setDate(Date.getCurrentDate());

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
    changeDate(FinderPanel.getInstance().getSelectedDate(), null);
  }


  private void changeDate(Date date, devplugin.ProgressMonitor monitor) {      
    mProgramTableModel.setDate(date, monitor);
    
    if (date.equals(new devplugin.Date())) {
      // If this is today -> scroll to now
      scrollToNow();
    }
  }



  /**
   * Implementation of Interface DateListener
   */
  public void dateChanged(final devplugin.Date date, devplugin.ProgressMonitor monitor) { 
      changeDate(date, monitor);
  }





  public void updateApplicationSkin() {
    int mode;
    if (Settings.propUseApplicationSkin.getBoolean()) {
      mode = SkinPanel.WALLPAPER;
    } else {
      mode = SkinPanel.NONE;
    }

    skinPanel.update(Settings.propApplicationSkin.getString(),mode);
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

  
  
  /**
   * Starts the tv data update.
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
    SettingsDialog dlg = new SettingsDialog(this);
    dlg.centerAndShow();
    Settings.handleChangedSettings();
  }
  
    
  /**
   * Shows the about box
   */
  private void showAboutBox() {
    AboutBox box=new AboutBox(this);
    box.setSize(450,550);
    UiUtilities.centerAndShow(box);
    box.dispose();
  }


  private void showUpdatePluginsDlg() {
    Object[] options = {mLocalizer.msg("checknow","Check now"),
        mLocalizer.msg("cancel","Cancel")};
    String msg=mLocalizer.msg("question.1","do you want to check for new plugins");     
    int answer = JOptionPane.showOptionDialog(this,msg,mLocalizer.msg("title.1","update plugins"),
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]); 
  
    if (answer==JOptionPane.YES_OPTION) {
      
      
      ProgressWindow progWin=new ProgressWindow(this,mLocalizer.msg("title.2","searching for new plugins..."));
      
      progWin.run(new Progress(){
        public void run() {
          try {
            java.net.URL url=null;
            //url=new java.io.File("plugins.txt").toURL();
            url=new java.net.URL("http://tvbrowser.sourceforge.net/plugins/plugins2.txt");    
            SoftwareUpdater softwareUpdater=null;
            softwareUpdater=new SoftwareUpdater(url);
            mSoftwareUpdateItems=softwareUpdater.getAvailableSoftwareUpdateItems();
          }catch (java.io.IOException e) {      
            e.printStackTrace();
          }
        }
      });
      
      
      if (mSoftwareUpdateItems==null) {
        JOptionPane.showMessageDialog(this,mLocalizer.msg("error.1","software check failed."));
      }
      else if (mSoftwareUpdateItems.length==0) {
        JOptionPane.showMessageDialog(this,mLocalizer.msg("error.2","No new items available"));
      }
      else {
        SoftwareUpdateDlg dlg=new SoftwareUpdateDlg(this);
        dlg.setSoftwareUpdateItems(mSoftwareUpdateItems);
        UiUtilities.centerAndShow(dlg);
      }
    }
  
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
  
  Locale locale=Locale.getDefault();
  String language=locale.getLanguage();
      
  
    java.io.File indexFile = new java.io.File("help/"+language+"/index.html");
    if (!indexFile.exists()) {
      indexFile = new java.io.File("help/default/index.html");
    } 
    util.ui.BrowserLauncher.openURL(indexFile.getAbsolutePath());
  
  
}



}