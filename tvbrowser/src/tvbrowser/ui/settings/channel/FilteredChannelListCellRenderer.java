/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import tvbrowser.core.ChannelList;
import tvbrowser.core.DuplicateChannelNameCounter;
import util.ui.ChannelLabel;
import devplugin.Channel;

/**
 * Creates a new Channellistrender.
 * If the Filter doesn't match, the Channel is disabled
 */
public class FilteredChannelListCellRenderer extends DefaultListCellRenderer {
  /** Internal reused ChannelLabel */
  private ChannelLabel mChannel;
  
  private ChannelFilter mFilter;

  private DuplicateChannelNameCounter mChannelCounter;

  public FilteredChannelListCellRenderer(ChannelFilter filter) {
    mFilter = filter;
  }
  
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
      boolean cellHasFocus) {
    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    if (mChannel == null) {
      mChannel = new ChannelLabel(true, true);
    }

    if (mChannelCounter == null) {
      mChannelCounter = new DuplicateChannelNameCounter(ChannelList.getAvailableChannels());
    }

    mChannel.setShowCountry(mChannelCounter.isDuplicate((Channel)value));
    mChannel.setShowService(mChannelCounter.isDuplicateIncludingCountry((Channel)value));

    if (value instanceof Channel) {
      mChannel.setChannel((Channel) value);
      mChannel.setOpaque(isSelected);
      mChannel.setBackground(label.getBackground());
      mChannel.setForeground(label.getForeground());
      mChannel.setEnabled(mFilter.accept((Channel)value));
      
      return mChannel;
    }

    return label;
  }
  
  public Component getListComponent() {
   return mChannel;
  }
  
  
}
