/*
 * TV-Browser Compat
 * Copyright (C) 2017 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date: 2014-06-17 15:59:09 +0200 (Di, 17 Jun 2014) $
 *   $Author: ds10 $
 * $Revision: 8152 $
 */
package compat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import javax.swing.JFrame;

import devplugin.ProgramFilter;
import devplugin.Version;
import tvbrowser.TVBrowser;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import tvbrowser.ui.filter.dlgs.EditFilterComponentDlg;
/**
 * Compatibility class for TV-Browser devplugin.FilterManager class.
 * 
 * @author René Mach
 * @since 0.2
 */
public final class FilterCompat {
  private static FilterCompat INSTANCE;
  private ArrayList<FilterChangeListener> mListListeners;
  
  private FilterCompat() {
    INSTANCE = this;
    mListListeners = new ArrayList<FilterCompat.FilterChangeListener>();
    
    try {
      String clazzName = null;
      
      if(TVBrowser.VERSION.compareTo(new Version(3,34,true)) >= 0) {
        clazzName = "devplugin.FilterChangeListenerV2";
      }
      else if(TVBrowser.VERSION.compareTo(new Version(3,33,true)) >= 0) {
        clazzName = "devplugin.FilterChangeListener";
      }
      
      if(clazzName != null) {
        Class<?> filterListenerClass = Class.forName(clazzName);
        Method addFilterListener = FilterManagerImpl.class.getDeclaredMethod("registerFilterChangeListener", filterListenerClass);
        
        Object filterListener = Proxy.newProxyInstance(filterListenerClass.getClassLoader(), new Class<?>[] {filterListenerClass}, new HandlerFilterListener(this));
        
        addFilterListener.invoke(FilterManagerImpl.getInstance(), filterListener);
      }
    }catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * @return The one instance of this class.
   */
  public static synchronized final FilterCompat getInstance() {
    if(INSTANCE == null) {
      new FilterCompat();
    }
    
    return INSTANCE;
  }
  
  /**
   * @return An array with names of all channel filter components
   * or an empty array if there are no channel filter components.
   */
  public static String[] getChannelFilterComponentNames() {
    String[] result = new String[0];
    if(TVBrowser.VERSION.compareTo(new Version(3,21,true)) >= 0) {
      try {
        Method m = FilterManagerImpl.class.getDeclaredMethod("getChannelFilterComponentNames");
        result = (String[])m.invoke(FilterManagerImpl.getInstance());
      }catch(Exception e) {}
    }
    else {
      result = getChannelFilterComponentNamesCompat();
    }
    
    return result;
  }
  
  /**
   * Creates a new channel filter component.
   * <p>
   * @return The name of the created channel filter component or <code>null</code>
   * if there was no channel filter component created.
   */
  public static String addNewChannelFilterComponent() {
    String result = null;
    
    if(TVBrowser.VERSION.compareTo(new Version(3,21,true)) >= 0) {
      try {
        Method m = FilterManagerImpl.class.getDeclaredMethod("addNewChannelFilterComponent");
        result = (String)m.invoke(FilterManagerImpl.getInstance());
      }catch(Exception e) {}
    }
    else {
      result = addNewChannelFilterComponentCompat();
    }
    
    return result;
  }
  
  /**
   * Registers the given listener to listen to filter change events.
   * <p>
   * @param listener The (compat) filter change listener to register.
   */
  public void registerFilterChangeListener(final FilterChangeListener listener) {
    if(!mListListeners.contains(listener)) {
      mListListeners.add(listener);
    }
  }
  
  /**
   * Unregisters the given listener from listening to filter change events.
   * <p>
   * @param listener The (compat) filter change listener to unregister.
   */
  public void unregisterFilterChangeListener(final FilterChangeListener listener) {
    mListListeners.remove(listener);
  }
  
  private void handleFilterEvent(final String method, final ProgramFilter filter) {
    if("filterAdded".equals(method)) {
      for(FilterChangeListener l : mListListeners) {
        l.filterAdded(filter);
      }
    }
    else if("filterRemoved".equals(method)) {
      for(FilterChangeListener l : mListListeners) {
        l.filterRemoved(filter);
      }
    }
    else if("filterTouched".equals(method)) {
      for(FilterChangeListener l : mListListeners) {
        l.filterTouched(filter);
      }
    }
    else if("filterDefaultChanged".equals(method)) {
      for(FilterChangeListener l : mListListeners) {
        l.filterDefaultChanged(filter);
      }
    }
  }
  
  private static String[] getChannelFilterComponentNamesCompat() {
    return FilterComponentList.getInstance().getChannelFilterNames();
  }
  
  private static String addNewChannelFilterComponentCompat() {
    EditFilterComponentDlg dlg = new EditFilterComponentDlg((JFrame)null, null, ChannelFilterComponent.class);
    FilterComponent rule = dlg.getFilterComponent();
    if (rule == null) {
      return null;
    }
    if (! (rule instanceof ChannelFilterComponent)) {
      return null;
    }
    FilterComponentList.getInstance().add(rule);
    FilterComponentList.getInstance().store();
    
    return rule.getName();
  }
  
  
  /**
   * A listener for filter change events.
   * <p>
   * @author René Mach
   * @since 0.2
   */
  public static interface FilterChangeListener {
    /**
     * Called when a filter is added.
     * <p>
     * @param filter The filter that was added.
     */
    public void filterAdded(ProgramFilter filter);
    
    /**
     * Called when a filter was removed.
     * <p>
     * @param filter The filter that was removed.
     */
    public void filterRemoved(ProgramFilter filter);
    
    /**
     * Called when user edited the filter.
     * <p>
     * @param filter The filter that was touched.
     */
    public void filterTouched(ProgramFilter filter);
    
    /**
     * Called when the default filter was changed.
     * <p>
     * @param filter The new default filter, or the
     * all filter if no default filter exists.
     */
    public void filterDefaultChanged(ProgramFilter filter);
  }
  
  private static final class HandlerFilterListener implements InvocationHandler {
    private FilterCompat mFilterCompat;
    
    private HandlerFilterListener(FilterCompat filterCompat) {
      mFilterCompat = filterCompat;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if(method != null && args != null && args.length == 1) {
        mFilterCompat.handleFilterEvent(method.getName(), (ProgramFilter)args[0]);
      }
      
      return null;
    }
  }
}
