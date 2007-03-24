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
 *     $Date: 2007-01-03 09:06:40 +0100 (Mi, 03 Jan 2007) $
 *   $Author: bananeweizen $
 * $Revision: 2979 $
 */
package captureplugin.drivers.dreambox;

import captureplugin.drivers.dreambox.connector.DreamboxChannel;
import captureplugin.drivers.utils.IDGenerator;
import devplugin.Channel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 * The configuration for the dreambox
 */
public class DreamboxConfig {
    /** ID */
    private String mId;
    /** IP */
    private String mDreamboxAddress = "";
    /** Channels on the dreambox */
    private DreamboxChannel[] mDreamboxChannels = new DreamboxChannel[0];
    /** HashMap for tvbrowser channels vs channels on dreambox*/
    private HashMap<Channel, DreamboxChannel> mChannels = new HashMap<Channel, DreamboxChannel>();

    /**
     * Constructor
     */
    public DreamboxConfig() {
    }

    /**
     * Clone another config
     * @param dreamboxConfig clone this config
     */
    public DreamboxConfig(DreamboxConfig dreamboxConfig) {
        mId = dreamboxConfig.getId();
        mDreamboxAddress = dreamboxConfig.getDreamboxAddress();
        mDreamboxChannels = dreamboxConfig.getDreamboxChannels();
        mChannels = dreamboxConfig.getChannels();
    }

    /**
     * Read config from Stream
     * @param stream read config from this stream
     * @throws IOException io errors
     * @throws ClassNotFoundException class errors
     */
    public DreamboxConfig(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        readData(stream);
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

    public DreamboxConfig clone() {
        return new DreamboxConfig(this);
    }

    /**
     * Write config to stream
     * @param stream write to this stream
     * @throws IOException io errors
     */
    public void writeData(ObjectOutputStream stream) throws IOException {
        stream.writeInt(1);
        stream.writeUTF(getId());

        stream.writeUTF(mDreamboxAddress);
        stream.writeInt(mDreamboxChannels.length);
        for (DreamboxChannel channel : mDreamboxChannels) {
            channel.writeData(stream);
        }

        stream.writeInt(mChannels.size());

        for (Channel channel : mChannels.keySet()) {
            if (mChannels.get(channel) != null) {
                channel.writeData(stream);
                stream.writeUTF(mChannels.get(channel).getReference());
            }
        }
    }

    /**
     * Read the config from a stream
     * @param stream read from this stream
     * @throws IOException io errors
     * @throws ClassNotFoundException class not found errors
     */
    private void readData(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        int version = stream.readInt();
        mId = stream.readUTF();
        mDreamboxAddress = stream.readUTF();

        int count = stream.readInt();

        mDreamboxChannels = new DreamboxChannel[count];

        for (int i = 0; i < count; i++) {
            mDreamboxChannels[i] = new DreamboxChannel(stream);
        }

        count = stream.readInt();

        for (int i = 0; i < count; i++) {
            Channel ch = Channel.readData(stream, true);
            DreamboxChannel dch = findDreamboxChannelForRef(stream.readUTF());
            mChannels.put(ch, dch);
        }

    }

    /**
     * Find a specific DreamboxChannel
     * @param ref ServiceReference
     * @return Channel with specific ref, <code>null</code>, if not found
     */
    private DreamboxChannel findDreamboxChannelForRef(String ref) {
        for (DreamboxChannel channel : mDreamboxChannels) {
            if (channel.getReference().equals(ref)) {
                return channel;
            }
        }

        return null;
    }

    /**
     * Returns the channel-mapping
     */
    private HashMap<Channel, DreamboxChannel> getChannels() {
        return mChannels;
    }

    /**
     * @param address IP-Address of the dreambox.
     */
    public void setDreamboxAddress(String address) {
        mDreamboxAddress = address;
    }

    /**
     * @return IP-Address of the dreambox
     */
    public String getDreamboxAddress() {
        return mDreamboxAddress;
    }

    /**
     * @param channels all channels on the dreambox
     */
    public void setDreamboxChannels(DreamboxChannel[] channels) {
        Arrays.sort(channels, new Comparator<DreamboxChannel>() {
            public int compare(DreamboxChannel o1, DreamboxChannel o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        mDreamboxChannels = channels;
    }

    /**
     * @return all channels on the dreambox
     */
    public DreamboxChannel[] getDreamboxChannels() {
        return mDreamboxChannels;
    }

    /**
     * Get a DreamboxChannel for a TVBrowser-channel
     * @param channel get DreamboxChannel for this channel
     * @return DreamboxChannel for given channel, <code>null</code> if not found
     */
    public DreamboxChannel getDreamboxChannel(Channel channel) {
        return mChannels.get(channel);
    }

    /**
     * Set the mapping between dreamboxchannel and subscribedchannel
     * @param channel TVBrowser-Channel
     * @param dreamboxChannel DreamboxChannel
     */
    public void setDreamboxChannel(Channel channel, DreamboxChannel dreamboxChannel) {
        mChannels.put(channel, dreamboxChannel);
    }
}
