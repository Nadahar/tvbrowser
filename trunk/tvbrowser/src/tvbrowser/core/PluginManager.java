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
import java.util.Properties;
import java.util.HashMap;

import util.exc.*;

/**
 * The PluginManager is a Class for communicating with installed plugins.
 *
 * @author Martin Oberhauser
 */
public class PluginManager {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(PluginManager.class.getName());
  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(PluginManager.class);
  
  private static HashMap plugins;
  private static HashMap installedPlugins;

  /**
   * Calls loadData() for each Plugin after loading the plugins data
   */
  private static void loadPluginData(Plugin plugin) {
    Class c=plugin.getClass();
    String dir=Settings.getUserDirectoryName();
    File f=new File(dir,c.getName()+".dat");
    if (f.exists()) {
      try {
        ObjectInputStream in=new ObjectInputStream(new FileInputStream(f));
        plugin.loadData(in);
        in.close();
      } catch (IOException exc) {
        String msg = mLocalizer.msg("error.1", "Loading data for plugin {0} failed!\n({1})",
          plugin.getButtonText(), f.getAbsolutePath(), exc);
        ErrorHandler.handle(msg, exc);
      }
    }
  }

  /**
   * Calls storeData for each Plugin and stores the plugins data
   */
  private static void storePluginData(Plugin plugin) {
    Object data=plugin.storeData();
    if (data!=null) {
      String dir=Settings.getUserDirectoryName();
      File f=new File(dir);
      if (!f.exists()) {
        f.mkdir();
      }
      f=new File(dir,plugin.getClass().getName()+".dat");
      try {
        ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(f));
        out.writeObject(data);
        out.close();
      } catch(IOException exc) {
        String msg = mLocalizer.msg("error.2", "Saving data for plugin {0} failed!\n({1})",
          plugin.getButtonText(), f.getAbsolutePath(), exc);
        ErrorHandler.handle(msg, exc);
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

  /**
   * Kind of constructor
   */
  public static void initInstalledPlugins() {
    Object[] p=getInstalledPlugins();
    for (int i=0;i<p.length;i++) {
    	initPlugin((Plugin)p[i]);
     // loadPluginData((Plugin)p[i]);
     // loadPluginSettings((Plugin)p[i]);
    }
  }

  /**
   * Kind of destructor: call plugins to store their data and settings
   */
  public static void finalizeInstalledPlugins() {
    Object[] p=getInstalledPlugins();
    for (int i=0;i<p.length;i++) {
      storePluginData((Plugin)p[i]);
      storePluginSettings((Plugin)p[i]);
    }
  }

  /**
   * Returns an Array of devplugin.Plugin objects containing all available plugins
   */

  public static Object[] getAvailablePlugins() {
    if (plugins==null) {
      loadAvailablePlugins();
    }

    return plugins.values().toArray();

  }


  private static void loadAvailablePlugins() {

    if (plugins!=null) {
      return;
    }

    File file=new File("plugins");
    plugins=new HashMap();
    if (!file.exists()) {
      file.mkdir();
    }

    File[] fileList=file.listFiles(new FileFilter() {
                                   public boolean accept(File f) {
                                     return f.getName().endsWith(".jar");
                                   }
    }
    );

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
        plugins.put(p.getClass().getName(),p);

        devplugin.PluginInfo info=p.getInfo();
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
   * Returns the installed plugins as an array of Plugin-Objects
   */
  public static Object[] getInstalledPlugins() {
    if (installedPlugins!=null) {
      return installedPlugins.values().toArray();
    }

    Object o[]=Settings.getInstalledPlugins();  // array of String
    installedPlugins=new HashMap();

    loadAvailablePlugins();
    for (int i=0;i<o.length;i++) {
      Plugin p=(Plugin)plugins.get(o[i]);
      if (p==null) continue;
      installedPlugins.put(p.getClass().getName(),p);
    }

    return installedPlugins.values().toArray();
  }

  /**
   * Returns a devplugin.Plugin object with the specified name, or null if
   * the plugin does not exist.
   */
  public static Plugin getPlugin(String plugin) {
    return (Plugin)plugins.get(plugin);
  }

  /**
   * Returns true, if the plugin with the specified name is currently installed.
   */
  public static boolean isInstalled(String plugin) {
    return installedPlugins.get(plugin) != null;
  }

  /**
   * Installs the plugin with the specified name.
   */
  public static void installPlugin(String plugin) {

    Object obj=installedPlugins.get(plugin);

    if (obj!=null) {
      return;  // already installed
    }

    obj=plugins.get(plugin);
    if (obj==null) {
      throw new RuntimeException("Plugin "+plugin+" not found");
    }

    installedPlugins.put(plugin,obj);
    
    initPlugin((Plugin)obj);
    
  }

  /**
   * Uninstalls the plugin with the specified name
   */
  public static void uninstallPlugin(String plugin) {
    installedPlugins.remove(plugin);
  }



}