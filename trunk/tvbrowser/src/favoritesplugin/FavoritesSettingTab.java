/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

package favoritesplugin;

import java.util.ArrayList;

import java.awt.*;
import javax.swing.*;

import util.ui.*;

import devplugin.*;

/**
 * The settings tab for the favorites plugin.
 *
 * @author Til Schneider, www.murfman.de
 */
public class FavoritesSettingTab extends SettingsTab {

  /** The localizer for this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(FavoritesSettingTab.class);
  
  private Plugin[] mChoosablePluginArr;
  private JCheckBox[] mChoosablePluginChBArr;

  
  
  /**
   * Creates a new instance of FavoritesSettingTab.
   */
  public FavoritesSettingTab() {
    super();
    
    setLayout(new FlowLayout(FlowLayout.LEADING));
    
    String msg;
    
    JPanel main = new JPanel(new TabLayout(1));
    add(main);
    
    // get the client plugins
    Plugin[] clientPluginArr
      = FavoritesPlugin.getInstance().getClientPlugins();
    
    // get the installed plugins
    Plugin[] installedPluginArr = Plugin.getPluginManager().getInstalledPlugins();
    
    // create a list of those who support multiple program execution
    ArrayList chooseablePluginList = new ArrayList();
    for (int i = 0; i < installedPluginArr.length; i++) {
      if (installedPluginArr[i].supportMultipleProgramExecution()) {
        chooseablePluginList.add(installedPluginArr[i]);
      }
    }

    if (chooseablePluginList.size() == 0) {
      msg = mLocalizer.msg("noPlugins", "There are no plugins that can receive multiple programs.");
      main.add(new JLabel(msg));
    } else {
      msg = mLocalizer.msg("passTo", "Pass favorite programs to");
      main.add(new JLabel(msg));
    }
    
    // put them into an array
    mChoosablePluginArr = new Plugin[chooseablePluginList.size()];
    chooseablePluginList.toArray(mChoosablePluginArr);
    
    // create a check box for each
    mChoosablePluginChBArr = new JCheckBox[mChoosablePluginArr.length];
    for (int i = 0; i < mChoosablePluginArr.length; i++) {
      String name = mChoosablePluginArr[i].getInfo().getName();
      mChoosablePluginChBArr[i] = new JCheckBox(name);
      main.add(mChoosablePluginChBArr[i]);
      
      // check wether the plugin is currently a client of the FavoritesPlugin
      boolean isClient = false;
      for (int j = 0; j < clientPluginArr.length; j++) {
        if (mChoosablePluginArr[i] == clientPluginArr[j]) {
          isClient = true;
          break;
        }
      }
      mChoosablePluginChBArr[i].setSelected(isClient);
    }
  }
  
  
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void ok() {
    // Find out the plugins that should be client
    ArrayList clientPluginList = new ArrayList();
    for (int i = 0; i < mChoosablePluginChBArr.length; i++) {
      if (mChoosablePluginChBArr[i].isSelected()) {
        clientPluginList.add(mChoosablePluginArr[i]);
      }
    }
    
    // Put them into an array
    Plugin[] clientPluginArr = new Plugin[clientPluginList.size()];
    clientPluginList.toArray(clientPluginArr);
    
    FavoritesPlugin.getInstance().setClientPlugins(clientPluginArr);
  }
  

  
  /**
   * Returns the name of the tab-sheet.
   */
  public String getName() {
    return mLocalizer.msg("name", "Favorite programs");
  }
  
}
