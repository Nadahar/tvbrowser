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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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
import tvbrowser.ui.filter.dlgs.SelectFilterDlg;
import tvbrowser.ui.finder.FinderPanel;
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
import devplugin.ProgramFilter;

import java.lang.reflect.Constructor;


/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class MainFrame extends JFrame implements /*ActionListener, */DateListener {

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
  private SkinPanel skinPanel;
  private tvbrowser.ui.mainframe.MainToolBar mToolBar;
  private StatusBar mStatusBar;

  private JMenu mPluginsMenu;
  
  private static MainFrame mSingleton;

  private TimeChooserPanel mTimeChooser;
  private ChannelChooserPanel mChannelChooser;
  private MenuBar mMenuBar;
  
 
  private MainFrame() {
    super(TVBrowser.MAINWINDOW_TITLE);
		
    String msg;
    Icon icon;

    mStatusBar = new StatusBar();
    
		if (System.getProperty("mrj.version") != null) {
			/* create the menu bar for MacOS X */
	    try {
        Class impl = Class.forName("tvbrowser.ui.mainframe.macosx.MacOSXMenuBar");
			  Class mainFrameClass = this.getClass();
			  Class jlabelClass = Class.forName("javax.swing.JLabel");
			  Constructor cons = impl.getConstructor(new Class[]{mainFrameClass, jlabelClass});
			  mMenuBar = (MenuBar)cons.newInstance(new Object[]{this, mStatusBar.getLabel()});
			}catch(Exception e) {
        mLog.warning("Could not instantiate MacOSXMenuBar\n"+e.toString());
				mMenuBar = new DefaultMenuBar(this, mStatusBar.getLabel());
				mLog.info("Using default menu bar");
			}
			
    }else {
	    mMenuBar = new DefaultMenuBar(this, mStatusBar.getLabel());
    }
    
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
        
    JLabel lb=mStatusBar.getLabel();
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
            new SplitWindow(false,0.25f,timeView,new SplitWindow(false,0.25f,dateView,channelView))));
    
    rootWindow.getRootWindowProperties().getSplitWindowProperties().setContinuousLayoutEnabled(true);
    
    rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);

    rootWindow.getRootWindowProperties().getWindowAreaProperties().setInsets(new Insets(0,0,0,0));
    rootWindow.getRootWindowProperties().getSplitWindowProperties().setDividerSize(2);
    rootWindow.getWindowBar(Direction.LEFT).getTabWindowProperties().getTabProperties().getNormalButtonProperties().getCloseButtonProperties().setVisible(false);
    
    rootWindow.getRootWindowProperties().getDockingWindowProperties().getTabProperties().
    getHighlightedButtonProperties().getCloseButtonProperties().setVisible(false);

    rootWindow.getRootWindowProperties().getWindowAreaProperties().setBackgroundColor(new Color(110, 130, 180));
        
    updateToolBar();
    
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

    setJMenuBar(mMenuBar);    
  }

  public JLabel getStatusBarLabel() {
    return mStatusBar.getLabel();
  }
  
  
  private void updateToolBar() {
    JPanel contentPane = (JPanel)getContentPane();
    String locationStr = Settings.propToolbarLocation.getString();
    String location=null;
    if ("hidden".equals(locationStr)) {
      location = null;  
    }
    else if ("east".equals(locationStr)) {
      location = BorderLayout.EAST;
    }else if ("south".equals(locationStr)) {
      location = BorderLayout.SOUTH;
    }else if ("west".equals(locationStr)) {
      location = BorderLayout.WEST;
    }else {
        location = BorderLayout.NORTH;
    }
    
    if (mToolBar!=null) {
      contentPane.remove(mToolBar);
    }
    
    mToolBar = new MainToolBar(this, new util.ui.toolbar.DefaultToolBarModel());
    mToolBar.setStatusLabel(mStatusBar.getLabel());    
    
    if (location!=null) {
      contentPane.add(mToolBar, location);
    }
      
  }
  
  public ProgramTableScrollPane getProgramTableScrollPane() {
    return mProgramTableScrollPane;
  }

  
  public void updateToolbar() {
    mToolBar.refresh();
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
  
  public void setProgramFilter(ProgramFilter filter) {
    mProgramTableModel.setProgramFilter(filter);
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
    mMenuBar.updatePluginsMenu();
  }
  

  public static void updatePluginsMenu(JMenu pluginsMenu, PluginProxy[] plugins) {
    pluginsMenu.removeAll();


    Arrays.sort(plugins, new Comparator() {

        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }

    });
    
    for (int i = 0; i < plugins.length; i++) {
      Action action = plugins[i].getButtonAction();
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
  
  public void storeSettings() {
    mToolBar.storeSettings();
  }

  public void scrollToTime(int time) {
    mProgramTableScrollPane.scrollToTime(time);
  }



  private void onDownloadStart() {
    mToolBar.showStopButton();
    mMenuBar.showStopMenuItem();
  }



  private void onDownloadDone() {
    TvDataUpdater.getInstance().stopDownload();
    mStatusBar.getProgressBar().setValue(0);
    
    mToolBar.showUpdateButton();
    mMenuBar.showUpdateMenuItem();

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
  public void showAboutBox() {
    AboutBox box=new AboutBox(this);
    box.setSize(500,520);
    UiUtilities.centerAndShow(box);
    box.dispose();
  }


  public void showUpdatePluginsDlg() {
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

public void showFilterDialog() {
  SelectFilterDlg dlg=new SelectFilterDlg(this);
  util.ui.UiUtilities.centerAndShow(dlg);
  mMenuBar.updateFiltersMenu();
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
/*
private JMenuItem[] createFilterMenuItems() {
  ButtonGroup group = new ButtonGroup();
  FilterList filterList = new FilterList();
  filterList.create();
  ProgramFilter[] filterArr = filterList.getFilterArr();
  JRadioButtonMenuItem[] result = new JRadioButtonMenuItem[filterArr.length];
  for (int i=0; i<filterArr.length; i++) {
    final ProgramFilter filter = filterArr[i];
    result[i] = new JRadioButtonMenuItem(filter.toString());
    final JRadioButtonMenuItem item = result[i];    
    group.add(item);
    result[i].addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent event) {
          mProgramTableModel.setProgramFilter(filter);
          item.setSelected(true);
        }});
  }
  
  return result;
}
*/
  /**
   * Updates the TimeChooser-Buttons
   */
  public void updateButtons() {
    mTimeChooser.updateButtons();
  }


  public void setShowToolbar(boolean visible) {
    if (visible) {
      Settings.propToolbarLocation.setString("north");
    }
    else {
      Settings.propToolbarLocation.setString("hidden");
    }
    updateToolBar();
  
  }

}