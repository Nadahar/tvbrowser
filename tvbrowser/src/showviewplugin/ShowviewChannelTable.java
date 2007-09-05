/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package showviewplugin;

import devplugin.Channel;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ShowviewChannelTable {

  private static final ChannelTableEntry[] CHANNEL_TABLE = {
    new ChannelTableEntry(new String[] {"ARD", "Das Erste (ARD)", "Das Erste"}, 1),
    new ChannelTableEntry("ZDF", 2),
    new ChannelTableEntry("Radio Bremen", 3),
    new ChannelTableEntry("RTL", 4),
    new ChannelTableEntry("SAT.1", "SAT 1", 5),
    new ChannelTableEntry("PRO7", "ProSieben", 6),
    new ChannelTableEntry("n-tv", 7),
    new ChannelTableEntry("Kabel 1", 8),
    new ChannelTableEntry("RTL2", 9),
    new ChannelTableEntry("ARTE", 10),
    new ChannelTableEntry("VOX", 11),
    new ChannelTableEntry("DSF", 12),
    new ChannelTableEntry("ORF1", "ORF 1", 14),
    new ChannelTableEntry("ORF2", "ORF 2", 15),
    new ChannelTableEntry("WDR", "WDR3", "WDR 3", 17),
    new ChannelTableEntry(new String[] {"BR Bayern", "Bayern", "BR", "BR3", "BR 3"}, 18),
    new ChannelTableEntry("NDR", "N3", 19),
    new ChannelTableEntry("Schweiz SF1", "SF1", "SF 1", 24),
    new ChannelTableEntry("HR Hessen", "HR", "HR3", "HR 3", 26),
    new ChannelTableEntry("SFB", 27),
    new ChannelTableEntry(new String[] {"SWR", "SWR3", "SWR 3", "S�dwest", "RP", "BW", "Saarland"}, 29),
    new ChannelTableEntry("MDR", "MDR3", "MDR 3", 32),
    new ChannelTableEntry("ORB", 35),
    new ChannelTableEntry("TV Berlin", 38),
    new ChannelTableEntry("Schweiz SF2", "SF2", "SF 2", 39),
    new ChannelTableEntry("FAB", 41),
    new ChannelTableEntry("TV M�nchen", 44),
    new ChannelTableEntry("tv.nrw", 46),
    new ChannelTableEntry("Spreekanal", 51),
    new ChannelTableEntry("HH1", 54),
    new ChannelTableEntry("BR alpha", 57),
    new ChannelTableEntry("Franken FS", 58),
    new ChannelTableEntry("9live", "TM3", 59),
    new ChannelTableEntry("XXP", 60),
    new ChannelTableEntry("Kinderkanal", "KIKA", 63),
    new ChannelTableEntry("B.TV W�rttemberg", "B.TV", 64),
    new ChannelTableEntry("N24", 65),
    new ChannelTableEntry("Niederlande 1", 88),
    new ChannelTableEntry("Niederlande 2", 89),
    new ChannelTableEntry("Niederlande 3", 90),
    new ChannelTableEntry("D�nemark", 91),
    new ChannelTableEntry("D�nemark TV 2", 92),
    new ChannelTableEntry("France 1", 93),
    new ChannelTableEntry("France 2", 94),
    new ChannelTableEntry("France 3", 95),
    new ChannelTableEntry("Belg. NL", 96),
    new ChannelTableEntry("TELE 5", 105),
    new ChannelTableEntry("Eurosport", 107),
    new ChannelTableEntry("MTV", 109),
    new ChannelTableEntry("BBC World", 112),
    new ChannelTableEntry("Premiere analog", 117),
    new ChannelTableEntry("3Sat", 118),
    new ChannelTableEntry("Viva", 121),
    new ChannelTableEntry("CNN", 126),
    new ChannelTableEntry("Bloomberg", 127),
    new ChannelTableEntry("TV5", 133),
    new ChannelTableEntry("NBC", 144),
    new ChannelTableEntry("TNT", 149),
    new ChannelTableEntry("MTV2", "MTV 2", "MTV2 Pop", 162),
    new ChannelTableEntry("Super RTL", "SuperRTL", 179),
    new ChannelTableEntry("VIVAplus", 181),
    new ChannelTableEntry("HSE", 189),    
    new ChannelTableEntry("Onyx", 205),
    new ChannelTableEntry("Phoenix", 206),
  };


  public static int getChannelNumberFor(Channel channel) {
    for (int i = 0; i < CHANNEL_TABLE.length; i++) {
      if (CHANNEL_TABLE[i].belongsTo(channel)) {
        return CHANNEL_TABLE[i].getChannelNumber();
      }
    }
    
    return -1;
  }
  
  
  private static class ChannelTableEntry {
    private String[] mChannelNameArr;
    private int mChannelNumber;

    
    public ChannelTableEntry(String channelName, int channelNumber) {
      this(new String[] { channelName }, channelNumber);
    }


    public ChannelTableEntry(String chName1, String chName2,
      int channelNumber)
    {
      this(new String[] { chName1, chName2 }, channelNumber);
    }


    public ChannelTableEntry(String chName1, String chName2, String chName3,
      int channelNumber)
    {
      this(new String[] { chName1, chName2, chName3 }, channelNumber);
    }


    public ChannelTableEntry(String chName1, String chName2, String chName3,
      String chName4, int channelNumber)
    {
      this(new String[] { chName1, chName2, chName3, chName4 }, channelNumber);
    }


    public ChannelTableEntry(String[] channelNameArr, int channelNumber) {
      mChannelNameArr = channelNameArr;
      mChannelNumber = channelNumber;
    }
    

    public boolean belongsTo(Channel channel) {
      for (int i = 0; i < mChannelNameArr.length; i++) {
        if (mChannelNameArr[i].equalsIgnoreCase(channel.getDefaultName())) {
          return true;
        }
      }
      return false;
    }
    
    
    public int getChannelNumber() {
      return mChannelNumber;
    }
  }

}
