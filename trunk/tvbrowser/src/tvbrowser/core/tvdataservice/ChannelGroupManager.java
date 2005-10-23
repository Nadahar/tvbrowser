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

import devplugin.ChannelGroup;
import devplugin.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collection;

import tvbrowser.core.Settings;
import util.exc.TvBrowserException;
import util.exc.ErrorHandler;


public class ChannelGroupManager {

  private static String[] DEFAULT_SUBSCRIBED_GROUPS = new String[]{
          "tvbrowserdataservice.TvBrowserDataService.main",
          "tvbrowserdataservice.TvBrowserDataService.local",
          "tvbrowserdataservice.TvBrowserDataService.others",
          "tvbrowserdataservice.TvBrowserDataService.austria",
          "tvbrowserdataservice.TvBrowserDataService.bodostv",
          "tvbrowserdataservice.TvBrowserDataService.digital",
          "tvbrowserdataservice.TvBrowserDataService.radio",
  };

  private static java.util.logging.Logger mLog
           = java.util.logging.Logger.getLogger(ChannelGroupManager.class.getName());


  private static ChannelGroupManager mInstance;

  private HashMap mServiceToGroupsMap; // key: TvDataServiceProxy;
                                       // value: Array of devplugin.ChannelGroup objects

  private HashMap mGroups;  // key: groupId
                            // value: devplugin.ChannelGroup

  private HashMap mGroupToService;   // key: devplugin.ChannelGroup
                                     // value: TvDataServiceProxy




  private ChannelGroupManager() {
    mGroups = new HashMap();
    mServiceToGroupsMap = new HashMap();
    mGroupToService = new HashMap();
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
    ArrayList groups = (ArrayList)mServiceToGroupsMap.get(service);
    if (groups == null) {
      groups = new ArrayList();
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

  public void subscribeGroup(ChannelGroup group) {
    String[] groupIds = getSubscribedGroupIds();
    TvDataServiceProxy service = getTvDataService(group);
    if (service != null) {
      String id = createId(service, group);
      ArrayList list = new ArrayList(Arrays.asList(groupIds));
      if (!list.contains(group.getId())) {
        list.add(id);
      }
      groupIds = (String[])list.toArray(new String[list.size()]);
      Settings.propSubscribedChannelGroups.setStringArray(groupIds);
    }
  }




  public void unsubscribeGroup(ChannelGroup group) {
    String[] groupIds = getSubscribedGroupIds();
    TvDataServiceProxy service = getTvDataService(group);
    if (service != null) {
      String id = createId(service, group);
      ArrayList list = new ArrayList(Arrays.asList(groupIds));
      list.remove(id);
      groupIds = (String[])list.toArray(new String[list.size()]);
      Settings.propSubscribedChannelGroups.setStringArray(groupIds);
    }
  }

  public ChannelGroup[] getSubscribedGroups() {
    String[] groupIds = getSubscribedGroupIds();
    ArrayList list = new ArrayList();
    for (int i=0; i<groupIds.length; i++) {
      ChannelGroup g = (ChannelGroup)mGroups.get(groupIds[i]);
      if (g != null) {
        list.add(g);
      }
    }

    return (ChannelGroup[])list.toArray(new ChannelGroup[list.size()]);
  }


  public ChannelGroup[] getSubscribedGroups(TvDataServiceProxy proxy) {
    String[] subscribedGroupIds = getSubscribedGroupIds();
    ChannelGroup[] availableGroups = (ChannelGroup[])mServiceToGroupsMap.get(proxy);
    if (availableGroups == null) {
      return new ChannelGroup[]{};
    }

    ArrayList list = new ArrayList();
    for (int i=0; i<subscribedGroupIds.length; i++) {
      for (int k=0; k<availableGroups.length; k++) {
        if (subscribedGroupIds[i].equals(availableGroups[k].getId())) {
          list.add(availableGroups[k]);
        }
      }
    }

    return (ChannelGroup[])list.toArray(new ChannelGroup[list.size()]);
  }


  public void checkForAvailableGroups() {
    removeAllGroups();
    TvDataServiceProxy[] services = TvDataServiceProxyManager.getInstance().getDataServices();
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
    }

    TvDataServiceProxy[] proxies = TvDataServiceProxyManager.getInstance().getDataServices();
    for (int i=0; i<proxies.length; i++) {
      if (proxies[i].supportsDynamicChannelList()) {
        try {
          proxies[i].checkForAvailableChannels(null);
        } catch (TvBrowserException e) {
          ErrorHandler.handle(e);
        }
      }
    }
  }

  public ChannelGroup[] getAvailableGroups() {
    Collection col = mGroups.values();

    return (ChannelGroup[])col.toArray(new ChannelGroup[col.size()]);

  }

  private String createId(TvDataServiceProxy service, ChannelGroup group) {
    return service.getId()+"."+group.getId();
  }

  private String[] getSubscribedGroupIds() {
    String[] groupIds = Settings.propSubscribedChannelGroups.getStringArray();
    if (groupIds == null) {
      groupIds = DEFAULT_SUBSCRIBED_GROUPS;
    }
    return groupIds;
  }

}
