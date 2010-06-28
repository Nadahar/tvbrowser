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
package tvbrowserdataservice.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

import util.io.DownloadJob;
import util.io.FileFormatException;
import util.io.IOUtilities;
import devplugin.Date;

/**
 * The summary file gives an overview over the files located on a mirror and
 * their versions.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class SummaryFile extends AbstractFile {

  /** The default file name for a summary file. */
  public static final String SUMMARY_FILE_NAME = "summary.gz";
  /** The version of the file format. */
  private static final int FILE_VERSION = 1;
  /** The charset used to encode Strings. */
  private static final String TEXT_CHARSET = "UTF-8";
  
  /**
   * The hash containings the ChannelFrames.
   * (key = String (See {@link #getChannelKey(String, String)}),
   *  value = ChannelFrame)
   */
  private HashMap<String, ChannelFrame> mChannelFrameHash;
  /** The number of levels used in this summary. */
  private int mLevelCount;
  
  
  /**
   * Creates a new instance.
   */
  public SummaryFile() {
    mChannelFrameHash = new HashMap<String, ChannelFrame>();
    mLevelCount = 0;
  }
  
  
  /**
   * Sets the version of a day program.
   * 
   * @param date The date of the day program.
   * @param country The country of the day program's channel.
   * @param channelId The ID of the day program's channel.
   * @param level The level to set the version for.
   * @param version The version to set.
   */
  public void setDayProgramVersion(Date date, String country, String channelId,
    int level, int version)
  {
    if (version < 0) {
      // Unknown version
      version = 0;
    }
    
    // Update level count
    if (level >= mLevelCount) {
      mLevelCount = (level + 1);
    }
    
    // Try to get a frame from the hash
    String key = getChannelKey(country, channelId);
    ChannelFrame frame = mChannelFrameHash.get(key);
    if (frame == null) {
      // There is no frame -> create one
      frame = new ChannelFrame(country, channelId);
			System.out.println("creating frame for "+country+", "+channelId);
      mChannelFrameHash.put(key, frame);
    }
    
    // Set the version
    frame.setVersion(date, level, version);
  }
  
  
  /**
   * Gets the version of a day program.
   * 
   * @param date The date of the day program.
   * @param country The country of the day program's channel.
   * @param channelId The ID of the day program's channel.
   * @param level The level to get the version for.
   * @return The version of the day program or <code>-1</code> if there is no
   *         version for that day program and level.
   */
  public int getDayProgramVersion(Date date, String country, String channelId,
    int level)
  {
    if (level >= mLevelCount) {
      // We have no data for that level
      return -1;
    }
    
    // Try to get a frame from the hash
    String key = getChannelKey(country, channelId);
    ChannelFrame frame = mChannelFrameHash.get(key);
    
    if (frame == null) {
      // We have no data for that channel
      return -1;
    } else {
      int version = frame.getVersion(date, level);
      if (version == 0) {
        return -1;
      } else {
        return version;
      }
    }
  }

  
  /**
   * Gets the hash key for a day program.
   * 
   * @param country The country of the day program's channel.
   * @param channelId The ID of the day program's channel.
   * @return The hash key.
   */
  private String getChannelKey(String country, String channelId) {
    return new StringBuilder(country).append('_').append(channelId).toString();
  }


  /**
   * Reads the summary from a stream.
   * 
   * @param stream The stream to read the summary from.
   */
  public void readFromStream(InputStream stream, DownloadJob job)
    throws IOException, FileFormatException
  {
    InputStream gIn = IOUtilities.openSaveGZipInputStream(stream);
    
    // The header
    int fileVersion = gIn.read();
    if (fileVersion > FILE_VERSION) {
      throw new FileFormatException("Unknown file version: " + fileVersion);
    }
    
    int startDaysSince1970 = ((gIn.read() & 0xFF) << 16)
                           | ((gIn.read() & 0xFF) << 8)
                           |  (gIn.read() & 0xFF);
    
    mLevelCount = gIn.read();

    int frameCount = ((gIn.read() & 0xFF) << 8)
                   |  (gIn.read() & 0xFF);

    // The frames
    mChannelFrameHash.clear();
    Date startDate = generateDate(startDaysSince1970);
    for (int frameIdx = 0; frameIdx < frameCount; frameIdx++) {
      String country = readString(gIn);
      String channelId = readString(gIn);
      ChannelFrame frame = new ChannelFrame(country, channelId);
      String key = getChannelKey(country, channelId);
      mChannelFrameHash.put(key, frame);
      
      
      int daysCount = gIn.read();
      Date date = startDate;
      for (int i = 0; i < daysCount; i++) {
        byte[] versionArr = IOUtilities.readBinaryData(gIn, mLevelCount);
        frame.setVersionArray(date, versionArr);
        date = date.addDays(1);
      }
    }
    
    gIn.close();
  }

  /**
   * Generates a Date.
   * The Constructor of Date is deprecated, it has some troubles with OS/2, but in this
   * class we only use it to compute the difference between two days. Only for this purpose
   * it's ok.
   * 
   * @param daysSince1970 Days since 1970
   * @return Date-Object
   */
  private Date generateDate(int daysSince1970) {
      long l = (long) daysSince1970 * 24 * 60 * 60 * 1000;
      java.util.Date d = new java.util.Date(l);
      Calendar cal = Calendar.getInstance();
      cal.setTime(d);
      
      int year = cal.get(Calendar.YEAR);
      int month = cal.get(Calendar.MONTH) + 1;
      int day = cal.get(Calendar.DAY_OF_MONTH);
      
      return new Date(year, month, day);
  }
  
  
  /**
   * Writes the summary into a stream.
   * 
   * @param stream The stream to write the summary to.
   */
  public void writeToStream(OutputStream stream, File fileName)
    throws IOException, FileFormatException
  {
    GZIPOutputStream gOut = new GZIPOutputStream(stream);

		
		
    gOut.write(FILE_VERSION);

		System.out.println("get the minimum start date...");
    
    // Get the minimum start date
    int minStartDaysSince1970 = Integer.MAX_VALUE;
    Iterator<ChannelFrame> iter = mChannelFrameHash.values().iterator();
    while (iter.hasNext()) {
      ChannelFrame frame = iter.next();
      int startDaysSince1970 = frame.getStartDaysSince1970();
      if (startDaysSince1970 < minStartDaysSince1970) {
        minStartDaysSince1970 = startDaysSince1970;
      }
    }
    
    gOut.write((byte) (minStartDaysSince1970 >> 16));
    gOut.write((byte) (minStartDaysSince1970 >> 8));
    gOut.write((byte) (minStartDaysSince1970));
    
    gOut.write((byte) mLevelCount);

    int frameCount = mChannelFrameHash.size();
    gOut.write((byte) (frameCount >> 8));
    gOut.write((byte) (frameCount));

    System.out.println("write frames...");
    System.out.println("frameCount: "+frameCount);
		
    // The frames
    Date startDate = generateDate(minStartDaysSince1970);
		System.out.println("minStartDaysSince1970: "+minStartDaysSince1970);
    iter = mChannelFrameHash.values().iterator();
    while (iter.hasNext()) {
      ChannelFrame frame = iter.next();
      
      
      System.out.println(frame.getChannelId()+", "+frame.getStartDaysSince1970()+", "+frame.getDaysCount(minStartDaysSince1970));
			
      writeString(gOut, frame.getCountry());
      writeString(gOut, frame.getChannelId());
      
      int daysCount = frame.getDaysCount(minStartDaysSince1970);
      gOut.write((byte) daysCount);
      
      Date date = startDate;
      for (int i = 0; i < daysCount; i++) {
        byte[] versionArr = frame.getVersionArray(date);
        gOut.write(versionArr);
        date = date.addDays(1);
      }
    }
        
    gOut.close();
  }


  /**
   * Reads a String from a stream.
   * 
   * @param stream The stream to read from.
   * @return The String.
   */
  private String readString(InputStream stream)
    throws IOException, FileFormatException
  {
    int len = stream.read();
    if (len == -1) {
      throw new FileFormatException("Unexpected end of stream");
    }
    
    byte[] data = IOUtilities.readBinaryData(stream, len);
    return new String(data, TEXT_CHARSET);
  }


  /**
   * Writes a String into a stream.
   * 
   * @param stream The stream to write to.
   * @param str The String to write.
   */
  private void writeString(OutputStream stream, String str)
    throws IOException, FileFormatException
  {
    byte[] data;
    try {
      data = str.getBytes(TEXT_CHARSET);
    }
    catch (UnsupportedEncodingException exc) {
      throw new FileFormatException("Encoding not supported: " + TEXT_CHARSET, exc);
    }
    
    stream.write((byte) data.length);
    stream.write(data);
  }


  /**
   * Gets the days since 1.1.1970 for the given date.
   * 
   * @param date The date to calculate the days since 1970 for.
   * @return The days since 1970.
   */
  private int getDaysSince1970(Date date) {
    Calendar cal = date.getCalendar();
    int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
    int daylight = cal.get(Calendar.DST_OFFSET);
    java.util.Date utilDate = cal.getTime();
    long millis = utilDate.getTime() + zoneOffset + daylight;
    return (int) (millis / 1000L / 60L / 60L / 24L);
  }
  
  
  /**
   * A channel frame of a summary file. Holds all file versions for one channel.
   */
  private class ChannelFrame {
    
    /** The channel's country. */
    private String mCountry;
    /** The channel's ID. */
    private String mChannelId;
    /** The date (in days since 1970) the first version array is for. */
    private int mStartDaysSince1970;
    /**
     * The list of version arrays. Each version array contains the versions
     * for all levels. (<code>versionForLevel1 = versionArr[1]</code>)
     */
    private ArrayList<byte[]> mVersionList;

    
    /**
     * Creates a new instance.
     * 
     * @param country The channel's country.
     * @param channelId The channel's ID.
     */
    public ChannelFrame(String country, String channelId) {
      mCountry = country;
      mChannelId = channelId;
    }
    
    
    /**
     * Gets the channel's country.
     * 
     * @return The channel's country.
     */
    public String getCountry() {
      return mCountry;
    }
    
    
    /**
     * Gets the channel's ID.
     * 
     * @return The channel's ID.
     */
    public String getChannelId() {
      return mChannelId;
    }
    
    
    /**
     * Gets the date (in days since 1970) the first version array is for.
     * 
     * @return The date the first version array is for.
     */
    public int getStartDaysSince1970() {
      return mStartDaysSince1970;
    }
    
    
    /**
     * Gets the number of days this frame has versions for since the starting
     * date.
     * 
     * @param startDays The day to count from (in days after 1.1.1970)
     * @return The number of days this frame has versions for
     */
    public int getDaysCount(int startDays) {
      // NOTE: mStartDaysSince1970 may be after startDays
      return mVersionList.size() + (mStartDaysSince1970 - startDays);
    }


    /**
     * Gets the version array for a particular date.
     * <p>
     * The returned array has a length of {@link #mLevelCount}.
     * 
     * @param date The date to get the version array for.
     * @return The version array for a particular date.
     */
    public byte[] getVersionArray(Date date) {
      // Try to get the version array
      byte[] versionArr = null;
      if (mVersionList != null) {
        int dateDaysSince1970 = getDaysSince1970(date);
        int differenceDays = dateDaysSince1970 - mStartDaysSince1970;
        if ((differenceDays >= 0) && (differenceDays < mVersionList.size())) {
          versionArr = mVersionList.get(differenceDays);
        }
      }
      
      // Verify the version array
      if (versionArr == null) {
        // We have no version array -> Create one
        versionArr = new byte[mLevelCount];
      }
      else if (versionArr.length < mLevelCount) {
        // The current versionArr is too short -> Create a bigger one
        byte[] oldVersionArr = versionArr;
        versionArr = new byte[mLevelCount];
          
        System.arraycopy(oldVersionArr, 0, versionArr, 0, oldVersionArr.length);
      }
      
      return versionArr;
    }
    
    
    /**
     * Sets the version array for a particular date.
     * 
     * @param date The date to set the version array for.
     * @param versionArr The version array to set.
     */
    public void setVersionArray(Date date, byte[] versionArr) {
      int dateDaysSince1970 = getDaysSince1970(date);

      // Check whether we already have versions
      if (mVersionList == null) {
        // We have no versions
        mStartDaysSince1970 = dateDaysSince1970;
        mVersionList = new ArrayList<byte[]>();
        mVersionList.add(versionArr);
      } else {
        // We already have versions

        // Check whether the date fits
        int differenceDays = dateDaysSince1970 - mStartDaysSince1970;
        if (differenceDays < 0) {
          // We have to insert empty version arrays
          int daysToInsert = differenceDays * -1;
          for (int i = 0; i < daysToInsert; i++) {
            mVersionList.add(0, null);
          }
            
          mStartDaysSince1970 = dateDaysSince1970;
          differenceDays = 0;
        }
        else if (differenceDays >= mVersionList.size()) {
          // We have to append empty version arrays
          int daysToAppend = differenceDays - mVersionList.size() + 1;
          for (int i = 0; i < daysToAppend; i++) {
            mVersionList.add(null);
          }
        }
  
        // Set the version array
        mVersionList.set(differenceDays, versionArr);
      }
    }


    /**
     * Gets the version for a date and level.
     * <p>
     * If the version is unkown <code>0</code> is returned.
     * 
     * @param date The date to get the version for.
     * @param level The level to get the version for.
     * @return The version for a date and level.
     */
    public int getVersion(Date date, int level) {
      // Get the version array
      byte[] versionArr = getVersionArray(date);

      // Get the version
      return versionArr[level];
    }
    

    /**
     * Sets the version for a date and level.
     * 
     * @param date The date to set the version for.
     * @param level The level to set the version for.
     * @param version The version to set.
     */
    public void setVersion(Date date, int level, int version) {
      // Get the version array
      byte[] versionArr = getVersionArray(date);

      // Set the version
      versionArr[level] = (byte) version;
      
      // Set the version array
      setVersionArray(date, versionArr);
    }
    
  } // class ChannelFrame

}
