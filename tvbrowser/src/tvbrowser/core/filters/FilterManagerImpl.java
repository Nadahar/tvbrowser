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

import tvbrowser.ui.mainframe.MainFrame;

import devplugin.FilterManager;
import devplugin.PluginsFilterComponent;
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
   * Creates an instance of this clas if
   * there is no one and returns it.
   * 
   * @return The instance of this class.
   */
  public static FilterManager getInstance() {
    if(mInstance == null)
      new FilterManagerImpl();
    
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
      if (!(filt instanceof SeparatorFilter)) {
        filters.add(filt);
      }
    }

    return filters.toArray(new ProgramFilter[filter.length]);
  }
  
  /**
   * @return The available filter components.
   */
  public FilterComponent[] getAvailableFilterComponents() {
    return FilterComponentList.getInstance().getAvailableFilterComponents();
  }

  /**
   * @param name The name of the used filter rule. 
   * @param rule The rule to use for this user filter.
   * @return True if filter could be added. 
   */
  public boolean addUserFilterRule(String name, String rule) {
    if(FilterList.getInstance().getFilterByName(name) == null) {
      UserFilter filter = new UserFilter(name);
      
      try {
        filter.setRule(rule);
      } catch (ParserException e) {
        e.printStackTrace();
        return false;
      }
      
      FilterList.getInstance().addProgramFilter(filter);
      MainFrame.getInstance().updateFilterMenu();
    }
    return false;
  }

  /**
   * @param name The name of the filter rule which should be deleted.
   * @return True if filter could be deleted. 
   */
  public boolean deleteUserFilterRule(String name) {
    ProgramFilter filter = FilterList.getInstance().getFilterByName(name);
    
    if(filter != null && filter instanceof UserFilter) {
      if(getCurrentFilter() == filter)
         setCurrentFilter(FilterList.getInstance().getDefaultFilter());
      
      FilterList.getInstance().remove(filter);
      MainFrame.getInstance().updateFilterMenu();
      
      return true;
    }
    
    return false;
  }
  
  /**
   * @param filterComponent The filter component to add.
   * @return True if the filter component could be added.
   */
  public boolean addFilterComponent(PluginsFilterComponent filterComponent) {
    if(FilterComponentList.getInstance().getFilterComponentByName(filterComponent.getName()) == null) {
      FilterComponentList.getInstance().add(filterComponent);
      MainFrame.getInstance().updateFilterMenu();
      return true;
    }
    
    return false;
  }
  
  /**
   * @param filterComponent The filter component to delete.
   * @return True if the filter component could be deleted.
   */
  public boolean deleteFilterComponent(PluginsFilterComponent filterComponent) {
    if(filterComponent.getPluginAccessOfComponent().isAllowedToDeleteOrChangeFilterComponent(filterComponent)) {
      removeFilterComponents(filterComponent);
      return true;
    }
    
    return false;
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
    if(filter.getPluginAccessOfFilter().isAllowedToDeleteProgramFilter(filter)) {
      if(getCurrentFilter() == filter)
        setCurrentFilter(FilterList.getInstance().getDefaultFilter());
      
      FilterList.getInstance().remove(filter);
      FilterList.getInstance().store();
      MainFrame.getInstance().updateFilterMenu();
      return true;
    }
      
    return false;
  }
  
  /**
   * Changes the name of a filter component.
   * 
   * @param component The changed filter component.
   * @param oldName The old name of the filter component.
   * @return True if the filter component could be changed.
   */
  public boolean changeFilterComponentName(PluginsFilterComponent component, String oldName) {
    if(!component.getPluginAccessOfComponent().isAllowedToDeleteOrChangeFilterComponent(component))
      return false;
    
    UserFilter[] userFilters = FilterList.getInstance().getUserFilterArr();
    
    for(UserFilter userFilter : userFilters) {
      
      String rule = userFilter.getRule();
      
      if(rule.indexOf(oldName) != -1) {
        String newRule = rule.replace(oldName, component.getName());
        
        try {
          userFilter.setRule(newRule);
        } catch (ParserException e) {
          e.printStackTrace();
          return false;
        }
      }
    }
    
    FilterList.getInstance().store();
    return true;
  }
  
  public void removeFilterComponents(PluginsFilterComponent filterComponent) {
    UserFilter[] userFilters = FilterList.getInstance().getUserFilterArr();      
    
    for(int i = 0; i < userFilters.length; i++)
      if(userFilters[i].getRule().indexOf(filterComponent.getName()) != -1)
        deleteUserFilterRule(userFilters[i].getName());
    
    FilterComponentList.getInstance().remove(filterComponent.getName());      
    MainFrame.getInstance().updateFilterMenu();    
  }
}
