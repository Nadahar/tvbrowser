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
public class FavoritesSettingTab implements SettingsTab {

  /** The localizer for this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(FavoritesSettingTab.class);
  
  private JPanel mSettingsPn;
  private Plugin[] mSelectablePluginArr;
  private JCheckBox[] mSelectablePluginChBArr;

  
  
  /**
   * Creates a new instance of FavoritesSettingTab.
   */
  public FavoritesSettingTab() {
  }

  
  
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {    
    String msg;

    mSettingsPn = new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createTitledBorder(""));
    
    JPanel main = new JPanel(new TabLayout(1));
    mSettingsPn.add(main, BorderLayout.NORTH);
    
    // get the client plugins
    String[] clientPluginArr
      = FavoritesPlugin.getInstance().getClientPlugins();
    
    // get the installed plugins
    Plugin[] installedPluginArr = Plugin.getPluginManager().getInstalledPlugins();
    
    // create a list of those who support multiple program execution
    ArrayList selectablePluginList = new ArrayList();
    for (int i=0; i<installedPluginArr.length; i++) {
      if (installedPluginArr[i].supportMultipleProgramExecution()) {
        selectablePluginList.add(installedPluginArr[i]);
      }
    }
    
    

    if (selectablePluginList.size() == 0) {
      msg = mLocalizer.msg("noPlugins", "There are no plugins that can receive multiple programs.");
      main.add(new JLabel(msg));
    } else {
      msg = mLocalizer.msg("passTo", "Pass favorite programs to");
      main.add(new JLabel(msg));
    }
    
    // put them into an array
    mSelectablePluginArr = new Plugin[selectablePluginList.size()];
    selectablePluginList.toArray(mSelectablePluginArr);
    
    // create a check box for each
    mSelectablePluginChBArr = new JCheckBox[mSelectablePluginArr.length];
    for (int i = 0; i < mSelectablePluginArr.length; i++) {
      String name = mSelectablePluginArr[i].getInfo().getName();
      mSelectablePluginChBArr[i] = new JCheckBox(name);
      main.add(mSelectablePluginChBArr[i]);
      
      // check wether the plugin is currently a client of the FavoritesPlugin
      boolean isClient = false;
      
      for (int j = 0; j < clientPluginArr.length; j++) {
        
        if (mSelectablePluginArr[i].getClass().getName().equals(clientPluginArr[j])) {
          isClient = true;
          break;
        }
      }
      mSelectablePluginChBArr[i].setSelected(isClient);
    }
    
    return mSettingsPn;
  }

  
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    
    // Find out the plugins that should be client
    ArrayList clientPluginList = new ArrayList();
    for (int i = 0; i < mSelectablePluginChBArr.length; i++) {
      if (mSelectablePluginChBArr[i].isSelected()) {
        clientPluginList.add(mSelectablePluginArr[i].getClass().getName());
      }
    }
    
    // Put them into an array
    String[] clientPluginArr = new String[clientPluginList.size()];
    clientPluginList.toArray(clientPluginArr);
    FavoritesPlugin.getInstance().setClientPlugins(clientPluginArr);
  }

  
  
  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    String iconName = "favoritesplugin/ThumbUp16.gif";
    return ImageUtilities.createImageIconFromJar(iconName, getClass());
  }
  
  
  
  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("name", "Favorite programs");
  }
  
}
