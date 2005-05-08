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

import java.util.Iterator;

import javax.swing.JPopupMenu;

import tvdataservice.TvDataService;
import util.exc.TvBrowserException;

/**
 * The PluginManager provides some usefull methods for a plugin. 
 * More methods may follow in future versions.
 *
 * @author Martin Oberhauser
 */
public interface PluginManager {
  
  /** Specifies, that the search term has to match exacly. */
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
  public Iterator getChannelDayProgram(Date date, Channel channel);

  /**
   * Searches the TV data for programs which match a regular expression.
   *
   * @param regex The regular expression programs must match to.
   * @param inTitle Should be searched in the title?
   * @param inText Should be searched in the desription?
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
   * @throws TvBrowserException
   * @throws TvBrowserException If there is a syntax error in the regular expression.
   * 
   * @deprecated Since 1.1. Use {@link #createProgramSearcher(int, String, boolean)}
   *             instead.
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
   * @param dataServiceClassName the class name of the wanted TvDataService.
   * @return The TvDataService or <code>null</code> if there is no such
   *         TvDataService. 
   */
  public TvDataService getDataService(String dataServiceClassName);

  /**
   * Creates a context menu for the given program containing all plugins.
   * 
   * @param program The program to create the context menu for
   * @param caller The calling plugin.
   * @return a context menu for the given program.
   */
  public JPopupMenu createPluginContextMenu(Program program, Plugin caller);

  /**
   * Returns an array of all available filters.
   * 
   * @return An array of all available filters.
   * @since 0.9.7.4
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
   * @param caller Plugin that calls this. Prevents the Plugin to be activated twice
   * 
   * @since 1.1
   */
  public void handleProgramDoubleClick(Program program, Plugin caller);


  /**
   * Returns some settings a plugin may need.
   * 
   * @return Some settings a plugin may need.
   */
  public TvBrowserSettings getTvBrowserSettings();

}