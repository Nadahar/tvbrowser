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


import devplugin.*;
import tvdataservice.TvDataService;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.JPopupMenu;

public class PluginLoader {
  
  private static java.util.logging.Logger mLog
     = java.util.logging.Logger.getLogger(PluginLoader.class.getName());
  
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(PluginLoader.class);

  private static PluginLoader mInstance;
  
  private HashSet mActivePlugins;
  private HashSet mInactivePlugins;
  private ArrayList mPluginStateListeners;
  
  private PluginLoader() {
    mActivePlugins = new HashSet();
    mInactivePlugins = new HashSet();  
    mPluginStateListeners = new ArrayList();
    
    TvDataBase.getInstance().addTvDataListener(new TvDataBaseListener() {
      public void dayProgramAdded(ChannelDayProgram prog) {
        fireTvDataAdded(prog);
      }

      public void dayProgramDeleted(ChannelDayProgram prog) {
        fireTvDataDeleted(prog);
      }
    });
    
    TvDataUpdater.getInstance().addTvDataUpdateListener(new TvDataUpdateListener() {
      public void tvDataUpdateStarted() {
    }

      public void tvDataUpdateFinished() {
        fireTvDataUpdateFinished();
      }
    });

    Plugin.setPluginManager(createDevpluginPluginManager());
    
    
  }
  
  public void addPluginStateListener(PluginStateListener listener) {
    mPluginStateListeners.add(listener);
  }
  
 
  /**
   * Calls for every subscribed plugin the fireTvDataAdded(...) method,
   * so the Plugin can react on the new data.
   *
   * @see Plugin#handleTvDataAdded(ChannelDayProgram)
   */
  private void fireTvDataAdded(ChannelDayProgram newProg) {
    Iterator pluginIter = mActivePlugins.iterator();
    while (pluginIter.hasNext()) {
      Plugin plugin = (Plugin) pluginIter.next();
      plugin.handleTvDataAdded(newProg);
    }
  }


  /**
   * Calls for every subscribed plugin the fireTvDataRemoved(...) method,
   * so the Plugin can react on the deleted data.
   *
   * @see Plugin#handleTvDataDeleted(ChannelDayProgram)
   */
  private void fireTvDataDeleted(ChannelDayProgram newProg) {
    Iterator pluginIter = mActivePlugins.iterator();
    while (pluginIter.hasNext()) {
      Plugin plugin = (Plugin) pluginIter.next();
      plugin.handleTvDataDeleted(newProg);
    }
  }


  /**
   * Calls for every subscribed plugin the handleTvDataChanged() method,
   * so the Plugin can react on the new data.
   *
   * @see Plugin#handleTvDataChanged()
   */
  private void fireTvDataUpdateFinished() {
    Iterator pluginIter = mActivePlugins.iterator();
    while (pluginIter.hasNext()) {
      Plugin plugin = (Plugin) pluginIter.next();
      plugin.handleTvDataChanged();
    }
  }

  
  
  public static PluginLoader getInstance() {
    if (mInstance==null) {
      mInstance=new PluginLoader();
    }
    return mInstance;
  }
  
    
  /**
   * Loads the plugin defined by the file pluginFile.
   * <p>
   * Calls the constructor of the plugin. Before you can use the plugin, you
   * have to activate it using PluginManager.active(Plugin).
   *
   * @return a new plugin
   */  
  public Plugin loadPlugin(File pluginFile) throws TvBrowserException {
    
    Plugin plugin=null;
    
    ClassLoader classLoader;
    try {
      classLoader = URLClassLoader.newInstance(new URL[] { pluginFile.toURL()},ClassLoader.getSystemClassLoader());
    } catch (MalformedURLException exc) {
      return null;
    }

    Class c;
    String pluginName = pluginFile.getName();
    if (pluginName.endsWith(".jar")) {
      pluginName=pluginName.substring(0,pluginName.length()-4); 
    }
  
    try {
      c=classLoader.loadClass(pluginName.toLowerCase()+"."+pluginName);
      plugin=(devplugin.Plugin)c.newInstance();
      plugin.setJarFile(pluginFile);

      String name=plugin.getClass().getName();
            
      mInactivePlugins.add(plugin);
      firePluginLoaded(plugin);
      mLog.info("Plugin " + name + " loaded (inactive)");
    }
    catch (Exception exc) {
      throw new TvBrowserException(getClass(), "error.1",
              "Could not load plugin {0}", pluginFile.getAbsolutePath(),exc);
    }
    
    return plugin;
  }
  
  /**
   * Unloads the plugin.
   * <p>
   * After unloading a plugin it can't be used again.
   * @param plugin
   */
  public void unloadPlugin(Plugin plugin) {
    mInactivePlugins.remove(plugin);
    firePluginUnloaded(plugin);    
    mLog.info("Plugin "+plugin.getClass().getName()+" unloaded");    
  }
  
  /**
   * Activates a plugin. A plugin must be activated before you can use it.
   * @param plugin
   */
  public void activatePlugin(Plugin plugin) {
      
    String userDirectoryName = Settings.getUserDirectoryName();
    File f;
    
    /* load plugin data */
        
    f = new File(userDirectoryName, plugin.getClass().getName() + ".dat");
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
    
    /* load plugin settings */
           
    f=new File(userDirectoryName,plugin.getClass().getName()+".prop");
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
    
    mInactivePlugins.remove(plugin);
    mActivePlugins.add(plugin);
    firePluginActivated(plugin);  
    mLog.info("Plugin "+plugin.getClass().getName()+" activated");
  }
  
  
  
  /**
   * Deactivates a plugin. A deactivated plugin must be activated before you
   * can use it again.
   * 
   * @param plugin
   */
  public void deactivatePlugin(Plugin plugin) {
    
    String userDirectoryName = Settings.getUserDirectoryName();
        
    /* save the plugin data */
    File f = new File(userDirectoryName, plugin.getClass().getName() + ".dat");
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(new FileOutputStream(f));
      plugin.writeData(out);
    }
    catch(IOException exc) {
      String msg = mLocalizer.msg("error.3", "Saving data for plugin {0} failed!\n({1})",
        plugin.getInfo().getName(), f.getAbsolutePath(), exc);
      ErrorHandler.handle(msg, exc);
    }
    finally {
      if (out != null) {
        try { out.close(); } catch (IOException exc) {}
      }
    }

    /* save the plugin settings */

    Properties prop=plugin.storeSettings();
    if (prop!=null) {
      String dir=Settings.getUserDirectoryName();
      f=new File(dir);
      if (!f.exists()) {
        f.mkdir();
      }
      f=new File(dir,plugin.getClass().getName()+".prop");
      try {
        FileOutputStream fOut=new FileOutputStream(f);
        prop.store(fOut,"settings");
        fOut.close();
      } catch (IOException exc) {
        String msg = mLocalizer.msg("error.4", "Saving settings for plugin {0} failed!\n({1})",
          plugin.getButtonText(), f.getAbsolutePath(), exc);
        ErrorHandler.handle(msg, exc);
      }
    }
    
    mActivePlugins.remove(plugin);
    mInactivePlugins.add(plugin);
    firePluginDeactivated(plugin);  
    mLog.info("Plugin "+plugin.getClass().getName()+" deactivated");
    
  }
    
  public void shutdownAllPlugins() {
    
    Plugin[] p = getActivePlugins();
    for (int i=0;i<p.length;i++) {
      deactivatePlugin(p[i]);
    }
    
    p = getActivePlugins();
    for (int i=0;i<p.length;i++) {
      unloadPlugin(p[i]);
    }

  }
  
  public boolean isActivePlugin(Plugin p) {
    return mActivePlugins.contains(p);
  }
  
  public boolean isInactivePlugin(Plugin p) {
    return mInactivePlugins.contains(p);
  }
    
  public Plugin[] getActivePlugins() {    
    return PluginManager.createPluginArr(mActivePlugins);    
  }
  
  public Plugin[] getInactivePlugins() {
    return PluginManager.createPluginArr(mInactivePlugins);
  }
  
  public Plugin[] getAllPlugins() {
    HashSet union=new HashSet();
    union.addAll(mActivePlugins);
    union.addAll(mInactivePlugins);
    return PluginManager.createPluginArr(union);    
  }
  
  
  public Plugin getPluginByClassName(String className) {
    Plugin result = getActivePluginByClassName(className);
    if (result==null) {
      return getInactivePluginByClassName(className);
    }
    return result;
  }
  
  public Plugin getPluginByName(String name) {
    return getPluginByClassName(name.toLowerCase()+"."+name);
  }
  
  public Plugin getActivePluginByClassName(String className) {
    Iterator it = mActivePlugins.iterator();
    while (it.hasNext()) {
      Plugin p = (Plugin)it.next();
      if (p.getClass().getName().equals(className)) {
        return p;
      }
    }
    return null;
  }
  
  public Plugin getActivePluginByName(String name) {
    return getActivePluginByClassName(name.toLowerCase()+"."+name);
  }
  
  public Plugin getInactivePluginByClassName(String className) {
      Iterator it = mInactivePlugins.iterator();
      while (it.hasNext()) {
        Plugin p = (Plugin)it.next();
        if (p.getClass().getName().equals(className)) {
          return p;
        }
      }
      return null;
    }
    
  public Plugin getInactivePluginByName(String name) {
     return getPluginByClassName(name.toLowerCase()+"."+name);
   }   
  
  private devplugin.PluginManager createDevpluginPluginManager() {
      return new devplugin.PluginManager() {
        public devplugin.Program getProgram(devplugin.Date date, String progID) {
          return doGetProgram(date, progID);
        }

        public Channel[] getSubscribedChannels() {
          return ChannelList.getSubscribedChannels();
        }

        public Iterator getChannelDayProgram(devplugin.Date date, Channel channel) {
          ChannelDayProgram channelDayProgram=TvDataBase.getInstance().getDayProgram(date,channel);
          if (channelDayProgram==null) {
            return null;    
          }
          return channelDayProgram.getPrograms();
        }

        public Program[] search(String regex, boolean inTitle, boolean inText,
          boolean caseSensitive, Channel[] channels, devplugin.Date startDate,
          int nrDays)
          throws TvBrowserException
        {
          return TvDataSearcher.getInstance().search(regex, inTitle, inText,
            caseSensitive, channels, startDate, nrDays);
        }

        public Plugin[] getInstalledPlugins() {
          return getActivePlugins();
        }

        public TvDataService getDataService(String className) {
          return TvDataServiceManager.getInstance().getDataService(className);
        }

        public JPopupMenu createPluginContextMenu(Program program, Plugin caller) {
          return PluginManager.createPluginContextMenu(program, caller);
        }
      };
    }
  
  
  private static Program doGetProgram(devplugin.Date date, String progID) {
      TvDataBase db = TvDataBase.getInstance();
    
      Iterator channelIter = ChannelList.getChannels();
      while (channelIter.hasNext()) {
        Channel channel = (Channel) channelIter.next();
  
        ChannelDayProgram dayProg = db.getDayProgram(date, channel);
        if (dayProg != null) {
          Program prog = dayProg.getProgram(progID);
          if (prog != null) {
            return prog;
          }
        }
      }
    
      return null;
    }
  
  private void firePluginLoaded(Plugin p) {
    synchronized(mPluginStateListeners) {
      for (int i=0;i<mPluginStateListeners.size();i++) {
        ((PluginStateListener)mPluginStateListeners.get(i)).pluginLoaded(p);
      }
    }
  }
    
  private void firePluginUnloaded(Plugin p) {
    synchronized(mPluginStateListeners) {
      for (int i=0;i<mPluginStateListeners.size();i++) {
        ((PluginStateListener)mPluginStateListeners.get(i)).pluginUnloaded(p);
      }
    }
  }
  
  private void firePluginActivated(Plugin p) {
    synchronized(mPluginStateListeners) {
      for (int i=0;i<mPluginStateListeners.size();i++) {
        ((PluginStateListener)mPluginStateListeners.get(i)).pluginActivated(p);
      }
    }
  }
  
  private void firePluginDeactivated(Plugin p) {
    synchronized(mPluginStateListeners) {
      for (int i=0;i<mPluginStateListeners.size();i++) {
        ((PluginStateListener)mPluginStateListeners.get(i)).pluginDeactivated(p);
      }
    }    
  }
  
}
