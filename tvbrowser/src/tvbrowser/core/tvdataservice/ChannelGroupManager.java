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

import java.util.ArrayList;
import java.util.HashMap;


public class ChannelGroupManager {

  private static ChannelGroupManager mInstance;

  private HashMap mGroups; // key: TvDataServiceProxy;
                           // value: ArrayList of devplugin.ChannelGroup objects

  private ChannelGroupManager() {

  }

  public ChannelGroupManager getInstance() {
    if (mInstance == null) {
      mInstance = new ChannelGroupManager();
    }
    return mInstance;
  }

  public void init() {
    TvDataServiceProxy[] proxies = TvDataServiceProxyManager.getInstance().getDataServices();
    ArrayList list = new ArrayList();
    for (int i=0; i<proxies.length; i++) {
      ChannelGroup[] groups = proxies[i].getAvailableGroups();
      for (int j=0; j<groups.length; j++) {
        list.add(groups[i]);
      }
    }

  }

}
