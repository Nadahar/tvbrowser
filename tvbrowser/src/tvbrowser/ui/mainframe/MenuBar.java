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

import javax.swing.*;

import tvbrowser.core.Settings;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.ChannelList;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.filter.dlgs.FilterButtons;
import tvbrowser.ui.licensebox.LicenseBox;
import tvbrowser.ui.mainframe.toolbar.ContextMenu;
import tvbrowser.ui.settings.SettingsDialog;
import tvbrowser.ui.settings.ToolBarDragAndDropSettings;
import util.ui.ScrollableMenu;
import devplugin.ProgramFilter;
import devplugin.ActionMenu;
import devplugin.Date;
import devplugin.Channel;


public abstract class MenuBar extends JMenuBar implements ActionListener {
  
    /** The localizer for this class. */
    public static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(MainFrame.class);
    
    
  private MainFrame mMainFrame;

  protected JMenuItem mSettingsMI, mQuitMI, mToolbarMI, mStatusbarMI, mTimeBtnsMI, mDatelistMI,
                    mChannellistMI, mPluginOverviewMI, mRestoreMI, mUpdateMI,
                    mPluginManagerMI, mDonorMI, mFaqMI, mForumMI, mWebsiteMI, mHandbookMI,
                    mConfigAssistantMI, mAboutMI,
                    mPreviousDayMI, mNextDayMI, mGotoNowMenuItem, mEditTimeButtonsMenuItem,
                    mToolbarCustomizeMI;
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
    mUpdateMI.setIcon(new ImageIcon("imgs/Refresh16.gif"));
  }
  
  public void showStopMenuItem() {
      mUpdateMI.setText(mLocalizer.msg("menuitem.stopUpdate","Stop"));
      mUpdateMI.setIcon(new ImageIcon("imgs/Stop16.gif"));
  }
  
  private void createMenuItems() {
      
    mSettingsMI = new JMenuItem(mLocalizer.msg("menuitem.settings", "Settings..."), new ImageIcon("imgs/Preferences16.gif"));
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



    mGotoChannelMenu = new ScrollableMenu(mLocalizer.msg("menuitem.channel","channel"));
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


    updateDateItems();
    updateChannelItems();
    updateTimeItems();


    mRestoreMI = new JMenuItem(mLocalizer.msg("menuitem.restore", "Restore"));
    mRestoreMI.addActionListener(this);
    
    mUpdateMI = new JMenuItem(mLocalizer.msg("menuitem.update", "Update..."), new ImageIcon("imgs/Refresh16.gif"));
    mUpdateMI.addActionListener(this);
    
    mLicenseMenu = createLicenseMenuItems();
    
    mPluginManagerMI = new JMenuItem(mLocalizer.msg("menuitem.managePlugins", "Manage Plugins"));
    mPluginManagerMI.addActionListener(this);
    
    Icon urlImg = new ImageIcon("imgs/WebComponent16.gif");
    mDonorMI=new JMenuItem(mLocalizer.msg("menuitem.donors","Donors"), urlImg);
    mDonorMI.addActionListener(this);
    
    mFaqMI=new JMenuItem("FAQ",urlImg);   
    mFaqMI.addActionListener(this);
    
    mForumMI=new JMenuItem("Forum",urlImg); 
    mForumMI.addActionListener(this);
    
    mHandbookMI=new JMenuItem(mLocalizer.msg("menuitem.handbook", "Handbook"),urlImg); 
    mHandbookMI.addActionListener(this);
    
    mWebsiteMI=new JMenuItem("Website",urlImg);
    mWebsiteMI.addActionListener(this);
    
    mConfigAssistantMI=new JMenuItem(mLocalizer.msg("menuitem.configAssistant","setup assistant"),new ImageIcon("imgs/Preferences16.gif"));
    mConfigAssistantMI.addActionListener(this);
    
    mAboutMI = new JMenuItem(mLocalizer.msg("menuitem.about", "About..."), new ImageIcon("imgs/About16.gif"));
    mAboutMI.addActionListener(this);

    mViewMenu.add(mToolbarMenu);
    mViewMenu.add(mStatusbarMI);
    mViewMenu.add(mTimeBtnsMI);
    mViewMenu.add(mDatelistMI);
    mViewMenu.add(mChannellistMI);
    mViewMenu.add(mPluginOverviewMI);
    mViewMenu.addSeparator();
    mViewMenu.add(mFiltersMenu);
    mViewMenu.addSeparator();
    mViewMenu.add(mRestoreMI);
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
    JMenuItem item = new JMenuItem(channel.getName());
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
          JMenuItem item=new JMenuItem(services[i].getInfo().getName(),new ImageIcon("imgs/About16.gif"));
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
     PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();

     Arrays.sort(plugins, new Comparator() {
         public int compare(Object o1, Object o2) {
             return o1.toString().compareTo(o2.toString());
         }
     });
     
     ArrayList list = new ArrayList();
     for (int i = 0; i < plugins.length; i++) {
       ActionMenu actionMenu = plugins[i].getButtonAction();

       if (actionMenu != null) {
         JMenuItem item = createMenuItem(actionMenu);
         list.add(item);
         new MenuHelpTextAdapter(item,plugins[i].getInfo().getDescription(),mLabel);
       }
     }     
     JMenuItem[] result = new JMenuItem[list.size()];
     list.toArray(result);
     
     return result;
   }


  public void setPluginViewItemChecked(boolean selected) {
    mPluginOverviewMI.setSelected(selected);
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
     else if (source == mRestoreMI) {
       mMainFrame.restoreViews();
     }
     
     else if (source == mUpdateMI) {
       mMainFrame.updateTvData();
     }
     else if (source == mPluginManagerMI) {
//       mMainFrame.showUpdatePluginsDlg();
       mMainFrame.showSettingsDialog(SettingsDialog.TAB_ID_PLUGINS);
     }
     else if (source == mDonorMI) {
       util.ui.BrowserLauncher.openURL(mLocalizer.msg("website.donors",""));
     }
     else if (source == mFaqMI) {
       util.ui.BrowserLauncher.openURL(mLocalizer.msg("website.faq",""));
     }
     else if (source == mForumMI) {
       util.ui.BrowserLauncher.openURL(mLocalizer.msg("website.forum",""));
     }
     else if (source == mWebsiteMI) {
       util.ui.BrowserLauncher.openURL(mLocalizer.msg("website.tvbrowser",""));
     } 
     else if (source == mHandbookMI) {
         util.ui.BrowserLauncher.openURL(mLocalizer.msg("website.handbook",""));
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
       mMainFrame.showSettingsDialog(SettingsDialog.TAB_ID_TIMEBUTTONS);
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
     new MenuHelpTextAdapter(mWebsiteMI,mLocalizer.msg("website.tvbrowser",""),mLabel); 
     new MenuHelpTextAdapter(mConfigAssistantMI,mLocalizer.msg("menuinfo.configAssistant",""),mLabel);
   }



}