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
 */
package devplugin;

import tvbrowser.core.filters.FilterComponent;

/**
 * The filter manager enables plugins to
 * use the filter system of TV-Browser.
 * 
 * @author René Mach
 * @since 2.5
 */
public interface FilterManager {

  /**
   * Returns the current selected filter of the program table.
   * 
   * @return The current selected filter of the program table.
   */
  public ProgramFilter getCurrentFilter();
  
  /**
   * Activates a filter.
   * 
   * @param filter The filter to activate.
   */
  public void setCurrentFilter(ProgramFilter filter);
  
  /**
   * Returns an array of all available filters.
   *
   * @return An array of all available filters.
   */
  public ProgramFilter[] getAvailableFilters();
  
  /**
   * @return The available filter components.
   */
  public FilterComponent[] getAvailableFilterComponents();
  
  /**
   * @param name The name of the used filter rule. 
   * @param description The description of the filter component
   * @param rule The rule to use for this user filter.
   * @return True if filter could be added. 
   */
  public boolean addUserFilterRule(String name, String rule);
  
  /**
   * @param name The name of the filter rule which should be deleted.
   * @return True if filter could be added. 
   */
  public boolean deleteUserFilterRule(String name);
  
  /**
   * @param filterComponent The filter component to add.
   * @return True if the filter component could be added.
   */
  public boolean addFilterComponent(PluginsFilterComponent filterComponent);
  
  /**
   * @param filterComponent The filter component to delete.
   * @return True if the filter component could be deleted.
   */
  public boolean deleteFilterComponent(PluginsFilterComponent filterComponent);
  
  /**
   * @param filter The filter to add.
   * @return True if the filter could be added.
   */
  public boolean addFilter(PluginsProgramFilter filter);
  
  /**
   * @param filter The filter to delete.
   * @return True if the filter could be deleted.
   */
  public boolean deleteFilter(PluginsProgramFilter filter);
  
  /**
   * Changed the name of a filter component.
   * 
   * @param component The changed filter component.
   * @param oldName The old name of the filter component.
   * @return True if the filter component could be changed.
   */
  public boolean changeFilterComponentName(PluginsFilterComponent component, String oldName);
}
