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
*     $Date$
*   $Author$
* $Revision$
*/
package util.ui;

import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JLabel;

import devplugin.Channel;
import tvbrowser.core.DummyChannel;
import tvbrowser.core.Settings;
import util.misc.SoftReferenceCache;

/**
 * A Label for Channels. It shows the Icon and/or the Channel-Name
 */
public class ChannelLabel extends JLabel {

  /** A Icon-Cache for Performance-Reasons */
  static private SoftReferenceCache<Channel,Icon> ICONCACHE = new SoftReferenceCache<Channel,Icon>();
    
  /**
   * default channel icon, already prepared for right size
   */
  static Icon DEFAULT_ICON =  UiUtilities.createChannelIcon(TVBrowserIcons.defaultChannelLogo());
  
  private boolean mChannelIconsVisible;
  private boolean mTextIsVisible;
  private boolean mShowDefaultValues;
  private boolean mShowCountry;

  private boolean mShowService;
  private boolean mShowJointChannelInfo;
  private boolean mShowTimeLimitation;
  
  private boolean mShowSortNumber;
  private boolean mPaintBackground;

  private Channel mChannel;
  private static TimeFormatter mTimeFormatter;
  
  /**
   * Creates the ChannelLabel
   */
  public ChannelLabel() {
    this(Settings.propShowChannelIconsInChannellist.getBoolean(), Settings.propShowChannelNamesInChannellist.getBoolean(), false, false, false, false, Settings.propShowSortNumberInProgramLists.getBoolean());
  }

  /**
   * Creates the ChannelLabel
   * 
   * @param channelIconsVisible Should the Icon be visible
   */
  public ChannelLabel(boolean channelIconsVisible) {
    this(channelIconsVisible,true,false);
  }

  /**
   * Creates the ChanelLabel
   * 
   * @param channelIconsVisible Should the Icon be visible
   * @param textIsVisible Should Text be visible ?
   */
  public ChannelLabel(boolean channelIconsVisible, boolean textIsVisible) {
    this(channelIconsVisible,textIsVisible,false);
  }

  /**
   * Creates the ChanelLabel
   * 
   * @param channelIconsVisible Should the Icon be visible
   * @param textIsVisible Should Text be visible ?
   * @param showDefaultValues Show the default channel icon and name.
   */
  public ChannelLabel(boolean channelIconsVisible, boolean textIsVisible, boolean showDefaultValues) {
    this(channelIconsVisible, textIsVisible, showDefaultValues, false);
  }

  /**
   * Creates the ChanelLabel
   * 
   * @param channelIconsVisible
   *          Should the Icon be visible
   * @param textIsVisible
   *          Should Text be visible ?
   * @param showDefaultValues
   *          Show the default channel icon and name.
   * @param showCountry
   *          Show information about the country
   * 
   * @since 2.6
   */
  public ChannelLabel(boolean channelIconsVisible, boolean textIsVisible, boolean showDefaultValues, boolean showCountry) {
    this(channelIconsVisible, textIsVisible, showDefaultValues, showCountry, false);
  }
  
  /**
   * Creates the ChanelLabel
   * 
   * @param channelIconsVisible
   *          Should the Icon be visible
   * @param textIsVisible
   *          Should Text be visible ?
   * @param showDefaultValues
   *          Show the default channel icon and name.
   * @param showCountry
   *          Show information about the country
   * @param showJoinedChannelInfo If the joined channel name and icon should be shown.
   * 
   * @since 3.2.1
   */
  public ChannelLabel(boolean channelIconsVisible, boolean textIsVisible, boolean showDefaultValues, boolean showCountry, boolean showJoinedChannelInfo) {
    this(channelIconsVisible, textIsVisible, showDefaultValues, showCountry, showJoinedChannelInfo, false);
  }
      

  /**
   * Creates the ChanelLabel
   * 
   * @param channelIconsVisible
   *          Should the Icon be visible
   * @param textIsVisible
   *          Should Text be visible ?
   * @param showDefaultValues
   *          Show the default channel icon and name.
   * @param showCountry
   *          Show information about the country
   * @param showJoinedChannelInfo If the joined channel name and icon should be shown.
   * @param showTimeLimitation If the time limitations should be shown.
   * 
   * @since 3.2.1
   */
  public ChannelLabel(boolean channelIconsVisible, boolean textIsVisible, boolean showDefaultValues, boolean showCountry, boolean showJoinedChannelInfo, boolean showTimeLimitation) {
    this(channelIconsVisible, textIsVisible, showDefaultValues, showCountry, showJoinedChannelInfo, showTimeLimitation, Settings.propShowSortNumberInProgramLists.getBoolean());
  }
  
  /**
   * Creates the ChanelLabel
   * 
   * @param channelIconsVisible
   *          Should the Icon be visible
   * @param textIsVisible
   *          Should Text be visible ?
   * @param showDefaultValues
   *          Show the default channel icon and name.
   * @param showCountry
   *          Show information about the country
   * @param showJoinedChannelInfo If the joined channel name and icon should be shown.
   * @param showTimeLimitation If the time limitations should be shown.
   * @param showSortNumber If the sort number (if available) should be shown.
   * 
   * @since 3.3.4
   */
  public ChannelLabel(boolean channelIconsVisible, boolean textIsVisible, boolean showDefaultValues, boolean showCountry, boolean showJoinedChannelInfo, boolean showTimeLimitation, boolean showSortNumber) {
    this(channelIconsVisible,textIsVisible,showDefaultValues,showCountry,showJoinedChannelInfo,showTimeLimitation,showSortNumber,false);
  }
  
  /**
   * Creates the ChanelLabel
   * 
   * @param channelIconsVisible
   *          Should the Icon be visible
   * @param textIsVisible
   *          Should Text be visible ?
   * @param showDefaultValues
   *          Show the default channel icon and name.
   * @param showCountry
   *          Show information about the country
   * @param showJoinedChannelInfo If the joined channel name and icon should be shown.
   * @param showTimeLimitation If the time limitations should be shown.
   * @param showSortNumber If the sort number (if available) should be shown.
   * @param paintBackground If the background color of the channel label should be shown.
   * 
   * @since 3.4.5
   */
  public ChannelLabel(boolean channelIconsVisible, boolean textIsVisible, boolean showDefaultValues, boolean showCountry, boolean showJoinedChannelInfo, boolean showTimeLimitation, boolean showSortNumber, boolean paintBackground) {
    mChannelIconsVisible = channelIconsVisible;
    mTextIsVisible = textIsVisible;
    mShowDefaultValues = showDefaultValues;
    mShowCountry = showCountry;
    mShowJointChannelInfo = showJoinedChannelInfo;
    mShowTimeLimitation = showTimeLimitation;
    mShowSortNumber = showSortNumber;
    mPaintBackground = paintBackground;
  }


  /**
   * Creates the ChannelLabel
   *
   * @param channel Channel to display
   */
  public ChannelLabel(Channel channel) {
  	this(channel, Settings.propShowChannelIconsInChannellist.getBoolean(),Settings.propShowChannelNamesInChannellist.getBoolean());
  }

  /**
   * Creates the ChannelLabel
   *
   * @param channel Channel to display
   * @param channelIconsVisible Should the Icon be visible
   * @param textIsVisible Should Text be visible ?
   * @since 2.2
   */
  public ChannelLabel(Channel channel, boolean channelIconsVisible, boolean textIsVisible) {
    this(channelIconsVisible,textIsVisible);
    setChannel(channel);
  }
  
  /**
   * Creates the ChannelLabel
   *
   * @param channel Channel to display
   * @param channelIconsVisible Should the Icon be visible
   * @since 2.2
   */
  public ChannelLabel(Channel channel, boolean channelIconsVisible) {
  	this(channel, channelIconsVisible, Settings.propShowChannelNamesInChannellist.getBoolean());
  }

  /**
   * Sets the Channel to display
   *
   * @param channel Channel to display
   */
  public void setChannel(Channel channel) {
    mChannel = channel;
    if (mChannelIconsVisible) {
      setChannelIcon(channel, (mShowJointChannelInfo && channel.getJointChannel() != null) ? channel.getJointChannelIcon() : (mShowDefaultValues ? channel.getDefaultIcon() : channel.getIcon()));
    }
    if (mTextIsVisible) {
      StringBuilder text = new StringBuilder((mShowJointChannelInfo && channel.getJointChannel() != null) ? channel.getJointChannelName() : (mShowDefaultValues ? channel.getDefaultName() : channel.getName()));

      if (mShowCountry || mShowService || mShowTimeLimitation) {
        text.append(" (");
      }
      if (mShowCountry) {
        text.append(channel.getBaseCountry());
      }
      if (mShowService && !(channel instanceof DummyChannel)) {
        if (mShowCountry) {
          text.append(", ");
        }
        text.append(channel.getDataServiceProxy().getInfo().getName());
      }
      if(mShowTimeLimitation) {
        if (mShowService || mShowCountry) {
          text.append(", ");
        }
        
        if(mTimeFormatter == null) {
          mTimeFormatter = new TimeFormatter(Settings.getTimePattern());
        }
        
        text.append(mTimeFormatter.formatTime(channel.getStartTimeLimit() / 60, channel.getStartTimeLimit() % 60)).append("-").append(mTimeFormatter.formatTime(channel.getEndTimeLimit() / 60, channel.getEndTimeLimit() % 60));
      }
      if (mShowCountry || mShowService || mShowTimeLimitation) {
        text.append(")");
      }
      
      if(mShowSortNumber && channel.getSortNumber().trim().length() > 0) {
        text.insert(0, channel.getSortNumber().trim() + ". ");
      }

      setText(text.toString());
    }
    else if(mShowSortNumber && channel.getSortNumber().trim().length() > 0) {
      setText(channel.getSortNumber().trim() + ". ");
    }
    
    setMinimumSize(new Dimension(42,22));
    setToolTipText(channel.getName());
  }

  /**
   * Set the minimum-Size.
   *
   * Overridden to set the Size now
   * @param dim The dimension.
   */
  public void setMinimumSize(Dimension dim) {
    super.setMinimumSize(dim);
    Dimension current = getSize();
    if (current.width < dim.width) {
      current.width = dim.width;
    }
    if (current.height < dim.height) {
      current.height = dim.height;
    }
    setSize(current);
  }

  /**
   * do not call this method, use setChannel instead, which will
   * set an icon matching all the current settings for the selected
   * channel
   *
   * @param ic Icon
   */
  public void setIcon(Icon ic) {
    // empty
  }
  
  private void setChannelIcon(Channel channel, Icon icon) {
    Icon cached = null;
    if (icon != null && !mShowJointChannelInfo) { // no hash lookup, if no icon to set
      cached = ICONCACHE.get(channel);
    }
    if (cached != null) {
      super.setIcon(cached);
    } else {
      if (!mChannelIconsVisible) {
        return;
      }
      if (icon == null) { // do not cache the default icon
        super.setIcon(getDefaultIcon());
      }
      else {
        Icon resizedIcon = UiUtilities.createChannelIcon(icon);
        
        if(channel.getJointChannel() == null && !(channel instanceof DummyChannel)) {
          ICONCACHE.put(channel, resizedIcon);
        }
        
        super.setIcon(resizedIcon);
      }
    }
  }

  /**
   * Returns the Default-Icon
   * @return default-icon
   */
  private Icon getDefaultIcon() {
    return DEFAULT_ICON;
  }

  /**
   * Should the country be added to the channel name ?
   * @param showCountry add country name to channel name ?
   * @since 2.6
   */
  public void setShowCountry(boolean showCountry) {
    mShowCountry = showCountry;
  }

  /**
   * Should the service name be added to the channel name ?
   * @param showService add service name to channel name ?
   * @since 2.6
   */
  public void setShowService(boolean showService) {
    mShowService = showService;
  }
  
  /**
   * Should the time limitation info shown in label.
   * <p>
   * @param showTimeLimitation If the time limitation should be shown.
   * @since 3.2.1
   */
  public void setShowTimeLimitation(boolean showTimeLimitation) {
    mShowTimeLimitation = showTimeLimitation;
  }
  
  /**
   * Clear the icon cache.<br>
   * This may be useful after lots of channel labels have been
   * created (e.g. in the settings dialog).
   * @since 2.7
   */
  public static void clearIconCache() {
    ICONCACHE.clear();
  }

  /**
   * @return The current Channel
   * @since 3.0
   */
  public Channel getChannel() {
    return mChannel;
  }
  
  public void setEnabled(boolean enabled, boolean selected) {
    if(!selected) {
      setEnabled(enabled);
    }
    else {
      setEnabled(true);
    }
  }
  
  /**
   * @return If the background should be painted
   */
  public boolean isBackgroundToPaint() {
    return mPaintBackground && mChannel.isUsingUserBackgroundColor();
  }
}
