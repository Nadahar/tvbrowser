/*
 * TV-Browser Compat
 * Copyright (C) 2017 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date: 2014-06-17 15:59:09 +0200 (Di, 17 Jun 2014) $
 *   $Author: ds10 $
 * $Revision: 8152 $
 */
package compat;

import java.lang.reflect.Method;

import javax.swing.Icon;

import devplugin.Channel;

/**
 * Compatibility class for TV-Browser devplugin.Channel class.
 * 
 * @author René Mach
 * @since 0.2
 */
public final class ChannelCompat {
  /**
   * @param channel The channel to check for base channel
   * @return The base channel for the given channel or <code>null</code> if
   * there isn't a base channel.
   */
  public static Channel getBaseChannel(final Channel channel) {
    Channel result = null;
    
    if(VersionCompat.isJointChannelSupported()) {
      try {
        Method m = Channel.class.getDeclaredMethod("getBaseChannel");
        result = (Channel)m.invoke(channel);
      }catch(Exception e) {}
    }
    
    return result;
  }
  
  /**
   * @param channel The channel to check for joint channel
   * @return The joint channel for the given channel or <code>null</code> if
   * there isn't a joint channel.
   */
  public static Channel getJointChannel(final Channel channel) {
    Channel result = null;
    
    if(VersionCompat.isJointChannelSupported()) {
      try {
        Method m = Channel.class.getDeclaredMethod("getJointChannel");
        result = (Channel)m.invoke(channel);
      }catch(Exception e) {}
    }
    
    return result;
  }
  
  /**
   * @param channel The channel to get the joint channel name for.
   * @return The joint channel name for the given channel or <code>null</code> if
   * there isn't a joint channel.
   */
  public static String getJointChannelName(final Channel channel) {
    String result = null;
    
    if(VersionCompat.isJointChannelSupported()) {
      try {
        Method m = Channel.class.getDeclaredMethod("getJointChannelName");
        result = (String)m.invoke(channel);
      }catch(Exception e) {}
    }
    
    return result;
  }
  
  /**
   * Gets the joint channel icon of this channel
   * <p>
   * @return The joint channel icon of this channel or <code>null</code> if there is no joint channel icon.
   */
  public static Icon getJointChannelIcon(final Channel channel) {
    Icon result = null;
    
    if(VersionCompat.isJointChannelSupported()) {
      try {
        Method m = Channel.class.getDeclaredMethod("getJointChannelIcon");
        result = (Icon)m.invoke(channel);
      }catch(Exception e) {}
    }
    
    return result;
  }
}
