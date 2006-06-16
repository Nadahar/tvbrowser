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

  }

  public abstract Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) throws TvBrowserException;

  public final Channel[] checkForAvailableChannels(ProgressMonitor monitor) throws TvBrowserException {
    ChannelGroup[] groups = ChannelGroupManager.getInstance().getAvailableGroups(this);
    monitor.setMaximum(groups.length);
    ArrayList<Channel> list = new ArrayList<Channel>();
    for (int i=0; i<groups.length; i++) {
      Channel[] ch = checkForAvailableChannels(groups[i], monitor);
      for (int k=0; k<ch.length; k++) {
        list.add(ch[k]);
      }
      monitor.setValue(i);
    }
    monitor.setValue(groups.length);
    return (Channel[])list.toArray(new Channel[list.size()]);
  }


  public Channel[] getAvailableChannels() {
    ChannelGroup[] groups = ChannelGroupManager.getInstance().getAvailableGroups(this);
    ArrayList<Channel> list = new ArrayList<Channel>();
    for (int i=0; i<groups.length; i++) {
      Channel[] ch = getAvailableChannels(groups[i]);
      if (ch != null) {
        for (int k=0; k<ch.length; k++) {
          list.add(ch[k]);
        }
      }
    }
    return (Channel[])list.toArray(new Channel[list.size()]);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + ((getId() == null) ? 0 : getId().hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final TvDataServiceProxy other = (TvDataServiceProxy) obj;
    
    if (!getId().equals(other.getId())) {
      return false;
    }
    
    return true;
  }

  
}
