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
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;



public class DefaultMenuBar extends MenuBar {
    
  private JMenu mPluginsMenu;
    
  public DefaultMenuBar(MainFrame mainFrame, JLabel label) {
    super(mainFrame, label);
    
    int keyModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    /* create the main menus */
    JMenu tvbrowserMenu = new JMenu(mLocalizer.msg("menu.main", "TV-Browser"));
    tvbrowserMenu.setMnemonic(KeyEvent.VK_B);
    add(tvbrowserMenu);
    
    JMenu viewMenu = new JMenu(mLocalizer.msg("menuitem.view","View"));
    add(viewMenu);
    
    JMenu tvListingsMenu = new JMenu(mLocalizer.msg("menu.tvData", "TV data"));
    tvListingsMenu.setMnemonic(KeyEvent.VK_D);
    add(tvListingsMenu);
    
    mPluginsMenu = new JMenu(mLocalizer.msg("menu.plugins", "Plugins"));
    mPluginsMenu.setMnemonic(KeyEvent.VK_P);
    add(mPluginsMenu);
    
    JMenu helpMenu = new JMenu(mLocalizer.msg("menu.help", "Help"));
    helpMenu.setMnemonic(KeyEvent.VK_H);
    add(helpMenu);
    
    // "TV-Browser" menu
    tvbrowserMenu.add(mSettingsMI);
    tvbrowserMenu.addSeparator();
    tvbrowserMenu.add(mQuitMI);
    
    // "View" menu
    viewMenu.add(mToolbarMI);
    viewMenu.add(mTimeBtnsMI);
    viewMenu.add(mChannellistMI);
    viewMenu.addSeparator();
    viewMenu.add(mFiltersMenu);
    viewMenu.add(mPluginsViewMenu);
    viewMenu.addSeparator();
    viewMenu.add(mRestoreMI);
   
    // "TV Listings" menu
    tvListingsMenu.add(mUpdateMI);
    tvListingsMenu.addSeparator();
    tvListingsMenu.add(mLicenseMenu);
        
    // "Plugins" menu
    JMenuItem[] pluginItems = createPluginMenuItems();
    for (int i=0; i<pluginItems.length; i++) {
      mPluginsMenu.add(pluginItems[i]);  
    }
    mPluginsMenu.addSeparator();
    mPluginsMenu.add(mFindPluginsMI);
    
    // "Help" menu
    helpMenu.add(mHelpMI);
    helpMenu.addSeparator();
    helpMenu.add(mDonorMI);
    helpMenu.add(mFaqMI);
    helpMenu.add(mForumMI);
    helpMenu.add(mWebsiteMI);
    helpMenu.addSeparator();
    helpMenu.add(mConfigAssistantMI);
    helpMenu.addSeparator();
    helpMenu.add(mAboutMI);
    
    mSettingsMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, keyModifier));
    
    mQuitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, keyModifier));
    
    mUpdateMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));    
    mHelpMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
        
  }


  protected void setPluginMenuItems(JMenuItem[] items) {
    mPluginsMenu.removeAll();
    JMenuItem[] pluginItems = createPluginMenuItems();
    for (int i=0; i<pluginItems.length; i++) {
      mPluginsMenu.add(pluginItems[i]);  
    }
    mPluginsMenu.addSeparator();
    mPluginsMenu.add(mFindPluginsMI);
  }


 
  
  
}