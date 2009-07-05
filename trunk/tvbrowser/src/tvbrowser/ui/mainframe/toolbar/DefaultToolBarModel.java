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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
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
import tvbrowser.core.DateListener;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.ButtonActionIf;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.extras.common.InternalPluginProxyList;
import tvbrowser.ui.filter.dlgs.SelectFilterPopup;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;
import util.ui.ScrollableMenu;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.ProgressMonitor;

public class DefaultToolBarModel implements ToolBarModel, ActionListener, DateListener {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(DefaultToolBarModel.class);

  private Map<String, Action> mAvailableActions;

  private ArrayList<Action> mVisibleActions, actionOrder;

  private Action mUpdateAction, mSettingsAction, mFilterAction,
      mPluginViewAction, mSeparatorAction, mScrollToNowAction,
      mGoToTodayAction, mGoToPreviousDayAction, mGoToNextDayAction, 
      mGoToPreviousWeekAction, mGoToNextWeekAction, 
      mGoToDateAction, mScrollToChannelAction, mScrollToTimeAction,
      mGlueAction, mSpaceAction, mFontSizeSmallerAction, mFontSizeLargerAction;

  private static DefaultToolBarModel sInstance;

  private DefaultToolBarModel(String[] buttonIds) {
    createAvailableActions();
    mSeparatorAction = getSeparatorAction();
    mGlueAction = getGlueAction();
    mSpaceAction = getSpaceAction();
    
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
          IconLoader.getInstance().getIconFromTheme("status","view-filter-set-list",16));
      mFilterAction.putValue(Plugin.BIG_ICON, 
          IconLoader.getInstance().getIconFromTheme("status","view-filter-set-list",22));
    } else {
      mFilterAction.putValue(Action.SMALL_ICON, 
          IconLoader.getInstance().getIconFromTheme("actions","view-filter-list",16));
      mFilterAction.putValue(Plugin.BIG_ICON,
          IconLoader.getInstance().getIconFromTheme("actions","view-filter-list",22));
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
    mSettingsAction = createAction(Localizer.getLocalization(Localizer.I18N_SETTINGS),
        "#settings", MainFrame.mLocalizer.msg("menuinfo.settings",
        ""), TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.preferences(TVBrowserIcons.SIZE_LARGE), ToolBar.BUTTON_ACTION, this);
    mFilterAction = createAction(TVBrowser.mLocalizer.msg("button.filter",
        "Filter"), "#filter", MainFrame.mLocalizer.msg("menuinfo.filter", ""),
        IconLoader.getInstance().getIconFromTheme("actions","view-filter-list",16),
        IconLoader.getInstance().getIconFromTheme("actions","view-filter-list",22),
        ToolBar.TOOGLE_BUTTON_ACTION, this);
    mPluginViewAction = createAction(TVBrowser.mLocalizer.msg(
        "button.pluginView", "Plugin View"), "#pluginView",
        MainFrame.mLocalizer.msg("menuinfo.pluginView", ""), IconLoader
        .getInstance().getIconFromTheme("actions", "view-tree", 16),
        IconLoader.getInstance().getIconFromTheme("actions",
            "view-tree", 22),
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
            mLocalizer.msg("goToPreviousToolTip", "Previous day"), TVBrowserIcons.left(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.left(TVBrowserIcons.SIZE_LARGE),
            ToolBar.BUTTON_ACTION, this);
    mGoToTodayAction = createAction(Localizer.getLocalization(
        Localizer.I18N_TODAY), "#goToToday", scrollTo
        + Localizer.getLocalization(Localizer.I18N_TODAY), TVBrowserIcons.down(TVBrowserIcons.SIZE_SMALL),
        TVBrowserIcons.down(TVBrowserIcons.SIZE_LARGE), ToolBar.BUTTON_ACTION, this);
    mGoToNextDayAction = createAction(mLocalizer.msg(
        "goToNextDay", "Next day"), "#goToNextDay", 
        mLocalizer.msg("goToNextToolTip", "Next day"), TVBrowserIcons.right(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.right(TVBrowserIcons.SIZE_LARGE),
        ToolBar.BUTTON_ACTION, this);
    mGoToPreviousWeekAction = createAction(mLocalizer.msg(
        "goToPreviousWeek", "Previous week"), "#goToPreviousWeek", 
        mLocalizer.msg("goToPreviousWeekToolTip", "Previous week"), TVBrowserIcons.previousWeek(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.previousWeek(TVBrowserIcons.SIZE_LARGE),
        ToolBar.BUTTON_ACTION, this);
    mGoToNextWeekAction = createAction(mLocalizer.msg(
        "goToNextWeek", "Next week"), "#goToNextWeek", 
        mLocalizer.msg("goToNextWeekToolTip", "Next week"), TVBrowserIcons.nextWeek(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.nextWeek(TVBrowserIcons.SIZE_LARGE),
        ToolBar.BUTTON_ACTION, this);
    mGoToDateAction = createAction(mLocalizer.msg("goToDate", "Go to date"),
            "#goToDate", mLocalizer.msg("goToDateTooltip", "Go to a date"),
            IconLoader.getInstance().getIconFromTheme("actions",
                "go-to-date-list", 16), IconLoader.getInstance()
                .getIconFromTheme("actions", "go-to-date-list", 22),
            ToolBar.TOOGLE_BUTTON_ACTION, this);
    mScrollToChannelAction = createAction(mLocalizer.msg("scrollToChannel",
        "Scroll to channel"), "#scrollToChannel", mLocalizer.msg("scrollToChannelTooltip",
        "Scroll to a channel"), IconLoader.getInstance().getIconFromTheme(
        "actions", "scroll-to-channel-list", 16),IconLoader.getInstance().getIconFromTheme(
        "actions", "scroll-to-channel-list", 22), ToolBar.TOOGLE_BUTTON_ACTION, this);
    mScrollToTimeAction = createAction(mLocalizer.msg("scrollToTime", "Scroll to time"),
        "#scrollToTime", mLocalizer.msg("scrollToTimeTooltip", "Scroll to a time"),
        IconLoader.getInstance().getIconFromTheme("actions", "scroll-to-time-list", 16),
        IconLoader.getInstance().getIconFromTheme("actions", "scroll-to-time-list", 22),
        ToolBar.TOOGLE_BUTTON_ACTION, this);
    mFontSizeSmallerAction = createAction(mLocalizer.msg("fontSizeSmaller", "Smaller fonts"),
        "#fontSizeSmaller", mLocalizer.msg("fontSizeSmallerToolTip", "Smaller font size in program table"),
        TVBrowserIcons.zoomOut(TVBrowserIcons.SIZE_SMALL),
        TVBrowserIcons.zoomOut(TVBrowserIcons.SIZE_LARGE),
        ToolBar.BUTTON_ACTION, this);
    mFontSizeLargerAction = createAction(mLocalizer.msg("fontSizeLarger", "Larger fonts"),
        "#fontSizeLarger", mLocalizer.msg("fontSizeLargerToolTip", "Larger font size in program table"),
        TVBrowserIcons.zoomIn(TVBrowserIcons.SIZE_SMALL),
        TVBrowserIcons.zoomIn(TVBrowserIcons.SIZE_LARGE),
        ToolBar.BUTTON_ACTION, this);

    updateTimeButtons();

    setPluginViewButtonSelected(Settings.propShowPluginView.getBoolean());
    
    InternalPluginProxyIf[] internalPlugins = InternalPluginProxyList.getInstance().getAvailableProxys();
    
    for (InternalPluginProxyIf internalPlugin : internalPlugins) {
      if(internalPlugin instanceof ButtonActionIf) {
        createPluginAction((ButtonActionIf)internalPlugin);
      }
    }
    
    PluginProxy[] pluginProxys = PluginProxyManager.getInstance().getActivatedPlugins();
    
    for (int i = 0; i < pluginProxys.length; i++) {
      createPluginAction(pluginProxys[i]);
    }
    
    TvDataServiceProxy[] dataServiceProxys = TvDataServiceProxyManager.getInstance().getDataServices();
    
    for(TvDataServiceProxy dataServiceProxy : dataServiceProxys) {
      createPluginAction(dataServiceProxy);
    }
  }
  
  private void createPluginAction(ButtonActionIf plugin) {
    ActionMenu actionMenu = plugin.getButtonAction();
    if (actionMenu != null) {
      if (!actionMenu.hasSubItems()) {
        Action action = actionMenu.getAction();
        action.putValue(ToolBar.ACTION_ID_KEY, plugin.getId());
        mAvailableActions.put(plugin.getId(), action);
        String tooltip = (String) action.getValue(Action.SHORT_DESCRIPTION);
        if (tooltip == null) {
          action.putValue(Action.SHORT_DESCRIPTION, plugin.getButtonActionDescription());
        }
      } else {
        createPluginAction(plugin, actionMenu.getSubItems());
      }
    }
  }
  
  private void createPluginAction(ButtonActionIf plugin, ActionMenu[] subMenus) {
    for(ActionMenu menu : subMenus) {
      if(menu.hasSubItems()) {
        createPluginAction(plugin, menu.getSubItems());
      } else {
        Action action = menu.getAction();
        if (!ContextMenuSeparatorAction.getInstance().equals(action)) {
          action.putValue(ToolBar.ACTION_ID_KEY, plugin.getId() + "##"
              + action.getValue(Action.NAME));
          mAvailableActions.put(plugin.getId() + "##"
              + action.getValue(Action.NAME), action);
          String tooltip = (String) action.getValue(Action.SHORT_DESCRIPTION);
          if (tooltip == null) {
            if (subMenus.length == 1) {
              action.putValue(Action.SHORT_DESCRIPTION, plugin
                .getButtonActionDescription());
            }
            else {
              action.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.NAME));
            }
          }
        }
      }
    }
  }
  
  protected void updatePluginButtons() {
    PluginProxy[] activatedPlugins = PluginProxyManager.getInstance().getActivatedPlugins();
    
    for(int i = 0; i < activatedPlugins.length; i++) {
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
    }
    
    String[] deactivatedPlugins = PluginProxyManager.getInstance().getDeactivatedPluginIds();
    
    for(int i = 0; i < deactivatedPlugins.length; i++) {
      if(mAvailableActions.containsKey(deactivatedPlugins[i])) {
        mVisibleActions.remove(mAvailableActions.remove(deactivatedPlugins[i]));
      }
    }
  }
  
  protected void updateTimeButtons() {
    String[] keys = new String[mAvailableActions.keySet().size()];
    mAvailableActions.keySet().toArray(keys);
    ArrayList<String> availableTimeActions = new ArrayList<String>();

    for (String key : keys) {
      Action action = mAvailableActions.get(key);
      String test = action.getValue(Action.NAME).toString();

      if (test.indexOf(':') != -1 && (test.length() == 4 || test.length() == 5)) {
        availableTimeActions.add(key);
      }
    }

    String scrollTo = MainFrame.mLocalizer
        .msg("menuinfo.scrollTo", "Scroll to")
        + ": ";
    for (final int timeMinutes : Settings.propTimeButtons.getIntArray()) {
      int hour = timeMinutes / 60;
      String time = String.valueOf(timeMinutes % 60);

      if (time.length() == 1) {
        time = hour + ":0" + time;
      } else {
        time = hour + ":" + time;
      }

      if (availableTimeActions.contains("#scrollTo" + time)) {
        availableTimeActions.remove("#scrollTo" + time);
        continue;
      }

      createAction(time, "#scrollTo" + time,
          scrollTo + time, IconLoader.getInstance().getIconFromTheme("actions",
              "scroll-to-specific-time", 16), IconLoader.getInstance().getIconFromTheme(
              "actions", "scroll-to-specific-time", 22), ToolBar.BUTTON_ACTION,
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              MainFrame.getInstance().scrollToTime(timeMinutes);
            }
          });
    }

    Iterator<String> it = availableTimeActions.iterator();

    while (it.hasNext()) {
      final String timeActionId = it.next();
      Action action = mAvailableActions.remove(timeActionId);

      if (mVisibleActions.contains(action)) {
        mVisibleActions.remove(action);
      }
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
      } else if ("#glue".equals(buttonNames[i])) {
        mVisibleActions.add(mGlueAction);
      } else if ("#space".equals(buttonNames[i])) {
        mVisibleActions.add(mSpaceAction);
      } else if (buttonNames[i].equals("java.searchplugin.SearchPlugin") || buttonNames[i].equals("#search")) {
        mVisibleActions.add(mAvailableActions.get("searchplugin.SearchPlugin"));
      } else if (buttonNames[i].equals("java.reminderplugin.ReminderPlugin") || buttonNames[i].equals("#reminder")) {
        mVisibleActions.add(mAvailableActions.get("reminderplugin.ReminderPlugin"));
      } else if (buttonNames[i].equals("java.favoritesplugin.FavoritesPlugin") || buttonNames[i].equals("#favorite")) {
        mVisibleActions.add(mAvailableActions.get("favoritesplugin.FavoritesPlugin"));
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
    // often invoked actions
    mVisibleActions.add(mUpdateAction);
    mVisibleActions.add(mPluginViewAction);
    mVisibleActions.add(mSettingsAction);
    
    // internal plugins
    mVisibleActions.add(getSeparatorAction());
    InternalPluginProxyIf[] internalPlugins = InternalPluginProxyList.getInstance().getAvailableProxys();
    
    for(InternalPluginProxyIf internalPlugin : internalPlugins) {
      if(internalPlugin instanceof ButtonActionIf) {
        addButtonActionIfToVisibleActions((ButtonActionIf)internalPlugin);
      }
    }

    // activated default plugins
    mVisibleActions.add(getSeparatorAction());
    PluginProxy[] pluginProxys = PluginProxyManager.getInstance().getActivatedPlugins();
    
    for (int i = 0; i < pluginProxys.length; i++) {
      addButtonActionIfToVisibleActions(pluginProxys[i]);
    }
    
    // remaining actions right aligned
    mVisibleActions.add(getGlueAction());
    
    // filter and view
    mVisibleActions.add(mFilterAction);
    mVisibleActions.add(getSeparatorAction());
    mVisibleActions.add(mFontSizeSmallerAction);
    mVisibleActions.add(mFontSizeLargerAction);
    mVisibleActions.add(getSeparatorAction());
    
    // date navigation
    mVisibleActions.add(mGoToPreviousDayAction);
    mVisibleActions.add(mGoToTodayAction);
    mVisibleActions.add(mGoToNextDayAction);
    mVisibleActions.add(mScrollToNowAction);
    
    // search bar has its own space, so we don't add the space action
  }
  
  private void addButtonActionIfToVisibleActions(ButtonActionIf buttonAction) {
    ActionMenu actionMenu = buttonAction.getButtonAction();
    if (actionMenu != null) {
      if (!actionMenu.hasSubItems()) {
        Action action = mAvailableActions.get(buttonAction.getId());
        if (action != null
            && !ContextMenuSeparatorAction.getInstance().equals(action)) {
          mVisibleActions.add(action);
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
      mSeparatorAction.putValue(ToolBar.ACTION_TYPE_KEY, ToolBar.SEPARATOR);
      mSeparatorAction.putValue(Action.NAME, "Separator");
      mSeparatorAction.putValue(Plugin.BIG_ICON, new Icon() {

        public int getIconHeight() {
          return 22;
        }

        public int getIconWidth() {
          return 22;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
          int width = c.getWidth();
          int height = c.getHeight();
          
          int xStart = width/2 - 1;
          
          g.setColor(c.getBackground().darker().darker());
          g.drawLine(xStart,1,xStart++,height/2);
          g.setColor(c.getBackground().brighter());
          g.drawLine(xStart,1,xStart,height/2);          
        }        
      });
    }
    return mSeparatorAction;
  }
  
  public Action getGlueAction() {
    if(mGlueAction == null) {
      mGlueAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {

        }
      };
      
      mGlueAction.putValue(ToolBar.ACTION_ID_KEY, "#glue");
      mGlueAction.putValue(ToolBar.ACTION_TYPE_KEY, ToolBar.GLUE);
      mGlueAction.putValue(Action.NAME, mLocalizer.msg("flexibleSpace","Flexible Space"));
      mGlueAction.putValue(Plugin.BIG_ICON, new Icon() {

        public int getIconHeight() {
          return 22;
        }

        public int getIconWidth() {
          return 22;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
          int width = c.getWidth();
          int height = c.getHeight();
          
          int yMiddle = height/4 + 2;          
          int xStart = width/2 - 20;
          
          int[] x1Values = {xStart + 4,xStart + 11,xStart + 11};
          int[] x2Values = {xStart + 36,xStart + 29,xStart + 29};          

          int[] yValues = {yMiddle, yMiddle - 7, yMiddle + 7};
          
          g.setColor(c.getBackground().darker().darker());
          g.drawRect(xStart,1,40,height/2+1);
          g.setColor(c.getBackground().brighter());
          g.fillRect(xStart+1,2,38,height/2-1);
          
          g.setColor(Color.gray);
          g.fillPolygon(x1Values,yValues,3);
          g.fillPolygon(x2Values,yValues,3);
          
          for(int i = 0; i < 4; i++) {
            g.drawRect(xStart + 13 + 4*i,yMiddle,1,1);
          }
        }        
      });
    }
    
    return mGlueAction;
  }
  
  public Action getSpaceAction() {
    if(mSpaceAction == null) {
      mSpaceAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {

        }
      };
      
      mSpaceAction.putValue(ToolBar.ACTION_ID_KEY, "#space");
      mSpaceAction.putValue(ToolBar.ACTION_TYPE_KEY, ToolBar.SPACE);
      mSpaceAction.putValue(Action.NAME, mLocalizer.msg("space","Space"));
      mSpaceAction.putValue(Plugin.BIG_ICON, new Icon() {

        public int getIconHeight() {
          return 22;
        }

        public int getIconWidth() {
          return 22;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
          int width = c.getWidth();
          int height = c.getHeight();
          
          int xStart = width/2 - 10;
          
          g.setColor(c.getBackground().darker().darker());
          g.drawRect(xStart,1,20,height/2+1);
          g.setColor(c.getBackground().brighter());
          g.fillRect(xStart+1,2,18,height/2-1);          
        }        
      });

    }
    
    return mSpaceAction;
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
    action.putValue(ToolBar.ACTION_TYPE_KEY, type);
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
      MainFrame.getInstance().goToToday();
    } else if (source == mGoToNextDayAction) {
      MainFrame.getInstance().goToNextDay();
    } else if (source == mGoToPreviousDayAction) {
      MainFrame.getInstance().goToPreviousDay();
    } else if (source == mGoToPreviousWeekAction) {
      MainFrame.getInstance().goToPreviousWeek();
    } else if (source == mGoToNextWeekAction) {
      MainFrame.getInstance().goToNextWeek();
    } else if (source == mFontSizeLargerAction) {
      MainFrame.getInstance().changeFontSize(+1);
    } else if (source == mFontSizeSmallerAction) {
      MainFrame.getInstance().changeFontSize(-1);
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

      if(TvDataBase.getInstance().dataAvailable(curDate)) {
        popup.add(createDateMenuItem(curDate, btn));
      }
      
      curDate = curDate.addDays(1);
      
      for (int i = 0; i < 21; i++) {
        if(!TvDataBase.getInstance().dataAvailable(curDate)) {
          break;
        }
        
        popup.add(createDateMenuItem(curDate, btn));
        curDate = curDate.addDays(1);
        
      }
    } else if (item == mScrollToChannelAction) {
      ScrollableMenu menu = new ScrollableMenu();
      popup = menu.getPopupMenu();

      Channel[] channels = Settings.propSubscribedChannels.getChannelArray();
      for (int i = 0; i < channels.length; i++) {
        menu.add(createChannelMenuItem(channels[i], btn));
      }
    } else if (item == mScrollToTimeAction) {
      popup = new JPopupMenu();

      int[] array = Settings.propTimeButtons.getIntArray();

      for (int i = 0; i < array.length; i++) {
        popup.add(createTimeMenuItem(array[i], btn));
      }

      if (popup.getComponentCount() > 0) {
        popup.addSeparator();
      }

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
            button.setSelected(!FilterManagerImpl.getInstance().getCurrentFilter().equals(FilterManagerImpl.getInstance().getDefaultFilter()));
            setFilterButtonSelected(button.isSelected());
          }
          if (item == mGoToDateAction) {
            button.setSelected(false);
          }

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
    String buttonText;
    
    if(date.equals(Date.getCurrentDate().addDays(-1))) {
      buttonText = Localizer.getLocalization(Localizer.I18N_YESTERDAY);
    }
    else if(date.equals(Date.getCurrentDate())) {
      buttonText = Localizer.getLocalization(Localizer.I18N_TODAY);
    }
    else if(date.equals(Date.getCurrentDate().addDays(1))) {
      buttonText = Localizer.getLocalization(Localizer.I18N_TOMORROW);
    }
    else {
      buttonText = date.toString();
    }
    
    JRadioButtonMenuItem item = new JRadioButtonMenuItem(buttonText);
    
    if(MainFrame.getInstance().getProgramTableModel().getDate().equals(date)) {
      item.setFont(item.getFont().deriveFont(Font.BOLD));
    }
    
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
    JMenuItem item = new JMenuItem();
    
    if (Settings.propShowChannelNamesInChannellist.getBoolean()) {
      item.setText(ch.getName());
    }

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

    if (minute.length() == 1) {
      minute = "0" + minute;
    }
    if (hour.length() == 1) {
      hour = "0" + hour;
    }

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

  public void dateChanged(Date date, ProgressMonitor monitor, Runnable callback) {
    mGoToPreviousDayAction.setEnabled(TvDataBase.getInstance().dataAvailable(date.addDays(-1)));
    mGoToNextDayAction.setEnabled(TvDataBase.getInstance().dataAvailable(date.addDays(1)));
    mGoToPreviousWeekAction.setEnabled(TvDataBase.getInstance().dataAvailable(date.addDays(-7)));
    mGoToNextWeekAction.setEnabled(TvDataBase.getInstance().dataAvailable(date.addDays(7)));
  }
}
