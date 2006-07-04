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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.core.tvdataservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import tvbrowser.core.Settings;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.progress.ProgressMonitorGroup;
import devplugin.ChannelGroup;
import devplugin.ProgressMonitor;


public class ChannelGroupManager {

  private static java.util.logging.Logger mLog
           = java.util.logging.Logger.getLogger(ChannelGroupManager.class.getName());

  private static ChannelGroupManager mInstance;

  private HashMap<TvDataServiceProxy, ArrayList<ChannelGroup>> mServiceToGroupsMap; // key: TvDataServiceProxy;
                                       // value: Array of devplugin.ChannelGroup objects

  private HashMap<String, ChannelGroup> mGroups;  // key: groupId
                            // value: devplugin.ChannelGroup

  private HashMap<ChannelGroup, TvDataServiceProxy> mGroupToService;   // key: devplugin.ChannelGroup
                                     // value: TvDataServiceProxy

  private ChannelGroupManager() {
    mGroups = new HashMap<String, ChannelGroup>();
    mServiceToGroupsMap = new HashMap<TvDataServiceProxy, ArrayList<ChannelGroup>>();
    mGroupToService = new HashMap<ChannelGroup, TvDataServiceProxy>();
    TvDataServiceProxy[] proxies = TvDataServiceProxyManager.getInstance().getDataServices();

    for (int i=0; i<proxies.length; i++) {
      ChannelGroup[] groups = proxies[i].getAvailableGroups();
      for (int j=0; j<groups.length; j++) {
        addGroup(proxies[i], groups[j]);
      }
    }
  }
  

  private void removeAllGroups() {
    mGroups.clear();
    mGroupToService.clear();
    mServiceToGroupsMap.clear();
  }

  private void addGroup(TvDataServiceProxy service, ChannelGroup group) {
    mGroups.put(createId(service, group), group);
    mGroupToService.put(group, service);
    ArrayList<ChannelGroup> groups = (ArrayList<ChannelGroup>)mServiceToGroupsMap.get(service);
    if (groups == null) {
      groups = new ArrayList<ChannelGroup>();
      mServiceToGroupsMap.put(service, groups);
    }
    groups.add(group);
  }

  public static ChannelGroupManager getInstance() {
    if (mInstance == null) {
      mInstance = new ChannelGroupManager();
    }
    return mInstance;
  }



  public TvDataServiceProxy getTvDataService(ChannelGroup group) {
    return (TvDataServiceProxy)mGroupToService.get(group);
  }

   /**
   * Refresh the list of available groups and refresh the lists of available channels
   */
  public void checkForAvailableGroupsAndChannels(ProgressMonitor monitor) {
    
    removeAllGroups();
    TvDataServiceProxy[] services = TvDataServiceProxyManager.getInstance().getDataServices();

    ProgressMonitorGroup progressGroup = new ProgressMonitorGroup(monitor, 30);
    
    ProgressMonitor curMon = progressGroup.getNextProgressMonitor(10);
    curMon.setMaximum(services.length);
    for (int i=0; i<services.length; i++) {
      ChannelGroup[] groupArr = null;
      if (services[i].supportsDynamicChannelGroups()) {
        try {
          groupArr = services[i].checkForAvailableGroups(null);
        } catch (TvBrowserException e) {
          ErrorHandler.handle(e);
       }
      }
      else {
        groupArr = services[i].getAvailableGroups();
      }
      if (groupArr != null) {
        for (int j=0; j<groupArr.length; j++) {
          addGroup(services[i], groupArr[j]);
        }
      }
      curMon.setValue(i+1);
    }

    curMon = progressGroup.getNextProgressMonitor(20);
    curMon.setMaximum(services.length);

    /* Call 'checkForAvailableChannels' for all groups to fetch the most recent channel lists */
    TvDataServiceProxy[] proxies = TvDataServiceProxyManager.getInstance().getDataServices();
    for (int i=0; i<proxies.length; i++) {
      if (proxies[i].supportsDynamicChannelList()) {
        ChannelGroup[] groups = proxies[i].getAvailableGroups();
        int max = groups.length;
        curMon.setMaximum(max);
        for (int j=0; j<max; j++) {
          try {
            proxies[i].checkForAvailableChannels(groups[j], curMon);
            curMon.setValue(j);
          }catch(TvBrowserException e) {
            ErrorHandler.handle(e);
          }
        }
      }
    }
  }

  private String createId(TvDataServiceProxy service, ChannelGroup group) {
    return new StringBuffer(service.getId()).append('.').append(group.getId()).toString();
  }

  public ChannelGroup[] getAvailableGroups() {
    Collection<ChannelGroup> col = mGroups.values();
    return col.toArray(new ChannelGroup[col.size()]);
  }

  /**
   * Returns all Groups for a TvDataServiceProxy
   * @param proxy get Groups for this TvDataService
   * @return ChannelGroups
   */
  public ChannelGroup[] getAvailableGroups(AbstractTvDataServiceProxy proxy) {
    Collection<ChannelGroup> groups = mServiceToGroupsMap.get(proxy);
    return groups.toArray(new ChannelGroup[groups.size()]);
  }
  
  /**
   * Return the groups wich are needed for TV-Browser start.
   * @param proxy The TvDataService to get the groups from.
   * @return The needed ChannelGroups.
   * @since 2.3
   */
  public ChannelGroup[] getUsedGroups(AbstractTvDataServiceProxy proxy) {
    String[] subscribedGroupIds = getUsedGroupIds();
    ArrayList<ChannelGroup> list = mServiceToGroupsMap.get(proxy);
    
    if(subscribedGroupIds == null)
      return list.toArray(new ChannelGroup[list.size()]);
    
    ArrayList<ChannelGroup> list2 = new ArrayList<ChannelGroup>();
    
    for(ChannelGroup group : list) {      
      String id = createId(proxy, group);
      for(int i = 0; i < subscribedGroupIds.length; i++) {
        if(id.compareTo(subscribedGroupIds[i]) == 0) {
          list2.add(group);
          break;
        }
      }
    }
    
    return list2.toArray(new ChannelGroup[list2.size()]);
  }
  
  /**
   *  @return The array with the used groups.
   *  @since 2.3
   */
  private String[] getUsedGroupIds() {
    String[] groupIds = Settings.propUsedChannelGroups.getStringArray();
    
    return groupIds;
  }
}