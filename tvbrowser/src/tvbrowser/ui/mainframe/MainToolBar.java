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


import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;


import devplugin.Plugin;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;

import util.ui.toolbar.*;



public class MainToolBar extends util.ui.toolbar.ToolBar implements ToolBarActionListener {

    
  private ToolBarButton mUpdateBtn, mSettingsBtn;
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
  
  public void showStopButton() {
    System.out.println("showStopButton()");
    mUpdateBtn.setName(TVBrowser.mLocalizer.msg("button.stop", "Stop"));
    mUpdateBtn.setIcon(new ImageIcon("imgs/Stop24.gif"));  
    refresh();
  }
  
  public void showUpdateButton() {
    System.out.println("showUpdateButton()");
    mUpdateBtn.setName(TVBrowser.mLocalizer.msg("button.update", "Update"));
    mUpdateBtn.setIcon(new ImageIcon("imgs/Refresh24.gif"));
    refresh();
  }
  
  private void loadAvailableItems() {
    HashSet buttons = new HashSet();
    
    buttons.add(mUpdateBtn = new ToolBarButton("#update",TVBrowser.mLocalizer.msg("button.update", "Update"),MainFrame.mLocalizer.msg("menuinfo.update",""), new ImageIcon("imgs/Refresh24.gif")));
    buttons.add(mSettingsBtn = new ToolBarButton("#settings",TVBrowser.mLocalizer.msg("button.settings", "Settings"),MainFrame.mLocalizer.msg("menuinfo.settings",""), new ImageIcon("imgs/Preferences24.gif")));    
    mUpdateBtn.setActionListener(this);
    mSettingsBtn.setActionListener(this);
    
    buttons.add(mSeparator = new util.ui.toolbar.Separator());
    PluginProxyManager pluginMng = PluginProxyManager.getInstance();
    PluginProxy[] pluginProxys = pluginMng.getActivatedPlugins();
    for (int i=0; i<pluginProxys.length; i++) {
      Action action = pluginProxys[i].getButtonAction();
      if (action != null) {
        ToolBarButton btn = createPluginButton(pluginProxys[i].getId(),action);
        btn.setActionListener(this);
        buttons.add(btn);
      }
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
      Action action = pluginProxys[i].getButtonAction();
      if (action != null) {
        buttonNames.add(pluginProxys[i].getId());
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
    else {
      String id =item.getId();
      System.out.println("id: "+id);
      PluginProxyManager pluginMng = PluginProxyManager.getInstance();
      PluginProxy p = pluginMng.getPluginForId(id);
      System.out.println("plugin: "+p.getId());
      Action action = p.getButtonAction();
      System.out.println("action: "+action.toString());
      action.actionPerformed(event.getActionEvent());
    }
  }
    
}