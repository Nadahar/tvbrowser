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
import javax.swing.ImageIcon;
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

  private Map mAvailableActions;

  private ArrayList mVisibleActions;

  private Action mUpdateAction, mSettingsAction, mFilterAction,
      mPluginViewAction, mSeparatorAction, mScrollToNowAction,
      mScrollToTodayAction, mScrollToPreviousDayAction, mScrollToNextDayAction, mFavoriteAction,
      mReminderAction, mGoToDateAction, mGoToChannelAction, mGoToTimeAction;

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
      mFilterAction.putValue(Action.SMALL_ICON, new ImageIcon(
          "imgs/FilterSet16.png"));
      mFilterAction.putValue(Plugin.BIG_ICON, new ImageIcon(
          "imgs/FilterSet22.png"));
    } else {
      mFilterAction.putValue(Action.SMALL_ICON, new ImageIcon(
          "imgs/Filter16.png"));
      mFilterAction.putValue(Plugin.BIG_ICON,
          new ImageIcon("imgs/Filter22.png"));
    }
  }

  private void createAvailableActions() {
    mAvailableActions = new HashMap();
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
        new ImageIcon("imgs/Filter16.png"), new ImageIcon("imgs/Filter22.png"),
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
        .getInstance().getIconFromTheme("actions", "media-playback-start", 16),
        IconLoader.getInstance().getIconFromTheme("actions",
            "media-playback-start", 22), ToolBar.BUTTON_ACTION, this);
    mScrollToTodayAction = createAction(TVBrowser.mLocalizer.msg(
        "button.today", "Today"), "#scrollToToday", scrollTo
        + TVBrowser.mLocalizer.msg("button.today", "Today"), IconLoader
        .getInstance().getIconFromTheme("actions", "document-open", 16),
        IconLoader.getInstance().getIconFromTheme("actions", "document-open",
            22), ToolBar.BUTTON_ACTION, this);
    mScrollToNextDayAction = createAction(mLocalizer.msg(
        "scrollToNextDay", "Next day"), "#scrollToNextDay", 
        mLocalizer.msg("scrollToNextToolTip", "Next day"), IconLoader
        .getInstance().getIconFromTheme("actions", "go-next", 16), IconLoader
        .getInstance().getIconFromTheme("actions", "go-next", 22),
        ToolBar.BUTTON_ACTION, this);
    mScrollToPreviousDayAction = createAction(mLocalizer.msg(
        "scrollToPreviousDay", "Previous day"), "#scrollToPreviousDay", 
        mLocalizer.msg("scrollToPreviousToolTip", "Previous day"), IconLoader
        .getInstance().getIconFromTheme("actions", "go-previous", 16), IconLoader
        .getInstance().getIconFromTheme("actions", "go-previous", 22),
        ToolBar.BUTTON_ACTION, this);
    mReminderAction = createAction(ReminderPlugin.mLocalizer.msg("buttonText",
        "Reminder list"), SettingsItem.REMINDER,
        ReminderPlugin.mLocalizer.msg("description",
            "Eine einfache Implementierung einer Erinnerungsfunktion."),
        IconLoader.getInstance().getIconFromTheme("apps", "appointment", 16),
        IconLoader.getInstance().getIconFromTheme("apps", "appointment", 22),
        ToolBar.BUTTON_ACTION, this);
    mFavoriteAction = createAction(FavoritesPlugin.mLocalizer.msg("buttonText",
        "Manage Favorites"), SettingsItem.FAVORITE,
        FavoritesPlugin.mLocalizer.msg("favoritesManager",
            "Manage favorite programs"), IconLoader.getInstance()
            .getIconFromTheme("apps", "bookmark", 16), IconLoader.getInstance()
            .getIconFromTheme("apps", "bookmark", 22), ToolBar.BUTTON_ACTION,
        this);
    mGoToDateAction = createAction(mLocalizer.msg("goToDate", "Go to date"),
        "#goToDate", mLocalizer.msg("goToDateTooltip", "Go to a date"),
        IconLoader.getInstance().getIconFromTheme("mimetypes",
            "x-office-calendar", 16), IconLoader.getInstance()
            .getIconFromTheme("mimetypes", "x-office-calendar", 22),
        ToolBar.TOOGLE_BUTTON_ACTION, this);
    mGoToChannelAction = createAction(mLocalizer.msg("goToChannel",
        "Go to channel"), "#goToChannel", mLocalizer.msg("goToChannelTooltip",
        "Go to a channel"), IconLoader.getInstance().getIconFromTheme(
        "actions", "go-down", 16), IconLoader.getInstance().getIconFromTheme(
        "actions", "go-down", 22), ToolBar.TOOGLE_BUTTON_ACTION, this);
    mGoToTimeAction = createAction(mLocalizer.msg("goToTime", "Go to time"),
        "#goToTime", mLocalizer.msg("goToTimeTooltip", "Go to a time"),
        IconLoader.getInstance().getIconFromTheme("actions", "go-down", 16),
        IconLoader.getInstance().getIconFromTheme("actions", "go-down", 22),
        ToolBar.TOOGLE_BUTTON_ACTION, this);

    updateTimeButtons();

    setPluginViewButtonSelected(Settings.propShowPluginView.getBoolean());

    mAvailableActions.put("#update", mUpdateAction);
    mAvailableActions.put("#settings", mSettingsAction);
    mAvailableActions.put("#filter", mFilterAction);
    mAvailableActions.put("#pluginView", mPluginViewAction);
    mAvailableActions.put("#scrollToNow", mScrollToNowAction);
    mAvailableActions.put("#scrollToToday", mScrollToTodayAction);
    mAvailableActions.put("#scrollToNextDay", mScrollToNextDayAction);
    mAvailableActions.put("#scrollToPreviousDay", mScrollToPreviousDayAction);
    mAvailableActions.put(SettingsItem.REMINDER, mReminderAction);
    mAvailableActions.put(SettingsItem.FAVORITE, mFavoriteAction);
    mAvailableActions.put("#goToDate", mGoToDateAction);
    mAvailableActions.put("#goToChannel", mGoToChannelAction);
    mAvailableActions.put("#goToTime", mGoToTimeAction);

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
            action.putValue(Action.SHORT_DESCRIPTION, pluginProxys[i].getInfo()
                .getDescription());
          }
        } else {
          // TODO: create drop down list button
        }
      }
    }
  }

  protected void updateTimeButtons() {
    Object[] keys = mAvailableActions.keySet().toArray();
    ArrayList availableTimeActions = new ArrayList();

    for (int i = 0; i < keys.length; i++) {
      Action action = (Action) mAvailableActions.get(keys[i]);
      String test = action.getValue(Action.NAME).toString();

      if (test.indexOf(":") != -1 && (test.length() == 4 || test.length() == 5))
        availableTimeActions.add(keys[i].toString());
    }

    String scrollTo = MainFrame.mLocalizer
        .msg("menuinfo.scrollTo", "Scroll to")
        + ": ";
    // create Time Buttons
    int[] array = Settings.propTimeButtons.getIntArray();
    Action timeButtonAction;

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

      timeButtonAction = createAction(time, "#scrollTo" + time,
          scrollTo + time, IconLoader.getInstance().getIconFromTheme("actions",
              "go-down", 16), IconLoader.getInstance().getIconFromTheme(
              "actions", "go-down", 22), ToolBar.BUTTON_ACTION,
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              MainFrame.getInstance().scrollToTime(scrollTime);
            }
          });

      mAvailableActions.put("#scrollTo" + time, timeButtonAction);
    }

    Iterator it = availableTimeActions.iterator();

    while (it.hasNext()) {
      Action action = (Action) mAvailableActions.remove(it.next());

      if (mVisibleActions.contains(action))
        mVisibleActions.remove(action);
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
      } else if (buttonNames[i].compareTo("java.reminderplugin.ReminderPlugin") == 0) {
        mVisibleActions.add(mReminderAction);
      } else if (buttonNames[i].compareTo("java.favoritesplugin.FavoritesPlugin") == 0) {
        mVisibleActions.add(mFavoriteAction);
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
    mVisibleActions.add(mFavoriteAction);
    mVisibleActions.add(mReminderAction);
    mVisibleActions.add(getSeparatorAction());

    PluginProxyManager pluginMng = PluginProxyManager.getInstance();
    PluginProxy[] pluginProxys = pluginMng.getActivatedPlugins();
    for (int i = 0; i < pluginProxys.length; i++) {
      ActionMenu actionMenu = pluginProxys[i].getButtonAction();
      if (actionMenu != null) {
        if (!actionMenu.hasSubItems()) {
          Action action = (Action) mAvailableActions.get(pluginProxys[i]
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

    return action;
  }

  public void actionPerformed(ActionEvent event) {
    Action source = (Action) event.getSource();
    if (source == mUpdateAction) {
      MainFrame.getInstance().updateTvData();
    } else if (source == mSettingsAction) {
      MainFrame.getInstance().showSettingsDialog();
    } else if (source == mFilterAction || source == mGoToDateAction
        || source == mGoToChannelAction || source == mGoToTimeAction) {
      showPopupMenu(source);
    } else if (source == mPluginViewAction) {
      AbstractButton button = (AbstractButton) source
          .getValue(ToolBar.ACTION_VALUE);
      MainFrame.getInstance().setShowPluginOverview(button.isSelected());
      setPluginViewButtonSelected(button.isSelected());
    } else if (source == mScrollToNowAction) {
      MainFrame.getInstance().scrollToNow();
    } else if (source == mScrollToTodayAction) {
      devplugin.Date d = devplugin.Date.getCurrentDate();
      MainFrame.getInstance().goTo(d);
    } else if (source == mScrollToNextDayAction) {
      MainFrame.getInstance().goToNextDay();
    } else if (source == mScrollToPreviousDayAction) {
      MainFrame.getInstance().goToPreviousDay();
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
    Action[] result = new Action[mAvailableActions.size()];
    mAvailableActions.values().toArray(result);
    return result;
  }

  public void store() {

  }

  public void showStopButton() {
    mUpdateAction.putValue(Action.NAME, TVBrowser.mLocalizer.msg("button.stop",
        "Stop"));
    mUpdateAction.putValue(Action.SMALL_ICON, IconLoader.getInstance()
        .getIconFromTheme("actions", "process-stop", 16));
    mUpdateAction.putValue(Plugin.BIG_ICON, IconLoader.getInstance()
        .getIconFromTheme("actions", "process-stop", 22));
    mUpdateAction.putValue(Action.SHORT_DESCRIPTION, MainFrame.mLocalizer.msg(
        "menuinfo.stop", ""));
  }

  public void showUpdateButton() {
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
    } else if (item == mGoToChannelAction) {
      ScrollableMenu menu = new ScrollableMenu();
      popup = menu.getPopupMenu();

      Channel[] channels = Settings.propSubscribedChannels
          .getChannelArray(false);
      for (int i = 0; i < channels.length; i++)
        menu.add(createChannelMenuItem(channels[i], btn));
    } else if (item == mGoToTimeAction) {
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
