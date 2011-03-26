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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import devplugin.Channel;

/**
 * A class that contains informations about a channel.
 * Used to keep channel values even if channel is
 * currently not available.
 * 
 * @author René Mach
 */
public class ChannelItem {
  private String mChannelDataServiceId;
  private String mCertainChannelId;
  private String mGroupId;
  private String mCountry;
  private Channel mChannel;
  
  private boolean mNullChannel;
  
  /**
   * Creates an instance of this class.
   * <p>
   * @param channel The channel to keep in this ChannelItem.
   */
  public ChannelItem(Channel channel) {
    if(channel != null) {
      mChannelDataServiceId = channel.getDataServiceProxy().getId();
      mGroupId = channel.getGroup().getId();
      mCertainChannelId = channel.getId();
      mCountry = channel.getCountry();
      mChannel = channel;
      mNullChannel = false;
    }
    else {
      mNullChannel = true;
      mChannelDataServiceId = "";
      mGroupId = "";
      mCertainChannelId = "";
      mCountry = "";
      mChannel = null;
    }
  }
  
  /**
   * Load the channel item from an stream.
   * <p>
   * @param in The stream to read the channel values from.
   * @param version The version of the file.
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public ChannelItem(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    if(version == 1) {
      mChannelDataServiceId = (String) in.readObject();
      mCertainChannelId =(String)in.readObject();
      
      mChannel = Channel.getChannel(mChannelDataServiceId, null, null, mCertainChannelId);
      
      if(mChannel != null) {
        mGroupId = mChannel.getGroup().getId();
        mCountry = mChannel.getCountry();
      }
    }
    else {
      mChannelDataServiceId = in.readUTF();
      mGroupId = in.readUTF();
      mCountry = in.readUTF();
      mCertainChannelId = in.readUTF();
      
      if(version == 3) {
        mNullChannel = in.readBoolean();
      }
      
      mChannel = Channel.getChannel(mChannelDataServiceId, mGroupId, mCountry, mCertainChannelId);
    }
  }
  
  /**
   * Gets if this channel is valid (could be found).
   * <p>
   * @return <code>True</code> if this channel is valid,
   * <code>false</code> otherwise.
   */
  public boolean isValid() {
    return mChannelDataServiceId != null && mGroupId != null && mCountry != null && mCertainChannelId != null;
  }
  
  /**
   * Gets if this channel ist available or is a null channel
   * <p>
   * @return <code>True</code> if this channel is available or a null
   * channel, <code>false</code> otherwise.
   */
  public boolean isAvailableOrNullChannel() {
    return mChannel != null || mNullChannel;
  }
  
  /**
   * Gets the channel of this channel item.
   * <p>
   * @return The channel of this item or <code>null</code>
   * if the channel is not valid (could not be found).
   */
  public Channel getChannel() {
    return mChannel;
  }
  
  /**
   * Saves this channel item in an output stream.
   * <p>
   * @param out The stream to save this item in.
   * @throws IOException
   */
  public void saveItem(ObjectOutputStream out) throws IOException {
    out.writeUTF(mChannelDataServiceId);
    out.writeUTF(mGroupId);
    out.writeUTF(mCountry);
    out.writeUTF(mCertainChannelId);
    out.writeBoolean(mNullChannel);
  }
  
  /**
   * Gets if this channel is a null channel.
   * <p>
   * @return <code>True</code> if the channel is a null channel,
   * <code>false</code> otherwise.
   */
  public boolean isNullChannel() {
    return mNullChannel;
  }
}
