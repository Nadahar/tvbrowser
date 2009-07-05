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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.common;

import java.util.ArrayList;

import tvbrowser.core.TvDataUpdateListener;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.programinfo.ProgramInfoProxy;
import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import tvbrowser.extras.searchplugin.SearchPluginProxy;

/**
 * A class that contains all available internal plugin proxys.
 * Add all internal plugin proxys to this list.
 * 
 * @author René Mach
 * @since 2.6
 */
public class InternalPluginProxyList {
  private ArrayList<InternalPluginProxyIf> mList;
  private static InternalPluginProxyList mInstance;
  
  private InternalPluginProxyList() {
    mInstance = this;
    
    mList = new ArrayList<InternalPluginProxyIf>();

    mList.add(FavoritesPluginProxy.getInstance());
    mList.add(ReminderPluginProxy.getInstance());
    mList.add(ProgramInfoProxy.getInstance());
    mList.add(SearchPluginProxy.getInstance());
    TvDataUpdater.getInstance().addTvDataUpdateListener(new TvDataUpdateListener() {
      
      @Override
      public void tvDataUpdateStarted() {
      }
      
      @Override
      public void tvDataUpdateFinished() {
        for (InternalPluginProxyIf proxy : mList) {
          proxy.handleTvDataUpdateFinished();
        }
      }
    });
  }
  
  /**
   * Gets the instance of this class.
   * If there is no instance it will be created.
   * 
   * @return The instance of this class.
   */
  public static InternalPluginProxyList getInstance() {
    if(mInstance == null) {
      new InternalPluginProxyList();
    }
    
    return mInstance;
  }
  
  /**
   * Gets all available internal plugin proxies.
   * 
   * @return All available internal plugin proxies.
   */
  public InternalPluginProxyIf[] getAvailableProxys() {
    return mList.toArray(new InternalPluginProxyIf[mList.size()]);
  }
  
  /**
   * Gets the internal plugin proxy for the given id.
   * 
   * @param id The id to get the internal plugin proxy for.
   * @return The internal plugin proxy for the given id or
   * <code>null</code> if the id was not found.
   */
  public InternalPluginProxyIf getProxyForId(String id) {
    for(InternalPluginProxyIf internalPluginProxy : mList) {
      if(internalPluginProxy.getId().equals(id)) {
        return internalPluginProxy;
      }
    }
    
    return null;
  }
}
