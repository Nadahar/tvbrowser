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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.filter.dlgs.SelectFilterPopup;
import tvbrowser.ui.mainframe.MainFrame;
import devplugin.ActionMenu;
import devplugin.Plugin;

public class DefaultToolBarModel implements ToolBarModel, ActionListener {

  private Map mAvailableActions;

  private ArrayList mVisibleActions;

  private Action mUpdateAction, mSettingsAction, mFilterAction, mPluginViewAction, mSeparatorAction,
      mScrollToNowAction, mScrollToTodayAction, mScrollToTomorrowAction;

  private Action[] mTimeButtonActions;

  private static DefaultToolBarModel sInstance;

  private DefaultToolBarModel(String[] buttonIds) {
    createAvailableActions();
    mSeparatorAction = getSeparatorAction();

    setButtonIds(buttonIds);

  }

  public void setButtonIds(String[] ids) {
    if (ids == null) {
      createDefaultVisibleActions();
    } else {
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
    mPluginViewAction.putValue(ToolBar.ACTION_IS_SELECTED, Boolean.valueOf(arg));
  }

  public void setFilterButtonSelected(boolean arg) {
    mFilterAction.putValue(ToolBar.ACTION_IS_SELECTED, Boolean.valueOf(arg));

    if (arg) {
      mFilterAction.putValue(Action.SMALL_ICON, new ImageIcon("imgs/FilterSet16.png"));
      mFilterAction.putValue(Plugin.BIG_ICON, new ImageIcon("imgs/FilterSet22.png"));
    } else {
      mFilterAction.putValue(Action.SMALL_ICON, new ImageIcon("imgs/Filter16.png"));
      mFilterAction.putValue(Plugin.BIG_ICON, new ImageIcon("imgs/Filter22.png"));
    }
  }

  private void createAvailableActions() {
    mAvailableActions = new HashMap();
    mUpdateAction = createAction(TVBrowser.mLocalizer.msg("button.update", "Update"), "#update", MainFrame.mLocalizer
        .msg("menuinfo.update", ""), IconLoader.getInstance().getIconFromTheme("apps", "system-software-update", 16), IconLoader.getInstance().getIconFromTheme("apps", "system-software-update", 22),
        ToolBar.BUTTON_ACTION, this);
    mSettingsAction = createAction(TVBrowser.mLocalizer.msg("button.settings", "Settings"), "#settings",
        MainFrame.mLocalizer.msg("menuinfo.settings", ""), IconLoader.getInstance().getIconFromTheme("category", "preferences-desktop", 16), IconLoader.getInstance().getIconFromTheme("category", "preferences-desktop", 22), 
        ToolBar.BUTTON_ACTION, this);
    mFilterAction = createAction(TVBrowser.mLocalizer.msg("button.filter", "Filter"), "#filter", MainFrame.mLocalizer
        .msg("menuinfo.filter", ""), new ImageIcon("imgs/Filter16.png"), new ImageIcon("imgs/Filter22.png"),
        ToolBar.TOOGLE_BUTTON_ACTION, this);
    mPluginViewAction = createAction(TVBrowser.mLocalizer.msg("button.pluginView", "Plugin View"), "#pluginView",
        MainFrame.mLocalizer.msg("menuinfo.pluginView", ""), new ImageIcon("imgs/Bookmarks16.gif"), new ImageIcon(
            "imgs/Bookmarks22.gif"), ToolBar.TOOGLE_BUTTON_ACTION, this);
    String scrollTo = MainFrame.mLocalizer.msg("menuinfo.scrollTo", "Scroll to") + ": ";
    mScrollToNowAction = createAction(TVBrowser.mLocalizer.msg("button.now", "Now"), "#scrollToNow", scrollTo
        + TVBrowser.mLocalizer.msg("button.now", "Now"),IconLoader.getInstance().getIconFromTheme("actions", "media-playback-start", 16), IconLoader.getInstance().getIconFromTheme("actions", "media-playback-start", 22), ToolBar.BUTTON_ACTION, this);
    mScrollToTodayAction = createAction(TVBrowser.mLocalizer.msg("button.today", "Today"), "#scrollToToday", scrollTo
        + TVBrowser.mLocalizer.msg("button.today", "Today"), IconLoader.getInstance().getIconFromTheme("actions", "document-open", 16), IconLoader.getInstance().getIconFromTheme("actions", "document-open", 22),
          ToolBar.BUTTON_ACTION, this);
    mScrollToTomorrowAction = createAction(TVBrowser.mLocalizer.msg("button.tomorrow", "Tomorrow"),
        "#scrollToTomorrow", scrollTo + TVBrowser.mLocalizer.msg("button.tomorrow", "Tomorrow"), IconLoader.getInstance().getIconFromTheme("actions", "go-next", 16),
        IconLoader.getInstance().getIconFromTheme("actions", "go-next", 22), ToolBar.BUTTON_ACTION, this);

    setPluginViewButtonSelected(Settings.propShowPluginView.getBoolean());

    mAvailableActions.put("#update", mUpdateAction);
    mAvailableActions.put("#settings", mSettingsAction);
    mAvailableActions.put("#filter", mFilterAction);
    mAvailableActions.put("#pluginView", mPluginViewAction);
    mAvailableActions.put("#scrollToNow", mScrollToNowAction);
    mAvailableActions.put("#scrollToToday", mScrollToTodayAction);
    mAvailableActions.put("#scrollToTomorrow", mScrollToTomorrowAction);

    // create Time Buttons
    int[] array = Settings.propTimeButtons.getIntArray();
    mTimeButtonActions = new Action[array.length];

    for (int i = 0; i < array.length; i++) {
      int hour = array[i] / 60;
      final int scrollTime = array[i];
      String time = String.valueOf(array[i] % 60);

      if (time.length() == 1)
        time = hour + ":0" + time;
      else
        time = hour + ":" + time;

      mTimeButtonActions[i] = createAction(time, "#scrollTo" + time, scrollTo + time, IconLoader.getInstance()
          .getIconFromTheme("actions", "go-down", 16), IconLoader.getInstance().getIconFromTheme("actions", "go-down",
          22), ToolBar.BUTTON_ACTION, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          MainFrame.getInstance().scrollToTime(scrollTime);
        }
      });

      mAvailableActions.put("#scrollTo" + time, mTimeButtonActions[i]);
    }

    PluginProxyManager pluginMng = PluginProxyManager.getInstance();
    PluginProxy[] pluginProxys = pluginMng.getActivatedPlugins();
    for (int i = 0; i < pluginProxys.length; i++) {
      ActionMenu actionMenu = pluginProxys[i].getButtonAction();
      if (actionMenu != null) {
        if (!actionMenu.hasSubItems()) {
          Action action = actionMenu.getAction();
          action.putValue(ToolBar.ACTION_ID_KEY, pluginProxys[i].getId());
          mAvailableActions.put(pluginProxys[i].getId(), action);
          String tooltip = (String) action.getValue(Action.SHORT_DESCRIPTION);
          if (tooltip == null) {
            action.putValue(Action.SHORT_DESCRIPTION, pluginProxys[i].getInfo().getDescription());
          }
        } else {
          // TODO: create drop down list button
        }
      }
    }
  }

  private void createVisibleActions(String[] buttonNames) {
    mVisibleActions = new ArrayList();
    for (int i = 0; i < buttonNames.length; i++) {
      Action action = (Action) mAvailableActions.get(buttonNames[i]);
      if (action != null) {
        mVisibleActions.add(action);
      } else if ("#separator".equals(buttonNames[i])) {
        mVisibleActions.add(mSeparatorAction);
      } else { // if the buttonName is not valid, we try to add the
        // prefix '.java' - maybe it's a plugin from
        // TV-Browser 1.0
        action = (Action) mAvailableActions.get("java." + buttonNames[i]);
        if (action != null) {
          mVisibleActions.add(action);
        }
      }
    }
  }

  private void createDefaultVisibleActions() {
    mVisibleActions = new ArrayList();
    mVisibleActions.add(mUpdateAction);
    mVisibleActions.add(mPluginViewAction);
    mVisibleActions.add(mFilterAction);
    mVisibleActions.add(getSeparatorAction());

    PluginProxyManager pluginMng = PluginProxyManager.getInstance();
    PluginProxy[] pluginProxys = pluginMng.getActivatedPlugins();
    for (int i = 0; i < pluginProxys.length; i++) {
      ActionMenu actionMenu = pluginProxys[i].getButtonAction();
      if (actionMenu != null) {
        if (!actionMenu.hasSubItems()) {
          Action action = (Action) mAvailableActions.get(pluginProxys[i].getId());
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

      mSeparatorAction.putValue(ToolBar.ACTION_ID_KEY, "#separator");
      mSeparatorAction.putValue(ToolBar.ACTION_TYPE_KEY, new Integer(ToolBar.SEPARATOR));
      mSeparatorAction.putValue(Action.NAME, "----SEPARATOR----");
    }
    return mSeparatorAction;
  }

  private Action createAction(String name, String id, String description, Icon smallIcon, Icon bigIcon, int type,
      final ActionListener listener) {
    Action action = new AbstractAction() {
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
    Action source = (Action) event.getSource();
    if (source == mUpdateAction) {
      MainFrame.getInstance().updateTvData();
    } else if (source == mSettingsAction) {
      MainFrame.getInstance().showSettingsDialog();
    } else if (source == mFilterAction) {
      showFilterPopup(source);
    } else if (source == mPluginViewAction) {
      AbstractButton button = (AbstractButton) source.getValue(ToolBar.ACTION_VALUE);
      MainFrame.getInstance().setShowPluginOverview(button.isSelected());
    } else if (source == mScrollToNowAction) {
      MainFrame.getInstance().scrollToNow();
    } else if (source == mScrollToTodayAction) {
      devplugin.Date d = devplugin.Date.getCurrentDate();
      MainFrame.getInstance().goTo(d);
    } else if (source == mScrollToTomorrowAction) {
      devplugin.Date d = devplugin.Date.getCurrentDate();
      d = d.addDays(1);
      MainFrame.getInstance().goTo(d);
    } else {

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
    mUpdateAction.putValue(Action.NAME, TVBrowser.mLocalizer.msg("button.stop", "Stop"));
    mUpdateAction.putValue(Action.SMALL_ICON, IconLoader.getInstance().getIconFromTheme("actions", "process-stop", 16));
    mUpdateAction.putValue(Plugin.BIG_ICON, IconLoader.getInstance().getIconFromTheme("actions", "process-stop", 22));
    mUpdateAction.putValue(Action.SHORT_DESCRIPTION, MainFrame.mLocalizer.msg("menuinfo.stop", ""));
  }

  public void showUpdateButton() {
    mUpdateAction.putValue(Action.NAME, TVBrowser.mLocalizer.msg("button.update", "Update"));
    mUpdateAction.putValue(Action.SMALL_ICON,IconLoader.getInstance().getIconFromTheme("apps", "system-software-update", 16));
    mUpdateAction.putValue(Plugin.BIG_ICON, IconLoader.getInstance().getIconFromTheme("apps", "system-software-update", 22));
    mUpdateAction.putValue(Action.SHORT_DESCRIPTION, MainFrame.mLocalizer.msg("menuinfo.update", ""));

  }

  private void showFilterPopup(final Action item) {
    AbstractButton btn = (AbstractButton) item.getValue(ToolBar.ACTION_VALUE);
    SelectFilterPopup popup = new SelectFilterPopup(MainFrame.getInstance());

    popup.addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuCanceled(PopupMenuEvent e) {
        AbstractButton button = (AbstractButton) item.getValue(ToolBar.ACTION_VALUE);
        button.setSelected(!MainFrame.getInstance().isShowAllFilterActivated());
        MainFrame.getInstance().updateToolbar();
      }

      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      }

      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      }
    });

    Point p = new Point(0, 0);

    // TODO: get Location of Toolbar
    //
    // String locationStr = Settings.propToolbarLocation.getString();
    // System.out.println(locationStr);
    // if ("east".equals(locationStr)) {
    // p.x = -1 * btn.getWidth();
    // }else if ("south".equals(locationStr)) {
    // p.y = -1 * popup.getHeight();
    // }else if ("west".equals(locationStr)) {
    // p.x = btn.getWidth();
    // }else {
    // p.y = btn.getHeight();
    // }

    p.y = btn.getHeight() + 1;

    popup.show(btn, p.x, p.y);
  }

}
