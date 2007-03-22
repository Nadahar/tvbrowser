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

import tvbrowser.core.ChannelList;
import tvbrowser.core.DateListener;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.extras.searchplugin.SearchPlugin;
import tvbrowser.ui.filter.dlgs.FilterButtons;
import tvbrowser.ui.licensebox.LicenseBox;
import tvbrowser.ui.mainframe.toolbar.ContextMenu;
import tvbrowser.ui.settings.ToolBarDragAndDropSettings;
import util.browserlauncher.Launch;
import util.ui.Localizer;
import util.ui.ScrollableMenu;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.ProgramFilter;
import devplugin.ProgressMonitor;
import devplugin.SettingsItem;


public abstract class MenuBar extends JMenuBar implements ActionListener, DateListener {
  
    /** The localizer for this class. */
    public static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(MainFrame.class);
    
    
  private MainFrame mMainFrame;

  protected JMenuItem mSettingsMI, mQuitMI, mToolbarMI, mStatusbarMI, mTimeBtnsMI, mDatelistMI,
                    mChannellistMI, mPluginOverviewMI, mUpdateMI,
                    mPluginManagerMI, mDonorMI, mFaqMI, mForumMI, mWebsiteMI, mHandbookMI,
                    mConfigAssistantMI, mAboutMI, mKeyboardShortcutsMI,
                    mPreviousDayMI, mNextDayMI, mGotoNowMenuItem, mEditTimeButtonsMenuItem,
                    mToolbarCustomizeMI,
                    mFavoritesMI, mReminderMI, mFullscreenMI, mSearchMI;
  protected JMenu mFiltersMenu, mPluginsViewMenu, mLicenseMenu, mGoMenu, mViewMenu, mToolbarMenu;

  private JMenu mGotoDateMenu, mGotoChannelMenu, mGotoTimeMenu;
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
    mQuitMI = new JMenuItem(mLocalizer.msg("menuitem.exit", "Exit..."));
    mQuitMI.addActionListener(this);
    
    mToolbarMenu = new JMenu(mLocalizer.msg("menuitem.viewToolbar","Toolbar"));
    
    mToolbarMI = new JCheckBoxMenuItem(ToolBarDragAndDropSettings.mLocalizer.msg(
        "showToolbar", "Show toolbar"));
    mToolbarMI.setSelected(Settings.propIsTooolbarVisible.getBoolean());
    mToolbarMI.addActionListener(this);
    
    mToolbarCustomizeMI = new JMenuItem(ContextMenu.mLocalizer.msg("configure","Configure")+"...");
    mToolbarCustomizeMI.addActionListener(this);
    
    mToolbarMenu.add(mToolbarMI);
    mToolbarMenu.addSeparator();
    mToolbarMenu.add(mToolbarCustomizeMI);
    
    mStatusbarMI = new JCheckBoxMenuItem(mLocalizer.msg("menuitem.viewStatusbar","Statusbar"));
    mStatusbarMI.setSelected(Settings.propIsStatusbarVisible.getBoolean());
    mStatusbarMI.addActionListener(this);
    
    mTimeBtnsMI = new JCheckBoxMenuItem(mLocalizer.msg("menuitem.timebuttons", "Time buttons"));
    mTimeBtnsMI.setSelected(Settings.propShowTimeButtons.getBoolean());
    mTimeBtnsMI.addActionListener(this);    
    
    mDatelistMI = new JCheckBoxMenuItem(mLocalizer.msg("menuitem.datelist","Date list"));
    mDatelistMI.setSelected(Settings.propShowDatelist.getBoolean());
    mDatelistMI.addActionListener(this);
    mChannellistMI = new JCheckBoxMenuItem(mLocalizer.msg("menuitem.channellist","channel list"));
    mChannellistMI.setSelected(Settings.propShowChannels.getBoolean());
    mChannellistMI.addActionListener(this);
    mPluginOverviewMI = new JCheckBoxMenuItem(mLocalizer.msg("menuitem.pluginOverview","Plugin overview"));
    mPluginOverviewMI.setSelected(Settings.propShowPluginView.getBoolean());
    mPluginOverviewMI.addActionListener(this);
    
    mFiltersMenu = new JMenu(mLocalizer.msg("menuitem.filters","Filter"));
    updateFiltersMenu();

    mGoMenu = new JMenu(mLocalizer.msg("menuitem.go","Go"));
    mGoMenu.setMnemonic(KeyEvent.VK_G);

    mPreviousDayMI = new JMenuItem(mLocalizer.msg("menuitem.previousDay","previous day"));
    mPreviousDayMI.addActionListener(this);
    mNextDayMI = new JMenuItem(mLocalizer.msg("menuitem.nextDay","next day"));
    mNextDayMI.addActionListener(this);
    mGotoNowMenuItem = new JMenuItem(mLocalizer.msg("menuitem.now","now"));
    mGotoNowMenuItem.addActionListener(this);
    mGotoDateMenu = new JMenu(mLocalizer.msg("menuitem.date","date"));



    mGotoChannelMenu = new ScrollableMenu(Localizer.getLocalization(Localizer.I18N_CHANNEL));
    mGotoTimeMenu = new JMenu(mLocalizer.msg("menuitem.time","time"));
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
    mFullscreenMI.addActionListener(this);

    updateDateItems();
    updateChannelItems();
    updateTimeItems();
    
    mUpdateMI = new JMenuItem(mLocalizer.msg("menuitem.update", "Update..."), IconLoader.getInstance().getIconFromTheme("apps", "system-software-update", 16));
    mUpdateMI.addActionListener(this);
    
    mLicenseMenu = createLicenseMenuItems();
    
    mPluginManagerMI = new JMenuItem(mLocalizer.msg("menuitem.managePlugins", "Manage Plugins"));
    mPluginManagerMI.addActionListener(this);
    
    Icon urlHelpImg = IconLoader.getInstance().getIconFromTheme("apps", "help-browser", 16);
    Icon urlBrowserImg = IconLoader.getInstance().getIconFromTheme("apps", "internet-web-browser", 16);
    
    mDonorMI=new JMenuItem(mLocalizer.msg("menuitem.donors","Donors"), urlBrowserImg);
    mDonorMI.addActionListener(this);
    
    mFaqMI=new JMenuItem("FAQ",urlHelpImg);   
    mFaqMI.addActionListener(this);
    
    mForumMI=new JMenuItem("Forum",urlBrowserImg); 
    mForumMI.addActionListener(this);
    
    mHandbookMI=new JMenuItem(mLocalizer.msg("menuitem.handbook", "Handbook"),urlHelpImg); 
    mHandbookMI.addActionListener(this);
    
    mWebsiteMI=new JMenuItem("Website",urlBrowserImg);
    mWebsiteMI.addActionListener(this);
    
    mConfigAssistantMI=new JMenuItem(mLocalizer.msg("menuitem.configAssistant","setup assistant"),IconLoader.getInstance().getIconFromTheme("category", "preferences-desktop", 16));
    mConfigAssistantMI.addActionListener(this);
    
    mAboutMI = new JMenuItem(mLocalizer.msg("menuitem.about", "About..."), new ImageIcon("imgs/tvbrowser16.png"));
    mAboutMI.addActionListener(this);
    
    mKeyboardShortcutsMI = new JMenuItem(mLocalizer.msg("menuitem.keyboardshortcuts","Keyboard shortcuts"),urlHelpImg);
    mKeyboardShortcutsMI.addActionListener(this);

    mFavoritesMI = new JMenuItem(FavoritesPlugin.getInstance().getButtonAction(mMainFrame).getAction());
    mReminderMI = new JMenuItem(ReminderPlugin.getInstance().getButtonAction(mMainFrame).getAction());
    mSearchMI = new JMenuItem(SearchPlugin.getInstance().getButtonAction().getAction());
    
    mViewMenu.add(mToolbarMenu);
    mViewMenu.add(mStatusbarMI);
    mViewMenu.add(mTimeBtnsMI);
    mViewMenu.add(mDatelistMI);
    mViewMenu.add(mChannellistMI);
    mViewMenu.add(mPluginOverviewMI);
    mViewMenu.addSeparator();
    mViewMenu.add(mFiltersMenu);
    mViewMenu.addSeparator();
    mViewMenu.add(mFullscreenMI);
  }


  private JMenuItem createDateMenuItem(final Date date) {
    JMenuItem item = new JMenuItem(date.toString());
    item.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mMainFrame.goTo(date);
      }
    });
    return item;
  }

  private JMenuItem createChannelMenuItem(final Channel channel) {
    Icon icon = null;
    if (Settings.propEnableChannelIcons.getBoolean()) {
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
    JMenuItem item = new JMenuItem(h+":"+(min<10?"0":"")+min);
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
      
      JMenu licenseMenu = new JMenu(mLocalizer.msg("menuitem.license","Terms of Use"));
      TvDataServiceProxy services[]= TvDataServiceProxyManager.getInstance().getDataServices();
      for (int i=0;i<services.length;i++) {
        final String license=services[i].getInfo().getLicense();
        if (license!=null) {
          JMenuItem item=new JMenuItem(services[i].getInfo().getName(),new ImageIcon("imgs/tvbrowser16.png"));
          item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
              LicenseBox box=new LicenseBox(mMainFrame, license, false);
              util.ui.UiUtilities.centerAndShow(box);
            }        
          });
          licenseMenu.add(item);
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
    for (int i=0; i<times.length; i++) {
      mGotoTimeMenu.add(createTimeMenuItem(times[i]));
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
     for (int i=0; i<channels.length; i++) {
       mGotoChannelMenu.add(createChannelMenuItem(channels[i]));
     }
   }

  public void updateDateItems() {
    mGotoDateMenu.removeAll();
    Date curDate = new Date();
    for (int i=0; i<21; i++) {
      if (!TvDataBase.getInstance().dataAvailable(curDate)) {
        break;
      }
      mGotoDateMenu.add(createDateMenuItem(curDate));
      curDate = curDate.addDays(1);
    }
  }

   public void updateFiltersMenu() {
       mFiltersMenu.removeAll();
       FilterButtons filterButtons = new FilterButtons(mMainFrame);
       JMenuItem[] filterMenuItems = filterButtons.createFilterMenuItems();
       for (int i=0; i<filterMenuItems.length; i++) {
         if (filterMenuItems[i] != null) {
             mFiltersMenu.add(filterMenuItems[i]);
         } else {
             mFiltersMenu.addSeparator();
         }
       }
       
   }
   
   protected abstract void setPluginMenuItems(JMenuItem[] items);


   private JMenuItem createMenuItem(ActionMenu menu) {
    JMenuItem result;
    if (menu.hasSubItems()) {
      result = new JMenu(menu.getTitle());
      
      if(menu.getAction().getValue(Action.SMALL_ICON) != null)
        result.setIcon((Icon)menu.getAction().getValue(Action.SMALL_ICON));
      
      ActionMenu[] subItems = menu.getSubItems();
      for (int i=0; i<subItems.length; i++) {
        result.add(createMenuItem(subItems[i]));
      }
    }
    else {
      result = new JMenuItem(menu.getAction());
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

   }
   
   private void createMenuItemInfos() {
     new MenuHelpTextAdapter(mSettingsMI, mLocalizer.msg("menuinfo.settings",""), mLabel); 
     new MenuHelpTextAdapter(mQuitMI, mLocalizer.msg("menuinfo.quit",""), mLabel);
     new MenuHelpTextAdapter(mUpdateMI, mLocalizer.msg("menuinfo.update",""), mLabel);
     new MenuHelpTextAdapter(mPluginManagerMI, mLocalizer.msg("menuinfo.findplugins",""), mLabel); 
     new MenuHelpTextAdapter(mAboutMI, mLocalizer.msg("menuinfo.about",""), mLabel);
     new MenuHelpTextAdapter(mDonorMI,mLocalizer.msg("website.donors",""),mLabel); 
     new MenuHelpTextAdapter(mFaqMI,mLocalizer.msg("website.faq",""),mLabel); 
     new MenuHelpTextAdapter(mForumMI,mLocalizer.msg("website.forum",""),mLabel); 
     new MenuHelpTextAdapter(mHandbookMI,mLocalizer.msg("website.handbook",""),mLabel); 
     new MenuHelpTextAdapter(mKeyboardShortcutsMI,mLocalizer.msg("website.keyboardshortcuts",""),mLabel);
     new MenuHelpTextAdapter(mWebsiteMI,mLocalizer.msg("website.tvbrowser",""),mLabel); 
     new MenuHelpTextAdapter(mConfigAssistantMI,mLocalizer.msg("menuinfo.configAssistant",""),mLabel);
   }

public void dateChanged(Date date, ProgressMonitor monitor, Runnable callback) {
    mPreviousDayMI.setEnabled(TvDataBase.getInstance().dataAvailable(date.addDays(-1)));
    mNextDayMI.setEnabled(TvDataBase.getInstance().dataAvailable(date.addDays(1)));
}



}