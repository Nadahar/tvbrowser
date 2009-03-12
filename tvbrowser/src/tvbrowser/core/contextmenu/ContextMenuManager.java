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

package tvbrowser.core.contextmenu;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.extras.common.InternalPluginProxyList;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.menu.MenuUtil;
import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Program;
import devplugin.SettingsItem;

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
   * left single clicking a program in the program table. It is shown with a dark
   * green font in the context menu.
   */
  private ContextMenuIf mDefaultLeftSingleClickMenuIf;
  
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
    // Get the left single click context menu action
    String id = Settings.propLeftSingleClickIf.getString();
    ContextMenuIf menuIf = getContextMenuIfForId(id);
    if (menuIf == null) {
      menuIf = getContextMenuIfForId(Settings.propLeftSingleClickIf.getDefault());
      if (menuIf != null) {
        Settings.propLeftSingleClickIf.setString(menuIf.getId());
      }
    }
    setLeftSingleClickIf(menuIf);
    
    // Get the default context menu action
    id = Settings.propDoubleClickIf.getString();
    menuIf = getContextMenuIfForId(id);
    if (menuIf == null) {
      menuIf = getContextMenuIfForId(Settings.propDoubleClickIf.getDefault());
      if (menuIf != null) {
        Settings.propDoubleClickIf.setString(menuIf.getId());
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
    PluginProxy plugin = PluginProxyManager.getInstance().getActivatedPluginForId(id);
    if(plugin != null) {
      return plugin;
    }    
    TvDataServiceProxy dataService = TvDataServiceProxyManager.getInstance().findDataServiceById(id);
    
    if(dataService != null) {
      return dataService;
    } else if (id != null){
      InternalPluginProxyIf internalPlugin = InternalPluginProxyList.getInstance().getProxyForId(id);
      if(internalPlugin != null && internalPlugin instanceof ContextMenuIf) {
        return (ContextMenuIf)internalPlugin;
      }
      else if(id.compareTo(ConfigMenuItem.CONFIG) == 0) 
        return ConfigMenuItem.getInstance();
      else if(id.compareTo(LeaveFullScreenMenuItem.LEAVEFULLSCREEN) == 0) 
        return LeaveFullScreenMenuItem.getInstance();
      else if(id.compareTo(DoNothingContextMenuItem.DONOTHING) == 0) {
        return DoNothingContextMenuItem.getInstance();
      }
    }
    
    return null;
  }
  
  /**
   * Gets the left single click context menu interface.
   * <p>
   * This is context menu that should be executed by default when single left clicking
   * a program in the program table. It is shown with a dark green font in the context
   * menu.
   *
   * @return The default context menu action or <code>null</code> if there is no
   *         default context menu interface.
   */
  public ContextMenuIf getLeftSingleClickIf() {
    return mDefaultLeftSingleClickMenuIf;
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
   * Sets the left single click context menu interface.
   *
   * @param value The ContextMenuIf to set as left single click context menu interface.
   */
  public void setLeftSingleClickIf(ContextMenuIf value) {
    mDefaultLeftSingleClickMenuIf = value;
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
   * Returns all available ContextMenuItems
   * 
   * @param includingDisabledItems If true the List also contains all disabled Items
   * @param cleanSeparator If true, all Separators that follow directly another Separator will be removed
   * @return The available context menu interfaces.
   */
  public ContextMenuIf[] getAvailableContextMenuIfs(boolean includingDisabledItems, boolean cleanSeparator) {
    InternalPluginProxyIf[] internalPluginProxies = InternalPluginProxyList.getInstance().getAvailableProxys();
    PluginProxy[] pluginArr = PluginProxyManager.getInstance().getActivatedPlugins();
    TvDataServiceProxy[] dataServiceArr = TvDataServiceProxyManager.getInstance().getDataServices();
    String[] order = Settings.propContextMenuOrder.getStringArray();    
    List<ContextMenuIf> disabledList = getDisabledContextMenuIfs();
    
    ArrayList<ContextMenuIf> ifList = new ArrayList<ContextMenuIf>();
        
    boolean lastWasSeparator = false;
    
    if(order == null) {
      for(InternalPluginProxyIf internalPluginProxy : internalPluginProxies) {
        if(internalPluginProxy instanceof ContextMenuIf) {
          ifList.add((ContextMenuIf)internalPluginProxy);
        }
      }

      for(int i = 0; i < pluginArr.length; i++) {
        ifList.add(pluginArr[i]);
      }
      
      for(TvDataServiceProxy dataService : dataServiceArr) {
        ifList.add(dataService);
      }
    }
    else {    
      for(int i = 0; i < order.length; i++) {
        if (order[i].compareTo(SeparatorMenuItem.SEPARATOR) == 0) {
          // Add Separator only when when one Entry exists
          if (!cleanSeparator || (cleanSeparator && (ifList.size() > 0) && !lastWasSeparator)) {
            ifList.add(new SeparatorMenuItem());
            lastWasSeparator = true;
          }
        } else if (order[i].compareTo(ConfigMenuItem.CONFIG) == 0) {
          if ((includingDisabledItems) || (!disabledList.contains(ConfigMenuItem.getInstance()))) {
            ifList.add(ConfigMenuItem.getInstance());
            lastWasSeparator = false;
          }
        } else if (order[i].compareTo(LeaveFullScreenMenuItem.LEAVEFULLSCREEN) == 0) {
          if ((includingDisabledItems) || (!disabledList.contains(LeaveFullScreenMenuItem.getInstance()))) {
            ifList.add(LeaveFullScreenMenuItem.getInstance());
            lastWasSeparator = false;
          }
        } else {
          ContextMenuIf item = getContextMenuIfForId(order[i]);
          if ((item != null) && (includingDisabledItems || !disabledList.contains(item))) {
            lastWasSeparator = false;
            ifList.add(item);
          }
        }
      }
    }
    
    for(InternalPluginProxyIf internalPluginProxy : internalPluginProxies) {
      if(internalPluginProxy instanceof ContextMenuIf) {
        if(!ifList.contains(internalPluginProxy)) {
          if ((includingDisabledItems) || ((internalPluginProxy != null) && (!disabledList.contains(internalPluginProxy)))) {
            ifList.add((ContextMenuIf)internalPluginProxy);
          }
        }
      }
    }
    
    for(int i = 0; i < pluginArr.length; i++) {
      if(!ifList.contains(pluginArr[i]))
        if ((includingDisabledItems) || ((pluginArr[i] != null) && (!disabledList.contains(pluginArr[i])))) {
          ifList.add(pluginArr[i]);
        }
    } 

    for(int i = 0; i < dataServiceArr.length; i++) {
      if(!ifList.contains(dataServiceArr[i]))
        if ((includingDisabledItems) || ((dataServiceArr[i] != null) && (!disabledList.contains(dataServiceArr[i])))) {
          ifList.add(dataServiceArr[i]);
        }
    } 

    if (!ifList.contains(LeaveFullScreenMenuItem.getInstance())) {
      if ((includingDisabledItems) || (!disabledList.contains(LeaveFullScreenMenuItem.getInstance()))) {
        ifList.add(LeaveFullScreenMenuItem.getInstance());
      }
    }
    if (!ifList.contains(ConfigMenuItem.getInstance())) {
      if ((includingDisabledItems) || (!disabledList.contains(ConfigMenuItem.getInstance()))) {
        if (!lastWasSeparator)
          ifList.add(new SeparatorMenuItem());
        ifList.add(ConfigMenuItem.getInstance());
      }
    }
    
    if (cleanSeparator) {
      // Wenn letztes Element Separator ist, diesen entfernen
      while (ifList.get(ifList.size()-1) instanceof SeparatorMenuItem) {
        ifList.remove(ifList.size()-1);
      }
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
  public JMenu createContextMenuItems(ContextMenuIf callerIf, Program program, boolean markDefaultIf) {
    ArrayList<JMenuItem> items = new ArrayList<JMenuItem>();
    ContextMenuIf leftSingleClickIf = getInstance().getLeftSingleClickIf();
    ContextMenuIf defaultIf = getInstance().getDefaultContextMenuIf();
    ContextMenuIf middleClickIf = getInstance().getMiddleClickIf();
    ContextMenuIf[] menuIfArr = getInstance().getAvailableContextMenuIfs(false, true);

    JMenu rootMenu = new JMenu();
    
    for (int i = 0; i < menuIfArr.length; i++) {
      ContextMenuIf menuIf = menuIfArr[i];

      boolean equalsPlugin = false;

      if ((callerIf != null) && (callerIf.getId().equals(menuIf.getId()))) {
        equalsPlugin = true;
      }

      if (menuIf instanceof SeparatorMenuItem) {
        if (rootMenu.getMenuComponentCount() > 0) // Only add Separator if Menu not Empty
	        rootMenu.addSeparator();
      } else if (menuIf instanceof ConfigMenuItem) {
        JMenuItem item = new JMenuItem(menuIf.toString());
        item.setIcon(IconLoader.getInstance().getIconFromTheme("categories", "preferences-system", 16));
        item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
        item.addActionListener(new ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            MainFrame.getInstance().showSettingsDialog(SettingsItem.CONTEXTMENU);
          };
        });
        rootMenu.add(item);
      } else if (menuIf instanceof LeaveFullScreenMenuItem) {
        if (MainFrame.getInstance().isFullScreenMode()) {
          JMenuItem item = new JMenuItem(menuIf.toString());
          item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
          item.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "view-fullscreen", 16));
          item.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
              if (MainFrame.getInstance().isFullScreenMode()) {
                MainFrame.getInstance().switchFullscreenMode();
              }
            };
          });
          rootMenu.add(item);
        }
      } else if (!equalsPlugin) {
        ActionMenu actionMenu = menuIf.getContextMenuActions(program);
        if (actionMenu != null) {
          JMenuItem menuItem = MenuUtil.createMenuItem(actionMenu);
          items.add(menuItem);
          if (menuIf == leftSingleClickIf && markDefaultIf && !menuIf.equals(DoNothingContextMenuItem.getInstance())) {
            menuItem.setForeground(new Color(0,90,0));
          }
          if (menuIf == defaultIf && menuIf == middleClickIf && markDefaultIf && !menuIf.equals(DoNothingContextMenuItem.getInstance())) {
            if (!actionMenu.hasSubItems() && actionMenu.getAction() != null) {
              menuItem.setFont(MenuUtil.CONTEXT_MENU_BOLDITALICFONT);
            }
          }
          else if (menuIf == defaultIf && markDefaultIf && !menuIf.equals(DoNothingContextMenuItem.getInstance())) {
            if (!actionMenu.hasSubItems() && actionMenu.getAction() != null) {
              menuItem.setFont(MenuUtil.CONTEXT_MENU_BOLDFONT);
            }
          }
          else if (menuIf == middleClickIf && markDefaultIf && !menuIf.equals(DoNothingContextMenuItem.getInstance())) {
            if (!actionMenu.hasSubItems() && actionMenu.getAction() != null) {
              menuItem.setFont(MenuUtil.CONTEXT_MENU_ITALICFONT);
            }
          }
          rootMenu.add(menuItem);
        }
      }
    }

    // Remove last Item if it's a Separator
    while (rootMenu.getMenuComponent(rootMenu.getMenuComponentCount()-1) instanceof JPopupMenu.Separator) {
      rootMenu.remove(rootMenu.getMenuComponentCount()-1);
    }
    
    // remove duplicate separators
    for (int i = rootMenu.getMenuComponentCount() - 2; i > 0; i--) {
      if (rootMenu.getMenuComponent(i) instanceof JPopupMenu.Separator
          && rootMenu.getMenuComponent(i + 1) instanceof JPopupMenu.Separator) {
        rootMenu.remove(i + 1);
      }
    }
    
    return rootMenu;
  }

  /**
   * Returns a List with all disabled ContextMenuIfs
   * @return disabled ContextMenuIfs
   */
  public List<ContextMenuIf> getDisabledContextMenuIfs() {
    String[] disabled = Settings.propContextMenuDisabledItems.getStringArray();
    
    ArrayList<ContextMenuIf> list = new ArrayList<ContextMenuIf>();
    if (disabled == null) {
      return list;
    }
    
    for (int i=0;i<disabled.length;i++) {
      ContextMenuIf item = getContextMenuIfForId(disabled[i]);
      if (item != null) {
        list.add(item);
      }
    }
    
    return list;
  }
}
