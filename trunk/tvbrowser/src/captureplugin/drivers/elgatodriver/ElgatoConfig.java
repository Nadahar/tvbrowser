/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mär 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */
package captureplugin.drivers.elgatodriver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import captureplugin.CapturePlugin;
import devplugin.Channel;

/**
 * Configuration for the Device
 * 
 * @author bodum
 */
public class ElgatoConfig {
  /** Mapping TVB Channels - Elgato Channels */
  private HashMap mChannels;
  /** List of all Available Elgato Channels */
  private ArrayList mElgatoChannels;
  
  /**
   * Create Config
   */
  public ElgatoConfig() {
    mChannels = new HashMap();
    mElgatoChannels = new ArrayList();
  }

  /**
   * Clone Config
   * @param config config to clone
   */
  public ElgatoConfig(ElgatoConfig config) {
    mChannels = (HashMap)config.getChannelMapping().clone();
    mElgatoChannels = new ArrayList(Arrays.asList(config.getAllElgatoChannels()));
  }

  /**
   * Load Config
   * @param stream Load Config from this Stream
   * 
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public ElgatoConfig(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    mChannels = new HashMap();
    mElgatoChannels = new ArrayList();
    readData(stream);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    return new ElgatoConfig(this);
  }

  /**
   * Get Mapping of TVB-Channels - Elgato Channels
   * @return Mapping
   */
  public HashMap getChannelMapping() {
    return mChannels;
  }
  
  /**
   * Get Elgato Channel for TVB Channel
   * @param channel Get Elgato Channel for this TVB Channel
   * @return Elgato Channel if mapped, else null
   */
  public ElgatoChannel getElgatoChannel(Channel channel) {
    return (ElgatoChannel)mChannels.get(channel);
  }
  
  /**
   * Set Mapping for Channel
   * @param channel TVB Channel
   * @param elgatoChannel Elgato Channel
   */
  public void setElgatoChannel(Channel channel, ElgatoChannel elgatoChannel) {
    mChannels.put(channel, elgatoChannel);
  }
  
  /**
   * Set List of Elgato Channels.
   * This checks the mappings and removes unavailable Elgato Channels
   * 
   * @param channels List of Channels
   */
  public void setElgatoChannels(ElgatoChannel[] channels) {
    mElgatoChannels = new ArrayList(Arrays.asList(channels));
    
    // Remove Channels that were removed/changed
    Iterator iterator = mChannels.keySet().iterator();
    
    HashMap cloneMap = (HashMap) mChannels.clone();
    
    while(iterator.hasNext()) {
      Channel rchan = (Channel) iterator.next();
      ElgatoChannel channel = (ElgatoChannel) mChannels.get(rchan);
      if (!mElgatoChannels.contains(channel)) {
        cloneMap.remove(rchan);
      }
    }
    
    mChannels = cloneMap;
    
    // Set Channels automatically if Name fits
    Channel[] subchannels = CapturePlugin.getPluginManager().getSubscribedChannels();
    for (int i = 0;i<subchannels.length;i++) {
      if (mChannels.get(subchannels[i]) == null) {
        for (int v = 0;v < channels.length;v++) {
          if (subchannels[i].getName().equalsIgnoreCase(channels[v].getName())) {
            mChannels.put(subchannels[i], channels[v]);
          }
        }
      }
    }

  }

  /**
   * @return get all available Elgato Channels
   */
  public ElgatoChannel[] getAllElgatoChannels() {
      return getAllElgatoChannels(null);
  }
  
  /**
   * @return get all available Elgato Channels
   */
  public ElgatoChannel[] getAllElgatoChannels(ElgatoConnection con) {
    if ((con != null) && (mElgatoChannels.size() == 0)) {
       setElgatoChannels(con.getAvailableChannels());
    }
    return (ElgatoChannel[])mElgatoChannels.toArray(new ElgatoChannel[mElgatoChannels.size()]);
  }
  
  /**
   * Read Settings
   * @param stream
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public void readData(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    int version = stream.readInt();
    
    int elsize = stream.readInt();
    for (int i = 0;i<elsize;i++) {
      ElgatoChannel channel = new ElgatoChannel(stream);
      mElgatoChannels.add(channel);
    }
    
    int mapCount = stream.readInt();

    Channel channels[] = CapturePlugin.getPluginManager().getSubscribedChannels();
    
    for (int i=0;i<mapCount;i++) {
      String chanId = stream.readUTF();
      ElgatoChannel channel = new ElgatoChannel(stream);
      
      for (int v = 0;v<channels.length;v++) {
        if (channels[v].getId().equals(chanId)) {
          mChannels.put(channels[v], channel);
        }
      }
    }
  } 

  /**
   * Store Settings
   * @param stream
   * @throws IOException
   */
  public void writeData(ObjectOutputStream stream) throws IOException {
    stream.writeInt(1);
    
    stream.writeInt(mElgatoChannels.size());
    
    for (int i = 0;i<mElgatoChannels.size();i++) {
      ((ElgatoChannel)mElgatoChannels.get(i)).writeData(stream);
    }
    
    stream.writeInt(mChannels.size());
    
    Iterator it = mChannels.keySet().iterator();
    
    while (it.hasNext()) {
      Channel ch = (Channel) it.next();
      stream.writeUTF(ch.getId());
      ((ElgatoChannel)mChannels.get(ch)).writeData(stream);
    }
  }

  /**
   * Returns TVB Channel for Elgato Chanel ID
   * @param channel Elgato Channel ID
   * @return TVB Channel, null if not found
   */
  public Channel getChannelForElgatoId(int channel) {
      Iterator it = mChannels.keySet().iterator();
      
      while (it.hasNext()) {
        Channel ch = (Channel) it.next();
        if (((ElgatoChannel)mChannels.get(ch)).getNumber() == channel)
            return ch;
      }

      return null;
  }
}