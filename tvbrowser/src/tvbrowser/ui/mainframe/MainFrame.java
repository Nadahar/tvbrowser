
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
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.View;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;
import tvbrowser.TVBrowser;
import tvbrowser.core.ChannelList;
import tvbrowser.core.DateListener;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.TvDataServiceManager;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.PluginStateAdapter;
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
  private StatusBar mStatusBar;

  private JMenu mPluginsMenu;
  
  private static MainFrame mSingleton;

  private TimeChooserPanel mTimeChooser;
  private ChannelChooserPanel mChannelChooser;
  
 
  private MainFrame() {
    super(TVBrowser.MAINWINDOW_TITLE);

    String msg;
    Icon icon;

    JMenuBar menuBar = new JMenuBar();
    
    
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
  
    updatePluginsMenu();
    
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
    dateChanged(new devplugin.Date(), null, null);
    
    mHorizontalToolBar=new HorizontalToolBar(this,new FilterChooser(this,mProgramTableModel));
    
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

    skinPanel.add(centerPanel, BorderLayout.CENTER);
    
    ViewMap viewMap = new ViewMap();
    
    mTimeChooser = new TimeChooserPanel(this);
    mChannelChooser = new ChannelChooserPanel(this);
    
    View programTableView = new View("Programm-Tabelle", null, skinPanel);
    View timeView = new View("Zeit", null, mTimeChooser);
    View dateView = new View("Datum", null, new DateChooserPanel(this, FinderPanel.getInstance()));
    View channelView = new View("Sender", null, mChannelChooser);
    
    viewMap.addView(0, programTableView);
    viewMap.addView(1, timeView);
    viewMap.addView(2, dateView);
    viewMap.addView(3, channelView);

   
    RootWindow rootWindow = DockingUtil.createRootWindow(viewMap, true);
    
    rootWindow.setWindow(new SplitWindow(true,
            1,
            programTableView,
            new SplitWindow(false,0,timeView,new SplitWindow(false,0.25f,dateView,channelView))));
    
    rootWindow.getRootWindowProperties().getSplitWindowProperties().setContinuousLayoutEnabled(true);
    
    rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);

    rootWindow.getRootWindowProperties().getWindowAreaProperties().setInsets(new Insets(0,0,0,0));
    rootWindow.getRootWindowProperties().getSplitWindowProperties().setDividerSize(2);
    rootWindow.getWindowBar(Direction.LEFT).getTabWindowProperties().getTabProperties().getNormalButtonProperties().getCloseButtonProperties().setVisible(false);
    
    rootWindow.getRootWindowProperties().getDockingWindowProperties().getTabProperties().
    getHighlightedButtonProperties().getCloseButtonProperties().setVisible(false);

    rootWindow.getRootWindowProperties().getWindowAreaProperties().setBackgroundColor(new Color(110, 130, 180));
        
    jcontentPane.add(mHorizontalToolBar,BorderLayout.NORTH);
    jcontentPane.add(rootWindow,BorderLayout.CENTER);
    jcontentPane.add(mStatusBar, BorderLayout.SOUTH);
    
    PluginProxyManager.getInstance().addPluginStateListener(new PluginStateAdapter() {
      public void pluginActivated(Plugin p) {
        updatePluginsMenu();
      }

      public void pluginDeactivated(Plugin p) {
        updatePluginsMenu();
      }
    });

    setJMenuBar(menuBar);    
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
  
  
  public DefaultProgramTableModel getProgramTableModel() {
    return mProgramTableModel;
  }
  

  public static MainFrame getInstance() {
    if (mSingleton==null) {
      mLog.info("Initializing main frame...");
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
            LicenseBox box=new LicenseBox(mSingleton, license, false);
            util.ui.UiUtilities.centerAndShow(box);
          }        
        });
        licenseMenu.add(item);
      }
    }
    
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
    }
    catch (Exception exc) {
      mLog.log(Level.WARNING, "Closing TV data base failed", exc);
    }

    mLog.info("Quitting");
    System.exit(0);
  }

  
  public void updatePluginsMenu() {
    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    updatePluginsMenu(mPluginsMenu, plugins);
    
    // Create the MenuHelpTextAdapter
    for (int i = 0; i < mPluginsMenu.getItemCount(); i++) {
      JMenuItem item = mPluginsMenu.getItem(i);
      new MenuHelpTextAdapter(item, plugins[i].getInfo().getDescription(),
                              mStatusBar.getLabel()); 
    }
    
    mPluginsMenu.addSeparator();
    mPluginsMenu.add(mPluginDownloadMenuItem);
  }
  

  public static void updatePluginsMenu(JMenu pluginsMenu, PluginProxy[] plugins) {
    pluginsMenu.removeAll();
    
    PluginProxy[] copy = new PluginProxy[plugins.length];
    
    for (int i = 0; i < plugins.length;i++) {
        copy[i] = plugins[i];
    }
    
    Arrays.sort(copy, new Comparator() {

        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }

    });
    
    for (int i = 0; i < copy.length; i++) {
      Action action = copy[i].getButtonAction();
      if (action != null) {
        pluginsMenu.add(new JMenuItem(action));
      }
    }
  }



  public void scrollToNow() {
    // Get the current time
    Calendar cal = Calendar.getInstance();
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    
    // Choose the day.
    // NOTE: If its early in the morning before the setted "day start" we should
    //       stay at the last day - otherwise the user won't see the current
    //       program. But until when should we stay at the old day?
    //       Example: day start: 0:00, day end: 6:00
    //       Directly after the day start is not a good choice, because the new
    //       day program table will not contain programs that started before 0:00.
    //       Directly after the day end is also not a good choice, because a
    //       minute before the old day program will not contain the coming programs.
    //       So I think the best choice will be the middle, in this case 3:00.
    devplugin.Date day = new devplugin.Date();
    int dayStart = Settings.propProgramTableStartOfDay.getInt();
    int dayEnd = Settings.propProgramTableEndOfDay.getInt();
    int splitHour = (dayEnd - dayStart) / 60;
    if (hour < splitHour) {
      // It's early in the morning -> use the program table of yesterday
      day = day.addDays(-1);
      hour += 24;
    }
    
    // Change to the shown day program to today if nessesary
    // and scroll to "now" afterwards
    final int fHour = hour;
    FinderPanel.getInstance().markDate(day, new Runnable() {
      public void run() {
        // Scroll to now
        mProgramTableScrollPane.scrollToTime(fHour * 60);
      }
    });
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
  
  public void scrollToTime(int time) {
    mProgramTableScrollPane.scrollToTime(time);
  }



  private void onDownloadStart() {
    JButton updateBtn=mHorizontalToolBar.getUpdateBtn();
    if (updateBtn != null) {
      int toolbarStyle = mHorizontalToolBar.getToolbarStyle();
      if ((toolbarStyle & util.ui.Toolbar.TEXT) == util.ui.Toolbar.TEXT) {
        updateBtn.setText(TVBrowser.mLocalizer.msg("button.stop", "Stop"));
      }
      if ((toolbarStyle & util.ui.Toolbar.ICON) == util.ui.Toolbar.ICON) {
        updateBtn.setIcon(new ImageIcon("imgs/Stop24.gif"));
      }
    }
    updateMenuItem.setText(mLocalizer.msg("menuitem.stopUpdate", "Stop update..."));
  }



  private void onDownloadDone() {
    TvDataUpdater.getInstance().stopDownload();
    mStatusBar.getProgressBar().setValue(0);
    
    JButton updateBtn=mHorizontalToolBar.getUpdateBtn();
    if (updateBtn != null) {
      int toolbarStyle = mHorizontalToolBar.getToolbarStyle();
      if ((toolbarStyle & util.ui.Toolbar.TEXT) == util.ui.Toolbar.TEXT) {
        updateBtn.setText(TVBrowser.mLocalizer.msg("button.update", "Update"));
      }
      if ((toolbarStyle & util.ui.Toolbar.ICON) == util.ui.Toolbar.ICON) {
        updateBtn.setIcon(new ImageIcon("imgs/Refresh24.gif"));
      }
    }
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
    changeDate(FinderPanel.getInstance().getSelectedDate(), null, null);
  }


  private void changeDate(Date date, devplugin.ProgressMonitor monitor, Runnable callback) {      
    mProgramTableModel.setDate(date, monitor, callback);
    
    //if (date.equals(new devplugin.Date())) {
      // If this is today -> scroll to now
    //  scrollToNow();
    //}
  }



  /**
   * Implementation of Interface DateListener
   */
  public void dateChanged(final devplugin.Date date, devplugin.ProgressMonitor monitor, Runnable callback) { 
      changeDate(date, monitor, callback);
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

  
  public void updateChannelChooser() {
    mChannelChooser.updateChannelChooser();
    
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
    box.setSize(500,520);
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
            url=new java.net.URL("http://www.tvbrowser.org/plugins/plugins.txt");    
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



/**
 * Updates the TimeChooser-Buttons
 */
public void updateButtons() {
    mTimeChooser.updateButtons();
}



}