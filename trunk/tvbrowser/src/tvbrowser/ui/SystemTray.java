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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.mainframe.MainFrame;

import com.gc.systray.SystemTrayFactory;
import com.gc.systray.SystemTrayIf;
import com.gc.systray.WinSystemTray;

import devplugin.ActionMenu;

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
    public static util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(SystemTray.class);

    /** Image-Handle */
    private int mSystrayImageHandle;
    
    /** State of the Window (max/normal) */
    private static int mState;    
    
    private SystemTrayIf mSystemTray;

    
    /**
     * Creates the SystemTray
     *
     */
    public SystemTray() {
    }

    /**
     * Initializes the System
     * @return true if successfull
     */
    public boolean initSystemTray() {
      
      mUseSystemTray = false;
      
      mSystemTray = SystemTrayFactory.createSystemTray();
      
      if (mSystemTray != null) {
    	  
    	  if (mSystemTray instanceof WinSystemTray) {
   	        mUseSystemTray = mSystemTray.init(MainFrame.getInstance(), "imgs/tvbrowser.ico", MainFrame.getInstance().getTitle());
    	  } else {
   	        mUseSystemTray = mSystemTray.init(MainFrame.getInstance(), "imgs/tvbrowser16.png", MainFrame.getInstance().getTitle());
    	  }
      } else {
        mUseSystemTray = false;
      }
      return mUseSystemTray;
    }

    /**
     * Creates the Menus
     * @param mainFrame MainFrame to use for hide/show
     */
    public void createMenus() {
        if (!mUseSystemTray) {
          return;
        }
        
        mLog.info("platform independent mode is OFF");

        JPopupMenu trayMenu = new JPopupMenu();
        final JMenuItem openMenuItem = new JMenuItem(mLocalizer.msg("menu.open", "Open"));
        JMenuItem quitMenuItem = new JMenuItem(mLocalizer.msg("menu.quit", "Quit"));
        trayMenu.add(openMenuItem);
        trayMenu.addSeparator();
        trayMenu.add(createPluginsMenu());
        trayMenu.addSeparator();
        trayMenu.add(quitMenuItem);

        openMenuItem.setEnabled(false);

        openMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent e) {
                MainFrame.getInstance().show();
                MainFrame.getInstance().toFront();
                MainFrame.getInstance().setExtendedState(mState);
            }
        });

        quitMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent e) {
                mSystemTray.setVisible(false);
                MainFrame.getInstance().quit();
            }
        });
        
        mSystemTray.addLeftDoubleClickAction(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (!MainFrame.getInstance().isVisible()) {
              MainFrame.getInstance().show();
              MainFrame.getInstance().toFront();
              MainFrame.getInstance().setExtendedState(mState);
            } else {
              MainFrame.getInstance().setVisible(false);
            }
          }
        });
        
        mSystemTray.setTrayPopUp(trayMenu);

        MainFrame.getInstance().addComponentListener(new ComponentListener() {

            public void componentResized(ComponentEvent e) {
                int state = MainFrame.getInstance().getExtendedState();
                if ((state & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                    mState = JFrame.MAXIMIZED_BOTH;
                } else {
                    mState = JFrame.NORMAL;
                }
            }

            public void componentHidden(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentShown(ComponentEvent e) {
            }
        });

        MainFrame.getInstance() .addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent evt) {
                if (Settings.propOnlyMinimizeWhenWindowClosing.getBoolean()) {
                    // Only minimize the main window, don't quit
                    MainFrame.getInstance().setVisible(false);
                    openMenuItem.setEnabled(true);
                } else {
                    mSystemTray.setVisible(false);
                    MainFrame.getInstance().quit();
                }
            }

            public void windowIconified(java.awt.event.WindowEvent evt) {
                if (Settings.propMinimizeToTray.getBoolean()) {
                  MainFrame.getInstance().setVisible(false);
                  openMenuItem.setEnabled(true);
                }
            }
        });

        mSystemTray.setVisible(true);
    }


    /**
     * Creates the Plugin-Menus
     * @return Plugin-Menu
     */
    private static JMenu createPluginsMenu() {
      JMenu pluginsMenu = new JMenu(mLocalizer.msg("menu.plugins", "Plugins"));
      
      PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
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
     * @return is Tray used?
     */
    public boolean isTrayUsed() {
        return mUseSystemTray;
    }

}