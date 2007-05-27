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


package tvbrowser.ui.mainframe;


import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import util.ui.Localizer;



public class DefaultMenuBar extends MenuBar {
    
  private JMenu mPluginsMenu;
    
  public DefaultMenuBar(MainFrame mainFrame, JLabel label) {
    super(mainFrame, label);
    
    int keyModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    /* create the main menus */
    JMenu tvbrowserMenu = new JMenu(mLocalizer.msg("menu.main", "TV-Browser"));
    tvbrowserMenu.setMnemonic(KeyEvent.VK_B);
    add(tvbrowserMenu);
    
    add(mViewMenu);

    add(mGoMenu);

    JMenu tvListingsMenu = new JMenu(mLocalizer.msg("menu.tvData", "TV data"));
    tvListingsMenu.setMnemonic(KeyEvent.VK_D);
    add(tvListingsMenu);
    
    mPluginsMenu = new JMenu(mLocalizer.msg("menu.plugins", "Plugins"));
    mPluginsMenu.setMnemonic(KeyEvent.VK_P);
    add(mPluginsMenu);
    
    JMenu helpMenu = new JMenu(Localizer.getLocalization(Localizer.I18N_HELP));
    helpMenu.setMnemonic(KeyEvent.VK_H);
    add(helpMenu);
    
    // "TV-Browser" menu
    tvbrowserMenu.add(mSettingsMI);
    tvbrowserMenu.addSeparator();
    tvbrowserMenu.add(mQuitMI);
    

    // "TV Listings" menu
    tvListingsMenu.add(mUpdateMI);
    tvListingsMenu.addSeparator();
    tvListingsMenu.add(mLicenseMenu);
        
    // "Plugins" menu
    mPluginsMenu.add(mFavoritesMI);
    mPluginsMenu.add(mReminderMI);
    mPluginsMenu.add(mSearchMI);
    mPluginsMenu.addSeparator();

    JMenuItem[] pluginItems = createPluginMenuItems();
    for (int i=0; i<pluginItems.length; i++) {
      mPluginsMenu.add(pluginItems[i]);  
    }
    mPluginsMenu.addSeparator();
    mPluginsMenu.add(mPluginManagerMI);
    
    // "Help" menu
    helpMenu.add(mHandbookMI);
    helpMenu.add(mKeyboardShortcutsMI);
    helpMenu.add(mFaqMI);
    helpMenu.add(mPluginHelpMenu);
    helpMenu.addSeparator();
    helpMenu.add(mForumMI);
    helpMenu.add(mWebsiteMI);
    helpMenu.add(mDonorMI);
    helpMenu.addSeparator();
    helpMenu.add(mConfigAssistantMI);
    helpMenu.addSeparator();
    helpMenu.add(mAboutMI);
    
    mSettingsMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, keyModifier));
    
    mQuitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, keyModifier));
    
    mUpdateMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));
    mPluginOverviewMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2,0));
    mPreviousDayMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_MASK));
    mNextDayMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_MASK));
    mGotoNowMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9,0));
    mFullscreenMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11,0));
    mSearchMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.CTRL_MASK));
    
    mFontSizeLargerMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_MASK));
    mFontSizeSmallerMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_MASK));
    mFontSizeDefaultMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_MASK));

    mColumnWidthLargerMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.ALT_MASK));
    mColumnWidthSmallerMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.ALT_MASK));
    mColumnWidthDefaultMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.ALT_MASK));
  }


  protected void setPluginMenuItems(JMenuItem[] items) {
    mPluginsMenu.removeAll();

    mPluginsMenu.add(mFavoritesMI);
    mPluginsMenu.add(mReminderMI);
    mPluginsMenu.add(mSearchMI);
    mPluginsMenu.addSeparator();
    
    JMenuItem[] pluginItems = createPluginMenuItems();
    for (int i=0; i<pluginItems.length; i++) {
      mPluginsMenu.add(pluginItems[i]);  
    }
    mPluginsMenu.addSeparator();
    mPluginsMenu.add(mPluginManagerMI);
  }


 
  
  
}