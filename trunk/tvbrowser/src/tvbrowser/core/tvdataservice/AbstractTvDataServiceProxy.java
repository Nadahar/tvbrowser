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
import devplugin.ProgressMonitor;
import tvbrowser.core.Settings;

import java.util.ArrayList;

import util.exc.TvBrowserException;


public abstract class AbstractTvDataServiceProxy implements TvDataServiceProxy {

  protected AbstractTvDataServiceProxy() {

  }

  public final ChannelGroup[] getSubscribedGroups()  {
    ChannelGroup[] availableGroups = getAvailableGroups();

    String[] groupIds = Settings.propSubscribedChannelGroups.getStringArray();
    if (groupIds == null) {
      return getAvailableGroups();
    }
    ArrayList list = new ArrayList();
    for (int i=0; i<availableGroups.length; i++) {
      ChannelGroup group = availableGroups[i];
      if (group != null) {
        for (int j=0; j<groupIds.length; j++) {
          if (groupIds[j].equals(getId()+"."+group.getId())) {
            list.add(group);
          }
        }
      }
    }
    ChannelGroup[] subscribedGroups = (ChannelGroup[])list.toArray(new ChannelGroup[list.size()]);
    return subscribedGroups;
  }

  protected abstract Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) throws TvBrowserException;

  public final Channel[] checkForAvailableChannels(ProgressMonitor monitor) throws TvBrowserException {
    ChannelGroup[] groups = getSubscribedGroups();
    ArrayList list = new ArrayList();
    for (int i=0; i<groups.length; i++) {
      Channel[] ch = checkForAvailableChannels(groups[i], monitor);
      for (int k=0; k<ch.length; k++) {
        list.add(ch[k]);
      }
    }
    return (Channel[])list.toArray(new Channel[list.size()]);
  }


  protected abstract Channel[] getAvailableChannels(ChannelGroup group);


  public Channel[] getAvailableChannels() {
    ChannelGroup[] groups = getSubscribedGroups();
    ArrayList list = new ArrayList();
    for (int i=0; i<groups.length; i++) {
      Channel[] ch = getAvailableChannels(groups[i]);
      for (int k=0; k<ch.length; k++) {
        list.add(ch[k]);
      }
    }
    return (Channel[])list.toArray(new Channel[list.size()]);
  }

}
