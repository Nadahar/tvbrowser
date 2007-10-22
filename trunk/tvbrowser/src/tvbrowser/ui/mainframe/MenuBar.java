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

import tvbrowser.core.ChannelList;
import tvbrowser.core.DateListener;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import tvbrowser.extras.searchplugin.SearchPlugin;
import tvbrowser.extras.searchplugin.SearchPluginProxy;
import tvbrowser.ui.filter.dlgs.EditFilterComponentDlg;
import tvbrowser.ui.filter.dlgs.FilterButtons;
import tvbrowser.ui.licensebox.LicenseBox;
import tvbrowser.ui.mainframe.toolbar.ContextMenu;
import tvbrowser.ui.settings.ToolBarDragAndDropSettings;
import util.browserlauncher.Launch;
import util.ui.FixedSizeIcon;
import util.ui.Localizer;
import util.ui.ScrollableMenu;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.ProgramFilter;
import devplugin.ProgressMonitor;
import devplugin.SettingsItem;


public abstract class MenuBar extends JMenuBar implements ActionListener, DateListener {
  
    /** The localizer for this class. */
    public static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(MainFrame.class);
    
    
  private MainFrame mMainFrame;

  protected JMenuItem mSettingsMI, mQuitMI, mToolbarMI, mStatusbarMI, mTimeBtnsMI, mDatelistMI,
                    mChannellistMI, mPluginOverviewMI, mViewFilterBarMI, mUpdateMI,
                    mPluginManagerMI, mDonorMI, mFaqMI, mForumMI, mWebsiteMI, mHandbookMI,
                    mConfigAssistantMI, mAboutMI, mKeyboardShortcutsMI,
                    mPreviousDayMI, mNextDayMI, mGotoNowMenuItem, mEditTimeButtonsMenuItem,
                    mToolbarCustomizeMI,
                    mFavoritesMI, mReminderMI, mFullscreenMI, mSearchMI,
                    mFontSizeLargerMI, mFontSizeSmallerMI, mFontSizeDefaultMI,
                    mColumnWidthLargerMI, mColumnWidthSmallerMI, mColumnWidthDefaultMI;
  protected JMenu mFiltersMenu, mPluginsViewMenu, mLicenseMenu, mGoMenu, mViewMenu, mToolbarMenu, mPluginHelpMenu;

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
      
    mSettingsMI = new JMenuItem(mLocalizer.msg("menuitem.settings", "Settings..."), IconLoader.getInstance().getIconFromTheme("category", "preferences-desktop", 16));
    mSettingsMI.addActionListener(this);
    new MenuHelpTextAdapter(mSettingsMI, mLocalizer.msg("menuinfo.settings",""), mLabel); 

    mQuitMI = new JMenuItem(mLocalizer.msg("menuitem.exit", "Exit..."));
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
    mFiltersMenu.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "view-filter", 16));
    updateFiltersMenu();
    
    mChannelGroupMenu = new JMenu(mLocalizer.msg("menuitem.channelgroup", "Channel group"));
    updateChannelGroupMenu();

    mGoMenu = new JMenu(mLocalizer.msg("menuitem.go","Go"));
    mGoMenu.setMnemonic(KeyEvent.VK_G);

    mPreviousDayMI = new JMenuItem(mLocalizer.msg("menuitem.previousDay","previous day"));
    mPreviousDayMI.addActionListener(this);
    mPreviousDayMI.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "go-to-previous-day", 16));
    new MenuHelpTextAdapter(mPreviousDayMI, mLocalizer.msg("menuinfo.previousDay",""), mLabel);

    mNextDayMI = new JMenuItem(mLocalizer.msg("menuitem.nextDay","next day"));
    mNextDayMI.addActionListener(this);
    mNextDayMI.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "go-to-next-day", 16));
    new MenuHelpTextAdapter(mNextDayMI, mLocalizer.msg("menuinfo.nextDay",""), mLabel);

    mGotoNowMenuItem = new JMenuItem(mLocalizer.msg("menuitem.now","now"));
    mGotoNowMenuItem.addActionListener(this);
    mGotoNowMenuItem.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "scroll-to-now", 16));
    new MenuHelpTextAdapter(mGotoNowMenuItem, mLocalizer.msg("menuinfo.now",""), mLabel);

    mGotoDateMenu = new JMenu(mLocalizer.msg("menuitem.date","date"));
    mGotoDateMenu.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "go-to-date", 16));



    mGotoChannelMenu = new ScrollableMenu(Localizer.getLocalization(Localizer.I18N_CHANNEL));
    mGotoChannelMenu.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "scroll-to-channel", 16));
    mGotoTimeMenu = new JMenu(mLocalizer.msg("menuitem.time","time"));
    mGotoTimeMenu.setIcon(IconLoader.getInstance().getIconFromTheme("apps", "scroll-to-time", 16));
    mGoMenu.add(mPreviousDayMI);
    mGoMenu.add(mNextDayMI);
    mGoMenu.addSeparator();
    mGoMenu.add(mGotoDateMenu);
    mGoMenu.add(mGotoChannelMenu);
    mGoMenu.add(mGotoTimeMenu);
    mGoMenu.addSeparator();
    mGoMenu.add(mGotoNowMenuItem);


    mViewMenu = new JMenu(mLocalizer.msg("menuitem.view","View"));

    mFullscreenMI = new JCheckBoxMenuItem(mLocalizer.msg("menuitem.fullscreen","Fullscreen"));
    mFullscreenMI.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "view-fullscreen", 16));
    mFullscreenMI.addActionListener(this);
    new MenuHelpTextAdapter(mFullscreenMI, mLocalizer.msg("menuinfo.fullscreen",""), mLabel);

    updateDateItems();
    updateChannelItems();
    updateTimeItems();
    
    mUpdateMI = new JMenuItem(mLocalizer.msg("menuitem.update", "Update..."), IconLoader.getInstance().getIconFromTheme("apps", "system-software-update", 16));
    mUpdateMI.addActionListener(this);
    new MenuHelpTextAdapter(mUpdateMI, mLocalizer.msg("menuinfo.update",""), mLabel);
    
    mLicenseMenu = createLicenseMenuItems();
    
    mPluginManagerMI = new JMenuItem(mLocalizer.msg("menuitem.managePlugins", "Manage Plugins"));
    mPluginManagerMI.addActionListener(this);
    mPluginManagerMI.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "view-plugins", 16));
    new MenuHelpTextAdapter(mPluginManagerMI, mLocalizer.msg("menuinfo.findplugins",""), mLabel);
    
    Icon urlHelpImg = IconLoader.getInstance().getIconFromTheme("apps", "help-browser", 16);
    Icon urlBrowserImg = IconLoader.getInstance().getIconFromTheme("apps", "internet-web-browser", 16);
    
    mDonorMI=new JMenuItem(mLocalizer.msg("menuitem.donors","Donors"), urlBrowserImg);
    mDonorMI.addActionListener(this);
    new MenuHelpTextAdapter(mDonorMI,mLocalizer.msg("website.donors",""),mLabel); 
    
    mFaqMI=new JMenuItem("FAQ",urlHelpImg);   
    mFaqMI.addActionListener(this);
    new MenuHelpTextAdapter(mFaqMI,mLocalizer.msg("website.faq",""),mLabel); 
    
    mForumMI=new JMenuItem("Forum",urlBrowserImg); 
    mForumMI.addActionListener(this);
    new MenuHelpTextAdapter(mForumMI,mLocalizer.msg("website.forum",""),mLabel); 
    
    mHandbookMI=new JMenuItem(mLocalizer.msg("menuitem.handbook", "Handbook"),urlHelpImg); 
    mHandbookMI.addActionListener(this);
    new MenuHelpTextAdapter(mHandbookMI,mLocalizer.msg("website.handbook",""),mLabel); 
    
    mWebsiteMI=new JMenuItem("Website",urlBrowserImg);
    mWebsiteMI.addActionListener(this);
    new MenuHelpTextAdapter(mWebsiteMI,mLocalizer.msg("website.tvbrowser",""),mLabel); 
    
    mConfigAssistantMI=new JMenuItem(mLocalizer.msg("menuitem.configAssistant","setup assistant"),IconLoader.getInstance().getIconFromTheme("category", "preferences-desktop", 16));
    mConfigAssistantMI.addActionListener(this);
    new MenuHelpTextAdapter(mConfigAssistantMI,mLocalizer.msg("menuinfo.configAssistant",""),mLabel);
    
    mAboutMI = new JMenuItem(mLocalizer.msg("menuitem.about", "About..."), new ImageIcon("imgs/tvbrowser16.png"));
    mAboutMI.addActionListener(this);
    new MenuHelpTextAdapter(mAboutMI, mLocalizer.msg("menuinfo.about",""), mLabel);
    
    mKeyboardShortcutsMI = new JMenuItem(mLocalizer.msg("menuitem.keyboardshortcuts","Keyboard shortcuts"),urlHelpImg);
    mKeyboardShortcutsMI.addActionListener(this);
    new MenuHelpTextAdapter(mKeyboardShortcutsMI,mLocalizer.msg("website.keyboardshortcuts",""),mLabel);

    mPluginHelpMenu = new JMenu(mLocalizer.msg("menu.plugins","Plugins"));
    mPluginHelpMenu.setIcon(urlHelpImg);
    updatePluginHelpMenuItems();

    mFavoritesMI = new JMenuItem(FavoritesPlugin.getInstance().getButtonAction(mMainFrame).getAction());
    mReminderMI = new JMenuItem(ReminderPlugin.getInstance().getButtonAction(mMainFrame).getAction());
    mSearchMI = new JMenuItem(SearchPlugin.getInstance().getButtonAction().getAction());
    
    mFontSizeLargerMI = new JMenuItem(mLocalizer.msg("menuitem.fontSizeLarger", "Larger"));
    mFontSizeLargerMI.addActionListener(this);
    mFontSizeLargerMI.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "zoom-in", 16));
    new MenuHelpTextAdapter(mFontSizeLargerMI, mLocalizer.msg("menuinfo.fontlarger",""), mLabel);
    
    mFontSizeSmallerMI = new JMenuItem(mLocalizer.msg("menuitem.fontSizeSmaller", "Smaller"));
    mFontSizeSmallerMI.addActionListener(this);
    mFontSizeSmallerMI.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "zoom-out", 16));
    new MenuHelpTextAdapter(mFontSizeSmallerMI, mLocalizer.msg("menuinfo.fontsmaller",""), mLabel);
    
    mFontSizeDefaultMI = new JMenuItem(mLocalizer.msg("menuitem.fontSizeDefault", "Reset to default"));
    mFontSizeDefaultMI.addActionListener(this);
    new MenuHelpTextAdapter(mFontSizeDefaultMI, mLocalizer.msg("menuinfo.fontdefault",""), mLabel);
    
    mFontSizeMenu = new JMenu(mLocalizer.msg("menuitem.fontSize", "Font size"));
    mFontSizeMenu.add(mFontSizeLargerMI);
    mFontSizeMenu.add(mFontSizeSmallerMI);
    mFontSizeMenu.addSeparator();
    mFontSizeMenu.add(mFontSizeDefaultMI);
    mFontSizeMenu.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "zoom-in", 16));
    
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
        result.add(createMenuItem(subItem));
      }
    }
    else {
      result = new JMenuItem(menu.getAction());
      if(menu.getAction().getValue(Action.SMALL_ICON) != null) {
        result.setIcon(new FixedSizeIcon(16, 16, (Icon) menu.getAction().getValue(Action.SMALL_ICON)));
      }
    }
    return result;

  }

   protected JMenuItem[] createPluginMenuItems() {
		PluginProxy[] plugins = PluginProxyManager.getInstance()
				.getActivatedPlugins();
		ArrayList<JMenuItem> list = new ArrayList<JMenuItem>();
		for (PluginProxy plugin : plugins) {
			ActionMenu actionMenu = plugin.getButtonAction();
			if (actionMenu != null) {
				JMenuItem item = createMenuItem(actionMenu);
				list.add(item);
				new MenuHelpTextAdapter(item,
						plugin.getInfo().getDescription(), mLabel);
			}
		}
		JMenuItem[] result = list.toArray(new JMenuItem[list.size()]);
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
      if(plugin.getInfo().getHelpUrl() != null) {
        JMenuItem item = pluginHelpMenuItem(plugin.getInfo());
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
    
    JMenuItem item = pluginHelpMenuItem(new PluginInfo(FavoritesPluginProxy.class, FavoritesPluginProxy.getInstance().toString(),null,null,mLocalizer.msg("menuitem.helpFavorites", "Favorites")));
    item.setIcon(FavoritesPluginProxy.getInstance().getMarkIcon());
    mPluginHelpMenu.add(item);
    item = pluginHelpMenuItem(new PluginInfo(ReminderPluginProxy.class, ReminderPluginProxy.getInstance().toString(),null,null,mLocalizer.msg("menuitem.helpReminder", "Reminder")));
    item.setIcon(ReminderPluginProxy.getInstance().getMarkIcon());
    mPluginHelpMenu.add(item);
    item = pluginHelpMenuItem(new PluginInfo(SearchPluginProxy.class, SearchPluginProxy.getInstance().toString(),null,null,mLocalizer.msg("menuitem.helpSearch", "Search")));
    item.setIcon((Icon) SearchPlugin.getInstance().getButtonAction().getAction().getValue(Action.SMALL_ICON));
    mPluginHelpMenu.add(item);
    
    if(result.length > 0) {
      mPluginHelpMenu.addSeparator();
    }
    
    for (JMenuItem pluginMenuItem : result) {
      mPluginHelpMenu.add(pluginMenuItem);
    }
  }

  private JMenuItem pluginHelpMenuItem(final PluginInfo info) {
    JMenuItem item = new JMenuItem(info.getName());
    item.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        Launch.openURL(info.getHelpUrl());
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
//       mMainFrame.showUpdatePluginsDlg();
       mMainFrame.showSettingsDialog(SettingsItem.PLUGINS);
     }
     else if (source == mDonorMI) {
       Launch.openURL(mLocalizer.msg("website.donors",""));
     }
     else if (source == mFaqMI) {
       Launch.openURL(mLocalizer.msg("website.faq",""));
     }
     else if (source == mForumMI) {
       Launch.openURL(mLocalizer.msg("website.forum",""));
     }
     else if (source == mWebsiteMI) {
       Launch.openURL(mLocalizer.msg("website.tvbrowser",""));
     } 
     else if (source == mHandbookMI) {
       Launch.openURL(mLocalizer.msg("website.handbook",""));
     }
     else if (source == mKeyboardShortcutsMI) {
       Launch.openURL(mLocalizer.msg("website.keyboardshortcuts",""));
     }
     else if (source == mConfigAssistantMI) {
         mMainFrame.runSetupAssistant();
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
    mPreviousDayMI.setEnabled(TvDataBase.getInstance().dataAvailable(date.addDays(-1)));
    mNextDayMI.setEnabled(TvDataBase.getInstance().dataAvailable(date.addDays(1)));
}

public boolean isShowFilterPanelEnabled() {
  return mViewFilterBarMI.isSelected();
}



}