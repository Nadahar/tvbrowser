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

import devplugin.ActionMenu;
import devplugin.ChannelDayProgram;
import devplugin.PluginAccess;
import devplugin.PluginTreeNode;

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
   * This method is automatically called, when the plugin after activating
   * the plugin.
   *
   * @since 1.1
   */
  public void onActivation();
  
  /**
   * This method is automatically called, when the plugin after deactivating
   * the plugin.
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
   * Handles a runtime exception that was caused by the plugin.
   * 
   * @param t The exception to handle
   * @since 2.1
   */
  public void handlePluginException(Throwable t);
}
