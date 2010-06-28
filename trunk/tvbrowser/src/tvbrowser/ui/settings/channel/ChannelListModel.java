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
package tvbrowser.ui.settings.channel;

import java.util.Collection;
import java.util.HashSet;

import tvbrowser.core.ChannelList;
import devplugin.Channel;

public class ChannelListModel {

  private HashSet<Channel> mSubscribedChannels;

  private Channel[] mAvailableChannels;

  public ChannelListModel() {
    mSubscribedChannels = new HashSet<Channel>();
    refresh();
  }

  public void subscribeChannel(Channel ch) {
    mSubscribedChannels.add(ch);
  }

  public void unsubscribeChannel(Channel ch) {
    mSubscribedChannels.remove(ch);
  }

  public void refresh() {
    ChannelList.reload();
    Channel[] channels = ChannelList.getSubscribedChannels();
    mAvailableChannels = ChannelList.getAvailableChannels();
    mSubscribedChannels.clear();
    for (Channel channel : channels) {
      subscribeChannel(channel);
    }
  }

  public boolean isSubscribed(Channel ch) {
    return mSubscribedChannels.contains(ch);
  }

  public Channel[] getAvailableChannels() {
    return mAvailableChannels;
  }

  public Collection<Channel> getSubscribedChannels() {
    return mSubscribedChannels;
  }

}