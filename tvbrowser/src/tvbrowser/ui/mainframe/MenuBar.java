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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import tvbrowser.core.Settings;
import tvbrowser.core.TvDataServiceManager;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.filter.dlgs.FilterButtons;
import tvbrowser.ui.licensebox.LicenseBox;
import tvdataservice.TvDataService;
import devplugin.ProgramFilter;
import devplugin.ActionMenu;


public abstract class MenuBar extends JMenuBar implements ActionListener {
  
    /** The localizer for this class. */
    public static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(MainFrame.class);
    
    
  private MainFrame mMainFrame;

  protected JMenuItem mSettingsMI, mQuitMI, mToolbarMI, mStatusbarMI, mTimeBtnsMI, mDatelistMI,
                    mChannellistMI, mPluginOverviewMI, mRestoreMI, mUpdateMI,
                    mFindPluginsMI, mHelpMI, mDonorMI, mFaqMI, mForumMI, mWebsiteMI, mHandbookMI,
                    mConfigAssistantMI, mAboutMI;
  protected JMenu mFiltersMenu, mPluginsViewMenu, mLicenseMenu;  
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
    mToolbarMI = new JCheckBoxMenuItem(mLocalizer.msg("menuitem.viewToolbar","Toolbar"));
    mToolbarMI.setSelected(Settings.propIsTooolbarVisible.getBoolean());
    mToolbarMI.addActionListener(this);
    
    mStatusbarMI = new JCheckBoxMenuItem(mLocalizer.msg("menuitem.viewStatusbar","Statusbar"));
    mStatusbarMI.setSelected(Settings.propIsStatusbarVisible.getBoolean());
    mStatusbarMI.addActionListener(this);
    
    mTimeBtnsMI = new JCheckBoxMenuItem("Zeitknoepfe");
    mTimeBtnsMI.setSelected(Settings.propShowTimeButtons.getBoolean());
    mTimeBtnsMI.addActionListener(this);
    mDatelistMI = new JCheckBoxMenuItem("Datum");
    mDatelistMI.setSelected(Settings.propShowDatelist.getBoolean());
    mDatelistMI.addActionListener(this);
    mChannellistMI = new JCheckBoxMenuItem("Sender");
    mChannellistMI.setSelected(Settings.propShowChannels.getBoolean());
    mChannellistMI.addActionListener(this);
    mPluginOverviewMI = new JCheckBoxMenuItem("Plugin Uebersicht");
    mPluginOverviewMI.setSelected(Settings.propShowPluginView.getBoolean());
    mPluginOverviewMI.addActionListener(this);
    
    mFiltersMenu = new JMenu(mLocalizer.msg("menuitem.filters","Filter"));
    updateFiltersMenu();
    
    mPluginsViewMenu = new JMenu("TODO: Plugins");    
    mPluginsViewMenu.add(new JMenuItem("Plugin #1"));
    mPluginsViewMenu.add(new JMenuItem("Plugin #2"));
    mPluginsViewMenu.add(new JMenuItem("Plugin #3"));
        
    mRestoreMI = new JMenuItem("Wiederherstellen");
    mRestoreMI.addActionListener(this);
    
    mUpdateMI = new JMenuItem(mLocalizer.msg("menuitem.update", "Update..."), new ImageIcon("imgs/Refresh16.gif"));
    mUpdateMI.addActionListener(this);
    
    mLicenseMenu = createLicenseMenuItems();
    
    mFindPluginsMI = new JMenuItem(mLocalizer.msg("menuitem.findPluginsOnWeb", "Find plugins on the web..."), new ImageIcon("imgs/Search16.gif"));
    mFindPluginsMI.addActionListener(this);
    
    mHelpMI = new JMenuItem(mLocalizer.msg("menuitem.help", "Help..."), new ImageIcon("imgs/Help16.gif"));
    mHelpMI.addActionListener(this);
    
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
      TvDataService services[]=TvDataServiceManager.getInstance().getDataServices();
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
     else if (source == mFindPluginsMI) {
       mMainFrame.showUpdatePluginsDlg();
     }
     else if (source == mHelpMI) {
       mMainFrame.showHelpDialog();
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
   }
   
   private void createMenuItemInfos() {
     new MenuHelpTextAdapter(mSettingsMI, mLocalizer.msg("menuinfo.settings",""), mLabel); 
     new MenuHelpTextAdapter(mQuitMI, mLocalizer.msg("menuinfo.quit",""), mLabel);
     new MenuHelpTextAdapter(mUpdateMI, mLocalizer.msg("menuinfo.update",""), mLabel);
     new MenuHelpTextAdapter(mFindPluginsMI, mLocalizer.msg("menuinfo.findplugins",""), mLabel); 
     new MenuHelpTextAdapter(mHelpMI, mLocalizer.msg("menuinfo.help",""), mLabel); 
     new MenuHelpTextAdapter(mAboutMI, mLocalizer.msg("menuinfo.about",""), mLabel);
     new MenuHelpTextAdapter(mDonorMI,mLocalizer.msg("website.donors",""),mLabel); 
     new MenuHelpTextAdapter(mFaqMI,mLocalizer.msg("website.faq",""),mLabel); 
     new MenuHelpTextAdapter(mForumMI,mLocalizer.msg("website.forum",""),mLabel); 
     new MenuHelpTextAdapter(mHandbookMI,mLocalizer.msg("website.handbook",""),mLabel); 
     new MenuHelpTextAdapter(mWebsiteMI,mLocalizer.msg("website.tvbrowser",""),mLabel); 
     new MenuHelpTextAdapter(mConfigAssistantMI,mLocalizer.msg("menuinfo.configAssistant",""),mLabel);
   }
}