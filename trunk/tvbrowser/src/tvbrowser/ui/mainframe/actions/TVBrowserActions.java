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
import java.util.ArrayList;
import java.util.List;

import tvbrowser.core.TvDataUpdater;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.toolbar.ToolBar;
import util.misc.OperatingSystem;
import util.ui.TVBrowserIcons;
import devplugin.SettingsItem;

/**
 * common TV-Browser actions
 *
 * @author bananeweizen
 *
 */
public final class TVBrowserActions {

  private static ArrayList<TVBrowserAction> mAllActions = new ArrayList<TVBrowserAction>();

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

  public final static TVBrowserAction fontSizeLargerNumPad = new TVBrowserAction("fontSizeLargerNumPad", TVBrowserIcons
      .zoomIn(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.zoomIn(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_ADD, Toolkit
      .getDefaultToolkit().getMenuShortcutKeyMask(), ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      fontSizeLarger.actionPerformed(e);
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
   * decrease program table font size
   */
  public final static TVBrowserAction fontSizeSmallerNumPad = new TVBrowserAction("fontSizeSmallerNumPad", TVBrowserIcons
      .zoomOut(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.zoomOut(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_SUBTRACT,
      Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      fontSizeSmaller.actionPerformed(e);
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
   * set program table font size to default
   */
  public final static TVBrowserAction fontSizeDefaultNumPad = new TVBrowserAction("fontSizeDefaultNumPad", KeyEvent.VK_NUMPAD0, Toolkit
      .getDefaultToolkit().getMenuShortcutKeyMask()) {

    @Override
    public void actionPerformed(ActionEvent e) {
      fontSizeDefault.actionPerformed(e);
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
   * increase program table column width
   */
  public final static TVBrowserAction columnWidthLargerNumPad = new TVBrowserAction("columnWidthLargerNumPad", TVBrowserIcons
      .zoomIn(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.zoomIn(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_ADD,
      InputEvent.ALT_MASK, ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      columnWidthLarger.actionPerformed(e);
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
   * decrease program table column width
   */
  public final static TVBrowserAction columnWidthSmallerNumPad = new TVBrowserAction("columnWidthSmallerNumPad", TVBrowserIcons
      .zoomOut(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.zoomOut(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_SUBTRACT,
      InputEvent.ALT_MASK, ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      columnWidthSmaller.actionPerformed(e);
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
   * set column width to default size
   */
  public final static TVBrowserAction columnWidthDefaultNumPad = new TVBrowserAction("columnWidthDefaultNumPad", KeyEvent.VK_NUMPAD0,
      InputEvent.ALT_MASK) {

    @Override
    public void actionPerformed(ActionEvent e) {
      columnWidthDefault.actionPerformed(e);
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
      if (isUpdating()) {
        TvDataUpdater.getInstance().stopDownload();
      }
      else {
        MainFrame.getInstance().updateTvData();
      }
    }

    public boolean useEllipsis() {
      return true;
    };

  };

  /**
   * open settings dialog
   */
  public final static TVBrowserAction settings = new TVBrowserAction("settings", TVBrowserIcons
      .preferences(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.preferences(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_S,
      InputEvent.CTRL_MASK, ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().showSettingsDialog();
    }

    public String getMenuText() {
      // Windows guidelines explicitly require "Options"
      if (OperatingSystem.isWindows()) {
        return mLocalizer.msg(getKey() + ".menu.win", "&Options");
      } else {
        return mLocalizer.msg(getKey() + ".menu", "&Settings");
      }
    };

    public boolean useEllipsis() {
      return true;
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
   * show/hide plugin view action
   */
  public final static TVBrowserAction pluginView = new TVBrowserAction("pluginView", TVBrowserIcons
      .viewTree(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.viewTree(TVBrowserIcons.SIZE_LARGE), 0, 0,
      ToolBar.TOOGLE_BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().setShowPluginOverview(!MainFrame.getInstance().isShowingPluginOverview());
    }
  };

  /**
   * scroll to now action
   */
  public final static TVBrowserAction scrollToNow = new TVBrowserAction("scrollToNow", TVBrowserIcons
      .scrollToNow(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.scrollToNow(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_F9,
      0, ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().scrollToNow();
    }
  };

  /**
   * previous day action
   */
  public final static TVBrowserAction goToPreviousDay = new TVBrowserAction("goToPreviousDay", TVBrowserIcons
      .left(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.left(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_LEFT,
      InputEvent.ALT_MASK, ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().goToPreviousDay();
    }
  };

  /**
   * today action
   */
  public final static TVBrowserAction goToToday = new TVBrowserAction("goToToday", TVBrowserIcons
      .down(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.down(TVBrowserIcons.SIZE_LARGE), 0, 0, ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().goToToday();
    }
  };

  /**
   * next day action
   */
  public final static TVBrowserAction goToNextDay = new TVBrowserAction("goToNextDay", TVBrowserIcons
      .right(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.right(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_RIGHT,
      InputEvent.ALT_MASK, ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().goToNextDay();
    }
  };

  /**
   * previous week action
   */
  public final static TVBrowserAction goToPreviousWeek = new TVBrowserAction("goToPreviousWeek", TVBrowserIcons
      .previousWeek(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.previousWeek(TVBrowserIcons.SIZE_LARGE), 0, 0,
      ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().goToPreviousWeek();
    }
  };

  /**
   * next week action
   */
  public final static TVBrowserAction goToNextWeek = new TVBrowserAction("goToNextWeek", TVBrowserIcons
      .nextWeek(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.nextWeek(TVBrowserIcons.SIZE_LARGE), 0, 0,
      ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().goToNextWeek();
    }
  };

  /**
   * scroll to date (popup menu) action
   */
  public final static TVBrowserAction goToDate = new TVBrowserAction("goToDate", TVBrowserIcons
      .goToDate(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.goToDate(TVBrowserIcons.SIZE_LARGE), 0, 0,
      ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      showPopupMenu();
    }

  };

  /**
   * scroll to channel (popup menu) action
   */
  public final static TVBrowserAction scrollToChannel = new TVBrowserAction("scrollToChannel", TVBrowserIcons
      .scrollToChannel(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.scrollToChannel(TVBrowserIcons.SIZE_LARGE), 0, 0,
      ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      showPopupMenu();
    }

  };

  /**
   * scroll to time (popup menu) action
   */
  public final static TVBrowserAction scrollToTime = new TVBrowserAction("scrollToTime", TVBrowserIcons
      .scrollToTime(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.scrollToTime(TVBrowserIcons.SIZE_LARGE), 0, 0,
      ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      showPopupMenu();
    }

  };

  /**
   * switch full screen action
   */
  public final static TVBrowserAction fullScreen = new TVBrowserAction("fullscreen", TVBrowserIcons
      .fullScreen(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.fullScreen(TVBrowserIcons.SIZE_LARGE), KeyEvent.VK_F11, 0,
      ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().switchFullscreenMode();
    }
  };

  /**
   * configure channels action
   */
  public static final TVBrowserAction configureChannels = new TVBrowserAction("configureChannels", TVBrowserIcons
      .preferences(TVBrowserIcons.SIZE_SMALL), TVBrowserIcons.preferences(TVBrowserIcons.SIZE_LARGE), 0, 0,
      ToolBar.BUTTON_ACTION) {

    @Override
    public void actionPerformed(ActionEvent e) {
      MainFrame.getInstance().showSettingsDialog(SettingsItem.CHANNELS);
    }

    public boolean useEllipsis() {
      return true;
    };
  };

  /**
   * register this action, so we can iterate over all actions later
   * @param tvBrowserAction
   */
  public static void register(final TVBrowserAction tvBrowserAction) {
    mAllActions.add(tvBrowserAction);
  }

  public static List<TVBrowserAction> getActions() {
    return (List<TVBrowserAction>) mAllActions.clone();
  }
}
