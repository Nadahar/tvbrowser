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

import java.awt.Dimension;
import java.util.WeakHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import tvbrowser.core.Settings;
import devplugin.Channel;

/**
 * A Label for Channels. It shows the Icon and/or the Channel-Name
 */
public class ChannelLabel extends JLabel {

  /** A Icon-Cache for Perfomance-Reasons*/
  static private WeakHashMap<Icon,Icon> ICONCACHE = new WeakHashMap<Icon,Icon>();
  
  static Icon DEFAULT_ICON =  new ImageIcon("./imgs/tvbrowser16.png");
  private boolean mChannelIconsVisible;
  private boolean mTextIsVisible;
  private boolean mShowDefaultValues;
  private boolean mShowCountry;
  
  /**
   * Creates the ChannelLabel
   */
  public ChannelLabel() {
    this(Settings.propShowChannelIconsInChannellist.getBoolean(), Settings.propShowChannelNamesInChannellist.getBoolean(), false);
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
   * @param channelIconsVisible Should the Icon be visible
   * @param textIsVisible Should Text be visible ?
   * @param showDefaultValues Show the default channel icon and name.
   * @param showCountry Show infomation about the country
   *
   * @since 2.6
   */
  public ChannelLabel(boolean channelIconsVisible, boolean textIsVisible, boolean showDefaultValues, boolean showCountry) {
    mChannelIconsVisible = channelIconsVisible;
    mTextIsVisible = textIsVisible;
    mShowDefaultValues = showDefaultValues;
    mShowCountry = showCountry;
  }


  /**
   * Creates the ChannelLabel
   *
   * @param ch Channel to display
   */
  public ChannelLabel(Channel ch) {
    mChannelIconsVisible = Settings.propShowChannelIconsInChannellist.getBoolean();
    mTextIsVisible = Settings.propShowChannelNamesInChannellist.getBoolean();
    setChannel(ch);
  }

  /**
   * Creates the ChannelLabel
   *
   * @param ch Channel to display
   * @param channelIconsVisible Should the Icon be visible
   * @param textIsVisible Should Text be visible ?
   * @since 2.2
   */
  public ChannelLabel(Channel ch, boolean channelIconsVisible, boolean textIsVisible) {
    mChannelIconsVisible = channelIconsVisible;
    mTextIsVisible = textIsVisible;
    setChannel(ch);
  }
  
  /**
   * Creates the ChannelLabel
   *
   * @param ch Channel to display
   * @param channelIconsVisible Should the Icon be visible
   * @since 2.2
   */
  public ChannelLabel(Channel ch, boolean channelIconsVisible) {
    mChannelIconsVisible = channelIconsVisible;
    mTextIsVisible = Settings.propShowChannelNamesInChannellist.getBoolean();
    setChannel(ch);
  }  

  /**
   * Sets the Channel to display
   *
   * @param ch Channel to display
   */
  public void setChannel(Channel ch) {
    if (mChannelIconsVisible) {
      setIcon(mShowDefaultValues ? ch.getDefaultIcon() : ch.getIcon());
    }
    if (mTextIsVisible) {
      StringBuilder text = new StringBuilder(mShowDefaultValues ? ch.getDefaultName() : ch.getName());

      if (mShowCountry) {
        text.append(" (");
        text.append(ch.getCountry());
        text.append(")");
      }

      setText(text.toString());
    }
    setMinimumSize(new Dimension(42,22));
    setToolTipText(ch.getName());
  }

  /**
   * Set the minimum-Size.
   *
   * Overridden to set the Size now
   * @param dim
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
   * Sets the Icon
   * This method immediatly returns without any changes if the Settings.propShowChannelIcons is false
   *
   * @param ic Icon
   */
  public void setIcon(Icon ic) {
    Icon cached = ICONCACHE.get(ic); 
    if (cached != null) {
      super.setIcon(cached);
    } else {
      if (!mChannelIconsVisible) {
        return;
      }
      if (ic == null) {
        ic = getDefaultIcon();
      }
      Icon icon =UiUtilities.createChannelIcon(ic); 
      ICONCACHE.put(ic, icon);
      super.setIcon(icon);
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
}
