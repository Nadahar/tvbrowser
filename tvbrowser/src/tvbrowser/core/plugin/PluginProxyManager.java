/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package tvbrowser.core.plugin;

import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import tvbrowser.core.*;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import devplugin.ChannelDayProgram;
import devplugin.PluginAccess;
import devplugin.Program;

/**
 * Manages all plugin proxies and creates them on startup.
 * <p>
 * Note: This class is not called "PluginManager" as in older versions, to make
 * a difference to the class {@link devplugin.PluginManager}.
 *
 * @author Til Schneider, www.murfman.de
 */
public class PluginProxyManager {

  /** The logger for this class */
  private static java.util.logging.Logger mLog
    = Logger.getLogger(PluginProxyManager.class.getName());

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(PluginProxyManager.class);
  
  /** The singleton. */
  private static PluginProxyManager mSingleton;

  /** The name of the directory where the plugins are located */
  private static String PLUGIN_DIRECTORY = "plugins";
  
  /**
   * The plugin state 'shut down'.
   * <p>
   * An shut down plugin has been shut down and can't be used any more.
   */
  public static int SHUT_DOWN_STATE = 1;
  
  /**
   * The plugin state 'loaded'.
   * <p>
   * A loaded plugin has an existing PluginProxy instance, but is not activated.
   */
  public static int LOADED_STATE = 2;
  
  /**
   * The plugin state 'activated'.
   * <p>
   * An activated plugin has loaded its settings and is ready to be used.
   */
  public static int ACTIVATED_STATE = 3;
  
  /**
   * The font to use in the context menu for normal plugins.
   * 
   * @see #createPluginContextMenu(Program) 
   */
  private static Font CONTEXT_MENU_PLAINFONT = new Font("Dialog", Font.PLAIN, 12);

  /**
   * The font to use in the context menu for the default plugin.
   * 
   * @see #createPluginContextMenu(Program) 
   */
  private static Font CONTEXT_MENU_BOLDFONT = new Font("Dialog", Font.BOLD, 12);
  
  /** The list containing all plugins (PluginListItem objects) in the right order. */
  private ArrayList mPluginList;

  /** The registered {@link PluginStateListener}s. */
  private ArrayList mPluginStateListenerList;

  /**
   * The plugin that should be executed by default when double-clicking a
   * program in the program table. It is shown with a bold font in the context
   * menu.
   */
  private PluginProxy mDefaultContextMenuPlugin;
  
  /** The currently activated plugins. This is only a cache, it might be null. */
  private PluginProxy[] mActivatedPluginCache;

  /** All plugins. This is only a cache, it might be null. */
  private PluginProxy[] mAllPluginCache;
  

  /**
   * Creates a new instance of PluginProxyManager.
   */
  private PluginProxyManager() {
    mPluginList = new ArrayList();
    mPluginStateListenerList = new ArrayList();
    
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
    
    devplugin.Plugin.setPluginManager(new PluginManagerImpl());
  }


  /**
   * Gets the singleton.
   * 
   * @return the singleton.
   */
  public static PluginProxyManager getInstance() {
    if (mSingleton == null) {
      mSingleton = new PluginProxyManager();
    }
    
    return mSingleton;
  }

  
  /**
   * This method must be called on start-up.
   * 
   * @throws TvBrowserException If initialization failed.
   */
  public void init() throws TvBrowserException {
    // Install the pending plugins
    installPendingPlugins();
    
    // Load all plugins
    loadAllPlugins();
    
    // Get the plugin order
    String[] pluginOrderArr = Settings.propPluginOrder.getStringArray();
    
    // Get the list of deactivated plugins
    String[] deactivatedPluginArr = Settings.propDeactivatedPlugins.getStringArray();
    
    // Check whether the plugin order is the old setting using class names
    if ((pluginOrderArr != null) && (pluginOrderArr.length > 0)) {
      // Check whether the first entry is a class name and not an ID
      String className = pluginOrderArr[0];
      String asId = "java." + className;
      if ((getPluginForId(className) == null) && (getPluginForId(asId) != null)) {
        // It is the old array
        
        // Create the list of deactivated plugins
        deactivatedPluginArr = installedClassNamesToDeactivatedIds(pluginOrderArr);
        Settings.propDeactivatedPlugins.setStringArray(deactivatedPluginArr);
        
        // Convert the old array of class names into an array of IDs
        for (int i = 0; i < pluginOrderArr.length; i++) {
          pluginOrderArr[i] = "java." + pluginOrderArr[i];
        }
        Settings.propPluginOrder.setStringArray(pluginOrderArr);
        
        // Convert the default context menu plugin from class name to ID
        String defaultPluginClassName = Settings.propDefaultContextMenuPlugin.getString();
        Settings.propDefaultContextMenuPlugin.setString("java." + defaultPluginClassName);
      }
    }
    
    // Set the plugin order
    setPluginOrder(pluginOrderArr);

    // Activate all plugins except the ones that are explicitely deactivated
    activateAllPluginsExcept(deactivatedPluginArr);

    // Get the default context menu plugin
    String id = Settings.propDefaultContextMenuPlugin.getString();
    setDefaultContextMenuPlugin(getPluginForId(id));
  }
  
  
  /**
   * Sets the parent frame to all plugins.
   * 
   * @param parent The parent frame to set.
   */
  public void setParentFrame(Frame parent) {
    synchronized(mPluginList) {
      for (int pluginIdx = 0; pluginIdx < mPluginList.size(); pluginIdx++) {
        PluginListItem item = (PluginListItem) mPluginList.get(pluginIdx);
        item.getPlugin().setParentFrame(parent);
      }
    }
  }

  
  /**
   * Installs all plugins that could not be installed the last time, because an
   * old version was in use.
   */
  private void installPendingPlugins() {
    File[] fileArr = new File(PLUGIN_DIRECTORY).listFiles();
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
        
        // Install the plugin
        if (! fileArr[i].renameTo(oldFile)) {
          mLog.warning("Installing pending plugin failed: " + fileName);
        }
      }
    }
  }
  
  
  /**
   * Loads all plugins.
   */
  private void loadAllPlugins() {
    File[] fileArr = new File(PLUGIN_DIRECTORY).listFiles();
    if (fileArr == null) {
      // Nothing to do
      return;
    }
    
    // Load all plugins
    synchronized(mPluginList) {
      for (int i = 0; i < fileArr.length; i++) {
        // Try to load this plugin
        AbstractPluginProxy plugin = null;
        try {
          plugin = loadPlugin(fileArr[i]);
        }
        catch (Throwable thr) {
          mLog.log(Level.WARNING, "Loading plugin file failed: "
            + fileArr[i].getAbsolutePath(), thr);
          thr.printStackTrace();
        }
        
        // Add the plugin to the list
        if (plugin != null) {
          // Log this event
          mLog.info("Loaded plugin " + plugin.getId());

          // Add it to the list
          mPluginList.add(new PluginListItem(plugin));
          firePluginLoaded(plugin);
          
          // Clear the cache
          mAllPluginCache = null;
        }
      }
    }
  }

  
  /**
   * Loads a plugin.
   * 
   * @param file The plugin to load.
   * @return The loaded plugin or <code>null</code> if this kind of plugin is
   *         not known (In this case a warning is logged).
   * @throws TvBrowserException If loading the plugin failed.
   */
  private AbstractPluginProxy loadPlugin(File file) throws TvBrowserException {
    String lcFileName = file.getName().toLowerCase();
    
    if (lcFileName.endsWith(".jar")) {
      return new JavaPluginProxy(file);
    } else if (lcFileName.endsWith(".bsh")) {
        return new BeanShellPluginProxy(file);
    }
    else if (lcFileName.endsWith(".inst")) {
      // This is a plugins with a pending installation
      // -> When installing failed a warning will be generated elsewere 
      return null;
    } else {
      // This pugin type is unknown -> Generate a warning
      mLog.warning("Unknown plugin type: " + file.getAbsolutePath());
      return null;
    }
  }
  
  
  /**
   * Converts an array of class names of installed (= activated) plugins
   * (that's the way plugins were saved in former times) into an array of IDs of
   * deactivated plugins (that's the way plugins are saved now).
   * 
   * @param installedPluginClassNameArr The (old) array of class names of
   *        installed (= activated) plugins
   * @return The (new) array of IDs of deactivated plugins.
   */
  private String[] installedClassNamesToDeactivatedIds(
    String[] installedPluginClassNameArr)
  {
    // Create a list of IDs of deactivated plugins.
    ArrayList deactivatedPluginList = new ArrayList();
    synchronized(mPluginList) {
      for (int pluginIdx = 0; pluginIdx < mPluginList.size(); pluginIdx++) {
        PluginListItem item = (PluginListItem) mPluginList.get(pluginIdx);
        String pluginId = item.getPlugin().getId();
        
        // Check whether this plugin is deactivated
        boolean activated = false;
        for (int i = 0; i < installedPluginClassNameArr.length; i++) {
          // Convert the class name into a ID
          String asId = "java." + installedPluginClassNameArr[i];
          if (pluginId.equals(asId)) {
            activated = true;
            break;
          }
        }
        
        // Add the plugin ID, if the plugin is not activated
        if (! activated) {
          deactivatedPluginList.add(pluginId);
        }
      }
    }
    
    // Create an array from the list
    String[] deactivatedPluginIdArr = new String[deactivatedPluginList.size()];
    deactivatedPluginList.toArray(deactivatedPluginIdArr);
    return deactivatedPluginIdArr;
  }

  
  /**
   * Sets the plugin order.
   * <p>
   * The order will not be saved to the settings. This must be done by the
   * caller.
   * 
   * @param pluginOrderArr The IDs of the plugins in the wanted order. All
   *        missing plugins will be appended at the end.
   */
  public void setPluginOrder(String[] pluginOrderArr) {
    if ((pluginOrderArr == null) || (pluginOrderArr.length == 0)) {
      // Nothing to do
      return;
    }
    
    synchronized(mPluginList) {
      // Move all plugin list items to a temporary list 
      ArrayList tempList = new ArrayList(mPluginList.size());
      tempList.addAll(mPluginList);
      mPluginList.clear();
      
      // Now bring the items back in the wanted order
      for (int idIdx = 0; idIdx < pluginOrderArr.length; idIdx++) {
        String id = pluginOrderArr[idIdx];
        
        // Find the item with this id and move it to the mPluginList
        for (int i = 0; i < tempList.size(); i++) {
          PluginListItem item = (PluginListItem) tempList.get(i);
          if (id.equals(item.getPlugin().getId())) {
            tempList.remove(i);
            mPluginList.add(item);
            break;
          }
        }
      }
      
      // Append all remaining items
      mPluginList.addAll(tempList);
    }
    
    // Clear the caches
    mActivatedPluginCache = null;
    mAllPluginCache = null;
  }
  
  
  /**
   * Activates all plugins except for the given ones
   * 
   * @param deactivatedPluginIdArr The IDs of the plugins that should NOT be
   *        activated.
   */
  private void activateAllPluginsExcept(String[] deactivatedPluginIdArr) {
    synchronized(mPluginList) {
      for (int pluginIdx = 0; pluginIdx < mPluginList.size(); pluginIdx++) {
        PluginListItem item = (PluginListItem) mPluginList.get(pluginIdx);
        String pluginId = item.getPlugin().getId();
        
        // Check whether this plugin is deactivated
        boolean activated = true;
        if (deactivatedPluginIdArr != null) {
          for (int i = 0; i < deactivatedPluginIdArr.length; i++) {
            if (pluginId.equals(deactivatedPluginIdArr[i])) {
              activated = false;
              break;
            }
          }
        }
        
        // Activate the plugin
        if (activated) {
          try {
            activatePlugin(item);
          }
          catch (TvBrowserException exc) {
            ErrorHandler.handle(exc);
          }
        }
      }
    }
  }

  
  /**
   * Activates a plugin.
   * 
   * @param plugin The plugin to activate
   * @throws TvBrowserException If activating failed
   */
  public void activatePlugin(PluginProxy plugin) throws TvBrowserException {
    PluginListItem item = getItemForPlugin(plugin);
    if (item != null) {
      activatePlugin(item);
    }
  }
  
  
  /**
   * Activates a plugin.
   * 
   * @param item The item of the plugin to activate
   * @throws TvBrowserException If activating failed
   */
  private void activatePlugin(PluginListItem item) throws TvBrowserException {
    // Check the state
    checkStateChange(item, LOADED_STATE, ACTIVATED_STATE);
    
    // Log this event
    mLog.info("Activating plugin " + item.getPlugin().getId());
    
    // Get the user directory
    String userDirectoryName = Settings.getUserDirectoryName();
    File userDirectory = new File(userDirectoryName);
    
    // Load the plugin settings
    item.getPlugin().loadSettings(userDirectory);
    
    // Set the plugin active
    item.setState(ACTIVATED_STATE);
    
    // Clear the activated plugins cache
    mActivatedPluginCache = null;

    // Inform the listeners
    firePluginActivated(item.getPlugin());
  }


  /**
   * Deactivates a plugin.
   * 
   * @param plugin The plugin to deactivate
   * @throws TvBrowserException If deactivating failed
   */
  public void deactivatePlugin(PluginProxy plugin) throws TvBrowserException {
    PluginListItem item = getItemForPlugin(plugin);
    if (item != null) {
      deactivatePlugin(item);
    }
  }


  /**
   * Deactivates a plugin.
   * 
   * @param item The item of the plugin to deactivate
   * @throws TvBrowserException If deactivating failed
   */
  private void deactivatePlugin(PluginListItem item) throws TvBrowserException {
    // Check the state
    checkStateChange(item, ACTIVATED_STATE, LOADED_STATE);
    
    // Log this event
    mLog.info("Deactivating plugin " + item.getPlugin().getId());
    
    // Get the user directory
    String userDirectoryName = Settings.getUserDirectoryName();
    File userDirectory = new File(userDirectoryName);
    
    // Create the user directory if it does not exist
    if (! userDirectory.exists()) {
      userDirectory.mkdirs();
    }
    
    // Save the plugin settings
    item.getPlugin().saveSettings(userDirectory);
    
    // Set the plugin active
    item.setState(LOADED_STATE);
    
    // Clear the activated plugins cache
    mActivatedPluginCache = null;

    // Inform the listeners
    firePluginDeactivated(item.getPlugin());
  }

  
  /**
   * Deactivates and shuts down all plugins.
   */
  public void shutdownAllPlugins() {
    synchronized(mPluginList) {
      // Deactivate all active plugins
      for (int i = 0; i < mPluginList.size(); i++) {
        PluginListItem item = (PluginListItem) mPluginList.get(i);
        if (item.getPlugin().isActivated()) {
          try {
            deactivatePlugin(item);
          }
          catch (TvBrowserException exc) {
            ErrorHandler.handle(exc);
          }
        }
      }

      // Shut down all plugins
      for (int i = 0; i < mPluginList.size(); i++) {
        PluginListItem item = (PluginListItem) mPluginList.get(i);

        // Log this event
        mLog.info("Shutting down plugin " + item.getPlugin().getId());
        
        // Shut the plugin down
        item.setState(SHUT_DOWN_STATE);
        firePluginUnloaded(item.getPlugin());
      }
    }
    
  }
  
  
  /**
   * Checks, whether a plugin state change is allowed.
   * 
   * @param item The item of the plugin, whichs state should be changed.
   * @param requiredState The state the plugin should have at the moment.
   * @param newState The state the plugin will have after changing
   * @throws TvBrowserException If the plugin already is in the new state
   *         or if it is not in the required state.
   */
  private void checkStateChange(PluginListItem item, int requiredState,
    int newState)
    throws TvBrowserException
  {
    if (item.getState() == newState) {
      throw new TvBrowserException(PluginProxyManager.class, "error.1",
        "The plugin {0} can not be set to {1}, because it already is {1}.",
        item.getPlugin().getInfo().getName(), getLocalizedStateName(newState));
    }

    if (item.getState() != requiredState) {
      String[] params = {
        item.getPlugin().getInfo().getName(), getLocalizedStateName(newState),
        getLocalizedStateName(item.getState()), getLocalizedStateName(requiredState)
      };
      throw new TvBrowserException(PluginProxyManager.class, "error.2",
        "The plugin {0} can not be set to {1}, because is currently {2} and not {3}.",
        params);
    }
  }


  /**
   * Gets the localized name of a state.
   * 
   * @param state The state to get the localized name for. 
   * @return The localized name for the given state.
   */
  public static String getLocalizedStateName(int state) {
    if (state == SHUT_DOWN_STATE) {
      return mLocalizer.msg("state.shutDown", "shut down");
    }
    else if (state == LOADED_STATE) {
      return mLocalizer.msg("state.loaded", "loaded");
    }
    else if (state == ACTIVATED_STATE) {
      return mLocalizer.msg("state.activated", "activated");
    }
    else {
      return mLocalizer.msg("state.unknown", "unknown ({0})", new Integer(state));
    }
  }


  /**
   * Gets all plugins.
   * 
   * @return All plugins.
   */
  public PluginProxy[] getAllPlugins() {
    // NOTE: To be thread-safe, we copy the cached plugin array in a local
    //       variable
    PluginProxy[] allPluginArr = mAllPluginCache;
    
    // Check whether the cache is already filled 
    if (allPluginArr == null) {
      // The cache is empty -> We've got to fill it
      synchronized(mPluginList) {
        allPluginArr = new PluginProxy[mPluginList.size()];
        for (int i = 0; i < mPluginList.size(); i++) {
          PluginListItem item = (PluginListItem) mPluginList.get(i);
          allPluginArr[i] = item.getPlugin();
        }
      }
      
      // Cache the array
      mAllPluginCache = allPluginArr;
    }
    
    return allPluginArr;
  }


  /**
   * Gets all activated plugins.
   * 
   * @return All activated plugins.
   */
  public PluginProxy[] getActivatedPlugins() {
    // NOTE: To be thread-safe, we copy the cached plugin array in a local
    //       variable
    PluginProxy[] activatedPluginArr = mActivatedPluginCache;
    
    // Check whether the cache is already filled 
    if (activatedPluginArr == null) {
      // The cache is empty -> We've got to fill it
      
      // Create a list with all activated plugins
      ArrayList activatedPluginList = new ArrayList();
      synchronized(mPluginList) {
        for (int i = 0; i < mPluginList.size(); i++) {
          PluginListItem item = (PluginListItem) mPluginList.get(i);
          if (item.getPlugin().isActivated()) {
            activatedPluginList.add(item.getPlugin());
          }
        }
      }
      
      // Create an array from the list
      activatedPluginArr = new PluginProxy[activatedPluginList.size()];
      activatedPluginList.toArray(activatedPluginArr);
      
      // Cache the array
      mActivatedPluginCache = activatedPluginArr;
    }
    
    return activatedPluginArr;
  }


  /**
   * Gets the IDs of all deactivated plugins.
   * 
   * @return the IDs of all deactivated plugins.
   */
  public String[] getDeactivatedPluginIds() {
    // Create a list with all deactivated plugin IDs
    ArrayList deactivatedPluginIdList = new ArrayList();
    synchronized(mPluginList) {
      for (int i = 0; i < mPluginList.size(); i++) {
        PluginListItem item = (PluginListItem) mPluginList.get(i);
        if (! item.getPlugin().isActivated()) {
          deactivatedPluginIdList.add(item.getPlugin().getId());
        }
      }
    }
    
    // Create an array from the list
    String[] deactivatedPluginIdArr = new String[deactivatedPluginIdList.size()];
    deactivatedPluginIdList.toArray(deactivatedPluginIdArr);
    return deactivatedPluginIdArr;
  }

  
  /**
   * Gets the plugin with the given ID.
   * 
   * @param pluginId The ID of the wanted plugin.
   * @param state The state the plugin must have. Pass <code>-1</code> if the
   *        state does not matter. 
   * @return The plugin with the given ID or <code>null</code> if no such plugin
   *         exists or has the wrong state.
   */
  private PluginProxy getPluginForId(String pluginId, int state) {
    synchronized(mPluginList) {
      /*if (pluginId == null) {
        return null;
      }*/
      for (int i = 0; i < mPluginList.size(); i++) {
        PluginListItem item = (PluginListItem) mPluginList.get(i);
        if (item != null && pluginId != null && pluginId.equals(item.getPlugin().getId())) {
          if ((state == -1) || (item.getState() == state)) {
            return item.getPlugin();
          } else {
            // The plugin is present, but has the wrong state
            return null;
          }
        }
      }
      
      // Nothing found
      return null;
    }
  }
  

  /**
   * Gets the plugin with the given ID.
   * 
   * @param pluginId The ID of the wanted plugin.
   * @return The plugin with the given ID or <code>null</code> if no such plugin
   *         exists.
   */
  public PluginProxy getPluginForId(String pluginId) {
    return getPluginForId(pluginId, -1);
  }

  
  /**
   * Gets the activated plugin with the given ID.
   * 
   * @param pluginId The ID of the wanted plugin.
   * @return The plugin with the given ID or <code>null</code> if no such plugin
   *         exists or if the plugin is not activated.
   */
  public PluginAccess getActivatedPluginForId(String pluginId) {
    return getPluginForId(pluginId, ACTIVATED_STATE);
  }
  
  
  /**
   * Gets a list item for the given plugin proxy.
   * 
   * @param plugin The plugin to get the list item for.
   * @return The list item for the given plugin proxy.
   */
  private PluginListItem getItemForPlugin(PluginProxy plugin) {
    synchronized(mPluginList) {
      for (int i = 0; i < mPluginList.size(); i++) {
        PluginListItem item = (PluginListItem) mPluginList.get(i);
        
        if (item.getPlugin() == plugin) {
          return item;
        }
      }
    }
    
    // Nothing fonnd
    mLog.warning("Unkown plugin: " + plugin.getId());
    return null;
  }  


  /**
   * Sets the default context menu plugin.
   * 
   * @param plugin The plugin to set as default context menu plugin.
   */
  public void setDefaultContextMenuPlugin(PluginProxy plugin) {
    mDefaultContextMenuPlugin = plugin;
  }


  /**
   * Gets the default context menu plugin.
   * <p>
   * This is the plugin that should be executed by default when double-clicking
   * a program in the program table. It is shown with a bold font in the context
   * menu.
   * 
   * @return The default context menu plugin or <code>null</code> if there is no
   *         default context menu plugin defined.
   */
  public PluginProxy getDefaultContextMenuPlugin() {
    return mDefaultContextMenuPlugin;
  }
  
  
  /**
   * Creates a context menu for the given program containing all plugins.
   * 
   * @param program The program to create the context menu for
   * @return a context menu for the given program.
   */
  public static JPopupMenu createPluginContextMenu(Program program) {
    JPopupMenu menu = new JPopupMenu();
    PluginProxy defaultPlugin = getInstance().getDefaultContextMenuPlugin();
    PluginProxy[] pluginArr = getInstance().getActivatedPlugins();
    for (int i = 0; i < pluginArr.length; i++) {
      PluginProxy plugin = pluginArr[i];
      
      Action[] actionArr = plugin.getContextMenuActions(program);
      if (actionArr != null) {
        for (int j = 0; j < actionArr.length; j++) {
          Action action = actionArr[j];
          
          try {
            JMenuItem item = createPluginContextMenuItem(program, action);
            
            if (plugin == defaultPlugin) {
              item.setFont(CONTEXT_MENU_BOLDFONT);
            } else {
              item.setFont(CONTEXT_MENU_PLAINFONT);
            }

            menu.add(item);
          }
          catch (Throwable thr) {
            mLog.log(Level.WARNING, "Adding context menu item from plugin '"
                + plugin + "' failed", thr);
          }
        }
      }
    }

    return menu;
  }

  
  private static JMenuItem createPluginContextMenuItem(final Program program,
    final Action action)
  {
    String text = (String) action.getValue(Action.NAME);
    Icon icon = (Icon) action.getValue(Action.SMALL_ICON);
    
    JMenuItem item = new JMenuItem(text, icon);
    
    item.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        // Set the program as source
        evt.setSource(program);
        
        action.actionPerformed(evt);
      }
    });
    
    return item;
  }
  
  
  /**
   * Registers a PluginStateListener.
   * 
   * @param listener The PluginStateListener to register
   */
  public void addPluginStateListener(PluginStateListener listener) {
    synchronized(mPluginStateListenerList) {
      mPluginStateListenerList.add(listener);
    }
  }

  
  /**
   * Deregisters a PluginStateListener.
   * 
   * @param listener The PluginStateListener to deregister
   */
  public void removePluginStateListener(PluginStateListener listener) {
    synchronized(mPluginStateListenerList) {
      mPluginStateListenerList.remove(listener);
    }
  }

  
  /**
   * Tells all registered PluginStateListeners that a plugin was deactivated.
   * 
   * @param plugin The deactivated plugin
   */
  private void firePluginLoaded(PluginProxy plugin) {
    synchronized(mPluginStateListenerList) {
      for (int i = 0; i < mPluginStateListenerList.size(); i++) {
        PluginStateListener lst = (PluginStateListener) mPluginStateListenerList.get(i); 
        try {
          lst.pluginLoaded(plugin);
        } catch(Throwable thr) {
          mLog.log(Level.WARNING, "Fireing event 'plugin loaded' failed", thr);
        }
      }
    }
  }
    
  
  /**
   * Tells all registered PluginStateListeners that a plugin was deactivated.
   * 
   * @param plugin The deactivated plugin
   */
  private void firePluginUnloaded(PluginProxy plugin) {
    synchronized(mPluginStateListenerList) {
      for (int i = 0; i < mPluginStateListenerList.size(); i++) {
        PluginStateListener lst = (PluginStateListener) mPluginStateListenerList.get(i); 
        try {
          lst.pluginUnloaded(plugin);
        } catch(Throwable thr) {
          mLog.log(Level.WARNING, "Fireing event 'plugin unloaded' failed", thr);
        }
      }
    }
  }
  

  /**
   * Tells all registered PluginStateListeners that a plugin was deactivated.
   * 
   * @param plugin The deactivated plugin
   */
  private void firePluginActivated(PluginProxy plugin) {
    synchronized(mPluginStateListenerList) {
      for (int i = 0; i < mPluginStateListenerList.size(); i++) {
        PluginStateListener lst = (PluginStateListener) mPluginStateListenerList.get(i); 
        try {
          lst.pluginActivated(plugin);
        } catch(Throwable thr) {
          mLog.log(Level.WARNING, "Fireing event 'plugin activated' failed", thr);
        }
      }
    }
  }
  
  
  /**
   * Tells all registered PluginStateListeners that a plugin was deactivated.
   * 
   * @param plugin The deactivated plugin
   */
  private void firePluginDeactivated(PluginProxy plugin) {
    synchronized(mPluginStateListenerList) {
      for (int i = 0; i < mPluginStateListenerList.size(); i++) {
        PluginStateListener lst = (PluginStateListener) mPluginStateListenerList.get(i); 
        try {
          lst.pluginDeactivated(plugin);
        } catch(Throwable thr) {
          mLog.log(Level.WARNING, "Fireing event 'plugin deactivated' failed", thr);
        }
      }
    }    
  }

  
  /**
   * Calls for every active plugin the handleTvDataAdded(...) method, so the
   * plugin can react on the new data.
   *
   * @param newProg The added program
   * @see PluginProxy#handleTvDataAdded(ChannelDayProgram)
   */
  private void fireTvDataAdded(ChannelDayProgram newProg) {
    synchronized(mPluginList) {
      for (int i = 0; i < mPluginList.size(); i++) {
        PluginListItem item = (PluginListItem) mPluginList.get(i);
        if (item.getPlugin().isActivated()) {
          item.getPlugin().handleTvDataAdded(newProg);
        }
      }
    }
  }


  /**
   * Calls for every subscribed plugin the handleTvDataDeleted(...) method,
   * so the plugin can react on the deleted data.
   *
   * @param deletedProg The deleted program
   * @see PluginProxy#handleTvDataDeleted(ChannelDayProgram)
   */
  private void fireTvDataDeleted(ChannelDayProgram deletedProg) {
    synchronized(mPluginList) {
      for (int i = 0; i < mPluginList.size(); i++) {
        PluginListItem item = (PluginListItem) mPluginList.get(i);
        if (item.getPlugin().isActivated()) {
          item.getPlugin().handleTvDataDeleted(deletedProg);
        }
      }
    }
  }


  /**
   * Calls for every subscribed plugin the handleTvDataUpdateFinished() method,
   * so the plugin can react on the new data.
   *
   * @see PluginProxy#handleTvDataUpdateFinished()
   */
  private void fireTvDataUpdateFinished() {
    synchronized(mPluginList) {
      for (int i = 0; i < mPluginList.size(); i++) {
        PluginListItem item = (PluginListItem) mPluginList.get(i);
        if (item.getPlugin().isActivated()) {
          item.getPlugin().handleTvDataUpdateFinished();
        }
      }
    }
  }
  
  
  /**
   * A plugin in the plugin list
   */
  private class PluginListItem {
    
    /** The plugin */
    private AbstractPluginProxy mPlugin;
    
    /** The state of the plugin */
    private int mState;


    /**
     * Creates a new instance of PluginListItem.
     * 
     * @param plugin The plugin of this list item
     */
    public PluginListItem(AbstractPluginProxy plugin) {
      mPlugin = plugin;
      mState = LOADED_STATE;
    }
    
    
    /**
     * Gets the plugin.
     * 
     * @return the plugin.
     */
    public AbstractPluginProxy getPlugin() {
      return mPlugin;
    }
    
    
    /**
     * Sets the state of the plugin.
     * 
     * @param state The new state.
     */
    public void setState(int state) {
      mState = state;

      // Tell the plugin whether it is activated
      mPlugin.setActivated(state == ACTIVATED_STATE);
    }

    
    /**
     * Gets the state of the plugin.
     * 
     * @return The state of the plugin.
     */
    public int getState() {
      return mState;
    }
    
  } // inner class PluginListItem

}
