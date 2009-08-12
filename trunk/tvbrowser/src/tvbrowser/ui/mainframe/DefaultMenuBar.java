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



public class DefaultMenuBar extends MenuBar {
    
  private JMenu mPluginsMenu;
    
  public DefaultMenuBar(MainFrame mainFrame, JLabel label) {
    super(mainFrame, label);
    
    /* create the main menus */
    JMenu fileMenu = createMenu("menu.main", "&File");
    add(fileMenu);
    
    add(mViewMenu);

    add(mGoMenu);

    JMenu tvListingsMenu = createMenu("menu.tvData", "TV &data");
    add(tvListingsMenu);
    
    mPluginsMenu = createMenu("menu.plugins", "&Tools");
    add(mPluginsMenu);
    
    JMenu helpMenu = createMenu("menu.help", "&Help");
    add(helpMenu);
    
    // "TV-Browser" menu
    fileMenu.add(mQuitMI);

    // "TV Listings" menu
    tvListingsMenu.add(mUpdateMI);
    tvListingsMenu.addSeparator();
    tvListingsMenu.add(mLicenseMenu);
        
    // "Plugins" menu    
    JMenuItem[] internalPluginItems = createInternalPluginMenuItems();
    for (JMenuItem menuItem : internalPluginItems) {
      mPluginsMenu.add(menuItem);
    }
    
    mPluginsMenu.addSeparator();

    JMenuItem[] pluginItems = createPluginMenuItems();
    for (JMenuItem menuItem : pluginItems) {
      mPluginsMenu.add(menuItem);
    }
    mPluginsMenu.addSeparator();
    mPluginsMenu.add(mInstallPluginsMI);
    mPluginsMenu.add(mPluginManagerMI);
    mPluginsMenu.addSeparator();
    mPluginsMenu.add(mSettingsMI);
    
    // Help menu
    createHelpMenuItems(helpMenu, true);
    
    mQuitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
    mSettingsMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    
    mPluginOverviewMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2,0));
    mTimeBtnsMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0));
    mDatelistMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,0));
    mChannellistMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6,0));

    mUpdateMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));

    mPreviousDayMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_MASK));
    mNextDayMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_MASK));
    mGotoNowMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9,0));
    mFullscreenMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11,0));
    
    mFontSizeLargerMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    mFontSizeSmallerMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    mFontSizeDefaultMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    mColumnWidthLargerMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.ALT_MASK));
    mColumnWidthSmallerMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.ALT_MASK));
    mColumnWidthDefaultMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.ALT_MASK));
  }


  protected void setPluginMenuItems(JMenuItem[] items) {
    mPluginsMenu.removeAll();
    
    JMenuItem[] internalPluginItems = createInternalPluginMenuItems();
    for (JMenuItem menuItem : internalPluginItems) {
      mPluginsMenu.add(menuItem);
    }
    
    mPluginsMenu.addSeparator();
    
    JMenuItem[] pluginItems = createPluginMenuItems();
    for (JMenuItem menuItem : pluginItems) {
      mPluginsMenu.add(menuItem);  
    }
    mPluginsMenu.addSeparator();
    mPluginsMenu.add(mInstallPluginsMI);
    mPluginsMenu.add(mPluginManagerMI);
    mPluginsMenu.addSeparator();
    mPluginsMenu.add(mSettingsMI);
  }


 
  
  
}