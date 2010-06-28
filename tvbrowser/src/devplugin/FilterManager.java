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
   * Returns the default filter of the program table.
   * 
   * @return The default filter of the program table.
   */
  public ProgramFilter getDefaultFilter();
  
  /**
   * Gets the "ShowAll" filter of the program table.
   * 
   * @return The "ShowAll" filter of the program table.
   */
  public ProgramFilter getAllFilter();
  
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
   * Tests if the filter is a plugin filter.
   * 
   * @param filter The filter to test.
   * @return <code>True</code> if the filter is a plugin filter,
   *         <code>false</code> otherwise.
   * @since 2.6
   */
  public boolean isPluginFilter(ProgramFilter filter);
}
