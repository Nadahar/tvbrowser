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
package captureplugin.drivers.simpledevice;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import captureplugin.drivers.utils.IDGenerator;
import captureplugin.utils.ConfigIf;
import captureplugin.utils.ExternalChannelIf;
import devplugin.Channel;
import devplugin.Plugin;

/**
 * Configuration for the Device
 *
 * @author bodum
 */
public final class SimpleConfig implements ConfigIf, Cloneable {
    /**
     * Mapping TVB Channels - external channels
     */
    private HashMap<Channel, SimpleChannel> mChannels;
    /**
     * List of all available external channels
     */
    private ArrayList<SimpleChannel> mSimpleChannels;
    /**
     * Unique ID
     */
    private String mId;

    /**
     * Create Config
     */
    public SimpleConfig() {
        mChannels = new HashMap<Channel, SimpleChannel>();
        mSimpleChannels = new ArrayList<SimpleChannel>();
    }

    /**
     * Clone Config
     *
     * @param config config to clone
     */
    public SimpleConfig(SimpleConfig config) {
        mChannels = (HashMap<Channel, SimpleChannel>) config.getChannelMapping().clone();
        mSimpleChannels = new ArrayList<SimpleChannel>(Arrays.asList((SimpleChannel[])config.getExternalChannels()));
        mId = config.getId();
    }

    /**
     * Load Config
     *
     * @param stream Load Config from this Stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public SimpleConfig(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        mChannels = new HashMap<Channel, SimpleChannel>();
        mSimpleChannels = new ArrayList<SimpleChannel>();
        readData(stream);
    }

    public Object clone() {
        return new SimpleConfig(this);
    }

    /**
     * Get Mapping of TVB-Channels - external Channels
     * @return Mapping
     */
    public HashMap<Channel, SimpleChannel> getChannelMapping() {
        return mChannels;
    }

    /**
     * Get external channel for TVB Channel
     *
     * @param channel Get external channel for this TVB Channel
     * @return external channel if mapped, else null
     */
    public ExternalChannelIf getExternalChannel(Channel channel) {
        return mChannels.get(channel);
    }

    /**
     * Set Mapping for Channel
     *
     * @param channel  TVB Channel
     * @param external external Channel
     */
    public void setExternalChannel(Channel channel, ExternalChannelIf external) {
        if ((external != null) && (channel != null)) {
          mChannels.put(channel, (SimpleChannel) external);
        } else if (channel != null) {
          mChannels.remove(channel);
        }
    }

    /**
     * Set List of external Channels.
     * This checks the mappings and removes unavailable external Channels
     *
     * @param channels List of Channels
     */
    public void setExternalChannels(SimpleChannel[] channels) {
        mSimpleChannels = new ArrayList<SimpleChannel>(Arrays.asList(channels));

        // Remove Channels that were removed/changed
        Iterator<Channel> iterator = mChannels.keySet().iterator();

        HashMap<Channel, SimpleChannel> cloneMap = (HashMap<Channel, SimpleChannel>) mChannels.clone();

        while (iterator.hasNext()) {
            Channel rchan = iterator.next();
            SimpleChannel channel = mChannels.get(rchan);
            if (!mSimpleChannels.contains(channel)) {
                cloneMap.remove(rchan);
            }
        }

        mChannels = cloneMap;

        // Set Channels automatically if Name fits
        Channel[] subchannels = Plugin.getPluginManager().getSubscribedChannels();
        for (Channel subchannel : subchannels) {
            if (mChannels.get(subchannel) == null) {
                for (SimpleChannel channel : channels) {
                    if (subchannel.getName().equalsIgnoreCase(channel.getName())) {
                        mChannels.put(subchannel, channel);
                    }
                }
            }
        }

    }

    /**
     * @return get all available external Channels
     */
    public ExternalChannelIf[] getExternalChannels() {
        return getAllExternalChannels(null);
    }

    /**
     * @return get all available external Channels
     */
    public ExternalChannelIf[] getAllExternalChannels(SimpleConnectionIf con) {
        if ((con != null) && (mSimpleChannels.size() == 0)) {
            SimpleChannel[] lists = con.getAvailableChannels();
            if (lists != null) {
              setExternalChannels(lists);
            }
        }
        return mSimpleChannels.toArray(new SimpleChannel[mSimpleChannels.size()]);
    }

    /**
     * Read Settings
     *
     * @param stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readData(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        int version = stream.readInt();

        int elsize = stream.readInt();
        for (int i = 0; i < elsize; i++) {
            SimpleChannel channel = new SimpleChannel(stream);
            mSimpleChannels.add(channel);
        }

        int mapCount = stream.readInt();

        Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();

        for (int i = 0; i < mapCount; i++) {
            String chanId = stream.readUTF();
            SimpleChannel channel = new SimpleChannel(stream);

            for (Channel channel2 : channels) {
                if (channel2.getId().equals(chanId)) {
                    mChannels.put(channel2, channel);
                }
            }
        }

        if (version > 1) {
            mId = (String) stream.readObject();
        }
    }

    /**
     * Store Settings
     *
     * @param stream
     * @throws IOException
     */
    public void writeData(ObjectOutputStream stream) throws IOException {
        stream.writeInt(2);

        stream.writeInt(mSimpleChannels.size());

        for (SimpleChannel mSimpleChannel : mSimpleChannels) {
            mSimpleChannel.writeData(stream);
        }

        stream.writeInt(mChannels.size());

        for (Channel ch : mChannels.keySet()) {
            stream.writeUTF(ch.getId());
            (mChannels.get(ch)).writeData(stream);
        }

        stream.writeObject(mId);
    }

    /**
     * Returns TVB Channel for external Channel ID
     *
     * @param channel external Channel ID
     * @return TVB Channel, null if not found
     */
    public Channel getChannelForExternalId(int channel) {

        for (Channel ch : mChannels.keySet()) {
            if ((mChannels.get(ch)).getNumber() == channel) {
              return ch;
            }
        }

        return null;
    }

    /**
     * @return ID of this Device
     */
    public String getId() {
        if (mId == null) {
          mId = IDGenerator.generateUniqueId();
        }
        return mId;
    }
}