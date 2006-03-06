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
 *
 */

package tvbrowser.core;

import java.util.ArrayList;

import javax.swing.JMenuItem;

import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.programinfo.ProgramInfo;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import util.ui.menu.MenuUtil;
import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Program;

/**
 * A class that handles the program context menu.
 * 
 * @author René Mach
 *
 */
public class ContextMenuManager {

  private static ContextMenuManager mInstance;
  
  /**
   * The context menu interface that should be executed by default when 
   * double-clicking a program in the program table. It is shown with a bold
   * font in the context menu.
   */
  private ContextMenuIf mDefaultContextMenuIf;
  
  /**
   * The context menu interface that should be executed by default when
   * middle-clicking a program in the program table.
   */
  private ContextMenuIf mDefaultMiddleClickIf;
  
  private ContextMenuManager() {
    mInstance = this;
    init();
  }
  
  private void init() {
    // Get the default context menu action
    String id = Settings.propDefaultContextMenuIf.getString();
    ContextMenuIf menuIf = getContextMenuIfForId(id);
    if (menuIf == null) {
      menuIf = getContextMenuIfForId(Settings.propDefaultContextMenuIf.getDefault());
      if (menuIf != null) {
        Settings.propDefaultContextMenuIf.setString(menuIf.getId());
      }
    }
    setDefaultContextMenuIf(menuIf);

    // Get the middle click context menu action
    id = Settings.propMiddleClickIf.getString();
    menuIf = getContextMenuIfForId(id);
    if (menuIf == null) {
      menuIf = getContextMenuIfForId(Settings.propMiddleClickIf.getDefault());
      if (menuIf != null) {
        Settings.propMiddleClickIf.setString(menuIf.getId());
      }
    }
    setMiddleClickIf(menuIf);
  }
  
  /**
   * Returns the instance of this class.
   * If the instance is null a new
   * will be created.
   * 
   * @return The instance of this class.
   */
  public static synchronized ContextMenuManager getInstance() {
    if(mInstance == null)
      new ContextMenuManager();
    return mInstance;
  }
  
  /**
   * Returns the ContextMenuIf for the id.
   * 
   * @param id The id to get the ContextMenuIf for.
   * @return The ContextMenuIf for the id or null if id wasn't found.
   */
  public ContextMenuIf getContextMenuIfForId(String id) {
    PluginProxy plugin = PluginProxyManager.getInstance().getPluginForId(id);
    if(plugin != null)
      return (ContextMenuIf)plugin;
    else if (id != null){
      if(id.compareTo(ProgramInfo.getInstance().getId()) == 0)
        return ProgramInfo.getInstance();
      else if(id.compareTo(FavoritesPlugin.getInstance().getId()) == 0)
        return FavoritesPlugin.getInstance();
      else if(id.compareTo(ReminderPlugin.getInstance().getId()) == 0)
        return ReminderPlugin.getInstance();      
    } else {
      return ProgramInfo.getInstance();
    }
    return null;
  }
  
  /**
   * Gets the default context menu interface.
   * <p>
   * This is context menu that should be executed by default when double-clicking
   * a program in the program table. It is shown with a bold font in the context
   * menu.
   *
   * @return The default context menu action or <code>null</code> if there is no
   *         default context menu interface.
   */
  public ContextMenuIf getDefaultContextMenuIf() {
    return mDefaultContextMenuIf;
  }
  
  /**
   * Gets the middle click context menu interface.
   * <p>
   * This is the context menu that should be executed by default when middle-clicking
   * a program in the program table. It is shown with an italic font in the context
   * menu.
   *
   * @return The middle click context menu interface or <code>null</code> if there is no
   *         middle click context menu interface defined.
   */
  public ContextMenuIf getMiddleClickIf() {
    return mDefaultMiddleClickIf;
  }
  
  /**
   * Sets the default context menu interface.
   *
   * @param value The ContextMenuIf to set as default context menu interface.
   */
  public void setDefaultContextMenuIf(ContextMenuIf value) {
    mDefaultContextMenuIf = value;
  }
  
  /**
   * Sets the middle click context menu interface.
   *
   * @param value The ContextMenuIf to set as middle click context menu interface.
   */
  public void setMiddleClickIf(ContextMenuIf value) {
    mDefaultMiddleClickIf = value;
  }
  
  /**
   * @return The available context menu interfaces.
   */
  public ContextMenuIf[] getAvailableContextMenuIfs() {
    PluginProxy[] pluginArr = PluginProxyManager.getInstance().getActivatedPlugins();
    String[] order = Settings.propContextMenuOrder.getStringArray();    
    
    ArrayList ifList = new ArrayList();
    
    ProgramInfo info = ProgramInfo.getInstance();
    FavoritesPlugin favorite = FavoritesPlugin.getInstance();
    ReminderPlugin reminder = ReminderPlugin.getInstance();
    
    if(order == null) {
      ifList.add(info);
      ifList.add(favorite);
      ifList.add(reminder);

      for(int i = 0; i < pluginArr.length; i++)
        ifList.add((ContextMenuIf)pluginArr[i]);
    }
    else    
    for(int i = 0; i < order.length; i++) {
      if (order[i].compareTo(info.getId()) == 0) {
        ifList.add(info);
        continue;
      }
      if (order[i].compareTo(favorite.getId()) == 0) {
        ifList.add(favorite);
        continue;
      }
      if (order[i].compareTo(reminder.getId()) == 0) {
        ifList.add(reminder);
        continue;
      }
      for(int j = 0; j < pluginArr.length; j++) {
        if(order[i].compareTo(pluginArr[j].getId()) == 0) {
          ifList.add((ContextMenuIf)pluginArr[j]);
          break;
        }
      }
    }
    
    if(pluginArr.length + 3 > ifList.size())
      for(int i = 0; i < pluginArr.length; i++) {
        if(!ifList.contains(pluginArr[i]))
          ifList.add(pluginArr[i]);
      }    
    
    ContextMenuIf[] menuIf = new ContextMenuIf[ifList.size()];
    ifList.toArray(menuIf);
    
    return menuIf;
  }


  /**
   * Creates the context menu items.
   * 
   * @param callerIf The caller Context menu interface.
   * @param program The program to show the context menu for.
   * @param markDefaultIf True if the default context menu interfaces should be highlighted.
   * @return The menu items of the context menu.
   */
  public JMenuItem[] createContextMenuItems(ContextMenuIf callerIf, Program program, boolean markDefaultIf) {
    ArrayList items = new ArrayList();
    ContextMenuIf defaultIf = getInstance().getDefaultContextMenuIf();
    ContextMenuIf middleClickIf = getInstance().getMiddleClickIf();
    ContextMenuIf[] menuIfArr = getInstance().getAvailableContextMenuIfs();

    for (int i = 0; i < menuIfArr.length; i++) {
      ContextMenuIf menuIf = menuIfArr[i];

      boolean equalsPlugin = false;

      if ((callerIf != null) && (callerIf.getId().equals(menuIf.getId()))) {
        equalsPlugin = true;
      }

      if (!equalsPlugin) {
        ActionMenu actionMenu = menuIf.getContextMenuActions(program);
        if (actionMenu != null) {
          JMenuItem menuItem = MenuUtil.createMenuItem(actionMenu);
          items.add(menuItem);
          if (menuIf == defaultIf && menuIf == middleClickIf && markDefaultIf) {
            if (!actionMenu.hasSubItems() && actionMenu.getAction() != null) {
              menuItem.setFont(MenuUtil.CONTEXT_MENU_BOLDITALICFONT);
            }
          }
          else if (menuIf == defaultIf && markDefaultIf) {
            if (!actionMenu.hasSubItems() && actionMenu.getAction() != null) {
              menuItem.setFont(MenuUtil.CONTEXT_MENU_BOLDFONT);
            }
          }
          else if (menuIf == middleClickIf && markDefaultIf) {
            if (!actionMenu.hasSubItems() && actionMenu.getAction() != null) {
              menuItem.setFont(MenuUtil.CONTEXT_MENU_ITALICFONT);
            }
          }
        }
      }
    }

    JMenuItem[] result = new JMenuItem[items.size()];
    items.toArray(result);
    return result;
  }
}
