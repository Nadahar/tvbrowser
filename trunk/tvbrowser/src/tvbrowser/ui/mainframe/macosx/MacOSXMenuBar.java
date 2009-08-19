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

import tvbrowser.ui.mainframe.MenuBar;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;

import javax.swing.*;

import com.apple.eawt.*;

public class MacOSXMenuBar extends MenuBar {

  public MacOSXMenuBar(MainFrame mainFrame, JLabel label) {
    super(mainFrame, label);

    createTVBrowserMenuItem();

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


  private void createTVBrowserMenuItem() {
    Application app = Application.getApplication();
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

    });
  }
}