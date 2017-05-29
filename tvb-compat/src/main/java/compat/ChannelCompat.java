package compat;

import java.lang.reflect.Method;

import javax.swing.Icon;

import devplugin.Channel;

public final class ChannelCompat {
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
