/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package tvbrowser.ui.mainframe.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.toolbar.ToolBar;
import util.misc.OperatingSystem;
import util.ui.TVBrowserIcons;

/**
 * common TV-Browser actions
 * 
 * @author bananeweizen
 * 
 */
public final class TVBrowserActions {

  /**
   * increase program table font size
   */
  public final static TVBrowserAction fontSizeLarger = new TVBrowserAction("fontSizeLarger", TVBrowserIcons
      .zoomIn(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.zoomIn(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_PLUS, Toolkit
      .getDefaultToolkit().getMenuShortcutKeyMask(), ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().changeFontSize(+1);
    }
  };

  /**
   * decrease program table font size
   */
  public final static TVBrowserAction fontSizeSmaller = new TVBrowserAction("fontSizeSmaller", TVBrowserIcons
      .zoomOut(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.zoomOut(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_MINUS,
      Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().changeFontSize(-1);
    }
  };

  /**
   * set program table font size to default
   */
  public final static TVBrowserAction fontSizeDefault = new TVBrowserAction("fontSizeDefault", KeyEvent.VK_0, Toolkit
      .getDefaultToolkit().getMenuShortcutKeyMask()) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().changeFontSize(0);
    }
  };

  /**
   * increase program table column width
   */
  public final static TVBrowserAction columnWidthLarger = new TVBrowserAction("columnWidthLarger", TVBrowserIcons
      .zoomIn(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.zoomIn(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_PLUS,
      InputEvent.ALT_MASK, ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().changeColumnWidth(+1);
    }
  };

  /**
   * decrease program table column width
   */
  public final static TVBrowserAction columnWidthSmaller = new TVBrowserAction("columnWidthSmaller", TVBrowserIcons
      .zoomOut(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.zoomOut(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_MINUS,
      InputEvent.ALT_MASK, ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().changeColumnWidth(-1);
    }
  };

  /**
   * set column width to default size
   */
  public final static TVBrowserAction columnWidthDefault = new TVBrowserAction("columnWidthDefault", KeyEvent.VK_0,
      InputEvent.ALT_MASK) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().changeColumnWidth(0);
    }
  };

  /**
   * start/stop data update
   */
  public final static TVBrowserUpdateAction update = new TVBrowserUpdateAction("update", TVBrowserIcons
      .update(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.update(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_F5, 0,
      ToolBar.BUTTON_ACTION) {
    
    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().updateTvData();
    }
    
  };

  /**
   * open settings dialog
   */
  public final static TVBrowserAction settings = new TVBrowserAction("settings", TVBrowserIcons
      .preferences(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.preferences(TVBrowserIcons.SIZE_LARGE), 0, 0,
      ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().showSettingsDialog();
    }
    
    public String getMenuText() {
      // Windows guidelines explicitly require "Options"
      if (OperatingSystem.isWindows()) {
        return mLocalizer.msg(getKey() + ".menu.win", "&Options");
      }
      else {
        return mLocalizer.msg(getKey() + ".menu", "&Settings");
      }
    };
  };

  /**
   * show filter popup menu
   */
  public final static TVBrowserAction filter = new TVBrowserAction("filter", TVBrowserIcons
      .filter(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.filter(TVBrowserIcons.SIZE_LARGE), 0, 0,
      ToolBar.TOOGLE_BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      showPopupMenu();
    }

  };

  /**
   * show/hide plugin view
   */
  public final static TVBrowserAction pluginView = new TVBrowserAction("pluginView", TVBrowserIcons
      .viewTree(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.viewTree(TVBrowserIcons.SIZE_LARGE), 0, 0,
      ToolBar.TOOGLE_BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().setShowPluginOverview(!MainFrame.getInstance().isShowingPluginOverview());
    }
  };

  public final static TVBrowserAction scrollToNow = new TVBrowserAction("scrollToNow", TVBrowserIcons
      .scrollToNow(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.scrollToNow(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_F9,
      0, ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().scrollToNow();
    }
  };

  public final static TVBrowserAction goToPreviousDay = new TVBrowserAction("goToPreviousDay", TVBrowserIcons
      .left(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.left(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_LEFT,
      InputEvent.ALT_MASK, ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().goToPreviousDay();
    }
  };

  public final static TVBrowserAction goToToday = new TVBrowserAction("goToToday", TVBrowserIcons
      .down(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.down(TVBrowserIcons.SIZE_LARGE), 0, 0, ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().goToToday();
    }
  };

  public final static TVBrowserAction goToNextDay = new TVBrowserAction("goToNextDay", TVBrowserIcons
      .right(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.right(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_RIGHT,
      InputEvent.ALT_MASK, ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().goToNextDay();
    }
  };

  public final static TVBrowserAction goToPreviousWeek = new TVBrowserAction("goToPreviousWeek", TVBrowserIcons
      .previousWeek(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.previousWeek(TVBrowserIcons.SIZE_LARGE), 0, 0,
      ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().goToPreviousWeek();
    }
  };

  public final static TVBrowserAction goToNextWeek = new TVBrowserAction("goToNextWeek", TVBrowserIcons
      .nextWeek(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.nextWeek(TVBrowserIcons.SIZE_LARGE), 0, 0,
      ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().goToNextWeek();
    }
  };

  public final static TVBrowserAction goToDate = new TVBrowserAction("goToDate", TVBrowserIcons
      .goToDate(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.goToDate(TVBrowserIcons.SIZE_LARGE), 0, 0,
      ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      showPopupMenu();
    }

  };

  public final static TVBrowserAction scrollToChannel = new TVBrowserAction("scrollToChannel", TVBrowserIcons
      .scrollToChannel(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.scrollToChannel(TVBrowserIcons.SIZE_LARGE), 0, 0,
      ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      showPopupMenu();
    }

  };

  public final static TVBrowserAction scrollToTime = new TVBrowserAction("scrollToTime", TVBrowserIcons
      .scrollToTime(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.scrollToTime(TVBrowserIcons.SIZE_LARGE), 0, 0,
      ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      showPopupMenu();
    }

  };

  /*
   * 
   * mGoToDateAction = createAction(mLocalizer.msg("goToDate", "Go to date"),
   * "#goToDate", mLocalizer.msg("goToDateTooltip", "Go to a date"),
   * IconLoader.getInstance().getIconFromTheme("actions", "go-to-date-list",
   * 16), IconLoader.getInstance() .getIconFromTheme("actions",
   * "go-to-date-list", 22), ToolBar.TOOGLE_BUTTON_ACTION, this);
   * mScrollToChannelAction = createAction(mLocalizer.msg("scrollToChannel",
   * "Scroll to channel"), "#scrollToChannel",
   * mLocalizer.msg("scrollToChannelTooltip", "Scroll to a channel"),
   * IconLoader.getInstance().getIconFromTheme( "actions",
   * "scroll-to-channel-list", 16),IconLoader.getInstance().getIconFromTheme(
   * "actions", "scroll-to-channel-list", 22), ToolBar.TOOGLE_BUTTON_ACTION,
   * this); mScrollToTimeAction = createAction(mLocalizer.msg("scrollToTime",
   * "Scroll to time"), "#scrollToTime", mLocalizer.msg("scrollToTimeTooltip",
   * "Scroll to a time"), IconLoader.getInstance().getIconFromTheme("actions",
   * "scroll-to-time-list", 16),
   * IconLoader.getInstance().getIconFromTheme("actions", "scroll-to-time-list",
   * 22), ToolBar.TOOGLE_BUTTON_ACTION, this);
   * 
   * mGoToPreviousWeekAction = createAction(mLocalizer.msg( "goToPreviousWeek",
   * "Previous week"), "#goToPreviousWeek",
   * mLocalizer.msg("goToPreviousWeekToolTip", "Previous week"),
   * TVBrowserIcons.previousWeek(TVBrowserIcons.SIZE_SMALL),
   * TVBrowserIcons.previousWeek(TVBrowserIcons.SIZE_LARGE),
   * ToolBar.BUTTON_ACTION, this); mGoToNextWeekAction =
   * createAction(mLocalizer.msg( "goToNextWeek", "Next week"), "#goToNextWeek",
   * mLocalizer.msg("goToNextWeekToolTip", "Next week"),
   * TVBrowserIcons.nextWeek(TVBrowserIcons.SIZE_SMALL),
   * TVBrowserIcons.nextWeek(TVBrowserIcons.SIZE_LARGE), ToolBar.BUTTON_ACTION,
   * this);
   * 
   * 
   * mGoToPreviousDayAction = createAction(mLocalizer.msg( "goToPreviousDay",
   * "Previous day"), "#goToPreviousDay", mLocalizer.msg("goToPreviousToolTip",
   * "Previous day"), TVBrowserIcons.left(TVBrowserIcons.SIZE_SMALL),
   * TVBrowserIcons.left(TVBrowserIcons.SIZE_LARGE), ToolBar.BUTTON_ACTION,
   * this); mGoToTodayAction = createAction(Localizer.getLocalization(
   * Localizer.I18N_TODAY), "#goToToday", scrollTo +
   * Localizer.getLocalization(Localizer.I18N_TODAY),
   * TVBrowserIcons.down(TVBrowserIcons.SIZE_SMALL),
   * TVBrowserIcons.down(TVBrowserIcons.SIZE_LARGE), ToolBar.BUTTON_ACTION,
   * this); mGoToNextDayAction = createAction(mLocalizer.msg( "goToNextDay",
   * "Next day"), "#goToNextDay", mLocalizer.msg("goToNextToolTip", "Next day"),
   * TVBrowserIcons.right(TVBrowserIcons.SIZE_SMALL),
   * TVBrowserIcons.right(TVBrowserIcons.SIZE_LARGE), ToolBar.BUTTON_ACTION,
   * this);
   * 
   * mScrollToNowAction = createAction(TVBrowser.mLocalizer.msg("button.now",
   * "Now"), "#scrollToNow", scrollTo + TVBrowser.mLocalizer.msg("button.now",
   * "Now"), IconLoader .getInstance().getIconFromTheme("actions",
   * "scroll-to-now", 16), IconLoader.getInstance().getIconFromTheme("actions",
   * "scroll-to-now", 22), ToolBar.BUTTON_ACTION, this);
   * 
   * 
   * createAction(TVBrowser.mLocalizer.msg( "button.pluginView", "Plugin View"),
   * "#pluginView", MainFrame.mLocalizer.msg("menuinfo.pluginView", ""),
   * IconLoader .getInstance().getIconFromTheme("actions", "view-tree", 16),
   * IconLoader.getInstance().getIconFromTheme("actions", "view-tree", 22),
   * ToolBar.TOOGLE_BUTTON_ACTION, this);
   * 
   * createAction(TVBrowser.mLocalizer.msg("button.filter", "Filter"),
   * "#filter", MainFrame.mLocalizer.msg("menuinfo.filter", ""),
   * IconLoader.getInstance().getIconFromTheme("actions","view-filter-list",16),
   * IconLoader.getInstance().getIconFromTheme("actions","view-filter-list",22),
   * ToolBar.TOOGLE_BUTTON_ACTION, this);
   * 
   * createAction createAction(TVBrowser.mLocalizer.msg("button.update",
   * "Update"), "#update", MainFrame.mLocalizer.msg("menuinfo.update", ""),
   * IconLoader.getInstance().getIconFromTheme("apps", "system-software-update",
   * 16), IconLoader.getInstance() .getIconFromTheme("apps",
   * "system-software-update", 22), ToolBar.BUTTON_ACTION, this);
   * 
   * 
   * mSettingsAction =
   * createAction(Localizer.getLocalization(Localizer.I18N_SETTINGS),
   * "#settings", MainFrame.mLocalizer.msg("menuinfo.settings", ""),
   * TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL),
   * TVBrowserIcons.preferences(TVBrowserIcons.SIZE_LARGE),
   * ToolBar.BUTTON_ACTION, this);
   */
}
