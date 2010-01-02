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
package tvbrowser.core.plugin;

import javax.swing.Icon;

import tvdataservice.MutableProgram;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.PluginAccess;
import devplugin.PluginTreeNode;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.ProgramRatingIf;

/**
 * Encapsulates a plugin and manages the access to it.
 * <p>
 * All operations that are only accessable from TV-Browser itself is defined in
 * this interface. All operations that are accessable by other plugins are
 * defined in {@link PluginAccess}.
 *
 * @author Til Schneider, www.murfman.de
 */
public interface PluginProxy extends PluginAccess {
  
  /**
   * Gets whether the plugin is currently activated.
   * 
   * @return whether the plugin is currently activated.
   */
  public boolean isActivated();

  /**
   * Gets the SettingsTab object, which is added to the settings-window.
   * 
   * @return the SettingsTab object or <code>null</code> if the plugin does not
   *         provide this feature.
   */
  public SettingsTabProxy getSettingsTab();

  /**
   * Gets the action to use for the main menu and the toolbar.
   *
   * @return the action to use for the menu and the toolbar or <code>null</code>
   *         if the plugin does not provide this feature.
   */
  public ActionMenu getButtonAction();

  /**
   * This method is automatically called, when the TV data update is finished.
   * 
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataDeleted(ChannelDayProgram)
   */
  public void handleTvDataUpdateFinished();

  /**
   * This method is automatically called, when TV data was added.
   * (E.g. after an update).
   * <p>
   * The TV data may be modified by the plugin! So this method must be called
   * before new TV data is saved.
   * 
   * @param newProg The new ChannelDayProgram.
   * 
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   */
  public void handleTvDataAdded(ChannelDayProgram newProg);
  
  /**
   * This method is automatically called, when TV data was touched (that means something was done with it).
   * (E.g. after an update).
   * <p>
   * @param removedDayProgram The old ChannelDayProgram.
   * @param addedDayProgram The new ChannelDayProgram.
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   */
  public void handleTvDataTouched(ChannelDayProgram removedDayProgram, ChannelDayProgram addedDayProgram);

  /**
   * This method is automatically called, when TV data was deleted.
   * (E.g. after an update).
   * 
   * @param oldProg The old ChannelDayProgram which was deleted.
   * 
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   */
  public void handleTvDataDeleted(ChannelDayProgram oldProg);

  /**
   * This method is automatically called after activating the plugin (either
   * during the TV-Browser startup phase or after manually activating the
   * disabled plugin in the settings).
   * 
   * @since 1.1
   */
  public void onActivation();

  /**
   * This method is automatically called after deactivating the plugin.
   * 
   * @since 1.1
   */
  public void onDeactivation();
  
  /**
   * 
   * @return true, if the programs of this plugin are handled by the plugin
   *      tree view
   * @since 1.1
   */
  public boolean canUseProgramTree();

  public PluginTreeNode getRootNode();
  
  /**
   * This method is called when the TV-Browser start is finished.
   * @since 2.2
   */
  public void handleTvBrowserStartFinished();
  
  /**
   * Handles a runtime exception that was caused by the plugin.
   * 
   * @param t The exception to handle
   * @since 2.1
   */
  public void handlePluginException(Throwable t);

  /**
   * return the file name of the plugin contained in this proxy
   * @return file name
   * @since 2.6
   */
  public String getPluginFileName();
  
  /**
   * get the icon to represent this plugin in the settings
   * @return icon
   * @since 2.6
   */
  public Icon getPluginIcon();

  public boolean hasArtificialPluginTree();

  public void addToArtificialPluginTree(MutableProgram program);

  public PluginTreeNode getArtificialRootNode();
  
  /**
   * comparator for plugin proxy (sorting by name)
   * @author bananeweizen
   * @since 2.7
   */
  public static class Comparator implements java.util.Comparator<PluginProxy> {
    public int compare(PluginProxy o1, PluginProxy o2) {
      return o1.getInfo().getName().compareToIgnoreCase(o2.getInfo().getName());
    }
  }
  
  public boolean isAllowedToDeleteProgramFilter(PluginsProgramFilter programFilter);


  /**
   * If this plugin can rate programs, this interface makes it possible to offer this ratings
   * to other plugins. You can get all ProgramRatingIfs of all plugins using {@link devplugin.PluginManager#getAllProgramRatingIfs()}
   *
   * The plugin can return more than one ratingif, e.g. average ratings, user rating ...
   *
   * @return the RatingIfs of this plugin
   * @since 2.7
   */
  public ProgramRatingIf[] getProgramRatingIfs();
  
  /**
   * Gets the actions for the context menu of a channel.
   * 
   * @param channel
   *          The channel the context menu will be shown for.
   * @return the actions this plugin provides for the given channel or
   *         <code>null</code> if the plugin does not provide this feature.
   * @since 3.0
   */
  public ActionMenu getContextMenuActions(final Channel channel);

  /**
   * Gets the importance of a program.The importance of all active plugins is used to determinate
   * the opacity of the used colors of a program, therefor a mean value of all values is used.
   * <p>
   * The importance value can be.
   * <ul>
   * <li>{@link Program#DEFAULT_PROGRAM_IMPORTANCE},</li>
   * <li>{@link Program#MIN_PROGRAM_IMPORTANCE},</li>
   * <li>{@link Program#LOWER_MEDIUM_PROGRAM_IMPORTANCE},</li>
   * <li>{@link Program#MEDIUM_PROGRAM_IMPORTANCE},</li>
   * <li>{@link Program#HIGHER_MEDIUM_PROGRAM_IMPORTANCE} or</li>
   * <li>{@link Program#MAX_PROGRAM_IMPORTANCE}.</li>
   * </ul>
   * <p>
   * @param p The program to get the importance value for.
   * @return The importance value for the given program.
   * @since 3.0
   */
  public byte getImportanceForProgram(Program p);
}
