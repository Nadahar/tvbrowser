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

package devplugin;

import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvdataservice.TvDataService;
import util.exc.TvBrowserException;
import util.program.AbstractPluginProgramFormating;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import java.util.Iterator;

/**
 * The PluginManager provides some useful methods for a plugin.
 * More methods may follow in future versions.
 *
 * @author Martin Oberhauser
 */
public interface PluginManager {

  /** Specifies, that the search term has to match exactly. */
  public static final int SEARCHER_TYPE_EXACTLY = 1;
  /** Specifies, that the search term is a keyword (= substring). */
  public static final int SEARCHER_TYPE_KEYWORD = 2;
  /** Specifies, that the search term is a regular expression. */
  public static final int SEARCHER_TYPE_REGULAR_EXPRESSION = 3;
  /** Specifies, that the search term is a boolean expression. */
  public static final int SEARCHER_TYPE_BOOLEAN = 4;


  /**
   * Gets a program.
   *
   * @param date The date when the program is shown.
   * @param progID The ID of the program.
   * @return The program or <code>null</code> if there is no such program.
   */
  public Program getProgram(Date date, String progID);

  /**
   * Gets all channels the user has subscribed.
   *
   * @return all channels the user has subscribed.
   */
  public Channel[] getSubscribedChannels();

  /**
   * Gets an iterator through all programs of the specified channel at the
   * specified date.
   *
   * @param date The date of the programs.
   * @param channel The channel of the programs.
   * @return an Iterator for all programs of one day and channel or
   *         <code>null</code> if the requested data is not available.
   */
  public Iterator<Program> getChannelDayProgram(Date date, Channel channel);

  /**
   * Searches the TV data for programs which match a regular expression.
   *
   * @param regex The regular expression programs must match to.
   * @param inTitle Should be searched in the title?
   * @param inText Should be searched in the description?
   * @param caseSensitive Should the search be case sensitive?
   * @param channels The channels to search in.
   * @param startDate The date to start the search.
   * @param nrDays The number of days to include after the start date. If
   *        negative the days before the start date are used.
   * @throws TvBrowserException If there is a syntax error in the regular expression.
   * @return The matching programs.
   *
   * @deprecated Use {@link #createProgramSearcher(int, String, boolean)}
   *             instead.
   */
  public Program[] search(String regex, boolean inTitle, boolean inText,
                          boolean caseSensitive, Channel[] channels, Date startDate,
                          int nrDays)
    throws TvBrowserException;

  /**
   * Searches the TV data base for programs that match a regular expression.
   *
   * @param regex The regular expression programs must match to.
   * @param caseSensitive Should the search be case sensitive?
   * @param fieldArr The fields to search in
   * @param startDate The date to start the search.
   * @param nrDays The number of days to include after the start date. If
   *        negative the days before the start date are used.
   * @param channels The channels to search in.
   * @param sortByStartTime Should the results be sorted by the start time?
   *        If not, the results will be grouped by date and channel and the
   *        search will be faster.
   * @return The matching programs.
   *
   * @deprecated Since 1.1. Use {@link #createProgramSearcher(int, String, boolean)}
   *             instead.
   * @throws util.exc.TvBrowserException Error while searching
   */
  public Program[] search(String regex, boolean caseSensitive,
                          ProgramFieldType[] fieldArr, Date startDate, int nrDays, Channel[] channels,
                          boolean sortByStartTime)
    throws TvBrowserException;


  /**
   * Creates a ProgramSearcher.
   *
   * @param type The searcher type to create. Must be one of
   *        {@link #SEARCHER_TYPE_EXACTLY}, {@link #SEARCHER_TYPE_KEYWORD},
   *        {@link #SEARCHER_TYPE_REGULAR_EXPRESSION} or
   *        {@link #SEARCHER_TYPE_BOOLEAN}.
   * @param searchTerm The search term the searcher should look for.
   * @param caseSensitive Specifies whether the searcher should be case sensitive.
   * @return A program searcher.
   * @throws TvBrowserException If creating the program searcher failed.
   */
  public ProgramSearcher createProgramSearcher(int type, String searchTerm,
                                               boolean caseSensitive)
      throws TvBrowserException;


  /**
   * Returns all activated Plugins.
   *
   * @return all activated Plugins.
   * @since 1.1
   */
  public PluginAccess[] getActivatedPlugins();

  /**
   * Gets the ID of the given Java plugin.
   *
   * @param javaPlugin The Java plugin to get the ID for.
   * @return The ID of the given Java plugin.
   */
  public String getJavaPluginId(Plugin javaPlugin);

  /**
   * Gets the activated plugin with the given ID.
   *
   * @param pluginId The ID of the wanted plugin.
   * @return The plugin with the given ID or <code>null</code> if no such plugin
   *         exists or if the plugin is not activated.
   */
  public PluginAccess getActivatedPluginForId(String pluginId);

  /**
   * Returns a list of all installed Plugins.
   * <p>
   * This method always returns an empty array! Use
   * {@link #getActivatedPlugins()} instead!
   *
   * @return An empty array!
   *
   * @deprecated Since 1.1. Use {@link #getActivatedPlugins()} instead.
   */
  public Plugin[] getInstalledPlugins();

  /**
   * Gets a TvDataService for a class name.
   *
   * @deprecated
   *
   * @param dataServiceClassName the class name of the wanted TvDataService.
   * @return The TvDataService or <code>null</code> if there is no such
   *         TvDataService.
   */
  public TvDataService getDataService(String dataServiceClassName);

  public TvDataServiceProxy getDataServiceProxy(String id);

  /**
   * Creates a context menu for the given program containing all plugins.
   *
   * @param program The program to create the context menu for
   * @param caller The calling plugin.
   * @return a context menu for the given program.
   */
  public JPopupMenu createPluginContextMenu(Program program, ContextMenuIf caller);
  
  /**
   * Returns an array of all available filters.
   *
   * @return An array of all available filters.
   * @since 0.9.7.4
   * @deprecated Since 2.5 Use {@link devplugin.FilterManager#getAvailableFilters()} instead.
   */
  public ProgramFilter[] getAvailableFilters();


  /**
   * Returns an example program. You can use it for preview stuff.
   *
   * @return an example program.
   * @since 0.9.7.4
   */
  public Program getExampleProgram();

  /**
   * Handles a single left click on a program.
   * <p>
   * Executes the single left click context menu function. Plugins should use
   * handleProgramDoubleClick(Program program, Plugin caller). It prevents the
   * Plugin to be activated a second time.
   *
   * @param program The program to pass to the single left click context menu function.
   *
   * @since 2.7
   */
  public void handleProgramSingleClick(Program program);


  /**
   * Handles a single left click on a program.
   * <p>
   * Executes the default context menu plugin.
   *
   * @param program The program to pass to the default context menu function.
   * @param caller The ContextMenuIf that calls this. Prevents the ContextMenuIf
   *        to be activated twice
   *
   * @since 2.7
   */
  public void handleProgramSingleClick(Program program, ContextMenuIf caller);
  
  /**
   * Handles a double click on a program.
   * <p>
   * Executes the default context menu plugin. Plugins should use
   * handleProgramDoubleClick(Program program, Plugin caller). It prevents the
   * Plugin to be activated a second time.
   *
   * @param program The program to pass to the default context menu plugin.
   *
   * @since 1.1
   */
  public void handleProgramDoubleClick(Program program);


  /**
   * Handles a double click on a program.
   * <p>
   * Executes the default context menu plugin.
   *
   * @param program The program to pass to the default context menu plugin.
   * @param caller The ContextMenuIf that calls this. Prevents the ContextMenuIf
   *        to be activated twice
   *
   * @since 1.1
   */
  public void handleProgramDoubleClick(Program program, ContextMenuIf caller);

  
  /**
   * Handles a middle click on a program.
   * <p>
   * Executes the middle click context menu plugin. Plugins should use
   * handleProgramMiddleClick(Program program, Plugin caller). It prevents the
   * Plugin to be activated a second time.
   *
   * @param program The program to pass to the middle click context menu plugin.
   *
   * @since 1.1
   */
  public void handleProgramMiddleClick(Program program);


  /**
   * Handles a middle click on a program.
   * <p>
   * Executes the middle click context menu plugin.
   *
   * @param program The program to pass to the middle click context menu plugin.
   * @param caller The ContextMenuIf that calls this. Prevents the ContextMenuIf
   *        to be activated twice.
   *
   * @since 1.1
   */
  public void handleProgramMiddleClick(Program program, ContextMenuIf caller);
  
  /**
   * Returns some settings a plugin may need.
   *
   * @return Some settings a plugin may need.
   */
  public TvBrowserSettings getTvBrowserSettings();

  /**
   * Returns an Icon from the Icon-Theme-System
   *  
   * If your Plugin has Icons that are not available as Icons within an Theme, you can add
   * your Icons into your Jar-File.
   * 
   * The Directory-Structure must be like this:
   * 
   * [PackageOfYourPlugin]/icons/[Size]x[Size]/[category]/[icon].png
   * 
   * Please try to use the FreeDesktop-Icon Naming Conventions
   * http://cvs.freedesktop.org/[*]checkout[*]/icon-theme/default-icon-theme/spec/icon-naming-spec.xml
   * (please remove the [ ])
   *  
   * @param plugin Plugin that wants to load an Icon
   * @param category Category of the Icon (Action, etc...) 
   * @param icon Icon-Name without File-Extension
   * @param size Size of the Icon
   * @return Icon if found, null if not
   * @since 2.2
   */
  public ImageIcon getIconFromTheme(Plugin plugin, String category, String icon, int size);
  
  /**
   * Returns an Icon from the Icon-Theme-System
   *  
   * If your Plugin has Icons that are not available as Icons within an Theme, you can add
   * your Icons into your Jar-File.
   * 
   * The Directory-Structure must be like this:
   * 
   * [PackageOfYourPlugin]/icons/[Size]x[Size]/[category]/[icon].png
   * 
   * Please try to use the FreeDesktop-Icon Naming Conventions
   * http://cvs.freedesktop.org/[*]checkout[*]/icon-theme/default-icon-theme/spec/icon-naming-spec.xml
   * (please remove the [ ])
   *  
   * @param plugin Plugin that wants to load an Icon
   * @param icon Icon in the Icon-Theme
   * @return Icon if found, null if not
   * @since 2.2
   */
  public ImageIcon getIconFromTheme(Plugin plugin, ThemeIcon icon);

  /**
   * Show the Settings-Dialog for a Plugin
   * 
   * @param plugin Use this Plugin
   * @since 2.2
   */
  public void showSettings(Plugin plugin);

  /**
   * Show the Settings-Dialog with a Specific SettingsItem
   * 
   * @param settingsItem SettingsItem to show (e.g. SettingsItem.CHANNELS)
   * @since 2.2
   */
  public void showSettings(String settingsItem);
  
  
  /**
   * Return all marked programs.
   * 
   * @return The marked programs
   * @since 2.2
   */
  public Program[] getMarkedPrograms();
  
  /**
   * Return all Plugins/Functions that are able to receive programs.
   * 
   * @return The ProgramReceiveIfs.
   * @since 2.5
   */
  public ProgramReceiveIf[] getReceiveIfs();  
  
  /**
   * Return all Plugins/Functions that are able to receive programs.
   * 
   * @param caller The caller ProgramReceiveIf.
   * @param callerTarget The target that calls the receive if array.
   * @return The ProgramReceiveIfs.
   * @since 2.5
   */
  public ProgramReceiveIf[] getReceiveIfs(ProgramReceiveIf caller, ProgramReceiveTarget callerTarget);
  
  /**
   * Return the ReceiveIfFor given id or <code>null</code> if there is
   * no ReceiveIf for the given id.
   * 
   * @param id The id of the ReceiveIf.
   * 
   * @return The ReceiveIf with the given id or <code>null</code>
   * @since 2.5
   */
  public ProgramReceiveIf getReceiceIfForId(String id);
  
  /**
   * Let TVB scroll to the given program.
   * 
   * @param program The program to scroll to.
   * @since 2.5
   */
  public void scrollToProgram(Program program);
  
  /**
   * Let TVB scroll to the given time.
   * 
   * @param time The time to scroll to in minutes.
   * @since 2.5
   */
  public void scrollToTime(int time);
  
  /**
   * Let TVB scroll to the given channel.
   * 
   * @param channel The channel to scroll to.
   * @since 2.5
   */
  public void scrollToChannel(Channel channel);
  
  /**
   * Let TVB change the date to the given date.
   * 
   * @param date The date to show the program for.
   * @since 2.5
   */
  public void goToDate(Date date);
  
  /**
   * Returns the filter manager of TV-Browser.
   * With the filter manager you get access to the filter
   * system of TV-Browser. You can add or remove filters
   * of you plugin and switch the current used filter.
   * <p>
   * Don't use this method until TV-Browser is fully loaded.
   * <p>
   * @return  The filter manager of TV-Browser or <code>null</code> if TV-Browser isn't fully loaded.
   * @since 2.5
   */
  public FilterManager getFilterManager();
  
  /**
   * Gets the available global program configurations.
   * <p>
   * @return The available global program configurations.
   * @since 2.5.1
   */
  public AbstractPluginProgramFormating[] getAvailableGlobalPuginProgramFormatings();

  /**
   * Get the date currently shown in the program table.
   * 
   * @return date currently shown in program table
   * @since 2.6
   */
  public Date getCurrentDate();

  /**
   * Gets all ProgramRatingIfs of all plugins. You can get all available ratings for                  ~
   * one program.
   *
   * @return all ProgramRatingIfs of all plugins
   * @since 2.7
   */
  public ProgramRatingIf[] getAllProgramRatingIfs();
}