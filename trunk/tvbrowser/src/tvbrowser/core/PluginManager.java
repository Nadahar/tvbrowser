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

import tvdataservice.TvDataService;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import devplugin.*;
import devplugin.Date;

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

  private static Font CONTEXT_MENU_PLAINFONT = new Font("Dialog", Font.PLAIN, 12);
  private static Font CONTEXT_MENU_BOLDFONT = new Font("Dialog", Font.BOLD, 12);
  
  private boolean mContextMenuIsValid;
  
  /** The singleton. */
  private static PluginManager mSingleton;
  
  private HashMap mAvailablePluginHash;
  private ArrayList mInstalledPluginList;
  private Plugin[] mContextMenuPluginList;
  private Plugin mContextMenuDefaultPlugin;
  
 
  private PluginManager() {
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
  
  
  public static PluginManager getInstance() {
    if (mSingleton == null) {
      mSingleton = new PluginManager();
    }
    
    return mSingleton;
  }
  
  
  public void loadPlugins() {
    Plugin[] p=getAvailablePlugins();
    for (int i=0;i<p.length;i++) {
      initPlugin((Plugin)p[i]);
    }
  }


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


  public static void installPendingPlugins() {
	  File file=new File("plugins");
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


  /**
   * Kind of destructor: call plugins to store their data and settings
   */
  public void finalizeInstalledPlugins() {
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
  public Plugin[] getAvailablePlugins() {
    if (mAvailablePluginHash == null) {
      loadAvailablePlugins();
    }

    Collection AvailablePluginCol = mAvailablePluginHash.values();
    Plugin[] result = new Plugin[AvailablePluginCol.size()];
    AvailablePluginCol.toArray(result);
  	return result;
  }
  


  private void loadAvailablePlugins() {
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
  
  public Plugin getContextMenuDefaultPlugin() {
    boolean contextMenuDefaultPluginsChanged= Settings.settingHasChanged(new String[]{"contextmenudefaultplugin"});
    if (mContextMenuDefaultPlugin==null || contextMenuDefaultPluginsChanged) {
      mContextMenuDefaultPlugin=getPlugin(Settings.getDefaultContextMenuPlugin());
    }
    if (isInstalled(mContextMenuDefaultPlugin)) {
      return mContextMenuDefaultPlugin;
    }
    return null;
  }
  
  public Plugin[] getContextMenuPlugins() {
    if (mContextMenuPluginList == null || ! mContextMenuIsValid) {
      mContextMenuIsValid=true;
      
      // Add all plugins which are installed by the settings (if valid)
      String plugins[]=Settings.getContextMenuItemPlugins();
      ArrayList list=new ArrayList();
      for (int i=0;i<plugins.length;i++) {
        Plugin p=getPlugin(plugins[i]);
        if (p!=null && p.getContextMenuItemText()!=null && isInstalled(p)) {
          list.add(p);
        }      
      }
      
      // Newer plugins may not be displayed in the settings. So check the rest of the plugins
      Plugin[] unconfiguredPlugins=getInstalledPlugins();
      for (int i=0;i<unconfiguredPlugins.length;i++) {
        Plugin p=unconfiguredPlugins[i];
        if (!list.contains(p) && p!=null && p.getContextMenuItemText()!=null) {
          list.add(p);
        }
      }
      
      mContextMenuPluginList=new Plugin[list.size()];
      list.toArray(mContextMenuPluginList);
    }
    return mContextMenuPluginList;
  }

  /**
   * Returns the installed mAvailablePluginHash as an array of Plugin-Objects
   */
  public Plugin[] getInstalledPlugins() {
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

  
  public void setInstalledPlugins(Plugin[] pluginArr) {
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
    
    // We must create a new context menu
    mContextMenuIsValid=false;
  }
  
  
  
  public Plugin getPluginByName(String name) {
  	return getPlugin(name.toLowerCase()+"."+name);
  }
  
  /**
   * Returns a devplugin.Plugin object with the specified name, or null if
   * the plugin does not exist.
   */
  public Plugin getPlugin(String pluginClassName) {
    return (Plugin) mAvailablePluginHash.get(pluginClassName);
  }

  
  
  /**
   * Returns true, if the plugin with the specified name is currently installed.
   */
  public boolean isInstalled(Plugin plugin) {
    return mInstalledPluginList.contains(plugin);
  }
  
  
  /**
   * Calls for every subscribed plugin the fireTvDataAdded(...) method,
   * so the Plugin can react on the new data.
   *
   * @see Plugin#handleTvDataAdded(ChannelDayProgram)
   */
  private void fireTvDataAdded(ChannelDayProgram newProg) {
    Iterator pluginIter = mInstalledPluginList.iterator();
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
    Iterator pluginIter = mInstalledPluginList.iterator();
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
    Iterator pluginIter = mInstalledPluginList.iterator();
    while (pluginIter.hasNext()) {
      Plugin plugin = (Plugin) pluginIter.next();
      plugin.handleTvDataChanged();
    }
  }

  
  private static devplugin.PluginManager createDevpluginPluginManager() {
    return new devplugin.PluginManager() {
      public devplugin.Program getProgram(Date date, String progID) {
        return doGetProgram(date, progID);
      }

      public Channel[] getSubscribedChannels() {
        return ChannelList.getSubscribedChannels();
      }

      public Iterator getChannelDayProgram(Date date, Channel channel) {
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
        return PluginManager.getInstance().getInstalledPlugins();
      }

      public TvDataService getDataService(String className) {
        return TvDataServiceManager.getInstance().getDataService(className);
      }

      public JPopupMenu createPluginContextMenu(Program program, Plugin caller) {
        return PluginManager.createPluginContextMenu(program, caller);
      }
    };
  }


  private static Program doGetProgram(Date date, String progID) {
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


  /**
   * Creates a context menu containg all subscribed plugins that support context
   * menues.
   *
   * @return a plugin context menu.
   */
  public static JPopupMenu createPluginContextMenu(final Program program,
    Plugin caller)
  {
    JPopupMenu menu = new JPopupMenu();
    Plugin defaultPlugin = getInstance().getContextMenuDefaultPlugin();
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

}