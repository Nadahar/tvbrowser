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

import util.exc.TvBrowserException;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.ProgressMonitor;

public abstract class AbstractTvDataServiceProxy implements TvDataServiceProxy {

  protected AbstractTvDataServiceProxy() {
    // empty
  }

  public abstract Channel[] checkForAvailableChannels(ChannelGroup group,
      ProgressMonitor monitor) throws TvBrowserException;

  public final Channel[] checkForAvailableChannels(ProgressMonitor monitor)
      throws TvBrowserException {
    ChannelGroup[] groups = ChannelGroupManager.getInstance()
        .getAvailableGroups(this);
    monitor.setMaximum(groups.length);
    ArrayList<Channel> list = new ArrayList<Channel>();
    for (int i = 0; i < groups.length; i++) {
      Channel[] ch = checkForAvailableChannels(groups[i], monitor);
      for (Channel element : ch) {
        list.add(element);
      }
      monitor.setValue(i);
    }
    monitor.setValue(groups.length);
    return list.toArray(new Channel[list.size()]);
  }

  private Channel[] loadChannelsForGroups(ChannelGroup[] groups) {
    ArrayList<Channel> list = new ArrayList<Channel>();
    for (ChannelGroup group : groups) {
      Channel[] ch = getAvailableChannels(group);
      if (ch != null) {
        for (Channel element : ch) {
          list.add(element);
        }
      }
    }
    return list.toArray(new Channel[list.size()]);
  }

  public Channel[] getChannelsForTvBrowserStart() {
    return loadChannelsForGroups(ChannelGroupManager.getInstance()
        .getUsedGroups(this));
  }

  public Channel[] getAvailableChannels() {
    return loadChannelsForGroups(ChannelGroupManager.getInstance()
        .getAvailableGroups(this));
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + ((getId() == null) ? 0 : getId().hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof AbstractTvDataServiceProxy)) {
      return false;
    }
    final TvDataServiceProxy otherProxy = (TvDataServiceProxy) obj;

    if (!getId().equals(otherProxy.getId())) {
      return false;
    }

    return true;
  }

}
