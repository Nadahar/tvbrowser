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

package tvbrowser.ui.mainframe.toolbar;

import tvbrowser.TVBrowser;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.filter.dlgs.SelectFilterPopup;

import javax.swing.*;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import java.util.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

import devplugin.Plugin;
import devplugin.ActionMenu;

public class DefaultToolBarModel implements ToolBarModel, ActionListener {

  private Map mAvailableActions;
  private ArrayList mVisibleActions;
  private Action mUpdateAction, mSettingsAction, mFilterAction, mPluginViewAction,
                 mSeparatorAction;

  private static DefaultToolBarModel sInstance;

  private DefaultToolBarModel(String[] buttonIds) {
    createAvailableActions();
    mSeparatorAction = getSeparatorAction();

    setButtonIds(buttonIds);

  }

  public void setButtonIds(String[] ids) {
    if (ids == null) {
      createDefaultVisibleActions();
    }
    else {
      createVisibleActions(ids);
    }
  }




  public static DefaultToolBarModel getInstance() {
    if (sInstance == null) {
      sInstance = new DefaultToolBarModel(Settings.propToolbarButtons.getStringArray());
    }
    return sInstance;
  }


  public void setPluginViewButtonSelected(boolean arg) {
    mPluginViewAction.putValue(ToolBar.ACTION_IS_SELECTED, new Boolean(arg));
  }

  public void setFilterButtonSelected(boolean arg) {
    mFilterAction.putValue(ToolBar.ACTION_IS_SELECTED, new Boolean(arg));
  }

  private void createAvailableActions() {
    mAvailableActions = new HashMap();
    mUpdateAction = createAction(TVBrowser.mLocalizer.msg("button.update", "Update"),"#update",MainFrame.mLocalizer.msg("menuinfo.update",""),new ImageIcon("imgs/Refresh16.gif"), new ImageIcon("imgs/Refresh24.gif"),ToolBar.BUTTON_ACTION, this);
    mSettingsAction = createAction(TVBrowser.mLocalizer.msg("button.settings", "Settings"),"#settings",MainFrame.mLocalizer.msg("menuinfo.settings",""), new ImageIcon("imgs/Preferences16.gif"), new ImageIcon("imgs/Preferences24.gif"), ToolBar.BUTTON_ACTION, this);
    mFilterAction = createAction(TVBrowser.mLocalizer.msg("button.filter", "Filter"),"#filter",MainFrame.mLocalizer.msg("menuinfo.filter",""), new ImageIcon("imgs/Filter24.png"), new ImageIcon("imgs/Filter24.png"), ToolBar.TOOGLE_BUTTON_ACTION, this);
    mPluginViewAction = createAction(TVBrowser.mLocalizer.msg("button.pluginView","Plugin View"),"#pluginView",MainFrame.mLocalizer.msg("menuinfo.pluginView",""), new ImageIcon("imgs/Bookmarks16.gif"), new ImageIcon("imgs/Bookmarks24.gif"), ToolBar.TOOGLE_BUTTON_ACTION, this);

    setPluginViewButtonSelected(Settings.propShowPluginView.getBoolean());

    mAvailableActions.put("#update",mUpdateAction);
    mAvailableActions.put("#settings", mSettingsAction);
    mAvailableActions.put("#filter", mFilterAction);
    mAvailableActions.put("#pluginView", mPluginViewAction);

    PluginProxyManager pluginMng = PluginProxyManager.getInstance();
    PluginProxy[] pluginProxys = pluginMng.getActivatedPlugins();
    for (int i=0; i<pluginProxys.length; i++) {
      ActionMenu actionMenu = pluginProxys[i].getButtonAction();
      if (actionMenu != null) {
        if (!actionMenu.hasSubItems()) {
          Action action = actionMenu.getAction();
          action.putValue(ToolBar.ACTION_ID_KEY, pluginProxys[i].getId());
          mAvailableActions.put(pluginProxys[i].getId(), action);
        }
        else {
          //TODO: create drop down list button
        }
      }
    }


  }


  private void createVisibleActions(String[] buttonNames) {
    mVisibleActions = new ArrayList();
    for (int i=0; i<buttonNames.length; i++) {
      Action action = (Action)mAvailableActions.get(buttonNames[i]);
      if (action != null) {
        mVisibleActions.add(action);
      }
      else if ("#separator".equals(buttonNames[i])) {
        mVisibleActions.add(mSeparatorAction);
      }
    }
  }

  private void createDefaultVisibleActions() {
    mVisibleActions = new ArrayList();
    mVisibleActions.add(mUpdateAction);

    mVisibleActions.add(getSeparatorAction());

    PluginProxyManager pluginMng = PluginProxyManager.getInstance();
    PluginProxy[] pluginProxys = pluginMng.getActivatedPlugins();
    for (int i=0; i<pluginProxys.length; i++) {
      ActionMenu actionMenu = pluginProxys[i].getButtonAction();
      if (actionMenu != null) {
        if (!actionMenu.hasSubItems()) {
          Action action = (Action)mAvailableActions.get(pluginProxys[i].getId());
          if (action != null) {
            mVisibleActions.add(action);
          }
        }
      }
    }
  }


    public Action getSeparatorAction() {
      if (mSeparatorAction == null) {
        mSeparatorAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {

        }
      };

      mSeparatorAction.putValue(ToolBar.ACTION_ID_KEY,"#separator");
      mSeparatorAction.putValue(ToolBar.ACTION_TYPE_KEY,new Integer(ToolBar.SEPARATOR));
      mSeparatorAction.putValue(Action.NAME, "----SEPARATOR----");
      }
    return mSeparatorAction;
    }

    private Action createAction(String name, String id, String description, Icon smallIcon, Icon bigIcon, int type, final ActionListener listener) {
      Action action = new AbstractAction(){
        public void actionPerformed(ActionEvent e) {
          listener.actionPerformed(new ActionEvent(this, e.getID(), e.getActionCommand()));
        }
      };
      action.putValue(Action.NAME, name);
      action.putValue(Action.SMALL_ICON, smallIcon);
      action.putValue(Plugin.BIG_ICON, bigIcon);
      action.putValue(Action.SHORT_DESCRIPTION, description);
      action.putValue(ToolBar.ACTION_TYPE_KEY, new Integer(type));
      action.putValue(ToolBar.ACTION_ID_KEY, id);
      return action;
    }



   public void actionPerformed(ActionEvent event) {
    Action source = (Action)event.getSource();
    if (source == mUpdateAction) {
      MainFrame.getInstance().updateTvData();
    }
    else if (source == mSettingsAction) {
      MainFrame.getInstance().showSettingsDialog();
    }
    else if (source == mFilterAction) {
      showFilterPopup(source);
    }
    else if (source == mPluginViewAction) {
      AbstractButton button = (AbstractButton)source.getValue(ToolBar.ACTION_VALUE);
      MainFrame.getInstance().setShowPluginOverview(button.isSelected());
    }
    else {

    }
    }

    public Action[] getActions() {
      Action[] result = new Action[mVisibleActions.size()];
      mVisibleActions.toArray(result);
      return result;
    }

    public Action[] getAvailableActions() {
      Action[] result = new Action[mAvailableActions.size()];
      mAvailableActions.values().toArray(result);
      return result;
    }

    public void store() {

    }

    public void showStopButton() {

    }

    public void showUpdateButton() {

    }



  private void showFilterPopup(final Action item) {
   AbstractButton btn = (AbstractButton)item.getValue(ToolBar.ACTION_VALUE);
    SelectFilterPopup popup = new SelectFilterPopup(MainFrame.getInstance());

    popup.addPopupMenuListener(new PopupMenuListener(){
      public void popupMenuCanceled(PopupMenuEvent e) {
        System.out.println("cancel");
        AbstractButton button = (AbstractButton)item.getValue(ToolBar.ACTION_VALUE);
        System.out.println("state: "+button.isSelected());
        button.setSelected(!MainFrame.getInstance().isShowAllFilterActivated());
        MainFrame.getInstance().updateToolbar();
      }

      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
    });

      Point p = new Point(0, 0);


//      TODO: get Location of Toolbar
//
//      String locationStr = Settings.propToolbarLocation.getString();
//      System.out.println(locationStr);
//      if ("east".equals(locationStr)) {
//        p.x = -1 * btn.getWidth();
//      }else if ("south".equals(locationStr)) {
//        p.y = -1 * popup.getHeight();
//      }else if ("west".equals(locationStr)) {
//        p.x = btn.getWidth();
//      }else {
//        p.y = btn.getHeight();
//      }

      p.y = btn.getHeight()+1;

      popup.show(btn, p.x,p.y);
  }

}

        