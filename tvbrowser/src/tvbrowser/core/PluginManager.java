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
import java.util.logging.Level;

import javax.swing.JPopupMenu;

import util.exc.TvBrowserException;
import devplugin.*;



public class PluginManager {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(PluginManager.class.getName());
  
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
    Plugin[] pluginArr = PluginLoader.getInstance().getActivePlugins();
    String[] classNameArr = pluginArrToClassNameArr(pluginArr);
  
    Settings.propInstalledPlugins.setStringArray(classNameArr);
  }

  /**
   * Loads all plugins from the plugins directory.
   * @throws TvBrowserException
   */
  private void loadAllPlugins() throws TvBrowserException {  
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
    for (int i = 0; i < classNames.length; i++) {
      Plugin p = loader.getInactivePluginByClassName(classNames[i]);
      if (p != null) {
        loader.activatePlugin(p);
      }
    }
  }


  private Plugin[] classNameArrToPluginArr(String[] classNameArr) {
    ArrayList list = new ArrayList();
    for (int i = 0; i < classNameArr.length; i++) {
      Plugin plugin = PluginLoader.getInstance().getPluginByClassName(classNameArr[i]);
      if (plugin != null) {
        list.add(plugin);
      }
    }
  
    Plugin[] asArr = new Plugin[list.size()];
    list.toArray(asArr);
    return asArr;
  }


  private String[] pluginArrToClassNameArr(Plugin[] pluginArr) {
    String[] classNameArr = new String[pluginArr.length];
    for (int i = 0; i < pluginArr.length; i++) {
      classNameArr[i] = pluginArr[i].getClass().getName();
    }
  
    return classNameArr;
  }


  public void installPlugin(Plugin plugin) {
    if (plugin == null) {
      throw new NullPointerException("plugin is null");
    }

    String[] installedList = Settings.propInstalledPlugins.getStringArray();

    if (installedList == null) {
      // Install all plugin by default
      Plugin[] pluginArr = PluginLoader.getInstance().getAllPlugins();
      installedList = pluginArrToClassNameArr(pluginArr);
    }
  
    // Add the plugin
    String[] newInstalledList = new String[installedList.length + 1];
    System.arraycopy(installedList, 0, newInstalledList, 0, installedList.length);
    String className = plugin.getClass().getName();
    newInstalledList[newInstalledList.length - 1] = className;
  
    // Save the property
    Settings.propInstalledPlugins.setStringArray(newInstalledList);
  }


  public void installPendingPlugins() {
  
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
  
      Plugin plugin = PluginLoader.getInstance().getPluginByClassName(pluginName);
      if (plugin == null) {
        mLog.warning("Installing plugin failed. Plugin not found: " + pluginName);
      } else {
        installPlugin(plugin);
      }
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
   * This method should be called on start-up.
   */
  public void init() throws TvBrowserException {
    loadAllPlugins();

    String[] installedPlugins = Settings.propInstalledPlugins.getStringArray();
    if (installedPlugins == null) {
      // Install all plugins by default
      Plugin[] allPluginArr = PluginLoader.getInstance().getAllPlugins();
      installedPlugins = pluginArrToClassNameArr(allPluginArr);
    }
    activatePlugins(installedPlugins);
  
    // init the context menu  
    updateContextMenuPlugins();

    String className = Settings.propDefaultContextMenuPlugin.getString();  
    Plugin plugin = PluginLoader.getInstance().getActivePluginByClassName(className);
    setDefaultContextMenuPlugin(plugin);  
  }
  
  /**
   * Set the context menu plugins
   */
  public void setContextMenuPlugins(Plugin[] plugins) {
    mContextMenuPlugins = plugins;  
  }

  
  private void updateContextMenuPlugins() {
    ArrayList list = new ArrayList();
    String[] contextMenuPluginsStrArr
      = Settings.propContextMenuItemPlugins.getStringArray();

    // Add active plugins given by the settings
    if (contextMenuPluginsStrArr != null) {
      for (int i = 0; i < contextMenuPluginsStrArr.length; i++) {
        String className = contextMenuPluginsStrArr[i];
        Plugin p = PluginLoader.getInstance().getActivePluginByClassName(className);
        if (p != null) {
          list.add(p);
        }
      }
    }

    // Add the other (active) plugins
    Plugin[] activePlugins = PluginLoader.getInstance().getActivePlugins();
    for (int i = 0; i < activePlugins.length; i++) {
      Plugin plugin = activePlugins[i];

      String contextMenuText = null;
      try {
        contextMenuText = plugin.getContextMenuItemText();
      }
      catch (Throwable thr) {
        mLog.log(Level.WARNING, "Getting context menu text from plugin '"
            + plugin + "' failed", thr);
      }

      if (contextMenuText != null && !list.contains(plugin)) {
        list.add(plugin);
      }
    }

    // create the array
    mContextMenuPlugins = createPluginArr(list);
  }


  public Plugin[] getContextMenuPlugins() {
  
    if (mContextMenuPlugins == null) {
      return new Plugin[]{};
    }
    return mContextMenuPlugins;
  }
  
  /**
   * 
   * @return an array of plugins which implements the execute() method
   */
  public Plugin[] getExecutablePlugins() {    
    ArrayList pluginList = new ArrayList();
    Plugin[] installedPlugins=getInstalledPlugins();
    for (int i=0;i<installedPlugins.length;i++) {
      Plugin plugin=installedPlugins[i];

      String buttonText = null;
      try {
        buttonText = plugin.getButtonText();
      }
      catch (Throwable thr) {
        mLog.log(Level.WARNING, "Getting button text from plugin '"
            + plugin + "' failed", thr);
      }
      
      if (buttonText != null) {
        pluginList.add(plugin);
      }
    }
    Plugin[] result = new Plugin[pluginList.size()];
    pluginList.toArray(result);
    return result;   
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
        String contextMenuText = null;
        try {
          contextMenuText = plugin.getContextMenuItemText();
        }
        catch (Throwable thr) {
          mLog.log(Level.WARNING, "Getting context menu text from plugin '"
              + plugin + "' failed", thr);
        }
        
        if (contextMenuText != null) {
          javax.swing.JMenuItem item = new javax.swing.JMenuItem(contextMenuText);
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

