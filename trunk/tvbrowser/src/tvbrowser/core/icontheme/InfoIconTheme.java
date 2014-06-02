/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.core.icontheme;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * A class that stores the info icons for the program panels.
 * <p>
 * @author René Mach
 */
public class InfoIconTheme implements Comparable<InfoIconTheme> {
  public static byte INFO_16_9 = 0;
  public static byte INFO_ARTS = 1;
  public static byte INFO_AUDIO_DESCRIPTION = 2;
  public static byte INFO_BLACK_AND_WHITE = 3;
  public static byte INFO_CHILDREN = 4;
  public static byte INFO_DOCU = 5;
  public static byte INFO_DOLBY_DIGITAL_5_1 = 6;
  public static byte INFO_DOLBY_SOURROUND = 7;
  public static byte INFO_HAS_PICTURE = 8;
  public static byte INFO_HD = 9;
  public static byte INFO_INFOTAINMENT = 10;
  public static byte INFO_LIVE = 11;
  public static byte INFO_MONO = 12;
  public static byte INFO_MOVIE = 13;
  public static byte INFO_NEW = 14;
  public static byte INFO_NEWS = 15;
  public static byte INFO_ORIGINAL_WITH_SUBTITLE = 16;
  public static byte INFO_SERIES = 17;
  public static byte INFO_SHOW = 18;
  public static byte INFO_SIGN_LANGUGAGE = 19;
  public static byte INFO_SPORTS = 20;
  public static byte INFO_STEREO = 21;
  public static byte INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED = 22;
  public static byte INFO_TWO_CHANNEL_TONE = 23;
  public static byte INFO_4_3 = 24;
  public static byte INFO_OTHERS = 25;
  
  private HashMap<Byte, ImageIcon> mIconMap;
  
  private String mID;
  private String mName;
  
  private File mDirOrZip;
  
  private Icon[] mInfoIconArr;
  private String[] mInfoIconURLs;
  
  /**
   * Creates an instance of this class.
   * <p>
   * @param dirOrZip The directory with the info icons or a zip file with them.
   */
  public InfoIconTheme(File dirOrZip) {
    mDirOrZip = dirOrZip;
    mID = dirOrZip.getName();
    mIconMap = new HashMap<Byte, ImageIcon>();
    
    loadName(dirOrZip);
  }
  
  private void loadName(File dirOrZip) {
    if(mDirOrZip.isDirectory()) {
      File name = new File(mDirOrZip,"name_" + Locale.getDefault().getLanguage());
      
      if(name.isFile()) {
        try {
          readName(new FileInputStream(name));
        } catch (FileNotFoundException e) {}
      }
      
      if(mName == null) {
        name = new File(mDirOrZip,"name");
        
        try {
          readName(new FileInputStream(name));
        } catch (FileNotFoundException e) {}
      }
    }
    else if(mDirOrZip.isFile() && mDirOrZip.getName().toLowerCase().endsWith(".zip")) {
      try {
        ZipFile iconFile = new ZipFile(dirOrZip);
      
        ZipEntry name = iconFile.getEntry("name_" + Locale.getDefault().getLanguage());
        
        if(name != null) {
          try {
            readName(iconFile.getInputStream(name));
          } catch (FileNotFoundException e) {}
        }
        
        if(mName == null) {
          name = iconFile.getEntry("name");
          
          try {
            readName(iconFile.getInputStream(name));
          } catch (IOException e) {}
        }
      } catch (IOException e1) {
      }
    }
  }
  
  private void readName(InputStream in) {
    try {
      BufferedReader read = new BufferedReader(new InputStreamReader(in, "UTF-8"));
      
      mName = read.readLine();
      
      read.close();
    } catch (IOException e) {
    }
  }
  
  private synchronized void loadIcons() {
    if(mIconMap.isEmpty()) {
      if(mDirOrZip.isDirectory()) {
        loadIconsFromDirectory(mDirOrZip);
      }
      else if(mDirOrZip.isFile() && mDirOrZip.getName().toLowerCase().endsWith(".zip")) {
        loadIconsFromZipFile(mDirOrZip);
      }
    }
  }
  
  private void loadIconsFromDirectory(File dir) {
    File[] files = dir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith("Info_");
      }
    });
    
    for(File icon : files) {
      try {
        URL url = new URL("file:///" + icon.getAbsolutePath().replace("\\", "/"));
        mapIcon(new ImageIcon(url), icon.getName());
      } catch (IOException e) {e.printStackTrace();}
    }
  }
    
  private void loadIconsFromZipFile(File zip) {
    try {
      ZipFile iconFile = new ZipFile(zip);
            
      Enumeration<? extends ZipEntry> entries = iconFile.entries();
      
      while(entries.hasMoreElements()) {
        ZipEntry iconEntry = entries.nextElement();
        
        if(iconEntry.getName().startsWith("Info_")) {
          try {
            URL url = new URL("jar:file:///" + zip.getAbsolutePath().replace("\\", "/") + "!/" + iconEntry.getName());
            mapIcon(new ImageIcon(url), iconEntry.getName());
          } catch (IOException e) {
            e.printStackTrace();
            
          }
        }
      }
      
    } catch (ZipException e) {
    } catch (IOException e) {}
  }
  
  private void mapIcon(ImageIcon icon, String name) {
    if(name.startsWith("Info_16to9")) {
      mIconMap.put(Byte.valueOf(INFO_16_9), icon);
    }
    else if(name.startsWith("Info_Arts")) {
      mIconMap.put(Byte.valueOf(INFO_ARTS), icon);
    }
    else if(name.startsWith("Info_AudioDescription")) {
      mIconMap.put(Byte.valueOf(INFO_AUDIO_DESCRIPTION), icon);
    }
    else if(name.startsWith("Info_BlackAndWhite")) {
      mIconMap.put(Byte.valueOf(INFO_BLACK_AND_WHITE), icon);
    }
    else if(name.startsWith("Info_Children")) {
      mIconMap.put(Byte.valueOf(INFO_CHILDREN), icon);
    }
    else if(name.startsWith("Info_Docu")) {
      mIconMap.put(Byte.valueOf(INFO_DOCU), icon);
    }
    else if(name.startsWith("Info_DolbyDigital51")) {
      mIconMap.put(Byte.valueOf(INFO_DOLBY_DIGITAL_5_1), icon);
    }
    else if(name.startsWith("Info_DolbySurround")) {
      mIconMap.put(Byte.valueOf(INFO_DOLBY_SOURROUND), icon);
    }
    else if(name.startsWith("Info_HasPicture")) {
      mIconMap.put(Byte.valueOf(INFO_HAS_PICTURE), icon);
    }
    else if(name.startsWith("Info_HD")) {
      mIconMap.put(Byte.valueOf(INFO_HD), icon);
    }
    else if(name.startsWith("Info_Infotainment")) {
      mIconMap.put(Byte.valueOf(INFO_INFOTAINMENT), icon);
    }
    else if(name.startsWith("Info_Live")) {
      mIconMap.put(Byte.valueOf(INFO_LIVE), icon);
    }
    else if(name.startsWith("Info_Mono")) {
      mIconMap.put(Byte.valueOf(INFO_MONO), icon);
    }
    else if(name.startsWith("Info_Movie")) {
      mIconMap.put(Byte.valueOf(INFO_MOVIE), icon);
    }
    else if(name.startsWith("Info_New")) {
      mIconMap.put(Byte.valueOf(INFO_NEW), icon);
    }
    else if(name.startsWith("Info_News")) {
      mIconMap.put(Byte.valueOf(INFO_NEWS), icon);
    }
    else if(name.startsWith("Info_OriginalWithSubtitle_EN")) {
      if(Locale.getDefault().getLanguage().equals("en")) {
        mIconMap.put(Byte.valueOf(INFO_ORIGINAL_WITH_SUBTITLE), icon);
      }
    }
    else if(name.startsWith("Info_OriginalWithSubtitle")) {
      if(mIconMap.get(Byte.valueOf(INFO_ORIGINAL_WITH_SUBTITLE)) == null) {
        mIconMap.put(Byte.valueOf(INFO_ORIGINAL_WITH_SUBTITLE), icon);
      }
    }
    else if(name.startsWith("Info_Series")) {
      mIconMap.put(Byte.valueOf(INFO_SERIES), icon);
    }
    else if(name.startsWith("Info_Show")) {
      mIconMap.put(Byte.valueOf(INFO_SHOW), icon);
    }
    else if(name.startsWith("Info_Signlanguage")) {
      mIconMap.put(Byte.valueOf(INFO_SIGN_LANGUGAGE), icon);
    }
    else if(name.startsWith("Info_Sports")) {
      mIconMap.put(Byte.valueOf(INFO_SPORTS), icon);
    }
    else if(name.startsWith("Info_Stereo")) {
      mIconMap.put(Byte.valueOf(INFO_STEREO), icon);
    }
    else if(name.startsWith("Info_SubtitleForAurallyHandicapped_DK")) {
      if(Locale.getDefault().getCountry().equalsIgnoreCase("dk")) {
        mIconMap.put(Byte.valueOf(INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED), icon);
      }
    }
    else if(name.startsWith("Info_SubtitleForAurallyHandicapped_US")) {
      if(Locale.getDefault().getCountry().equalsIgnoreCase("us")) {
        mIconMap.put(Byte.valueOf(INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED), icon);
      }
    }
    else if(name.startsWith("Info_SubtitleForAurallyHandicapped")) {
      if(mIconMap.get(Byte.valueOf(INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED)) == null) {
        mIconMap.put(Byte.valueOf(INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED), icon);
      }
    }
    else if(name.startsWith("Info_TwoChannelTone")) {
      mIconMap.put(Byte.valueOf(INFO_TWO_CHANNEL_TONE), icon);
    }
    else if(name.startsWith("Info_4to3")) {
      mIconMap.put(Byte.valueOf(INFO_4_3), icon);
    }
    else if(name.startsWith("Others")) {
      mIconMap.put(Byte.valueOf(INFO_OTHERS), icon);
    }
  }
  
  /**
   * Gets the icon for the given type.
   * <p>
   * @param type The type of the icon to get.
   * @return The icon for the type or <code>null</code> if no icon for given type available.
   */
  public ImageIcon getInfoIcon(byte type) {
    loadIcons();
    
    return mIconMap.get(Byte.valueOf(type));
  }
  
  /**
   * Gets the ID of this info icon theme.
   * <p>
   * @return The ID of this info icon theme.
   */
  public String getID() {
    return mID;
  }
  
  @Override
  public String toString() {
    return mName;
  }
  
  public Icon[] getInfoIcons() {
    loadIcons();
    
    if(mInfoIconArr == null) {
      mInfoIconArr = new Icon[25];
      
      mInfoIconArr[0] = mIconMap.get(Byte.valueOf(INFO_BLACK_AND_WHITE));
      mInfoIconArr[1] = mIconMap.get(Byte.valueOf(INFO_4_3));
      mInfoIconArr[2] = mIconMap.get(Byte.valueOf(INFO_16_9));
      mInfoIconArr[3] = mIconMap.get(Byte.valueOf(INFO_MONO));
      mInfoIconArr[4] = mIconMap.get(Byte.valueOf(INFO_STEREO));
      mInfoIconArr[5] = mIconMap.get(Byte.valueOf(INFO_DOLBY_SOURROUND));
      mInfoIconArr[6] = mIconMap.get(Byte.valueOf(INFO_DOLBY_DIGITAL_5_1));
      mInfoIconArr[7] = mIconMap.get(Byte.valueOf(INFO_TWO_CHANNEL_TONE));
      mInfoIconArr[8] = mIconMap.get(Byte.valueOf(INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED));
      mInfoIconArr[9] = mIconMap.get(Byte.valueOf(INFO_LIVE));
      mInfoIconArr[10] = mIconMap.get(Byte.valueOf(INFO_ORIGINAL_WITH_SUBTITLE));
      mInfoIconArr[11] = mIconMap.get(Byte.valueOf(INFO_NEW));
      mInfoIconArr[12] = mIconMap.get(Byte.valueOf(INFO_AUDIO_DESCRIPTION));
      mInfoIconArr[13] = mIconMap.get(Byte.valueOf(INFO_HD));
      mInfoIconArr[14] = mIconMap.get(Byte.valueOf(INFO_MOVIE));
      mInfoIconArr[15] = mIconMap.get(Byte.valueOf(INFO_SERIES));
      mInfoIconArr[16] = mIconMap.get(Byte.valueOf(INFO_NEWS));
      mInfoIconArr[17] = mIconMap.get(Byte.valueOf(INFO_SHOW));
      mInfoIconArr[18] = mIconMap.get(Byte.valueOf(INFO_INFOTAINMENT));
      mInfoIconArr[19] = mIconMap.get(Byte.valueOf(INFO_DOCU));
      mInfoIconArr[20] = mIconMap.get(Byte.valueOf(INFO_ARTS));
      mInfoIconArr[21] = mIconMap.get(Byte.valueOf(INFO_SPORTS));
      mInfoIconArr[22] = mIconMap.get(Byte.valueOf(INFO_CHILDREN));
      mInfoIconArr[23] = mIconMap.get(Byte.valueOf(INFO_OTHERS));
      mInfoIconArr[24] = mIconMap.get(Byte.valueOf(INFO_SIGN_LANGUGAGE));
    }
    
    return mInfoIconArr;
  }
  
  private String getDescriptionForIconType(byte type) {
    ImageIcon icon = mIconMap.get(Byte.valueOf(type));
    
    if(icon != null) {
      return icon.getDescription();
    }
    
    return null;
  }
  
  public String[] getInfoIconURLs() {
    loadIcons();
    
    if(mInfoIconURLs == null) {
      mInfoIconURLs = new String[25];
      
      mInfoIconURLs[0] = getDescriptionForIconType(INFO_BLACK_AND_WHITE);
      mInfoIconURLs[1] = getDescriptionForIconType(INFO_4_3);
      mInfoIconURLs[2] = getDescriptionForIconType(INFO_16_9);
      mInfoIconURLs[3] = getDescriptionForIconType(INFO_MONO);
      mInfoIconURLs[4] = getDescriptionForIconType(INFO_STEREO);
      mInfoIconURLs[5] = getDescriptionForIconType(INFO_DOLBY_SOURROUND);
      mInfoIconURLs[6] = getDescriptionForIconType(INFO_DOLBY_DIGITAL_5_1);
      mInfoIconURLs[7] = getDescriptionForIconType(INFO_TWO_CHANNEL_TONE);
      mInfoIconURLs[8] = getDescriptionForIconType(INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED);
      mInfoIconURLs[9] = getDescriptionForIconType(INFO_LIVE);
      mInfoIconURLs[10] = getDescriptionForIconType(INFO_ORIGINAL_WITH_SUBTITLE);
      mInfoIconURLs[11] = getDescriptionForIconType(INFO_NEW);
      mInfoIconURLs[12] = getDescriptionForIconType(INFO_AUDIO_DESCRIPTION);
      mInfoIconURLs[13] = getDescriptionForIconType(INFO_HD);
      mInfoIconURLs[14] = getDescriptionForIconType(INFO_MOVIE);
      mInfoIconURLs[15] = getDescriptionForIconType(INFO_SERIES);
      mInfoIconURLs[16] = getDescriptionForIconType(INFO_NEWS);
      mInfoIconURLs[17] = getDescriptionForIconType(INFO_SHOW);
      mInfoIconURLs[18] = getDescriptionForIconType(INFO_INFOTAINMENT);
      mInfoIconURLs[19] = getDescriptionForIconType(INFO_DOCU);
      mInfoIconURLs[20] = getDescriptionForIconType(INFO_ARTS);
      mInfoIconURLs[21] = getDescriptionForIconType(INFO_SPORTS);
      mInfoIconURLs[22] = getDescriptionForIconType(INFO_CHILDREN);
      mInfoIconURLs[23] = getDescriptionForIconType(INFO_OTHERS);
      mInfoIconURLs[24] = getDescriptionForIconType(INFO_SIGN_LANGUGAGE);
    }
    
    return mInfoIconURLs;
  }

  @Override
  public int compareTo(InfoIconTheme o) {
    return (mName != null && o.mName != null) ? mName.compareToIgnoreCase(o.mName) : mID.compareToIgnoreCase(o.mID);
  }
}
