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

import java.awt.Color;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

import tvbrowser.TVBrowser;
import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.programformating.GlobalPluginProgramFormatingManager;
import tvbrowser.core.search.booleansearch.BooleanSearcher;
import tvbrowser.core.search.booleansearch.ParserException;
import tvbrowser.core.search.regexsearch.RegexSearcher;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.SettingsDialog;
import tvdataservice.MarkedProgramsList;
import tvdataservice.MutableProgram;
import util.exc.TvBrowserException;
import util.program.AbstractPluginProgramFormating;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.ContextMenuIf;
import devplugin.Date;
import devplugin.FilterManager;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.PluginManager;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramRatingIf;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;
import devplugin.ProgramSearcher;
import devplugin.ThemeIcon;
import devplugin.TvBrowserSettings;

/**
 * The implementation of the PluginManager interface. This class is the
 * connection for the plugins to TV-Browser.
 *
 * @author Til Schneider, www.murfman.de
 */
public class PluginManagerImpl implements PluginManager {

  public static class RatingFieldRating implements ProgramRatingIf {

    @Override
    public Icon getIcon() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Icon getIconForProgram(final Program program) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getName() {
      return "TV data rating";
    }

    @Override
    public int getRatingForProgram(final Program program) {
      int rating = program.getIntField(ProgramFieldType.RATING_TYPE);
      if (rating >= 0) {
        return rating;
      }
      return 0;
    }

    @Override
    public boolean hasDetailsDialog() {
      return false;
    }

    @Override
    public void showDetailsFor(Program p) {
      // TODO Auto-generated method stub
    }

  }

  /** An example program. */
  private Program mExampleProgram;

   /** The logger for this class */
  private static java.util.logging.Logger mLog
    = Logger.getLogger(PluginManagerImpl.class.getName());

  private static PluginManagerImpl mInstance;
  
  private boolean mTvBrowserStartFinished = false;

  /**
   * Creates a new instance of PluginManagerImpl.
   */
  private PluginManagerImpl() {
  }

  /**
   * @return The instance of this class.
   */
  /**
   * Returns the instance of this class, if
   * there is no instance it is created first.
   * 
   * @return The instance of this class.
   */
  public static PluginManager getInstance() {
    if (mInstance == null) {
      mInstance = new PluginManagerImpl();
    }
    return mInstance;
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
    
    Channel ch = getChannelFromProgId(progID);
    if (ch != null && ChannelList.isSubscribedChannel(ch)) {
      int index = progID.lastIndexOf('_');
      String timeString = progID.substring(index + 1);
      int hourIndex = timeString.indexOf(':');
      int offsetIndex = timeString.lastIndexOf(':');

      if(hourIndex != offsetIndex) {
        int timeZoneOffset = Integer.parseInt(timeString.substring(offsetIndex + 1));
        int currentTimeZoneOffset = TimeZone.getDefault().getRawOffset()/60000;
        
        if(timeZoneOffset != currentTimeZoneOffset) {
          String[] hourMinute = timeString.split(":");
          int timeZoneDiff = currentTimeZoneOffset - timeZoneOffset;
          
          int hour = Integer.parseInt(hourMinute[0]) + (timeZoneDiff/60);
          int minute = Integer.parseInt(hourMinute[1]) + (timeZoneDiff%60);
          
          if(hour >= 24) {
            hour -= 24;
            date = date.addDays(1);
          }
          else if(hour < 0) {
            hour += 24;
            date = date.addDays(-1);
          }
          
          hourMinute[0] = String.valueOf(hour);
          hourMinute[1] = String.valueOf(minute);
          hourMinute[2] = String.valueOf(currentTimeZoneOffset);
          
          StringBuilder newId = new StringBuilder(progID.substring(0, index + 1));
          newId.append(hourMinute[0]).append(":").append(hourMinute[1]).append(":").append(hourMinute[2]);
          
          progID = newId.toString();
        }
      }
      else {
        String[] hourMinute = timeString.split(":");
        StringBuilder newId = new StringBuilder(progID.substring(0, index + 1));
        newId.append(hourMinute[0]).append(":").append(hourMinute[1]).append(":").append(TimeZone.getDefault().getRawOffset()/60000);
        
        progID = newId.toString();
      }      
      
      if(ch.getTimeZone().getRawOffset() != TimeZone.getDefault().getRawOffset()) {
        String[] hourMinute = timeString.split(":");
        int milliSeconds = Integer.parseInt(hourMinute[0]) * 60 * 60 * 1000 + Integer.parseInt(hourMinute[1]) * 60 * 1000;

        int diff = Math.abs(ch.getTimeZone().getRawOffset() - TimeZone.getDefault().getRawOffset());
        
        if(ch.getTimeZone().getRawOffset() < TimeZone.getDefault().getRawOffset()) {
          if(milliSeconds < diff) {
            date = date.addDays(-1);
          }
        }
        else if(milliSeconds + diff >= 86400 * 1000) {
          date = date.addDays(1);
        }
      }
      
      ChannelDayProgram dayProg = db.getDayProgram(date, ch);
      
      if (dayProg != null) {        
        Program prog = dayProg.getProgram(progID);
        if (prog != null) {
          return prog;
        }
        else {
          mLog.warning("could not find program with id '"+progID+"' (date: "+date+")");
        }
      }
      else {
        mLog.warning("day program not found: "+progID+"; "+date);
      }
    }else{
      mLog.warning("channel for program '"+progID+"' not found or not a subscribed channel");
    }

    return null;
  }
  
  /** {@inheritDoc} */
  public Program getProgram(String uniqueID) {
    String[] id = uniqueID.split("_");
    Date progDate;
    try {
      java.util.Date date = new SimpleDateFormat(MutableProgram.ID_DATE_FORMAT).parse(id[4]);
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(date.getTime());
      progDate = new Date(cal);
    } catch (ParseException e) {
      mLog.severe("Couldn't parse date from unique ID");
      return null;
    }
    
    String progID = new StringBuilder(id[0]).append('_').append(id[1]).append('_').append(id[2]).append('_').append(id[3]).append('_').append(id[5]).toString();
    return getProgram(progDate, progID);
  }


  private Channel getChannelFromProgId(String progId) {
    // try to avoid the split operation as it is costly
    int strLen = progId.length();
    int count = 0;
    int lastSeparator = 0;
    for (int i = 0; i < strLen; i++) {
      if (progId.charAt(i) == '_') {
        count++;
        lastSeparator = i;
      }
    }
    if (count == 4) {
      Channel channel = ChannelList.getChannel(progId.substring(0, lastSeparator));
      if (channel != null) {
        return channel;
      }
    }

    String[] s = progId.split("_");    
    
    if(s.length < 4) {
      return ChannelList.getChannel(null, null, null, s[0]);
    } else if(s.length == 4) {
      return ChannelList.getChannel(s[0], s[1], null, s[2]);
    } else {
      return ChannelList.getChannel(s[0], s[1], s[2], s[3]);
    }
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
  public Iterator<Program> getChannelDayProgram(Date date, Channel channel) {
    ChannelDayProgram channelDayProgram = TvDataBase.getInstance()
        .getDayProgram(date, channel);
    if (channelDayProgram == null) {
      return null;
    }
    return channelDayProgram.getPrograms();
  }

  /**
   * Creates a ProgramSearcher.
   * 
   * @param type
   *          The searcher type to create. Must be one of
   *          <ul>
   *          <li>{@link #SEARCHER_TYPE_EXACTLY},</li>
   *          <li>{@link #SEARCHER_TYPE_KEYWORD},</li>
   *          <li>{@link #SEARCHER_TYPE_REGULAR_EXPRESSION} or</li>
   *          <li>{@link #SEARCHER_TYPE_BOOLEAN}.</li>
   *          </ul>
   * @param searchTerm
   *          The search term the searcher should look for.
   * @param caseSensitive
   *          Specifies whether the searcher should be case sensitive.
   * @return A program searcher.
   * @throws TvBrowserException
   *           If creating the program searcher failed.
   */
  public ProgramSearcher createProgramSearcher(int type, String searchTerm,
      boolean caseSensitive)
      throws TvBrowserException
  {
    switch(type) {
      case SEARCHER_TYPE_EXACTLY: {
        String regex = RegexSearcher.searchTextToRegex(searchTerm, false);
        return new RegexSearcher(regex, caseSensitive, searchTerm);
      }
      case SEARCHER_TYPE_KEYWORD: {
        String regex = RegexSearcher.searchTextToRegex(searchTerm, true);
        return new RegexSearcher(regex, caseSensitive, searchTerm);
      }
      case SEARCHER_TYPE_REGULAR_EXPRESSION:
        return new RegexSearcher(searchTerm, caseSensitive);
      case SEARCHER_TYPE_BOOLEAN:
        try {
          return new BooleanSearcher(searchTerm, caseSensitive);
        }catch (ParserException e) {
          throw new TvBrowserException(PluginManagerImpl.class, "parser.error","Invalid input: {0}", e.getLocalizedMessage());          
        }
      default: throw new IllegalArgumentException("Unknown searcher type: " + type);
    }
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

  public TvDataServiceProxy getDataServiceProxy(String id) {
    return TvDataServiceProxyManager.getInstance().findDataServiceById(id);
  }

  /**
   * Creates a context menu for the given program containing all plugins.
   *
   * @param program The program to create the context menu for
   * @param caller The calling plugin.
   * @return a context menu for the given program.
   */
  public JPopupMenu createPluginContextMenu(Program program, ContextMenuIf caller) {

    return PluginProxyManager.createPluginContextMenu(program, caller);
  }

  /**
   * Returns an example program. You can use it for preview stuff.
   *
   * @return an example program.
   * @since 0.9.7.4
   */
  public Program getExampleProgram() {
    if (mExampleProgram == null) {
      // TODO: internationalize
      
      Channel exampleChannel = new Channel(null, "Channel 1",
          TimeZone.getDefault(), "de", "");

      MutableProgram prog = new MutableProgram(exampleChannel,
                                               Date.getCurrentDate(), 14, 45, true);
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
      //new fields for 2.6
      prog.setIntField(ProgramFieldType.EPISODE_NUMBER_TYPE, 13);
      prog.setIntField(ProgramFieldType.EPISODE_TOTAL_NUMBER_TYPE, 24);
      prog.setIntField(ProgramFieldType.SEASON_NUMBER_TYPE, 2);
      prog.setTextField(ProgramFieldType.PRODUCTION_COMPANY_TYPE,
          "Lorimar Television");
      prog.setTextField(ProgramFieldType.CAMERA_TYPE, "Jon Doe");
      prog.setTextField(ProgramFieldType.CUTTER_TYPE, "Jon Doe");
      prog.setIntField(ProgramFieldType.RATING_TYPE, 70);
      mExampleProgram = prog;
    }

    return mExampleProgram;
  }
  
  public void handleProgramSingleClick(Program program) {
    handleProgramSingleClick(program, null);
  }

  public void handleProgramSingleClick(Program program, ContextMenuIf caller) {
    if (program == null) {
      // Nothing to do
      return;
    }
    
    ContextMenuIf leftSingleClickIf = 
      ContextMenuManager.getInstance().getLeftSingleClickIf();
    
    if (leftSingleClickIf == null) {
      return;
    }

    if ((caller != null)  && (leftSingleClickIf.getId().equals(caller.getId()))) {
      return;
    }

    handleAction(program, leftSingleClickIf.getContextMenuActions(program));
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
   * @param caller ContextMenuIf that calls this. Prevents the ContextMenuIf to be activated twice
   *
   * @since 1.1
   */
  public void handleProgramDoubleClick(Program program, ContextMenuIf caller) {
    if (program == null) {
      // Nothing to do
      return;
    }
    
    ContextMenuIf defaultContextMenuIf = 
      ContextMenuManager.getInstance().getDefaultContextMenuIf();

    if (defaultContextMenuIf == null) {
      return;
    }

    if ((caller != null)  && (defaultContextMenuIf.getId().equals(caller.getId()))) {
      return;
    }

    handleAction(program, defaultContextMenuIf.getContextMenuActions(program));
  }


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
  public void handleProgramMiddleClick(Program program) {
    handleProgramMiddleClick(program, null);
  }


  /**
   * Handles a middle click on a program.
   * <p>
   * Executes the middle click context menu plugin.
   *
   * @param program The program to pass to the middle click context menu plugin.
   * @param caller ContextMenuIf that calls this. Prevents the ContextMenuIf to be activated twice.
   *
   * @since 1.1
   */
  public void handleProgramMiddleClick(Program program, ContextMenuIf caller) {
    if (program == null) {
      // Nothing to do
      return;
    }

    ContextMenuIf middleClickIf
      = ContextMenuManager.getInstance().getMiddleClickIf();

    if (middleClickIf == null) {
      return;
    }

    if ((caller != null)  && (middleClickIf.getId().equals(caller.getId()))) {
      return;
    }

    handleAction(program, middleClickIf.getContextMenuActions(program));
  }
  
  private void handleAction(Program program, ActionMenu menu) {
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
      ActionEvent evt = new ActionEvent(program, 0, (String)action.
          getValue(Action.ACTION_COMMAND_KEY));
      action.actionPerformed(evt);
    }
  }


  /**
   * Gets the plugin that is used as default in the context menu.
   *
   * @return the default context menu plugin.
   * @since 1.1
   */
  public PluginAccess getDefaultContextMenuPlugin() {
    return null ;//PluginProxyManager.getInstance().getDefaultContextMenuPlugin();
  }


  /**
   * Returns some settings a plugin may need.
   *
   * @return Some settings a plugin may need.
   */
  public TvBrowserSettings getTvBrowserSettings() {
    return new TvBrowserSettings(){
      public String getTvBrowserUserHome() {
        return Settings.getUserSettingsDirName();
      }

      public int[] getTimeButtonTimes() {
        return Settings.propTimeButtons.getIntArray();
      }

      public Date getLastDownloadDate() {
        return Settings.propLastDownloadDate.getDate();
      }
      
      public int getDefaultNetworkConnectionTimeout(){
        return Settings.propDefaultNetworkConnectionTimeout.getInt();
      }
      
      public Color getColorForMarkingPriority(int priority) {
        switch(priority) {
          case Program.NO_MARK_PRIORITY: return new Color(255,255,255,0);
          case Program.MIN_MARK_PRIORITY: return Settings.propProgramPanelMarkedMinPriorityColor.getColor();
          case Program.LOWER_MEDIUM_MARK_PRIORITY: return Settings.propProgramPanelMarkedLowerMediumPriorityColor.getColor();
          case Program.MEDIUM_MARK_PRIORITY: return Settings.propProgramPanelMarkedMediumPriorityColor.getColor();
          case Program.HIGHER_MEDIUM_MARK_PRIORITY: return Settings.propProgramPanelMarkedHigherMediumPriorityColor.getColor();
          case Program.MAX_MARK_PRIORITY: return Settings.propProgramPanelMarkedMaxPriorityColor.getColor();
        }
        
        return null;
      }
      
      public int getProgramTableEndOfDay() {
        return Settings.propProgramTableEndOfDay.getInt();
      }

      public int getProgramTableStartOfDay() {
        return Settings.propProgramTableStartOfDay.getInt();
      }
      
      public Color getProgramPanelOnAirLightColor() {
        return Settings.propProgramTableColorOnAirLight.getColor();
      }
      
      public Color getProgramPanelOnAirDarkColor() {
        return Settings.propProgramTableColorOnAirDark.getColor();
      }

      public boolean isMarkingBorderPainted() {
        return Settings.propProgramPanelWithMarkingsShowingBoder.getBoolean();
      }

      public boolean isUsingExtraSpaceForMarkIcons() {
        return Settings.propProgramPanelUsesExtraSpaceForMarkIcons.getBoolean();
      }

      public short getAutoDownloadWaitingTime() {
        return Settings.propAutoDownloadWaitingTime.getShort();
      }
    };
  }

  /**
   * Returns an icon from the icon-theme-system
   *  
   * If your plugin has icons that are not available as icons within an theme, you can add
   * your icons into your jar-file.
   * 
   * The directory structure must be like this:
   * 
   * [PackageOfYourPlugin]/icons/[size]x[size]/[category]/[icon].png
   * 
   * Please try to use the icon naming conventions of the FreeDesktop project:
   * http://cvs.freedesktop.org/[*]checkout[*]/icon-theme/default-icon-theme/spec/icon-naming-spec.xml
   * (please remove the [ ])
   *  
   * @param plugin Plugin that wants to load an icon
   * @param category Category of the icon (action, etc...) 
   * @param iconName icon name without file extension
   * @param size Size of the icon
   * @return Icon if found, <code>null</code> if not
   */
  public ImageIcon getIconFromTheme(Plugin plugin, String category, String iconName, int size) {
    return IconLoader.getInstance().getIconFromTheme(plugin, category, iconName, size);
  }


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
   * @param icon ThemeIcon that represents the Icon
   *
   * @return Icon if found, null if not
   */
  public ImageIcon getIconFromTheme(Plugin plugin, ThemeIcon icon) {
    return IconLoader.getInstance().getIconFromTheme(plugin, icon);
  }


  /**
   * Show the Settings-Dialog for a Plugin
   * 
   * @param plugin Use this Plugin
   * @since 2.2
   */
  public void showSettings(Plugin plugin) {
    MainFrame.getInstance().showSettingsDialog(plugin);
  }

  /**
   * Show the Settings-Dialog with a Specific SettingsItem
   * 
   * @param settingsItem SettingsItem to show (e.g. SettingsItem.CHANNELS)
   * @since 2.2
   */
  public void showSettings(String settingsItem) {
    if(SettingsDialog.getInstance() == null) {
      MainFrame.getInstance().showSettingsDialog(settingsItem);
    }
    else {
      SettingsDialog.getInstance().showSettingsTab(settingsItem);
    }
  }  
  
  /**
   * Return all marked programs.
   * 
   * @return The marked programs
   * @since 2.2
   */
  public Program[] getMarkedPrograms() {
    return MarkedProgramsList.getInstance().getMarkedPrograms();
  }

  /**
   * Return all Plugins/Functions that are able to receive programs.
   * 
   * @return The ProgramReceiveIfs.
   * @since 2.5
   */
  public ProgramReceiveIf[] getReceiveIfs() {
    return getReceiveIfs(null,null);
  }
  
  /**
   * Return all Plugins/Functions that are able to receive programs.
   * 
   * @param caller The caller ProgramReceiveIf.
   * @param callerTarget The target that calls the receive if array.
   * @return The ProgramReceiveIfs.
   * @since 2.5
   */
  public ProgramReceiveIf[] getReceiveIfs(ProgramReceiveIf caller, ProgramReceiveTarget callerTarget) {
    PluginAccess[] plugins = getActivatedPlugins();
    
    ArrayList<ProgramReceiveIf> receiveIfs = new ArrayList<ProgramReceiveIf>();
    
    if(caller == null || caller.getId().compareTo(ReminderPluginProxy.getInstance().getId()) != 0) {
      receiveIfs.add(ReminderPluginProxy.getInstance());
    }
    
    for(PluginAccess plugin : plugins) {
      if (plugin.canReceiveProgramsWithTarget()
          && plugin.getProgramReceiveTargets() != null
          && plugin.getProgramReceiveTargets().length > 0
          &&
          ((caller == null || plugin.getId().compareTo(caller.getId()) != 0) || (plugin.getId().compareTo(caller.getId()) == 0) && callerTarget != null && !(plugin.getProgramReceiveTargets().length == 1 && plugin.getProgramReceiveTargets()[0].equals(callerTarget)))) {
        receiveIfs.add(plugin);
      }
    }
    Collections.sort(receiveIfs);
    
    return receiveIfs.toArray(new ProgramReceiveIf[receiveIfs.size()]);
  }


  /**
   * Return the ReceiveIfFor given id or <code>null</code> if there is
   * no ReceiveIf for the given id.
   * 
   * @param id The id of the ReceiveIf.
   * 
   * @return The ReceiveIf with the given id or <code>null</code>
   * @since 2.5
   */
  public ProgramReceiveIf getReceiceIfForId(String id) {
    ProgramReceiveIf[] receiveIfs = getReceiveIfs();
    
    for(ProgramReceiveIf receiveIf : receiveIfs) {
      if(receiveIf.getId().compareTo(id) == 0) {
        return receiveIf;
      }
    }
    
    return null;
  }

  /**
   * Let TV-Browser scroll to the given program.
   * 
   * @param program The program to scroll to.
   * @since 2.5
   */  
  public void scrollToProgram(Program program) {
    if(program != null) {
      MainFrame.getInstance().scrollToProgram(program);
    }
  }
  
  /**
   * Let TV-Browser scroll to the given time.
   * 
   * @param time The time to scroll to in minutes after midnight.
   * @since 2.5
   */
  public void scrollToTime(int time) {
    MainFrame.getInstance().scrollToTime(time);
  }
  
  /**
   * Let TV-Browser scroll to the given channel.
   * 
   * @param channel The channel to scroll to.
   * @since 2.5
   */
  public void scrollToChannel(Channel channel) {
    MainFrame.getInstance().getProgramTableScrollPane().scrollToChannel(channel);
  }
  
  /**
   * Let TV-Browser change the date to the given date.
   * 
   * @param date The date to show the program for.
   * @since 2.5
   */
  public void goToDate(Date date) {
    if(TvDataBase.getInstance().dataAvailable(date)) {
      MainFrame.getInstance().goTo(date);
    }
  }

  /**
   * Returns the filter manager of TV-Browser.
   * With the filter manager you get access to the filter
   * system of TV-Browser. You can add or remove filters
   * of your plugin and switch the currently used filter.
   * 
   * @return  The filter manager of TV-Browser or <code>null</code> if TV-Browser isn't fully loaded.
   * @since 2.5
   */
  public FilterManager getFilterManager() {
    return FilterManagerImpl.getInstance();
  }
  
  protected void handleTvBrowserStartFinished() {
    mTvBrowserStartFinished = true;
  }
  
  /**
   * Gets the available global program configurations.
   * <p>
   * @return The available global program configurations.
   * @since 2.5.1
   */
  public AbstractPluginProgramFormating[] getAvailableGlobalPuginProgramFormatings() {
    return GlobalPluginProgramFormatingManager.getInstance().getAvailableGlobalPluginProgramFormatings();
  }

  public Date getCurrentDate() {
    return MainFrame.getInstance().getCurrentSelectedDate();
  }

  /**
   * Gets all ProgramRatingIfs of all plugins. You can get all available ratings for
   * one program.
   *
   * @return all ProgramRatingIfs of all plugins
   * @since 2.7
   */
  public ProgramRatingIf[] getAllProgramRatingIfs() {
    ArrayList<ProgramRatingIf> ratingArray = new ArrayList<ProgramRatingIf>();
    ratingArray.add(new RatingFieldRating());
    for (PluginAccess access : getActivatedPlugins()){
      ProgramRatingIf[] ratings = access.getProgramRatingIfs();
      if (ratings != null) {
        ratingArray.addAll(Arrays.asList(ratings));
      }
    }

    return ratingArray.toArray(new ProgramRatingIf[ratingArray.size()]);
  }

  public void deleteFileOnNextStart(String path) {
    Settings.propDeleteFilesAtStart.addItem(path);
  }

  @Override
  public boolean isDataAvailable(Date date) {
    return TvDataBase.getInstance().dataAvailable(date);
  }

  @Override
  public boolean showBalloonTip(String caption, String message, MessageType messageType) {
    return TVBrowser.showBalloonTip(caption,message,messageType);
  }
}