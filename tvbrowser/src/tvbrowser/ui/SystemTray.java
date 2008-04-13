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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.ButtonActionIf;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.extras.common.InternalPluginProxyList;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.ui.mainframe.MainFrame;
import tvdataservice.MarkedProgramsList;
import util.io.IOUtilities;
import util.misc.OperatingSystem;
import util.ui.ScrollableMenu;
import util.ui.UiUtilities;
import util.ui.menu.MenuUtil;

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
  private static java.util.logging.Logger mLog
  = java.util.logging.Logger.getLogger(SystemTray.class.getName());

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(SystemTray.class);

  /** State of the Window (max/normal) */
  private static int mState;
  private boolean mMenuCreated;
  private boolean mTime24 = !Settings.propTwelveHourFormat.getBoolean();

  private SystemTrayIf mSystemTray;

  private JMenuItem mOpenCloseMenuItem, mQuitMenuItem, mConfigure, mReminderItem;
  
  private JPopupMenu mTrayMenu;
  private Timer mClickTimer;
  
  private JMenu mPluginsMenu;

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
      
      mReminderItem = new JMenuItem(mLocalizer.msg("menu.pauseReminder","Pause Reminder"));
      mReminderItem.setIcon(IconLoader.getInstance().getIconFromTheme("apps",
          "appointment", 16));
      mReminderItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          toggleReminderState(false);
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
                toggleShowHide();
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

      mTrayMenu = new JPopupMenu();
      

      mSystemTray.addRightClickAction(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          //mTrayMenu.getPopupMenu().setVisible(false);
          buildMenu();
        }

      });
      mSystemTray.setTrayPopUp(mTrayMenu);

      mSystemTray.setVisible(Settings.propTrayIsEnabled.getBoolean());

      if (!Settings.propTrayUseSpecialChannels.getBoolean()
          && Settings.propTraySpecialChannels.getChannelArray().length == 0) {
        Channel[] channelArr = Settings.propSubscribedChannels.getChannelArray();
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
    
    mPluginsMenu = createPluginsMenu();
    mPluginsMenu.addSeparator();
    mPluginsMenu.add(mReminderItem);

    mTrayMenu.add(mPluginsMenu);
    mTrayMenu.addSeparator();
    
    mTrayMenu.addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuCanceled(PopupMenuEvent e) {}

      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {        
        mPluginsMenu.setEnabled(!UiUtilities.containsModalDialogChild(MainFrame.getInstance()));
      }
    });

    if (Settings.propTrayOnTimeProgramsEnabled.getBoolean()
        || Settings.propTrayNowProgramsEnabled.getBoolean()
        || Settings.propTraySoonProgramsEnabled.getBoolean()
        || Settings.propTrayImportantProgramsEnabled.getBoolean())
      searchForToAddingPrograms();

    if (Settings.propTrayOnTimeProgramsEnabled.getBoolean()) {
      if (!Settings.propTrayNowProgramsInSubMenu.getBoolean() &&
          Settings.propTrayNowProgramsEnabled.getBoolean() && 
          Settings.propTraySoonProgramsEnabled.getBoolean())
        mTrayMenu.addSeparator();
      addTimeInfoMenu();
    }

    if( Settings.propTrayNowProgramsEnabled.getBoolean() ||
        Settings.propTraySoonProgramsEnabled.getBoolean() ||
        Settings.propTrayOnTimeProgramsEnabled.getBoolean())
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
      Channel[] channels = Settings.propSubscribedChannels.getChannelArray();

      JComponent subMenu;

      // Put the programs in a submenu?
      if (Settings.propTrayNowProgramsInSubMenu.getBoolean() && Settings.propTrayNowProgramsEnabled.getBoolean())
        subMenu = new ScrollableMenu(mLocalizer.msg("menu.programsNow",
            "Now running programs"));
      else
        subMenu = mTrayMenu;

      ArrayList<ProgramMenuItem> programs = new ArrayList<ProgramMenuItem>();
      ArrayList<ProgramMenuItem> additional = new ArrayList<ProgramMenuItem>();
      ArrayList<ProgramMenuItem> nextPrograms = new ArrayList<ProgramMenuItem>();
      ArrayList<ProgramMenuItem> nextAdditionalPrograms = new ArrayList<ProgramMenuItem>();

      /*
       * Fill the ArrayList to support storing the programs on the correct
       * position in the list.
       */
      for (int i = 0; i < Settings.propTraySpecialChannels.getChannelArray().length; i++) {
        programs.add(i, null);
        nextPrograms.add(i, null);
      }

      /*
       * Search through all channels.
       */
      Date currentDate = Date.getCurrentDate();
      for (Channel channel : channels) {
        ChannelDayProgram today = TvDataBase.getInstance().getDayProgram(
            currentDate, channel);

        if (today != null && today.getProgramCount() > 0)
          for (int j = 0; j < today.getProgramCount(); j++) {
            if (j == 0
                && today.getProgramAt(j).getStartTime() > IOUtilities
                    .getMinutesAfterMidnight()) {
              ChannelDayProgram yesterday = TvDataBase
                  .getInstance()
                  .getDayProgram(currentDate.addDays(-1), channel);

              if (yesterday != null && yesterday.getProgramCount() > 0) {
                Program p = yesterday
                    .getProgramAt(yesterday.getProgramCount() - 1);

                if (p.isOnAir()) {
                  addProgramToNowRunning(p, programs, additional);
                  Program p1 = today.getProgramAt(0);
                  addToNext(p1, nextPrograms, nextAdditionalPrograms);
                  break;
                }
              }
            }

            Program p = today.getProgramAt(j);

            if (p.isOnAir()) {
              addProgramToNowRunning(p, programs, additional);
              if (j < today.getProgramCount() - 1) {
                Program p1 = today.getProgramAt(j + 1);
                addToNext(p1, nextPrograms, nextAdditionalPrograms);
              } else {
                ChannelDayProgram tomorrow = TvDataBase.getInstance()
                    .getDayProgram(currentDate.addDays(1),
                        channel);

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
          mTrayMenu.add(addToImportantMenu(new ScrollableMenu(mLocalizer.msg(
              "menu.programsImportant", "Important programs"))));
        } else
          addToImportantMenu(mTrayMenu);

      /*
       * if there are running programs and they should be displayed add them to
       * the menu.
       */
      
      if (Settings.propTrayImportantProgramsEnabled.getBoolean())
        mTrayMenu.addSeparator();
      
      boolean now = false;
      
      if (Settings.propTrayNowProgramsEnabled.getBoolean()
          && (programs.size() > 0 || additional.size() > 0)) {

        for (ProgramMenuItem item : programs) {
          if (item != null)
            subMenu.add(item);
        }
        for (ProgramMenuItem item : additional)
          subMenu.add(item);
        
        now = true;
        
        while(programs.contains(null))
          programs.remove(null);
        
        if(subMenu instanceof JMenu && programs.isEmpty() && additional.isEmpty())
          addNoProgramsItem(subMenu);
      }
      
      if (Settings.propTrayNowProgramsInSubMenu.getBoolean() && Settings.propTrayNowProgramsEnabled.getBoolean())
        mTrayMenu.add(subMenu);
      
      if(Settings.propTraySoonProgramsEnabled.getBoolean()
          && (!nextPrograms.isEmpty() || !nextAdditionalPrograms.isEmpty())) {        
        
      final JMenu next = new ScrollableMenu(now ? mLocalizer.msg("menu.programsSoon",
      "Soon runs") : mLocalizer.msg("menu.programsSoonAlone",
      "Soon runs"));

        int j = 0;

        for (ProgramMenuItem item : nextPrograms) {
          if (item != null) {
            item.setBackground(j);
            next.add(item);
            j++;
          }
        }
        for (ProgramMenuItem pItem : nextAdditionalPrograms) {
          pItem.setBackground(j);
          next.add(pItem);
          j++;
        }
       
        while(nextPrograms.contains(null))
          nextPrograms.remove(null);
        
        if(nextPrograms.isEmpty() && nextAdditionalPrograms.isEmpty())
          addNoProgramsItem(next);
          
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
        .getTimeSortedProgramsForTray(MainFrame.getInstance().getProgramFilter(),
            Settings.propTrayImportantProgramsPriority.getInt(), Settings.propTrayImportantProgramsSize.getInt(),
            !Settings.propTrayNowProgramsEnabled.getBoolean());
    
    boolean added = false;

    if (p.length > 0) {
      for (int i = 0; i < p.length; i++) {
        menu.add(new ProgramMenuItem(p[i], ProgramMenuItem.IMPORTANT_TYPE, -1,i));
        added = true;
      }
    }

    if (p.length == 0 || !added) {
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
    Channel[] channels = Settings.propTraySpecialChannels.getChannelArray();

    for (Channel channel : channels) {
      if (ch.equals(channel)) {
        return true;
      }
    }
    
    return false;
  }

  /**
   * @param ch
   *          The channel to get the index from.
   * @return The index of the channel in the tray channel list.
   */
  private int getIndexOfChannel(Channel ch) {
    Channel[] channels = Settings.propTraySpecialChannels.getChannelArray();

    for (int i = 0; i < channels.length; i++)
      if (ch.equals(channels[i]))
        return i;

    return -1;
  }

  /**
   * Add the time info menu.
   */
  private void addTimeInfoMenu() {
    JComponent time; 
    
    if(Settings.propTrayOnTimeProgramsInSubMenu.getBoolean()) {
      time = new JMenu(mLocalizer.msg("menu.programsAtTime",
        "Programs at time"));
      mTrayMenu.add(time);
    }
    else
      time = mTrayMenu;

    int[] tempTimes = Settings.propTimeButtons.getIntArray();
    
    ArrayList<Integer> today = new ArrayList<Integer>();
    ArrayList<Integer> tomorrow = new ArrayList<Integer>();
    
    for(int i = 0; i < tempTimes.length; i++)
      if(tempTimes[i] < IOUtilities.getMinutesAfterMidnight())
        tomorrow.add(new Integer(tempTimes[i]));
      else
        today.add(new Integer(tempTimes[i]));
    
    int[] times;
    
    if(tomorrow.isEmpty() || today.isEmpty())
      times = tempTimes;
    else {
      times = new int[tempTimes.length + 1];
      
      int j = 0;
      
      for(int i = 0; i < today.size(); i++) {
        times[j] = today.get(i).intValue();
        j++;
      }
      
      times[j] = -1;
      j++;
      
      for(int i = 0; i < tomorrow.size(); i++) {
        times[j] = tomorrow.get(i).intValue();
        j++;
      }        
    }

    for (int value : times) {
      if(value == -1) {
        if(time instanceof JMenu)
          ((JMenu)time).addSeparator();
        else
          ((JPopupMenu)time).addSeparator();
      }
      else {
      final int fvalue = value;
      
      final JMenu menu = new ScrollableMenu(IOUtilities.timeToString(value) + " "
          + (mTime24 ? mLocalizer.msg("menu.time", "") : ""));

      if (value < IOUtilities.getMinutesAfterMidnight())
        menu
            .setText(menu.getText() + " " + mLocalizer.msg("menu.tomorrow", ""));

      menu.addMenuListener(new MenuListener() {
        public void menuSelected(MenuEvent e) {
          createTimeProgramMenu(menu, fvalue);
        }

        public void menuCanceled(MenuEvent e) {}

        public void menuDeselected(MenuEvent e) {}
      });
      time.add(menu);
    }
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
      Channel[] c = Settings.propSubscribedChannels.getChannelArray();

      ArrayList<ProgramMenuItem> programs = new ArrayList<ProgramMenuItem>();
      ArrayList<ProgramMenuItem> additional = new ArrayList<ProgramMenuItem>();
      
      for (int i = 0; i < Settings.propTraySpecialChannels.getChannelArray().length; i++)
        programs.add(i, null);

      Date currentDate = Date.getCurrentDate();
      for (Channel ch : c) {
        Iterator<Program> it = null;
        int day = 0;
        
        try {
          it = TvDataBase.getInstance()
              .getDayProgram(
                  currentDate.addDays(
                      (time < IOUtilities.getMinutesAfterMidnight() ? ++day : day)),
                  ch).getPrograms();
        } catch (Exception ee) {}
        
        int count = 0;
        
        while (it != null && it.hasNext()) {
          Program p = it.next();

          int start = p.getStartTime();
          int end = p.getStartTime() + p.getLength();

          if (start <= time && time < end 
              && MainFrame.getInstance().getProgramFilter().accept(p)) {
            if (isOnChannelList(ch))
              programs.add(getIndexOfChannel(ch), new ProgramMenuItem(p,
                  ProgramMenuItem.ON_TIME_TYPE, time, -1));
            else if (p.getMarkerArr().length > 0 && p.getMarkPriority() >= Settings.propTrayImportantProgramsPriority.getInt())
              additional.add(new ProgramMenuItem(p,
                  ProgramMenuItem.ON_TIME_TYPE, time, -1));
          } else if(start > time && day == 1 && count == 0) {

            int temptime = time + 24 * 60;
            try {
              ChannelDayProgram dayProg = TvDataBase.getInstance()
                  .getDayProgram(
                      currentDate,
                      ch);
              p = dayProg.getProgramAt(dayProg.getProgramCount() - 1);
              
              start = p.getStartTime();
              end = p.getStartTime() + p.getLength();

              if (start <= temptime && temptime < end 
                  && MainFrame.getInstance().getProgramFilter().accept(p)) {
                if (isOnChannelList(ch))
                  programs.add(getIndexOfChannel(ch), new ProgramMenuItem(p,
                      ProgramMenuItem.ON_TIME_TYPE, time, -1));
                else if (p.getMarkerArr().length > 0 && p.getMarkPriority() >= Settings.propTrayImportantProgramsPriority.getInt())
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

      for (ProgramMenuItem pItem : programs) {
        if (pItem != null) {
          pItem.setBackground(j);
          menu.add(pItem);
          j++;
        }
      }
      for (ProgramMenuItem pItem : additional) {
        pItem.setBackground(j);
        menu.add(pItem);
        j++;
      }
      
      while(programs.contains(null))
        programs.remove(null);
            
      if(programs.isEmpty() && additional.isEmpty()) 
        addNoProgramsItem(menu);
    }
  }

  private void addNoProgramsItem(JComponent menu) {
    JMenuItem item = new JMenuItem(mLocalizer.msg("menu.noPrograms","No programs found."));
    item.setEnabled(false);
    menu.add(item);
  }
  
  /**
   * Checks and adds programs to a next list.
   * 
   * @param p
   *          The program to check and add.
   * @return False if the program was put on a list.
   */
  private boolean addToNext(Program p, ArrayList<ProgramMenuItem> nextPrograms,
      ArrayList<ProgramMenuItem> nextAdditionalPrograms) {
    if (!p.isExpired() && !p.isOnAir() 
        && MainFrame.getInstance().getProgramFilter().accept(p)) {
      if (this.isOnChannelList(p.getChannel())) {
        nextPrograms.set(getIndexOfChannel(p.getChannel()),
            new ProgramMenuItem(p, ProgramMenuItem.SOON_TYPE, -1, -1));
        return false;
      } else if (p.getMarkerArr().length > 0 && p.getMarkPriority() >= Settings.propTrayImportantProgramsPriority.getInt()) {
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
  private boolean addProgramToNowRunning(Program p, ArrayList<ProgramMenuItem> defaultList,
      ArrayList<ProgramMenuItem> addList) {
    if (p.isOnAir()
        && MainFrame.getInstance().getProgramFilter().accept(p)) {
      if (isOnChannelList(p.getChannel())) {
        defaultList.set(getIndexOfChannel(p.getChannel()), new ProgramMenuItem(
            p, ProgramMenuItem.NOW_TYPE, -1, -1));
        return true;
      } else if (p.getMarkerArr().length > 0 && p.getMarkPriority() >= Settings.propTrayImportantProgramsPriority.getInt()) {
        addList.add(new ProgramMenuItem(p, ProgramMenuItem.NOW_TYPE, -1,
            -1));
        return true;
      }
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
          toggleReminderState(true);
          
          if (Settings.propNowOnRestore.getBoolean())
            MainFrame.getInstance().scrollToNow();
        }
      });
      toggleOpenCloseMenuItem(false);
    } else {
      if(OperatingSystem.isWindows() || !Settings.propTrayMinimizeTo.getBoolean()) {
        MainFrame.getInstance().setExtendedState(JFrame.ICONIFIED);
      }
      
      if (Settings.propTrayMinimizeTo.getBoolean()) {
        MainFrame.getInstance().setVisible(false);
      }
      
      toggleOpenCloseMenuItem(true);
    }
  }
  
  private void toggleReminderState(boolean tvbShown) {
    if(mReminderItem.getText().compareTo(mLocalizer.msg("menu.pauseReminder","Pause Reminder")) == 0 && !tvbShown) {
      mReminderItem.setText(mLocalizer.msg("menu.continueReminder","Continue Reminder"));
      ReminderPlugin.getInstance().pauseRemider();
    }
    else {
      mReminderItem.setText(mLocalizer.msg("menu.pauseReminder","Pause Reminder"));
      ReminderPlugin.getInstance().handleTvBrowserStartFinished();
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

    Arrays.sort(plugins, new PluginProxy.Comparator());

    InternalPluginProxyIf[] internalPlugins = InternalPluginProxyList.getInstance().getAvailableProxys();
    
    for(InternalPluginProxyIf internalPlugin : internalPlugins) {
      if(internalPlugin instanceof ButtonActionIf) {
        ActionMenu action = ((ButtonActionIf)internalPlugin).getButtonAction();
        
        if (action != null) {
          pluginsMenu.add(MenuUtil.createMenuItem(action,false));
        }
      }
    }
    
    pluginsMenu.addSeparator();
    
    for (PluginProxy plugin : plugins) {
      ActionMenu action = plugin.getButtonAction();
      if (action != null) {
        pluginsMenu.add(MenuUtil.createMenuItem(action,false));
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