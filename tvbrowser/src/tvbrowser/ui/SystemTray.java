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

import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.mainframe.MainFrame;

import com.gc.systray.SystemTrayIconListener;
import com.gc.systray.SystemTrayIconManager;

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
    public static util.ui.Localizer mLocalizer;

    /** Image-Handle */
    private int mSystrayImageHandle;
    
    /** State of the Window (max/normal) */
    private static int mState;    
    
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
        mSystrayImageHandle = -1;
        File iconTrayLib = new File("DesktopIndicator.dll");

        if (iconTrayLib.exists()) {
            mUseSystemTray = SystemTrayIconManager.initializeSystemDependent();
            if (!mUseSystemTray) {
                mLog.info("could not load library " + iconTrayLib.getAbsolutePath());
            } else {
                mSystrayImageHandle = SystemTrayIconManager.loadImage("imgs/TVBrowser.ico");
                if (mSystrayImageHandle == -1) {
                    mLog.info("Could not load system tray icon");
                    mUseSystemTray = false;
                }
            }
        }
        
        return mUseSystemTray;
    }

    /**
     * Creates the Menus
     * @param mainFrame MainFrame to use for hide/show
     */
    public void createMenus(final MainFrame mainFrame) {
        mLog.info("platform independent mode is OFF");

        final SystemTrayIconManager mgr = new SystemTrayIconManager(mSystrayImageHandle, TVBrowser.MAINWINDOW_TITLE);
        mgr.setVisible(true);
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
                mainFrame.show();
                mainFrame.toFront();
                mainFrame.setExtendedState(mState);
            }
        });

        quitMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent e) {
                mgr.setVisible(false);
                mainFrame.quit();
            }
        });

        mgr.addSystemTrayIconListener(new SystemTrayIconListener() {

            public void mouseClickedLeftButton(Point pos, SystemTrayIconManager source) {
            }

            public void mouseClickedRightButton(Point pos, SystemTrayIconManager ssource) {
            }

            public void mouseLeftDoubleClicked(Point pos, SystemTrayIconManager source) {
                if (!mainFrame.isVisible()) {
                    mainFrame.show();
                }
                mainFrame.toFront();
                mainFrame.setExtendedState(mState);
            }

            public void mouseRightDoubleClicked(Point pos, SystemTrayIconManager source) {
            }
        });

        mgr.setRightClickView(trayMenu);

        mainFrame.addComponentListener(new ComponentListener() {

            public void componentResized(ComponentEvent e) {
                int state = mainFrame.getExtendedState();
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

        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent evt) {
                if (Settings.propOnlyMinimizeWhenWindowClosing.getBoolean()) {
                    // Only minimize the main window, don't quit
                    mainFrame.hide();
                    openMenuItem.setEnabled(true);
                } else {
                    mgr.setVisible(false);
                    mainFrame.quit();
                }
            }

            public void windowIconified(java.awt.event.WindowEvent evt) {
                mainFrame.hide();
                openMenuItem.setEnabled(true);
            }
        });
    }


    /**
     * Creates the Plugin-Menus
     * @return Plugin-Menu
     */
    private static JMenu createPluginsMenu() {
      JMenu pluginsMenu = new JMenu(mLocalizer.msg("menu.plugins", "Plugins"));
      
      PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
      MainFrame.updatePluginsMenu(pluginsMenu, plugins);
      
      return pluginsMenu;
    }
        
    
    /**
     * Is the Tray activated and used?
     * @return is Tray used?
     */
    public boolean isTrayUsed() {
        return mUseSystemTray;
    }

}