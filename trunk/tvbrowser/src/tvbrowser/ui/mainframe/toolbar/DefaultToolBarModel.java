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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.extras.searchplugin.SearchPlugin;
import tvbrowser.ui.filter.dlgs.SelectFilterPopup;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.ScrollableMenu;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.SettingsItem;

public class DefaultToolBarModel implements ToolBarModel, ActionListener {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(DefaultToolBarModel.class);

  private Map<String, Action> mAvailableActions;

  private ArrayList<Action> mVisibleActions, actionOrder;

  private Action mUpdateAction, mSettingsAction, mFilterAction,
      mPluginViewAction, mSeparatorAction, mScrollToNowAction,
      mGoToTodayAction, mGoToPreviousDayAction, mGoToNextDayAction, mFavoriteAction,
      mReminderAction, mGoToDateAction, mScrollToChannelAction, mScrollToTimeAction,
      mSearchAction;

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
      sInstance = new DefaultToolBarModel(Settings.propToolbarButtons
          .getStringArray());
    }
    return sInstance;
  }

  public void setPluginViewButtonSelected(boolean arg) {
    mPluginViewAction
        .putValue(ToolBar.ACTION_IS_SELECTED, Boolean.valueOf(arg));
  }

  public void setFilterButtonSelected(boolean arg) {
    mFilterAction.putValue(ToolBar.ACTION_IS_SELECTED, Boolean.valueOf(arg));

    if (arg) {
      mFilterAction.putValue(Action.SMALL_ICON,
          IconLoader.getInstance().getIconFromTheme("status","view-filter-set",16));
      mFilterAction.putValue(Plugin.BIG_ICON, 
          IconLoader.getInstance().getIconFromTheme("status","view-filter-set",22));
    } else {
      mFilterAction.putValue(Action.SMALL_ICON, 
          IconLoader.getInstance().getIconFromTheme("actions","view-filter",16));
      mFilterAction.putValue(Plugin.BIG_ICON,
          IconLoader.getInstance().getIconFromTheme("actions","view-filter",22));
    }
  }

  private void createAvailableActions() {
    mAvailableActions = new HashMap<String, Action>();
    actionOrder = new ArrayList<Action>();
    mUpdateAction = createAction(TVBrowser.mLocalizer.msg("button.update",
        "Update"), "#update", MainFrame.mLocalizer.msg("menuinfo.update", ""),
        IconLoader.getInstance().getIconFromTheme("apps",
            "system-software-update", 16), IconLoader.getInstance()
            .getIconFromTheme("apps", "system-software-update", 22),
        ToolBar.BUTTON_ACTION, this);
    mSettingsAction = createAction(TVBrowser.mLocalizer.msg("button.settings",
        "Settings"), "#settings", MainFrame.mLocalizer.msg("menuinfo.settings",
        ""), IconLoader.getInstance().getIconFromTheme("category",
        "preferences-desktop", 16), IconLoader.getInstance().getIconFromTheme(
        "category", "preferences-desktop", 22), ToolBar.BUTTON_ACTION, this);
    mFilterAction = createAction(TVBrowser.mLocalizer.msg("button.filter",
        "Filter"), "#filter", MainFrame.mLocalizer.msg("menuinfo.filter", ""),
        IconLoader.getInstance().getIconFromTheme("actions","view-filter",16),
        IconLoader.getInstance().getIconFromTheme("actions","view-filter",22),
        ToolBar.TOOGLE_BUTTON_ACTION, this);
    mPluginViewAction = createAction(TVBrowser.mLocalizer.msg(
        "button.pluginView", "Plugin View"), "#pluginView",
        MainFrame.mLocalizer.msg("menuinfo.pluginView", ""), IconLoader
        .getInstance().getIconFromTheme("actions", "view-plugins", 16),
        IconLoader.getInstance().getIconFromTheme("actions",
            "view-plugins", 22),
        ToolBar.TOOGLE_BUTTON_ACTION, this);
    String scrollTo = MainFrame.mLocalizer
        .msg("menuinfo.scrollTo", "Scroll to")
        + ": ";
    mScrollToNowAction = createAction(TVBrowser.mLocalizer.msg("button.now",
        "Now"), "#scrollToNow", scrollTo
        + TVBrowser.mLocalizer.msg("button.now", "Now"), IconLoader
        .getInstance().getIconFromTheme("actions", "scroll-to-now", 16),
        IconLoader.getInstance().getIconFromTheme("actions",
            "scroll-to-now", 22), ToolBar.BUTTON_ACTION, this);
    mGoToPreviousDayAction = createAction(mLocalizer.msg(
            "goToPreviousDay", "Previous day"), "#goToPreviousDay", 
            mLocalizer.msg("goToPreviousToolTip", "Previous day"), IconLoader
            .getInstance().getIconFromTheme("actions", "go-to-previous-day", 16), IconLoader
            .getInstance().getIconFromTheme("actions", "go-to-previous-day", 22),
            ToolBar.BUTTON_ACTION, this);
    mGoToTodayAction = createAction(TVBrowser.mLocalizer.msg(
        "button.today", "Today"), "#goToToday", scrollTo
        + TVBrowser.mLocalizer.msg("button.today", "Today"), IconLoader
        .getInstance().getIconFromTheme("actions", "go-to-today", 16),
        IconLoader.getInstance().getIconFromTheme("actions", "go-to-today",
            22), ToolBar.BUTTON_ACTION, this);
    mGoToNextDayAction = createAction(mLocalizer.msg(
        "goToNextDay", "Next day"), "#goToNextDay", 
        mLocalizer.msg("goToNextToolTip", "Next day"), IconLoader
        .getInstance().getIconFromTheme("actions", "go-to-next-day", 16), IconLoader
        .getInstance().getIconFromTheme("actions", "go-to-next-day", 22),
        ToolBar.BUTTON_ACTION, this);
    mGoToDateAction = createAction(mLocalizer.msg("goToDate", "Go to date"),
            "#goToDate", mLocalizer.msg("goToDateTooltip", "Go to a date"),
            IconLoader.getInstance().getIconFromTheme("actions",
                "go-to-date", 16), IconLoader.getInstance()
                .getIconFromTheme("actions", "go-to-date", 22),
            ToolBar.TOOGLE_BUTTON_ACTION, this);
    mReminderAction = createAction(ReminderPlugin.mLocalizer.msg("buttonText",
        "Reminder list"), SettingsItem.REMINDER,
        ReminderPlugin.mLocalizer.msg("description",
            "Eine einfache Implementierung einer Erinnerungsfunktion."),
        IconLoader.getInstance().getIconFromTheme("apps", "appointment", 16),
        IconLoader.getInstance().getIconFromTheme("apps", "appointment", 22),
        ToolBar.BUTTON_ACTION, this);
    mSearchAction = createAction(SearchPlugin.mLocalizer.msg("searchPrograms", "Search programs")
        , SettingsItem.SEARCH,
        SearchPlugin.mLocalizer.msg("description", "Allows searching programs containing a certain text."),
        IconLoader.getInstance().getIconFromTheme("actions", "system-search", 16),
        IconLoader.getInstance().getIconFromTheme("actions", "system-search", 22),
    ToolBar.BUTTON_ACTION, this);
    mFavoriteAction = createAction(FavoritesPlugin.mLocalizer.msg("buttonText",
        "Manage Favorites"), SettingsItem.FAVORITE,
        FavoritesPlugin.mLocalizer.msg("favoritesManager",
            "Manage favorite programs"), IconLoader.getInstance()
            .getIconFromTheme("apps", "bookmark", 16), IconLoader.getInstance()
            .getIconFromTheme("apps", "bookmark", 22), ToolBar.BUTTON_ACTION,
        this);
    mScrollToChannelAction = createAction(mLocalizer.msg("scrollToChannel",
        "Scroll to channel"), "#scrollToChannel", mLocalizer.msg("scrollToChannelTooltip",
        "Scroll to a channel"), IconLoader.getInstance().getIconFromTheme(
        "actions", "scroll-to-channel", 16),IconLoader.getInstance().getIconFromTheme(
        "actions", "scroll-to-channel", 22), ToolBar.TOOGLE_BUTTON_ACTION, this);
    mScrollToTimeAction = createAction(mLocalizer.msg("scrollToTime", "Scroll to time"),
        "#scrollToTime", mLocalizer.msg("scrollToTimeTooltip", "Scroll to a time"),
        IconLoader.getInstance().getIconFromTheme("actions", "scroll-to-time", 16),
        IconLoader.getInstance().getIconFromTheme("actions", "scroll-to-time", 22),
        ToolBar.TOOGLE_BUTTON_ACTION, this);

    updateTimeButtons();

    setPluginViewButtonSelected(Settings.propShowPluginView.getBoolean());

    PluginProxyManager pluginMng = PluginProxyManager.getInstance();
    PluginProxy[] pluginProxys = pluginMng.getActivatedPlugins();
    
    for (int i = 0; i < pluginProxys.length; i++)
      createPluginAction(pluginProxys[i]);
  }
  
  private void createPluginAction(PluginProxy plugin) {
    ActionMenu actionMenu = plugin.getButtonAction();
    if (actionMenu != null) {
      if (!actionMenu.hasSubItems()) {
        Action action = actionMenu.getAction();
        action.putValue(ToolBar.ACTION_ID_KEY, plugin.getId());
        mAvailableActions.put(plugin.getId(), action);
        String tooltip = (String) action.getValue(Action.SHORT_DESCRIPTION);
        if (tooltip == null) {
          action.putValue(Action.SHORT_DESCRIPTION, plugin.getInfo()
              .getDescription());
        }
      } else {
        // TODO: create drop down list button
      }
    }
  }
  
  protected void updatePluginButtons() {
    PluginProxy[] activatedPlugins = PluginProxyManager.getInstance().getActivatedPlugins();
    
    for(int i = 0; i < activatedPlugins.length; i++)
      if(!mAvailableActions.containsKey(activatedPlugins[i].getId())) {
        createPluginAction(activatedPlugins[i]);
        
        String[] buttonNames = Settings.propToolbarButtons.getStringArray();
        
        if(buttonNames != null) {
          for (int j = 0; j < buttonNames.length; j++) {
            if(buttonNames[j].compareTo(activatedPlugins[i].getId()) == 0) {
              Action action = mAvailableActions.get(buttonNames[j]);
          
              if(action != null) {
                int index = mVisibleActions.size();
                mVisibleActions.add(j > index ? index : j , action);
              }
            }
          }
        }
      }
    
    String[] deactivatedPlugins = PluginProxyManager.getInstance().getDeactivatedPluginIds();
    
    for(int i = 0; i < deactivatedPlugins.length; i++)
      if(mAvailableActions.containsKey(deactivatedPlugins[i]))
        mVisibleActions.remove(mAvailableActions.remove(deactivatedPlugins[i]));
  }
  
  protected void updateTimeButtons() {
    Object[] keys = mAvailableActions.keySet().toArray();
    ArrayList<String> availableTimeActions = new ArrayList<String>();

    for (int i = 0; i < keys.length; i++) {
      Action action = mAvailableActions.get(keys[i]);
      String test = action.getValue(Action.NAME).toString();

      if (test.indexOf(":") != -1 && (test.length() == 4 || test.length() == 5))
        availableTimeActions.add(keys[i].toString());
    }

    String scrollTo = MainFrame.mLocalizer
        .msg("menuinfo.scrollTo", "Scroll to")
        + ": ";
    // create Time Buttons
    int[] array = Settings.propTimeButtons.getIntArray();

    for (int i = 0; i < array.length; i++) {
      int hour = array[i] / 60;
      final int scrollTime = array[i];
      String time = String.valueOf(array[i] % 60);

      if (time.length() == 1)
        time = hour + ":0" + time;
      else
        time = hour + ":" + time;

      if (availableTimeActions.contains(new String("#scrollTo" + time))) {
        availableTimeActions.remove(new String("#scrollTo" + time));
        continue;
      }

      createAction(time, "#scrollTo" + time,
          scrollTo + time, IconLoader.getInstance().getIconFromTheme("actions",
              "scroll-to-specific-time", 16), IconLoader.getInstance().getIconFromTheme(
              "actions", "scroll-to-specific-time", 22), ToolBar.BUTTON_ACTION,
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              MainFrame.getInstance().scrollToTime(scrollTime);
            }
          });
    }

    Iterator it = availableTimeActions.iterator();

    while (it.hasNext()) {
      Action action = mAvailableActions.remove(it.next());

      if (mVisibleActions.contains(action))
        mVisibleActions.remove(action);
    }
  }

  private void createVisibleActions(String[] buttonNames) {
    mVisibleActions = new ArrayList<Action>();
    for (int i = 0; i < buttonNames.length; i++) {
      Action action = mAvailableActions.get(buttonNames[i]);
      if (action != null) {
        mVisibleActions.add(action);
      } else if ("#separator".equals(buttonNames[i])) {
        mVisibleActions.add(mSeparatorAction);
      } else if (buttonNames[i].compareTo("java.searchplugin.SearchPlugin") == 0) {
        mVisibleActions.add(mSearchAction);
      } else if (buttonNames[i].compareTo("java.reminderplugin.ReminderPlugin") == 0) {
        mVisibleActions.add(mReminderAction);
      } else if (buttonNames[i].compareTo("java.favoritesplugin.FavoritesPlugin") == 0) {
        mVisibleActions.add(mFavoriteAction);
      } else { // if the buttonName is not valid, we try to add the
        // prefix '.java' - maybe it's a plugin from
        // TV-Browser 1.0
        action = mAvailableActions.get("java." + buttonNames[i]);
        if (action != null) {
          mVisibleActions.add(action);
        }
      }
    }
  }

  private void createDefaultVisibleActions() {
    mVisibleActions = new ArrayList<Action>();
    mVisibleActions.add(mUpdateAction);
    mVisibleActions.add(mPluginViewAction);
    mVisibleActions.add(mFilterAction);
    mVisibleActions.add(getSeparatorAction());
    mVisibleActions.add(mFavoriteAction);
    mVisibleActions.add(mReminderAction);
    mVisibleActions.add(mSearchAction);
    mVisibleActions.add(getSeparatorAction());

    PluginProxyManager pluginMng = PluginProxyManager.getInstance();
    PluginProxy[] pluginProxys = pluginMng.getActivatedPlugins();
    for (int i = 0; i < pluginProxys.length; i++) {
      ActionMenu actionMenu = pluginProxys[i].getButtonAction();
      if (actionMenu != null) {
        if (!actionMenu.hasSubItems()) {
          Action action = mAvailableActions.get(pluginProxys[i]
              .getId());
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
      mSeparatorAction.putValue(ToolBar.ACTION_TYPE_KEY, new Integer(
          ToolBar.SEPARATOR));
      mSeparatorAction.putValue(Action.NAME, "----SEPARATOR----");
    }
    return mSeparatorAction;
  }

  private Action createAction(String name, String id, String description,
      Icon smallIcon, Icon bigIcon, int type, final ActionListener listener) {
    Action action = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        listener.actionPerformed(new ActionEvent(this, e.getID(), e
            .getActionCommand()));
      }
    };
    action.putValue(Action.NAME, name);
    action.putValue(Action.SMALL_ICON, smallIcon);
    action.putValue(Plugin.BIG_ICON, bigIcon);
    action.putValue(Action.SHORT_DESCRIPTION, description);
    action.putValue(ToolBar.ACTION_TYPE_KEY, new Integer(type));
    action.putValue(ToolBar.ACTION_ID_KEY, id);
    mAvailableActions.put(id, action);
    actionOrder.add(action);
    return action;
  }

  public void actionPerformed(ActionEvent event) {
    Action source = (Action) event.getSource();
    if (source == mUpdateAction) {
      MainFrame.getInstance().updateTvData();
    } else if (source == mSettingsAction) {
      MainFrame.getInstance().showSettingsDialog();
    } else if (source == mFilterAction || source == mGoToDateAction
        || source == mScrollToChannelAction || source == mScrollToTimeAction) {
      showPopupMenu(source);
    } else if (source == mPluginViewAction) {
      MainFrame.getInstance().setShowPluginOverview(!MainFrame.getInstance().isShowingPluginOverview());
      setPluginViewButtonSelected(MainFrame.getInstance().isShowingPluginOverview());
    } else if (source == mScrollToNowAction) {
      MainFrame.getInstance().scrollToNow();
    } else if (source == mGoToTodayAction) {
      devplugin.Date d = devplugin.Date.getCurrentDate();
      MainFrame.getInstance().goTo(d);
    } else if (source == mGoToNextDayAction) {
      MainFrame.getInstance().goToNextDay();
    } else if (source == mGoToPreviousDayAction) {
      MainFrame.getInstance().goToPreviousDay();
    } else if (source == mSearchAction) {
      SearchPlugin.getInstance().getButtonAction()
          .getAction().actionPerformed(null);
    } else if (source == mReminderAction) {
      ReminderPlugin.getInstance().getButtonAction(MainFrame.getInstance())
          .getAction().actionPerformed(null);
    } else if (source == mFavoriteAction) {
      FavoritesPlugin.getInstance().getButtonAction(MainFrame.getInstance())
          .getAction().actionPerformed(null);
    } else {

    }
  }

  public Action[] getActions() {
    Action[] result = new Action[mVisibleActions.size()];
    mVisibleActions.toArray(result);
    return result;
  }

  public Action[] getAvailableActions() {
    ArrayList<Action> orderedList = new ArrayList<Action>(actionOrder);
    for (Iterator<Action> iter = mAvailableActions.values().iterator(); iter.hasNext();) {
		Action action = iter.next();
		if (! orderedList.contains(action)) {
			orderedList.add(action);
		}
	}
    Action[] result = new Action[mAvailableActions.size()];
    orderedList.toArray(result);
    return result;
  }

  public void store() {

  }

  protected Action getUpdateAction() {
    return mUpdateAction;
  }
  
  protected void showStopButton() {
    mUpdateAction.putValue(Action.NAME, TVBrowser.mLocalizer.msg("button.stop",
        "Stop"));
    mUpdateAction.putValue(Action.SMALL_ICON, IconLoader.getInstance()
        .getIconFromTheme("actions", "process-stop", 16));
    mUpdateAction.putValue(Plugin.BIG_ICON, IconLoader.getInstance()
        .getIconFromTheme("actions", "process-stop", 22));
    mUpdateAction.putValue(Action.SHORT_DESCRIPTION, MainFrame.mLocalizer.msg(
        "menuinfo.stop", ""));
  }

  protected void showUpdateButton() {
    mUpdateAction.putValue(Action.NAME, TVBrowser.mLocalizer.msg(
        "button.update", "Update"));
    mUpdateAction.putValue(Action.SMALL_ICON, IconLoader.getInstance()
        .getIconFromTheme("apps", "system-software-update", 16));
    mUpdateAction.putValue(Plugin.BIG_ICON, IconLoader.getInstance()
        .getIconFromTheme("apps", "system-software-update", 22));
    mUpdateAction.putValue(Action.SHORT_DESCRIPTION, MainFrame.mLocalizer.msg(
        "menuinfo.update", ""));

  }

  private void showPopupMenu(final Action item) {
    final AbstractButton btn = (AbstractButton) item
        .getValue(ToolBar.ACTION_VALUE);

    JPopupMenu popup = null;

    if (item == mFilterAction) {
      popup = new SelectFilterPopup(MainFrame.getInstance());
    } else if (item == mGoToDateAction) {
      popup = new JPopupMenu();
      
      Date curDate = Date.getCurrentDate().addDays(-1);

      if(TvDataBase.getInstance().dataAvailable(curDate))
        popup.add(createDateMenuItem(curDate, btn));
      
      curDate = curDate.addDays(1);
      
      for (int i = 0; i < 21; i++) {
        if(!TvDataBase.getInstance().dataAvailable(curDate))
          break;
        
        popup.add(createDateMenuItem(curDate, btn));
        curDate = curDate.addDays(1);
        
      }
    } else if (item == mScrollToChannelAction) {
      ScrollableMenu menu = new ScrollableMenu();
      popup = menu.getPopupMenu();

      Channel[] channels = Settings.propSubscribedChannels.getChannelArray();
      for (int i = 0; i < channels.length; i++)
        menu.add(createChannelMenuItem(channels[i], btn));
    } else if (item == mScrollToTimeAction) {
      popup = new JPopupMenu();

      int[] array = Settings.propTimeButtons.getIntArray();

      for (int i = 0; i < array.length; i++)
        popup.add(createTimeMenuItem(array[i], btn));

      if (popup.getComponentCount() > 0)
        popup.addSeparator();

      JMenuItem menuItem = new JMenuItem(TVBrowser.mLocalizer.msg("button.now",
          "Now"));
      menuItem.setHorizontalTextPosition(JMenuItem.CENTER);

      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          MainFrame.getInstance().scrollToNow();
          btn.setSelected(false);
          MainFrame.getInstance().updateToolbar();
        }
      });
      popup.add(menuItem);
    }
    
    if (popup != null) {
      popup.addPopupMenuListener(new PopupMenuListener() {
        public void popupMenuCanceled(PopupMenuEvent e) {  }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
          AbstractButton button = (AbstractButton) item
              .getValue(ToolBar.ACTION_VALUE);
          if (item == mFilterAction) {
            button.setSelected(!MainFrame.getInstance()
                .isShowAllFilterActivated());
            setFilterButtonSelected(button.isSelected());
          }
          if (item == mGoToDateAction)
            button.setSelected(false);

          MainFrame.getInstance().updateToolbar();
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
      });

      Point p = new Point(0, 0);
      p.y = btn.getHeight() + 1;

      popup.show(btn, p.x, p.y);
    }
  }

  private JMenuItem createDateMenuItem(final Date date, final AbstractButton btn) {
    JRadioButtonMenuItem item = new JRadioButtonMenuItem(
        date.compareTo(Date.getCurrentDate()) == 0 ? mLocalizer.msg("today",
            "Today") : date.toString(),
            date.compareTo(MainFrame.getInstance().getCurrentSelectedDate()) == 0);
    
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().goTo(date);
        btn.setSelected(false);
        MainFrame.getInstance().updateToolbar();
      }
    });
    return item;
  }

  private JMenuItem createChannelMenuItem(final Channel ch,
      final AbstractButton btn) {
    JMenuItem item = new JMenuItem(ch.getName());

    if (Settings.propShowChannelIconsInChannellist.getBoolean()) {
      item.setIcon(UiUtilities.createChannelIcon(ch.getIcon()));
      item.setPreferredSize(new Dimension(item.getPreferredSize().width, item
          .getIcon().getIconHeight()));
    }

    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showChannel(ch);
        btn.setSelected(false);
        MainFrame.getInstance().updateToolbar();
      }
    });
    return item;
  }

  private JMenuItem createTimeMenuItem(final int time, final AbstractButton btn) {
    String minute = String.valueOf(time % 60);
    String hour = String.valueOf(time / 60);

    if (minute.length() == 1)
      minute = "0" + minute;
    if (hour.length() == 1)
      hour = "0" + hour;

    JMenuItem item = new JMenuItem(hour + ":" + minute);
    item.setHorizontalTextPosition(JMenuItem.CENTER);

    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().scrollToTime(time);
        btn.setSelected(false);
        MainFrame.getInstance().updateToolbar();
      }
    });
    return item;
  }
}
