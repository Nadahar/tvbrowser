/*
* TV-Browser
* Copyright (C) 2014 TV-Browser-Team (dev@tvbrowser.org)
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
package tvbrowser.core;

import java.util.HashMap;
import java.util.TimeZone;

import javax.swing.ImageIcon;

import util.ui.Localizer;

import devplugin.Channel;
import devplugin.ChannelGroup;

/**
 * A channel that fakes to be another channel to use if original channel is currently unavailable.
 * 
 * @author René Mach
 */
public class DummyChannel extends Channel {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(DummyChannel.class);
  private static final ImageIcon ICON = new ImageIcon("imgs/unknown_channel.png");
  private static final HashMap<String, DummyGroup> DUMMY_GROUP_MAP = new HashMap<String, DummyChannel.DummyGroup>();
  
  private static final DummyGroup getDummyGroup(String groupId) {
    DummyGroup dummyGroup = DUMMY_GROUP_MAP.get(groupId);
    
    if(dummyGroup == null) {
      dummyGroup = new DummyGroup(groupId);
      DUMMY_GROUP_MAP.put(groupId, dummyGroup);
    }
    
    return dummyGroup;
  }
  
  public DummyChannel(String dataServiceId, String groupId, String country, String channelId, String channelName) {
    super(dataServiceId, channelName + " " + LOCALIZER.msg("na","(N/A)"), channelId, TimeZone.getDefault(), country, "\u00A9 " + LOCALIZER.msg("unknown", "Unknown"), LOCALIZER.msg("url", "http://enwiki.tvbrowser.org/index.php/Not_available"), getDummyGroup(groupId), ICON, Channel.CATEGORY_NONE, null, new String[] {country}, null);
  }
  
  private static final class DummyGroup implements ChannelGroup {
    private String mGroupID;
    
    public DummyGroup(String groupID) {
      mGroupID = groupID;
    }
    
    @Override
    public String getName() {
      return LOCALIZER.msg("dummyGroupName", "Dummy Group");
    }

    @Override
    public String getId() {
      return mGroupID;
    }

    @Override
    public String getDescription() {
      return LOCALIZER.msg("dummyGroupDesc", "Dummy group for unknown channels");
    }

    @Override
    public String getProviderName() {
      return LOCALIZER.msg("unknown", "Unknown");
    }
  }
}
