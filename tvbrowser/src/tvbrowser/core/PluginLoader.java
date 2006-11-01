/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import tvbrowser.core.plugin.AbstractPluginProxy;
import tvbrowser.core.plugin.BeanShellPluginProxy;
import tvbrowser.core.plugin.JavaPluginProxy;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.DefaultTvDataServiceProxy;
import tvbrowser.core.tvdataservice.DeprecatedTvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvdataservice.TvDataService;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import devplugin.Plugin;

/**
 * The PluginLoader loads all plugins and assigns each plugin to
 * the appropriate manager (TvDataServiceProxyManager or PluginProxyManager)
 */
public class PluginLoader {

  /** The logger for this class */
  private static java.util.logging.Logger mLog
      = Logger.getLogger(PluginLoader.class.getName());

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(PluginLoader.class);

  private static PluginLoader mInstance;

  /** The name of the directory where the plugins are located in TV-Browser 2.1 and later */
  private static String PLUGIN_DIRECTORY = "plugins";

  private HashSet mSuccessfullyLoadedPluginFiles;

  private HashMap mDeleteablePlugin;
  
  private PluginLoader() {
    mSuccessfullyLoadedPluginFiles = new HashSet();
    mDeleteablePlugin = new HashMap();
  }

  public static PluginLoader getInstance() {
    if (mInstance == null) {
      mInstance = new PluginLoader();
    }
    return mInstance;
  }

  /**
   * Installs all plugins that could not be installed the last time, because an
   * old version was in use.
   */
  public void installPendingPlugins() {
    File[] fileArr = new File(Settings.propPluginsDirectory.getString()).listFiles();
    if (fileArr == null) {
      // Nothing to do
      return;
    }

    // Install all pending plugins
    for (int i = 0; i < fileArr.length; i++) {
      if (fileArr[i].getName().endsWith(".inst")) {
        // This plugin wants to be installed
        String fileName = fileArr[i].getAbsolutePath();
        String oldFileName = fileName.substring(0, fileName.length() - 5);
        File oldFile = new File(oldFileName);

        // Delete the old file
        oldFile.delete();

        // Rename the file, so the PluginLoader will install it later
        if (!fileArr[i].renameTo(oldFile)) {
          mLog.warning("Installing pending plugin failed: " + fileName);
        }
      }
    }
  }


  /**
   * Loads the plugin from the file system
   * @param pluginFile File to load
   * @param deleteable is the Plugin deleteable
   */
  private void loadPlugin(File pluginFile, boolean deleteable) {
    Object plugin = null;
    String lcFileName = pluginFile.getName().toLowerCase();
    if (mSuccessfullyLoadedPluginFiles.contains(lcFileName)) {
      mLog.warning("cannot load plugin "+pluginFile.getAbsolutePath()+" - already loaded");
      return;
    }
    mSuccessfullyLoadedPluginFiles.add(lcFileName);
      try {
        if (lcFileName.endsWith(".jar")) {
          plugin = loadJavaPlugin(pluginFile);
        }
        else if (lcFileName.endsWith(".bsh")) {
          plugin = loadBeanShellPlugin(pluginFile);
        }
        else {
          mLog.warning("Unknown plugin type: " + pluginFile.getAbsolutePath());
        }


        if (plugin instanceof Plugin) {
          ((Plugin)plugin).setJarFile(pluginFile);
          
          JavaPluginProxy javaplugin = new JavaPluginProxy((Plugin)plugin);
          PluginProxyManager.getInstance().registerPlugin(javaplugin);

          if (deleteable)
            mDeleteablePlugin.put(javaplugin, pluginFile);
        }
        else if (plugin instanceof AbstractPluginProxy) {
          PluginProxyManager.getInstance().registerPlugin((AbstractPluginProxy)plugin);
          if (deleteable)
            mDeleteablePlugin.put(plugin, pluginFile);
        }
        else if (plugin instanceof devplugin.TvDataService) {
          TvDataServiceProxy proxy = new DefaultTvDataServiceProxy((devplugin.TvDataService)plugin);
          TvDataServiceProxyManager.getInstance().registerTvDataService(proxy);
        }
        else if (plugin instanceof TvDataService) {
          TvDataServiceProxy proxy = new DeprecatedTvDataServiceProxy((TvDataService)plugin);
          TvDataServiceProxyManager.getInstance().registerTvDataService(proxy);
        }


        mLog.info("Loaded plugin "+pluginFile.getAbsolutePath());

      }catch (Throwable thr) {
        mLog.log(Level.WARNING, "Loading plugin file failed: "
            + pluginFile.getAbsolutePath(), thr);
        thr.printStackTrace();
      }

  }


  /**
   * Loads all plugins within the specified folder
   * @param folder specific Folder
   * @param deleteable True if the Plugins in this Folder are deleteable
   */
  private void loadPlugins(File folder, boolean deleteable) {

    File[] fileArr = folder.listFiles(new FilenameFilter(){
      public boolean accept(File dir, String name) {
        return !("FavoritesPlugin.jar".equals(name)
                || "ReminderPlugin.jar".equals(name)
                || "ProgramInfo.jar".equals(name)
                || "SearchPlugin.jar".equals(name)); 
      }
    });
    if (fileArr == null) {
      // Nothing to do
      return;
    }

    for (int i = 0; i < fileArr.length; i++) {
      // Try to load this plugin
      loadPlugin(fileArr[i], deleteable);
    }
  }

  public void loadAllPlugins() {

    /* 0) delete all plugins the user doesn't want anymore */
    
    String[] files = Settings.propDeleteFilesAtStart.getStringArray();
    
    if ((files != null) && (files.length > 0)) {
      for (int i=0;i<files.length;i++) {
        try {
          new File(files[i]).delete();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      
      Settings.propDeleteFilesAtStart.setStringArray(new String[0]);
    }
    
    
    /* 1) load all plugins from the plugins folder in the user's home directory */
    String pluginsFolderName = Settings.propPluginsDirectory.getString();
    File f = new File(pluginsFolderName);
    boolean success = true;
    if (!f.exists()) {
      if (!f.mkdirs()) {
        mLog.warning("Could not create plugins folder "+f.getAbsolutePath());
        success = false;
      }
    }
    if (success) {
      loadPlugins(f, true);
    }

    /* 2) load the plugins from the plugins folder */
    loadPlugins(new File(PluginProxyManager.PLUGIN_DIRECTORY), false);

    /* 3) load the plugins from the tvdataservice folder */
    loadPlugins(new File(TvDataServiceProxyManager.PLUGIN_DIRECTORY), false);

  }




  private Object loadJavaPlugin(File jarFile) throws TvBrowserException {
    Object plugin;

    // Create a class loader for the plugin
    ClassLoader classLoader;
    try {
      URL[] urls = new URL[] { jarFile.toURL() };
      classLoader = URLClassLoader.newInstance(urls, ClassLoader.getSystemClassLoader());
    } catch (MalformedURLException exc) {
      throw new TvBrowserException(getClass(), "error.1",
        "Loading Jar file failed of a plugin failed: {0}.",
        jarFile.getAbsolutePath(), exc);
    }

    // Get the plugin name
    String pluginName = jarFile.getName();
    if (pluginName.endsWith(".jar")) {
      pluginName = pluginName.substring(0, pluginName.length() - 4);
    }

    // Create a plugin instance
    try {
      Class pluginClass = classLoader.loadClass(pluginName.toLowerCase() + "." + pluginName);
      plugin = pluginClass.newInstance();
    }
    catch (Throwable thr) {
      throw new TvBrowserException(getClass(), "error.2",
         "Could not load plugin {0}.", jarFile.getAbsolutePath(), thr);
    }

    return plugin;
  }

  private Object loadBeanShellPlugin(File file) {
    return new BeanShellPluginProxy(file);
  }

  /**
   * Delete a Plugin
   * @param proxy Proxy for the Plugin that should be deleted
   * @return true if successful
   */
  public boolean deletePlugin(PluginProxy proxy) {
    File file = (File)mDeleteablePlugin.get(proxy);
    
    Settings.propDeleteFilesAtStart.addItem(file.toString());
    
    try {
        PluginProxyManager.getInstance().removePlugin(proxy);
    } catch (TvBrowserException exc) {
        ErrorHandler.handle(exc);
        return false;
    }

    mDeleteablePlugin.remove(proxy);
    return true;
  }

  /**
   * Is a Plugin deleteable ?
   * @param plugin Plugin that should be deleted
   * @return true if deleteable
   */
  public boolean isPluginDeletable(PluginProxy plugin) {
    return mDeleteablePlugin.containsKey(plugin);
  }


}
