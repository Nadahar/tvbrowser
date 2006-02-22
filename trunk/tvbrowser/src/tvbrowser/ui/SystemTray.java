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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.SettingsDialog;
import util.io.IOUtilities;

import com.gc.systray.SystemTrayFactory;
import com.gc.systray.SystemTrayIf;
import com.gc.systray.WinSystemTray;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Date;
import devplugin.Program;

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

  private SystemTrayIf mSystemTray;

  private JMenuItem mOpenCloseMenuItem, mQuitMenuItem, mConfigure;

  private JPopupMenu mTrayMenu;

  private ArrayList mNextPrograms = new ArrayList();
  private ArrayList mNextAdditionalPrograms = new ArrayList();
  private Hashtable mImportantOnListPrograms = new Hashtable();
  private Hashtable mImportantOffListPrograms = new Hashtable();

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

    mLog.info("platform independent mode is OFF");

    mOpenCloseMenuItem = new JMenuItem(mLocalizer.msg("menu.open", "Open"));
    Font f = mOpenCloseMenuItem.getFont();
    mOpenCloseMenuItem.setFont(f.deriveFont(Font.BOLD));
    mQuitMenuItem = new JMenuItem(mLocalizer.msg("menu.quit", "Quit"));

    mConfigure = new JMenuItem(mLocalizer.msg("menu.configure", "Configure"));

    mConfigure.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showSettingsDialog(SettingsDialog.TAB_ID_TRAY);
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
        if (Settings.propUseSingeClickInTray.getBoolean()) {
          toggleShowHide();
        }
      }
    });

    mSystemTray.addLeftDoubleClickAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!Settings.propUseSingeClickInTray.getBoolean()) {
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
              if (Settings.propMinimizeToTray.getBoolean())
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
            if (Settings.propMinimizeToTray.getBoolean()) {
              MainFrame.getInstance().setVisible(false);
            }
            toggleOpenCloseMenuItem(true);
          }
        });

    toggleOpenCloseMenuItem(false);

    mTrayMenu = new JPopupMenu();

    mSystemTray.addRightClickAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        buildMenu();
      }

    });
    mSystemTray.setTrayPopUp(mTrayMenu);

    mSystemTray.setVisible(true);

    if (!Settings.propShowProgramsInTrayWasConfigured.getBoolean()
        && Settings.propNowRunningProgramsInTrayChannels.getChannelArray(false).length == 0) {
      Channel[] channelArr = Settings.propSubscribedChannels
          .getChannelArray(false);
      Channel[] tempArr = new Channel[channelArr.length > 10 ? 10
          : channelArr.length];
      for (int i = 0; i < tempArr.length; i++)
        tempArr[i] = channelArr[i];

      Settings.propNowRunningProgramsInTrayChannels.setChannelArray(tempArr);
    }
  }

  private void buildMenu() {
    mTrayMenu.removeAll();
    mNextPrograms.clear();
    mNextAdditionalPrograms.clear();
    mTrayMenu.add(mOpenCloseMenuItem);
    mTrayMenu.addSeparator();
    mTrayMenu.add(createPluginsMenu());

    if (Settings.propShowProgramsInTrayEnabled.getBoolean())
      searchForToAddingPrograms();

    if (Settings.propShowProgramsInTrayEnabled.getBoolean()) {
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
    // show the now running programs
    boolean add = Settings.propShowNowRunningProgramsInTray.getBoolean();
    try {
      Channel[] channels = Settings.propSubscribedChannels
          .getChannelArray(false);

      JComponent subMenu;

      // Put the programs in a submenu?
      if (Settings.propShowNowRunningProgramsInTrayInSubMenu.getBoolean())
        subMenu = new JMenu(mLocalizer.msg("menu.programsNow",
            "Now running programs"));
      else
        subMenu = mTrayMenu;

      ArrayList programs = new ArrayList();
      ArrayList additional = new ArrayList();

      /*
       * Fill the ArrayList to support storing the programs on the correct
       * position in the list.
       */
      for (int i = 0; i < Settings.propNowRunningProgramsInTrayChannels
          .getChannelArray(false).length; i++) {
        programs.add(i, null);
        mNextPrograms.add(i, null);
      }

      /*
       * Search through all channels.
       */
      for (int i = 0; i < channels.length; i++) {
        Iterator it = null;

        try {
          it = TvDataBase.getInstance().getDayProgram(Date.getCurrentDate(),
              channels[i]).getPrograms();
        } catch (Exception e) {}

        boolean found = false;
        boolean findnext = true;
        boolean complete = false;

        while (it != null && it.hasNext()) {
          Program p = (Program) it.next();
          // return true if the program was stored in a list
          found = addProgramToNowRunning(p, programs, additional);

          if (isOnAir(p) && it.hasNext()) {
            p = (Program) it.next();
            /*
             * find the next program for the now list returns false if the
             * program was found, because there's no need to search again.
             */
            findnext = addToNext(p);

            // Search for the important programs.
            if (Settings.propShowImportantProgramsInTray.getBoolean())
              complete = searchForImportantPrograms(p, it);
          }

          // if found stop the search for this channel
          if (found || !findnext)
            break;
        }
        Program p = null;
        
        // if not found search the previous day.
        if (!found) {
          ChannelDayProgram channelDayProgram = TvDataBase.getInstance()
              .getDayProgram(Date.getCurrentDate().addDays(-1), channels[i]);
          
          if (channelDayProgram != null) {
            p = channelDayProgram.getProgramAt(channelDayProgram.getProgramCount() - 1);
            addProgramToNowRunning(p,programs, additional);
          }

          /*
           * The last checked programs start time is in the time range off the
           * important programs if it was found, so search on today.
           */
          if (!complete
              && Settings.propShowImportantProgramsInTray.getBoolean() && p != null &&isOnAir(p)) {
            try {
              it = TvDataBase.getInstance().getDayProgram(
                  Date.getCurrentDate(), channels[i]).getPrograms();
            } catch (Exception e) {}

            if (it != null && it.hasNext())
              complete = searchForImportantPrograms((Program) it.next(), it);
          }
        }
        // find the next program for the next list.
        if (findnext) {
          ChannelDayProgram channelDayProgram = TvDataBase.getInstance()
              .getDayProgram(Date.getCurrentDate().addDays(p != null && isOnAir(p) ? 0 : 1),
                  channels[i]);

          if (channelDayProgram != null
              && channelDayProgram.getProgramCount() > 0)
            addToNext(channelDayProgram.getProgramAt(0));
        }

        if (!complete && Settings.propShowImportantProgramsInTray.getBoolean()
            && (p == null || !isOnAir(p))) {
          try {
            it = TvDataBase.getInstance().getDayProgram(
                Date.getCurrentDate().addDays(1), channels[i]).getPrograms();
          } catch (Exception e) {}

          if (it != null && it.hasNext())
            complete = searchForImportantPrograms((Program) it.next(), it);
        }
      }

      // Show important program?
      if (Settings.propShowImportantProgramsInTray.getBoolean())
        if (Settings.propShowImportantProgramsInTrayInSubMenu.getBoolean())
          mTrayMenu.add(addToImportantMenu(new JMenu(mLocalizer.msg(
              "menu.programsImportant", "Important programs"))));
        else
          addToImportantMenu(mTrayMenu);

      /*
       * if there are running programs and they should be displayed add them to
       * the menu.
       */
      if (add && (programs.size() > 0 || additional.size() > 0)) {
        mTrayMenu.addSeparator();
        for (int i = 0; i < programs.size(); i++) {
          Object o = programs.get(i);
          if (o != null)
            subMenu.add((ProgramMenuItem) o);
        }
        for (int i = 0; i < additional.size(); i++)
          subMenu.add((ProgramMenuItem) additional.get(i));

        /*
         * if the program sould be in a submenu add the menu to the propup menu
         */
        if (Settings.propShowNowRunningProgramsInTrayInSubMenu.getBoolean())
          mTrayMenu.add(subMenu);
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
    ArrayList important = new ArrayList();

    Channel[] nowChannels = Settings.propNowRunningProgramsInTrayChannels
        .getChannelArray(false);
    Channel[] c = Settings.propSubscribedChannels.getChannelArray(false);

    boolean isEmpty = false;

    /*
     * Add the important programs that are on a selected channel to the list
     * first.
     */
    while (important.size() < Settings.propImportantProgramsInTraySize.getInt()
        && !isEmpty) {
      isEmpty = true;
      for (int i = 0; i < nowChannels.length; i++) {
        boolean empty = putOnImportant(mImportantOnListPrograms, important,
            nowChannels[i]);

        if (!empty)
          isEmpty = false;
      }
    }

    isEmpty = false;

    /*
     * If the maximum number of important program isn't reached, add the
     * important programs from the channels that are not selected to the list.
     */
    while (important.size() < Settings.propImportantProgramsInTraySize.getInt()
        && !isEmpty) {
      isEmpty = true;
      for (int i = 0; i < c.length; i++) {
        if (!isOnChannelList(c[i])) {
          boolean empty = putOnImportant(mImportantOffListPrograms, important,
              c[i]);

          if (!empty)
            isEmpty = false;
        }
      }
    }

    if (!important.isEmpty())
      if (menu instanceof JPopupMenu)
        ((JPopupMenu) menu).addSeparator();

    // fill the menu with the list entries.
    for (int i = 0; i < Settings.propImportantProgramsInTraySize.getInt(); i++)
      if (!important.isEmpty()) {
        menu
            .add(new ProgramMenuItem((Program) important.remove(0),
                Settings.propImportantProgramsInTrayContainsStartTime
                    .getBoolean()));
      } else
        break;

    return menu;
  }

  /**
   * Put a program of the Hastable to the target list.
   * 
   * @param src
   *          The Hashtable to get the program from.
   * @param target
   *          The target list.
   * @param c
   *          The channel to get the programs from.
   * @return True if the list in the Hastable is empty
   */
  private boolean putOnImportant(Hashtable src, ArrayList target, Channel c) {
    ArrayList list = (ArrayList) src.get(c);

    if (list == null)
      return true;

    if (!list.isEmpty())
      target.add((Program) list.remove(0));

    if (!list.isEmpty())
      return false;

    return true;
  }

  /**
   * Searches for the important programs
   * 
   * @param p
   *          The first program to check.
   * @param it
   *          The programs to check.
   * @return True if the maximum number of important programs was found
   */
  private boolean searchForImportantPrograms(Program p, Iterator it) {
    ArrayList toFill;
    Hashtable target;

    // select the target list to use
    if (isOnChannelList(p.getChannel()))
      target = mImportantOnListPrograms;
    else
      target = mImportantOffListPrograms;

    if (target.get(p.getChannel()) != null)
      toFill = (ArrayList) target.get(p.getChannel());
    else {
      toFill = new ArrayList();
      target.put(p.getChannel(), toFill);
    }

    int time = IOUtilities.getMinutesAfterMidnight()
        + Settings.propImportantProgramsInTrayHours.getInt() * 60;

    boolean complete = checkAndPutOnImportantList(toFill, p, time);

    // check the programs if they are important
    while (!complete && it.hasNext())
      complete = checkAndPutOnImportantList(toFill, (Program) it.next(), time);

    return complete;
  }

  /**
   * Checks if the program it to put on the important list.
   * 
   * @param list
   *          The list to put on.
   * @param p
   *          The program to check.
   * @param time
   *          The time in wich the program can be.
   * @return True if the programs start time is later than the allowed time.
   */
  private boolean checkAndPutOnImportantList(ArrayList list, Program p, int time) {
    int start = p.getStartTime();
    
    if (Date.getCurrentDate().addDays(1).compareTo(p.getDate()) == 0)
      start += 24 * 60;

    if (p.getMarkerArr().length > 0 && start <= time)
      list.add(p);
    else if (start > time)
      return true;

    return false;
  }

  /**
   * @param ch
   *          The channel to check.
   * @return True if the channel is on the tray channel list.
   */
  private boolean isOnChannelList(Channel ch) {
    Channel[] channels = Settings.propNowRunningProgramsInTrayChannels
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
    Channel[] channels = Settings.propNowRunningProgramsInTrayChannels
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

    JMenu next = new JMenu(mLocalizer.msg("menu.programsSoon", "Soon"));

    for (int i = 0; i < mNextPrograms.size(); i++) {
      Object o = mNextPrograms.get(i);
      if (o != null)
        next.add((ProgramMenuItem) o);
    }
    for (int i = 0; i < mNextAdditionalPrograms.size(); i++)
      next.add((ProgramMenuItem) mNextAdditionalPrograms.get(i));

    time.add(next);

    int[] times = Settings.propTimeButtons.getIntArray();

    for (int i = 0; i < times.length; i++) {
      String minutes = String.valueOf(times[i] % 60);

      if (minutes.length() == 1)
        minutes = "0" + minutes;

      final int value = times[i];
      final JMenu menu = new JMenu(value / 60 + ":" + minutes);

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

      for (int i = 0; i < Settings.propNowRunningProgramsInTrayChannels
          .getChannelArray(false).length; i++)
        programs.add(i, null);

      for (int i = 0; i < c.length; i++) {
        Iterator it = null;

        try {
          it = TvDataBase.getInstance().getDayProgram(Date.getCurrentDate(),
              c[i]).getPrograms();
        } catch (Exception ee) {}

        while (it != null && it.hasNext()) {
          Program p = (Program) it.next();

          int start = p.getStartTime();
          int end = p.getStartTime() + p.getLength();

          if (start <= time && time < end)
            if (isOnChannelList(c[i]))
              programs.add(getIndexOfChannel(c[i]),
                  new ProgramMenuItem(p, true));
            else if (p.getMarkerArr().length > 0)
              additional.add(new ProgramMenuItem(p, true));
        }
      }

      for (int i = 0; i < programs.size(); i++) {
        Object o = programs.get(i);
        if (o != null)
          menu.add((ProgramMenuItem) o);
      }
      for (int i = 0; i < additional.size(); i++)
        menu.add((ProgramMenuItem) additional.get(i));
    }
  }

  /**
   * Checks and adds programs to a next list.
   * 
   * @param p
   *          The program to check and add.
   * @return False if the program was put on a list.
   */
  private boolean addToNext(Program p) {
    if (!p.isExpired() && !isOnAir(p)) {
      if (this.isOnChannelList(p.getChannel())) {
        mNextPrograms.set(getIndexOfChannel(p.getChannel()),
            new ProgramMenuItem(p, true));
        return false;
      } else if (p.getMarkerArr().length > 0) {
        mNextAdditionalPrograms.add(new ProgramMenuItem(p, true));
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
    if (isOnAir(p))
      if (isOnChannelList(p.getChannel())) {
        defaultList.set(getIndexOfChannel(p.getChannel()), new ProgramMenuItem(
            p, Settings.propNowRunningProgramsInTrayContainsStartTime
                .getBoolean()));
        return true;
      } else if (p.getMarkerArr().length > 0) {
        addList
            .add(new ProgramMenuItem(p,
                Settings.propNowRunningProgramsInTrayContainsStartTime
                    .getBoolean()));
        return true;
      }

    return false;
  }

  /**
   * Helper method to check if a program runs.
   * 
   * @param p
   *          The program to check.
   * @return True if the program runs.
   */
  private boolean isOnAir(Program p) {

    int time = IOUtilities.getMinutesAfterMidnight();

    if (Date.getCurrentDate().addDays(-1).compareTo(p.getDate()) == 0)
      time += 24 * 60;

    if (p.getStartTime() <= time && (p.getStartTime() + p.getLength()) > time)
      return true;
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
    if (!MainFrame.getInstance().isVisible()
        || (MainFrame.getInstance().getExtendedState() == JFrame.ICONIFIED)) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          MainFrame.getInstance().showFromTray(mState);
        }
      });
      toggleOpenCloseMenuItem(false);
    } else {
      if (Settings.propMinimizeToTray.getBoolean())
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