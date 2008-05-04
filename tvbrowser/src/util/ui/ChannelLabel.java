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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;

import tvbrowser.core.Settings;
import devplugin.Channel;
import util.misc.SoftReferenceCache;

/**
 * A Label for Channels. It shows the Icon and/or the Channel-Name
 */
public class ChannelLabel extends JLabel {

  /** A Icon-Cache for Perfomance-Reasons*/
  static private SoftReferenceCache<Channel,Icon> ICONCACHE = new SoftReferenceCache<Channel,Icon>();
  
  /**
   * default channel icon, already prepared for right size
   */
  static Icon DEFAULT_ICON =  UiUtilities.createChannelIcon(new ImageIcon("./imgs/tvbrowser16.png"));
  
  private boolean mChannelIconsVisible;
  private boolean mTextIsVisible;
  private boolean mShowDefaultValues;
  private boolean mShowCountry;

  private boolean mShowService;

  private Channel mChannel;
  
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
   * @param channel Channel to display
   */
  public void setChannel(Channel channel) {
    mChannel = channel;
    if (mChannelIconsVisible) {
      setChannelIcon(channel, mShowDefaultValues ? channel.getDefaultIcon() : channel.getIcon());
    }
    if (mTextIsVisible) {
      StringBuilder text = new StringBuilder(mShowDefaultValues ? channel.getDefaultName() : channel.getName());

      if (mShowCountry || mShowService) {
        text.append(" (");
      }
      if (mShowCountry) {
        text.append(channel.getCountry());
      }
      if (mShowService) {
        if (mShowCountry) {
          text.append(", ");
        }
        text.append(channel.getDataServiceProxy().getInfo().getName());
      }
      if (mShowCountry || mShowService) {
        text.append(")");
      }

      setText(text.toString());
    }
    setMinimumSize(new Dimension(42,22));
//    setToolTipText(channel.getName());
    setToolTipText("");
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
   * do not call this method, use setChannel instead, which will
   * set an icon matching all the current settings for the selected
   * channel
   *
   * @param ic Icon
   */
  public void setIcon(Icon ic) {
    return;
  }
  
  private void setChannelIcon(Channel channel, Icon icon) {
    Icon cached = null;
    if (icon != null) { // no hash lookup, if no icon to set
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
        Icon resizedIcon =UiUtilities.createChannelIcon(icon); 
        ICONCACHE.put(channel, resizedIcon);
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
   * Clear the icon cache.<br/>
   * This may be useful after lots of channel labels have been
   * created (e.g. in the settings dialog).
   * @since 2.7
   */
  public static void clearIconCache() {
    ICONCACHE.clear();
  }

  @Override
  public JToolTip createToolTip() {
    // don't show tooltip, if disabled in settings
    if (!Settings.propShowChannelTooltipInProgramTable.getBoolean()) {
      return new ToolTipWithIcon(null, null);
    }
    boolean showIcon = false;
    boolean showText = false;
    JToolTip tip;
    Icon channelIcon = mChannel.getIcon();
    if (channelIcon != null && channelIcon instanceof ImageIcon) {
      Icon shownIcon = this.getIcon();
      if (shownIcon != null && (channelIcon.getIconHeight() > shownIcon.getIconHeight() || channelIcon.getIconWidth() > shownIcon.getIconWidth())) {
        showIcon = true;
      }
    }
    if (showIcon) {
      tip = new ToolTipWithIcon((ImageIcon) channelIcon);
//      tip.setMinimumSize(new Dimension(getIcon().getIconWidth() + 2,getIcon().getIconHeight() + 2));
    }
    else {
      tip = new ToolTipWithIcon((ImageIcon)null);
    }
    tip.setBackground(Color.WHITE);
    tip.setComponent(this);
    String text = null;
    if (showText) {
      text = mChannel.getName();
    }
    tip.setTipText(text);
    return tip;
  }

  @Override
  public Point getToolTipLocation(MouseEvent event) {
    FontMetrics metrics = this.getFontMetrics(this.getFont());
    int stringWidth = SwingUtilities.computeStringWidth(metrics, this.getText());
    int x = 0;
    Icon icon = this.getIcon();

    int iconWidth = getIcon().getIconWidth();
    if (mChannel.getIcon() != null) {
      iconWidth = mChannel.getIcon().getIconWidth();
    }

    int iconHeight = getIcon().getIconHeight();
    if (mChannel.getIcon() != null) {
      iconHeight = mChannel.getIcon().getIconHeight();
    }

    if (icon != null) {
      x = (this.getWidth() - stringWidth - this.getIconTextGap() - icon.getIconWidth()) / 2;
      if (x < 0) {
        x = 0;
      }
      x += (icon.getIconWidth() - iconWidth) / 2;
    }
    int y = (this.getHeight() - iconHeight - 2) / 2;
    return new Point(x, y);
  }

}
