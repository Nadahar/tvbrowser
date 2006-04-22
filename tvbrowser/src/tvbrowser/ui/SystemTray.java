/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package tvbrowser.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.mainframe.MainFrame;
import tvdataservice.MarkedProgramsList;
import util.io.IOUtilities;
import util.program.ProgramUtilities;
import util.ui.ScrollableMenu;

import com.gc.systray.SystemTrayFactory;
import com.gc.systray.SystemTrayIf;
import com.gc.systray.WinSystemTray;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Date;
import devplugin.Program;
import devplugin.SettingsItem;

/**
 * This Class creates a SystemTray
 */
public class SystemTray {
  /** Using SystemTray ? */
  private boolean mUseSystemTray;

  /** Logger */
  private static java.util.logging.Logger mLog = java.util.logging.Logger
      .getLogger(SystemTray.class.getName());

  /** The localizer for this class. */
  public static util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(SystemTray.class);

  /** State of the Window (max/normal) */
  private static int mState;
  private boolean mMenuCreated;

  private SystemTrayIf mSystemTray;

  private JMenuItem mOpenCloseMenuItem, mQuitMenuItem, mConfigure;

  private ScrollableMenu mTrayMenu;
  private Timer mClickTimer;

  /**
   * Creates the SystemTray
   * 
   */
  public SystemTray() {}

  /**
   * Initializes the System
   * 
   * @return true if successfull
   */
  public boolean initSystemTray() {

    mUseSystemTray = false;

    mSystemTray = SystemTrayFactory.createSystemTray();

    if (mSystemTray != null) {

      if (mSystemTray instanceof WinSystemTray) {
        mUseSystemTray = mSystemTray.init(MainFrame.getInstance(),
            "imgs/systray.ico", TVBrowser.MAINWINDOW_TITLE);
        mLog.info("using windows system tray");
      } else {
        mUseSystemTray = mSystemTray.init(MainFrame.getInstance(),
            "imgs/tvbrowser16.png", TVBrowser.MAINWINDOW_TITLE);
        mLog.info("using default system tray");
      }
    } else {
      mUseSystemTray = false;
      Settings.propTrayIsEnabled.setBoolean(false);
    }

    return mUseSystemTray;
  }

  /**
   * Creates the Menus
   * 
   */
  public void createMenus() {
    if (!mUseSystemTray) {
      return;
    }
    if (!mMenuCreated) {
      mLog.info("platform independent mode is OFF");

      mOpenCloseMenuItem = new JMenuItem(mLocalizer.msg("menu.open", "Open"));
      Font f = mOpenCloseMenuItem.getFont();
      mOpenCloseMenuItem.setFont(f.deriveFont(Font.BOLD));
      mQuitMenuItem = new JMenuItem(mLocalizer.msg("menu.quit", "Quit"));

      mConfigure = new JMenuItem(mLocalizer.msg("menu.configure", "Configure"));

      mConfigure.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          MainFrame.getInstance().showSettingsDialog(SettingsItem.TRAY);
        }
      });

      mOpenCloseMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          toggleShowHide();
        }
      });

      mQuitMenuItem.addActionListener(new java.awt.event.ActionListener() {

        public void actionPerformed(java.awt.event.ActionEvent e) {
          mSystemTray.setVisible(false);
          MainFrame.getInstance().quit();
        }
      });

      mSystemTray.addLeftClickAction(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (mClickTimer == null || !mClickTimer.isRunning()) {
            toggleShowHide();
          }
        }
      });

      MainFrame.getInstance().addComponentListener(new ComponentListener() {

        public void componentResized(ComponentEvent e) {
          int state = MainFrame.getInstance().getExtendedState();
          if ((state & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
            mState = JFrame.MAXIMIZED_BOTH;
          } else {
            mState = JFrame.NORMAL;
          }
        }

        public void componentHidden(ComponentEvent e) {}

        public void componentMoved(ComponentEvent e) {}

        public void componentShown(ComponentEvent e) {}
      });

      MainFrame.getInstance().addWindowListener(
          new java.awt.event.WindowAdapter() {

            public void windowOpened(WindowEvent e) {
              toggleOpenCloseMenuItem(false);
            }

            public void windowClosing(java.awt.event.WindowEvent evt) {
              if (Settings.propOnlyMinimizeWhenWindowClosing.getBoolean()) {
                // Only minimize the main window, don't quit
                if (Settings.propTrayMinimizeTo.getBoolean())
                  MainFrame.getInstance().setVisible(false);
                else
                  MainFrame.getInstance().setExtendedState(JFrame.ICONIFIED);
                toggleOpenCloseMenuItem(true);
              } else {
                mSystemTray.setVisible(false);
                MainFrame.getInstance().quit();
              }
            }

            public void windowDeiconified(WindowEvent e) {
              toggleOpenCloseMenuItem(false);
            }

            public void windowIconified(java.awt.event.WindowEvent evt) {
              if (Settings.propTrayMinimizeTo.getBoolean()) {
                MainFrame.getInstance().setVisible(false);
              }
              toggleOpenCloseMenuItem(true);
            }
          });

      toggleOpenCloseMenuItem(false);

      mTrayMenu = new ScrollableMenu();
      

      mSystemTray.addRightClickAction(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          buildMenu();
        }

      });
      mSystemTray.setTrayPopUp(mTrayMenu.getPopupMenu());

      mSystemTray.setVisible(Settings.propTrayIsEnabled.getBoolean());

      if (!Settings.propTrayUseSpecialChannels.getBoolean()
          && Settings.propTraySpecialChannels
              .getChannelArray(false).length == 0) {
        Channel[] channelArr = Settings.propSubscribedChannels
            .getChannelArray(false);
        Channel[] tempArr = new Channel[channelArr.length > 10 ? 10
            : channelArr.length];
        for (int i = 0; i < tempArr.length; i++)
          tempArr[i] = channelArr[i];

        Settings.propTraySpecialChannels.setChannelArray(tempArr);
      }
      mMenuCreated = true;
    } else
      mSystemTray.setVisible(Settings.propTrayIsEnabled.getBoolean());
  }

  /**
   * Sets the visibility of the tray.
   * 
   * @param value
   *          True if visible.
   */
  public void setVisible(boolean value) {
    mSystemTray.setVisible(value);
  }

  private void buildMenu() {
    mTrayMenu.removeAll();
    mTrayMenu.add(mOpenCloseMenuItem);
    mTrayMenu.addSeparator();
    mTrayMenu.add(createPluginsMenu());
    mTrayMenu.addSeparator();

    if (Settings.propTrayOnTimeProgramsEnabled.getBoolean()
        || Settings.propTrayNowProgramsEnabled.getBoolean()
        || Settings.propTraySoonProgramsEnabled.getBoolean()
        || Settings.propTrayImportantProgramsEnabled.getBoolean())
      searchForToAddingPrograms();

    if (Settings.propTrayOnTimeProgramsEnabled.getBoolean()) {
      if (!Settings.propTrayNowProgramsInSubMenu.getBoolean())
        mTrayMenu.addSeparator();
      addTimeInfoMenu();
    }

    mTrayMenu.addSeparator();
    mTrayMenu.add(mConfigure);
    mTrayMenu.addSeparator();
    mTrayMenu.add(mQuitMenuItem);
  }

  /**
   * Searches the programs to show in the Tray.
   */
  private void searchForToAddingPrograms() {
    // show the now/soon running programs
    try {
      Channel[] channels = Settings.propSubscribedChannels
          .getChannelArray(false);

      JComponent subMenu;

      // Put the programs in a submenu?
      if (Settings.propTrayNowProgramsInSubMenu.getBoolean() && Settings.propTrayNowProgramsEnabled.getBoolean())
        subMenu = new JMenu(mLocalizer.msg("menu.programsNow",
            "Now running programs"));
      else
        subMenu = mTrayMenu;

      ArrayList programs = new ArrayList();
      ArrayList additional = new ArrayList();
      ArrayList nextPrograms = new ArrayList();
      ArrayList nextAdditionalPrograms = new ArrayList();

      /*
       * Fill the ArrayList to support storing the programs on the correct
       * position in the list.
       */
      for (int i = 0; i < Settings.propTraySpecialChannels
          .getChannelArray(false).length; i++) {
        programs.add(i, null);
        nextPrograms.add(i, null);
      }

      /*
       * Search through all channels.
       */
      for (int i = 0; i < channels.length; i++) {
        ChannelDayProgram today = TvDataBase.getInstance().getDayProgram(
            Date.getCurrentDate(), channels[i]);

        if (today != null && today.getProgramCount() > 0)
          for (int j = 0; j < today.getProgramCount(); j++) {
            if (j == 0
                && today.getProgramAt(j).getStartTime() > IOUtilities
                    .getMinutesAfterMidnight()) {
              ChannelDayProgram yesterday = TvDataBase
                  .getInstance()
                  .getDayProgram(Date.getCurrentDate().addDays(-1), channels[i]);

              if (yesterday != null && yesterday.getProgramCount() > 0) {
                Program p = yesterday
                    .getProgramAt(yesterday.getProgramCount() - 1);

                if (ProgramUtilities.isOnAir(p)) {
                  addProgramToNowRunning(p, programs, additional);
                  Program p1 = today.getProgramAt(0);
                  addToNext(p1, nextPrograms, nextAdditionalPrograms);
                  break;
                }
              }
            }

            Program p = today.getProgramAt(j);

            if (ProgramUtilities.isOnAir(p)) {
              addProgramToNowRunning(p, programs, additional);
              if (j < today.getProgramCount() - 1) {
                Program p1 = today.getProgramAt(j + 1);
                addToNext(p1, nextPrograms, nextAdditionalPrograms);
              } else {
                ChannelDayProgram tomorrow = TvDataBase.getInstance()
                    .getDayProgram(Date.getCurrentDate().addDays(1),
                        channels[i]);

                if (tomorrow != null && tomorrow.getProgramCount() > 0) {
                  Program p1 = tomorrow.getProgramAt(0);
                  addToNext(p1, nextPrograms, nextAdditionalPrograms);
                  break;
                }
              }

              break;
            }
          }
      }

      // Show important program?
      if (Settings.propTrayImportantProgramsEnabled.getBoolean())
        if (Settings.propTrayImportantProgramsInSubMenu.getBoolean()) {
          mTrayMenu.addSeparator();
          mTrayMenu.add(addToImportantMenu(new JMenu(mLocalizer.msg(
              "menu.programsImportant", "Important programs"))));
        } else
          addToImportantMenu(mTrayMenu);

      /*
       * if there are running programs and they should be displayed add them to
       * the menu.
       */
      
      if (((Settings.propTraySoonProgramsEnabled.getBoolean() && (!nextPrograms.isEmpty() || !nextAdditionalPrograms.isEmpty())) ||
          (Settings.propTrayNowProgramsEnabled.getBoolean() && (!programs.isEmpty() || !additional.isEmpty()))) &&
          !Settings.propTrayNowProgramsInSubMenu.getBoolean())
        mTrayMenu.addSeparator();
      
      boolean now = false;
      
      if (Settings.propTrayNowProgramsEnabled.getBoolean()
          && (programs.size() > 0 || additional.size() > 0)) {

        for (int i = 0; i < programs.size(); i++) {
          Object o = programs.get(i);
          if (o != null)
            subMenu.add((ProgramMenuItem) o);
        }
        for (int i = 0; i < additional.size(); i++)
          subMenu.add((ProgramMenuItem) additional.get(i));
        
        now = true;
      }
      
      if (Settings.propTrayNowProgramsInSubMenu.getBoolean() && Settings.propTrayNowProgramsEnabled.getBoolean())
        mTrayMenu.add(subMenu);
      
      if(Settings.propTraySoonProgramsEnabled.getBoolean()
          && (!nextPrograms.isEmpty() || !nextAdditionalPrograms.isEmpty())) {        
        
      final JMenu next = new JMenu(now ? mLocalizer.msg("menu.programsSoon",
      "Soon runs") : mLocalizer.msg("menu.programsSoonAlone",
      "Soon runs"));

        int j = 0;

        for (int i = 0; i < nextPrograms.size(); i++) {
          Object o = nextPrograms.get(i);
          if (o != null) {
            ProgramMenuItem pItem = (ProgramMenuItem) o;
            pItem.setBackground(j);
            next.add(pItem);
            j++;
          }
        }
        for (int i = 0; i < nextAdditionalPrograms.size(); i++) {
          ProgramMenuItem pItem = (ProgramMenuItem) nextAdditionalPrograms
              .get(i);
          pItem.setBackground(j);
          next.add(pItem);
          j++;
        }
        
        if(Settings.propTraySoonProgramsEnabled.getBoolean())
          mTrayMenu.add(next);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Adds the important programs to the menu.
   * 
   * @param menu
   *          The menu to on
   * @return The filled menu menu.
   */
  private JComponent addToImportantMenu(JComponent menu) {
    Program[] p = MarkedProgramsList.getInstance()
        .getTimeSortedProgramsForTray();

    if (menu instanceof JPopupMenu)
      ((JPopupMenu) menu).addSeparator();
    if (p.length > 0) {

      for (int i = 0; i < p.length; i++)
        menu.add(new ProgramMenuItem(p[i], ProgramMenuItem.IMPORTANT_TYPE, -1,
            i));
    }

    if (p.length == 0) {
      JMenuItem item = new JMenuItem(mLocalizer.msg("menu.noImportantPrograms",
          "No important programs found."));

      item.setEnabled(false);
      item.setForeground(Color.red);
      menu.add(item);
    }

    return menu;
  }

  /**
   * @param ch
   *          The channel to check.
   * @return True if the channel is on the tray channel list.
   */
  private boolean isOnChannelList(Channel ch) {
    Channel[] channels = Settings.propTraySpecialChannels
        .getChannelArray(false);

    for (int i = 0; i < channels.length; i++)
      if (ch.getId().compareTo(channels[i].getId()) == 0)
        return true;
    return false;
  }

  /**
   * @param ch
   *          The channel to get the index from.
   * @return The index of the channel in the tray channel list.
   */
  private int getIndexOfChannel(Channel ch) {
    Channel[] channels = Settings.propTraySpecialChannels
        .getChannelArray(false);

    for (int i = 0; i < channels.length; i++)
      if (ch.getId().compareTo(channels[i].getId()) == 0)
        return i;

    return -1;
  }

  /**
   * Add the time info menu.
   */
  private void addTimeInfoMenu() {
    JMenu time = new JMenu(mLocalizer.msg("menu.programsAtTime",
        "Programs at time"));
    mTrayMenu.add(time);

    int[] times = Settings.propTimeButtons.getIntArray();

    for (int i = 0; i < times.length; i++) {
      String minutes = String.valueOf(times[i] % 60);
      String hour = String.valueOf(times[i] / 60);

      if (minutes.length() == 1)
        minutes = "0" + minutes;
      if (hour.length() == 1)
        hour = "0" + hour;

      final int value = times[i];

      final JMenu menu = new JMenu(hour + ":" + minutes + " "
          + mLocalizer.msg("menu.time", ""));

      if (times[i] < IOUtilities.getMinutesAfterMidnight())
        menu
            .setText(menu.getText() + " " + mLocalizer.msg("menu.tomorrow", ""));

      menu.addMenuListener(new MenuListener() {
        public void menuSelected(MenuEvent e) {
          createTimeProgramMenu(menu, value);
        }

        public void menuCanceled(MenuEvent e) {}

        public void menuDeselected(MenuEvent e) {}
      });
      time.add(menu);
    }
  }

  /**
   * Creates the entries of a time menu.
   * 
   * @param menu
   *          The menu to put the programs on
   * @param time
   *          The time on which the programs are allowed to run.
   */
  private void createTimeProgramMenu(JMenu menu, int time) {
    // the menu is empty, so search for the programs at the time
    if (menu.getMenuComponentCount() < 1) {
      Channel[] c = Settings.propSubscribedChannels.getChannelArray(false);

      ArrayList programs = new ArrayList();
      ArrayList additional = new ArrayList();

      for (int i = 0; i < Settings.propTraySpecialChannels
          .getChannelArray(false).length; i++)
        programs.add(i, null);

      for (int i = 0; i < c.length; i++) {
        Iterator it = null;
        int day = 0;
        
        try {
          it = TvDataBase.getInstance()
              .getDayProgram(
                  Date.getCurrentDate().addDays(
                      (time < IOUtilities.getMinutesAfterMidnight() ? ++day : day)),
                  c[i]).getPrograms();
        } catch (Exception ee) {}
        
        int count = 0;
        
        while (it != null && it.hasNext()) {
          Program p = (Program) it.next();

          int start = p.getStartTime();
          int end = p.getStartTime() + p.getLength();

          if (start <= time && time < end) {
            if (isOnChannelList(c[i]))
              programs.add(getIndexOfChannel(c[i]), new ProgramMenuItem(p,
                  ProgramMenuItem.ON_TIME_TYPE, time, -1));
            else if (p.getMarkerArr().length > 0)
              additional.add(new ProgramMenuItem(p,
                  ProgramMenuItem.ON_TIME_TYPE, time, -1));
          } else if(start > time && day == 1 && count == 0) {

            int temptime = time + 24 * 60;
            try {
              ChannelDayProgram dayProg = TvDataBase.getInstance()
                  .getDayProgram(
                      Date.getCurrentDate(),
                      c[i]);
              p = dayProg.getProgramAt(dayProg.getProgramCount() - 1);
              
              start = p.getStartTime();
              end = p.getStartTime() + p.getLength();

              if (start <= temptime && temptime < end) {
                if (isOnChannelList(c[i]))
                  programs.add(getIndexOfChannel(c[i]), new ProgramMenuItem(p,
                      ProgramMenuItem.ON_TIME_TYPE, time, -1));
                else if (p.getMarkerArr().length > 0)
                  additional.add(new ProgramMenuItem(p,
                      ProgramMenuItem.ON_TIME_TYPE, time, -1));
              }
            } catch (Exception ee) {}
          } else if(start > time)
            break;
          
          count++;
        }
      }

      int j = 0;

      for (int i = 0; i < programs.size(); i++) {
        Object o = programs.get(i);
        if (o != null) {
          ProgramMenuItem pItem = (ProgramMenuItem) o;
          pItem.setBackground(j);
          menu.add(pItem);
          j++;
        }
      }
      for (int i = 0; i < additional.size(); i++) {
        ProgramMenuItem pItem = (ProgramMenuItem) additional.get(i);
        pItem.setBackground(j);
        menu.add(pItem);
        j++;
      }
    }
  }

  /**
   * Checks and adds programs to a next list.
   * 
   * @param p
   *          The program to check and add.
   * @return False if the program was put on a list.
   */
  private boolean addToNext(Program p, ArrayList nextPrograms,
      ArrayList nextAdditionalPrograms) {
    if (!p.isExpired() && !ProgramUtilities.isOnAir(p)) {
      if (this.isOnChannelList(p.getChannel())) {
        nextPrograms.set(getIndexOfChannel(p.getChannel()),
            new ProgramMenuItem(p, ProgramMenuItem.SOON_TYPE, -1, -1));
        return false;
      } else if (p.getMarkerArr().length > 0) {
        nextAdditionalPrograms.add(new ProgramMenuItem(p,
            ProgramMenuItem.SOON_TYPE, -1, -1));
        return false;
      }
    }

    return true;
  }

  /**
   * Checks and adds programs to a now running list.
   * 
   * @param p
   *          The program to check and add to a list.
   * @param defaultList
   *          The list with the programs on a selected channel.
   * @param addList
   *          The list with the programs that are not on a selected channel, but
   *          are important.
   * @return True if the program was added to a list.
   */
  private boolean addProgramToNowRunning(Program p, ArrayList defaultList,
      ArrayList addList) {
    if (ProgramUtilities.isOnAir(p))
      if (isOnChannelList(p.getChannel())) {
        defaultList.set(getIndexOfChannel(p.getChannel()), new ProgramMenuItem(
            p, ProgramMenuItem.NOW_TYPE, -1, -1));
        return true;
      } else if (p.getMarkerArr().length > 0) {
        addList.add(new ProgramMenuItem(p, ProgramMenuItem.NOW_TYPE, -1,
            -1));
        return true;
      }

    return false;
  }

  /**
   * Toggle the Text in the Open/Close-Menu
   * 
   * @param open
   *          True, if "Open" should be displayed
   */
  private void toggleOpenCloseMenuItem(boolean open) {
    if (open)
      mOpenCloseMenuItem.setText(mLocalizer.msg("menu.open", "Open"));
    else
      mOpenCloseMenuItem.setText(mLocalizer.msg("menu.close", "Close"));
  }

  /**
   * Toggle Hide/Show of the MainFrame
   */
  private void toggleShowHide() {
    mClickTimer = new Timer(200, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mClickTimer.stop();
      }
    });
    mClickTimer.start();

    if (!MainFrame.getInstance().isVisible()
        || (MainFrame.getInstance().getExtendedState() == JFrame.ICONIFIED)) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          MainFrame.getInstance().showFromTray(mState);
        }
      });
      toggleOpenCloseMenuItem(false);
    } else {
      if (Settings.propTrayMinimizeTo.getBoolean())
        MainFrame.getInstance().setVisible(false);
      else
        MainFrame.getInstance().setExtendedState(JFrame.ICONIFIED);
      toggleOpenCloseMenuItem(true);
    }
  }

  /**
   * Creates the Plugin-Menus
   * 
   * @return Plugin-Menu
   */
  private static JMenu createPluginsMenu() {
    JMenu pluginsMenu = new JMenu(mLocalizer.msg("menu.plugins", "Plugins"));

    PluginProxy[] plugins = PluginProxyManager.getInstance()
        .getActivatedPlugins();
    updatePluginsMenu(pluginsMenu, plugins);

    return pluginsMenu;
  }

  /**
   * @deprecated TODO: check, if we can remove this method
   * @param pluginsMenu
   * @param plugins
   */
  private static void updatePluginsMenu(JMenu pluginsMenu, PluginProxy[] plugins) {
    pluginsMenu.removeAll();

    Arrays.sort(plugins, new Comparator() {

      public int compare(Object o1, Object o2) {
        return o1.toString().compareTo(o2.toString());
      }

    });

    for (int i = 0; i < plugins.length; i++) {
      ActionMenu action = plugins[i].getButtonAction();
      if (action != null) {
        pluginsMenu.add(new JMenuItem(action.getAction()));

      }
    }
  }

  /**
   * Is the Tray activated and used?
   * 
   * @return is Tray used?
   */
  public boolean isTrayUsed() {
    return mUseSystemTray;
  }

}