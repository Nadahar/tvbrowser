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

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.TimeZone;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import tvbrowser.core.ChannelList;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.TvDataSearcher;
import tvbrowser.core.TvDataServiceManager;
import tvbrowser.core.filters.FilterList;
import tvdataservice.MutableProgram;
import tvdataservice.TvDataService;
import util.exc.TvBrowserException;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.PluginManager;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramFilter;

/**
 * The implementation of the PluginManager interface. This class is the
 * connection for the plugins to TV-Browser.
 *
 * @author Til Schneider, www.murfman.de
 */
public class PluginManagerImpl implements PluginManager {
  
  /** The available filters. */
  private ProgramFilter[] mAvailableFilters;
  
  /** An example program. */
  private Program mExampleProgram;

  public PluginManagerImpl() {

  }
  
  /**
   * Gets a program.
   * 
   * @param date The date when the program is shown.
   * @param progID The ID of the program.
   * @return The program or <code>null</code> if there is no such program.
   */
  public Program getProgram(Date date, String progID) {
    TvDataBase db = TvDataBase.getInstance();

    Channel[] channels = ChannelList.getAvailableChannels();
    for (int i = 0; i < channels.length; i++) {
      ChannelDayProgram dayProg = db.getDayProgram(date, channels[i]);
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
   * Gets all channels the user has subscribed.
   * 
   * @return all channels the user has subscribed.
   */
  public Channel[] getSubscribedChannels() {
    return ChannelList.getSubscribedChannels();
  }


  /**
   * Gets an iterator through all programs of the specified channel at the
   * specified date.
   * 
   * @param date The date of the programs.
   * @param channel The channel of the programs.
   * @return an Iterator for all programs of one day and channel or
   *         <code>null</code> if the requested data is not available.
   */
  public Iterator getChannelDayProgram(Date date, Channel channel) {
    ChannelDayProgram channelDayProgram = TvDataBase.getInstance()
        .getDayProgram(date, channel);
    if (channelDayProgram == null) {
      return null;
    }
    return channelDayProgram.getPrograms();
  }


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
   * @deprecated Use {@link #search(String, boolean, ProgramFieldType[], Date, int, Channel[], boolean)}
   *             instead.
   */
  public Program[] search(String regex, boolean inTitle, boolean inText,
    boolean caseSensitive, Channel[] channels, devplugin.Date startDate,
    int nrDays) throws TvBrowserException
  {
    return TvDataSearcher.getInstance().search(regex, inTitle, inText,
        caseSensitive, channels, startDate, nrDays);
  }


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
   */
  public Program[] search(String regex, boolean caseSensitive,
    ProgramFieldType[] fieldArr, devplugin.Date startDate, int nrDays,
    Channel[] channels, boolean sortByStartTime) throws TvBrowserException
  {
    return TvDataSearcher.getInstance().search(regex, caseSensitive, fieldArr,
        startDate, nrDays, channels, sortByStartTime);
  }


  /**
   * Returns all activated Plugins.
   * 
   * @return all activated Plugins.
   * @since 1.1
   */
  public PluginAccess[] getActivatedPlugins() {
    return PluginProxyManager.getInstance().getActivatedPlugins();
  }


  /**
   * Gets the ID of the given Java plugin.
   * 
   * @param javaPlugin The Java plugin to get the ID for.
   * @return The ID of the given Java plugin.
   */
  public String getJavaPluginId(Plugin javaPlugin) {
    return JavaPluginProxy.getJavaPluginId(javaPlugin);
  }

  
  /**
   * Gets the activated plugin with the given ID.
   * 
   * @param pluginId The ID of the wanted plugin.
   * @return The plugin with the given ID or <code>null</code> if no such plugin
   *         exists or if the plugin is not activated.
   */
  public PluginAccess getActivatedPluginForId(String pluginId) {
    return PluginProxyManager.getInstance().getActivatedPluginForId(pluginId);
  }
  
  
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
  public Plugin[] getInstalledPlugins() {
    return new Plugin[0];
  }
  

  /**
   * Gets a TvDataService for a class name.
   *
   * @param dataServiceClassName the class name of the wanted TvDataService.
   * @return The TvDataService or <code>null</code> if there is no such
   *         TvDataService. 
   */
  public TvDataService getDataService(String dataServiceClassName) {
    return TvDataServiceManager.getInstance().getDataService(dataServiceClassName);
  }


  /**
   * Creates a context menu for the given program containing all plugins.
   * 
   * @param program The program to create the context menu for
   * @param caller The calling plugin.
   * @return a context menu for the given program.
   */
  public JPopupMenu createPluginContextMenu(Program program, Plugin caller) {
    return PluginProxyManager.createPluginContextMenu(program, caller);
  }


  /**
   * Returns an array of all available filters.
   * 
   * @return An array of all available filters.
   * @since 0.9.7.4
   */
  public ProgramFilter[] getAvailableFilters() {
    if (mAvailableFilters == null) {
      FilterList filterList = FilterList.getInstance();
      mAvailableFilters = filterList.getFilterArr();
    }
    
    return mAvailableFilters;
  }


  /**
   * Returns an example program. You can use it for preview stuff.
   * 
   * @return an example program.
   * @since 0.9.7.4
   */
  public Program getExampleProgram() {
    if (mExampleProgram == null) {
      // TODO: interationalize

      Channel exampleChannel = new Channel(null, "Channel 1",
          TimeZone.getDefault(), "de", "");
      
      MutableProgram prog = new MutableProgram(exampleChannel,
                                               Date.getCurrentDate(), 14, 45);
      prog.setTitle("Die Waltons");
      prog.setShortInfo("Die Verfilmung der Kindheits- und Jugenderinnerungen des Romanschriftstellers Earl Hamner jr.");
      prog.setDescription("Olivia ist schon seit einigen Tagen niedergeschlagen, obwohl ihr Geburtstag bevorsteht. Ihre einzige Freude scheint das Postflugzeug zu sein, dem sie allabendlich von der Haust\u00FCr aus sehnsuchtsvoll hinterhersieht.");
      prog.setTextField(ProgramFieldType.SHOWVIEW_NR_TYPE, "123-456");
      prog.setTextField(ProgramFieldType.ACTOR_LIST_TYPE,
                        "Ralph Waite (Vater John Walton), Mary McDonough (Erin Walton), Michael Learned (Mutter Olivia Walton), Kami Cotler (Elisabeth Walton), Jon Walmsley (Jason Walton), Ellen Corby (Gro\u00dfmutter Ester Walton), David Harper (Jim Bob Walton), Judy Taylor (Mary Ellen Walton), Richard Thomas (John-Boy Walton)");
      prog.setIntField(ProgramFieldType.AGE_LIMIT_TYPE, 6);
      prog.setTextField(ProgramFieldType.EPISODE_TYPE, "Der Postflieger");
      prog.setTextField(ProgramFieldType.GENRE_TYPE, "Familie");
      prog.setTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE, "Air Mail Man");
      prog.setTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE, "The Waltons");
      prog.setTextField(ProgramFieldType.ORIGIN_TYPE, "USA");
      prog.setIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE, 1972);
      prog.setTextField(ProgramFieldType.REPETITION_OF_TYPE, "Wh von gestern, 8:00");
      //prog.setTextField(ProgramFieldType.SCRIPT_TYPE,"");
      prog.setIntField(ProgramFieldType.NET_PLAYING_TIME_TYPE, 45);
      prog.setTimeField(ProgramFieldType.END_TIME_TYPE, 15 * 60 + 45);
      prog.setTextField(ProgramFieldType.URL_TYPE, "http://www.thewaltons.com");
      prog.setTimeField(ProgramFieldType.VPS_TYPE, 14 * 60 + 45);
      prog.setInfo(Program.INFO_AUDIO_TWO_CHANNEL_TONE
                   | Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED);
      
      mExampleProgram = prog;
    }

    return mExampleProgram;
  }

  
  /**
   * Handles a double click on a program.
   * <p>
   * Executes the default context menu plugin. Plugins should use 
   * handleProgramDoubleClick(Program program, Plugin caller). It prevetns the 
   * Plugin to be activated a second time. 
   * 
   * @param program The program to pass to the default context menu plugin.
   * 
   * @since 1.1
   */
  public void handleProgramDoubleClick(Program program) {
    handleProgramDoubleClick(program, null);
  }
  
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
  public void handleProgramDoubleClick(Program program, Plugin caller) {
    if (program == null) {
      // Nothing to do
      return;
    }
    
    PluginAccess defaultContextMenuPlugin
      = PluginProxyManager.getInstance().getDefaultContextMenuPlugin();
    
    if ((caller != null)  && (defaultContextMenuPlugin.getId().equals(caller.getId()))) {
      return;
    }
    
    if (defaultContextMenuPlugin != null) {
      ActionMenu menu = defaultContextMenuPlugin.getContextMenuActions(program);
      while (menu != null && menu.hasSubItems()) {
        ActionMenu[] subItems = menu.getSubItems();
        if (subItems.length>0) {
          menu = subItems[0];
        }
        else {
          menu = null;
        }
      }
      if (menu == null) {
        return;
      }

      Action action = menu.getAction();

      if (action != null) {
        ActionEvent evt = new ActionEvent(program, 0, null);
        action.actionPerformed(evt);
      }

    }
  }
  

  /**
   * Gets the plugin that is used as default in the context menu.
   * 
   * @return the default context menu plugin.
   * @since 1.1
   */
  public PluginAccess getDefaultContextMenuPlugin() {
    return PluginProxyManager.getInstance().getDefaultContextMenuPlugin();
  }
   
}