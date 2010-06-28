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
package util.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import tvbrowser.core.ChannelList;
import tvbrowser.core.DuplicateChannelNameCounter;
import devplugin.Channel;

/**
 * A ListCellRenderer for Channel-Lists
 */
public class ChannelListCellRenderer extends DefaultListCellRenderer {
  /** Internal reused ChannelLabel */
  private ChannelLabel mChannel;

  private DuplicateChannelNameCounter mChannelCounter;

  private boolean mChannelIconsVisible;
  private boolean mTextVisible;
  private boolean mDefaultValues;
  private boolean mShowCountry;
  private Channel[] mChannels;

  public ChannelListCellRenderer() {
    this(true,false,false);
  }

  public ChannelListCellRenderer(boolean channelIconsVisible) {
    this(channelIconsVisible,false, false, false);
  }

  public ChannelListCellRenderer(boolean channelIconsVisible, boolean textVisible) {
    this(channelIconsVisible,textVisible,false);
  }
  
  public ChannelListCellRenderer(boolean channelIconsVisible, boolean textVisible, boolean defaultValues) {
    this(channelIconsVisible,textVisible,defaultValues, false);
  }

  public ChannelListCellRenderer(boolean channelIconsVisible, boolean textVisible, boolean defaultValues, boolean showCountry) {
    this(channelIconsVisible,textVisible,defaultValues, showCountry, null);
  }

  /**
   * Create Renderer
   * 
   * @param channelIconsVisible
   *          show Channel Icon?
   * @param textVisible
   *          show Channel Name?
   * @param defaultValues
   *          show Default Channel Name?
   * @param showCountry
   *          show Country Information if channel name is a duplicate?
   * @since 2.6
   */
  public ChannelListCellRenderer(boolean channelIconsVisible, boolean textVisible, boolean defaultValues, boolean showCountry, Channel[] channels) {
    mChannelIconsVisible = channelIconsVisible;
    mTextVisible = textVisible;
    mDefaultValues = defaultValues;
    mShowCountry = showCountry;
    mChannels = channels;
  }

  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
      boolean cellHasFocus) {
    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    if (mChannel == null) {
      mChannel = new ChannelLabel(mChannelIconsVisible, mTextVisible,mDefaultValues);
    }

    if (mShowCountry) {
      if (mChannelCounter == null) {
        mChannelCounter = new DuplicateChannelNameCounter(ChannelList.getAvailableChannels());
      }

      if(value instanceof Channel) {
        mChannel.setShowCountry(mChannelCounter.isDuplicate((Channel)value));
        mChannel.setShowService(mChannelCounter.isDuplicateIncludingCountry((Channel)value));
      }
    }

    if (value instanceof Channel) {
      mChannel.setChannel((Channel) value);
      mChannel.setOpaque(isSelected);
      mChannel.setBackground(label.getBackground());
      mChannel.setForeground(label.getForeground());

      boolean found = (mChannels == null);
      if (mChannels != null) {
        for (Channel mChannel2 : mChannels) {
          if (mChannel2.equals(value)) {
            found = true;
            break;
          }
        }
      }
      mChannel.setEnabled(found);
      return mChannel;
    }

    return label;
  }
  
  public Component getListComponent() {
   return mChannel;
  }
  
  public void setChannels(Channel[] channels) {
    mChannels = channels;
  }
}