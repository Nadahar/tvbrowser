/*
 * ChannelList.java
 * Copyright (C) 2006 Ullrich Pollaehne (pollaehne@users.sourceforge.net)
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
package dvbplugin.dvbviewer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class reads a DVB Viewer Pro/GE channel list (channels.dat) and returns
 * a class containing all information contained in the list.
 *
 * @author Pollaehne
 * @version $Revision: $
 */
public class ChannelList {
  /** the expected length of the ID string in the list header. */
  public static final byte WELLKNOWNIDLENGTH = 4;

  /** the expected ID string in the list header. */
  public static final String WELLKNOWNID = "B2C2";

  /** the expected high version byte of the list header. */
  public static final byte WELLKNOWNHIGHVERSION = 1;

  /** the expected low version byte of the list header. */
  public static final byte WELLKNOWNLOWVERSION = 8;

  /** a list of channels in the list. */
  private List<Channel> channels;

  private static Logger logger = Logger.getLogger(ChannelList.class.getName());


  /**
   * Creates a new channel list by reading the file given in
   * <code>listName</code>.
   *
   * @param listName the name of the list to read (i.e. channels.dat)
   * @return a new channel list containing all entries of the file
   * @throws IOException if opening/reading the file failed
   * @throws IllegalArgumentException if the file format is wrong
   */
  public static ChannelList getChannelList(final String listName) throws IOException {
    FileInputStream fi = null;
    FileChannel fc = null;
    ChannelList list = new ChannelList();

    try {
      // Open the file and then get a channel from the stream
      fi = new FileInputStream(listName);
      fc = fi.getChannel();

      // Get the file's size and then map it into memory
      MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

      if (!bb.hasRemaining()) {
        // nothing to do bail out
        return list;
      }

      // 1. read the list header
      readHeader(bb, list);

      // 2. read all channels
      list.channels = new ArrayList<Channel>();
      while (bb.hasRemaining()) {
        // append the channel to the list
        list.channels.add(new Channel(bb));
      }
    } finally {
      if (null != fc) {
        try {
          fc.close();
        } catch (IOException e) {
          // at least we tried it
          logger.log(Level.SEVERE, "Unable to close \"" + listName + "\" channel.", e);
        }
      }
      if (null != fi) {
        try {
          fi.close();
        } catch (IOException e) {
          // at least we tried it
          logger.log(Level.SEVERE, "Unable to close \"" + listName + "\" inputstream.", e);
        }
      }
    }

    return list;
  }


  /**
   * This method reads the header bytes of the list given in ByteBuffer
   * <code>bb</code> and checks that all values are the expected ones. That
   * means the length of the ID is 4, the ID is "B2C2", the high version is 1
   * and the low version is 8.
   *
   * @param bb the buffer containing the data
   * @param list the ChannelList object to store the header infos
   * @throws IllegalArgumentException if the start of the buffer <code>bb</code>
   *         is of wrong format
   */
  private static void readHeader(final ByteBuffer bb, final ChannelList list) {
    // get Header ID
    int bufferLen = ChannelList.readByte(bb);
    if (WELLKNOWNIDLENGTH != bufferLen) {
      logger.log(Level.WARNING, "Expected ID length is {0} but it was {1}",
                 new Object[] { String.valueOf(WELLKNOWNIDLENGTH), String.valueOf(bufferLen) });
      throw new IllegalArgumentException("ID length of DVB Viewer channel list is wrong");
    }

    byte[] buffer = new byte[bufferLen];
    bb.get(buffer, 0, bufferLen);
    String id = new String(buffer, 0, bufferLen);
    if (!WELLKNOWNID.equals(id)) {
      logger.log(Level.WARNING, "Expected ID is {0} but it was {1}",
                 new Object[] { WELLKNOWNID, id });
      throw new IllegalArgumentException("ID of DVB Viewer channel list is wrong");
    }

    // jump over the remaining (free) bytes
    bb.position(bb.position() + (WELLKNOWNIDLENGTH - bufferLen));

    // get Header Version
    byte high = bb.get();
    byte low = bb.get();
    if (WELLKNOWNHIGHVERSION != high || WELLKNOWNLOWVERSION != low) {
      logger.log(Level.WARNING, "Expected version is {0}.{1} but it was {2}.{3}",
                 new Object[] { String.valueOf(WELLKNOWNHIGHVERSION),
                                String.valueOf(WELLKNOWNLOWVERSION),
                                String.valueOf(high), String.valueOf(low) });
      throw new IllegalArgumentException("Version of DVB Viewer channel list is wrong");
    }
  }


  /**
   * @return Returns a newly created iterator over all channels.
   */
  public final Iterator<Channel> iterator() {
    return Collections.unmodifiableList(channels).iterator();
  }


  /**
   * @return Returns returns the number of all channels contained in this list.
   */
  public final int size() {
    return channels.size();
  }


  static final short readByte(final ByteBuffer buffer) {
    return (short)(0xFF & buffer.get());
  }


  static final long readDWord(final ByteBuffer buffer) {
    long low = readWord(buffer);
    long high = readWord(buffer);
    return high << 16 | low;
  }


  static final int readWord(final ByteBuffer buffer) {
    int low = readByte(buffer);
    int high = readByte(buffer);
    return high << 8 | low;
  }


  /**
   * Contains the tuner related data of a channel list entry
   * <p>
   * This class contains all tuner related data for an entry in the channel list
   * All values are upgrade to the next larger type since Java does not support
   * unsigned types. To get around this and make further processing easier the
   * types are upgraded as follows (Delphi type -> Java type): Byte -> short
   * Word -> int DWord -> long
   *
   * @author Pollaehne
   */
  public static final class Tuner {
    /** Tuner Type DVB-C */
    public static final byte TT_CABLE = 0;

    /** Tuner Type DVB-S */
    public static final byte TT_SATELLITE = 1;

    /** Tuner Type DVB-T */
    public static final byte TT_TERRESTRIAL = 2;

    /** Tuner Type ATSC (stuff from the new world */
    public static final byte TT_ATSC = 3;

    /** Tuner Type unknown (used as higest index) */
    public static final byte TT_UNKNOWN = 4;

    private short tunerType = TT_UNKNOWN;

    private long frequency;

    private long symbolrate;

    private int lnb;

    private int lnbSelection;

    private int pmt;

    private int ecm;

    private short avFormat;

    private int fec;

    private short satModulation;

    private int polarity;

    private int diseqc;

    private int audioPid;

    private int videoPid;

    private int transportStreamId;

    private int telePid;

    private int networkId;

    private int sId;

    private int pcrPid;


    protected Tuner(final ByteBuffer buffer) {
      setTunerType(ChannelList.readByte(buffer));
      // jump over the padding
      buffer.position(buffer.position() + 3);

      frequency = ChannelList.readDWord(buffer);
      symbolrate = ChannelList.readDWord(buffer);
      lnb = ChannelList.readWord(buffer);
      pmt = ChannelList.readWord(buffer);
      ecm = ChannelList.readWord(buffer);
      // overread reserved1
      buffer.get();
      avFormat = buffer.get();
      // as it seems fec is one off
      fec = ChannelList.readWord(buffer) - 1;
      satModulation = buffer.get();
      // overread reserved2
      buffer.get();
      polarity = ChannelList.readWord(buffer);
      // overread reserved3
      buffer.getShort();
      lnbSelection = ChannelList.readWord(buffer);
      // overread reserved4
      buffer.getShort();
      diseqc = ChannelList.readWord(buffer);
      // overread reserved5
      buffer.getShort();
      audioPid = ChannelList.readWord(buffer);
      // overread reserved6
      buffer.getShort();
      // get Tuner VideoPID
      videoPid = ChannelList.readWord(buffer);
      transportStreamId = ChannelList.readWord(buffer);
      telePid = ChannelList.readWord(buffer);
      networkId = ChannelList.readWord(buffer);
      sId = ChannelList.readWord(buffer);
      pcrPid = ChannelList.readWord(buffer);
    }


    /**
     * If lower nibble of avFormat = 1 then there is AC3
     *
     * @return true if AC3, false otherwise.
     */
    public boolean hasAc3() {
      return (avFormat & 0x1) == 1;
    }


    /**
     * If higher nibble of avFormat = 1 then there is H.264
     *
     * @return true if H.264, false otherwise.
     */
    public boolean hasH264() {
      return (avFormat & 0x10) == 0x10;
    }


    /**
     * @return Returns value of audioPid.
     */
    public int getAudioPid() {
      return audioPid;
    }


    /**
     * Returns the raw value of AVFormat (FKA AC3). For decoded infos use
     * <code>hasAc3()</code> and <code>hasH264()</code>.
     *
     * @return Returns the raw value of avFormat.
     */
    public short getAvFormat() {
      return avFormat;
    }


    /**
     * The DiseQ 0 = none, 1 = A, 2 = B, 3 = A/A, 4 = B/A, 5 = A/B, 6 = B/B
     *
     * @return Returns value of diseqc.
     */
    public int getDiseqc() {
      return diseqc;
    }


    /**
     * The ECM = 0 if unencrypted
     *
     * @return Returns value of ecm.
     */
    public int getEcm() {
      return ecm;
    }


    /**
     * The FEC 1 = 1/2, 2 = 2/3, 3 = 3/4, 4 = 5/6, 5 = 7/8, 6 = Auto
     *
     * @return Returns value of fec.
     */
    public int getFec() {
      return fec;
    }


    /**
     * @return Returns value of frequency.
     */
    public long getFrequency() {
      return frequency;
    }


    /**
     * The LNB frequency e.g. 9750, 10600
     *
     * @return Returns value of lnb.
     */
    public int getLnb() {
      return lnb;
    }


    /**
     * The LNB Selection 0 = none, 1 = 22 khz
     *
     * @return Returns value of lnbSelection.
     */
    public int getLnbSelection() {
      return lnbSelection;
    }


    /**
     * The Network ID only if scanned channel
     *
     * @return Returns value of networkId.
     */
    public int getNetworkId() {
      return networkId;
    }


    /**
     * @return Returns value of pcrPid.
     */
    public int getPcrPid() {
      return pcrPid;
    }


    /**
     * The PMT Pid
     *
     * @return Returns value of pmt.
     */
    public int getPmt() {
      return pmt;
    }


    /**
     * The Polarity 0 = H, 1 = V or Modulation or GuardInterval
     *
     * @return Returns value of polarity.
     */
    public int getPolarity() {
      return polarity;
    }


    /**
     * The Service ID
     *
     * @return Returns value of sId.
     */
    public int getSId() {
      return sId;
    }


    /**
     * @return Returns value of satModulation.
     */
    public short getSatModulation() {
      return satModulation;
    }


    /**
     * @return Returns value of symbolrate.
     */
    public long getSymbolrate() {
      return symbolrate;
    }


    /**
     * @return Returns value of telePid.
     */
    public int getTelePid() {
      return telePid;
    }


    /**
     * The Transportstream ID only if scanned channel
     *
     * @return Returns value of transportStreamId.
     */
    public int getTransportStreamId() {
      return transportStreamId;
    }


    /**
     * The type of tuner, one of <code>TT_CABLE</code>,
     * <code>TT_SATELLITE</code>, <code>TT_TERESTRIAL</code> or
     * <code>TT_ATSC</code>
     *
     * @return Returns value of tunerType.
     */
    public short getTunerType() {
      return tunerType;
    }


    /**
     * @return Returns value of videoPid.
     */
    public int getVideoPid() {
      return videoPid;
    }


    /**
     * @param type New value for tunerType.
     */
    private void setTunerType(final short type) {
      if (TT_CABLE <= type && TT_UNKNOWN > type) {
        tunerType = type;
      }
    }
  }

  /**
   * Contains the channel related data of a channel list entry
   * <p>
   * This class contains all channel list entry related data
   */
  public static final class Channel {
    /** the maximum size necessary for a string in the channel list */
    private static final int MAXSTRINGBUFFER = 25;

    /** the tuner related data */
    private Tuner tuner;

    /** the root (list name) of this channel */
    private String root;

    /** the name of this channel */
    private String name;

    /** the category (provider name or frequency) */
    private String category;

    /** FTA (=0) or encrypted channel */
    private short encrypted;


    protected Channel(final ByteBuffer buffer) {
      byte[] byteBuffer = new byte[MAXSTRINGBUFFER];

      tuner = new Tuner(buffer);

      // get Channel Root
      int bufferLen = ChannelList.readByte(buffer);
      buffer.get(byteBuffer, 0, bufferLen);
      root = new String(byteBuffer, 0, bufferLen);

      // jump over the remaining (free) bytes
      buffer.position(buffer.position() + (MAXSTRINGBUFFER - bufferLen));

      // get Channel Name
      bufferLen = ChannelList.readByte(buffer);
      buffer.get(byteBuffer, 0, bufferLen);
      name = new String(byteBuffer, 0, bufferLen);

      // jump over the remaining (free) bytes
      buffer.position(buffer.position() + (MAXSTRINGBUFFER - bufferLen));

      // get Channel Category
      bufferLen = ChannelList.readByte(buffer);
      buffer.get(byteBuffer, 0, bufferLen);
      category = new String(byteBuffer, 0, bufferLen);

      // jump over the remaining (free) bytes
      buffer.position(buffer.position() + (MAXSTRINGBUFFER - bufferLen));

      // get Channel Encrypted
      encrypted = ChannelList.readByte(buffer);

      // read over the unknown stuntman^H^H^H^H^H^H^H^H byte
      buffer.get();
    }


    /**
     * @return Returns value of category.
     */
    public String getCategory() {
      return category;
    }


    /**
     * @return Returns the raw value of encrypted.
     */
    public short getEncrypted() {
      return encrypted;
    }


    /**
     * @return Is the channel encrypted?.
     */
    public boolean isEncrypted() {
      return 1 == (0x1 & encrypted);
    }


    /**
     * @return Is the channel excluded from EPG "what's now" tab?
     */
    public boolean isExcludedFromEPG() {
      return 0x2 == (0x2 & encrypted);
    }


    /**
     * @return Does this (radio) channel provide RDS?
     */
    public boolean providesRDS() {
      return 0x4 == (0x4 & encrypted);
    }


    /**
     * @return Returns value of listTuner.
     */
    public Tuner getTuner() {
      return tuner;
    }


    /**
     * @return Returns value of name.
     */
    public String getName() {
      return name;
    }


    /**
     * @return Returns value of root.
     */
    public String getRoot() {
      return root;
    }
  }


  /**
   * A simple main method for tests
   *
   * @param args unused
   */
  /*
  public static void main(final String[] args) {
    java.io.BufferedWriter bw = null;
    try {
      if (1 != args.length) {
        System.out.println("please give the channel.dat as argument");
        return;
      }

      // get the base path/name to create the output ini file
      int index = args[0].indexOf('.');
      String basename = args[0].substring(0, index);

      long start = System.currentTimeMillis();
      // read the channel list
      ChannelList list = ChannelList.getChannelList(args[0]);
      System.out.println("Read " + list.size() + " channels in "
              + (System.currentTimeMillis() - start) + "ms");

      // create the output file
      bw = new java.io.BufferedWriter(new java.io.FileWriter(basename + ".ini"));

      String sep = System.getProperty("line.separator", "\n");
      int i = 0;
      // iterate over all channels (and write a DVBViewer Pro ini file)
      for (Iterator it = list.iterator(); it.hasNext();) {
        Channel ch = (Channel)it.next();

        Tuner tuner = ch.getTuner();
        StringBuffer builder = new StringBuffer(300);
        builder.append("[Channel");
        builder.append(i++);
        builder.append(']');
        builder.append(sep);
        builder.append("TunerType=");
        builder.append(tuner.getTunerType());
        builder.append(sep);
        builder.append("Frequency=");
        builder.append(tuner.getFrequency());
        builder.append(sep);
        int pol = tuner.getPolarity();
        builder.append("Polarity=");
        switch (pol) {
          case 0:
            builder.append('h');
            break;
          case 1:
            builder.append('v');
            break;
          default:
            builder.append(pol);
        }
        builder.append(sep);
        builder.append("Symbolrate=");
        builder.append(tuner.getSymbolrate());
        builder.append(sep);
        builder.append("FEC=");
        builder.append(tuner.getFec());
        builder.append(sep);
        builder.append("APID=");
        builder.append(tuner.getAudioPid());
        builder.append(sep);
        builder.append("VPID=");
        builder.append(tuner.getVideoPid());
        builder.append(sep);
        builder.append("PMTPID=");
        builder.append(tuner.getPmt());
        builder.append(sep);
        builder.append("PCRPID=");
        builder.append(tuner.getPcrPid());
        builder.append(sep);
        builder.append("AC3=");
        builder.append(tuner.getAvFormat());
        builder.append(sep);
        builder.append("TelePID=");
        builder.append(tuner.getTelePid());
        builder.append(sep);
        builder.append("SID=");
        builder.append(tuner.getSId());
        builder.append(sep);
        builder.append("StreamID=");
        builder.append(tuner.getTransportStreamId());
        builder.append(sep);
        builder.append("NetworkID=");
        builder.append(tuner.getNetworkId());
        builder.append(sep);
        builder.append("LNB-Selection=");
        builder.append(tuner.getLnbSelection());
        builder.append(sep);
        builder.append("LNB=");
        builder.append(tuner.getLnb());
        builder.append(sep);
        builder.append("DiseqC=");
        builder.append(tuner.getDiseqc());
        builder.append(sep);
        builder.append("Root=");
        builder.append(ch.getRoot());
        builder.append(sep);
        builder.append("Name=");
        builder.append(ch.getName());
        builder.append(sep);
        builder.append("Category=");
        builder.append(ch.getCategory());
        builder.append(sep);
        builder.append("Encrypted=");
        builder.append(ch.getEncrypted());
        builder.append(sep);
        builder.append("SatModulation=");
        builder.append(tuner.getSatModulation());
        builder.append(sep);
        builder.append(sep);

        // write the channel info
        String output = builder.toString();
        bw.write(output, 0, output.length());
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Some I/O operation failed!", e);
    } finally {
      if (null != bw) {
        try {
          // close the output file
          bw.close();
        } catch (IOException e) {
          // ok, at least we tried it
          logger.log(Level.WARNING, "Could not close output file.", e);
        }
      }
    }
  }
  */
}
