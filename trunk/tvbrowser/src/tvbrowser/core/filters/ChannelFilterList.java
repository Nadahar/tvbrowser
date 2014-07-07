/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.core.filters;

import java.util.ArrayList;

import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import util.exc.TvBrowserException;

import devplugin.ChannelFilter;
import devplugin.ChannelFilterChangeListener;

public class ChannelFilterList {
  private ArrayList<ChannelFilter> mChannelFilterList;
  private ArrayList<ChannelFilterChangeListener> mChannelFilterChangeListenerList;
  
  private static ChannelFilterList INSTANCE;
  
  private ChannelFilterList() {
    INSTANCE = this;
    mChannelFilterList = new ArrayList<ChannelFilter>(0);
    mChannelFilterChangeListenerList = new ArrayList<ChannelFilterChangeListener>(0);
  }
  
  public static final synchronized ChannelFilterList getInstance() {
    if(INSTANCE == null) {
      new ChannelFilterList();
    }
    
    return INSTANCE;
  }
  
  public ChannelFilter getChannelFilterForName(String name) {
    synchronized (mChannelFilterList) {
      for(ChannelFilter filter : mChannelFilterList) {
        if(filter.getName().equals(name)) {
          return filter;
        }
      }
      
      try {
        ChannelFilter test = ChannelFilter.createChannelFilterForName(name);
        
        mChannelFilterList.add(test);
        
        return test;
      } catch (ClassCastException e) {
      } catch (TvBrowserException e) {}
    }
    
    return null;
  }
  
  ChannelFilter[] getAvailableChannelFilter() {
    synchronized (mChannelFilterList) {
      String[] names = FilterComponentList.getInstance().getChannelFilterNames();
      
      ArrayList<String> foundNames = new ArrayList<String>();
      
      for(ChannelFilter filter : mChannelFilterList) {
        for(String name : names) {
          if(filter.getName().equals(name)) {
            foundNames.add(name);
            break;
          }
        }
      }
      
      for(String name : names) {
        if(!foundNames.contains(name)) {
          try {
            ChannelFilter test = ChannelFilter.createChannelFilterForName(name);
            
            mChannelFilterList.add(test);
          } catch (ClassCastException e) {
          } catch (TvBrowserException e) {}
        }
      }
      
      return mChannelFilterList.toArray(new ChannelFilter[mChannelFilterList.size()]);
    }
  }
  
  void removeChannelFilter(ChannelFilter filter) {
    synchronized (mChannelFilterList) {
      mChannelFilterList.remove(filter);
    }
  }
  
  void registerChannelFilterChangeListener(ChannelFilterChangeListener listener) {
    mChannelFilterChangeListenerList.add(listener);
  }
  
  void unregisterChannelFilterChangeListener(ChannelFilterChangeListener listener) {
    mChannelFilterChangeListenerList.remove(listener);
  }
  
  void fireChannelFilterAdded(ChannelFilterComponent filterComponent) {
    synchronized (mChannelFilterList) {
      ChannelFilter filter = getChannelFilterForName(filterComponent.getName());
      if(filter != null) {
        for(ChannelFilterChangeListener listener : mChannelFilterChangeListenerList) {
          listener.channelFilterAdded(filter);
        }
      }
    }
  }
  
  void fireChannelFilterRemoved(ChannelFilterComponent filterComponent) {
    synchronized (mChannelFilterList) {
      for(int i = mChannelFilterList.size()-1; i >= 0; i--) {
        if(mChannelFilterList.get(i).getName().equals(filterComponent.getName())) {
          ChannelFilter filter = mChannelFilterList.remove(i);
          for(ChannelFilterChangeListener listener : mChannelFilterChangeListenerList) {
            listener.channelFilterRemoved(filter);
          }
          break;
        }
      }
    }
  }
}
