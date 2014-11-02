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

package tvbrowser.ui.mainframe.macosx;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.KeyStroke;

import tvbrowser.TVBrowser;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.MenuBar;

//import com.apple.eawt.*;

public class MacOSXMenuBar extends MenuBar {
  private final static Logger LOGGER = Logger.getLogger(MacOSXMenuBar.class.getName());

  public MacOSXMenuBar(MainFrame mainFrame, JLabel label) {
    super(mainFrame, label);

    Thread toAddMenus = new Thread() {
      public void run() {
        if(!createTVBrowserMenuItem()) {
          JMenu fileMenu = createMenu("menu.main", "&File", true);
          add(fileMenu);
          
          if (TVBrowser.restartEnabled()) {
            fileMenu.add(mRestartMI);
          }
          
          fileMenu.addSeparator();
          fileMenu.add(mQuitMI);
        }
    
        createCommonMenus();
    
        int commandModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
        // shortcuts as defined in the Apple guidelines
        // command Q
        mQuitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, commandModifier));
    
        // command ,
        mSettingsMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, commandModifier));
    
        // alt command T
        mToolbarMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, commandModifier | KeyEvent.ALT_DOWN_MASK));
      }
    };
    
    addAdditionalMenus(toAddMenus);
  }

  private boolean createTVBrowserMenuItem() {
    boolean menusCreated = false;
    
    try {
      Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
      Class<?> applicationListenerClass = Class.forName("com.apple.eawt.ApplicationListener");
      
      Method getApplication = applicationClass.getMethod("getApplication", new Class<?>[0]);
      Method addAboutMenuItem = applicationClass.getMethod("addAboutMenuItem", new Class<?>[0]);
      Method addPreferencesMenuItem = applicationClass.getMethod("addPreferencesMenuItem", new Class<?>[0]);
      
      Method setEnabledAboutMenu = applicationClass.getMethod("setEnabledAboutMenu", new Class<?>[]{boolean.class});
      Method setEnabledPreferencesMenu = applicationClass.getMethod("setEnabledPreferencesMenu", new Class<?>[]{boolean.class});
      
      Method addApplicationListener = applicationClass.getMethod("addApplicationListener", new Class<?>[]{applicationListenerClass});
      
      Object app = getApplication.invoke(applicationClass, new Object[0]);
      
      addAboutMenuItem.invoke(app, new Object[0]);
      addPreferencesMenuItem.invoke(app, new Object[0]);
      
      setEnabledAboutMenu.invoke(app, new Object[]{true});
      setEnabledPreferencesMenu.invoke(app, new Object[]{true});
      
      Object applicationListener = Proxy.newProxyInstance(app.getClass().getClassLoader(), new Class<?>[] {applicationListenerClass}, new ApplicationListenerHandler(this));
      
      addApplicationListener.invoke(app, new Object[] {applicationListener});
      
      menusCreated = true;
    } catch (Exception e) {
      LOGGER.log(Level.INFO, "OS X specific classes not found.", e);
    }
    
   /* Application app = Application.getApplication();
    app.addAboutMenuItem();
    app.addPreferencesMenuItem();

    app.setEnabledAboutMenu(true);
    app.setEnabledPreferencesMenu(true);

    app.addApplicationListener(new ApplicationListener() {
      public void handleAbout(ApplicationEvent event) {
        getMainFrame().showAboutBox();
        event.setHandled(true);
      }

      public void handleOpenApplication(ApplicationEvent event) {
      }

      public void handleOpenFile(ApplicationEvent event) {
      }

      public void handlePreferences(ApplicationEvent event) {
        getMainFrame().showSettingsDialog();
        event.setHandled(true);
      }

      public void handlePrintFile(ApplicationEvent event) {
      }

      public void handleQuit(ApplicationEvent event) {
        getMainFrame().quit();
      }

      public void handleReOpenApplication(ApplicationEvent event) {
      }

    });*/
    
    return menusCreated;
  }
  
  private static final class ApplicationListenerHandler implements InvocationHandler {
    private MacOSXMenuBar mMenuBar;
    
    public ApplicationListenerHandler(MacOSXMenuBar menuBar) {
      mMenuBar = menuBar;
    }
    
    private void handleAbout(Object event) {
      mMenuBar.getMainFrame().showAboutBox();
      setEventHandled(event);
    }
    
    private void handlePreferences(Object event) {
      mMenuBar.getMainFrame().showSettingsDialog();
      setEventHandled(event);
    }
    
    private void handleQuit(Object event) {
      mMenuBar.getMainFrame().quit();
    }
    
    private void setEventHandled(Object event) {
      try {
        Method setHandled = event.getClass().getMethod("setHandled", new Class<?>[] {boolean.class});
        setHandled.invoke(event, new Object[]{true});
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Method setHandled(boolean) for ApplicationEvent could not be called.", e);
      }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if(method != null && args != null && args.length == 1) {
        if(method.getName().equals("handleAbout")) {
          handleAbout(args[0]);
        }
        else if(method.getName().equals("handlePreferences")) {
          handlePreferences(args[0]);
        }
        else if(method.getName().equals("handleQuit")) {
          handleQuit(args[0]);
        }
      }
      
      return null;
    }
  }
}