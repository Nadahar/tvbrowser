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
package tvbrowser.core.plugin;

import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.TvDataBaseListener;
import tvbrowser.core.TvDataUpdateListener;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.extras.programinfo.ProgramInfo;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.menu.MenuUtil;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Date;
import devplugin.Plugin;
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

  /** The name of the directory where the plugins are located in TV-Browser 2.01 and before */
  public static String PLUGIN_DIRECTORY = "plugins";

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
 // private static Font CONTEXT_MENU_PLAINFONT = new Font("Dialog", Font.PLAIN, 12);

  /**
   * The font to use in the context menu for the default plugin.
   *
   * @see #createPluginContextMenu(Program)
   */
 // private static Font CONTEXT_MENU_BOLDFONT = new Font("Dialog", Font.BOLD, 12);

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

  /**
   * The plugin that should be executed by default when middle-clicking a
   * program in the program table.
   */
  private PluginProxy mDefaultMiddleClickPlugin;

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

    devplugin.Plugin.setPluginManager(PluginManagerImpl.getInstance());
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



  public void registerPlugin(AbstractPluginProxy plugin) {
    // Add it to the list
    mPluginList.add(new PluginListItem(plugin));
    firePluginLoaded(plugin);

          // Clear the cache
    mAllPluginCache = null;
  }

  /**
   * This method must be called on start-up.
   *
   * @throws TvBrowserException If initialization failed.
   */
  public void init() throws TvBrowserException {
    // Install the pending plugins
  //  installPendingPlugins();

    // Load all plugins
  //  loadAllPlugins();

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

        // Convert the middle click context menu plugin from class name to ID
        String middleClickPluginClassName = Settings.propMiddleClickPlugin.getString();
        Settings.propMiddleClickPlugin.setString("java." + middleClickPluginClassName);
      }
    }

    // Set the plugin order
    setPluginOrder(pluginOrderArr);

    // Activate all plugins except the ones that are explicitely deactivated
    activateAllPluginsExcept(deactivatedPluginArr);

    // Get the default context menu plugin
    String id = Settings.propDefaultContextMenuPlugin.getString();
    PluginProxy plugin = getPluginForId(id);
    if (plugin == null) {
      plugin = getPluginForId(Settings.propDefaultContextMenuPlugin.getDefault());
      if (plugin != null) {
        Settings.propDefaultContextMenuPlugin.setString(plugin.getId());
      }
    }
    setDefaultContextMenuPlugin(plugin);

    // Get the middle click context menu plugin
    id = Settings.propMiddleClickPlugin.getString();
    plugin = getPluginForId(id);
    if (plugin == null) {
      plugin = getPluginForId(Settings.propMiddleClickPlugin.getDefault());
      if (plugin != null) {
        Settings.propMiddleClickPlugin.setString(plugin.getId());
      }
    }
    setMiddleClickPlugin(plugin);
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

    // Tell the plugin that we activate it now
    item.getPlugin().onActivation();

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
      deactivatePlugin(item, true);
    }
  }


  /**
   * Deactivates a plugin.
   *
   * @param item The item of the plugin to deactivate
   * //@throws TvBrowserException If deactivating failed
   */
  private void deactivatePlugin(PluginListItem item, boolean log) throws TvBrowserException {
    // Check the state
    checkStateChange(item, ACTIVATED_STATE, LOADED_STATE);

    // Log this event
    if(log)
      mLog.info("Deactivating plugin " + item.getPlugin().getId());

    // Get the user directory
    String userDirectoryName = Settings.getUserDirectoryName();
    File userDirectory = new File(userDirectoryName);

    // Create the user directory if it does not exist
    if (! userDirectory.exists()) {
      userDirectory.mkdirs();
    }

    // Try to save the plugin settings. If saving fails, we continue deactivating the plugin.
    try {
      item.getPlugin().saveSettings(userDirectory, log);
    }catch (TvBrowserException e) {
      ErrorHandler.handle(e);
    }

    // Tell the plugin that we deactivate it now
    item.getPlugin().onDeactivation();

    // Set the plugin active
    item.setState(LOADED_STATE);

    // Clear the activated plugins cache
    mActivatedPluginCache = null;

    final PluginProxy plugin = item.getPlugin();
    
    // Run through all Programs and umark this Plugin
    new Thread(){
      public void run() {
        setPriority(Thread.MIN_PRIORITY);

        Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();

        Date date = new Date();
        
        int daysWithoutData = 0;
        
        while (daysWithoutData < 10) {
            for (int i = 0; i < channels.length; i++) {
                Iterator it = Plugin.getPluginManager().getChannelDayProgram(date, channels[i]);
                
                if ((it == null) || (!it.hasNext())) {
                  daysWithoutData++;
                } else {
                  daysWithoutData = 0;
                  while ((it != null) && (it.hasNext())) {
                    Program program = (Program) it.next();
                    program.unmark(plugin);
                  }
                }
                
            }

            date = date.addDays(1);
        }
      };
    }.start();
    
    
    // Inform the listeners
    firePluginDeactivated(item.getPlugin(), log);
  }

  
  /**
   * Deactivates a plugin and removes it from the PluginList
   *
   * @param plugin The plugin to deactivate
   * @throws TvBrowserException If deactivating failed
   */
  public void removePlugin(PluginProxy plugin) throws TvBrowserException {
    PluginListItem item = getItemForPlugin(plugin);
    if (item != null) {
      deactivatePlugin(item, true);
      mPluginList.remove(item);
      mActivatedPluginCache = null;
      mAllPluginCache = null;
    }
  }

  /**
   * Deactivates and shuts down all plugins.
   */
  public void shutdownAllPlugins(boolean log) {
    synchronized(mPluginList) {
      // Deactivate all active plugins
      for (int i = 0; i < mPluginList.size(); i++) {
        PluginListItem item = (PluginListItem) mPluginList.get(i);
        if (item.getPlugin().isActivated()) {
          try {
            deactivatePlugin(item, log);
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
        if(log)
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

    // Return only a copy so the array may be changed by the caller
    return (PluginProxy[]) allPluginArr.clone();
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

    // Return only a copy so the array may be changed by the caller
    return (PluginProxy[]) activatedPluginArr.clone();
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
   * Sets the middle click context menu plugin.
   *
   * @param plugin The plugin to set as middle click context menu plugin.
   */
  public void setMiddleClickPlugin(PluginProxy plugin) {
    mDefaultMiddleClickPlugin = plugin;
  }


  /**
   * Gets the middle click context menu plugin.
   * <p>
   * This is the plugin that should be executed by default when middle-clicking
   * a program in the program table. It is shown with an italic font in the context
   * menu.
   *
   * @return The middle click context menu plugin or <code>null</code> if there is no
   *         middle click context menu plugin defined.
   */
  public PluginProxy getMiddleClickPlugin() {
    return mDefaultMiddleClickPlugin;
  }

   /*
  private static JMenuItem getMenuItem(ActionMenu menu) {
    JMenuItem result;
    if (menu.hasSubItems()) {
      result = new JMenu(menu.getAction());
      ActionMenu[] subItems = menu.getSubItems();
      for (int i=0; i<subItems.length; i++) {
        result.add(getMenuItem(subItems[i]));
      }
    }
    else {
      if (menu.isSelected()) {
        result = new JCheckBoxMenuItem(menu.getAction().getValue(Action.NAME).toString(), true);
      }
      else {
        result = new JMenuItem(menu.getAction());
      }
    }
    result.setFont(CONTEXT_MENU_PLAINFONT);
    return result;
  }
        */

  /**
   * Creates the MenuItems for the ContextMenus
   */
  public static JMenuItem[] createPluginContextMenuItems(Plugin callerplugin, Program program, boolean markDefaultPlugin) {
    ArrayList items = new ArrayList();
    PluginProxy defaultPlugin = getInstance().getDefaultContextMenuPlugin();
    PluginProxy middleClickPlugin = getInstance().getMiddleClickPlugin();
    PluginProxy[] pluginArr = getInstance().getActivatedPlugins();

    JMenuItem mi = MenuUtil.createMenuItem(ProgramInfo.getInstance().getContextMenuActions(program));
    if (mi != null) {
      items.add(mi);
    }

    mi = MenuUtil.createMenuItem(FavoritesPlugin.getInstance().getContextMenuActions(null, program));
    if (mi != null) {
      items.add(mi);
    }

    mi = MenuUtil.createMenuItem(ReminderPlugin.getInstance().getContextMenuActions(null, program));
    if (mi != null) {
      items.add(mi);
    }


    for (int i = 0; i < pluginArr.length; i++) {
      PluginProxy plugin = pluginArr[i];

      boolean equalsPlugin = false;

      if ((callerplugin != null) && (callerplugin.getId().equals(plugin.getId()))) {
        equalsPlugin = true;
      }

      if (!equalsPlugin) {
        ActionMenu actionMenu = plugin.getContextMenuActions(program);
        if (actionMenu != null) {
          JMenuItem menuItem = MenuUtil.createMenuItem(actionMenu);
          items.add(menuItem);
          if (plugin == defaultPlugin && plugin == middleClickPlugin && markDefaultPlugin) {
            if (!actionMenu.hasSubItems() && actionMenu.getAction() != null) {
              menuItem.setFont(MenuUtil.CONTEXT_MENU_BOLDITALICFONT);
            }
          }
          else if (plugin == defaultPlugin && markDefaultPlugin) {
            if (!actionMenu.hasSubItems() && actionMenu.getAction() != null) {
              menuItem.setFont(MenuUtil.CONTEXT_MENU_BOLDFONT);
            }
          }
          else if (plugin == middleClickPlugin && markDefaultPlugin) {
            if (!actionMenu.hasSubItems() && actionMenu.getAction() != null) {
              menuItem.setFont(MenuUtil.CONTEXT_MENU_ITALICFONT);
            }
          }
        }
      }

     /*
      if (actionArr != null) {
        for (int j = 0; j < actionArr.length; j++) {
          Action action = actionArr[j];
          try {
            JMenuItem item = createPluginContextMenuItem(program, action);

            if (plugin == defaultPlugin && markDefaultPlugin) {
              item.setFont(CONTEXT_MENU_BOLDFONT);
            } else {
              item.setFont(CONTEXT_MENU_PLAINFONT);
            }
            items.add(item);
          }
          catch (Throwable thr) {
            mLog.log(Level.WARNING, "Adding context menu item from plugin '"
              + plugin + "' failed", thr);
          }
        }
      }   */
    }

    JMenuItem[] result = new JMenuItem[items.size()];
    items.toArray(result);
    return result;

  }

  /**
   * Creates a context menu for the given program containing all plugins.
   *
   * @param program The program to create the context menu for
   * @return a context menu for the given program.
   */

  public static JPopupMenu createPluginContextMenu(Program program) {
    return createPluginContextMenu(program, null);
  }

  /**
   * Creates a context menu for the given program containing all plugins.
   *
   * @param program The program to create the context menu for
   * @param plugin The Plugin that wants to create the ContextMenu
   * @return a context menu for the given program.
   */

  public static JPopupMenu createPluginContextMenu(Program program, Plugin plugin) {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem[] items = createPluginContextMenuItems(plugin, program, true);
    for (int i=0; i<items.length; i++) {
      menu.add(items[i]);
    }
    return menu;
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
  private void firePluginDeactivated(PluginProxy plugin, boolean log) {
    synchronized(mPluginStateListenerList) {
      for (int i = 0; i < mPluginStateListenerList.size(); i++) {
        PluginStateListener lst = (PluginStateListener) mPluginStateListenerList.get(i);
        try {
          lst.pluginDeactivated(plugin);
        } catch(Throwable thr) {
          if(log)
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