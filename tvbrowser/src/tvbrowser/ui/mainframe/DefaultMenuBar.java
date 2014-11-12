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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.mainframe;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import tvbrowser.TVBrowser;
import util.misc.OperatingSystem;

/**
 * menu bar for non Mac systems
 *
 */
public class DefaultMenuBar extends MenuBar {
  private boolean mMenusAdded = false;

  public DefaultMenuBar(final MainFrame mainFrame, final JLabel label) {
    super(mainFrame, label);

    Thread toAddMenus = new Thread() {
      public void run() {
        JMenu fileMenu = createMenu("menu.main", "&File", true);
        add(fileMenu);
        
        if (TVBrowser.restartEnabled()) {
          fileMenu.add(mRestartMI);
        }
        
        fileMenu.addSeparator();
        fileMenu.add(mQuitMI);

        createCommonMenus(true);
        
        if(mEditMenu != null) {
          mEditMenu.add(mSettingsMI);
        }
        
        mQuitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
        mRestartMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        mSettingsMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit()
            .getMenuShortcutKeyMask()));
        
        mMenusAdded = true;
      };
    };
    
    addAdditionalMenus(toAddMenus);
  }
  
  protected void setPluginMenuItems(JMenuItem[] items) {
    super.setPluginMenuItems(items);
    // on Windows systems, the settings are in the tools menu
    if(mEditMenu == null) {
      mPluginsMenu.addSeparator();
      mPluginsMenu.add(mSettingsMI);
    }
  }
}