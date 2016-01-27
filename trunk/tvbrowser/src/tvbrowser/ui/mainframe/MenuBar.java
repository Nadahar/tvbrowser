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

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;

import tvbrowser.TVBrowser;
import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.filters.ChannelFilterList;
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
import tvbrowser.ui.mainframe.actions.TVBrowserAction;
import tvbrowser.ui.mainframe.actions.TVBrowserActions;
import tvbrowser.ui.mainframe.toolbar.ContextMenu;
import tvbrowser.ui.settings.ToolBarDragAndDropSettings;
import tvdataservice.MarkedProgramsMap;
import util.browserlauncher.Launch;
import util.exc.TvBrowserException;
import util.misc.OperatingSystem;
import util.ui.FixedSizeIcon;
import util.ui.Localizer;
import util.ui.ScrollableMenu;
import util.ui.TVBrowserIcons;
import util.ui.UIThreadRunner;
import util.ui.UiUtilities;
import util.ui.persona.Persona;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ChannelFilter;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.ProgramFilter;
import devplugin.SettingsItem;

public abstract class MenuBar extends JMenuBar implements ActionListener {

	/** The localizer for this class. */
	public static final util.ui.Localizer mLocalizer = util.ui.Localizer
			.getLocalizerFor(MainFrame.class);

	private MainFrame mMainFrame;

  protected JMenuItem mQuitMI, mRestartMI, mToolbarMI, mSettingsMI, mAboutMI, mDebugMI, mDonateMI; // these are accessed in MacOS menu sub class
  protected JMenu mPluginsMenu, mHelpMenu, mEditMenu; // these are accessed in common menu sub class

	private JMenuItem mStatusbarMI,
			mTimeBtnsMI, mDatelistMI, mChannellistMI, mPluginOverviewMI,
			mViewFilterBarMI, mPluginManagerMI, mInstallPluginsMI,
			mDonorMI, mFaqMI, mBackupMI, mForumMI, mWebsiteMI, mHandbookMI,
			mDownloadMI, mConfigAssistantMI, mKeyboardShortcutsMI,
			mEditTimeButtonsMenuItem, mToolbarCustomizeMI,
			mFullscreenMI,
			mPluginInfoDlgMI,
			mCopySettingsToSystem, mMenubarMI;

	private JMenu mFiltersMenu, mLicenseMenu, mGoMenu, mViewMenu, mToolbarMenu,
			mPluginHelpMenu, mGotoDateMenu, mGotoChannelMenu, mGotoTimeMenu, mFontSizeMenu,
			mColumnWidthMenu, mChannelGroupMenu;

	private boolean mCopySettingsRequested = false;

	/**
	 * status bar label for menu help
	 */
	private JLabel mLabel;
	private Border mDefaultBorder;
	private Thread mInitializeThread;
	
	protected MenuBar(MainFrame mainFrame, JLabel label) {
	  mDefaultBorder = getBorder();
		mMainFrame = mainFrame;
		mLabel = label;
		createNeededMenuItems();
		
		mInitializeThread = new Thread("MENU BAR INITIALIZE THREAD") {
		  public void run() {
		    createMenuItems();
		  };
		};
		mInitializeThread.start();
	}
	
	private void createNeededMenuItems() {
	  mSettingsMI = createMenuItem(TVBrowserActions.settings);
	  
	  mQuitMI = createMenuItem("menuitem.exit", "Exit", TVBrowserIcons
        .quit(TVBrowserIcons.SIZE_SMALL));
    mQuitMI.addActionListener(this);
    
    mRestartMI = createMenuItem("menuitem.restart", "Restart", TVBrowserIcons
        .restart(TVBrowserIcons.SIZE_SMALL));
    mRestartMI.addActionListener(this);
    
    mGoMenu = createMenu("menu.go", "Go", true);
    mViewMenu = createMenu("menu.view", "View", true);
    
    mToolbarMenu = createMenu("menuitem.viewToolbar", "Toolbar");
    mChannelGroupMenu = createMenu("menuitem.channelgroup", "Channel group");
    mGotoDateMenu = createMenu("menuitem.date", "date");
    mGotoTimeMenu = createMenu("menuitem.time", "time");
    mPluginHelpMenu = createMenu("menuitem.pluginHelp", "Plugin help");
    mFontSizeMenu = createMenu("menuitem.fontSize", "Font size");
    mColumnWidthMenu = createMenu("menuitem.columnWidth", "ColumnWidth");
    mFiltersMenu = createScrollableMenu("menuitem.filters", "Filter");
    
    mLicenseMenu = createLicenseMenuItems();
	}

	protected MainFrame getMainFrame() {
		return mMainFrame;
	}

	public JLabel getLabel() {
		return mLabel;
	}

	public void showUpdateMenuItem() {
/*
		setLabelAndAccessKeys("menuitem.update", "Update", mUpdateMI, false);
		mUpdateMI.setIcon(IconLoader.getInstance().getIconFromTheme("apps",
				"system-software-update", 16));
*/
	}

	public void showStopMenuItem() {
/*
		setLabelAndAccessKeys("menuitem.stopUpdate", "Stop", mUpdateMI, false);
		mUpdateMI.setIcon(IconLoader.getInstance().getIconFromTheme("actions",
				"process-stop", 16));
*/
	}

	private void createMenuItems() {
		new MenuHelpTextAdapter(mQuitMI, mLocalizer.msg("menuinfo.quit", ""),
				mLabel);
		new MenuHelpTextAdapter(mRestartMI, mLocalizer.msg("menuinfo.restart", ""),
				mLabel);

		mToolbarMI = new JCheckBoxMenuItem(ToolBarDragAndDropSettings.mLocalizer
				.msg("showToolbar", "Show toolbar"));
		mToolbarMI.setSelected(Settings.propIsToolbarVisible.getBoolean());
		mToolbarMI.addActionListener(this);
		new MenuHelpTextAdapter(mToolbarMI, mLocalizer.msg("menuinfo.toolbar", ""),
				mLabel);

    mMenubarMI = new JCheckBoxMenuItem(ContextMenu.mLocalizer.msg("showMenubar", "Show menubar"));
    mMenubarMI.setSelected(Settings.propIsMenubarVisible.getBoolean());
    mMenubarMI.addActionListener(this);
    new MenuHelpTextAdapter(mMenubarMI, mLocalizer.msg("menuinfo.menuBar", ""),
        mLabel);

		mToolbarCustomizeMI = new JMenuItem(ContextMenu.mLocalizer.ellipsisMsg(
				"configure", "Configure"));
		mToolbarCustomizeMI.addActionListener(this);
		new MenuHelpTextAdapter(mToolbarCustomizeMI, mLocalizer.msg(
				"menuinfo.customizeToolbar", ""), mLabel);

		if(!OperatingSystem.isMacOs() || TVBrowser.isTransportable()) {
		  mToolbarMenu.add(mMenubarMI);
		  mMenubarMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		}
		
		mToolbarMenu.add(mToolbarMI);
		mToolbarMenu.addSeparator();
		mToolbarMenu.add(mToolbarCustomizeMI);

		mStatusbarMI = createCheckBoxItem("menuitem.viewStatusbar", "Statusbar");
		mStatusbarMI.setSelected(Settings.propIsStatusbarVisible.getBoolean());
		mStatusbarMI.addActionListener(this);
		new MenuHelpTextAdapter(mStatusbarMI, mLocalizer.msg("menuinfo.statusbar",
				""), mLabel);

		mTimeBtnsMI = createCheckBoxItem("menuitem.timebuttons", "Time buttons");
		mTimeBtnsMI.setSelected(Settings.propShowTimeButtons.getBoolean());
		mTimeBtnsMI.addActionListener(this);
		new MenuHelpTextAdapter(mTimeBtnsMI, mLocalizer.msg("menuinfo.timebuttons",
				""), mLabel);

		mDatelistMI = createCheckBoxItem("menuitem.datelist", "Date list");
		mDatelistMI.setSelected(Settings.propShowDatelist.getBoolean());
		mDatelistMI.addActionListener(this);
		new MenuHelpTextAdapter(mDatelistMI, mLocalizer
				.msg("menuinfo.datelist", ""), mLabel);

		mChannellistMI = createCheckBoxItem("menuitem.channellist", "channel list");
		mChannellistMI.setSelected(Settings.propShowChannels.getBoolean());
		mChannellistMI.addActionListener(this);
		new MenuHelpTextAdapter(mChannellistMI, mLocalizer.msg(
				"menuinfo.channellist", ""), mLabel);

		mPluginOverviewMI = createCheckBoxItem("menuitem.pluginOverview",
				"Plugin overview");
		mPluginOverviewMI.setSelected(Settings.propShowPluginView.getBoolean());
		mPluginOverviewMI.addActionListener(this);
		mPluginOverviewMI.setIcon(IconLoader.getInstance().getIconFromTheme(
				"actions", "view-tree", 16));
		new MenuHelpTextAdapter(mPluginOverviewMI, mLocalizer.msg(
				"menuinfo.pluginOverview", ""), mLabel);

		mViewFilterBarMI = createCheckBoxItem("menuitem.viewFilterBar",
				"Filter bar");
		mViewFilterBarMI.setSelected(Settings.propShowFilterBar.getBoolean());
		mViewFilterBarMI.addActionListener(this);
		new MenuHelpTextAdapter(mViewFilterBarMI, mLocalizer.msg(
				"menuinfo.filterbar", ""), mLabel);
		
		mFiltersMenu.setIcon(TVBrowserIcons.filter(TVBrowserIcons.SIZE_SMALL));
		updateFiltersMenu();
		
		updateChannelGroupMenu(mChannelGroupMenu);
		
		mGotoDateMenu.setIcon(IconLoader.getInstance().getIconFromTheme("apps",
				"office-calendar", 16));

		mGotoChannelMenu = new ScrollableMenu(Localizer
				.getLocalization(Localizer.I18N_CHANNEL));
		mGotoChannelMenu.setIcon(IconLoader.getInstance().getIconFromTheme(
				"actions", "scroll-to-channel", 16));
		
		mGotoTimeMenu.setIcon(IconLoader.getInstance().getIconFromTheme("actions",
				"scroll-to-time", 16));
		mGoMenu.add(createMenuItem(TVBrowserActions.goToPreviousDay));
		mGoMenu.add(createMenuItem(TVBrowserActions.goToNextDay));
		mGoMenu.add(createMenuItem(TVBrowserActions.goToPreviousWeek));
		mGoMenu.add(createMenuItem(TVBrowserActions.goToNextWeek));
    mGoMenu.add(createMenuItem(TVBrowserActions.goToToday));
		mGoMenu.addSeparator();
		mGoMenu.add(mGotoDateMenu);
		mGoMenu.add(mGotoChannelMenu);
		mGoMenu.add(mGotoTimeMenu);
		mGoMenu.addSeparator();
    mGoMenu.add(createMenuItem(TVBrowserActions.scrollToNow));
    
		mFullscreenMI = createCheckBoxItem("menuitem.fullscreen", "Fullscreen");
		mFullscreenMI.setIcon(TVBrowserIcons.fullScreen(TVBrowserIcons.SIZE_SMALL));
		mFullscreenMI.addActionListener(this);
		new MenuHelpTextAdapter(mFullscreenMI, mLocalizer.msg(
				"menuinfo.fullscreen", ""), mLabel);

		updateDateItems();
		updateChannelItems();
		updateTimeItems();
		
		Icon urlHelpImg = IconLoader.getInstance().getIconFromTheme("apps",
				"help-browser", 16);
		Icon urlBrowserImg = IconLoader.getInstance().getIconFromTheme("apps",
				"internet-web-browser", 16);

		mInstallPluginsMI = createMenuItem("menuitem.installPlugins",
				"Install/Update Plugins", urlBrowserImg, true);
		mInstallPluginsMI.addActionListener(this);
		new MenuHelpTextAdapter(
				mInstallPluginsMI,
				mLocalizer
						.msg(
								"menuinfo.installPlugins",
								"Add additional functions to TV-Browser/search for updates for installed Plugins"),
				mLabel);

		mPluginManagerMI = createMenuItem("menuitem.managePlugins",
				"Manage Plugins", null, true);
		mPluginManagerMI.addActionListener(this);
		mPluginManagerMI.setIcon(TVBrowserIcons.plugin(TVBrowserIcons.SIZE_SMALL));
		new MenuHelpTextAdapter(mPluginManagerMI, mLocalizer.msg(
				"menuinfo.findplugins", ""), mLabel);

		mHandbookMI = createMenuItem("menuitem.handbook", "Handbook", urlHelpImg);
		mHandbookMI.addActionListener(this);
		new MenuHelpTextAdapter(mHandbookMI,
				mLocalizer.msg("website.handbook", ""), mLabel);

		mKeyboardShortcutsMI = createMenuItem("menuitem.keyboardshortcuts",
				"Keyboard shortcuts", urlHelpImg);
		mKeyboardShortcutsMI.addActionListener(this);
		new MenuHelpTextAdapter(mKeyboardShortcutsMI, mLocalizer.msg(
				"website.keyboardshortcuts", ""), mLabel);

		mFaqMI = createMenuItem("menuitem.faq", "FAQ", urlHelpImg);
		mFaqMI.addActionListener(this);
		new MenuHelpTextAdapter(mFaqMI, mLocalizer.msg("website.faq", ""), mLabel);

		mBackupMI = createMenuItem("menuitem.backup", "Backup", urlHelpImg);
		mBackupMI.addActionListener(this);
		new MenuHelpTextAdapter(mBackupMI, mLocalizer.msg("website.backup", ""),
				mLabel);

		mWebsiteMI = createMenuItem("menuitem.website", "Website", urlBrowserImg);
		mWebsiteMI.addActionListener(this);
		new MenuHelpTextAdapter(mWebsiteMI,
				mLocalizer.msg("website.tvbrowser", ""), mLabel);

		mForumMI = createMenuItem("menuitem.forum", "Bulletin board", urlBrowserImg);
		mForumMI.addActionListener(this);
		new MenuHelpTextAdapter(mForumMI, mLocalizer.msg("website.forum", ""),
				mLabel);

		mDownloadMI = createMenuItem("menuitem.download", "Download", urlBrowserImg);
		mDownloadMI.addActionListener(this);
		new MenuHelpTextAdapter(mForumMI, mLocalizer.msg("website.download", "http://tvbrowser.org/download_tvbrowser.php"),
				mLabel);

		mDonorMI = createMenuItem("menuitem.donors", "Donors", urlBrowserImg);
		mDonorMI.addActionListener(this);
		new MenuHelpTextAdapter(mDonorMI, mLocalizer.msg("website.donors", "http://tvbrowser.org/donors.html"),
				mLabel);

		mConfigAssistantMI = createMenuItem("menuitem.configAssistant",
				"Setup assistant", TVBrowserIcons
						.preferences(TVBrowserIcons.SIZE_SMALL));
		mConfigAssistantMI.addActionListener(this);
		new MenuHelpTextAdapter(mConfigAssistantMI, mLocalizer.msg(
				"menuinfo.configAssistant", ""), mLabel);

		if(TVBrowser.isTransportable()) {
  		mCopySettingsToSystem = createMenuItem("menuitem.copySettings","Copy settings to system",
  		    IconLoader.getInstance().getIconFromTheme("actions","edit-copy", 16));
  		mCopySettingsToSystem.addActionListener(this);
      new MenuHelpTextAdapter(mCopySettingsToSystem, mLocalizer.msg(
          "menuinfo.copySettings", "Copy settings of transportable version to the system"), mLabel);
		}

		mPluginInfoDlgMI = createMenuItem("menuitem.pluginInfoDlg",
				"What are Plugins?", urlHelpImg);
		mPluginInfoDlgMI.addActionListener(this);
		new MenuHelpTextAdapter(mPluginInfoDlgMI, mLocalizer.msg(
				"menuinfo.pluginInfoDlg",
				"Describes the Plugin functionality of TV-Browser."), mLabel);

		mDebugMI = createMenuItem("menuitem.debug", "Create debug info", new ImageIcon(
        "imgs/tvbrowser16.png"), false);
		mDebugMI.addActionListener(this);
		new MenuHelpTextAdapter(mDebugMI, mLocalizer.msg("menuinfo.debug", ""),
        mLabel);
		
    mDonateMI = createMenuItem("menuitem.donate", "How to donate?", TVBrowserIcons.webBrowser(TVBrowserIcons.SIZE_SMALL), false);
    mDonateMI.addActionListener(this);
    new MenuHelpTextAdapter(mDonateMI, mLocalizer.msg("menuinfo.donate", ""),
        mLabel);
		
		mAboutMI = createMenuItem("menuitem.about", "About", new ImageIcon(
				"imgs/tvbrowser16.png"), false);
		mAboutMI.addActionListener(this);
		new MenuHelpTextAdapter(mAboutMI, mLocalizer.msg("menuinfo.about", ""),
				mLabel);
		
		mPluginHelpMenu.setIcon(urlHelpImg);
		updatePluginHelpMenuItems();
		
		mFontSizeMenu.add(createMenuItem(TVBrowserActions.fontSizeLarger));
		mFontSizeMenu.add(createMenuItem(TVBrowserActions.fontSizeSmaller));
		mFontSizeMenu.addSeparator();
		mFontSizeMenu.add(createMenuItem(TVBrowserActions.fontSizeDefault));
		mFontSizeMenu.setIcon(TVBrowserIcons.zoomIn(TVBrowserIcons.SIZE_SMALL));
		
		mColumnWidthMenu.add(createMenuItem(TVBrowserActions.columnWidthLarger));
		mColumnWidthMenu.add(createMenuItem(TVBrowserActions.columnWidthSmaller));
		mColumnWidthMenu.addSeparator();
		mColumnWidthMenu.add(createMenuItem(TVBrowserActions.columnWidthDefault));

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

	private JMenuItem createMenuItem(final String localizerKey,
			final String defaultLabel, Icon icon, boolean ellipsis) {
		JMenuItem item = new JMenuItem();
		setLabelAndAccessKeys(localizerKey, defaultLabel, item, ellipsis);
		if (icon != null) {
			item.setIcon(icon);
		}
		return item;
	}

  private JMenuItem createMenuItem(final TVBrowserAction action) {
    JMenuItem item = new JMenuItem(action);
    setLabelAndAccessKeys("", action.getMenuText(), item, action.useEllipsis());
    item.setIcon(action.getIcon());
    new MenuHelpTextAdapter(item, action.getMenuHelpText(), mLabel);
    KeyStroke accelerator = action.getAccelerator();
    if (accelerator != null) {
      item.setAccelerator(accelerator);
    }
    return item;
  }

	private JCheckBoxMenuItem createCheckBoxItem(final String localizerKey,
			final String defaultLabel) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem();
		setLabelAndAccessKeys(localizerKey, defaultLabel, item, false);
		return item;
	}

	private JMenuItem createMenuItem(final String localizerKey,
			final String defaultLabel, final Icon icon) {
		return createMenuItem(localizerKey, defaultLabel, icon, false);
	}

	protected JMenu createScrollableMenu(final String localizerKey,
	     final String defaultLabel) {
	  ScrollableMenu menu = new ScrollableMenu();
	   setLabelAndAccessKeys(localizerKey, defaultLabel, menu, false);
	   return menu;
	}
	
	protected JMenu createMenu(final String localizerKey,
      final String defaultLabel) {
	  return createMenu(localizerKey,defaultLabel,false);
	}

	protected JMenu createMenu(final String localizerKey,
			final String defaultLabel, boolean paintForPersona) {
		JMenu menu = new JMenu();
		
		if(paintForPersona) {
		  menu = Persona.getInstance().createPersonaMenu();
		}

		setLabelAndAccessKeys(localizerKey, defaultLabel, menu, false);
		
		return menu;
	}

	private void setLabelAndAccessKeys(final String localizerKey,
			final String defaultLabel, final JMenuItem item, final boolean ellipsis) {
		// get the pure label or a label with "..."
		String label;
		if (localizerKey != null && !localizerKey.isEmpty()) {
  		if (ellipsis) {
  			label = mLocalizer.ellipsisMsg(localizerKey, defaultLabel);
  		} else {
  			label = mLocalizer.msg(localizerKey, defaultLabel);
  		}
		}
		else {
		  if (ellipsis) {
		    label = mLocalizer.ellipsis(defaultLabel);
		  }
		  else {
		    label = defaultLabel;
		  }
		}

		// find and extract the mnemonic
		int index = label.indexOf('&');
		String mnemonic = "";
		if (index >= 0) {
			mnemonic = label.substring(index+1, index+2);
			label = label.substring(0, index) + label.substring(index + 1);
		} else {
			String mnemonicKey = localizerKey + ".mnemonic";
			if (mLocalizer.hasMessage(mnemonicKey)) {
				mnemonic = mLocalizer.msg(mnemonicKey, "");
			}
		}
		item.setText(label);

		// mnemonics are discouraged on MacOS by Apples user interface guidelines
		if (!OperatingSystem.isMacOs()) {
			if (mnemonic != null && !mnemonic.isEmpty()) {
				item.setMnemonic(mnemonic.charAt(0));
			}
			if (index >= 0) {
				item.setDisplayedMnemonicIndex(index);
			}
		}
	}

  void updateChannelGroupMenu() {
    try {
      UIThreadRunner.invokeAndWait(new Runnable() {

        @Override
        public void run() {
          updateChannelGroupMenu(mChannelGroupMenu);
        }
      });
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

	public void updateChannelGroupMenu(JMenu menu) {
	  menu.removeAll();
		String channelFilterName = Settings.propLastUsedChannelGroup.getString();
		// all channels
		JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(mLocalizer.msg(
				"channelGroupAll", "All channels"));
		menuItem.setSelected(channelFilterName == null);
		menuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				MainFrame.getInstance().setChannelFilter(null);
			}
		});
		// selective groups
		menu.add(menuItem);
		String[] channelFilterNames = FilterComponentList.getInstance().getChannelFilterNames();
		
		for (final String filterName : channelFilterNames) {
			try {
        menuItem = new JRadioButtonMenuItem(ChannelFilter.createChannelFilterForName(filterName).toString());
        
        menuItem.addActionListener(new ActionListener() {

          public void actionPerformed(ActionEvent e) {
            MainFrame.getInstance().setChannelFilter(ChannelFilterList.getInstance().getChannelFilterForName(filterName));
          }
        });
        menu.add(menuItem);
        if (channelFilterName != null && filterName.equals(channelFilterName)) {
          menuItem.setSelected(true);
        }
      } catch (ClassCastException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      } catch (TvBrowserException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
		}
		menu.add(new JSeparator());
		// new channel group
		JMenuItem menuItemAdd = createMenuItem("channelGroupNew",
				"Add channel group", null, true);
		menuItemAdd.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
			  Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
			  
				EditFilterComponentDlg dlg = null;
				
				if(w instanceof JDialog) {
				  dlg = new EditFilterComponentDlg((JDialog)w, null, ChannelFilterComponent.class);
				}
				else {
				  dlg = new EditFilterComponentDlg((JFrame)w, null, ChannelFilterComponent.class);
				}
				
				FilterComponent rule = dlg.getFilterComponent();
				if ((rule != null) && (rule instanceof ChannelFilterComponent)) {
					FilterComponentList.getInstance().add(rule);
					FilterComponentList.getInstance().store();
					MainFrame.getInstance()
							.setChannelFilter(ChannelFilterList.getInstance().getChannelFilterForName(rule.getName()));
				}
			}
		});
		menu.add(menuItemAdd);
		// edit channel group
    JMenuItem menuItemEdit = createMenuItem("channelGroupEdit",
        "Edit current channel group", null, true);
    menuItemEdit.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        ChannelFilter rule = MainFrame.getInstance().getChannelFilter();
        if (rule != null) {
          FilterComponent test = FilterComponentList.getInstance().getFilterComponentByName(rule.getName());
          
          if(test instanceof ChannelFilterComponent) {
            // rule must be removed before editing it, otherwise the dialog doesn't save it
            FilterComponentList.getInstance().remove(test.getName());
            EditFilterComponentDlg dlg = new EditFilterComponentDlg(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), test);
            FilterComponent newRule = dlg.getFilterComponent();
            if (newRule == null) { // restore original rule
              newRule = test;
            }
            FilterComponentList.getInstance().add(newRule);
            FilterComponentList.getInstance().store();
            MainFrame.getInstance().setChannelFilter(ChannelFilterList.getInstance().getChannelFilterForName(newRule.getName()));
          }
        }
      }
    });
    menu.add(menuItemEdit);
    menuItemEdit.setEnabled(!MainFrame.isStarting() && MainFrame.getInstance().getChannelFilter() != null);
	}

	private JMenuItem createDateMenuItem(final Date date) {
		JMenuItem item = new JMenuItem(date.getLongDateString());
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mMainFrame.goTo(date);
			}
		});
		return item;
	}

	private JMenuItem createChannelMenuItem(final Channel channel) {
		Icon icon = null;
		
		if (Settings.propShowChannelIconsInChannellist.getBoolean()) {
			icon = UiUtilities.createChannelIcon(channel.getJointChannel() != null ? channel.getJointChannelIcon() : channel.getIcon());
		}
		
		StringBuilder text = new StringBuilder();
		
		if(Settings.propShowSortNumberInProgramLists.getBoolean() && channel.getSortNumber().trim().length() > 0) {
		  text.append(channel.getSortNumber().trim()).append(". ");
		}
		
		text.append(channel.getJointChannel() != null ? channel.getJointChannelName() : channel.getName());
		
		JMenuItem item = new JMenuItem(text.toString(), icon);
				
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mMainFrame.showChannel(channel);
			}
		});

		return item;
	}

	private JMenuItem createTimeMenuItem(final int time) {
		int h = time / 60;
		int min = time % 60;
		JMenuItem item = new JMenuItem((h < 10 ? "0" : "") + h + ":"
				+ (min < 10 ? "0" : "") + min);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mMainFrame.scrollToTime(time,true);
			}
		});
		return item;
	}

	protected JMenuItem[] createFilterMenuItems() {
		ButtonGroup group = new ButtonGroup();
		FilterList filterList = FilterList.getInstance();
		ProgramFilter[] filterArr = filterList.getFilterArr();
		JRadioButtonMenuItem[] result = new JRadioButtonMenuItem[filterArr.length];
		for (int i = 0; i < filterArr.length; i++) {
			final ProgramFilter filter = filterArr[i];
			result[i] = new JRadioButtonMenuItem(filter.toString());
			final JRadioButtonMenuItem item = result[i];
			group.add(item);
			result[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					mMainFrame.setProgramFilter(filter);
					item.setSelected(true);
				}
			});
		}
		result[0].setSelected(true);
		return result;
	}

	private JMenu createLicenseMenuItems() {
		JMenu licenseMenu = new JMenu();
		TvDataServiceProxy[] services = TvDataServiceProxyManager.getInstance()
				.getDataServices();
		for (TvDataServiceProxy service : services) {
			final String license = service.getInfo().getLicense();
			if (license != null) {
				String name = service.getInfo().getName();
				JMenuItem item = new JMenuItem(name, new ImageIcon(
						"imgs/tvbrowser16.png"));
				setMnemonic(item);
				item.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						LicenseBox box = new LicenseBox(mMainFrame, license, false);
						util.ui.UiUtilities.centerAndShow(box);
					}
				});
				licenseMenu.add(item);
			}
		}
		if (licenseMenu.getItemCount() > 1) {
			setLabelAndAccessKeys("menuitem.licenseMultiple", "Terms of Use",
					licenseMenu, false);
		} else {
			setLabelAndAccessKeys("menuitem.license", "Terms of Use", licenseMenu,
					true);
		}
		return licenseMenu;
	}

	private void setMnemonic(final JMenuItem item) {
		// Apple doesn't want mnemonics (see Apple user interface guidelines)
		if (!OperatingSystem.isMacOs()) {
			item.setMnemonic(item.getText().charAt(0));
		}
	}

	public void updatePluginsMenu() {
		setPluginMenuItems(createPluginMenuItems());
	}

	public void updateTimeItems() {
		mGotoTimeMenu.removeAll();
		int[] times = Settings.propTimeButtons.getIntArray();
		for (int time : times) {
			mGotoTimeMenu.add(createTimeMenuItem(time));
		}
		mGotoTimeMenu.addSeparator();
		mEditTimeButtonsMenuItem = createMenuItem("menuitem.editTimeItems",
				"Edit Items", null, true);
		mEditTimeButtonsMenuItem.addActionListener(this);
		mGotoTimeMenu.add(mEditTimeButtonsMenuItem);
	}

	public void updateViewToolbarItem() {
		mToolbarMI.setSelected(Settings.propIsToolbarVisible.getBoolean());
		mMenubarMI.setSelected(Settings.propIsMenubarVisible.getBoolean());
	}

	public void updateChannelItems() {
		mGotoChannelMenu.removeAll();
		Channel[] channels = ChannelList.getSubscribedChannels();
		String[] separatorArr = Settings.propSubscribedChannelsSeparators.getStringArray();
		
		Channel previousChannel = null;
		int lastSeparatorIndex = 0;
		
    if(channels.length > 0) {
      mGotoChannelMenu.add(createChannelMenuItem(channels[0]));
      previousChannel = channels[0];
    }
    
    for (int i = 1; i < channels.length; i++) {
      for(int j = lastSeparatorIndex; j < separatorArr.length; j++) {
        String separator = separatorArr[j];
        
        if(separator.endsWith(channels[i].getUniqueId()) && 
            previousChannel != null && separator.startsWith(previousChannel.getUniqueId()) ) {
          mGotoChannelMenu.addSeparator();
          lastSeparatorIndex = j+1;
        }
      }
      
      previousChannel = channels[i];
      
      if(channels[i-1].getJointChannel() == null || 
          !channels[i-1].getJointChannel().equals(channels[i])) {
        mGotoChannelMenu.add(createChannelMenuItem(channels[i]));
      }
    }
    
		mGotoChannelMenu.setEnabled(channels.length > 0);
	}

	public void updateDateItems() {
		mGotoDateMenu.removeAll();
		Date curDate = new Date();
    Date maxDate = TvDataBase.getInstance().getMaxSupportedDate();
    while (maxDate.getNumberOfDaysSince(curDate) >= 0) {
			if (!TvDataBase.getInstance().dataAvailable(curDate)) {
				break;
			}
			if (curDate.isFirstDayOfWeek() && mGotoDateMenu.getItemCount() > 0) {
				mGotoDateMenu.addSeparator();
			}
			mGotoDateMenu.add(createDateMenuItem(curDate));
			curDate = curDate.addDays(1);
		}
    mGotoDateMenu.setEnabled(ChannelList.getNumberOfSubscribedChannels() > 0);
	}

	public void updateFiltersMenu() {
	  try {
		mFiltersMenu.removeAll();
		FilterButtons.createFilterButtons(mFiltersMenu,mMainFrame);
	  }catch(Throwable t) {t.printStackTrace();}
		//FilterButtons filterButtons = new FilterButtons(mFiltersMenu);
		/*JMenuItem[] filterMenuItems = filterButtons.createFilterMenuItems();
		for (JMenuItem menuItem : filterMenuItems) {
			if (menuItem != null) {
				mFiltersMenu.add(menuItem);
			} else {
				mFiltersMenu.addSeparator();
			}
		}*/
		mViewFilterBarMI.setEnabled(!mMainFrame.isDefaultFilterActivated());
	}

  protected void setPluginMenuItems(JMenuItem[] items) {
    mPluginsMenu.removeAll();

    JMenuItem[] internalPluginItems = createInternalPluginMenuItems();
    for (JMenuItem menuItem : internalPluginItems) {
      mPluginsMenu.add(menuItem);
    }

    JMenuItem[] pluginItems = createPluginMenuItems();
    if (pluginItems.length > 0) {
      mPluginsMenu.addSeparator();
    }
    for (JMenuItem pluginItem : pluginItems) {
      mPluginsMenu.add(pluginItem);
    }

    mPluginsMenu.addSeparator();
    
    mPluginsMenu.add(mInstallPluginsMI);
    mPluginsMenu.add(mPluginManagerMI);
  }

	private JMenuItem createMenuItem(ActionMenu menu) {
		JMenuItem result;
		Icon icon = (Icon) menu.getAction().getValue(Action.SMALL_ICON);
		if (icon != null) {
		  // resize any icon that is not a channel icon
		  if (icon.getIconWidth() != 42 || icon.getIconHeight() != 22) {
		    icon = new FixedSizeIcon(16, 16, icon);
		  }
		}
    if (menu.hasSubItems()) {
			result = new ScrollableMenu(menu.getTitle());

			if (icon != null) {
				result.setIcon(icon);
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
		} else {
			if (ContextMenuSeparatorAction.getInstance().equals(menu.getAction())) {
				return null;
			}
			result = new JMenuItem(menu.getAction());
			if (icon != null) {
				result.setIcon(icon);
			}
		}

		Object accelerator = menu.getAction().getValue(InternalPluginProxyIf.KEYBOARD_ACCELERATOR);
		if (accelerator != null) {
			if (accelerator instanceof KeyStroke) {
				result.setAccelerator((KeyStroke) accelerator);
			}
		}

		return result;

	}

	protected JMenuItem[] createInternalPluginMenuItems() {
		InternalPluginProxyIf[] internalPlugins = InternalPluginProxyList
				.getInstance().getAvailableProxys();

		ArrayList<JMenuItem> list = new ArrayList<JMenuItem>();
		for (InternalPluginProxyIf internalPlugin : internalPlugins) {
			if (internalPlugin instanceof ButtonActionIf) {
				fillButtonActionList(list, (ButtonActionIf) internalPlugin);
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

		TvDataServiceProxy[] dataServices = TvDataServiceProxyManager.getInstance()
				.getDataServices();

		for (TvDataServiceProxy dataService : dataServices) {
			fillButtonActionList(list, dataService);
		}

		return createSortedArrayFromList(list);
	}

	private void fillButtonActionList(ArrayList<JMenuItem> list,
			ButtonActionIf buttonActionIf) {
		ActionMenu actionMenu = buttonActionIf.getButtonAction();
		if (actionMenu != null) {
			JMenuItem item = createMenuItem(actionMenu);
			setMnemonic(item);
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
			if (helpUrl != null) {
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

		InternalPluginProxyIf[] internalPlugins = InternalPluginProxyList
				.getInstance().getAvailableProxys();

		for (InternalPluginProxyIf internalPlugin : internalPlugins) {
			JMenuItem item = pluginHelpMenuItem(internalPlugin.toString(), PluginInfo
					.getHelpUrl(internalPlugin.getId()));
			item.setIcon(internalPlugin.getIcon());
			mPluginHelpMenu.add(item);
		}

		if (result.length > 0) {
			mPluginHelpMenu.addSeparator();
		}

		for (JMenuItem pluginMenuItem : result) {
			mPluginHelpMenu.add(pluginMenuItem);
		}
	}

	private JMenuItem pluginHelpMenuItem(final String name, final String helpUrl) {
		JMenuItem item = new JMenuItem(name);
		setMnemonic(item);
		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Launch.openURL(helpUrl);
			}
		});
		return item;
	}

	public void setPluginViewItemChecked(boolean selected) {
		mPluginOverviewMI.setSelected(selected);
	}

	public void setFullscreenItemChecked(boolean selected) {
		mFullscreenMI.setSelected(selected);
	}
	
	public void setTimeCooserItemChecked(boolean selected) {
	  mTimeBtnsMI.setSelected(selected);
	}
	
	public void setDateListItemChecked(boolean selected) {
	  mDatelistMI.setSelected(selected);
	}
	
	public void setChannelListItemChecked(boolean selected) {
	  mChannellistMI.setSelected(selected);
	}

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == mTimeBtnsMI) {
			mMainFrame.setShowTimeButtons(mTimeBtnsMI.isSelected());
		} else if (source == mQuitMI) {
			mMainFrame.quit();
		} else if (source == mRestartMI) {
			TVBrowser.addRestart();
			mMainFrame.quit();
		} else if (source == mToolbarMI) {
			mMainFrame.setShowToolbar(mToolbarMI.isSelected());
		} else if (source == mMenubarMI) {
      mMainFrame.setShowMenubar(mMenubarMI.isSelected());
    } else if (source == mStatusbarMI) {
			mMainFrame.setShowStatusbar(mStatusbarMI.isSelected());
		} else if (source == mViewFilterBarMI) {
			mMainFrame.updateFilterPanel();
		} else if (source == mDatelistMI) {
			mMainFrame.setShowDatelist(mDatelistMI.isSelected());
		} else if (source == mChannellistMI) {
			mMainFrame.setShowChannellist(mChannellistMI.isSelected());
		} else if (source == mPluginOverviewMI) {
			boolean selected = mPluginOverviewMI.isSelected();
			mMainFrame.setShowPluginOverview(selected);
			mMainFrame.setPluginViewButton(selected);
		} else if (source == mFullscreenMI) {
			mMainFrame.switchFullscreenMode();
		} else if (source == mPluginManagerMI) {
			mMainFrame.showSettingsDialog(SettingsItem.PLUGINS);
		} else if (source == mInstallPluginsMI) {
			mMainFrame.showUpdatePluginsDlg(true);
		} else if (source == mHandbookMI) {
			Launch.openURL(mLocalizer.msg("website.handbook", ""));
		} else if (source == mKeyboardShortcutsMI) {
			Launch.openURL(mLocalizer.msg("website.keyboardshortcuts", ""));
		} else if (source == mFaqMI) {
			Launch.openURL(mLocalizer.msg("website.faq", ""));
		} else if (source == mBackupMI) {
			Launch.openURL(mLocalizer.msg("website.backup", ""));
		} else if (source == mWebsiteMI) {
			Launch.openURL(mLocalizer.msg("website.tvbrowser", ""));
		} else if (source == mForumMI) {
			Launch.openURL(mLocalizer.msg("website.forum", ""));
		} else if (source == mDownloadMI) {
			Launch.openURL(mLocalizer.msg("website.download", "http://tvbrowser.org/download_tvbrowser.php"));
		} else if (source == mDonorMI) {
			Launch.openURL(mLocalizer.msg("website.donors", "http://tvbrowser.org/donors.html"));
		} else if (source == mConfigAssistantMI) {
			mMainFrame.runSetupAssistant();
		} else if (source == mCopySettingsToSystem) {
		  mCopySettingsRequested = true;
      mMainFrame.copySettingsToSystem();
      mCopySettingsRequested = false;
    } else if (source == mPluginInfoDlgMI) {
			mMainFrame.showPluginInfoDlg();
		} else if (source == mAboutMI) {
			mMainFrame.showAboutBox();
		} else if (source == mEditTimeButtonsMenuItem) {
			mMainFrame.showSettingsDialog(SettingsItem.TIMEBUTTONS);
		} else if (source == mToolbarCustomizeMI) {
			new ToolBarDragAndDropSettings();
		} else if(source == mDebugMI) {
		  StringBuilder infoBuilder = new StringBuilder();
		  		  
		  infoBuilder.append("System: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append(" ").append(System.getProperty("os.arch")).append(" ").append(System.getProperty("file.encoding"));
		  infoBuilder.append("\nJava: ").append(System.getProperty("java.runtime.name")).append(" ").append(System.getProperty("java.version")).append(" ").append(System.getProperty("java.home"));
		  infoBuilder.append("\nTV-Browser: ").append(TVBrowser.VERSION).append(" ").append(new File("").getAbsolutePath());
		  infoBuilder.append("\nUser settings: ").append(System.getProperty("user.dir")).append(" ").append(System.getProperty("user.language")).append(" ").append(System.getProperty("user.country")).append(" ").append(System.getProperty("user.timezone"));
		  infoBuilder.append("\nLookAndFeel: ").append(Settings.propLookAndFeel.getString());
		  infoBuilder.append("\nPlastic-Theme: ").append(Settings.propJGoodiesTheme.getString());
		  infoBuilder.append("\nPersona: ").append(Persona.getInstance().getName());
		  infoBuilder.append("\nIcons: ").append(Settings.propIcontheme.getString());
		  infoBuilder.append("\nInfo-Icons: ").append(Settings.propInfoIconThemeID.getString());
		  infoBuilder.append("\nNumber of markings: ").append(MarkedProgramsMap.getInstance().getMarkedPrograms().length);
		  infoBuilder.append("\n\nInstalled plugins:");
		  
		  PluginProxy[] plugins = PluginProxyManager.getInstance().getAllPlugins();
		  
		  for(PluginProxy plugin : plugins) {
		    infoBuilder.append("\n  ").append(plugin.getId()).append(" ").append(plugin.isActivated());
		  }
		  
		  infoBuilder.append("\n\nSubscribed channels:");
		  
		  Channel[] channels = ChannelList.getSubscribedChannels();
		  
		  for(Channel ch : channels) {
		    infoBuilder.append("\n  ").append(ch.getName()).append(" ").append(ch.getDataServicePackageName());
		  }
		  
		  infoBuilder.append("\n\nData available:");
		  
		  Date test = Date.getCurrentDate().addDays(-1);
		  Date maxDate = TvDataBase.getInstance().getMaxSupportedDate();
		  
		  while(test.compareTo(maxDate) < 0) {
		    infoBuilder.append("\n  ").append(test).append(" ").append(TvDataBase.getInstance().dataAvailable(test));
		    test = test.addDays(1);
		  }
		  
		  StringSelection stringSelection = new StringSelection (infoBuilder.toString());
		  Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
		  clpbrd.setContents (stringSelection, null);
		  
		  JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), mLocalizer.msg("debugCopied", "Debug information copied to clipboard."), Localizer.getLocalization(Localizer.I18N_INFO), JOptionPane.INFORMATION_MESSAGE);
		} else if(source == mDonateMI) {
		  Launch.openURL("http://www.tvbrowser.org/index.php?id=donations");
		}
	}

	public boolean isShowFilterPanelEnabled() {
		return mViewFilterBarMI.isSelected();
	}

	protected void createHelpMenuItems(boolean showAbout) {
	}
	
	protected final void addAdditionalMenus(final Runnable callback) {
	  if(mInitializeThread != null && mInitializeThread.isAlive()) {
	    new Thread("ADD ADITONAL MENUS WAITING THREAD") {
	      public void run() {
	        while(mInitializeThread.isAlive()) {
	          try {
              sleep(100);
            } catch (InterruptedException e) {}
	        }
	        
	        callback.run();
	        updatePersona();
	      };
	    }.start();
	  }
	}

  protected void createCommonMenus(boolean aboutMenu) {
    if(!OperatingSystem.isWindows() && (!OperatingSystem.isMacOs() || aboutMenu)) {
      mEditMenu = createMenu("menu.edit", "&Edit", true);
      
      add(mEditMenu);
    }
    
    add(mViewMenu);

    add(mGoMenu);
    
    JMenu tvListingsMenu = createMenu("menu.tvData", "TV &data", true);
    
    add(tvListingsMenu);
    tvListingsMenu.add(createMenuItem(TVBrowserActions.update));
    tvListingsMenu.add(createMenuItem(TVBrowserActions.configureChannels));
    tvListingsMenu.addSeparator();
    tvListingsMenu.add(mLicenseMenu);

    mPluginsMenu = createMenu("menu.plugins", "&Tools", true);
    
    add(mPluginsMenu);
    updatePluginsMenu();
    
    mHelpMenu = createMenu("menu.help", "&Help", true);
    
    add(mHelpMenu);
    mHelpMenu.add(mConfigAssistantMI);

    if(TVBrowser.isTransportable()) {
      mHelpMenu.add(mCopySettingsToSystem);
    }

    mHelpMenu.addSeparator();
    mHelpMenu.add(mHandbookMI);
    mHelpMenu.add(mKeyboardShortcutsMI);
    mHelpMenu.add(mFaqMI);
    mHelpMenu.add(mBackupMI);
    mHelpMenu.add(mPluginHelpMenu);
    mHelpMenu.add(mPluginInfoDlgMI);
    mHelpMenu.addSeparator();
    mHelpMenu.add(mWebsiteMI);
    mHelpMenu.add(mForumMI);
    mHelpMenu.add(mDownloadMI);
    mHelpMenu.add(mDonorMI);
    mHelpMenu.addSeparator();
    mHelpMenu.add(mDonateMI);
    mHelpMenu.add(mDebugMI);
    
    if(aboutMenu) {
      mHelpMenu.add(mAboutMI);
    }
    
    // the split panes reserve F6 and F8, so our accelerator keys don't work on split panes
    // therefore remove those bindings
    InputMap map = (InputMap) UIManager.get("SplitPane.ancestorInputMap");
    KeyStroke keyStrokeF6 = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
    KeyStroke keyStrokeF8 = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
    map.remove(keyStrokeF6);
    map.remove(keyStrokeF8);

    mPluginOverviewMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
    mTimeBtnsMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
    mDatelistMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
    mChannellistMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));    
    mFullscreenMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
  }

  public boolean getUserRequestedCopyToSystem() {
    return mCopySettingsRequested;
  }
  
  protected void paintComponent(Graphics g) {
    if(Persona.getInstance().getAccentColor() != null) {
      g.setColor(Persona.getInstance().getAccentColor());
      g.fillRect(0,0,getWidth(),getHeight());
    }
    else {
      super.paintComponent(g);
    }
  
    if(Persona.getInstance().getHeaderImage() != null) {
      try {
        g.drawImage(Persona.getInstance().getHeaderImage(),0,0,getWidth(),Persona.getInstance().getHeaderImage().getHeight(),Persona.getInstance().getHeaderImage().getWidth()-getWidth(),0,Persona.getInstance().getHeaderImage().getWidth(),Persona.getInstance().getHeaderImage().getHeight(),null);
      }catch(Exception e) {}
    }
  }
  
  /**
   * Updates the search field on Persona change.
   */
  public void updatePersona() {
    if(Persona.getInstance().getHeaderImage() != null) {
      setOpaque(false);
      setBorder(BorderFactory.createEmptyBorder());
    }
    else {
      setOpaque(true);
      setBorder(mDefaultBorder);
    }
    repaint();
  }
}