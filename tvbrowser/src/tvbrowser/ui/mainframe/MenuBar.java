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
 *     $Date$
 *   $Author$
 * $Revision$
 */


package tvbrowser.ui.mainframe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import tvbrowser.core.ChannelList;
import tvbrowser.core.DateListener;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.ButtonActionIf;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.extras.common.InternalPluginProxyList;
import tvbrowser.ui.filter.dlgs.EditFilterComponentDlg;
import tvbrowser.ui.filter.dlgs.FilterButtons;
import tvbrowser.ui.licensebox.LicenseBox;
import tvbrowser.ui.mainframe.toolbar.ContextMenu;
import tvbrowser.ui.settings.ToolBarDragAndDropSettings;
import util.browserlauncher.Launch;
import util.ui.FixedSizeIcon;
import util.ui.Localizer;
import util.ui.ScrollableMenu;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.ProgramFilter;
import devplugin.ProgressMonitor;
import devplugin.SettingsItem;


public abstract class MenuBar extends JMenuBar implements ActionListener, DateListener {
  
    /** The localizer for this class. */
    protected static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(MainFrame.class);
    
    
  private MainFrame mMainFrame;

  protected JMenuItem mSettingsMI, mQuitMI, mToolbarMI, mStatusbarMI, mTimeBtnsMI, mDatelistMI,
                    mChannellistMI, mPluginOverviewMI, mViewFilterBarMI, mUpdateMI,
                    mPluginManagerMI, mInstallPluginsMI, mDonorMI, mFaqMI, mBackupMI, mForumMI, mWebsiteMI, mHandbookMI, mDownloadMI,
                    mConfigAssistantMI, mAboutMI, mKeyboardShortcutsMI,
                    mPreviousDayMI, mNextDayMI, mPreviousWeekMI, mNextWeekMI, mTodayMI,
                    mGotoNowMenuItem, mEditTimeButtonsMenuItem,
                    mToolbarCustomizeMI, mFullscreenMI, mFontSizeLargerMI, mFontSizeSmallerMI,
                    mFontSizeDefaultMI, mColumnWidthLargerMI, mColumnWidthSmallerMI, 
                    mColumnWidthDefaultMI, mPluginInfoDlgMI;
  protected JMenu mFiltersMenu, mLicenseMenu, mGoMenu, mViewMenu, mToolbarMenu, mPluginHelpMenu;

  private JMenu mGotoDateMenu, mGotoChannelMenu, mGotoTimeMenu, mFontSizeMenu, mColumnWidthMenu, mChannelGroupMenu;
  
  /**
   * status bar label for menu help
   */
  private JLabel mLabel;
  
  protected MenuBar(MainFrame mainFrame, JLabel label) {
    mMainFrame = mainFrame;
    mLabel = label;
    createMenuItems();
    createMenuItemInfos();
  }

	protected MainFrame getMainFrame() {
		return mMainFrame;
	}
    
  public JLabel getLabel() {
    return mLabel;
  }
    
  public void showUpdateMenuItem() {
    mUpdateMI.setText(mLocalizer.msg("menuitem.update","Update"));
    mUpdateMI.setIcon(IconLoader.getInstance().getIconFromTheme("apps", "system-software-update", 16));
  }
  
  public void showStopMenuItem() {
      mUpdateMI.setText(mLocalizer.msg("menuitem.stopUpdate","Stop"));
      mUpdateMI.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "process-stop", 16));
  }
  
  private void createMenuItems() {
      
    mSettingsMI = new JMenuItem(mLocalizer.msg("menuitem.settings", "Settings..."), TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL));
    mSettingsMI.addActionListener(this);
    new MenuHelpTextAdapter(mSettingsMI, mLocalizer.msg("menuinfo.settings",""), mLabel); 

    mQuitMI = new JMenuItem(mLocalizer.msg("menuitem.exit", "Exit..."), TVBrowserIcons.quit(TVBrowserIcons.SIZE_SMALL));
    mQuitMI.addActionListener(this);
    new MenuHelpTextAdapter(mQuitMI, mLocalizer.msg("menuinfo.quit",""), mLabel);
    
    mToolbarMenu = new JMenu(mLocalizer.msg("menuitem.viewToolbar","Toolbar"));
    
    mToolbarMI = new JCheckBoxMenuItem(ToolBarDragAndDropSettings.mLocalizer.msg(
        "showToolbar", "Show toolbar"));
    mToolbarMI.setSelected(Settings.propIsTooolbarVisible.getBoolean());
    mToolbarMI.addActionListener(this);
    new MenuHelpTextAdapter(mToolbarMI, mLocalizer.msg("menuinfo.toolbar",""), mLabel);
    
    mToolbarCustomizeMI = new JMenuItem(ContextMenu.mLocalizer.msg("configure","Configure")+"...");
    mToolbarCustomizeMI.addActionListener(this);
    new MenuHelpTextAdapter(mToolbarCustomizeMI, mLocalizer.msg("menuinfo.customizeToolbar",""), mLabel);
    
    mToolbarMenu.add(mToolbarMI);
    mToolbarMenu.addSeparator();
    mToolbarMenu.add(mToolbarCustomizeMI);
    
    mStatusbarMI = new JCheckBoxMenuItem(mLocalizer.msg("menuitem.viewStatusbar","Statusbar"));
    mStatusbarMI.setSelected(Settings.propIsStatusbarVisible.getBoolean());
    mStatusbarMI.addActionListener(this);
    new MenuHelpTextAdapter(mStatusbarMI, mLocalizer.msg("menuinfo.statusbar",""), mLabel);
    
    mTimeBtnsMI = new JCheckBoxMenuItem(mLocalizer.msg("menuitem.timebuttons", "Time buttons"));
    mTimeBtnsMI.setSelected(Settings.propShowTimeButtons.getBoolean());
    mTimeBtnsMI.addActionListener(this);    
    new MenuHelpTextAdapter(mTimeBtnsMI, mLocalizer.msg("menuinfo.timebuttons",""), mLabel);
    
    mDatelistMI = new JCheckBoxMenuItem(mLocalizer.msg("menuitem.datelist","Date list"));
    mDatelistMI.setSelected(Settings.propShowDatelist.getBoolean());
    mDatelistMI.addActionListener(this);
    new MenuHelpTextAdapter(mDatelistMI, mLocalizer.msg("menuinfo.datelist",""), mLabel);

    mChannellistMI = new JCheckBoxMenuItem(mLocalizer.msg("menuitem.channellist","channel list"));
    mChannellistMI.setSelected(Settings.propShowChannels.getBoolean());
    mChannellistMI.addActionListener(this);
    new MenuHelpTextAdapter(mChannellistMI, mLocalizer.msg("menuinfo.channellist",""), mLabel);

    mPluginOverviewMI = new JCheckBoxMenuItem(mLocalizer.msg("menuitem.pluginOverview","Plugin overview"));
    mPluginOverviewMI.setSelected(Settings.propShowPluginView.getBoolean());
    mPluginOverviewMI.addActionListener(this);
    mPluginOverviewMI.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "view-tree", 16));
    new MenuHelpTextAdapter(mPluginOverviewMI, mLocalizer.msg("menuinfo.pluginOverview",""), mLabel);

    mViewFilterBarMI = new JCheckBoxMenuItem(mLocalizer.msg("menuitem.viewFilterBar","Filter bar"));
    mViewFilterBarMI.setSelected(Settings.propShowFilterBar.getBoolean());
    mViewFilterBarMI.addActionListener(this);
    new MenuHelpTextAdapter(mViewFilterBarMI, mLocalizer.msg("menuinfo.filterbar",""), mLabel);
    
    mFiltersMenu = new JMenu(mLocalizer.msg("menuitem.filters","Filter"));
    mFiltersMenu.setIcon(TVBrowserIcons.filter(TVBrowserIcons.SIZE_SMALL));
    updateFiltersMenu();
    
    mChannelGroupMenu = new JMenu(mLocalizer.msg("menuitem.channelgroup", "Channel group"));
    updateChannelGroupMenu();

    mGoMenu = new JMenu(mLocalizer.msg("menuitem.go","Go"));
    mGoMenu.setMnemonic(KeyEvent.VK_G);

    mPreviousDayMI = new JMenuItem(mLocalizer.msg("menuitem.previousDay","previous day"));
    mPreviousDayMI.addActionListener(this);
    mPreviousDayMI.setIcon(TVBrowserIcons.left(TVBrowserIcons.SIZE_LARGE));
    new MenuHelpTextAdapter(mPreviousDayMI, mLocalizer.msg("menuinfo.previousDay",""), mLabel);

    mNextDayMI = new JMenuItem(mLocalizer.msg("menuitem.nextDay","next day"));
    mNextDayMI.addActionListener(this);
    mNextDayMI.setIcon(TVBrowserIcons.right(TVBrowserIcons.SIZE_SMALL));
    new MenuHelpTextAdapter(mNextDayMI, mLocalizer.msg("menuinfo.nextDay",""), mLabel);

    mPreviousWeekMI = new JMenuItem(mLocalizer.msg("menuitem.previousWeek","previous week"));
    mPreviousWeekMI.addActionListener(this);
    mPreviousWeekMI.setIcon(TVBrowserIcons.previousWeek(TVBrowserIcons.SIZE_SMALL));
    new MenuHelpTextAdapter(mPreviousWeekMI, mLocalizer.msg("menuinfo.previousWeek",""), mLabel);

    mNextWeekMI = new JMenuItem(mLocalizer.msg("menuitem.nextWeek","next week"));
    mNextWeekMI.addActionListener(this);
    mNextWeekMI.setIcon(TVBrowserIcons.nextWeek(TVBrowserIcons.SIZE_SMALL));
    new MenuHelpTextAdapter(mNextWeekMI, mLocalizer.msg("menuinfo.nextWeek",""), mLabel);

    mTodayMI = new JMenuItem(mLocalizer.msg("menuitem.today","today"));
    mTodayMI.addActionListener(this);
    mTodayMI.setIcon(TVBrowserIcons.down(TVBrowserIcons.SIZE_SMALL));
    new MenuHelpTextAdapter(mTodayMI, mLocalizer.msg("menuinfo.today",""), mLabel);

    mGotoNowMenuItem = new JMenuItem(mLocalizer.msg("menuitem.now","now"));
    mGotoNowMenuItem.addActionListener(this);
    mGotoNowMenuItem.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "scroll-to-now", 16));
    new MenuHelpTextAdapter(mGotoNowMenuItem, mLocalizer.msg("menuinfo.now",""), mLabel);

    mGotoDateMenu = new JMenu(mLocalizer.msg("menuitem.date","date"));
    mGotoDateMenu.setIcon(IconLoader.getInstance().getIconFromTheme("apps", "office-calendar", 16));



    mGotoChannelMenu = new ScrollableMenu(Localizer.getLocalization(Localizer.I18N_CHANNEL));
    mGotoChannelMenu.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "scroll-to-channel", 16));
    mGotoTimeMenu = new JMenu(mLocalizer.msg("menuitem.time","time"));
    mGotoTimeMenu.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "scroll-to-time", 16));
    mGoMenu.add(mPreviousDayMI);
    mGoMenu.add(mNextDayMI);
    mGoMenu.add(mPreviousWeekMI);
    mGoMenu.add(mNextWeekMI);
    mGoMenu.add(mTodayMI);
    mGoMenu.addSeparator();
    mGoMenu.add(mGotoDateMenu);
    mGoMenu.add(mGotoChannelMenu);
    mGoMenu.add(mGotoTimeMenu);
    mGoMenu.addSeparator();
    mGoMenu.add(mGotoNowMenuItem);


    mViewMenu = new JMenu(mLocalizer.msg("menuitem.view","View"));

    mFullscreenMI = new JCheckBoxMenuItem(mLocalizer.msg("menuitem.fullscreen","Fullscreen"));
    mFullscreenMI.setIcon(TVBrowserIcons.fullScreen(TVBrowserIcons.SIZE_SMALL));
    mFullscreenMI.addActionListener(this);
    new MenuHelpTextAdapter(mFullscreenMI, mLocalizer.msg("menuinfo.fullscreen",""), mLabel);

    updateDateItems();
    updateChannelItems();
    updateTimeItems();
    
    mUpdateMI = new JMenuItem(mLocalizer.msg("menuitem.update", "Update..."), IconLoader.getInstance().getIconFromTheme("apps", "system-software-update", 16));
    mUpdateMI.addActionListener(this);
    new MenuHelpTextAdapter(mUpdateMI, mLocalizer.msg("menuinfo.update",""), mLabel);
    
    mLicenseMenu = createLicenseMenuItems();

    Icon urlHelpImg = IconLoader.getInstance().getIconFromTheme("apps", "help-browser", 16);
    Icon urlBrowserImg = IconLoader.getInstance().getIconFromTheme("apps", "internet-web-browser", 16);
    
    mInstallPluginsMI = new JMenuItem(mLocalizer.msg("menuitem.installPlugins","Install/Update Plugins..."),urlBrowserImg);
    mInstallPluginsMI.addActionListener(this);
    new MenuHelpTextAdapter(mInstallPluginsMI, mLocalizer.msg("menuinfo.installPlugins","Add additonal functions to TV-Browser/search for updates for installed Plugins"), mLabel);
    
    mPluginManagerMI = new JMenuItem(mLocalizer.msg("menuitem.managePlugins", "Manage Plugins"));
    mPluginManagerMI.addActionListener(this);
    mPluginManagerMI.setIcon(TVBrowserIcons.plugin(TVBrowserIcons.SIZE_SMALL));
    new MenuHelpTextAdapter(mPluginManagerMI, mLocalizer.msg("menuinfo.findplugins",""), mLabel);
        
    mHandbookMI=new JMenuItem(mLocalizer.msg("menuitem.handbook", "Handbook"),urlHelpImg); 
    mHandbookMI.addActionListener(this);
    new MenuHelpTextAdapter(mHandbookMI,mLocalizer.msg("website.handbook",""),mLabel); 
    
    mKeyboardShortcutsMI = new JMenuItem(mLocalizer.msg("menuitem.keyboardshortcuts","Keyboard shortcuts"),urlHelpImg);
    mKeyboardShortcutsMI.addActionListener(this);
    new MenuHelpTextAdapter(mKeyboardShortcutsMI,mLocalizer.msg("website.keyboardshortcuts",""),mLabel);

    mFaqMI=new JMenuItem(mLocalizer.msg("menuitem.faq", "FAQ"),urlHelpImg);   
    mFaqMI.addActionListener(this);
    new MenuHelpTextAdapter(mFaqMI,mLocalizer.msg("website.faq",""),mLabel); 
    
    mBackupMI=new JMenuItem(mLocalizer.msg("menuitem.backup", "Backup"),urlHelpImg);   
    mBackupMI.addActionListener(this);
    new MenuHelpTextAdapter(mBackupMI,mLocalizer.msg("website.backup",""),mLabel); 
    
    mWebsiteMI=new JMenuItem(mLocalizer.msg("menuitem.website","Website"),urlBrowserImg);
    mWebsiteMI.addActionListener(this);
    new MenuHelpTextAdapter(mWebsiteMI,mLocalizer.msg("website.tvbrowser",""),mLabel); 
    
    mForumMI=new JMenuItem(mLocalizer.msg("menuitem.forum","Bulletin board"),urlBrowserImg); 
    mForumMI.addActionListener(this);
    new MenuHelpTextAdapter(mForumMI,mLocalizer.msg("website.forum",""),mLabel); 
    
    mDownloadMI=new JMenuItem(mLocalizer.msg("menuitem.download","Download"),urlBrowserImg); 
    mDownloadMI.addActionListener(this);
    new MenuHelpTextAdapter(mForumMI,mLocalizer.msg("website.download",""),mLabel); 
    
    mDonorMI=new JMenuItem(mLocalizer.msg("menuitem.donors","Donors"), urlBrowserImg);
    mDonorMI.addActionListener(this);
    new MenuHelpTextAdapter(mDonorMI,mLocalizer.msg("website.donors",""),mLabel); 
    
    mConfigAssistantMI=new JMenuItem(mLocalizer.msg("menuitem.configAssistant","setup assistant"),TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL));
    mConfigAssistantMI.addActionListener(this);
    new MenuHelpTextAdapter(mConfigAssistantMI,mLocalizer.msg("menuinfo.configAssistant",""),mLabel);
    
    mPluginInfoDlgMI=new JMenuItem(mLocalizer.msg("menuitem.pluginInfoDlg","What are Plugins?"),urlHelpImg);
    mPluginInfoDlgMI.addActionListener(this);
    new MenuHelpTextAdapter(mPluginInfoDlgMI,mLocalizer.msg("menuinfo.pluginInfoDlg","Describes the Plugin functionality of TV-Browser."),mLabel);
    
    mAboutMI = new JMenuItem(mLocalizer.msg("menuitem.about", "About..."), new ImageIcon("imgs/tvbrowser16.png"));
    mAboutMI.addActionListener(this);
    new MenuHelpTextAdapter(mAboutMI, mLocalizer.msg("menuinfo.about",""), mLabel);
    
    mPluginHelpMenu = new JMenu(mLocalizer.msg("menuitem.pluginHelp","Plugin help"));
    mPluginHelpMenu.setIcon(urlHelpImg);
    updatePluginHelpMenuItems();
    
    mFontSizeLargerMI = new JMenuItem(mLocalizer.msg("menuitem.fontSizeLarger", "Larger"));
    mFontSizeLargerMI.addActionListener(this);
    mFontSizeLargerMI.setIcon(TVBrowserIcons.zoomIn(TVBrowserIcons.SIZE_SMALL));
    new MenuHelpTextAdapter(mFontSizeLargerMI, mLocalizer.msg("menuinfo.fontlarger",""), mLabel);
    
    mFontSizeSmallerMI = new JMenuItem(mLocalizer.msg("menuitem.fontSizeSmaller", "Smaller"));
    mFontSizeSmallerMI.addActionListener(this);
    mFontSizeSmallerMI.setIcon(TVBrowserIcons.zoomOut(TVBrowserIcons.SIZE_SMALL));
    new MenuHelpTextAdapter(mFontSizeSmallerMI, mLocalizer.msg("menuinfo.fontsmaller",""), mLabel);
    
    mFontSizeDefaultMI = new JMenuItem(mLocalizer.msg("menuitem.fontSizeDefault", "Reset to default"));
    mFontSizeDefaultMI.addActionListener(this);
    new MenuHelpTextAdapter(mFontSizeDefaultMI, mLocalizer.msg("menuinfo.fontdefault",""), mLabel);
    
    mFontSizeMenu = new JMenu(mLocalizer.msg("menuitem.fontSize", "Font size"));
    mFontSizeMenu.add(mFontSizeLargerMI);
    mFontSizeMenu.add(mFontSizeSmallerMI);
    mFontSizeMenu.addSeparator();
    mFontSizeMenu.add(mFontSizeDefaultMI);
    mFontSizeMenu.setIcon(TVBrowserIcons.zoomIn(TVBrowserIcons.SIZE_SMALL));
    
    mColumnWidthLargerMI = new JMenuItem(mLocalizer.msg("menuitem.columnWidthLarger", "Larger"));
    mColumnWidthLargerMI.addActionListener(this);
    new MenuHelpTextAdapter(mColumnWidthLargerMI, mLocalizer.msg("menuinfo.columnlarger",""), mLabel);
    
    mColumnWidthSmallerMI = new JMenuItem(mLocalizer.msg("menuitem.columnWidthSmaller", "Smaller"));
    mColumnWidthSmallerMI.addActionListener(this);
    new MenuHelpTextAdapter(mColumnWidthSmallerMI, mLocalizer.msg("menuinfo.columnsmaller",""), mLabel);
    
    mColumnWidthDefaultMI = new JMenuItem(mLocalizer.msg("menuitem.columnWidthDefault", "Reset to default"));
    mColumnWidthDefaultMI.addActionListener(this);
    new MenuHelpTextAdapter(mColumnWidthDefaultMI, mLocalizer.msg("menuinfo.columndefault",""), mLabel);
    
    mColumnWidthMenu = new JMenu(mLocalizer.msg("menuitem.columnWidth", "ColumnWidth"));
    mColumnWidthMenu.add(mColumnWidthLargerMI);
    mColumnWidthMenu.add(mColumnWidthSmallerMI);
    mColumnWidthMenu.addSeparator();
    mColumnWidthMenu.add(mColumnWidthDefaultMI);
    
    mViewMenu.add(mToolbarMenu);
    mViewMenu.add(mPluginOverviewMI);
    mViewMenu.add(mTimeBtnsMI);
    mViewMenu.add(mDatelistMI);
    mViewMenu.add(mChannellistMI);
    mViewMenu.add(mStatusbarMI);
    mViewMenu.add(mViewFilterBarMI);
    mViewMenu.addSeparator();
    mViewMenu.add(mFiltersMenu);
    mViewMenu.add(mChannelGroupMenu);
    mViewMenu.add(mFontSizeMenu);
    mViewMenu.add(mColumnWidthMenu);
    mViewMenu.addSeparator();
    mViewMenu.add(mFullscreenMI);
  }


  void updateChannelGroupMenu() {
    mChannelGroupMenu.removeAll();
    String channelFilterName = Settings.propLastUsedChannelGroup.getString();
    JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(mLocalizer.msg("channelGroupAll", "All channels"));
    menuItem.setSelected(channelFilterName == null);
    menuItem.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().setChannelGroup(null);
      }});
    mChannelGroupMenu.add(menuItem);
    String[] channelFilterNames = FilterComponentList.getInstance().getChannelFilterNames();
    for (final String filterName : channelFilterNames) {
      menuItem = new JRadioButtonMenuItem(filterName);
      menuItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          MainFrame.getInstance().setChannelGroup((ChannelFilterComponent) FilterComponentList.getInstance().getFilterComponentByName(filterName));
        }});
      mChannelGroupMenu.add(menuItem);
      if (channelFilterName != null && filterName.equals(channelFilterName)) {
        menuItem.setSelected(true);
      }
    }
    mChannelGroupMenu.add(new JSeparator());
    JMenuItem menuItemAdd = new JMenuItem(mLocalizer.msg("channelGroupNew", "Add channel group..."));
    menuItemAdd.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent e) {
        EditFilterComponentDlg dlg = new EditFilterComponentDlg(null, null, ChannelFilterComponent.class);
        FilterComponent rule = dlg.getFilterComponent();
        if ((rule != null) && (rule instanceof ChannelFilterComponent)) {
          FilterComponentList.getInstance().add(rule);
          FilterComponentList.getInstance().store();
          MainFrame.getInstance().setChannelGroup((ChannelFilterComponent) rule);
        }
      }});
    mChannelGroupMenu.add(menuItemAdd);
  }

  private JMenuItem createDateMenuItem(final Date date) {
    JMenuItem item = new JMenuItem(date.getLongDateString());
    item.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mMainFrame.goTo(date);
      }
    });
    return item;
  }

  private JMenuItem createChannelMenuItem(final Channel channel) {
    Icon icon = null;
    if (Settings.propShowChannelIconsInChannellist.getBoolean()) {
      icon = UiUtilities.createChannelIcon(channel.getIcon());
    }
    JMenuItem item = new JMenuItem(channel.getName(), icon);
    item.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mMainFrame.showChannel(channel);
      }
    });

    return item;
  }

  private JMenuItem createTimeMenuItem(final int time) {
    int h = time/60;
    int min = time%60;
    JMenuItem item = new JMenuItem((h<10?"0":"")+h+":"+(min<10?"0":"")+min);
    item.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mMainFrame.scrollToTime(time);
      }
    });
    return item;
  }

  protected JMenuItem[] createFilterMenuItems() {
      ButtonGroup group = new ButtonGroup();
      FilterList filterList = FilterList.getInstance();
      ProgramFilter[] filterArr = filterList.getFilterArr();
      JRadioButtonMenuItem[] result = new JRadioButtonMenuItem[filterArr.length];
      for (int i=0; i<filterArr.length; i++) {
        final ProgramFilter filter = filterArr[i];
        result[i] = new JRadioButtonMenuItem(filter.toString());
        final JRadioButtonMenuItem item = result[i];    
        group.add(item);
        result[i].addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
              mMainFrame.setProgramFilter(filter);
              item.setSelected(true);
            }});
      }
      result[0].setSelected(true);
      return result;
    }

  private JMenu createLicenseMenuItems() {
      
      JMenu licenseMenu = new JMenu(mLocalizer.msg("menuitem.license","Terms of Use..."));
      TvDataServiceProxy services[]= TvDataServiceProxyManager.getInstance().getDataServices();
      for (TvDataServiceProxy service : services) {
        final String license=service.getInfo().getLicense();
        if (license!=null) {
          JMenuItem item=new JMenuItem(service.getInfo().getName(),new ImageIcon("imgs/tvbrowser16.png"));
          item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
              LicenseBox box=new LicenseBox(mMainFrame, license, false);
              util.ui.UiUtilities.centerAndShow(box);
            }        
          });
          licenseMenu.add(item);
        }
      }
      if (licenseMenu.getItemCount() > 1) {
        licenseMenu.setText(mLocalizer.msg("menuitem.licenseMultiple","Terms of Use"));
        for (int i = 0; i < licenseMenu.getItemCount(); i++) {
          licenseMenu.getItem(i).setText(licenseMenu.getItem(i).getText()+"...");
        }
      }
      return licenseMenu;
    }
  
   public void updatePluginsMenu() {
     JMenuItem[] items = createPluginMenuItems();
     setPluginMenuItems(items);
   }

   public void updateTimeItems() {
     mGotoTimeMenu.removeAll();
     int[] times = Settings.propTimeButtons.getIntArray();
    for (int time : times) {
      mGotoTimeMenu.add(createTimeMenuItem(time));
    }
    mGotoTimeMenu.addSeparator();
    mEditTimeButtonsMenuItem = new JMenuItem(mLocalizer.msg("menuitem.editTimeItems","Edit Items..."));
    mEditTimeButtonsMenuItem.addActionListener(this);
    mGotoTimeMenu.add(mEditTimeButtonsMenuItem);
   }
   
   public void updateViewToolbarItem() {
     mToolbarMI.setSelected(Settings.propIsTooolbarVisible.getBoolean());
   }

   public void updateChannelItems() {
     mGotoChannelMenu.removeAll();
     Channel[] channels = ChannelList.getSubscribedChannels();
     for (Channel channel : channels) {
       mGotoChannelMenu.add(createChannelMenuItem(channel));
     }
   }

  public void updateDateItems() {
    mGotoDateMenu.removeAll();
    Date curDate = new Date();
    for (int i=0; i<21; i++) {
      if (!TvDataBase.getInstance().dataAvailable(curDate)) {
        break;
      }
      if (i > 0) {
        if (curDate.isFirstDayOfWeek()) {
          mGotoDateMenu.addSeparator();
        }
      }
      mGotoDateMenu.add(createDateMenuItem(curDate));
      curDate = curDate.addDays(1);
    }
    // update enable state of "goto previous/next" menu items after data download 
    if (! MainFrame.isStarting()) {
      dateChanged(mMainFrame.getCurrentSelectedDate(), null, null);
    }
  }

   public void updateFiltersMenu() {
       mFiltersMenu.removeAll();
       FilterButtons filterButtons = new FilterButtons(mMainFrame);
       JMenuItem[] filterMenuItems = filterButtons.createFilterMenuItems();
       for (JMenuItem menuItem : filterMenuItems) {
         if (menuItem != null) {
             mFiltersMenu.add(menuItem);
         } else {
             mFiltersMenu.addSeparator();
         }
       }
       mViewFilterBarMI.setEnabled(!mMainFrame.isDefaultFilterActivated());
   }
   
   protected abstract void setPluginMenuItems(JMenuItem[] items);


   private JMenuItem createMenuItem(ActionMenu menu) {
    JMenuItem result;
    if (menu.hasSubItems()) {
      result = new JMenu(menu.getTitle());
      
      if(menu.getAction().getValue(Action.SMALL_ICON) != null) {
        result.setIcon(new FixedSizeIcon(16, 16, (Icon) menu.getAction().getValue(Action.SMALL_ICON)));
      }
      
      ActionMenu[] subItems = menu.getSubItems();
      for (ActionMenu subItem : subItems) {
        final JMenuItem menuItem = createMenuItem(subItem);
        if (menuItem != null) {
          result.add(menuItem);
        } else {
          ((JMenu) result).addSeparator();
        }
      }
    }
    else {
      if (ContextMenuSeparatorAction.getInstance().equals(menu.getAction())) {
        return null;
      }
      result = new JMenuItem(menu.getAction());
      if(menu.getAction().getValue(Action.SMALL_ICON) != null) {
        result.setIcon(new FixedSizeIcon(16, 16, (Icon) menu.getAction().getValue(Action.SMALL_ICON)));
      }
    }
    
    if(menu.getAction().getValue(InternalPluginProxyIf.KEYBOARD_ACCELERATOR) != null) {
      if(menu.getAction().getValue(InternalPluginProxyIf.KEYBOARD_ACCELERATOR) instanceof KeyStroke) {
        result.setAccelerator((KeyStroke)menu.getAction().getValue(InternalPluginProxyIf.KEYBOARD_ACCELERATOR));
      }
    }
    
    return result;

  }

   protected JMenuItem[] createInternalPluginMenuItems() {
     InternalPluginProxyIf[] internalPlugins = InternalPluginProxyList.getInstance().getAvailableProxys();
     
     ArrayList<JMenuItem> list = new ArrayList<JMenuItem>();
     for(InternalPluginProxyIf internalPlugin : internalPlugins) {
       if(internalPlugin instanceof ButtonActionIf) {
         fillButtonActionList(list, (ButtonActionIf)internalPlugin);
       }
     }
     
     return createSortedArrayFromList(list);
   }
   
   protected JMenuItem[] createPluginMenuItems() {
     PluginProxy[] plugins = PluginProxyManager.getInstance()
         .getActivatedPlugins();

     ArrayList<JMenuItem> list = new ArrayList<JMenuItem>();
     for (PluginProxy plugin : plugins) {
       fillButtonActionList(list, plugin);
     }
     
     TvDataServiceProxy[] dataServices = TvDataServiceProxyManager.getInstance().getDataServices();
     
     for(TvDataServiceProxy dataService : dataServices) {
       fillButtonActionList(list, dataService);
     }
     
     return createSortedArrayFromList(list);
   }
   
   private void fillButtonActionList(ArrayList<JMenuItem> list, ButtonActionIf buttonActionIf) {
     ActionMenu actionMenu = buttonActionIf.getButtonAction();
     if (actionMenu != null) {
       JMenuItem item = createMenuItem(actionMenu);
       list.add(item);
       new MenuHelpTextAdapter(item,
           buttonActionIf.getButtonActionDescription(), mLabel);
     }
   }
   
   private JMenuItem[] createSortedArrayFromList(ArrayList<JMenuItem> itemList) {
     JMenuItem[] result = itemList.toArray(new JMenuItem[itemList.size()]);
     Arrays.sort(result, new Comparator<JMenuItem>() {

       public int compare(JMenuItem item1, JMenuItem item2) {
         return item1.getText().compareTo(item2.getText());
       }
     });
     
     return result;
   }

  protected void updatePluginHelpMenuItems() {
     mPluginHelpMenu.removeAll();
    PluginProxy[] plugins = PluginProxyManager.getInstance()
        .getActivatedPlugins();
    ArrayList<JMenuItem> list = new ArrayList<JMenuItem>();
    for (final PluginProxy plugin : plugins) {
      String helpUrl = plugin.getInfo().getHelpUrl();
      if (helpUrl == null) {
        helpUrl = PluginInfo.getHelpUrl(plugin.getId());
      }
      if(helpUrl != null) {
        JMenuItem item = pluginHelpMenuItem(plugin.getInfo().getName(), helpUrl);
        item.setIcon(plugin.getPluginIcon());
        list.add(item);
      }
    }
    JMenuItem[] result = list.toArray(new JMenuItem[list.size()]);
    Arrays.sort(result, new Comparator<JMenuItem>() {

      public int compare(JMenuItem item1, JMenuItem item2) {
        return item1.getText().compareTo(item2.getText());
      }
    });
    
    InternalPluginProxyIf[] internalPlugins = InternalPluginProxyList.getInstance().getAvailableProxys();
    
    for(InternalPluginProxyIf internalPlugin : internalPlugins) {
      JMenuItem item = pluginHelpMenuItem(internalPlugin.toString(), PluginInfo.getHelpUrl(internalPlugin.getId()));
      item.setIcon(internalPlugin.getIcon());
      mPluginHelpMenu.add(item);
    }
    
    if(result.length > 0) {
      mPluginHelpMenu.addSeparator();
    }
    
    for (JMenuItem pluginMenuItem : result) {
      mPluginHelpMenu.add(pluginMenuItem);
    }
  }

  private JMenuItem pluginHelpMenuItem(final String name, final String helpUrl) {
    JMenuItem item = new JMenuItem(name);
    item.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        Launch.openURL(helpUrl);
      }});
    return item;
  }

  public void setPluginViewItemChecked(boolean selected) {
    mPluginOverviewMI.setSelected(selected);
  }
  
  public void setFullscreenItemChecked(boolean selected) {
    mFullscreenMI.setSelected(selected);
  }

   public void actionPerformed(ActionEvent event) {
     Object source = event.getSource();
     if (source == mSettingsMI) {
       mMainFrame.showSettingsDialog();
     }
     else if (source == mTimeBtnsMI) {
       mMainFrame.setShowTimeButtons(mTimeBtnsMI.isSelected());   
     }     
     else if (source == mQuitMI) {
       mMainFrame.quit();
     }
     else if (source == mToolbarMI) {
       mMainFrame.setShowToolbar(mToolbarMI.isSelected());
     }
     else if (source == mStatusbarMI) {
       mMainFrame.setShowStatusbar(mStatusbarMI.isSelected());
     }
     else if (source == mViewFilterBarMI) {
       mMainFrame.updateFilterPanel();
     }
     else if (source == mDatelistMI) {
       mMainFrame.setShowDatelist(mDatelistMI.isSelected());  
     }
     else if (source == mChannellistMI) {
       mMainFrame.setShowChannellist(mChannellistMI.isSelected());  
     }
     else if (source == mPluginOverviewMI) {
       boolean selected = mPluginOverviewMI.isSelected();
       mMainFrame.setShowPluginOverview(selected);
       mMainFrame.setPluginViewButton(selected);
     }
     else if (source == mFullscreenMI) {
       mMainFrame.switchFullscreenMode();
     }
     
     else if (source == mUpdateMI) {
       mMainFrame.updateTvData();
     }
     else if (source == mPluginManagerMI) {
       mMainFrame.showSettingsDialog(SettingsItem.PLUGINS);
     }
     else if (source == mInstallPluginsMI) {
       mMainFrame.showUpdatePluginsDlg(true);
     }
     else if (source == mHandbookMI) {
       Launch.openURL(mLocalizer.msg("website.handbook",""));
     }
     else if (source == mKeyboardShortcutsMI) {
       Launch.openURL(mLocalizer.msg("website.keyboardshortcuts",""));
     }
     else if (source == mFaqMI) {
       Launch.openURL(mLocalizer.msg("website.faq",""));
     }
     else if (source == mBackupMI) {
       Launch.openURL(mLocalizer.msg("website.backup",""));
     }
     else if (source == mWebsiteMI) {
       Launch.openURL(mLocalizer.msg("website.tvbrowser",""));
     } 
     else if (source == mForumMI) {
       Launch.openURL(mLocalizer.msg("website.forum",""));
     }
     else if (source == mDownloadMI) {
       Launch.openURL(mLocalizer.msg("website.download",""));
     } 
     else if (source == mDonorMI) {
       Launch.openURL(mLocalizer.msg("website.donors",""));
     }
     else if (source == mConfigAssistantMI) {
         mMainFrame.runSetupAssistant();
     }
     else if (source == mPluginInfoDlgMI) {
       mMainFrame.showPluginInfoDlg();
     } 
     else if (source == mAboutMI) {
       mMainFrame.showAboutBox();
     }
     else if (source == mPreviousDayMI) {
       mMainFrame.goToPreviousDay();
     }
     else if (source == mNextDayMI) {
       mMainFrame.goToNextDay();
     }
     else if (source == mPreviousWeekMI) {
       mMainFrame.goToPreviousWeek();
     }
     else if (source == mNextWeekMI) {
       mMainFrame.goToNextWeek();
     }
     else if (source == mTodayMI) {
       mMainFrame.goToToday();
     }
     else if (source == mGotoNowMenuItem) {
       mMainFrame.scrollToNow();
     }
     else if (source == mEditTimeButtonsMenuItem) {
       mMainFrame.showSettingsDialog(SettingsItem.TIMEBUTTONS);
     }
     else if (source == mToolbarCustomizeMI) {
       new ToolBarDragAndDropSettings();
     }
     else if (source == mFontSizeLargerMI) {
       mMainFrame.changeFontSize(+1);
     }
     else if (source == mFontSizeSmallerMI) {
       mMainFrame.changeFontSize(-1);
     }
     else if (source == mFontSizeDefaultMI) {
       mMainFrame.changeFontSize(0);
     }
     else if (source == mColumnWidthLargerMI) {
       mMainFrame.changeColumnWidth(1);
     }
     else if (source == mColumnWidthSmallerMI) {
       mMainFrame.changeColumnWidth(-1);
     }
     else if (source == mColumnWidthDefaultMI) {
       mMainFrame.changeColumnWidth(0);
     }
   }
   
   private void createMenuItemInfos() {
     
     
   }

  public void dateChanged(Date date, ProgressMonitor monitor, Runnable callback) {
    mPreviousDayMI.setEnabled(TvDataBase.getInstance().dataAvailable(
        date.addDays(-1)));
    mNextDayMI.setEnabled(TvDataBase.getInstance().dataAvailable(
        date.addDays(1)));
    mPreviousWeekMI.setEnabled(TvDataBase.getInstance().dataAvailable(
        date.addDays(-7)));
    mNextWeekMI.setEnabled(TvDataBase.getInstance().dataAvailable(
        date.addDays(7)));
  }

  public boolean isShowFilterPanelEnabled() {
    return mViewFilterBarMI.isSelected();
  }

  protected void createHelpMenuItems(JMenu helpMenu, boolean showAbout) {
    helpMenu.add(mHandbookMI);
    helpMenu.add(mKeyboardShortcutsMI);
    helpMenu.add(mFaqMI);
    helpMenu.add(mBackupMI);
    helpMenu.add(mPluginHelpMenu);
    helpMenu.add(mPluginInfoDlgMI);
    helpMenu.addSeparator();
    helpMenu.add(mWebsiteMI);
    helpMenu.add(mForumMI);
    helpMenu.add(mDownloadMI);
    helpMenu.add(mDonorMI);
    helpMenu.addSeparator();
    helpMenu.add(mConfigAssistantMI);
    if (showAbout) {
      helpMenu.addSeparator();
      helpMenu.add(mAboutMI);
    }
  }

}