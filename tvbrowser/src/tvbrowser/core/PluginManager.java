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

import devplugin.Plugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import util.exc.*;

/**
 * The PluginManager is a Class for communicating with installed mAvailablePluginHash.
 *
 * @author Martin Oberhauser
 */
public class PluginManager {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(PluginManager.class.getName());
  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(PluginManager.class);
  
  private static HashMap mAvailablePluginHash;
  private static ArrayList mInstalledPluginList;

  
  
  /**
   * Loads the data of the specified Plugin from the disk.
   */
  private static void loadPluginData(Plugin plugin) {
    String dir = Settings.getUserDirectoryName();
    File f = new File(dir, plugin.getClass().getName() + ".dat");
    if (f.exists()) {
      ObjectInputStream in = null;
      try {
        in = new ObjectInputStream(new FileInputStream(f));
        plugin.readData(in);
      }
      catch (Exception exc) {
        String msg = mLocalizer.msg("error.1", "Loading data for plugin {0} failed!\n({1})",
          plugin.getInfo().getName(), f.getAbsolutePath(), exc);
        ErrorHandler.handle(msg, exc);
      }
      finally {
        if (in != null) {
          try { in.close(); } catch (IOException exc) {}
        }
      }
    }
  }

  
  
  /**
   * Saves the data of the specified Plugin to disk.
   */
  private static void storePluginData(Plugin plugin) {
    // ensure the settings directory is present
    String dir = Settings.getUserDirectoryName();
    File f = new File(dir);
    if (! f.exists()) {
      f.mkdir();
    }
    
    // save the plugin data
    f = new File(dir, plugin.getClass().getName() + ".dat");
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(new FileOutputStream(f));
      plugin.writeData(out);
    }
    catch(IOException exc) {
      String msg = mLocalizer.msg("error.2", "Saving data for plugin {0} failed!\n({1})",
        plugin.getInfo().getName(), f.getAbsolutePath(), exc);
      ErrorHandler.handle(msg, exc);
    }
    finally {
      if (out != null) {
        try { out.close(); } catch (IOException exc) {}
      }
    }
  }

  
  
  private static void loadPluginSettings(Plugin plugin) {
    Class c=plugin.getClass();
    String dir=Settings.getUserDirectoryName();
    File f=new File(dir,c.getName()+".prop");
    if (f.exists()) {
      try {
        Properties p=new Properties();
        FileInputStream in=new FileInputStream(f);
        p.load(in);
        in.close();
        plugin.loadSettings(p);
      } catch (IOException exc) {
        String msg = mLocalizer.msg("error.3", "Loading settings for plugin {0} failed!\n({1})",
          plugin.getButtonText(), f.getAbsolutePath(), exc);
        ErrorHandler.handle(msg, exc);
      }
    }else{
      plugin.loadSettings(new Properties());
    }
  }

  
  
  private static void storePluginSettings(Plugin plugin) {
    Properties prop=plugin.storeSettings();
    if (prop!=null) {
      String dir=Settings.getUserDirectoryName();
      File f=new File(dir);
      if (!f.exists()) {
        f.mkdir();
      }
      f=new File(dir,plugin.getClass().getName()+".prop");
      try {
        FileOutputStream out=new FileOutputStream(f);
        prop.store(out,"settings");
        out.close();
      } catch (IOException exc) {
        String msg = mLocalizer.msg("error.4", "Saving settings for plugin {0} failed!\n({1})",
          plugin.getButtonText(), f.getAbsolutePath(), exc);
        ErrorHandler.handle(msg, exc);
      }
    }
  }

  
  
  private static void initPlugin(Plugin p) {
    loadPluginData(p);
    loadPluginSettings(p);
  }
  
  
  
  private static void finalizePlugin(Plugin p) {
    storePluginData(p);
    storePluginSettings(p);
  }
  
  
  
  /**
   * Kind of constructor
   */
  public static void initInstalledPlugins() {
    Plugin[] p=getInstalledPlugins();
    for (int i=0;i<p.length;i++) {
      initPlugin((Plugin)p[i]);
    }
  }



  /**
   * Kind of destructor: call plugins to store their data and settings
   */
  public static void finalizeInstalledPlugins() {
    Plugin[] p=getInstalledPlugins();
    for (int i=0;i<p.length;i++) {
      storePluginData(p[i]);
      storePluginSettings(p[i]);
    }
  }

  
  
  /**
   * Returns an Array of devplugin.Plugin objects containing all available
   * plugins
   */
  public static Plugin[] getAvailablePlugins() {
    if (mAvailablePluginHash == null) {
      loadAvailablePlugins();
    }

    Collection AvailablePluginCol = mAvailablePluginHash.values();
    Plugin[] result = new Plugin[AvailablePluginCol.size()];
    AvailablePluginCol.toArray(result);
  	return result;
  }
  


  private static void loadAvailablePlugins() {
    if (mAvailablePluginHash != null) {
      return;
    }

    File file=new File("plugins");
    mAvailablePluginHash = new HashMap();
    if (!file.exists()) {
      file.mkdir();
    }

    File[] fileList=file.listFiles(new FileFilter() {
      public boolean accept(File f) {
        return f.getName().endsWith(".jar");
      }
    });
    
    java.net.URL[] urlList=new java.net.URL[fileList.length];
    for (int i=0;i<urlList.length;i++) {
      try {
        urlList[i] = fileList[i].toURL();
      } catch (java.net.MalformedURLException exc) {
        String msg = mLocalizer.msg("error.5", "Loading plugin failed!\n({0})",
          fileList[i].getAbsolutePath(), exc);
        ErrorHandler.handle(msg, exc);
      }
    }

    ClassLoader classLoader=new java.net.URLClassLoader(urlList,ClassLoader.getSystemClassLoader());

    Class c;

    for (int i=0;i<fileList.length;i++) {
      String s=fileList[i].getName();
      s=s.substring(0,s.length()-4);
      try {
        c=classLoader.loadClass(s.toLowerCase()+"."+s);
        Plugin p=(devplugin.Plugin)c.newInstance();
        p.setJarFile(fileList[i]);

        String name=p.getClass().getName();
        mAvailablePluginHash.put(p.getClass().getName(),p);

       	mLog.info("Plugin " + name + " available");
      }
      catch (Exception exc) {
        String msg = mLocalizer.msg("error.5", "Loading plugin failed!\n({0})",
          fileList[i].getAbsolutePath(), exc);
        ErrorHandler.handle(msg, exc);
      }
    }
  }
  
  

  /**
   * Returns the installed mAvailablePluginHash as an array of Plugin-Objects
   */
  public static Plugin[] getInstalledPlugins() {
    boolean installedPluginsChanged = Settings.settingHasChanged(new String[]{"plugins"});
  	if ((mInstalledPluginList == null) || installedPluginsChanged) {
  		mInstalledPluginList = new ArrayList();
  		loadAvailablePlugins();
  		String[] instPI = Settings.getInstalledPlugins();
  		for (int i = 0; i < instPI.length; i++) {
        Plugin plugin = (Plugin) mAvailablePluginHash.get(instPI[i]);
        if (plugin != null) {
          mInstalledPluginList.add(plugin);
        }
      }
  	}
  	
    Plugin[] subscribedPluginArr = new Plugin[mInstalledPluginList.size()];
    mInstalledPluginList.toArray(subscribedPluginArr);
    return subscribedPluginArr;
  }

  
  
  /**
   * Returns the installed mAvailablePluginHash as an array of Plugin-Objects
   */
  public static void setInstalledPlugins(Plugin[] pluginArr) {
    // Create the new list and init those plugins who are new
    ArrayList newInstalledPluginList = new ArrayList(pluginArr.length);
    String[] pluginClassNameArr = new String[pluginArr.length];
    for (int i = 0; i < pluginArr.length; i++) {
      newInstalledPluginList.add(pluginArr[i]);
      pluginClassNameArr[i] = pluginArr[i].getClass().getName();
      if (! isInstalled(pluginArr[i])) {
        initPlugin(pluginArr[i]);
      }
    }
    
    // Now finalize those plugins who are not installed any more
    Iterator iter = mInstalledPluginList.iterator();
    while (iter.hasNext()) {
      Plugin plugin = (Plugin) iter.next();
      if (! newInstalledPluginList.contains(plugin)) {
        finalizePlugin(plugin);
      }
    }
    
    // Now set the new list a current list
    mInstalledPluginList = newInstalledPluginList;
    
    // Finally update the Settings
    Settings.setInstalledPlugins(pluginClassNameArr);
  }
  
  
  
  /**
   * Returns a devplugin.Plugin object with the specified name, or null if
   * the plugin does not exist.
   */
  public static Plugin getPlugin(String pluginClassName) {
    return (Plugin) mAvailablePluginHash.get(pluginClassName);
  }

  
  
  /**
   * Returns true, if the plugin with the specified name is currently installed.
   */
  public static boolean isInstalled(Plugin plugin) {
    return mInstalledPluginList.contains(plugin);
  }

  
  
  /**
   * Should be called every time the TV data has changed.
   * <p>
   * Calls for every subscribed plugin the handleTvDataChanged() method,
   * so the mAvailablePluginHash can react on the new data.
   *
   * @see Plugin#handleTvDataChanged()
   */
  public static void fireTvDataChanged() {
    Iterator pluginIter = mInstalledPluginList.iterator();
    while (pluginIter.hasNext()) {
      Plugin plugin = (Plugin) pluginIter.next();
      plugin.handleTvDataChanged();
    }
  }

}