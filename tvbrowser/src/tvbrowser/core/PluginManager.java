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
package tvbrowser.core;

import java.awt.Font;
import java.io.*;
import java.util.*;

import javax.swing.JPopupMenu;

import util.exc.TvBrowserException;
import devplugin.*;



public class PluginManager {
  
  private static PluginManager mInstance;
  private Plugin mDefaultContextMenuPlugin;
  private Plugin[] mContextMenuPlugins;
  private static String PLUGIN_DIRECTORY="plugins";
  
  private static Font CONTEXT_MENU_PLAINFONT = new Font("Dialog", Font.PLAIN, 12);
  private static Font CONTEXT_MENU_BOLDFONT = new Font("Dialog", Font.BOLD, 12);

  
  private PluginManager() {
    PluginLoader.getInstance().addPluginStateListener(
      new PluginStateListener(){

        public void pluginActivated(Plugin p) {
          updateContextMenuPlugins();   
        }
  
        public void pluginDeactivated(Plugin p) {
          updateContextMenuPlugins();
        }
  
        public void pluginLoaded(Plugin p) {    
        }

				public void pluginUnloaded(Plugin p) {
					
				}
      }
    );
  }
  
  /**
   * Store a list of all installed plugins into the settings file
   *
   */
  
  public void storeSettings() {
    Plugin[] p = PluginLoader.getInstance().getActivePlugins();
    String[] classNames = new String[p.length];
    
    for (int i=0;i<p.length;i++) {      
      classNames[i] = p[i].getClass().getName();
    }
    Settings.setInstalledPlugins(classNames);
    
  }
  
  /**
   * Loads all plugins from the plugins directory.
   * @throws TvBrowserException
   */
  public  void loadAllPlugins() throws TvBrowserException {    
      File file=new File(PLUGIN_DIRECTORY);
      if (!file.exists()) {
        return;
      }

      File[] fileList=file.listFiles(new FileFilter() {
        public boolean accept(File f) {
          return f.getName().endsWith(".jar");
        }
      });
    
      for (int i=0;i<fileList.length;i++) {
        PluginLoader.getInstance().loadPlugin(fileList[i]);
      }
    
    }

  /**
   * Activates an array of plugins
   * 
   * @param classNames An array of classnames
   */
 public void activatePlugins(String[] classNames) {
   PluginLoader loader = PluginLoader.getInstance();
   for (int i=0;i<classNames.length;i++) {
     Plugin p=loader.getInactivePluginByClassName(classNames[i]);
     if (p!=null) {
       loader.activatePlugin(p);
     } 
   }
    
 }
  
 
  public static void installPendingPlugins() {
    
    File file=new File(PLUGIN_DIRECTORY);
    if (!file.exists()) {
      return;
    }
    File[] fileList=file.listFiles(new FileFilter() {
      public boolean accept(File f) {
        return f.getName().endsWith(".jar.inst");
      }
    });

    for (int i=0;i<fileList.length;i++) {
      String fName=fileList[i].getAbsolutePath();
      File fNewFile=new File(fName.substring(0,fName.length()-5));
      if (fNewFile.exists()) {
        fNewFile.delete();
      }
      fileList[i].renameTo(fNewFile);
      String pluginName=fNewFile.getName().substring(0,fNewFile.getName().length()-4);
      pluginName=pluginName.toLowerCase()+"."+pluginName;
    
      Settings.addInstalledPlugin(pluginName);
    }
    
  }
  
  public static Plugin[] createPluginArr(Collection col) {
    Plugin[] result = new Plugin[col.size()];
    col.toArray(result);
    return result;  
  }
  
  /**
   * returns an array of all currently activated plugins
   */
  public Plugin[] getInstalledPlugins() {
    return PluginLoader.getInstance().getActivePlugins();
  }
  
  /**
   * The default context menu plugin can be executed by double-click into the
   * program table.
   * 
   */
  public void setDefaultContextMenuPlugin(Plugin plugin) {
    mDefaultContextMenuPlugin = plugin;
  }
  
  
  public Plugin getDefaultContextMenuPlugin() {
    return mDefaultContextMenuPlugin;
  }
  
  
  /**
   * Create the context menu by reading the settings.
   * This method should be called on start-up.
   */
  
  public void initContextMenu() {    
    
    updateContextMenuPlugins();
    
    Plugin p=PluginLoader.getInstance().getActivePluginByClassName(Settings.getDefaultContextMenuPlugin());
    setDefaultContextMenuPlugin(p);    
  }
    
  /**
   * Set the context menu plugins 
   */ 
  public void setContextMenuPlugins(Plugin[] plugins) {
    mContextMenuPlugins = plugins;    
  }
  
  private void updateContextMenuPlugins() {
    
    ArrayList list = new ArrayList();
    String[] contextMenuPluginsStrArr = Settings.getContextMenuItemPlugins();
    
    
    /* Add active plugins given by the settings */
    for (int i=0;i<contextMenuPluginsStrArr.length;i++) {
      Plugin p = PluginLoader.getInstance().getActivePluginByClassName(contextMenuPluginsStrArr[i]);
      if (p!=null) {
        list.add(p);
      }
    }   
      
    /* Add the other (active) plugins */
    Plugin[] activePlugins = PluginLoader.getInstance().getActivePlugins();
    for (int i=0;i<activePlugins.length;i++) {
      Plugin p = activePlugins[i];
      if (p.getContextMenuItemText()!= null && !list.contains(p)) {
        list.add(p);
      }
    }      
      
    /* create the array*/
    mContextMenuPlugins=createPluginArr(list);
    
  }
  
  public Plugin[] getContextMenuPlugins() {
    
    if (mContextMenuPlugins == null) {
      return new Plugin[]{};
    }
    return mContextMenuPlugins;
  }
  
  /**
   * Creates a context menu containing all subscribed plugins that support context
   * menues.
   *
   * @return a plugin context menu.
   */
  public static JPopupMenu createPluginContextMenu(final Program program, Plugin caller) {
    JPopupMenu menu = new JPopupMenu();
    Plugin defaultPlugin = getInstance().getDefaultContextMenuPlugin();
    Plugin[] pluginArr = getInstance().getContextMenuPlugins();
    for (int i = 0; i < pluginArr.length; i++) {
      final devplugin.Plugin plugin = pluginArr[i];
      if (!plugin.equals(caller)) {
        String text = plugin.getContextMenuItemText();
        if (text != null) {
          javax.swing.JMenuItem item = new javax.swing.JMenuItem(text);
          if (plugin == defaultPlugin) {
            item.setFont(CONTEXT_MENU_BOLDFONT);
          } else {
            item.setFont(CONTEXT_MENU_PLAINFONT);
          }
          item.setIcon(plugin.getMarkIcon());
          item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
              plugin.execute(program);
            }
          });
          menu.add(item);
        }
      }
    }
    return menu;
  }
  
  
  public static PluginManager getInstance() {
    if (mInstance==null) {
      mInstance=new PluginManager();
    }
    return mInstance;
  }
}

