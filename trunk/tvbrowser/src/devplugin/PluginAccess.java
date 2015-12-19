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
package devplugin;

import javax.swing.Icon;

import tvbrowser.core.plugin.ButtonActionIf;


/**
 * An access to another plugin.
 * <p>
 * Contains all operations that may be called on a plugin from another plugin.
 *
 * @author Til Schneider, www.murfman.de
 */
public interface PluginAccess extends ButtonActionIf,Marker,ProgramReceiveIf,ContextMenuIf {
  
  /**
   * Gets the ID of this plugin.
   * 
   * @return The ID of this plugin.
   */
  public String getId();
  
  /**
   * Gets the meta information about the plugin.
   * 
   * @return The meta information about the plugin.
   */
  public PluginInfo getInfo();

  /**
   * Gets the description text for the program table icons provided by this
   * Plugin.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @return The description text for the program table icons or
   *         <code>null</code> if the plugin does not provide this feature.
   *
   * @see #getProgramTableIcons(Program)
   */
  public String getProgramTableIconText();
  
  /**
   * Gets the ToolTipIcons for the program table icons provided by this
   * Plugin.
   * <p>
   * Override this method if your plugin provides icons for the program table
   * (shown below the start time) and you want the icons to be shown in the tooltip.
   *
   * @param program The program to get the ToolTipIcons for.
   * @return The description text for the program table icons or
   *         <code>null</code> if the plugin does not provide this feature.
   *
   * @see #getProgramTableIcons(Program)
   * @since 3.4.2
   */
  public ToolTipIcon[] getProgramTableToolTipIcons(Program program);

  /**
   * Gets the icons this Plugin provides for the given program. These icons will
   * be shown in the program table under the start time.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @param program The programs to get the icons for.
   * @return The icons for the given program or <code>null</code>.
   *
   * @see #getProgramTableIconText()
   */
  public Icon[] getProgramTableIcons(Program program);


  /**
   * Gets the icon to use for marking programs in the program table. Should be
   * 16x16.
   * 
   * @return the icon to use for marking programs in the program table.
   */
  public Icon getMarkIcon();

  /**
   * Gets the icons to use for marking programs in the program table. Should be
   * 16x16.
   * 
   * @return the icons to use for marking programs in the program table.
   * @since 2.5
   */
  public Icon[] getMarkIcons(Program p);
  
  /**
   * Returns the available program filters that the plugin supports.
   * @since 2.5
   * 
   * @return The available program filters that the plugin supports or <code>null</code> if it supports no filter.
   */
  public PluginsProgramFilter[] getAvailableFilter();
  
  /**
   * Returns the available plugins filter component classes.
   * <br>
   * ATTENTON: Use return <code>(Class<? extends PluginsFilterComponent>[]) new Class[] {MyFilterComponent1.class,MyFilterComponent2.class};</code>
   * because the creation of a class array with generic type didn't work.
   * 
   * @return The available plugins filter components classes or <code>null</code> if no plugins filter components are supported.
   * @since 2.5
   */
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses();


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
   * If this Plugin grants access to functions it will provide a communication class for other Plugins to use.
   * <p>
   * @return The communication class of this Plugin or <code>null</code>
   * if there is no communication class for this Plugin.
   * @since 3.3.4
   */
  public PluginCommunication getCommunicationClass();
}
