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
    ChannelGroup[] groups = ChannelGroupManager.getInstance().getSubscribedGroups(this);
    monitor.setMaximum(groups.length);
    ArrayList list = new ArrayList();
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
    ChannelGroup[] groups = ChannelGroupManager.getInstance().getSubscribedGroups(this);
    ArrayList list = new ArrayList();
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

}
