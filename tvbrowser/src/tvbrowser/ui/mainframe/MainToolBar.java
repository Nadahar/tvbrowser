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


import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.filter.dlgs.SelectFilterPopup;
import util.ui.toolbar.DefaultToolBarModel;
import util.ui.toolbar.ToolBarActionListener;
import util.ui.toolbar.ToolBarButton;
import util.ui.toolbar.ToolBarEvent;
import util.ui.toolbar.ToolBarItem;
import devplugin.Plugin;
import devplugin.ActionMenu;



public class MainToolBar extends util.ui.toolbar.ToolBar implements ToolBarActionListener {

    
  private ToolBarButton mUpdateBtn, mSettingsBtn, mFilterBtn;
  private ToolBarItem mSeparator;
  private MainFrame mMainFrame;
  private DefaultToolBarModel mModel;
    
  public MainToolBar(MainFrame mainFrame, DefaultToolBarModel model) {
    super(model);
    mStyle = getStyle(Settings.propToolbarButtonStyle.getString());
    mModel = model;    
    mMainFrame = mainFrame;
    loadAvailableItems();
    loadVisibleItems();
    setFloatable(Settings.propToolbarIsFloatable.getBoolean());
    
  }
  
  public void storeSettings() {
    ToolBarItem[] items = mModel.getVisibleItems();
    String[] names = new String[items.length];
    for (int i=0; i<items.length; i++) {
      names[i] = items[i].getId();
      if (names[i].startsWith("#separator")) {
        names[i]="#separator";
      }
    }
    Settings.propToolbarButtons.setStringArray(names);
    Settings.propToolbarButtonStyle.setString(getStyleString(getStyle()));
    Settings.propToolbarIsFloatable.setBoolean(isFloatable());
  }
  
  private String getStyleString(int style) {
    if (style == TEXT) {
      return "text";
    } else if (style == ICON) {
      return "icon";
    } else {
      return "text&icon";
    }
  }
  
  private int getStyle(String s) {
    if ("text".equals(s)) {
      return TEXT;
    }
    else if ("icon".equals(s)) {
      return ICON;
    }
    return TEXT|ICON;
  }
  
  public void refresh() {
    loadAvailableItems();
    loadVisibleItems();
    super.refresh();
  }
  
  public void showStopButton() {
    mUpdateBtn.setName(TVBrowser.mLocalizer.msg("button.stop", "Stop"));
    mUpdateBtn.setIcon(new ImageIcon("imgs/Stop24.gif"));  
    super.refresh();
  }
  
  public void showUpdateButton() {
    mUpdateBtn.setName(TVBrowser.mLocalizer.msg("button.update", "Update"));
    mUpdateBtn.setIcon(new ImageIcon("imgs/Refresh24.gif"));
    super.refresh();
  }
  
  private void loadAvailableItems() {
    HashSet buttons = new HashSet();
    
    buttons.add(mUpdateBtn = new ToolBarButton("#update",TVBrowser.mLocalizer.msg("button.update", "Update"),MainFrame.mLocalizer.msg("menuinfo.update",""), new ImageIcon("imgs/Refresh24.gif")));
    buttons.add(mSettingsBtn = new ToolBarButton("#settings",TVBrowser.mLocalizer.msg("button.settings", "Settings"),MainFrame.mLocalizer.msg("menuinfo.settings",""), new ImageIcon("imgs/Preferences24.gif")));    
    mUpdateBtn.setActionListener(this);
    mSettingsBtn.setActionListener(this);

    buttons.add(mFilterBtn = new ToolBarButton("#filter",TVBrowser.mLocalizer.msg("button.filter", "Filter"),MainFrame.mLocalizer.msg("menuinfo.filter",""), new ImageIcon("imgs/Filter24.png")));    
    mFilterBtn.setActionListener(this);

    
    buttons.add(mSeparator = new util.ui.toolbar.Separator());
    PluginProxyManager pluginMng = PluginProxyManager.getInstance();
    PluginProxy[] pluginProxys = pluginMng.getActivatedPlugins();
    for (int i=0; i<pluginProxys.length; i++) {
      ActionMenu actionMenu = pluginProxys[i].getButtonAction();
      if (actionMenu != null) {
        if (!actionMenu.hasSubItems()) {
          Action action = actionMenu.getAction();
          ToolBarButton btn = createPluginButton(pluginProxys[i].getId(),action);
          btn.setActionListener(this);
          buttons.add(btn);
        }
        else {
          //TODO: create drop down list button
        }
      }
      /*
      Action action = pluginProxys[i].getButtonAction();
      if (action != null) {
        ToolBarButton btn = createPluginButton(pluginProxys[i].getId(),action);
        btn.setActionListener(this);
        buttons.add(btn);
      }  */
    }
    ToolBarItem[] items = new ToolBarItem[buttons.size()];
    buttons.toArray(items);
      
    mModel.setAvailableItems(items);
  }
  
  private ToolBarButton createPluginButton(String id, Action action) {
    String text = (String) action.getValue(Action.NAME);
    String desc = (String) action.getValue(Action.SHORT_DESCRIPTION);
    Icon icon = (Icon) action.getValue(Plugin.BIG_ICON);
            
    return new ToolBarButton(id, text, desc, icon);
    
  }
  
  private void loadVisibleItems() {
    String[] buttonNames = Settings.propToolbarButtons.getStringArray();
    if (buttonNames == null) {
      buttonNames = createDefaultButtonNames();
    }
    mModel.setVisibleItemsById(buttonNames);
  }
  
  private String[] createDefaultButtonNames() {
    ArrayList buttonNames = new ArrayList();
    buttonNames.add("#update");    
    buttonNames.add(mSeparator.getId());
    PluginProxyManager pluginMng = PluginProxyManager.getInstance();
    PluginProxy[] pluginProxys = pluginMng.getActivatedPlugins();
    for (int i=0; i<pluginProxys.length; i++) {
      ActionMenu actionMenu = pluginProxys[i].getButtonAction();
      if (actionMenu != null) {
        if (!actionMenu.hasSubItems()) {
          buttonNames.add(pluginProxys[i].getId());
        }
      }
    }
    String[] result = new String[buttonNames.size()];
    buttonNames.toArray(result);
    return result;
  }

  public void actionPerformed(ToolBarEvent event) {
    ToolBarItem item = event.getItem();
    if (item == mUpdateBtn) {
      mMainFrame.updateTvData();
    }
    else if (item == mSettingsBtn) {
      mMainFrame.showSettingsDialog();
    } 
    else if (item == mFilterBtn) {
        showFilterPopup(item);
    }
    else {
      String id =item.getId();
      PluginProxyManager pluginMng = PluginProxyManager.getInstance();
      PluginProxy p = pluginMng.getPluginForId(id);
      ActionMenu actionMenu = p.getButtonAction();
      if (actionMenu != null) {
        if (!actionMenu.hasSubItems()) {
          Action action = actionMenu.getAction();
          action.actionPerformed(event.getActionEvent());
        }
      }
    }
  }
  
  private void showFilterPopup(ToolBarItem item) {
      JButton btn = getButton((ToolBarButton)item);
      SelectFilterPopup popup = new SelectFilterPopup(mMainFrame);
      
      Point p = new Point(0, 0);
      
   /*  
      TODO: get Location of Toolbar
      
      String locationStr = Settings.propToolbarLocation.getString();
      System.out.println(locationStr);
      if ("east".equals(locationStr)) {
        p.x = -1 * btn.getWidth();
      }else if ("south".equals(locationStr)) {
        p.y = -1 * popup.getHeight(); 
      }else if ("west".equals(locationStr)) {
        p.x = btn.getWidth();
      }else {
        p.y = btn.getHeight();
      }      
     */ 
      p.y = btn.getHeight()+1;

      popup.show(btn, p.x,p.y);
  }
}