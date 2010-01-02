/*
 * DVBViewerChannel.java
 * Copyright (C) 2007 Ullrich Pollaehne (pollaehne@users.sourceforge.net)
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
 *     $Date: $
 *   $Author: $
 * $Revision: $
 */
package dvbviewer;

import java.io.Serializable;

import dvbviewer.com4j.IChannelItem;
import dvbviewer.com4j.TDVBVTunerType;


/**
 * Contains infos about a channel of DVB Viewer.
 * <p>
 * This class holds enough information about a DVB Viewer channel
 * to identify this channel in DVB Viewer's channel list
 *
 * @author pollaehne
 * @version $Revision: $
 */
public final class DVBViewerChannel implements Serializable, Comparable<DVBViewerChannel> {

  /** field <code>serialVersionUID</code> */
  private static final long serialVersionUID = -7762892911086342084L;

  /** the name of the channel */
  private String name = "";

  /** the service ID of the channel */
  private int sid = -1;

  /** the audio ID of the channel */
  private int aid = -1;

  /** the tuner type of the channel (ordinal of enum TDVBVTunerType) */
  private int tType = -1;

  /** the category of the channel */
  private String category = "";

  /** the EPG ID of the channel */
  private int epgid = -1;

  /** the number of the channel (index number in the channel list) */
  private int chNr = -1;

  /** set to true if this channel is not TV (has no video ID) */
  private boolean isRadio;

  /** set to true if this channel is encrypted */
  private boolean isEncrypted;

  /** set to true if EPG should be shown for this channel */
  private boolean displayEPG = true;

  /** the root of the channel */
  private String root = "";

  /** the frequency of the channel */
  private int frequency;

  /** the polarity of the channel */
  private int polarity;


  /**
   * Construct a DVBViewerChannel object with initial (invalid) values.
   */
  public DVBViewerChannel() {
    // nothing to do
  }

  /**
   * Construct a DVBViewerChannel object with the values of <code>nr</code>
   * and <code>channel</code>
   *
   * @param nr the channel number (enumeration number from channel list)
   * @param channel the channel item
   */
  public DVBViewerChannel(int nr, IChannelItem channel) {
    setChannelNr(nr);
    if (null == channel) {
      return;
    }
    setName(channel.name());
    setServiceID(channel.tuner().sid());
    setAudioID(channel.tuner().audioPID());
    setTunerType(channel.tuner().tunerType());
    setCategory(channel.category());
    setEPGID(channel.epgChannelID());
    setRoot(channel.root());
    setFrequency(channel.tuner().frequency());
    setPolarity(channel.tuner().polarity());
    int enc = channel.encrypted();
    isEncrypted = 1 == (0x1 & enc);
    boolean hasVideo = 0x8 == (0x8 & enc);
    boolean hasAudio = 0x10 == (0x10 & enc);
    isRadio = hasAudio && !hasVideo;
    displayEPG = 0 == (0x2 & enc);
  }


  /**
   * Construct a DVBViewerChannel object with the values of <code>channel</code>
   * which was encoded by <code>getSerializedChannel()</code>
   *
   * @param channel the channel as serialized string
   */
  public DVBViewerChannel(String channel) {
    if (null != channel) {
      String [] values = channel.split(";");
      
      setChannelNr(Integer.parseInt(values[0]));
      setRoot(values[1]);
      setName(values[2]);
      setServiceID(Integer.parseInt(values[3]));
      setAudioID(Integer.parseInt(values[4]));
      tType = Integer.parseInt(values[5]);
      setCategory(values[6]);
      setEPGID(Integer.parseInt(values[7]));
      setFrequency(Integer.parseInt(values[8]));
      setPolarity(Integer.parseInt(values[9]));
      isRadio = Boolean.parseBoolean(values[10]);
      displayEPG = Boolean.parseBoolean(values[11]);
      isEncrypted = Boolean.parseBoolean(values[12]);
    }
  }


  /**
   * Create a string containing all values of this channel, separated by ";"
   * 
   * @return String containing all values
   */
  public String getSerializedChannel() {
    StringBuilder buffer = new StringBuilder(128);
    buffer.append(chNr).append(';');
    buffer.append(root).append(';');
    buffer.append(name).append(';');
    buffer.append(sid).append(';');
    buffer.append(aid).append(';');
    buffer.append(tType).append(';');
    buffer.append(category).append(';');
    buffer.append(epgid).append(';');
    buffer.append(frequency).append(';');
    buffer.append(polarity).append(';');
    buffer.append(isRadio).append(';');
    buffer.append(displayEPG).append(';');
    buffer.append(isEncrypted);

    return buffer.toString();
  }


  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(DVBViewerChannel rhs) {
    int cmp = getRoot().compareTo(rhs.getRoot());
    if (0 == cmp) {
      cmp = getName().compareTo(rhs.getName());
    if (0 == cmp) {
      if (sid == rhs.sid) {
        if (aid == rhs.aid) {
          if (tType == rhs.tType) {
            cmp = 0;
          } else if (tType > rhs.tType) {
            cmp = 1;
          } else if (tType < rhs.tType) {
            cmp = -1;
          }
        } else if(aid > rhs.aid) {
          cmp = 1;
        } else if (aid < rhs.aid) {
          cmp = -1;
        }
      } else if (sid > rhs.sid) {
        cmp = 1;
      } else if (sid < rhs.sid) {
        cmp = -1;
      }
    }
    }

    return cmp;
  }


  /**
   * A simple toString to get a decent display in a ComboBox
   *
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return getName();
  }


  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + aid;
    result = prime * result + ((category == null) ? 0 : category.hashCode());
    result = prime * result + chNr;
    result = prime * result + (displayEPG ? 1231 : 1237);
    result = prime * result + epgid;
    result = prime * result + (isEncrypted ? 1231 : 1237);
    result = prime * result + (isRadio ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((root == null) ? 0 : root.hashCode());
    result = prime * result + sid;
    result = prime * result + tType;
    return result;
  }



  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof DVBViewerChannel)) return false;
    final DVBViewerChannel rhs = (DVBViewerChannel)obj;
    return 0 == compareTo(rhs);
  }



  /**
   * @param channelName The name of the channel.
   */
  public void setName(String channelName) {
    name = channelName;
  }


  /**
   * @return Returns the name of the channel.
   */
  public String getName() {
    return name;
  }


  /**
   * @param sid The service ID of the channel.
   */
  public void setServiceID(int serviceID) {
    sid = serviceID;
  }


  /**
   * @return Returns the service ID of the channel.
   */
  public int getServiceID() {
    return sid;
  }


  /**
   * @param audioID The audio ID of the channel.
   */
  public void setAudioID(int audioID) {
    aid = audioID;
  }


  /**
   * @return Returns the audio ID of the channel.
   */
  public int getAudioID() {
    return aid;
  }


  /**
   * @param type The tuner type of the channel.
   */
  public void setTunerType(TDVBVTunerType type) {
    tType = type.ordinal();
  }


  /**
   * @return Returns the tuner type of the channel.
   */
  public TDVBVTunerType getTunerType() {
    return TDVBVTunerType.values()[tType];
  }


  public final String getCategory() {
    return category;
  }


  public final void setCategory(String cat) {
    category = cat;
  }


  public final int getEPGID() {
    return epgid;
  }


  public final void setEPGID(int epgID) {
    epgid = epgID;
  }


  /**
   * @param nr The number in DVBViewer's list of the channel.
   */
  public void setChannelNr(int nr) {
    chNr = nr;
  }


  /**
   * @return Returns the number in DVBViewer's list of the channel.
   */
  public int getChannelNr() {
    return chNr;
  }


  public final boolean isRadio() {
    return isRadio;
  }



  public final void setRadio(boolean radio) {
    isRadio = radio;
  }



  public final boolean isEncrypted() {
    return isEncrypted;
  }



  public final void setEncrypted(boolean encrypted) {
    isEncrypted = encrypted;
  }



  public final boolean isDisplayEPG() {
    return displayEPG;
  }


  public final void setDisplayEPG(boolean display) {
    displayEPG = display;
  }


  public final String getRoot() {
    return root;
  }



  public final void setRoot(String newRoot) {
    root = newRoot;
  }




  /**
   * @return Returns value of frequency.
   */
  public final int getFrequency() {
    return frequency;
  }



  /**
   * @param newFrequency New value for frequency.
   */
  public final void setFrequency(int newFrequency) {
    frequency = newFrequency;
  }


  /**
   * @return Returns value of polarity.
   */
  public final int getPolarity() {
    return polarity;
  }


  /**
   * @param newPolarity New value for polarity.
   */
  public final void setPolarity(int newPolarity) {
    polarity = newPolarity;
  }



  /**
   * Returns the id of the channel calculated by this formula:
   * (TunerType +1) << 29 + APID << 16 + SID + '|' + Channelname
   *
   * @return the ID of the channel
   */
  public String getID() {
    StringBuilder buffer = new StringBuilder(128);
    buffer.append(((tType + 1) << 29) + (aid << 16) + sid);
    buffer.append('|');
    buffer.append(name);
   return buffer.toString();
  }

  public String getTransponderID() {
    return String.valueOf(tType) + "," + String.valueOf(frequency) + "," + String.valueOf(polarity);
  }
}


