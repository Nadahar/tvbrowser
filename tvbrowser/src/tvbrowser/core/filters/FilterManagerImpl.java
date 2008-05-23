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
package tvbrowser.core.filters;

import java.util.ArrayList;

import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.mainframe.MainFrame;
import devplugin.FilterManager;
import devplugin.PluginsProgramFilter;
import devplugin.ProgramFilter;

/**
 * Is the implementation of the FilterManager.
 * 
 * @author René Mach
 * @since 2.5
 */
public class FilterManagerImpl implements FilterManager {
  private static FilterManagerImpl mInstance;
  
  private FilterManagerImpl() {
    mInstance = this;
  }
  
  /**
   * Creates an instance of this class if
   * there is no one and returns it.
   * 
   * @return The instance of this class.
   */
  public static FilterManager getInstance() {
    if(mInstance == null) {
      new FilterManagerImpl();
    }
    
    return mInstance;
  }

  
  /**
   * Returns the current selected filter of the program table.
   * 
   * @return The current selected filter of the program table.
   */
  public ProgramFilter getCurrentFilter() {
    return MainFrame.getInstance().getProgramFilter();
  }
  
  /**
   * Activates a filter.
   * 
   * @param filter The filter to activate.
   */
  public void setCurrentFilter(ProgramFilter filter) {
    MainFrame.getInstance().setProgramFilter(filter);
  }

  /**
   * Returns an array of all available filters.
   *
   * @return An array of all available filters.
   */
  public ProgramFilter[] getAvailableFilters() {
    ArrayList<ProgramFilter> filters = new ArrayList<ProgramFilter>();

    FilterList filterList = FilterList.getInstance();

    ProgramFilter[] filter = filterList.getFilterArr();

    for (ProgramFilter filt : filter) {
      if (filt != null && !(filt instanceof SeparatorFilter)) {
        filters.add(filt);
      }
    }

    return filters.toArray(new ProgramFilter[filters.size()]);
  }
  
  /**
   * @param filter The filter to add.
   * @return True if the filter could be added.
   */
  public boolean addFilter(PluginsProgramFilter filter) {
    if(FilterList.getInstance().getFilterByName(filter.getName()) == null) {
      FilterList.getInstance().addProgramFilter(filter);
      MainFrame.getInstance().updateFilterMenu();
      return true;
    }
      
    return false;
  }
  
  /**
   * @param filter The filter to delete.
   * @return True if the filter could be deleted.
   */
  public boolean deleteFilter(PluginsProgramFilter filter) {
    if(PluginProxyManager.getInstance().getActivatedPluginForId(filter.getPluginAccessOfFilter().getId()).isAllowedToDeleteProgramFilter(filter)) {
      if(getCurrentFilter() == filter) {
        setCurrentFilter(FilterList.getInstance().getDefaultFilter());
      }
      
      FilterList.getInstance().remove(filter);
      FilterList.getInstance().store();
      MainFrame.getInstance().updateFilterMenu();
      return true;
    }
      
    return false;
  }

  /**
   * Returns the default filter of the program table.
   * 
   * @return The default filter of the program table.
   */
  public ProgramFilter getDefaultFilter() {
    return FilterList.getInstance().getDefaultFilter();
  }
  
  /**
   * Gets the "ShowAll" filter of the program table.
   * 
   * @return The "ShowAll" filter of the program table.
   */
  public ProgramFilter getAllFilter() {
    return FilterList.getInstance().getAllFilter();
  }
  
  /**
   * Tests if the filter is a plugin filter.
   * 
   * @param filter The filter to test.
   * @return <code>True</code> if the filter is a plugin filter,
   *         <code>false</code> otherwise.
   * @since 2.6
   */
  public boolean isPluginFilter(ProgramFilter filter) {
    return filter instanceof PluginFilter;
  }
}
