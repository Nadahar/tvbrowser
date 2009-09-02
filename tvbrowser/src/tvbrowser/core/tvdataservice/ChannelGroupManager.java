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

import tvbrowser.core.Settings;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.progress.ProgressMonitorGroup;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.ProgressMonitor;

public class ChannelGroupManager {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ChannelGroupManager.class);
  private static ChannelGroupManager mInstance;

  private HashMap<TvDataServiceProxy, ArrayList<ChannelGroup>> mServiceToGroupsMap; // key:
                                                                                    // TvDataServiceProxy;
  // value: Array of devplugin.ChannelGroup objects

  private HashMap<String, ChannelGroup> mGroups; // key: groupId
  // value: devplugin.ChannelGroup

  private HashMap<ChannelGroup, TvDataServiceProxy> mGroupToService; // key:
                                                                     // devplugin.ChannelGroup

  // value: TvDataServiceProxy

  private ChannelGroupManager() {
    mGroups = new HashMap<String, ChannelGroup>();
    mServiceToGroupsMap = new HashMap<TvDataServiceProxy, ArrayList<ChannelGroup>>();
    mGroupToService = new HashMap<ChannelGroup, TvDataServiceProxy>();
    TvDataServiceProxy[] proxies = TvDataServiceProxyManager.getInstance().getDataServices();

    for (TvDataServiceProxy proxy : proxies) {
      ChannelGroup[] groups = proxy.getAvailableGroups();
      for (ChannelGroup group : groups) {
        addGroup(proxy, group);
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
    ArrayList<ChannelGroup> groups = mServiceToGroupsMap.get(service);
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
    return mGroupToService.get(group);
  }

  /**
   * Refresh the list of available groups and refresh the lists of available
   * channels
   * 
   * @param monitor
   *          Progress monitor that shows the current status of the refresh
   */
  public void checkForAvailableGroupsAndChannels(ProgressMonitor monitor) {

    removeAllGroups();
    TvDataServiceProxy[] services = TvDataServiceProxyManager.getInstance().getDataServices();

    ProgressMonitorGroup progressGroup = new ProgressMonitorGroup(monitor, 30);

    ProgressMonitor curMon = progressGroup.getNextProgressMonitor(10);
    curMon.setMaximum(services.length);
    for (int i = 0; i < services.length; i++) {
      ChannelGroup[] groupArr = null;
      if (services[i].supportsDynamicChannelGroups()) {
        try {
          groupArr = services[i].checkForAvailableGroups(null);
        } catch (TvBrowserException e) {
          ErrorHandler.handle(e);
        }
      } else {
        groupArr = services[i].getAvailableGroups();
      }
      if (groupArr != null) {
        for (ChannelGroup aGroupArr : groupArr) {
          addGroup(services[i], aGroupArr);
        }
      }
      curMon.setValue(i + 1);
    }

    curMon = progressGroup.getNextProgressMonitor(20);
    curMon.setMaximum(services.length);

    int channelCount = 0;

    /*
     * Call 'checkForAvailableChannels' for all groups to fetch the most recent
     * channel lists
     */
    TvDataServiceProxy[] proxies = TvDataServiceProxyManager.getInstance().getDataServices();
    for (TvDataServiceProxy proxy : proxies) {
      if (proxy.supportsDynamicChannelList()) {
        ChannelGroup[] groups = proxy.getAvailableGroups();
        int max = groups.length;
        curMon.setMaximum(max);
        for (int j = 0; j < max; j++) {
          try {
            final String channelCountString = Integer.toString(channelCount);
            final ProgressMonitor finalMonitor = curMon;
            // use a proxy progress monitor to be able to add the number of
            // channels found so far
            Channel[] channels = proxy.checkForAvailableChannels(groups[j], new ProgressMonitor() {

              @Override
              public void setValue(int value) {
                finalMonitor.setValue(value);
              }

              @Override
              public void setMessage(String msg) {
                finalMonitor.setMessage(msg + " "
                    + mLocalizer.msg("channelCount", "(Found {0} channels)", channelCountString));
              }

              @Override
              public void setMaximum(int maximum) {
                finalMonitor.setMaximum(maximum);
              }
            });
            if (channels != null) {
              channelCount += channels.length;
            }
            curMon.setValue(j);
          } catch (TvBrowserException e) {
            ErrorHandler.handle(e);
          }
        }
      }
    }
  }

  private String createId(TvDataServiceProxy service, ChannelGroup group) {
    return new StringBuilder(service.getId()).append('.').append(group.getId()).toString();
  }

  public ChannelGroup[] getAvailableGroups() {
    Collection<ChannelGroup> col = mGroups.values();
    return col.toArray(new ChannelGroup[col.size()]);
  }

  /**
   * Returns all Groups for a TvDataServiceProxy
   * 
   * @param proxy
   *          get Groups for this TvDataService
   * @return ChannelGroups
   */
  public ChannelGroup[] getAvailableGroups(AbstractTvDataServiceProxy proxy) {
    Collection<ChannelGroup> groups = mServiceToGroupsMap.get(proxy);
    if (groups == null) {
      return new ChannelGroup[0];
    }
    return groups.toArray(new ChannelGroup[groups.size()]);
  }

  /**
   * Return the groups which are needed for TV-Browser start.
   * 
   * @param proxy
   *          The TvDataService to get the groups from.
   * @return The needed ChannelGroups.
   * @since 2.3
   */
  public ChannelGroup[] getUsedGroups(AbstractTvDataServiceProxy proxy) {
    String[] subscribedGroupIds = getUsedGroupIds();
    ArrayList<ChannelGroup> groups = mServiceToGroupsMap.get(proxy);
    if (groups == null) {
      return new ChannelGroup[0];
    }

    if (subscribedGroupIds == null) {
      return groups.toArray(new ChannelGroup[groups.size()]);
    }

    ArrayList<ChannelGroup> result = new ArrayList<ChannelGroup>();

    for (ChannelGroup group : groups) {
      String id = createId(proxy, group);
      for (String subscribedGroupId : subscribedGroupIds) {
        if (id.equals(subscribedGroupId)) {
          result.add(group);
          break;
        }
      }
    }

    return result.toArray(new ChannelGroup[result.size()]);
  }

  /**
   * @return The array with the used groups.
   * @since 2.3
   */
  private String[] getUsedGroupIds() {
    return Settings.propUsedChannelGroups.getStringArray();
  }
}