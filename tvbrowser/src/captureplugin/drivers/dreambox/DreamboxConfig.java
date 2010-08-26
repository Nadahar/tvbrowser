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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TimeZone;

import util.io.IOUtilities;
import captureplugin.drivers.dreambox.connector.DreamboxChannel;
import captureplugin.drivers.utils.IDGenerator;
import captureplugin.utils.ConfigIf;
import captureplugin.utils.ExternalChannelIf;
import devplugin.Channel;
import devplugin.ProgramReceiveTarget;

/**
 * The configuration for the dreambox
 */
public final class DreamboxConfig implements ConfigIf, Cloneable {
    /** ID */
    private String mId;
    /** IP */
    private String mDreamboxAddress = "";
    /** Channels on the dreambox */
    private DreamboxChannel[] mDreamboxChannels = new DreamboxChannel[0];
    /** HashMap for tvbrowser channels vs channels on dreambox*/
    private HashMap<Channel, DreamboxChannel> mChannels = new HashMap<Channel, DreamboxChannel>();
    /** HashMap for tvbrowser channels vs channels on dreambox*/
    private HashMap<DreamboxChannel, Channel> mDChannels = new HashMap<DreamboxChannel, Channel>();

    /** Time after recording */
    private int mAfter = 0;

    /** Time before recording */
    private int mBefore = 0;
    /** The TimeZone */
    private String mTimeZone;

    /** UserName for Authentification */
    private String mUsername = "";
    /** Password for Authentification */
    private String mPassword = "";

    /** Path to a Mediaplayer typically vlc */
    private String mMediaplayer = "vlc";

    /** Timeout for connection to the dreambox */
    private int mTimeout = 1000;

    /** The targets for the program export */
    private ProgramReceiveTarget[] mReceiveTargets = new ProgramReceiveTarget[0];
    
    /** The version of the box software is at least 1.6 */
    private boolean mIsAtLeastVersion_1_6 = true;

    /**
     * Constructor
     */
    public DreamboxConfig() {
        resetTimeZone();
    }

    /**
     * Clone another config
     * @param dreamboxConfig clone this config
     */
    public DreamboxConfig(DreamboxConfig dreamboxConfig) {
        mId = dreamboxConfig.getId();
        mDreamboxAddress = dreamboxConfig.getDreamboxAddress();
        mDreamboxChannels = (DreamboxChannel[]) dreamboxConfig.getExternalChannels();
        mChannels = dreamboxConfig.getChannels();
        mDChannels = dreamboxConfig.getDreamChannels();
        mBefore = dreamboxConfig.getPreTime();
        mAfter = dreamboxConfig.getAfterTime();
        mTimeout = dreamboxConfig.getTimeout();
        mTimeZone = dreamboxConfig.getTimeZoneAsString();
        mUsername = dreamboxConfig.getUserName();
        mPassword = dreamboxConfig.getPassword();
        mMediaplayer = dreamboxConfig.getMediaplayer();
        mReceiveTargets = dreamboxConfig.getProgramReceiveTargets();
        mIsAtLeastVersion_1_6 = dreamboxConfig.getIsVersionAtLeast_1_6();
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
        stream.writeInt(6);
        stream.writeUTF(getId());

        stream.writeUTF(mDreamboxAddress);
        stream.writeInt(mDreamboxChannels.length);
        for (DreamboxChannel channel : mDreamboxChannels) {
            channel.writeData(stream);
        }

        int max = 0;
        for (Channel channel : mChannels.keySet()) {
            if (mChannels.get(channel) != null) {
                max++;
            }
        }

        stream.writeInt(max);

        for (Channel channel : mChannels.keySet()) {
            if ((channel != null) && (mChannels.get(channel) != null)) {
                channel.writeData(stream);
                stream.writeUTF(mChannels.get(channel).getReference());
            }
        }

        stream.writeInt(mBefore);
        stream.writeInt(mAfter);
        stream.writeInt(mTimeout);

        if (mTimeZone == null) {
            stream.writeUTF(TimeZone.getDefault().getID());
        } else {
            stream.writeUTF(mTimeZone);
        }

        stream.writeUTF(mUsername);
        stream.writeUTF(IOUtilities.xorEncode(mPassword, 21341));

        stream.writeUTF(mMediaplayer);

        stream.writeInt(mReceiveTargets.length);

        for(ProgramReceiveTarget receiveTarget : mReceiveTargets) {
          receiveTarget.writeData(stream);
        }
        
        stream.writeBoolean(mIsAtLeastVersion_1_6);
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
            DreamboxChannel dch = getDreamboxChannelForRef(stream.readUTF());
            mChannels.put(ch, dch);
            mDChannels.put(dch, ch);
        }


        if (version < 2) {
            return;
        }

        mBefore = stream.readInt();
        mAfter = stream.readInt();
        mTimeout = stream.readInt();

        if (version < 3) {
            resetTimeZone();
            return;
        }

        mTimeZone = stream.readUTF();

        if (version < 4) {
            return;
        }

        mUsername = stream.readUTF();
        mPassword = IOUtilities.xorDecode(stream.readUTF(), 21341);

        mMediaplayer = stream.readUTF();

        if(version > 4) {
          mReceiveTargets = new ProgramReceiveTarget[stream.readInt()];

          for(int i = 0; i < mReceiveTargets.length; i++) {
            mReceiveTargets[i] = new ProgramReceiveTarget(stream);
          }
        }
        
        if(version > 5) {
          mIsAtLeastVersion_1_6 = stream.readBoolean();
        }
    }

    /**
     * Find a specific DreamboxChannel
     * @param ref ServiceReference
     * @return Channel with specific ref, <code>null</code>, if not found
     */
    public DreamboxChannel getDreamboxChannelForRef(String ref) {
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
    void setDreamboxChannels(DreamboxChannel[] channels) {
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
    public ExternalChannelIf[] getExternalChannels() {
        return mDreamboxChannels.clone();
    }

    /**
     *  @return internal HashMap for Dreambox vs TVBrowser Channels
     */
    private HashMap<DreamboxChannel, Channel> getDreamChannels() {
        return mDChannels;
    }

    /**
     * Get a DreamboxChannel for a TVBrowser-channel
     * @param channel get DreamboxChannel for this channel
     * @return DreamboxChannel for given channel, <code>null</code> if not found
     */
    public ExternalChannelIf getExternalChannel(Channel channel) {
        return mChannels.get(channel);
    }

    /**
     * Get a TVBrowser-channel for a DreamboxChannel
     * @param channel get TVBrowser channel for this channel
     * @return TVBrowser channel for a given channel, <code>null</code> if not found
     */
    public Channel getChannel(DreamboxChannel channel) {
        return mDChannels.get(channel);
    }

    /**
     * Set the mapping between dreamboxchannel and subscribedchannel
     * @param channel TVBrowser-Channel
     * @param dreamboxChannel DreamboxChannel
     */
    public void setExternalChannel(Channel channel, ExternalChannelIf dreamboxChannel) {
        mChannels.put(channel, (DreamboxChannel) dreamboxChannel);
        mDChannels.put((DreamboxChannel) dreamboxChannel, channel);
    }

    /**
     * @return Time after recording
     */
    public int getAfterTime() {
        return mAfter;
    }

    /**
     * Set Time after recording
     * @param after After recording
     */
    public void setAfterTime(int after) {
        mAfter = after;
    }

    /**
     * @return Time before recording
     */
    public int getPreTime() {
        return mBefore;
    }

    /**
     * Set Time before recording
     * @param before before recording
     */
    public void setBeforeTime(int before) {
        mBefore = before;
    }

    /**
     * Set the timezone
     * @param timezone new Timezone
     */
    public void setTimeZone(String timezone) {
        mTimeZone = timezone;
    }

    /**
     * Get the Timezone as String
     * @return timezone
     */
    public String getTimeZoneAsString() {
        if (mTimeZone == null) {
            resetTimeZone();
        }
        return mTimeZone;
    }

    /**
     * Get the Timezone
     * @return timezone
     */
    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(getTimeZoneAsString());
    }

    /**
     * Reset the TimeZone to the local default
     */
    private void resetTimeZone() {
        TimeZone def = TimeZone.getDefault();
        mTimeZone = def.getID();
    }

    /**
     * @param username set the new username for authentification
     */
    public void setUserName(String username) {
        mUsername = username;
    }

    /**
     * @return username
     */
    public String getUserName() {
        return mUsername;
    }

    /**
     * @param password set the new password for authentification
     */
    public void setPassword(char[] password) {
        mPassword = new String(password);
    }

    /**
     * @return password
     */
    public String getPassword() {
        return mPassword;
    }

    public String getMediaplayer() {
      return mMediaplayer;
    }

    public void setMediaplayer(String mediaplayer) {
      mMediaplayer = mediaplayer;
    }

    public int getTimeout() {
      return mTimeout;
    }

    public void setTimeout(int timeout) {
      mTimeout = timeout;
    }

    public boolean hasValidAddress() {
      final String address = getDreamboxAddress();
      return address != null && !address.trim().isEmpty();
    }

    /**
     * Sets the program receive targets for this device.
     * @param receiveTargets The receive targets for this device.
     */
    public void setProgramReceiveTargets(ProgramReceiveTarget[] receiveTargets) {
      mReceiveTargets = receiveTargets;
    }

    /**
     * Gets the program receive targets of this device.
     * @return The program receive targets of this device.
     */
    public ProgramReceiveTarget[] getProgramReceiveTargets() {
      return mReceiveTargets;
    }
    
    /**
     * Gets if the software version of the box is at least 1.6.
     * @return <code>true</code> if the version is at least 1.6, <code>false</code> otherwise.
     */
    public boolean getIsVersionAtLeast_1_6() {
      return mIsAtLeastVersion_1_6;
    }
    
    /**
     * Sets if the software version of the box is at least 1.6.
     * @param value The new value.
     */
    public void setIsVersionAtLeast_1_6(boolean value) {
      mIsAtLeastVersion_1_6 = value;
    }
}
